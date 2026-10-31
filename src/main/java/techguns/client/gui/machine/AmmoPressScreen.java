package techguns.client.gui.machine;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.TGIds;
import techguns.machine.AmmoPressBlockEntity;
import techguns.machine.AmmoPressMenu;
import techguns.recipe.AmmoPressRecipe;

/**
 * Ammo Press screen (Techguns 2 {@code AmmoPressGui}): progress arrow and the build plans as a stonecutter-style list
 * like the Ammo Bench (Techguns 2 cycled them with arrow buttons).
 */
public class AmmoPressScreen extends TGMachineScreen<AmmoPressMenu> {
    private static final net.minecraft.resources.ResourceLocation TEXTURE = TGIds.id("textures/gui/ammo_press_gui.png");
    /** The list with its border sits centred between the power bar (ends at 13) and the first input slot (starts at 99). */
    private static final int POWER_BAR_END = 13;
    private static final int INPUT_SLOTS_START = 99;
    /** Top of the list, level with the inside of the input slots. */
    private static final int LIST_TOP = 17;
    /** Two rows hold eight plans without a scroller; more (datapacks) scroll. */
    private static final int ROWS = 2;
    private static final int PANEL_COLOR = 0xFF8B8B8B;
    private static final int PANEL_SHADOW = 0xFF373737;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;

    private final TGRecipeGrid grid;

    public AmmoPressScreen(AmmoPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        this.grid = new TGRecipeGrid(this::choices, this::selected, index -> pressButton(AmmoPressBlockEntity.BUTTON_SELECT + index))
                .showCounts().rows(ROWS).hideScrollerWhenFits();
    }

    private List<ItemStack> choices() {
        return menu.machine().plans().stream().map(AmmoPressRecipe::result).toList();
    }

    private int selected() {
        int count = menu.machine().plans().size();
        return count == 0 ? -1 : Math.floorMod(menu.plan(), count);
    }

    @Override
    protected void init() {
        super.init();
        // the grid draws its cells from one pixel below its origin
        grid.setPosition(leftPos + listLeft(), topPos + LIST_TOP - 1);
    }

    /** Left edge of the list inside its one pixel border. */
    private int listLeft() {
        int bordered = grid.width() + 2;
        return POWER_BAR_END + (INPUT_SLOTS_START - POWER_BAR_END - bordered) / 2 + 1;
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        if (menu.totalTime() > 0) {
            graphics.blit(TEXTURE, x + 119, y + 36, 176, 0, 19, progressScaled(21) + 1);
        }
        // inset like a vanilla slot: dark top left edge, light bottom right edge
        int left = x + listLeft();
        int right = left + grid.width();
        int top = y + LIST_TOP;
        int bottom = top + grid.trackHeight();
        graphics.fill(left - 1, top - 1, right, bottom, PANEL_SHADOW);
        graphics.fill(left, top, right + 1, bottom + 1, PANEL_LIGHT);
        graphics.fill(left, top, right, bottom, PANEL_COLOR);
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
