package techguns.debug;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Techguns 2 {@code BlockDebugMarker} interior marker: a structure editor marker facing the placer (it marks the
 * inside of a template; scans turn it into air, see {@code StructureScans}).
 */
public class InteriorMarkerBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<InteriorMarkerBlock> CODEC = simpleCodec(InteriorMarkerBlock::new);

    public InteriorMarkerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
