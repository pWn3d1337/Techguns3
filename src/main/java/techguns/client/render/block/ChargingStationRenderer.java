package techguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import techguns.machine.ChargingStationBlockEntity;
import techguns.machine.TGMachineBlock;

/** Techguns 2 {@code RenderChargingStation}: the item being charged lies in the station. */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class ChargingStationRenderer implements BlockEntityRenderer<ChargingStationBlockEntity> {
    public ChargingStationRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ChargingStationBlockEntity station, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ItemStack item = station.getShown();
        if (item.isEmpty() || !(station.isWorking() || station.isCharging())) {
            return;
        }
        Direction facing = station.getBlockState().getValue(TGMachineBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.4, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f * facing.get2DDataValue()));
        Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.GROUND, light, overlay, poseStack, buffer,
                station.getLevel(), 0);
        poseStack.popPose();
    }
}
