package techguns.transport;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import techguns.core.transport.SideMode;
import techguns.registry.TGItems;

/**
 * The shared block of all Techguns pipes: holds up to three segments of different kinds ({@link TGPipeBlockEntity}).
 * Breaking removes the looked-at segment only, the block goes with the last one; a wrench on an arm or plate changes
 * that side's mode, sneak + wrench takes the segment out. Drawn by the pipe model from the block entity's model data.
 */
public class TGPipeBlock extends BaseEntityBlock {
    public static final MapCodec<TGPipeBlock> CODEC = simpleCodec(TGPipeBlock::new);

    public TGPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TGPipeBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return level.getBlockEntity(pos) instanceof TGPipeBlockEntity pipe ? pipe.shape() : super.getShape(state, level, pos, context);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level instanceof ServerLevel server) {
            TransportManager.get(server).markDirty(pos);
        }
    }

    public static ItemStack itemOf(PipeSegment segment) {
        return new ItemStack(TGItems.pipe(segment.kind(), segment.tier()).get());
    }

    /** Breaking takes out the segment the player looks at; the block stays while other segments are left. */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level.getBlockEntity(pos) instanceof TGPipeBlockEntity pipe && pipe.segments().size() > 1) {
            PipeHit hit = PipeHit.pick(pipe, player);
            PipeSegment segment = hit != null ? hit.segment() : pipe.segments().getFirst();
            if (!level.isClientSide) {
                takeOut(level, pos, pipe, segment, player);
            }
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    /** Removes {@code segment} and drops its item (none in creative). */
    private static void takeOut(Level level, BlockPos pos, TGPipeBlockEntity pipe, PipeSegment segment, Player player) {
        pipe.removeSegment(segment.kind());
        if (!player.getAbilities().instabuild) {
            popResource(level, pos, itemOf(segment));
        }
        SoundType sound = SoundType.METAL;
        level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0f) / 2.0f, sound.getPitch() * 0.8f);
        if (pipe.isEmpty()) {
            level.removeBlock(pos, false);
        }
    }

    /** The items of every segment (the last segment broken by a player, explosions, other mods breaking it). */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TGPipeBlockEntity pipe) {
            pipe.segments().forEach(segment -> drops.add(itemOf(segment)));
        }
        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof TGPipeBlockEntity pipe && !pipe.isEmpty()) {
            PipeHit hit = PipeHit.pick(pipe, player);
            return itemOf(hit != null ? hit.segment() : pipe.segments().getFirst());
        }
        return ItemStack.EMPTY;
    }

    /**
     * Wrench use: a side linked to another pipe toggles connected/disabled at both ends, a side towards a block cycles
     * neutral, output, input, disabled; sneaking takes the segment out as an item.
     */
    public void wrench(Level level, BlockPos pos, Player player, boolean sneaking) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof TGPipeBlockEntity pipe)) {
            return;
        }
        PipeHit hit = PipeHit.pick(pipe, player);
        if (hit == null) {
            return;
        }
        PipeSegment segment = hit.segment();
        PipeKind kind = segment.kind();
        if (sneaking) {
            pipe.removeSegment(kind);
            ItemStack item = itemOf(segment);
            if (!player.getAbilities().instabuild && !player.addItem(item)) {
                popResource(level, pos, item);
            }
            if (pipe.isEmpty()) {
                level.removeBlock(pos, false);
            }
            return;
        }
        Direction side = hit.side();
        SideMode mode = segment.mode(side);
        BlockPos next = pos.relative(side);
        SideMode newMode;
        if (level.getBlockEntity(next) instanceof TGPipeBlockEntity other && other.segment(kind) != null) {
            newMode = mode.connects() ? SideMode.DISABLED : SideMode.NEUTRAL;
            other.setMode(kind, side.getOpposite(), newMode);
        } else {
            newMode = mode.next();
        }
        pipe.setMode(kind, side, newMode);
        player.displayClientMessage(Component.translatable("techguns.pipe.mode", Component.translatable("techguns.side." + side.getName()),
                Component.translatable("techguns.pipe.mode." + newMode.name().toLowerCase(java.util.Locale.ROOT))
                        .withStyle(modeColor(newMode))), true);
    }

    private static ChatFormatting modeColor(SideMode mode) {
        return switch (mode) {
            case NEUTRAL -> ChatFormatting.GRAY;
            case OUTPUT -> ChatFormatting.GOLD;
            case INPUT -> ChatFormatting.BLUE;
            case DISABLED -> ChatFormatting.DARK_RED;
        };
    }
}
