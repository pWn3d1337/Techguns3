package techguns.gametest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import techguns.registry.TGStructures;
import techguns.world.structure.AlienBugNest;
import techguns.world.structure.AvoidingSpreadPlacement;
import techguns.world.structure.CampLevel;
import techguns.world.structure.MilitaryBasePiece;
import techguns.world.structure.MilitaryBaseStructure;
import techguns.world.structure.LegacyTemplatePiece;
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

/** Structures from Techguns 2 (Phase 7b/7c): templates, the legacy palette processor, chest loot, structures and sets. */
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

    @GameTest(template = ARENA)
    public static void overworldStructuresAndSetsAreRegistered(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var structures = registries.registryOrThrow(Registries.STRUCTURE);
        for (TGStructures.Template t : TGStructures.OVERWORLD) {
            helper.assertTrue(structures.containsKey(t.key()), t.name() + " is a structure");
        }
        helper.assertTrue(structures.containsKey(TGStructures.ALIEN_BUG_NEST_KEY), "The alien bug nest is a structure");
        helper.assertTrue(structures.containsKey(TGStructures.MILITARY_BASE_KEY), "The military base is a structure");
        var sets = registries.registryOrThrow(Registries.STRUCTURE_SET);
        for (TGStructures.Size size : TGStructures.Size.values()) {
            StructureSet set = sets.getOrThrow(size.set);
            helper.assertTrue(set.placement() instanceof AvoidingSpreadPlacement spread && spread.spacing() == size.spacing,
                    size + " structures every " + size.spacing + " chunks");
        }
        helper.assertTrue(sets.getOrThrow(TGStructures.Size.SMALL.set).structures().size() == 4
                && sets.getOrThrow(TGStructures.Size.MEDIUM.set).structures().size() == 5
                && sets.getOrThrow(TGStructures.Size.BIG.set).structures().size() == 2
                && sets.getOrThrow(TGStructures.Size.NETHER_SMALL.set).structures().size() == 5
                && sets.getOrThrow(TGStructures.Size.NETHER_MEDIUM.set).structures().size() == 3, "Techguns 2 small and medium tables");
        for (TGStructures.Template t : TGStructures.NETHER) {
            helper.assertTrue(structures.containsKey(t.key()), t.name() + " is a structure");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void templatePieceBuildsFoundation(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(0, 4, 0));
        LegacyTemplatePiece piece = new LegacyTemplatePiece(level.getStructureManager(), TGIds.id("ore_cluster_spike"), origin, Rotation.NONE,
                new BlockPos(4, 0, 4), LegacyPaletteProcessor.BiomeColor.WOODLAND, 3, 7, false);
        BoundingBox box = new BoundingBox(origin.getX() - 1, origin.getY() - 4, origin.getZ() - 1, origin.getX() + 9, origin.getY() + 9, origin.getZ() + 9);
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(1), box,
                new ChunkPos(origin), origin);
        int filled = 0;
        int baseBlocks = 0;
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                BlockPos base = origin.offset(x, 0, z);
                if (!level.getBlockState(base).isAir()) {
                    baseBlocks++;
                    if (!level.getBlockState(base.below(3)).isAir()) {
                        filled++;
                    }
                }
            }
        }
        helper.assertTrue(baseBlocks > 0 && filled == baseBlocks, "The foundation reaches 3 blocks under every base block: " + filled + "/" + baseBlocks);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void alienBugNestGenerates(GameTestHelper helper) {
        for (long seed = 0; seed < 5; seed++) {
            AlienBugNest nest = new AlienBugNest(seed, 0, 64, 0, 16 + (int) seed, 20);
            int spawners = nest.spawners().size();
            helper.assertTrue(spawners >= 5 && spawners <= 10, "One spawner per side chamber, got " + spawners);
            helper.assertTrue(nest.blocks().containsValue(TGBlocks.BUGNEST_SAND.get().defaultBlockState())
                    && nest.blocks().values().stream().anyMatch(s -> s.is(TGBlocks.SLIMY_LADDER.get()))
                    && nest.blocks().containsValue(TGBlocks.BUGNEST_EGGS.get().defaultBlockState()), "Walls, slime ladders and eggs");
            BoundingBox bounds = nest.bounds();
            helper.assertTrue(bounds.getXSpan() < 128 && bounds.getZSpan() < 128 && bounds.maxY() <= 64 + 8, "Nest size " + bounds);
        }
        helper.succeed();
    }

    /** A camp on generated ground around (0, 0): flat at y 64, or hills of 6 blocks. */
    private static CampLevel camp(long seed, int sizeX, int sizeZ, boolean hilly) {
        int countX = (sizeX + 8) / 4 + 2;
        int countZ = (sizeZ + 8) / 4 + 2;
        int[] heights = new int[countX * countZ];
        for (int i = 0; i < countX; i++) {
            for (int j = 0; j < countZ; j++) {
                heights[i * countZ + j] = hilly ? 64 + (int) Math.round(3 + 3 * Math.sin(i * 0.7) * Math.cos(j * 0.5)) : 64;
            }
        }
        return MilitaryBasePiece.build(seed, 0, 0, sizeX, sizeZ, LegacyPaletteProcessor.BiomeColor.WOODLAND,
                new MilitaryBaseStructure.HeightGrid(-4, -4, countX, countZ, heights));
    }

    @GameTest(template = ARENA)
    public static void militaryCampGenerates(GameTestHelper helper) {
        int chests = 0;
        for (long seed = 0; seed < 6; seed++) {
            int sizeX = 32 + (int) seed * 9;
            int sizeZ = 79 - (int) seed * 8;
            boolean hilly = seed % 2 == 1;
            CampLevel camp = camp(seed, sizeX, sizeZ, hilly);
            String name = "Camp " + seed + " (" + sizeX + "x" + sizeZ + (hilly ? ", hills" : "") + ")";

            Map<BlockPos, CampLevel.Spawner> spawners = camp.spawners();
            helper.assertTrue(spawners.values().stream().filter(t -> t == CampLevel.Spawner.HELICOPTER).count() == 1,
                    name + ": one helicopter spawner on the flag segment");
            helper.assertTrue(spawners.containsValue(CampLevel.Spawner.SOLDIERS), name + ": soldier spawners on the roads");
            for (BlockPos pos : spawners.keySet()) {
                helper.assertTrue(camp.get(pos.getX(), pos.getY(), pos.getZ()).is(TGBlocks.MILITARY_SPAWN_POINT.get()), name + ": spawner block at " + pos);
            }
            helper.assertTrue(camp.get(0, camp.groundY(0, 0) + 1, 0).is(Blocks.IRON_BARS)
                    && camp.get(sizeX - 1, camp.groundY(sizeX - 1, sizeZ - 1) + 2, sizeZ - 1).is(Blocks.IRON_BARS), name + ": fence corners");
            helper.assertTrue(camp.blocks().containsValue(Blocks.GRAVEL.defaultBlockState()), name + ": gravel roads");

            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    min = Math.min(min, camp.groundY(x, z));
                    max = Math.max(max, camp.groundY(x, z));
                }
            }
            helper.assertTrue(max - min <= 4, name + ": ground flattened to 4 blocks, got " + min + ".." + max);

            CampLevel again = camp(seed, sizeX, sizeZ, hilly);
            helper.assertTrue(again.blocks().equals(camp.blocks()) && again.spawners().equals(camp.spawners()), name + ": rebuilt the same from its seed");

            for (ResourceKey<LootTable> table : camp.chests().values()) {
                helper.assertTrue(helper.getLevel().getServer().reloadableRegistries().getLootTable(table) != LootTable.EMPTY, table + " exists");
                chests++;
            }
        }
        helper.assertTrue(chests > 0, "Bunkers and barracks have loot chests");
        helper.succeed();
    }
}
