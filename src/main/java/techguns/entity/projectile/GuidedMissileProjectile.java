package techguns.entity.projectile;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import techguns.core.damage.TGDamageCategory;
import techguns.gun.ProjectileFactory;
import techguns.gun.ShotParams;
import techguns.gun.ammo.DamageModifier;
import techguns.registry.TGEntities;

/**
 * Guided missile (Techguns 2 {@code GuidedMissileProjectile}): a rocket that turns towards its locked-on target by at
 * most {@link #MAX_TURN_DEGREES} per tick; without a target it flies like a rocket with almost no gravity.
 */
public class GuidedMissileProjectile extends RocketProjectile {
    /** 9 degrees per tick = 180 degrees per second. */
    public static final double MAX_TURN_DEGREES = 9.0;
    /** Gravity of an unguided missile (Techguns 2 value). */
    private static final double UNGUIDED_GRAVITY = 0.01;

    @Nullable
    private Entity target;
    /** Target id from save data or spawn data, resolved on the next tick (the entity may not exist yet). */
    private int pendingTargetId = -1;

    public GuidedMissileProjectile(EntityType<? extends GuidedMissileProjectile> type, Level level) {
        super(type, level);
    }

    protected GuidedMissileProjectile(ShotParams params) {
        super(TGEntities.GUIDED_MISSILE.get(), params, Variant.GUIDED);
        this.target = params.target();
        this.gravity = target != null ? 0.0 : UNGUIDED_GRAVITY;
    }

    @Nullable
    public Entity target() {
        return target;
    }

    @Override
    public void tick() {
        if (pendingTargetId >= 0) {
            target = level().getEntity(pendingTargetId);
            pendingTargetId = -1;
        }
        if (target != null && !target.isAlive()) {
            target = null;
        }
        if (target != null && !dying) {
            steer();
        }
        super.tick();
    }

    private void steer() {
        Vec3 motion = getDeltaMovement();
        double speed = motion.length();
        if (speed < 1.0e-6) {
            return;
        }
        Vec3 current = motion.normalize();
        Vec3 wanted = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(position()).normalize();
        double angle = Math.acos(Math.max(-1.0, Math.min(1.0, current.dot(wanted))));
        double maxTurn = Math.toRadians(MAX_TURN_DEGREES);
        if (angle < maxTurn) {
            setDeltaMovement(wanted.scale(speed));
        } else {
            Vec3 axis = current.cross(wanted);
            if (axis.lengthSqr() < 1.0e-12) {
                return; // exactly opposite: any axis works, keep flying straight this tick
            }
            axis = axis.normalize();
            // Rodrigues rotation of current around axis by maxTurn (axis is perpendicular to current)
            Vec3 rotated = current.scale(Math.cos(maxTurn)).add(axis.cross(current).scale(Math.sin(maxTurn)));
            setDeltaMovement(rotated.normalize().scale(speed));
        }
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        super.writeSpawnData(buf);
        buf.writeVarInt(target != null ? target.getId() : -1);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        super.readSpawnData(buf);
        pendingTargetId = buf.readVarInt();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // the target is not saved (entity ids don't survive a reload); a reloaded missile flies on unguided
        variant = Variant.GUIDED;
    }

    public record Factory(DamageModifier modifier) implements ProjectileFactory<GuidedMissileProjectile> {
        public Factory() {
            this(DamageModifier.NONE);
        }

        @Override
        public GuidedMissileProjectile create(ShotParams params) {
            return new GuidedMissileProjectile(params);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return TGDamageCategory.EXPLOSION;
        }
    }
}
