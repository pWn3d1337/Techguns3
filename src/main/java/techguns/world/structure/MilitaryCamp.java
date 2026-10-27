package techguns.world.structure;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import techguns.registry.TGBlocks;
import techguns.world.LegacyPaletteProcessor.BiomeColor;
import techguns.world.structure.CampBuildings.Building;

/**
 * Techguns 2 {@code MilitaryCamp}: a fenced camp split into segments by roads, recursively, with a building in each
 * segment (border and corner buildings on the outer ring), soldier spawners on the roads and an attack helicopter
 * spawner on the central flag segment. The terrain is flattened to 4 blocks of height difference and smoothed first.
 * Built into a {@link CampLevel}.
 */
final class MilitaryCamp {
    static final int OUTER_FENCE_WIDTH = 1;
    static final int OUTER_FENCE_HEIGHT = 2;
    static final int OUTER_FENCE_SPACING = 1;
    private static final int ROAD_MAX_WIDTH = 5;
    private static final int ROAD_MIN_WIDTH = 1;
    /** Below this (total) size border buildings are never used, above the next always. */
    private static final int MIN_BORDER_STRUCTURE_SIZE = 30;
    private static final int MAX_BORDER_STRUCTURE_SIZE = 80;
    private static final int MIN_CORNER_STRUCTURE_SIZE = 20;
    private static final int MAX_CORNER_STRUCTURE_SIZE = 60;
    /** Techguns 2 {@code MilitaryBaseStructure}: {@code new MilitaryCamp(4, rnd)}. */
    private static final int MAX_HEIGHT_DIFF = 4;

    static final float[][] FILTER_GAUSSIAN_5X5 = {
            {.00325f, .01375f, .0235f, .01375f, .00325f},
            {.01375f, .059f, .097f, .059f, .01375f},
            {.0235f, .097f, .159f, .097f, .0235f},
            {.01375f, .059f, .097f, .059f, .01375f},
            {.00325f, .01375f, .0235f, .01375f, .00325f}
    };

    private final CampLevel level;
    private final RandomSource rand;
    private final BiomeColor color;
    private final int posX;
    private final int posZ;
    private final int sizeX;
    private final int sizeZ;
    private final float borderChanceX;
    private final float borderChanceZ;
    private final float cornerChance;
    private final int minSubSegmentSize;
    private final int maxSubSegmentSize;
    private final double campRadiusSquared;
    private final Segment rootSegment;
    private Segment flagSegment;
    private final List<int[]> spawnPositions = new ArrayList<>();

    MilitaryCamp(CampLevel level, RandomSource rand, BiomeColor color, int posX, int posZ, int sizeX, int sizeZ) {
        this.level = level;
        this.rand = rand;
        this.color = color;
        this.posX = posX;
        this.posZ = posZ;
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
        double dx = sizeX / 2.0;
        double dz = sizeZ / 2.0;
        this.campRadiusSquared = dx * dx + dz * dz;
        int x = posX + OUTER_FENCE_WIDTH + OUTER_FENCE_SPACING;
        int z = posZ + OUTER_FENCE_WIDTH + OUTER_FENCE_SPACING;
        int roadWidth = Math.max(ROAD_MIN_WIDTH, Math.min(ROAD_MAX_WIDTH, Math.max(sizeX, sizeZ) / 8));
        this.borderChanceX = (float) (sizeX - MIN_BORDER_STRUCTURE_SIZE) / (MAX_BORDER_STRUCTURE_SIZE - MIN_BORDER_STRUCTURE_SIZE);
        this.borderChanceZ = (float) (sizeZ - MIN_BORDER_STRUCTURE_SIZE) / (MAX_BORDER_STRUCTURE_SIZE - MIN_BORDER_STRUCTURE_SIZE);
        this.cornerChance = (float) ((sizeX + sizeZ) / 2 - MIN_CORNER_STRUCTURE_SIZE) / (MAX_CORNER_STRUCTURE_SIZE - MIN_CORNER_STRUCTURE_SIZE);
        this.minSubSegmentSize = Math.min(sizeX, sizeZ) < 10 ? 5 : 7;
        this.maxSubSegmentSize = Math.min(sizeX, sizeZ) <= 45 ? 15 : Math.min(sizeX, sizeZ) / 3;
        int border = 2 * (OUTER_FENCE_WIDTH + OUTER_FENCE_SPACING);
        this.rootSegment = new Segment(x, z, sizeX - border, sizeZ - border, roadWidth, 0);
        this.rootSegment.isCenter = true;
        this.rootSegment.splitStep = 0;
        this.rootSegment.generateSubSegments();
    }

