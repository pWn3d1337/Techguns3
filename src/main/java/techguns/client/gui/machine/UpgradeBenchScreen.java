package techguns.client.gui.machine;

import java.awt.Color;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.UpgradeBenchMenu;

/**
 * Upgrade Bench screen (Techguns 2 {@code UpgradeBenchGui}): the player's XP and the cost in anvil colours, and the
 * cycling rainbow arrow while a result is ready.
 */
public class UpgradeBenchScreen extends TGMachineScreen<UpgradeBenchMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/upgrade_bench_gui.png");
    /** Anvil cost colours. */
    private static final int COLOR_AFFORDABLE = 0x80FF20;
    private static final int COLOR_TOO_EXPENSIVE = 0xFF6060;

    public UpgradeBenchScreen(UpgradeBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        this.powered = false;
        this.hasUpgradeSlot = false;
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        if (!menu.hasResult() || minecraft == null || minecraft.level == null) {
            return;
        }
        long time = minecraft.level.getGameTime();
        int frame = 3 - (int) (time % 4);
        Color color = new Color(Color.HSBtoRGB((time % 360) / 360.0f, 1.0f, 1.0f));
        graphics.setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1.0f);
        graphics.blit(TEXTURE, x + 37, y + 40, frame >= 2 ? 0 : 89, 167 + (frame % 2 == 1 ? 0 : 31), 89, 31);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        int xp = menu.xpCost();
        boolean affordable = minecraft.player.getAbilities().instabuild || xp <= minecraft.player.totalExperience;
        drawXpText(graphics, Component.translatable("techguns.gui.xp", minecraft.player.totalExperience), 95, 17, COLOR_AFFORDABLE);
        drawXpText(graphics, Component.translatable("techguns.gui.xpcost", xp), 95, 27, affordable ? COLOR_AFFORDABLE : COLOR_TOO_EXPENSIVE);
    }

    /** Anvil style text with a dark outline (Techguns 2 {@code drawXPText}). */
    private void drawXpText(GuiGraphics graphics, Component text, int x, int y, int color) {
        int shadow = 0xFF000000 | (color & 0xFCFCFC) >> 2;
        graphics.drawString(font, text, x, y + 1, shadow, false);
        graphics.drawString(font, text, x + 1, y, shadow, false);
        graphics.drawString(font, text, x + 1, y + 1, shadow, false);
        graphics.drawString(font, text, x, y, 0xFF000000 | color, false);
    }
}
