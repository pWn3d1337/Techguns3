package techguns.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Techguns 2 {@code ModelSteamArmor}: biped parts recreated without boxes; the helmet cubes use the model scale as
 * inflate, everything else is un-inflated. Generated from the 1.12 values (mirror flags as in effect at addBox time).
 */
final class SteamArmorModel {
    private SteamArmorModel() {
    }

    static LayerDefinition create(int type, float scale) {
        CubeDeformation inflate = new CubeDeformation(scale);
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(PartNames.HAT, ArmorMeshUtil.empty(), PartPose.ZERO);
        PartDefinition head = root.addOrReplaceChild(PartNames.HEAD, ArmorMeshUtil.empty(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild(PartNames.BODY, ArmorMeshUtil.empty(), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild(PartNames.RIGHT_ARM, ArmorMeshUtil.empty(), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition leftArm = root.addOrReplaceChild(PartNames.LEFT_ARM, ArmorMeshUtil.empty(), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition rightLeg = root.addOrReplaceChild(PartNames.RIGHT_LEG, ArmorMeshUtil.empty(), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition leftLeg = root.addOrReplaceChild(PartNames.LEFT_LEG, ArmorMeshUtil.empty(), PartPose.offset(1.9f, 12.0f, 0.0f));
        if (type == 0) {
            ArmorMeshUtil.box(rightArm, "ra1", 108, 0, false, -2.0f, -1.5f, 0.0f, 0.0f, 0.0f, -0.5235988f, -1.0f, -4.0f, -1.0f, 2, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra2", 79, 48, false, -1.5f, -3.5f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra3", 96, 0, false, -2.0f, -1.5f, 0.0f, 0.0f, 0.0f, -0.5235988f, -1.5f, -2.0f, -1.5f, 3, 2, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra4", 71, 40, false, -4.0f, 4.5f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra5", 48, 18, false, -3.5f, -2.5f, -2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 13, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra6", 71, 48, false, -4.5f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 9, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra7", 96, 5, false, -5.0f, 4.0f, -1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 3, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra8", 71, 32, false, -4.5f, -1.5f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la1", 108, 0, false, 2.0f, -1.5f, 0.0f, 0.0f, 0.0f, 0.5235988f, -1.0f, -4.0f, -1.0f, 2, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la2", 96, 5, false, -15.0f, 4.0f, -1.5f, 0.0f, 0.0f, 0.0f, 18.0f, 0.0f, 0.0f, 2, 3, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la3", 79, 48, false, -0.5f, -3.5f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la4", 96, 0, false, 2.0f, -1.5f, 0.0f, 0.0f, 0.0f, 0.5235988f, -1.5f, -2.0f, -1.5f, 3, 2, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la5", 71, 32, false, -1.5f, -1.5f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la6", 71, 40, false, -14.0f, 4.5f, -3.0f, 0.0f, 0.0f, 0.0f, 12.0f, 0.0f, 0.0f, 6, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la7", 71, 48, false, 2.5f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 9, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la8", 48, 18, false, -1.5f, -2.5f, -2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 13, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h1", 95, 32, false, -2.0f, -3.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4, 4, 2, inflate);
            ArmorMeshUtil.box(head, "h2", 36, 0, false, -5.5f, -7.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 6, 6, inflate);
            ArmorMeshUtil.box(head, "h3", 0, 0, false, -4.5f, -8.5f, -4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 9, 9, 9, inflate);
            ArmorMeshUtil.box(head, "h4", 107, 32, false, -3.5f, -1.5f, -5.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 7, 2, 1, inflate);
            ArmorMeshUtil.box(head, "h5", 36, 0, false, 4.5f, -7.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 6, 6, inflate);
            ArmorMeshUtil.box(head, "h6", 124, 0, false, -3.0f, -6.5f, 5.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 6, 1, inflate);
            ArmorMeshUtil.box(head, "h7", 107, 35, false, -3.5f, -2.0f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 2, 1, inflate);
            ArmorMeshUtil.box(head, "h8", 107, 35, false, -3.5f, -7.0f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 2, 1, inflate);
            ArmorMeshUtil.box(head, "h9", 107, 35, false, 1.5f, -7.0f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 2, 1, inflate);
            ArmorMeshUtil.box(head, "h10", 124, 0, false, 2.0f, -6.5f, 5.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 6, 1, inflate);
            ArmorMeshUtil.box(head, "h11", 107, 35, false, 1.5f, -2.0f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 2, 1, inflate);
            ArmorMeshUtil.box(body, "b1", 20, 18, false, -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, -4.0f, 0.0f, -2.0f, 9, 12, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b2", 50, 36, false, 0.0f, 0.5f, 4.5f, 0.0f, 0.0f, 0.0f, -4.0f, 0.0f, -2.0f, 8, 10, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b3", 94, 38, false, -3.5f, 7.5f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 1, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b4", 96, 18, false, 1.0f, 8.5f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 1, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b5", 96, 11, false, 0.5f, 3.5f, 4.5f, 0.0f, 0.0f, 0.0f, -4.0f, 0.0f, 0.0f, 3, 4, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b6", 96, 18, false, -3.0f, 8.5f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 1, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b7", 96, 11, false, 0.5f, 3.5f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 4, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b8", 94, 38, false, 0.5f, 7.5f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 1, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b9", 94, 38, false, -3.5f, 2.5f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 1, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b10", 94, 38, false, 0.5f, 2.5f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 1, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b11", 113, 18, false, -3.0f, 4.5f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 3, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b12", 96, 18, false, -3.0f, 1.5f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 1, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b13", 50, 5, false, -2.5f, 6.5f, -3.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 4, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b14", 50, 0, false, -3.5f, 1.5f, -3.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 7, 4, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b15", 96, 18, false, 1.0f, 1.5f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 1, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b16", 120, 18, false, 1.0f, 4.5f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 3, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rb1", 95, 49, false, 0.4f, 9.5f, -1.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 3, 3, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rb2", 100, 56, false, -3.1f, 10.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rb3", 0, 18, false, -0.6f, 8.5f, -0.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 5, 4, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "lb1", 95, 49, false, 0.6f, 9.5f, -1.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 3, 3, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "lb2", 100, 56, false, -2.9f, 10.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "lb3", 0, 18, false, -4.4f, 8.5f, -0.5f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, -2.0f, 5, 4, 5, CubeDeformation.NONE);
        } else {
            ArmorMeshUtil.box(body, "p1", 95, 41, false, -5.0f, 11.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 10, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "p2", 20, 47, false, -4.5f, 11.5f, -2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 9, 3, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "p3", 84, 56, false, -1.0f, 13.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl1", 0, 47, false, -0.6f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 5, 9, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl2", 96, 5, false, -4.1f, 4.5f, -1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 3, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl3", 71, 32, false, -3.1f, 5.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl4", 71, 48, false, -3.6f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 10, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll1", 71, 48, false, 1.6f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 10, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll2", 0, 47, false, -0.4f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 5, 9, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll3", 96, 5, false, 2.1f, 4.5f, -1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 3, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll4", 71, 32, false, -1.9f, 5.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 2, 6, CubeDeformation.NONE);
        }
        return LayerDefinition.create(mesh, 128, 64);
    }
}
