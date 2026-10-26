package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.render.TGMatrixOps;
import techguns.gun.GrenadeItem;

/**
 * Hand grenade renderer (Techguns 2 {@code RenderGrenade}): while the use key is held in first person the grenade
 * tilts back by up to 25 degrees (wind-up); the charge is also handed to the model as its fire progress (the frag
 * grenade hides its pin ring while winding up).
 */
public class RenderGrenade extends RenderItemBase {
    /** Wind-up time of a full charge, same as {@link GrenadeItem}'s throw charge. */
    private static final float FULL_CHARGE_TICKS = 30.0f;

    protected float baseRotY = 180.0f;

    public RenderGrenade(TGModel model, ResourceLocation texture) {
        super(model, texture);
    }

    public RenderGrenade(TGModel model, ResourceLocation texture, float baseRotationY) {
        super(model, texture);
        this.baseRotY = baseRotationY;
    }

    @Override
    protected void setBaseRotation(PoseStack poseStack, ItemDisplayContext transform) {
        TGMatrixOps.rotate(poseStack, -180.0f, 1.0f, 0, 0);
        TGMatrixOps.rotate(poseStack, baseRotY, 0f, 1.0f, 0);
    }

    @Override
    public void render(ItemRenderContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        ItemDisplayContext transform = context.displayContext();
        LivingEntity entity = context.entity();
        float chargeProgress = 0;

        poseStack.pushPose();
        this.applyTranslation(poseStack, transform);

        if (context.isFirstPerson()) {
            if (entity != null && entity.isUsingItem() && entity.getUsedItemHand() == context.hand()) {
                int duration = entity.getUseItem().getUseDuration(entity) - entity.getUseItemRemainingTicks();
                chargeProgress = Mth.clamp(duration / FULL_CHARGE_TICKS, 0f, 1f);
                TGMatrixOps.rotate(poseStack, 25.0f * chargeProgress, 1f, 0f, 0f);
            }
        } else if (ItemDisplayContext.GUI == transform) {
            TGMatrixOps.rotate(poseStack, 40.0f, 0, 1f, 0);
            TGMatrixOps.rotate(poseStack, 20.0f, 1f, 0, 0);
        } else if (ItemDisplayContext.FIXED == transform) {
            TGMatrixOps.rotate(poseStack, -90.0f, 0, 1.0f, 0);
        }

        this.setBaseScale(entity, poseStack, transform);
        this.setBaseRotation(poseStack, transform);
        this.applyBaseTranslation(poseStack);

        for (int i = 0; i < parts; i++) {
            model.render(entity, poseStack, buffers.getBuffer(model.getRenderType(texture, i)), 0, 0f, transform, i, chargeProgress, chargeProgress, light, overlay);
        }

        poseStack.popPose();
    }
}
