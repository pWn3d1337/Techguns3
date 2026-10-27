package techguns.client.render.entity.npc;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Techguns 2 {@code ModelSuperMutant} (Tabula): a broad biped with shoulder pads, chest plates, belt and boots as
 * children of the biped parts, so the humanoid animation moves them.
 */
// MIGRATION(26.x): ModelPart / LayerDefinition rendering changes with render states in 1.21.2+.
public final class SuperMutantModel {
    private SuperMutantModel() {
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4F, 0F, -2F, 8F, 12F, 4F), PartPose.ZERO);
        body.addOrReplaceChild("chest_armor_front", CubeListBuilder.create().texOffs(21, 34).addBox(-4F, 0F, -2F, 6F, 7F, 1F),
                PartPose.offset(1F, 1F, -1F));
        body.addOrReplaceChild("chest_armor_back", CubeListBuilder.create().texOffs(21, 44).addBox(-4F, 0F, -2F, 6F, 7F, 1F),
                PartPose.offset(1F, 1F, 4F));
        body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(0, 53).addBox(0F, 0F, 0F, 9F, 4F, 5F), PartPose.offset(-4.5F, 9F, -2.5F));
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3F, -2F, -2F, 4F, 12F, 4F),
                PartPose.offset(-5F, 2F, 0F));
        rightArm.addOrReplaceChild("right_arm_upper", CubeListBuilder.create().texOffs(40, 33).addBox(-3F, -2F, -2F, 5F, 7F, 5F),
                PartPose.offset(-0.9F, -0.2F, -0.5F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1F, -2F, -2F, 4F, 12F, 4F),
                PartPose.offset(5F, 2F, 0F));
        leftArm.addOrReplaceChild("left_arm_upper", CubeListBuilder.create().texOffs(40, 33).mirror().addBox(-1F, -2F, -2F, 5F, 7F, 5F),
                PartPose.offset(-0.1F, -0.2F, -0.5F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(-1.9F, 11F, 0F));
        rightLeg.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(0, 33).addBox(-2F, 0F, -2F, 5F, 6F, 5F), PartPose.offset(-0.6F, 6F, -0.7F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(1.9F, 11F, 0F));
        leftLeg.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(0, 33).mirror().addBox(-2F, 0F, -2F, 5F, 6F, 5F),
                PartPose.offset(-0.4F, 6F, -0.7F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
