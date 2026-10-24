package techguns.core.gun;

/**
 * Gun and projectile math (ported from Techguns). Pure functions, no Minecraft types.
 */
public final class GunMath {
    private GunMath() {
    }

    /**
     * Damage of a projectile after travelling {@code distance} blocks. Full damage up to {@code dropStart},
     * linear falloff to {@code damageMin} at {@code dropEnd}, {@code damageMin} beyond.
     */
    public static float damageAtDistance(float damage, float damageMin, float dropStart, float dropEnd, double distance) {
        if (dropEnd <= 0.0f || distance <= dropStart) {
            return damage;
        }
        if (distance >= dropEnd) {
            return damageMin;
        }
        float factor = 1.0f - (float) ((distance - dropStart) / (dropEnd - dropStart));
        return damageMin + (damage - damageMin) * factor;
    }

    /** Projectile lifetime in ticks for a range given in blocks ("ticks to live" at speed 1). */
    public static int lifetimeTicks(float range, float speed) {
        return (int) Math.ceil(range / speed);
    }

    /**
     * How many shots one ammo item (bullet) is worth when unloading a gun.
     *
     * @param clipSize      shots per full reload
     * @param ammoPerReload ammo items consumed per reload
     * @param bulletsPerMag bullets that fill one magazine (0 = not a magazine)
     */
    public static float shotsPerBullet(int clipSize, int ammoPerReload, int bulletsPerMag) {
        // Techguns 2 inverted the ratio for multi-item reloads (returned ammoPerReload); fixed here.
        float bullets = bulletsPerMag == 0 ? ammoPerReload : bulletsPerMag;
        return clipSize / bullets;
    }
}
