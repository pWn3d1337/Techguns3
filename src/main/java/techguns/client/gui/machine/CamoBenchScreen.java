package techguns.client.gui.machine;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import techguns.TGIds;
import techguns.registry.TGBlocks;
import techguns.slot.TGSlotType;
import techguns.machine.CamoBenchBlockEntity;
import techguns.machine.CamoBenchMenu;

/**
 * Camo Bench screen: the input slot of the Techguns 2 {@code CamoBenchGui} with the player model on the left,
 * creative-style tabs on top to pick what to work on (the input, a worn armor piece or a worn gear item; the gear lives in
 * Curios or the extra slot screen; more tabs than fit over the screen scroll with arrow buttons, as the creative tab pages
 * do), and a Fabric-style list of every look of that item to pick from.
 */
// MIGRATION(26.x): blitSprite, atlas sprite blits and item rendering through GuiGraphics change with deferred GUI
// rendering (1.21.6).
public class CamoBenchScreen extends TGMachineScreen<CamoBenchMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/camo_bench_gui.png");
    private static final ResourceLocation[] TAB_UNSELECTED = new ResourceLocation[7];
    private static final ResourceLocation[] TAB_SELECTED = new ResourceLocation[7];
    static {
        for (int i = 0; i < 7; i++) {
            TAB_UNSELECTED[i] = TGIds.vanilla("container/creative_inventory/tab_top_unselected_" + (i + 1));
            TAB_SELECTED[i] = TGIds.vanilla("container/creative_inventory/tab_top_selected_" + (i + 1));
        }
    }
    private static final ResourceLocation[] EMPTY_ARMOR = {InventoryMenu.EMPTY_ARMOR_SLOT_HELMET,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS};
    private static final String[] TAB_KEYS = {"input", "head", "chest", "legs", "feet", "face", "back", "hand"};
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int TAB_SPACING = 27;
    /** Tabs reach 4 pixels into the screen, as in the creative inventory. */
    private static final int TAB_Y = -28;
    /** The list (cells and scroller) ends at the texture's inner right edge. */
    private static final int LIST_X = 90;
    private static final int LIST_Y = 13;
    /** Tabs shown at once between the scroll buttons, which sit at the screen's left and right edge. */
    private static final int VISIBLE_TABS = 5;
    private static final int SCROLL_BUTTON_WIDTH = 12;
    private static final int TABS_X = SCROLL_BUTTON_WIDTH
            + (176 - 2 * SCROLL_BUTTON_WIDTH - (VISIBLE_TABS * TAB_SPACING - 1)) / 2;

    private final TGRecipeGrid grid;
    private int tab = CamoBenchBlockEntity.TARGET_INPUT;
    /** First tab shown. */
    private int firstTab;
    @Nullable
    private Button scrollBack;
    @Nullable
    private Button scrollForward;

    public CamoBenchScreen(CamoBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        this.powered = false;
        this.hasUpgradeSlot = false;
        this.securityButtonY = 120;
        this.grid = new TGRecipeGrid(this::variants, this::currentVariant,
                index -> pressButton(CamoBenchBlockEntity.variantButton(tab, index)));
    }

    private ItemStack target(int target) {
        return minecraft == null || minecraft.player == null ? ItemStack.EMPTY : menu.machine().targetStack(minecraft.player, target);
    }

    // the camo lookups stream and sort the camo registry: computed again only when the tab or its item changes
    private int cachedTab = -1;
    @Nullable
    private ItemStack cachedStack;
    private List<ItemStack> cachedVariants = List.of();
    private int cachedCurrent = -1;

    private void refresh() {
        ItemStack stack = target(tab);
        if (cachedTab == tab && cachedStack != null && ItemStack.matches(stack, cachedStack)) {
            return;
        }
        cachedTab = tab;
        cachedStack = stack.copy();
        if (minecraft == null || minecraft.level == null) {
            cachedVariants = List.of();
            cachedCurrent = -1;
            return;
        }
        cachedVariants = CamoBenchBlockEntity.variants(minecraft.level, stack);
        cachedCurrent = CamoBenchBlockEntity.currentVariant(minecraft.level, stack);
    }

    private List<ItemStack> variants() {
        refresh();
        return cachedVariants;
    }

    private int currentVariant() {
        refresh();
        return cachedCurrent;
    }

    @Override
    protected void init() {
        super.init();
        grid.setPosition(leftPos + LIST_X, topPos + LIST_Y);
        int y = topPos + TAB_Y + 4;
        scrollBack = addRenderableWidget(Button.builder(Component.literal("<"), b -> scrollTabs(-1))
                .bounds(leftPos, y, SCROLL_BUTTON_WIDTH, 20).build());
        scrollForward = addRenderableWidget(Button.builder(Component.literal(">"), b -> scrollTabs(1))
                .bounds(leftPos + 176 - SCROLL_BUTTON_WIDTH, y, SCROLL_BUTTON_WIDTH, 20).build());
        scrollTabs(0);
    }

    private void scrollTabs(int amount) {
        firstTab = Math.clamp(firstTab + amount, 0, CamoBenchBlockEntity.TARGETS - VISIBLE_TABS);
        if (scrollBack != null && scrollForward != null) {
            scrollBack.active = firstTab > 0;
            scrollForward.active = firstTab < CamoBenchBlockEntity.TARGETS - VISIBLE_TABS;
        }
    }

    private boolean tabShown(int target) {
        return target >= firstTab && target < firstTab + VISIBLE_TABS;
    }

    private int tabX(int target) {
        return leftPos + TABS_X + (target - firstTab) * TAB_SPACING;
    }

    private int tabAt(double mouseX, double mouseY) {
        int y = topPos + TAB_Y;
        for (int i = firstTab; i < firstTab + VISIBLE_TABS; i++) {
            if (mouseX >= tabX(i) && mouseX < tabX(i) + TAB_WIDTH && mouseY >= y && mouseY < y + TAB_HEIGHT - 4) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        for (int i = firstTab; i < firstTab + VISIBLE_TABS; i++) {
            if (i != tab) {
                renderTab(graphics, i);
            }
        }
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        if (tabShown(tab)) {
            renderTab(graphics, tab);
        }
    }

    private void renderTab(GuiGraphics graphics, int target) {
        int x = tabX(target);
        int y = topPos + TAB_Y;
        // a middle tab sprite: no tab lines up with the screen's corners
        graphics.blitSprite((target == tab ? TAB_SELECTED : TAB_UNSELECTED)[1], x, y, TAB_WIDTH, TAB_HEIGHT);
        ItemStack stack = target(target);
        x += 5;
        y += 9;
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x, y);
        } else if (target == CamoBenchBlockEntity.TARGET_INPUT) {
            graphics.renderItem(new ItemStack(TGBlocks.CAMO_BENCH.get()), x, y);
        } else if (target < CamoBenchBlockEntity.TARGET_GEAR) {
            graphics.blit(x, y, 0, 16, 16, minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(EMPTY_ARMOR[target - CamoBenchBlockEntity.TARGET_ARMOR]));
        } else {
            TGSlotType slot = TGSlotType.camoSlots().get(target - CamoBenchBlockEntity.TARGET_GEAR);
            graphics.blit(TGIds.id("textures/gui/emptyslots/emptyslot_" + slot.id() + ".png"), x, y, 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        graphics.blit(TEXTURE, x - 45, y + 6, 178, 0, 45, 84);
        if (minecraft != null && minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x - 42, y + 9, x - 3, y + 87, 30, 0.0625f,
                    lastMouseX, lastMouseY, minecraft.player);
        }
        // the gear and armor slots and the camo number boxes of the Techguns 2 texture (the tabs and the list replace them)
        graphics.fill(x + 38, y + 14, x + 172, y + 48, 0xFFC6C6C6);
        // sunken backgrounds of the cells and the scroller, as in the stonecutter
        inset(graphics, x + LIST_X - 1, y + LIST_Y, 66, grid.trackHeight() + 3);
        inset(graphics, x + LIST_X + 66, y + LIST_Y, 14, grid.trackHeight() + 3);
        grid.render(graphics, lastMouseX, lastMouseY);
    }

    private static void inset(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFFFFFFFF);
        graphics.fill(x, y, x + width - 1, y + height - 1, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF8B8B8B);
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
        if (!menu.getCarried().isEmpty()) {
            return;
        }
        int hoveredTab = tabAt(mouseX, mouseY);
        if (hoveredTab >= 0) {
            ItemStack stack = target(hoveredTab);
            graphics.renderComponentTooltip(font, List.of(Component.translatable("techguns.container.camobench.tab." + TAB_KEYS[hoveredTab]),
                    (stack.isEmpty() ? Component.translatable("techguns.gui.empty") : stack.getHoverName()).copy()
                            .withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        grid.renderTooltip(graphics, font, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // the title left of the list
        drawText(graphics, title, 8, 6);
        drawText(graphics, playerInventoryTitle, 8, imageHeight - 96 + 2);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int clickedTab = tabAt(mouseX, mouseY);
        if (clickedTab >= 0) {
            if (clickedTab != tab) {
                tab = clickedTab;
                grid.scrollToTop();
                if (minecraft != null) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
            }
            return true;
        }
        return grid.mouseClicked(mouseX, mouseY) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return grid.mouseDragged(mouseY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseY >= topPos + TAB_Y && mouseY < topPos && mouseX >= leftPos && mouseX < leftPos + 176) {
            scrollTabs(scrollY > 0 ? -1 : 1);
            return true;
        }
        return grid.mouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        return super.hasClickedOutside(mouseX, mouseY, left, top, button)
                && !(mouseX >= left - 45 && mouseX < left && mouseY >= top + 6 && mouseY < top + 90)
                && !(mouseX >= left && mouseX < left + imageWidth && mouseY >= top + TAB_Y && mouseY < top);
    }
}
