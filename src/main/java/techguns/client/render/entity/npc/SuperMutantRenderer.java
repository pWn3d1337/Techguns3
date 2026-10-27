package techguns.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import techguns.TGIds;
import techguns.entity.npc.SuperMutant;
import techguns.entity.npc.TGNpc;

/** Techguns 2 {@code RenderSuperMutant}: the mutant model scaled by kind, one texture per kind, the held gun. */
// MIGRATION(26.x): entity renderers take render states in 1.21.2+.
public class SuperMutantRenderer extends HumanoidMobRenderer<TGNpc, TGNpcModel> {
    private static final ResourceLocation[] TEXTURES = {TGIds.id("textures/entity/supermutant_texture_1.png"),
            TGIds.id("textures/entity/supermutant_texture_2.png"), TGIds.id("textures/entity/supermutant_texture_3.png")};

    public SuperMutantRenderer(EntityRendererProvider.Context context) {
        super(context, new TGNpcModel(SuperMutantModel.createLayer().bakeRoot()), 0.7f);
    }

    /** Techguns 2 texture index: basic 1, heavy 2, elite 3. */
    @Override
    public ResourceLocation getTextureLocation(TGNpc npc) {
        return TEXTURES[npc instanceof SuperMutant mutant ? mutant.kind().ordinal() : 0];
    }

    @Override
    protected void scale(TGNpc npc, PoseStack poseStack, float partialTick) {
        float scale = npc instanceof SuperMutant mutant ? mutant.kind().scale : 1.0f;
        poseStack.scale(scale, scale, scale);
    }
}
