package techguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import techguns.block.Door3x3Block;
import techguns.machine.TGMachineBlock;

/**
 * Wrenches on Techguns blocks (Techguns 2: every item with the tool class "wrench", here the item tag
 * {@code c:tools/wrench}, so the crowbar and other mods' wrenches): sneak + use on a 3x3 door opens its settings; use on
 * a single-block machine (not multiblocks) turns it (turret bases: stand or hang the turret by the face used), sneak +
 * use picks it up with its contents. Handled before the block's own use, which vanilla skips when sneaking with an
 * item in hand.
 */
public final class WrenchEvents {
    private WrenchEvents() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(WrenchEvents::onRightClickBlock);
    }

    public static boolean isWrench(ItemStack stack) {
        return stack.is(Tags.Items.TOOLS_WRENCH);
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!isWrench(event.getItemStack())) {
            return;
        }
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        boolean sneaking = player.isSecondaryUseActive();
        if (state.getBlock() instanceof Door3x3Block door && sneaking) {
            door.openSettings(level, pos, state, player);
        } else if (state.getBlock() instanceof TGMachineBlock machine && machine.canBeWrenched()) {
            if (sneaking) {
                machine.dismantle(level, pos, state, player);
            } else {
                machine.turn(level, pos, state, player, event.getHitVec().getDirection());
            }
        } else {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
