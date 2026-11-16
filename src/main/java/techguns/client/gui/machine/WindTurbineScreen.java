package techguns.client.gui.machine;

import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.BiomeEfficiency;
import techguns.machine.WindTurbineMenu;

/**
 * Wind Turbine screen (Techguns 3): the power bar of the machine screens (column energy) and the column's segments,
 * output, stored energy and its biome, height and weather factors as text.
 */
// MIGRATION(26.x): GuiGraphics#blit and screen rendering change with deferred GUI rendering (1.21.6).
public class WindTurbineScreen extends AbstractContainerScreen<WindTurbineMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/wind_turbine_gui.png");
    private static final int TEXT_COLOR = 0x404040;
    private static final int WARNING_COLOR = 0xA02020;

    public WindTurbineScreen(WindTurbineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        int mx = mouseX - leftPos;
        int my = mouseY - topPos;
        if (mx >= 7 && mx <= 12 && my >= 17 && my <= 66) {
            graphics.renderTooltip(font, Component.translatable("techguns.container.power.amount", menu.energy(), menu.capacity()), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        // power bar: the shared machine sprite
        int diff = 48 - menu.energy() * 48 / Math.max(1, menu.capacity());
        graphics.blit(TGMachineScreen.WIDGETS, leftPos + 8, topPos + 17 + diff, 251, 1 + diff, 4, 48 - diff);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 2, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("techguns.wind_turbine.segments", menu.segments()), 22, 18, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("techguns.solar.output", format(menu.output(), "%.1f")), 22, 29, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("techguns.solar.stored", menu.energy(), menu.capacity()), 22, 40, TEXT_COLOR, false);
        // the biome at the column's top: the efficiency data map is synced, so the client looks it up itself
        if (minecraft != null && minecraft.level != null) {
            BiomeEfficiency efficiency = BiomeEfficiency.wind(minecraft.level.getBiome(menu.pos().above(Math.max(0, menu.segments() - 1))));
            graphics.drawString(font, Component.translatable("techguns.solar.biome", Component.translatable(efficiency.translationKey()),
                    format(menu.biomeFactor(), "%.2f")), 22, 51, TEXT_COLOR, false);
        }
        if (menu.openSky()) {
            graphics.drawString(font, Component.translatable("techguns.wind_turbine.factors", format(menu.heightFactor(), "%.2f"),
                    format(menu.weatherFactor(), "%.2f")), 22, 62, TEXT_COLOR, false);
        } else {
            graphics.drawString(font, Component.translatable("techguns.wind_turbine.blocked"), 22, 62, WARNING_COLOR, false);
        }
    }

    private static String format(double value, String pattern) {
        return String.format(Locale.ROOT, pattern, value);
    }
}
