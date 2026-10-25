package techguns.client.model;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import techguns.Techguns;

/**
 * Dev tool (Q3 GeckoLib prototype): converts a Techguns Java model ({@link TGModel} with {@link TGModelPart} fields)
 * into a Bedrock {@code .geo.json} that renders identically through GeckoLib.
 * <p>
 * Coordinates: the renderer turns GeckoLib's output 180 degrees about Z into Java model space J (pixels, y down), and
 * GeckoLib mirrors Bedrock x on load. Together: cube origin = (px + x, -(py + y + h), pz + z), bone pivot =
 * (px, -py, pz), rotation unchanged. Box UV faces then line up with the Java faces except east/west, which the
 * inverted {@code mirror} flag swaps back (a turn about X would swap north/south instead, with no flag to undo it).
 * GeckoLib rotates Z, Y, X while Java models rotate X, Y, Z, so parts rotated on several axes become nested
 * single-axis bones.
 */
public final class TGGeoExporter {
    private static final float RAD2DEG = 180.0f / (float) Math.PI;
    /** Bones whose name ends with this are drawn full bright by the gun renderer. */
    public static final String GLOW_SUFFIX = "_glow";

    private TGGeoExporter() {
    }

    /**
     * @param groups    optional animation groups: group bone name to the part field names it contains
     * @param glowParts part field names drawn full bright in the Java model; their bones get the {@code _glow} suffix
     */
    public static Path export(TGModel model, String id, Map<String, List<String>> groups, Set<String> glowParts, Path dir)
            throws IOException {
        Map<String, TGModelPart> parts = parts(model);
        Map<String, String> parentOf = new LinkedHashMap<>();
        groups.forEach((group, members) -> members.forEach(m -> parentOf.put(m, group)));

        JsonArray bones = new JsonArray();
        JsonObject root = bone("root", null, 0, 0, 0);
        bones.add(root);
        for (String group : groups.keySet()) {
            bones.add(bone(group, "root", 0, 0, 0));
        }
        for (Map.Entry<String, TGModelPart> e : parts.entrySet()) {
            String name = glowParts.contains(e.getKey()) ? e.getKey() + GLOW_SUFFIX : e.getKey();
            addPart(bones, name, parentOf.getOrDefault(e.getKey(), "root"), e.getValue(), 0, 0, 0);
        }

        JsonObject description = new JsonObject();
        description.addProperty("identifier", "geometry.techguns." + id);
        // Tabula exports often set the texture size per part, overriding the model's; one geo file has one size
        int[] size = textureSize(model, parts.values());
        description.addProperty("texture_width", size[0]);
        description.addProperty("texture_height", size[1]);
        description.addProperty("visible_bounds_width", 4);
        description.addProperty("visible_bounds_height", 3);
        JsonArray offset = new JsonArray();
        offset.add(0);
        offset.add(0);
        offset.add(0);
        description.add("visible_bounds_offset", offset);

        JsonObject geometry = new JsonObject();
        geometry.add("description", description);
        geometry.add("bones", bones);
        JsonArray geometries = new JsonArray();
        geometries.add(geometry);
        JsonObject file = new JsonObject();
        file.addProperty("format_version", "1.12.0");
        file.add("minecraft:geometry", geometries);

        Files.createDirectories(dir);
        Path out = dir.resolve(id + ".geo.json");
        Files.writeString(out, new GsonBuilder().setPrettyPrinting().create().toJson(file));
        return out;
    }

    private static int[] textureSize(TGModel model, Iterable<TGModelPart> parts) {
        int[] size = null;
        for (TGModelPart p : parts) {
            int[] partSize = {p.textureWidth, p.textureHeight};
            if (size == null) {
                size = partSize;
            } else if (size[0] != partSize[0] || size[1] != partSize[1]) {
                throw new IllegalStateException("Parts of " + model.getClass().getSimpleName() + " use different texture sizes");
            }
        }
        return size != null ? size : new int[] {model.textureWidth, model.textureHeight};
    }

    private static final Map<Class<?>, Map<String, TGModelPart>> SCALED_PARTS = new java.util.concurrent.ConcurrentHashMap<>();

    /** Parts with a static render scale, by bone name (field name); their geo bones are scaled at render time. */
    public static Map<String, TGModelPart> scaledParts(TGModel model) {
        return SCALED_PARTS.computeIfAbsent(model.getClass(), c -> {
            Map<String, TGModelPart> scaled = new LinkedHashMap<>();
            parts(model).forEach((name, p) -> {
                if (p.hasRenderScale()) {
                    scaled.put(name, p);
                    if (!Arrays.equals(geoScale(p), new float[] {p.scaleX, p.scaleY, p.scaleZ})) {
                        Techguns.LOGGER.warn("{}.{}: render scale doesn't commute with its rotation (shear), geo model approximates it",
                                c.getSimpleName(), name);
                    }
                }
            });
            return scaled;
        });
    }

    /**
     * Bone scale (in the part's rotated frame) for a part's static render scale. Exact when the scale commutes with the
     * rotation; otherwise the Java render shears the box, which a bone can't, so each rotated pair of axes gets the
     * geometric mean of its two scales.
     */
    public static float[] geoScale(TGModelPart p) {
        float sx = p.scaleX, sy = p.scaleY, sz = p.scaleZ;
        if (p.roll != 0 && sx != sy) {
            sx = sy = (float) Math.sqrt(sx * sy);
        }
        if (p.yaw != 0 && sx != sz) {
            sx = sz = (float) Math.sqrt(sx * sz);
        }
        if (p.pitch != 0 && sy != sz) {
            sy = sz = (float) Math.sqrt(sy * sz);
        }
        return new float[] {sx, sy, sz};
    }

