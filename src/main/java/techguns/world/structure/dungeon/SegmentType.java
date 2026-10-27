package techguns.world.structure.dungeon;

import java.util.Locale;

/**
 * Techguns 2 {@code TemplateSegment}: the segment types of a maze dungeon template and the connection pattern each one
 * matches. Pattern indices go clockwise around the cell from the north-west corner:
 * <pre>
 *       -z
 *     0 1 2
 * -x  7 X 3  +x
 *     6 5 4
 *       +z
 * </pre>
 * A segment can be placed in {@code rotations} orientations (counter-clockwise quarter turns, like Techguns 2).
 */
public enum SegmentType {
    STRAIGHT(1, new boolean[] {false, false, false, true, false, false, false, true}, 2),
    CURVE(1, new boolean[] {false, false, false, true, false, true, false, false}, 4),
    FORK(1, new boolean[] {false, false, false, true, false, true, false, true}, 4),
    CROSS(1, new boolean[] {false, true, false, true, false, true, false, true}, 1),
    END(1, new boolean[] {false, false, false, true, false, false, false, false}, 4),
    RAMP(2, null, 0),
    ENTRANCE(1, null, 0),
    ROOM_WALL(1, new boolean[] {false, false, false, true, true, true, true, true}, 4),
    ROOM_CORNER(1, new boolean[] {false, false, false, true, true, true, false, false}, 4),
    ROOM_INNER(1, new boolean[] {true, true, true, true, true, true, true, true}, 1),
    ROOM_DOOR(1, new boolean[] {false, true, false, true, true, true, true, true}, 4),
    ROOM_DOOR_CORNER1(1, new boolean[] {false, true, false, true, true, true, false, false}, 4),
    ROOM_DOOR_CORNER2(1, new boolean[] {false, false, false, true, true, true, false, true}, 4),
    ROOM_DOOR_CORNER_DOUBLE(1, new boolean[] {false, true, false, true, true, true, false, true}, 4),
    FOUNDATION(1, null, 0),
    PILLARS(1, null, 0);

    /** Cells high (the ramp spans two). */
    public final int height;
    /** The connection pattern a path cell must have, or null for segments placed by rule, not by matching. */
    final boolean[] pattern;
    final int rotations;

    SegmentType(int height, boolean[] pattern, int rotations) {
        this.height = height;
        this.pattern = pattern;
        this.rotations = rotations;
    }

    public String fileName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
