package techguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Machine block items drawn with the machine model (Techguns 2 used builtin/entity item models for them). */
// MIGRATION(26.x): BlockEntityWithoutLevelRenderer / IClientItemExtensions#getCustomRenderer are replaced by special item model renderers in 1.21.4.
public final class TGMachineItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final TGMachineRenderer<?> renderer;

    private TGMachineItemRenderer(TGMachineRenderer<?> renderer) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.renderer = renderer;
    }

    public static void register(RegisterClientExtensionsEvent event, ItemLike item, TGMachineRenderer<?> renderer) {
        TGMachineItemRenderer itemRenderer = new TGMachineItemRenderer(renderer);
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return itemRenderer;
            }
        }, item.asItem());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        renderer.renderStatic(poseStack, buffer, packedLight, packedOverlay);
    }
}
