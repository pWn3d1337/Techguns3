package techguns.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import techguns.multiblock.MultiblockPart;
import techguns.multiblock.MultiblockPartBlock;
import techguns.multiblock.MultiblockSchematic;
import techguns.registry.TGBlocks;
import techguns.registry.TGTags;

/**
 * Techguns 2 {@code OreDrillDefinition}: a drill of variable size pointing from the controller to an ore cluster, in
 * any of the six directions (the one neighbour that is an engine or a rod). Along the drill axis: {@code engines}
 * layers of a (2r+1)² engine block, then {@code rods} rod blocks, then the cluster. A frame (edges) and scaffold
 * (faces) of radius r+1 enclose engines and rods; the rods' layers are air around the rod, the last layer is the
 * scaffold drill plate. Rules: engines > r, rods ≥ 2r+1, rods ≥ engines, engines + rods ≤ 16, r ≤ 3. A mini drill is
 * the controller, one rod and the cluster.
 */
public final class OreDrillSchematic implements MultiblockSchematic {
    public static final OreDrillSchematic INSTANCE = new OreDrillSchematic();
    public static final int MAX_RADIUS = 3;
    public static final int MAX_SIZE = 16;

    private OreDrillSchematic() {
    }

    /** A drill's shape as found in the world. */
    public record Layout(Direction direction, int engines, int rods, int radius) {
        /** Techguns 2 {@code getDrillRadius}: 0 for the mini drill, else radius + 1. */
        public int drillRadius() {
            return engines == 0 ? 0 : radius + 1;
        }

        public int length() {
            return engines + rods;
        }

        public BlockPos clusterPos(BlockPos master) {
            return master.relative(direction, length() + 1);
        }
    }

    /** Techguns 2 {@code getSide1/getSide2}: the two axes across the drill. */
    public static Direction side1(Direction direction) {
        return direction.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.DOWN;
    }

    public static Direction side2(Direction direction) {
        return direction.getAxis() == Direction.Axis.X ? Direction.NORTH : Direction.EAST;
    }

    @Override
    public boolean canFormFromSide(Direction side) {
        return true;
    }

    private static boolean isUnformed(Level level, BlockPos pos, Block block) {
        BlockState state = level.getBlockState(pos);
        return state.is(block) && !state.getValue(MultiblockPartBlock.FORMED);
    }

