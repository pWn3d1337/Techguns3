package techguns.gametest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.Techguns;
import techguns.entity.spawn.TGSpawnerBlockEntity;
import techguns.registry.TGBlocks;
import techguns.world.LegacyPaletteProcessor;

/** Structures converted from Techguns 2 (Phase 7b): templates, the legacy palette processor, chest loot. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGStructureTests {
    private static final String ARENA = "empty_8x10x8";

    /** Template name and the Techguns 2 scan size. */
    private record Expected(String name, int x, int y, int z) {
    }

    private static final List<Expected> TEMPLATES = List.of(
            new Expected("factory_house_small", 9, 7, 11), new Expected("small_trainstation", 11, 7, 11), new Expected("small_mine", 17, 11, 11),
            new Expected("gas_station", 9, 7, 12), new Expected("police_station", 13, 8, 13), new Expected("survivor_hideout", 11, 11, 19),
            new Expected("ore_cluster_spike", 8, 7, 8), new Expected("ore_cluster_meteor_basis", 17, 12, 17),
            new Expected("desert_oil_cluster", 11, 10, 11), new Expected("aircraft_carrier", 54, 24, 21),
            new Expected("nether_altar_small", 11, 9, 11), new Expected("nether_altar_medium", 16, 8, 16),
            new Expected("nether_soul_platform", 13, 10, 13), new Expected("nether_loot_01", 6, 10, 6), new Expected("nether_acid_hole", 9, 6, 9),
            new Expected("nether_ore_cluster_small", 3, 3, 3), new Expected("nether_ore_cluster_castle", 11, 9, 11),
            new Expected("nether_ghast_spawner", 10, 14, 10), new Expected("nether_dungeon_entrance", 10, 6, 7));

    private static StructureTemplate template(GameTestHelper helper, String name) {
        return helper.getLevel().getStructureManager().get(TGIds.id(name))
                .orElseThrow(() -> new IllegalStateException("Missing structure template " + name));
    }

    @GameTest(template = ARENA)
    public static void convertedTemplatesLoad(GameTestHelper helper) {
        for (Expected expected : TEMPLATES) {
            Vec3i size = template(helper, expected.name()).getSize();
            helper.assertTrue(size.equals(new Vec3i(expected.x(), expected.y(), expected.z())), expected.name() + " has the Techguns 2 size, got " + size);
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void gasStationHasSpawnerAndLootChest(GameTestHelper helper) {
        StructureTemplate gasStation = template(helper, "gas_station");
        List<StructureTemplate.StructureBlockInfo> spawners = gasStation.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(),
                TGBlocks.MONSTER_HOLE.get());
        helper.assertTrue(spawners.size() == 1 && spawners.getFirst().nbt() != null
                && spawners.getFirst().nbt().getList("mobTypes", 10).size() == 3 && spawners.getFirst().nbt().getInt("mobsLeft") == 3,
                "Gas station: one monster hole with zombie soldier, farmer and miner, 3 mobs");
        List<StructureTemplate.StructureBlockInfo> chests = gasStation.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.CHEST);
        helper.assertTrue(!chests.isEmpty() && chests.stream().allMatch(c -> c.nbt() != null
                && c.nbt().getString("LootTable").equals("techguns:chests/gasstation")), "Gas station chests use the gas station loot table");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void processorResolvesLegacyPalette(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        StructureTemplate spike = template(helper, "ore_cluster_spike");
        BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0));
        StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor(LegacyPaletteProcessor.INSTANCE);
        spike.placeInWorld(level, origin, origin, settings, RandomSource.create(42), Block.UPDATE_CLIENTS);
        Set<Block> clusters = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(origin, origin.offset(7, 6, 7))) {
            Block block = level.getBlockState(pos).getBlock();
            if (TGBlocks.ORE_CLUSTERS.stream().anyMatch(c -> c.get() == block)) {
                clusters.add(block);
            }
            if (level.getBlockEntity(pos) instanceof TGSpawnerBlockEntity spawner) {
                CompoundTag data = spawner.saveWithoutMetadata(level.registryAccess());
                helper.assertTrue(data.getList("mobTypes", 10).getCompound(0).getString("id").equals("techguns:alien_bug"),
                        "The spike's spawner spawns alien bugs");
                helper.assertFalse(data.contains(LegacyPaletteProcessor.KEY), "No processor data left in block entities");
            }
        }
        helper.assertTrue(clusters.size() == 1, "One ore cluster type per structure, got " + clusters);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void convertedChestLootRolls(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (String name : List.of("gasstation", "policestation", "survivor_hideout", "aircraftcarrier", "castle", "factory_building",
                "small_trainstation", "militarybase_barracks", "militarybase_bunker")) {
            LootTable table = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, TGIds.id("chests/" + name)));
            helper.assertTrue(table != LootTable.EMPTY, "Loot table chests/" + name + " exists");
            int items = 0;
            for (int i = 0; i < 10; i++) {
                LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                        .create(LootContextParamSets.CHEST);
                items += table.getRandomItems(params).stream().mapToInt(ItemStack::getCount).sum();
            }
            helper.assertTrue(items > 0, "chests/" + name + " gives items");
        }
        helper.succeed();
    }
}
