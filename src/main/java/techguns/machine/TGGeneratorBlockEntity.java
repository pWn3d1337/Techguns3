package techguns.machine;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import techguns.core.transport.EnergySplit;

/**
 * A Techguns generator (new in Techguns 3, no Techguns 2 counterpart): burns fuel into an extract-only FE buffer and
 * pushes it into the blocks on all six sides, split evenly. It only burns while the buffer has room for a tick's
 * output, so fuel is never wasted; the redstone mode stops burning and pushing. No jobs ({@link MachineOperation}).
 */
public abstract class TGGeneratorBlockEntity extends TGMachineBlockEntity {
    public static final int DATA_BURN_LOW = DATA_COUNT;
    public static final int DATA_BURN_HIGH = DATA_COUNT + 1;
    public static final int DATA_BURN_TOTAL_LOW = DATA_COUNT + 2;
    public static final int DATA_BURN_TOTAL_HIGH = DATA_COUNT + 3;
    /** First synced value of a generator subclass. */
    public static final int DATA_GENERATOR_COUNT = DATA_COUNT + 4;

    /** Fuel burn ticks left of the fuel burning now, and the total of that fuel. */
    protected int burnRemaining;
    protected int burnTotal;
    @Nullable
    private Map<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> neighbours;

    protected TGGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots, int energyCapacity) {
        super(type, pos, state, slots, energyCapacity);
    }

    @Override
    protected MachineEnergy createEnergy(int capacity) {
        return MachineEnergy.generator(capacity, outputRate(), this::setChanged);
    }

    /** FE made per tick while burning. */
    protected abstract int fePerTick();

    /** Fuel burn ticks used per tick. */
    protected int burnPerTick() {
        return 1;
    }

    /** Most FE pushed out per tick in total (and taken per extract call). Called from the constructor. */
    protected abstract int outputRate();

    /** Takes the next fuel and sets {@link #burnRemaining} and {@link #burnTotal}; false if there is none. */
    protected abstract boolean refuel();

    @Override
    protected void checkAndStartOperation() {
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
    }

    @Override
    protected boolean idleTick() {
        int made = fePerTick();
        if (burnRemaining <= 0 && energy.space() >= made && refuel()) {
            setChanged();
        }
        if (burnRemaining > 0 && energy.space() >= made) {
            burnRemaining = Math.max(0, burnRemaining - burnPerTick());
            energy.produce(made);
        }
        setLit(burnRemaining > 0);
        pushEnergy();
        return true;
    }

    /** Hands the buffer to the neighbours that take FE, at most {@link #outputRate} per tick. */
    private void pushEnergy() {
        int available = Math.min(energy.getEnergyStored(), outputRate());
        if (available <= 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        if (neighbours == null) {
            neighbours = new EnumMap<>(Direction.class);
            for (Direction side : Direction.values()) {
                neighbours.put(side, BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, server, worldPosition.relative(side),
                        side.getOpposite()));
            }
        }
        IEnergyStorage[] targets = new IEnergyStorage[6];
        int[] accept = new int[6];
        for (Direction side : Direction.values()) {
            IEnergyStorage target = neighbours.get(side).getCapability();
            if (target != null && target != energy && target.canReceive()) {
                targets[side.ordinal()] = target;
                accept[side.ordinal()] = target.receiveEnergy(available, true);
            }
        }
        int[] give = EnergySplit.waterFill(available, accept);
        int sent = 0;
        for (int i = 0; i < 6; i++) {
            if (give[i] > 0) {
                sent += targets[i].receiveEnergy(give[i], false);
            }
        }
        if (sent > 0) {
            energy.extractEnergy(sent, false);
        }
    }

    private void setLit(boolean lit) {
        BlockState state = getBlockState();
        if (level != null && state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, lit), Block.UPDATE_ALL);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        neighbours = null;
    }

    public int getBurnRemaining() {
        return burnRemaining;
    }

    @Override
    protected int extraDataCount() {
        return DATA_GENERATOR_COUNT - DATA_COUNT;
    }

    @Override
    protected int getExtraData(int index) {
        return switch (index) {
            case DATA_BURN_LOW -> burnRemaining & 0xFFFF;
            case DATA_BURN_HIGH -> burnRemaining >>> 16;
            case DATA_BURN_TOTAL_LOW -> burnTotal & 0xFFFF;
            case DATA_BURN_TOTAL_HIGH -> burnTotal >>> 16;
            default -> 0;
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("burn_remaining", burnRemaining);
        tag.putInt("burn_total", burnTotal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        burnRemaining = tag.getInt("burn_remaining");
        burnTotal = tag.getInt("burn_total");
    }
}
