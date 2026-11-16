package techguns.client.gui.machine;

import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.BiomeEfficiency;
import techguns.machine.SolarPanelMenu;

/**
 * Solar Panel screen (Techguns 3): the power bar of the machine screens (array energy) and the array's size, output,
 * stored energy and this panel's biome type and multiplier as text.
 */
// MIGRATION(26.x): GuiGraphics#blit and screen rendering change with deferred GUI rendering (1.21.6).
public class SolarPanelScreen extends AbstractContainerScreen<SolarPanelMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/solar_panel_gui.png");
    private static final int TEXT_COLOR = 0x404040;

    public SolarPanelScreen(SolarPanelMenu menu, Inventory inventory, Component title) {
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
        graphics.drawString(font, Component.translatable("techguns.solar.panels", menu.panels()), 22, 20, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("techguns.solar.output", String.format(Locale.ROOT, "%.1f", menu.output())),
                22, 32, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("techguns.solar.stored", menu.energy(), menu.capacity()), 22, 44, TEXT_COLOR, false);
        // the biome of this panel: the efficiency data map is synced, so the client looks it up itself
        if (minecraft != null && minecraft.level != null) {
            BiomeEfficiency efficiency = BiomeEfficiency.solar(minecraft.level.getBiome(menu.pos()));
            graphics.drawString(font, Component.translatable("techguns.solar.biome", Component.translatable(efficiency.translationKey()),
                    String.format(Locale.ROOT, "%.2f", efficiency.multiplier())), 22, 56, TEXT_COLOR, false);
        }
    }
}
