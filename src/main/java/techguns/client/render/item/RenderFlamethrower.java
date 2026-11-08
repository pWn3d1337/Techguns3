package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;
import techguns.client.model.TGModel;
import techguns.client.render.fx.IScreenEffect;
import techguns.client.render.fx.ScreenEffects;

import net.minecraft.resources.ResourceLocation;

/**
 * Flamethrower: draws a small pilot flame while the gun is idle (and not under water), at the model's {@code idle_fx}
 * marker bone, else at the muzzle flash offsets.
 */
public class RenderFlamethrower extends RenderGunBase90 {
    /** The pilot flame of the Java renderer (data guns name it in {@code idle_effect}). */
    private static final IScreenEffect PILOT_FLAME = ScreenEffects.ref("flamethrower_flame");

    private static final String IDLE_FX = "idle_fx";

    public RenderFlamethrower(TGModel model, ResourceLocation texture) {
        super(model, texture);
    }

    public RenderFlamethrower(TGModel model, int parts, ResourceLocation texture) {
        super(model, parts, texture);
    }

    @Override
    protected void drawIdleFx(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, boolean leftHand, String ammoVariant) {
        if (entity != null && !entity.isUnderWater() && Minecraft.getInstance().level != null) {
            Vector3f marker = markerOffset(poseStack, IDLE_FX);
            float x = marker != null ? marker.x : leftHand ? this.muzzleEffect.offsetX_l : this.muzzleEffect.offsetX_r;
            float y = marker != null ? marker.y : this.muzzleEffect.offsetY;
            float z = marker != null ? marker.z : this.muzzleEffect.offsetZ;
            PILOT_FLAME.doRender(poseStack, buffers, pilotProgress(), x, y, z,
                    this.muzzleEffect.effectScale * 0.5f, false, ammoVariant);
        }
    }

    @Override
    protected void drawIdleFx3P(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, boolean leftHand, String ammoVariant) {
        if (entity != null && !entity.isUnderWater() && Minecraft.getInstance().level != null) {
            Vector3f marker = markerOffset(poseStack, IDLE_FX);
            float x = marker != null ? marker.x : 0;
            float y = marker != null ? marker.y : this.muzzleEffect.offsetY_3p;
            float z = marker != null ? marker.z : this.muzzleEffect.offsetZ_3p;
            PILOT_FLAME.doRender(poseStack, buffers, pilotProgress(), x, y, z,
                    this.muzzleEffect.effectScale * 0.5f * this.muzzleEffect.effectScale_3p, true, ammoVariant);
        }
    }

    private static float pilotProgress() {
        return (float) (Minecraft.getInstance().level.getGameTime() % 20) / 20.0f;
    }
}
