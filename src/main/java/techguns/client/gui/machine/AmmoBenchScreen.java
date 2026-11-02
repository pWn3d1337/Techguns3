package techguns.client.gui.machine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import techguns.machine.AmmoBenchMenu;
import techguns.TGIds;

/**
 * Ammo Bench screen (Fabric {@code AmmoBenchScreen}): the vanilla stonecutter screen with three input slots and the
 * list of rounds the inputs can make.
 */
// MIGRATION(26.x): GuiGraphics#blit and screen rendering change with deferred GUI rendering (1.21.6).
public class AmmoBenchScreen extends AbstractContainerScreen<AmmoBenchMenu> {
    private static final ResourceLocation TEXTURE = TGIds.vanilla("textures/gui/container/stonecutter.png");
    /** Empty slot icons of the core, casing and powder slots, as in the Ammo Press ({@code AmmoPressMenu}). */
    private static final String[] SLOT_ICONS = {"ingot_dark", "ingot", "powder"};

    private final TGRecipeGrid grid;

    public AmmoBenchScreen(AmmoBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.grid = new TGRecipeGrid(menu::choices, menu::selected, index -> {
            if (menu.clickMenuButton(inventory.player, index) && minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
            }
        }).showCounts();
        --titleLabelY;
    }

    @Override
    protected void init() {
        super.init();
        grid.setPosition(leftPos + 52, topPos + 14);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!grid.renderTooltip(graphics, font, mouseX, mouseY)) {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        // the stonecutter's single input slot becomes three (Fabric)
        graphics.blit(TEXTURE, x + 19, y + 32, 19, 14, 18, 18);
        graphics.blit(TEXTURE, x + 19, y + 14, 19, 32, 18, 18);
        graphics.blit(TEXTURE, x + 19, y + 33, 19, 32, 18, 18);
        graphics.blit(TEXTURE, x + 19, y + 52, 19, 32, 18, 18);
        // empty input slots show the Ammo Press's slot icons (menu slots 0-2 are the inputs)
        for (int i = 0; i < SLOT_ICONS.length; i++) {
            Slot slot = menu.getSlot(i);
            if (!slot.hasItem()) {
                graphics.blit(TGIds.id("textures/gui/emptyslots/emptyslot_" + SLOT_ICONS[i] + ".png"), x + slot.x, y + slot.y, 0, 0, 16, 16, 16, 16);
            }
        }
        grid.render(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return grid.mouseClicked(mouseX, mouseY) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return grid.mouseDragged(mouseY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return grid.mouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
