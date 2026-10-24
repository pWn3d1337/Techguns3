package techguns.core.damage;

import java.util.Locale;

/**
 * Techguns damage categories. Every damage source maps to exactly one category (see {@code TGDamage#categoryOf}),
 * which decides how much armor applies against it.
 */
public enum TGDamageCategory {
    /** Melee hits. */
    PHYSICAL(1.0f),
    /** Arrows, bullets. */
    PROJECTILE(1.0f),
    /** Fire, flamethrower. */
    FIRE(0.5f),
    /** TNT, rockets. */
    EXPLOSION(0.5f),
    /** Energy weapons, lasers, magic. */
    ENERGY(0.5f),
    /** Gas, slime, wither. */
    POISON(0.0f),
    /** Never reduced by armor: starving, falling, suffocating, void. */
    UNRESISTABLE(0.0f),
    /** Anything cold related. */
    ICE(0.5f),
    /** Electricity, shock, lightning. */
    LIGHTNING(0.5f),
    RADIATION(0.0f),
    /** Dark energy, black holes, dark magic. */
    DARK(0.5f);

    /** Fraction of the regular armor value that applies against this category. */
    private final float defaultArmorFactor;

    TGDamageCategory(float defaultArmorFactor) {
        this.defaultArmorFactor = defaultArmorFactor;
    }

    /**
     * Fraction of regular armor that applies against this category. Fire-immune entities get double armor against fire.
     */
    public float armorFactor(boolean fireImmune) {
        if (this == FIRE && fireImmune) {
            return 2.0f;
        }
        return defaultArmorFactor;
    }

    /** Whether armor can reduce this category at all (also via bonus armor). */
    public boolean isResistable() {
        return this != UNRESISTABLE;
    }

    /** Lowercase name used in ids, tags and translation keys. */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
