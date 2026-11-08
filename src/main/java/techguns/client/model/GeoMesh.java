package techguns.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Quaternionf;

/**
 * A mesh drawn at a geo model's mesh bone ({@link TGGeoBridge#MESH}) from data (the display file's {@code meshes}): an
 * OBJ mesh, turned by {@code rotation} inside the bone's space.
 */
public record GeoMesh(ObjMesh mesh, Quaternionf rotation) {
    /** Draws the mesh; {@code poseStack} is at the bone's space (GeckoLib space, y up, blocks). */
    public void render(PoseStack poseStack, VertexConsumer vertices, int light, int overlay) {
        poseStack.mulPose(rotation);
        mesh.render(poseStack, vertices, light, overlay);
    }
}
