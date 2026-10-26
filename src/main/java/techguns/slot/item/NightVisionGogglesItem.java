package techguns.slot.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.player.TGPlayerData;
import techguns.registry.TGAmmo;
import techguns.registry.TGAttributes;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/** Night vision goggles (Techguns 2 {@code ItemNightVisionGoggles}): 1 charge per tick while the night vision toggle is on. */
public class NightVisionGogglesItem extends TGSlotItem {
    public NightVisionGogglesItem(Properties properties, int durability) {
        super(properties, TGSlotType.FACE, durability);
        bonus(TGAttributes.NIGHT_VISION, 1.0);
        refill(TGAmmo.REDSTONE_BATTERY, TGAmmo.REDSTONE_BATTERY_EMPTY);
    }

    @Override
    public void slotTick(Player player, ItemStack stack) {
        if (isCharged(stack)) {
            if (TGPlayerData.of(player).get(TGPlayerData.Toggle.NIGHT_VISION)) {
                useCharge(player, stack, 1);
            }
        } else {
            SlotItemSupport.recharge(this, player, stack);
        }
    }
}
