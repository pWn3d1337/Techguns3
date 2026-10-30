package techguns.client.gui;

import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.joml.Matrix4f;
import techguns.TGIds;
import techguns.armor.TGArmorTooltip;

/**
 * Draws a Techguns armor piece's armor values as one row of damage-category icons with the value below each
 * (Fabric {@code TGArmorTooltipComponent}).
 */
// MIGRATION(26.x): ClientTooltipComponent / GuiGraphics blit changed in 1.21.2+.
public class ClientArmorTooltip implements ClientTooltipComponent {
    private static final ResourceLocation ICONS = TGIds.id("textures/gui/armor_value_icons.png");
    private static final int ICON = 13;
    /** Smallest column width; wider values (shield percentages) widen all columns so the numbers don't overlap. */
    private static final int SPACING = 16;
    private static final ChatFormatting[] COLORS = {ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.AQUA, ChatFormatting.GOLD,
            ChatFormatting.RED, ChatFormatting.BLUE, ChatFormatting.YELLOW, ChatFormatting.DARK_GREEN, ChatFormatting.DARK_GRAY,
            ChatFormatting.GREEN};

    private final List<String> texts;
    private final int spacing;
    /** Shields: a space before the icons (not the numbers below them), which centres them over the longer percentages. */
    private final int indent;
    /** One more pixel after a first value of 100 %, whose width the column hasn't room to spare for. */
    private final int afterFirst;

    public ClientArmorTooltip(TGArmorTooltip tooltip) {
        this.texts = tooltip.values().stream().map(value -> tooltip.percent()
                ? String.format(Locale.ROOT, "%.0f%%", value) : String.format(Locale.ROOT, "%.1f", value)).toList();
        Font font = net.minecraft.client.Minecraft.getInstance().font;
        this.spacing = Math.max(SPACING, texts.stream().mapToInt(font::width).max().orElse(0) + 1);
        this.indent = tooltip.percent() ? font.width(" ") : 0;
        this.afterFirst = !texts.isEmpty() && texts.get(0).equals("100%") ? 1 : 0;
    }

    public static void register(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(TGArmorTooltip.class, ClientArmorTooltip::new);
    }

    @Override
    public int getHeight() {
        return 25;
    }

    @Override
    public int getWidth(Font font) {
        return indent + texts.size() * spacing + afterFirst;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        for (int i = 0; i < texts.size(); i++) {
            graphics.blit(ICONS, x + indent + column(i), y, i * ICON, 0, ICON, ICON, 256, 16);
        }
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffers) {
        for (int i = 0; i < texts.size(); i++) {
            font.drawInBatch(texts.get(i), x + column(i), y + 14, COLORS[i % COLORS.length].getColor(), true, matrix, buffers,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
    }

    private int column(int i) {
        return i * spacing + (i > 0 ? afterFirst : 0);
    }
}
