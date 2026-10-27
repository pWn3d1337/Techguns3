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
import java.util.Arrays;

import techguns.recipe.BlastFurnaceRecipe;
import techguns.registry.TGBlocks;

/** Techguns 2 {@code BlastFurnaceJeiRecipeCategory}: one or two inputs with counts, power and time per recipe. */
public class BlastFurnaceCategory extends TGRecipeCategory<RecipeHolder<BlastFurnaceRecipe>> {
    public static final RecipeType<RecipeHolder<BlastFurnaceRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("blast_furnace"));
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/blast_furnace_gui.png");
    private final IDrawableAnimated progress;

    public BlastFurnaceCategory(IGuiHelper helper) {
        super(helper, TYPE, "techguns.container.blastfurnace", TEXTURE, TGBlocks.BLAST_FURNACE.get(), true);
        progress = helper.createAnimatedDrawable(helper.createDrawable(TEXTURE, 0, 167, 91, 11), 100, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    protected int powerPerTick(RecipeHolder<BlastFurnaceRecipe> holder) {
        return holder.value().powerPerTick();
    }

    @Override
    protected int time(RecipeHolder<BlastFurnaceRecipe> holder) {
        return holder.value().time();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BlastFurnaceRecipe> holder, IFocusGroup focuses) {
        BlastFurnaceRecipe recipe = holder.value();
        slot(builder, RecipeIngredientRole.INPUT, 19, 17).addItemStacks(Arrays.asList(recipe.first().getItems()));
        if (!recipe.second().ingredient().isEmpty()) {
            slot(builder, RecipeIngredientRole.INPUT, 47, 17).addItemStacks(Arrays.asList(recipe.second().getItems()));
        }
        slot(builder, RecipeIngredientRole.OUTPUT, 116, 50).addItemStack(recipe.result());
    }

    @Override
    protected void drawProgress(RecipeHolder<BlastFurnaceRecipe> holder, GuiGraphics graphics) {
        drawAt(progress, graphics, 19, 53);
    }
}
