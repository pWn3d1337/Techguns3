package techguns.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import techguns.TGIds;
import techguns.machine.MetalPressBlockEntity;
import techguns.recipe.MetalPressRecipe;
import techguns.registry.TGBlocks;

/** Techguns 2 {@code MetalPressJeiRecipeCategory}: two inputs pressed into one output. */
public class MetalPressCategory extends TGRecipeCategory<RecipeHolder<MetalPressRecipe>> {
    public static final RecipeType<RecipeHolder<MetalPressRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("metal_press"));
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/metal_press_gui.png");
    private final IDrawableAnimated progress;

    public MetalPressCategory(IGuiHelper helper) {
        super(helper, TYPE, "techguns.container.metalpress", TEXTURE, TGBlocks.METAL_PRESS.get(), true);
        progress = helper.createAnimatedDrawable(helper.createDrawable(TEXTURE, 176, 0, 19, 21), MetalPressBlockEntity.TIME, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override
    protected int powerPerTick(RecipeHolder<MetalPressRecipe> holder) {
        return MetalPressBlockEntity.POWER_PER_TICK;
    }

    @Override
    protected int time(RecipeHolder<MetalPressRecipe> holder) {
        return MetalPressBlockEntity.TIME;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MetalPressRecipe> holder, IFocusGroup focuses) {
        MetalPressRecipe recipe = holder.value();
        slot(builder, RecipeIngredientRole.INPUT, 110, 17).addIngredients(recipe.first());
        slot(builder, RecipeIngredientRole.INPUT, 130, 17).addIngredients(recipe.second());
        slot(builder, RecipeIngredientRole.OUTPUT, 120, 60).addItemStack(recipe.result());
    }

    @Override
    protected void drawProgress(RecipeHolder<MetalPressRecipe> holder, GuiGraphics graphics) {
        drawAt(progress, graphics, 119, 36);
    }
}
