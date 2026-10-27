package techguns.world.structure.dungeon;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import techguns.registry.TGStructures;
import techguns.world.LegacyPaletteProcessor;
import techguns.world.structure.LegacyTemplatePiece;
import techguns.world.structure.LegacyTemplateStructure;

/**
 * Techguns 2 {@code CastleStructure} and {@code Dungeon}: a maze dungeon from the chunk corner, 32 to 47 blocks wide
 * and long and 24 to 39 high (whole cells of the preset), where the ground (every 4 blocks) varies by at most 10 with
 * no liquid on top, not in oceans. Its lowest level is sunk into the ground. The best of 5 paths (most cells) is
 * built; each segment is a template piece, rotated about its cell centre.
 */
public class MazeDungeonStructure extends Structure {
    public static final MapCodec<MazeDungeonStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            DungeonPreset.CODEC.fieldOf("preset").forGetter(s -> s.preset)
    ).apply(instance, MazeDungeonStructure::new));

    /** Techguns 2 {@code CastleStructure.heightdiffLimit}. */
    private static final int HEIGHT_TOLERANCE = 10;
    /** Techguns 2 {@code Dungeon.GENERATE_ATTEMPTS}. */
    private static final int ATTEMPTS = 5;

    private final DungeonPreset preset;

    public MazeDungeonStructure(StructureSettings settings, DungeonPreset preset) {
        super(settings);
        this.preset = preset;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        WorldgenRandom random = context.random();
        int cellsX = (32 + random.nextInt(16)) / preset.sizeXZ;
        int cellsZ = (32 + random.nextInt(16)) / preset.sizeXZ;
        int levels = (24 + random.nextInt(16)) / preset.sizeY;
        int x0 = context.chunkPos().getMinBlockX();
        int z0 = context.chunkPos().getMinBlockZ();
        int sizeX = cellsX * preset.sizeXZ;
        int sizeZ = cellsZ * preset.sizeXZ;
        Optional<Integer> ground = LegacyTemplateStructure.groundHeight(context, x0, z0, sizeX, sizeZ, HEIGHT_TOLERANCE)
                .filter(y -> !LegacyTemplateStructure.isOcean(context, x0 + sizeX / 2, y, z0 + sizeZ / 2));
        if (ground.isEmpty()) {
            return Optional.empty();
        }
        // Techguns 2: one level below the first free block above the ground
        int y0 = ground.get() - preset.sizeY;
        return Optional.of(new GenerationStub(new BlockPos(x0 + sizeX / 2, ground.get(), z0 + sizeZ / 2),
                pieces -> build(pieces, context, random, cellsX, levels, cellsZ, new BlockPos(x0, y0, z0))));
    }

    private void build(StructurePiecesBuilder pieces, GenerationContext context, WorldgenRandom random, int cellsX, int levels, int cellsZ,
                       BlockPos origin) {
        MazeDungeonPath path = null;
        for (int i = 0; i < ATTEMPTS; i++) {
            MazeDungeonPath p = new MazeDungeonPath(cellsX, levels, cellsZ, random);
            preset.init(p);
            p.generatePath();
            if (path == null || p.numSegments() > path.numSegments()) {
                path = p;
            }
        }
        BlockPos pivot = new BlockPos(preset.sizeXZ / 2, 0, preset.sizeXZ / 2);
        MazeDungeonPath dungeon = path;
        dungeon.generateDungeon((type, level, x, y, z, rotation) -> pieces.addPiece(new LegacyTemplatePiece(context.structureTemplateManager(),
                preset.template(type, level, dungeon.sY), cell(origin, x, y, z), counterClockwise(rotation), pivot,
                LegacyPaletteProcessor.BiomeColor.WOODLAND, 0, 0, false)));
        for (int[] cell : dungeon.spawnPositions(DungeonPreset.SPAWN_DENSITY)) {
            BlockPos at = cell(origin, cell[0], cell[1], cell[2]).offset(preset.sizeXZ / 2, DungeonPreset.SPAWN_Y_OFFSET, preset.sizeXZ / 2);
            pieces.addPiece(new DungeonSpawnerPiece(at, preset));
        }
    }

    private BlockPos cell(BlockPos origin, int x, int y, int z) {
        return origin.offset(x * preset.sizeXZ, y * preset.sizeY, z * preset.sizeXZ);
    }

    /** Techguns 2 turned segments counter-clockwise. */
    static Rotation counterClockwise(int quarterTurns) {
        return switch (quarterTurns & 3) {
            case 1 -> Rotation.COUNTERCLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.CLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    @Override
    public StructureType<?> type() {
        return TGStructures.MAZE_DUNGEON.get();
    }
}
