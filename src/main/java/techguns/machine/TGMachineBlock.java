package techguns.machine;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A single-block Techguns machine (Techguns 2 {@code BasicMachine} / {@code SimpleMachine} variants, one block each):
 * faces away from the placer, belongs to the placer, opens its menu on use, drops its inventory when broken. The look
 * comes from the block entity renderer.
 */
public class TGMachineBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private final Supplier<? extends BlockEntityType<? extends TGMachineBlockEntity>> type;
    private final VoxelShape shape;
    private final RenderShape renderShape;
    private final MapCodec<TGMachineBlock> codec;

    /**
     * @param renderShape {@code ENTITYBLOCK_ANIMATED} for machines drawn only by their renderer (Techguns 2 TESR models),
     *                    {@code MODEL} for block models with renderer extras
     */
    public TGMachineBlock(Properties properties, Supplier<? extends BlockEntityType<? extends TGMachineBlockEntity>> type, VoxelShape shape,
                          RenderShape renderShape) {
        super(properties);
        this.type = type;
        this.shape = shape;
        this.renderShape = renderShape;
        this.codec = simpleCodec(p -> new TGMachineBlock(p, type, shape, renderShape));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    /** A full block machine drawn by its renderer. */
    public TGMachineBlock(Properties properties, Supplier<? extends BlockEntityType<? extends TGMachineBlockEntity>> type) {
        this(properties, type, Shapes.block(), RenderShape.ENTITYBLOCK_ANIMATED);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * Techguns 2: renderer-drawn machines store the placer's look direction (yaw quadrant); block model machines
     * ({@code SimpleMachine}) face the placer.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction look = context.getHorizontalDirection();
        return defaultBlockState().setValue(FACING, renderShape == RenderShape.MODEL ? look.getOpposite() : look);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return renderShape;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return type.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType != type.get() ? null
                : level.isClientSide ? (l, p, s, be) -> TGMachineBlockEntity.clientTick(l, p, s, (TGMachineBlockEntity) be)
                : (l, p, s, be) -> TGMachineBlockEntity.serverTick(l, p, s, (TGMachineBlockEntity) be);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player && level.getBlockEntity(pos) instanceof TGMachineBlockEntity machine) {
            machine.setOwner(player);
            machine.updateSignal();
        }
    }

    /** Fluid containers go into / out of the machine's tanks (Techguns 2 {@code onFluidContainerInteract}). */
    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                                                 net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TGMachineBlockEntity machine && machine.mayAccess(player)
                && (level.isClientSide ? machine.canUseItem(stack) : machine.useItem(player, hand))) {
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TGMachineBlockEntity machine)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (machine.mayAccess(player)) {
                serverPlayer.openMenu(machine, buf -> buf.writeBlockPos(pos));
            } else {
                denied(player, machine);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /** Whether a wrench turns and dismantles this machine (Techguns 2 {@code canBeWrenchRotated / Dismantled}). */
    public boolean canBeWrenched() {
        return true;
    }

    /** Wrench use on face {@code face} (Techguns 2 {@code rotateBlock}): turns the machine a quarter clockwise. */
    public void turn(Level level, BlockPos pos, BlockState state, Player player, Direction face) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof TGMachineBlockEntity machine)) {
            return;
        }
        if (machine.mayAccess(player)) {
            turn(level, pos, state, machine, face);
        } else {
            denied(player, machine);
        }
    }

    /** Turns the machine for a wrench use on {@code face}; the player may access it. */
    protected void turn(Level level, BlockPos pos, BlockState state, TGMachineBlockEntity machine, Direction face) {
        level.setBlock(pos, state.rotate(level, pos, Rotation.CLOCKWISE_90), Block.UPDATE_ALL);
    }

    /**
     * Wrench sneak + use (Techguns 2 dismantling): removes the machine and drops it as an item that keeps its contents,
     * power and settings.
     */
    public void dismantle(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof TGMachineBlockEntity machine)) {
            return;
        }
        if (!machine.mayAccess(player)) {
            denied(player, machine);
            return;
        }
        machine.beforeDismantle();
        ItemStack item = new ItemStack(this);
        machine.saveToItem(item, level.registryAccess());
        // the contents travel in the item; breaking must not drop them as well
        machine.clearContents();
        level.removeBlock(pos, false);
        popResource(level, pos, item);
    }

    private static void denied(Player player, TGMachineBlockEntity machine) {
        player.displayClientMessage(Component.translatable("techguns.container.security.denied", machine.getDisplayName())
                .withStyle(ChatFormatting.RED), true);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.getBlockEntity(pos) instanceof TGMachineBlockEntity machine) {
            machine.updateSignal();
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TGMachineBlockEntity machine) {
            machine.dropContents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
