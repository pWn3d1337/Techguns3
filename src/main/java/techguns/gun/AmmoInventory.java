package techguns.gun;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.item.AmmoBagData;

/**
 * Finding, consuming and returning ammo in a player's inventory, including ammo stored in Ammo Bags
 * (design doc §4.4). Loose items are used first, then bags.
 */
public final class AmmoInventory {
    private AmmoInventory() {
    }

    /**
     * Consumes one of each required ammo item. Creative players always succeed without consuming.
     *
     * @return true if all items were available (and consumed)
     */
    public static boolean consume(Player player, List<ItemStack> ammo) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        for (ItemStack required : ammo) {
            if (count(player, required) < required.getCount()) {
                return false;
            }
        }
        for (ItemStack required : ammo) {
            remove(player, required, required.getCount());
        }
        return true;
    }

    /** Whether the player carries one of each required ammo item (always true in creative). */
    public static boolean has(Player player, List<ItemStack> ammo) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        return ammo.stream().allMatch(required -> count(player, required) >= required.getCount());
    }

    /** Gives items to the player: into Ammo Bags first if possible, the rest into the inventory or dropped (server side only). */
    public static void give(Player player, ItemStack stack) {
        if (stack.isEmpty() || player.getAbilities().instabuild) {
            return;
        }
        ItemStack rest = stack.copy();
        insertIntoBags(player, rest);
        if (!rest.isEmpty() && !player.getInventory().add(rest) && !player.level().isClientSide) {
            player.drop(rest, false);
        }
    }

    /**
     * Moves as much of {@code stack} as fits into the Ammo Bags in the player's inventory (non-ammo is ignored)
     * and shrinks {@code stack} by that amount.
     *
     * @return the number of items inserted
     */
    public static int insertIntoBags(Player player, ItemStack stack) {
        int inserted = 0;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize() && !stack.isEmpty(); i++) {
            ItemStack bag = inventory.getItem(i);
            if (AmmoBagData.isBag(bag)) {
                inserted += AmmoBagData.insert(bag, stack);
            }
        }
        return inserted;
    }

    /** Total number of matching items the player carries, loose and in Ammo Bags. */
    public static int count(Player player, ItemStack wanted) {
        return countLoose(player.getInventory(), wanted) + countInBags(player, wanted);
    }

    /** Number of matching items stored in Ammo Bags in the player's inventory. */
    public static int countInBags(Player player, ItemStack wanted) {
        int count = 0;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack bag = inventory.getItem(i);
            if (AmmoBagData.isBag(bag)) {
                count += AmmoBagData.count(bag, wanted);
            }
        }
        return count;
    }

    /**
     * Removes up to {@code amount} matching items, loose items first, then from bags. Does not check for creative mode.
     *
     * @return the number of items actually removed
     */
    public static int remove(Player player, ItemStack wanted, int amount) {
        Inventory inventory = player.getInventory();
        int left = amount;
        for (int i = 0; i < inventory.getContainerSize() && left > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                int taken = Math.min(left, stack.getCount());
                stack.shrink(taken);
                left -= taken;
            }
        }
        for (int i = 0; i < inventory.getContainerSize() && left > 0; i++) {
            ItemStack bag = inventory.getItem(i);
            if (AmmoBagData.isBag(bag)) {
                left -= AmmoBagData.remove(bag, wanted, left);
            }
        }
        return amount - left;
    }

    private static int countLoose(Inventory inventory, ItemStack wanted) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
