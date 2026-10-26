package techguns.client.render.block;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import techguns.client.model.machine.MachineModel;
import techguns.machine.TGMachineBlock;
import techguns.machine.TGMachineBlockEntity;

/**
 * Techguns 2 {@code RenderMachine}: draws a machine model, turned to the block's facing and animated by the job
 * progress. Also draws the item ({@link #renderStatic}).
 */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class TGMachineRenderer<T extends TGMachineBlockEntity> implements BlockEntityRenderer<T> {
    private final Supplier<? extends MachineModel> model;
    private final ResourceLocation texture;

    public TGMachineRenderer(Supplier<? extends MachineModel> model, ResourceLocation texture) {
        this.model = model;
        this.texture = texture;
    }

    public static <T extends TGMachineBlockEntity> BlockEntityRendererProvider<T> provider(Supplier<? extends MachineModel> model,
                                                                                          ResourceLocation texture) {
        return context -> new TGMachineRenderer<>(model, texture);
    }

    @Override
    public void render(T machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = machine.getBlockState();
        Direction facing = state.hasProperty(TGMachineBlock.FACING) ? state.getValue(TGMachineBlock.FACING) : Direction.SOUTH;
        renderModel(poseStack, buffer, light, overlay, facing, machine.isWorking() ? machine.getProgress() : 0.0f);
    }

    /** Item / static version: facing south, no job. */
    public void renderStatic(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderModel(poseStack, buffer, light, overlay, Direction.SOUTH, 0.0f);
    }

    private void renderModel(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, Direction facing, float progress) {
        poseStack.pushPose();
        // Techguns 2: models are upside down, origin one block above the block centre; rotation in yaw quadrants
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f * facing.get2DDataValue()));
        model.get().render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay, progress);
        poseStack.popPose();
    }
}
