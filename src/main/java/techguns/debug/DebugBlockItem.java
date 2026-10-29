package techguns.debug;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** The block item of a debug block: placing it needs the debug tools on. */
public class DebugBlockItem extends BlockItem {
    public DebugBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        return TGDebug.check(context.getLevel().isClientSide ? null : context.getPlayer()) && super.canPlace(context, state);
    }
}
