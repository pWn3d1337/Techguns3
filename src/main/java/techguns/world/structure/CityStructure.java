package techguns.world.structure;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import techguns.registry.TGStructures;

/**
 * Techguns 2 {@code CityStructure} (new: Techguns 2 never spawned it): a city of 32 to 79 blocks per side from the
 * chunk corner where the ground (sampled every 16 blocks) varies by at most 5 blocks with no liquid on top; otherwise
 * both sides shrink by 16 and it tries again, down to 32. Not in oceans. Generated like the military camp into a
 * virtual level of the sampled heights ({@link CityPiece}), after the trees, which it clears.
 */
public class CityStructure extends Structure {
    public static final MapCodec<CityStructure> CODEC = simpleCodec(CityStructure::new);

    /** Techguns 2 {@code getValidSpawnYArea(..., 5, getStep())} with a step of 16. */
    private static final int HEIGHT_TOLERANCE = 5;
    private static final int VALID_STEP = 16;
    private static final int MIN_SIZE = 32;
    private static final int SIZE_RANGE = 48;
    private static final int SHRINK = 16;

    public CityStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        WorldgenRandom random = context.random();
        int sizeX = MIN_SIZE + random.nextInt(SIZE_RANGE);
        int sizeZ = MIN_SIZE + random.nextInt(SIZE_RANGE);
        int x0 = context.chunkPos().getMinBlockX();
        int z0 = context.chunkPos().getMinBlockZ();
        GroundSampler sampler = new GroundSampler(context, x0 - CampLevelPiece.MARGIN, z0 - CampLevelPiece.MARGIN);
        if (!validBiome(context, x0 + sizeX / 2, sampler.height(x0 + sizeX / 2, z0 + sizeZ / 2), z0 + sizeZ / 2)) {
            return Optional.empty();
        }
        Optional<Integer> ground;
        while ((ground = sampler.validSpawnY(x0, z0, sizeX, sizeZ, HEIGHT_TOLERANCE, VALID_STEP)).isEmpty()) {
            sizeX -= SHRINK;
            sizeZ -= SHRINK;
            if (sizeX < MIN_SIZE || sizeZ < MIN_SIZE) {
                return Optional.empty();
            }
        }
        int centerX = x0 + sizeX / 2;
        int centerZ = z0 + sizeZ / 2;
        if (!validBiome(context, centerX, ground.get(), centerZ)) {
            return Optional.empty();
        }
        MilitaryBaseStructure.HeightGrid grid = sampler.grid(sizeX + 2 * CampLevelPiece.MARGIN, sizeZ + 2 * CampLevelPiece.MARGIN);
        long seed = random.nextLong();
        int finalSizeX = sizeX;
        int finalSizeZ = sizeZ;
        int baseY = ground.get();
        return Optional.of(new GenerationStub(new BlockPos(centerX, baseY, centerZ),
                pieces -> pieces.addPiece(new CityPiece(seed, x0, z0, finalSizeX, finalSizeZ, baseY, grid))));
    }

    private static boolean validBiome(GenerationContext context, int x, int y, int z) {
        var biome = LegacyTemplateStructure.biome(context, x, y, z);
        return context.validBiome().test(biome) && !LegacyTemplateStructure.isOcean(context, x, y, z);
    }

    @Override
    public StructureType<?> type() {
        return TGStructures.CITY.get();
    }
}
