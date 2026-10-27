package techguns.block;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import techguns.machine.AmmoBenchMenu;

/**
 * Ammo Bench (Fabric {@code AmmoBenchBlock}): an unpowered stonecutter-style workbench for rounds, facing the placer;
 * keeps no items.
 */
public class AmmoBenchBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<AmmoBenchBlock> CODEC = simpleCodec(AmmoBenchBlock::new);
    private static final Component TITLE = Component.translatable("techguns.container.ammobench");
    /** Legs and table top, back panel at the south side for a north-facing bench (Fabric bounding boxes). */
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            SHAPES.put(direction, Shapes.or(box(direction, 1, 0, 1, 15, 8, 15), box(direction, 0, 8, 0, 16, 10, 16),
                    box(direction, 1, 10, 14, 15, 16, 16)));
        }
    }

    public AmmoBenchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /** A box given for a north-facing bench, turned to {@code direction} (Fabric {@code getHorizonzalRotatedVoxelShape}). */
    private static VoxelShape box(Direction direction, double x0, double y0, double z0, double x1, double y1, double z1) {
        return switch (direction) {
            case SOUTH -> Block.box(x0, y0, 16 - z1, x1, y1, 16 - z0);
            case EAST -> Block.box(16 - z1, y0, x0, 16 - z0, y1, x1);
            case WEST -> Block.box(z0, y0, 16 - x1, z1, y1, 16 - x0);
            default -> Block.box(x0, y0, z0, x1, y1, z1);
        };
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            player.openMenu(state.getMenuProvider(level, pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) -> new AmmoBenchMenu(id, inventory, ContainerLevelAccess.create(level, pos)), TITLE);
    }
}
