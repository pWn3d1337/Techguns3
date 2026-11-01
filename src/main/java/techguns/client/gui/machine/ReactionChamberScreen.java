package techguns.client.gui.machine;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.ReactionChamberBlockEntity;
import techguns.machine.ReactionChamberMenu;

/**
 * Reaction Chamber screen (Techguns 2 {@code ReactionChamberGui}): the tank with the liquid level mark and buttons, the
 * intensity bar (red while it misses the required intensity, with the required mark), completion and duration bars,
 * the tank dump button.
 */
public class ReactionChamberScreen extends TGMachineScreen<ReactionChamberMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/reaction_chamber_gui.png");
    private static final int TANK_X = 18;
    private static final int TANK_Y = 17;
    private static final int TANK_W = 10;
    private static final int TANK_H = 50;

    public ReactionChamberScreen(ReactionChamberMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        this.hasUpgradeSlot = false;
    }

    @Override
    protected void init() {
        super.init();
        button("+", 33, 58, 10, ReactionChamberBlockEntity.BUTTON_LIQUID_UP);
        button("-", 43, 58, 10, ReactionChamberBlockEntity.BUTTON_LIQUID_DOWN);
        button("+", 91, 36, 10, ReactionChamberBlockEntity.BUTTON_INTENSITY_UP);
        button("-", 101, 36, 10, ReactionChamberBlockEntity.BUTTON_INTENSITY_DOWN);
        button("x", 20, 7, 8, ReactionChamberBlockEntity.BUTTON_DUMP);
    }

    private void button(String label, int x, int y, int size, int id) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> pressButton(id)).bounds(leftPos + x, topPos + y, size, size).build());
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        drawFluid(graphics, menu.fluid(), ReactionChamberBlockEntity.TANK_CAPACITY, x + TANK_X + 1, y + TANK_Y, TANK_W, TANK_H);
        // tank frame and the liquid level mark
        graphics.blit(TEXTURE, x + 18, y + 17, 176, 32, 12, 52);
        graphics.blit(TEXTURE, x + 19, y + 17 + 50 - menu.liquidLevel() * 5, 177, 32, 7, 1);
        int intensityPx = (10 - menu.intensity()) * 4;
        if (menu.isReacting()) {
            int completion = menu.requiredCompletion() > 0 ? Math.round(menu.completion() * 100.0f / menu.requiredCompletion()) : 0;
            graphics.blit(TEXTURE, x + 67, y + 61, 0, 167, Math.min(100, completion), 4);
            graphics.blit(TEXTURE, x + 67, y + 69, 0, 175, progressScaled(100), 4);
            boolean miss = menu.requiredIntensity() != menu.intensity();
            graphics.blit(TEXTURE, x + 114, y + 16 + intensityPx, miss ? 198 : 190, intensityPx, 5, 40 - intensityPx);
            graphics.blit(TEXTURE, x + 112, y + 15 + 40 - menu.requiredIntensity() * 4, 178, 22, 5, 3);
        } else {
            graphics.blit(TEXTURE, x + 114, y + 16 + intensityPx, 190, intensityPx, 5, 40 - intensityPx);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        drawText(graphics, Component.literal(menu.liquidLevel() * 10 + "%"), 34, 50);
        drawText(graphics, Component.literal(String.valueOf(menu.intensity())), 121, 18);
    }

    @Override
    protected void renderMachineTooltips(GuiGraphics graphics, int mx, int my, int mouseX, int mouseY) {
        super.renderMachineTooltips(graphics, mx, my, mouseX, mouseY);
        if (isIn(mx, my, 18, 16, 11, 51)) {
            graphics.renderComponentTooltip(font, tankTooltip(menu.fluid(), ReactionChamberBlockEntity.TANK_CAPACITY), mouseX, mouseY);
        } else if (isIn(mx, my, 20, 7, 8, 8)) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable("techguns.reactionchamber.dump"),
                    Component.translatable("techguns.reactionchamber.dump.tooltip")), mouseX, mouseY);
        }
    }
}
