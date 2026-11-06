package techguns.client.render.item;

import net.minecraft.resources.ResourceLocation;
import techguns.client.model.TGModel;
import techguns.gun.MeleeToolItem;

/**
 * Powered melee tools (chainsaw, mining drill, power hammer): render part 1 (blades, drill head, hammer head) is
 * textured by the installed mining head, like Techguns 2 {@code RenderMiningToolMultiTexture}. The texture is passed to
 * the render type of the part, so the Java model and the geo model (which draws into that buffer) both use it.
 */
public class RenderMiningTool extends RenderGunBase90 {
    private final ResourceLocation[] headTextures;

    /**
     * @param headTextures texture of render part 1 per head level (index = {@link MeleeToolItem#headLevel}); levels
     *                     beyond the array use the base texture
     */
    public RenderMiningTool(TGModel model, int parts, ResourceLocation texture, ResourceLocation... headTextures) {
        super(model, parts, texture);
        this.headTextures = headTextures;
    }

    @Override
    protected ResourceLocation partTexture(ItemRenderContext context, int part) {
        if (part == 1 && context.stack().getItem() instanceof MeleeToolItem tool) {
            int level = tool.headLevel(context.stack());
            if (level >= 0 && level < headTextures.length) {
                return headTextures[level];
            }
        }
        return super.partTexture(context, part);
    }
}
