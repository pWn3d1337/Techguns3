package techguns.world.structure;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * Joins side-by-side chests of a generated structure into double chests. 1.12 chests joined any chest next to them
 * when placed (and turned both halves the same way); 1.21 stores the half in the state ({@link ChestBlock#TYPE}) and
 * only joins on player placement, so a structure built from single chest states keeps two single chests. Template
 * placement re-shapes the placed blocks, which keeps a pair but never forms one.
 */
public final class ChestPairs {
    private ChestPairs() {
    }

    /**
     * The double chest halves for single chests that stand next to a chest of the same block: each chest joins at
     * most one neighbour (east or south first, in x, z, y order). A pair faces the first chest's way if that is
     * across the pair, else the second's, else turns the first's clockwise.
     *
     * @param chests single chest states by position
     * @return the new states of the paired chests
     */
    public static Map<BlockPos, BlockState> pair(Map<BlockPos, BlockState> chests) {
        Map<BlockPos, BlockState> paired = new HashMap<>();
        Set<BlockPos> taken = new HashSet<>();
        List<BlockPos> order = chests.keySet().stream()
                .sorted(Comparator.<BlockPos>comparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ).thenComparingInt(BlockPos::getY)).toList();
        for (BlockPos pos : order) {
            BlockState state = chests.get(pos);
            if (taken.contains(pos) || !isSingleChest(state)) {
                continue;
            }
            for (Direction side : List.of(Direction.EAST, Direction.SOUTH)) {
                BlockPos other = pos.relative(side);
                BlockState otherState = chests.get(other);
                if (otherState == null || taken.contains(other) || !isSingleChest(otherState) || otherState.getBlock() != state.getBlock()) {
                    continue;
                }
                Direction facing = state.getValue(ChestBlock.FACING);
                if (facing.getAxis() == side.getAxis()) {
                    facing = otherState.getValue(ChestBlock.FACING);
                    if (facing.getAxis() == side.getAxis()) {
                        facing = state.getValue(ChestBlock.FACING).getClockWise();
                    }
                }
                // a LEFT half has its partner clockwise of its facing (ChestBlock.getConnectedDirection)
                ChestType type = side == facing.getClockWise() ? ChestType.LEFT : ChestType.RIGHT;
                paired.put(pos, state.setValue(ChestBlock.FACING, facing).setValue(ChestBlock.TYPE, type));
                paired.put(other, otherState.setValue(ChestBlock.FACING, facing).setValue(ChestBlock.TYPE, type.getOpposite()));
                taken.add(pos);
                taken.add(other);
                break;
            }
        }
        return paired;
    }

    private static boolean isSingleChest(BlockState state) {
        return state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) == ChestType.SINGLE;
    }
}
