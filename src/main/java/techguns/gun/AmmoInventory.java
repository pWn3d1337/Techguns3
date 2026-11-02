package techguns.gun;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.item.AmmoBagData;
import techguns.slot.TabSlots;

/**
 * Finding, consuming and returning ammo in a player's inventory, including ammo stored in Ammo Bags in the inventory and
 * in the ammo box slots of the Techguns inventory tab (design doc §4.4). Loose items are used first, then bags.
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
     * Moves as much of {@code stack} as fits into the player's Ammo Bags (non-ammo is ignored)
     * and shrinks {@code stack} by that amount.
     *
     * @return the number of items inserted
     */
    public static int insertIntoBags(Player player, ItemStack stack) {
        int inserted = 0;
        for (ItemStack bag : bags(player)) {
            if (stack.isEmpty()) {
                break;
            }
            inserted += AmmoBagData.insert(bag, stack);
        }
        return inserted;
    }

    /** Total number of matching items the player carries, loose and in Ammo Bags. */
    public static int count(Player player, ItemStack wanted) {
        return countLoose(player.getInventory(), wanted) + countInBags(player, wanted);
    }

    /** Number of matching items stored in the player's Ammo Bags. */
    public static int countInBags(Player player, ItemStack wanted) {
        int count = 0;
        for (ItemStack bag : bags(player)) {
            count += AmmoBagData.count(bag, wanted);
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
        for (ItemStack bag : bags(player)) {
            if (left <= 0) {
                break;
            }
            left -= AmmoBagData.remove(bag, wanted, left);
        }
        return amount - left;
    }

    /** The player's Ammo Bags: in the inventory, then in the inventory tab's ammo box slots (live stacks). */
    private static List<ItemStack> bags(Player player) {
        List<ItemStack> bags = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (AmmoBagData.isBag(stack)) {
                bags.add(stack);
            }
        }
        for (ItemStack stack : TabSlots.of(player).ammoBoxes()) {
            if (AmmoBagData.isBag(stack)) {
                bags.add(stack);
            }
        }
        return bags;
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
