package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/**
 * Camo Bench menu (Techguns 2 {@code CamoBenchContainer}): the input slot and the player inventory. The screen's tabs change
 * the look of the worn armor and gear where they are worn; the armor has hidden slots here only so the menu sends those
 * changes to the client (the worn gear is sent by Curios or the extra slots).
 */
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
            addSlot(new SyncOnlySlot(inventory, Inventory.INVENTORY_SIZE + RepairBenchBlockEntity.ARMOR_SLOTS.get(i).getIndex()));
        }
    }

    /** A slot that is neither drawn nor clickable; it only keeps the client's copy of the item up to date. */
    private static class SyncOnlySlot extends Slot {
        SyncOnlySlot(Container container, int index) {
            super(container, index, 0, 0);
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
