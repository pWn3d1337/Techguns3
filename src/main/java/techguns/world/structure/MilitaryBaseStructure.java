package techguns.world.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.common.Tags;
import techguns.registry.TGStructures;
import techguns.world.LegacyPaletteProcessor.BiomeColor;

/**
 * Techguns 2 {@code MilitaryBaseStructure}: a military camp of 32 to 79 blocks per side from the chunk corner. It
 * spawns where the ground (sampled every 8 blocks) varies by at most 5 blocks with no liquid on top; otherwise both
 * sides shrink by 16 and it tries again, down to 31 blocks. Not in oceans.
 * <p>
 * The camp needs the ground height of every column; the generator's heights are sampled every {@link HeightGrid#STEP}
 * blocks (lazily, a failing candidate stops at the first bad sample) and interpolated (the camp flattens and
 * smooths them anyway), and the piece sets each column to the camp's
 * height when it is placed. Placed in the top layer step, after the trees, which it clears like Techguns 2 did.
 */
public class MilitaryBaseStructure extends Structure {
    public static final MapCodec<MilitaryBaseStructure> CODEC = simpleCodec(MilitaryBaseStructure::new);

    /** Techguns 2 {@code getValidSpawnYArea(..., 5, 8)}. */
    private static final int HEIGHT_TOLERANCE = 5;
    private static final int VALID_STEP = 8;
    private static final int MIN_SIZE = 32;
    private static final int SIZE_RANGE = 48;
    private static final int SHRINK = 16;

