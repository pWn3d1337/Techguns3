package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/** Grinder menu (Techguns 2 {@code GrinderContainer}: input, upgrade, 3 x 3 outputs). */
public class GrinderMenu extends TGMachineMenu<GrinderBlockEntity> {
    /** Client constructor (menu type factory). */
    public GrinderMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<GrinderBlockEntity>machineAt(inventory, buf));
    }

    public GrinderMenu(int id, Inventory inventory, GrinderBlockEntity machine) {
        super(TGMenus.GRINDER.get(), id, machine);
        addMachineSlot(GrinderBlockEntity.SLOT_INPUT, 18, 17);
        addMachineSlot(GrinderBlockEntity.SLOT_UPGRADE, 152, 60);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addMachineSlot(GrinderBlockEntity.SLOT_OUTPUT_FIRST + row * 3 + col, 80 + col * 18, 17 + row * 18);
            }
        }
        addPlayerInventory(inventory);
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }
}
