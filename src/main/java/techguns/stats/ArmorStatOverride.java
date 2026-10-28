package techguns.stats;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import techguns.core.damage.TGDamageCategory;

/**
 * Datapack override of one Techguns armor piece ({@code data/<namespace>/techguns/armor_stats/<item>.json}, replaces
 * the Techguns 2 CraftTweaker {@code ArmorStats}). Every field is optional; what is left out keeps the built-in value.
 *
 * <pre>{@code
 * {
 *   "armor": { "physical": 6.0, "fire": 4.5 },      // this piece's armor per damage category
 *   "toughness": 2.0,
 *   "bonuses": { "techguns:speed_bonus": 0.1 },      // bonus attributes; 0 removes one
 *   "unpowered_bonuses": { "techguns:speed_bonus": 0.0 } // powered armor without power
 * }
 * }</pre>
 */
public record ArmorStatOverride(Map<TGDamageCategory, Float> armor, Optional<Float> toughness,
                                Map<Holder<Attribute>, Double> bonuses, Map<Holder<Attribute>, Double> unpoweredBonuses) {
    private static final Codec<TGDamageCategory> CATEGORY = Codec.STRING.comapFlatMap(id -> Arrays.stream(TGDamageCategory.values())
                    .filter(category -> category.isResistable() && category.id().equals(id)).findFirst()
                    .map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown or unresistable damage category: " + id)),
            TGDamageCategory::id);
    private static final Codec<Map<Holder<Attribute>, Double>> BONUSES = Codec.unboundedMap(BuiltInRegistries.ATTRIBUTE.holderByNameCodec(), Codec.DOUBLE);

    public static final Codec<ArmorStatOverride> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(CATEGORY, Codec.FLOAT).optionalFieldOf("armor", Map.of()).forGetter(ArmorStatOverride::armor),
            Codec.FLOAT.optionalFieldOf("toughness").forGetter(ArmorStatOverride::toughness),
            BONUSES.optionalFieldOf("bonuses", Map.of()).forGetter(ArmorStatOverride::bonuses),
            BONUSES.optionalFieldOf("unpowered_bonuses", Map.of()).forGetter(ArmorStatOverride::unpoweredBonuses)
    ).apply(i, ArmorStatOverride::new));
}
