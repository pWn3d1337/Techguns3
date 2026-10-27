package techguns;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Typed access to Techguns config values. Read values through these fields only.
 */
public final class TGConfig {
    private static final ModConfigSpec.Builder COMMON = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING = COMMON
            .comment("Enable additional debug logging for Techguns.")
            .define("debugLogging", false);

    static {
        COMMON.push("damage");
    }

    public static final ModConfigSpec.BooleanValue TG_ARMOR_SYSTEM = COMMON
            .comment("Use the Techguns armor system for all damage: armor depends on the damage category (projectile, energy, fire, ...)",
                    "and Techguns armor penetration applies. If false, vanilla armor calculation is used.")
            .define("tgArmorSystem", true);

    public static final ModConfigSpec.DoubleValue DAMAGE_PVP = COMMON
            .comment("Damage multiplier for Techguns damage dealt by players to players (only if PvP is enabled).")
            .defineInRange("damagePvP", 1.0, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue DAMAGE_FACTOR_NPC = COMMON
            .comment("Damage multiplier for Techguns damage dealt by non-players (NPCs, turrets).")
            .defineInRange("damageFactorNPC", 1.0, 0.0, 100.0);

    static {
        COMMON.pop();
    }

    static {
        COMMON.push("machines");
    }

    public static final ModConfigSpec.BooleanValue MACHINES_NEED_NO_POWER = COMMON
            .comment("Machines don't need power. Activate this if you don't want to install a mod with generators and still use the machines.")
            .define("machinesNeedNoPower", false);

    public static final ModConfigSpec.IntValue UPGRADE_XP_COST = COMMON
            .comment("Experience points per enchantment level an Upgrade Bench upgrade costs.")
            .defineInRange("upgradeXpCost", 20, 0, 10000);

    public static final ModConfigSpec.DoubleValue EXPLOSIVE_CHARGE_MAX_HARDNESS = COMMON
            .comment("Highest block hardness normal explosive charges can break (obsidian is 50).")
            .defineInRange("explosiveChargeMaxHardness", 30.0, 0.0, Double.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue EXPLOSIVE_CHARGE_ADVANCED_MAX_HARDNESS = COMMON
            .comment("Highest block hardness advanced explosive charges can break (obsidian is 50).")
            .defineInRange("explosiveChargeAdvancedMaxHardness", 100.0, 0.0, Double.MAX_VALUE);

    static {
        COMMON.pop();
        COMMON.push("ore_drills");
    }

    public static final ModConfigSpec.DoubleValue ORE_DRILL_MULTIPLIER_ORES = COMMON
            .comment("Multiplier to the default rate of how many ores an ore drill produces.")
            .defineInRange("oreDrillMultiplierOre", 1.0, 0.001, 1000.0);
    public static final ModConfigSpec.DoubleValue ORE_DRILL_MULTIPLIER_POWER = COMMON
            .comment("Multiplier to the default rate of how much power an ore drill requires.")
            .defineInRange("oreDrillMultiplierPower", 1.0, 0.0, 1000.0);
    public static final ModConfigSpec.DoubleValue ORE_DRILL_MULTIPLIER_FUEL = COMMON
            .comment("FE per furnace burn time tick for ore drills: burn time * this value = FE. Only used inside the ore drill.")
            .defineInRange("oreDrillFuelMultiplier", 1000.0, 1.0, 100000.0);
    public static final ModConfigSpec.DoubleValue ORE_DRILL_FUEL_VALUE_FUEL = COMMON
            .comment("Fuel value of liquid fuels (fluid tag techguns:drill_fuel) in ore drills, per millibucket.")
            .defineInRange("oreDrillFuelValueFuel", 100.0, 1.0, 100000.0);

    static {
        COMMON.pop();
    }

    static final ModConfigSpec COMMON_SPEC = COMMON.build();

    private TGConfig() {
    }
}
