package techguns.transport;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import techguns.core.transport.SideMode;
import techguns.core.transport.ZoneGraph;

/**
 * Connected pipes of one kind (tiers may differ), built by {@link TransportManager} and dropped whenever a member or its
 * surroundings change. Holds the endpoints (blocks with the kind's capability next to a member), the zone graph and the
 * flow budgets of the current tick; nothing that is moved is ever stored here.
 */
public final class PipeNetwork {
    /**
     * A block next to a member pipe. {@code target} is the block, {@code side} the pipe side facing it.
     */
    public record Endpoint(BlockPos pipePos, Direction side, BlockPos target, SideMode mode, int zone, int tier,
                           BlockCapabilityCache<?, Direction> cache) {
        @Nullable
        public Object capability() {
            return cache.getCapability();
        }
    }

    private final PipeKind kind;
    private final int id;
    private final Set<Long> members;
    private final ZoneGraph graph;
    private final List<Endpoint> endpoints;
    private final Map<Long, Integer> endpointIndex = new HashMap<>();
    private final int[] zoneBudget;
    private final int[] endpointBudget;
    private long budgetTick = Long.MIN_VALUE;
    /** Per pipe position: endpoint indices by distance from it (nearest first). */
    private final Map<Long, int[]> targetOrder = new HashMap<>();
    /** Per source endpoint: next target (round robin) and next slot (items). */
    private final int[] targetCursor;
    private final int[] slotCursor;
    /** A transfer is running: a target pushing back into this network is refused. */
    boolean routing;

    PipeNetwork(PipeKind kind, int id, Set<Long> members, ZoneGraph graph, List<Endpoint> endpoints) {
        this.kind = kind;
        this.id = id;
        this.members = members;
        this.graph = graph;
        this.endpoints = endpoints;
        for (int i = 0; i < endpoints.size(); i++) {
            endpointIndex.put(key(endpoints.get(i).pipePos(), endpoints.get(i).side()), i);
        }
        this.zoneBudget = new int[graph.zoneCount()];
        this.endpointBudget = new int[endpoints.size()];
        this.targetCursor = new int[endpoints.size()];
        this.slotCursor = new int[endpoints.size()];
    }

    private static long key(BlockPos pos, Direction side) {
        return pos.asLong() * 8 + side.ordinal();
    }

    public PipeKind kind() {
        return kind;
    }

    public int id() {
        return id;
    }

    public Set<Long> members() {
        return members;
    }

    public ZoneGraph graph() {
        return graph;
    }

    public List<Endpoint> endpoints() {
        return endpoints;
    }

    /** Index of the endpoint at the pipe {@code pos}, {@code side}, or -1. */
    public int endpointAt(BlockPos pos, Direction side) {
        return endpointIndex.getOrDefault(key(pos, side), -1);
    }

    /** Restores the budgets at the start of a tick (called by every use, so it works for pushes before the tick too). */
    public void refreshBudgets(long gameTime) {
        if (budgetTick == gameTime) {
            return;
        }
        budgetTick = gameTime;
        for (int z = 0; z < zoneBudget.length; z++) {
            zoneBudget[z] = kind.rate(graph.zoneTier(z));
        }
        for (int i = 0; i < endpointBudget.length; i++) {
            endpointBudget[i] = kind.rate(endpoints.get(i).tier());
        }
    }

    /** The live budgets, or copies of them for a simulated transfer. */
    public Budgets budgets(boolean simulate) {
        return simulate ? new Budgets(zoneBudget.clone(), endpointBudget.clone()) : new Budgets(zoneBudget, endpointBudget);
    }

    /** Endpoint indices ordered by distance from the pipe at {@code from} (nearest first, then by side). */
    public int[] targetsByDistance(BlockPos from) {
        return targetOrder.computeIfAbsent(from.asLong(), pos -> {
            Map<Long, Integer> dist = graph.distancesFrom(pos);
            return IntStream.range(0, endpoints.size()).boxed()
                    .sorted(Comparator.<Integer>comparingInt(i -> dist.getOrDefault(endpoints.get(i).pipePos().asLong(), Integer.MAX_VALUE))
                            .thenComparingInt(i -> endpoints.get(i).side().ordinal()))
                    .mapToInt(Integer::intValue).toArray();
        });
    }

    int targetCursor(int source) {
        return targetCursor[source];
    }

    void setTargetCursor(int source, int value) {
        targetCursor[source] = value;
    }

    int slotCursor(int source) {
        return slotCursor[source];
    }

    void setSlotCursor(int source, int value) {
        slotCursor[source] = value;
    }

    /** Whether {@code target} may receive what comes from {@code source} (not the same block, delivering side). */
    public boolean isTarget(int source, int target) {
        Endpoint t = endpoints.get(target);
        return target != source && t.mode().delivers() && !t.target().equals(endpoints.get(source).target());
    }

    @Override
    public String toString() {
        return kind + " network " + id + " " + members.size() + " pipes, " + endpoints.size() + " endpoints, zones "
                + Arrays.toString(IntStream.range(0, graph.zoneCount()).map(graph::zoneTier).toArray());
    }

    /** Remaining flow of the zones and endpoint sides in this tick. */
    public record Budgets(int[] zones, int[] endpoints) {
        /** The widest zone path from the source endpoint's zone to the target's, under these budgets. */
        public int[] path(PipeNetwork network, int source, int target) {
            return network.graph.widestPath(network.endpoints.get(source).zone(), network.endpoints.get(target).zone(), zones);
        }

        /** What may still flow from {@code source} to {@code target} along {@code path}. */
        public int limit(int[] path, int source, int target) {
            return Math.min(ZoneGraph.bottleneck(path, zones), Math.min(endpoints[source], endpoints[target]));
        }

        public void use(int[] path, int source, int target, int amount) {
            ZoneGraph.use(path, zones, amount);
            endpoints[source] = Math.max(0, endpoints[source] - amount);
            endpoints[target] = Math.max(0, endpoints[target] - amount);
        }
    }
}
