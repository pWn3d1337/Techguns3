package techguns.compat.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import techguns.TGIds;
import techguns.recipe.CamoChangeRecipe;
import techguns.registry.TGBlocks;

/**
 * Techguns 2 {@code CamoBenchJeiRecipeCategory}: a block of a camo group turns into the next look of the group (the
 * bench cycles through them); input and output are linked.
 */
// MIGRATION(26.x): JEI API of the new game version.
public class CamoBenchCategory implements IRecipeCategory<RecipeHolder<CamoChangeRecipe>> {
    public static final RecipeType<RecipeHolder<CamoChangeRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("camo_bench"));
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;

    public CamoBenchCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemLike(TGBlocks.CAMO_BENCH.get());
        slot = helper.getSlotDrawable();
        arrow = helper.getRecipeArrow();
    }

    @Override
    public RecipeType<RecipeHolder<CamoChangeRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("techguns.container.camobench");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 82;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CamoChangeRecipe> holder, IFocusGroup focuses) {
        List<Item> entries = holder.value().entries();
        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            inputs.add(new ItemStack(entries.get(i)));
            outputs.add(new ItemStack(entries.get((i + 1) % entries.size())));
        }
        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).setBackground(slot, -1, -1).addItemStacks(inputs);
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 65, 1).setBackground(slot, -1, -1).addItemStacks(outputs);
        builder.createFocusLink(input, output);
    }

    @Override
    public void draw(RecipeHolder<CamoChangeRecipe> holder, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 28, 1);
    }
}
