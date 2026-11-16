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
import techguns.registry.TGBlocks;
import techguns.registry.TGMenus;

/**
 * Solar Panel screen data (Techguns 3): the array's energy, capacity, panel count and output, synced as 16 bit halves
 * like the machine menus; no machine slots, only the player inventory.
 */
// MIGRATION(26.x): menu/slot APIs change in 1.21.2+, as for TGMachineMenu.
public class SolarPanelMenu extends AbstractContainerMenu {
    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_CAPACITY_LOW = 2;
    public static final int DATA_CAPACITY_HIGH = 3;
    public static final int DATA_PANELS = 4;
    /** Output in tenths of FE per tick. */
    public static final int DATA_OUTPUT_LOW = 5;
    public static final int DATA_OUTPUT_HIGH = 6;
    public static final int DATA_COUNT = 7;

    private final BlockPos pos;
    private final ContainerData data;

    /** Client constructor (menu type factory). */
    public SolarPanelMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, buf.readBlockPos(), new SimpleContainerData(DATA_COUNT));
    }

    public SolarPanelMenu(int id, Inventory inventory, SolarPanelBlockEntity panel) {
        this(id, inventory, panel.getBlockPos(), new ArrayData(panel));
    }

    private SolarPanelMenu(int id, Inventory inventory, BlockPos pos, ContainerData data) {
        super(TGMenus.SOLAR_PANEL.get(), id);
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

    /** The panel this screen was opened on. */
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

    public int panels() {
        return data.get(DATA_PANELS) & 0xFFFF;
    }

    /** FE per tick the array makes now. */
    public double output() {
        return combined(DATA_OUTPUT_LOW, DATA_OUTPUT_HIGH) / 10.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(pos).is(TGBlocks.SOLAR_PANEL.get())
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    /** Server: live values of the panel's array. */
    private static final class ArrayData implements ContainerData {
        private final SolarPanelBlockEntity panel;

        ArrayData(SolarPanelBlockEntity panel) {
            this.panel = panel;
        }

        @Override
        public int get(int index) {
            SolarArray array = panel.array();
            if (array == null) {
                return 0;
            }
            int output = (int) Math.round(array.lastPerTick() * 10.0);
            return switch (index) {
                case DATA_ENERGY_LOW -> array.getEnergyStored() & 0xFFFF;
                case DATA_ENERGY_HIGH -> array.getEnergyStored() >>> 16;
                case DATA_CAPACITY_LOW -> array.getMaxEnergyStored() & 0xFFFF;
                case DATA_CAPACITY_HIGH -> array.getMaxEnergyStored() >>> 16;
                case DATA_PANELS -> Math.min(0xFFFF, array.panels());
                case DATA_OUTPUT_LOW -> output & 0xFFFF;
                case DATA_OUTPUT_HIGH -> output >>> 16;
                default -> 0;
            };
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
