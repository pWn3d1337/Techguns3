package techguns.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Lantern (Techguns 2 {@code BlockTGLamp}, lantern types yellow and white): hangs between up to six supporting blocks;
 * the six booleans mark the sides with a support and show a connector there. It pops off when no side is supported.
 * <p>
 * Simplified vs 1.12: there the lantern only checked the support of the side it was placed with (forgotten after a
 * reload), so it dropped on any neighbour update unless it hung on a floor; here every side counts.
 */
public class TGLanternBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(4, 2, 4, 12, 14, 12);

    public TGLanternBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any();
        for (BooleanProperty property : PipeBlock.PROPERTY_BY_DIRECTION.values()) {
            state = state.setValue(property, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        PipeBlock.PROPERTY_BY_DIRECTION.values().forEach(builder::add);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    private static boolean canAttach(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos support = pos.relative(direction);
        return level.getBlockState(support).isFaceSturdy(level, support, direction.getOpposite());
    }

    private static boolean hasSupport(BlockState state) {
        return PipeBlock.PROPERTY_BY_DIRECTION.values().stream().anyMatch(state::getValue);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (canAttach(level, pos, direction)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction direction : Direction.values()) {
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction),
                    canAttach(context.getLevel(), context.getClickedPos(), direction));
        }
        return hasSupport(state) ? state : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
                                     BlockPos neighborPos) {
        state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), canAttach(level, pos, direction));
        return hasSupport(state) ? state : Blocks.AIR.defaultBlockState();
    }
}
