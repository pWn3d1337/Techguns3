package techguns.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import techguns.core.transport.Lane;
import techguns.core.transport.SideMode;

/**
 * Geometry of the pipe parts in 1/16 block, shared by the block shape, the hit test (which side and kind a wrench hit)
 * and the part models (datagen {@code TGPipeModelProvider}): per segment a core cube, an arm to each connected side, a
 * connector plate on each endpoint side and a diagonal adapter piece on each side where the neighbouring pipe of the
 * same kind sits in the centre while this one was moved aside (several kinds in one block).
 */
public final class PipeParts {
    public enum Type { CORE, ARM, PLATE, ADAPTER }

    /** Depth of the fittings at both ends of a diagonal adapter, and of the diagonal piece inside them (1/16 block). */
    public static final double JOINT_DEPTH = 2 * Lane.CORE_HALF;
    public static final double DIAGONAL_INSET = 0.5;

    /** A part box in block coordinates (0..1); {@code side} is null for the core. */
    public record Part(Type type, @Nullable Direction side, AABB box) {
    }

    private PipeParts() {
    }

    /** The core cube of {@code lane}, in 1/16 block. */
    public static double[] core(Lane lane) {
        double lo = lane.center - Lane.CORE_HALF;
        double hi = lane.center + Lane.CORE_HALF;
        return new double[]{lo, lo, lo, hi, hi, hi};
    }

    /** The arm of {@code lane} from the core to the face {@code side}, in 1/16 block. */
    public static double[] arm(Lane lane, Direction side) {
        return along(lane.center, lane.center, side, Lane.TUBE_HALF, side.getAxisDirection() == Direction.AxisDirection.POSITIVE
                ? new double[]{lane.center + Lane.CORE_HALF, 16} : new double[]{0, lane.center - Lane.CORE_HALF});
    }

    /** The connector plate of {@code lane} on the face {@code side}, in 1/16 block. */
    public static double[] plate(Lane lane, Direction side) {
        return along(lane.center, lane.center, side, Lane.PLATE_HALF, faceRange(side, Lane.PLATE_DEPTH));
    }

    /**
     * A diagonal tube piece: {@code box} (unrotated, in 1/16 block) is turned by {@code angle} degrees about
     * {@code rotationAxis} through {@code origin}; its length runs along {@code longAxis}.
     */
    public record Bridge(double[] box, Direction.Axis longAxis, Direction.Axis rotationAxis, float angle, double[] origin) {
    }

    /**
     * The diagonal piece of the adapter on the face {@code side}: a tube lying diagonally in the face plane from this
     * segment's tube in {@code lane} to the neighbour's tube in the centre lane (the lanes sit on the block diagonal, so
     * the offset is the same on both axes of the face and a 45 degree turn about the arm axis lines it up). It runs
     * from centre to centre and sits inside the depth of the two {@link #adapterJoints joints}, which hide its cut ends.
     */
    public static Bridge adapter(Lane lane, Direction side) {
        double m = (lane.center + Lane.B.center) / 2;
        double half = Math.abs(lane.center - Lane.B.center) * Math.sqrt(2) / 2;
        double t = Lane.TUBE_HALF;
        double[] j = faceRange(side, JOINT_DEPTH);
        double[] r = {j[0] + DIAGONAL_INSET, j[1] - DIAGONAL_INSET};
        double c = (r[0] + r[1]) / 2;
        // element rotations are right-handed: +45 about Z turns +X to (1,1,0), about X turns +Y to (0,1,1); about Y
        // +X turns to (1,0,-1), so -45 there
        return switch (side.getAxis()) {
            case Z -> new Bridge(new double[]{m - half, m - t, r[0], m + half, m + t, r[1]}, Direction.Axis.X, Direction.Axis.Z, 45.0f,
                    new double[]{m, m, c});
            case X -> new Bridge(new double[]{r[0], m - half, m - t, r[1], m + half, m + t}, Direction.Axis.Y, Direction.Axis.X, 45.0f,
                    new double[]{c, m, m});
            case Y -> new Bridge(new double[]{m - half, r[0], m - t, m + half, r[1], m + t}, Direction.Axis.X, Direction.Axis.Y, -45.0f,
                    new double[]{m, c, m});
        };
    }

    /**
     * The two fittings of the adapter on the face {@code side}, core-sized cubes at the face around this segment's tube
     * and around the neighbour's centred tube: they close both tube ends and cover the ends of the diagonal piece.
     */
    public static List<double[]> adapterJoints(Lane lane, Direction side) {
        double[] range = faceRange(side, JOINT_DEPTH);
        return List.of(along(lane.center, lane.center, side, Lane.CORE_HALF, range),
                along(Lane.B.center, Lane.B.center, side, Lane.CORE_HALF, range));
    }

