package techguns.client.render.entity;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import techguns.TGIds;
import techguns.client.render.TGMatrixOps;
import techguns.entity.projectile.GrapplingHookProjectile;
import techguns.gun.FirePos;

/** Grappling hook: the hook model plus a steel wire to the shooter's hand (Fabric {@code RenderGrapplingHookProjectile}). */
public class GrapplingHookProjectileRenderer extends EntityRenderer<GrapplingHookProjectile> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/guns/grappling_hook.png");
    private static final ResourceLocation TEXTURE_WIRE = TGIds.id("textures/entity/steel_wire.png");
    private static final float WIRE_WIDTH = 0.005f;
    /** Wire start relative to the shooter's eyes (forward, up, sideways for the left/right hand). */
    private static final float OFFSET_X = 0.25f;
    private static final float OFFSET_Y = -0.12f;
    private static final float OFFSET_SIDE = 0.15f;

    private final GrapplingHookModel model = new GrapplingHookModel();

    public GrapplingHookProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(GrapplingHookProjectile entity) {
        return TEXTURE;
    }

    /** The wire is visible even when the (small) hook itself is outside the view. */
    @Override
    public boolean shouldRender(GrapplingHookProjectile entity, Frustum frustum, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void render(GrapplingHookProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight) {
        poseStack.pushPose();
        TGMatrixOps.rotate(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0f, 0f, 1f, 0f);
        TGMatrixOps.rotate(poseStack, Mth.lerp(partialTick, entity.xRotO, entity.getXRot()), 0f, 0f, 1f);
        poseStack.scale(0.35f, 0.35f, 0.35f);
        model.render(null, poseStack, buffers.getBuffer(model.renderType(TEXTURE)), 0, 0f, ItemDisplayContext.NONE, 0, 0f, 0f, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        if (entity.status() != GrapplingHookProjectile.Status.NONE && entity.getOwner() instanceof LivingEntity shooter) {
            renderWire(entity, shooter, partialTick, poseStack, buffers);
        }
    }

    private void renderWire(GrapplingHookProjectile entity, LivingEntity shooter, float partialTick, PoseStack poseStack,
                            MultiBufferSource buffers) {
        BlockPos shooterPos = BlockPos.containing(shooter.getEyePosition(partialTick));
        int light = LightTexture.pack(shooter.level().getBrightness(LightLayer.BLOCK, shooterPos),
                shooter.level().getBrightness(LightLayer.SKY, shooterPos));

        float pitch = Mth.lerp(partialTick, shooter.xRotO, shooter.getXRot());
        float yaw = Mth.lerp(partialTick, shooter.yHeadRotO, shooter.yHeadRot);
        float side = entity.firePos() == FirePos.RIGHT ? OFFSET_SIDE : entity.firePos() == FirePos.LEFT ? -OFFSET_SIDE : 0.0f;
        Vec3 offset = new Vec3(OFFSET_X, OFFSET_Y, side).zRot(pitch * Mth.DEG_TO_RAD).yRot(-(yaw + 90.0f) * Mth.DEG_TO_RAD);
        Vec3 src = shooter.getEyePosition(partialTick).add(offset);

        double ex = Mth.lerp((double) partialTick, entity.xOld, entity.getX());
        double ey = Mth.lerp((double) partialTick, entity.yOld, entity.getY());
        double ez = Mth.lerp((double) partialTick, entity.zOld, entity.getZ());
        Vec3 diff = new Vec3(ex, ey, ez).subtract(src);
        float distance = (float) diff.length();
        if (distance < 1.0E-3f) {
            return;
        }
        Vec3 dir = diff.scale(1.0 / distance);
        float wirePitch = (float) Math.toDegrees(Math.asin(dir.y));
        float wireYaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));

        // the wire runs along +x of the pose: four flat quads around the axis form its cross section
        poseStack.pushPose();
        poseStack.translate(src.x - ex, src.y - ey, src.z - ez);
        TGMatrixOps.rotate(poseStack, wireYaw - 90.0f, 0f, 1f, 0f);
        TGMatrixOps.rotate(poseStack, wirePitch, 0f, 0f, 1f);
        TGMatrixOps.rotate(poseStack, 45f, 1f, 0f, 0f);
        float u2 = distance / WIRE_WIDTH * 0.25f;
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE_WIRE));
        for (int i = 0; i < 4; i++) {
            TGMatrixOps.rotate(poseStack, 90f, 1f, 0f, 0f);
            Matrix4f mat = poseStack.last().pose();
            wireVertex(consumer, poseStack, mat, 0f, -WIRE_WIDTH, 0f, 0f, light);
            wireVertex(consumer, poseStack, mat, distance, -WIRE_WIDTH, u2, 0f, light);
            wireVertex(consumer, poseStack, mat, distance, WIRE_WIDTH, u2, 1f, light);
            wireVertex(consumer, poseStack, mat, 0f, WIRE_WIDTH, 0f, 1f, light);
        }
        poseStack.popPose();
    }

    private static void wireVertex(VertexConsumer consumer, PoseStack poseStack, Matrix4f mat, float x, float y, float u, float v, int light) {
        consumer.addVertex(mat, x, y, 0f).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(poseStack.last(), 0f, 0f, 1f);
    }
}
