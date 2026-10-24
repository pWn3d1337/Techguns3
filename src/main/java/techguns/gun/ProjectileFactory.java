package techguns.gun;

import net.minecraft.world.entity.projectile.Projectile;
import techguns.core.damage.TGDamageCategory;
import techguns.gun.ammo.DamageModifier;

/**
 * Creates the projectile entity for one ammo variant of a gun.
 */
public interface ProjectileFactory<T extends Projectile> {
    T create(ShotParams params);

    /** Damage category of the projectile, shown in tooltips. */
    TGDamageCategory damageCategory();

    default DamageModifier modifier() {
        return DamageModifier.NONE;
    }
}