    /** Techguns 2 {@code MilitaryCamp.setBlocks} (the junk removal happens when the camp is placed). */
    void build() {
        level.flatten(posX, posZ, sizeX, sizeZ, MAX_HEIGHT_DIFF);
        level.filter(posX - 1, posZ - 1, sizeX + 2, sizeZ + 2, FILTER_GAUSSIAN_5X5);
        int minSize = (OUTER_FENCE_WIDTH + OUTER_FENCE_SPACING) * 2 + 1;
        if (sizeX < minSize || sizeZ < minSize) {
            return;
        }
        BlockState fence = Blocks.IRON_BARS.defaultBlockState();
        for (int k = 1; k < OUTER_FENCE_HEIGHT + 1; k++) {
            for (int i = 0; i < sizeX; i++) {
                level.set(posX + i, level.groundY(posX + i, posZ) + k, posZ, fence);
                level.set(posX + i, level.groundY(posX + i, posZ + sizeZ - 1) + k, posZ + sizeZ - 1, fence);
            }
            for (int j = 1; j < sizeZ - 1; j++) {
                level.set(posX, level.groundY(posX, posZ + j) + k, posZ + j, fence);
                level.set(posX + sizeX - 1, level.groundY(posX + sizeX - 1, posZ + j) + k, posZ + j, fence);
            }
        }
        rootSegment.setBlocks();
        initCampFlag();
    }

    /** The helicopter spawner on the flag segment and the soldier spawners on the roads. */
    private void initCampFlag() {
        if (flagSegment == null) {
            return;
        }
        level.flatten(flagSegment.x, flagSegment.z, flagSegment.sizeX, flagSegment.sizeZ, 0);
        int x = flagSegment.x + flagSegment.sizeX / 2;
        int z = flagSegment.z + flagSegment.sizeZ / 2;
        BlockState spawner = TGBlocks.MILITARY_SPAWN_POINT.get().defaultBlockState();
        level.spawner(x, level.groundY(x, z) + 1, z, spawner, CampLevel.Spawner.HELICOPTER);
        for (int[] pos : spawnPositions) {
            level.spawner(pos[0], pos[1], pos[2], spawner, CampLevel.Spawner.SOLDIERS);
        }
    }

    /** A road along x ({@code alignment}) or z, gravel with a concrete middle from 4 wide; a soldier spawner in its middle. */
    private void setRoadBlocks(int x, int z, int roadWidth, int roadLength, boolean alignment) {
        level.flatten(x, z, alignment ? roadLength : roadWidth, alignment ? roadWidth : roadLength, 1);
        BlockState gravel = Blocks.GRAVEL.defaultBlockState();
        BlockState concrete = TGBlocks.CONCRETE_GREY.get().defaultBlockState();
        for (int a = 0; a < roadLength; a++) {
            for (int b = 0; b < roadWidth; b++) {
                BlockState block = roadWidth >= 4 && b > 0 && b < roadWidth - 1 ? concrete : gravel;
                int bx = alignment ? x + a : x + b;
                int bz = alignment ? z + b : z + a;
                level.set(bx, level.groundY(bx, bz), bz, block);
            }
        }
        int spawnX = alignment ? x + roadLength / 2 : x + roadWidth / 2;
        int spawnZ = alignment ? z + roadWidth / 2 : z + roadLength / 2;
        spawnPositions.add(new int[] {spawnX, level.groundY(spawnX, spawnZ) + 1, spawnZ});
    }

