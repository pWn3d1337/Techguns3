package techguns.client.gui.machine;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import techguns.registry.TGMenus;

/** Screens of the machine menus and the 3x3 door settings. */
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
        event.register(TGMenus.REPAIR_BENCH.get(), RepairBenchScreen::new);
        event.register(TGMenus.CAMO_BENCH.get(), CamoBenchScreen::new);
        event.register(TGMenus.UPGRADE_BENCH.get(), UpgradeBenchScreen::new);
        event.register(TGMenus.AMMO_BENCH.get(), AmmoBenchScreen::new);
        event.register(TGMenus.FABRICATOR.get(), FabricatorScreen::new);
        event.register(TGMenus.REACTION_CHAMBER.get(), ReactionChamberScreen::new);
        event.register(TGMenus.ORE_DRILL.get(), OreDrillScreen::new);
        event.register(TGMenus.EXPLOSIVE_CHARGE.get(), ExplosiveChargeScreen::new);
        event.register(TGMenus.TURRET.get(), TurretScreen::new);
        event.register(TGMenus.DOOR_3X3.get(), Door3x3Screen::new);
        event.register(TGMenus.DUNGEON_TOOL.get(), techguns.client.gui.debug.DungeonToolScreen::new);
    }
}
