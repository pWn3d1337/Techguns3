package techguns.machine;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import techguns.TGConfig;
import techguns.core.power.SolarOutput;
import techguns.core.transport.EnergySplit;

/**
 * Solar panels side by side form an array (an implicit multiblock): one shared energy pool, so power moves between the
 * panels without limit, drained through any panel and pushed into the blocks next to the array.
 *
 * <p>Built for large fields: panels do not tick, the level's {@link Manager} ticks each array once. A placed panel joins
 * the array next to it in constant time; only removing a panel or joining two arrays rebuilds (a search over the
 * panels). Per-panel work (sky check, finding machines and cables next to it, marking the chunk for saving) runs on a
 * twentieth of the panels each tick; output comes from a running count of panels under open sky. Dissolving an array
 * hands the pool back to the panels, so nothing is lost when chunks unload.
 */
public final class SolarArray implements IEnergyStorage {
    /** Every panel's sky view and neighbours are looked at once in this many ticks. */
    static final int SLICE_TICKS = 20;

    private final ServerLevel level;
    private final List<SolarPanelBlockEntity> members = new ArrayList<>();
    private final Set<BlockCapabilityCache<IEnergyStorage, Direction>> targets = new LinkedHashSet<>();
    private int energy;
    private int skyCount;
    private int cursor;
    private final double[] remainder = {0.0};
    private boolean valid = true;
    private double lastPerTick;

    private SolarArray(ServerLevel level) {
        this.level = level;
    }

    // --- manager: one per level, ticks the arrays and builds them for new panels ---

    public static void init(IEventBus gameBus) {
        gameBus.addListener((LevelTickEvent.Post event) -> {
            if (event.getLevel() instanceof ServerLevel server) {
                Manager manager = Manager.MANAGERS.get(server);
                if (manager != null) {
                    manager.tick();
                }
            }
        });
        gameBus.addListener((ServerStoppedEvent event) -> Manager.MANAGERS.clear());
    }

    static Manager manager(ServerLevel level) {
        return Manager.MANAGERS.computeIfAbsent(level, Manager::new);
    }

    static final class Manager {
        private static final Map<ServerLevel, Manager> MANAGERS = new WeakHashMap<>();

        private final ServerLevel level;
        private final Set<SolarPanelBlockEntity> pending = new LinkedHashSet<>();
        private final Set<SolarArray> arrays = new LinkedHashSet<>();

        private Manager(ServerLevel level) {
            this.level = level;
        }

        /** A panel loaded or was placed: it joins the one array next to it, or arrays are rebuilt. */
        void added(SolarPanelBlockEntity panel) {
            Set<SolarArray> around = new HashSet<>();
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos next = panel.getBlockPos().relative(side);
                if (level.isLoaded(next) && level.getBlockEntity(next) instanceof SolarPanelBlockEntity other && other.currentArray() != null) {
                    around.add(other.currentArray());
                }
            }
            if (around.size() == 1) {
                around.iterator().next().join(panel);
                return;
            }
            // two or more arrays meet here: they become one at the next build
            for (SolarArray array : around) {
                dissolve(array);
            }
            pending.add(panel);
        }

        /** A panel is gone (broken or unloaded): its array is rebuilt from the others. */
        void removed(SolarPanelBlockEntity panel) {
            pending.remove(panel);
            SolarArray array = panel.currentArray();
            if (array != null) {
                dissolve(array);
                pending.remove(panel);
            }
        }

        private void dissolve(SolarArray array) {
            array.dissolve();
            arrays.remove(array);
            for (SolarPanelBlockEntity member : array.members) {
                if (!member.isRemoved()) {
                    pending.add(member);
                }
            }
        }

        /** The array of {@code panel}, built now if it waits for one. */
        @Nullable
        SolarArray arrayOf(SolarPanelBlockEntity panel) {
            SolarArray array = panel.currentArray();
            if (array == null && !panel.isRemoved()) {
                array = build(panel);
            }
            return array;
        }

