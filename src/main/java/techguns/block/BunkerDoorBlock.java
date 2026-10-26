package techguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.BlockHitResult;
import techguns.registry.TGSounds;

/**
 * Bunker door (Techguns 2 {@code BlockTGDoor2x1}): a vanilla double-high door that opens by hand, with the bunker door
 * sound for opening and closing, and that toggles the matching door next to it (hinges facing each other) as well.
 */
public class BunkerDoorBlock extends DoorBlock {
    private BunkerDoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    /** Creates the block with its own block set type; call once, when the block is registered (the sounds exist by then). */
    public static BunkerDoorBlock create(Properties properties) {
        SoundEvent doorSound = TGSounds.BUNKER_DOOR_OPEN.get();
        BlockSetType type = BlockSetType.register(new BlockSetType("techguns_bunker", true, true, false,
                BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.METAL, doorSound, doorSound,
                SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
                SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
                SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
        return new BunkerDoorBlock(type, properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionResult result = super.useWithoutItem(state, level, pos, player, hit);
        if (result.consumesAction() && !level.isClientSide) {
            // 1.12 double door: the door on the hinge side's opposite also toggles when it was in the same state
            boolean lower = state.getValue(HALF) == DoubleBlockHalf.LOWER;
            BlockPos lowerPos = lower ? pos : pos.below();
            BlockState lowerState = level.getBlockState(lowerPos);
            if (lowerState.is(this)) {
                Direction facing = lowerState.getValue(FACING);
                Direction side = lowerState.getValue(HINGE) == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise();
                BlockPos partnerPos = lowerPos.relative(side);
                BlockState partner = level.getBlockState(partnerPos);
                boolean open = lowerState.getValue(OPEN);
                if (partner.is(this) && partner.getValue(HALF) == DoubleBlockHalf.LOWER && partner.getValue(OPEN) != open) {
                    level.setBlock(partnerPos, partner.setValue(OPEN, open), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
                }
            }
        }
        return result;
    }
}
