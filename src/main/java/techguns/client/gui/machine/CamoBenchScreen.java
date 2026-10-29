package techguns.client.gui.machine;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.TGIds;
import techguns.camo.CamoData;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotType;
import techguns.machine.CamoBenchBlockEntity;
import techguns.machine.CamoBenchMenu;
import techguns.machine.RepairBenchBlockEntity;

/**
 * Camo Bench screen (Techguns 2 {@code CamoBenchGui}): the player model with the offhand slot on the left, the input
 * item's camo with cycle arrows, camo cycle buttons and camo numbers for the worn armor and the worn face/back/hand gear
 * (shown in the texture's gear slots; they live in Curios or the extra slot screen), and on the right a Fabric-style
 * list of every look of the input item to pick from.
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

    // the camo lookups stream and sort the camo registry: computed again only when the input or an armor piece changes
    @Nullable
    private ItemStack cachedInput;
    private List<ItemStack> cachedVariants = List.of();
    private int cachedCurrent = -1;
    private Component cachedName = Component.empty();
    /** Worn armor pieces, then the face/back/hand gear. */
    private static final int WORN = RepairBenchBlockEntity.ARMOR_SLOTS.size() + TGSlotType.camoSlots().size();
    /** Left edge of the face/back/hand slots of the texture (the armor slots follow at 99). */
    private static final int GEAR_X = 45;
    private final ItemStack[] cachedWorn = new ItemStack[WORN];
    private final int[] cachedWornCamo = new int[WORN];

    private void refreshInput() {
        ItemStack input = input();
        if (cachedInput != null && ItemStack.matches(input, cachedInput)) {
            return;
        }
        cachedInput = input.copy();
        if (minecraft == null || minecraft.level == null) {
            cachedVariants = List.of();
            cachedCurrent = -1;
            cachedName = Component.empty();
            return;
        }
        cachedVariants = CamoBenchBlockEntity.variants(minecraft.level, input);
        cachedCurrent = CamoBenchBlockEntity.currentVariant(minecraft.level, input);
        cachedName = CamoData.current(minecraft.level.registryAccess(), input).map(camo -> camo.display()).orElse(Component.empty());
    }

    private List<ItemStack> variants() {
        refreshInput();
        return cachedVariants;
    }

    private int currentVariant() {
        refreshInput();
        return cachedCurrent;
    }

    /** Camo number (1-based, 0 = no camos) of worn item {@code i}: armor pieces, then the gear. */
    private int wornCamo(int i, ItemStack worn) {
        if (cachedWorn[i] == null || !ItemStack.matches(worn, cachedWorn[i])) {
            cachedWorn[i] = worn.copy();
            cachedWornCamo[i] = minecraft == null || minecraft.level == null ? 0
                    : CamoData.camos(minecraft.level.registryAccess(), worn).indexOf(
                            CamoData.current(minecraft.level.registryAccess(), worn).orElse(null)) + 1;
        }
        return cachedWornCamo[i];
    }

    private ItemStack gear(TGSlotType slot) {
        return minecraft == null || minecraft.player == null ? ItemStack.EMPTY : ExtraSlots.get(minecraft.player, slot);
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
        for (TGSlotType slot : TGSlotType.camoSlots()) {
            int id = CamoBenchBlockEntity.BUTTON_GEAR + 2 * slot.ordinal();
            int x = leftPos + GEAR_X + 3 + slot.ordinal() * 18;
            addRenderableWidget(Button.builder(Component.literal("+"), b -> pressButton(id)).bounds(x, topPos + 59, 10, 10).build());
            addRenderableWidget(Button.builder(Component.literal("-"), b -> pressButton(id + 1)).bounds(x, topPos + 69, 10, 10).build());
        }
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
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
            for (TGSlotType slot : TGSlotType.camoSlots()) {
                ItemStack gear = gear(slot);
                if (!gear.isEmpty() && isIn(mx, my, GEAR_X + slot.ordinal() * 18, 18, 16, 16)) {
                    graphics.renderTooltip(font, gear, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        List<ItemStack> variants = variants();
        Component name = Component.translatable("techguns.item.invalidcamo");
        String index = "";
        if (!variants.isEmpty()) {
            index = (currentVariant() + 1) + "/" + variants.size();
            name = cachedName;
        }
        drawText(graphics, Component.literal(index), 10, 40);
        drawText(graphics, name, 10, 50);
        if (minecraft != null && minecraft.player != null) {
            for (int i = 0; i < RepairBenchBlockEntity.ARMOR_SLOTS.size(); i++) {
                ItemStack armor = minecraft.player.getItemBySlot(RepairBenchBlockEntity.ARMOR_SLOTS.get(i));
                int camo = wornCamo(i, armor);
                drawText(graphics, Component.literal(String.valueOf(camo)), 104 + i * 18, 38);
            }
            // the worn gear drawn in the gear slots of the texture (no slots: the items stay where they are worn)
            for (TGSlotType slot : TGSlotType.camoSlots()) {
                ItemStack gear = gear(slot);
                int x = GEAR_X + slot.ordinal() * 18;
                graphics.renderItem(gear, x, 18);
                int camo = wornCamo(RepairBenchBlockEntity.ARMOR_SLOTS.size() + slot.ordinal(), gear);
                drawText(graphics, Component.literal(String.valueOf(camo)), x + 5, 38);
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
