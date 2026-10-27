package techguns.gametest;

import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.core.spawn.NpcSpawnTable;
import techguns.core.spawn.SpawnDanger;
import techguns.entity.npc.TGNpc;
import techguns.entity.spawn.NpcSpawnMarker;
import techguns.entity.spawn.SpawnerLink;
import techguns.entity.spawn.TGSpawnManager;
import techguns.entity.spawn.TGSpawnerBlockEntity;
import techguns.registry.TGBlocks;
import techguns.registry.TGEntities;
import techguns.registry.TGNpcs;

/** Natural NPC spawning (danger levels, spawn tables, biome modifiers) and the TG spawner blocks (Phase 6f). */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGSpawnTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA)
    public static void dangerLevelsAndSpawnTables(GameTestHelper helper) {
        int[] levels = {500, 1000, 2500};
        helper.assertTrue(SpawnDanger.fromDistance(100, false, levels) == 0 && SpawnDanger.fromDistance(700, false, levels) == 1
                && SpawnDanger.fromDistance(700, true, levels) == 2 && SpawnDanger.fromDistance(3000, false, levels) == 3,
                "Danger rises with the distance to the world spawn, twice as fast in the Nether");
        NpcSpawnTable<String> table = new NpcSpawnTable<String>().add("farmer", 200, 0).add("soldier", 100, 1).add("off", 0, 0);
        helper.assertTrue(table.totalWeight(0) == 200 && table.totalWeight(1) == 300, "Entries count from their danger level on, weight 0 is left out");
        helper.assertTrue(table.pick(0, bound -> bound - 1).orElseThrow().equals("farmer") && table.pick(1, bound -> bound - 1).orElseThrow().equals("soldier"),
                "Weighted picks only reach entries of the allowed danger");
        helper.assertTrue(TGSpawnManager.overworldTable().totalWeight(0) == 400 && TGSpawnManager.overworldTable().totalWeight(2) == 653
                && TGSpawnManager.netherTable().totalWeight(0) == 130, "Techguns 2 default weights");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void biomeModifiersAddTheSpawnMarker(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        for (var key : List.of(Biomes.PLAINS, Biomes.DESERT, Biomes.NETHER_WASTES)) {
            Biome biome = biomes.getOrThrow(key);
            List<MobSpawnSettings.SpawnerData> monsters = biome.getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
            helper.assertTrue(monsters.stream().anyMatch(data -> data.type == TGEntities.NPC_SPAWN_MARKER.get()),
                    key.location() + " spawns Techguns NPCs");
        }
        helper.assertFalse(biomes.getOrThrow(Biomes.THE_END).getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                .anyMatch(data -> data.type == TGEntities.NPC_SPAWN_MARKER.get()), "No Techguns NPCs in the End");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void spawnMarkerBecomesAnNpc(GameTestHelper helper) {
        NpcSpawnMarker marker = TGEntities.NPC_SPAWN_MARKER.get().create(helper.getLevel());
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        marker.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        helper.assertFalse(helper.getLevel().addFreshEntity(marker), "The marker never joins the world");
        Set<EntityType<?>> overworld = Set.of(TGNpcs.ZOMBIE_SOLDIER.type().get(), TGNpcs.ZOMBIE_FARMER.type().get(), TGNpcs.ZOMBIE_MINER.type().get(),
                TGNpcs.SKELETON_SOLDIER.type().get(), TGNpcs.PSYCHO_STEVE.type().get(), TGNpcs.BANDIT.type().get());
        List<TGNpc> npcs = helper.getLevel().getEntitiesOfClass(TGNpc.class, new AABB(pos).inflate(1.0));
        helper.assertTrue(npcs.size() == 1 && overworld.contains(npcs.getFirst().getType()), "An overworld NPC took its place: " + npcs);
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void spawnerSpawnsUntilItsMobsAreKilled(GameTestHelper helper) {
        BlockPos spawnerPos = new BlockPos(3, 1, 3);
        helper.setBlock(spawnerPos, TGBlocks.MONSTER_HOLE.get());
        TGSpawnerBlockEntity spawner = (TGSpawnerBlockEntity) helper.getBlockEntity(spawnerPos);
        spawner.addMobType(TGNpcs.ZOMBIE_FARMER.type().get(), 1);
        spawner.setParams(2, 1, 5, 1);
        BlockPos absolute = helper.absolutePos(spawnerPos);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(spawner.activeMobs() == 1, "The spawner spawned its first NPC"))
                .thenExecute(() -> linkedNpc(helper, absolute).kill())
                .thenWaitUntil(() -> helper.assertTrue(spawner.mobsLeft() == 1, "A kill uses up one of the spawner's mobs"))
                .thenWaitUntil(() -> helper.assertTrue(spawner.activeMobs() == 1, "The spawner replaced the killed NPC"))
                .thenExecute(() -> linkedNpc(helper, absolute).kill())
                .thenWaitUntil(() -> helper.assertBlockNotPresent(TGBlocks.MONSTER_HOLE.get(), spawnerPos))
                .thenSucceed();
    }

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void despawnedNpcsDontCount(GameTestHelper helper) {
        BlockPos spawnerPos = new BlockPos(3, 1, 3);
        helper.setBlock(spawnerPos, TGBlocks.MILITARY_SPAWN_POINT.get());
        TGSpawnerBlockEntity spawner = (TGSpawnerBlockEntity) helper.getBlockEntity(spawnerPos);
        spawner.addMobType(TGNpcs.ARMY_SOLDIER.type().get(), 1);
        spawner.setParams(3, 1, 5, 1);
        BlockPos absolute = helper.absolutePos(spawnerPos);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(spawner.activeMobs() == 1, "The spawner spawned an NPC"))
                .thenExecute(() -> linkedNpc(helper, absolute).discard())
                .thenExecute(() -> helper.assertTrue(spawner.activeMobs() == 0 && spawner.mobsLeft() == 3, "A despawn frees the slot, no mob used up"))
                .thenSucceed();
    }

    private static TGNpc linkedNpc(GameTestHelper helper, BlockPos spawner) {
        return helper.getLevel().getEntitiesOfClass(TGNpc.class, new AABB(spawner).inflate(12.0),
                npc -> npc.isAlive() && SpawnerLink.get(npc).filter(spawner::equals).isPresent()).getFirst();
    }
}
