package techguns.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import techguns.entity.spawn.TGSpawnerBlockEntity;
import techguns.registry.TGNpcs;
import techguns.registry.TGStructures;
import techguns.world.LegacyPaletteProcessor.BiomeColor;

/** The military camp ({@link MilitaryCamp}) as one {@link CampLevelPiece}. */
public class MilitaryBasePiece extends CampLevelPiece {
    /** Techguns 2 camp flag spawner: the helicopter appears 64 blocks above it, below the build limit. */
    private static final int HELICOPTER_HEIGHT = 64;

    MilitaryBasePiece(long seed, int x0, int z0, int sizeX, int sizeZ, BiomeColor color, MilitaryBaseStructure.HeightGrid grid) {
        super(TGStructures.MILITARY_BASE_PIECE.get(), seed, x0, z0, sizeX, sizeZ, color, grid, build(seed, x0, z0, sizeX, sizeZ, color, grid));
    }

    public MilitaryBasePiece(CompoundTag tag) {
        super(TGStructures.MILITARY_BASE_PIECE.get(), tag);
    }

    /** Builds the camp into a virtual level of the interpolated ground heights. */
    public static CampLevel build(long seed, int x0, int z0, int sizeX, int sizeZ, BiomeColor color, MilitaryBaseStructure.HeightGrid grid) {
        CampLevel level = level(x0, z0, sizeX, sizeZ, grid);
        new MilitaryCamp(level, RandomSource.create(seed), color, x0, z0, sizeX, sizeZ).build();
        return level;
    }

    @Override
    protected CampLevel rebuild() {
        return build(seed, x0, z0, sizeX, sizeZ, color, grid);
    }

    @Override
    protected void setUpSpawner(TGSpawnerBlockEntity spawner, BlockPos at, CampLevel.Spawner type, WorldGenLevel level) {
        if (type == CampLevel.Spawner.HELICOPTER) {
            spawner.addMobType(TGNpcs.ATTACK_HELICOPTER.type().get(), 1);
            spawner.setParams(1, 1, 200, 0);
            spawner.setSpawnHeightOffset(Math.min(at.getY() + HELICOPTER_HEIGHT, level.getMaxBuildHeight()) - at.getY());
        } else {
            spawner.addMobType(TGNpcs.ARMY_SOLDIER.type().get(), 1);
            spawner.setParams(3, 1, 200, 0);
        }
    }
}
