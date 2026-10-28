package techguns.data.legacy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import com.google.common.hash.Hashing;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import techguns.Techguns;
import techguns.compat.fusion.FusionTextures;

/**
 * The Techguns 2 connected textures (CTM mod format, {@code src/legacy/ctm}) as Fusion connecting textures, in the
 * built-in resource pack {@code resourcepacks/fusion} that {@link FusionTextures} enables only when Fusion is
 * installed: the pack replaces the plain textures with Fusion sheets and their {@code .mcmeta}.
 */
public class LegacyConnectedTextureProvider implements DataProvider {
    /** Techguns 2 textures with a CTM sheet; the drill rod is CTM type {@code pillar}, the rest type {@code ctm}. */
    private static final List<String> CTM = List.of("concrete_brown_light_scaff", "concrete_brown_pipes", "container_blue", "container_green",
            "container_orange", "container_red", "drill_engine_c", "drill_engine_top_c", "drill_frame_c", "drill_scaffold_c",
            "nethermetal_border_alpha", "nethermetal_border_red", "nethermetal_plate_black", "nethermetal_plate_red", "panel_large_border",
            "steelframe_blue", "steelframe_dark", "steelframe_scaffold");
    private static final List<String> PILLAR = List.of("drill_rod");

    private final PackOutput output;

    public LegacyConnectedTextureProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path legacy = LegacyFiles.dir().orElse(null);
        if (legacy == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            Path textures = legacy.getParent().resolve("main/resources/assets/" + Techguns.MODID + "/textures/block");
            Path pack = output.getOutputFolder().resolve(FusionTextures.PACK_PATH);
            Path outTextures = pack.resolve("assets/" + Techguns.MODID + "/textures/block");
            List<String> all = new ArrayList<>(CTM);
            all.addAll(PILLAR);
            for (String name : all) {
                BufferedImage ctm = read(legacy.resolve("ctm/" + name + "-ctm.png"));
                boolean pillar = PILLAR.contains(name);
                BufferedImage sheet = pillar ? CtmSheets.vertical(ctm) : CtmSheets.pieced(read(textures.resolve(name + ".png")), ctm);
                write(cache, outTextures.resolve(name + ".png"), png(sheet));
                write(cache, outTextures.resolve(name + ".png.mcmeta"), json(metadata(pillar ? "vertical" : "pieced")));
            }
            JsonObject packMeta = new JsonObject();
            JsonObject info = new JsonObject();
            info.addProperty("description", "Techguns connected textures (Fusion)");
            info.addProperty("pack_format", 34);
            packMeta.add("pack", info);
            JsonObject fusion = new JsonObject();
            fusion.addProperty("min_version", "1.3.0");
            packMeta.add("fusion", fusion);
            write(cache, pack.resolve("pack.mcmeta"), json(packMeta));
        });
    }

    /** Fusion connecting texture, connecting to the same block (Techguns 2 CTM: same block). */
    private static JsonObject metadata(String layout) {
        JsonObject connections = new JsonObject();
        connections.addProperty("type", "is_same_block");
        JsonObject fusion = new JsonObject();
        fusion.addProperty("type", "connecting");
        fusion.addProperty("layout", layout);
        fusion.add("connections", connections);
        JsonObject root = new JsonObject();
        root.add("fusion", fusion);
        return root;
    }

    private static BufferedImage read(Path path) {
        try {
            return ImageIO.read(path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + path, e);
        }
    }

    private static byte[] png(BufferedImage image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] json(JsonObject json) {
        return (new GsonBuilder().setPrettyPrinting().create().toJson(json) + "\n").getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("deprecation")
    private static void write(CachedOutput cache, Path path, byte[] bytes) {
        try {
            Files.createDirectories(path.getParent());
            cache.writeIfNeeded(path, bytes, Hashing.sha1().hashBytes(bytes));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + path, e);
        }
    }

    @Override
    public String getName() {
        return "Techguns connected textures (Fusion)";
    }
}
