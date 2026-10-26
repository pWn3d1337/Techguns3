package techguns.data;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import techguns.recipe.TransferAmmoRecipe;

/**
 * Shaped recipe whose result is an arbitrary {@link ItemStack} (with data components, e.g. a gun with
 * loaded ammo). Vanilla's {@code ShapedRecipeBuilder} only takes an item and a count.
 */
public final class StackShapedRecipeBuilder {
    private final RecipeCategory category;
    private final ItemStack result;
    private final String[] pattern;
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private String criterionName;
    private Criterion<?> criterion;
    private boolean transferAmmo;

    private StackShapedRecipeBuilder(RecipeCategory category, ItemStack result, String... pattern) {
        this.category = category;
        this.result = result;
        int width = java.util.Arrays.stream(pattern).mapToInt(String::length).max().orElse(0);
        this.pattern = java.util.Arrays.stream(pattern).map(row -> row + " ".repeat(width - row.length())).toArray(String[]::new);
    }

    public static StackShapedRecipeBuilder shaped(RecipeCategory category, ItemStack result, String... pattern) {
        return new StackShapedRecipeBuilder(category, result, pattern);
    }

    public StackShapedRecipeBuilder define(char symbol, ItemLike item) {
        key.put(symbol, Ingredient.of(item));
        return this;
    }

    public StackShapedRecipeBuilder define(char symbol, TagKey<Item> tag) {
        key.put(symbol, Ingredient.of(tag));
        return this;
    }

    /** Saves a {@link TransferAmmoRecipe}: the result keeps the rounds of the input guns. */
    public StackShapedRecipeBuilder transferAmmo() {
        this.transferAmmo = true;
        return this;
    }

    public StackShapedRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criterionName = name;
        this.criterion = criterion;
        return this;
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        ShapedRecipePattern shapedPattern = ShapedRecipePattern.of(key, pattern);
        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        advancement.addCriterion(criterionName, criterion);
        CraftingBookCategory bookCategory = RecipeBuilder.determineBookCategory(category);
        ShapedRecipe recipe = transferAmmo ? new TransferAmmoRecipe("", bookCategory, shapedPattern, result, true)
                : new ShapedRecipe("", bookCategory, shapedPattern, result);
        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/" + category.getFolderName() + "/")));
    }
}
