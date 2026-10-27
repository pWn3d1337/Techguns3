package techguns.world.structure.dungeon;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

/**
 * Techguns 2 {@code MazeDungeonPath}: a maze of corridors, ramps between levels and rooms grown into a grid of
 * {@code sX * sY * sZ} cells, starting from an entrance on one side. Each filled cell gets a segment whose connection
 * pattern matches its neighbours; empty cells under the maze get foundations or pillars, the top gets a roof. Pure
 * logic: the segments come out through a {@link Placer}.
 * <p>
 * Directions are horizontal indices (0 south, 1 west, 2 north, 3 east, {@link Direction#get2DDataValue}), rotations
 * counter-clockwise quarter turns.
 */
public final class MazeDungeonPath {
    private final RandomSource rand;
    private Cell[][][] volume;
    private final List<Cell> cells = new ArrayList<>();
    private int nextRoomId = 1;
    final int sX;
    int sY;
    final int sZ;

    /** The level the entrance is on; negative counts from the top. */
    int startHeightLevel = 0;
    float chanceStraight = 0.5f;
    /** When going straight, the chance of a ramp. */
    float chanceRamp = 0.25f;
    float chanceRoom = 0.25f;
    /** The chance of another direction from a cell (not after a ramp). */
    float chanceFork = 0.2f;
    float chanceUp = 0.5f;
    int minRoomArea = 4;
    int maxRoomArea = 16;
    int minRoomWidth = 2;
    int maxRoomWidth = 5;
    boolean usePillars = true;
    boolean useFoundations = true;
    /** The top segments get the roof layer's segment of the same type above them. */
    boolean useRoof = false;
    /** Copies the lowest level one level down (one level more). */
    boolean useBottomLayer = false;
    /** The path starts with this many segments of downward ramps. */
    int entranceRampLength = 0;
    private int rampPlaced = 0;
    private int numSegments = 0;
    private int startFacing;
    private int[] startPos;
    /** Cells no segment matched (Techguns 2 printed them); none expected. */
    private int unmatched = 0;

    /** Receives the segments: type, the level its template comes from (-1: roof), the cell and the rotation. */
    public interface Placer {
        void place(SegmentType type, int level, int x, int y, int z, int rotation);
    }

    public MazeDungeonPath(int sX, int sY, int sZ, RandomSource rand) {
        this.sX = sX;
        this.sY = sY;
        this.sZ = sZ;
        this.rand = rand;
        this.volume = new Cell[sX][sY][sZ];
    }

    public int numSegments() {
        return numSegments;
    }

    /** The entrance cell (x, y, z) and the direction the path goes from it. */
    public int[] startPos() {
        return startPos.clone();
    }

    public int startFacing() {
        return startFacing;
    }

    public int unmatched() {
        return unmatched;
    }

    private static int[] vector(int dir) {
        Direction d = Direction.from2DDataValue(dir);
        return new int[] {d.getStepX(), d.getStepZ()};
    }

    private static int opposite(int dir) {
        return (dir + 2) % 4;
    }

    private static int clockwise(int dir) {
        return (dir + 1) % 4;
    }

    private static int counterClockwise(int dir) {
        return (dir + 3) % 4;
    }

    /** The pattern index of the edge towards a direction. */
    private static int edge(int dir) {
        return ((dir + 2) % 4) * 2 + 1;
    }

    public void generatePath() {
        if (useBottomLayer) {
            sY -= 1;
        }
        int startX;
        int startZ;
        int startY = (sY + startHeightLevel) % sY;
        switch (rand.nextInt(4)) {
            case 0 -> {
                startX = 0;
                startZ = sZ / 2;
                startFacing = 3;
            }
            case 1 -> {
                startX = sX - 1;
                startZ = sZ / 2;
                startFacing = 1;
            }
            case 2 -> {
                startX = sX / 2;
                startZ = 0;
                startFacing = 0;
            }
            default -> {
                startX = sX / 2;
                startZ = sZ - 1;
                startFacing = 2;
            }
        }
        entranceRampLength = Math.min(entranceRampLength, startY);
        startPos = new int[] {startX, startY, startZ};
        generateSegment(startX, startY, startZ, startFacing, null);
    }

