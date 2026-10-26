package techguns.client.model.machine;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Techguns 2 {@code ModelMetalPress}: the frame, the metal piece shown while pressing, and the press head that moves
 * down twice per operation.
 */
// MIGRATION(26.x): ModelPart / LayerDefinition rendering changes with render states in 1.21.2+.
public final class MetalPressModel implements MachineModel {
    private final ModelPart base;
    private final ModelPart piece;
    private final ModelPart press;

    private MetalPressModel(ModelPart root) {
        this.base = root.getChild("base");
        this.piece = root.getChild("piece");
        this.press = root.getChild("press");
    }

    public static MetalPressModel bake() {
        return new MetalPressModel(createLayer().bakeRoot());
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition piece = root.addOrReplaceChild("piece", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition press = root.addOrReplaceChild("press", CubeListBuilder.create(), PartPose.ZERO);
        base.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(51, 18).addBox(0F, 0F, 0F, 3F, 2F, 10F), PartPose.offsetAndRotation(5F, 20F, -5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(0, 50).addBox(0F, 0F, 0F, 16F, 2F, 12F), PartPose.offsetAndRotation(-8F, 8F, -6F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape3", CubeListBuilder.create().texOffs(66, 0).addBox(0F, 0F, 0F, 2F, 8F, 8F), PartPose.offsetAndRotation(5.5F, 12F, -4F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape4", CubeListBuilder.create().texOffs(66, 0).addBox(-1.5F, 0F, -4F, 2F, 8F, 8F), PartPose.offsetAndRotation(0F, 12F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape5", CubeListBuilder.create().texOffs(55, 0).addBox(0F, 0F, 0F, 1F, 1F, 3F), PartPose.offsetAndRotation(6.5F, 10.5F, -7.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape6", CubeListBuilder.create().texOffs(0, 38).addBox(0F, 0F, 0F, 2F, 2F, 10F), PartPose.offsetAndRotation(6F, 10F, -5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape7", CubeListBuilder.create().texOffs(50, 0).addBox(0F, 0F, 0F, 1F, 11F, 1F), PartPose.offsetAndRotation(6.5F, 11.5F, -7.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape8", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 16F, 2F, 16F), PartPose.offsetAndRotation(-8F, 22F, -8F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape10", CubeListBuilder.create().texOffs(38, 25).addBox(-2F, -2F, -3F, 3F, 6F, 6F), PartPose.offsetAndRotation(0F, 15F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape11", CubeListBuilder.create().texOffs(51, 18).addBox(0F, 0F, 0F, 3F, 2F, 10F), PartPose.offsetAndRotation(-8F, 20F, -5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape12", CubeListBuilder.create().texOffs(65, 31).addBox(0F, 0F, 0F, 10F, 2F, 12F), PartPose.offsetAndRotation(-5F, 20F, -6F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape13", CubeListBuilder.create().texOffs(0, 28).addBox(0F, 0F, 0F, 12F, 2F, 7F), PartPose.offsetAndRotation(-6F, 10F, -3.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape14", CubeListBuilder.create().texOffs(38, 25).addBox(-2F, -2F, -2F, 3F, 6F, 6F), PartPose.offsetAndRotation(7F, 15F, -1F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape15", CubeListBuilder.create().texOffs(24, 37).addBox(0F, 0F, 0F, 10F, 3F, 10F), PartPose.offsetAndRotation(-5F, 10F, -5F, 0F, 0F, 0F));
        piece.addOrReplaceChild("metalpiece", CubeListBuilder.create().texOffs(104, 46).addBox(0F, 0F, 0F, 6F, 1F, 6F), PartPose.offsetAndRotation(-3F, 18F, -3F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape17", CubeListBuilder.create().texOffs(50, 0).addBox(0F, 0F, 0F, 1F, 11F, 1F), PartPose.offsetAndRotation(6.5F, 11.5F, 6.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape18", CubeListBuilder.create().texOffs(55, 0).addBox(0F, 0F, 0F, 1F, 1F, 3F), PartPose.offsetAndRotation(6.5F, 10.5F, 4.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape19", CubeListBuilder.create().texOffs(53, 0).addBox(-1.5F, 0F, 2.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 11F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape20", CubeListBuilder.create().texOffs(55, 0).addBox(-1.5F, 0F, -6.5F, 1F, 1F, 3F), PartPose.offsetAndRotation(0F, 10.5F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape21", CubeListBuilder.create().texOffs(50, 0).addBox(0F, 0F, 0F, 1F, 11F, 1F), PartPose.offsetAndRotation(-7.5F, 11.5F, -7.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape22", CubeListBuilder.create().texOffs(50, 0).addBox(0F, 0F, 0F, 1F, 11F, 1F), PartPose.offsetAndRotation(-7.5F, 11.5F, 6.5F, 0F, 0F, 0F));
        press.addOrReplaceChild("p1", CubeListBuilder.create().texOffs(89, 10).addBox(0F, 0F, 0F, 8F, 5F, 8F), PartPose.offsetAndRotation(-4F, 9F, -4F, 0F, 0F, 0F));
        press.addOrReplaceChild("p2", CubeListBuilder.create().texOffs(86, 0).addBox(0F, 0F, 0F, 1F, 5F, 1F), PartPose.offsetAndRotation(3.5F, 9F, -0.5F, 0F, 0F, 0F));
        press.addOrReplaceChild("p3", CubeListBuilder.create().texOffs(86, 0).addBox(0F, 0F, 0F, 9F, 1F, 9F), PartPose.offsetAndRotation(-4.5F, 14F, -4.5F, 0F, 0F, 0F));
        press.addOrReplaceChild("p4", CubeListBuilder.create().texOffs(86, 0).addBox(0F, 0F, 0F, 1F, 5F, 1F), PartPose.offsetAndRotation(-0.5F, 9F, -4.5F, 0F, 0F, 0F));
        press.addOrReplaceChild("p5", CubeListBuilder.create().texOffs(86, 0).addBox(0F, 0F, 0F, 1F, 5F, 1F), PartPose.offsetAndRotation(-0.5F, 9F, 3.5F, 0F, 0F, 0F));
        press.addOrReplaceChild("p6", CubeListBuilder.create().texOffs(86, 0).addBox(0F, 0F, 0F, 1F, 5F, 1F), PartPose.offsetAndRotation(-4.5F, 9F, -0.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape23", CubeListBuilder.create().texOffs(66, 0).addBox(0F, 0F, 0F, 2F, 8F, 8F), PartPose.offsetAndRotation(-7.5F, 12F, -4F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape24", CubeListBuilder.create().texOffs(38, 25).addBox(-2F, -2F, -2F, 3F, 6F, 6F), PartPose.offsetAndRotation(-6F, 15F, -1F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape25", CubeListBuilder.create().texOffs(0, 38).addBox(0F, 0F, 0F, 2F, 2F, 10F), PartPose.offsetAndRotation(-8F, 10F, -5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape26", CubeListBuilder.create().texOffs(55, 0).addBox(0F, 0F, 0F, 1F, 1F, 3F), PartPose.offsetAndRotation(-7.5F, 10.5F, -7.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape27", CubeListBuilder.create().texOffs(55, 0).addBox(0F, 0F, 0F, 1F, 1F, 3F), PartPose.offsetAndRotation(-7.5F, 10.5F, 4.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape28", CubeListBuilder.create().texOffs(0, 38).addBox(-2F, 0F, -5F, 2F, 2F, 10F), PartPose.offsetAndRotation(0F, 20F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape29", CubeListBuilder.create().texOffs(55, 0).addBox(-1.5F, 0F, 3.5F, 1F, 1F, 3F), PartPose.offsetAndRotation(0F, 10.5F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape30", CubeListBuilder.create().texOffs(53, 0).addBox(-1.5F, 0F, -3.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 11F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape31", CubeListBuilder.create().texOffs(67, 46).addBox(0F, 0F, 0F, 9F, 1F, 9F), PartPose.offsetAndRotation(-4.5F, 19F, -4.5F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float progress) {
        base.render(poseStack, consumer, light, overlay);
        poseStack.pushPose();
        if (progress > 0.0f) {
            piece.render(poseStack, consumer, light, overlay);
            // two strokes: the head goes down and up in each half of the operation
            float p = progress > 0.5f ? progress - 0.5f : progress;
            float y = 0.5f + (float) Math.sin((Math.sqrt(p * 2.0f) + 0.75f) * Math.PI * 2.0f) / 2.0f;
            poseStack.translate(0.0f, 0.25f * y, 0.0f);
        }
        press.render(poseStack, consumer, light, overlay);
        poseStack.popPose();
    }
}
