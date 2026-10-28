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

    /** Shots one ammo item loads in a multi-item reload (shotgun shells, 40 mm grenades); at least 1. */
    public static int shotsPerReloadItem(int clipSize, int ammoPerReload) {
        return Math.max(1, clipSize / Math.max(1, ammoPerReload));
    }

    /**
     * Ammo items a multi-item reload takes to fill the gun from {@code loaded} shots; at least 1 (the reload always
     * takes one). Shots and items are different units once a clip holds more shots than items (stat overrides).
     */
    public static int reloadItems(int clipSize, int ammoPerReload, int loaded) {
        int shotsPerItem = shotsPerReloadItem(clipSize, ammoPerReload);
        int missing = Math.max(0, clipSize - loaded);
        return Math.max(1, (missing + shotsPerItem - 1) / shotsPerItem);
    }
}
