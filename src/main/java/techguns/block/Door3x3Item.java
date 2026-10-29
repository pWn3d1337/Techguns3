package techguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Places a 3x3 door (Techguns 2 {@code ItemTGDoor3x3}): right-click the top face of a block; the door stands on the
 * clicked block, three wide and three high, in the plane facing the player.
 */
public class Door3x3Item extends BlockItem {
    public Door3x3Item(Door3x3Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Door3x3Block door = (Door3x3Block) getBlock();
        BlockPos base = context.getClickedPos();
        // the door plane is the Z axis when the player looks east or west
        boolean zplane = context.getHorizontalDirection().getAxis() == Direction.Axis.X;
        BlockPos master = base.above();

        if (!Door3x3Block.isFree(level, master, zplane)) {
            return InteractionResult.FAIL;
        }
        if (player != null && !player.mayUseItemAt(base, Direction.UP, stack)) {
            return InteractionResult.FAIL;
        }
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                BlockPos pos = Door3x3Block.partPos(master, zplane, col, row);
                if (!level.isUnobstructed(door.partState(zplane, col, row), pos, CollisionContext.empty())) {
                    return InteractionResult.FAIL;
                }
            }
        }

        if (!level.isClientSide) {
            for (int col = 0; col < 3; col++) {
                for (int row = 0; row < 3; row++) {
                    level.setBlock(Door3x3Block.partPos(master, zplane, col, row), door.partState(zplane, col, row), Block.UPDATE_ALL);
                }
            }
            BlockState masterState = level.getBlockState(master);
            SoundType sound = masterState.getSoundType();
            level.playSound(null, base, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0f) / 2.0f, sound.getPitch() * 0.8f);
            level.gameEvent(GameEvent.BLOCK_PLACE, master, GameEvent.Context.of(player, masterState));
            // the placer owns the door (security setting, Techguns 2 BasicOwnedTileEnt)
            if (player != null && level.getBlockEntity(master) instanceof Door3x3BlockEntity entity) {
                entity.setOwner(player);
            }
            door.updatePower(level, master);
        }
        stack.consume(1, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
