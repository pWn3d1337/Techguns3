package techguns.client.render.item;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import techguns.client.gun.ClientGunController;
import techguns.client.gun.LockOnTracker;
import techguns.client.gun.ShooterValues;
import techguns.entity.projectile.GrapplingHookProjectile;
import techguns.gun.ChargeGunItem;
import techguns.gun.GrapplingHookItem;
import techguns.gun.GunData;
import techguns.gun.GunItem;
import techguns.gun.LockOnGunItem;

/**
 * Animation state of a gun for one frame, extracted before rendering.
 *
 * @param fireProgress        recoil animation progress 0..1 (0 = not recoiling)
 * @param reloadProgress      reload animation progress 0..1 (0 = not reloading)
 * @param muzzleFlashProgress muzzle flash progress 0..1 (0 = no flash)
 * @param chargeProgress      charge 0..1 for charge weapons (fades out during the recoil after release)
 * @param chargeProgressFull  like chargeProgress while charging, but keeps growing past 1 (charge FX pulse), 0 otherwise
 * @param zoomProgress        0 = hip, 1 = fully aimed down sights (first person main hand only)
 * @param zooming             the local player is zoomed with this gun
 * @param loadedAmmo          rounds in the gun
 * @param ammoVariant         loaded ammo variant key
 * @param muzzleFlashSeed     differs for every muzzle flash (holder, hand and flash start): seeds the flash jitter
 * @param fireCycle           identifies the running recoil (its timer start), 0 if none: keyframe events fire once per run
 * @param reloadCycle         identifies the running reload, 0 if none
 * @param grapple             grappling hook of the holder: 0 = in the gun, 1 = launched, -1 = attached or pulling
 * @param locked              first person, and the local player's lock-on with the gun in the main hand is complete (GML holo)
 */
public record GunRenderState(float fireProgress, float reloadProgress, float muzzleFlashProgress, float chargeProgress,
                             float chargeProgressFull, float zoomProgress, boolean zooming, int loadedAmmo, String ammoVariant,
                             long muzzleFlashSeed, long fireCycle, long reloadCycle, int grapple,
                             boolean locked) {

    /** Dev: fixed fire, reload, zoom and charge progress of the local player's held guns ({@code /tgdebug pose}); null = live. */
    @Nullable
    public static float[] debugPose;

    public static GunRenderState extract(ItemRenderContext context, GunItem gun) {
        float fire = 0f;
        float reload = 0f;
        float flash = 0f;
        float charge = 0f;
        float chargeFull = 0f;
        long flashSeed = 0L;
        long fireCycle = 0L;
        long reloadCycle = 0L;
        if (gun instanceof ChargeGunItem chargeGun && context.isInHand() && !context.isOffhand() && context.entity() != null
                && context.entity().isUsingItem() && context.entity().getUseItem().getItem() == gun) {
            chargeFull = Math.max(0f, chargeGun.chargeFull(context.entity()));
            charge = Mth.clamp(chargeFull, 0f, 1f);
        }
        if (context.isInHand() && context.entity() != null && context.hand() != null) {
            ShooterValues values = ShooterValues.of(context.entity().getId());
            ShooterValues.Timer reloadTimer = values.reload(context.hand());
            ShooterValues.Timer recoilTimer = values.recoil(context.hand());
            if (reloadTimer.isActive()) {
                reload = reloadTimer.progress();
                reloadCycle = reloadTimer.startId();
            } else if (recoilTimer.isActive()) {
                fire = recoilTimer.progress();
                fireCycle = recoilTimer.startId();
                if (gun instanceof ChargeGunItem) {
                    charge = (1.0f - fire) * values.recoilCharge(context.hand());
                }
            }
            ShooterValues.Timer flashTimer = values.muzzleFlash(context.hand());
            if (flashTimer.isActive()) {
                flash = Mth.clamp(flashTimer.progress(), 0.0001f, 1f);
                flashSeed = flashTimer.startId() * 31L + context.entity().getId() * 2L + context.hand().ordinal();
            }
        }

        float zoomProgress = 0f;
        boolean zooming = false;
        if (context.isFirstPerson() && !context.isOffhand()) {
            ClientGunController controller = ClientGunController.get();
            zooming = controller.isZooming() && gun.stats().zoom().isPresent();
            zoomProgress = controller.zoomProgress(context.partialTick());
        }
        if (debugPose != null && context.isInHand() && context.entity() == Minecraft.getInstance().player) {
            fire = debugPose[0];
            reload = debugPose[1];
            zoomProgress = debugPose[2];
            charge = debugPose[3];
            chargeFull = debugPose[3];
        }
        return new GunRenderState(fire, reload, flash, charge, chargeFull, zoomProgress, zooming, GunData.loaded(context.stack()), GunData.variant(context.stack()),
                flashSeed, fireCycle, reloadCycle,
                gun instanceof GrapplingHookItem && context.isInHand() && context.entity() != null ? grappleStatus(context.entity()) : 0,
                context.isFirstPerson() && context.entity() != null
                        && context.entity().getMainHandItem().getItem() instanceof LockOnGunItem lockOn && LockOnTracker.isLocked(lockOn));
    }

    /**
     * Grappling state of {@code holder}, from its hook entity in the client level: 0 = hook in the gun, 1 = hook launched
     * (the wheel unrolls), -1 = hook attached or pulling (the wheel rolls back).
     */
    public static int grappleStatus(LivingEntity holder) {
        if (holder.level() instanceof ClientLevel level) {
            for (Entity entity : level.entitiesForRendering()) {
                if (entity instanceof GrapplingHookProjectile hook && hook.getOwner() == holder && hook.isAlive()) {
                    return switch (hook.status()) {
                        case NONE -> 0;
                        case LAUNCHING -> 1;
                        default -> -1;
                    };
                }
            }
        }
        return 0;
    }
}
