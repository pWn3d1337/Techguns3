package techguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Flat camo netting laid over the ground (Techguns 2 {@code BlockTGCamoNetTop}): a 1/16 high net that connects to
 * neighbouring top nets of any type on the four horizontal sides.
 */
public class CamoNetTopBlock extends Block {
    private static final VoxelShape[] SHAPES = new VoxelShape[16];

    static {
        for (int index = 0; index < 16; index++) {
            boolean n = (index & 8) != 0;
            boolean e = (index & 4) != 0;
            boolean s = (index & 2) != 0;
            boolean w = (index & 1) != 0;
            // the 1.12 table: a net reaches the block edge on connected sides and 9/16 past the centre on the opposite side
            double x0 = w ? 0 : (e ? 7 : 4);
            double x1 = e ? 16 : (w ? 9 : 12);
            double z0 = n ? 0 : (s ? 7 : 4);
            double z1 = s ? 16 : (n ? 9 : 12);
            SHAPES[index] = Block.box(x0, 0, z0, x1, 1, z1);
        }
    }

    public CamoNetTopBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CamoNetBlock.NORTH, false).setValue(CamoNetBlock.EAST, false)
                .setValue(CamoNetBlock.SOUTH, false).setValue(CamoNetBlock.WEST, false));
    }

    private static int index(BlockState state) {
        return (state.getValue(CamoNetBlock.NORTH) ? 8 : 0) | (state.getValue(CamoNetBlock.EAST) ? 4 : 0)
                | (state.getValue(CamoNetBlock.SOUTH) ? 2 : 0) | (state.getValue(CamoNetBlock.WEST) ? 1 : 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CamoNetBlock.NORTH, CamoNetBlock.EAST, CamoNetBlock.SOUTH, CamoNetBlock.WEST);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[index(state)];
    }

    private static boolean connectsTo(BlockState neighbor) {
        return neighbor.getBlock() instanceof CamoNetTopBlock;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return defaultBlockState()
                .setValue(CamoNetBlock.NORTH, connectsTo(level.getBlockState(pos.north())))
                .setValue(CamoNetBlock.EAST, connectsTo(level.getBlockState(pos.east())))
                .setValue(CamoNetBlock.SOUTH, connectsTo(level.getBlockState(pos.south())))
                .setValue(CamoNetBlock.WEST, connectsTo(level.getBlockState(pos.west())));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
                                     BlockPos neighborPos) {
        if (direction.getAxis().isHorizontal()) {
            return state.setValue(CamoNetBlock.propertyFor(direction), connectsTo(neighborState));
        }
        return state;
    }
}
