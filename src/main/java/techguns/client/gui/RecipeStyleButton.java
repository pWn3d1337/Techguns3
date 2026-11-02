package techguns.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * A 20x18 button framed like the recipe book button next to it (switches between the vanilla inventory and the Techguns
 * tab): the recipe button sprite with its book covered by the frame's own fill colour, and the label or an item icon on top.
 */
// MIGRATION(26.x): Button rendering and GuiGraphics#blitSprite change with the 1.21.6 GUI pipeline.
public class RecipeStyleButton extends Button {
    public static final int WIDTH = 20;
    public static final int HEIGHT = 18;
    /** Inner fill of the recipe button sprites, normal and highlighted. */
    private static final int FILL = 0xFFC6C6C6;
    private static final int FILL_HIGHLIGHTED = 0xFF8892C9;
    private static final int TEXT = 0xFF404040;

    /** Drawn instead of the label when not empty; the label stays for narration. */
    private final ItemStack icon;

    public RecipeStyleButton(int x, int y, Component label, Component tooltip, OnPress onPress) {
        this(x, y, label, ItemStack.EMPTY, tooltip, onPress);
    }

    public RecipeStyleButton(int x, int y, Component label, ItemStack icon, Component tooltip, OnPress onPress) {
        super(x, y, WIDTH, HEIGHT, label, onPress, DEFAULT_NARRATION);
        this.icon = icon;
        setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean highlighted = isHoveredOrFocused();
        graphics.blitSprite(RecipeBookComponent.RECIPE_BUTTON_SPRITES.get(isActive(), highlighted), getX(), getY(), WIDTH, HEIGHT);
        // the frame is two pixels wide on every side
        graphics.fill(getX() + 2, getY() + 2, getX() + WIDTH - 2, getY() + HEIGHT - 2, highlighted ? FILL_HIGHLIGHTED : FILL);
        if (!icon.isEmpty()) {
            graphics.renderItem(icon, getX() + 2, getY() + 1);
            return;
        }
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, getMessage(), getX() + (WIDTH - font.width(getMessage()) + 1) / 2, getY() + (HEIGHT - 8) / 2 + 1, TEXT, false);
    }
}