    /** Opens the outer fence where a main road leaves the camp. */
    private void setFenceGateBlocks(int x, int z, int roadWidth, int length, int height, boolean alignment) {
        BlockState fence = Blocks.IRON_BARS.defaultBlockState();
        for (int a = 0; a < length; a++) {
            for (int b = 0; b < roadWidth; b++) {
                int bx = alignment ? x + a : x + b;
                int bz = alignment ? z + b : z + a;
                int y = level.groundY(bx, bz);
                for (int c = 0; c < height + 10; c++) {
                    if (level.get(bx, y + c + 1, bz) == fence) {
                        level.setAir(bx, y + c + 1, bz);
                    }
                }
            }
        }
    }

    private final class Segment {
        final int x;
        final int z;
        final int sizeX;
        final int sizeZ;
        final int roadWidth;
        boolean isCenter;
        Segment[] subSegments;
        /** false: split along x, true: along z. */
        boolean subAlignment;
        /** Where the building faces: 0 -x, 1 -z, 2 +x, 3 +z. */
        final int direction;
        int splitStep;

        Segment(int x, int z, int sizeX, int sizeZ, int roadWidth, int direction) {
            this.x = x;
            this.z = z;
            this.sizeX = sizeX;
            this.sizeZ = sizeZ;
            this.roadWidth = Math.max(ROAD_MIN_WIDTH, roadWidth);
            this.direction = direction;
        }

        void setBlocks() {
            if (subSegments != null && subSegments.length >= 2) {
                for (int i = 0; i < subSegments.length; i++) {
                    Segment sub = subSegments[i];
                    sub.setBlocks();
                    if (i < subSegments.length - 1) {
                        if (this == rootSegment) { // main road, through the fence
                            int extraLength = OUTER_FENCE_WIDTH + OUTER_FENCE_SPACING + 1;
                            int gateOffset = OUTER_FENCE_SPACING + OUTER_FENCE_WIDTH;
                            if (subAlignment) {
                                int roadZ = sub.z + sub.sizeZ;
                                setRoadBlocks(x - extraLength, roadZ, roadWidth, sizeX + 2 * extraLength, true);
                                setFenceGateBlocks(x - gateOffset, roadZ, roadWidth, OUTER_FENCE_WIDTH, OUTER_FENCE_HEIGHT, true);
                                setFenceGateBlocks(x + sizeX + OUTER_FENCE_SPACING, roadZ, roadWidth, OUTER_FENCE_WIDTH, OUTER_FENCE_HEIGHT, true);
                            } else {
                                int roadX = sub.x + sub.sizeX;
                                setRoadBlocks(roadX, z - extraLength, roadWidth, sizeZ + 2 * extraLength, false);
                                setFenceGateBlocks(roadX, z - gateOffset, roadWidth, OUTER_FENCE_WIDTH, OUTER_FENCE_HEIGHT, false);
                                setFenceGateBlocks(roadX, z + sizeZ + OUTER_FENCE_SPACING, roadWidth, OUTER_FENCE_WIDTH, OUTER_FENCE_HEIGHT, false);
                            }
                        } else if (subAlignment) {
                            setRoadBlocks(x - 1, sub.z + sub.sizeZ, roadWidth, sizeX + 2, true);
                        } else {
                            setRoadBlocks(sub.x + sub.sizeX, z - 1, roadWidth, sizeZ + 2, false);
                        }
                    }
                }
            } else if (isCenter && flagSegment == null) {
                flagSegment = this;
            } else {
                Building building;
                if (shouldPlaceCornerStructure()) {
                    building = matching(CampBuildings.CORNER);
                } else if (shouldPlaceBorderStructure()) {
                    building = matching(CampBuildings.BORDER);
                } else {
                    building = matching(CampBuildings.REGULAR);
                }
                int height = building.maxY == -1 || building.maxY < building.minY ? building.minY
                        : building.minY + rand.nextInt(building.maxY + 1 - building.minY);
                level.flatten(x, z, sizeX, sizeZ, 0);
                int groundY = level.groundY(x + sizeX / 2, z + sizeZ / 2);
                building.place(level, x, groundY, z, sizeX, height, sizeZ, direction, color, rand);
            }
        }

