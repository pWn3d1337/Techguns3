package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.registry.TGBlocks;
import techguns.registry.TGFeatures;

/** World generation (Phase 7): ore features and the biome modifiers that add them. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGWorldgenTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA)
    public static void oresGenerateInTheirRegions(GameTestHelper helper) {
        for (TGFeatures.Ore ore : TGFeatures.ORES) {
            for (TGFeatures.Placement placement : ore.placements()) {
                String name = ore.placed(placement).location().toString();
                switch (placement.region()) {
                    case OVERWORLD -> {
                        helper.assertTrue(has(helper, Biomes.PLAINS, ore, placement), name + " generates in the plains");
                        helper.assertTrue(has(helper, Biomes.DESERT, ore, placement), name + " generates in deserts");
                    }
                    case MOUNTAINS -> {
                        helper.assertTrue(has(helper, Biomes.JAGGED_PEAKS, ore, placement), name + " generates in mountains");
                        helper.assertFalse(has(helper, Biomes.PLAINS, ore, placement), name + " stays out of the plains");
                    }
                    case URANIUM_DEPOSITS -> {
                        helper.assertTrue(has(helper, Biomes.BADLANDS, ore, placement) && has(helper, Biomes.DESERT, ore, placement),
                                name + " generates in badlands and deserts");
                        helper.assertFalse(has(helper, Biomes.PLAINS, ore, placement), name + " stays out of the plains");
                    }
                }
                helper.assertFalse(has(helper, Biomes.NETHER_WASTES, ore, placement), name + " stays out of the Nether");
            }
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void oreVeinsReplaceStoneAndDeepslate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var configured = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        TGFeatures.Ore tin = TGFeatures.ORES.getFirst();
        ConfiguredFeature<?, ?> vein = configured.getOrThrow(tin.configured(tin.placements().getFirst()));
        helper.assertTrue(veinBlocks(helper, vein, Blocks.STONE, TGBlocks.TIN_ORE.get()) > 0, "Tin veins replace stone");
        helper.assertTrue(veinBlocks(helper, vein, Blocks.DEEPSLATE, TGBlocks.DEEPSLATE_TIN_ORE.get()) > 0, "Tin veins replace deepslate with deepslate tin");
        helper.succeed();
    }

    private static boolean has(GameTestHelper helper, ResourceKey<Biome> biome, TGFeatures.Ore ore, TGFeatures.Placement placement) {
        var registries = helper.getLevel().registryAccess();
        Holder<PlacedFeature> feature = registries.registryOrThrow(Registries.PLACED_FEATURE).getHolderOrThrow(ore.placed(placement));
        return registries.registryOrThrow(Registries.BIOME).getOrThrow(biome).getGenerationSettings().features()
                .get(GenerationStep.Decoration.UNDERGROUND_ORES.ordinal()).contains(feature);
    }

    /** Fills the arena with {@code host}, places veins in the middle and counts the {@code ore} blocks. */
    private static int veinBlocks(GameTestHelper helper, ConfiguredFeature<?, ?> feature, Block host, Block ore) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(7, 3, 7))) {
            helper.setBlock(pos, host);
        }
        ServerLevel level = helper.getLevel();
        // a vein may fall outside the thin arena: place several
        for (int i = 0; i < 8; i++) {
            feature.place(level, level.getChunkSource().getGenerator(), level.random, helper.absolutePos(new BlockPos(4, 2, 4)));
        }
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(7, 3, 7))) {
            if (helper.getBlockState(pos).is(ore)) {
                count++;
            }
        }
        return count;
    }
}
