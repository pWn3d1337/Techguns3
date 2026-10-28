package techguns.slot.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import techguns.player.TGPlayerData;
import techguns.registry.TGAmmo;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/** Jetpack (Techguns 2 {@code ItemJetpack}): thrust while the boost key is held, 1 charge per tick; also glides. */
public class JetpackItem extends TGSlotItem {
    public JetpackItem(Properties properties, int durability) {
        super(properties, TGSlotType.BACK, durability);
        refill(TGAmmo.FUEL_TANK, TGAmmo.FUEL_TANK_EMPTY);
    }

    @Override
    public void slotTick(Player player, ItemStack stack) {
        if (!isCharged(stack)) {
            SlotItemSupport.recharge(this, player, stack);
            return;
        }
        TGPlayerData data = TGPlayerData.of(player);
        if (data.jetpackBoost() && data.get(TGPlayerData.Toggle.JETPACK)) {
            applyBoost(player, stack);
        }
        SlotItemSupport.glide(player);
    }

    /** Techguns 2 {@code applyBoostToPlayer}. */
    protected void applyBoost(Player player, ItemStack stack) {
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, Math.min(motion.y + 0.15, 0.4), motion.z);
        player.fallDistance = 0;
        useCharge(player, stack, 1);
    }
}
