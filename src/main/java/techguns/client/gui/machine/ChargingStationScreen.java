package techguns.client.gui.machine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.ChargingStationMenu;

/** Charging Station screen (Techguns 2 {@code ChargingStationGui}): the charge arrow of recipe jobs. */
public class ChargingStationScreen extends TGMachineScreen<ChargingStationMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/charging_station_gui.png");

    public ChargingStationScreen(ChargingStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        if (menu.totalTime() > 0) {
            graphics.blit(TEXTURE, x + 39, y + 19, 0, 167, progressScaled(26) + 1, 10);
        }
    }
}
