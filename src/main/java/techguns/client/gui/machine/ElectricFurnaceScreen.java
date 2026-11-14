package techguns.client.gui.machine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.ElectricFurnaceMenu;

/** Electric Furnace screen: the progress arrow between input and output (sprite 24x17 at u 176, v 14). */
public class ElectricFurnaceScreen extends TGMachineScreen<ElectricFurnaceMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/electric_furnace_gui.png");
    public static final int ARROW_X = 79;
    public static final int ARROW_Y = 35;

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        if (menu.totalTime() > 0) {
            graphics.blit(TEXTURE, x + ARROW_X, y + ARROW_Y, 176, 14, progressScaled(24) + 1, 17);
        }
    }
}
