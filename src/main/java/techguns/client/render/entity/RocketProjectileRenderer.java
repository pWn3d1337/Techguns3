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

/** Renders rockets and guided missiles with the shared rocket model (Techguns 2 {@code RenderRocketProjectile}). */
public class RocketProjectileRenderer<T extends RocketProjectile> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/guns/rocket.png");
    private static final ResourceLocation TEXTURE_NUKE = TGIds.id("textures/guns/rocket_nuke.png");
    private static final ResourceLocation TEXTURE_HV = TGIds.id("textures/guns/rocket_hv.png");

    private final ModelRocket model = new ModelRocket();

    public RocketProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return switch (entity.variant()) {
            case NUKE -> TEXTURE_NUKE;
            case HIGH_VELOCITY -> TEXTURE_HV;
            default -> TEXTURE;
        };
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        TGMatrixOps.rotate(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0f, 0f, 1f, 0f);
        TGMatrixOps.rotate(poseStack, Mth.lerp(partialTick, entity.xRotO, entity.getXRot()), 0f, 0f, 1f);
        poseStack.scale(0.9f, 0.9f, 0.9f);

        VertexConsumer consumer = buffers.getBuffer(model.renderType(getTextureLocation(entity)));
        model.render(null, poseStack, consumer, 0, 0f, ItemDisplayContext.NONE, 0, 0f, 0f, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
