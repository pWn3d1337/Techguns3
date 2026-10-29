package techguns.debug;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * A Techguns 2 debug tool that works on an area: use on a block sets the first corner, use on another the second
 * and runs the tool on the box between them (both corners included).
 */
public abstract class AreaToolItem extends Item {
    protected AreaToolItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null || player.isSecondaryUseActive() && cyclesMode()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!TGDebug.check(player)) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Optional<BlockPos> first = DebugToolData.corner(stack);
        if (first.isEmpty()) {
            DebugToolData.setCorner(stack, pos);
            player.displayClientMessage(Component.translatable("techguns.debug.corner1", pos.toShortString()), false);
            return InteractionResult.CONSUME;
        }
        DebugToolData.clearCorner(stack);
        BlockPos min = new BlockPos(Math.min(pos.getX(), first.get().getX()), Math.min(pos.getY(), first.get().getY()),
                Math.min(pos.getZ(), first.get().getZ()));
        BlockPos max = new BlockPos(Math.max(pos.getX(), first.get().getX()), Math.max(pos.getY(), first.get().getY()),
                Math.max(pos.getZ(), first.get().getZ()));
        player.displayClientMessage(Component.translatable("techguns.debug.corner2", pos.toShortString(),
                max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1), false);
        apply((ServerLevel) level, player, stack, min, max);
        return InteractionResult.CONSUME;
    }

    /** Whether sneak + use switches the tool's mode instead of selecting. */
    protected boolean cyclesMode() {
        return false;
    }

    /** Runs the tool on the box from {@code min} to {@code max}, both included. */
    protected abstract void apply(ServerLevel level, Player player, ItemStack stack, BlockPos min, BlockPos max);
}
