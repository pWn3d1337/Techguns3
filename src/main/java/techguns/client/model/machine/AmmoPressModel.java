package techguns.client.model.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Techguns 2 {@code ModelAmmoPress}: the frame, and per half operation a metal piece pressed by two side stamps into
 * three bullets, which the top stamp then pushes out.
 */
// MIGRATION(26.x): ModelPart / LayerDefinition rendering changes with render states in 1.21.2+.
public final class AmmoPressModel implements MachineModel {
    private final ModelPart base;
    private final ModelPart piece;
    private final ModelPart bullets;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart top;

    private AmmoPressModel(ModelPart root) {
        this.base = root.getChild("base");
        this.piece = root.getChild("piece");
        this.bullets = root.getChild("bullets");
        this.left = root.getChild("left");
        this.right = root.getChild("right");
        this.top = root.getChild("top");
    }

    public static AmmoPressModel bake() {
        return new AmmoPressModel(createLayer().bakeRoot());
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition piece = root.addOrReplaceChild("piece", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition bullets = root.addOrReplaceChild("bullets", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition left = root.addOrReplaceChild("left", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition right = root.addOrReplaceChild("right", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition top = root.addOrReplaceChild("top", CubeListBuilder.create(), PartPose.ZERO);
        base.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(48, 36).addBox(0F, 0F, 0F, 3F, 4F, 10F), PartPose.offsetAndRotation(-8F, 8F, -5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape4", CubeListBuilder.create().texOffs(66, 0).addBox(-1.5F, 0F, -4F, 2F, 8F, 8F), PartPose.offsetAndRotation(0F, 12F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape5", CubeListBuilder.create().texOffs(55, 0).addBox(0F, 0F, 0F, 1F, 1F, 3F), PartPose.offsetAndRotation(6.5F, 9.5F, -7.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape7", CubeListBuilder.create().texOffs(50, 0).addBox(0F, 0F, 0F, 1F, 12F, 1F), PartPose.offsetAndRotation(6.5F, 10.5F, -7.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape8", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 16F, 2F, 16F), PartPose.offsetAndRotation(-8F, 22F, -8F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape10", CubeListBuilder.create().texOffs(38, 25).addBox(-2F, -2F, -3F, 3F, 6F, 6F), PartPose.offsetAndRotation(0F, 15F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape11", CubeListBuilder.create().texOffs(51, 18).addBox(0F, 0F, 0F, 3F, 2F, 10F), PartPose.offsetAndRotation(-8F, 20F, -5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape12", CubeListBuilder.create().texOffs(65, 31).addBox(0F, 0F, 0F, 10F, 2F, 12F), PartPose.offsetAndRotation(-5F, 20F, -6F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape13", CubeListBuilder.create().texOffs(0, 18).addBox(0F, 0F, 0F, 1F, 3F, 8F), PartPose.offsetAndRotation(4F, 8.5F, -4F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape14", CubeListBuilder.create().texOffs(31, 19).addBox(-2F, -2F, -2F, 1F, 6F, 6F), PartPose.offsetAndRotation(9F, 15F, -1F, 0F, 0F, 0F));
        piece.addOrReplaceChild("metalpiece", CubeListBuilder.create().texOffs(110, 46).addBox(-0.5F, 0F, 0F, 1F, 5F, 5F), PartPose.offsetAndRotation(0F, 15.5F, -2.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape17", CubeListBuilder.create().texOffs(50, 0).addBox(0F, 0F, 0F, 1F, 12F, 1F), PartPose.offsetAndRotation(6.5F, 10.5F, 6.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape18", CubeListBuilder.create().texOffs(55, 0).addBox(0F, 0F, 0F, 1F, 1F, 3F), PartPose.offsetAndRotation(6.5F, 9.5F, 4.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape19", CubeListBuilder.create().texOffs(53, 0).addBox(-1.5F, 0F, 2.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 11F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape20", CubeListBuilder.create().texOffs(55, 0).addBox(-1.5F, 0F, -6.5F, 1F, 1F, 3F), PartPose.offsetAndRotation(0F, 10.5F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape21", CubeListBuilder.create().texOffs(50, 0).addBox(0F, 0F, 0F, 1F, 12F, 1F), PartPose.offsetAndRotation(-7.5F, 10.5F, -7.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape22", CubeListBuilder.create().texOffs(50, 0).addBox(0F, 0F, 0F, 1F, 12F, 1F), PartPose.offsetAndRotation(-7.5F, 10.5F, 6.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape23", CubeListBuilder.create().texOffs(66, 0).addBox(0F, 0F, 0F, 2F, 8F, 8F), PartPose.offsetAndRotation(-7.5F, 12F, -4F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape24", CubeListBuilder.create().texOffs(31, 19).addBox(-2F, -2F, -2F, 1F, 6F, 6F), PartPose.offsetAndRotation(-6F, 15F, -1F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape26", CubeListBuilder.create().texOffs(55, 0).addBox(0F, 0F, 0F, 1F, 1F, 3F), PartPose.offsetAndRotation(-7.5F, 9.5F, -7.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape27", CubeListBuilder.create().texOffs(55, 0).addBox(0F, 0F, 0F, 1F, 1F, 3F), PartPose.offsetAndRotation(-7.5F, 9.5F, 4.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape28", CubeListBuilder.create().texOffs(0, 38).addBox(-2F, 0F, -5F, 2F, 2F, 10F), PartPose.offsetAndRotation(0F, 20F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape29", CubeListBuilder.create().texOffs(55, 0).addBox(-1.5F, 0F, 3.5F, 1F, 1F, 3F), PartPose.offsetAndRotation(0F, 10.5F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape30", CubeListBuilder.create().texOffs(53, 0).addBox(-1.5F, 0F, -3.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 11F, -6F, 0F, -1.570796F, 0F));
        base.addOrReplaceChild("shape31", CubeListBuilder.create().texOffs(66, 0).addBox(0F, 0F, 0F, 2F, 8F, 8F), PartPose.offsetAndRotation(5.5F, 12F, -4F, 0F, 0F, 0F));
        right.addOrReplaceChild("pb2", CubeListBuilder.create().texOffs(94, 0).addBox(0F, 0F, 0F, 1F, 5F, 7F), PartPose.offsetAndRotation(-3F, 15F, -3.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape32", CubeListBuilder.create().texOffs(110, 0).addBox(0F, 0F, 0F, 2F, 6F, 7F), PartPose.offsetAndRotation(3.5F, 14F, -3.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape33", CubeListBuilder.create().texOffs(110, 0).addBox(0F, 0F, 0F, 2F, 6F, 7F), PartPose.offsetAndRotation(-5.5F, 14F, -3.5F, 0F, 0F, 0F));
        right.addOrReplaceChild("pb1", CubeListBuilder.create().texOffs(110, 13).addBox(0F, 0F, 0F, 3F, 5F, 6F), PartPose.offsetAndRotation(-6F, 15F, -3F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape34", CubeListBuilder.create().texOffs(68, 45).addBox(0F, 0F, 0F, 8F, 4F, 10F), PartPose.offsetAndRotation(-4F, 8F, -5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape35", CubeListBuilder.create().texOffs(100, 24).addBox(0F, 0F, 0F, 6F, 1F, 8F), PartPose.offsetAndRotation(-3F, 12F, -4F, 0F, 0F, 0F));
        left.addOrReplaceChild("pa1", CubeListBuilder.create().texOffs(110, 13).addBox(0F, 0F, 0F, 3F, 5F, 6F), PartPose.offsetAndRotation(3F, 15F, -3F, 0F, 0F, 0F));
        left.addOrReplaceChild("pa2", CubeListBuilder.create().texOffs(94, 0).addBox(0F, 0F, 0F, 1F, 5F, 7F), PartPose.offsetAndRotation(2F, 15F, -3.5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape36", CubeListBuilder.create().texOffs(51, 18).addBox(0F, 0F, 0F, 3F, 2F, 10F), PartPose.offsetAndRotation(5F, 20F, -5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape37", CubeListBuilder.create().texOffs(48, 36).addBox(0F, 0F, 0F, 3F, 4F, 10F), PartPose.offsetAndRotation(5F, 8F, -5F, 0F, 0F, 0F));
        base.addOrReplaceChild("shape38", CubeListBuilder.create().texOffs(0, 18).addBox(0F, 0F, 0F, 1F, 3F, 8F), PartPose.offsetAndRotation(-5F, 8.5F, -4F, 0F, 0F, 0F));
        top.addOrReplaceChild("pc1", CubeListBuilder.create().texOffs(108, 33).addBox(0F, 0F, 0F, 4F, 3F, 6F), PartPose.offsetAndRotation(-2F, 11F, -3F, 0F, 0F, 0F));
        bullets.addOrReplaceChild("bullet1", CubeListBuilder.create().texOffs(110, 47).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F), PartPose.offsetAndRotation(0F, 15.5F, -2F, 0F, 0.785398F, 0F));
        bullets.addOrReplaceChild("bullet3", CubeListBuilder.create().texOffs(118, 47).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F), PartPose.offsetAndRotation(0F, 15.5F, 2F, 0F, 0.785398F, 0F));
        bullets.addOrReplaceChild("bullet2", CubeListBuilder.create().texOffs(114, 47).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F), PartPose.offsetAndRotation(0F, 15.5F, 0F, 0F, 0.785398F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float progress) {
        base.render(poseStack, consumer, light, overlay);
        float sides = 0.0f;
        float push = 0.0f;
        if (progress > 0.0f) {
            // the same sequence in each half
            float p = (progress > 0.5f ? progress - 0.5f : progress) * 2.0f;
            if (p < 0.25f) {
                piece.render(poseStack, consumer, light, overlay);
                sides = (float) Math.sqrt(p * 4.0f);
            } else if (p < 0.66f) {
                float q = (p - 0.25f) * 2.439f;
                sides = (float) (0.5 + Math.sin(((1.0 - q) / 2.0 + 0.75) * 2.0 * Math.PI) / 2.0);
                bullets.render(poseStack, consumer, light, overlay);
            } else {
                float q = Math.min(1.0f, (p - 0.66f) * 3.0f);
                push = 0.5f + (float) Math.sin((Math.sqrt(q) + 0.75) * Math.PI * 2.0) / 2.0f;
                if (p < 0.9f) {
                    bullets.render(poseStack, consumer, light, overlay);
                }
            }
        }
        renderMoved(left, poseStack, consumer, light, overlay, -0.125f * sides, 0.0f);
        renderMoved(right, poseStack, consumer, light, overlay, 0.125f * sides, 0.0f);
        renderMoved(top, poseStack, consumer, light, overlay, 0.0f, 0.09375f * push);
    }

    private static void renderMoved(ModelPart part, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float x, float y) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0f);
        part.render(poseStack, consumer, light, overlay);
        poseStack.popPose();
    }
}
