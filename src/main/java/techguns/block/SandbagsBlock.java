package techguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Sandbags (Techguns 2 {@code BlockSandbags}): a 8/16 wide pillar that connects like a fence to other sandbags, fences,
 * fence gates and solid block faces. The corner flags only select the model (a filled corner when both neighbours and
 * their diagonal connect); they are derived from the world like the connections.
 */
public class SandbagsBlock extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty CORNER_NE = BooleanProperty.create("corner_ne");
    public static final BooleanProperty CORNER_ES = BooleanProperty.create("corner_es");
    public static final BooleanProperty CORNER_SW = BooleanProperty.create("corner_sw");
    public static final BooleanProperty CORNER_WN = BooleanProperty.create("corner_wn");

    private static final VoxelShape PILLAR = Block.box(4, 0, 4, 12, 16, 12);
    private static final VoxelShape[] SELECTION = new VoxelShape[16];
    private static final VoxelShape[] COLLISION = new VoxelShape[16];

    static {
        for (int index = 0; index < 16; index++) {
            boolean n = (index & 8) != 0;
            boolean e = (index & 4) != 0;
            boolean s = (index & 2) != 0;
            boolean w = (index & 1) != 0;
            // the 1.12 bounding boxes are one box reaching the edge on connected sides
            SELECTION[index] = Block.box(w ? 0 : 4, 0, n ? 0 : 4, e ? 16 : 12, 16, s ? 16 : 12);
            VoxelShape collision = PILLAR;
            if (n) {
                collision = Shapes.or(collision, Block.box(4, 0, 0, 12, 16, 4));
            }
            if (e) {
                collision = Shapes.or(collision, Block.box(12, 0, 4, 16, 16, 12));
            }
            if (s) {
                collision = Shapes.or(collision, Block.box(4, 0, 12, 12, 16, 16));
            }
            if (w) {
                collision = Shapes.or(collision, Block.box(0, 0, 4, 4, 16, 12));
            }
            COLLISION[index] = collision;
        }
    }

    public SandbagsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false)
                .setValue(CORNER_NE, false).setValue(CORNER_ES, false).setValue(CORNER_SW, false).setValue(CORNER_WN, false));
    }

    private static int index(BlockState state) {
        return (state.getValue(NORTH) ? 8 : 0) | (state.getValue(EAST) ? 4 : 0) | (state.getValue(SOUTH) ? 2 : 0) | (state.getValue(WEST) ? 1 : 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, CORNER_NE, CORNER_ES, CORNER_SW, CORNER_WN);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SELECTION[index(state)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION[index(state)];
    }

    /** Whether a sandbag block at the neighbour position connects towards {@code direction} (from this block). */
    private boolean connects(BlockGetter level, BlockPos neighborPos, Direction direction) {
        BlockState neighbor = level.getBlockState(neighborPos);
        Block block = neighbor.getBlock();
        if (block instanceof FenceBlock || block instanceof FenceGateBlock || block instanceof SandbagsBlock) {
            return true;
        }
        boolean exception = isExceptionForConnection(neighbor) || block == Blocks.BARRIER || block == Blocks.MELON || block == Blocks.PUMPKIN
                || block == Blocks.CARVED_PUMPKIN || block == Blocks.JACK_O_LANTERN;
        return !exception && neighbor.isFaceSturdy(level, neighborPos, direction.getOpposite());
    }

    private boolean connectsFrom(BlockGetter level, BlockPos pos, Direction direction) {
        return connects(level, pos.relative(direction), direction);
    }

    private boolean corner(BlockGetter level, BlockPos pos, Direction d1, Direction d2) {
        return connectsFrom(level, pos, d1) && connectsFrom(level, pos, d2)
                && (connectsFrom(level, pos.relative(d1), d2) || connectsFrom(level, pos.relative(d2), d1));
    }

    private BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        return state
                .setValue(NORTH, connectsFrom(level, pos, Direction.NORTH))
                .setValue(EAST, connectsFrom(level, pos, Direction.EAST))
                .setValue(SOUTH, connectsFrom(level, pos, Direction.SOUTH))
                .setValue(WEST, connectsFrom(level, pos, Direction.WEST))
                .setValue(CORNER_NE, corner(level, pos, Direction.NORTH, Direction.EAST))
                .setValue(CORNER_ES, corner(level, pos, Direction.EAST, Direction.SOUTH))
                .setValue(CORNER_SW, corner(level, pos, Direction.SOUTH, Direction.WEST))
                .setValue(CORNER_WN, corner(level, pos, Direction.WEST, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
                                     BlockPos neighborPos) {
        return direction.getAxis().isHorizontal() ? withConnections(state, level, pos) : state;
    }
}
