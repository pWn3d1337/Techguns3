package techguns.slot.item;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotItem;

/**
 * Behaviour shared by the extra slot items (Techguns 2 {@code ItemTGSpecialSlot*} helpers).
 */
final class SlotItemSupport {
    private static final List<Holder<MobEffect>> GAS_EFFECTS = List.of(MobEffects.POISON, MobEffects.WITHER);

    private SlotItemSupport() {
    }

    /** Server side: tells clients about a charge change (the fallback slots are synced, Curios does it itself). */
    static void sync(Player player) {
        if (player instanceof ServerPlayer) {
            ExtraSlots.markChanged(player);
        }
    }

    /** Recharges empty gear from the inventory and syncs when that worked. */
    static void recharge(TGSlotItem item, Player player, ItemStack stack) {
        if (!TGSlotItem.isCharged(stack)) {
            item.tryRecharge(player, stack);
            if (TGSlotItem.isCharged(stack)) {
                sync(player);
            }
        }
    }

    /** Removes poison and wither, 1 charge each (server side). */
    static void removeGasEffects(Player player, ItemStack stack) {
        if (player.level().isClientSide) {
            return;
        }
        for (Holder<MobEffect> effect : GAS_EFFECTS) {
            if (TGSlotItem.isCharged(stack) && player.hasEffect(effect)) {
                player.removeEffect(effect);
                TGSlotItem.useCharge(player, stack, 1);
                sync(player);
            }
        }
    }

    /** Techguns 2 {@code ItemGlider.glide}: sneaking in the air slows the fall and pushes along the look direction. */
    // MIGRATION(26.x): Player movement API (setDeltaMovement, fallDistance) may change.
    static void glide(Player player) {
        if (!player.onGround() && player.isShiftKeyDown()) {
            Vec3 motion = player.getDeltaMovement();
            if (motion.y < -0.1) {
                Vec3 look = player.getLookAngle();
                double push = Math.min(0.08, -0.1 - motion.y);
                player.setDeltaMovement(motion.x + look.x * push, motion.y + push, motion.z + look.z * push);
            }
            player.fallDistance = 0;
        }
    }
}
