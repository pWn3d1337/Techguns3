package techguns.world.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import techguns.block.TGLampBlock;
import techguns.registry.TGBlocks;

/**
 * Techguns 2 {@code world/structures/city} (new: Techguns 2 only built it with a test tool): a city block split
 * recursively into segments with roads between them (wider near the root), each leaf segment holding a building of
 * stacked floors with windows, a door to its side, stairs or ladders between the floors, ceiling lamps and a roof
 * hatch. Built into a {@link CampLevel}. Techguns 2 drew the walls from Chisel blocks; its {@code MBlockRegister}
 * fallbacks (vanilla stone, bricks, quartz, sandstone) are used.
 * <p>
 * Changes: building lots and roads level the ground to the city height (Techguns 2 flattened lots to their median and
 * left roads floating), doors stand in their wall (Techguns 2 turned them a quarter across), the ladders of small
 * buildings get their wall blocks at each step (Techguns 2 used the floor index), and the style variations come from
 * the city's random (Techguns 2 used an unseeded one).
 */
final class CityGenerator {
    /** Techguns 2 {@code GenericCity.SET_AIR_HEIGHT}. */
    private static final int SET_AIR_HEIGHT = 3;
    /** Techguns 2 {@code testCity}: road widths per split step. */
    private static final int[] ROAD_WIDTH = {6, 4, 4, 2, 1};
    /** Techguns 2 {@code testCity}: half the border segments past the second split stay empty. */
    private static final float FUZZY_BORDER = 0.5f;

    private final CampLevel level;
    private final RandomSource rand;
    private int minSubSegmentSize;
    private int maxSubSegmentSize;

    CityGenerator(CampLevel level, RandomSource rand) {
        this.level = level;
        this.rand = rand;
    }

    /**
     * Techguns 2 {@code CityStructure.spawnStructureWorldgen} and {@code GenericCity.createCity}: the area flattened to
     * 1 and smoothed, the segments split, smoothed again, then roads and buildings.
     */
    void build(int posX, int posY, int posZ, int sizeX, int sizeZ) {
        level.flatten(posX - 1, posZ - 1, sizeX + 2, sizeZ + 2, 1);
        level.filter(posX - 4, posZ - 4, sizeX + 4, sizeZ + 4, MilitaryCamp.FILTER_GAUSSIAN_5X5);
        minSubSegmentSize = Math.min(sizeX, sizeZ) < 14 ? 7 : 10;
        maxSubSegmentSize = Math.min(sizeX, sizeZ) <= 45 ? 15 : Math.min(sizeX, sizeZ) / 3;
        Segment root = new Segment(posX, posY, posZ, sizeX, sizeZ, ROAD_WIDTH[0], 0, null);
        root.isCenter = true;
        if (root.split()) {
            level.filter(posX - 1, posZ - 1, sizeX + 2, sizeZ + 2, MilitaryCamp.FILTER_GAUSSIAN_5X5);
            root.place();
        }
    }

    /** Techguns 2 {@code GenericCitySegment}. */
    private final class Segment {
        final int x;
        final int y;
        final int z;
        final int sizeX;
        final int sizeZ;
        final int roadWidth;
        /** Techguns 2: 0 north, 1 east, 2 south, 3 west; the wall the building's door is in. */
        final int direction;
        final Segment root;
        final float rootRadiusSquared;
        boolean isCenter;
        int splitStep;
        /** false: split along x, true: along z. */
        boolean subAlignment;
        List<Segment> subSegments;

        Segment(int x, int y, int z, int sizeX, int sizeZ, int roadWidth, int direction, Segment root) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.sizeX = sizeX;
            this.sizeZ = sizeZ;
            this.roadWidth = roadWidth;
            this.direction = direction;
            this.root = root == null ? this : root;
            this.rootRadiusSquared = sizeX * sizeX / 4.0f + sizeZ * sizeZ / 4.0f;
        }

