package techguns.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import techguns.TGIds;
import techguns.network.TGNetwork;
import techguns.registry.TGMenus;
import techguns.slot.ExtraSlots;
import techguns.slot.menu.ExtraSlotsMenu;

/**
 * Fallback extra slot screen (no Curios): face, back and hand slot next to the inventory, opened with a button on the
 * vanilla inventory screen. The panel is drawn in code in the vanilla style (the Techguns 2 texture carries slots of
 * features that do not exist here); the empty slot icons are the 1.12 ones.
 */
// MIGRATION(26.x): AbstractContainerScreen, GuiGraphics drawing and ScreenEvent.Init change with the 1.21.2+ GUI pipeline.
public class ExtraSlotsScreen extends AbstractContainerScreen<ExtraSlotsMenu> {
    private static final ResourceLocation[] EMPTY_ICONS = {
            TGIds.id("textures/gui/emptyslots/emptyslot_face.png"),
            TGIds.id("textures/gui/emptyslots/emptyslot_back.png"),
            TGIds.id("textures/gui/emptyslots/emptyslot_hand.png")};

    public ExtraSlotsScreen(ExtraSlotsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(TGMenus.EXTRA_SLOTS.get(), ExtraSlotsScreen::new);
    }

    /** Adds the "TG" button to the vanilla survival inventory (not to the creative one, which is another screen class). */
    public static void onInventoryScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen && !ExtraSlots.usesCurios()) {
            event.addListener(new OpenButton(screen));
        }
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.techguns.inventory"), button -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // tells the server to close this menu, then reopen the vanilla screen
                mc.player.closeContainer();
                mc.setScreen(new InventoryScreen(mc.player));
            }
        }).bounds(leftPos + 126, topPos + 61, 20, 18).tooltip(Tooltip.create(Component.translatable("gui.techguns.inventory.tooltip"))).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        // panel: black outline, white top-left / dark bottom-right bevel, grey fill
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFFFFFFF);
        graphics.fill(x + 3, y + 3, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);
        graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFFC6C6C6);
        for (Slot slot : menu.slots) {
            drawSlotBox(graphics, x + slot.x - 1, y + slot.y - 1);
        }
        // entity box
        graphics.fill(x + 25, y + 17, x + 76, y + 79, 0xFF373737);
        graphics.fill(x + 26, y + 18, x + 75, y + 78, 0xFF000000);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x + 26, y + 18, x + 75, y + 78, 30, 0.0625F, mouseX, mouseY,
                Minecraft.getInstance().player);
        // empty slot icons
        for (int i = 0; i < ExtraSlotsMenu.SLOT_COUNT; i++) {
            Slot slot = menu.slots.get(i);
            if (!slot.hasItem()) {
                graphics.blit(EMPTY_ICONS[i], x + slot.x, y + slot.y, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    private static void drawSlotBox(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x, y, x + 17, y + 17, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
    }

    /** Button on the vanilla inventory screen that stays beside the recipe book button, wherever the screen moves. */
    private static final class OpenButton extends Button {
        private final InventoryScreen screen;

        OpenButton(InventoryScreen screen) {
            super(screen.getGuiLeft() + 126, screen.getGuiTop() + 61, 20, 18, Component.translatable("gui.techguns.extra_slots.button"),
                    button -> TGNetwork.sendOpenExtraSlots(), DEFAULT_NARRATION);
            this.screen = screen;
            setTooltip(Tooltip.create(Component.translatable("gui.techguns.extra_slots")));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            setPosition(screen.getGuiLeft() + 126, screen.getGuiTop() + 61);
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }
}
