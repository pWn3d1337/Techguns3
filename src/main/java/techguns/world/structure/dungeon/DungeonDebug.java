package techguns.world.structure.dungeon;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import techguns.TGIds;
import techguns.Techguns;
import techguns.world.structure.DebugStructures;
import techguns.world.structure.StructureScans;

/**
 * The Techguns 2 dungeon scanner and generator: a dungeon template is one structure template per segment type,
 * {@code techguns:dungeon/<template>/<segment>}. The scanner lays the segments out in the Techguns 2 grid next to it
 * ({@link #segmentOrigin}), scans them into templates and loads them back; the generator builds a maze dungeon from
 * a template (or the Nether preset, like Techguns 2).
 */
public final class DungeonDebug {
    /** Blocks between the scanner and its segments and between two segments (Techguns 2 {@code SPACING}). */
    public static final int SPACING = 1;
    /** Techguns 2 {@code TemplateSegment} grid: row and column per segment type. */
    private static final Map<SegmentType, int[]> LAYOUT = Map.ofEntries(
            Map.entry(SegmentType.STRAIGHT, new int[] {0, 0}), Map.entry(SegmentType.CURVE, new int[] {0, 1}),
            Map.entry(SegmentType.FORK, new int[] {0, 2}), Map.entry(SegmentType.CROSS, new int[] {0, 3}), Map.entry(SegmentType.END, new int[] {0, 4}),
            Map.entry(SegmentType.RAMP, new int[] {1, 0}), Map.entry(SegmentType.ENTRANCE, new int[] {1, 1}),
            Map.entry(SegmentType.ROOM_WALL, new int[] {2, 0}), Map.entry(SegmentType.ROOM_CORNER, new int[] {2, 1}),
            Map.entry(SegmentType.ROOM_INNER, new int[] {2, 2}), Map.entry(SegmentType.ROOM_DOOR, new int[] {2, 3}),
            Map.entry(SegmentType.ROOM_DOOR_CORNER1, new int[] {2, 4}), Map.entry(SegmentType.ROOM_DOOR_CORNER2, new int[] {2, 5}),
            Map.entry(SegmentType.ROOM_DOOR_CORNER_DOUBLE, new int[] {2, 6}), Map.entry(SegmentType.FOUNDATION, new int[] {3, 0}),
            Map.entry(SegmentType.PILLARS, new int[] {3, 1}));
    private static final String DUNGEON_DIR = "dungeon/";

    private DungeonDebug() {
    }

    /** The scanner's corner of segment {@code type}: east by column, south by row. */
    public static BlockPos segmentOrigin(BlockPos scanner, SegmentType type, int sizeXZ) {
        int[] cell = LAYOUT.get(type);
        return scanner.offset(SPACING + cell[1] * (sizeXZ + SPACING), 0, SPACING + cell[0] * (sizeXZ + SPACING));
    }

    /** Techguns 2 {@code getMaxExtents}: the area the scanner's segments take. */
    public static Vec3i extents(int sizeXZ, int sizeY) {
        int cols = LAYOUT.values().stream().mapToInt(cell -> cell[1]).max().orElse(0) + 1;
        int height = java.util.Arrays.stream(SegmentType.values()).mapToInt(type -> type.height).max().orElse(1);
        return new Vec3i(cols * (sizeXZ + SPACING), sizeY * height, cols * (sizeXZ + SPACING));
    }

    public static ResourceLocation template(String name, SegmentType type) {
        return TGIds.id(DUNGEON_DIR + name + "/" + type.fileName());
    }

    /** Saves every segment as {@code techguns:dungeon/<name>/<segment>}; the number of segments saved. */
    public static int scan(ServerLevel level, BlockPos scanner, int sizeXZ, int sizeY, String name) {
        int saved = 0;
        for (SegmentType type : SegmentType.values()) {
            Vec3i size = new Vec3i(sizeXZ, sizeY * type.height, sizeXZ);
            if (StructureScans.save(level, segmentOrigin(scanner, type, sizeXZ), size, template(name, type))) {
                saved++;
            }
        }
        return saved;
    }

    /** Places the segments of template {@code name} in the scanner's grid; the number placed. */
    public static int load(ServerLevel level, BlockPos scanner, int sizeXZ, String name) {
        int placed = 0;
        for (SegmentType type : SegmentType.values()) {
            if (StructureScans.place(level, segmentOrigin(scanner, type, sizeXZ), template(name, type))) {
                placed++;
            }
        }
        return placed;
    }

