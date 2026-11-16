package techguns.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import techguns.TGIds;

/**
 * How well solar panels work in a biome: a multiplier on their output and a type name for the screen
 * ({@code techguns.solar.biome_type.<type>}). A biome data map, so datapacks can change single biomes or tags; biomes
 * without an entry use {@link #DEFAULT}. A biome in several tags takes the entry with the highest {@code priority}; an
 * entry for the biome itself beats every tag entry.
 */
public record SolarEfficiency(String type, float multiplier, int priority) {
    public static final SolarEfficiency DEFAULT = new SolarEfficiency("default", 1.0f, 0);

    public static final Codec<SolarEfficiency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(SolarEfficiency::type),
            Codec.floatRange(0.0f, 100.0f).fieldOf("multiplier").forGetter(SolarEfficiency::multiplier),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(SolarEfficiency::priority)
    ).apply(instance, SolarEfficiency::new));

    /** {@code techguns:solar_efficiency}, synced: the screen reads it on the client. */
    public static final AdvancedDataMapType<Biome, SolarEfficiency, DataMapValueRemover.Default<SolarEfficiency, Biome>> DATA_MAP =
            AdvancedDataMapType.builder(TGIds.id("solar_efficiency"), Registries.BIOME, CODEC)
                    .synced(CODEC, false)
                    .merger(SolarEfficiency::merge)
                    .build();

    /** Two entries for one biome: the biome's own entry wins, else the higher priority (the later one on a tie). */
    private static SolarEfficiency merge(net.minecraft.core.Registry<Biome> registry, Either<TagKey<Biome>, ResourceKey<Biome>> first,
                                         SolarEfficiency firstValue, Either<TagKey<Biome>, ResourceKey<Biome>> second, SolarEfficiency secondValue) {
        boolean firstDirect = first.right().isPresent();
        boolean secondDirect = second.right().isPresent();
        if (firstDirect != secondDirect) {
            return firstDirect ? firstValue : secondValue;
        }
        return firstValue.priority() > secondValue.priority() ? firstValue : secondValue;
    }

    public static SolarEfficiency of(Holder<Biome> biome) {
        SolarEfficiency data = biome.getData(DATA_MAP);
        return data != null ? data : DEFAULT;
    }

    /** Lang key of the type name. */
    public String translationKey() {
        return "techguns.solar.biome_type." + type;
    }
}
