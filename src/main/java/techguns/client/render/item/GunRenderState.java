package techguns.client.render.item;

import net.minecraft.util.Mth;
import techguns.client.gun.ClientGunController;
import techguns.client.gun.ShooterValues;
import techguns.gun.ChargeGunItem;
import techguns.gun.GunData;
import techguns.gun.GunItem;

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
 */
public record GunRenderState(float fireProgress, float reloadProgress, float muzzleFlashProgress, float chargeProgress,
                             float chargeProgressFull, float zoomProgress, boolean zooming, int loadedAmmo, String ammoVariant) {

    public static GunRenderState extract(ItemRenderContext context, GunItem gun) {
        float fire = 0f;
        float reload = 0f;
        float flash = 0f;
        float charge = 0f;
        float chargeFull = 0f;
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
            } else if (recoilTimer.isActive()) {
                fire = recoilTimer.progress();
                if (gun instanceof ChargeGunItem) {
                    charge = (1.0f - fire) * values.recoilCharge(context.hand());
                }
            }
            ShooterValues.Timer flashTimer = values.muzzleFlash(context.hand());
            if (flashTimer.isActive()) {
                flash = Mth.clamp(flashTimer.progress(), 0.0001f, 1f);
            }
        }

        float zoomProgress = 0f;
        boolean zooming = false;
        if (context.isFirstPerson() && !context.isOffhand()) {
            ClientGunController controller = ClientGunController.get();
            zooming = controller.isZooming() && gun.stats().zoom().isPresent();
            zoomProgress = controller.zoomProgress(context.partialTick());
        }
        return new GunRenderState(fire, reload, flash, charge, chargeFull, zoomProgress, zooming, GunData.loaded(context.stack()), GunData.variant(context.stack()));
    }
}
