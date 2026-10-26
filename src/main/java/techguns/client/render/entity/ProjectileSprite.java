package techguns.client.render.entity;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;

/**
 * Camera-facing textured quad for sprite projectiles (Fabric {@code RenderTextureProjectile}): 1 x 1 unit quad, its
 * centre half a unit above the entity position, scaled to {@code size} blocks.
 */
final class ProjectileSprite {
    private ProjectileSprite() {
    }

    /** Draws into a POSITION_COLOR_TEX_LIGHTMAP buffer; the pose stack must be at the entity position. */
    static void draw(PoseStack poseStack, VertexConsumer consumer, float size, int light) {
        poseStack.pushPose();
        poseStack.scale(size, size, size);
        // MIGRATION(26.x): camera access changes with the render state extraction
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        Matrix4f mat = poseStack.last().pose();
        consumer.addVertex(mat, -0.5f, -0.25f, 0f).setColor(255, 255, 255, 255).setUv(0f, 1f).setLight(light);
        consumer.addVertex(mat, 0.5f, -0.25f, 0f).setColor(255, 255, 255, 255).setUv(1f, 1f).setLight(light);
        consumer.addVertex(mat, 0.5f, 0.75f, 0f).setColor(255, 255, 255, 255).setUv(1f, 0f).setLight(light);
        consumer.addVertex(mat, -0.5f, 0.75f, 0f).setColor(255, 255, 255, 255).setUv(0f, 0f).setLight(light);
        poseStack.popPose();
    }
}
