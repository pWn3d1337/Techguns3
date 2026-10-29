package techguns.multiblock;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import techguns.registry.TGBlocks;
import techguns.registry.TGTags;

/**
 * Techguns 2 {@code ItemBlockOreDrill} placement rules: a drill rod goes next to an ore cluster that has no drill rod
 * yet, or next to another rod; an ore drill controller not next to another controller. A refused placement shows why.
 */
public class OreDrillPartItem extends BlockItem {
    /** Upper bound of the cluster flood fill (clusters are a few dozen blocks). */
    private static final int MAX_CLUSTER = 4096;

    public OreDrillPartItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        String error = placementError(context.getLevel(), context.getClickedPos(), state);
        if (error != null) {
            Player player = context.getPlayer();
            if (player != null && context.getLevel().isClientSide) {
                player.displayClientMessage(Component.translatable(error), true);
            }
            return false;
        }
        return super.canPlace(context, state);
    }

    /** Lang key of the reason {@code state} may not go to {@code pos}, or null. */
    @Nullable
    public static String placementError(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.is(TGBlocks.ORE_DRILL_ROD.get())) {
            BlockPos cluster = neighbour(level, pos, TGTags.Blocks.ORE_CLUSTERS);
            if (cluster != null) {
                return hasRod(level, cluster) ? "techguns.msg.error.alreadyrod" : null;
            }
            return touches(level, pos, TGBlocks.ORE_DRILL_ROD.get()) ? null : "techguns.msg.error.rodplacement";
        }
        if (state.is(TGBlocks.ORE_DRILL_CONTROLLER.get()) && touches(level, pos, TGBlocks.ORE_DRILL_CONTROLLER.get())) {
            return "techguns.msg.error.oredrill.controllernext";
        }
        return null;
    }

    @Nullable
    private static BlockPos neighbour(BlockGetter level, BlockPos pos, net.minecraft.tags.TagKey<Block> tag) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(tag)) {
                return pos.relative(direction);
            }
        }
        return null;
    }

    private static boolean touches(BlockGetter level, BlockPos pos, Block block) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(block)) {
                return true;
            }
        }
        return false;
    }

    /** Techguns 2 {@code getHasConnectedDrillRod}: a drill rod touches the cluster (its connected blocks of one kind). */
    private static boolean hasRod(BlockGetter level, BlockPos start) {
        Block cluster = level.getBlockState(start).getBlock();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> open = new ArrayDeque<>();
        open.add(start);
        visited.add(start);
        while (!open.isEmpty() && visited.size() < MAX_CLUSTER) {
            BlockPos pos = open.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                BlockState state = level.getBlockState(next);
                if (state.is(TGBlocks.ORE_DRILL_ROD.get())) {
                    return true;
                }
                if (state.is(cluster) && visited.add(next)) {
                    open.add(next);
                }
            }
        }
        return false;
    }
}
