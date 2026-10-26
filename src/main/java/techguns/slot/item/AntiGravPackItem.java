package techguns.slot.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;
import techguns.registry.TGAmmo;
import techguns.registry.TGAttributes;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/** Anti gravity pack (Techguns 2 {@code ItemAntiGravPack}): creative flight while charged, 1 charge per tick of flying. */
public class AntiGravPackItem extends TGSlotItem {
    public AntiGravPackItem(Properties properties, int durability) {
        super(properties, TGSlotType.BACK, durability);
        bonus(NeoForgeMod.CREATIVE_FLIGHT, 1.0);
        bonus(TGAttributes.FLY_SPEED_BONUS, 0.25);
        refill(TGAmmo.NUCLEAR_POWER_CELL, TGAmmo.NUCLEAR_POWER_CELL_EMPTY);
    }

    @Override
    public void slotTick(Player player, ItemStack stack) {
        if (isCharged(stack)) {
            if (player.getAbilities().flying && !player.getAbilities().instabuild) {
                useCharge(player, stack, 1);
            }
        } else {
            SlotItemSupport.recharge(this, player, stack);
        }
    }
}
