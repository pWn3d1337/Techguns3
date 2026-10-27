package techguns.recipe;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Recipe input of a machine: its input slots in order. */
public record MachineInput(List<ItemStack> items) implements RecipeInput {
    public static MachineInput of(ItemStack... items) {
        return new MachineInput(List.of(items));
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
