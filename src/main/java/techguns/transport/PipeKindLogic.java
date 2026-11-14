package techguns.transport;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/** How a network of one {@link PipeKind} moves things: no buffers, everything moves within the call. */
public interface PipeKindLogic {
    /** Active work of a network once per tick: pulls from its input endpoints and delivers. */
    void tick(PipeNetwork network, ServerLevel level, long gameTime);

    /**
     * The capability object the pipe at {@code pos} offers on {@code side} to the block there, which pushes into it;
     * the network routes what comes in right away. Null if nothing can be pushed in.
     */
    @Nullable
    Object port(TGPipeBlockEntity pipe, Direction side);

    /** Whether a neighbour's capability object is worth connecting to (it can take or give something). */
    default boolean isEndpoint(Object capability) {
        return capability != null;
    }

    /** Neighbours of a pipe this network delivers to: the pipe's own position and side are not targets of its port. */
    static boolean sameSide(PipeNetwork.Endpoint endpoint, BlockPos pos, Direction side) {
        return endpoint.side() == side && endpoint.pipePos().equals(pos);
    }
}
