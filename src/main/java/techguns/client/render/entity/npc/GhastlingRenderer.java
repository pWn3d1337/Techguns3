package techguns.client.render.entity.npc;

import java.util.Random;

import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import techguns.TGIds;
import techguns.entity.npc.Ghastling;
import techguns.entity.npc.TGNpc;

/**
 * Techguns 2 {@code RenderGhastling} / {@code ModelGhastling}: a ghast body with nine short tentacles at full size (a
 * ghast is drawn 4.5 times bigger), shooting face while attacking.
 */
// MIGRATION(26.x): entity renderers take render states in 1.21.2+.
public class GhastlingRenderer extends MobRenderer<TGNpc, GhastModel<TGNpc>> {
    private static final ResourceLocation TEXTURE = TGIds.of("minecraft", "textures/entity/ghast/ghast.png");
    private static final ResourceLocation SHOOTING = TGIds.of("minecraft", "textures/entity/ghast/ghast_shooting.png");

    public GhastlingRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel<>(createLayer().bakeRoot()), 0.25f);
    }

    /** Techguns 2 {@code ModelGhastling}: body and tentacles as the ghast's, raised by the model's 0.6 block shift. */
    // MIGRATION(26.x): ModelPart / LayerDefinition rendering changes with render states in 1.21.2+.
    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        float shift = -9.6f;
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8F, -8F, -8F, 16F, 16F, 16F), PartPose.offset(0F, 8F + shift, 0F));
        // the same tentacle lengths as Techguns 2 (and vanilla): seeded random
        Random random = new Random(1660L);
        for (int i = 0; i < 9; i++) {
            float x = (((i % 3) - (i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float z = ((i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
            int length = random.nextInt(7) + 8;
            root.addOrReplaceChild("tentacle" + i, CubeListBuilder.create().texOffs(0, 0).addBox(-1F, 0F, -1F, 2F, length, 2F),
                    PartPose.offset(x, 15F + shift, z));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ResourceLocation getTextureLocation(TGNpc npc) {
        return npc instanceof Ghastling ghastling && ghastling.isAttacking() ? SHOOTING : TEXTURE;
    }
}
