package techguns.world.structure;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.Tags;
import techguns.block.CamoNetBlock;
import techguns.block.CamoNetTopBlock;
import techguns.block.SandbagsBlock;
import techguns.entity.spawn.TGSpawnerBlockEntity;
import techguns.world.LegacyPaletteProcessor.BiomeColor;

/**
 * A structure generated into a {@link CampLevel} (the military camp, the city) as one piece: the level is rebuilt from
 * its seed and the sampled ground heights (and kept while the piece lives), each chunk places its part. A column first loses what isn't ground on top (Techguns 2
 * {@code removeJunkInArea}: trees, plants, water), then is raised or cut to the camp's height, its surface block moved
 * to the new top; then the camp's blocks follow. Fences, panes, camouflage nets and sandbags get their connections
 * from their neighbours, also across the edge to an already placed neighbour chunk.
 */
public abstract class CampLevelPiece extends StructurePiece {
    /** Columns sampled around the camp, for the smoothing filter at its edge. */
    static final int MARGIN = 4;
    /** How far a tree's leaves reach into the next chunk. */
    private static final int LEAF_REACH = 5;
    /** World generation never updates neighbour shapes; a live level would without {@link Block#UPDATE_KNOWN_SHAPE}. */
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    protected final long seed;
    protected final int x0;
    protected final int z0;
    protected final int sizeX;
    protected final int sizeZ;
    protected final BiomeColor color;
    protected final MilitaryBaseStructure.HeightGrid grid;
    @Nullable
    private CampLevel camp;

    protected CampLevelPiece(StructurePieceType type, long seed, int x0, int z0, int sizeX, int sizeZ, BiomeColor color, MilitaryBaseStructure.HeightGrid grid,
                             CampLevel camp) {
        super(type, 0, bounds(camp, x0, z0, sizeX, sizeZ, grid));
        this.seed = seed;
        this.x0 = x0;
        this.z0 = z0;
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
        this.color = color;
        this.grid = grid;
        this.camp = camp;
    }

    protected CampLevelPiece(StructurePieceType type, CompoundTag tag) {
        super(type, tag);
        this.seed = tag.getLong("Seed");
        this.x0 = tag.getInt("X");
        this.z0 = tag.getInt("Z");
        this.sizeX = tag.getInt("SizeX");
        this.sizeZ = tag.getInt("SizeZ");
        this.color = BiomeColor.CODEC.byName(tag.getString("Color"), BiomeColor.WOODLAND);
        this.grid = new MilitaryBaseStructure.HeightGrid(tag.getInt("GridX"), tag.getInt("GridZ"), tag.getInt("GridCountX"), tag.getInt("GridCountZ"),
                tag.getIntArray("Heights"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Seed", seed);
        tag.putInt("X", x0);
        tag.putInt("Z", z0);
        tag.putInt("SizeX", sizeX);
        tag.putInt("SizeZ", sizeZ);
        tag.putString("Color", color.getSerializedName());
        tag.putInt("GridX", grid.originX());
        tag.putInt("GridZ", grid.originZ());
        tag.putInt("GridCountX", grid.countX());
        tag.putInt("GridCountZ", grid.countZ());
        tag.putIntArray("Heights", grid.heights());
    }

    /** A virtual level of the interpolated ground heights over the area and its margin. */
    public static CampLevel level(int x0, int z0, int sizeX, int sizeZ, MilitaryBaseStructure.HeightGrid grid) {
        int minX = x0 - MARGIN;
        int minZ = z0 - MARGIN;
        int spanX = sizeX + 2 * MARGIN;
        int spanZ = sizeZ + 2 * MARGIN;
        int[] heights = new int[spanX * spanZ];
        for (int x = 0; x < spanX; x++) {
            for (int z = 0; z < spanZ; z++) {
                heights[x * spanZ + z] = grid.height(minX + x, minZ + z);
            }
        }
        return new CampLevel(minX, minZ, spanX, spanZ, heights);
    }

    /** Rebuilds the level from the seed (after loading). */
    protected abstract CampLevel rebuild();

    /** Sets up a spawner block entity the level placed. */
    protected abstract void setUpSpawner(TGSpawnerBlockEntity spawner, BlockPos at, CampLevel.Spawner type, WorldGenLevel level);

    /** The changed columns (the smoothing reaches one block past the fence) between the lowest and highest block. */
    private static BoundingBox bounds(CampLevel camp, int x0, int z0, int sizeX, int sizeZ, MilitaryBaseStructure.HeightGrid grid) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int x = x0 - 1; x <= x0 + sizeX; x++) {
            for (int z = z0 - 1; z <= z0 + sizeZ; z++) {
                int before = grid.height(x, z);
                int after = camp.groundY(x, z);
                minY = Math.min(minY, Math.min(before, after));
                maxY = Math.max(maxY, Math.max(before, after));
            }
        }
        for (BlockPos pos : camp.blocks().keySet()) {
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
        }
        return new BoundingBox(x0 - 1, minY - 1, z0 - 1, x0 + sizeX, maxY + 1, z0 + sizeZ);
    }

