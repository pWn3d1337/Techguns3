package techguns.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import techguns.TGIds;
import techguns.recipe.AmmoBenchRecipe;
import techguns.registry.TGBlocks;

/** Fabric Ammo Bench: bullet core, casing and powder give rounds (the bench screen is a list, not a fixed layout). */
// MIGRATION(26.x): JEI API of the new game version.
public class AmmoBenchCategory implements IRecipeCategory<RecipeHolder<AmmoBenchRecipe>> {
    public static final RecipeType<RecipeHolder<AmmoBenchRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("ammo_bench"));
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;

    public AmmoBenchCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemLike(TGBlocks.AMMO_BENCH.get());
        slot = helper.getSlotDrawable();
        arrow = helper.getRecipeArrow();
    }

    @Override
    public RecipeType<RecipeHolder<AmmoBenchRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("techguns.container.ammobench");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 116;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AmmoBenchRecipe> holder, IFocusGroup focuses) {
        AmmoBenchRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).setBackground(slot, -1, -1).addIngredients(recipe.core());
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).setBackground(slot, -1, -1).addIngredients(recipe.casing());
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 37).setBackground(slot, -1, -1).addIngredients(recipe.powder());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 19).setBackground(slot, -1, -1).addItemStack(recipe.result());
    }

    @Override
    public void draw(RecipeHolder<AmmoBenchRecipe> holder, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 36, 19);
    }
}