    public static boolean isCluster(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TGTags.Blocks.ORE_CLUSTERS);
    }

    /** The drill direction: the only neighbour that is an unformed engine or rod; null if none or several. */
    @Nullable
    private static Direction findDirection(Level level, BlockPos master) {
        Direction found = null;
        for (Direction direction : Direction.values()) {
            BlockPos pos = master.relative(direction);
            if (isUnformed(level, pos, TGBlocks.ORE_DRILL_ENGINE.get()) || isUnformed(level, pos, TGBlocks.ORE_DRILL_ROD.get())) {
                if (found != null) {
                    return null;
                }
                found = direction;
            }
        }
        return found;
    }

    /** Engines and rods along the axis, or a message why the drill line is wrong (Techguns 2 {@code checkLength}). */
    private static Layout findLayout(Level level, BlockPos master, List<Component> problem) {
        Direction direction = findDirection(level, master);
        if (direction == null) {
            problem.add(Component.translatable("techguns.msg.error.oredrill.controlleralone"));
            return null;
        }
        Block engine = TGBlocks.ORE_DRILL_ENGINE.get();
        Block rod = TGBlocks.ORE_DRILL_ROD.get();
        int engines = 0;
        while (engines < MAX_SIZE && isUnformed(level, master.relative(direction, engines + 1), engine)) {
            engines++;
        }
        int rods = 0;
        while (rods < MAX_SIZE && isUnformed(level, master.relative(direction, engines + rods + 1), rod)) {
            rods++;
        }
        if (rods == 0 || (engines == 0 && rods > 1) || !isCluster(level, master.relative(direction, engines + rods + 1))) {
            problem.add(Component.translatable("techguns.msg.error.oredrill.nocluster"));
            return null;
        }
        if (engines == 0) {
            return new Layout(direction, 0, 1, 0);
        }
        // Techguns 2 checkWidth: engines counted sideways from the first engine layer
        int radius = 0;
        BlockPos first = master.relative(direction, 1);
        for (Direction across : new Direction[] {side1(direction), side1(direction).getOpposite(), side2(direction), side2(direction).getOpposite()}) {
            int count = 0;
            while (count < MAX_RADIUS && isUnformed(level, first.relative(across, count + 1), engine)) {
                count++;
            }
            radius = Math.max(radius, count);
        }
        Layout layout = new Layout(direction, engines, rods, radius);
        if (layout.length() > MAX_SIZE || rods < radius * 2 + 1 || rods < engines || engines <= radius) {
            problem.add(Component.translatable("techguns.msg.error.oredrill.rodsize"));
            return null;
        }
        return layout;
    }

    /** The drill layout at {@code master}, if it is a valid drill. */
    public static Optional<Layout> layout(Level level, BlockPos master) {
        return Optional.ofNullable(findLayout(level, master, new ArrayList<>()));
    }

    @Override
    public Optional<Component> check(Level level, BlockPos master, Direction side) {
        List<Component> problem = new ArrayList<>();
        findLayout(level, master, problem);
        return problem.stream().findFirst();
    }

    @Override
    public List<MultiblockPart> parts(Level level, BlockPos master, Direction side) {
        Layout layout = findLayout(level, master, new ArrayList<>());
        List<MultiblockPart> parts = new ArrayList<>();
        if (layout == null) {
            return parts;
        }
        Direction dir = layout.direction();
        if (layout.engines() == 0) {
            parts.add(new MultiblockPart(master.relative(dir), TGBlocks.ORE_DRILL_ROD.get(), MultiblockPart.Connector.ALL));
            return parts;
        }
        int r = layout.radius();
        int len = layout.length();
        Direction s1 = side1(dir);
        Direction s2 = side2(dir);
        for (int h = 1; h <= len; h++) {
            for (int a = -(r + 1); a <= r + 1; a++) {
                for (int b = -(r + 1); b <= r + 1; b++) {
                    BlockPos pos = master.relative(dir, h).relative(s1, a).relative(s2, b);
                    boolean outerA = Math.abs(a) == r + 1;
                    boolean outerB = Math.abs(b) == r + 1;
                    boolean endLayer = h == 1 || h == len;
                    int edges = (outerA ? 1 : 0) + (outerB ? 1 : 0) + (endLayer ? 1 : 0);
                    if (outerA || outerB) {
                        // the frame on the box edges, scaffold on the faces (Techguns 2 isFramePos / isScaffoldPos)
                        parts.add(new MultiblockPart(pos, edges >= 2 ? TGBlocks.ORE_DRILL_FRAME.get() : TGBlocks.ORE_DRILL_SCAFFOLD.get(),
                                MultiblockPart.Connector.ALL));
                    } else if (h <= layout.engines()) {
                        parts.add(new MultiblockPart(pos, TGBlocks.ORE_DRILL_ENGINE.get(), MultiblockPart.Connector.ALL));
                    } else if (a == 0 && b == 0) {
                        parts.add(new MultiblockPart(pos, TGBlocks.ORE_DRILL_ROD.get(), MultiblockPart.Connector.NONE));
                    } else if (h == len) {
                        // the last layer is the drill plate, hidden while formed
                        parts.add(new MultiblockPart(pos, TGBlocks.ORE_DRILL_SCAFFOLD.get(), MultiblockPart.Connector.NONE, true));
                    } else {
                        parts.add(MultiblockPart.air(pos));
                    }
                }
            }
        }
        return parts;
    }
}
