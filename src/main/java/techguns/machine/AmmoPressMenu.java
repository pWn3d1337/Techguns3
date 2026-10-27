package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/** Ammo Press menu (Techguns 2 {@code AmmoPressContainer}, same slot positions and slot icons). */
public class AmmoPressMenu extends TGMachineMenu<AmmoPressBlockEntity> {
    /** Client constructor (menu type factory). */
    public AmmoPressMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<AmmoPressBlockEntity>machineAt(inventory, buf));
    }

    public AmmoPressMenu(int id, Inventory inventory, AmmoPressBlockEntity machine) {
        super(TGMenus.AMMO_PRESS.get(), id, machine);
        addMachineSlot(AmmoPressBlockEntity.SLOT_BULLET_METAL, 100, 17, "ingot_dark");
        addMachineSlot(AmmoPressBlockEntity.SLOT_CASING_METAL, 120, 17, "ingot");
        addMachineSlot(AmmoPressBlockEntity.SLOT_POWDER, 140, 17, "powder");
        addMachineSlot(AmmoPressBlockEntity.SLOT_OUTPUT, 120, 60);
        addMachineSlot(AmmoPressBlockEntity.SLOT_UPGRADE, 152, 60);
        addPlayerInventory(inventory);
    }

    public int plan() {
        return data.get(AmmoPressBlockEntity.DATA_PLAN);
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }
}
