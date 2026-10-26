package techguns.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Techguns 2 {@code ModelT3PowerArmor}: biped parts recreated without boxes, helmet cubes inflated by the model scale, the
 * rest un-inflated. Generated from the 1.12 values.
 */
final class PowerArmorModel {
    private PowerArmorModel() {
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
        if (type==0) {
            ArmorMeshUtil.box(leftArm, "la01", 90, 38, false, -1.5f, -2.5f, -2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 13, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la02", 72, 8, false, 0.0f, -4.0f, -4.0f, 0.0f, 0.0f, 0.5235988f, 1.0f, 0.0f, 0.0f, 7, 2, 8, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la03", 70, 27, false, 0.0f, -2.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 3, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la04", 100, 19, false, 0.0f, -5.0f, -3.0f, 0.0f, 0.0f, 0.5235988f, 1.5f, 0.0f, 0.0f, 7, 1, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la05", 56, 38, false, 5.0f, 7.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.5f, -2.5f, -2.0f, 1, 5, 4, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la06", 46, 38, false, 1.0f, 3.0f, 2.0f, 0.0f, 0.0f, 0.0f, -1.0f, -1.0f, 0.0f, 3, 4, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra01", 69, 38, false, -3.5f, -2.5f, -2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 13, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra02", 72, 8, false, 0.0f, -4.0f, -4.0f, 0.0f, 0.0f, -0.5235988f, -8.0f, 0.0f, 0.0f, 7, 2, 8, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra03", 70, 27, false, -3.0f, -2.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 3, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra04", 73, 19, false, 0.0f, -5.0f, -3.0f, 0.0f, 0.0f, -0.5235988f, -8.5f, 0.0f, 0.0f, 7, 1, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra05", 56, 38, false, -4.5f, 4.5f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 5, 4, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra06", 46, 38, false, -3.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 4, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h01", 0, 0, false, -4.5f, -8.5f, -4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 9, 9, 9, inflate);
            ArmorMeshUtil.box(head, "h02", 0, 19, false, -2.0f, -3.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4, 4, 3, inflate);
            ArmorMeshUtil.box(head, "h03", 0, 33, false, 2.0f, -2.0f, -5.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 2, 2, inflate);
            ArmorMeshUtil.box(head, "h04", 0, 28, false, -2.0f, -2.0f, -5.5f, 0.0f, 0.0f, 0.0f, -3.0f, 0.0f, 0.0f, 3, 2, 2, inflate);
            ArmorMeshUtil.box(head, "h05", 13, 27, false, 4.0f, -2.0f, -3.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 2, 8, inflate);
            ArmorMeshUtil.box(head, "h06", 89, 27, false, -5.0f, -2.0f, -3.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 2, 8, inflate);
            ArmorMeshUtil.box(head, "h07", 0, 5, false, 1.0f, -8.0f, -5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 2, 1, inflate);
            ArmorMeshUtil.box(head, "h08", 19, 19, false, -4.5f, -2.0f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 9, 2, 1, inflate);
            ArmorMeshUtil.box(head, "h09", 24, 23, false, -2.0f, -2.5f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4, 3, 1, inflate);
            ArmorMeshUtil.box(head, "h10", 28, 0, false, -5.5f, -6.0f, -3.0f, 0.0f, 0.0f, 0.7853982f, -1.0f, -1.0f, 0.0f, 2, 2, 4, inflate);
            ArmorMeshUtil.box(body, "b01", 41, 19, false, -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, -4.0f, 0.0f, -2.0f, 9, 12, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b02", 48, 0, false, -4.0f, 2.0f, -5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 8, 5, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b03", 71, 0, false, -3.5f, 7.0f, -4.5f, 0.3665191f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 7, 5, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b04", 48, 10, false, 4.5f, 5.0f, 4.0f, 0.0f, 0.0f, 0.0f, -4.0f, 0.0f, -2.0f, 3, 5, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b05", 48, 10, false, 0.5f, 5.0f, 4.0f, 0.0f, 0.0f, 0.0f, -4.0f, 0.0f, -2.0f, 3, 5, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b06", 59, 10, false, -2.5f, 3.0f, 2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 2, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b07", 59, 10, false, 1.5f, 3.0f, 2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 2, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b08", 59, 15, false, -2.5f, 2.0f, 2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 1, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b09", 64, 10, false, 0.0f, 2.5f, 3.0f, 0.0f, 0.0f, 0.7853982f, -1.0f, -1.0f, 0.0f, 2, 2, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rb01", 103, 0, false, -0.6f, 8.5f, -0.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 5, 4, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rb02", 103, 10, false, -0.1f, 10.0f, -2.0f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 4, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "lb01", 103, 0, false, -4.4f, 8.5f, -0.5f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, -2.0f, 5, 4, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "lb02", 103, 10, false, 0.1f, 10.0f, -2.0f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 4, 2, 2, CubeDeformation.NONE);
        } else {
            ArmorMeshUtil.box(body, "p01", 40, 48, false, -4.5f, 11.5f, -2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 9, 3, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "p02", 0, 38, false, -5.0f, 11.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 10, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "p03", 21, 47, false, -1.5f, 13.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl01", 0, 47, false, -0.6f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 5, 9, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl02", 21, 56, false, -0.4f, 2.5f, -3.0f, 0.0f, 0.0f, 0.7853982f, 0.0f, 0.0f, 0.0f, 3, 3, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl03", 30, 56, false, -3.1f, 4.5f, 0.0f, 0.7853982f, 0.0f, 0.0f, 0.0f, -1.0f, -1.0f, 1, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl04", 33, 38, false, -3.1f, 3.5f, 0.0f, 0.0f, 0.0f, 0.148353f, 0.0f, -3.0f, -1.0f, 1, 3, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl05", 33, 38, false, -3.1f, 5.5f, 0.0f, 0.0f, 0.0f, -0.148353f, 0.0f, 0.0f, -1.0f, 1, 3, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll01", 0, 47, false, -0.4f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 5, 9, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll02", 21, 56, false, 0.4f, 2.5f, -3.0f, 0.0f, 0.0f, 0.7853982f, 0.0f, 0.0f, 0.0f, 3, 3, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll03", 30, 56, false, 2.1f, 4.5f, 0.0f, 0.7853982f, 0.0f, 0.0f, 0.0f, -1.0f, -1.0f, 1, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll04", 33, 38, false, 2.1f, 3.5f, 0.0f, 0.0f, 0.0f, -0.148353f, 0.0f, -3.0f, -1.0f, 1, 3, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll05", 33, 38, false, 2.1f, 5.5f, 0.0f, 0.0f, 0.0f, 0.148353f, 0.0f, 0.0f, -1.0f, 1, 3, 2, CubeDeformation.NONE);
        }
        return LayerDefinition.create(mesh, 128, 64);
    }
}
