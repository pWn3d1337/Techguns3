package techguns.core.damage;

/**
 * Techguns armor math (ported from Techguns 2 {@code DamageSystem}). Pure functions, no Minecraft types.
 */
public final class TGArmorFormula {
    /** Armor value at which damage would be fully absorbed; effective armor is capped below it. */
    public static final float FULL_ABSORB_ARMOR = 25.0f;
    public static final float MAX_EFFECTIVE_ARMOR = 24.0f;
    /** Penetration is given per armor piece in Techguns stats; a full set has four pieces. */
    public static final float PENETRATION_PER_PIECE_FACTOR = 4.0f;

    private TGArmorFormula() {
    }

    /**
     * Armor value that applies against a damage category.
     *
     * @param category    damage category of the hit
     * @param baseArmor   the entity's regular armor value
     * @param bonusArmor  extra armor against this category (Techguns armor attributes), may be negative
     * @param fireImmune  whether the entity is immune to fire
     */
    public static float armorAgainst(TGDamageCategory category, float baseArmor, float bonusArmor, boolean fireImmune) {
        if (!category.isResistable()) {
            return 0.0f;
        }
        return Math.max(0.0f, baseArmor * category.armorFactor(fireImmune) + bonusArmor);
    }

    /**
     * Damage left after armor, based on the old 1.7 formula. Toughness only counters penetration.
     *
     * @param damage      incoming damage
     * @param armor       armor against the hit's category, see {@link #armorAgainst}
     * @param toughness   the entity's armor toughness
     * @param penetration armor penetration of the hit (per armor piece, as in gun stats)
     */
    public static float damageAfterArmor(float damage, float armor, float toughness, float penetration) {
        float effectivePenetration = Math.max(penetration * PENETRATION_PER_PIECE_FACTOR - toughness, 0.0f);
        float effectiveArmor = Math.clamp(armor - effectivePenetration, 0.0f, MAX_EFFECTIVE_ARMOR);
        return damage * (1.0f - effectiveArmor / FULL_ABSORB_ARMOR);
    }
}