        /** Techguns 2 {@code generateSubSegments}: false if the segment stays whole. */
        boolean split() {
            int size;
            int size2;
            if (sizeX >= sizeZ) {
                size = sizeX;
                size2 = sizeZ;
                subAlignment = false;
            } else {
                size = sizeZ;
                size2 = sizeX;
                subAlignment = true;
            }
            if (size <= 0) {
                return false;
            }
            double s = Math.sqrt((double) (size - minSubSegmentSize) / (maxSubSegmentSize - minSubSegmentSize));
            if (size2 < minSubSegmentSize || size < maxSubSegmentSize && rand.nextFloat() > s * 0.75f + centerFactor() * 0.25f) {
                subSegments = null;
                return false;
            }
            int count = 2;
            if (this != root && rand.nextFloat() >= 0.5f) {
                count = Math.max(2, Math.round(1.0f + rand.nextFloat() * 0.5f * ((size - (minSubSegmentSize + 1) / 2.0f) / (roadWidth + 2))));
            }
            double[] parts = new double[count];
            double sum = 0;
            for (int i = 0; i < count; i++) {
                parts[i] = 1.0 + rand.nextFloat();
                sum += parts[i];
            }
            int sizeTotal = 0;
            subSegments = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                int splitSize;
                int dir;
                if (i >= count - 1) {
                    splitSize = size - sizeTotal;
                    dir = subAlignment ? 1 : 0;
                } else {
                    splitSize = (int) Math.round(parts[i] / sum * (size - roadWidth * count));
                    dir = subAlignment ? 3 : 2;
                }
                int nextRoadWidth = ROAD_WIDTH[Math.min(ROAD_WIDTH.length - 1, splitStep + 1)];
                Segment sub = subAlignment ? new Segment(x, y, z + sizeTotal, sizeX, splitSize, nextRoadWidth, dir, root)
                        : new Segment(x + sizeTotal, y, z, splitSize, sizeZ, nextRoadWidth, dir, root);
                sizeTotal += splitSize + roadWidth;
                if (isCenter && splitStep <= 1 && i == (count - 1) / 2 || isCenter && i == count - 1) {
                    sub.isCenter = true;
                }
                sub.splitStep = splitStep + 1;
                subSegments.add(sub);
                sub.split();
            }
            return true;
        }

        float centerFactor() {
            float dx = (root.x + root.sizeX / 2.0f) - (x + sizeX / 2.0f);
            float dz = (root.z + root.sizeZ / 2.0f) - (z + sizeZ / 2.0f);
            return 1.0f - (dx * dx + dz * dz) / root.rootRadiusSquared;
        }

        boolean isBorderSegment() {
            return x <= root.x || x + sizeX >= root.x + root.sizeX || z <= root.z || z + sizeZ >= root.z + root.sizeZ;
        }

        /** Techguns 2 {@code GenericCitySegment.setBlocks}: the sub segments with a road after each but the last. */
        void place() {
            if (subSegments == null || subSegments.size() < 2) {
                placeStructure(this);
                return;
            }
            for (int i = 0; i < subSegments.size(); i++) {
                Segment sub = subSegments.get(i);
                sub.place();
                if (i < subSegments.size() - 1) {
                    if (subAlignment) {
                        int y1 = level.groundY(x, sub.z + sub.sizeZ);
                        int y2 = level.groundY(x + sizeX, sub.z + sub.sizeZ + roadWidth);
                        road(x, y, sub.z + sub.sizeZ, roadWidth, sizeX, y2 - y1, true);
                    } else {
                        int y1 = level.groundY(sub.x + sub.sizeX, z);
                        int y2 = level.groundY(sub.x + sub.sizeX + roadWidth, z + sizeZ);
                        road(sub.x + sub.sizeX, y, z, roadWidth, sizeZ, y2 - y1, false);
                    }
                }
            }
        }
    }

    /**
     * Techguns 2 {@code CityRoad.setRoadBlocks}: dark concrete inside, grey concrete edges (all grey when narrower than
     * 4), two blocks thick, sloping by {@code heightDif} along its length, air above; one block longer at both ends.
     */
    private void road(int x, int y, int z, int roadWidth, int roadLength, int heightDif, boolean alongX) {
        BlockState main = TGBlocks.CONCRETE_GREY_DARK.get().defaultBlockState();
        BlockState side = TGBlocks.CONCRETE_GREY.get().defaultBlockState();
        float h = heightDif != 0 ? (float) roadLength / heightDif : 0;
        int c = 0;
        for (int a = -1; a < roadLength + 2; a++) {
            if (h != 0 && a == (int) ((c + 1) * h) && c < heightDif) {
                c++;
            }
            for (int b = 0; b < roadWidth; b++) {
                BlockState block = roadWidth >= 4 && b > 0 && b < roadWidth - 1 ? main : side;
                int px = alongX ? x + a : x + b;
                int pz = alongX ? z + b : z + a;
                level.setGroundY(px, pz, y + c);
                for (int i = -1; i < 1; i++) {
                    if (level.get(px, y + c + i, pz) != main) {
                        level.set(px, y + c + i, pz, block);
                    }
                }
                for (int i = 1; i < (alongX ? 4 : SET_AIR_HEIGHT); i++) {
                    level.setAir(px, y + c + i, pz);
                }
            }
        }
    }

    /** Techguns 2 {@code GenericCity.placeStructure}: a matching building (or a cobblestone lot) on the segment. */
    private void placeStructure(Segment segment) {
        if (segment.isBorderSegment() && segment.splitStep > 2 && rand.nextFloat() < FUZZY_BORDER) {
            return;
        }
        level.fill(segment.x, segment.y + 1, segment.z, segment.sizeX, SET_AIR_HEIGHT, segment.sizeZ, Blocks.AIR.defaultBlockState());
        List<BuildingType> matching = new ArrayList<>();
        for (BuildingType type : BuildingType.values()) {
            if (type.fits(segment.sizeX, segment.sizeZ)) {
                matching.add(type);
            }
        }
        for (int i = 0; i < segment.sizeX; i++) {
            for (int j = 0; j < segment.sizeZ; j++) {
                level.setGroundY(segment.x + i, segment.z + j, segment.y - 1);
            }
        }
        if (matching.isEmpty()) {
            // Techguns 2 EmptyPlane
            level.fill(segment.x, segment.y, segment.z, segment.sizeX, 1, segment.sizeZ, Blocks.COBBLESTONE.defaultBlockState());
            return;
        }
        BuildingType type = matching.get(rand.nextInt(matching.size()));
        int height = type.minY + rand.nextInt(type.maxY + 1 - type.minY);
        new Building(type).place(segment.x, segment.y, segment.z, segment.sizeX, height, segment.sizeZ, segment.direction);
    }

    /** Techguns 2 {@code BuildingStyle}: block options for walls (0), windows (1), floors (2), borders (3), door decoration (4). */
    private enum Style {
        STONE(stairs(Blocks.OAK_STAIRS, Blocks.DARK_OAK_PLANKS),
                of(Blocks.STONE, Blocks.STONE_BRICKS, Blocks.COBBLESTONE, Blocks.COBBLESTONE, Blocks.COBBLESTONE, Blocks.POLISHED_ANDESITE),
                of(Blocks.GLASS_PANE),
                of(Blocks.POLISHED_ANDESITE, Blocks.STONE_BRICKS, Blocks.POLISHED_ANDESITE, Blocks.DIORITE, Blocks.POLISHED_GRANITE),
                of(Blocks.POLISHED_ANDESITE, Blocks.POLISHED_ANDESITE, Blocks.STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.POLISHED_ANDESITE)),
        FACTORY(stairs(Blocks.OAK_STAIRS, Blocks.DARK_OAK_PLANKS), of(Blocks.BRICKS), of(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE),
                List.of(TGBlocks.CONCRETE_GREY_DARK)),
        BRICK(stairs(Blocks.OAK_STAIRS, Blocks.DARK_OAK_PLANKS), of(Blocks.BRICKS), of(Blocks.GLASS_PANE),
                of(Blocks.POLISHED_ANDESITE, Blocks.STONE_BRICKS, Blocks.POLISHED_ANDESITE, Blocks.DIORITE, Blocks.POLISHED_GRANITE), of(Blocks.BRICKS)),
        LAB(stairs(Blocks.QUARTZ_STAIRS, Blocks.QUARTZ_BLOCK), of(Blocks.QUARTZ_BLOCK), of(Blocks.WHITE_STAINED_GLASS_PANE), of(Blocks.POLISHED_ANDESITE),
                of(Blocks.QUARTZ_PILLAR), of(Blocks.CHISELED_QUARTZ_BLOCK)),
        LIME(stairs(Blocks.OAK_STAIRS, Blocks.DARK_OAK_PLANKS), of(Blocks.CUT_SANDSTONE), of(Blocks.GLASS_PANE), of(Blocks.SANDSTONE),
                of(Blocks.CHISELED_SANDSTONE), of(Blocks.CHISELED_SANDSTONE));

        final Block[] stairs;
        /** Techguns 2 {@code MBlockRegister} fallbacks of its Chisel blocks; suppliers, as the Techguns blocks register later. */
        final List<List<Supplier<? extends Block>>> blocks;

        @SafeVarargs
        Style(Block[] stairs, List<? extends Supplier<? extends Block>>... blocks) {
            this.stairs = stairs;
            List<List<Supplier<? extends Block>>> lists = new ArrayList<>();
            for (List<? extends Supplier<? extends Block>> options : blocks) {
                lists.add(List.copyOf(options));
            }
            this.blocks = List.copyOf(lists);
        }

        private static Block[] stairs(Block stairs, Block support) {
            return new Block[] {stairs, support};
        }

        private static List<Supplier<Block>> of(Block... blocks) {
            List<Supplier<Block>> out = new ArrayList<>();
            for (Block block : blocks) {
                out.add(() -> block);
            }
            return out;
        }
    }

    /**
     * Techguns 2 {@code CityBuildingType} building1 to building6 and their {@code CityBuilding} size limits: wall
     * segment width and floor height, wall and door patterns (top row first, -1 air), door width and offset.
     */
    private enum BuildingType {
        STONE_SMALL(Style.STONE, 2, 5, 3, 5, true, new int[][] {{0, 0}, {0, 1}, {0, 1}, {0, 0}, {3, 3}},
                new int[][] {{0, 0}, {0, 0}, {0, -1}, {0, -1}, {3, 3}}, 1, 1, true, 3),
        FACTORY(Style.FACTORY, 3, 4, 4, 4, false, new int[][] {{0, 0, 0}, {0, 1, 1}, {0, 0, 0}, {2, 2, 2}},
                new int[][] {{0, 0, 0}, {0, -1, -1}, {0, -1, -1}, {2, 2, 2}}, 2, 1, false, 0),
        STONE(Style.STONE, 3, 4, 4, 4, false, new int[][] {{0, 0, 0}, {0, 1, 1}, {0, 0, 0}, {3, 3, 3}},
                new int[][] {{0, 0, 0}, {0, -1, -1}, {0, -1, -1}, {3, 3, 3}}, 2, 1, true, 3),
        BRICK(Style.BRICK, 3, 4, 4, 4, false, new int[][] {{0, 0, 0}, {0, 1, 1}, {0, 0, 0}, {3, 3, 3}},
                new int[][] {{0, 0, 0}, {0, -1, -1}, {0, -1, -1}, {3, 3, 3}}, 2, 1, true, 3),
        LAB(Style.LAB, 3, 5, 4, 4, false, new int[][] {{0, 0, 0}, {0, 1, 1}, {0, 1, 1}, {0, 0, 0}, {3, 3, 3}},
                new int[][] {{0, 0, 0}, {0, 4, 4}, {0, -1, -1}, {0, -1, -1}, {3, 3, 3}}, 2, 1, true, 3),
        LIME(Style.LIME, 3, 4, 4, 4, false, new int[][] {{0, 0, 0}, {0, 1, 1}, {0, 0, 0}, {3, 3, 3}},
                new int[][] {{0, 0, 0}, {0, -1, -1}, {0, -1, -1}, {3, 3, 3}}, 2, 1, true, 3);

        final Style style;
        final int wallX;
        final int wallY;
        final int minX;
        final int minZ;
        final boolean swapXZ;
        final int[][] wallPattern;
        final int[][] doorPattern;
        final int doorWidth;
        final int doorOffset;
        final boolean roofLikeFloor;
        final int roofBorderIndex;
        /** Techguns 2 {@code CityBuilding}: 5 to 16 blocks high. */
        final int minY = 5;
        final int maxY = 16;

        BuildingType(Style style, int wallX, int wallY, int minX, int minZ, boolean swapXZ, int[][] wallPattern, int[][] doorPattern, int doorWidth,
                     int doorOffset, boolean roofLikeFloor, int roofBorderIndex) {
            this.style = style;
            this.wallX = wallX;
            this.wallY = wallY;
            this.minX = minX;
            this.minZ = minZ;
            this.swapXZ = swapXZ;
            this.wallPattern = wallPattern;
            this.doorPattern = doorPattern;
            this.doorWidth = doorWidth;
            this.doorOffset = doorOffset;
            this.roofLikeFloor = roofLikeFloor;
            this.roofBorderIndex = roofBorderIndex;
        }

        /** Techguns 2 {@code getMatchingStructure} (no upper limits). */
        boolean fits(int sizeX, int sizeZ) {
            return sizeX >= minX && sizeZ >= minZ || swapXZ && sizeZ >= minX && sizeX >= minZ;
        }
    }

    /** One building of a type with its rolled block variation. */
    private final class Building {
        private static final int WALL_EXTRA_LENGTH = 1;
        private static final int FLOOR_HEIGHT = 1;
        private static final int LAMP_DENSITY = 3;
        final BuildingType type;
        final List<BlockState> blocks = new ArrayList<>();
        final Block stairs;
        final Block stairSupport;

        Building(BuildingType type) {
            this.type = type;
            for (List<Supplier<? extends Block>> options : type.style.blocks) {
                blocks.add(options.get(rand.nextInt(options.size())).get().defaultBlockState());
            }
            this.stairs = type.style.stairs[0];
            this.stairSupport = type.style.stairs[1];
        }

        private BlockState block(int index) {
            BlockState state = blocks.get(index);
            return state.is(Blocks.QUARTZ_PILLAR) ? state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y) : state;
        }

        /** Techguns 2 {@code CityBuildingType.setBlocks}. */
        void place(int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction) {
            int wallX = type.wallX;
            int wallY = type.wallY;
            int stairsDir = direction;
            if ((direction == 0 || direction == 2) && sizeX - 2 < wallY + 1 || (direction == 1 || direction == 3) && sizeZ - 2 < wallY + 1) {
                stairsDir = (stairsDir + 1) % 4;
            }
            int segX = (sizeX - WALL_EXTRA_LENGTH) / wallX;
            int sx = segX * wallX + WALL_EXTRA_LENGTH;
            int segZ = (sizeZ - WALL_EXTRA_LENGTH) / wallX;
            int sz = segZ * wallX + WALL_EXTRA_LENGTH;
            int segY = sizeY / wallY;
            boolean isSmall = Math.max(sx - 2, sz - 2) < wallY + 1 || Math.min(sx, sz) < 4;

            for (int i = 0; i < segY; i++) {
                int y = posY + i * wallY;
                floor(posX + 1, y, posZ + 1, sx - 2, sz - 2);
                if (i >= 1) {
                    lamps(posX + 1, y - 1, posZ + 1, sx - 2, sz - 2);
                }
                wall(posX, y, posZ, segX, 1, 0, 0, i == 0 && direction == 0);
                wall(posX + sx - 1, y, posZ, segZ, 0, 1, 1, i == 0 && direction == 1);
                wall(posX + sx - 1, y, posZ + sz - 1, segX, -1, 0, 2, i == 0 && direction == 2);
                wall(posX, y, posZ + sz - 1, segZ, 0, -1, 3, i == 0 && direction == 3);
            }
            int y2 = posY + segY * wallY;
            if (type.roofLikeFloor) {
                floor(posX + 1, y2, posZ + 1, sx - 2, sz - 2);
            } else {
                floor(posX, y2, posZ, sx, sz);
            }
            lamps(posX + 1, y2 - 1, posZ + 1, sx - 2, sz - 2);
            BlockState wallBlock = block(0);
            for (int i = 0; i < sx; i++) {
                for (int j = 0; j < sz; j++) {
                    if (i == 0 || i == sx - 1 || j == 0 || j == sz - 1) {
                        level.set(posX + i, y2 + 1, posZ + j, wallBlock);
                        if (type.roofLikeFloor) {
                            level.set(posX + i, y2, posZ + j, block(type.roofBorderIndex));
                        }
                    }
                }
            }

            int dx;
            int dz;
            int px;
            int pz;
            switch (stairsDir) {
                case 0 -> {
                    dx = 0;
                    dz = -1;
                    px = posX + sx - 2;
                    pz = posZ + sz - 2;
                }
                case 1 -> {
                    dx = 1;
                    dz = 0;
                    px = posX + 1;
                    pz = posZ + sz - 2;
                }
                case 2 -> {
                    dx = 0;
                    dz = 1;
                    px = posX + 1;
                    pz = posZ + 1;
                }
                default -> {
                    dx = -1;
                    dz = 0;
                    px = posX + sx - 2;
                    pz = posZ + 1;
                }
            }
            BlockState ladder = TGBlocks.LADDER_METAL.get().defaultBlockState().setValue(LadderBlock.FACING, direction(dx, dz));
            for (int i = 0; i < segY - 1; i++) {
                int y = posY + i * wallY;
                if (!isSmall) {
                    if (i % 2 == 1) {
                        staircase(px + dx, y + 1, pz + dz, wallY, (stairsDir + 2) % 4, true);
                    } else {
                        staircase(px, y + 1, pz, wallY, (stairsDir + 2) % 4, false);
                    }
                } else {
                    for (int j = 0; j < wallY; j++) {
                        if (j < wallY - 1) {
                            level.set(px - dx, y + j + 1, pz - dz, wallBlock);
                        }
                        level.set(px, y + j + 1, pz, ladder);
                    }
                }
            }
            // roof ladder up through the top floor to a hatch
            int top = posY + (segY - 1) * wallY;
            for (int i = 0; i < wallY; i++) {
                if (i < wallY - 1) {
                    level.set(px - dx, top + i + 1, pz - dz, wallBlock);
                }
                level.set(px, top + i + 1, pz, ladder);
            }
            level.set(px, y2 + 1, pz, Blocks.OAK_TRAPDOOR.defaultBlockState());
        }

        private void floor(int posX, int posY, int posZ, int sizeX, int sizeZ) {
            for (int x = posX; x < posX + sizeX; x++) {
                for (int z = posZ; z < posZ + sizeZ; z++) {
                    level.set(x, posY, z, block(2));
                }
            }
        }

        /** Ceiling lamps every 3 blocks under a floor. */
        private void lamps(int posX, int posY, int posZ, int sizeX, int sizeZ) {
            BlockState lamp = TGBlocks.LAMP_WHITE.get().defaultBlockState().setValue(TGLampBlock.FACING, Direction.UP);
            int ix = (sizeX - 1) % LAMP_DENSITY;
            int iz = (sizeZ - 1) % LAMP_DENSITY;
            for (int x = posX + ix / 2; x < posX + sizeX - (1 - ix / 2); x += LAMP_DENSITY) {
                for (int z = posZ + iz / 2; z < posZ + sizeZ - (1 - iz / 2); z += LAMP_DENSITY) {
                    level.set(x, posY, z, lamp);
                }
            }
        }

        /** One wall of wall segments; the middle one of the door wall has the door. */
        private void wall(int posX, int posY, int posZ, int segments, int dx, int dz, int wallDirection, boolean placeDoor) {
            int mid = segments / 2;
            int wallX = type.wallX;
            int wallY = type.wallY;
            for (int i = 0; i < segments; i++) {
                int[][] pattern = placeDoor && i == mid ? type.doorPattern : type.wallPattern;
                for (int j = 0; j < wallX; j++) {
                    for (int l = 1; l <= wallY; l++) {
                        int b = pattern[wallY - l][j];
                        int offset = i * wallX + j;
                        level.set(posX + offset * dx, posY + l - 1, posZ + offset * dz, b >= 0 ? block(b) : Blocks.AIR.defaultBlockState());
                    }
                }
                if (placeDoor && i == mid) {
                    // the door faces out of its wall: north, east, south, west
                    Direction facing = Direction.from2DDataValue((wallDirection + 2) % 4);
                    int offset = i * wallX + type.doorOffset;
                    for (int k = 0; k < type.doorWidth; k++) {
                        CampBuildings.door(level, posX + (offset + k) * dx, posY + 1, posZ + (offset + k) * dz, Blocks.OAK_DOOR, facing,
                                k == 0 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT);
                    }
                }
            }
        }

        /** Techguns 2 {@code Staircase.placeStaircase}: {@code size} steps along a direction (0 +x, 1 +z, 2 -x, 3 -z), supported below. */
        private void staircase(int posX, int posY, int posZ, int size, int dir, boolean inverted) {
            int dx = dir == 0 ? 1 : dir == 2 ? -1 : 0;
            int dz = dir == 1 ? 1 : dir == 3 ? -1 : 0;
            Direction up = inverted ? direction(-dx, -dz) : direction(dx, dz);
            BlockState step = stairs.defaultBlockState().setValue(StairBlock.FACING, up);
            BlockState support = stairSupport.defaultBlockState();
            for (int i = 0; i < size; i++) {
                int x = inverted ? posX + i * dx : posX + (i + 1) * dx;
                int y = inverted ? posY + size - 1 - i : posY + i;
                int z = inverted ? posZ + i * dz : posZ + (i + 1) * dz;
                level.set(x, y, z, step);
                for (int j = 1; j <= y - posY; j++) {
                    level.set(x, y - j, z, support);
                }
                for (int j = 1; j <= 3; j++) {
                    level.setAir(x, y + j, z);
                }
            }
        }
    }

    private static Direction direction(int dx, int dz) {
        return dx > 0 ? Direction.EAST : dx < 0 ? Direction.WEST : dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
