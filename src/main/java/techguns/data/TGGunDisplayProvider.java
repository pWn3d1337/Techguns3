package techguns.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import techguns.client.render.item.GunDisplay;
import techguns.client.render.item.GunDisplays;
import techguns.client.render.item.TGItemRendererSetup;

/**
 * Display files of the guns whose motion moved to their model files
 * ({@code assets/techguns/techguns/gun_display/<gun>.json}), written from their renderers' settings so they move as before.
 */
public class TGGunDisplayProvider implements DataProvider {
    private final PackOutput output;

    public TGGunDisplayProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        TGItemRendererSetup.exportDisplays().forEach((item, display) -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            Path path = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(id.getNamespace())
                    .resolve(GunDisplays.DIRECTORY).resolve(id.getPath() + ".json");
            writes.add(DataProvider.saveStable(cache, GunDisplay.CODEC.encodeStart(JsonOps.INSTANCE, display).getOrThrow(), path));
        });
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Techguns gun display files";
    }
}
