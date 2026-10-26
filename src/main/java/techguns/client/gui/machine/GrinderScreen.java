package techguns.client.gui.machine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.GrinderMenu;

/** Grinder screen (Techguns 2 {@code GrinderGui}): two progress bars closing in from both sides. */
public class GrinderScreen extends TGMachineScreen<GrinderMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/grinder_gui.png");

    public GrinderScreen(GrinderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        if (menu.totalTime() > 0) {
            int done = progressScaled(21);
            int rest = 20 - done;
            graphics.blit(TEXTURE, x + 31, y + 39, 0, 167, done + 1, 22);
            graphics.blit(TEXTURE, x + 31 + 20 + rest, y + 39, rest, 167, done + 1, 22);
        }
    }
}