    /** Techguns 2 {@code clearBlocks}: air over the scanner's whole segment area. */
    public static void clear(ServerLevel level, BlockPos scanner, int sizeXZ, int sizeY) {
        Vec3i max = extents(sizeXZ, sizeY);
        for (BlockPos pos : BlockPos.betweenClosed(scanner.offset(SPACING, 0, SPACING), scanner.offset(max.getX() - 1, max.getY() - 1, max.getZ() - 1))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Techguns 2 {@code copyFirstSegment}: the straight segment copied into every other segment's place. */
    public static void copyFirst(ServerLevel level, BlockPos scanner, int sizeXZ, int sizeY) {
        StructureTemplate straight = new StructureTemplate();
        straight.fillFromWorld(level, segmentOrigin(scanner, SegmentType.STRAIGHT, sizeXZ), new Vec3i(sizeXZ, sizeY, sizeXZ), false, null);
        for (SegmentType type : SegmentType.values()) {
            if (type != SegmentType.STRAIGHT) {
                BlockPos at = segmentOrigin(scanner, type, sizeXZ);
                straight.placeInWorld(level, at, at, new StructurePlaceSettings(), level.random, Block.UPDATE_CLIENTS);
            }
        }
    }

    /** The dungeon template names (resources and scanned ones), for the screens. */
    public static List<String> templateNames(ServerLevel level) {
        TreeSet<String> names = new TreeSet<>();
        level.getStructureManager().listTemplates().filter(id -> id.getNamespace().equals(Techguns.MODID) && id.getPath().startsWith(DUNGEON_DIR))
                .forEach(id -> {
                    String rest = id.getPath().substring(DUNGEON_DIR.length());
                    if (rest.indexOf('/') > 0) {
                        names.add(rest.substring(0, rest.indexOf('/')));
                    }
                });
        return List.copyOf(names);
    }

    /**
     * Techguns 2 {@code DungeonGeneratorTileEnt.generateDungeon}: a maze dungeon in the box from {@code origin}, built
     * from template {@code name} on every level (default path settings, castle spawners), or from the Nether preset
     * when the name is empty or unknown. Returns the template used.
     */
    public static String generate(ServerLevel level, BlockPos origin, int sizeX, int sizeY, int sizeZ, @Nullable String name, RandomSource random) {
        return generate(level, origin, sizeX, sizeY, sizeZ, name, DungeonPreset.NETHER, random);
    }

    /** A maze dungeon of {@code preset} (its templates and path settings) in the box from {@code origin}. */
    public static void generate(ServerLevel level, BlockPos origin, int sizeX, int sizeY, int sizeZ, DungeonPreset preset, RandomSource random) {
        generate(level, origin, sizeX, sizeY, sizeZ, null, preset, random);
    }

    private static String generate(ServerLevel level, BlockPos origin, int sizeX, int sizeY, int sizeZ, @Nullable String name, DungeonPreset preset,
                                   RandomSource random) {
        Optional<StructureTemplate> straight = name == null || name.isEmpty() ? Optional.empty()
                : level.getStructureManager().get(template(name, SegmentType.STRAIGHT));
        int sizeXZ = straight.map(t -> t.getSize().getX()).orElse(preset.sizeXZ);
        int cellY = straight.map(t -> t.getSize().getY()).orElse(preset.sizeY);
        int cellsX = Math.max(1, sizeX / sizeXZ);
        int levels = Math.max(1, sizeY / cellY);
        int cellsZ = Math.max(1, sizeZ / sizeXZ);
        MazeDungeonPath path = MazeDungeonStructure.bestPath(random, cellsX, levels, cellsZ, straight.isPresent() ? null : preset);
        MazeDungeonStructure.Templates templates = straight.isPresent() ? (type, lvl, all) -> template(name, type) : preset::template;
        MazeDungeonStructure.addPieces(piece -> DebugStructures.place(level, piece, random), level.getStructureManager(), path, origin, sizeXZ, cellY,
                templates, straight.isPresent() ? DungeonPreset.CASTLE : preset);
        return straight.isPresent() ? name : preset.getSerializedName();
    }
}
