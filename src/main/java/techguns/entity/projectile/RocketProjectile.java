package techguns.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.damage.TGExplosion;
import techguns.gun.ProjectileFactory;
import techguns.gun.ShotParams;
import techguns.gun.ammo.DamageModifier;
import techguns.network.FxPayloads;
import techguns.network.TGNetwork;
import techguns.registry.TGEntities;
import techguns.registry.TGSounds;

/**
 * Rocket: flies with slight gravity and explodes on impact (Techguns 2 {@code RocketProjectile} and its nuke and
 * high velocity subclasses). Damage falls off from full damage within {@code dropStart} blocks to {@code damageMin} at
 * {@code dropEnd} blocks, see {@link TGExplosion}.
 */
public class RocketProjectile extends GenericProjectile {

    public enum Variant {
        ROCKET("RocketExplosion", 50.0, "RocketLauncherExhaust", 0.5, DeathType.GORE),
        /** Huge blast, laser (vaporize) deaths; radiation follows with the radiation system. */
        NUKE("NukeExplosion", 150.0, "RocketLauncherExhaust", 0.5, DeathType.LASER),
        /** Faster, smaller blast (the speed and radius come from its ammo's damage modifier). */
        HIGH_VELOCITY("RocketExplosion", 50.0, "RocketLauncherExhaust", 0.5, DeathType.GORE),
        GUIDED("GuidedMissileExplosion", 50.0, "GuidedMissileExhaust", 0.25, DeathType.GORE);

        public final String explosionFx;
        /** Radius (blocks) within which players see the explosion FX. */
        public final double explosionFxRadius;
        public final String trailFx;
        /** {@link TGExplosion} block damage factor when the gun does block damage. */
        public final double blockDamageFactor;
        public final DeathType deathType;

        Variant(String explosionFx, double explosionFxRadius, String trailFx, double blockDamageFactor, DeathType deathType) {
            this.explosionFx = explosionFx;
            this.explosionFxRadius = explosionFxRadius;
            this.trailFx = trailFx;
            this.blockDamageFactor = blockDamageFactor;
            this.deathType = deathType;
        }
    }

    protected Variant variant = Variant.ROCKET;

    public RocketProjectile(EntityType<? extends RocketProjectile> type, Level level) {
        super(type, level);
    }

    protected RocketProjectile(EntityType<? extends RocketProjectile> type, ShotParams params, Variant variant) {
        super(type, params);
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        explode();
    }

    protected void explode() {
        if (level() instanceof ServerLevel server) {
            TGNetwork.sendFxNear(server, getX(), getY(), getZ(), variant.explosionFxRadius,
                    new FxPayloads.SpawnFx(variant.explosionFx, getX(), getY(), getZ()));
            TGExplosion explosion = new TGExplosion(server, getOwner(), this, position(), damage, damageMin, dropStart, dropEnd,
                    blockDamage ? variant.blockDamageFactor : 0.0);
            if (variant == Variant.NUKE) {
                explosion.setBlockDropChance(0.05f);
                explosion.explode(createDamageSource(), false);
                server.playSound(null, getX(), getY(), getZ(), TGSounds.NUKE_EXPLOSION.get(), SoundSource.BLOCKS, 4.0f, 1.0f);
                if (techguns.TGConfig.RADIATION_ENABLED.get()) {
                    server.addFreshEntity(techguns.radiation.RadiationArea.nuke(server, getX(), getY(), getZ(), dropStart, dropEnd));
                }
            } else {
                explosion.explode(createDamageSource(), true);
            }
        }
        removeAfterHit();
    }

    @Override
    protected TGDamageSource createDamageSource() {
        // Techguns 2 also tripled the knockback of nukes; that follows with the knockback multiplier
        return TGDamage.explosion(level(), this, getOwner(), variant.deathType).setGoreChance(1.0f).setArmorPenetration(penetration);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        super.writeSpawnData(buf);
        buf.writeByte(variant.ordinal());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        super.readSpawnData(buf);
        variant = Variant.values()[buf.readByte()];
        startTrail(variant.trailFx);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("variant", (byte) variant.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        variant = Variant.values()[Mth.clamp(tag.getByte("variant"), 0, Variant.values().length - 1)];
    }

    public record Factory(Variant variant, DamageModifier modifier) implements ProjectileFactory<RocketProjectile> {
        public Factory() {
            this(Variant.ROCKET, DamageModifier.NONE);
        }

        @Override
        public RocketProjectile create(ShotParams params) {
            return new RocketProjectile(TGEntities.ROCKET_PROJECTILE.get(), params, variant);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return TGDamageCategory.EXPLOSION;
        }
    }
}
