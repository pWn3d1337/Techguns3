package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import techguns.registry.TGMenus;

/** Ore Drill menu (Techguns 2 {@code OreDrillContainer}, same slot positions); tanks and fuel come as data slots. */
public class OreDrillMenu extends TGMachineMenu<OreDrillBlockEntity> {
    /** Client constructor (menu type factory). */
    public OreDrillMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<OreDrillBlockEntity>machineAt(inventory, buf));
    }

    public OreDrillMenu(int id, Inventory inventory, OreDrillBlockEntity machine) {
        super(TGMenus.ORE_DRILL.get(), id, machine);
        addMachineSlot(OreDrillBlockEntity.SLOT_DRILL, 30, 17);
        addMachineSlot(OreDrillBlockEntity.SLOT_FUEL, 30, 51);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addMachineSlot(OreDrillBlockEntity.SLOT_OUTPUT_FIRST + i * 3 + j, 102 + 18 * i, 17 + 18 * j);
            }
        }
        addPlayerInventory(inventory);
    }

    public FluidStack inputFluid() {
        return fluid(OreDrillBlockEntity.DATA_INPUT_FLUID, OreDrillBlockEntity.DATA_INPUT_AMOUNT);
    }

    public FluidStack outputFluid() {
        return fluid(OreDrillBlockEntity.DATA_OUTPUT_FLUID, OreDrillBlockEntity.DATA_OUTPUT_AMOUNT);
    }

    private FluidStack fluid(int typeIndex, int amountIndex) {
        int amount = data.get(amountIndex);
        int id = data.get(typeIndex);
        return amount <= 0 || id < 0 ? FluidStack.EMPTY : new FluidStack(ChemLabBlockEntity.fluidById(id), amount);
    }

    private int split(int low, int high) {
        return (data.get(low) & 0xFFFF) | (data.get(high) & 0xFFFF) << 16;
    }

    public int fuelBuffer() {
        return split(OreDrillBlockEntity.DATA_FUEL_LOW, OreDrillBlockEntity.DATA_FUEL_HIGH);
    }

    public int fuelBufferMax() {
        return split(OreDrillBlockEntity.DATA_FUEL_MAX_LOW, OreDrillBlockEntity.DATA_FUEL_MAX_HIGH);
    }

    public int powerPerTick() {
        return split(OreDrillBlockEntity.DATA_POWER_LOW, OreDrillBlockEntity.DATA_POWER_HIGH);
    }

    public int headMiningLevel() {
        return (short) data.get(OreDrillBlockEntity.DATA_HEAD_LEVEL);
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }
}
