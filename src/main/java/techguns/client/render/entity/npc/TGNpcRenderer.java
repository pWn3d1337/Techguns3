package techguns.client.render.entity.npc;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import techguns.entity.npc.TGNpc;

/**
 * Techguns 2 {@code RenderGenericNPC}: a biped NPC with the Techguns (or vanilla) armor layer and the held item, on
 * the vanilla model layers of the mob it looks like.
 */
// MIGRATION(26.x): entity renderers take render states in 1.21.2+.
public class TGNpcRenderer extends HumanoidMobRenderer<TGNpc, TGNpcModel> {
    private final ResourceLocation texture;

    public TGNpcRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ModelLayerLocation body, ModelLayerLocation innerArmor,
                         ModelLayerLocation outerArmor) {
        super(context, new TGNpcModel(context.bakeLayer(body)), 0.5f);
        this.texture = texture;
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(innerArmor)), new HumanoidModel<>(context.bakeLayer(outerArmor)),
                context.getModelManager()));
    }

    /** Moves the held gun for the model's arms (Techguns 2 weapon offset, skeleton soldiers). */
    public TGNpcRenderer weaponOffset(EntityRendererProvider.Context context, NpcHeldItemLayer.Offset offset) {
        layers.removeIf(layer -> layer instanceof net.minecraft.client.renderer.entity.layers.ItemInHandLayer);
        addLayer(new NpcHeldItemLayer<>(this, context.getItemInHandRenderer(), npc -> offset));
        return this;
    }

    @Override
    public ResourceLocation getTextureLocation(TGNpc npc) {
        return texture;
    }
}
