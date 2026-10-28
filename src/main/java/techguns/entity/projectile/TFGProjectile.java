package techguns.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.damage.TGExplosion;
import techguns.gun.ChargedProjectileFactory;
import techguns.gun.ShotParams;
import techguns.network.FxPayloads;
import techguns.network.TGNetwork;
import techguns.registry.TGEntities;
import techguns.registry.TGSounds;

/**
 * TFG plasma ball (Fabric {@code TFGProjectile}): explodes on any hit; the blast size depends on the charge. Invisible,
 * only its particle trail is drawn.
 */
public class TFGProjectile extends GenericProjectile {
    /** Radius (blocks) within which players see the explosion FX. */
    private static final double EXPLOSION_FX_RADIUS = 100.0;
    /** Trail and explosion FX are drawn smaller than the blast radius. */
    private static final float FX_SCALE = 0.75f;
    /** Above this size the blast also plays the echo. */
    private static final float ECHO_SIZE = 3.0f;

    /** Blast size: 1 uncharged, up to 4 charged, 5 at full charge. */
    protected float size = 1.0f;

    public TFGProjectile(EntityType<? extends TFGProjectile> type, Level level) {
        super(type, level);
    }

    protected TFGProjectile(ShotParams params, float size) {
        super(TGEntities.TFG_PROJECTILE.get(), params);
        this.size = size;
    }

    public float size() {
        return size;
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
            float damageMax = damage * 0.66f * size;
            float damageMin = damage * 0.33f * size;
            TGNetwork.sendFxNear(server, getX(), getY(), getZ(), EXPLOSION_FX_RADIUS,
                    new FxPayloads.SpawnFx("TFGExplosion", getX(), getY(), getZ(), 0, 0, 0, 0f, 0f, size * FX_SCALE));

            TGExplosion explosion = new TGExplosion(server, getOwner(), this, position(), damageMax, damageMin, size, size * 2.0f,
                    blockDamage ? 0.5 : 0.0);
            explosion.setBlockDropChance(0.1f);
            explosion.explode(createDamageSource(), false);

            server.playSound(null, getX(), getY(), getZ(), TGSounds.TFG_EXPLOSION.get(), SoundSource.BLOCKS, 5.0f, 1.0f);
            if (size > ECHO_SIZE) {
                server.playSound(null, getX(), getY(), getZ(), TGSounds.TFG_EXPLOSION_ECHO.get(), SoundSource.BLOCKS, 10.0f, 1.0f);
            }
        }
        removeAfterHit();
    }

    @Override
    protected TGDamageSource createDamageSource() {
        // Fabric TFGProjectile: the explosion knocks back three times as far
        return TGDamage.energy(level(), this, getOwner(), DeathType.GORE).setGoreChance(1.0f).setArmorPenetration(penetration)
                .setKnockbackMultiplier(3.0f);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        super.writeSpawnData(buf);
        buf.writeFloat(size);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        super.readSpawnData(buf);
        size = buf.readFloat();
        startTrail("TFGTrail", size * FX_SCALE);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("size", size);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        size = tag.getFloat("size");
    }

    public record Factory() implements ChargedProjectileFactory<GenericProjectile> {
        @Override
        public GenericProjectile createCharged(ShotParams params, float charge, int ammoConsumed) {
            float size = 1.0f + charge * 3.0f;
            if (charge >= 1.0f) {
                size += 1.0f;
            }
            return new TFGProjectile(params, size);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return TGDamageCategory.ENERGY;
        }
    }
}
