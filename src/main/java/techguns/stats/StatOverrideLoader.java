package techguns.stats;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import techguns.Techguns;
import techguns.armor.TGArmorItem;
import techguns.gun.GunItem;

/**
 * Loads the stat override files of a datapack (server side, also on {@code /reload}). The file id is the item id:
 * {@code data/techguns/techguns/gun_stats/m4.json} overrides {@code techguns:m4}. Broken files are logged and skipped.
 */
public final class StatOverrideLoader {
    public static final String GUN_DIRECTORY = "techguns/gun_stats";
    public static final String ARMOR_DIRECTORY = "techguns/armor_stats";
    private static final Gson GSON = new Gson();

    private StatOverrideLoader() {
    }

    /** Gun stats: each file holds only the fields to change, in the {@link techguns.gun.GunStats} layout. */
    public static final class Guns extends SimpleJsonResourceReloadListener {
        public Guns() {
            super(GSON, GUN_DIRECTORY);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
            StatOverrides.setGuns(loadGuns(files));
        }
    }

    /** Armor pieces: see {@link ArmorStatOverride}. */
    public static final class Armor extends SimpleJsonResourceReloadListener {
        public Armor() {
            super(GSON, ARMOR_DIRECTORY);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
            StatOverrides.setArmor(loadArmor(files));
        }
    }

    public static Map<ResourceLocation, techguns.gun.GunStats> loadGuns(Map<ResourceLocation, JsonElement> files) {
        Map<ResourceLocation, techguns.gun.GunStats> result = new HashMap<>();
        files.forEach((id, json) -> {
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (!(item instanceof GunItem gun)) {
                Techguns.LOGGER.warn("Gun stat override {}: {} is not a Techguns gun", GUN_DIRECTORY, id);
                return;
            }
            StatOverrides.mergeGunStats(gun.defaultStats(), json)
                    .ifSuccess(stats -> result.put(id, stats))
                    .ifError(error -> Techguns.LOGGER.warn("Gun stat override {} skipped: {}", id, error.message()));
        });
        if (!result.isEmpty()) {
            Techguns.LOGGER.info("Loaded {} gun stat overrides", result.size());
        }
        return result;
    }

    public static Map<ResourceLocation, ArmorStatOverride> loadArmor(Map<ResourceLocation, JsonElement> files) {
        Map<ResourceLocation, ArmorStatOverride> result = new HashMap<>();
        files.forEach((id, json) -> {
            if (!(BuiltInRegistries.ITEM.getOptional(id).orElse(null) instanceof TGArmorItem)) {
                Techguns.LOGGER.warn("Armor stat override {}: {} is not a Techguns armor piece", ARMOR_DIRECTORY, id);
                return;
            }
            ArmorStatOverride.CODEC.parse(JsonOps.INSTANCE, json)
                    .ifSuccess(override -> result.put(id, override))
                    .ifError(error -> Techguns.LOGGER.warn("Armor stat override {} skipped: {}", id, error.message()));
        });
        if (!result.isEmpty()) {
            Techguns.LOGGER.info("Loaded {} armor stat overrides", result.size());
        }
        return result;
    }
}
