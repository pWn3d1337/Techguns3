package techguns.client.model.turret;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Techguns 2 {@code ModelTurret}: the swivel (plate and two posts, turned by the yaw) and the gun mount (also turned
 * by the pitch).
 */
// MIGRATION(26.x): ModelPart / LayerDefinition rendering changes with render states in 1.21.2+.
public final class TurretModel {
    private final ModelPart swivel;
    private final ModelPart mount;

    private TurretModel(ModelPart root) {
        this.swivel = root.getChild("swivel");
        this.mount = root.getChild("mount");
    }

    public static TurretModel bake() {
        return new TurretModel(createLayer().bakeRoot());
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition swivel = root.addOrReplaceChild("swivel", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("mount", CubeListBuilder.create().texOffs(34, 12).addBox(0F, 0F, -2F, 4F, 1F, 4F), PartPose.offsetAndRotation(-2F, 13F, 0F, 0F, 0F, 0F));
        swivel.addOrReplaceChild("base", CubeListBuilder.create().texOffs(32, 22).addBox(-4F, 0F, -4F, 8F, 1F, 8F), PartPose.offsetAndRotation(0F, 23F, 0F, 0F, 0F, 0F));
        swivel.addOrReplaceChild("stick01", CubeListBuilder.create().texOffs(9, 30).addBox(0F, 0F, 0F, 2F, 11F, 2F), PartPose.offsetAndRotation(2F, 12F, -1F, 0F, 0F, 0F));
        swivel.addOrReplaceChild("stick02", CubeListBuilder.create().texOffs(9, 30).addBox(0F, 0F, 0F, 2F, 11F, 2F), PartPose.offsetAndRotation(-4F, 12F, -1F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public void renderSwivel(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        swivel.render(poseStack, consumer, light, overlay);
    }

    public void renderMount(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        mount.render(poseStack, consumer, light, overlay);
    }
}
