package techguns.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import techguns.entity.spawn.TGSpawnerBlockEntity;
import techguns.registry.TGStructures;
import techguns.world.LegacyPaletteProcessor.BiomeColor;

/** The city ({@link CityGenerator}) as one {@link CampLevelPiece}; its base height is the area's average ground. */
public class CityPiece extends CampLevelPiece {
    private final int baseY;

    CityPiece(long seed, int x0, int z0, int sizeX, int sizeZ, int baseY, MilitaryBaseStructure.HeightGrid grid) {
        super(TGStructures.CITY_PIECE.get(), seed, x0, z0, sizeX, sizeZ, BiomeColor.WOODLAND, grid, build(seed, x0, z0, sizeX, sizeZ, baseY, grid));
        this.baseY = baseY;
    }

    public CityPiece(CompoundTag tag) {
        super(TGStructures.CITY_PIECE.get(), tag);
        this.baseY = tag.getInt("BaseY");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putInt("BaseY", baseY);
    }

    /** Builds the city into a virtual level of the interpolated ground heights. */
    public static CampLevel build(long seed, int x0, int z0, int sizeX, int sizeZ, int baseY, MilitaryBaseStructure.HeightGrid grid) {
        CampLevel level = level(x0, z0, sizeX, sizeZ, grid);
        new CityGenerator(level, RandomSource.create(seed)).build(x0, baseY, z0, sizeX, sizeZ);
        return level;
    }

    @Override
    protected CampLevel rebuild() {
        return build(seed, x0, z0, sizeX, sizeZ, baseY, grid);
    }

    @Override
    protected void setUpSpawner(TGSpawnerBlockEntity spawner, BlockPos at, CampLevel.Spawner type, WorldGenLevel level) {
        // Techguns 2 cities have no spawners
    }
}
