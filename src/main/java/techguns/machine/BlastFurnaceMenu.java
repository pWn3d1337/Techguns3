package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/** Blast Furnace menu (Techguns 2 {@code BlastFurnaceContainer}, same slot positions and slot icons). */
public class BlastFurnaceMenu extends TGMachineMenu<BlastFurnaceBlockEntity> {
    /** Client constructor (menu type factory). */
    public BlastFurnaceMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<BlastFurnaceBlockEntity>machineAt(inventory, buf));
    }

    public BlastFurnaceMenu(int id, Inventory inventory, BlastFurnaceBlockEntity machine) {
        super(TGMenus.BLAST_FURNACE.get(), id, machine);
        addMachineSlot(BlastFurnaceBlockEntity.SLOT_INPUT1, 19, 17, "ingot");
        addMachineSlot(BlastFurnaceBlockEntity.SLOT_INPUT2, 47, 17, "powder");
        addMachineSlot(BlastFurnaceBlockEntity.SLOT_OUTPUT, 116, 50);
        addMachineSlot(BlastFurnaceBlockEntity.SLOT_UPGRADE, 150, 50);
        addPlayerInventory(inventory);
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }
}
