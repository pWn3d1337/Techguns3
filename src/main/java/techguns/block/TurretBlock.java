package techguns.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import techguns.machine.TGMachineBlock;
import techguns.machine.TurretBlockEntity;
import techguns.registry.TGBlockEntities;

/**
 * Turret base (Techguns 2 {@code BasicMachine} type TURRET): the turret stands on top, or hangs below a base placed
 * against a ceiling ({@link #HANGING}; Techguns 2 turned it with a wrench). The base and the turret are drawn by
 * renderers.
 */
public class TurretBlock extends TGMachineBlock {
    public static final BooleanProperty HANGING = BooleanProperty.create("hanging");
    public static final MapCodec<TurretBlock> CODEC = simpleCodec(TurretBlock::new);

    public TurretBlock(Properties properties) {
        super(properties, TGBlockEntities.TURRET, Shapes.block(), RenderShape.ENTITYBLOCK_ANIMATED);
        registerDefaultState(defaultBlockState().setValue(HANGING, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANGING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(HANGING, context.getClickedFace() == Direction.DOWN);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TGBlockEntities.TURRET.get(), TurretBlockEntity::serverTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TurretBlockEntity turret) {
            turret.removeTurret();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
