package techguns.world.structure.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import techguns.entity.spawn.TGSpawnerBlockEntity;
import techguns.registry.TGBlocks;
import techguns.registry.TGNpcs;
import techguns.registry.TGStructures;

/**
 * Techguns 2 {@code generateNPCSpawners}: a monster hole on the floor of a dungeon cell, with the preset's mobs and
 * settings. Placed after the segments (pieces place in order), like Techguns 2.
 */
public class DungeonSpawnerPiece extends StructurePiece {
    private final DungeonPreset preset;

    public DungeonSpawnerPiece(BlockPos pos, DungeonPreset preset) {
        super(TGStructures.DUNGEON_SPAWNER_PIECE.get(), 0, new BoundingBox(pos));
        this.preset = preset;
    }

    public DungeonSpawnerPiece(CompoundTag tag) {
        super(TGStructures.DUNGEON_SPAWNER_PIECE.get(), tag);
        this.preset = DungeonPreset.CODEC.byName(tag.getString("Preset"), DungeonPreset.CASTLE);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Preset", preset.getSerializedName());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box,
                            ChunkPos chunkPos, BlockPos pivot) {
        BlockPos pos = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
        if (!box.isInside(pos)) {
            return;
        }
        level.setBlock(pos, TGBlocks.MONSTER_HOLE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(pos) instanceof TGSpawnerBlockEntity spawner) {
            for (TGNpcs.Npc mob : preset.spawnerMobs) {
                spawner.addMobType(mob.type().get(), 1);
            }
            spawner.setParams(DungeonPreset.SPAWNER_MOBS, DungeonPreset.SPAWNER_MAX_ACTIVE, DungeonPreset.SPAWNER_DELAY, DungeonPreset.SPAWNER_RANGE);
        }
    }
}
