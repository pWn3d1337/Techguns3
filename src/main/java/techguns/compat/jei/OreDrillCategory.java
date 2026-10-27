package techguns.compat.jei;

import java.util.List;
import java.util.Locale;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import techguns.TGIds;
import techguns.recipe.OreClusterRecipe;
import techguns.registry.TGBlocks;

/**
 * Techguns 2 {@code OreDrillJeiRecipeCategory}: an ore cluster, what it gives with the chances, and the mining level
 * and multipliers.
 */
// MIGRATION(26.x): JEI API of the new game version.
public class OreDrillCategory implements IRecipeCategory<RecipeHolder<OreClusterRecipe>> {
    public static final RecipeType<RecipeHolder<OreClusterRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("ore_drill"));
    private static final int TEXT_COLOR = 0x404040;
    private final IDrawable icon;
    private final IDrawable slot;

    public OreDrillCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemLike(TGBlocks.ORE_DRILL_CONTROLLER.get());
        slot = helper.getSlotDrawable();
    }

    @Override
    public RecipeType<RecipeHolder<OreClusterRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("techguns.container.oredrill");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 162;
    }

    @Override
    public int getHeight() {
        return 63;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<OreClusterRecipe> holder, IFocusGroup focuses) {
        OreClusterRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.CATALYST, 1, 1).setBackground(slot, -1, -1).addItemLike(recipe.cluster());
        List<OreClusterRecipe.Entry> entries = recipe.entries().stream().filter(entry -> !entry.isEmpty()).toList();
        int total = entries.stream().mapToInt(OreClusterRecipe.Entry::weight).sum();
        for (int i = 0; i < Math.min(9, entries.size()); i++) {
            OreClusterRecipe.Entry entry = entries.get(i);
            IRecipeSlotBuilder out = builder.addSlot(RecipeIngredientRole.OUTPUT, 102 + i % 3 * 18, 1 + i / 3 * 18).setBackground(slot, -1, -1);
            if (entry.itemOutput().isEmpty()) {
                out.addIngredient(NeoForgeTypes.FLUID_STACK, entry.fluidOutput());
            } else {
                out.addItemStack(entry.itemOutput());
            }
            float chance = entry.weight() * 100.0f / total;
            out.addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("techguns.jei.chance",
                    String.format(Locale.ROOT, "%.1f", chance))));
        }
    }

    @Override
    public void draw(RecipeHolder<OreClusterRecipe> holder, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        OreClusterRecipe recipe = holder.value();
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("techguns.jei.oredrill.mininglevel", recipe.miningLevel()), 1, 24, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("techguns.jei.oredrill.oremult",
                String.format(Locale.ROOT, "%.1f", recipe.oreMultiplier())), 1, 36, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("techguns.jei.oredrill.powermult",
                String.format(Locale.ROOT, "%.1f", recipe.powerMultiplier())), 1, 48, TEXT_COLOR, false);
    }
}
