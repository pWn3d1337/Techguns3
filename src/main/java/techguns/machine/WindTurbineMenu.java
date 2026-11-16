package techguns.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import techguns.core.power.WindOutput;
import techguns.registry.TGBlocks;
import techguns.registry.TGMenus;

/**
 * Wind Turbine screen data (Techguns 3): the column's energy, capacity, segment count, output and its height, biome and
 * weather factors, synced as 16 bit halves / hundredths like the solar panel menu; only the player inventory.
 */
// MIGRATION(26.x): menu/slot APIs change in 1.21.2+, as for TGMachineMenu.
public class WindTurbineMenu extends AbstractContainerMenu {
    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_CAPACITY_LOW = 2;
    public static final int DATA_CAPACITY_HIGH = 3;
    public static final int DATA_SEGMENTS = 4;
    /** Output in tenths of FE per tick. */
    public static final int DATA_OUTPUT_LOW = 5;
    public static final int DATA_OUTPUT_HIGH = 6;
    /** Factors in hundredths. */
    public static final int DATA_HEIGHT = 7;
    public static final int DATA_BIOME = 8;
    public static final int DATA_WEATHER = 9;
    /** 1 under open sky. */
    public static final int DATA_SKY = 10;
    public static final int DATA_COUNT = 11;

    /** The column's base. */
    private final BlockPos pos;
    private final ContainerData data;

    /** Client constructor (menu type factory). */
    public WindTurbineMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, buf.readBlockPos(), new SimpleContainerData(DATA_COUNT));
    }

    public WindTurbineMenu(int id, Inventory inventory, WindTurbineBlockEntity base) {
        this(id, inventory, base.getBlockPos(), new ColumnData(base));
    }

    private WindTurbineMenu(int id, Inventory inventory, BlockPos pos, ContainerData data) {
        super(TGMenus.WIND_TURBINE.get(), id);
        this.pos = pos;
        this.data = data;
        addDataSlots(data);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + (row + 1) * 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    /** The column's base. */
    public BlockPos pos() {
        return pos;
    }

    private int combined(int low, int high) {
        return (data.get(high) & 0xFFFF) << 16 | (data.get(low) & 0xFFFF);
    }

    public int energy() {
        return combined(DATA_ENERGY_LOW, DATA_ENERGY_HIGH);
    }

    public int capacity() {
        return combined(DATA_CAPACITY_LOW, DATA_CAPACITY_HIGH);
    }

    public int segments() {
        return data.get(DATA_SEGMENTS) & 0xFFFF;
    }

    /** FE per tick the column makes now. */
    public double output() {
        return combined(DATA_OUTPUT_LOW, DATA_OUTPUT_HIGH) / 10.0;
    }

    public double heightFactor() {
        return (data.get(DATA_HEIGHT) & 0xFFFF) / 100.0;
    }

    public double biomeFactor() {
        return (data.get(DATA_BIOME) & 0xFFFF) / 100.0;
    }

    public double weatherFactor() {
        return (data.get(DATA_WEATHER) & 0xFFFF) / 100.0;
    }

    public boolean openSky() {
        return data.get(DATA_SKY) != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(pos).is(TGBlocks.WIND_TURBINE.get())
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0 + segments() * 16.0;
    }

    /** Server: live values of the column. */
    private static final class ColumnData implements ContainerData {
        private final WindTurbineBlockEntity base;

        ColumnData(WindTurbineBlockEntity base) {
            this.base = base;
        }

        @Override
        public int get(int index) {
            int output = (int) Math.round(base.lastPerTick() * 10.0);
            return switch (index) {
                case DATA_ENERGY_LOW -> base.energy() & 0xFFFF;
                case DATA_ENERGY_HIGH -> base.energy() >>> 16;
                case DATA_CAPACITY_LOW -> base.capacity() & 0xFFFF;
                case DATA_CAPACITY_HIGH -> base.capacity() >>> 16;
                case DATA_SEGMENTS -> Math.min(0xFFFF, base.segments());
                case DATA_OUTPUT_LOW -> output & 0xFFFF;
                case DATA_OUTPUT_HIGH -> output >>> 16;
                case DATA_HEIGHT -> hundredths(base.averageHeightFactor());
                case DATA_BIOME -> hundredths(base.biomeFactor());
                case DATA_WEATHER -> base.getLevel() == null ? 100
                        : hundredths(WindOutput.weatherFactor(base.getLevel().getRainLevel(1.0f), base.getLevel().getThunderLevel(1.0f)));
                case DATA_SKY -> base.openSky() ? 1 : 0;
                default -> 0;
            };
        }

        private static int hundredths(double value) {
            return Math.min(0xFFFF, (int) Math.round(value * 100.0));
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    }
}
