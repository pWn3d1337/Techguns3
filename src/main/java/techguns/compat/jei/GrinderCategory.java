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
import net.minecraft.network.chat.Component;
import techguns.machine.GrinderBlockEntity;
import techguns.recipe.GrinderRecipe;
import techguns.registry.TGBlocks;

/** Techguns 2 {@code GrinderRecipeCategory}: one input, up to nine outputs with their chances. */
public class GrinderCategory extends TGRecipeCategory<RecipeHolder<GrinderRecipe>> {
    public static final RecipeType<RecipeHolder<GrinderRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("grinder"));
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/grinder_gui.png");
    private final IDrawableAnimated left;
    private final IDrawableAnimated right;

    public GrinderCategory(IGuiHelper helper) {
        super(helper, TYPE, "techguns.container.grinder", TEXTURE, TGBlocks.GRINDER.get(), true);
        left = helper.createAnimatedDrawable(helper.createDrawable(TEXTURE, 0, 167, 21, 22), GrinderBlockEntity.TIME, IDrawableAnimated.StartDirection.LEFT, false);
        right = helper.createAnimatedDrawable(helper.createDrawable(TEXTURE, 20, 167, 21, 22), GrinderBlockEntity.TIME, IDrawableAnimated.StartDirection.RIGHT, false);
    }

    @Override
    protected int powerPerTick(RecipeHolder<GrinderRecipe> holder) {
        return GrinderBlockEntity.POWER_PER_TICK;
    }

    @Override
    protected int time(RecipeHolder<GrinderRecipe> holder) {
        return GrinderBlockEntity.TIME;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<GrinderRecipe> holder, IFocusGroup focuses) {
        GrinderRecipe recipe = holder.value();
        slot(builder, RecipeIngredientRole.INPUT, 18, 17).addIngredients(recipe.input());
        for (int i = 0; i < Math.min(9, recipe.outputs().size()); i++) {
            GrinderRecipe.Output output = recipe.outputs().get(i);
            var slot = slot(builder, RecipeIngredientRole.OUTPUT, 80 + i % 3 * 18, 17 + i / 3 * 18).addItemStack(output.stack());
            if (output.chance() < 1.0f) {
                slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("techguns.jei.chance",
                        String.format(java.util.Locale.ROOT, "%.1f", output.chance() * 100.0f))));
            }
        }
    }

    @Override
    protected void drawProgress(RecipeHolder<GrinderRecipe> holder, GuiGraphics graphics) {
        drawAt(left, graphics, 31, 39);
        drawAt(right, graphics, 51, 39);
    }
}
