package techguns.client.gui.debug;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import techguns.TGIds;
import techguns.debug.DungeonGeneratorBlockEntity;
import techguns.debug.DungeonScannerBlockEntity;
import techguns.debug.DungeonToolBlockEntity;
import techguns.debug.DungeonToolMenu;
import techguns.network.DebugPayloads;

/**
 * Techguns 2 {@code DungeonScannerGui} and {@code DungeonGeneratorGui}: ghost toggle, the scanner's segment size, the
 * template name field with the known dungeon templates listed on the right (click one to take its name), scan / load
 * or generate, the scanner's copy button and clear.
 */
// MIGRATION(26.x): GuiGraphics#blit and screen rendering change with deferred GUI rendering (1.21.6).
public class DungeonToolScreen extends AbstractContainerScreen<DungeonToolMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/dungeon_scanner_gui.png");
    private static final int TEXT_COLOR = 0x404040;
    private static final int LIST_X = 75;
    private static final int LIST_Y = 18;
    private static final int LIST_WIDTH = 93;
    private static final int LIST_LINE = 8;
    private static final int LIST_LINES = 17;

    private final boolean scanner;
    private EditBox name;

    public DungeonToolScreen(DungeonToolMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.scanner = menu.tool() instanceof DungeonScannerBlockEntity;
    }

    @Override
    protected void init() {
        super.init();
        name = addRenderableWidget(new EditBox(font, leftPos + 5, topPos + 68, 64, 12, Component.translatable("techguns.debug.dungeon.template")));
        name.setMaxLength(DebugPayloads.MAX_NAME);
        button("G", DungeonToolBlockEntity.BUTTON_GHOST, 5, 5, 12);
        button("C", scanner ? DungeonScannerBlockEntity.BUTTON_CLEAR : DungeonGeneratorBlockEntity.BUTTON_CLEAR, 5, 149, 12);
        if (scanner) {
            button("-", DungeonScannerBlockEntity.BUTTON_SIZE_XZ_MINUS, 5, 27, 12);
            button("+", DungeonScannerBlockEntity.BUTTON_SIZE_XZ_PLUS, 18, 27, 12);
            button("-", DungeonScannerBlockEntity.BUTTON_SIZE_Y_MINUS, 5, 50, 12);
            button("+", DungeonScannerBlockEntity.BUTTON_SIZE_Y_PLUS, 18, 50, 12);
            templateButton("techguns.debug.dungeon.scan", DungeonScannerBlockEntity.ACTION_SCAN, 5, 31);
            templateButton("techguns.debug.dungeon.load", DungeonScannerBlockEntity.ACTION_LOAD, 38, 31);
            addRenderableWidget(Button.builder(Component.translatable("techguns.debug.dungeon.copy"),
                    b -> press(DungeonScannerBlockEntity.BUTTON_COPY_FIRST)).bounds(leftPos + 5, topPos + 97, 64, 12).build());
        } else {
            templateButton("techguns.debug.dungeon.generate", DungeonGeneratorBlockEntity.ACTION_GENERATE, 5, 64);
        }
    }

    private void button(String label, int id, int x, int y, int size) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> press(id)).bounds(leftPos + x, topPos + y, size, size).build());
    }

    private void templateButton(String key, int action, int x, int width) {
        addRenderableWidget(Button.builder(Component.translatable(key),
                b -> PacketDistributor.sendToServer(new DebugPayloads.TemplateAction(menu.containerId, action, name.getValue())))
                .bounds(leftPos + x, topPos + 81, width, 12).build());
    }

    private void press(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private List<String> templates() {
        return menu.templates().subList(0, Math.min(LIST_LINES, menu.templates().size()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<String> templates = templates();
        int mx = (int) mouseX - leftPos - LIST_X;
        int my = (int) mouseY - topPos - LIST_Y;
        if (mx >= 0 && mx < LIST_WIDTH && my >= 0 && my / LIST_LINE < templates.size()) {
            name.setValue(templates.get(my / LIST_LINE));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** The name field takes the keys (the inventory key would close the screen). */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (name.isFocused() && keyCode != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            return name.keyPressed(keyCode, scanCode, modifiers) || name.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.fill(leftPos + LIST_X - 1, topPos + LIST_Y - 1, leftPos + LIST_X + LIST_WIDTH + 1, topPos + 158, 0xFF888888);
        int mx = mouseX - leftPos - LIST_X;
        int my = mouseY - topPos - LIST_Y;
        if (mx >= 0 && mx < LIST_WIDTH && my >= 0 && my / LIST_LINE < templates().size()) {
            int y = topPos + LIST_Y + my / LIST_LINE * LIST_LINE;
            graphics.fill(leftPos + LIST_X, y, leftPos + LIST_X + LIST_WIDTH, y + LIST_LINE, 0xFFA0A0A0);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 20, 7, TEXT_COLOR, false);
        if (scanner) {
            graphics.drawString(font, Component.translatable("techguns.debug.dungeon.size_xz", menu.data(DungeonScannerBlockEntity.DATA_SIZE_XZ)), 5, 19,
                    TEXT_COLOR, false);
            graphics.drawString(font, Component.translatable("techguns.debug.dungeon.size_y", menu.data(DungeonScannerBlockEntity.DATA_SIZE_Y)), 5, 41,
                    TEXT_COLOR, false);
        }
        List<String> templates = templates();
        for (int i = 0; i < templates.size(); i++) {
            graphics.drawString(font, templates.get(i), LIST_X, LIST_Y + i * LIST_LINE, TEXT_COLOR, false);
        }
    }
}
