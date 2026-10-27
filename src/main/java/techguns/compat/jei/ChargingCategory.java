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
import techguns.machine.ChargingStationBlockEntity;
import techguns.recipe.ChargingRecipe;
import techguns.registry.TGBlocks;

/** Techguns 2 {@code ChargingStationJeiRecipeCategory}: an empty item charged to the full one. */
public class ChargingCategory extends TGRecipeCategory<RecipeHolder<ChargingRecipe>> {
    public static final RecipeType<RecipeHolder<ChargingRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("charging"));
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/charging_station_gui.png");
    private final IDrawableAnimated progress;

    public ChargingCategory(IGuiHelper helper) {
        super(helper, TYPE, "techguns.container.chargingstation", TEXTURE, TGBlocks.CHARGING_STATION.get(), true);
        progress = helper.createAnimatedDrawable(helper.createDrawable(TEXTURE, 0, 167, 27, 10), 100, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    protected int powerPerTick(RecipeHolder<ChargingRecipe> holder) {
        return ChargingStationBlockEntity.CHARGE_RATE;
    }

    @Override
    protected int time(RecipeHolder<ChargingRecipe> holder) {
        return Math.max(1, holder.value().charge() / ChargingStationBlockEntity.CHARGE_RATE);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ChargingRecipe> holder, IFocusGroup focuses) {
        ChargingRecipe recipe = holder.value();
        slot(builder, RecipeIngredientRole.INPUT, 19, 17).addIngredients(recipe.input());
        slot(builder, RecipeIngredientRole.OUTPUT, 68, 17).addItemStack(recipe.result());
    }

    @Override
    protected void drawProgress(RecipeHolder<ChargingRecipe> holder, GuiGraphics graphics) {
        drawAt(progress, graphics, 39, 19);
    }
}
