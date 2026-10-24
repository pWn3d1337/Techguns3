package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Bridges vanilla item rendering (builtin/entity item models) to {@link TGItemRenderer}s.
 */
// MIGRATION(26.x): replace with a special model renderer (item model definitions, 1.21.4+).
public final class TGItemRendererAdapter extends BlockEntityWithoutLevelRenderer {
    public static final TGItemRendererAdapter INSTANCE = new TGItemRendererAdapter();

    private TGItemRendererAdapter() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        TGItemRenderer renderer = TGItemRenderers.get(stack.getItem());
        if (renderer == null) {
            return;
        }
        LivingEntity holder = RenderingEntityTracker.holder(displayContext);
        ItemRenderContext context = new ItemRenderContext(stack, displayContext, holder, ItemRenderContext.handFor(holder, displayContext),
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));

        poseStack.pushPose();
        // vanilla moved the origin to the item corner for builtin models; Techguns renderers expect the centre
        poseStack.translate(0.5f, 0.5f, 0.5f);
        renderer.render(context, poseStack, buffers, light, overlay);
        poseStack.popPose();
    }
}
