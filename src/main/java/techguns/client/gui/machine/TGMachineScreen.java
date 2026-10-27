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
    private static final int TEXT_COLOR = 0x404040;

    private final ResourceLocation texture;
    protected boolean hasUpgradeSlot = true;
    protected int upgradeSlotX = 151;
    protected int upgradeSlotY = 59;
    /** False for the unpowered benches (Techguns 2 {@code OwnedTileEntGui}): only the security button. */
    protected boolean powered = true;
    protected int securityButtonY = 40;
    protected boolean showInventoryTitle = true;

    protected TGMachineScreen(M menu, Inventory inventory, Component title, ResourceLocation texture) {
        super(menu, inventory, title);
        this.texture = texture;
    }

    @Override
    protected void init() {
        super.init();
        if (powered) {
            addRenderableWidget(new IconButton(leftPos - 22, topPos + 10, 20, TGMachineBlockEntity.BUTTON_REDSTONE, WIDGETS, 234,
                    () -> 10 + 16 * menu.redstoneMode().ordinal(), 16));
        }
        // Techguns 2 security icons: 0 public ... 3 owner only
        addRenderableWidget(new IconButton(leftPos - 22, topPos + securityButtonY, 20, TGMachineBlockEntity.BUTTON_SECURITY, WIDGETS, 234,
                () -> 58 + 16 * (menu.isOwnerOnly() ? 3 : 0), 16));
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
        graphics.blit(WIDGETS, x - 27, y + securityButtonY - 5, 195, 0, 27, 30);
        if (powered) {
            graphics.blit(WIDGETS, x - 27, y + 5, 195, 0, 27, 30);
            // redstone lights: green when the machine may work
            boolean enabled = menu.redstoneMode().allows(menu.hasSignal());
            graphics.blit(WIDGETS, x + 6, y + 5, 240, enabled ? 0 : 5, 5, 5);
            graphics.blit(WIDGETS, x + 11, y + 5, 245, enabled ? 5 : 0, 5, 5);
            // power bar
            int diff = TGConfig.MACHINES_NEED_NO_POWER.get() ? 0 : 48 - menu.energy() * 48 / Math.max(1, menu.maxEnergy());
            graphics.blit(WIDGETS, x + 8, y + 17 + diff, 251, 1 + diff, 4, 48 - diff);
        }
        if (hasUpgradeSlot) {
            graphics.blit(WIDGETS, x + upgradeSlotX, y + upgradeSlotY, 119, 59, 20, 20);
        }
        menu.slotIcons().forEach((slot, icon) -> {
            if (!slot.hasItem()) {
                graphics.blit(TGIds.id("textures/gui/emptyslots/emptyslot_" + icon + ".png"), x + slot.x, y + slot.y, 0, 0, 16, 16, 16, 16);
            }
        });
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
        if (showInventoryTitle) {
            graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 2, TEXT_COLOR, false);
        }
    }

    protected void drawText(GuiGraphics graphics, Component text, int x, int y) {
        graphics.drawString(font, text, x, y, TEXT_COLOR, false);
    }

    /** Tooltips of the power bar and the side buttons; {@code mx, my} relative to the screen origin. */
    protected void renderMachineTooltips(GuiGraphics graphics, int mx, int my, int mouseX, int mouseY) {
        if (powered && isIn(mx, my, 7, 17, 5, 49)) {
            Component power = TGConfig.MACHINES_NEED_NO_POWER.get() ? Component.translatable("techguns.container.power")
                    : Component.translatable("techguns.container.power.amount", menu.energy(), menu.maxEnergy());
            graphics.renderTooltip(font, power, mouseX, mouseY);
        } else if (powered && (isIn(mx, my, 6, 5, 10, 5) || isIn(mx, my, -22, 10, 20, 20))) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("techguns.container.redstone"));
            lines.add(Component.translatable("techguns.container.redstone.mode", Component.translatable(menu.redstoneMode().translationKey())));
            lines.add(Component.translatable("techguns.container.redstone.signal",
                    Component.translatable(menu.hasSignal() ? "techguns.redstonemode.signal.high" : "techguns.redstonemode.signal.low")));
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
        } else if (isIn(mx, my, -22, securityButtonY, 20, 20)) {
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

    /**
     * Square vanilla button with a Techguns 2 state icon ({@code iconSize} pixels at {@code u, v()} of {@code texture},
     * centred), e.g. the redstone and security states.
     */
    protected final class IconButton extends Button {
        private final ResourceLocation iconTexture;
        private final int iconU;
        private final java.util.function.IntSupplier iconV;
        private final int iconSize;

        public IconButton(int x, int y, int size, int id, ResourceLocation iconTexture, int iconU, java.util.function.IntSupplier iconV, int iconSize) {
            super(x, y, size, size, Component.empty(), button -> pressButton(id), DEFAULT_NARRATION);
            this.iconTexture = iconTexture;
            this.iconU = iconU;
            this.iconV = iconV;
            this.iconSize = iconSize;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            int offset = (getWidth() - iconSize) / 2;
            graphics.blit(iconTexture, getX() + offset, getY() + offset, iconU, iconV.getAsInt(), iconSize, iconSize);
        }
    }

    /**
     * Draws a tank's fluid filling {@code amount / capacity} of the area from the bottom, tiled with the fluid's still
     * texture and tint (Techguns 2 {@code drawFluidWithTesselator}).
     */
    protected void drawFluid(GuiGraphics graphics, net.neoforged.neoforge.fluids.FluidStack fluid, int capacity, int x, int y, int width, int height) {
        if (fluid.isEmpty() || capacity <= 0) {
            return;
        }
        var extensions = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid());
        var sprite = net.minecraft.client.Minecraft.getInstance().getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS)
                .apply(extensions.getStillTexture(fluid));
        int tint = extensions.getTintColor(fluid);
        float r = (tint >> 16 & 0xFF) / 255.0f;
        float g = (tint >> 8 & 0xFF) / 255.0f;
        float b = (tint & 0xFF) / 255.0f;
        int filled = Math.min(height, fluid.getAmount() * height / capacity);
        for (int drawn = 0; drawn < filled; drawn += 16) {
            int h = Math.min(16, filled - drawn);
            graphics.blit(x, y + height - drawn - h, 0, width, h, sprite, r, g, b, 1.0f);
        }
    }

    /** Tooltip lines of a tank: fluid name (or empty) and amount. */
    protected static List<Component> tankTooltip(net.neoforged.neoforge.fluids.FluidStack fluid, int capacity) {
        return List.of(fluid.isEmpty() ? Component.translatable("techguns.gui.empty") : fluid.getHoverName(),
                Component.literal(fluid.getAmount() + "/" + capacity + " mB"));
    }
}
