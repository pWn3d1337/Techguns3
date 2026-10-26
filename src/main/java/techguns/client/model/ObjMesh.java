package techguns.client.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import techguns.Techguns;

/**
 * Triangle mesh from a Wavefront OBJ file (positions, texture coordinates, normals), drawn through a
 * {@link VertexConsumer}. Replaces Fabric's {@code TGObjLoader} baked models: GeckoLib has no OBJ support, and drawing
 * the mesh directly keeps OBJ guns on the same renderer as the box models. Materials are ignored; the renderer
 * supplies the texture. Loaded lazily from the client resources on first draw.
 */
public final class ObjMesh {
    private final ResourceLocation file;
    private final boolean flipV;
    /** Per triangle corner: x, y, z, u, v, nx, ny, nz. */
    private float[] corners;

    /**
     * @param file  e.g. {@code techguns:models/item/grenadelauncher.obj}
     * @param flipV use {@code 1 - v} (OBJ v points up, Minecraft texture v down; some exports already flipped it)
     */
    public ObjMesh(ResourceLocation file, boolean flipV) {
        this.file = file;
        this.flipV = flipV;
    }

    public void render(PoseStack poseStack, VertexConsumer vertices, int light, int overlay) {
        if (corners == null) {
            corners = load();
        }
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i < corners.length; i += 8 * 3) {
            // the entity formats draw quads: repeat the last corner of each triangle
            for (int c = 0; c < 4; c++) {
                int o = i + Math.min(c, 2) * 8;
                vertices.addVertex(pose, corners[o], corners[o + 1], corners[o + 2]).setColor(-1).setUv(corners[o + 3], corners[o + 4])
                        .setOverlay(overlay).setLight(light).setNormal(pose, corners[o + 5], corners[o + 6], corners[o + 7]);
            }
        }
    }

    private float[] load() {
        List<Vector3f> positions = new ArrayList<>();
        List<float[]> uvs = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Float> out = new ArrayList<>();
        try (BufferedReader reader = Minecraft.getInstance().getResourceManager().openAsReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] t = line.trim().split("\\s+");
                switch (t[0]) {
                    case "v" -> positions.add(new Vector3f(Float.parseFloat(t[1]), Float.parseFloat(t[2]), Float.parseFloat(t[3])));
                    case "vt" -> {
                        float v = Float.parseFloat(t[2]);
                        uvs.add(new float[] {Float.parseFloat(t[1]), flipV ? 1.0f - v : v});
                    }
                    case "vn" -> normals.add(new Vector3f(Float.parseFloat(t[1]), Float.parseFloat(t[2]), Float.parseFloat(t[3])));
                    case "f" -> {
                        // fan-triangulate polygons
                        for (int k = 2; k + 1 < t.length; k++) {
                            for (String corner : new String[] {t[1], t[k], t[k + 1]}) {
                                String[] idx = corner.split("/");
                                Vector3f p = positions.get(Integer.parseInt(idx[0]) - 1);
                                float[] uv = idx.length > 1 && !idx[1].isEmpty() ? uvs.get(Integer.parseInt(idx[1]) - 1) : new float[2];
                                Vector3f n = idx.length > 2 ? normals.get(Integer.parseInt(idx[2]) - 1) : new Vector3f(0, 1, 0);
                                out.add(p.x);
                                out.add(p.y);
                                out.add(p.z);
                                out.add(uv[0]);
                                out.add(uv[1]);
                                out.add(n.x);
                                out.add(n.y);
                                out.add(n.z);
                            }
                        }
                    }
                    default -> {
                        // comments, groups, objects, materials and smoothing groups are not needed
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            Techguns.LOGGER.error("Could not load OBJ model {}", file, e);
            return new float[0];
        }
        float[] result = new float[out.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = out.get(i);
        }
        return result;
    }
}
