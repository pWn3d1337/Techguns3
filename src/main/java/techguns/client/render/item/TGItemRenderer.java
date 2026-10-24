package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Renders one Techguns item (gun, magazine, rocket, ...). Implementations draw with the pose stack as
 * Techguns 2 left it: no vanilla model display transforms applied, origin at the item's anchor.
 */
// MIGRATION(26.x): PoseStack + MultiBufferSource become a submit-node collector; only the adapter and this signature change.
public interface TGItemRenderer {
    void render(ItemRenderContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay);
}
