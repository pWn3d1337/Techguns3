package techguns.gun;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Gun that locks on to a target while the use key is held (Techguns 2 {@code GuidedMissileLauncher}): the client
 * tracks the lock (see {@code LockOnTracker}) and sends the locked target with the shot; projectiles get it through
 * {@link ShotParams#target()}.
 */
public class LockOnGunItem extends GunItem {
    /** Maximum lock-on distance (blocks). */
    public static final float LOCK_RANGE = 150.0f;

    /** Ticks on target until the lock is complete. */
    private final int lockOnTicks;
    /** Extra ticks a completed lock survives after the target leaves the crosshair. */
    private final int lockOnPersistTicks;

    public LockOnGunItem(Properties properties, GunStats stats, GunSounds sounds, ProjectileSelector projectiles, int lockOnTicks,
                         int lockOnPersistTicks) {
        super(properties, stats, sounds, projectiles);
        this.lockOnTicks = lockOnTicks;
        this.lockOnPersistTicks = lockOnPersistTicks;
    }

    public int lockOnTicks() {
        return lockOnTicks;
    }

    public int lockOnPersistTicks() {
        return lockOnPersistTicks;
    }

    @Nullable
    @Override
    public Entity acceptTarget(Player player, @Nullable Entity target) {
        // the client decides when the lock is complete; the server only rejects impossible targets
        if (target instanceof LivingEntity living && living.isAlive() && target != player
                && target.distanceToSqr(player) <= (LOCK_RANGE + 8.0) * (LOCK_RANGE + 8.0)
                && !(target instanceof Player other && !player.canHarmPlayer(other))) {
            return target;
        }
        return null;
    }
}
