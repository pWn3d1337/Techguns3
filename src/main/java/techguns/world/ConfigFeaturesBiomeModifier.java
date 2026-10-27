package techguns.world;

import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import techguns.TGConfig;
import techguns.registry.TGBiomeModifiers;

/**
 * Adds placed features to {@code biomes}, each only while its config switch is on (Techguns 2 {@code doOreGen*}).
 * Applied when the server starts, so config changes take effect on the next start.
 */
public record ConfigFeaturesBiomeModifier(HolderSet<Biome> biomes, List<Entry> features, GenerationStep.Decoration step) implements BiomeModifier {
    /** The config switches a generated feature can depend on. */
    public enum Toggle implements StringRepresentable {
        TIN(TGConfig.ORE_GEN_TIN::get),
        LEAD(TGConfig.ORE_GEN_LEAD::get),
        TITANIUM(TGConfig.ORE_GEN_TITANIUM::get),
        URANIUM(TGConfig.ORE_GEN_URANIUM::get);

        public static final Codec<Toggle> CODEC = StringRepresentable.fromEnum(Toggle::values);

        private final BooleanSupplier enabled;

        Toggle(BooleanSupplier enabled) {
            this.enabled = enabled;
        }

        public boolean enabled() {
            return enabled.getAsBoolean();
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record Entry(Holder<PlacedFeature> feature, Toggle toggle) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PlacedFeature.CODEC.fieldOf("feature").forGetter(Entry::feature),
                Toggle.CODEC.fieldOf("config").forGetter(Entry::toggle)).apply(instance, Entry::new));
    }

    public static final MapCodec<ConfigFeaturesBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ConfigFeaturesBiomeModifier::biomes),
            Entry.CODEC.listOf().fieldOf("features").forGetter(ConfigFeaturesBiomeModifier::features),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ConfigFeaturesBiomeModifier::step)).apply(instance, ConfigFeaturesBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biomes.contains(biome)) {
            return;
        }
        for (Entry entry : features) {
            if (entry.toggle.enabled()) {
                builder.getGenerationSettings().addFeature(step, entry.feature);
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return TGBiomeModifiers.CONFIG_FEATURES.get();
    }
}
