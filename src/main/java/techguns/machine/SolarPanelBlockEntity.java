package techguns.machine;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import techguns.registry.TGBlockEntities;

/**
 * A solar panel (Techguns 3): its energy lives in the {@link SolarArray} of all panels next to it while that array
 * exists; alone (between arrays, or saved) it keeps its own part. The first panel of an array ticks the whole array.
 */
public class SolarPanelBlockEntity extends BlockEntity {
    /** FE one panel adds to its array's pool. */
    public static final int CAPACITY = 4000;

    @Nullable
    private SolarArray array;
    /** Own energy while not in an array (and what was loaded). */
    private int stored;
    private boolean sky;
    private boolean skyChecked;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.SOLAR_PANEL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity panel) {
        SolarArray array = panel.array();
        if (array != null && array.isMaster(panel)) {
            array.tick();
        }
    }

    /** Server: the array this panel belongs to, built if needed. */
    @Nullable
    public SolarArray array() {
        if (!(level instanceof ServerLevel server) || isRemoved()) {
            return null;
        }
        if (array == null || !array.isValid()) {
            array = null;
            SolarArray.build(server, this);
        }
        return array;
    }

    @Nullable
    SolarArray currentArray() {
        return array != null && array.isValid() ? array : null;
    }

    void join(SolarArray array) {
        this.array = array;
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

    boolean skyChecked() {
        return skyChecked;
    }

    void setSky(boolean sky) {
        this.sky = sky;
        this.skyChecked = true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // a panel appearing joins the panels around it
        if (level != null) {
            SolarArray.invalidateAround(level, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            SolarArray current = currentArray();
            if (current != null) {
                current.dissolve();
            }
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
