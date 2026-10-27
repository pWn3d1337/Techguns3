package techguns.client.gui.machine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.MetalPressBlockEntity;
import techguns.machine.MetalPressMenu;

/** Metal Press screen (Techguns 2 {@code MetalPressGui}): progress arrow and the auto split switch. */
public class MetalPressScreen extends TGMachineScreen<MetalPressMenu> {
    public MetalPressScreen(MetalPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TGIds.id("textures/gui/metal_press_gui.png"));
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("techguns.button.switch"),
                button -> pressButton(MetalPressBlockEntity.BUTTON_AUTO_SPLIT)).bounds(leftPos + 20, topPos + 50, 40, 20).build());
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        if (menu.totalTime() > 0) {
            graphics.blit(TGIds.id("textures/gui/metal_press_gui.png"), x + 119, y + 36, 176, 0, 19, progressScaled(21) + 1);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        drawText(graphics, Component.translatable("techguns.container.metalpress.autosplit"), 20, 30);
        drawText(graphics, Component.translatable(menu.isAutoSplit() ? "techguns.container.info.on" : "techguns.container.info.off"), 20, 40);
    }
}
