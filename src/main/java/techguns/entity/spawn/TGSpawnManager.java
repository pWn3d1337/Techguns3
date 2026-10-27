package techguns.entity.spawn;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import techguns.TGConfig;
import techguns.core.spawn.NpcSpawnTable;
import techguns.core.spawn.SpawnDanger;
import techguns.entity.npc.TGNpc;
import techguns.registry.TGEntities;
import techguns.registry.TGNpcs;

/**
 * Techguns 2 {@code TGSpawnManager}: natural spawns of Techguns NPCs. Spawn markers in the biome spawn lists
 * ({@code techguns:npc_spawns} biome modifiers) are replaced, when they would join the world, by an NPC from the
 * overworld or Nether table: the danger level (distance to the world spawn plus the biome's roughness) decides which
 * NPCs may appear, the config weights how often.
 */
public final class TGSpawnManager {
    /** Techguns 2 {@code getBiomeDanger}, first match wins: Forge biome dictionary types as common biome tags. */
    private static final List<TagKey<Biome>> DANGER_2 = List.of(Tags.Biomes.IS_WASTELAND, Tags.Biomes.IS_SANDY, Tags.Biomes.IS_SNOWY,
            Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_SPOOKY, Tags.Biomes.IS_DEAD);
    private static final List<TagKey<Biome>> DANGER_1 = List.of(Tags.Biomes.IS_COLD, Tags.Biomes.IS_SPARSE_VEGETATION, Tags.Biomes.IS_SAVANNA,
            Tags.Biomes.IS_DRY);

    private TGSpawnManager() {
    }

    public static void init(IEventBus modBus, IEventBus gameBus) {
        modBus.addListener(TGSpawnManager::registerSpawnPlacements);
        gameBus.addListener(TGSpawnManager::onEntityJoinLevel);
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(TGEntities.NPC_SPAWN_MARKER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof NpcSpawnMarker marker) {
            event.setCanceled(true);
            if (!event.loadedFromDisk() && event.getLevel() instanceof ServerLevel level) {
                replace(level, marker);
            }
        }
    }

    /** Techguns 2 overworld table: zombies near the spawn, soldiers and Psycho Steve farther out, bandits beyond. */
    public static NpcSpawnTable<TGNpcs.Npc> overworldTable() {
        return new NpcSpawnTable<TGNpcs.Npc>()
                .add(TGNpcs.ZOMBIE_SOLDIER, TGConfig.SPAWN_WEIGHT_ZOMBIE_SOLDIER.get(), 1)
                .add(TGNpcs.ZOMBIE_FARMER, TGConfig.SPAWN_WEIGHT_ZOMBIE_FARMER.get(), 0)
                .add(TGNpcs.ZOMBIE_MINER, TGConfig.SPAWN_WEIGHT_ZOMBIE_MINER.get(), 0)
                .add(TGNpcs.SKELETON_SOLDIER, TGConfig.SPAWN_WEIGHT_SKELETON_SOLDIER.get(), 1)
                .add(TGNpcs.PSYCHO_STEVE, TGConfig.SPAWN_WEIGHT_PSYCHO_STEVE.get(), 1)
                .add(TGNpcs.BANDIT, TGConfig.SPAWN_WEIGHT_BANDIT.get(), 2);
    }

    public static NpcSpawnTable<TGNpcs.Npc> netherTable() {
        return new NpcSpawnTable<TGNpcs.Npc>()
                .add(TGNpcs.ZOMBIE_PIGMAN_SOLDIER, TGConfig.SPAWN_WEIGHT_ZOMBIE_PIGMAN_SOLDIER.get(), 0)
                .add(TGNpcs.CYBER_DEMON, TGConfig.SPAWN_WEIGHT_CYBER_DEMON.get(), 0);
    }

    /** Danger level at {@code pos}: distance to the world spawn plus the biome's. */
    public static int danger(ServerLevel level, BlockPos pos) {
        boolean nether = level.dimension() == Level.NETHER;
        BlockPos spawn = level.getSharedSpawnPos();
        double distance = Math.sqrt(Math.pow(pos.getX() - spawn.getX(), 2) + Math.pow(pos.getZ() - spawn.getZ(), 2));
        int[] levels = {TGConfig.DISTANCE_SPAWN_LEVEL_0.get(), TGConfig.DISTANCE_SPAWN_LEVEL_1.get(), TGConfig.DISTANCE_SPAWN_LEVEL_2.get()};
        return SpawnDanger.fromDistance(distance, nether, levels) + biomeDanger(level.getBiome(pos));
    }

    private static int biomeDanger(Holder<Biome> biome) {
        if (biome.is(Tags.Biomes.IS_NETHER) || biome.is(Tags.Biomes.IS_END)) {
            return 0;
        }
        if (DANGER_2.stream().anyMatch(biome::is)) {
            return 2;
        }
        return DANGER_1.stream().anyMatch(biome::is) ? 1 : 0;
    }

    /** Spawns the NPC the tables pick for the marker's place instead of the marker; empty if none may spawn there. */
    public static Optional<TGNpc> replace(ServerLevel level, NpcSpawnMarker marker) {
        BlockPos pos = marker.blockPosition();
        NpcSpawnTable<TGNpcs.Npc> table = level.dimension() == Level.NETHER ? netherTable() : overworldTable();
        Optional<TGNpcs.Npc> picked = table.pick(danger(level, pos), bound -> level.random.nextInt(bound));
        return picked.map(npc -> {
            TGNpc entity = npc.type().get().create(level);
            if (entity == null) {
                return null;
            }
            entity.moveTo(marker.getX(), marker.getY(), marker.getZ(), marker.getYRot(), 0.0f);
            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
            level.addFreshEntity(entity);
            return entity;
        });
    }
}
