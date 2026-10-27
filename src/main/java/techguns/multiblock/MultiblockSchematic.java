package techguns.multiblock;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * Shape of a multiblock (Techguns 2 {@code MultiBlockMachineSchematic}): which blocks must stand where around the
 * controller for a direction, the controller excluded. {@code direction} is the controller side the player used; the
 * structure lies behind and beside it.
 */
public interface MultiblockSchematic {
    /** Whether the structure can be formed from the controller's {@code side}. */
    boolean canFormFromSide(Direction side);

    /** Why the structure can't form at all (sizes, missing target block), checked before the parts; empty if fine. */
    default Optional<Component> check(Level level, BlockPos master, Direction direction) {
        return Optional.empty();
    }

    /** The expected parts for a controller at {@code master} formed from {@code direction}. */
    List<MultiblockPart> parts(Level level, BlockPos master, Direction direction);
}
