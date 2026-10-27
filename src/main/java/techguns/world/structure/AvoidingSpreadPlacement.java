package techguns.world.structure;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import techguns.registry.TGStructures;

/**
 * Random spread placement that keeps away from several other structure sets (vanilla allows one exclusion zone). The
 * Techguns 2 size grids never overlapped; here a small structure keeps away from the medium and the big sets, a medium
 * one from the big set.
 */
public class AvoidingSpreadPlacement extends RandomSpreadStructurePlacement {
    /** The vanilla random spread fields (without its single exclusion zone) and the zones to avoid. */
    @SuppressWarnings("deprecation")
    public static final MapCodec<AvoidingSpreadPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(AvoidingSpreadPlacement::locateOffset),
            FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", FrequencyReductionMethod.DEFAULT)
                    .forGetter(AvoidingSpreadPlacement::frequencyReductionMethod),
            Codec.floatRange(0.0f, 1.0f).optionalFieldOf("frequency", 1.0f).forGetter(AvoidingSpreadPlacement::frequency),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(AvoidingSpreadPlacement::salt),
            Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing),
            Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation),
            RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType),
            ExclusionZone.CODEC.listOf().fieldOf("avoid").forGetter(p -> p.avoid)
    ).apply(instance, AvoidingSpreadPlacement::new));

    @SuppressWarnings("deprecation")
    private final List<ExclusionZone> avoid;

    @SuppressWarnings("deprecation")
    public AvoidingSpreadPlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, int spacing,
                                   int separation, RandomSpreadType spreadType, List<ExclusionZone> avoid) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, Optional.empty(), spacing, separation, spreadType);
        this.avoid = List.copyOf(avoid);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int x, int z) {
        if (!super.isPlacementChunk(state, x, z)) {
            return false;
        }
        for (ExclusionZone zone : avoid) {
            if (state.hasStructureChunkInRange(zone.otherSet(), x, z, zone.chunkCount())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public StructurePlacementType<?> type() {
        return TGStructures.AVOIDING_SPREAD.get();
    }
}
