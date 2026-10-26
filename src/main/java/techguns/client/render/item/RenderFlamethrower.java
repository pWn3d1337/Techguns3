package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import techguns.client.model.TGModel;
import techguns.client.render.fx.IScreenEffect;

import net.minecraft.resources.ResourceLocation;

/**
 * Flamethrower: draws a small pilot flame at the muzzle while the gun is idle (and not under water).
 */
public class RenderFlamethrower extends RenderGunBase90 {

    public RenderFlamethrower(TGModel model, ResourceLocation texture) {
        super(model, texture);
    }

    public RenderFlamethrower(TGModel model, int parts, ResourceLocation texture) {
        super(model, parts, texture);
    }

    @Override
    protected void drawIdleFx(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, boolean leftHand, String ammoVariant) {
        if (entity != null && !entity.isUnderWater() && Minecraft.getInstance().level != null) {
            float p = (float) (Minecraft.getInstance().level.getGameTime() % 20) / 20.0f;
            float x = leftHand ? this.muzzleEffect.offsetX_l : this.muzzleEffect.offsetX_r;
            IScreenEffect.FlamethrowerMuzzleFlame.doRender(poseStack, buffers, p, x, this.muzzleEffect.offsetY, this.muzzleEffect.offsetZ,
                    this.muzzleEffect.effectScale * 0.5f, false, ammoVariant);
        }
    }

    @Override
    protected void drawIdleFx3P(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, boolean leftHand, String ammoVariant) {
        if (entity != null && !entity.isUnderWater() && Minecraft.getInstance().level != null) {
            float p = (float) (Minecraft.getInstance().level.getGameTime() % 20) / 20.0f;
            IScreenEffect.FlamethrowerMuzzleFlame.doRender(poseStack, buffers, p, 0, this.muzzleEffect.offsetY_3p, this.muzzleEffect.offsetZ_3p,
                    this.muzzleEffect.effectScale * 0.5f * this.muzzleEffect.effectScale_3p, true, ammoVariant);
        }
    }
}
