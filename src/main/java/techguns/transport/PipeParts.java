package techguns.transport;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import techguns.core.transport.Lane;

/**
 * Geometry of the pipe parts in 1/16 block, shared by the block shape, the hit test (which side and kind a wrench hit)
 * and the part models (datagen {@code TGPipeModelProvider}): per lane a core cube, an arm to each connected side and a
 * connector plate on each endpoint side.
 */
public final class PipeParts {
    public enum Type { CORE, ARM, PLATE }

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
        return along(lane, side, Lane.TUBE_HALF, side.getAxisDirection() == Direction.AxisDirection.POSITIVE
                ? new double[]{lane.center + Lane.CORE_HALF, 16} : new double[]{0, lane.center - Lane.CORE_HALF});
    }

    /** The connector plate of {@code lane} on the face {@code side}, in 1/16 block. */
    public static double[] plate(Lane lane, Direction side) {
        return along(lane, side, Lane.PLATE_HALF, side.getAxisDirection() == Direction.AxisDirection.POSITIVE
                ? new double[]{16 - Lane.PLATE_DEPTH, 16} : new double[]{0, Lane.PLATE_DEPTH});
    }

    private static double[] along(Lane lane, Direction side, double half, double[] range) {
        double lo = lane.center - half;
        double hi = lane.center + half;
        return switch (side.getAxis()) {
            case X -> new double[]{range[0], lo, lo, range[1], hi, hi};
            case Y -> new double[]{lo, range[0], lo, hi, range[1], hi};
            case Z -> new double[]{lo, lo, range[0], hi, hi, range[1]};
        };
    }

    private static AABB box(double[] b) {
        return new AABB(b[0] / 16, b[1] / 16, b[2] / 16, b[3] / 16, b[4] / 16, b[5] / 16);
    }

    /** The parts of one segment: core, arms to linked and endpoint sides, plates on endpoint sides. */
    public static List<Part> parts(Lane lane, int links, int ends) {
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
        }
        return parts;
    }

    // --- part model names (block/pipe/part/<name>), shared by the datagen and the pipe model ---

    public static String coreModel(PipeKind kind, int tier) {
        return kind.name() + "_" + PipeKind.TIER_NAMES[tier] + "_core";
    }

    public static String armModel(PipeKind kind, int tier, Direction side) {
        return kind.name() + "_" + PipeKind.TIER_NAMES[tier] + "_arm_" + side.getName();
    }

    public static String plateModel(PipeKind kind, Direction side, techguns.core.transport.SideMode mode) {
        return kind.name() + "_plate_" + side.getName() + "_" + mode.name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Modes that show a connector plate (an endpoint side is never disabled). */
    public static final List<techguns.core.transport.SideMode> PLATE_MODES = List.of(techguns.core.transport.SideMode.NEUTRAL,
            techguns.core.transport.SideMode.OUTPUT, techguns.core.transport.SideMode.INPUT);

    /** Every part model name. */
    public static List<String> allModels() {
        List<String> names = new ArrayList<>();
        for (PipeKind kind : TGPipeKinds.ALL) {
            for (int tier = 0; tier < kind.tierCount(); tier++) {
                names.add(coreModel(kind, tier));
                for (Direction side : Direction.values()) {
                    names.add(armModel(kind, tier, side));
                }
            }
            for (Direction side : Direction.values()) {
                for (techguns.core.transport.SideMode mode : PLATE_MODES) {
                    names.add(plateModel(kind, side, mode));
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
