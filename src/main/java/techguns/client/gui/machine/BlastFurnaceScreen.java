package techguns.client.gui.machine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.BlastFurnaceMenu;

/** Blast Furnace screen (Techguns 2 {@code BlastFurnaceGui}): a long progress bar under the inputs. */
public class BlastFurnaceScreen extends TGMachineScreen<BlastFurnaceMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/blast_furnace_gui.png");

    public BlastFurnaceScreen(BlastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        upgradeSlotX = 149;
        upgradeSlotY = 49;
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        if (menu.totalTime() > 0) {
            graphics.blit(TEXTURE, x + 19, y + 53, 0, 167, progressScaled(90) + 1, 11);
        }
    }
}
