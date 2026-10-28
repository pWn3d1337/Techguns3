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
import techguns.TGIds;
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
    /** Underground: the lowest level stays this far above the world's floor (the Nether bedrock). */
    private static final int MIN_FLOOR_OFFSET = 6;
    /** Underground: the entrance and its ramp take 4 levels, the maze needs at least one more. */
    private static final int MIN_UNDERGROUND_LEVELS = 5;
    /** The entrance hut's Nether foundation (Techguns 2 {@code placeFoundationNether}). */
    private static final int ENTRANCE_FOUNDATION = 16;

    private final DungeonPreset preset;

    public MazeDungeonStructure(StructureSettings settings, DungeonPreset preset) {
        super(settings);
        this.preset = preset;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (preset.underground) {
            return findUnderground(context);
        }
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

    /**
     * Techguns 2 test tool for the Nether dungeon (never spawned in Techguns 2): 24 to 48 blocks of levels under a cave
     * floor, the top level holding only the entrance, whose hut stands on the floor. The floor is checked around the
     * entrance; the bottom stays 6 above the world's floor (fewer levels on low floors, none below 5).
     */
    private Optional<GenerationStub> findUnderground(GenerationContext context) {
        WorldgenRandom random = context.random();
        int x0 = context.chunkPos().getMinBlockX();
        int z0 = context.chunkPos().getMinBlockZ();
        Optional<Integer> estimate = LegacyTemplateStructure.caveHeight(context, x0, z0);
        if (estimate.isEmpty()) {
            return Optional.empty();
        }
        int bottom = context.heightAccessor().getMinBuildHeight() + MIN_FLOOR_OFFSET;
        int levels = Math.min((24 + random.nextInt(25)) / preset.sizeY, (estimate.get() - bottom) / preset.sizeY + 1);
        if (levels < MIN_UNDERGROUND_LEVELS) {
            return Optional.empty();
        }
        int cellsX = (32 + random.nextInt(16)) / preset.sizeXZ;
        int cellsZ = (32 + random.nextInt(16)) / preset.sizeXZ;
        MazeDungeonPath path = bestPath(random, cellsX, levels, cellsZ);
        int[] start = path.startPos();
        int entranceX = x0 + start[0] * preset.sizeXZ + preset.sizeXZ / 2;
        int entranceZ = z0 + start[2] * preset.sizeXZ + preset.sizeXZ / 2;
        Optional<Integer> floor = LegacyTemplateStructure.caveHeight(context, entranceX - 8, entranceZ - 8);
        if (floor.isEmpty() || floor.get() - start[1] * preset.sizeY < bottom) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x0, floor.get() - start[1] * preset.sizeY, z0);
        return Optional.of(new GenerationStub(new BlockPos(entranceX, floor.get(), entranceZ), pieces -> {
            place(pieces, context, path, origin);
            // Techguns 2 NetherDungeonEntrance on the start cell: one block out of the cell, turned about (3, 3)
            BlockPos cell = cell(origin, start[0], start[1], start[2]);
            pieces.addPiece(new LegacyTemplatePiece(context.structureTemplateManager(), TGIds.id("nether_dungeon_entrance"), cell.offset(-1, 0, -1),
                    counterClockwise(legacyRotation(path.startFacing())), new BlockPos(3, 0, 3), LegacyPaletteProcessor.BiomeColor.WOODLAND,
                    ENTRANCE_FOUNDATION, 0, true));
        }));
    }

    /** Techguns 2 {@code facingToRotation} of the entrance direction (west 0, south 1, east 2, north 3). */
    static int legacyRotation(int direction2d) {
        return switch (direction2d) {
            case 0 -> 1;
            case 1 -> 0;
            case 3 -> 2;
            default -> 3;
        };
    }

    private void build(StructurePiecesBuilder pieces, GenerationContext context, WorldgenRandom random, int cellsX, int levels, int cellsZ,
                       BlockPos origin) {
        place(pieces, context, bestPath(random, cellsX, levels, cellsZ), origin);
    }

    /** Techguns 2 {@code Dungeon.generate}: the path with the most cells of a few attempts. */
    private MazeDungeonPath bestPath(WorldgenRandom random, int cellsX, int levels, int cellsZ) {
        MazeDungeonPath path = null;
        for (int i = 0; i < ATTEMPTS; i++) {
            MazeDungeonPath p = new MazeDungeonPath(cellsX, levels, cellsZ, random);
            preset.init(p);
            p.generatePath();
            if (path == null || p.numSegments() > path.numSegments()) {
                path = p;
            }
        }
        return path;
    }

    private void place(StructurePiecesBuilder pieces, GenerationContext context, MazeDungeonPath dungeon, BlockPos origin) {
        BlockPos pivot = new BlockPos(preset.sizeXZ / 2, 0, preset.sizeXZ / 2);
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