    /** Boxes covering the whole adapter for the shape and the hit test (an outline cannot be turned), in 1/16 block. */
    public static List<double[]> adapterBoxes(Lane lane, Direction side) {
        List<double[]> boxes = new ArrayList<>(adapterJoints(lane, side));
        double p = (lane.center + Lane.B.center) / 2;
        boxes.add(along(p, p, side, Lane.TUBE_HALF, faceRange(side, JOINT_DEPTH)));
        return boxes;
    }

    private static double[] faceRange(Direction side, double depth) {
        return side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? new double[]{16 - depth, 16} : new double[]{0, depth};
    }

    private static double[] along(double a, double b, Direction side, double half, double[] range) {
        return switch (side.getAxis()) {
            case X -> new double[]{range[0], a - half, b - half, range[1], a + half, b + half};
            case Y -> new double[]{a - half, range[0], b - half, a + half, range[1], b + half};
            case Z -> new double[]{a - half, b - half, range[0], a + half, b + half, range[1]};
        };
    }

    private static AABB box(double[] b) {
        return new AABB(b[0] / 16, b[1] / 16, b[2] / 16, b[3] / 16, b[4] / 16, b[5] / 16);
    }

    /**
     * The parts of one segment in {@code lane}: core, arms to linked and endpoint sides, plates on endpoint sides,
     * adapters on the {@code adapters} sides.
     */
    public static List<Part> parts(Lane lane, int links, int ends, int adapters) {
        List<Part> parts = new ArrayList<>();
        parts.add(new Part(Type.CORE, null, box(core(lane))));
        for (Direction side : Direction.values()) {
            int bit = 1 << side.get3DDataValue();
            if (((links | ends) & bit) != 0) {
                parts.add(new Part(Type.ARM, side, box(arm(lane, side))));
            }
            if ((ends & bit) != 0) {
                parts.add(new Part(Type.PLATE, side, box(plate(lane, side))));
            }
            if ((adapters & bit) != 0 && lane != Lane.B) {
                for (double[] b : adapterBoxes(lane, side)) {
                    parts.add(new Part(Type.ADAPTER, side, box(b)));
                }
            }
        }
        return parts;
    }

    /** The lanes a kind can be drawn in: the centre, and its own lane when it shares a block. */
    public static List<Lane> lanes(PipeKind kind) {
        return kind.lane() == Lane.B ? List.of(Lane.B) : List.of(Lane.B, kind.lane());
    }

    // --- part model names (block/pipe/part/<name>), shared by the datagen and the pipe model ---

    private static String lane(Lane lane) {
        return lane.name().toLowerCase(Locale.ROOT);
    }

    public static String coreModel(PipeKind kind, int tier, Lane lane) {
        return kind.name() + "_" + PipeKind.TIER_NAMES[tier] + "_" + lane(lane) + "_core";
    }

    public static String armModel(PipeKind kind, int tier, Lane lane, Direction side) {
        return kind.name() + "_" + PipeKind.TIER_NAMES[tier] + "_" + lane(lane) + "_arm_" + side.getName();
    }

    public static String adapterModel(PipeKind kind, int tier, Direction side) {
        return kind.name() + "_" + PipeKind.TIER_NAMES[tier] + "_adapter_" + side.getName();
    }

    public static String plateModel(Lane lane, Direction side, SideMode mode) {
        return "plate_" + lane(lane) + "_" + side.getName() + "_" + mode.name().toLowerCase(Locale.ROOT);
    }

    /** Modes that show a connector plate (an endpoint side is never disabled). */
    public static final List<SideMode> PLATE_MODES = List.of(SideMode.NEUTRAL, SideMode.OUTPUT, SideMode.INPUT);

    /** Every part model name. */
    public static List<String> allModels() {
        List<String> names = new ArrayList<>();
        for (PipeKind kind : TGPipeKinds.ALL) {
            for (int tier = 0; tier < kind.tierCount(); tier++) {
                for (Lane lane : lanes(kind)) {
                    names.add(coreModel(kind, tier, lane));
                    for (Direction side : Direction.values()) {
                        names.add(armModel(kind, tier, lane, side));
                    }
                }
                if (kind.lane() != Lane.B) {
                    for (Direction side : Direction.values()) {
                        names.add(adapterModel(kind, tier, side));
                    }
                }
            }
        }
        for (Lane lane : Lane.values()) {
            for (Direction side : Direction.values()) {
                for (SideMode mode : PLATE_MODES) {
                    names.add(plateModel(lane, side, mode));
                }
            }
        }
        return names;
    }

    public static VoxelShape shape(List<Part> parts) {
        VoxelShape shape = Shapes.empty();
        for (Part part : parts) {
            shape = Shapes.or(shape, Shapes.create(part.box()));
        }
        return shape.optimize();
    }
}
