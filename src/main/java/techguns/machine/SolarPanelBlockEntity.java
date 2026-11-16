package techguns.machine;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.energy.IEnergyStorage;
import techguns.registry.TGBlockEntities;

/**
 * A solar panel (Techguns 3): its energy lives in the {@link SolarArray} of all panels next to it while that array
 * exists; alone (between arrays, or saved) it keeps its own part. Panels do not tick: the level's array manager ticks
 * each array once.
 */
public class SolarPanelBlockEntity extends BlockEntity {
    /** FE one panel adds to its array's pool. */
    public static final int CAPACITY = 4000;

    @Nullable
    private SolarArray array;
    /** Own energy while not in an array (and what was loaded). */
    private int stored;
    private boolean sky;
    /** Biome multiplier of this panel ({@link SolarEfficiency}). */
    private float biomeFactor = 1.0f;
    /** The array's capability caches of the blocks next to this panel, by {@link Direction#ordinal()}. */
    @SuppressWarnings("unchecked")
    private final BlockCapabilityCache<IEnergyStorage, Direction>[] neighbourCaches = new BlockCapabilityCache[6];

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.SOLAR_PANEL.get(), pos, state);
    }

    /** Server: the array this panel belongs to, built if needed. */
    @Nullable
    public SolarArray array() {
        return level instanceof ServerLevel server && !isRemoved() ? SolarArray.manager(server).arrayOf(this) : null;
    }

    @Nullable
    SolarArray currentArray() {
        return array != null && array.isValid() ? array : null;
    }

    void join(SolarArray array) {
        this.array = array;
        this.stored = 0;
    }

    void leave(int share) {
        this.array = null;
        this.stored = Math.max(0, Math.min(CAPACITY, share));
        setChanged();
    }

    int storedAlone() {
        return stored;
    }

    boolean seesSky() {
        return sky;
    }

    void setSky(boolean sky) {
        this.sky = sky;
    }

    float biomeFactor() {
        return biomeFactor;
    }

    void setBiomeFactor(float biomeFactor) {
        this.biomeFactor = biomeFactor;
    }

    /** What this panel adds to its array's sunlit sum: its biome multiplier under open sky, else nothing. */
    double contribution() {
        return sky ? biomeFactor : 0.0;
    }

    BlockCapabilityCache<IEnergyStorage, Direction>[] neighbourCaches() {
        return neighbourCaches;
    }

    /** A block next to the panel changed: the array looks for machines and cables there again. */
    void neighbourChanged() {
        SolarArray current = currentArray();
        if (current != null) {
            current.scanNeighbours(this);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            SolarArray.manager(server).added(this);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel server) {
            SolarArray.manager(server).removed(this);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        SolarArray current = currentArray();
        tag.putInt("energy", current != null ? current.shareOf(this) : stored);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stored = Math.max(0, Math.min(CAPACITY, tag.getInt("energy")));
    }
}
