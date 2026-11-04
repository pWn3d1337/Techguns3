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
import net.minecraft.world.item.Item;
import techguns.client.model.GeoGunModel;

/**
 * Dev tool ({@code /tgdebug convertgun}): seeds a gun's model files with the values of its code renderer when the gun
 * moves to its model files (docs/gun_animation_plan.md). Writes into the source assets, which are hand-owned from then
 * on (Blockbench):
 * <ul>
 *     <li>the {@code root} bone pivot of every geo file of the gun: the hand origin ({@link RenderGunBase#exportRootPivot});</li>
 *     <li>the {@code aim} animation in {@code animations/item/<first geo id>.animation.json}: {@code root} moves to the
 *     aim offset over the animation ({@link RenderGunBase#exportAim}); other animations in the file are kept.</li>
 * </ul>
 * The hold positions ({@code display} block) and the display file come from datagen once the gun is listed in
 * {@code TGItemRendererSetup.DATA_DRIVEN}.
 */
public final class GunConversion {
    private static final Pattern ROOT_PIVOT = Pattern.compile("(\"name\"\\s*:\\s*\"root\"[^{}]*?\"pivot\"\\s*:\\s*\\[)([^\\]]*)(\\])",
            Pattern.DOTALL);
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?");

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
        return report;
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
