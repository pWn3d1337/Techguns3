package techguns.machine;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import techguns.registry.TGBlockEntities;

/**
 * Wind Turbine (Techguns 3): a vertical-axis rotor segment with one helical winding per block. Segments stack into a
 * column; the lowest one ({@link #DOWN} false) is the generator that makes the column's power and pushes it out through
 * its sides and bottom ({@link WindTurbineBlockEntity}). Columns need room: a turbine cannot be placed right next to
 * another one. Drawn by its block entity renderer; use opens the column's screen.
 */
public class WindTurbineBlock extends BaseEntityBlock {
    public static final MapCodec<WindTurbineBlock> CODEC = simpleCodec(WindTurbineBlock::new);
    /** A turbine segment above / below this one. */
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public WindTurbineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Whether a turbine stands right next to {@code pos} (north, east, south or west). */
    public static boolean crowded(LevelReader level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(side)).getBlock() instanceof WindTurbineBlock) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (crowded(level, pos)) {
            Player player = context.getPlayer();
            if (player != null && level.isClientSide) {
                player.displayClientMessage(Component.translatable("techguns.wind_turbine.too_close"), true);
            }
            return null;
        }
        return defaultBlockState().setValue(UP, level.getBlockState(pos.above()).getBlock() instanceof WindTurbineBlock)
                .setValue(DOWN, level.getBlockState(pos.below()).getBlock() instanceof WindTurbineBlock);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (side == Direction.UP) {
            return state.setValue(UP, neighbor.getBlock() instanceof WindTurbineBlock);
        }
        if (side == Direction.DOWN) {
            boolean down = neighbor.getBlock() instanceof WindTurbineBlock;
            if (down != state.getValue(DOWN) && level instanceof Level real) {
                // only the lowest segment offers the column's energy
                real.invalidateCapabilities(pos);
            }
            return state.setValue(DOWN, down);
        }
        return state;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WindTurbineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // only the lowest segment of a column ticks
        if (state.getValue(DOWN)) {
            return null;
        }
        return level.isClientSide
                ? createTickerHelper(type, TGBlockEntities.WIND_TURBINE.get(), (l, p, s, turbine) -> turbine.clientTick())
                : createTickerHelper(type, TGBlockEntities.WIND_TURBINE.get(), (l, p, s, turbine) -> turbine.serverTick());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof WindTurbineBlockEntity turbine) {
            WindTurbineBlockEntity base = turbine.base();
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, p) -> new WindTurbineMenu(id, inventory, base), getName()),
                    buf -> buf.writeBlockPos(base.getBlockPos()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
