package techguns.armor;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** One material of a Repair Bench repair: {@code count} items matching {@code ingredient}. */
public record RepairMaterial(Ingredient ingredient, int count) {
    /** Stack shown in tooltips and given back by the grinder: the first matching item. */
    public ItemStack display() {
        ItemStack[] items = ingredient.getItems();
        return items.length == 0 ? ItemStack.EMPTY : items[0].copyWithCount(count);
    }

    /** Items that repair on the Repair Bench (Techguns 2 {@code GenericArmor} and {@code GenericShield}). */
    public interface Repairable {
        /** Materials to fully repair {@code stack}; empty if it is undamaged or has no repair materials. */
        default List<RepairMaterial> repairCost(ItemStack stack) {
            return repairCost(stack.getDamageValue(), stack.getMaxDamage());
        }

        /** Materials to repair {@code damage} lost points of a piece with {@code maxDamage} durability. */
        List<RepairMaterial> repairCost(int damage, int maxDamage);

        /** Whether this item has repair materials at all (Techguns 2 {@code canRepairOnRepairBench}). */
        boolean canRepairOnBench();
    }
}
