package techguns.slot.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGAmmo;
import techguns.registry.TGAttributes;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/** Scuba tanks (Techguns 2 {@code ItemScubaTanks}): with oxygen gear (e.g. an oxygen mask) they top up the air for 1 charge. */
public class ScubaTanksItem extends TGSlotItem {
    /** Air supply at or below which the tanks kick in. */
    private static final int AIR_THRESHOLD = 280;
    private static final int AIR_REFILL = 20;

    public ScubaTanksItem(Properties properties, int durability) {
        super(properties, TGSlotType.BACK, durability);
        refill(TGAmmo.COMPRESSED_AIR_TANK, TGAmmo.COMPRESSED_AIR_TANK_EMPTY);
    }

    @Override
    public void slotTick(Player player, ItemStack stack) {
        if (player.level().isClientSide) {
            return;
        }
        SlotItemSupport.recharge(this, player, stack);
        if (isCharged(stack) && player.getAirSupply() <= AIR_THRESHOLD && player.getAttributeValue(TGAttributes.OXYGEN_GEAR) > 0.0) {
            player.setAirSupply(player.getAirSupply() + AIR_REFILL);
            useCharge(player, stack, 1);
            SlotItemSupport.sync(player);
        }
    }
}
