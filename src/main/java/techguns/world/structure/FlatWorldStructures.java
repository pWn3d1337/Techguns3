package techguns.world.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.neoforged.neoforge.event.level.LevelEvent;
import techguns.TGConfig;
import techguns.Techguns;

/**
 * Techguns structures in superflat worlds. Techguns 2 generated them in every world type; a 1.21 flat world only places
 * the structure sets its preset lists (villages and strongholds by default), so a flat world that lists any structures
 * gets the Techguns sets too. Presets without structures (The Void, Redstone Ready) stay empty, and so does the GameTest
 * world, whose tests need predictable surroundings.
 */
public final class FlatWorldStructures {
    private FlatWorldStructures() {
    }

    /** Swaps the flat level's structure state before any chunk or the spawn point is generated. */
    // MIGRATION(26.x): replaces ChunkMap.chunkGeneratorState (made public and non-final by accesstransformer.cfg).
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getServer() instanceof GameTestServer
                || !TGConfig.FLAT_WORLD_STRUCTURES.get() || !(level.getChunkSource().getGenerator() instanceof FlatLevelSource flat)) {
            return;
        }
        Registry<StructureSet> sets = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        withTechgunsSets(flat.settings().structureOverrides(), sets).ifPresent(structureSets -> {
            ChunkMap chunkMap = level.getChunkSource().chunkMap;
            chunkMap.chunkGeneratorState = ChunkGeneratorStructureState.createForFlat(level.getChunkSource().randomState(), level.getSeed(),
                    flat.getBiomeSource(), structureSets.stream());
        });
    }

    /**
     * The structure sets of a flat world with the Techguns sets added, or empty when nothing changes: without overrides
     * the world already places every set, an empty override list means the preset wants no structures.
     */
    public static Optional<List<Holder<StructureSet>>> withTechgunsSets(Optional<HolderSet<StructureSet>> overrides, Registry<StructureSet> sets) {
        if (overrides.isEmpty() || overrides.get().size() == 0) {
            return Optional.empty();
        }
        HolderSet<StructureSet> listed = overrides.get();
        List<Holder<StructureSet>> result = new ArrayList<>(listed.stream().toList());
        sets.holders().filter(set -> set.key().location().getNamespace().equals(Techguns.MODID) && !listed.contains(set)).forEach(result::add);
        return result.size() == listed.size() ? Optional.empty() : Optional.of(result);
    }
}
