package techguns.client.render.item;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.joml.Vector3f;
import techguns.client.model.GeoGunModel;
import techguns.client.model.TGGeoBridge;

/**
 * Dev tool ({@code /tgdebug convertgun}): seeds a gun's model files with the values of its code renderer when the gun
 * moves to its model files (docs/gun_animation_plan.md). Writes into the source assets, which are hand-owned from then
 * on (Blockbench):
 * <ul>
 *     <li>the {@code root} bone pivot of every geo file of the gun: the hand origin ({@link RenderGunBase#exportRootPivot});</li>
 *     <li>the {@code aim} animation in {@code animations/item/<first geo id>.animation.json}: {@code root} moves to the
 *     aim offset over the animation ({@link RenderGunBase#exportAim}); other animations in the file are kept;</li>
 *     <li>the {@code muzzle} marker bone (child of {@code root}) in the first geo file, where the old first person flash
 *     offset is: placed at the next first person frame of the gun at rest ({@link RenderGunBase}), since it needs the
 *     drawn pose. Skipped if the file has one; then the computed position is only reported.</li>
 * </ul>
 * The hold positions ({@code display} block) and the display file come from datagen once the gun is listed in
 * {@code TGItemRendererSetup.DATA_DRIVEN}.
 */
public final class GunConversion {
    private static final Pattern ROOT_PIVOT = Pattern.compile("(\"name\"\\s*:\\s*\"root\"[^{}]*?\"pivot\"\\s*:\\s*\\[)([^\\]]*)(\\])",
            Pattern.DOTALL);
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?");
    /** The {@code root} bone without cubes or rotation, as the exporter writes it. */
    private static final Pattern ROOT_BONE = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"root\"\\s*,\\s*\"pivot\"\\s*:\\s*\\[[^\\]]*\\]\\s*}");

    /** A muzzle bone waiting for a first person rest frame of {@code gun}. */
    private record MuzzleRequest(Item gun, Path file) {
    }

    @Nullable
    private static MuzzleRequest muzzleRequest;

    private GunConversion() {
    }

    /**
     * @param assets the source {@code assets/techguns} folder
     * @return one line per written file
     */
    public static List<String> convert(Item gun, Path assets) throws IOException {
        if (!(TGItemRenderers.get(gun) instanceof RenderGunBase renderer) || !(renderer.model instanceof GeoGunModel geo)) {
            throw new IllegalArgumentException("not a gun drawn from geo files");
        }
        List<String> report = new ArrayList<>();
        float[] pivot = renderer.exportRootPivot();
        for (String id : geo.geoIds()) {
            Path file = assets.resolve("geo/item/" + id + ".geo.json");
            String text = Files.readString(file, StandardCharsets.UTF_8);
            Matcher m = ROOT_PIVOT.matcher(text);
            if (!m.find()) {
                throw new IOException("no root bone pivot in " + file);
            }
            Files.writeString(file, text.substring(0, m.start(2)) + replaceNumbers(m.group(2), pivot) + text.substring(m.end(2)),
                    StandardCharsets.UTF_8);
            report.add(id + ".geo.json: root pivot " + format(pivot));
        }

        float[] aim = renderer.exportAim();
        String animId = geo.geoIds().get(0);
        Path animFile = assets.resolve("animations/item/" + animId + ".animation.json");
        JsonObject root;
        if (Files.exists(animFile)) {
            root = JsonParser.parseString(Files.readString(animFile, StandardCharsets.UTF_8)).getAsJsonObject();
        } else {
            root = new JsonObject();
            root.addProperty("format_version", "1.8.0");
            root.add("animations", new JsonObject());
        }
        root.getAsJsonObject("animations").add("aim", aimAnimation(aim));
        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        // Blockbench's layout
        writer.setIndent("\t");
        Streams.write(root, writer);
        Files.writeString(animFile, out + "\n", StandardCharsets.UTF_8);
        report.add(animId + ".animation.json: aim " + format(aim));
        report.add(requestMuzzle(gun, assets));
        return report;
    }

    /** Asks for the {@code muzzle} bone at the next first person rest frame of {@code gun}; returns a report line. */
    public static String requestMuzzle(Item gun, Path assets) {
        if (!(TGItemRenderers.get(gun) instanceof RenderGunBase renderer) || !(renderer.model instanceof GeoGunModel geo)) {
            throw new IllegalArgumentException("not a gun drawn from geo files");
        }
        if (renderer.muzzleEffect == null) {
            return "muzzle: no muzzle effect, no bone";
        }
        muzzleRequest = new MuzzleRequest(gun, assets.resolve("geo/item/" + geo.geoIds().get(0) + ".geo.json"));
        return "muzzle: placed at the next first person frame at rest (main hand, not aiming)";
    }

    /** True while a muzzle bone for {@code gun} waits for a rest frame. */
    public static boolean wantsMuzzle(Item gun) {
        return muzzleRequest != null && muzzleRequest.gun() == gun;
    }

    static void muzzleFailed(String reason) {
        muzzleRequest = null;
        chat("muzzle: failed, " + reason);
    }

    /**
     * Writes the requested muzzle bone at {@code local}: GeckoLib model space of the first geo file, in blocks. If the file
     * already has one ({@code existing*} non-null), only reports: the computed pivot, the existing bone through the same
     * conversion, and where the existing bone puts the flash against the old offset {@code flash}.
     */
    static void placeMuzzle(Vector3f local, @Nullable Vector3f existingLocal, @Nullable Vector3f existingFlash, Vector3f flash) {
        MuzzleRequest request = muzzleRequest;
        muzzleRequest = null;
        if (request == null) {
            return;
        }
        float[] pivot = filePivot(local);
        String name = request.file().getFileName().toString();
        try {
            String text = Files.readString(request.file(), StandardCharsets.UTF_8);
            if (text.contains("\"name\": \"" + TGGeoBridge.MUZZLE + "\"")) {
                chat("muzzle: " + name + " already has one; computed " + format(pivot)
                        + (existingLocal != null ? ", existing " + format(filePivot(existingLocal)) : "")
                        + (existingFlash != null ? ", its flash " + format(new float[]{existingFlash.x(), existingFlash.y(), existingFlash.z()})
                        + " vs old " + format(new float[]{flash.x(), flash.y(), flash.z()}) : ""));
                return;
            }
            Matcher m = ROOT_BONE.matcher(text);
            if (!m.find()) {
                chat("muzzle: failed, no plain root bone in " + name);
                return;
            }
            String nl = text.contains("\r\n") ? "\r\n" : "\n";
            String bone = "," + nl
                    + "        {" + nl
                    + "          \"name\": \"" + TGGeoBridge.MUZZLE + "\"," + nl
                    + "          \"parent\": \"root\"," + nl
                    + "          \"pivot\": [" + nl
                    + "            " + round(pivot[0]).toPlainString() + "," + nl
                    + "            " + round(pivot[1]).toPlainString() + "," + nl
                    + "            " + round(pivot[2]).toPlainString() + nl
                    + "          ]" + nl
                    + "        }";
            Files.writeString(request.file(), text.substring(0, m.end()) + bone + text.substring(m.end()), StandardCharsets.UTF_8);
            chat("muzzle: " + name + " bone at " + format(pivot));
        } catch (IOException e) {
            chat("muzzle: failed, " + e.getMessage());
        }
    }

    /** File pivot (pixels) of a point in GeckoLib model space (blocks): GeckoLib bakes a bone pivot as (-x, y, z) of the file. */
    private static float[] filePivot(Vector3f local) {
        return new float[]{-16f * local.x(), 16f * local.y(), 16f * local.z()};
    }

    private static void chat(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(text), false);
        }
    }

    /** {@code root} from rest to the aim offset over one second (sampled by zoom progress). */
    private static JsonObject aimAnimation(float[] aim) {
        JsonObject position = new JsonObject();
        position.add("0.0", vector(new float[]{0f, 0f, 0f}));
        position.add("1.0", vector(aim));
        JsonObject bone = new JsonObject();
        bone.add("position", position);
        JsonObject bones = new JsonObject();
        bones.add("root", bone);
        JsonObject animation = new JsonObject();
        animation.addProperty("animation_length", 1.0);
        animation.add("bones", bones);
        return animation;
    }

    private static JsonArray vector(float[] v) {
        JsonArray array = new JsonArray();
        for (float f : v) {
            array.add(round(f));
        }
        return array;
    }

    /** Replaces the three numbers of a JSON array body, keeping its layout. */
    private static String replaceNumbers(String body, float[] values) {
        Matcher m = NUMBER.matcher(body);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (m.find()) {
            m.appendReplacement(sb, round(values[i++]).toPlainString());
        }
        m.appendTail(sb);
        if (i != values.length) {
            throw new IllegalStateException("pivot has " + i + " numbers");
        }
        return sb.toString();
    }

    /** 4 decimals, trailing zeros dropped (pixels; float noise below that). */
    private static BigDecimal round(float f) {
        BigDecimal d = new BigDecimal(Float.toString(f)).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
        return d.signum() == 0 ? BigDecimal.ZERO : d;
    }

    private static String format(float[] v) {
        return "[" + round(v[0]).toPlainString() + ", " + round(v[1]).toPlainString() + ", " + round(v[2]).toPlainString() + "]";
    }
}
