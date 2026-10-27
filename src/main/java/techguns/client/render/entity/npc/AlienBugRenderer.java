package techguns.client.render.entity.npc;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import techguns.TGIds;
import techguns.entity.npc.TGNpc;

/** Techguns 2 {@code RenderAlienBug}. */
// MIGRATION(26.x): entity renderers take render states in 1.21.2+.
public class AlienBugRenderer extends MobRenderer<TGNpc, AlienBugModel> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/entity/alien_bug.png");

    public AlienBugRenderer(EntityRendererProvider.Context context) {
        super(context, new AlienBugModel(AlienBugModel.createLayer().bakeRoot()), 0.8f);
    }

    @Override
    public ResourceLocation getTextureLocation(TGNpc entity) {
        return TEXTURE;
    }
}
