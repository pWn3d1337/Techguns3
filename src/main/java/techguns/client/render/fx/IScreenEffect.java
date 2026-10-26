package techguns.client.render.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;

import net.minecraft.client.renderer.MultiBufferSource;
import techguns.gun.ammo.AmmoType;

/**
 * A billboard style effect drawn in the item's pose space (muzzle flashes, scopes, charge effects).
 */
public interface IScreenEffect {

    IScreenEffect muzzleFlashLaser = new ScreenEffect("textures/fx/laserflare02.png", 4, 4, 7, BlendMode.ADDITIVE);
    IScreenEffect muzzleFlashSonic = new ScreenEffect("textures/fx/sonicwave4x4.png", 4, 4, 16, BlendMode.ADDITIVE);

    IScreenEffect muzzleFlashNukeBeam = new ScreenEffect("textures/fx/nukebeamflare.png", 4, 4, 16, BlendMode.ADDITIVE);
    IScreenEffect muzzleFlashFireball_alpha = new ScreenEffect("textures/fx/fireball_4x4_alpha.png", 4, 4, 16, BlendMode.ALPHA);
    IScreenEffect muzzleFlashAlienBlaster = new ScreenEffect("textures/fx/alienflare.png", 4, 4, 14, BlendMode.ADDITIVE);
    IScreenEffect muzzleFlashLightning = new ScreenEffect("textures/fx/teslaflare01.png", 4, 4, 14, BlendMode.ADDITIVE);
    IScreenEffect muzzleFlashMibGun = new ScreenEffect("textures/fx/alienflare.png", 4, 4, 14, BlendMode.ADDITIVE).setColor(0.15686f, 1.0f, 0.549f, 1f);

    IScreenEffect muzzleFlash_rifle = new ScreenEffect("textures/fx/muzzleflashnew_add.png", 4, 4, 12, BlendMode.ADDITIVE);
    IScreenEffect muzzleFlash_gun = new ScreenEffect("textures/fx/muzzleflashnew_2_add_2.png", 4, 4, 11, BlendMode.ADDITIVE);
    IScreenEffect muzzleFlash_minigun = new ScreenEffect("textures/fx/muzzleflash_minigun.png", 2, 2, 4, BlendMode.ADDITIVE);

    IScreenEffect muzzleFlash_blue = new ScreenEffect("textures/fx/bluemuzzleflash.png", 4, 4, 8, BlendMode.ADDITIVE).setFlipAxis(true, true);

    IScreenEffect FlamethrowerMuzzleFlame = new ScreenEffect("textures/fx/flamethrower2.png", 4, 4, 16, BlendMode.ADDITIVE).setFlipAxis(false, true);
    IScreenEffect FlamethrowerMuzzleFlash = new ScreenEffect("textures/fx/flamethrower.png", 4, 4, 16, BlendMode.ADDITIVE).setFlipAxis(false, true);

    IScreenEffect muzzleFlashTFG_glow = new ScreenEffect("textures/fx/lensflare1.png", 1, 1, 1, BlendMode.ADDITIVE).setFade(FadeType.SMOOTH).setColor(0.25f, 1.0f, 0.25f, 0.75f);
    IScreenEffect muzzleFlashTFG_flash = new ScreenEffect("textures/fx/lensflare4.png", 1, 1, 1, BlendMode.ADDITIVE).setFade(FadeType.FAST).setColor(0.25f, 1.0f, 0.25f, 1.0f);
    IScreenEffect muzzleFlashTFG_main = new ScreenEffect("textures/fx/tfg_flare.png", 4, 4, 16, BlendMode.ADDITIVE).setFade(FadeType.FAST);
    IScreenEffect muzzleFlashTFG = new MultiScreenEffect().add(muzzleFlashTFG_main, 0, 0.75f, 1.0f).add(muzzleFlashTFG_glow, 0, 1.0f, 0.75f).add(muzzleFlashTFG_flash, 0, 0.35f, 1.35f);

