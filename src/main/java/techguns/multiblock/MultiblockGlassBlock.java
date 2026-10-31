package techguns.multiblock;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A glass casing of a multiblock machine (Fabricator, Reaction Chamber): lit and shaded like vanilla glass
 * ({@code TransparentBlock}): no ambient occlusion on its neighbours, sky light passes, and faces between two unformed
 * glass blocks are hidden.
 */
public class MultiblockGlassBlock extends MultiblockPartBlock {
    public static final MapCodec<MultiblockGlassBlock> CODEC = simpleCodec(MultiblockGlassBlock::new);

    public MultiblockGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /** A formed neighbour draws no model of its own, so only faces between two unformed glass blocks are hidden. */
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) && !state.getValue(FORMED) && !adjacent.getValue(FORMED) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
