package techguns.transport;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import techguns.registry.TGBlocks;

/**
 * A cable or pipe of one kind and tier. Used on a pipe block without this kind it adds the segment there (stacking
 * kinds in one block); on a segment of the same kind but another tier it swaps the tier and gives the old one back;
 * otherwise it places a new pipe block (or adds to the pipe block) next to the clicked face.
 */
public class PipeItem extends Item {
    private final PipeKind kind;
    private final int tier;

    public PipeItem(Properties properties, PipeKind kind, int tier) {
        super(properties);
        this.kind = kind;
        this.tier = tier;
    }

    public PipeKind kind() {
        return kind;
    }

    public int tier() {
        return tier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        Player player = context.getPlayer();
        if (!(player != null && player.isSecondaryUseActive()) && level.getBlockEntity(clicked) instanceof TGPipeBlockEntity pipe) {
            InteractionResult result = useOnPipe(context, pipe);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        BlockPlaceContext place = new BlockPlaceContext(context);
        BlockPos target = place.getClickedPos();
        if (level.getBlockEntity(target) instanceof TGPipeBlockEntity pipe) {
            InteractionResult result = useOnPipe(context, pipe);
            return result == InteractionResult.PASS ? InteractionResult.FAIL : result;
        }
        BlockState state = TGBlocks.PIPE.get().defaultBlockState();
        if (!place.canPlace() || !level.isUnobstructed(state, target, player == null ? CollisionContext.empty() : CollisionContext.of(player))) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            level.setBlock(target, state, Block.UPDATE_ALL);
            if (level.getBlockEntity(target) instanceof TGPipeBlockEntity pipe) {
                pipe.addSegment(kind, tier);
            }
            placed(level, target, player, context.getItemInHand());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Adds this kind to {@code pipe} or upgrades it; PASS if the pipe already has this kind and tier. */
    private InteractionResult useOnPipe(UseOnContext context, TGPipeBlockEntity pipe) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        PipeSegment present = pipe.segment(kind);
        if (present != null && present.tier() == tier) {
            return InteractionResult.PASS;
        }
        if (present == null && !pipe.canHold(kind)) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.translatable("techguns.pipe.lane_taken").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            if (present == null) {
                pipe.addSegment(kind, tier);
            } else {
                int old = pipe.setTier(kind, tier);
                ItemStack oldItem = new ItemStack(techguns.registry.TGItems.pipe(kind, old).get());
                if (player != null && !player.getAbilities().instabuild && !player.addItem(oldItem)) {
                    Block.popResource(level, pipe.getBlockPos(), oldItem);
                }
            }
            placed(level, pipe.getBlockPos(), player, context.getItemInHand());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void placed(Level level, BlockPos pos, Player player, ItemStack stack) {
        SoundType sound = SoundType.METAL;
        level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0f) / 2.0f, sound.getPitch() * 0.8f);
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int rate;
        try {
            rate = kind.rate(tier);
        } catch (IllegalStateException configNotLoaded) {
            return;
        }
        if (kind == TGPipeKinds.ITEM_PIPE) {
            tooltip.add(Component.translatable("tooltip.techguns.pipe.rate.item_pipe", rate, kind.interval(tier)).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.techguns.pipe.rate." + kind.name(), rate).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltip.techguns.pipe.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
