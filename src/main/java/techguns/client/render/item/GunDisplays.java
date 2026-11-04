package techguns.client.render.item;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import techguns.Techguns;

/**
 * Loads the {@link GunDisplay} files of the resource packs. The file id is the item id:
 * {@code assets/techguns/techguns/gun_display/pistol.json} is {@code techguns:pistol}. Broken files are logged and skipped.
 */
public final class GunDisplays extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "techguns/gun_display";
    private static final Gson GSON = new Gson();
    private static Map<ResourceLocation, GunDisplay> displays = Map.of();

    private GunDisplays() {
        super(GSON, DIRECTORY);
    }

    public static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new GunDisplays());
    }

    /** The display file of {@code item}; null if it has none (its motion is still in code). */
    @Nullable
    public static GunDisplay get(Item item) {
        return displays.get(BuiltInRegistries.ITEM.getKey(item));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, GunDisplay> result = new HashMap<>();
        files.forEach((id, json) -> GunDisplay.CODEC.parse(JsonOps.INSTANCE, json)
                .ifSuccess(display -> result.put(id, display))
                .ifError(error -> Techguns.LOGGER.warn("Gun display {} skipped: {}", id, error.message())));
        displays = result;
    }
}
