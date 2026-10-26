package techguns.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.damage.TGExplosion;
import techguns.gun.ProjectileFactory;
import techguns.gun.ShotParams;
import techguns.network.FxPayloads;
import techguns.network.TGNetwork;
import techguns.registry.TGEntities;

/**
 * Bouncing grenade (Fabric / Techguns 2 {@code GrenadeProjectile}): bounces off blocks a few times, losing speed, and
 * explodes on the next block hit or when it hits an entity.
 */
public class GrenadeProjectile extends GenericProjectile {
    private static final double EXPLOSION_FX_RADIUS = 50.0;

    public enum Type {
        /** Grenade launcher round (Fabric values: 3 bounces, 75 % speed kept, light block damage). */
        LAUNCHER_40MM(3, 0.75f, 0.05, "GrenadeLauncherTrail"),
        /** Stick grenade (Techguns 2 values). */
        STIELGRANATE(3, 0.5f, 0.0, null),
        FRAG(2, 0.5f, 0.0, null);

        public final int bounces;
        public final float bounceSpeed;
        /** {@link TGExplosion} block damage factor when block damage is on. */
        public final double blockDamageFactor;
        public final String trailFx;

        Type(int bounces, float bounceSpeed, double blockDamageFactor, String trailFx) {
            this.bounces = bounces;
            this.bounceSpeed = bounceSpeed;
            this.blockDamageFactor = blockDamageFactor;
            this.trailFx = trailFx;
        }
    }

    private Type type = Type.LAUNCHER_40MM;
    private int bounces;

    public GrenadeProjectile(EntityType<? extends GrenadeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public GrenadeProjectile(ShotParams params, Type type) {
        super(TGEntities.GRENADE_PROJECTILE.get(), params);
        this.type = type;
        this.bounces = type.bounces;
    }

    public Type grenadeType() {
        return type;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (bounces <= 0) {
            explode();
            return;
        }
        Vec3 motion = getDeltaMovement();
        Vec3 reflected = switch (result.getDirection().getAxis()) {
            case X -> new Vec3(-motion.x, motion.y, motion.z);
            case Z -> new Vec3(motion.x, motion.y, -motion.z);
            case Y -> new Vec3(motion.x, -motion.y, motion.z);
        };
        // continue from the impact point; the base tick skips its own move after a hit
        setPos(result.getLocation());
        setDeltaMovement(reflected.scale(type.bounceSpeed));
        bounces--;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    protected void explode() {
        if (level() instanceof ServerLevel server) {
            TGNetwork.sendFxNear(server, getX(), getY(), getZ(), EXPLOSION_FX_RADIUS, new FxPayloads.SpawnFx("RocketExplosion", getX(), getY(), getZ()));
            new TGExplosion(server, getOwner(), this, position(), damage, damageMin, dropStart, dropEnd,
                    blockDamage ? type.blockDamageFactor : 0.0).explode(createDamageSource(), true);
        }
        removeAfterHit();
    }

    @Override
    protected TGDamageSource createDamageSource() {
        return TGDamage.explosion(level(), this, getOwner(), DeathType.GORE).setArmorPenetration(penetration);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        super.writeSpawnData(buf);
        buf.writeByte(type.ordinal());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        super.readSpawnData(buf);
        type = Type.values()[buf.readByte()];
        startTrail(type.trailFx);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("grenade_type", (byte) type.ordinal());
        tag.putByte("bounces", (byte) bounces);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        type = Type.values()[Mth.clamp(tag.getByte("grenade_type"), 0, Type.values().length - 1)];
        bounces = tag.getByte("bounces");
    }

    public record Factory(Type type) implements ProjectileFactory<GrenadeProjectile> {
        @Override
        public GrenadeProjectile create(ShotParams params) {
            return new GrenadeProjectile(params, type);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return TGDamageCategory.EXPLOSION;
        }
    }
}
