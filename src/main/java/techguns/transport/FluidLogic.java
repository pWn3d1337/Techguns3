package techguns.transport;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import techguns.core.transport.EnergySplit;

/**
 * Fluid pipes: like cables with millibuckets. Input sides drain their block each tick, blocks push into neutral and
 * input sides; the fluid is split evenly over the delivering endpoints that take it. Pipes hold no fluid, so different
 * fluids never mix.
 */
public final class FluidLogic implements PipeKindLogic {
    @Override
    public void tick(PipeNetwork network, ServerLevel level, long gameTime) {
        network.refreshBudgets(gameTime);
        for (int i = 0; i < network.endpoints().size(); i++) {
            PipeNetwork.Endpoint source = network.endpoints().get(i);
            if (!source.mode().pulls() || !(source.capability() instanceof IFluidHandler handler)) {
                continue;
            }
            FluidStack available = handler.drain(network.budgets(false).endpoints()[i], FluidAction.SIMULATE);
            if (available.isEmpty()) {
                continue;
            }
            network.routing = true;
            try {
                distribute(network, i, available, false, stack -> handler.drain(stack, FluidAction.EXECUTE), handler);
            } finally {
                network.routing = false;
            }
        }
    }

    /**
     * Hands up to {@code offer} from endpoint {@code source} to the delivering endpoints, evenly.
     *
     * @param take   drains the source for real (identity for pushed fluid)
     * @param giveBack where fluid a target unexpectedly refused goes back to, or null
     * @return millibuckets delivered (or deliverable when simulating)
     */
    static int distribute(PipeNetwork network, int source, FluidStack offer, boolean simulate, Function<FluidStack, FluidStack> take,
                          @Nullable IFluidHandler giveBack) {
        PipeNetwork.Budgets budgets = network.budgets(simulate);
        int n = network.endpoints().size();
        IFluidHandler[] targets = new IFluidHandler[n];
        int[] accept = new int[n];
        for (int j = 0; j < n; j++) {
            if (!network.isTarget(source, j) || !(network.endpoints().get(j).capability() instanceof IFluidHandler target)) {
                continue;
            }
            int[] path = budgets.path(network, source, j);
            int limit = Math.min(offer.getAmount(), budgets.limit(path, source, j));
            if (limit > 0) {
                targets[j] = target;
                accept[j] = target.fill(offer.copyWithAmount(limit), FluidAction.SIMULATE);
            }
        }
        int[] give = EnergySplit.waterFill(offer.getAmount(), accept);
        int total = 0;
        for (int j = 0; j < n; j++) {
            if (give[j] <= 0) {
                continue;
            }
            int[] path = budgets.path(network, source, j);
            int share = Math.min(give[j], budgets.limit(path, source, j));
            if (share <= 0) {
                continue;
            }
            int moved;
            if (simulate) {
                moved = share;
            } else {
                FluidStack taken = take.apply(offer.copyWithAmount(share));
                if (taken.isEmpty()) {
                    break;
                }
                moved = targets[j].fill(taken, FluidAction.EXECUTE);
                if (moved < taken.getAmount() && giveBack != null) {
                    giveBack.fill(taken.copyWithAmount(taken.getAmount() - moved), FluidAction.EXECUTE);
                }
            }
            budgets.use(path, source, j, moved);
            total += moved;
        }
        return total;
    }

    @Nullable
    @Override
    public Object port(TGPipeBlockEntity pipe, Direction side) {
        return new Port(pipe, side);
    }

    /** A fluid pipe side a block pushes fluid into; nothing can be drained from it. */
    private record Port(TGPipeBlockEntity pipe, Direction side) implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            PipeNetwork network = pipe.network(TGPipeKinds.FLUID_PIPE);
            int source = network == null ? -1 : network.endpointAt(pipe.getBlockPos(), side);
            return source < 0 ? 0 : network.kind().rate(network.endpoints().get(source).tier());
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            PipeNetwork network = pipe.network(TGPipeKinds.FLUID_PIPE);
            if (network == null || network.routing || resource.isEmpty() || pipe.getLevel() == null) {
                return 0;
            }
            int source = network.endpointAt(pipe.getBlockPos(), side);
            if (source < 0) {
                return 0;
            }
            network.refreshBudgets(pipe.getLevel().getGameTime());
            network.routing = true;
            try {
                return distribute(network, source, resource, action.simulate(), Function.identity(), null);
            } finally {
                network.routing = false;
            }
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
