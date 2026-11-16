package techguns.machine;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import techguns.TGIds;

/**
 * How well a biome-dependent generator works in a biome: a multiplier on its output and a type name for the screen
 * ({@code techguns.biome_type.<type>}). Biome data maps, so datapacks can change single biomes or tags; biomes without
 * an entry use {@link #DEFAULT}. A biome in several tags takes the entry with the highest {@code priority}; an entry for
 * the biome itself beats every tag entry. {@link #SOLAR} is for solar panels, {@link #WIND} for wind turbines.
 */
public record BiomeEfficiency(String type, float multiplier, int priority) {
    public static final BiomeEfficiency DEFAULT = new BiomeEfficiency("default", 1.0f, 0);

    public static final Codec<BiomeEfficiency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(BiomeEfficiency::type),
            Codec.floatRange(0.0f, 100.0f).fieldOf("multiplier").forGetter(BiomeEfficiency::multiplier),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(BiomeEfficiency::priority)
    ).apply(instance, BiomeEfficiency::new));

    /** {@code techguns:solar_efficiency}, synced: the screen reads it on the client. */
    public static final AdvancedDataMapType<Biome, BiomeEfficiency, DataMapValueRemover.Default<BiomeEfficiency, Biome>> SOLAR = create("solar_efficiency");
    /** {@code techguns:wind_efficiency}, synced: the screen reads it on the client. */
    public static final AdvancedDataMapType<Biome, BiomeEfficiency, DataMapValueRemover.Default<BiomeEfficiency, Biome>> WIND = create("wind_efficiency");

    private static AdvancedDataMapType<Biome, BiomeEfficiency, DataMapValueRemover.Default<BiomeEfficiency, Biome>> create(String name) {
        return AdvancedDataMapType.builder(TGIds.id(name), Registries.BIOME, CODEC)
                .synced(CODEC, false)
                .merger(BiomeEfficiency::merge)
                .build();
    }

    /** Two entries for one biome: the biome's own entry wins, else the higher priority (the later one on a tie). */
    private static BiomeEfficiency merge(Registry<Biome> registry, Either<TagKey<Biome>, ResourceKey<Biome>> first,
                                         BiomeEfficiency firstValue, Either<TagKey<Biome>, ResourceKey<Biome>> second, BiomeEfficiency secondValue) {
        boolean firstDirect = first.right().isPresent();
        boolean secondDirect = second.right().isPresent();
        if (firstDirect != secondDirect) {
            return firstDirect ? firstValue : secondValue;
        }
        return firstValue.priority() > secondValue.priority() ? firstValue : secondValue;
    }

    public static BiomeEfficiency solar(Holder<Biome> biome) {
        return of(biome, SOLAR);
    }

    public static BiomeEfficiency wind(Holder<Biome> biome) {
        return of(biome, WIND);
    }

    private static BiomeEfficiency of(Holder<Biome> biome,
                                      AdvancedDataMapType<Biome, BiomeEfficiency, DataMapValueRemover.Default<BiomeEfficiency, Biome>> map) {
        BiomeEfficiency data = biome.getData(map);
        return data != null ? data : DEFAULT;
    }

    /** Lang key of the type name. */
    public String translationKey() {
        return "techguns.biome_type." + type;
    }
}
