package techguns.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.joml.Matrix4f;

/**
 * One line of the inventory tab's total armor tooltip: the damage category's armor icon (the icon row of armor tooltips,
 * {@link ClientArmorTooltip}) followed by the text.
 *
 * @param icon index in {@code armor_value_icons.png}
 */
// MIGRATION(26.x): ClientTooltipComponent / GuiGraphics blit changed in 1.21.2+.
public record ArmorSumLine(int icon, Component text) implements TooltipComponent {
    private static final int GAP = 3;

    public static void register(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ArmorSumLine.class, Client::new);
    }

    private record Client(ArmorSumLine line) implements ClientTooltipComponent {
        @Override
        public int getHeight() {
            return ClientArmorTooltip.ICON + 1;
        }

        @Override
        public int getWidth(Font font) {
            return ClientArmorTooltip.ICON + GAP + font.width(line.text());
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
            graphics.blit(ClientArmorTooltip.ICONS, x, y, line.icon() * ClientArmorTooltip.ICON, 0, ClientArmorTooltip.ICON,
                    ClientArmorTooltip.ICON, 256, 16);
        }

        @Override
        public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffers) {
            font.drawInBatch(line.text(), x + ClientArmorTooltip.ICON + GAP, y + 3, 0xFFFFFFFF, true, matrix, buffers,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
    }
}
