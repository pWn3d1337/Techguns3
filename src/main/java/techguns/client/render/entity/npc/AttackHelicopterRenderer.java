package techguns.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import techguns.TGIds;
import techguns.client.model.ObjMesh;
import techguns.client.render.TGMatrixOps;
import techguns.entity.npc.AttackHelicopter;
import techguns.entity.npc.TGNpc;

/**
 * Techguns 2 {@code RenderAttackHelicopter}: three OBJ meshes (hull, rotor, nose gun) scaled by 2.5; the rotor spins,
 * the gun tilts down with the pitch, and the wreck spirals down while dying.
 */
// MIGRATION(26.x): entity renderers take render states in 1.21.2+.
public class AttackHelicopterRenderer extends EntityRenderer<TGNpc> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/entity/apache.png");
    private static final ObjMesh HULL = new ObjMesh(TGIds.id("models/obj/helicopter0.obj"), true);
    private static final ObjMesh ROTOR = new ObjMesh(TGIds.id("models/obj/helicopter1.obj"), true);
    private static final ObjMesh GUN = new ObjMesh(TGIds.id("models/obj/helicopter2.obj"), true);
    private static final float SCALE = 2.5f;
    /** The gun's pivot on the model's x axis (the nose). */
    private static final float GUN_PIVOT_X = 1.2f;

    public AttackHelicopterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 5.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(TGNpc entity) {
        return TEXTURE;
    }

    @Override
    public void render(TGNpc entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        int overlay = OverlayTexture.NO_OVERLAY;
        float rotorTime = (entity.level().getGameTime() % 60) + partialTick;

        poseStack.pushPose();
        TGMatrixOps.rotate(poseStack, -Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0f, 0f, 1f, 0f);
        poseStack.scale(SCALE, SCALE, SCALE);
        if (entity.deathTime == 0) {
            HULL.render(poseStack, vertices, packedLight, overlay);

            poseStack.pushPose();
            TGMatrixOps.rotate(poseStack, rotorTime * 24.0f, 0f, 1f, 0f);
            ROTOR.render(poseStack, vertices, packedLight, overlay);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(GUN_PIVOT_X, 0, 0);
            TGMatrixOps.rotate(poseStack, -Mth.lerp(partialTick, entity.xRotO, entity.getXRot()), 0f, 0f, 1f);
            poseStack.translate(-GUN_PIVOT_X, 0, 0);
            GUN.render(poseStack, vertices, packedLight, overlay);
            poseStack.popPose();
        } else {
            float progress = (entity.deathTime + partialTick) / AttackHelicopter.MAX_DEATH_TIME;
            float p = progress * progress;
            poseStack.translate(0, p * -4.0f, 0);
            // Techguns 2 applied this spin twice (GL11 and GlStateManager)
            TGMatrixOps.rotate(poseStack, p * 1440.0f, 0f, 1f, 0f);
            HULL.render(poseStack, vertices, packedLight, overlay);
            GUN.render(poseStack, vertices, packedLight, overlay);
            TGMatrixOps.rotate(poseStack, rotorTime * 12.0f, 0f, 1f, 0f);
            ROTOR.render(poseStack, vertices, packedLight, overlay);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }
}
