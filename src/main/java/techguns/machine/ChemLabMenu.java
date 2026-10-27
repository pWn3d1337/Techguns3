package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import techguns.registry.TGMenus;

/** Chem Lab menu (Techguns 2 {@code ChemLabContainer}, same slot positions); tank contents come as data slots. */
public class ChemLabMenu extends TGMachineMenu<ChemLabBlockEntity> {
    /** Client constructor (menu type factory). */
    public ChemLabMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<ChemLabBlockEntity>machineAt(inventory, buf));
    }

    public ChemLabMenu(int id, Inventory inventory, ChemLabBlockEntity machine) {
        super(TGMenus.CHEM_LAB.get(), id, machine);
        addMachineSlot(ChemLabBlockEntity.SLOT_INPUT1, 35, 17);
        addMachineSlot(ChemLabBlockEntity.SLOT_INPUT2, 57, 17);
        addMachineSlot(ChemLabBlockEntity.SLOT_FLASK, 35, 40, "bottle");
        addMachineSlot(ChemLabBlockEntity.SLOT_OUTPUT, 135, 17);
        addMachineSlot(ChemLabBlockEntity.SLOT_UPGRADE, 135, 60);
        addPlayerInventory(inventory);
    }

    public FluidStack inputFluid() {
        return fluid(ChemLabBlockEntity.DATA_INPUT_FLUID, ChemLabBlockEntity.DATA_INPUT_AMOUNT);
    }

    public FluidStack outputFluid() {
        return fluid(ChemLabBlockEntity.DATA_OUTPUT_FLUID, ChemLabBlockEntity.DATA_OUTPUT_AMOUNT);
    }

    private FluidStack fluid(int typeIndex, int amountIndex) {
        int amount = data.get(amountIndex);
        int id = data.get(typeIndex);
        return amount <= 0 || id < 0 ? FluidStack.EMPTY : new FluidStack(ChemLabBlockEntity.fluidById(id), amount);
    }

    public boolean isDrainInput() {
        return data.get(ChemLabBlockEntity.DATA_DRAIN_INPUT) != 0;
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }
}
