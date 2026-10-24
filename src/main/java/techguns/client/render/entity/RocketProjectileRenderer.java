package techguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.TGIds;
import techguns.client.model.item.ModelRocket;
import techguns.client.render.TGMatrixOps;
import techguns.entity.projectile.RocketProjectile;

/** Renders a rocket with the shared rocket model (Techguns 2 {@code RenderRocketProjectile}). */
public class RocketProjectileRenderer extends EntityRenderer<RocketProjectile> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/guns/rocket.png");

    private final ModelRocket model = new ModelRocket();

    public RocketProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketProjectile entity) {
        return TEXTURE;
    }

    @Override
    public void render(RocketProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        TGMatrixOps.rotate(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0f, 0f, 1f, 0f);
        TGMatrixOps.rotate(poseStack, Mth.lerp(partialTick, entity.xRotO, entity.getXRot()), 0f, 0f, 1f);
        poseStack.scale(0.9f, 0.9f, 0.9f);

        VertexConsumer consumer = buffers.getBuffer(model.renderType(getTextureLocation(entity)));
        model.render(null, poseStack, consumer, 0, 0f, ItemDisplayContext.NONE, 0, 0f, 0f, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        // TODO FX system: rocket smoke / flame trail (Techguns 2 spawned it from the entity tick)
    }
}
