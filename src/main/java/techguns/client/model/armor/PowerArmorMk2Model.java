package techguns.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Techguns 2 {@code ModelT4PowerArmorMk2}: biped parts recreated without boxes, all cubes explicitly un-inflated.
 * Generated from the 1.12 values.
 */
final class PowerArmorMk2Model {
    private PowerArmorMk2Model() {
    }

    static LayerDefinition create(int type, float scale) {
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
            ArmorMeshUtil.box(leftArm, "la01", 69, 38, true, -1.5f, -2.5f, -2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 13, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la03", 68, 27, false, 0.0f, -3.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4, 4, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la04", 75, 12, false, 3.2f, -5.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 7, 4, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la05", 56, 38, true, 4.7f, 7.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.5f, -2.5f, -2.0f, 1, 5, 4, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftArm, "la06", 46, 38, false, 1.0f, 3.0f, 2.0f, 0.0f, 0.0f, 0.0f, -1.0f, -1.0f, 0.0f, 3, 4, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra01", 69, 38, false, -3.5f, -2.5f, -2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 13, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra03", 68, 27, true, -4.0f, -3.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4, 4, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra04", 75, 12, false, -4.2f, -5.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 7, 4, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra05", 56, 38, false, -4.2f, 4.5f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 5, 4, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightArm, "ra06", 46, 38, false, -3.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 4, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h01", 0, 0, false, -4.5f, -8.5f, -4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 9, 9, 9, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h02", 0, 19, false, -2.0f, -2.2f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4, 3, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h03", 0, 28, false, 2.0f, -2.0f, -5.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h04", 0, 28, false, -2.0f, -2.0f, -5.5f, 0.0f, 0.0f, 0.0f, -3.0f, 0.0f, 0.0f, 3, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h05", 13, 27, false, 4.0f, -2.0f, -3.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 2, 8, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h06", 13, 27, false, -5.0f, -2.0f, -3.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 2, 8, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h07", 0, 33, false, -1.5f, -4.2f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h08", 19, 19, false, -4.5f, -2.0f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 9, 2, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h09", 24, 23, false, -2.0f, -8.5f, 4.2f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4, 9, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h10", 38, 0, false, 4.5f, -5.6f, -1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 6, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h11", 38, 0, true, -5.5f, -5.6f, -1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1, 6, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(head, "h12", 97, 15, false, -2.0f, -9.5f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4, 1, 7, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b01", 41, 19, false, -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, -4.0f, 0.0f, -2.0f, 9, 6, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b02", 48, 0, false, -4.5f, 1.5f, -4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 9, 4, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b03", 71, 0, false, -3.0f, 5.2f, -4.5f, 0.2792527f, -0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6, 7, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b04", 48, 10, false, 4.5f, 1.5f, 4.0f, 0.0f, 0.0f, 0.0f, -4.0f, 0.0f, -2.0f, 3, 5, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b05", 48, 10, false, 0.5f, 1.5f, 4.0f, 0.0f, 0.0f, 0.0f, -4.0f, 0.0f, -2.0f, 3, 5, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b06", 60, 10, false, -2.5f, 5.0f, 2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5, 5, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b07", 50, 32, false, -3.0f, 11.0f, 3.0f, 0.7853982f, 0.0f, 0.0f, 0.0f, -1.0f, -1.0f, 6, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b10", 89, 27, false, -1.0f, 0.8f, -4.8f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 5, 3, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b12", 90, 36, false, 0.0f, 5.5f, -0.6f, 0.0f, 0.0f, 0.0f, -4.0f, 0.0f, -2.0f, 8, 6, 5, new CubeDeformation(0.1f));
            ArmorMeshUtil.box(body, "b13", 100, 24, false, 2.2f, 5.9f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 3, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "b14", 100, 24, true, -4.2f, 5.9f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2, 3, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rb01", 103, 0, false, -0.6f, 8.5f, -0.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 5, 4, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rb02", 103, 10, false, -0.1f, 10.0f, -2.0f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 4, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "lb01", 103, 0, false, -4.4f, 8.5f, -0.5f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, -2.0f, 5, 4, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "lb02", 103, 10, true, 0.1f, 10.0f, -2.0f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 4, 2, 2, CubeDeformation.NONE);
        } else {
            ArmorMeshUtil.box(body, "p02", 0, 38, false, -5.0f, 9.9f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 10, 3, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(body, "p03", 21, 47, false, -1.5f, 12.9f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3, 2, 6, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl01", 0, 47, false, -0.6f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 5, 9, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl02", 21, 56, false, -0.4f, 3.5f, -3.0f, 0.0f, -0.0f, 0.7853982f, 0.0f, 0.0f, 0.0f, 3, 3, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(rightLeg, "rl03", 30, 56, false, -2.9f, 5.5f, 0.0f, 0.7853982f, -0.0f, 0.0f, 0.0f, -1.0f, -1.0f, 1, 2, 2, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll01", 0, 47, true, -0.4f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, -2.0f, 5, 9, 5, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll02", 21, 56, false, 0.4f, 3.5f, -3.0f, 0.0f, -0.0f, 0.7853982f, 0.0f, 0.0f, 0.0f, 3, 3, 1, CubeDeformation.NONE);
            ArmorMeshUtil.box(leftLeg, "ll03", 30, 56, true, 1.9f, 5.5f, 0.0f, 0.7853982f, -0.0f, 0.0f, 0.0f, -1.0f, -1.0f, 1, 2, 2, CubeDeformation.NONE);
        }
        return LayerDefinition.create(mesh, 128, 64);
    }
}
