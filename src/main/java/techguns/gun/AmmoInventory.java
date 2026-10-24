package techguns.gun;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Finding, consuming and returning ammo in a player's inventory.
 */
// TODO Phase 3: also pull from and fill Ammo Bags (design doc §4.4).
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
        Inventory inventory = player.getInventory();
        for (ItemStack required : ammo) {
            if (count(inventory, required) < required.getCount()) {
                return false;
            }
        }
        for (ItemStack required : ammo) {
            remove(inventory, required, required.getCount());
        }
        return true;
    }

    /** Whether the player carries one of each required ammo item (always true in creative). */
    public static boolean has(Player player, List<ItemStack> ammo) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Inventory inventory = player.getInventory();
        return ammo.stream().allMatch(required -> count(inventory, required) >= required.getCount());
    }

    /** Gives items to the player, dropping what doesn't fit (server side only). */
    public static void give(Player player, ItemStack stack) {
        if (stack.isEmpty() || player.getAbilities().instabuild) {
            return;
        }
        if (!player.getInventory().add(stack.copy()) && !player.level().isClientSide) {
            player.drop(stack.copy(), false);
        }
    }

    private static int count(Inventory inventory, ItemStack wanted) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void remove(Inventory inventory, ItemStack wanted, int amount) {
        for (int i = 0; i < inventory.getContainerSize() && amount > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                int taken = Math.min(amount, stack.getCount());
                stack.shrink(taken);
                amount -= taken;
            }
        }
    }
}