    private void generateSegment(int x, int y, int z, int dir, Cell prev) {
        Cell segment = new Cell(x, y, z);
        if (prev != null) {
            segment.connect(opposite(dir));
            prev.connect(dir);
        } else {
            segment.isEntrance = true;
        }
        addSegment(segment);

        int maxRolls = 3;
        int roll = 0;
        boolean success = false;
        boolean canRollAgain = true;
        boolean fork = false;

        if (rampPlaced < entranceRampLength) {
            int[] offset = vector(dir);
            int[] nextPos = {x + offset[0], y, z + offset[1]};
            int[] elevPos = {x, y - 1, z};
            int[] elevNextPos = {x + offset[0], y - 1, z + offset[1]};
            if (isWithinBounds(nextPos) && isWithinBounds(elevNextPos)) {
                Cell rampDummy = new Cell(elevPos[0], elevPos[1], elevPos[2]);
                addSegment(rampDummy);
                segment.isRamp = true;
                rampDummy.isRamp = true;
                segment.elevation = -1;
                rampDummy.elevation = 1;
                rampDummy.rampRotation = opposite(dir);
                generateSegment(elevNextPos[0], elevNextPos[1], elevNextPos[2], dir, rampDummy);
                success = true;
            }
            rampPlaced++;
        }

        while (!success && roll++ < maxRolls) {
            int nextDir;
            if (segment.isEntrance) {
                nextDir = dir;
                canRollAgain = false;
            } else {
                nextDir = randomDir(dir);
            }
            int[] offset = vector(nextDir);
            int[] nextPos = {x + offset[0], y, z + offset[1]};
            if (isWithinBounds(nextPos)) {
                if (dir == nextDir && !segment.isEntrance && !fork && rand.nextFloat() < chanceRamp) {
                    int dy = rand.nextFloat() < chanceUp ? 1 : -1;
                    int[] elevPos = {x, y + dy, z};
                    if (isWithinBounds(elevPos) && !isOccupied(elevPos)) {
                        int[] elevNextPos = {x + offset[0], y + dy, z + offset[1]};
                        boolean cont = false;
                        if (!isOccupied(elevNextPos)) {
                            success = true;
                            cont = true;
                        } else {
                            Cell seg = get(elevNextPos);
                            if (seg != null && !seg.isRamp && !seg.isEntrance) {
                                seg.connect(opposite(nextDir));
                                success = true;
                            }
                        }
                        if (success) {
                            canRollAgain = false;
                            Cell rampDummy = new Cell(elevPos[0], elevPos[1], elevPos[2]);
                            addSegment(rampDummy);
                            segment.isRamp = true;
                            rampDummy.isRamp = true;
                            if (dy == 1) {
                                segment.elevation = 1;
                                rampDummy.elevation = -1;
                                segment.rampRotation = nextDir;
                            } else {
                                segment.elevation = -1;
                                rampDummy.elevation = 1;
                                rampDummy.rampRotation = opposite(nextDir);
                            }
                            if (cont) {
                                generateSegment(elevNextPos[0], elevNextPos[1], elevNextPos[2], nextDir, rampDummy);
                            }
                        }
                    }
                } else if (!isOccupied(nextPos)) {
                    if (rand.nextFloat() < chanceRoom) {
                        int area = minRoomArea + rand.nextInt(maxRoomArea - minRoomArea);
                        int width = minRoomWidth + rand.nextInt(maxRoomWidth - minRoomWidth);
                        int length = minRoomWidth + rand.nextInt(maxRoomWidth - minRoomWidth);
                        if (!tryGrowRoom(nextPos, segment, nextDir, width, length, area)) {
                            generateSegment(nextPos[0], nextPos[1], nextPos[2], nextDir, segment);
                        }
                    } else {
                        generateSegment(nextPos[0], nextPos[1], nextPos[2], nextDir, segment);
                    }
                    success = true;
                } else {
                    Cell seg = get(nextPos);
                    if (seg != null && !seg.isRamp && !seg.isEntrance) {
                        segment.connect(nextDir);
                        seg.connect(opposite(nextDir));
                        success = true;
                    }
                }
            }
            if (success && canRollAgain && rand.nextFloat() < chanceFork) {
                success = false;
                fork = true;
            }
        }
    }

