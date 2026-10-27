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
        COMMON.push("npc_spawn");
    }

    // Techguns 2 "NPC Spawn" category
    public static final ModConfigSpec.IntValue DISTANCE_SPAWN_LEVEL_0 = COMMON
            .comment("Up to which distance to the world spawn only NPCs with danger level up to 0 spawn (half that in the Nether).")
            .defineInRange("distanceSpawnLevel0", 500, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue DISTANCE_SPAWN_LEVEL_1 = COMMON
            .comment("Up to which distance to the world spawn only NPCs with danger level up to 1 spawn (half that in the Nether).")
            .defineInRange("distanceSpawnLevel1", 1000, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue DISTANCE_SPAWN_LEVEL_2 = COMMON
            .comment("Up to which distance to the world spawn only NPCs with danger level up to 2 spawn (half that in the Nether).")
            .defineInRange("distanceSpawnLevel2", 2500, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT_OVERWORLD = COMMON
            .comment("Spawn weight of Techguns NPCs among the monsters of overworld biomes; determines how many Techguns NPCs spawn. 0 disables them.")
            .defineInRange("spawnWeightOverworld", 600, 0, 10000);
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT_NETHER = COMMON
            .comment("Spawn weight of Techguns NPCs among the monsters of Nether biomes. 0 disables them.")
            .defineInRange("spawnWeightNether", 300, 0, 10000);
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT_ZOMBIE_SOLDIER = COMMON
            .comment("Spawn weight of zombie soldiers (danger level 1). 0 disables them.").defineInRange("spawnWeightZombieSoldier", 100, 0, 10000);
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT_ZOMBIE_FARMER = COMMON
            .comment("Spawn weight of zombie farmers (danger level 0). 0 disables them.").defineInRange("spawnWeightZombieFarmer", 200, 0, 10000);
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT_ZOMBIE_MINER = COMMON
            .comment("Spawn weight of zombie miners (danger level 0). 0 disables them.").defineInRange("spawnWeightZombieMiner", 200, 0, 10000);
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT_SKELETON_SOLDIER = COMMON
            .comment("Spawn weight of skeleton soldiers (danger level 1). 0 disables them.").defineInRange("spawnWeightSkeletonSoldier", 100, 0, 10000);
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT_PSYCHO_STEVE = COMMON
            .comment("Spawn weight of Psycho Steve (danger level 1), an early game boss; keep it low. 0 disables him.")
            .defineInRange("spawnWeightPsychoSteve", 3, 0, 10000);
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT_BANDIT = COMMON
            .comment("Spawn weight of bandits (danger level 2). 0 disables them.").defineInRange("spawnWeightBandit", 50, 0, 10000);
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT_ZOMBIE_PIGMAN_SOLDIER = COMMON
            .comment("Spawn weight of zombie pigman soldiers (Nether, danger level 0). 0 disables them.")
            .defineInRange("spawnWeightZombiePigmanSoldier", 100, 0, 10000);
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT_CYBER_DEMON = COMMON
            .comment("Spawn weight of cyber demons (Nether, danger level 0). 0 disables them.").defineInRange("spawnWeightCyberDemon", 30, 0, 10000);
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> SPAWN_BIOME_BLACKLIST = COMMON
            .comment("Biome ids (e.g. minecraft:mushroom_fields) where no Techguns NPCs spawn naturally.")
            .defineListAllowEmpty("biomeBlacklist", java.util.List.of(), () -> "", entry -> entry instanceof String);

    static {
        COMMON.pop();
        COMMON.push("worldgen");
    }

    // Techguns 2 "World Generation" category; read when the server starts
    public static final ModConfigSpec.BooleanValue ORE_GEN_TIN = COMMON
            .comment("Generate tin ore; disable if another mod does.").define("oreGenTin", true);
    public static final ModConfigSpec.BooleanValue ORE_GEN_LEAD = COMMON
            .comment("Generate lead ore; disable if another mod does.").define("oreGenLead", true);
    public static final ModConfigSpec.BooleanValue ORE_GEN_TITANIUM = COMMON
            .comment("Generate titanium ore; few other mods add it, leave it on in most cases.").define("oreGenTitanium", true);
    public static final ModConfigSpec.BooleanValue ORE_GEN_URANIUM = COMMON
            .comment("Generate uranium ore; disable if another mod already adds it and you want only one kind.").define("oreGenUranium", true);

    static {
        COMMON.pop();
    }

    static final ModConfigSpec COMMON_SPEC = COMMON.build();

    private TGConfig() {
    }
}
