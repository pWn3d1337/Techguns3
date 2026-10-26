package techguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import techguns.TGIds;
import techguns.machine.BlastFurnaceBlockEntity;
import techguns.machine.TGMachineBlock;

/**
 * Techguns 2 {@code RenderBlastfurnace}: while working, glowing flowing lava fills the front opening of the block
 * model.
 */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class BlastFurnaceRenderer implements BlockEntityRenderer<BlastFurnaceBlockEntity> {
    private static final float UNIT = 1.0f / 16.0f;

    public BlastFurnaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlastFurnaceBlockEntity furnace, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (!furnace.isWorking()) {
            return;
        }
        Direction facing = furnace.getBlockState().getValue(TGMachineBlock.FACING);
        TextureAtlasSprite lava = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TGIds.vanilla("block/lava_flow"));
        poseStack.pushPose();
        // the model's front is north; turn like the blockstate (clockwise from above)
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(facing.toYRot() - 180.0f)));
        poseStack.translate(-0.5, 0.0, -0.5);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        float x0 = 3 * UNIT;
        float x1 = 13 * UNIT;
        float y0 = 3 * UNIT;
        float y1 = 9 * UNIT;
        float z = 0.06f;
        int bright = LightTexture.FULL_BRIGHT;
        vertex(pose, consumer, overlay, bright, x0, y1, z, lava.getU0(), lava.getV1());
        vertex(pose, consumer, overlay, bright, x1, y1, z, lava.getU1(), lava.getV1());
        vertex(pose, consumer, overlay, bright, x1, y0, z, lava.getU1(), lava.getV0());
        vertex(pose, consumer, overlay, bright, x0, y0, z, lava.getU0(), lava.getV0());
        poseStack.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, int overlay, int light, float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
    }
}
