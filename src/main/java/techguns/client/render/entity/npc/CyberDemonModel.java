package techguns.client.render.entity.npc;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Techguns 2 {@code ModelCyberDemon}: empty biped parts at the biped pivots carrying the demon's horned head, broad
 * body, armored arms and bent legs as children, so the humanoid animation moves them.
 */
// MIGRATION(26.x): ModelPart / LayerDefinition rendering changes with render states in 1.21.2+.
public final class CyberDemonModel {
    private static final float LEG_BEND = -0.5235988F;

    private CyberDemonModel() {
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -3F, 8F, 8F, 7F), PartPose.ZERO);
        head.addOrReplaceChild("h2", CubeListBuilder.create().texOffs(48, 0).addBox(-3F, -5F, -5F, 6F, 5F, 2F), PartPose.ZERO);
        head.addOrReplaceChild("h3", CubeListBuilder.create().texOffs(52, 7).addBox(4F, -7F, -2F, 2F, 4F, 4F), PartPose.ZERO);
        head.addOrReplaceChild("h4", CubeListBuilder.create().texOffs(30, 8).addBox(-1F, 0F, -5F, 2F, 2F, 5F),
                PartPose.offsetAndRotation(-7.5F, -5.5F, -1F, -0.4363323F, 0F, 0F));
        head.addOrReplaceChild("h5", CubeListBuilder.create().texOffs(52, 7).addBox(-6F, -7F, -2F, 2F, 4F, 4F), PartPose.ZERO);
        head.addOrReplaceChild("h6", CubeListBuilder.create().texOffs(24, 0).addBox(0F, -1.5F, -3F, 4F, 3F, 3F),
                PartPose.offsetAndRotation(6F, -5F, 1.5F, 0F, 0.6981317F, 0F));
        head.addOrReplaceChild("h7", CubeListBuilder.create().texOffs(24, 0).addBox(-4F, -1.5F, -3F, 4F, 3F, 3F),
                PartPose.offsetAndRotation(-6F, -5F, 1.5F, 0F, -0.6981317F, 0F));
        head.addOrReplaceChild("h8", CubeListBuilder.create().texOffs(30, 8).addBox(-1F, 0F, -5F, 2F, 2F, 5F),
                PartPose.offsetAndRotation(7.5F, -5.5F, -1F, -0.4363323F, 0F, 0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 15).addBox(-6F, 0F, -3F, 12F, 7F, 6F), PartPose.ZERO);
        body.addOrReplaceChild("b2", CubeListBuilder.create().texOffs(0, 28).addBox(-4.5F, 7F, -2.5F, 9F, 5F, 5F), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        rightArm.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(44, 16).addBox(-6F, -2F, -2.5F, 5F, 7F, 5F), PartPose.ZERO);
        rightArm.addOrReplaceChild("ra2", CubeListBuilder.create().texOffs(48, 28).addBox(-5.5F, 3F, 0.5F, 4F, 8F, 4F),
                PartPose.rotation(-0.5205006F, 0F, 0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        leftArm.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(44, 16).addBox(1F, -2F, -2.5F, 5F, 7F, 5F), PartPose.ZERO);
        leftArm.addOrReplaceChild("la2", CubeListBuilder.create().texOffs(48, 28).addBox(1.5F, 3F, 0.5F, 4F, 8F, 4F),
                PartPose.rotation(-0.5205006F, 0F, 0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        rightLeg.addOrReplaceChild("thigh", CubeListBuilder.create().texOffs(0, 38).addBox(-3.6F, -1F, -2.5F, 5F, 7F, 5F),
                PartPose.rotation(LEG_BEND, 0F, 0F));
        rightLeg.addOrReplaceChild("rl2", CubeListBuilder.create().texOffs(20, 39).addBox(-3.1F, 3F, 1.5F, 4F, 7F, 4F),
                PartPose.rotation(LEG_BEND, 0F, 0F));
        rightLeg.addOrReplaceChild("rl3", CubeListBuilder.create().texOffs(0, 50).addBox(-3.6F, 9F, -4F, 5F, 3F, 5F), PartPose.ZERO);
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        leftLeg.addOrReplaceChild("thigh", CubeListBuilder.create().texOffs(0, 38).addBox(-1.4F, -1F, -2.5F, 5F, 7F, 5F),
                PartPose.rotation(LEG_BEND, 0F, 0F));
        leftLeg.addOrReplaceChild("ll2", CubeListBuilder.create().texOffs(20, 39).addBox(-0.9F, 3F, 1.5F, 4F, 7F, 4F),
                PartPose.rotation(LEG_BEND, 0F, 0F));
        leftLeg.addOrReplaceChild("ll3", CubeListBuilder.create().texOffs(0, 50).addBox(-1.4F, 9F, -4F, 5F, 3F, 5F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
