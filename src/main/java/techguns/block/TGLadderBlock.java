package techguns.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Techguns 2 metal ladder ({@code BlockTGLadder}): a vanilla ladder (climbable via the {@code minecraft:climbable} tag)
 * that needs no supporting wall and is 2/16 thick. Placing it on a top or bottom face continues the ladder above or below.
 */
public class TGLadderBlock extends LadderBlock {
    private static final VoxelShape EAST = Block.box(0, 0, 0, 2, 16, 16);
    private static final VoxelShape WEST = Block.box(14, 0, 0, 16, 16, 16);
    private static final VoxelShape SOUTH = Block.box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape NORTH = Block.box(0, 0, 14, 16, 16, 16);

    public TGLadderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> EAST;
        };
    }

    /** Free-standing: ladders stay where they were put, like in Techguns 2. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction facing;
        if (face.getAxis().isHorizontal()) {
            facing = face;
        } else if (level.getBlockState(pos.below()).getBlock() instanceof TGLadderBlock) {
            facing = level.getBlockState(pos.below()).getValue(FACING);
        } else if (level.getBlockState(pos.above()).getBlock() instanceof TGLadderBlock) {
            facing = level.getBlockState(pos.above()).getValue(FACING);
        } else {
            facing = context.getHorizontalDirection().getOpposite();
        }
        boolean water = level.getFluidState(pos).getType() == Fluids.WATER;
        return defaultBlockState().setValue(FACING, facing).setValue(WATERLOGGED, water);
    }
}
