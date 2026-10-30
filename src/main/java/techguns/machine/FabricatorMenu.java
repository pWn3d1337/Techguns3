package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/** Fabricator menu (Techguns 2 {@code FabricatorContainer}, same slot positions). */
public class FabricatorMenu extends TGMachineMenu<FabricatorBlockEntity> {
    /** Client constructor (menu type factory). */
    public FabricatorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<FabricatorBlockEntity>machineAt(inventory, buf));
    }

    public FabricatorMenu(int id, Inventory inventory, FabricatorBlockEntity machine) {
        super(TGMenus.FABRICATOR.get(), id, machine);
        addMachineSlot(FabricatorBlockEntity.SLOT_INPUT, 19, 17);
        addMachineSlot(FabricatorBlockEntity.SLOT_WIRES, 47, 17, "wires");
        addMachineSlot(FabricatorBlockEntity.SLOT_POWDER, 68, 17, "powder");
        addMachineSlot(FabricatorBlockEntity.SLOT_PLATE, 89, 17, "plate");
        addMachineSlot(FabricatorBlockEntity.SLOT_OUTPUT, 116, 50);
        addMachineSlot(FabricatorBlockEntity.SLOT_UPGRADE, 150, 50);
        addPlayerInventory(inventory);
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }
}
