package techguns.client.render.entity.npc;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import techguns.entity.npc.TGNpc;

/** An NPC on its own biped-shaped Techguns 2 model (no armor layer), e.g. the cyber demon. */
// MIGRATION(26.x): entity renderers take render states in 1.21.2+.
public class CustomModelNpcRenderer extends HumanoidMobRenderer<TGNpc, TGNpcModel> {
    private final ResourceLocation texture;

    public CustomModelNpcRenderer(EntityRendererProvider.Context context, LayerDefinition model, boolean aimsGuns, ResourceLocation texture,
                                  float shadow) {
        super(context, new TGNpcModel(model.bakeRoot(), aimsGuns), shadow);
        this.texture = texture;
    }

    /** Moves and scales the held gun for the model's arms (Techguns 2 weapon offset and gun scale). */
    public CustomModelNpcRenderer weaponOffset(EntityRendererProvider.Context context, NpcHeldItemLayer.Offset offset) {
        layers.removeIf(layer -> layer instanceof net.minecraft.client.renderer.entity.layers.ItemInHandLayer);
        addLayer(new NpcHeldItemLayer<>(this, context.getItemInHandRenderer(), npc -> offset));
        return this;
    }

    @Override
    public ResourceLocation getTextureLocation(TGNpc npc) {
        return texture;
    }
}