    IScreenEffect muzzleGreenFlare = new ScreenEffect("textures/fx/lensflare1.png", 1, 1, 1, BlendMode.ADDITIVE).setFade(FadeType.SMOOTH).setColor(0.5f, 1.0f, 0.25f, 1.0f);

    IScreenEffect magicRifleCharge = new VariableScreenEffect(
            Map.<String, IScreenEffect>entry(AmmoType.DEFAULT, magicCircle("textures/fx/magic_circle_arcane.png", 0.75f, 0.75f, 0.75f)),
            Map.<String, IScreenEffect>entry(AmmoType.FIRE, magicCircle("textures/fx/magic_circle_fire.png", 0.75f, 0.75f, 0.75f)),
            Map.<String, IScreenEffect>entry(AmmoType.LIGHTNING, magicCircle("textures/fx/magic_circle_01.png", 0.55f, 0.75f, 1.0f))
    ).setDefaultEffect(magicCircle("textures/fx/magic_circle_01.png", 1.0f, 1.0f, 1.0f));

    private static ScreenEffect magicCircle(String texture, float r, float g, float b) {
        return new ScreenEffect(texture, 1, 1, 1, BlendMode.ADDITIVE).setFade(FadeType.RISE_SMOOTH_AND_PULSE_FAST).setColor(r, g, b, 1.0f)
                .setDynamicScale(FadeType.RISE_SMOOTH, 5.0f, 10.0f).setDrawMode3p(ScreenEffect.DrawMode3p.PLANE_Z);
    }

    IScreenEffect sniperScope = new ScreenEffect("textures/fx/testscope.png", 1, 1, 1, BlendMode.SCOPE);
    IScreenEffect techScope = new ScreenEffect("textures/fx/techscope.png", 1, 1, 1, BlendMode.SCOPE).setFlipAxis(false, true);

    default void doRender(PoseStack poseStack, MultiBufferSource buffers, float progress, float offsetX, float offsetY, float offsetZ, float scale, boolean is3p, String ammoVariant) {
        doRender(poseStack, buffers, progress, offsetX, offsetY, offsetZ, scale, 0, 0, 0, is3p, ammoVariant);
    }

    void doRender(PoseStack poseStack, MultiBufferSource buffers, float progress, float offsetX, float offsetY, float offsetZ, float scale,
                  float rotX, float rotY, float rotZ, boolean is3p, String ammoVariant);

    enum FadeType {
        SMOOTH, FAST, NONE, RISE_SMOOTH, RISE_SMOOTH_AND_PULSE_FAST;

        public float getValueAt(float progress) {
            switch (this) {
                case FAST: {
                    progress = Math.max(0.0f, Math.min(1.0f, progress));
                    return (float) ((1.0 - Math.cos(Math.sqrt(progress) * 2.0 * Math.PI)) * 0.5);
                }
                case SMOOTH: {
                    progress = Math.max(0.0f, Math.min(1.0f, progress));
                    double d2 = Math.sin(Math.PI * progress);
                    return (float) (d2 * d2);
                }
                case RISE_SMOOTH: {
                    if (progress <= 1.0f) {
                        double d2 = Math.sin(Math.PI * 0.5 * progress);
                        return (float) (d2 * d2);
                    } else {
                        return 1.0f;
                    }
                }
                case RISE_SMOOTH_AND_PULSE_FAST: {
                    if (progress <= 1.0f) {
                        double d2 = Math.sin(Math.PI * 0.5 * progress);
                        return (float) (d2 * d2);
                    } else {
                        progress = progress - (int) progress;
                        return (float) (1.0f - (0.5f * Math.pow(Math.sin(progress * Math.PI * 2), 2)));
                    }
                }
                case NONE:
                default:
                    return 1.0f;
            }
        }
    }

    /** Blend behaviour of an effect (Techguns 2 {@code IScreenEffect.RenderType}). */
    enum BlendMode {
        ALPHA, ADDITIVE, SOLID, ALPHA_SHADED, NO_Z_TEST, SCOPE
    }
}