    /** Name of the bone that holds the part's cube (the innermost of the nested rotation bones). */
    public static String cubeBoneName(String name, TGModelPart p) {
        int rotations = (p.pitch != 0 ? 1 : 0) + (p.yaw != 0 ? 1 : 0) + (p.roll != 0 ? 1 : 0);
        return rotations > 1 ? name + "_r" + (rotations - 1) : name;
    }

    /** Part fields in declaration order. */
    private static Map<String, TGModelPart> parts(TGModel model) {
        Map<String, TGModelPart> parts = new LinkedHashMap<>();
        for (Class<?> c = model.getClass(); c != TGModel.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() == TGModelPart.class && !Modifier.isStatic(f.getModifiers())) {
                    try {
                        f.setAccessible(true);
                        TGModelPart part = (TGModelPart) f.get(model);
                        if (part != null) {
                            parts.putIfAbsent(f.getName(), part);
                        }
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        return parts;
    }

    /**
     * Adds a part (and, recursively, its children: Java children get the parent's pivot translation but not its
     * rotation, so they are emitted as siblings with absolute pivots).
     */
    private static void addPart(JsonArray bones, String name, String parent, TGModelPart p, float ox, float oy, float oz) {
        float px = ox + p.pivotX;
        float py = oy + p.pivotY;
        float pz = oz + p.pivotZ;
        float rx = p.pitch * RAD2DEG;
        float ry = p.yaw * RAD2DEG;
        float rz = p.roll * RAD2DEG;

        // one bone per rotated axis, outermost X (Java order X, Y, Z)
        List<float[]> rotations = new ArrayList<>();
        if (rx != 0) {
            rotations.add(new float[] {rx, 0, 0});
        }
        if (ry != 0) {
            rotations.add(new float[] {0, ry, 0});
        }
        if (rz != 0) {
            rotations.add(new float[] {0, 0, rz});
        }
        String current = parent;
        String boneName = name;
        for (int i = 0; i < Math.max(1, rotations.size()); i++) {
            boneName = i == 0 ? name : name + "_r" + i;
            JsonObject bone = bone(boneName, current, px, -py, pz);
            if (i < rotations.size()) {
                float[] r = rotations.get(i);
                bone.add("rotation", vec(r[0], r[1], r[2]));
            }
            bones.add(bone);
            current = boneName;
        }
        JsonObject innermost = bones.get(bones.size() - 1).getAsJsonObject();

        if (p.width != 0 || p.height != 0 || p.length != 0) {
            JsonObject cube = new JsonObject();
            cube.add("origin", vec(px + p.x, -(py + p.y + p.height), pz + p.z));
            cube.add("size", vec(p.width, p.height, p.length));
            if (p.width < 1 && p.height < 1 && p.length < 1) {
                // GeckoLib floors box UV sizes, so a sub-pixel cube (glow dots) would sample the corner of its
                // texture area; vanilla samples inside it. Give every face the texel at the box UV origin.
                JsonObject faces = new JsonObject();
                for (String face : new String[] {"north", "east", "south", "west", "up", "down"}) {
                    JsonObject f = new JsonObject();
                    JsonArray faceUv = new JsonArray();
                    faceUv.add(p.u + p.length + 0.25f);
                    faceUv.add(p.v + p.length + 0.25f);
                    f.add("uv", faceUv);
                    JsonArray faceSize = new JsonArray();
                    faceSize.add(0.5f);
                    faceSize.add(0.5f);
                    f.add("uv_size", faceSize);
                    faces.add(face, f);
                }
                cube.add("uv", faces);
            } else {
                JsonArray uv = new JsonArray();
                uv.add(p.u);
                uv.add(p.v);
                cube.add("uv", uv);
            }
            if (p.mirror) {
                cube.addProperty("mirror", true);
            }
            if (p.expansionX != 0 && p.expansionX == p.expansionY && p.expansionY == p.expansionZ) {
                cube.addProperty("inflate", p.expansionX);
            }
            JsonArray cubes = new JsonArray();
            cubes.add(cube);
            innermost.add("cubes", cubes);
        }

        int i = 0;
        for (TGModelPart child : p.children()) {
            addPart(bones, name + "_c" + i++, parent, child, px, py, pz);
        }
    }

    private static JsonObject bone(String name, String parent, float px, float py, float pz) {
        JsonObject bone = new JsonObject();
        bone.addProperty("name", name);
        if (parent != null) {
            bone.addProperty("parent", parent);
        }
        bone.add("pivot", vec(px, py, pz));
        return bone;
    }

    private static JsonArray vec(float x, float y, float z) {
        JsonArray a = new JsonArray();
        a.add(round(x));
        a.add(round(y));
        a.add(round(z));
        return a;
    }

    /** Keeps the files readable: 4 decimals, no -0. */
    private static float round(float v) {
        float r = Math.round(v * 10000f) / 10000f;
        return r == 0f ? 0f : r;
    }
}
