package techguns.stats;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import techguns.gun.GunStats;
import techguns.registry.TGAttributes;

/**
 * Datapack stat overrides for guns and armor (Techguns 2 tuned these with CraftTweaker). The server loads them from
 * {@code techguns/gun_stats} and {@code techguns/armor_stats} ({@link StatOverrideLoader}) and syncs them to clients.
 * One copy per JVM: on an integrated server client and server hold the same values; a client clears its copy when it
 * leaves a server.
 */
public final class StatOverrides {
    private static volatile Map<ResourceLocation, GunStats> gunsById = Map.of();
    private static volatile Map<Item, GunStats> guns = Map.of();
    private static volatile Map<ResourceLocation, ArmorStatOverride> armorById = Map.of();
    private static volatile Map<Item, ArmorStatOverride> armor = Map.of();

    private StatOverrides() {
    }

    public static GunStats gunStats(Item gun, GunStats defaultStats) {
        return guns.getOrDefault(gun, defaultStats);
    }

    @Nullable
    public static ArmorStatOverride armor(Item piece) {
        return armor.get(piece);
    }

    public static Map<ResourceLocation, GunStats> gunsById() {
        return gunsById;
    }

    public static Map<ResourceLocation, ArmorStatOverride> armorById() {
        return armorById;
    }

    public static void setGuns(Map<ResourceLocation, GunStats> byId) {
        gunsById = Map.copyOf(byId);
        guns = byItem(byId);
    }

    public static void setArmor(Map<ResourceLocation, ArmorStatOverride> byId) {
        armorById = Map.copyOf(byId);
        armor = byItem(byId);
    }

    public static void clear() {
        setGuns(Map.of());
        setArmor(Map.of());
    }

    private static <T> Map<Item, T> byItem(Map<ResourceLocation, T> byId) {
        Map<Item, T> map = new IdentityHashMap<>();
        byId.forEach((id, value) -> BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> map.put(item, value)));
        return map;
    }

    /** Applies a partial gun stat override: the JSON is merged into the encoded built-in stats, then decoded. */
    public static DataResult<GunStats> mergeGunStats(GunStats defaultStats, JsonElement override) {
        if (!override.isJsonObject()) {
            return DataResult.error(() -> "Gun stat override must be a JSON object");
        }
        return GunStats.CODEC.encodeStart(JsonOps.INSTANCE, defaultStats).flatMap(base -> {
            JsonObject merged = base.getAsJsonObject().deepCopy();
            deepMerge(merged, override.getAsJsonObject());
            return GunStats.CODEC.parse(JsonOps.INSTANCE, merged);
        });
    }

    private static void deepMerge(JsonObject target, JsonObject source) {
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            JsonElement existing = target.get(entry.getKey());
            if (existing != null && existing.isJsonObject() && entry.getValue().isJsonObject()) {
                deepMerge(existing.getAsJsonObject(), entry.getValue().getAsJsonObject());
            } else {
                target.add(entry.getKey(), entry.getValue().deepCopy());
            }
        }
    }

    /** Bonus attributes with an override applied: listed attributes are replaced, a value of 0 removes one. */
    public static Map<Holder<Attribute>, Double> mergeBonuses(Map<Holder<Attribute>, Double> base, Map<Holder<Attribute>, Double> override) {
        if (override.isEmpty()) {
            return base;
        }
        Map<Holder<Attribute>, Double> merged = new LinkedHashMap<>(base);
        override.forEach((attribute, value) -> {
            Holder<Attribute> key = canonical(attribute, merged);
            if (value == 0.0) {
                merged.remove(key);
            } else {
                merged.put(key, value);
            }
        });
        return merged;
    }

    /**
     * The holder the rest of Techguns uses for this attribute. Decoded holders are registry references, Techguns'
     * own constants are deferred holders, and the two are not equal as map keys.
     */
    private static Holder<Attribute> canonical(Holder<Attribute> attribute, Map<Holder<Attribute>, Double> existing) {
        ResourceKey<Attribute> key = attribute.unwrapKey().orElse(null);
        if (key == null) {
            return attribute;
        }
        for (Holder<Attribute> holder : existing.keySet()) {
            if (holder.is(key)) {
                return holder;
            }
        }
        for (Holder<Attribute> holder : TGAttributes.ATTRIBUTES.getEntries()) {
            if (holder.is(key)) {
                return holder;
            }
        }
        return attribute;
    }
}
