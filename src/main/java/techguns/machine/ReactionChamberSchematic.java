package techguns.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import techguns.multiblock.MultiblockPart;
import techguns.multiblock.MultiblockSchematic;
import techguns.registry.TGBlocks;

/**
 * Techguns 2 {@code ReactionChamberDefinition}: 3 wide, 3 deep, 4 high, extending away from the used controller side
 * ({@link #structureDirection}). Bottom layer: the controller in the middle of the front row, housings, item/fluid
 * connectors at the three middle edges behind the controller; two layers of 3x3 glass; a 3x3 housing top with the
 * energy connector in the middle.
 */
public final class ReactionChamberSchematic implements MultiblockSchematic {
    public static final ReactionChamberSchematic INSTANCE = new ReactionChamberSchematic();
    private static final double BOTTOM = 0.25;
    private static final double MIDDLE = 0.45;
    private static final double TOP = 0.35;
    /** Techguns 2 part bounding boxes for a south-facing chamber, keyed by (x, y, z) relative to the bottom centre. */
    private static final Map<Vec3i, double[]> BOXES = new HashMap<>();

    static {
        for (int x : new int[] {-1, 1}) {
            for (int z : new int[] {-1, 1}) {
                BOXES.put(new Vec3i(x, 0, z), cornerBox(x, z, BOTTOM, 1.0));
                BOXES.put(new Vec3i(x, 1, z), cornerBox(x, z, MIDDLE, 1.0));
                BOXES.put(new Vec3i(x, 2, z), cornerBox(x, z, MIDDLE, 1.0));
                BOXES.put(new Vec3i(x, 3, z), cornerBox(x, z, MIDDLE, 1.0 - TOP));
            }
        }
        for (int y = 1; y <= 3; y++) {
            double top = y == 3 ? 1.0 - TOP : 1.0;
            BOXES.put(new Vec3i(-1, y, 0), new double[] {MIDDLE, 0, 0, 1, top, 1});
            BOXES.put(new Vec3i(1, y, 0), new double[] {0, 0, 0, 1 - MIDDLE, top, 1});
            BOXES.put(new Vec3i(0, y, -1), new double[] {0, 0, MIDDLE, 1, top, 1});
            BOXES.put(new Vec3i(0, y, 1), new double[] {0, 0, 0, 1, top, 1 - MIDDLE});
        }
    }

    private static double[] cornerBox(int x, int z, double inset, double top) {
        return new double[] {x < 0 ? inset : 0, 0, z < 0 ? inset : 0, x > 0 ? 1 - inset : 1, top, z > 0 ? 1 - inset : 1};
    }

    private ReactionChamberSchematic() {
    }

    /** Techguns 2 forms with the opposite of the used side: the chamber lies in front of the controller. */
    public static Direction structureDirection(Direction side) {
        return side.getOpposite();
    }

    @Override
    public boolean canFormFromSide(Direction side) {
        return side.getAxis().isHorizontal();
    }

    @Override
    public List<MultiblockPart> parts(Level level, BlockPos master, Direction side) {
        Direction dir = structureDirection(side);
        Direction left = dir.getClockWise();
        BlockPos center = master.relative(dir);
        List<MultiblockPart> parts = new ArrayList<>();
        for (int up = 1; up <= 2; up++) {
            for (int forward = 0; forward <= 2; forward++) {
                for (int side2 = -1; side2 <= 1; side2++) {
                    parts.add(part(master.relative(dir, forward).relative(left, side2).above(up), TGBlocks.REACTION_CHAMBER_GLASS.get(),
                            MultiblockPart.Connector.NONE));
                }
            }
        }
        // bottom: housings at the four corners and the centre, item/fluid connectors at the middle edges
        housing(parts, master.relative(left), MultiblockPart.Connector.NONE);
        housing(parts, master.relative(left.getOpposite()), MultiblockPart.Connector.NONE);
        housing(parts, master.relative(left).relative(dir, 2), MultiblockPart.Connector.NONE);
        housing(parts, master.relative(left.getOpposite()).relative(dir, 2), MultiblockPart.Connector.NONE);
        housing(parts, center, MultiblockPart.Connector.NONE);
        housing(parts, center.relative(dir), MultiblockPart.Connector.ITEMS_FLUIDS);
        housing(parts, center.relative(left), MultiblockPart.Connector.ITEMS_FLUIDS);
        housing(parts, center.relative(left.getOpposite()), MultiblockPart.Connector.ITEMS_FLUIDS);
        // top: full 3x3, the energy connector in the middle
        for (int forward = 0; forward <= 2; forward++) {
            for (int side2 = -1; side2 <= 1; side2++) {
                BlockPos pos = master.relative(dir, forward).relative(left, side2).above(3);
                housing(parts, pos, forward == 1 && side2 == 0 ? MultiblockPart.Connector.ENERGY : MultiblockPart.Connector.NONE);
            }
        }
        return parts;
    }

    private static MultiblockPart part(BlockPos pos, net.minecraft.world.level.block.Block block, MultiblockPart.Connector connector) {
        return new MultiblockPart(pos.immutable(), block, connector);
    }

    private static void housing(List<MultiblockPart> parts, BlockPos pos, MultiblockPart.Connector connector) {
        parts.add(part(pos, TGBlocks.REACTION_CHAMBER_HOUSING.get(), connector));
    }

    /** Techguns 2 {@code getBBforSlave}: the slimmer outer parts, turned from the south-facing boxes to {@code dir}. */
    public static VoxelShape partShape(BlockPos master, Direction dir, BlockPos pos) {
        BlockPos center = master.relative(dir);
        int dx = pos.getX() - center.getX();
        int dz = pos.getZ() - center.getZ();
        // into the south frame: forward = +z, the structure's left (clockwise) = -x
        Direction left = dir.getClockWise();
        int forward = dx * dir.getStepX() + dz * dir.getStepZ();
        int leftward = dx * left.getStepX() + dz * left.getStepZ();
        double[] box = BOXES.get(new Vec3i(-leftward, pos.getY() - center.getY(), forward));
        if (box == null) {
            return Shapes.block();
        }
        // back into the world: turn the south box clockwise once per step from south to dir
        double x0 = box[0], z0 = box[2], x1 = box[3], z1 = box[5];
        for (int i = 0; i < dir.get2DDataValue(); i++) {
            double nx0 = 1 - z1, nx1 = 1 - z0, nz0 = x0, nz1 = x1;
            x0 = nx0;
            x1 = nx1;
            z0 = nz0;
            z1 = nz1;
        }
        return Shapes.box(x0, box[1], z0, x1, box[4], z1);
    }
}
