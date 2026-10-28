package techguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import techguns.TGIds;
import techguns.client.model.machine.FabricatorModel;
import techguns.machine.FabricatorBlockEntity;

/**
 * Techguns 2 {@code RenderFabricator}: draws the whole formed machine from the controller, turned to the side it was
 * formed from, with the plate being worked on (later the result) on the work table.
 */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class FabricatorRenderer implements BlockEntityRenderer<FabricatorBlockEntity> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/block/fabricator.png");
    private FabricatorModel model;

    public FabricatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FabricatorBlockEntity fabricator, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (!fabricator.isFormed()) {
            return;
        }
        if (model == null) {
            model = FabricatorModel.bake();
        }
        float progress = fabricator.isWorking() ? fabricator.getProgress(partialTick) : 0.0f;
        poseStack.pushPose();
        // Techguns 2: models upside down, origin one block above the controller; turned by the form direction
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f + 90.0f * fabricator.getDirection().get2DDataValue()));
        model.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, overlay, progress);
        ItemStack shown = fabricator.isWorking() ? fabricator.getShownItem() : ItemStack.EMPTY;
        if (!shown.isEmpty()) {
            poseStack.pushPose();
            // lying on the middle plate under the head (model y points down; plate top at 10 px)
            poseStack.translate(-0.5, 0.625, 0.5);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            poseStack.translate(0.0, 0.02, 0.0);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
            poseStack.scale(0.4f, 0.4f, 0.4f);
            Minecraft.getInstance().getItemRenderer().renderStatic(shown, ItemDisplayContext.FIXED, light, overlay, poseStack, buffer,
                    fabricator.getLevel(), 0);
            poseStack.popPose();
        }
        model.renderTranslucent(poseStack, buffer.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, overlay, progress);
        poseStack.popPose();
    }

    /** The formed machine reaches two blocks around the controller. */
    @Override
    public AABB getRenderBoundingBox(FabricatorBlockEntity fabricator) {
        return new AABB(fabricator.getBlockPos()).inflate(2.0);
    }
}
