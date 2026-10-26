package techguns.slot.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/** Glider (Techguns 2 {@code ItemGlider}): sneak in the air to glide. */
public class GliderItem extends TGSlotItem {
    public GliderItem(Properties properties) {
        super(properties, TGSlotType.BACK, 0);
    }

    @Override
    public void slotTick(Player player, ItemStack stack) {
        SlotItemSupport.glide(player);
    }
}
