package techguns.client.gui.machine;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGConfig;
import techguns.TGIds;
import techguns.machine.TGMachineBlockEntity;
import techguns.machine.TGMachineMenu;

/**
 * Screen of a powered machine (Techguns 2 {@code TGBaseGui} → {@code OwnedTileEntGui} → {@code RedstoneTileEntGui} →
 * {@code PoweredTileEntGui}): the machine texture, the power bar, the upgrade slot background, the redstone and
 * security buttons on the left side and their tooltips. All drawing goes through the helpers here.
 */
// MIGRATION(26.x): GuiGraphics#blit and screen rendering change with deferred GUI rendering (1.21.6).
public abstract class TGMachineScreen<M extends TGMachineMenu<?>> extends AbstractContainerScreen<M> {
    /** Shared widget sprites of the Techguns 2 machine screens (power bar, buttons, lights). */
    protected static final ResourceLocation WIDGETS = TGIds.id("textures/gui/ammo_press_gui.png");
    private static final int SECURITY_BUTTON_Y = 40;
    private static final int TEXT_COLOR = 0x404040;

    private final ResourceLocation texture;
    protected boolean hasUpgradeSlot = true;
    protected int upgradeSlotX = 151;
    protected int upgradeSlotY = 59;

    protected TGMachineScreen(M menu, Inventory inventory, Component title, ResourceLocation texture) {
        super(menu, inventory, title);
        this.texture = texture;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new IconButton(leftPos - 22, topPos + 10, TGMachineBlockEntity.BUTTON_REDSTONE,
                () -> 10 + 16 * menu.redstoneMode().ordinal()));
        // Techguns 2 security icons: 0 public ... 3 owner only
        addRenderableWidget(new IconButton(leftPos - 22, topPos + SECURITY_BUTTON_Y, TGMachineBlockEntity.BUTTON_SECURITY,
                () -> 58 + 16 * (menu.isOwnerOnly() ? 3 : 0)));
    }

    /** Sends a machine button press to the server menu ({@link TGMachineMenu#clickMenuButton}). */
    protected void pressButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderMachineTooltips(graphics, mouseX - leftPos, mouseY - topPos, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(texture, x, y, 0, 0, imageWidth, imageHeight);
        // button backgrounds sticking out on the left
        graphics.blit(WIDGETS, x - 27, y + 5, 195, 0, 27, 30);
        graphics.blit(WIDGETS, x - 27, y + SECURITY_BUTTON_Y - 5, 195, 0, 27, 30);
        // redstone lights: green when the machine may work
        boolean enabled = menu.redstoneMode().allows(menu.hasSignal());
        graphics.blit(WIDGETS, x + 6, y + 5, 240, enabled ? 0 : 5, 5, 5);
        graphics.blit(WIDGETS, x + 11, y + 5, 245, enabled ? 5 : 0, 5, 5);
        // power bar
        int diff = TGConfig.MACHINES_NEED_NO_POWER.get() ? 0 : 48 - menu.energy() * 48 / Math.max(1, menu.maxEnergy());
        graphics.blit(WIDGETS, x + 8, y + 17 + diff, 251, 1 + diff, 4, 48 - diff);
        if (hasUpgradeSlot) {
            graphics.blit(WIDGETS, x + upgradeSlotX, y + upgradeSlotY, 119, 59, 20, 20);
        }
        renderMachineBg(graphics, x, y);
    }

    /** Machine-specific background parts (progress bars), at the screen origin {@code x, y}. */
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
    }

    /** Job progress scaled to {@code length} pixels (Techguns 2 {@code getProgressScaled}). */
    protected int progressScaled(int length) {
        return menu.totalTime() > 0 ? menu.progress() * length / menu.totalTime() : 0;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 2, TEXT_COLOR, false);
    }

    protected void drawText(GuiGraphics graphics, Component text, int x, int y) {
        graphics.drawString(font, text, x, y, TEXT_COLOR, false);
    }

    /** Tooltips of the power bar and the side buttons; {@code mx, my} relative to the screen origin. */
    protected void renderMachineTooltips(GuiGraphics graphics, int mx, int my, int mouseX, int mouseY) {
        if (isIn(mx, my, 7, 17, 5, 49)) {
            Component power = TGConfig.MACHINES_NEED_NO_POWER.get() ? Component.translatable("techguns.container.power")
                    : Component.translatable("techguns.container.power.amount", menu.energy(), menu.maxEnergy());
            graphics.renderTooltip(font, power, mouseX, mouseY);
        } else if (isIn(mx, my, 6, 5, 10, 5) || isIn(mx, my, -22, 10, 20, 20)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("techguns.container.redstone"));
            lines.add(Component.translatable("techguns.container.redstone.mode", Component.translatable(menu.redstoneMode().translationKey())));
            lines.add(Component.translatable("techguns.container.redstone.signal",
                    Component.translatable(menu.hasSignal() ? "techguns.redstonemode.signal.high" : "techguns.redstonemode.signal.low")));
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
        } else if (isIn(mx, my, -22, SECURITY_BUTTON_Y, 20, 20)) {
            int level = menu.isOwnerOnly() ? 3 : 0;
            String ownerName = menu.machine().getOwnerName();
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("techguns.container.security.owner", ownerName.isEmpty()
                    ? Component.translatable("techguns.container.security.tooltip.unowned") : Component.literal(ownerName)));
            lines.add(Component.translatable("techguns.container.security.tooltip",
                    Component.translatable("techguns.container.security.tooltip." + level)
                            .withStyle(level == 0 ? ChatFormatting.GREEN : ChatFormatting.RED)));
            lines.add(Component.translatable("techguns.container.security.tooltip.description." + level));
            lines.add(Component.translatable("techguns.container.security.tooltip.descr2"));
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
        }
    }

    protected static boolean isIn(int mx, int my, int x, int y, int width, int height) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    /** Vanilla button with a 16x16 Techguns 2 icon from {@link #WIDGETS} (redstone and security states). */
    private final class IconButton extends Button {
        private final java.util.function.IntSupplier iconV;

        IconButton(int x, int y, int id, java.util.function.IntSupplier iconV) {
            super(x, y, 20, 20, Component.empty(), button -> pressButton(id), DEFAULT_NARRATION);
            this.iconV = iconV;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            graphics.blit(WIDGETS, getX() + 2, getY() + 2, 234, iconV.getAsInt(), 16, 16);
        }
    }
}