    /**
     * A room of up to {@code maxWidth} cells to each side and {@code maxLength} ahead, the size with the area closest to
     * {@code preferredArea} and the squarest shape; at least 2 x 2.
     */
    private boolean tryGrowRoom(int[] pos, Cell prev, int dir, int maxWidth, int maxLength, int preferredArea) {
        int x = pos[0];
        int y = pos[1];
        int z = pos[2];
        int[] front = vector(dir);
        int[] right = vector(clockwise(dir));
        int[] left = vector(counterClockwise(dir));
        int bestL = 0;
        int bestWL = 0;
        int bestWR = 0;
        float bestValue = Float.MAX_VALUE;
        int maxWL = Integer.MAX_VALUE;
        int maxWR = Integer.MAX_VALUE;
        for (int l = 0; l < maxLength; l++) {
            int[] posF = {x + front[0] * l, y, z + front[1] * l};
            if (!isWithinBounds(posF) || isOccupied(posF)) {
                break;
            }
            int wl = 0;
            while (wl < maxWidth && wl < maxWL) {
                int[] p = {x + left[0] * (wl + 1) + front[0] * l, y, z + left[1] * (wl + 1) + front[1] * l};
                if (!isWithinBounds(p) || isOccupied(p)) {
                    maxWL = wl;
                    break;
                }
                wl++;
            }
            int wr = 0;
            while (wr < maxWidth && wr < maxWR) {
                int[] p = {x + right[0] * (wr + 1) + front[0] * l, y, z + right[1] * (wr + 1) + front[1] * l};
                if (!isWithinBounds(p) || isOccupied(p)) {
                    maxWR = wr;
                    break;
                }
                wr++;
            }
            int width = wl + wr + 1;
            int area = width * (l + 1);
            float areaRatio = (float) Math.max(area, preferredArea) / Math.min(area, preferredArea);
            float aspectRatio = (float) Math.max(width, l + 1) / Math.min(width, l + 1);
            float value = areaRatio + aspectRatio;
            if (value < bestValue) {
                bestValue = value;
                bestL = l;
                bestWL = wl;
                bestWR = wr;
            }
        }
        if (bestL < 2 || bestWL + bestWR + 1 < 2) {
            return false;
        }
        int[] p1 = {x + left[0] * bestWL, z + left[1] * bestWL};
        int[] p2 = {x + right[0] * bestWR + front[0] * bestL, z + right[1] * bestWR + front[1] * bestL};
        placeRoomSegments(Math.min(p1[0], p2[0]), Math.min(p1[1], p2[1]), Math.max(p1[0], p2[0]), Math.max(p1[1], p2[1]), y);
        prev.connect(dir);
        Cell segment = get(pos);
        if (segment != null) {
            segment.connect(opposite(dir));
        }
        return true;
    }

