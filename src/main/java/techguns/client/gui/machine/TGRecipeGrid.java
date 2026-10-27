package techguns.client.gui.machine;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Stonecutter-style list of item choices (Fabric {@code StoneCutterStyleScreen}): 4 columns, 3 rows of 16x18 cells and
 * a scroller, drawn with the vanilla stonecutter sprites. Used by the Camo Bench and the Ammo Bench screens.
 */
// MIGRATION(26.x): blitSprite and item rendering through GuiGraphics change with deferred GUI rendering (1.21.6).
public class TGRecipeGrid {
    private static final ResourceLocation CELL = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe");
    private static final ResourceLocation CELL_SELECTED = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final ResourceLocation CELL_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final ResourceLocation SCROLLER = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller");
    private static final ResourceLocation SCROLLER_DISABLED = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private static final int COLUMNS = 4;
    private static final int ROWS = 3;
    private static final int CELL_WIDTH = 16;
    private static final int CELL_HEIGHT = 18;
    /** Scroller left of the list origin, as in the stonecutter (list at 52, scroller at 119). */
    private static final int SCROLLER_X = 67;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int TRACK_HEIGHT = ROWS * CELL_HEIGHT;

    private final Supplier<List<ItemStack>> entries;
    private final IntSupplier selected;
    private final IntConsumer onPick;
    private boolean showCounts;
    private int x;
    private int y;
    private float scrollAmount;
    private int startIndex;
    private boolean scrolling;

    /**
     * @param entries  current choices
     * @param selected index of the highlighted choice, or -1
     * @param onPick   called with the index of a clicked choice
     */
    public TGRecipeGrid(Supplier<List<ItemStack>> entries, IntSupplier selected, IntConsumer onPick) {
        this.entries = entries;
        this.selected = selected;
        this.onPick = onPick;
    }

    /** Shows stack sizes on the choices (the Ammo Bench makes several rounds at once). */
    public TGRecipeGrid showCounts() {
        this.showCounts = true;
        return this;
    }

    /** Screen position of the list's top left cell (the stonecutter's 52, 14). */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Width including the scroller. */
    public static int width() {
        return SCROLLER_X + 12;
    }

    public static int height() {
        return TRACK_HEIGHT + 2;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        List<ItemStack> list = entries.get();
        if (list.size() <= startIndex) {
            startIndex = 0;
            scrollAmount = 0;
        }
        int scrollerY = (int) ((TRACK_HEIGHT - SCROLLER_HEIGHT - 1) * scrollAmount);
        graphics.blitSprite(canScroll(list) ? SCROLLER : SCROLLER_DISABLED, x + SCROLLER_X, y + 1 + scrollerY, 12, SCROLLER_HEIGHT);
        int end = Math.min(list.size(), startIndex + COLUMNS * ROWS);
        for (int i = startIndex; i < end; i++) {
            int cellX = cellX(i);
            int cellY = cellY(i);
            boolean hovered = mouseX >= cellX && mouseY >= cellY && mouseX < cellX + CELL_WIDTH && mouseY < cellY + CELL_HEIGHT;
            ResourceLocation sprite = i == selected.getAsInt() ? CELL_SELECTED : hovered ? CELL_HIGHLIGHTED : CELL;
            graphics.blitSprite(sprite, cellX, cellY - 1, CELL_WIDTH, CELL_HEIGHT);
        }
        for (int i = startIndex; i < end; i++) {
            graphics.renderItem(list.get(i), cellX(i), cellY(i));
            if (showCounts) {
                graphics.renderItemDecorations(Minecraft.getInstance().font, list.get(i), cellX(i), cellY(i));
            }
        }
    }

    /** Tooltip of the hovered choice; true if one was drawn. */
    public boolean renderTooltip(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        int index = indexAt(mouseX, mouseY);
        if (index < 0) {
            return false;
        }
        graphics.renderTooltip(font, entries.get().get(index), mouseX, mouseY);
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        scrolling = false;
        int index = indexAt(mouseX, mouseY);
        if (index >= 0) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0f));
            onPick.accept(index);
            return true;
        }
        if (mouseX >= x + SCROLLER_X && mouseX < x + SCROLLER_X + 12 && mouseY >= y && mouseY < y + TRACK_HEIGHT) {
            scrolling = true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseY) {
        List<ItemStack> list = entries.get();
        if (!scrolling || !canScroll(list)) {
            return false;
        }
        scrollAmount = Mth.clamp(((float) mouseY - y - 7.5f) / (TRACK_HEIGHT - 15.0f), 0.0f, 1.0f);
        startIndex = (int) (scrollAmount * hiddenRows(list) + 0.5) * COLUMNS;
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        List<ItemStack> list = entries.get();
        if (!isOver(mouseX, mouseY) || !canScroll(list)) {
            return false;
        }
        int hidden = hiddenRows(list);
        scrollAmount = Mth.clamp(scrollAmount - (float) amount / hidden, 0.0f, 1.0f);
        startIndex = (int) (scrollAmount * hidden + 0.5) * COLUMNS;
        return true;
    }

    public boolean isOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width() && mouseY >= y && mouseY < y + height();
    }

    private int indexAt(double mouseX, double mouseY) {
        int end = Math.min(entries.get().size(), startIndex + COLUMNS * ROWS);
        for (int i = startIndex; i < end; i++) {
            if (mouseX >= cellX(i) && mouseY >= cellY(i) && mouseX < cellX(i) + CELL_WIDTH && mouseY < cellY(i) + CELL_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    private int cellX(int index) {
        return x + (index - startIndex) % COLUMNS * CELL_WIDTH;
    }

    private int cellY(int index) {
        return y + (index - startIndex) / COLUMNS * CELL_HEIGHT + 2;
    }

    private static boolean canScroll(List<ItemStack> list) {
        return list.size() > COLUMNS * ROWS;
    }

    private static int hiddenRows(List<ItemStack> list) {
        return (list.size() + COLUMNS - 1) / COLUMNS - ROWS;
    }
}
