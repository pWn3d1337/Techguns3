package techguns.client.render.entity;

import java.util.Random;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import techguns.TGIds;
import techguns.client.render.TGMatrixOps;
import techguns.client.render.TGRenderTypes;
import techguns.entity.projectile.GenericProjectile;

/**
 * Renders a bullet as a glowing tracer: four crossed quads along the flight direction, drawn additively and
 * fully bright (Techguns 2 {@code GenericProjectileRenderer}).
 */
public class GenericProjectileRenderer extends EntityRenderer<GenericProjectile> {
    private static final ResourceLocation BULLET_TEXTURE = TGIds.id("textures/entity/bullet1.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float HALF_LENGTH = 10f;
    private static final float HALF_WIDTH = 1.25f;

    public GenericProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(GenericProjectile entity) {
        return BULLET_TEXTURE;
    }

    @Override
    public void render(GenericProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        // hide the bullet for its first moments so it does not pop up inside the muzzle
        if (!(entity.tickCount >= 2 || (entity.tickCount == 1 && partialTick > 0.35f))) {
            return;
        }
        float angleX = (float) new Random(entity.getId()).nextGaussian();

        poseStack.pushPose();
        TGMatrixOps.rotate(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0f, 0f, 1f, 0f);
        TGMatrixOps.rotate(poseStack, Mth.lerp(partialTick, entity.xRotO, entity.getXRot()), 0f, 0f, 1f);
        TGMatrixOps.rotate(poseStack, 360.0f * angleX, 1f, 0f, 0f);
        poseStack.scale(0.05625f, 0.05625f, 0.05625f);
        poseStack.translate(-4.0, 0.0, 0.0);

        VertexConsumer consumer = buffers.getBuffer(TGRenderTypes.projectileAdditive(getTextureLocation(entity)));
        int r = 255, g = 255, b = 255;
        if (entity.kind() == GenericProjectile.Kind.INCENDIARY) {
            g = 170;
            b = 80;
        }
        for (int i = 0; i < 4; i++) {
            TGMatrixOps.rotate(poseStack, 45.0f, 1f, 0f, 0f);
            Matrix4f mat = poseStack.last().pose();
            quad(consumer, mat, r, g, b);
        }
        poseStack.popPose();
    }

    /** POSITION_COLOR_TEX_LIGHTMAP quad; the render type does not cull. */
    private static void quad(VertexConsumer c, Matrix4f mat, int r, int g, int b) {
        float[][] v = {
                {-HALF_LENGTH, -HALF_WIDTH, 0f, 0f, 0f},
                {HALF_LENGTH, -HALF_WIDTH, 0f, 1f, 0f},
                {HALF_LENGTH, HALF_WIDTH, 0f, 1f, 1f},
                {-HALF_LENGTH, HALF_WIDTH, 0f, 0f, 1f},
        };
        for (int i = 0; i < 4; i++) {
            float[] p = v[i];
            c.addVertex(mat, p[0], p[1], p[2]).setColor(r, g, b, 255).setUv(p[3], p[4])
                    .setLight(FULL_BRIGHT);
        }
    }
}
