package techguns.gun;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Everything a projectile factory needs for one projectile, after ammo modifiers are applied.
 *
 * @param gun         the firing gun; null for thrown grenades
 * @param spread      inaccuracy for this projectile
 * @param blockDamage whether the projectile may break/ignite blocks (safe mode off)
 * @param lifetime    lifetime in ticks
 * @param target      locked-on target (guided missiles), or null
 */
public record ShotParams(@Nullable GunItem gun, Level level, LivingEntity shooter, float damage, float damageMin, float dropStart, float dropEnd,
                         float penetration, float speed, int lifetime, float spread, double gravity, float radius,
                         boolean blockDamage, FirePos firePos, @Nullable Entity target) {
}
