package techguns.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Techguns 2 {@code ModelExoSuit}: keeps the biped boxes (super(scale, 0, 64, 64)) and adds the exo cubes. Generated from
 * the 1.12 values.
 */
final class ExoSuitModel {
    private ExoSuitModel() {
    }

    static LayerDefinition create(int type, float scale) {
        CubeDeformation inflate = new CubeDeformation(scale);
        MeshDefinition mesh = HumanoidModel.createMesh(inflate, 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild(PartNames.HEAD);
        PartDefinition body = root.getChild(PartNames.BODY);
        PartDefinition rightArm = root.getChild(PartNames.RIGHT_ARM);
        PartDefinition leftArm = root.getChild(PartNames.LEFT_ARM);
        PartDefinition rightLeg = root.getChild(PartNames.RIGHT_LEG);
        PartDefinition leftLeg = root.getChild(PartNames.LEFT_LEG);
        if (type == 0 || type == 2) {
            ArmorMeshUtil.box(rightArm, "ra4", 24, 32, false, -4.0f, 4.5f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra6", 48, 32, false, -4.5f, 1.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 8, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra7", 27, 41, false, -5.0f, 4.0f, -1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 3, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra8", 0, 32, false, -4.5f, -3.5f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6, 5, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la2", 27, 41, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3.0f, 4.0f, -1.5f, 2, 3, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la5", 0, 32, false, 0.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.5f, -1.5f, -3.0f, 6, 5, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la6", 24, 32, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -2.0f, 4.5f, -3.0f, 6, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la7", 48, 32, false, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.5f, 0.5f, -1.0f, 2, 8, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b2", 0, 60, false, -3.0f, 1.0f, 3.533333f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 10, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b3", 38, 40, false, 0.5f, 3.0f, 3.533333f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 3, 9, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rb1", 56, 51, false, 0.4f, 9.5f, -1.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 3, 3, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rb2", 40, 56, false, -3.1f, 10.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "lb1", 56, 51, false, 0.6f, 9.5f, -1.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 3, 3, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "lb2", 40, 56, false, -2.9f, 10.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6, 2, 6, CubeDeformation.NONE);
        } else {
            ArmorMeshUtil.box(body, "p1", 0, 43, false, -5.0f, 11.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 10, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "p3", 0, 52, false, -1.0f, 13.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl2", 27, 41, false, -4.1f, 4.5f, -1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 3, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl3", 17, 52, false, -3.1f, 5.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl4", 56, 32, false, -3.6f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 10, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll1", 56, 32, false, 1.6f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 10, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll3", 27, 41, false, 2.1f, 4.5f, -1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 3, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll4", 17, 52, false, -1.9f, 5.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 2, 6, CubeDeformation.NONE);
        }
        return LayerDefinition.create(mesh, 64, 64);
    }
}
