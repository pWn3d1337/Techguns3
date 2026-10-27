package techguns.world.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import techguns.registry.TGBlocks;

/**
 * Techguns 2 {@code AlienBugNest}: a nest of spherical chambers under the surface, a main chamber connected to an
 * entrance shaft with slime ladders and to five (up to ten) side chambers with eggs and one alien bug spawner each,
 * walls of bug nest sand. Generated in one go from a seed into a block map (the Techguns 2 generator read back what
 * it placed), so each chunk can place its part. Positions the nest doesn't set keep the terrain, which counts as solid.
 */
public final class AlienBugNest {
    static final int MAX_RADIUS = 10;
    static final int MIN_RADIUS = 3;
    private static final int SEGMENTS = 5;
    private static final int MAIN = 0;
    private static final int REGULAR = 1;
    private static final int ENTRANCE = 2;

    private final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private final Set<BlockPos> spawners = new HashSet<>();
    private final RandomSource random;

    private static final class Segment {
        final int x;
        final int y;
        final int z;
        final int radius;
        final int type;
        int status;
        final List<Segment> neighbors = new ArrayList<>();

        Segment(int x, int y, int z, int radius, int type) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.type = type;
        }
    }

    /**
     * @param x, y, z surface corner of the nest area (Techguns 2: chunk corner, 2 below the ground)
     * @param sizeX   16..31
     * @param sizeZ   16..31
     */
    public AlienBugNest(long seed, int x, int y, int z, int sizeX, int sizeZ) {
        this.random = RandomSource.create(seed);
        int startRadius = Math.min(MAX_RADIUS, MIN_RADIUS + SEGMENTS / 2);
        int cx = x + sizeX / 2;
        int cz = z + sizeZ / 2;
        Segment entrance = new Segment(cx, y, cz, 2, ENTRANCE);
        int mainY = y - (startRadius + 5 + random.nextInt(7));
        Segment main = new Segment(cx + random.nextInt(3), mainY, cz + random.nextInt(3), startRadius, MAIN);
        main.neighbors.add(entrance);
        double angle = Math.PI * 2.0 / SEGMENTS;
        Segment previous = main;
        for (int i = 0; i < SEGMENTS; i++) {
            int radius = Math.max(MIN_RADIUS, startRadius - random.nextInt(i + 1));
            double distance = startRadius + radius + 5.0 + random.nextDouble() * 8.0;
            double sx = cx + distance * Math.cos(angle * i);
            double sz = cz + distance * Math.sin(angle * i);
            int segY = mainY + random.nextInt(startRadius * 3) - startRadius * 2;
            Segment segment = new Segment((int) sx, segY, (int) sz, radius, REGULAR);
            if (previous == main) {
                main.neighbors.add(segment);
            } else if (random.nextBoolean()) {
                previous.neighbors.add(segment);
                if (random.nextBoolean()) {
                    main.neighbors.add(segment);
                }
            } else {
                main.neighbors.add(segment);
            }
            if (random.nextBoolean()) {
                radius = Math.max(MIN_RADIUS, startRadius - random.nextInt(i + 1));
                distance = startRadius + radius + 5.0 + random.nextDouble() * 8.0;
                double a = angle * i + Math.PI * 0.5 * (random.nextDouble() - 0.5);
                segY = mainY + random.nextInt(startRadius * 3) - startRadius * 2;
                segment.neighbors.add(new Segment((int) (sx + distance * Math.cos(a)), segY, (int) (sz + distance * Math.sin(a)), radius, REGULAR));
            }
            previous = segment;
        }
        placeSegment(main);
        placeConnections(main);
        placeInterior(main);
    }

    /** The nest's blocks; terrain stays where the map has no entry. */
    public Map<BlockPos, BlockState> blocks() {
        return blocks;
    }

    /** Positions of the alien bug spawners (monster holes). */
    public Set<BlockPos> spawners() {
        return spawners;
    }

    public BoundingBox bounds() {
        return BoundingBox.encapsulatingPositions(blocks.keySet()).orElseThrow();
    }

    // --- blocks ---

    /** Techguns 2 {@code wallBlock}: bug nest sand 16, eggs 1, sandstone 2. */
    private BlockState wall() {
        int roll = random.nextInt(19);
        return roll < 16 ? TGBlocks.BUGNEST_SAND.get().defaultBlockState() : roll < 17 ? TGBlocks.BUGNEST_EGGS.get().defaultBlockState()
                : Blocks.SANDSTONE.defaultBlockState();
    }

    /** Techguns 2 {@code wallBlock2} (tunnels): bug nest sand 12, sandstone 1. */
    private BlockState tunnelWall() {
        return random.nextInt(13) < 12 ? TGBlocks.BUGNEST_SAND.get().defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
    }

    private boolean isAir(BlockPos pos) {
        BlockState state = blocks.get(pos);
        return state != null && state.isAir();
    }

    /** Terrain (no entry) counts as solid. */
    private boolean isSolid(BlockPos pos) {
        BlockState state = blocks.get(pos);
        return state == null || (!state.isAir() && !state.is(TGBlocks.SLIMY_LADDER.get()));
    }

    // --- Techguns 2 nest steps ---

    private void placeSegment(Segment segment) {
        if (segment.status >= 1 || segment.type == ENTRANCE) {
            return;
        }
        sphereShell(segment.x, segment.y, segment.z, segment.radius, 1.0f);
        segment.status = 1;
        for (Segment neighbor : segment.neighbors) {
            placeSegment(neighbor);
        }
    }

    private void placeConnections(Segment segment) {
        if (segment.status >= 2) {
            return;
        }
        segment.status = 2;
        for (Segment neighbor : segment.neighbors) {
            Vec3 from = new Vec3(segment.x, segment.y, segment.z);
            Vec3 offset = new Vec3(neighbor.x, neighbor.y, neighbor.z).subtract(from);
            Vec3 dir = offset.normalize();
            double end = offset.length() - neighbor.radius + (neighbor.type == ENTRANCE ? 5.0 : 0.0);
            int x1 = (int) (segment.x + dir.x * segment.radius);
            int y1 = (int) (segment.y + dir.y * segment.radius);
            int z1 = (int) (segment.z + dir.z * segment.radius);
            int x2 = (int) (segment.x + dir.x * end);
            int y2 = (int) (segment.y + dir.y * end);
            int z2 = (int) (segment.z + dir.z * end);
            int r = Math.min(segment.radius, Math.min(2 + random.nextInt(2), neighbor.radius));
            cylinder(x1, y1, z1, x2, y2, z2, r);
            if (neighbor.type == ENTRANCE) {
                slimeLadders(x1, y1, z1, x2, y2, z2, r - 1);
            }
            placeConnections(neighbor);
        }
    }

    private void placeInterior(Segment segment) {
        if (segment.status >= 3 || segment.type == ENTRANCE) {
            return;
        }
        sphere(segment.x, segment.y, segment.z, segment.radius - 1);
        segment.status = 3;
        if (segment.type == REGULAR) {
            placeEggs(segment);
        }
        for (Segment neighbor : segment.neighbors) {
            if (segment.type == MAIN && neighbor.type == ENTRANCE) {
                // a slime ladder line from the bottom of the main chamber towards the entrance
                double angle = random.nextDouble() * 2.0 * Math.PI;
                double radius = 1.0 + random.nextDouble() * 0.66;
                int xo = (int) (radius * Math.cos(angle));
                int zo = (int) (radius * Math.sin(angle));
                Vec3 dir = new Vec3(neighbor.x - segment.x, neighbor.y - segment.y, neighbor.z - segment.z).normalize();
                double end = (segment.radius - 1) * 2;
                line((int) (segment.x + xo + dir.x * -(segment.radius + 1)), (int) (segment.y + dir.y * -(segment.radius + 1)),
                        (int) (segment.z + zo + dir.z * -(segment.radius + 1)), (int) (segment.x + xo + dir.x * end), (int) (segment.y + dir.y * end),
                        (int) (segment.z + zo + dir.z * end));
            }
            placeInterior(neighbor);
        }
    }

    private void placeEggs(Segment segment) {
        int r = segment.radius - 2;
        int ri = r > 1 ? r - 1 : r;
        int spawnerX = random.nextInt(2 * ri) - ri;
        int spawnerZ = random.nextInt(2 * ri) - ri;
        for (int i = -r; i < r; i++) {
            for (int j = -r; j < r; j++) {
                if (i == spawnerX && j == spawnerZ) {
                    BlockPos pos = new BlockPos(segment.x + i, segment.y - (segment.radius - 2), segment.z + j);
                    blocks.put(pos, TGBlocks.MONSTER_HOLE.get().defaultBlockState());
                    spawners.add(pos);
                } else {
                    float f1 = (float) (r - Math.abs(i)) / r;
                    float f2 = (float) (r - Math.abs(j)) / r;
                    if (random.nextDouble() < (f1 + f2) / 2.0f) {
                        for (int k = 0; k < segment.radius; k++) {
                            BlockPos pos = new BlockPos(segment.x + i, segment.y - (segment.radius - 1) + k, segment.z + j);
                            if (isAir(pos)) {
                                blocks.put(pos, TGBlocks.BUGNEST_EGGS.get().defaultBlockState());
                            }
                            if (random.nextDouble() > (f1 + f2) / 3.0f) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Techguns 2 BlockUtils shapes ---

    /** {@code fillSphere2}: air inside, wall in the outer {@code thickness}. */
    private void sphereShell(int x, int y, int z, float radius, float thickness) {
        float outer = radius * radius;
        float inner = (radius - thickness) * (radius - thickness);
        int r = (int) radius;
        for (int i = -r; i <= r; i++) {
            for (int j = -r; j <= r; j++) {
                for (int k = -r; k <= r; k++) {
                    float d = i * i + j * j + k * k;
                    if (d < inner) {
                        blocks.put(new BlockPos(x + i, y + j, z + k), Blocks.AIR.defaultBlockState());
                    } else if (d < outer) {
                        blocks.put(new BlockPos(x + i, y + j, z + k), wall());
                    }
                }
            }
        }
    }

    /** {@code fillSphere} with air. */
    private void sphere(int x, int y, int z, float radius) {
        float outer = radius * radius;
        int r = (int) radius;
        for (int i = -r; i <= r; i++) {
            for (int j = -r; j <= r; j++) {
                for (int k = -r; k <= r; k++) {
                    if (i * i + j * j + k * k < outer) {
                        blocks.put(new BlockPos(x + i, y + j, z + k), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    /** {@code fillCylinder}: a tunnel, air inside and walls in the outer block. */
    private void cylinder(int x1, int y1, int z1, int x2, int y2, int z2, float radius) {
        Vec3 v1 = new Vec3(x1, y1, z1);
        Vec3 axis = v1.subtract(new Vec3(x2, y2, z2));
        double length = axis.length();
        int r = (int) radius;
        for (int x = Math.min(x1, x2) - r; x < Math.max(x1, x2) + r; x++) {
            for (int y = Math.min(y1, y2) - r; y < Math.max(y1, y2) + r; y++) {
                for (int z = Math.min(z1, z2) - r; z < Math.max(z1, z2) + r; z++) {
                    double distance = axis.cross(v1.subtract(x, y, z)).length() / length;
                    if (distance < radius - 1.0f) {
                        blocks.put(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                    } else if (distance < radius) {
                        blocks.put(new BlockPos(x, y, z), tunnelWall());
                    }
                }
            }
        }
    }

    /**
     * {@code createSlimeLadder}: slime ladders on every free wall spot in the box around the entrance tunnel (the
     * Techguns 2 distance test always passed).
     */
    private void slimeLadders(int x1, int y1, int z1, int x2, int y2, int z2, float radius) {
        int r = (int) radius;
        for (int x = Math.min(x1, x2) - r; x < Math.max(x1, x2) + r; x++) {
            for (int y = Math.min(y1, y2) - r; y < Math.max(y1, y2) + r; y++) {
                for (int z = Math.min(z1, z2) - r; z < Math.max(z1, z2) + r; z++) {
                    if (radius > 0) {
                        ladder(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }

    /** {@code drawLine} (3D Bresenham) of bug nest sand with slime ladders around ({@code MBlockSlime}). */
    private void line(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int dz = z2 - z1;
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        for (int i = 0; i <= steps; i++) {
            float t = steps == 0 ? 0 : (float) i / steps;
            BlockPos pos = new BlockPos(Math.round(x1 + dx * t), Math.round(y1 + dy * t), Math.round(z1 + dz * t));
            blocks.put(pos, TGBlocks.BUGNEST_SAND.get().defaultBlockState());
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (isAir(pos.relative(side))) {
                    ladder(pos.relative(side));
                }
            }
        }
    }

    /** A slime ladder in a nest air block, against the first solid side (Techguns 2 placement facing). */
    private void ladder(BlockPos pos) {
        if (!isAir(pos)) {
            return;
        }
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (isSolid(pos.relative(facing.getOpposite()))) {
                blocks.put(pos, TGBlocks.SLIMY_LADDER.get().defaultBlockState().setValue(LadderBlock.FACING, facing));
                return;
            }
        }
    }
}
