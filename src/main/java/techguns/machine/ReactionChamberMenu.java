package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import techguns.registry.TGMenus;

/** Reaction Chamber menu (Techguns 2 {@code ReactionChamberContainer}, same slot positions); tank and levels as data slots. */
public class ReactionChamberMenu extends TGMachineMenu<ReactionChamberBlockEntity> {
    /** Client constructor (menu type factory). */
    public ReactionChamberMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<ReactionChamberBlockEntity>machineAt(inventory, buf));
    }

    public ReactionChamberMenu(int id, Inventory inventory, ReactionChamberBlockEntity machine) {
        super(TGMenus.REACTION_CHAMBER.get(), id, machine);
        addMachineSlot(ReactionChamberBlockEntity.SLOT_INPUT, 35, 17);
        addMachineSlot(ReactionChamberBlockEntity.SLOT_FOCUS, 93, 17);
        addMachineSlot(ReactionChamberBlockEntity.SLOT_OUTPUT_FIRST, 134, 17);
        addMachineSlot(ReactionChamberBlockEntity.SLOT_OUTPUT_FIRST + 1, 152, 17);
        addMachineSlot(ReactionChamberBlockEntity.SLOT_OUTPUT_FIRST + 2, 134, 35);
        addMachineSlot(ReactionChamberBlockEntity.SLOT_OUTPUT_FIRST + 3, 152, 35);
        addPlayerInventory(inventory);
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }

    public FluidStack fluid() {
        int id = data.get(ReactionChamberBlockEntity.DATA_FLUID);
        int amount = data.get(ReactionChamberBlockEntity.DATA_AMOUNT);
        return amount <= 0 || id < 0 ? FluidStack.EMPTY : new FluidStack(ChemLabBlockEntity.fluidById(id), amount);
    }

    public int liquidLevel() {
        return data.get(ReactionChamberBlockEntity.DATA_LIQUID_LEVEL);
    }

    public int intensity() {
        return data.get(ReactionChamberBlockEntity.DATA_INTENSITY);
    }

    public int completion() {
        return data.get(ReactionChamberBlockEntity.DATA_COMPLETION);
    }

    public int requiredCompletion() {
        return data.get(ReactionChamberBlockEntity.DATA_REQUIRED_COMPLETION);
    }

    public int requiredIntensity() {
        return data.get(ReactionChamberBlockEntity.DATA_REQUIRED_INTENSITY);
    }

    public boolean isReacting() {
        return data.get(ReactionChamberBlockEntity.DATA_REACTING) != 0;
    }
}
