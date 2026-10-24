package techguns.gun;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Everything a projectile factory needs for one projectile, after ammo modifiers are applied.
 *
 * @param spread      inaccuracy for this projectile
 * @param blockDamage whether the projectile may break/ignite blocks (safe mode off)
 * @param lifetime    lifetime in ticks
 */
public record ShotParams(GunItem gun, Level level, LivingEntity shooter, float damage, float damageMin, float dropStart, float dropEnd,
                         float penetration, float speed, int lifetime, float spread, double gravity, float radius,
                         boolean blockDamage, FirePos firePos) {
}
