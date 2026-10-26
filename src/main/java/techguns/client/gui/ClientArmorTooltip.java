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
    private static final int SPACING = 16;
    private static final ChatFormatting[] COLORS = {ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.AQUA, ChatFormatting.GOLD,
            ChatFormatting.RED, ChatFormatting.BLUE, ChatFormatting.YELLOW, ChatFormatting.DARK_GREEN, ChatFormatting.DARK_GRAY,
            ChatFormatting.GREEN};

    private final List<Float> values;

    public ClientArmorTooltip(TGArmorTooltip tooltip) {
        this.values = tooltip.values();
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
        return values.size() * SPACING;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        for (int i = 0; i < values.size(); i++) {
            graphics.blit(ICONS, x + i * SPACING, y, i * ICON, 0, ICON, ICON, 256, 16);
        }
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffers) {
        for (int i = 0; i < values.size(); i++) {
            String text = String.format(Locale.ROOT, "%.1f", values.get(i));
            font.drawInBatch(text, x + i * SPACING, y + 14, COLORS[i % COLORS.length].getColor(), true, matrix, buffers,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
    }
}
