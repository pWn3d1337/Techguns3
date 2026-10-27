package techguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import techguns.TGIds;
import techguns.machine.OreDrillBlockEntity;

/**
 * Techguns 2 {@code RenderOreDrill}: the formed drill's blocks are plain block models (rods and the drill plate are
 * hidden); this draws the drill bit through the rod section, a stack of four cubes per rod block, every other one
 * turned by 45 degrees and shrinking towards the cluster, spinning while the drill works. Without a head only a thin
 * rod is drawn.
 */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class OreDrillRenderer implements BlockEntityRenderer<OreDrillBlockEntity> {
    private static final ResourceLocation[] HEADS = {TGIds.id("textures/block/drillhead.png"), TGIds.id("textures/block/drillhead_obsidian.png"),
            TGIds.id("textures/block/drillhead_carbon.png")};
    private static final int CUBES_PER_BLOCK = 4;
    private static final float CUBE_STEP = 1.0f / CUBES_PER_BLOCK;

    public OreDrillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(OreDrillBlockEntity drill, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (!drill.isFormed() || drill.getLevel() == null || drill.getRods() <= 0) {
            return;
        }
        Direction direction = drill.getDrillDirection();
        poseStack.pushPose();
        // local frame: +Y runs from the controller centre along the drill
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(direction.getRotation());
        boolean spinning = drill.getDrillType() > 0 && drill.isWorking() && !drill.isStalled();
        if (spinning) {
            poseStack.mulPose(Axis.YP.rotationDegrees(((drill.getLevel().getGameTime() % 36) + partialTick) * 10.0f));
        }
        if (drill.getDrillType() > 0) {
            renderBit(drill, poseStack, buffer, HEADS[Math.min(drill.getDrillType(), HEADS.length) - 1]);
        } else {
            renderRod(drill, poseStack, buffer);
        }
        poseStack.popPose();
    }

    /** Techguns 2 {@code drawDriller}: log-tapered cubes, half a block high, a quarter block apart. */
    private void renderBit(OreDrillBlockEntity drill, PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(texture));
        int rods = drill.getRods();
        int count = rods * CUBES_PER_BLOCK;
        int drillRadius = drill.drillRadius();
        float radiusFactor = switch (drillRadius) {
            case 0, 1 -> 0.35f;
            case 2 -> 0.5f;
            case 3 -> 0.55f;
            default -> 0.6f;
        };
        float max = (float) Math.log(count + 1.0f);
        float start = drill.getEngines() + 0.5f;
        for (int i = 0; i < count; i++) {
            float size = (drillRadius == 0 ? 1.0f : drillRadius) * (float) Math.log(count - i + 1.0f) / max * radiusFactor;
            float y = start + (i + 1) * CUBE_STEP;
            int light = lightAt(drill, drill.getEngines() + 1 + i / CUBES_PER_BLOCK);
            poseStack.pushPose();
            poseStack.translate(0.0f, y, 0.0f);
            if (i % 2 == 1) {
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0f));
            }
            cube(consumer, poseStack.last(), size, CUBE_STEP, size, light);
            poseStack.popPose();
        }
    }

    /** Techguns 2 {@code drawDrillRod}: one thin turned cube per rod block. */
    private void renderRod(OreDrillBlockEntity drill, PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(HEADS[0]));
        for (int i = 0; i < drill.getRods(); i++) {
            int block = drill.getEngines() + 1 + i;
            poseStack.pushPose();
            poseStack.translate(0.0f, block, 0.0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0f));
            cube(consumer, poseStack.last(), 0.2f, 0.5f, 0.2f, lightAt(drill, block));
            poseStack.popPose();
        }
    }

    private static int lightAt(OreDrillBlockEntity drill, int distance) {
        BlockPos pos = drill.getBlockPos().relative(drill.getDrillDirection(), distance);
        return LevelRenderer.getLightColor(drill.getLevel(), pos);
    }

    /** A box of half extents {@code x, y, z} around the origin, the whole texture on every face. */
    private static void cube(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z, int light) {
        quad(c, pose, light, 0, 1, 0, -x, y, -z, -x, y, z, x, y, z, x, y, -z);
        quad(c, pose, light, 0, -1, 0, x, -y, -z, x, -y, z, -x, -y, z, -x, -y, -z);
        quad(c, pose, light, 0, 0, -1, x, y, -z, x, -y, -z, -x, -y, -z, -x, y, -z);
        quad(c, pose, light, 0, 0, 1, -x, y, z, -x, -y, z, x, -y, z, x, y, z);
        quad(c, pose, light, -1, 0, 0, -x, y, -z, -x, -y, -z, -x, -y, z, -x, y, z);
        quad(c, pose, light, 1, 0, 0, x, y, z, x, -y, z, x, -y, -z, x, y, -z);
    }

    private static void quad(VertexConsumer c, PoseStack.Pose pose, int light, float nx, float ny, float nz, float x0, float y0, float z0,
                             float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        vertex(c, pose, x0, y0, z0, 0, 0, light, nx, ny, nz);
        vertex(c, pose, x1, y1, z1, 0, 1, light, nx, ny, nz);
        vertex(c, pose, x2, y2, z2, 1, 1, light, nx, ny, nz);
        vertex(c, pose, x3, y3, z3, 1, 0, light, nx, ny, nz);
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light,
                               float nx, float ny, float nz) {
        c.addVertex(pose, x, y, z).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    /** From the controller to the cluster, as wide as the frame. */
    @Override
    public AABB getRenderBoundingBox(OreDrillBlockEntity drill) {
        BlockPos pos = drill.getBlockPos();
        BlockPos end = pos.relative(drill.getDrillDirection(), drill.getEngines() + drill.getRods() + 1);
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), end.getX() + 1, end.getY() + 1, end.getZ() + 1).inflate(drill.getRadius() + 1);
    }
}
