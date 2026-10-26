package techguns.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import techguns.gun.AmmoInventory;

/** Ammo picked up from the ground goes into Ammo Bags in the player's inventory first. */
public final class AmmoBagEvents {
    private AmmoBagEvents() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(AmmoBagEvents::onItemPickup);
    }

    private static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        ItemEntity entity = event.getItemEntity();
        ItemStack stack = entity.getItem();
        if (entity.hasPickUpDelay() || event.canPickup() == TriState.FALSE || !AmmoBagData.isAmmo(stack)) {
            return;
        }
        int before = stack.getCount();
        AmmoInventory.insertIntoBags(event.getPlayer(), stack);
        int moved = before - stack.getCount();
        if (moved <= 0) {
            return;
        }
        event.getPlayer().take(entity, moved);
        event.getPlayer().awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), moved);
        if (stack.isEmpty()) {
            entity.discard();
            event.setCanPickup(TriState.FALSE);
        }
    }
}
