package techguns.client.gui.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGConfig;
import techguns.TGIds;
import techguns.item.OreDrillHeadItem;
import techguns.machine.OreDrillBlockEntity;
import techguns.machine.OreDrillMenu;

/**
 * Ore Drill screen (Techguns 2 {@code OreDrillGui}): fuel and oil tanks, the progress bar, the fuel buffer flame and a
 * warning while the structure is broken or lacks a fitting drill head.
 */
public class OreDrillScreen extends TGMachineScreen<OreDrillMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/ore_drill_gui.png");
    private static final int INPUT_TANK_X = 15;
    private static final int OUTPUT_TANK_X = 157;
    private static final int TANK_Y = 17;
    private static final int TANK_W = 10;
    private static final int TANK_H = 50;
    private static final int WARNING_COLOR = 0xFFFF2020;

    public OreDrillScreen(OreDrillMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        hasUpgradeSlot = false;
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        drawFluid(graphics, menu.inputFluid(), OreDrillBlockEntity.INPUT_TANK_CAPACITY, x + INPUT_TANK_X + 1, y + TANK_Y, TANK_W, TANK_H);
        drawFluid(graphics, menu.outputFluid(), OreDrillBlockEntity.OUTPUT_TANK_CAPACITY, x + OUTPUT_TANK_X + 1, y + TANK_Y, TANK_W, TANK_H);
        graphics.blit(TEXTURE, x + INPUT_TANK_X, y + TANK_Y, 176, 40, TANK_W + 2, TANK_H + 2);
        graphics.blit(TEXTURE, x + OUTPUT_TANK_X, y + TANK_Y, 176, 40, TANK_W + 2, TANK_H + 2);
        if (menu.totalTime() > 0) {
            graphics.blit(TEXTURE, x + 73, y + 17, 177, 1, 25, progressScaled(36) + 1);
        }
        int max = menu.fuelBufferMax();
        if (max > 0) {
            int s = 12 - (int) ((long) menu.fuelBuffer() * 12 / max);
            graphics.blit(TEXTURE, x + 35, y + 36 + s, 188, 39 + s, 6, 12 - s);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        OreDrillBlockEntity drill = menu.machine();
        Component warning = null;
        if (!drill.isFormed()) {
            warning = Component.translatable("techguns.container.oredrill.broken");
        } else if (menu.headMiningLevel() < 0) {
            warning = Component.translatable("techguns.container.oredrill.drillrequired."
                    + OreDrillHeadItem.Size.forDrillRadius(drill.drillRadius()).name().toLowerCase(Locale.ROOT));
        }
        if (warning != null) {
            // Techguns 2 drew it across the slots, centred a bit right of the middle
            graphics.drawString(font, warning, imageWidth / 2 - font.width(warning) / 2 + 14, 24, WARNING_COLOR, false);
        }
    }

    @Override
    protected void renderMachineTooltips(GuiGraphics graphics, int mx, int my, int mouseX, int mouseY) {
        OreDrillBlockEntity drill = menu.machine();
        if (isIn(mx, my, 74, 16, 27, 39)) {
            List<Component> lines = new ArrayList<>();
            if (menu.totalTime() > 0) {
                lines.add(Component.literal(String.format(Locale.ROOT, "%.2f%%", menu.progress() * 100.0f / menu.totalTime())));
                lines.add(Component.translatable("techguns.gui.oredrill.orehour")
                        .append(String.format(Locale.ROOT, ": %.2f", 1200.0 / menu.totalTime() * 60.0)));
            }
            if (menu.powerPerTick() > 0) {
                lines.add(Component.translatable("techguns.gui.oredrill.rftick").append(": " + menu.powerPerTick()));
            }
            lines.add(Component.empty());
            lines.add(Component.translatable("techguns.oredrill.drillsize").append(": " + (drill.getEngines() + drill.getRods())));
            lines.add(Component.translatable("techguns.oredrill.drillradius").append(": " + drill.drillRadius()));
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
        } else if (isIn(mx, my, INPUT_TANK_X, TANK_Y - 1, TANK_W + 1, TANK_H + 1)) {
            graphics.renderComponentTooltip(font, tankTooltip(menu.inputFluid(), OreDrillBlockEntity.INPUT_TANK_CAPACITY), mouseX, mouseY);
        } else if (isIn(mx, my, OUTPUT_TANK_X, TANK_Y - 1, TANK_W + 1, TANK_H + 1)) {
            graphics.renderComponentTooltip(font, tankTooltip(menu.outputFluid(), OreDrillBlockEntity.OUTPUT_TANK_CAPACITY), mouseX, mouseY);
        } else if (isIn(mx, my, 34, 36, 8, 14)) {
            if (menu.fuelBufferMax() > 0) {
                int power = menu.powerPerTick();
                double factor = TGConfig.ORE_DRILL_MULTIPLIER_FUEL.get();
                int ticksLeft = power <= 0 ? -1 : (int) (menu.fuelBuffer() / Math.max(power / factor, 1.0));
                graphics.renderComponentTooltip(font, List.of(
                        Component.translatable("techguns.oredrill.currentfuel").append(" " + menu.fuelBuffer() + "/" + menu.fuelBufferMax()),
                        Component.translatable("techguns.oredrill.remaining").append(": " + ticksLeft + " ")
                                .append(Component.translatable("techguns.oredrill.ticks"))), mouseX, mouseY);
            } else {
                graphics.renderTooltip(font, Component.translatable("techguns.oredrill.nofuel"), mouseX, mouseY);
            }
        } else {
            super.renderMachineTooltips(graphics, mx, my, mouseX, mouseY);
        }
    }
}
