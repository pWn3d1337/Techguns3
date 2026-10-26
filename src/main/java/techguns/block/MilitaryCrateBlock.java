package techguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Military crate (Techguns 2 {@code BlockMilitaryCrate}): a crate slightly smaller than a block. It is not a container:
 * breaking it drops loot from its block loot table (the crate itself with Silk Touch); see the block loot provider.
 */
public class MilitaryCrateBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.5, 0, 0.5, 15.5, 16, 15.5);

    public MilitaryCrateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
