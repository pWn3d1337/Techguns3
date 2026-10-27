package techguns.client.render.block;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import techguns.TGIds;
import techguns.client.model.turret.TurretBaseModel;
import techguns.machine.TurretBlockEntity;

/** Techguns 2 {@code RenderTurret}: the turret base, its texture by armor tier, upside down when hanging. */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class TurretBaseRenderer implements BlockEntityRenderer<TurretBlockEntity> {
    /** Techguns 2 turret textures, tier 0 (no plate) to 4 (carbon); shared with the turret. */
    private static final ResourceLocation[] TEXTURES = {TGIds.id("textures/block/turret_base.png"), TGIds.id("textures/block/turret_base2.png"),
            TGIds.id("textures/block/turret_base3.png"), TGIds.id("textures/block/turret_base4.png"), TGIds.id("textures/block/turret_base5.png")};
    private static final Supplier<TurretBaseModel> MODEL = Suppliers.memoize(TurretBaseModel::bake);

    public TurretBaseRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static ResourceLocation texture(int tier) {
        return TEXTURES[Math.clamp(tier, 0, TEXTURES.length - 1)];
    }

    @Override
    public void render(TurretBlockEntity base, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderBase(poseStack, buffer, light, overlay, base.isHanging(), base.getTier());
    }

    /** Also used for the item. */
    public static void renderBase(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, boolean hanging, int tier) {
        poseStack.pushPose();
        // Techguns 2: models are upside down with the origin a block above the block centre
        if (hanging) {
            poseStack.translate(0.5, -0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        } else {
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        }
        MODEL.get().render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(texture(tier))), light, overlay);
        poseStack.popPose();
    }
}
