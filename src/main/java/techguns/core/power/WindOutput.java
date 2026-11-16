package techguns.core.power;

/**
 * What a wind turbine column makes: every rotor segment adds the base rate times its height factor; the biome and the
 * weather multiply the sum. Height counts from the sea level: nothing 24 blocks below it, half the rate at sea level,
 * the full rate 64 blocks above it, up to twice the rate 192 blocks above it.
 */
public final class WindOutput {
    /** Height factor at sea level. */
    public static final double SEA_LEVEL_FACTOR = 0.5;
    /** Blocks below sea level where the wind stops. */
    public static final double CALM_DEPTH = 24.0;
    /** Blocks above sea level per +1 height factor. */
    public static final double BLOCKS_PER_FACTOR = 128.0;
    public static final double MAX_HEIGHT_FACTOR = 2.0;
    /** Extra factor in full rain and in a full thunderstorm (they add up). */
    public static final double RAIN_BONUS = 0.25;
    public static final double THUNDER_BONUS = 0.25;
    /** Rotor turn in degrees per tick at wind factor 1, and the most it turns per tick. */
    public static final float DEGREES_PER_TICK = 6.0f;
    public static final float MAX_DEGREES_PER_TICK = 24.0f;

    private WindOutput() {
    }

    /** Height factor of a segment whose centre is {@code aboveSea} blocks above the sea level (negative below). */
    public static double heightFactor(double aboveSea) {
        if (aboveSea <= -CALM_DEPTH) {
            return 0.0;
        }
        if (aboveSea < 0.0) {
            return SEA_LEVEL_FACTOR * (aboveSea + CALM_DEPTH) / CALM_DEPTH;
        }
        return Math.min(MAX_HEIGHT_FACTOR, SEA_LEVEL_FACTOR + aboveSea / BLOCKS_PER_FACTOR);
    }

    /** Weather factor from the level's rain and thunder strength (0..1 each). */
    public static double weatherFactor(float rain, float thunder) {
        return 1.0 + RAIN_BONUS * clamp01(rain) + THUNDER_BONUS * clamp01(thunder);
    }

    /** FE per tick (may be fractional) of a column: {@code rate} per segment at factor 1. */
    public static double perTick(int rate, double heightFactorSum, double biome, double weather, boolean openSky) {
        if (!openSky || rate <= 0) {
            return 0.0;
        }
        return rate * heightFactorSum * biome * weather;
    }

    /** Rotor turn per tick for a column whose wind factor (height average x biome x weather) is {@code wind}. */
    public static float degreesPerTick(double wind) {
        return (float) Math.min(MAX_DEGREES_PER_TICK, Math.max(0.0, wind) * DEGREES_PER_TICK);
    }

    private static double clamp01(float value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
