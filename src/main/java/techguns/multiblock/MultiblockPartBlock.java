package techguns.multiblock;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A structure block of a multiblock machine (Techguns 2 {@code MultiBlockMachine} housing and glass variants, one
 * block each). Formed it shows no model of its own (the controller's renderer draws the machine), opens the master's
 * menu on use and unforms the structure when broken.
 */
public class MultiblockPartBlock extends BaseEntityBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final MapCodec<MultiblockPartBlock> CODEC = simpleCodec(MultiblockPartBlock::new);

    public MultiblockPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    /** The state of a part as linked into a formed structure. */
    public BlockState formedState(BlockState state, MultiblockPart part) {
        return state.setValue(FORMED, true);
    }

    /** The state of a part after its structure unformed. */
    public BlockState unformedState(BlockState state) {
        return state.setValue(FORMED, false);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiblockPartBlockEntity(pos, state);
    }

    @Nullable
    private static MultiblockMasterBlockEntity master(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity part ? part.master() : null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        MultiblockMasterBlockEntity master = state.getValue(FORMED) ? master(level, pos) : null;
        return master != null ? master.partShape(pos) : Shapes.block();
    }

    /**
     * Formed parts stay solid blocks: the slimmer {@link MultiblockMasterBlockEntity#partShape} (Techguns 2
     * {@code getBBforSlave}) is only the outline, else players walk into the structure's blocks.
     */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    /** Techguns 2: using a formed part opens the master's menu. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        MultiblockMasterBlockEntity master = state.getValue(FORMED) ? master(level, pos) : null;
        if (master == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && master.mayAccess(player)) {
            BlockPos masterPos = master.getBlockPos();
            serverPlayer.openMenu(master, buf -> buf.writeBlockPos(masterPos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            MultiblockMasterBlockEntity master = master(level, pos);
            if (master != null) {
                master.unform();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
