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

    static final ModConfigSpec COMMON_SPEC = COMMON.build();

    private TGConfig() {
    }
}
