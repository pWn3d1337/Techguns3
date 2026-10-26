package techguns.client.model.armor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Helpers for translating 1.12 {@code ModelRenderer} boxes (rotation point + box + rotation) to {@link PartDefinition}s. */
final class ArmorMeshUtil {
    private ArmorMeshUtil() {
    }

    static CubeListBuilder empty() {
        return CubeListBuilder.create();
    }

    /**
     * One 1.12 ModelRenderer with a single box, added as a child of {@code parent}.
     *
     * @param mirror the renderer's {@code mirror} flag as it was when {@code addBox} ran
     */
    static void box(PartDefinition parent, String name, int u, int v, boolean mirror,
            float px, float py, float pz, float rx, float ry, float rz,
            float x, float y, float z, int w, int h, int d, CubeDeformation deformation) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).mirror(mirror).addBox(x, y, z, w, h, d, deformation),
                PartPose.offsetAndRotation(px, py, pz, rx, ry, rz));
    }
}
