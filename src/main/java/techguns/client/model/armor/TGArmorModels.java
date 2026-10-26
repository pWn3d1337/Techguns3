package techguns.client.model.armor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import techguns.registry.TGArmors;

/**
 * Custom armor models (Techguns 2 {@code client/models/armor}, registered in its {@code ClientProxy}), baked on first
 * use. Values follow the 1.12 models; Fabric's ports of them were only used as an API reference.
 */
// MIGRATION(26.x): armor rendering moves to equipment assets / render states in 1.21.2+.
public final class TGArmorModels {
    private static final Map<ResourceLocation, Supplier<LayerDefinition>> DEFINITIONS = new HashMap<>();
    private static final Map<ResourceLocation, HumanoidModel<LivingEntity>> BAKED = new HashMap<>();

    static {
        DEFINITIONS.put(TGArmors.MODEL_COAT_0, () -> plain(1.0f));
        DEFINITIONS.put(TGArmors.MODEL_COAT_1, () -> coat(1, 0.51f));
        DEFINITIONS.put(TGArmors.MODEL_COAT_2, () -> coat(2, 0.49f));
        DEFINITIONS.put(TGArmors.MODEL_COAT_3, () -> plain(0.75f));
        DEFINITIONS.put(TGArmors.MODEL_BERET, TGArmorModels::beret);
    }

    private TGArmorModels() {
    }

    @Nullable
    public static HumanoidModel<LivingEntity> get(ResourceLocation id) {
        Supplier<LayerDefinition> definition = DEFINITIONS.get(id);
        if (definition == null) {
            return null;
        }
        return BAKED.computeIfAbsent(id, key -> new HumanoidModel<>(definition.get().bakeRoot()));
    }

    /** Plain biped armor on a 64x64 texture (Techguns 2 {@code ModelArmorCoat} types 0 and 3). */
    private static LayerDefinition plain(float scale) {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(scale), 0.0f), 64, 64);
    }

    /** Techguns 2 {@code ModelArmorCoat}: type 1 adds coat tails and shoulder pads, type 2 coat legs. */
    private static LayerDefinition coat(int type, float scale) {
        CubeDeformation inflate = new CubeDeformation(scale);
        MeshDefinition mesh = HumanoidModel.createMesh(inflate, 0.0f);
        PartDefinition root = mesh.getRoot();
        if (type == 1) {
            PartDefinition body = root.getChild(PartNames.BODY);
            body.addOrReplaceChild("coat_left", CubeListBuilder.create().texOffs(50, 47).mirror()
                    .addBox(-4.0f, 0.0f, -2.0f, 2, 12, 5, inflate), PartPose.offset(7.0f, -0.1f, -0.5f));
            body.addOrReplaceChild("coat_back", CubeListBuilder.create().texOffs(44, 35)
                    .addBox(-4.0f, 0.0f, -2.0f, 10, 12, 0, inflate), PartPose.offset(-1.0f, -0.1f, 4.5f));
            body.addOrReplaceChild("coat_right", CubeListBuilder.create().texOffs(50, 47)
                    .addBox(-4.0f, 0.0f, -2.0f, 2, 12, 5, inflate), PartPose.offset(-1.0f, -0.1f, -0.5f));
            root.getChild(PartNames.LEFT_ARM).addOrReplaceChild("left_shoulder", CubeListBuilder.create().texOffs(0, 34).mirror()
                    .addBox(-2.0f, -2.5f, -2.0f, 5, 3, 6, inflate),
                    PartPose.offsetAndRotation(1.0f, -0.5f, -1.0f, 0.0f, 0.0f, 0.22689280275926282f));
            root.getChild(PartNames.RIGHT_ARM).addOrReplaceChild("right_shoulder", CubeListBuilder.create().texOffs(0, 34)
                    .addBox(-3.0f, -2.5f, -2.0f, 5, 3, 6, inflate),
                    PartPose.offsetAndRotation(-1.0f, -0.5f, -1.0f, 0.0f, 0.0f, -0.22689280275926282f));
        } else if (type == 2) {
            root.getChild(PartNames.LEFT_LEG).addOrReplaceChild("coat_left_leg", CubeListBuilder.create().texOffs(0, 48).mirror()
                    .addBox(-2.0f, 0.0f, -2.0f, 5, 10, 5, inflate), PartPose.offset(0.1f, -0.1f, -0.5f));
            root.getChild(PartNames.RIGHT_LEG).addOrReplaceChild("coat_right_leg", CubeListBuilder.create().texOffs(0, 48)
                    .addBox(-2.0f, 0.0f, -2.0f, 5, 10, 5, inflate), PartPose.offset(-1.1f, -0.1f, -0.5f));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Techguns 2 {@code ModelBeret}: no head cube, a top and a tilted side on a 32x32 texture. */
    private static LayerDefinition beret() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild(PartNames.HEAD, CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild(PartNames.HAT, CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("beret_top", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.0f, -8.7f, -4.0f, 6, 2, 8, new CubeDeformation(0.7f)), PartPose.ZERO);
        head.addOrReplaceChild("beret_side", CubeListBuilder.create().texOffs(0, 11)
                .addBox(-3.0f, 0.0f, 0.0f, 3, 2, 8, new CubeDeformation(0.65f)),
                PartPose.offsetAndRotation(-2.7f, -8.5f, -4.0f, 0.0f, 0.0f, -0.6108652381980153f));
        return LayerDefinition.create(mesh, 32, 32);
    }
}
