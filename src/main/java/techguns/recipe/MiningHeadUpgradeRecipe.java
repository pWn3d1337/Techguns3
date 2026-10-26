package techguns.recipe;

import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import techguns.gun.MeleeToolItem;
import techguns.registry.TGRecipes;

/**
 * Swaps the mining head of a melee tool (Fabric {@code MiningHeadUpgradeRecipe}): a tool plus one of its heads gives
 * the tool with that head; the old head comes back. One special recipe covers all tools.
 */
public class MiningHeadUpgradeRecipe extends CustomRecipe {
    public MiningHeadUpgradeRecipe(CraftingBookCategory category) {
        super(category);
    }

    private record Target(ItemStack tool, MeleeToolItem item, int level, int toolSlot) {
    }

    @Nullable
    private static Target target(CraftingInput input) {
        ItemStack tool = ItemStack.EMPTY;
        ItemStack head = ItemStack.EMPTY;
        int toolSlot = -1;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof MeleeToolItem) {
                if (!tool.isEmpty()) {
                    return null;
                }
                tool = stack;
                toolSlot = i;
            } else {
                if (!head.isEmpty()) {
                    return null;
                }
                head = stack;
            }
        }
        if (tool.isEmpty() || head.isEmpty()) {
            return null;
        }
        MeleeToolItem item = (MeleeToolItem) tool.getItem();
        int level = item.headLevelFor(head);
        if (level < 0 || level == item.headLevel(tool)) {
            return null;
        }
        return new Target(tool, item, level, toolSlot);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return target(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Target target = target(input);
        if (target == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = target.tool().copyWithCount(1);
        target.item().setHeadLevel(result, target.level());
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(input);
        Target target = target(input);
        if (target != null) {
            MeleeToolItem.MiningHead old = target.item().heads().get(target.item().headLevel(target.tool()));
            remaining.set(target.toolSlot(), new ItemStack(old.item().get()));
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.MINING_HEAD_UPGRADE.get();
    }
}
