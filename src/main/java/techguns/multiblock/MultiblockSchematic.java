package techguns.multiblock;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Shape of a multiblock (Techguns 2 {@code MultiBlockMachineSchematic}): which blocks must stand where around the
 * controller for a direction, the controller excluded. {@code direction} is the controller side the player used; the
 * structure lies behind and beside it.
 */
public interface MultiblockSchematic {
    /** Whether the structure can be formed from the controller's {@code side}. */
    boolean canFormFromSide(Direction side);

    /** The expected parts for a controller at {@code master} formed from {@code direction}. */
    List<MultiblockPart> parts(Level level, BlockPos master, Direction direction);
}
