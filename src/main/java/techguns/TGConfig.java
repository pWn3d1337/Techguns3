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

    static final ModConfigSpec COMMON_SPEC = COMMON.build();

    private TGConfig() {
    }
}
