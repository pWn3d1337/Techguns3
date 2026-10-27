package techguns.world.structure;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import techguns.entity.spawn.TGSpawnerBlockEntity;
import techguns.registry.TGNpcs;
import techguns.registry.TGStructures;

/**
 * The alien bug nest as one piece: the nest is rebuilt from its seed (and kept while the piece lives) and each chunk
 * places the part inside it. Spawners get the Techguns 2 settings: alien bugs, 7 mobs, 3 at a time, delay 150, range 1.
 */
public class AlienBugNestPiece extends StructurePiece {
    private final long seed;
    private final BlockPos origin;
    private final int sizeX;
    private final int sizeZ;
    @Nullable
    private AlienBugNest nest;

    public AlienBugNestPiece(long seed, BlockPos origin, int sizeX, int sizeZ) {
        this(seed, origin, sizeX, sizeZ, new AlienBugNest(seed, origin.getX(), origin.getY(), origin.getZ(), sizeX, sizeZ));
    }

    private AlienBugNestPiece(long seed, BlockPos origin, int sizeX, int sizeZ, AlienBugNest nest) {
        super(TGStructures.ALIEN_BUG_NEST_PIECE.get(), 0, nest.bounds());
        this.seed = seed;
        this.origin = origin;
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
        this.nest = nest;
    }

    public AlienBugNestPiece(CompoundTag tag) {
        super(TGStructures.ALIEN_BUG_NEST_PIECE.get(), tag);
        this.seed = tag.getLong("Seed");
        this.origin = NbtUtils.readBlockPos(tag, "Origin").orElse(BlockPos.ZERO);
        this.sizeX = tag.getInt("SizeX");
        this.sizeZ = tag.getInt("SizeZ");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Seed", seed);
        tag.put("Origin", NbtUtils.writeBlockPos(origin));
        tag.putInt("SizeX", sizeX);
        tag.putInt("SizeZ", sizeZ);
    }

    private AlienBugNest nest() {
        if (nest == null) {
            nest = new AlienBugNest(seed, origin.getX(), origin.getY(), origin.getZ(), sizeX, sizeZ);
        }
        return nest;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box,
                            ChunkPos chunkPos, BlockPos pos) {
        AlienBugNest nest = nest();
        for (Map.Entry<BlockPos, BlockState> block : nest.blocks().entrySet()) {
            if (box.isInside(block.getKey())) {
                level.setBlock(block.getKey(), block.getValue(), Block.UPDATE_CLIENTS);
            }
        }
        for (BlockPos spawnerPos : nest.spawners()) {
            if (box.isInside(spawnerPos) && level.getBlockEntity(spawnerPos) instanceof TGSpawnerBlockEntity spawner) {
                spawner.addMobType(TGNpcs.ALIEN_BUG.type().get(), 1);
                spawner.setParams(7, 3, 150, 1);
            }
        }
    }
}
