package techguns.registry;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.Tags;
import techguns.TGIds;
import techguns.world.ConfigFeaturesBiomeModifier;

/**
 * Generated world features: the Techguns 2 {@code OreGenerator} ores, re-balanced for the 1.18+ world. Each ore keeps
 * its tool tier's depth like the vanilla ores do: tin (stone tier) peaks near the surface like copper, lead (iron tier)
 * at mid depth like iron, titanium (diamond tier) deep like diamonds, uranium deepest and rarest. Like vanilla gold
 * and iron, two ores get extra deposits in some biomes.
 */
public final class TGFeatures {
    /** Where a placement generates; each region is one biome modifier. */
    public enum Region {
        OVERWORLD("ores", Tags.Biomes.IS_OVERWORLD),
        MOUNTAINS("ores_mountains", Tags.Biomes.IS_MOUNTAIN),
        URANIUM_DEPOSITS("ores_uranium_deposits", TGTags.Biomes.URANIUM_DEPOSITS);

        public final String modifierName;
        public final TagKey<Biome> biomes;

        Region(String modifierName, TagKey<Biome> biomes) {
            this.modifierName = modifierName;
            this.biomes = biomes;
        }
    }

    /**
     * One vein kind of an ore: vein size, chance to skip blocks next to air (vanilla "buried" ores), tries per chunk,
     * height distribution and region.
     */
    public record Placement(String suffix, int veinSize, float airDiscard, int tries, HeightProvider height, Region region) {
    }

    public record Ore(String name, Supplier<? extends Block> stone, Supplier<? extends Block> deepslate, ConfigFeaturesBiomeModifier.Toggle toggle,
                      List<Placement> placements) {
        public ResourceKey<ConfiguredFeature<?, ?>> configured(Placement placement) {
            return ResourceKey.create(Registries.CONFIGURED_FEATURE, TGIds.id(id(placement)));
        }

        public ResourceKey<PlacedFeature> placed(Placement placement) {
            return ResourceKey.create(Registries.PLACED_FEATURE, TGIds.id(id(placement)));
        }

        private String id(Placement placement) {
            return "ore_" + name + placement.suffix;
        }
    }

    public static final List<Ore> ORES = List.of(
            new Ore("tin", TGBlocks.TIN_ORE, TGBlocks.DEEPSLATE_TIN_ORE, ConfigFeaturesBiomeModifier.Toggle.TIN, List.of(
                    new Placement("", 8, 0.0f, 10, triangle(-16, 96), Region.OVERWORLD),
                    new Placement("_mountains", 6, 0.0f, 8, uniform(64, 160), Region.MOUNTAINS))),
            new Ore("lead", TGBlocks.LEAD_ORE, TGBlocks.DEEPSLATE_LEAD_ORE, ConfigFeaturesBiomeModifier.Toggle.LEAD, List.of(
                    new Placement("", 7, 0.0f, 8, triangle(-48, 64), Region.OVERWORLD))),
            new Ore("titanium", TGBlocks.TITANIUM_ORE, TGBlocks.DEEPSLATE_TITANIUM_ORE, ConfigFeaturesBiomeModifier.Toggle.TITANIUM, List.of(
                    new Placement("", 6, 0.3f, 5, triangle(-64, 16), Region.OVERWORLD))),
            new Ore("uranium", TGBlocks.URANIUM_ORE, TGBlocks.DEEPSLATE_URANIUM_ORE, ConfigFeaturesBiomeModifier.Toggle.URANIUM, List.of(
                    new Placement("", 5, 0.5f, 3, uniform(-64, -8), Region.OVERWORLD),
                    new Placement("_deposits", 5, 0.0f, 4, uniform(32, 80), Region.URANIUM_DEPOSITS))));

    private TGFeatures() {
    }

    private static HeightProvider triangle(int min, int max) {
        return TrapezoidHeight.of(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max));
    }

    private static HeightProvider uniform(int min, int max) {
        return UniformHeight.of(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max));
    }

    public static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        TagMatchTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        TagMatchTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        for (Ore ore : ORES) {
            for (Placement placement : ore.placements()) {
                context.register(ore.configured(placement), new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                        OreConfiguration.target(stone, ore.stone().get().defaultBlockState()),
                        OreConfiguration.target(deepslate, ore.deepslate().get().defaultBlockState())), placement.veinSize(), placement.airDiscard())));
            }
        }
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        for (Ore ore : ORES) {
            for (Placement placement : ore.placements()) {
                context.register(ore.placed(placement), new PlacedFeature(configured.getOrThrow(ore.configured(placement)), List.of(
                        CountPlacement.of(placement.tries()), InSquarePlacement.spread(), HeightRangePlacement.of(placement.height()), BiomeFilter.biome())));
            }
        }
    }
}
