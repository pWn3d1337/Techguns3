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
 * connector plate on each endpoint side and an adapter plate on each side where the neighbouring pipe of the same kind
 * sits in the centre while this one was moved aside (several kinds in one block).
 */
public final class PipeParts {
    public enum Type { CORE, ARM, PLATE, ADAPTER }

    /** Half size of an adapter plate's tube openings, and its depth (1/16 block). */
    public static final double ADAPTER_HALF = 2.0;
    public static final double ADAPTER_DEPTH = 1.5;

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
     * The adapter plate on the face {@code side} joining this segment's tube in {@code lane} to the neighbour's tube in
     * the centre lane: it spans both tube openings, in 1/16 block.
     */
    public static double[] adapter(Lane lane, Direction side) {
        double lo = Math.min(lane.center, Lane.B.center) - ADAPTER_HALF;
        double hi = Math.max(lane.center, Lane.B.center) + ADAPTER_HALF;
        double[] range = faceRange(side, ADAPTER_DEPTH);
        return switch (side.getAxis()) {
            case X -> new double[]{range[0], lo, lo, range[1], hi, hi};
            case Y -> new double[]{lo, range[0], lo, hi, range[1], hi};
            case Z -> new double[]{lo, lo, range[0], hi, hi, range[1]};
        };
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
                parts.add(new Part(Type.ADAPTER, side, box(adapter(lane, side))));
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
