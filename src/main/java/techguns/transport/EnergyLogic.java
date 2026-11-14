package techguns.transport;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.energy.IEnergyStorage;
import techguns.core.transport.EnergySplit;

/**
 * Cables: input sides pull FE from their block each tick, blocks push into neutral and input sides; either way the FE
 * is split evenly over the delivering endpoints at once, limited by the zones on the way and the endpoint sides.
 */
public final class EnergyLogic implements PipeKindLogic {
    @Override
    public void tick(PipeNetwork network, ServerLevel level, long gameTime) {
        network.refreshBudgets(gameTime);
        for (int i = 0; i < network.endpoints().size(); i++) {
            PipeNetwork.Endpoint source = network.endpoints().get(i);
            if (!source.mode().pulls() || !(source.capability() instanceof IEnergyStorage storage) || !storage.canExtract()) {
                continue;
            }
            PipeNetwork.Budgets budgets = network.budgets(false);
            int available = storage.extractEnergy(budgets.endpoints()[i], true);
            if (available > 0) {
                network.routing = true;
                try {
                    distribute(network, i, available, false, amount -> storage.extractEnergy(amount, false));
                } finally {
                    network.routing = false;
                }
            }
        }
    }

    /**
     * Hands up to {@code amount} FE from endpoint {@code source} to the delivering endpoints, evenly.
     *
     * @param take takes FE from the source for real and returns what it got (identity for pushed FE)
     * @return the FE delivered (or deliverable when simulating)
     */
    static int distribute(PipeNetwork network, int source, int amount, boolean simulate, IntUnaryOperator take) {
        PipeNetwork.Budgets budgets = network.budgets(simulate);
        int n = network.endpoints().size();
        IEnergyStorage[] targets = new IEnergyStorage[n];
        int[] accept = new int[n];
        for (int j = 0; j < n; j++) {
            if (!network.isTarget(source, j) || !(network.endpoints().get(j).capability() instanceof IEnergyStorage target) || !target.canReceive()) {
                continue;
            }
            int[] path = budgets.path(network, source, j);
            int limit = Math.min(amount, budgets.limit(path, source, j));
            if (limit > 0) {
                targets[j] = target;
                accept[j] = target.receiveEnergy(limit, true);
            }
        }
        int[] give = EnergySplit.waterFill(amount, accept);
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
                int taken = take.applyAsInt(share);
                if (taken <= 0) {
                    break;
                }
                moved = targets[j].receiveEnergy(taken, false);
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

    @Override
    public boolean isEndpoint(Object capability) {
        return capability instanceof IEnergyStorage storage && (storage.canReceive() || storage.canExtract());
    }

    /** A cable side a block pushes FE into. */
    private record Port(TGPipeBlockEntity pipe, Direction side) implements IEnergyStorage {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            PipeNetwork network = pipe.network(TGPipeKinds.CABLE);
            if (network == null || network.routing || toReceive <= 0 || pipe.getLevel() == null) {
                return 0;
            }
            int source = network.endpointAt(pipe.getBlockPos(), side);
            if (source < 0) {
                return 0;
            }
            network.refreshBudgets(pipe.getLevel().getGameTime());
            network.routing = true;
            try {
                return distribute(network, source, toReceive, simulate, IntUnaryOperator.identity());
            } finally {
                network.routing = false;
            }
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            PipeNetwork network = pipe.network(TGPipeKinds.CABLE);
            int source = network == null ? -1 : network.endpointAt(pipe.getBlockPos(), side);
            return source < 0 ? 0 : network.kind().rate(network.endpoints().get(source).tier());
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
