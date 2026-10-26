package techguns.client.render.item;

import techguns.TGIds;
import techguns.client.model.gun.ModelAK;
import techguns.client.model.gun.ModelAUG;
import techguns.client.model.gun.ModelBoltaction;
import techguns.client.model.gun.ModelGoldenRevolver;
import techguns.client.model.gun.ModelLMG;
import techguns.client.model.gun.ModelM4Infiltrator;
import techguns.client.model.gun.ModelMac10;
import techguns.client.model.gun.ModelMinigun;
import techguns.client.model.gun.ModelPistol;
import techguns.client.model.gun.ModelRevolver;
import techguns.client.model.gun.ModelScar;
import techguns.client.model.gun.ModelThompson;
import techguns.client.model.gun.ModelVector;
import techguns.client.model.gun.ModelLasergun;
import techguns.client.model.GeoGunModel;
import techguns.client.model.gun.ModelAS50;
import techguns.client.model.gun.ModelBlasterRifle;
import techguns.client.model.gun.ModelCombatShotgun;
import techguns.client.model.gun.ModelGaussRifle;
import techguns.client.model.gun.ModelLasergun2;
import techguns.client.model.gun.ModelPDW;
import techguns.client.model.gun.ModelLaserPistol;
import techguns.client.model.gun.ModelNDR;
import techguns.client.model.gun.ModelTeslaGun;
import techguns.client.model.gun.ModelAlienBlaster;
import techguns.client.model.gun.ModelMibGun;
import techguns.client.model.gun.ModelNetherBlaster;
import techguns.client.model.gun.ModelSonicShotgun;
import techguns.client.model.gun.ModelPulseRifle;
import techguns.client.model.gun.ModelSawedOff;
import techguns.client.model.item.ModelAS50Mag;
import techguns.client.model.gun.ModelM4;
import techguns.client.model.gun.ModelRocketLauncher;
import techguns.client.model.item.ModelARMagazine;
import techguns.client.model.item.ModelLmgMag;
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
        TGItemRenderers.register(TGGuns.M4, new RenderGunBase(new GeoGunModel(new ModelM4(), "m4"), TGIds.id("textures/guns/m4texture.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0)
                .setGUIScale(0.35f).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.18f, -1.29f, 0.75f, 0).setRecoilAnim(GunAnimation.genericRecoil, 0.1f, 4.0f).setTransformTranslations(new float[][]{
                        {0f, 0f, -0.05f}, // First Person
                        {0f, 0.01f, -0.1f}, // Third Person
                        {0f, 0f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.13f, -1f).setMuzzleFlashJitter(0.02f, 0.02f, 5.0f, 0.1f).setAdsOffsets(-0.001F, 0F, 0F).setScopeRecoilAnim(GunAnimation.genericRecoil, 0.05F, 2.0F));

        TGItemRenderers.register(TGGuns.ROCKET_LAUNCHER, new RenderGunBase90(new GeoGunModel(new ModelRocketLauncher(), "rocket_launcher", "rocket_launcher_rocket")
                .setVisibility((part, ammo, reload) -> part != 1 || (reload == 0 && ammo > 0) || reload > 0.5f), 2, TGIds.id("textures/guns/rocketlauncher.png")).setBaseTranslation(-0.4f, -0.2f, -RenderItemBase.SCALE * 0.5f)
                .setGUIScale(0.45f).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0, 0.39f, -0.6f, 0.87f, 0).setTransformTranslations(new float[][]{
                        {-0.13f, 0.3f, 0.32f}, // First Person
                        {0, 0.02f, 0.07f}, // Third Person
                        {-0.06f, 0.0f, 0.0f}, // GUI
                        {0.0f, 0.0f, 0}, // Ground
                        {0, 0, 0f} // frame
                }).setMuzzleFXPos3P(0.09f, -0.26f));

        TGItemRenderers.register(TGGuns.LASERGUN, new RenderGunBase90(new GeoGunModel(new ModelLasergun(), "lasergun"), 1, TGIds.id("textures/guns/lasergun.png")).setBaseTranslation(0.25f, -0.2f, RenderItemBase.SCALE * 0.5f - 0.10f)
                .setBaseScale(1.1f).setGUIScale(0.40f).setMuzzleFx(ScreenEffect.muzzleFlashLaser, 0, 0.30f, -1.06f, 0.5f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.15f, 0.04f}, // First Person
                        {0f, 0.02f, 0.01f}, // Third Person
                        {0.13f, 0.01f, 0f}, // GUI
                        {0f, 0f, 0.15f}, // Ground
                        {-0.18f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.11f, -0.83f).setRecoilAnim(GunAnimation.genericRecoil, 0.2f, 5.0f).setAdsOffsets(-0.004F, -0.092F, 0F).setScopeRecoilAnim(GunAnimation.genericRecoil, 0.1F, 2.0F));

        TGItemRenderers.register(TGGuns.M4_INFILTRATOR, new RenderGunBase(new GeoGunModel(new ModelM4Infiltrator(), "m4_infiltrator"), 1, TGIds.id("textures/guns/m4_uq_texture.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0)
                .setGUIScale(0.35f)/*.setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.18f, -1.5f, 0.5f,0)*/.setRecoilAnim(GunAnimation.genericRecoil, 0.1f, 4.0f).setTransformTranslations(new float[][]{
                        {0f, 0f, -0.05f}, // First Person
                        {0f, 0.01f, -0.1f}, // Third Person
                        {0f, 0f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.14f, -1.15f).setScope(ScreenEffect.sniperScope).setScopeRecoilAnim(GunAnimation.scopeRecoil, 0.05f, 1.0f));

        TGItemRenderers.register(TGGuns.SCAR, new RenderGunBase(new GeoGunModel(new ModelScar(), "scar", "scar_1"), 2, TGIds.id("textures/guns/scar_texture.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0.1f)
                .setGUIScale(0.35f).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.23f, -1.48f, 0.78f, 0).setRecoilAnim(GunAnimation.genericRecoil, 0.1f, 4.0f).setTransformTranslations(new float[][]{
                        {0f, 0.04f, -0.15f}, // First Person
                        {0f, 0.02f, -0.11f}, // Third Person
                        {0.05f, 0f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0.02f, -0.09f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.14f, -1.04f).setMuzzleFlashJitter(0.02f, 0.02f, 5.0f, 0.1f).setScope(ScreenEffect.sniperScope).setScopeRecoilAnim(GunAnimation.scopeRecoil, 0.05f, 2.0f));

        TGItemRenderers.register(TGGuns.AK47, new RenderGunBase(new GeoGunModel(new ModelAK(), "ak47"), 1, TGIds.id("textures/guns/ak47texture.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0).setBaseScale(0.75f)
                .setGUIScale(0.35f).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.18f, -1.36f, 0.8f, 0).setRecoilAnim(GunAnimation.genericRecoil, 0.1f, 4.0f).setTransformTranslations(new float[][]{
                        {0f, 0.06f, -0.02f}, // First Person
                        {0f, 0f, -0.08f}, // Third Person
                        {0.06f, -0.01f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.08f, -1.02f).setMuzzleFlashJitter(0.02f, 0.02f, 5.0f, 0.1f));

        TGItemRenderers.register(TGGuns.AUG, new RenderGunBase(new GeoGunModel(new ModelAUG(), "aug", "aug_1"), 2, TGIds.id("textures/guns/augtexture.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0.1f)
                .setGUIScale(0.35f).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.19f, -1.45f, 0.75f, 0).setRecoilAnim(GunAnimation.genericRecoil, 0.1f, 4.0f).setTransformTranslations(new float[][]{
                        {0f, 0.01f, -0.15f}, // First Person
                        {0f, 0.0f, -0.01f}, // Third Person
                        {0f, 0f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0.02f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.12f, -0.87f).setMuzzleFlashJitter(0.02f, 0.02f, 5.0f, 0.1f).setScope(ScreenEffect.sniperScope).setScopeRecoilAnim(GunAnimation.scopeRecoil, 0.075f, 1.0f));

        TGItemRenderers.register(TGGuns.MINIGUN, new RenderGunBase90(new GeoGunModel(new ModelMinigun(), "minigun", "minigun_1"), 2, TGIds.id("textures/guns/minigun.png")).setBaseTranslation(0, -0.2f, RenderItemBase.SCALE * 0.5f).setBaseScale(1.2f)
                .setGUIScale(0.3f).setMuzzleFx(ScreenEffect.muzzleFlash_minigun, -0.04f, 0.05f, -1.25f, 0.8f, 0.04f).setTransformTranslations(new float[][]{
                        {0f, -0.16f, 0.1f}, // First Person
                        {0f, -0.53f, 0.2f}, // Third Person
                        {0.12f, -0.02f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(-0.38f, -0.78f).setRecoilAnim(GunAnimation.genericRecoil, 0.05f, 3.0f));

        TGItemRenderers.register(TGGuns.PISTOL, new RenderGunBase(new GeoGunModel(new ModelPistol(), "pistol", "pistol_1"), 2, TGIds.id("textures/guns/pistol3.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.3f, -0.4f)
                .setBaseScale(1.2f).setGUIScale(0.9f).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.03f, 0.2f, -0.5f, 0.55f, -0.03f).setTransformTranslations(new float[][]{
                        {0, 0.09f, -0.02f}, // First Person
                        {0.0f, -0.03f, 0.0f}, // Third Person
                        {0.02f, -0.08f, 0}, // GUI
                        {0.02f, -0.08f, 0}, // Ground
                        {0, 0, 0f} // frame
                }).setRecoilAnim(GunAnimation.genericRecoil, 0.025f, 12.0f).setMuzzleFlashJitter(0.01f, 0.01f, 5.0f, 0.05f).setMuzzleFXPos3P(0.07f, -0.26f));

        TGItemRenderers.register(TGGuns.MAC10, new RenderGunBase(new GeoGunModel(new ModelMac10(), "mac10"), 1, TGIds.id("textures/guns/mac10texture.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.45f, -0.3f)
                .setBaseScale(1.2f).setGUIScale(0.55f).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.23f, -0.46f, 0.5f, 0).setRecoilAnim(GunAnimation.genericRecoil, 0.06f, 3.0f).setTransformTranslations(new float[][]{
                        {0f, 0f, -0.05f}, // First Person
                        {0f, -0.10f, 0.01f}, // Third Person
                        {-0.02f, -0.02f, 0f}, // GUI
                        {0f, 0.03f, 0f}, // Ground
                        {0f, -0.05f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.07f, -0.26f).setMuzzleFlashJitter(0.02f, 0.05f, 5.0f, 0.1f));

        TGItemRenderers.register(TGGuns.VECTOR, new RenderGunBase(new GeoGunModel(new ModelVector(), "vector"), 1, TGIds.id("textures/guns/vector_texture.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.57f, -0.2f)
                .setBaseScale(1.1f).setGUIScale(0.45f).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.10f, -0.72f, 0.60f, 0).setRecoilAnim(GunAnimation.genericRecoil, 0.05f, 2.0f).setTransformTranslations(new float[][]{
                        {0f, -0.17f, 0.0f}, // First Person
                        {0f, -0.2f, -0.04f}, // Third Person
                        {-0.08f, -0.09f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0.05f, -0.17f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.0f, -0.53f).setMuzzleFlashJitter(0.02f, 0.02f, 5.0f, 0.1f));

        TGItemRenderers.register(TGGuns.LMG, new RenderGunBase(new GeoGunModel(new ModelLMG(), "lmg"), 1, TGIds.id("textures/guns/mg2_texture.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0)
                .setGUIScale(0.35f).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.21f, -1.5f, 0.78f, 0).setMuzzleFXPos3P(0.13f, -0.98f).setRecoilAnim(GunAnimation.genericRecoil, 0.01f, 2.0f).setTransformTranslations(new float[][]{
                        {0f, 0.02f, -0.09f}, // First Person
                        {0f, 0f, -0.06f}, // Third Person
                        {0.05f, -0.03f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.14f, -1.02f).setMuzzleFlashJitter(0.03f, 0.03f, 5.0f, 0.1f).setAdsOffsets(-0.001f, -0.013f, 0f));

        TGItemRenderers.register(TGGuns.THOMPSON, new RenderGunBase90(new GeoGunModel(new ModelThompson(), "thompson"), 1, TGIds.id("textures/guns/thompson.png")).setBaseTranslation(0, -0.2f, RenderItemBase.SCALE * 0.5f - 0.1f)
                .setGUIScale(0.45f).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.14f, -0.75f, 0.55f, 0).setMuzzleFXPos3P(0.1f, -0.59f).setMuzzleFlashJitter(0.02f, 0.02f, 5.0f, 0.1f));

        TGItemRenderers.register(TGGuns.BOLT_ACTION, new RenderGunBase90(new GeoGunModel(new ModelBoltaction(), "bolt_action"), 1, TGIds.id("textures/guns/boltactionrifle.png")).setBaseTranslation(0, -0.2f, RenderItemBase.SCALE - 0.1f).setBaseScale(1.35f)
                .setGUIScale(0.35f).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0, 0.21f, -1.48f, 0.9f, 0).setScope(ScreenEffect.sniperScope).setTransformTranslations(new float[][]{
                        {0f, -0.02f, -0.09f}, // First Person
                        {0f, -0.04f, -0.11f}, // Third Person
                        {0.1f, -0.08f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.025f} // frame
                }).setMuzzleFXPos3P(0.12f, -1.13f).setScopeRecoilAnim(GunAnimation.scopeRecoil, 0.20f, 2.0f));

        TGItemRenderers.register(TGGuns.REVOLVER, new RenderGunBase90(new GeoGunModel(new ModelRevolver(), "revolver"), 1, TGIds.id("textures/guns/revolver.png")).setBaseTranslation(-0.35f, -0.2f, RenderItemBase.SCALE * 0.5f - 0.1f)
                .setBaseScale(0.75f).setGUIScale(0.75f).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0, 0.25f, -0.41f, 0.5f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.14f, 0.01f}, // First Person
                        {0f, 0.0f, 0.0f}, // Third Person
                        {0.05f, 0.01f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, 0f} // frame
                }).setMuzzleFXPos3P(0.09f, -0.30f).setRecoilAnim(GunAnimation.genericRecoil, 0.1f, 10.0f));

        TGItemRenderers.register(TGGuns.GOLDEN_REVOLVER, new RenderGunBase90(new GeoGunModel(new ModelGoldenRevolver(), "golden_revolver"), 1, TGIds.id("textures/guns/goldenrevolver.png")).setBaseTranslation(-0.35f, -0.2f, RenderItemBase.SCALE * 0.5f - 0.1f)
                .setBaseScale(0.75f).setGUIScale(0.75f).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0, 0.25f, -0.41f, 0.5f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.14f, 0.01f}, // First Person
                        {0f, 0.0f, 0.0f}, // Third Person
                        {0.05f, 0.01f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, 0f} // frame
                }).setMuzzleFXPos3P(0.09f, -0.30f).setRecoilAnim(GunAnimation.genericRecoil, 0.1f, 15.0f));

        TGItemRenderers.register(TGGuns.AS50, new RenderGunBase(new GeoGunModel(new ModelAS50(), "as50"), 1, TGIds.id("textures/guns/as50texture.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0)
                .setBaseScale(0.85f).setGUIScale(0.30f).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0, 0.29f, -1.82f, 1.15f, 0).setRecoilAnim(GunAnimation.genericRecoil, 0.25f, 4.0f).setTransformTranslations(new float[][]{
                        {0f, 0.06f, -0.1f}, // First Person
                        {0f, 0.0f, -0.05f}, // Third Person
                        {0.13f, -0.09f, -0.05f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, -0.2f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.17f, -1.29f).setScope(ScreenEffect.sniperScope).setRecoilAnim(GunAnimation.genericRecoil, 0.25f, 5.0f).setScopeRecoilAnim(GunAnimation.scopeRecoil, 0.2f, 2.0f));

        TGItemRenderers.register(TGGuns.COMBAT_SHOTGUN, new RenderGunBase90(new GeoGunModel(new ModelCombatShotgun(), "combat_shotgun", "combat_shotgun_1"), 2, TGIds.id("textures/guns/combatshotgun.png")).setBaseTranslation(0, -0.2f, RenderItemBase.SCALE - 0.1f)
                .setGUIScale(0.45f).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0, 0.21f, -0.91f, 0.75f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.03f, 0f}, // First Person
                        {0f, -0.01f, -0.10f}, // Third Person
                        {0.05f, 0f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.12f, -0.81f).setRecoilAnim(GunAnimation.genericRecoil, 0.3f, 15.0f));

        TGItemRenderers.register(TGGuns.SAWED_OFF, new RenderGunBase90(new GeoGunModel(new ModelSawedOff(), "sawed_off"), 1, TGIds.id("textures/guns/sawedoff.png")).setBaseTranslation(0, -0.2f, RenderItemBase.SCALE - 0.1f)
                .setGUIScale(0.45f).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0, 0.16f, -0.75f, 1.05f, 0).setReloadAnim(GunAnimation.breechReload, -0.15f, 55.0f).setReloadAnim3p(GunAnimation.breechReload, 0f, 55.0f).setTransformTranslations(new float[][]{
                        {0f, 0f, 0f}, // First Person
                        {0f, -0.04f, 0f}, // Third Person
                        {0f, 0f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.06f, -0.49f).setRecoilAnim(GunAnimation.genericRecoil, 0.2f, 20.0f));

        TGItemRenderers.register(TGGuns.BLASTER_RIFLE, new RenderGunBase(new GeoGunModel(new ModelBlasterRifle(), "blaster_rifle"), 1, TGIds.id("textures/guns/blasterrifle.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0)
                .setBaseScale(0.9f).setGUIScale(0.45f).setMuzzleFx(ScreenEffect.muzzleFlashLaser, 0, 0.24f, -0.93f, 0.75f, 0).setRecoilAnim(GunAnimation.genericRecoil, 0.1f, 4.0f).setTransformTranslations(new float[][]{
                        {0f, 0.11f, -0.20f}, // First Person
                        {0f, 0.0f, -0.04f}, // Third Person
                        {0f, 0f, 0.03f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.09f, -0.57f).setScope(ScreenEffect.sniperScope).setScopeRecoilAnim(GunAnimation.scopeRecoil, 0.05f, 1.0f));

        TGItemRenderers.register(TGGuns.BLASTER_SHOTGUN, new RenderGunBase(new GeoGunModel(new ModelLasergun2(), "blaster_shotgun", "blaster_shotgun_1"), 2, TGIds.id("textures/guns/lasergunnew.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0.1f)
                .setGUIScale(0.35f).setMuzzleFx(ScreenEffect.muzzleFlashLaser, 0, 0.22f, -1.09f, 0.75f, 0).setRecoilAnim(GunAnimation.genericRecoil, 0.1f, 4.0f).setTransformTranslations(new float[][]{
                        {0f, 0.04f, -0.05f}, // First Person
                        {0f, 0.01f, -0.1f}, // Third Person
                        {0f, 0f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.14f, -0.82f).setAdsOffsets(-0.002F, -0.048F, 0F));

        TGItemRenderers.register(TGGuns.GAUSS_RIFLE, new RenderGunBase90(new GeoGunModel(new ModelGaussRifle(), "gauss_rifle"), 1, TGIds.id("textures/guns/gaussrifle.png"))
                .setBaseTranslation(-0.6f, 0f, RenderItemBase.SCALE * 0.5f - 0.09f)
                .setBaseScale(0.9f).setGUIScale(0.25f).setMuzzleFx(ScreenEffect.muzzleFlashSonic, 0, 0.21f, -1.56f, 1.0f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.12f, -0.1f}, // First Person
                        {0f, 0.05f, -0.17f}, // Third Person
                        {0.f, 0.06f, 0.06f}, // GUI
                        {0f, 0f, 0.f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.09f, -1.26f).setRecoilAnim(GunAnimation.genericRecoil, 0.25f, 7.5f).setScope(ScreenEffect.techScope, 2.125f).setScopeRecoilAnim(GunAnimation.scopeRecoil, 0.15f, 1.0f));

        TGItemRenderers.register(TGGuns.PDW, new RenderGunBase90(new GeoGunModel(new ModelPDW(), "pdw"), 1, TGIds.id("textures/guns/pdw.png")).setBaseTranslation(0, -0.2f, RenderItemBase.SCALE * 1.5f - 0.1f)
                .setGUIScale(0.55f).setMuzzleFx(ScreenEffect.muzzleFlash_blue, 0, 0.13f, -0.58f, 0.55f, 0).setTransformTranslations(new float[][]{
                        {0.0f, 0.09f, -0.04f}, // First Person
                        {0f, 0.06f, -0.02f}, // Third Person
                        {0f, 0f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.11f, -0.41f).setMuzzleFlashJitter(0.01f, 0.01f, 5.0f, 0.1f).setRecoilAnim(GunAnimation.genericRecoil, 0.06f, 4.0f));

        TGItemRenderers.register(TGGuns.PULSE_RIFLE, new RenderGunBase90(new GeoGunModel(new ModelPulseRifle(), "pulse_rifle"), 1, TGIds.id("textures/guns/pulserifle.png")).setBaseTranslation(0, -0.2f, RenderItemBase.SCALE * 1.5f - 0.09f)
                .setGUIScale(0.45f).setMuzzleFx(ScreenEffect.muzzleFlash_blue, 0, 0.22f, -0.76f, 0.6f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.16f, 0.01f}, // First Person
                        {0f, 0.05f, 0.08f}, // Third Person
                        {0.05f, 0f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.1f, -0.50f).setScope(ScreenEffect.techScope, 2.125f).setScopeRecoilAnim(GunAnimation.scopeRecoil, 0.10f, 1.5f).setRecoilAnim(GunAnimation.pulseRifleRecoil, 0.25f, 10.0f));

        TGItemRenderers.register(TGGuns.LASER_PISTOL, new RenderGunBase(new GeoGunModel(new ModelLaserPistol(), "laser_pistol"), 1, TGIds.id("textures/guns/laser_pistol.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.3f, -0.4f)
                .setBaseScale(1.2f).setGUIScale(0.7f).setMuzzleFx(ScreenEffect.muzzleFlashLaser, 0.03f, 0.2f, -0.5f, 0.55f, -0.03f).setTransformTranslations(new float[][]{
                        {0, 0.09f, -0.02f}, // First Person
                        {0.0f, -0.03f, 0.0f}, // Third Person
                        {0.02f, -0.08f, 0}, // GUI
                        {0.02f, -0.08f, 0}, // Ground
                        {0, 0, 0f} // frame
                }).setRecoilAnim(GunAnimation.genericRecoil, 0.0125f, 6.0f).setMuzzleFXPos3P(0.07f, -0.26f));

        TGItemRenderers.register(TGGuns.NUCLEAR_DEATHRAY, new RenderGunBase90(new GeoGunModel(new ModelNDR(), "nuclear_deathray", "nuclear_deathray_1"), 2, TGIds.id("textures/guns/ndr.png")).setBaseTranslation(1f, -0.2f, RenderItemBase.SCALE * 1.5f - 0.09f)
                .setBaseScale(1.2f).setGUIScale(0.40f).setMuzzleFx(ScreenEffect.muzzleFlashNukeBeam, 0, 0.19f, -0.91f, 0.65f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.02f, 0.09f}, // First Person
                        {-0.01f, 0.04f, 0.3f}, // Third Person
                        {0.11f, -0.08f, 0f}, // GUI
                        {0f, 0f, 0.15f}, // Ground
                        {-0.23f, -0.08f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.11f, -0.83f).setRecoilAnim(GunAnimation.swayRecoil, 0.025f, 0.75f));

        TGItemRenderers.register(TGGuns.TESLA_GUN, new RenderGunBase90(new GeoGunModel(new ModelTeslaGun(), "tesla_gun"), 1, TGIds.id("textures/guns/teslagun.png")).setBaseTranslation(0.25f, -0.2f, RenderItemBase.SCALE - 0.09f)
                .setGUIScale(0.45f).setMuzzleFx(ScreenEffect.muzzleFlashLightning, 0, 0.26f, -0.67f, 0.5f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.15f, 0.04f}, // First Person
                        {0f, 0.05f, -0.08f}, // Third Person
                        {0.03f, 0.01f, 0f}, // GUI
                        {0f, 0f, 0f}, // Ground
                        {0f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.12f, -0.65f).setRecoilAnim(GunAnimation.genericRecoil, 0.15f, 5.0f).setAdsOffsets(0.001f, -0.036f, 0f).setScopeRecoilAnim(GunAnimation.genericRecoil, 0.05f, 2.0f));

        TGItemRenderers.register(TGGuns.ALIEN_BLASTER, new RenderGunBase(new GeoGunModel(new ModelAlienBlaster(), "alien_blaster"), 1, TGIds.id("textures/guns/alien_blaster.png")).setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.3f, -0.2f)
                .setBaseScale(0.75f).setGUIScale(0.75f).setMuzzleFx(ScreenEffect.muzzleFlashAlienBlaster, 0f, 0.28f, -0.52f, 0.55f, 0f).setTransformTranslations(new float[][]{
                        {0, 0.06f, -0.02f}, // First Person
                        {0.0f, -0.07f, -0.01f}, // Third Person
                        {0.02f, -0.08f, 0}, // GUI
                        {0.02f, -0.08f, 0}, // Ground
                        {-0.04f, -0.09f, 0f} // frame
                }).setRecoilAnim(GunAnimation.genericRecoil, 0.025f, 10.0f).setMuzzleFXPos3P(0.07f, -0.36f).setReloadAnim(GunAnimation.breechReload, -0.15f, 55.0f).setReloadAnim3p(GunAnimation.breechReload, 0f, 55.0f));

        TGItemRenderers.register(TGGuns.MIB_GUN, new RenderGunBase(new GeoGunModel(new ModelMibGun(), "mib_gun"), 1, TGIds.id("textures/guns/mibgun.png")).setBaseTranslation(0f, -0.56f, -0.02f)
                .setBaseScale(1.2f).setGUIScale(0.75f).setMuzzleFx(ScreenEffect.muzzleFlashMibGun, 0f, 0.26f, -0.42f, 0.55f, 0f).setTransformTranslations(new float[][]{
                        {0, 0.10f, -0.02f}, // First Person
                        {0.0f, -0.04f, -0.01f}, // Third Person
                        {0.02f, -0.08f, 0}, // GUI
                        {0.02f, -0.08f, 0}, // Ground
                        {-0.04f, -0.09f, 0f} // frame
                }).setRecoilAnim(GunAnimation.genericRecoil, 0.025f, 10.0f).setMuzzleFXPos3P(0.06f, -0.31f).setReloadAnim(GunAnimation.breechReload, -0.15f, 55.0f).setReloadAnim3p(GunAnimation.breechReload, 0f, 55.0f));

        TGItemRenderers.register(TGGuns.NETHER_BLASTER, new RenderGunBase(new GeoGunModel(new ModelNetherBlaster(), "nether_blaster"), 1, TGIds.id("textures/guns/cyberdemonblaster.png")).setBaseTranslation(0f, -0.2f, RenderItemBase.SCALE - 0.09f)
                .setGUIScale(0.60f).setMuzzleFx(ScreenEffect.muzzleFlashFireball_alpha, 0, 0.29f, -0.33f, 0.5f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.15f, 0.04f}, // First Person
                        {0f, -0.16f, -0.24f}, // Third Person
                        {-0.10f, 0.01f, 0f}, // GUI
                        {0f, 0f, -0.11f}, // Ground
                        {0.16f, -0.07f, -0.16f} // frame
                }).setMuzzleFXPos3P(-0.06f, -0.45f));

        TGItemRenderers.register(TGGuns.SONIC_SHOTGUN, new RenderGunBase90(new GeoGunModel(new ModelSonicShotgun(), "sonic_shotgun"), TGIds.id("textures/guns/sonicshotgun.png")).setBaseTranslation(0, -0.2f, RenderItemBase.SCALE * 0.5f - 0.10f)
                .setBaseScale(1.0f).setGUIScale(0.35f).setMuzzleFx(ScreenEffect.muzzleFlashSonic, 0, 0.28f, -0.98f, 0.5f, 0).setTransformTranslations(new float[][]{
                        {0f, 0.10f, 0.04f}, // First Person
                        {0f, -0.01f, -0.03f}, // Third Person
                        {0.08f, 0.0f, 0f}, // GUI
                        {0f, 0.05f, 0.15f}, // Ground
                        {-0.18f, 0f, -0.05f} // frame
                }).setMuzzleFXPos3P(0.11f, -0.81f));

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

        float[][] lmgmagTranslations = {
                {0, 0.35f, 0}, // First Person
                {0f, 0.15f, 0.0f}, // Third Person
                {0.1f, 0.25f, 0}, // GUI
                {0, 0f, 0}, // Ground
                {0, 0.25f, -0.05f} // frame
        };

        float[][] as50magTranslations = {
                {0, 0f, 0}, // First Person
                {0f, 0f, 0f}, // Third Person
                {0f, 0.05f, 0}, // GUI
                {0, -0.1f, 0}, // Ground
                {0, 0, 0f} // frame
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

        TGItemRenderers.register(TGAmmo.ASSAULT_RIFLE_MAGAZINE_INCENDIARY, new RenderItemBase(new ModelARMagazine(false), TGIds.id("textures/guns/ar_mag_inc.png")).setBaseScale(1.25f).setGUIScale(0.85f).setBaseTranslation(0, -0.2f, 0).setTransformTranslations(m4magTranslations));

        TGItemRenderers.register(TGAmmo.LMG_MAGAZINE, new RenderItemLMGMag(new ModelLmgMag(false), TGIds.id("textures/guns/lmg_mag.png")).setBaseScale(1.25f).setGUIScale(0.75f).setBaseTranslation(0, 0f, 0.2f).setTransformTranslations(lmgmagTranslations));
        TGItemRenderers.register(TGAmmo.LMG_MAGAZINE_EMPTY, new RenderItemLMGMag(new ModelLmgMag(true), TGIds.id("textures/guns/lmg_mag.png")).setBaseScale(1.25f).setGUIScale(0.75f).setBaseTranslation(0, 0f, 0.2f).setTransformTranslations(lmgmagTranslations));
        TGItemRenderers.register(TGAmmo.LMG_MAGAZINE_INCENDIARY, new RenderItemLMGMag(new ModelLmgMag(false), TGIds.id("textures/guns/lmg_mag_inc.png")).setBaseScale(1.25f).setGUIScale(0.75f).setBaseTranslation(0, 0f, 0.2f).setTransformTranslations(lmgmagTranslations));

        TGItemRenderers.register(TGAmmo.AS50_MAGAZINE, new RenderItemBase(new ModelAS50Mag(false), TGIds.id("textures/guns/as50_mag.png")).setBaseScale(1.5f).setGUIScale(0.75f).setBaseTranslation(0.0325f, -0.2f, 0.33f).setTransformTranslations(as50magTranslations));
        TGItemRenderers.register(TGAmmo.AS50_MAGAZINE_EMPTY, new RenderItemBase(new ModelAS50Mag(true), TGIds.id("textures/guns/as50_mag.png")).setBaseScale(1.5f).setGUIScale(0.75f).setBaseTranslation(0.0325f, -0.2f, 0.33f).setTransformTranslations(as50magTranslations));
        TGItemRenderers.register(TGAmmo.AS50_MAGAZINE_INCENDIARY, new RenderItemBase(new ModelAS50Mag(false), TGIds.id("textures/guns/as50_mag_inc.png")).setBaseScale(1.5f).setGUIScale(0.75f).setBaseTranslation(0.0325f, -0.2f, 0.33f).setTransformTranslations(as50magTranslations));
        TGItemRenderers.register(TGAmmo.AS50_MAGAZINE_EXPLOSIVE, new RenderItemBase(new ModelAS50Mag(false), TGIds.id("textures/guns/as50_mag_exp.png")).setBaseScale(1.5f).setGUIScale(0.75f).setBaseTranslation(0.0325f, -0.2f, 0.33f).setTransformTranslations(as50magTranslations));

        TGItemRenderers.register(TGAmmo.ROCKET, new RenderItemBaseRocketItem(new ModelRocket(), TGIds.id("textures/guns/rocket.png")).setBaseScale(1.5f).setGUIScale(0.5f).setBaseTranslation(0, 0, 0.1f).setTransformTranslations(rocketTranslations).setFirstPersonScale(0.35f));
    }
}
