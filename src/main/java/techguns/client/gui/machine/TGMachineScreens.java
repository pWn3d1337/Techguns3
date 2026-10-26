package techguns.client.gui.machine;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import techguns.registry.TGMenus;

/** Screens of the machine menus. */
public final class TGMachineScreens {
    private TGMachineScreens() {
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(TGMenus.METAL_PRESS.get(), MetalPressScreen::new);
    }
}
