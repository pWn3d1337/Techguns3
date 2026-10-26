package techguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Standing camo netting (Techguns 2 {@code BlockTGCamoNet}): a thin post that connects to neighbouring camo nets of any
 * type on the four horizontal sides, like a fence. The selection box is the 1.12 bounding box, the collision a post plus
 * thin arms.
 */
public class CamoNetBlock extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    private static final VoxelShape[] SELECTION = new VoxelShape[16];
    private static final VoxelShape[] COLLISION = new VoxelShape[16];

    static {
        for (int index = 0; index < 16; index++) {
            boolean n = (index & 8) != 0;
            boolean e = (index & 4) != 0;
            boolean s = (index & 2) != 0;
            boolean w = (index & 1) != 0;
            SELECTION[index] = selection(n, e, s, w);
            VoxelShape collision = Block.box(7, 0, 7, 9, 16, 9);
            if (n) {
                collision = Shapes.or(collision, Block.box(7, 0, 0, 9, 16, 7));
            }
            if (e) {
                collision = Shapes.or(collision, Block.box(9, 0, 7, 16, 16, 9));
            }
            if (s) {
                collision = Shapes.or(collision, Block.box(7, 0, 9, 9, 16, 16));
            }
            if (w) {
                collision = Shapes.or(collision, Block.box(0, 0, 7, 7, 16, 9));
            }
            COLLISION[index] = collision;
        }
    }

    public CamoNetBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
    }

    /** The 1.12 bounding box: the post, stretched to the edge on connected sides (a single box, not a cross). */
    private static VoxelShape selection(boolean n, boolean e, boolean s, boolean w) {
        double x0 = 7;
        double x1 = 9;
        double z0 = 7;
        double z1 = 9;
        if (n || e || s || w) {
            if (w && e) {
                x0 = 0;
                x1 = 16;
            } else if (w) {
                x0 = 0;
            } else if (e) {
                x1 = 16;
            }
            if (n && s) {
                z0 = 0;
                z1 = 16;
            } else if (n) {
                z0 = 0;
            } else if (s) {
                z1 = 16;
            }
        }
        return Block.box(x0, 0, z0, x1, 16, z1);
    }

    private static int index(BlockState state) {
        return (state.getValue(NORTH) ? 8 : 0) | (state.getValue(EAST) ? 4 : 0) | (state.getValue(SOUTH) ? 2 : 0) | (state.getValue(WEST) ? 1 : 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SELECTION[index(state)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION[index(state)];
    }

    protected boolean connectsTo(BlockState neighbor) {
        return neighbor.getBlock() instanceof CamoNetBlock;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return defaultBlockState()
                .setValue(NORTH, connectsTo(level.getBlockState(pos.north())))
                .setValue(EAST, connectsTo(level.getBlockState(pos.east())))
                .setValue(SOUTH, connectsTo(level.getBlockState(pos.south())))
                .setValue(WEST, connectsTo(level.getBlockState(pos.west())));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
                                     BlockPos neighborPos) {
        if (direction.getAxis().isHorizontal()) {
            return state.setValue(propertyFor(direction), connectsTo(neighborState));
        }
        return state;
    }

    public static BooleanProperty propertyFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            default -> WEST;
        };
    }
}
