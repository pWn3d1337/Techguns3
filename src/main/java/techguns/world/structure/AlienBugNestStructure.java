package techguns.world.structure;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import techguns.registry.TGStructures;

/**
 * Techguns 2 {@code AlienBugNestStructure}: an alien bug nest below the chunk corner, its entrance 2 blocks under the
 * ground there (Techguns 2 accepted any slope, but no liquid on top).
 */
public class AlienBugNestStructure extends Structure {
    public static final MapCodec<AlienBugNestStructure> CODEC = simpleCodec(AlienBugNestStructure::new);

    public AlienBugNestStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMinBlockX();
        int z = context.chunkPos().getMinBlockZ();
        int sizeX = 16 + context.random().nextInt(16);
        int sizeZ = 16 + context.random().nextInt(16);
        // Techguns 2 getValidSpawnY: the four footprint corners (here the nest's own 4 x 4 area)
        Optional<Integer> ground = LegacyTemplateStructure.groundHeight(context, x, z, 4, 4, Integer.MAX_VALUE);
        if (ground.isEmpty() || LegacyTemplateStructure.isOcean(context, x, ground.get(), z)) {
            return Optional.empty();
        }
        long seed = context.random().nextLong();
        int y = ground.get() - 2;
        return Optional.of(new GenerationStub(new BlockPos(x + sizeX / 2, y, z + sizeZ / 2),
                pieces -> pieces.addPiece(new AlienBugNestPiece(seed, new BlockPos(x, y, z), sizeX, sizeZ))));
    }

    @Override
    public StructureType<?> type() {
        return TGStructures.ALIEN_BUG_NEST.get();
    }
}
