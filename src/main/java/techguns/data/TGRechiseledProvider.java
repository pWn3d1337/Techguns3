package techguns.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import techguns.Techguns;
import techguns.registry.TGBlocks;

/**
 * Chiseling groups for Rechiseled (Techguns 2 registered the same groups with Chisel: camo nets, camo net tops, metal
 * panels, neon lights, concrete, metal ladders): {@code data/techguns/chiseling_recipes/<group>.json}. Plain JSON in
 * Rechiseled's data folder, read only when Rechiseled is installed; no dependency on its classes.
 */
public class TGRechiseledProvider implements DataProvider {
    private final PackOutput output;

    public TGRechiseledProvider(PackOutput output) {
        this.output = output;
    }

    /** Group name -> blocks; the stairs of a block go with it. */
    static Map<String, List<? extends DeferredBlock<? extends Block>>> groups() {
        return Map.of(
                "camo_net", TGBlocks.CAMO_NETS,
                "camo_net_top", TGBlocks.CAMO_NETS_TOP,
                "metal_panel", TGBlocks.METAL_PANEL,
                "neon_lights", TGBlocks.NEON_LIGHTS,
                "concrete", TGBlocks.CONCRETE,
                "metal_ladder", TGBlocks.LADDERS);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        groups().forEach((name, blocks) -> {
            JsonObject json = new JsonObject();
            json.addProperty("type", "rechiseled:chiseling");
            JsonArray entries = new JsonArray();
            for (DeferredBlock<? extends Block> block : blocks) {
                JsonObject entry = new JsonObject();
                entry.addProperty("block", block.getId().toString());
                TGBlocks.stairsOf(block).ifPresent(stairs -> entry.addProperty("stairs", stairs.getId().toString()));
                entries.add(entry);
            }
            json.add("entries", entries);
            Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(Techguns.MODID).resolve("chiseling_recipes/" + name + ".json");
            writes.add(DataProvider.saveStable(cache, json, path));
        });
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Techguns Rechiseled groups";
    }
}
