package techguns.transport;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import techguns.core.transport.SideMode;
import techguns.core.transport.ZoneGraph;

/**
 * The pipe networks of one server level. Pipe block entities register here when they load; any change marks positions
 * dirty and their networks are rebuilt at the next use or tick. Networks are never saved: they are rebuilt from the
 * pipes after loading. Also writes the computed connections back into the segments (for the model and the shape).
 */
public final class TransportManager {
    private static final Map<ServerLevel, TransportManager> MANAGERS = new WeakHashMap<>();

    private final ServerLevel level;
    private final Map<Long, TGPipeBlockEntity> pipes = new HashMap<>();
    private final Map<PipeKind, KindNetworks> kinds = new HashMap<>();
    /** Unloaded positions next to pipes, by the pipe: when they load, an endpoint may appear there. */
    private final Map<Long, Long> waitingForLoad = new HashMap<>();
    private int nextId;

    private TransportManager(ServerLevel level) {
        this.level = level;
        for (PipeKind kind : TGPipeKinds.ALL) {
            kinds.put(kind, new KindNetworks());
        }
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener((LevelTickEvent.Post event) -> {
            if (event.getLevel() instanceof ServerLevel server) {
                TransportManager manager = MANAGERS.get(server);
                if (manager != null) {
                    manager.tick();
                }
            }
        });
        gameBus.addListener((ServerStoppedEvent event) -> MANAGERS.clear());
    }