    /** Chunks of the camp are placed on several threads; the first one rebuilds it after loading. */
    private synchronized CampLevel camp() {
        if (camp == null) {
            camp = rebuild();
        }
        return camp;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box,
                            ChunkPos chunkPos, BlockPos pos) {
        CampLevel camp = camp();
        int minX = Math.max(box.minX(), x0 - 1);
        int maxX = Math.min(box.maxX(), x0 + sizeX);
        int minZ = Math.max(box.minZ(), z0 - 1);
        int maxZ = Math.min(box.maxZ(), z0 + sizeZ);
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                setColumn(level, p, x, z, camp.groundY(x, z));
            }
        }
        // a chunk's trees grow before its camp part is placed and reach into the neighbour chunks, placed before or later
        for (int x = Math.max(box.minX() - LEAF_REACH, x0 - 1); x <= Math.min(box.maxX() + LEAF_REACH, x0 + sizeX); x++) {
            for (int z = Math.max(box.minZ() - LEAF_REACH, z0 - 1); z <= Math.min(box.maxZ() + LEAF_REACH, z0 + sizeZ); z++) {
                if (x < box.minX() || x > box.maxX() || z < box.minZ() || z > box.maxZ()) {
                    clearLeaves(level, p, x, z, camp.groundY(x, z));
                }
            }
        }
        placeBlocks(level, camp.blocks(), box);
        for (Map.Entry<BlockPos, ResourceKey<LootTable>> chest : camp.chests().entrySet()) {
            if (box.isInside(chest.getKey())) {
                RandomizableContainer.setBlockEntityLootTable(level, random, chest.getKey(), chest.getValue());
            }
        }
        for (Map.Entry<BlockPos, CampLevel.Spawner> spawnerEntry : camp.spawners().entrySet()) {
            BlockPos at = spawnerEntry.getKey();
            if (box.isInside(at) && level.getBlockEntity(at) instanceof TGSpawnerBlockEntity spawner) {
                setUpSpawner(spawner, at, spawnerEntry.getValue(), level);
            }
        }
    }

    /**
     * Places the blocks inside the box, then gives fences, panes, walls, fence gates, stairs (corners), camouflage nets
     * (standing and top) and sandbags their connections from their neighbours, also a neighbour already placed right next to the box. Blocks are placed like
     * world generation does (no neighbour shape updates), also when {@code /place} runs the piece on a live level.
     */
    public static void placeBlocks(WorldGenLevel level, Map<BlockPos, BlockState> blocks, BoundingBox box) {
        for (Map.Entry<BlockPos, BlockState> block : blocks.entrySet()) {
            if (box.isInside(block.getKey())) {
                level.setBlock(block.getKey(), block.getValue(), PLACE_FLAGS);
            }
        }
        for (Map.Entry<BlockPos, BlockState> block : blocks.entrySet()) {
            BlockPos at = block.getKey();
            if (connects(block.getValue()) && (box.isInside(at) || nextTo(box, at) && level.getBlockState(at).is(block.getValue().getBlock()))) {
                BlockState current = level.getBlockState(at);
                BlockState shaped = Block.updateFromNeighbourShapes(current, level, at);
                if (shaped != current) {
                    level.setBlock(at, shaped, PLACE_FLAGS);
                }
            }
        }
    }

    /** Clears the column down to the ground, then raises or cuts it to {@code target} with its surface block on top. */
    private static void setColumn(WorldGenLevel level, BlockPos.MutableBlockPos p, int x, int z, int target) {
        BlockState air = Blocks.AIR.defaultBlockState();
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        for (; y > level.getMinBuildHeight(); y--) {
            BlockState state = level.getBlockState(p.set(x, y, z));
            if (isGround(state)) {
                break;
            }
            if (!state.isAir()) {
                level.setBlock(p, air, Block.UPDATE_CLIENTS);
            }
        }
        if (y == target) {
            return;
        }
        BlockState surface = level.getBlockState(p.set(x, y, z));
        BlockState below = level.getBlockState(p.set(x, y - 1, z));
        if (y < target) {
            BlockState filler = isGround(below) ? below : Blocks.DIRT.defaultBlockState();
            for (int i = y; i < target; i++) {
                level.setBlock(p.set(x, i, z), filler, Block.UPDATE_CLIENTS);
            }
        } else {
            for (int i = y; i > target; i--) {
                level.setBlock(p.set(x, i, z), air, Block.UPDATE_CLIENTS);
            }
        }
        level.setBlock(p.set(x, target, z), surface, Block.UPDATE_CLIENTS);
    }

    /** Removes tree leaves and vines above the camp ground of a column (the camp's own blocks stay). */
    private static void clearLeaves(WorldGenLevel level, BlockPos.MutableBlockPos p, int x, int z, int ground) {
        for (int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1; y > ground; y--) {
            BlockState state = level.getBlockState(p.set(x, y, z));
            if (state.is(BlockTags.LEAVES) && !state.getOptionalValue(LeavesBlock.PERSISTENT).orElse(false) || state.is(Blocks.VINE)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Techguns 2 {@code isGroundBlock}: the ground, rock, sand and clay materials. */
    static boolean isGround(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.TERRACOTTA)
                || state.is(Tags.Blocks.GRAVELS) || state.is(Tags.Blocks.SANDSTONE_BLOCKS) || state.is(Tags.Blocks.ORES) || state.is(Blocks.CLAY);
    }

    private static boolean connects(BlockState state) {
        Block block = state.getBlock();
        // 1.12 derived these from their neighbours when drawn (stair corners, wall and fence connections)
        return block instanceof CrossCollisionBlock || block instanceof StairBlock || block instanceof WallBlock
                || block instanceof FenceGateBlock || block instanceof CamoNetBlock || block instanceof CamoNetTopBlock
                || block instanceof SandbagsBlock;
    }

    /** In a neighbour chunk, right next to the box. */
    private static boolean nextTo(BoundingBox box, BlockPos pos) {
        if (box.isInside(pos)) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (box.isInside(pos.relative(direction))) {
                return true;
            }
        }
        return false;
    }
}
