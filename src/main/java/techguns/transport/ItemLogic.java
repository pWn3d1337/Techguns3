package techguns.transport;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Item pipes: no items travel or render. Every {@code interval} ticks of its tier an input side takes up to its tier's
 * count from one slot of its block and puts it straight into the delivering endpoints, the nearest first, starting one
 * further each operation (round robin). Zones limit how many items pass them per tick; blocks pushing into neutral or
 * input sides (hoppers) are routed the same way at once.
 */
public final class ItemLogic implements PipeKindLogic {
    @Override
    public void tick(PipeNetwork network, ServerLevel level, long gameTime) {
        network.refreshBudgets(gameTime);
        for (int i = 0; i < network.endpoints().size(); i++) {
            PipeNetwork.Endpoint source = network.endpoints().get(i);
            if (!source.mode().pulls() || (gameTime + network.id()) % network.kind().interval(source.tier()) != 0
                    || !(source.capability() instanceof IItemHandler handler) || handler.getSlots() == 0) {
                continue;
            }
            int count = Math.min(network.kind().rate(source.tier()), network.budgets(false).endpoints()[i]);
            if (count <= 0) {
                continue;
            }
            int slots = handler.getSlots();
            int start = network.slotCursor(i) % slots;
            for (int k = 0; k < slots; k++) {
                int slot = (start + k) % slots;
                ItemStack available = handler.extractItem(slot, count, true);
                if (available.isEmpty()) {
                    continue;
                }
                network.routing = true;
                int moved;
                try {
                    moved = route(network, i, available, false, level, handler, slot);
                } finally {
                    network.routing = false;
                }
                if (moved > 0) {
                    network.setSlotCursor(i, slot);
                    break;
                }
            }
        }
    }

    /**
     * Routes {@code stack} from endpoint {@code source} to the delivering endpoints, nearest first, round robin.
     *
     * @param from the source inventory and slot to extract from; null when the items are pushed in
     * @return the number of items delivered (or deliverable when simulating)
     */
    static int route(PipeNetwork network, int source, ItemStack stack, boolean simulate, ServerLevel level,
                     @Nullable IItemHandler from, int slot) {
        PipeNetwork.Budgets budgets = network.budgets(simulate);
        int[] order = network.targetsByDistance(network.endpoints().get(source).pipePos());
        if (order.length == 0) {
            return 0;
        }
        int remaining = stack.getCount();
        int start = network.targetCursor(source) % order.length;
        int total = 0;
        for (int k = 0; k < order.length && remaining > 0; k++) {
            int pos = (start + k) % order.length;
            int j = order[pos];
            if (!network.isTarget(source, j) || !(network.endpoints().get(j).capability() instanceof IItemHandler target)) {
                continue;
            }
            int[] path = budgets.path(network, source, j);
            int allowed = Math.min(remaining, budgets.limit(path, source, j));
            if (allowed <= 0) {
                continue;
            }
            ItemStack rest = ItemHandlerHelper.insertItemStacked(target, stack.copyWithCount(allowed), true);
            int accepted = allowed - rest.getCount();
            if (accepted <= 0) {
                continue;
            }
            int moved = accepted;
            if (!simulate) {
                ItemStack taken = from != null ? from.extractItem(slot, accepted, false) : stack.copyWithCount(accepted);
                if (taken.isEmpty()) {
                    break;
                }
                ItemStack left = ItemHandlerHelper.insertItemStacked(target, taken, false);
                moved = taken.getCount() - left.getCount();
                // pushed items the target refused stay with the pusher (only the moved count is reported)
                if (!left.isEmpty() && from != null) {
                    giveBack(network, source, level, from, slot, left);
                }
                network.setTargetCursor(source, pos + 1);
            }
            budgets.use(path, source, j, moved);
            remaining -= moved;
            total += moved;
        }
        return total;
    }

    /** Items a target refused after all go back to their inventory, else they drop at the pipe. */
    private static void giveBack(PipeNetwork network, int source, ServerLevel level, IItemHandler from, int slot, ItemStack left) {
        left = from.insertItem(slot, left, false);
        if (!left.isEmpty()) {
            left = ItemHandlerHelper.insertItemStacked(from, left, false);
        }
        if (!left.isEmpty()) {
            BlockPos pos = network.endpoints().get(source).pipePos();
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, left);
        }
    }

    @Nullable
    @Override
    public Object port(TGPipeBlockEntity pipe, Direction side) {
        return new Port(pipe, side);
    }

    /** An item pipe side a block (a hopper) pushes items into; it looks like one empty slot. */
    private record Port(TGPipeBlockEntity pipe, Direction side) implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            PipeNetwork network = pipe.network(TGPipeKinds.ITEM_PIPE);
            if (network == null || network.routing || stack.isEmpty() || !(pipe.getLevel() instanceof ServerLevel level)) {
                return stack;
            }
            int source = network.endpointAt(pipe.getBlockPos(), side);
            if (source < 0) {
                return stack;
            }
            network.refreshBudgets(level.getGameTime());
            network.routing = true;
            int moved;
            try {
                moved = route(network, source, stack, simulate, level, null, 0);
            } finally {
                network.routing = false;
            }
            return moved <= 0 ? stack : stack.copyWithCount(stack.getCount() - moved);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }
}
