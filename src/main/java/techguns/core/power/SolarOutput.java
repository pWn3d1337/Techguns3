package techguns.core.power;

/**
 * What a solar panel makes per tick: the full rate in sunlight, an eighth of it in moonlight, nothing without a view of
 * the sky or in a dimension without a sky. Fractions add up over ticks ({@link #produce}).
 */
public final class SolarOutput {
    /** Moonlight gives this fraction of the daylight rate. */
    public static final double NIGHT_FACTOR = 1.0 / 8.0;

    private SolarOutput() {
    }

    /** FE per tick (may be fractional) of one panel. */
    public static double perTick(int dayRate, boolean skyVisible, boolean dimensionHasSky, boolean day) {
        if (!skyVisible || !dimensionHasSky || dayRate <= 0) {
            return 0.0;
        }
        return day ? dayRate : dayRate * NIGHT_FACTOR;
    }

    /**
     * Adds {@code perTick} to the carried fraction {@code remainder[0]} and returns the whole FE to produce now; the
     * rest stays in {@code remainder[0]}.
     */
    public static int produce(double perTick, double[] remainder) {
        double total = remainder[0] + perTick;
        int whole = (int) Math.floor(total);
        remainder[0] = total - whole;
        return whole;
    }
}
