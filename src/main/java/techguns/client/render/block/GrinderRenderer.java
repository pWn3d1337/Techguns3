package techguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import techguns.TGIds;
import techguns.gun.GunItem;
import techguns.machine.GrinderBlockEntity;
import techguns.machine.TGMachineBlock;

/**
 * Techguns 2 {@code RenderGrinder}: the two grinder rolls turning against each other and the item sinking between them
 * while it is ground. The housing is the block model.
 */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class GrinderRenderer implements BlockEntityRenderer<GrinderBlockEntity> {
    private static final ResourceLocation ROLL_TEXTURE = TGIds.id("textures/block/grinder_roll.png");
    private static final float UNIT = 1.0f / 16.0f;
    /** Roll radius and length (Techguns 2 pixel sizes). */
    private static final float RADIUS = 1.5f * UNIT;
    private static final float START = 2.0f * UNIT;
    private static final float LENGTH = 12.0f * UNIT;

    public GrinderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GrinderBlockEntity grinder, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Direction facing = grinder.getBlockState().getValue(TGMachineBlock.FACING);
        boolean working = grinder.isWorking();
        float progress = working ? grinder.getProgress(partialTick) : 0.0f;
        float rotation = working ? (45.0f + 1080.0f * progress) % 360.0f : 45.0f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.5, 0.0, -0.5);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(ROLL_TEXTURE));
        renderRoll(poseStack, consumer, light, overlay, 6.5f * UNIT, rotation);
        renderRoll(poseStack, consumer, light, overlay, 9.5f * UNIT, -rotation);

        ItemStack item = grinder.getGrinding();
        if (working && !item.isEmpty()) {
            boolean gun = item.getItem() instanceof GunItem;
            poseStack.pushPose();
            poseStack.translate(0.5, 0.6 - progress * 0.4 + (gun ? 0.15 : 0.0), 0.5);
            if (gun) {
                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.GROUND, light, overlay, poseStack, buffer,
                    grinder.getLevel(), 0);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    /** One roll along X at height 7.5 px and depth {@code z}, turned by {@code degrees} around its axis. */
    private static void renderRoll(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float z, float degrees) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 7.5f * UNIT, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(degrees));
        PoseStack.Pose pose = poseStack.last();
        float x0 = START;
        float x1 = START + LENGTH;
        float r = RADIUS;
        // Techguns 2 texture coordinates: 2 px in, the roll length along u, the diameter along v
        float u0 = 2.0f * UNIT;
        float u1 = u0 + LENGTH;
        float v0 = 2.0f * UNIT;
        float v1 = v0 + 2.0f * r;
        quad(pose, consumer, light, overlay, 0, 1, 0, x0, r, -r, x0, r, r, x1, r, r, x1, r, -r, u0, v0, u1, v1);
        quad(pose, consumer, light, overlay, 0, 0, 1, x0, r, r, x0, -r, r, x1, -r, r, x1, r, r, u0, v0, u1, v1);
        quad(pose, consumer, light, overlay, 0, -1, 0, x0, -r, r, x0, -r, -r, x1, -r, -r, x1, -r, r, u0, v0, u1, v1);
        quad(pose, consumer, light, overlay, 0, 0, -1, x0, -r, -r, x0, r, -r, x1, r, -r, x1, -r, -r, u0, v0, u1, v1);
        poseStack.popPose();
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float nx, float ny, float nz,
                             float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz,
                             float dx, float dy, float dz, float u0, float v0, float u1, float v1) {
        vertex(pose, consumer, light, overlay, nx, ny, nz, ax, ay, az, u0, v0);
        vertex(pose, consumer, light, overlay, nx, ny, nz, bx, by, bz, u0, v1);
        vertex(pose, consumer, light, overlay, nx, ny, nz, cx, cy, cz, u1, v1);
        vertex(pose, consumer, light, overlay, nx, ny, nz, dx, dy, dz, u1, v0);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float nx, float ny, float nz,
                               float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
    }
}
