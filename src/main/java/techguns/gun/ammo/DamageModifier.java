package techguns.gun.ammo;

/**
 * How an ammo variant changes the gun's base stats: {@code value * multiplier + add}.
 */
public record DamageModifier(float damageMul, float damageAdd, float radiusMul, float radiusAdd,
                             float rangeMul, float rangeAdd, float velocityMul, float velocityAdd) {
    public static final DamageModifier NONE = new DamageModifier(1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f);
    public static final DamageModifier INCENDIARY_BULLET = NONE.withDamage(1.1f, 0f);
    public static final DamageModifier ROCKET_HIGH_VELOCITY = NONE.withRadius(0.75f, 0f).withRange(0.75f, 0f).withVelocity(2.0f, 0f);
    public static final DamageModifier ROCKET_NUKE = NONE.withDamage(5f, 0f).withRadius(5f, 0f).withRange(5f, 0f);

    public DamageModifier withDamage(float mul, float add) {
        return new DamageModifier(mul, add, radiusMul, radiusAdd, rangeMul, rangeAdd, velocityMul, velocityAdd);
    }

    public DamageModifier withRadius(float mul, float add) {
        return new DamageModifier(damageMul, damageAdd, mul, add, rangeMul, rangeAdd, velocityMul, velocityAdd);
    }

    public DamageModifier withRange(float mul, float add) {
        return new DamageModifier(damageMul, damageAdd, radiusMul, radiusAdd, mul, add, velocityMul, velocityAdd);
    }

    public DamageModifier withVelocity(float mul, float add) {
        return new DamageModifier(damageMul, damageAdd, radiusMul, radiusAdd, rangeMul, rangeAdd, mul, add);
    }

    public float damage(float damage) {
        return damage * damageMul + damageAdd;
    }

    public float radius(float radius) {
        return radius * radiusMul + radiusAdd;
    }

    public float range(float range) {
        return range * rangeMul + rangeAdd;
    }

    public float velocity(float velocity) {
        return velocity * velocityMul + velocityAdd;
    }
}
