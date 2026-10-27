package techguns.client.gui.machine;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.TGIds;
import techguns.camo.CamoData;
import techguns.machine.CamoBenchBlockEntity;
import techguns.machine.CamoBenchMenu;
import techguns.machine.RepairBenchBlockEntity;

/**
 * Camo Bench screen (Techguns 2 {@code CamoBenchGui}): the player model with the offhand slot on the left, the input
 * item's camo with cycle arrows, camo cycle buttons and camo numbers for the worn armor, and on the right a
 * Fabric-style list of every look of the input item to pick from.
 */
public class CamoBenchScreen extends TGMachineScreen<CamoBenchMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/camo_bench_gui.png");
    private static final int PANEL_PADDING = 4;
    private static final int PANEL_COLOR = 0xFFC6C6C6;

    private final TGRecipeGrid grid;

    public CamoBenchScreen(CamoBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        this.powered = false;
        this.hasUpgradeSlot = false;
        this.securityButtonY = 120;
        // the armor "-" buttons sit where the inventory title would be
        this.showInventoryTitle = false;
        this.grid = new TGRecipeGrid(this::variants, this::currentVariant,
                index -> pressButton(CamoBenchBlockEntity.BUTTON_VARIANT + index));
    }

    private ItemStack input() {
        return menu.machine().getInventory().getStackInSlot(CamoBenchBlockEntity.SLOT_INPUT);
    }

    private List<ItemStack> variants() {
        return minecraft == null || minecraft.level == null ? List.of() : CamoBenchBlockEntity.variants(minecraft.level, input());
    }

    private int currentVariant() {
        return minecraft == null || minecraft.level == null ? -1 : CamoBenchBlockEntity.currentVariant(minecraft.level, input());
    }

    private int panelX() {
        return leftPos + imageWidth + 2;
    }

    @Override
    protected void init() {
        super.init();
        grid.setPosition(panelX() + PANEL_PADDING, topPos + PANEL_PADDING);
        addRenderableWidget(Button.builder(Component.literal(">"), b -> pressButton(CamoBenchBlockEntity.BUTTON_NEXT))
                .bounds(leftPos + 26, topPos + 59, 12, 12).build());
        addRenderableWidget(Button.builder(Component.literal("<"), b -> pressButton(CamoBenchBlockEntity.BUTTON_PREVIOUS))
                .bounds(leftPos + 14, topPos + 59, 12, 12).build());
        for (int i = 0; i < RepairBenchBlockEntity.ARMOR_SLOTS.size(); i++) {
            int id = CamoBenchBlockEntity.BUTTON_ARMOR + 2 * i;
            addRenderableWidget(Button.builder(Component.literal("+"), b -> pressButton(id))
                    .bounds(leftPos + 102 + i * 18, topPos + 59, 10, 10).build());
            addRenderableWidget(Button.builder(Component.literal("-"), b -> pressButton(id + 1))
                    .bounds(leftPos + 102 + i * 18, topPos + 69, 10, 10).build());
        }
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        // the face/back/hand slots of the Techguns 2 texture: the extra slots live in Curios or their own screen here
        graphics.fill(x + 40, y + 15, x + 98, y + 48, PANEL_COLOR);
        graphics.blit(TEXTURE, x - 45, y + 6, 178, 0, 45, 84);
        graphics.blit(TEXTURE, x - 18, y + 93, 7, 83, 18, 18);
        if (minecraft != null && minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x - 42, y + 9, x - 3, y + 87, 30, 0.0625f,
                    lastMouseX, lastMouseY, minecraft.player);
        }
        int panelX = panelX();
        int width = TGRecipeGrid.width() + 2 * PANEL_PADDING;
        int height = TGRecipeGrid.height() + 2 * PANEL_PADDING;
        graphics.fill(panelX, y, panelX + width, y + height, 0xFF000000);
        graphics.fill(panelX + 1, y + 1, panelX + width - 1, y + height - 1, PANEL_COLOR);
        grid.render(graphics, lastMouseX, lastMouseY);
    }

    private int lastMouseX;
    private int lastMouseY;

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderMachineTooltips(GuiGraphics graphics, int mx, int my, int mouseX, int mouseY) {
        super.renderMachineTooltips(graphics, mx, my, mouseX, mouseY);
        if (menu.getCarried().isEmpty()) {
            grid.renderTooltip(graphics, font, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        ItemStack input = input();
        List<ItemStack> variants = variants();
        Component name = Component.translatable("techguns.item.invalidcamo");
        String index = "";
        if (!variants.isEmpty() && minecraft != null && minecraft.level != null) {
            index = (currentVariant() + 1) + "/" + variants.size();
            name = CamoData.current(minecraft.level.registryAccess(), input).map(camo -> camo.display()).orElse(Component.empty());
        }
        drawText(graphics, Component.literal(index), 10, 40);
        drawText(graphics, name, 10, 50);
        if (minecraft != null && minecraft.player != null) {
            for (int i = 0; i < RepairBenchBlockEntity.ARMOR_SLOTS.size(); i++) {
                ItemStack armor = minecraft.player.getItemBySlot(RepairBenchBlockEntity.ARMOR_SLOTS.get(i));
                int camo = CamoData.camos(minecraft.level.registryAccess(), armor).indexOf(
                        CamoData.current(minecraft.level.registryAccess(), armor).orElse(null)) + 1;
                drawText(graphics, Component.literal(String.valueOf(camo)), 104 + i * 18, 38);
            }
        }
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

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        return !grid.isOver(mouseX, mouseY) && super.hasClickedOutside(mouseX, mouseY, left, top, button)
                && !(mouseX >= left - 45 && mouseX < left && mouseY >= top && mouseY < top + 112);
    }
}
