package techguns.client.model.turret;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Techguns 2 {@code ModelTurretBase}: the box the turret stands on. */
// MIGRATION(26.x): ModelPart / LayerDefinition rendering changes with render states in 1.21.2+.
public final class TurretBaseModel {
    private final ModelPart root;

    private TurretBaseModel(ModelPart root) {
        this.root = root;
    }

    public static TurretBaseModel bake() {
        return new TurretBaseModel(createLayer().bakeRoot());
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("corner01", CubeListBuilder.create().texOffs(0, 30).addBox(0F, 0F, 0F, 2F, 15F, 2F), PartPose.offsetAndRotation(-8F, 9F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(30, 0).addBox(0F, 0F, 0F, 8F, 2F, 8F), PartPose.offsetAndRotation(-4F, 8F, -4F, 0F, 0F, 0F));
        root.addOrReplaceChild("corner02", CubeListBuilder.create().texOffs(0, 30).addBox(0F, 0F, 0F, 2F, 15F, 2F), PartPose.offsetAndRotation(-8F, 9F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("corner03", CubeListBuilder.create().texOffs(0, 30).addBox(0F, 0F, 0F, 2F, 15F, 2F), PartPose.offsetAndRotation(6F, 9F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("bar03", CubeListBuilder.create().texOffs(36, 18).addBox(0F, 0F, -1F, 6F, 1F, 2F), PartPose.offsetAndRotation(7F, 9F, -7F, 0F, -2.356194F, 0F));
        root.addOrReplaceChild("corner04", CubeListBuilder.create().texOffs(0, 30).addBox(0F, 0F, 0F, 2F, 15F, 2F), PartPose.offsetAndRotation(6F, 9F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("bottom04", CubeListBuilder.create().texOffs(0, 15).addBox(0F, 0F, 0F, 1F, 2F, 12F), PartPose.offsetAndRotation(6F, 22F, 7F, 0F, -1.570796F, 0F));
        root.addOrReplaceChild("bar04", CubeListBuilder.create().texOffs(36, 18).addBox(0F, 0F, -1F, 6F, 1F, 2F), PartPose.offsetAndRotation(7F, 9F, 7F, 0F, 2.356194F, 0F));
        root.addOrReplaceChild("bar01", CubeListBuilder.create().texOffs(36, 18).addBox(0F, 0F, -1F, 6F, 1F, 2F), PartPose.offsetAndRotation(-7F, 9F, 7F, 0F, 0.785398F, 0F));
        root.addOrReplaceChild("bar02", CubeListBuilder.create().texOffs(36, 18).addBox(0F, 0F, -1F, 6F, 1F, 2F), PartPose.offsetAndRotation(-7F, 9F, -7F, 0F, -0.785398F, 0F));
        root.addOrReplaceChild("base01", CubeListBuilder.create().texOffs(8, 36).addBox(0F, 0F, 0F, 14F, 14F, 14F), PartPose.offsetAndRotation(-7F, 10F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("bottom01", CubeListBuilder.create().texOffs(0, 15).addBox(0F, 0F, 0F, 1F, 2F, 12F), PartPose.offsetAndRotation(-8F, 22F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("socket02", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1F, 6F, 6F), PartPose.offsetAndRotation(-8F, 13F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("socket04", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1F, 6F, 6F), PartPose.offsetAndRotation(3F, 13F, 7F, 0F, -1.570796F, 0F));
        root.addOrReplaceChild("bottom02", CubeListBuilder.create().texOffs(0, 15).addBox(0F, 0F, 0F, 1F, 2F, 12F), PartPose.offsetAndRotation(7F, 22F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("socket01", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1F, 6F, 6F), PartPose.offsetAndRotation(7F, 13F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("bottom03", CubeListBuilder.create().texOffs(0, 15).addBox(0F, 0F, 0F, 1F, 2F, 12F), PartPose.offsetAndRotation(6F, 22F, -8F, 0F, -1.570796F, 0F));
        root.addOrReplaceChild("socket03", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1F, 6F, 6F), PartPose.offsetAndRotation(3F, 13F, -8F, 0F, -1.570796F, 0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        root.render(poseStack, consumer, light, overlay);
    }
}
