package techguns.client.gui.machine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.ExplosiveChargeBlockEntity;
import techguns.machine.ExplosiveChargeMenu;

/**
 * Mining charge screen (Techguns 2 {@code ExplosiveChargeGui}): radius, length and delay with +/- buttons, and the
 * detonate button that arms the charge and closes the screen.
 */
public class ExplosiveChargeScreen extends AbstractContainerScreen<ExplosiveChargeMenu> {
    private static final ResourceLocation TNT = TGIds.id("textures/gui/tnt_charge_gui.png");
    private static final ResourceLocation ADVANCED = TGIds.id("textures/gui/exp_charge_gui.png");
    private static final int TEXT_COLOR = 0x404040;

    private final ResourceLocation texture;

    public ExplosiveChargeScreen(ExplosiveChargeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        texture = menu.charge().kind() == ExplosiveChargeBlockEntity.Kind.ADVANCED ? ADVANCED : TNT;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("techguns.explosivecharge.detonate"), button -> {
            press(ExplosiveChargeBlockEntity.BUTTON_ARM);
            onClose();
        }).bounds(leftPos + 57, topPos + 73, 60, 20).build());
        int id = ExplosiveChargeBlockEntity.BUTTON_RADIUS_UP;
        for (int row = 0; row < 3; row++) {
            int y = topPos + 40 + row * 10;
            int up = id++;
            int down = id++;
            addRenderableWidget(Button.builder(Component.literal("+"), button -> press(up)).bounds(leftPos + 120, y, 10, 10).build());
            addRenderableWidget(Button.builder(Component.literal("-"), button -> press(down)).bounds(leftPos + 130, y, 10, 10).build());
        }
    }

    private void press(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("techguns.explosivecharge.radius", menu.radius()), 37, 40, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("techguns.explosivecharge.length", menu.length()), 37, 50, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("techguns.explosivecharge.delay", menu.fuse() / 20), 37, 60, TEXT_COLOR, false);
    }
}
