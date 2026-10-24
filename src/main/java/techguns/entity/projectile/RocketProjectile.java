package techguns.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGDamageCategory;
import techguns.core.gun.GunMath;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.gun.ProjectileFactory;
import techguns.gun.ShotParams;
import techguns.registry.TGEntities;

/**
 * Rocket: flies with slight gravity and explodes on impact. Damage falls off from full damage within
 * {@code dropStart} blocks to {@code damageMin} at {@code dropEnd} blocks.
 */
// TODO: port TGExplosion (block damage chance, line of sight, knockback multiplier) if the vanilla-based blast is not close enough.
public class RocketProjectile extends GenericProjectile {
    public RocketProjectile(EntityType<? extends RocketProjectile> type, Level level) {
        super(type, level);
    }

    protected RocketProjectile(ShotParams params) {
        super(TGEntities.ROCKET_PROJECTILE.get(), params);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        explode();
    }

    private void explode() {
        if (!level().isClientSide) {
            damageEntities();
            // vanilla blast for sound, particles and (outside safe mode) block damage; it never damages entities
            Level.ExplosionInteraction interaction = blockDamage ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
            level().explode(this, null, NO_ENTITY_DAMAGE, getX(), getY(), getZ(), Math.max(1.0f, dropStart), false, interaction);
        }
        removeAfterHit();
    }

    private void damageEntities() {
        float range = Math.max(dropStart, dropEnd);
        for (Entity target : level().getEntities(this, new AABB(position(), position()).inflate(range))) {
            if (!(target instanceof LivingEntity living) || !living.isAlive()) {
                continue;
            }
            double distance = target.getBoundingBox().getCenter().distanceTo(position());
            if (distance > range) {
                continue;
            }
            float dmg = GunMath.damageAtDistance(damage, damageMin, dropStart, dropEnd, distance);
            living.hurt(createDamageSource(), dmg);
        }
    }

    @Override
    protected TGDamageSource createDamageSource() {
        return TGDamage.explosion(level(), this, getOwner(), DeathType.GORE).setGoreChance(1.0f).setArmorPenetration(penetration);
    }

    private static final ExplosionDamageCalculator NO_ENTITY_DAMAGE = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return 1.0f;
        }
    };

    public record Factory() implements ProjectileFactory<RocketProjectile> {
        @Override
        public RocketProjectile create(ShotParams params) {
            return new RocketProjectile(params);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return TGDamageCategory.EXPLOSION;
        }
    }
}
