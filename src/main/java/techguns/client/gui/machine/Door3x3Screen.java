package techguns.client.gui.machine;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.block.Door3x3BlockEntity;
import techguns.block.Door3x3Menu;
import techguns.machine.RedstoneMode;

/**
 * Door settings screen (Techguns 2 {@code Door3x3Gui}): the mode button and name, the auto close toggle (only for manual
 * doors), a warning when a redstone door ignores redstone, and the redstone and security buttons on the left side like
 * the machine screens, on the empty dungeon scanner panel like Techguns 2 (only its top is used).
 */
// MIGRATION(26.x): GuiGraphics#blit and screen rendering change with deferred GUI rendering (1.21.6).
public class Door3x3Screen extends AbstractContainerScreen<Door3x3Menu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/dungeon_scanner_gui.png");
    private static final int TEXT_COLOR = 0x404040;
    private static final int PANEL_TOP = 76;
    private static final int PANEL_BOTTOM = 4;

    private Button autoCloseButton;

    public Door3x3Screen(Door3x3Menu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = PANEL_TOP + PANEL_BOTTOM;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("techguns.container.door3x3.mode"), button -> press(Door3x3BlockEntity.BUTTON_MODE))
                .bounds(leftPos + 8, topPos + 16, 32, 16).build());
        autoCloseButton = addRenderableWidget(Button.builder(CommonComponents.OPTION_ON, button -> press(Door3x3BlockEntity.BUTTON_AUTO_CLOSE))
                .bounds(leftPos + 8, topPos + 36, 32, 16).build());
        addRenderableWidget(new TGIconButton(leftPos - 22, topPos + 10, 20, button -> press(Door3x3BlockEntity.BUTTON_REDSTONE),
                TGMachineScreen.WIDGETS, 234, () -> 10 + 16 * menu.redstoneMode().ordinal(), 16));
        addRenderableWidget(new TGIconButton(leftPos - 22, topPos + 40, 20, button -> press(Door3x3BlockEntity.BUTTON_SECURITY),
                TGMachineScreen.WIDGETS, 234, () -> 58 + 16 * menu.security(), 16));
    }

    private void press(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        autoCloseButton.setMessage(menu.autoClose() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
        autoCloseButton.active = menu.mode() == Door3x3BlockEntity.Mode.MANUAL;
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        int mx = mouseX - leftPos;
        int my = mouseY - topPos;
        if (isIn(mx, my, -22, 10)) {
            graphics.renderComponentTooltip(font, TGMachineScreen.redstoneTooltip(menu.redstoneMode(), menu.hasSignal()), mouseX, mouseY);
        } else if (isIn(mx, my, -22, 40)) {
            graphics.renderComponentTooltip(font, TGMachineScreen.securityTooltip(menu.security(), menu.door().getOwnerName()), mouseX, mouseY);
        }
    }

    private static boolean isIn(int mx, int my, int x, int y) {
        return mx >= x && mx <= x + 20 && my >= y && my <= y + 20;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, PANEL_TOP);
        graphics.blit(TEXTURE, leftPos, topPos + PANEL_TOP, 0, 166 - PANEL_BOTTOM, imageWidth, PANEL_BOTTOM);
        // button backgrounds sticking out on the left
        graphics.blit(TGMachineScreen.WIDGETS, leftPos - 27, topPos + 5, 195, 0, 27, 30);
        graphics.blit(TGMachineScreen.WIDGETS, leftPos - 27, topPos + 35, 195, 0, 27, 30);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, TEXT_COLOR, false);
        Door3x3BlockEntity.Mode mode = menu.mode();
        graphics.drawString(font, Component.translatable(mode.translationKey()), 44, 20, TEXT_COLOR, false);
        Component autoClose = Component.translatable("techguns.container.door3x3.autoclose");
        graphics.drawString(font, mode == Door3x3BlockEntity.Mode.MANUAL ? autoClose : autoClose.copy().withStyle(ChatFormatting.STRIKETHROUGH),
                44, 40, TEXT_COLOR, false);
        if (mode == Door3x3BlockEntity.Mode.REDSTONE && menu.redstoneMode() == RedstoneMode.IGNORE) {
            graphics.drawString(font, Component.translatable("techguns.container.door3x3.redstonewarning").withStyle(ChatFormatting.RED),
                    8, 60, TEXT_COLOR, false);
        }
    }
}
