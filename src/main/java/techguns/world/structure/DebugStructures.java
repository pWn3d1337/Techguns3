package techguns.world.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import techguns.world.LegacyPaletteProcessor.BiomeColor;

/**
 * The Techguns 2 {@code WorldGenTestTool} operations on a selected area of an existing world: the military camp and
 * the city built over the area (as their structures would, with the ground of the world as it is now), and the
 * terrain tools (Techguns 2 {@code BlockUtils}): smooth, flatten and remove junk.
 */
public final class DebugStructures {
    /** Techguns 2 {@code FILTER_GAUSSIAN_5x5}. */
    private static final float[][] GAUSSIAN_5X5 = {
            {.00325f, .01375f, .0235f, .01375f, .00325f},
            {.01375f, .059f, .097f, .059f, .01375f},
            {.0235f, .097f, .159f, .097f, .0235f},
            {.01375f, .059f, .097f, .059f, .01375f},
            {.00325f, .01375f, .0235f, .01375f, .00325f}};
    /** Techguns 2 {@code flattenArea(..., 2)}. */
    private static final int FLATTEN_HEIGHT_DIFF = 2;

    private DebugStructures() {
    }

    /** The military camp over {@code sizeX} x {@code sizeZ} blocks from {@code x0, z0}. */
    public static void placeCamp(ServerLevel level, int x0, int z0, int sizeX, int sizeZ, RandomSource random) {
        GroundSampler sampler = new GroundSampler(level, x0 - CampLevelPiece.MARGIN, z0 - CampLevelPiece.MARGIN);
        var grid = sampler.grid(sizeX + 2 * CampLevelPiece.MARGIN, sizeZ + 2 * CampLevelPiece.MARGIN);
        int centerX = x0 + sizeX / 2;
        int centerZ = z0 + sizeZ / 2;
        BiomeColor color = MilitaryBaseStructure.campColor(level.getBiome(new BlockPos(centerX, sampler.height(centerX, centerZ), centerZ)));
        place(level, new MilitaryBasePiece(random.nextLong(), x0, z0, sizeX, sizeZ, color, grid), random);
    }

    /** The city over {@code sizeX} x {@code sizeZ} blocks from {@code x0, z0}, at the area's average ground height. */
    public static void placeCity(ServerLevel level, int x0, int z0, int sizeX, int sizeZ, RandomSource random) {
        GroundSampler sampler = new GroundSampler(level, x0 - CampLevelPiece.MARGIN, z0 - CampLevelPiece.MARGIN);
        var grid = sampler.grid(sizeX + 2 * CampLevelPiece.MARGIN, sizeZ + 2 * CampLevelPiece.MARGIN);
        int baseY = sampler.validSpawnY(x0, z0, sizeX, sizeZ, Integer.MAX_VALUE, MilitaryBaseStructure.HeightGrid.STEP)
                .orElse(sampler.height(x0 + sizeX / 2, z0 + sizeZ / 2) + 1);
        place(level, new CityPiece(random.nextLong(), x0, z0, sizeX, sizeZ, baseY, grid), random);
    }

    /** Builds a structure piece into the world chunk by chunk, like {@code /place structure}. */
    public static void place(ServerLevel level, StructurePiece piece, RandomSource random) {
        BoundingBox box = piece.getBoundingBox();
        BlockPos center = box.getCenter();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                BoundingBox chunkBox = new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                        chunk.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunk.getMaxBlockZ());
                piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), random, chunkBox, chunk, center);
            }
        }
    }

    // --- terrain (Techguns 2 BlockUtils) ---

    /** Techguns 2 {@code getHeightValueNoTrees} - 1: the top block that isn't leaves. */
    private static int ground(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
    }

    /** Techguns 2 {@code apply2DHeightmapFilter} with the 5x5 gaussian: smooths the ground heights of the area. */
    public static void smooth(ServerLevel level, int x0, int z0, int sizeX, int sizeZ) {
        int[][] target = new int[sizeX][sizeZ];
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                float y = 0.0f;
                for (int i = 0; i < GAUSSIAN_5X5.length; i++) {
                    for (int j = 0; j < GAUSSIAN_5X5[i].length; j++) {
                        y += ground(level, x0 + x + i - 2, z0 + z + j - 2) * GAUSSIAN_5X5[i][j];
                    }
                }
                target[x][z] = Math.round(y);
            }
        }
        // Techguns 2 changed the heights while it filtered; computing all first keeps the filter symmetric
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                adjustHeight(level, p, x0 + x, z0 + z, ground(level, x0 + x, z0 + z), target[x][z]);
            }
        }
    }

    /**
     * Techguns 2 {@code flattenArea}: the area's height span is squeezed to 2 blocks around the median height (an area
     * that is flat enough stays as it is).
     */
    public static void flatten(ServerLevel level, int x0, int z0, int sizeX, int sizeZ) {
        List<Integer> heights = new ArrayList<>();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                int y = ground(level, x0 + x, z0 + z);
                heights.add(y);
                min = Math.min(min, y);
                max = Math.max(max, y);
            }
        }
        int span = max - min;
        if (span <= FLATTEN_HEIGHT_DIFF) {
            return;
        }
        Collections.sort(heights);
        float median = heights.get(heights.size() / 2);
        float bottom = median - FLATTEN_HEIGHT_DIFF / 2.0f;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                int y = ground(level, x0 + x, z0 + z);
                adjustHeight(level, p, x0 + x, z0 + z, y, Math.round((float) (y - min) / span * FLATTEN_HEIGHT_DIFF + bottom));
            }
        }
    }

    /** Techguns 2 {@code adjustHeightAtPos}: raises the column with its ground block (or dirt) or cuts it down. */
    private static void adjustHeight(ServerLevel level, BlockPos.MutableBlockPos p, int x, int z, int y, int newY) {
        if (newY > y) {
            BlockState top = level.getBlockState(p.set(x, y, z));
            BlockState fill = CampLevelPiece.isGround(top) ? top : Blocks.DIRT.defaultBlockState();
            for (int i = y + 1; i <= newY; i++) {
                level.setBlock(p.set(x, i, z), fill, Block.UPDATE_CLIENTS);
            }
        } else {
            for (int i = y; i > newY; i--) {
                level.setBlock(p.set(x, i, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Techguns 2 {@code removeJunkInArea}: clears every column down to its ground (trees, plants, buildings). */
    public static void removeJunk(ServerLevel level, int x0, int z0, int sizeX, int sizeZ) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = x0; x < x0 + sizeX; x++) {
            for (int z = z0; z < z0 + sizeZ; z++) {
                for (int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1; y > level.getMinBuildHeight(); y--) {
                    if (CampLevelPiece.isGround(level.getBlockState(p.set(x, y, z)))) {
                        break;
                    }
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }
}