        private SolarArray build(SolarPanelBlockEntity start) {
            SolarArray array = new SolarArray(level);
            Set<BlockPos> seen = new HashSet<>();
            ArrayDeque<SolarPanelBlockEntity> queue = new ArrayDeque<>();
            queue.add(start);
            seen.add(start.getBlockPos());
            while (!queue.isEmpty()) {
                SolarPanelBlockEntity panel = queue.poll();
                pending.remove(panel);
                array.join(panel);
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    BlockPos next = panel.getBlockPos().relative(side);
                    if (seen.add(next) && level.isLoaded(next) && level.getBlockEntity(next) instanceof SolarPanelBlockEntity other
                            && !other.isRemoved()) {
                        // an array that formed next to this one first (a chunk loaded later) is taken in
                        SolarArray existing = other.currentArray();
                        if (existing != null && existing != array) {
                            dissolve(existing);
                        }
                        if (other.currentArray() == null) {
                            queue.add(other);
                        }
                    }
                }
            }
            arrays.add(array);
            return array;
        }

        private void tick() {
            if (!pending.isEmpty()) {
                for (SolarPanelBlockEntity panel : new ArrayList<>(pending)) {
                    if (pending.contains(panel) && !panel.isRemoved() && panel.currentArray() == null) {
                        build(panel);
                    }
                }
                pending.clear();
            }
            for (SolarArray array : arrays) {
                array.tick();
            }
        }
    }

    // --- membership ---

    /** Adds {@code panel} with its own energy (constant time). */
    private void join(SolarPanelBlockEntity panel) {
        int own = panel.storedAlone();
        members.add(panel);
        panel.join(this);
        energy = Math.min(getMaxEnergyStored(), energy + own);
        boolean sky = level.canSeeSky(panel.getBlockPos().above());
        panel.setSky(sky);
        if (sky) {
            skyCount++;
        }
        scanNeighbours(panel);
        // cables and machines next to the panel fetch the array as its capability
        level.invalidateCapabilities(panel.getBlockPos());
    }

    /** Hands the pool back to the panels; the manager builds new arrays from them. */
    private void dissolve() {
        if (!valid) {
            return;
        }
        valid = false;
        int n = members.size();
        int each = n == 0 ? 0 : energy / n;
        int left = energy;
        for (int i = 0; i < n; i++) {
            int share = i == n - 1 ? left : Math.min(left, each);
            SolarPanelBlockEntity member = members.get(i);
            member.leave(share);
            dropTargets(member);
            left -= share;
            level.invalidateCapabilities(member.getBlockPos());
        }
        energy = 0;
    }

    /** Finds the blocks with an energy capability next to {@code panel} (neighbouring block entities only). */
    void scanNeighbours(SolarPanelBlockEntity panel) {
        if (!valid || panel.currentArray() != this) {
            return;
        }
        BlockCapabilityCache<IEnergyStorage, Direction>[] caches = panel.neighbourCaches();
        for (Direction side : Direction.values()) {
            BlockPos next = panel.getBlockPos().relative(side);
            BlockEntity other = level.isLoaded(next) ? level.getBlockEntity(next) : null;
            boolean wanted = other != null && !(other instanceof SolarPanelBlockEntity);
            int i = side.ordinal();
            if (wanted && caches[i] == null) {
                caches[i] = BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, level, next, side.getOpposite());
                targets.add(caches[i]);
            } else if (!wanted && caches[i] != null) {
                targets.remove(caches[i]);
                caches[i] = null;
            }
        }
    }

    private void dropTargets(SolarPanelBlockEntity panel) {
        BlockCapabilityCache<IEnergyStorage, Direction>[] caches = panel.neighbourCaches();
        for (int i = 0; i < caches.length; i++) {
            if (caches[i] != null) {
                targets.remove(caches[i]);
                caches[i] = null;
            }
        }
    }

    // --- ticking ---

    private void tick() {
        int n = members.size();
        if (n == 0) {
            return;
        }
        // a twentieth of the panels: sky view, neighbours, chunk marked for saving
        int slice = (n + SLICE_TICKS - 1) / SLICE_TICKS;
        for (int k = 0; k < slice; k++) {
            cursor = cursor >= n ? 0 : cursor;
            SolarPanelBlockEntity panel = members.get(cursor++);
            boolean sky = level.canSeeSky(panel.getBlockPos().above());
            if (sky != panel.seesSky()) {
                skyCount += sky ? 1 : -1;
                panel.setSky(sky);
            }
            scanNeighbours(panel);
            panel.setChanged();
        }
        lastPerTick = skyCount * SolarOutput.perTick(TGConfig.SOLAR_PANEL_FE_PER_TICK.get(), true, level.dimensionType().hasSkyLight(),
                level.isDay());
        int made = SolarOutput.produce(lastPerTick, remainder);
        if (made > 0) {
            energy = Math.min(getMaxEnergyStored(), energy + made);
        }
        push();
    }

    private void push() {
        if (energy <= 0 || targets.isEmpty()) {
            return;
        }
        List<IEnergyStorage> storages = new ArrayList<>(targets.size());
        for (BlockCapabilityCache<IEnergyStorage, Direction> cache : targets) {
            IEnergyStorage target = cache.getCapability();
            if (target != null && target.canReceive() && !(target instanceof SolarArray)) {
                storages.add(target);
            }
        }
        int[] accept = new int[storages.size()];
        for (int i = 0; i < accept.length; i++) {
            accept[i] = storages.get(i).receiveEnergy(energy, true);
        }
        int[] give = EnergySplit.waterFill(energy, accept);
        for (int i = 0; i < give.length; i++) {
            if (give[i] > 0) {
                energy -= storages.get(i).receiveEnergy(Math.min(give[i], energy), false);
            }
        }
    }

    // --- queries ---

    boolean isValid() {
        return valid;
    }

    /** What one panel saves while in the array: its part of the pool. */
    int shareOf(SolarPanelBlockEntity member) {
        int n = members.size();
        int each = energy / n;
        return members.get(0) == member ? energy - each * (n - 1) : each;
    }

    public int panels() {
        return members.size();
    }

    /** Panels of the array under open sky. */
    public int panelsUnderSky() {
        return skyCount;
    }

    /** FE per tick the array makes now. */
    public double lastPerTick() {
        return lastPerTick;
    }

    /** Test hook: sets the pool. */
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(getMaxEnergyStored(), energy));
    }

    // --- IEnergyStorage: any panel drains the shared pool; nothing can be inserted ---

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!valid) {
            return 0;
        }
        int taken = Math.max(0, Math.min(energy, toExtract));
        if (!simulate) {
            energy -= taken;
        }
        return taken;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return members.size() * SolarPanelBlockEntity.CAPACITY;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
