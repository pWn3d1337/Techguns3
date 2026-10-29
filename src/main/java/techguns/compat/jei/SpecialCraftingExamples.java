package techguns.compat.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import techguns.TGIds;
import techguns.gun.GunItem;
import techguns.gun.MeleeToolItem;
import techguns.recipe.AmmoChangeRecipe;
import techguns.recipe.MiningHeadUpgradeRecipe;

/**
 * Display recipes for the special crafting recipes JEI can't show by itself: one shapeless example per gun and ammo
 * variant ({@link AmmoChangeRecipe}) and per melee tool and mining head ({@link MiningHeadUpgradeRecipe}). Each result
 * comes from the real recipe, so the examples show what crafting gives. No JEI types here (GameTests use it).
 */
public final class SpecialCraftingExamples {
    private static final AmmoChangeRecipe AMMO_CHANGE = new AmmoChangeRecipe(CraftingBookCategory.EQUIPMENT);
    private static final MiningHeadUpgradeRecipe HEAD_UPGRADE = new MiningHeadUpgradeRecipe(CraftingBookCategory.EQUIPMENT);

    private SpecialCraftingExamples() {
    }

    public static List<RecipeHolder<CraftingRecipe>> all(HolderLookup.Provider registries) {
        List<RecipeHolder<CraftingRecipe>> examples = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof MeleeToolItem tool) {
                List<Item> heads = tool.heads().stream().map(head -> (Item) head.item().get()).toList();
                addExamples(examples, registries, HEAD_UPGRADE, "mining_head", tool, heads);
            } else if (item instanceof GunItem gun) {
                // the recipe takes variants reloaded from a single item
                List<Item> ammo = gun.ammoType().variants().stream().filter(variant -> variant.ammo().size() == 1)
                        .map(variant -> variant.ammo().getFirst().asItem()).toList();
                addExamples(examples, registries, AMMO_CHANGE, "ammo_change", gun, ammo);
            }
        }
        return examples;
    }

    /**
     * One example per {@code part}: the plain item plus the part, or, for the part the plain item already has, the item
     * changed to another part first.
     */
    private static void addExamples(List<RecipeHolder<CraftingRecipe>> examples, HolderLookup.Provider registries, CustomRecipe recipe,
                                    String kind, Item item, List<Item> parts) {
        if (parts.size() < 2) {
            return;
        }
        ItemStack plain = new ItemStack(item);
        for (Item part : parts) {
            ItemStack from = plain;
            ItemStack result = craft(recipe, registries, from, part);
            if (result.isEmpty()) {
                Item other = parts.get(parts.indexOf(part) == 0 ? 1 : 0);
                from = craft(recipe, registries, plain, other);
                result = from.isEmpty() ? ItemStack.EMPTY : craft(recipe, registries, from, part);
            }
            if (!result.isEmpty()) {
                String id = "jei/" + kind + "/" + BuiltInRegistries.ITEM.getKey(item).getPath() + "/"
                        + BuiltInRegistries.ITEM.getKey(part).getPath().toLowerCase(Locale.ROOT);
                examples.add(new RecipeHolder<>(TGIds.id(id), new ShapelessRecipe("", CraftingBookCategory.EQUIPMENT, result,
                        NonNullList.of(Ingredient.EMPTY, Ingredient.of(from), Ingredient.of(part)))));
            }
        }
    }

    private static ItemStack craft(CustomRecipe recipe, HolderLookup.Provider registries, ItemStack item, Item part) {
        CraftingInput input = CraftingInput.of(2, 1, List.of(item, new ItemStack(part)));
        return recipe.matches(input, null) ? recipe.assemble(input, registries) : ItemStack.EMPTY;
    }
}
