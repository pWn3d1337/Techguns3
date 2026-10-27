package techguns.compat.jei;

import java.util.Arrays;
import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import techguns.TGIds;

/**
 * Techguns 2 {@code BasicRecipeCategory}: a recipe drawn on the machine's own GUI texture (the part from (7, 15),
 * 162 x 63), slots at the machine's slot positions, the draining power bar with a power tooltip and the machine's
 * progress animation.
 */
// MIGRATION(26.x): JEI API of the new game version.
public abstract class TGRecipeCategory<T> implements IRecipeCategory<T> {
    /** Top-left corner of the GUI part shown; machine slot positions are given in GUI coordinates. */
    protected static final int GUI_X = 7;
    protected static final int GUI_Y = 15;
    private static final int WIDTH = 162;
    private static final int HEIGHT = 63;
    private static final ResourceLocation WIDGETS = TGIds.id("textures/gui/ammo_press_gui.png");
    private static final int POWER_X = 8;
    private static final int POWER_Y = 17;

    private final RecipeType<T> type;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated power;
    private final boolean powered;

    protected TGRecipeCategory(IGuiHelper helper, RecipeType<T> type, String titleKey, ResourceLocation texture, ItemLike icon, boolean powered) {
        this.type = type;
        this.title = Component.translatable(titleKey);
        this.background = helper.createDrawable(texture, GUI_X, GUI_Y, WIDTH, HEIGHT);
        this.icon = helper.createDrawableItemLike(icon);
        this.power = helper.createAnimatedDrawable(helper.createDrawable(WIDGETS, 251, 1, 4, 48), 100, IDrawableAnimated.StartDirection.TOP, true);
        this.powered = powered;
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    /** FE per tick of the recipe. */
    protected abstract int powerPerTick(T recipe);

    /** Ticks per operation. */
    protected abstract int time(T recipe);

    /** The progress animation(s), at GUI coordinates through {@link #drawAt}. */
    protected void drawProgress(T recipe, GuiGraphics graphics) {
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
        if (powered) {
            drawAt(power, graphics, POWER_X, POWER_Y);
        }
        drawProgress(recipe, graphics);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, T recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        double x = mouseX + GUI_X;
        double y = mouseY + GUI_Y;
        if (powered && x >= POWER_X && x <= POWER_X + 5 && y >= POWER_Y && y <= POWER_Y + 49) {
            int perTick = powerPerTick(recipe);
            tooltip.add(Component.translatable("techguns.container.power").append(":"));
            tooltip.add(Component.literal("-" + perTick + " FE/t"));
            tooltip.add(Component.literal("-" + perTick * time(recipe) + " FE"));
        }
    }

    protected static void drawAt(IDrawable drawable, GuiGraphics graphics, int guiX, int guiY) {
        drawable.draw(graphics, guiX - GUI_X, guiY - GUI_Y);
    }

    /** A slot at the machine's GUI position. */
    protected static IRecipeSlotBuilder slot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int guiX, int guiY) {
        return builder.addSlot(role, guiX - GUI_X, guiY - GUI_Y);
    }

    /** The ingredient's items, each with {@code count}. */
    protected static List<ItemStack> stacks(Ingredient ingredient, int count) {
        return Arrays.stream(ingredient.getItems()).map(stack -> stack.copyWithCount(count)).toList();
    }
}
