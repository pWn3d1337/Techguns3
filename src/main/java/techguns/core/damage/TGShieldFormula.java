package techguns.core.damage;

/**
 * Shield math (ported from Techguns 2 {@code ShieldStats.getAmount}). Pure functions, no Minecraft types.
 */
public final class TGShieldFormula {
    private TGShieldFormula() {
    }

    /**
     * Damage that gets through a blocking shield.
     *
     * @param damage         incoming damage
     * @param blockRate      fraction the shield blocks against the hit's category (1 = all)
     * @param damageThreshold flat reduction after the rate; lowered by twice the hit's armor penetration
     * @param penetration    armor penetration of the hit
     */
    public static float damageThrough(float damage, double blockRate, double damageThreshold, float penetration) {
        double through = damage * (1.0 - blockRate);
        double threshold = Math.max(damageThreshold - penetration * 2.0, 0.0);
        return (float) Math.max(through - threshold, 0.0);
    }
}
