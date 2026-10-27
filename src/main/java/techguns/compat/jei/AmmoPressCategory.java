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
import techguns.machine.AmmoPressBlockEntity;
import techguns.recipe.AmmoPressRecipe;
import techguns.registry.TGBlocks;

/** Techguns 2 {@code AmmoPressRecipeCategory}: bullet metal, two casing metal and powder make rounds. */
public class AmmoPressCategory extends TGRecipeCategory<RecipeHolder<AmmoPressRecipe>> {
    public static final RecipeType<RecipeHolder<AmmoPressRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("ammo_press"));
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/ammo_press_gui.png");
    private final IDrawableAnimated progress;

    public AmmoPressCategory(IGuiHelper helper) {
        super(helper, TYPE, "techguns.container.ammopress", TEXTURE, TGBlocks.AMMO_PRESS.get(), true);
        progress = helper.createAnimatedDrawable(helper.createDrawable(TEXTURE, 176, 0, 19, 21), AmmoPressBlockEntity.TIME, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override
    protected int powerPerTick(RecipeHolder<AmmoPressRecipe> holder) {
        return AmmoPressBlockEntity.POWER_PER_TICK;
    }

    @Override
    protected int time(RecipeHolder<AmmoPressRecipe> holder) {
        return AmmoPressBlockEntity.TIME;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AmmoPressRecipe> holder, IFocusGroup focuses) {
        AmmoPressRecipe recipe = holder.value();
        slot(builder, RecipeIngredientRole.INPUT, 100, 17).addIngredients(recipe.bulletMetal());
        slot(builder, RecipeIngredientRole.INPUT, 120, 17).addItemStacks(stacks(recipe.casingMetal(), AmmoPressRecipe.CASING_METAL_COUNT));
        slot(builder, RecipeIngredientRole.INPUT, 140, 17).addIngredients(recipe.powder());
        slot(builder, RecipeIngredientRole.OUTPUT, 120, 60).addItemStack(recipe.result());
    }

    @Override
    protected void drawProgress(RecipeHolder<AmmoPressRecipe> holder, GuiGraphics graphics) {
        drawAt(progress, graphics, 119, 36);
    }
}
