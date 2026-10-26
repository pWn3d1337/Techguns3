package techguns.item;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Immutable contents of an Ammo Bag (the value of the {@code ammo_bag_contents} data component).
 * <p>
 * Capacity is measured in "rounds": a full stack of any item fills {@link #FULL_STACK_WEIGHT} of the bag,
 * so a bag of capacity 256 holds 4 full stacks (e.g. 256 pistol rounds, or 64 magazines with stack size 16).
 * Use {@link AmmoBagData} to read and modify the contents of a bag stack.
 */
// MIGRATION(26.x): own record instead of vanilla BundleContents (its capacity is hardcoded and its API changed in 1.21.2+).
public record AmmoBagContents(List<ItemStack> items) {
    public static final int FULL_STACK_WEIGHT = 64;

    public static final AmmoBagContents EMPTY = new AmmoBagContents(List.of());

    public static final Codec<AmmoBagContents> CODEC = ItemStack.CODEC.listOf().xmap(AmmoBagContents::new, AmmoBagContents::items);
    public static final StreamCodec<RegistryFriendlyByteBuf, AmmoBagContents> STREAM_CODEC =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(AmmoBagContents::new, AmmoBagContents::items);

    public AmmoBagContents {
        List<ItemStack> copy = new ArrayList<>(items.size());
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                copy.add(stack.copy());
            }
        }
        items = List.copyOf(copy);
    }

    /** Capacity points taken by one item of the given stack (stack size 64 = 1, 16 = 4, 1 = 64). */
    public static int unitWeight(ItemStack stack) {
        return Math.max(1, FULL_STACK_WEIGHT / Math.max(1, stack.getMaxStackSize()));
    }

    /** Capacity points taken by the stack (count times unit weight). */
    public static int weight(ItemStack stack) {
        return stack.getCount() * unitWeight(stack);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Total capacity points taken by the contents. */
    public int occupancy() {
        int sum = 0;
        for (ItemStack stack : items) {
            sum += weight(stack);
        }
        return sum;
    }

    /** Number of items matching {@code wanted} (same item and components). */
    public int count(ItemStack wanted) {
        int sum = 0;
        for (ItemStack stack : items) {
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                sum += stack.getCount();
            }
        }
        return sum;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof AmmoBagContents contents && ItemStack.listMatches(items, contents.items);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(items);
    }
}
