package techguns.client.gui.machine;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.ChemLabBlockEntity;
import techguns.machine.ChemLabMenu;

/**
 * Chem Lab screen (Techguns 2 {@code ChemLabGui}): input and output tanks, the five-step progress drawing, tank dump
 * buttons and the drained-tank toggle.
 */
public class ChemLabScreen extends TGMachineScreen<ChemLabMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/chem_lab_gui.png");
    private static final int INPUT_TANK_X = 18;
    private static final int OUTPUT_TANK_X = 157;
    private static final int TANK_Y = 17;
    private static final int TANK_W = 10;
    private static final int TANK_H = 50;

    public ChemLabScreen(ChemLabMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        upgradeSlotX = 134;
        upgradeSlotY = 59;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("x"), button -> pressButton(ChemLabBlockEntity.BUTTON_DUMP_INPUT))
                .bounds(leftPos + 20, topPos + 7, 8, 8).build());
        addRenderableWidget(Button.builder(Component.literal("x"), button -> pressButton(ChemLabBlockEntity.BUTTON_DUMP_OUTPUT))
                .bounds(leftPos + 159, topPos + 7, 8, 8).build());
        addRenderableWidget(new IconButton(leftPos + 156, topPos + 68, 14, ChemLabBlockEntity.BUTTON_TOGGLE_DRAIN, TEXTURE, 188,
                () -> 31 + (menu.isDrainInput() ? 12 : 0), 12));
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        drawFluid(graphics, menu.inputFluid(), ChemLabBlockEntity.INPUT_TANK_CAPACITY, x + INPUT_TANK_X + 1, y + TANK_Y, TANK_W, TANK_H);
        drawFluid(graphics, menu.outputFluid(), ChemLabBlockEntity.OUTPUT_TANK_CAPACITY, x + OUTPUT_TANK_X + 1, y + TANK_Y, TANK_W, TANK_H);
        graphics.blit(TEXTURE, x + INPUT_TANK_X, y + TANK_Y, 176, 32, TANK_W + 2, TANK_H + 2);
        graphics.blit(TEXTURE, x + OUTPUT_TANK_X, y + TANK_Y, 176, 32, TANK_W + 2, TANK_H + 2);
        if (menu.totalTime() <= 0) {
            return;
        }
        // Techguns 2: the liquid runs through five pipe sections one after another
        float p = (float) menu.progress() / menu.totalTime();
        graphics.blit(TEXTURE, x + 81, y + 19, 178, 5, 8, (int) ((p < 0.2f ? p * 5 : 1.0f) * 25));
        if (p >= 0.2f) {
            graphics.blit(TEXTURE, x + 88, y + 39, 186, 25, (int) ((p < 0.4f ? (p - 0.2f) * 5 : 1.0f) * 9), 1);
        }
        if (p >= 0.4f) {
            int h = (int) ((p < 0.6f ? (p - 0.4f) * 5 : 1.0f) * 20);
            graphics.blit(TEXTURE, x + 97, y + 23 + (20 - h), 194, 9 + (20 - h), 14, h);
        }
        if (p >= 0.6f) {
            graphics.blit(TEXTURE, x + 108, y + 17, 205, 3, (int) ((p < 0.8f ? (p - 0.6f) * 5 : 1.0f) * 16), 6);
        }
        if (p >= 0.8f) {
            graphics.blit(TEXTURE, x + 117, y + 20, 214, 6, 14, (int) ((p - 0.8f) * 5 * 24) + 1);
        }
    }

    @Override
    protected void renderMachineTooltips(GuiGraphics graphics, int mx, int my, int mouseX, int mouseY) {
        if (isIn(mx, my, INPUT_TANK_X, TANK_Y - 1, TANK_W + 1, TANK_H + 1)) {
            graphics.renderComponentTooltip(font, tankTooltip(menu.inputFluid(), ChemLabBlockEntity.INPUT_TANK_CAPACITY), mouseX, mouseY);
        } else if (isIn(mx, my, OUTPUT_TANK_X, TANK_Y - 1, TANK_W + 1, TANK_H + 1)) {
            graphics.renderComponentTooltip(font, tankTooltip(menu.outputFluid(), ChemLabBlockEntity.OUTPUT_TANK_CAPACITY), mouseX, mouseY);
        } else if (isIn(mx, my, 20, 7, 8, 8)) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable("techguns.chemlab.dumpinput"),
                    Component.translatable("techguns.chemlab.dump.tooltip")), mouseX, mouseY);
        } else if (isIn(mx, my, 159, 7, 8, 8)) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable("techguns.chemlab.dumpoutput"),
                    Component.translatable("techguns.chemlab.dump.tooltip")), mouseX, mouseY);
        } else if (isIn(mx, my, 156, 68, 14, 14)) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable("techguns.chemlab.toggledrain"),
                    Component.translatable("techguns.chemlab.toggledrain.tooltip." + (menu.isDrainInput() ? 1 : 0))), mouseX, mouseY);
        } else {
            super.renderMachineTooltips(graphics, mx, my, mouseX, mouseY);
        }
    }
}
