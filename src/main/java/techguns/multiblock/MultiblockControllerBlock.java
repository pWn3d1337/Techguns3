package techguns.multiblock;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import techguns.machine.TGMachineBlock;
import techguns.machine.TGMachineBlockEntity;

/**
 * Controller of a multiblock machine (Techguns 2 {@code MultiBlockMachine} controller variants): using an unformed
 * controller (not sneaking) on a valid side forms the structure (Techguns 2 {@code BasicMachine.onBlockActivated});
 * otherwise it opens the machine menu. Formed, its renderer draws the whole machine.
 */
public class MultiblockControllerBlock extends TGMachineBlock {
    private final MapCodec<MultiblockControllerBlock> codec;

    /** Techguns 2: multiblock masters are neither turned nor dismantled by a wrench. */
    @Override
    public boolean canBeWrenched() {
        return false;
    }

    public MultiblockControllerBlock(Properties properties, Supplier<? extends BlockEntityType<? extends TGMachineBlockEntity>> type) {
        super(properties, type, Shapes.block(), RenderShape.MODEL);
        this.codec = simpleCodec(p -> new MultiblockControllerBlock(p, type));
        registerDefaultState(defaultBlockState().setValue(MultiblockPartBlock.FORMED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MultiblockPartBlock.FORMED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof MultiblockMasterBlockEntity master && !master.isFormed() && !player.isShiftKeyDown()
                && master.schematic().canFormFromSide(hit.getDirection())) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && master.mayAccess(player)) {
                master.tryForm(serverPlayer, hit.getDirection());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MultiblockMasterBlockEntity master) {
            master.unform();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
