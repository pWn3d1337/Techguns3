package techguns.client.render.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * A billboard style effect drawn in the item's pose space (muzzle flashes, scopes, charge effects).
 */
public interface IScreenEffect {

    IScreenEffect muzzleFlashLaser = new ScreenEffect("textures/fx/laserflare02.png", 4, 4, 7, BlendMode.ADDITIVE);

    IScreenEffect muzzleFlash_rifle = new ScreenEffect("textures/fx/muzzleflashnew_add.png", 4, 4, 12, BlendMode.ADDITIVE);
    IScreenEffect muzzleFlash_gun = new ScreenEffect("textures/fx/muzzleflashnew_2_add_2.png", 4, 4, 11, BlendMode.ADDITIVE);
    IScreenEffect muzzleFlash_minigun = new ScreenEffect("textures/fx/muzzleflash_minigun.png", 2, 2, 4, BlendMode.ADDITIVE);

    IScreenEffect sniperScope = new ScreenEffect("textures/fx/testscope.png", 1, 1, 1, BlendMode.SCOPE);

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