    private void placeRoomSegments(int minX, int minZ, int maxX, int maxZ, int y) {
        int roomId = nextRoomId++;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                Cell segment = new Cell(x, y, z);
                segment.roomId = roomId;
                addSegment(segment);
                if (x > minX) segment.pattern[7] = true;
                if (x < maxX) segment.pattern[3] = true;
                if (z > minZ) segment.pattern[1] = true;
                if (z < maxZ) segment.pattern[5] = true;
                if (x != minX && z != minZ) segment.pattern[0] = true;
                if (x != minX && z != maxZ) segment.pattern[6] = true;
                if (x != maxX && z != minZ) segment.pattern[2] = true;
                if (x != maxX && z != maxZ) segment.pattern[4] = true;
            }
        }
        // doors on the four walls: west, east, north, south
        int length = maxZ - minZ + 1;
        for (int i = 0, n = rand.nextInt(length); i < n; i++) {
            int z = minZ + rand.nextInt(length);
            placeDoor(minX, y, z, 1);
        }
        for (int i = 0, n = rand.nextInt(length); i < n; i++) {
            int z = minZ + rand.nextInt(length);
            placeDoor(maxX, y, z, 3);
        }
        length = maxX - minX + 1;
        for (int i = 0, n = rand.nextInt(length); i < n; i++) {
            int x = minX + rand.nextInt(length);
            placeDoor(x, y, minZ, 2);
        }
        for (int i = 0, n = rand.nextInt(length); i < n; i++) {
            int x = minX + rand.nextInt(length);
            placeDoor(x, y, maxZ, 0);
        }
    }

    private void placeDoor(int x, int y, int z, int dir) {
        Cell segment = get(new int[] {x, y, z});
        int[] offset = vector(dir);
        int[] nextPos = {x + offset[0], y, z + offset[1]};
        if (isWithinBounds(nextPos)) {
            if (!isOccupied(nextPos)) {
                generateSegment(nextPos[0], nextPos[1], nextPos[2], dir, segment);
            } else {
                Cell seg = get(nextPos);
                if (seg != null && !seg.isRamp && !seg.isEntrance) {
                    segment.connect(dir);
                    seg.connect(opposite(dir));
                }
            }
        }
    }

    private void addSegment(Cell seg) {
        if (seg.x >= 0 && seg.x < sX && seg.y >= 0 && seg.y < sY && seg.z >= 0 && seg.z < sZ) {
            volume[seg.x][seg.y][seg.z] = seg;
            cells.add(seg);
            numSegments++;
        }
    }

    private boolean isOccupied(int[] pos) {
        return volume[pos[0]][pos[1]][pos[2]] != null;
    }

    private Cell get(int[] pos) {
        return volume[pos[0]][pos[1]][pos[2]];
    }

    private int randomDir(int dir) {
        float f = rand.nextFloat();
        if (f < chanceStraight) {
            return dir;
        } else if (f < chanceStraight + (1.0f - chanceStraight) * 0.5f) {
            return clockwise(dir);
        }
        return counterClockwise(dir);
    }

    /** Within the grid; the top levels the entrance ramp descends from stay free once it is done. */
    private boolean isWithinBounds(int[] pos) {
        int maxY = entranceRampLength > rampPlaced ? sY : sY - entranceRampLength;
        return pos[0] >= 0 && pos[0] < sX && pos[1] >= 0 && pos[1] < maxY && pos[2] >= 0 && pos[2] < sZ;
    }

    /** Techguns 2 {@code generateDungeon}: the segment of every cell. */
    public void generateDungeon(Placer placer) {
        if (useBottomLayer) {
            cloneBottomLayer();
        }
        int startLevel = (sY + startHeightLevel) % sY;
        for (int i = 0; i < sX; i++) {
            for (int j = 0; j < sY; j++) {
                for (int k = 0; k < sZ; k++) {
                    boolean above = false;
                    for (int y = j + 1; y < sY; y++) {
                        if (volume[i][y][k] != null) {
                            above = true;
                            break;
                        }
                    }
                    boolean below = false;
                    for (int y = j - 1; y >= 0; y--) {
                        if (volume[i][y][k] != null) {
                            below = true;
                            break;
                        }
                    }
                    Cell segment = volume[i][j][k];
                    if (segment == null) {
                        if (useFoundations && above && !below) {
                            placer.place(SegmentType.FOUNDATION, j, i, j, k, 0);
                        } else if (usePillars && above) {
                            placer.place(SegmentType.PILLARS, j, i, j, k, 0);
                        }
                    } else if (segment.isRamp) {
                        if (segment.elevation == 1) {
                            placer.place(SegmentType.RAMP, segment.y, i, j, k, rampRotation(segment.rampRotation));
                        } else if (useRoof && !above && segment.y + 1 < sY) {
                            placer.place(SegmentType.END, -1, i, j + 1, k, rampRotation(volume[i][j - 1][k].rampRotation));
                        }
                    } else {
                        boolean matched = false;
                        for (SegmentType type : SegmentType.values()) {
                            if (type.pattern == null) {
                                continue;
                            }
                            int r = 0;
                            while (r < type.rotations && !segment.matches(type.pattern, r)) {
                                r++;
                            }
                            if (r < type.rotations) {
                                boolean entrance = type == SegmentType.END && j == startLevel;
                                placer.place(entrance ? SegmentType.ENTRANCE : type, segment.y, i, j, k, r);
                                if (useRoof && !above && segment.y >= startHeightLevel && segment.y + 1 < sY) {
                                    placer.place(type, -1, i, j + 1, k, r);
                                }
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            unmatched++;
                        }
                    }
                }
            }
        }
    }

    /** Techguns 2: the ramp's direction, north and south swapped, a quarter turn on. */
    private static int rampRotation(int r) {
        if (r == 0 || r == 2) {
            r = (r + 2) % 4;
        }
        return (r + 1) % 4;
    }

    /** One level more: everything shifted up, the lowest level copied below it. */
    private void cloneBottomLayer() {
        Cell[][][] shifted = new Cell[sX][sY + 1][sZ];
        for (int i = 0; i < sX; i++) {
            for (int j = 0; j < sY; j++) {
                for (int k = 0; k < sZ; k++) {
                    Cell seg = volume[i][j][k];
                    if (seg != null) {
                        shifted[i][j + 1][k] = seg;
                        seg.y++;
                        if (j == 0) {
                            Cell copy = new Cell(i, j, k);
                            copy.pattern = seg.pattern;
                            shifted[i][j][k] = copy;
                        }
                    }
                }
            }
        }
        volume = shifted;
        sY++;
        startPos[1]++;
    }

    /**
     * Techguns 2 {@code getSpawnPositions}: {@code density} spawners per cell on random corridor and room cells (not
     * the entrance or ramps), cells (x, y, z).
     */
    public List<int[]> spawnPositions(float density) {
        int numSpawns = (int) (numSegments * density);
        List<Cell> spawners = new ArrayList<>();
        int tries = 0;
        while (numSpawns > 0 && tries < 5) {
            Cell seg = cells.get(rand.nextInt(cells.size()));
            if (!spawners.contains(seg) && !seg.isEntrance && !seg.isRamp) {
                spawners.add(seg);
                numSpawns--;
                tries = 0;
            } else {
                tries++;
            }
        }
        List<int[]> positions = new ArrayList<>();
        for (Cell seg : spawners) {
            positions.add(new int[] {seg.x, seg.y, seg.z});
        }
        return positions;
    }

    private static final class Cell {
        final int x;
        int y;
        final int z;
        boolean isEntrance;
        /** Ramps don't connect to anything else. */
        boolean isRamp;
        /** 1: the lower cell of a ramp, -1: the upper one. */
        int elevation;
        int rampRotation = -1;
        int roomId = -1;
        boolean[] pattern = new boolean[8];

        Cell(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        void connect(int dir) {
            pattern[edge(dir)] = true;
        }

        /** The pattern equals the template's turned {@code rotations} counter-clockwise quarter turns. */
        boolean matches(boolean[] template, int rotations) {
            int offset = rotations * 2;
            for (int i = 0; i < 8; i++) {
                if (pattern[i] != template[(i + offset) % 8]) {
                    return false;
                }
            }
            return true;
        }
    }
}
