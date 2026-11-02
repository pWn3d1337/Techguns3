package techguns.core.damage;

/**
 * Decides whether a kill plays a special death effect (Techguns 2 {@code TGEventHandler.onLivingDeathEvent} and
 * {@code EntityDeathUtils.hasSpecialDeathAnim}).
 */
public final class DeathEffectRules {
    private DeathEffectRules() {
    }

    /**
     * @param deathType  death type of the killing damage source
     * @param bleeds     whether the dying entity can be gored (the {@code techguns:gore} entity tag); bio and laser deaths
     *                   play on every entity
     * @param goreChance chance (0..1) of the damage source to play its death effect
     * @param roll       uniform random value in [0, 1)
     */
    public static boolean playsDeathEffect(DeathType deathType, boolean bleeds, float goreChance, float roll) {
        return deathType != DeathType.DEFAULT && (deathType != DeathType.GORE || bleeds) && roll < goreChance;
    }
}
