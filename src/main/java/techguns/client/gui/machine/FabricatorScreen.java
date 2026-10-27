package techguns.client.gui.machine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.FabricatorMenu;

/** Fabricator screen (Techguns 2 {@code FabricatorGui}): the long progress bar under the input slots. */
public class FabricatorScreen extends TGMachineScreen<FabricatorMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/fabricator_gui.png");

    public FabricatorScreen(FabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        this.upgradeSlotX = 149;
        this.upgradeSlotY = 49;
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        if (menu.totalTime() > 0) {
            graphics.blit(TEXTURE, x + 21, y + 53, 0, 167, progressScaled(90) + 1, 11);
        }
    }
}
