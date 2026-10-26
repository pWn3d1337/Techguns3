package techguns.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/** Common defaults of the machine recipes: never in the recipe book, matched by the machine itself. */
public interface MachineRecipe extends Recipe<MachineInput> {
    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    default ItemStack assemble(MachineInput input, HolderLookup.Provider registries) {
        return getResultItem(registries).copy();
    }
}