    public MilitaryBaseStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        WorldgenRandom random = context.random();
        int sizeX = MIN_SIZE + random.nextInt(SIZE_RANGE);
        int sizeZ = MIN_SIZE + random.nextInt(SIZE_RANGE);
        int x0 = context.chunkPos().getMinBlockX();
        int z0 = context.chunkPos().getMinBlockZ();
        Sampler sampler = new Sampler(context, x0 - MilitaryBasePiece.MARGIN, z0 - MilitaryBasePiece.MARGIN);
        // most candidates fail; the biome is cheaper than the heights
        if (!validBiome(context, x0 + sizeX / 2, sampler.height(x0 + sizeX / 2, z0 + sizeZ / 2), z0 + sizeZ / 2)) {
            return Optional.empty();
        }
        Optional<Integer> ground;
        while ((ground = sampler.validSpawnY(x0, z0, sizeX, sizeZ)).isEmpty()) {
            if (sizeX < MIN_SIZE || sizeZ < MIN_SIZE) {
                return Optional.empty();
            }
            sizeX -= SHRINK;
            sizeZ -= SHRINK;
        }
        int centerX = x0 + sizeX / 2;
        int centerZ = z0 + sizeZ / 2;
        if (!validBiome(context, centerX, ground.get(), centerZ)) {
            return Optional.empty();
        }
        HeightGrid grid = sampler.grid(sizeX + 2 * MilitaryBasePiece.MARGIN, sizeZ + 2 * MilitaryBasePiece.MARGIN);
        BiomeColor color = campColor(LegacyTemplateStructure.biome(context, centerX, ground.get(), centerZ));
        long seed = random.nextLong();
        int finalSizeX = sizeX;
        int finalSizeZ = sizeZ;
        return Optional.of(new GenerationStub(new BlockPos(centerX, ground.get(), centerZ),
                pieces -> pieces.addPiece(new MilitaryBasePiece(seed, x0, z0, finalSizeX, finalSizeZ, color, grid))));
    }

    private static boolean validBiome(GenerationContext context, int x, int y, int z) {
        Holder<Biome> biome = LegacyTemplateStructure.biome(context, x, y, z);
        return context.validBiome().test(biome) && !LegacyTemplateStructure.isOcean(context, x, y, z);
    }

    /** Techguns 2 {@code BlockUtils.getBiomeType}, the camp's own biome colour rule. */
    static BiomeColor campColor(Holder<Biome> biome) {
        if (biome.is(Tags.Biomes.IS_SANDY) || biome.is(Tags.Biomes.IS_SAVANNA) || biome.is(Tags.Biomes.IS_WASTELAND)) {
            return BiomeColor.DESERT;
        }
        return biome.is(Tags.Biomes.IS_SNOWY) ? BiomeColor.SNOW : BiomeColor.WOODLAND;
    }

    @Override
    public StructureType<?> type() {
        return TGStructures.MILITARY_BASE.get();
    }

    /** The generator's ground heights (top solid block) every {@link #STEP} blocks from the origin, interpolated between. */
    public record HeightGrid(int originX, int originZ, int countX, int countZ, int[] heights) {
        static final int STEP = 4;

        private int at(int i, int j) {
            return heights[Math.clamp(i, 0, countX - 1) * countZ + Math.clamp(j, 0, countZ - 1)];
        }

        /** The interpolated top ground block of a column. */
        int height(int x, int z) {
            float fx = (float) (x - originX) / STEP;
            float fz = (float) (z - originZ) / STEP;
            int i = (int) Math.floor(fx);
            int j = (int) Math.floor(fz);
            float tx = fx - i;
            float tz = fz - j;
            float top = at(i, j) * (1 - tx) + at(i + 1, j) * tx;
            float bottom = at(i, j + 1) * (1 - tx) + at(i + 1, j + 1) * tx;
            return Math.round(top * (1 - tz) + bottom * tz);
        }
    }

    /**
     * Samples the generator's ground on the {@link HeightGrid#STEP} lattice from the origin when first asked, so a
     * candidate that fails early costs only a few columns.
     */
    private static final class Sampler {
        private final GenerationContext context;
        private final int originX;
        private final int originZ;
        private final Map<Long, int[]> samples = new HashMap<>();

        Sampler(GenerationContext context, int originX, int originZ) {
            this.context = context;
            this.originX = originX;
            this.originZ = originZ;
        }

        /** Top solid block and whether a liquid covers it, at lattice point (i, j). */
        private int[] sample(int i, int j) {
            return samples.computeIfAbsent(ChunkPos.asLong(i, j), key -> {
                int x = originX + i * HeightGrid.STEP;
                int z = originZ + j * HeightGrid.STEP;
                int floor = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
                int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                return new int[] {floor - 1, surface > floor ? 1 : 0};
            });
        }

        /** The top ground block at the lattice point nearest to (x, z). */
        int height(int x, int z) {
            return sample(Math.round((float) (x - originX) / HeightGrid.STEP), Math.round((float) (z - originZ) / HeightGrid.STEP))[0];
        }

        /**
         * Techguns 2 {@code getValidSpawnYArea}: every {@link #VALID_STEP} blocks from the corner (the far edges only
         * when on the step), no liquid and at most {@link #HEIGHT_TOLERANCE} apart; the average first free block.
         */
        Optional<Integer> validSpawnY(int x0, int z0, int sizeX, int sizeZ) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            long sum = 0;
            int count = 0;
            for (int dx = 0; dx <= sizeX; dx += VALID_STEP) {
                for (int dz = 0; dz <= sizeZ; dz += VALID_STEP) {
                    int[] sample = sample((x0 + dx - originX) / HeightGrid.STEP, (z0 + dz - originZ) / HeightGrid.STEP);
                    int h = sample[0] + 1;
                    min = Math.min(min, h);
                    max = Math.max(max, h);
                    if (sample[1] != 0 || max - min > HEIGHT_TOLERANCE) {
                        return Optional.empty();
                    }
                    sum += h;
                    count++;
                }
            }
            return Optional.of((int) (sum / count));
        }

        /** All lattice points over the span from the origin. */
        HeightGrid grid(int spanX, int spanZ) {
            int countX = Math.ceilDiv(spanX, HeightGrid.STEP) + 1;
            int countZ = Math.ceilDiv(spanZ, HeightGrid.STEP) + 1;
            int[] heights = new int[countX * countZ];
            for (int i = 0; i < countX; i++) {
                for (int j = 0; j < countZ; j++) {
                    heights[i * countZ + j] = sample(i, j)[0];
                }
            }
            return new HeightGrid(originX, originZ, countX, countZ, heights);
        }
    }
}
