package techguns.radiation;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import techguns.TGConfig;
import techguns.core.radiation.RadiationRules;
import techguns.registry.TGMobEffects;

/** Techguns 2 {@code TGRadiationSystem.applyRadToEntities}: radiation for everything living around a point. */
public final class RadiationSources {
    private RadiationSources() {
    }

    /**
     * Gives the radiation effect ({@code strength} is the effect amplifier) for {@code duration} ticks to living
     * entities within {@code radius}: full within {@code innerRadius}, falling to {@code outerStrength} at the edge.
     * Nothing while the radiation system is off (Techguns 2 checked it at every source).
     */
    public static void irradiate(Level level, Vec3 center, double radius, int duration, int strength, double innerRadius, int outerStrength) {
        if (level.isClientSide || !TGConfig.RADIATION_ENABLED.get()) {
            return;
        }
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius))) {
            int amplifier = RadiationRules.strengthAt(center.distanceTo(entity.position()), radius, innerRadius, strength, outerStrength);
            if (amplifier >= 0) {
                entity.addEffect(new MobEffectInstance(TGMobEffects.RADIATION, duration, amplifier, true, true));
            }
        }
    }

    /** Techguns 2 {@code NDRProjectile.onHitEffect}: radiation V for 2 s. */
    public static void ndrHit(LivingEntity target) {
        if (!target.level().isClientSide && TGConfig.RADIATION_ENABLED.get()) {
            target.addEffect(new MobEffectInstance(TGMobEffects.RADIATION, 40, 4, false, true));
        }
    }
}
