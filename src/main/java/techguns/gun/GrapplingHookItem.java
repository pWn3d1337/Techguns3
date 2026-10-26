package techguns.gun;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Grappling hook (Fabric {@code GrapplingHook}): pressing the use key shoots the hook at once, and holding it keeps the
 * hook pulling (see {@code GrapplingHookProjectile}); releasing the key ends the pull and fires nothing.
 */
public class GrapplingHookItem extends ChargeGunItem {
    public GrapplingHookItem(Properties properties, GunStats stats, GunSounds sounds, ProjectileSelector projectiles, float fullChargeTicks,
                             int ammoPerFullCharge) {
        super(properties, stats, sounds, projectiles, fullChargeTicks, ammoPerFullCharge);
    }

    /** Only the use key shoots the hook; a left click shot would be dropped at once as the key is not held. */
    @Override
    public boolean shootsWithLeftClick() {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (ShooterState.of(player).fireDelay(hand) > 0) {
            return InteractionResultHolder.pass(stack);
        }
        if (GunData.loaded(stack) <= 0) {
            startReload(stack, level, player, hand, false);
            return InteractionResultHolder.pass(stack);
        }
        // shoots before the use starts: charge guns cannot shoot while using
        shoot(stack, level, player, hand, false, null);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        // the hook notices the released key itself
    }
}
