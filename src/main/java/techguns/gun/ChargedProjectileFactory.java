package techguns.gun;

import net.minecraft.world.entity.projectile.Projectile;

/**
 * Projectile factory of charge guns (Techguns 2 {@code IChargedProjectileFactory}): a released charge creates a
 * projectile that scales with the charge; a normal (left click) shot is an uncharged one.
 */
public interface ChargedProjectileFactory<T extends Projectile> extends ProjectileFactory<T> {
    /**
     * @param charge       0..1, how long the charge was held relative to the full charge time
     * @param ammoConsumed ammo used by the charge (at least 1)
     */
    T createCharged(ShotParams params, float charge, int ammoConsumed);

    @Override
    default T create(ShotParams params) {
        return createCharged(params, 0.0f, 1);
    }
}
