package techguns.machine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import techguns.TGConfig;
import techguns.core.power.SolarOutput;
import techguns.core.transport.EnergySplit;

/**
 * Solar panels side by side form an array (an implicit multiblock): one shared energy pool, so power moves between the
 * panels without limit, drained through any panel and pushed into every block next to the array. The array is built
 * lazily from the first panel asked for it and dissolved whenever a panel or a neighbouring block changes; dissolving
 * hands the pool back to the panels in proportion to their capacity, so nothing is lost when chunks unload.
 */
public final class SolarArray implements IEnergyStorage {
    /** Sky visibility of each panel is checked this often (ticks). */
    private static final int SKY_CHECK_INTERVAL = 20;

    private final ServerLevel level;
    private final List<SolarPanelBlockEntity> members;
    private final int capacity;
    private int energy;
    private final double[] remainder = {0.0};
    private final List<BlockCapabilityCache<IEnergyStorage, Direction>> targets = new ArrayList<>();
    private boolean valid = true;
    private double lastPerTick;

    private SolarArray(ServerLevel level, List<SolarPanelBlockEntity> members) {
        this.level = level;
        this.members = members;
        this.capacity = members.size() * SolarPanelBlockEntity.CAPACITY;
        long sum = 0;
        for (SolarPanelBlockEntity member : members) {
            sum += member.storedAlone();
        }
        this.energy = (int) Math.min(capacity, sum);
        for (SolarPanelBlockEntity member : members) {
            for (Direction side : Direction.values()) {
                BlockPos next = member.getBlockPos().relative(side);
                if (!level.isLoaded(next) || level.getBlockEntity(next) instanceof SolarPanelBlockEntity || level.getBlockState(next).isAir()) {
                    continue;
                }
                targets.add(BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, level, next, side.getOpposite()));
            }
        }
    }

    /** Collects the loaded panels connected to {@code start} (horizontally) into a new array. */
    static SolarArray build(ServerLevel level, SolarPanelBlockEntity start) {
        Set<BlockPos> seen = new LinkedHashSet<>();
        List<SolarPanelBlockEntity> members = new ArrayList<>();
        ArrayDeque<SolarPanelBlockEntity> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start.getBlockPos());
        while (!queue.isEmpty()) {
            SolarPanelBlockEntity panel = queue.poll();
            members.add(panel);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos next = panel.getBlockPos().relative(side);
                if (seen.add(next) && level.isLoaded(next) && level.getBlockEntity(next) instanceof SolarPanelBlockEntity other && !other.isRemoved()) {
                    queue.add(other);
                }
            }
        }
        SolarArray array = new SolarArray(level, members);
        members.forEach(member -> member.join(array));
        return array;
    }

    /** Dissolves the arrays of the panels at and around {@code pos} (a panel or a neighbouring block changed). */
    static void invalidateAround(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        for (BlockPos p : new BlockPos[]{pos, pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below()}) {
            if (level.isLoaded(p) && level.getBlockEntity(p) instanceof SolarPanelBlockEntity panel && panel.currentArray() != null) {
                panel.currentArray().dissolve();
            }
        }
    }

    /** Hands the pool back to the panels and lets them build a new array when next asked. */
    void dissolve() {
        if (!valid) {
            return;
        }
        valid = false;
        int left = energy;
        for (int i = 0; i < members.size(); i++) {
            int share = i == members.size() - 1 ? left : Math.min(left, shareOfOne());
            members.get(i).leave(share);
            left -= share;
        }
        energy = 0;
        // cables and machines holding this array as a capability must fetch the new one
        members.forEach(member -> level.invalidateCapabilities(member.getBlockPos()));
    }

    private int shareOfOne() {
        return members.isEmpty() ? 0 : energy / members.size();
    }

    /** What one panel saves while in the array: its part of the pool. */
    int shareOf(SolarPanelBlockEntity member) {
        return members.get(0) == member ? energy - shareOfOne() * (members.size() - 1) : shareOfOne();
    }

    boolean isValid() {
        return valid;
    }

    boolean isMaster(SolarPanelBlockEntity member) {
        return members.get(0) == member;
    }

    /** One tick of the whole array, run by its first panel: make power, then push it out. */
    void tick() {
        long time = level.getGameTime();
        boolean dimensionHasSky = level.dimensionType().hasSkyLight();
        boolean day = level.isDay();
        int rate = TGConfig.SOLAR_PANEL_FE_PER_TICK.get();
        double perTick = 0.0;
        for (SolarPanelBlockEntity member : members) {
            if (time % SKY_CHECK_INTERVAL == Math.floorMod(member.getBlockPos().hashCode(), SKY_CHECK_INTERVAL) || !member.skyChecked()) {
                member.setSky(level.canSeeSky(member.getBlockPos().above()));
            }
            perTick += SolarOutput.perTick(rate, member.seesSky(), dimensionHasSky, day);
        }
        lastPerTick = perTick;
        int before = energy;
        int made = SolarOutput.produce(perTick, remainder);
        if (made > 0) {
            energy = Math.min(capacity, energy + made);
        }
        push();
        // the panels save their part of the pool: keep their chunks marked for saving while it changes
        if (energy != before && time % SKY_CHECK_INTERVAL == 0) {
            members.forEach(SolarPanelBlockEntity::setChanged);
        }
    }

    private void push() {
        if (energy <= 0 || targets.isEmpty()) {
            return;
        }
        IEnergyStorage[] storages = new IEnergyStorage[targets.size()];
        int[] accept = new int[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            IEnergyStorage target = targets.get(i).getCapability();
            if (target != null && target.canReceive() && !(target instanceof SolarArray)) {
                storages[i] = target;
                accept[i] = target.receiveEnergy(energy, true);
            }
        }
        int[] give = EnergySplit.waterFill(energy, accept);
        for (int i = 0; i < give.length; i++) {
            if (give[i] > 0) {
                energy -= storages[i].receiveEnergy(Math.min(give[i], energy), false);
            }
        }
    }

    public int panels() {
        return members.size();
    }

    /** FE per tick the array made last tick. */
    public double lastPerTick() {
        return lastPerTick;
    }

    /** Test hook: sets the pool. */
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
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
        return capacity;
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
