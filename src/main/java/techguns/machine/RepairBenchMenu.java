package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/**
 * Repair Bench menu (Techguns 2 {@code RepairBenchContainer}, same positions): material slots, the repair slot, the
 * player inventory, then the worn armor and the offhand.
 */
public class RepairBenchMenu extends TGMachineMenu<RepairBenchBlockEntity> {
    /** Client constructor (menu type factory). */
    public RepairBenchMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<RepairBenchBlockEntity>machineAt(inventory, buf));
    }

    public RepairBenchMenu(int id, Inventory inventory, RepairBenchBlockEntity machine) {
        super(TGMenus.REPAIR_BENCH.get(), id, machine);
        for (int i = RepairBenchBlockEntity.SLOT_MATERIAL_FIRST; i <= RepairBenchBlockEntity.SLOT_MATERIAL_LAST; i++) {
            addMachineSlot(i, 8 + i * 18, 57);
        }
        addMachineSlot(RepairBenchBlockEntity.SLOT_REPAIR, 8, 18);
        addPlayerInventory(inventory);
        for (int i = 0; i < RepairBenchBlockEntity.ARMOR_SLOTS.size(); i++) {
            addSlot(PlayerEquipmentSlot.armor(inventory, RepairBenchBlockEntity.ARMOR_SLOTS.get(i), 89 + i * 20, 18));
        }
        addSlot(PlayerEquipmentSlot.offhand(inventory, 64, 18));
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }

    @Override
    protected int targetSlotCount(int target) {
        return target == RepairBenchBlockEntity.SLOT_MATERIAL_FIRST ? RepairBenchBlockEntity.SLOT_MATERIAL_LAST + 1 : 1;
    }
}
