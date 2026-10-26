package techguns.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.gun.ChargedProjectileFactory;
import techguns.gun.ShotParams;
import techguns.network.TGNetwork;
import techguns.registry.TGEntities;
import techguns.registry.TGSounds;

/**
 * Short-range projectile of the chainsaw, mining drill and power hammer (Fabric {@code ChainsawProjectile}): physical
 * damage that always gores, and the power hammer impact sound and FX on any hit.
 */
public class ChainsawProjectile extends GenericProjectile {
    /** Position of the entity hit, for the impact effects of {@link #onHitLiving}. */
    private Vec3 hitPos = Vec3.ZERO;

    public ChainsawProjectile(EntityType<? extends ChainsawProjectile> type, Level level) {
        super(type, level);
    }

    protected ChainsawProjectile(ShotParams params) {
        super(TGEntities.CHAINSAW_PROJECTILE.get(), params);
    }

    /** Fabric {@code PROJECTILE_TYPE_CHAINSAW / PROJECTILE_TYPE_POWERHAMMER}. */
    public enum Type {
        CHAINSAW, POWER_HAMMER
    }

    @Override
    protected TGDamageSource createDamageSource() {
        return TGDamage.physical(level(), this, getOwner(), DeathType.GORE).setGoreChance(1.0f).setArmorPenetration(penetration);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        hitPos = result.getLocation();
        super.onHitEntity(result);
    }

    @Override
    protected void onHitLiving(LivingEntity target) {
        if (level() instanceof ServerLevel server) {
            impact(server, hitPos);
        }
    }

    @Override
    protected void impactEffects(ServerLevel level, BlockHitResult result) {
        impact(level, result.getLocation());
    }

    private void impact(ServerLevel level, Vec3 pos) {
        level.playSound(null, pos.x, pos.y, pos.z, TGSounds.POWERHAMMER_IMPACT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        TGNetwork.sendFxNear(level, pos, IMPACT_FX_RADIUS, "PowerhammerImpact");
    }

    @Override
    protected double waterDrag() {
        // regular slowdown
        return 0.99;
    }

    /** The type only selects the gun's tooltip/model in Fabric; both behave the same. */
    public record Factory(Type type) implements ChargedProjectileFactory<GenericProjectile> {
        @Override
        public GenericProjectile createCharged(ShotParams params, float charge, int ammoConsumed) {
            return new ChainsawProjectile(params);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return TGDamageCategory.PHYSICAL;
        }
    }
}
