package techguns.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import techguns.machine.AmmoPressMenu;
import techguns.machine.BlastFurnaceMenu;
import techguns.machine.ChargingStationMenu;
import techguns.machine.ChemLabMenu;
import techguns.machine.GrinderMenu;
import techguns.machine.AmmoBenchMenu;
import techguns.machine.CamoBenchMenu;
import techguns.machine.FabricatorMenu;
import techguns.machine.ReactionChamberMenu;
import techguns.machine.MetalPressMenu;
import techguns.machine.RepairBenchMenu;
import techguns.machine.UpgradeBenchMenu;
import techguns.slot.menu.ExtraSlotsMenu;

/** Container menu types. */
public final class TGMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Techguns.MODID);

    // MIGRATION(26.x): MenuType constructor / registration may change in 1.21.2+.
    public static final DeferredHolder<MenuType<?>, MenuType<ExtraSlotsMenu>> EXTRA_SLOTS = MENUS.register("extra_slots",
            () -> new MenuType<>(ExtraSlotsMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<MenuType<?>, MenuType<MetalPressMenu>> METAL_PRESS = MENUS.register("metal_press",
            () -> IMenuTypeExtension.create(MetalPressMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AmmoPressMenu>> AMMO_PRESS = MENUS.register("ammo_press",
            () -> IMenuTypeExtension.create(AmmoPressMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<BlastFurnaceMenu>> BLAST_FURNACE = MENUS.register("blast_furnace",
            () -> IMenuTypeExtension.create(BlastFurnaceMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<GrinderMenu>> GRINDER = MENUS.register("grinder",
            () -> IMenuTypeExtension.create(GrinderMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ChemLabMenu>> CHEM_LAB = MENUS.register("chem_lab",
            () -> IMenuTypeExtension.create(ChemLabMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ChargingStationMenu>> CHARGING_STATION = MENUS.register("charging_station",
            () -> IMenuTypeExtension.create(ChargingStationMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RepairBenchMenu>> REPAIR_BENCH = MENUS.register("repair_bench",
            () -> IMenuTypeExtension.create(RepairBenchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<CamoBenchMenu>> CAMO_BENCH = MENUS.register("camo_bench",
            () -> IMenuTypeExtension.create(CamoBenchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<UpgradeBenchMenu>> UPGRADE_BENCH = MENUS.register("upgrade_bench",
            () -> IMenuTypeExtension.create(UpgradeBenchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AmmoBenchMenu>> AMMO_BENCH = MENUS.register("ammo_bench",
            () -> new MenuType<>(AmmoBenchMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<MenuType<?>, MenuType<FabricatorMenu>> FABRICATOR = MENUS.register("fabricator",
            () -> IMenuTypeExtension.create(FabricatorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ReactionChamberMenu>> REACTION_CHAMBER = MENUS.register("reaction_chamber",
            () -> IMenuTypeExtension.create(ReactionChamberMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<techguns.machine.OreDrillMenu>> ORE_DRILL = MENUS.register("ore_drill",
            () -> IMenuTypeExtension.create(techguns.machine.OreDrillMenu::new));

    private TGMenus() {
    }
}
