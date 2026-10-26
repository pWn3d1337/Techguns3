package techguns.client.gui.machine;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import techguns.registry.TGMenus;

/** Screens of the machine menus. */
public final class TGMachineScreens {
    private TGMachineScreens() {
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(TGMenus.METAL_PRESS.get(), MetalPressScreen::new);
        event.register(TGMenus.AMMO_PRESS.get(), AmmoPressScreen::new);
        event.register(TGMenus.BLAST_FURNACE.get(), BlastFurnaceScreen::new);
        event.register(TGMenus.GRINDER.get(), GrinderScreen::new);
        event.register(TGMenus.CHEM_LAB.get(), ChemLabScreen::new);
        event.register(TGMenus.CHARGING_STATION.get(), ChargingStationScreen::new);
    }
}
