package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import techguns.registry.TGMenus;

/** Camo Bench menu (Techguns 2 {@code CamoBenchContainer}): the input slot, the player inventory, the worn armor. */
public class CamoBenchMenu extends TGMachineMenu<CamoBenchBlockEntity> {
    /** Client constructor (menu type factory). */
    public CamoBenchMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<CamoBenchBlockEntity>machineAt(inventory, buf));
    }

    public CamoBenchMenu(int id, Inventory inventory, CamoBenchBlockEntity machine) {
        super(TGMenus.CAMO_BENCH.get(), id, machine);
        addMachineSlot(CamoBenchBlockEntity.SLOT_INPUT, 17, 18);
        addPlayerInventory(inventory);
        for (int i = 0; i < RepairBenchBlockEntity.ARMOR_SLOTS.size(); i++) {
            addSlot(PlayerEquipmentSlot.armor(inventory, RepairBenchBlockEntity.ARMOR_SLOTS.get(i), 99 + i * 18, 18));
        }
        addSlot(PlayerEquipmentSlot.offhand(inventory, -17, 94));
    }
}
