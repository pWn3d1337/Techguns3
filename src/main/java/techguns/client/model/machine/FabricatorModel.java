package techguns.client.model.machine;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

/**
 * Techguns 2 {@code ModelFabricator}: the 2x2x2 frame drawn from the controller, the moving frame and working head
 * (sliding with the progress), the drill that spins and lifts, the tube that pumps, the laser in the last part, and
 * the glass cover (translucent).
 */
// MIGRATION(26.x): ModelPart / LayerDefinition rendering changes with render states in 1.21.2+.
public final class FabricatorModel implements MachineModel {
    private final ModelPart base;
    private final ModelPart frame;
    private final ModelPart head;
    private final ModelPart drill;
    private final ModelPart tube;
    private final ModelPart laser;
    private final ModelPart glass;

    private FabricatorModel(ModelPart root) {
        this.base = root.getChild("base");
        this.frame = root.getChild("frame");
        this.head = root.getChild("head");
        this.drill = root.getChild("drill");
        this.tube = root.getChild("tube");
        this.laser = root.getChild("laser");
        this.glass = root.getChild("glass");
    }

    public static FabricatorModel bake() {
        return new FabricatorModel(createLayer().bakeRoot());
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition frame = root.addOrReplaceChild("frame", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition drill = root.addOrReplaceChild("drill", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition tube = root.addOrReplaceChild("tube", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition laser = root.addOrReplaceChild("laser", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition glass = root.addOrReplaceChild("glass", CubeListBuilder.create(), PartPose.ZERO);
        laser.addOrReplaceChild("workinglaser1", CubeListBuilder.create().texOffs(14, 102).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F), PartPose.offsetAndRotation(-6.7F, 2.5F, 6.5F, 0F, 0.785398F, 0F));
        base.addOrReplaceChild("corner3", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 3F, 14F, 3F), PartPose.offsetAndRotation(-24F, 8F, 21F, 0F, 0F, 0F));
        base.addOrReplaceChild("connection2", CubeListBuilder.create().texOffs(0, 21).addBox(-4F, -4F, 0F, 8F, 8F, 1F), PartPose.offsetAndRotation(-24F, 16F, 0F, 0F, 1.570796F, 0F));
        base.addOrReplaceChild("corner4", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 3F, 14F, 3F), PartPose.offsetAndRotation(5F, 8F, 21F, 0F, 0F, 0F));
        glass.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(8, 86).addBox(-15F, 0F, -15F, 30F, 12F, 30F), PartPose.offsetAndRotation(-8F, -6F, 8F, 0F, 0F, 0F));
        base.addOrReplaceChild("corner1", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 3F, 14F, 3F), PartPose.offsetAndRotation(5F, 8F, -8F, 0F, 0F, 0F));
        base.addOrReplaceChild("connection7", CubeListBuilder.create().texOffs(0, 21).addBox(-4F, -4F, 0F, 8F, 8F, 1F), PartPose.offsetAndRotation(8F, 16F, 16F, 0F, -1.570796F, 0F));
        frame.addOrReplaceChild("framemove1", CubeListBuilder.create().texOffs(0, 71).addBox(-10F, 0F, -2F, 20F, 2F, 4F), PartPose.offsetAndRotation(-8F, -5F, 8F, 0F, 0F, 0F));
        base.addOrReplaceChild("frame4", CubeListBuilder.create().texOffs(18, 0).addBox(-1F, 0F, -2F, 2F, 16F, 4F), PartPose.offsetAndRotation(3F, -5F, 18F, 0F, 3.141593F, 0F));
        base.addOrReplaceChild("frame2", CubeListBuilder.create().texOffs(18, 0).addBox(-1F, 0F, -2F, 2F, 16F, 4F), PartPose.offsetAndRotation(-19F, -5F, 18F, 0F, 0F, 0F));
        base.addOrReplaceChild("sideplate2", CubeListBuilder.create().texOffs(0, 35).addBox(-13F, 0F, 0F, 26F, 14F, 3F), PartPose.offsetAndRotation(-8F, 8F, 23F, 0F, 3.141593F, 0F));
        base.addOrReplaceChild("sideplate1", CubeListBuilder.create().texOffs(0, 35).addBox(-13F, 0F, 0F, 26F, 14F, 3F), PartPose.offsetAndRotation(-8F, 8F, -7F, 0F, 0F, 0F));
        base.addOrReplaceChild("connection3", CubeListBuilder.create().texOffs(0, 21).addBox(-4F, -4F, 0F, 8F, 8F, 1F), PartPose.offsetAndRotation(-24F, 16F, 16F, 0F, 1.570796F, 0F));
        base.addOrReplaceChild("sidebartop4", CubeListBuilder.create().texOffs(0, 62).addBox(-12F, 0F, 0F, 24F, 2F, 4F), PartPose.offsetAndRotation(8F, 6F, 8F, 0F, -1.570796F, 0F));
        head.addOrReplaceChild("workinglaser2", CubeListBuilder.create().texOffs(0, 102).addBox(-1F, 0F, -1F, 2F, 3F, 2F), PartPose.offsetAndRotation(-6.7F, -2.5F, 6.5F, 0F, 0.785398F, 0F));
        base.addOrReplaceChild("sidebartop1", CubeListBuilder.create().texOffs(0, 53).addBox(-16F, 0F, 0F, 32F, 2F, 4F), PartPose.offsetAndRotation(-8F, 6F, -8F, 0F, 0F, 0F));
        base.addOrReplaceChild("sidebartop2", CubeListBuilder.create().texOffs(0, 53).addBox(-16F, 0F, 0F, 32F, 2F, 4F), PartPose.offsetAndRotation(-8F, 6F, 24F, 0F, 3.141593F, 0F));
        base.addOrReplaceChild("sideplate3", CubeListBuilder.create().texOffs(0, 35).addBox(-13F, 0F, 0F, 26F, 14F, 3F), PartPose.offsetAndRotation(-23F, 8F, 8F, 0F, 1.570796F, 0F));
        base.addOrReplaceChild("frametop2", CubeListBuilder.create().texOffs(92, 35).addBox(-1F, -1F, -8F, 2F, 2F, 16F), PartPose.offsetAndRotation(3F, -4F, 8F, 0F, 3.141593F, 0F));
        head.addOrReplaceChild("workingtube1", CubeListBuilder.create().texOffs(9, 77).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offsetAndRotation(-6.7F, -2.5F, 9.5F, 0F, 0.785398F, 0F));
        base.addOrReplaceChild("sideplate4", CubeListBuilder.create().texOffs(0, 35).addBox(-13F, 0F, 0F, 26F, 14F, 3F), PartPose.offsetAndRotation(7F, 8F, 8F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("sidebartop3", CubeListBuilder.create().texOffs(0, 62).addBox(-12F, 0F, 0F, 24F, 2F, 4F), PartPose.offsetAndRotation(-24F, 6F, 8F, 0F, 1.570796F, 0F));
        base.addOrReplaceChild("connection1", CubeListBuilder.create().texOffs(0, 21).addBox(-4F, -4F, 0F, 8F, 8F, 1F), PartPose.offsetAndRotation(-16F, 16F, -8F, 0F, 0F, 0F));
        drill.addOrReplaceChild("workingdrill2", CubeListBuilder.create().texOffs(0, 85).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F), PartPose.offsetAndRotation(-9.3F, -3.5F, 8F, 0F, 0.785398F, 0F));
        tube.addOrReplaceChild("workingtube2", CubeListBuilder.create().texOffs(18, 78).addBox(-0.5F, 0F, -0.5F, 1F, 9F, 1F), PartPose.offsetAndRotation(-6.7F, -3.5F, 9.5F, 0F, 0.785398F, 0F));
        head.addOrReplaceChild("workingdrill1", CubeListBuilder.create().texOffs(0, 77).addBox(-1F, 0F, -1F, 2F, 5F, 2F), PartPose.offsetAndRotation(-9.3F, -2.5F, 8F, 0F, 0.785398F, 0F));
        base.addOrReplaceChild("innerplate", CubeListBuilder.create().texOffs(32, 59).addBox(-12F, 0F, -12F, 24F, 2F, 24F), PartPose.offsetAndRotation(-8F, 11F, 8F, 0F, 0F, 0F));
        base.addOrReplaceChild("frametop1", CubeListBuilder.create().texOffs(92, 35).addBox(-1F, -1F, -8F, 2F, 2F, 16F), PartPose.offsetAndRotation(-19F, -4F, 8F, 0F, 0F, 0F));
        base.addOrReplaceChild("connection5", CubeListBuilder.create().texOffs(0, 21).addBox(-4F, -4F, 0F, 8F, 8F, 1F), PartPose.offsetAndRotation(0F, 16F, 24F, 0F, 3.141593F, 0F));
        base.addOrReplaceChild("connection6", CubeListBuilder.create().texOffs(0, 21).addBox(-4F, -5F, 0F, 8F, 8F, 1F), PartPose.offsetAndRotation(8F, 17F, 0F, 0F, -1.570796F, 0F));
        head.addOrReplaceChild("workinglaserbeam", CubeListBuilder.create().texOffs(9, 102).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(-6.7F, 0.5F, 6.5F, 0F, 0.785398F, 0F));
        head.addOrReplaceChild("workinghead", CubeListBuilder.create().texOffs(0, 94).addBox(-3F, 0F, -3F, 6F, 2F, 6F), PartPose.offsetAndRotation(-8F, -4.5F, 8F, 0F, 0F, 0F));
        base.addOrReplaceChild("baseplate", CubeListBuilder.create().texOffs(0, 0).addBox(-16F, 0F, -16F, 32F, 2F, 32F), PartPose.offsetAndRotation(-8F, 22F, 8F, 0F, 0F, 0F));
        base.addOrReplaceChild("connection4", CubeListBuilder.create().texOffs(0, 21).addBox(-4F, -4F, 0F, 8F, 8F, 1F), PartPose.offsetAndRotation(-16F, 16F, 24F, 0F, 3.141593F, 0F));
        base.addOrReplaceChild("frame3", CubeListBuilder.create().texOffs(18, 0).addBox(-1F, 0F, -2F, 2F, 16F, 4F), PartPose.offsetAndRotation(3F, -5F, -2F, 0F, 3.141593F, 0F));
        base.addOrReplaceChild("controller", CubeListBuilder.create().texOffs(103, 0).addBox(0F, 0F, 0F, 8F, 8F, 3F), PartPose.offsetAndRotation(-4F, 12F, -7F, -0.1309F, 0F, 0F));
        base.addOrReplaceChild("midplate", CubeListBuilder.create().texOffs(58, 36).addBox(-6F, 0F, -6F, 12F, 1F, 12F), PartPose.offsetAndRotation(-8F, 10F, 8F, 0F, 0F, 0F));
        base.addOrReplaceChild("corner2", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 3F, 14F, 3F), PartPose.offsetAndRotation(-24F, 8F, -8F, 0F, 0F, 0F));
        base.addOrReplaceChild("frame1", CubeListBuilder.create().texOffs(18, 0).addBox(-1F, 0F, -2F, 2F, 16F, 4F), PartPose.offsetAndRotation(-19F, -5F, -2F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Techguns 2 {@code render}: frame and head slide, then drill, tube and laser by the progress phase. */
    @Override
    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float progress) {
        base.render(poseStack, consumer, light, overlay);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 0.25f * (float) Math.sin(progress * Math.PI * 4));
        frame.render(poseStack, consumer, light, overlay);
        poseStack.translate(0.125f * (float) Math.sin(progress * Math.PI * 8), 0.0f, 0.0f);
        head.render(poseStack, consumer, light, overlay);
        if (progress < 0.1f || progress > 0.9f) {
            drill.render(poseStack, consumer, light, overlay);
            tube.render(poseStack, consumer, light, overlay);
        } else if (progress < 0.3f) {
            float scaled = (progress - 0.1f) * 5.0f;
            poseStack.pushPose();
            float offsetX = -0.575f;
            float offsetZ = 0.5f;
            poseStack.translate(offsetX, 0.0f, offsetZ);
            poseStack.mulPose(Axis.YP.rotationDegrees(scaled * 360.0f * 8.0f));
            poseStack.translate(-offsetX, 0.0f, -offsetZ);
            double sin = Math.sin(scaled * Math.PI);
            poseStack.translate(0.0, 0.3 * sin * sin, 0.0);
            drill.render(poseStack, consumer, light, overlay);
            poseStack.popPose();
            tube.render(poseStack, consumer, light, overlay);
        } else if (progress < 0.6f) {
            drill.render(poseStack, consumer, light, overlay);
            float scaled = (progress - 0.3f) * (1.0f / 0.3f);
            poseStack.pushPose();
            double sin = Math.sin(scaled * Math.PI * 4);
            poseStack.translate(0.0, 0.3 * sin * sin, 0.0);
            tube.render(poseStack, consumer, light, overlay);
            poseStack.popPose();
        } else {
            drill.render(poseStack, consumer, light, overlay);
            laser.render(poseStack, consumer, light, overlay);
            tube.render(poseStack, consumer, light, overlay);
        }
        poseStack.popPose();
    }

    @Override
    public void renderTranslucent(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float progress) {
        glass.render(poseStack, consumer, light, overlay);
    }
}
