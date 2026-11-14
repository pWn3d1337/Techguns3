package techguns.transport;

import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.neoforged.neoforge.capabilities.Capabilities;
import techguns.TGConfig;
import techguns.core.transport.Lane;

/**
 * The Techguns pipe kinds, one per lane of the pipe block: fluid pipes (lane A), cables (B), item pipes (C). A new kind
 * (heat, gas) gets an entry here, a lane it shares with a kind it never needs in the same block, textures and lang.
 */
public final class TGPipeKinds {
    private static final IntSupplier ONE = () -> 1;

    public static final PipeKind CABLE = new PipeKind("cable", "cable", Lane.B, Capabilities.EnergyStorage.BLOCK,
            List.of(TGConfig.CABLE_RATE_BASIC::get, TGConfig.CABLE_RATE_ADVANCED::get, TGConfig.CABLE_RATE_ELITE::get),
            List.of(ONE, ONE, ONE), new EnergyLogic());
    public static final PipeKind FLUID_PIPE = new PipeKind("fluid_pipe", "fluid_pipe", Lane.A, Capabilities.FluidHandler.BLOCK,
            List.of(TGConfig.FLUID_PIPE_RATE_BASIC::get, TGConfig.FLUID_PIPE_RATE_ADVANCED::get, TGConfig.FLUID_PIPE_RATE_ELITE::get),
            List.of(ONE, ONE, ONE), new FluidLogic());
    public static final PipeKind ITEM_PIPE = new PipeKind("item_pipe", "item_pipe", Lane.C, Capabilities.ItemHandler.BLOCK,
            List.of(TGConfig.ITEM_PIPE_COUNT_BASIC::get, TGConfig.ITEM_PIPE_COUNT_ADVANCED::get, TGConfig.ITEM_PIPE_COUNT_ELITE::get),
            List.of(TGConfig.ITEM_PIPE_INTERVAL_BASIC::get, TGConfig.ITEM_PIPE_INTERVAL_ADVANCED::get, TGConfig.ITEM_PIPE_INTERVAL_ELITE::get),
            new ItemLogic());

    /**
     * Which kind takes the centre of a block holding several kinds: the first one present. A kind alone in its block is
     * always drawn in the centre; the others move to their own lanes.
     */
    public static final List<PipeKind> CENTER_PRIORITY = List.of(CABLE, ITEM_PIPE, FLUID_PIPE);

    /** All kinds, in lane order. */
    public static final List<PipeKind> ALL = List.of(FLUID_PIPE, CABLE, ITEM_PIPE);
    private static final Map<String, PipeKind> BY_NAME = ALL.stream().collect(Collectors.toMap(PipeKind::name, Function.identity()));

    private TGPipeKinds() {
    }

    /** The lane {@code kind} is drawn in when the kinds {@code present} share its block. */
    public static techguns.core.transport.Lane drawnLane(PipeKind kind, java.util.function.Predicate<PipeKind> present) {
        for (PipeKind first : CENTER_PRIORITY) {
            if (present.test(first)) {
                return first == kind ? techguns.core.transport.Lane.B : kind.lane();
            }
        }
        return techguns.core.transport.Lane.B;
    }

    @Nullable
    public static PipeKind byName(String name) {
        return BY_NAME.get(name);
    }
}
