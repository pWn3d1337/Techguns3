package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/** Metal Press menu (Techguns 2 {@code MetalPressContainer}, same slot positions). */
public class MetalPressMenu extends TGMachineMenu<MetalPressBlockEntity> {
    /** Client constructor (menu type factory). */
    public MetalPressMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<MetalPressBlockEntity>machineAt(inventory, buf));
    }

    public MetalPressMenu(int id, Inventory inventory, MetalPressBlockEntity machine) {
        super(TGMenus.METAL_PRESS.get(), id, machine);
        addMachineSlot(MetalPressBlockEntity.SLOT_INPUT1, 110, 17);
        addMachineSlot(MetalPressBlockEntity.SLOT_INPUT2, 130, 17);
        addMachineSlot(MetalPressBlockEntity.SLOT_OUTPUT, 120, 60);
        addMachineSlot(MetalPressBlockEntity.SLOT_UPGRADE, 152, 60);
        addPlayerInventory(inventory);
    }

    public boolean isAutoSplit() {
        return data.get(MetalPressBlockEntity.DATA_AUTO_SPLIT) != 0;
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }
}
