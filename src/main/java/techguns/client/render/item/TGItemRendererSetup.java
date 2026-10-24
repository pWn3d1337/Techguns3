package techguns.client.render.item;

import techguns.TGIds;
import techguns.client.model.gun.ModelLasergun;
import techguns.client.model.gun.ModelM4;
import techguns.client.model.gun.ModelRocketLauncher;
import techguns.client.model.item.ModelARMagazine;
import techguns.client.model.item.ModelRocket;
import techguns.client.render.fx.ScreenEffect;
import techguns.registry.TGAmmo;
import techguns.registry.TGGuns;

/**
 * Creates and registers all item renderers (Techguns 2 {@code ClientProxy} item renderer section).
 */
public final class TGItemRendererSetup {
    private TGItemRendererSetup() {
    }

    static void registerAll() {
        TGItemRenderers.register(TGGuns.M4, new RenderGunBase(new ModelM4(), TGIds.id("textures/guns/m4texture.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0)
                .setGUIScale(0.35f).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.18f, -1.29f, 0.75f, 0).setRecoilAnim(GunAnimation.genericRecoil, 0.1f, 4.0f).setTransformTranslations(new float[][]{
                        {0f, 0f, -0.05f}, // First Person
                        {0f, 0.01f, -0.1f}, // Third Person
                        {0f, 0f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.13f, -1f).setMuzzleFlashJitter(0.02f, 0.02f, 5.0f, 0.1f).setAdsOffsets(-0.001F, 0F, 0F).setScopeRecoilAnim(GunAnimation.genericRecoil, 0.05F, 2.0F));

        TGItemRenderers.register(TGGuns.ROCKET_LAUNCHER, new RenderGunBase90(new ModelRocketLauncher(), 2, TGIds.id("textures/guns/rocketlauncher.png")).setBaseTranslation(-0.4f, -0.2f, -RenderItemBase.SCALE * 0.5f)
                .setGUIScale(0.45f).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0, 0.39f, -0.6f, 0.87f, 0).setTransformTranslations(new float[][]{
                        {-0.13f, 0.3f, 0.32f}, // First Person
                        {0, 0.02f, 0.07f}, // Third Person
                        {-0.06f, 0.0f, 0.0f}, // GUI
                        {0.0f, 0.0f, 0}, // Ground
                        {0, 0, 0f} // frame
                }).setMuzzleFXPos3P(0.09f, -0.26f));

        TGItemRenderers.register(TGGuns.LASERGUN, new RenderGunBase90(new ModelLasergun(), 1, TGIds.id("textures/guns/lasergun.png")).setBaseTranslation(0.25f, -0.2f, RenderItemBase.SCALE * 0.5f - 0.10f)
                .setBaseScale(1.1f).setGUIScale(0.40f).setMuzzleFx(ScreenEffect.muzzleFlashLaser, 0, 0.30f, -1.06f, 0.5f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.15f, 0.04f}, // First Person
                        {0f, 0.02f, 0.01f}, // Third Person
                        {0.13f, 0.01f, 0f}, // GUI
                        {0f, 0f, 0.15f}, // Ground
                        {-0.18f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.11f, -0.83f).setRecoilAnim(GunAnimation.genericRecoil, 0.2f, 5.0f).setAdsOffsets(-0.004F, -0.092F, 0F).setScopeRecoilAnim(GunAnimation.genericRecoil, 0.1F, 2.0F));

        registerAmmo();
    }

    private static void registerAmmo() {
        float[][] m4magTranslations = {
                {0, 0.25f, 0}, // First Person
                {0f, 0.25f, 0.05f}, // Third Person
                {0.1f, 0.25f, 0}, // GUI
                {0, -0.1f, 0}, // Ground
                {0, 0, -0.05f} // frame
        };

        float[][] rocketTranslations = {
                {0, 0f, 0f}, // First Person
                {0f, -0.1f, 0.02f}, // Third Person
                {0.0f, 0.0f, 0}, // GUI
                {0.0f, 0.0f, 0}, // Ground
                {0, 0, 0f} // frame
        };

        TGItemRenderers.register(TGAmmo.ASSAULT_RIFLE_MAGAZINE, new RenderItemBase(new ModelARMagazine(false), TGIds.id("textures/guns/ar_mag.png")).setBaseScale(1.25f).setGUIScale(0.85f).setBaseTranslation(0, -0.2f, 0).setTransformTranslations(m4magTranslations));
        TGItemRenderers.register(TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY, new RenderItemBase(new ModelARMagazine(true), TGIds.id("textures/guns/ar_mag.png")).setBaseScale(1.25f).setGUIScale(0.85f).setBaseTranslation(0, -0.2f, 0).setTransformTranslations(m4magTranslations));

        TGItemRenderers.register(TGAmmo.ROCKET, new RenderItemBaseRocketItem(new ModelRocket(), TGIds.id("textures/guns/rocket.png")).setBaseScale(1.5f).setGUIScale(0.5f).setBaseTranslation(0, 0, 0.1f).setTransformTranslations(rocketTranslations).setFirstPersonScale(0.35f));
    }
}