    public static TransportManager get(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, TransportManager::new);
    }

    private static final class KindNetworks {
        final Map<Long, PipeNetwork> byPos = new HashMap<>();
        final Set<PipeNetwork> networks = new LinkedHashSet<>();
        final Set<Long> dirty = new HashSet<>();
    }

    void add(TGPipeBlockEntity pipe) {
        pipes.put(pipe.getBlockPos().asLong(), pipe);
        markDirty(pipe.getBlockPos());
    }

    void remove(TGPipeBlockEntity pipe) {
        long pos = pipe.getBlockPos().asLong();
        if (pipes.get(pos) == pipe) {
            pipes.remove(pos);
        }
        markDirty(pipe.getBlockPos());
    }

    /** Something at or next to {@code pos} changed: rebuild the networks there, of every kind. */
    public void markDirty(BlockPos pos) {
        for (KindNetworks k : kinds.values()) {
            k.dirty.add(pos.asLong());
        }
    }

    /** The network of {@code kind} through the pipe at {@code pos}, rebuilt first if needed. */
    @Nullable
    public PipeNetwork network(BlockPos pos, PipeKind kind) {
        rebuild(kind);
        return kinds.get(kind).byPos.get(pos.asLong());
    }

    public List<PipeNetwork> networks(PipeKind kind) {
        rebuild(kind);
        return new ArrayList<>(kinds.get(kind).networks);
    }

    private void tick() {
        long time = level.getGameTime();
        if (!waitingForLoad.isEmpty() && time % 20 == 0) {
            waitingForLoad.entrySet().removeIf(entry -> {
                if (level.isLoaded(BlockPos.of(entry.getKey()))) {
                    markDirty(BlockPos.of(entry.getValue()));
                    return true;
                }
                return false;
            });
        }
        for (PipeKind kind : TGPipeKinds.ALL) {
            rebuild(kind);
            for (PipeNetwork network : new ArrayList<>(kinds.get(kind).networks)) {
                kind.logic().tick(network, level, time);
            }
        }
    }

    private void rebuild(PipeKind kind) {
        KindNetworks k = kinds.get(kind);
        if (k.dirty.isEmpty()) {
            return;
        }
        Set<Long> visit = new LinkedHashSet<>();
        for (long pos : k.dirty) {
            visit.add(pos);
            // a change next to a pipe matters too (a block added or removed, a neighbouring pipe's side mode)
            BlockPos p = BlockPos.of(pos);
            for (Direction d : Direction.values()) {
                visit.add(p.relative(d).asLong());
            }
        }
        k.dirty.clear();
        for (long pos : new ArrayList<>(visit)) {
            PipeNetwork old = k.byPos.get(pos);
            if (old != null && k.networks.remove(old)) {
                for (long member : old.members()) {
                    k.byPos.remove(member);
                    visit.add(member);
                }
            }
        }
        for (long pos : visit) {
            if (!k.byPos.containsKey(pos)) {
                TGPipeBlockEntity pipe = pipes.get(pos);
                if (pipe != null && !pipe.isRemoved() && pipe.segment(kind) != null) {
                    build(kind, k, pipe);
                }
            }
        }
    }

    /** Collects the network of {@code kind} through {@code start}: members, endpoints, zones; updates the segments. */
    private void build(PipeKind kind, KindNetworks k, TGPipeBlockEntity start) {
        Set<Long> members = new LinkedHashSet<>();
        Map<Long, long[]> links = new HashMap<>();
        List<PendingEndpoint> pending = new ArrayList<>();
        ArrayDeque<TGPipeBlockEntity> queue = new ArrayDeque<>();
        queue.add(start);
        members.add(start.getBlockPos().asLong());
        while (!queue.isEmpty()) {
            TGPipeBlockEntity pipe = queue.poll();
            PipeSegment segment = pipe.segment(kind);
            BlockPos pos = pipe.getBlockPos();
            List<Long> linked = new ArrayList<>();
            int linkBits = 0;
            int endBits = 0;
            int adapterBits = 0;
            techguns.core.transport.Lane lane = pipe.drawnLane(kind);
            for (Direction side : Direction.values()) {
                SideMode mode = segment.mode(side);
                BlockPos next = pos.relative(side);
                if (!mode.connects()) {
                    continue;
                }
                if (!level.isLoaded(next)) {
                    waitingForLoad.put(next.asLong(), pos.asLong());
                    continue;
                }
                TGPipeBlockEntity other = pipes.get(next.asLong());
                if (other != null && !other.isRemoved()) {
                    // pipe blocks are never endpoints; link only to the same kind connected on its side as well
                    PipeSegment otherSegment = other.segment(kind);
                    if (otherSegment != null && otherSegment.mode(side.getOpposite()).connects()) {
                        linked.add(next.asLong());
                        linkBits |= 1 << side.get3DDataValue();
                        if (lane != techguns.core.transport.Lane.B && other.drawnLane(kind) != lane) {
                            adapterBits |= 1 << side.get3DDataValue();
                        }
                        if (members.add(next.asLong())) {
                            queue.add(other);
                        }
                    }
                    continue;
                }
                Object capability = level.getCapability(kind.capability(), next, level.getBlockState(next), null, side.getOpposite());
                if (capability != null && kind.logic().isEndpoint(capability)) {
                    endBits |= 1 << side.get3DDataValue();
                    pending.add(new PendingEndpoint(pos, side, mode, segment.tier()));
                }
            }
            links.put(pos.asLong(), linked.stream().mapToLong(Long::longValue).toArray());
            pipe.applyConnections(kind, linkBits, endBits, adapterBits);
        }
        long[] nodes = members.stream().mapToLong(Long::longValue).toArray();
        ZoneGraph graph = new ZoneGraph(nodes, pos -> pipes.get(pos).segment(kind).tier(), pos -> links.getOrDefault(pos, new long[0]));
        List<PipeNetwork.Endpoint> endpoints = new ArrayList<>();
        for (PendingEndpoint e : pending) {
            BlockPos target = e.pos.relative(e.side);
            endpoints.add(new PipeNetwork.Endpoint(e.pos, e.side, target, e.mode, graph.zoneAt(e.pos.asLong()), e.tier,
                    cache(kind.capability(), target, e.side.getOpposite())));
        }
        PipeNetwork network = new PipeNetwork(kind, nextId++, members, graph, endpoints);
        k.networks.add(network);
        for (long member : members) {
            k.byPos.put(member, network);
        }
    }

    private <T> BlockCapabilityCache<T, Direction> cache(BlockCapability<T, Direction> capability, BlockPos pos, Direction side) {
        // the cache is valid while the network is; it re-fetches when the block's capabilities change
        return BlockCapabilityCache.create(capability, level, pos, side);
    }

    private record PendingEndpoint(BlockPos pos, Direction side, SideMode mode, int tier) {
    }
}
