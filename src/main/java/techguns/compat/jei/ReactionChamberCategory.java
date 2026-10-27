package techguns.compat.jei;

import java.util.List;
import java.util.Locale;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import techguns.TGIds;
import techguns.machine.ReactionChamberBlockEntity;
import techguns.recipe.ReactionChamberRecipe;
import techguns.registry.TGBlocks;

/**
 * Techguns 2 {@code ReactionChamberJeiRecipeCategory}: input, beam focus and the liquid up to the required level give
 * up to four outputs; the required intensity is marked on the intensity bar, the reaction data is a tooltip.
 */
public class ReactionChamberCategory extends TGRecipeCategory<RecipeHolder<ReactionChamberRecipe>> {
    public static final RecipeType<RecipeHolder<ReactionChamberRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("reaction_chamber"));
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/reaction_chamber_gui.png");
    private static final int TEXT_COLOR = 0x404040;

    public ReactionChamberCategory(IGuiHelper helper) {
        super(helper, TYPE, "techguns.container.reactionchamber", TEXTURE, TGBlocks.REACTION_CHAMBER_CONTROLLER.get(), true);
    }

    @Override
    protected int powerPerTick(RecipeHolder<ReactionChamberRecipe> holder) {
        return holder.value().powerPerTick();
    }

    /** Power is used once per reaction tick: the total over the reaction, per game tick on average. */
    @Override
    protected int time(RecipeHolder<ReactionChamberRecipe> holder) {
        return holder.value().ticks();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ReactionChamberRecipe> holder, IFocusGroup focuses) {
        ReactionChamberRecipe recipe = holder.value();
        slot(builder, RecipeIngredientRole.INPUT, 35, 17).addIngredients(recipe.input());
        slot(builder, RecipeIngredientRole.CATALYST, 93, 17).addIngredients(recipe.focus());
        int amount = recipe.liquidLevel() * ReactionChamberRecipe.BUCKET;
        slot(builder, RecipeIngredientRole.INPUT, 19, 17).setFluidRenderer(ReactionChamberBlockEntity.TANK_CAPACITY, false, 10, 50)
                .addIngredients(NeoForgeTypes.FLUID_STACK, List.of(recipe.fluid().getStacks()).stream()
                        .map(stack -> new FluidStack(stack.getFluid(), amount)).toList());
        int[][] outputs = {{134, 17}, {152, 17}, {134, 35}, {152, 35}};
        for (int i = 0; i < Math.min(outputs.length, recipe.outputs().size()); i++) {
            slot(builder, RecipeIngredientRole.OUTPUT, outputs[i][0], outputs[i][1]).addItemStack(recipe.outputs().get(i));
        }
    }

    @Override
    protected void drawProgress(RecipeHolder<ReactionChamberRecipe> holder, GuiGraphics graphics) {
        ReactionChamberRecipe recipe = holder.value();
        // the required intensity on the intensity bar (Techguns 2 marker), the liquid level on the tank
        graphics.blit(TEXTURE, 112 - GUI_X, 15 + 40 - recipe.intensity() * 4 - GUI_Y, 178, 22, 5, 3);
        graphics.blit(TEXTURE, 19 - GUI_X, 17 + 50 - recipe.liquidLevel() * 5 - GUI_Y, 177, 32, 7, 1);
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("techguns.jei.reactionchamber.ticks", recipe.ticks()),
                35 - GUI_X, 46 - GUI_Y, TEXT_COLOR, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<ReactionChamberRecipe> holder, IRecipeSlotsView slots, double mouseX, double mouseY) {
        super.getTooltip(tooltip, holder, slots, mouseX, mouseY);
        double x = mouseX + GUI_X;
        double y = mouseY + GUI_Y;
        if (x >= 110 && x <= 120 && y >= 15 && y <= 57) {
            ReactionChamberRecipe recipe = holder.value();
            tooltip.add(Component.translatable("techguns.jei.reactionchamber.intensity", recipe.intensity(), recipe.intensityMargin()));
            tooltip.add(Component.translatable("techguns.jei.reactionchamber.completion", recipe.requiredCompletion(), recipe.ticks()));
            tooltip.add(Component.translatable("techguns.jei.reactionchamber.liquid", recipe.liquidLevel(), recipe.liquidConsumption()));
            tooltip.add(Component.translatable("techguns.jei.reactionchamber.instability",
                    String.format(Locale.ROOT, "%.0f", recipe.instability() * 100)));
            tooltip.add(Component.translatable("techguns.jei.reactionchamber.risk",
                    Component.translatable("techguns.reactionchamber.risk." + recipe.risk().getSerializedName())));
        }
    }
}
