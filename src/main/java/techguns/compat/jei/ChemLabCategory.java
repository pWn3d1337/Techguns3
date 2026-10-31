package techguns.compat.jei;

import java.util.Arrays;
import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import techguns.TGIds;
import techguns.client.gui.machine.ChemLabScreen;
import techguns.machine.ChemLabBlockEntity;
import techguns.recipe.ChemLabRecipe;
import techguns.registry.TGBlocks;

/** Techguns 2 {@code ChemLabJeiRecipeCategory}: two inputs, a flask and a fluid make an item and/or a fluid. */
public class ChemLabCategory extends TGRecipeCategory<RecipeHolder<ChemLabRecipe>> {
    public static final RecipeType<RecipeHolder<ChemLabRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("chem_lab"));
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/chem_lab_gui.png");
    private static final int TANK_Y = 17;
    private final ITickTimer progress;

    public ChemLabCategory(IGuiHelper helper) {
        super(helper, TYPE, "techguns.container.chemlab", TEXTURE, TGBlocks.CHEM_LAB.get(), true);
        // Techguns 2 ProgressBarChemLab: the five pipe sections fill one after another, as on the machine screen
        progress = helper.createTickTimer(ChemLabBlockEntity.TIME, ChemLabBlockEntity.TIME, false);
    }

    @Override
    protected int powerPerTick(RecipeHolder<ChemLabRecipe> holder) {
        return holder.value().powerPerTick();
    }

    @Override
    protected int time(RecipeHolder<ChemLabRecipe> holder) {
        return ChemLabBlockEntity.TIME;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ChemLabRecipe> holder, IFocusGroup focuses) {
        ChemLabRecipe recipe = holder.value();
        recipe.first().ifPresent(in -> slot(builder, RecipeIngredientRole.INPUT, 35, 17).addItemStacks(Arrays.asList(in.getItems())));
        recipe.second().ifPresent(in -> slot(builder, RecipeIngredientRole.INPUT, 57, 17).addItemStacks(Arrays.asList(in.getItems())));
        recipe.flask().ifPresent(in -> slot(builder, RecipeIngredientRole.INPUT, 35, 40).addItemStacks(Arrays.asList(in.getItems())));
        recipe.fluid().ifPresent(in -> slot(builder, RecipeIngredientRole.INPUT, 19, TANK_Y)
                .setFluidRenderer(ChemLabBlockEntity.INPUT_TANK_CAPACITY, false, 10, 50)
                .addIngredients(NeoForgeTypes.FLUID_STACK, List.of(in.getFluids())));
        if (!recipe.result().isEmpty()) {
            slot(builder, RecipeIngredientRole.OUTPUT, 135, 17).addItemStack(recipe.result());
        }
        if (!recipe.fluidResult().isEmpty()) {
            slot(builder, RecipeIngredientRole.OUTPUT, 158, TANK_Y).setFluidRenderer(ChemLabBlockEntity.OUTPUT_TANK_CAPACITY, false, 10, 50)
                    .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.fluidResult());
        }
    }

    @Override
    protected void drawProgress(RecipeHolder<ChemLabRecipe> holder, GuiGraphics graphics) {
        ChemLabScreen.drawProgress(graphics, -GUI_X, -GUI_Y, (float) progress.getValue() / progress.getMaxValue());
    }
}
