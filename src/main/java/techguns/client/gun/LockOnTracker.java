package techguns.client.gun;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import techguns.gun.LockOnGunItem;
import techguns.registry.TGSounds;

/**
 * Client lock-on state of the local player (Techguns 2 {@code TGExtendedPlayer.lockOnEntity/lockOnTicks} and
 * {@code GuidedMissileLauncher.traceTarget}): while the use key is held with a {@link LockOnGunItem}, the entity in the
 * crosshair is locked after {@link LockOnGunItem#lockOnTicks()} ticks.
 */
public final class LockOnTracker {
    /** Extra hitbox size when tracing for targets (Techguns 2 {@code LOCK_ERROR_THRESHOLD}). */
    private static final double LOCK_ERROR_THRESHOLD = 0.5;

    @Nullable
    private static Entity target;
    private static int ticks;
    private static int useTicks;

    private LockOnTracker() {
    }

    @Nullable
    public static Entity target() {
        return target;
    }

    /** Lock progress in ticks; at or above the gun's lock-on ticks the lock is complete. */
    public static int ticks() {
        return ticks;
    }

    public static boolean isLocked(LockOnGunItem gun) {
        return target != null && ticks >= gun.lockOnTicks();
    }

    /** Target id to send with a shot: the locked entity, or -1. */
    public static int lockedTargetId(LockOnGunItem gun) {
        return isLocked(gun) ? target.getId() : -1;
    }

    public static void reset() {
        target = null;
        ticks = 0;
        useTicks = 0;
    }

    /** Called every client tick while a lock-on gun is held without the use key: the lock runs out. */
    public static void decay() {
        if (target != null && !target.isAlive()) {
            reset();
        } else if (ticks > 0) {
            ticks--;
        } else {
            target = null;
        }
    }

    /** Called every client tick while the use key is held with a lock-on gun. */
    public static void tick(LocalPlayer player, LockOnGunItem gun) {
        Entity seen = trace(player);
        if (seen != null) {
            if (target != null && target.isAlive()) {
                if (target == seen) {
                    ticks++;
                    if (ticks >= gun.lockOnTicks()) {
                        ticks = gun.lockOnTicks() + gun.lockOnPersistTicks();
                    }
                } else if (ticks > 0) {
                    ticks--;
                } else {
                    target = null;
                    ticks = 0;
                }
            } else {
                target = seen;
                ticks = 1;
            }
        } else if (target != null && !target.isAlive()) {
            target = null;
            ticks = 0;
        } else if (ticks > 0) {
            ticks--;
        } else {
            target = null;
        }
        beep(player, gun);
        useTicks++;
    }

    private static void beep(LocalPlayer player, LockOnGunItem gun) {
        if (ticks >= gun.lockOnTicks()) {
            if (useTicks % 4 == 0) {
                play(player, TGSounds.LOCKED_BEEP.get(), 1.0f);
            }
        } else if (ticks > 0) {
            if (useTicks % 4 == 0) {
                play(player, TGSounds.LOCKON_BEEP.get(), 1.0f);
            }
        } else if (useTicks % 12 == 0) {
            play(player, TGSounds.LOCKON_BEEP.get(), 0.5f);
        }
    }

    private static void play(LocalPlayer player, SoundEvent sound, float volume) {
        player.level().playLocalSound(player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, volume, 1.0f, false);
    }

    /** Nearest living entity along the view ray, preferring the current target (Techguns 2 {@code findEntityOnPath}). */
    @Nullable
    private static Entity trace(LocalPlayer player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0f).scale(LockOnGunItem.LOCK_RANGE));
        HitResult block = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        Entity best = null;
        double bestDistance = Double.MAX_VALUE;
        AABB search = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0);
        for (Entity entity : player.level().getEntities(player, search, e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator()
                && !player.isPassengerOfSameVehicle(e))) {
            Optional<Vec3> hit = entity.getBoundingBox().inflate(LOCK_ERROR_THRESHOLD).clip(start, end);
            if (hit.isEmpty()) {
                continue;
            }
            if (entity == target) {
                return entity;
            }
            double distance = start.distanceToSqr(hit.get());
            if (distance < bestDistance) {
                best = entity;
                bestDistance = distance;
            }
        }
        if (best instanceof Player other && !player.canHarmPlayer(other)) {
            return null;
        }
        return best;
    }
}
