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
import techguns.machine.FabricatorBlockEntity;
import techguns.recipe.FabricatorRecipe;
import techguns.registry.TGBlocks;

/** Techguns 2 {@code FabricatorJeiRecipeCategory}: input, wires, powder and plate with counts. */
public class FabricatorCategory extends TGRecipeCategory<RecipeHolder<FabricatorRecipe>> {
    public static final RecipeType<RecipeHolder<FabricatorRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("fabricator"));
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/fabricator_gui.png");
    private final IDrawableAnimated progress;

    public FabricatorCategory(IGuiHelper helper) {
        super(helper, TYPE, "techguns.container.fabricator", TEXTURE, TGBlocks.FABRICATOR_CONTROLLER.get(), true);
        progress = helper.createAnimatedDrawable(helper.createDrawable(TEXTURE, 0, 167, 91, 11), FabricatorBlockEntity.TIME, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    protected int powerPerTick(RecipeHolder<FabricatorRecipe> holder) {
        return FabricatorBlockEntity.POWER_PER_TICK;
    }

    @Override
    protected int time(RecipeHolder<FabricatorRecipe> holder) {
        return FabricatorBlockEntity.TIME;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<FabricatorRecipe> holder, IFocusGroup focuses) {
        FabricatorRecipe recipe = holder.value();
        int[] xs = {19, 47, 68, 89};
        for (int i = 0; i < xs.length; i++) {
            FabricatorRecipe.Input input = recipe.slot(i);
            if (input != null && input.count() > 0) {
                slot(builder, RecipeIngredientRole.INPUT, xs[i], 17).addItemStacks(stacks(input.ingredient(), input.count()));
            }
        }
        slot(builder, RecipeIngredientRole.OUTPUT, 116, 50).addItemStack(recipe.result());
    }

    @Override
    protected void drawProgress(RecipeHolder<FabricatorRecipe> holder, GuiGraphics graphics) {
        drawAt(progress, graphics, 21, 53);
    }
}
