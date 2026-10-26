package techguns.slot.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.player.TGPlayerData;
import techguns.registry.TGAmmo;
import techguns.registry.TGAttributes;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/** Tactical mask (Techguns 2 {@code ItemTacticalMask}): night vision, oxygen gear and gas protection in one. */
public class TacticalMaskItem extends TGSlotItem {
    public TacticalMaskItem(Properties properties, int durability) {
        super(properties, TGSlotType.FACE, durability);
        bonus(TGAttributes.NIGHT_VISION, 1.0);
        bonus(TGAttributes.OXYGEN_GEAR, 1.0);
        refill(TGAmmo.ENERGY_CELL, TGAmmo.ENERGY_CELL_EMPTY);
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
        SlotItemSupport.removeGasEffects(player, stack);
    }
}
