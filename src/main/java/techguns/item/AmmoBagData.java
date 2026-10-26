package techguns.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import techguns.registry.TGComponents;
import techguns.registry.TGTags;

/**
 * Typed access to the contents of Ammo Bag stacks. All methods are pure logic on {@link ItemStack}s so they can
 * be used from GameTests without a player. Mutating methods only touch the bag stack (and the stack passed for
 * insertion), never the world.
 */
public final class AmmoBagData {
    private AmmoBagData() {
    }

    public static boolean isBag(ItemStack stack) {
        return stack.getItem() instanceof AmmoBagItem;
    }

    /** Whether the stack may be stored in an Ammo Bag (tag {@code techguns:ammo}, can be nested in containers). */
    public static boolean isAmmo(ItemStack stack) {
        return !stack.isEmpty() && stack.is(TGTags.Items.AMMO) && stack.getItem().canFitInsideContainerItems();
    }

    public static AmmoBagContents contents(ItemStack bag) {
        return bag.getOrDefault(TGComponents.AMMO_BAG_CONTENTS.get(), AmmoBagContents.EMPTY);
    }

    public static void setContents(ItemStack bag, AmmoBagContents contents) {
        bag.set(TGComponents.AMMO_BAG_CONTENTS.get(), contents);
    }

    /** Capacity of the bag in rounds, 0 if the stack is not a bag. */
    public static int capacity(ItemStack bag) {
        return bag.getItem() instanceof AmmoBagItem item ? item.capacity() : 0;
    }

    public static int occupancy(ItemStack bag) {
        return contents(bag).occupancy();
    }

    public static int remainingCapacity(ItemStack bag) {
        return Math.max(0, capacity(bag) - occupancy(bag));
    }

    /** Number of items matching {@code wanted} (same item and components) stored in the bag. */
    public static int count(ItemStack bag, ItemStack wanted) {
        return contents(bag).count(wanted);
    }

    /** How many items of {@code stack} would fit into the bag right now. */
    public static int maxAmountToAdd(ItemStack bag, ItemStack stack) {
        if (!isBag(bag) || !isAmmo(stack)) {
            return 0;
        }
        return Math.min(stack.getCount(), remainingCapacity(bag) / AmmoBagContents.unitWeight(stack));
    }

    /**
     * Moves as much of {@code stack} into the bag as fits and shrinks {@code stack} accordingly.
     * Non-ammo stacks are rejected.
     *
     * @return the number of items inserted
     */
    public static int insert(ItemStack bag, ItemStack stack) {
        int amount = maxAmountToAdd(bag, stack);
        if (amount <= 0) {
            return 0;
        }
        List<ItemStack> items = mutableItems(bag);
        int left = amount;
        for (ItemStack existing : items) {
            if (left > 0 && ItemStack.isSameItemSameComponents(existing, stack)) {
                int moved = Math.min(left, existing.getMaxStackSize() - existing.getCount());
                existing.grow(moved);
                left -= moved;
            }
        }
        while (left > 0) {
            ItemStack added = stack.copyWithCount(Math.min(left, stack.getMaxStackSize()));
            left -= added.getCount();
            items.add(added);
        }
        setContents(bag, new AmmoBagContents(items));
        stack.shrink(amount);
        return amount;
    }

    /**
     * Removes up to {@code amount} items matching {@code wanted} from the bag.
     *
     * @return the number of items actually removed
     */
    public static int remove(ItemStack bag, ItemStack wanted, int amount) {
        List<ItemStack> items = mutableItems(bag);
        int left = amount;
        for (ItemStack existing : items) {
            if (left > 0 && ItemStack.isSameItemSameComponents(existing, wanted)) {
                int taken = Math.min(left, existing.getCount());
                existing.shrink(taken);
                left -= taken;
            }
        }
        int removed = amount - left;
        if (removed > 0) {
            items.removeIf(ItemStack::isEmpty);
            setContents(bag, new AmmoBagContents(items));
        }
        return removed;
    }

    /** Removes and returns the first stored stack. */
    public static Optional<ItemStack> removeFirst(ItemStack bag) {
        List<ItemStack> items = mutableItems(bag);
        if (items.isEmpty()) {
            return Optional.empty();
        }
        ItemStack first = items.remove(0);
        setContents(bag, new AmmoBagContents(items));
        return Optional.of(first);
    }

    /** Empties the bag and returns what it contained. */
    public static List<ItemStack> clear(ItemStack bag) {
        List<ItemStack> items = mutableItems(bag);
        if (!items.isEmpty()) {
            setContents(bag, AmmoBagContents.EMPTY);
        }
        return items;
    }

    /** Moves everything from {@code from} into {@code to} as far as it fits. */
    public static void transferAll(ItemStack from, ItemStack to) {
        List<ItemStack> rest = new ArrayList<>();
        for (ItemStack stack : mutableItems(from)) {
            insert(to, stack);
            if (!stack.isEmpty()) {
                rest.add(stack);
            }
        }
        setContents(from, new AmmoBagContents(rest));
    }

    private static List<ItemStack> mutableItems(ItemStack bag) {
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack stack : contents(bag).items()) {
            copy.add(stack.copy());
        }
        return copy;
    }
}
