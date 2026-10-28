package techguns.world.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationContext;

/**
 * Samples the generator's ground on the {@link MilitaryBaseStructure.HeightGrid#STEP} lattice from the origin when first asked, so a
 * candidate that fails early costs only a few columns.
 */
final class GroundSampler {
    private final GenerationContext context;
    private final int originX;
    private final int originZ;
    private final Map<Long, int[]> samples = new HashMap<>();

    GroundSampler(GenerationContext context, int originX, int originZ) {
        this.context = context;
        this.originX = originX;
        this.originZ = originZ;
    }

    /** Top solid block and whether a liquid covers it, at lattice point (i, j). */
    private int[] sample(int i, int j) {
        return samples.computeIfAbsent(ChunkPos.asLong(i, j), key -> {
            int x = originX + i * MilitaryBaseStructure.HeightGrid.STEP;
            int z = originZ + j * MilitaryBaseStructure.HeightGrid.STEP;
            int floor = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
            int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            return new int[] {floor - 1, surface > floor ? 1 : 0};
        });
    }

    /** The top ground block at the lattice point nearest to (x, z). */
    int height(int x, int z) {
        return sample(Math.round((float) (x - originX) / MilitaryBaseStructure.HeightGrid.STEP), Math.round((float) (z - originZ) / MilitaryBaseStructure.HeightGrid.STEP))[0];
    }

    /**
     * Techguns 2 {@code getValidSpawnYArea}: every {@code step} blocks from the corner (the far edges only when on
     * the step), no liquid and at most {@code tolerance} apart; the average first free block.
     */
    Optional<Integer> validSpawnY(int x0, int z0, int sizeX, int sizeZ, int tolerance, int step) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        long sum = 0;
        int count = 0;
        for (int dx = 0; dx <= sizeX; dx += step) {
            for (int dz = 0; dz <= sizeZ; dz += step) {
                int[] sample = sample((x0 + dx - originX) / MilitaryBaseStructure.HeightGrid.STEP, (z0 + dz - originZ) / MilitaryBaseStructure.HeightGrid.STEP);
                int h = sample[0] + 1;
                min = Math.min(min, h);
                max = Math.max(max, h);
                if (sample[1] != 0 || max - min > tolerance) {
                    return Optional.empty();
                }
                sum += h;
                count++;
            }
        }
        return Optional.of((int) (sum / count));
    }

    /** All lattice points over the span from the origin. */
    MilitaryBaseStructure.HeightGrid grid(int spanX, int spanZ) {
        int countX = Math.ceilDiv(spanX, MilitaryBaseStructure.HeightGrid.STEP) + 1;
        int countZ = Math.ceilDiv(spanZ, MilitaryBaseStructure.HeightGrid.STEP) + 1;
        int[] heights = new int[countX * countZ];
        for (int i = 0; i < countX; i++) {
            for (int j = 0; j < countZ; j++) {
                heights[i * countZ + j] = sample(i, j)[0];
            }
        }
        return new MilitaryBaseStructure.HeightGrid(originX, originZ, countX, countZ, heights);
    }
}