        /** A random building that fits the segment; a cobblestone plane if none does. */
        private Building matching(List<Building> buildings) {
            List<Building> matching = new ArrayList<>();
            for (Building b : buildings) {
                if (b.matches(sizeX, sizeZ)) {
                    matching.add(b);
                }
            }
            return matching.isEmpty() ? CampBuildings.EMPTY_PLANE : matching.get(rand.nextInt(matching.size()));
        }

        boolean generateSubSegments() {
            int size;
            if (sizeX >= sizeZ) {
                size = sizeX;
                subAlignment = false;
            } else {
                size = sizeZ;
                subAlignment = true;
            }
            if (size <= 0) {
                return false;
            }
            double s = Math.sqrt((double) (size - minSubSegmentSize) / (double) (maxSubSegmentSize - minSubSegmentSize));
            if (size < minSubSegmentSize || size < maxSubSegmentSize && rand.nextFloat() > s * 0.75f + getCenterFactor() * 0.25f) {
                subSegments = null;
                return false;
            }
            int count = 2;
            if (this != rootSegment && rand.nextFloat() >= 0.5) {
                count = Math.max(2, Math.round(1.0f + rand.nextFloat() * 0.5f * ((size - (minSubSegmentSize + 1) / 2.0f) / (roadWidth + 2))));
            }
            double[] parts = new double[count];
            double sum = 0;
            for (int i = 0; i < count; i++) {
                parts[i] = 1.0 + rand.nextFloat();
                sum += parts[i];
            }
            int sizeTotal = 0;
            subSegments = new Segment[count];
            for (int i = 0; i < count; i++) {
                int splitSize;
                int dir;
                if (i >= count - 1) {
                    splitSize = size - sizeTotal;
                    dir = subAlignment ? 1 : 0;
                } else {
                    splitSize = (int) Math.round((parts[i] / sum) * (size - roadWidth * count));
                    dir = subAlignment ? 3 : 2;
                }
                subSegments[i] = subAlignment ? new Segment(x, z + sizeTotal, sizeX, splitSize, roadWidth - 1, dir)
                        : new Segment(x + sizeTotal, z, splitSize, sizeZ, roadWidth - 1, dir);
                sizeTotal += splitSize + roadWidth;
                if (isCenter && splitStep <= 1 && i == (count - 1) / 2) {
                    subSegments[i].isCenter = true;
                } else if (isCenter && i == count - 1) {
                    subSegments[i].isCenter = true;
                }
                subSegments[i].splitStep = splitStep + 1;
                subSegments[i].generateSubSegments();
            }
            return true;
        }

        /** 1 in the camp centre, 0 at the corners. */
        float getCenterFactor() {
            double dx = posX + MilitaryCamp.this.sizeX / 2.0 - (x + sizeX / 2.0);
            double dz = posZ + MilitaryCamp.this.sizeZ / 2.0 - (z + sizeZ / 2.0);
            return 1.0f - (float) ((dx * dx + dz * dz) / campRadiusSquared);
        }

        boolean shouldPlaceBorderStructure() {
            int border = OUTER_FENCE_WIDTH + OUTER_FENCE_SPACING;
            boolean isBorderX = x <= posX + border || x + sizeX >= posX + MilitaryCamp.this.sizeX - border;
            boolean isBorderZ = z <= posZ + border || z + sizeZ >= posZ + MilitaryCamp.this.sizeZ - border;
            return isBorderX && borderChanceX > rand.nextFloat() || isBorderZ && borderChanceZ > rand.nextFloat();
        }

        boolean shouldPlaceCornerStructure() {
            int border = OUTER_FENCE_WIDTH + OUTER_FENCE_SPACING;
            boolean minX = x <= posX + border;
            boolean maxX = x + sizeX >= posX + MilitaryCamp.this.sizeX - border;
            boolean minZ = z <= posZ + border;
            boolean maxZ = z + sizeZ >= posZ + MilitaryCamp.this.sizeZ - border;
            return (minX || maxX) && (minZ || maxZ) && cornerChance > rand.nextFloat();
        }
    }
}
