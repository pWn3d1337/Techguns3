package techguns.entity.projectile;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
 * Magic rifle bolt (Fabric-only weapon, {@code MagicRifleProjectile}): three bolt types that grow with the charge.
 * Arcane bolts explode on impact (not hurting the target hit directly), fire bolts explode and set things on fire,
 * lightning bolts call down lightning on the impact point and, when charged, keep striking around it for a while.
 */
public class MagicRifleProjectile extends GenericProjectile {
    /** Bullet type (Fabric {@code TYPE_DEFAULT / TYPE_FIRE / TYPE_LIGHTNING}). */
    public enum Type {
        ARCANE, FIRE, LIGHTNING
    }

    /** Radius (blocks) within which players see the explosion FX. */
    private static final double EXPLOSION_FX_RADIUS = 100.0;
    /** Lightning bolts only linger after impact above this charge. */
    private static final float LINGER_MIN_CHARGE = 0.25f;
    /** Lightning free-position search range (blocks) up and down. */
    private static final int FREE_POS_RANGE = 3;

    protected Type type = Type.ARCANE;
    /** 0..1, how long the shot was charged. */
    protected float chargeAmount;
    /** Lightning: ticks left of the strikes after the impact; the bolt itself stands still meanwhile. */
    protected int postImpactTicks;

    public MagicRifleProjectile(EntityType<? extends MagicRifleProjectile> entityType, Level level) {
        super(entityType, level);
    }

    protected MagicRifleProjectile(ShotParams params, Type type, float chargeAmount) {
        super(TGEntities.MAGIC_RIFLE_PROJECTILE.get(), params);
        this.type = type;
        this.chargeAmount = chargeAmount;
        // the type decides the impact effects (fire bolts use the incendiary bullet impact)
        setKind(type == Type.FIRE ? Kind.INCENDIARY : Kind.DEFAULT);
    }

    public Type type() {
        return type;
    }

    public float chargeAmount() {
        return chargeAmount;
    }

    @Override
    public void tick() {
        if (postImpactTicks > 0) {
            postImpactTicks--;
            postImpactTick();
            if (postImpactTicks <= 0) {
                discard();
            }
        } else {
            super.tick();
        }
    }

    // --- damage ---

    @Override
    protected TGDamageSource createDamageSource() {
        TGDamageSource source = switch (type) {
            case ARCANE -> TGDamage.energy(level(), this, getOwner(), DeathType.GORE);
            case FIRE -> TGDamage.fire(level(), this, getOwner(), DeathType.LASER);
            case LIGHTNING -> TGDamage.lightning(level(), this, getOwner(), DeathType.GORE);
        };
        return source.setArmorPenetration(penetration);
    }

    @Override
    protected void onHitLiving(LivingEntity target) {
        switch (type) {
            case ARCANE -> explodeArcane(target);
            case FIRE -> {
                // Fabric: 3 + (int) chargeAmount * 7, i.e. the cast binds to the charge, so only a full charge adds 7 seconds
                int fireTime = 3 + (int) chargeAmount * 7;
                if (!target.fireImmune()) {
                    target.igniteForSeconds(fireTime);
                }
                explodeFire();
            }
            case LIGHTNING -> {
                BlockPos pos = freeBlockPos(target.getX(), target.getY(), target.getZ());
                if (pos != null) {
                    createLightning(pos);
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        switch (type) {
            case ARCANE -> explodeArcane(null);
            case FIRE -> {
                if (blockDamage && level() instanceof ServerLevel server) {
                    burnBlock(server, result, 1.0);
                }
                explodeFire();
            }
            case LIGHTNING -> createLightning(result.getBlockPos());
        }
        super.onHitBlock(result);
    }

    // --- arcane ---

    private void explodeArcane(@Nullable Entity targetHit) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        float size = 0.5f + chargeAmount * 2.5f;
        TGNetwork.sendFxNear(server, getX(), getY(), getZ(), EXPLOSION_FX_RADIUS,
                new FxPayloads.SpawnFx("MagicRifleExplosion_Arcane", getX(), getY(), getZ(), 0, 0, 0, 0f, 0f, size));
        // no block damage, the target hit directly is excluded (Fabric TGExplosionIgnoreBlocks)
        new TGExplosion(server, getOwner(), this, position(), damage * 0.66f * size, damage * 0.33f * size, size, size * 2.0f, 0.0)
                .setExcludedEntity(targetHit)
                .explode(createDamageSource(), false);
        server.playSound(null, getX(), getY(), getZ(), TGSounds.TFG_EXPLOSION.get(), SoundSource.BLOCKS, 5.0f, 1.0f);
    }

    // --- fire ---

    private void explodeFire() {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        float size = 0.5f + chargeAmount * 1.5f;
        int fireTime = 3 + (int) chargeAmount * 7;
        String fx = chargeAmount > 0.8 ? "MagicRifleExplosion_Fire_Charged" : "MagicRifleExplosion_Fire";
        TGNetwork.sendFxNear(server, getX(), getY(), getZ(), EXPLOSION_FX_RADIUS,
                new FxPayloads.SpawnFx(fx, getX(), getY(), getZ(), 0, 0, 0, 0f, 0f, size));
        // Fabric also set blocks in the blast on fire (setIncendiary with blockDamage); without block damage in the blast the
        // fire is only lit at the impact point, see onHitBlock
        new TGExplosion(server, getOwner(), this, position(), damage * 0.5f * size, damage * 0.25f * size, size * 1.75f, size * 3.5f, 0.0)
                .setEntityFire(fireTime)
                .explode(createDamageSource(), false);
        server.playSound(null, getX(), getY(), getZ(), TGSounds.TFG_EXPLOSION.get(), SoundSource.BLOCKS, 5.0f, 1.0f);
    }

    // --- lightning ---

    /** A charged lightning bolt stays alive after the impact and keeps calling down lightning (Fabric {@code handleRemoval}). */
    @Override
    protected void removeAfterHit() {
        if (type == Type.LIGHTNING && !level().isClientSide && tickCount < lifetime && chargeAmount > LINGER_MIN_CHARGE
                && postImpactTicks <= 0) {
            postImpactTicks = 10 + (int) (chargeAmount * 30);
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        super.removeAfterHit();
    }

    private void postImpactTick() {
        if (!(level() instanceof ServerLevel)) {
            return;
        }
        // lightning every 8 ticks on the spot
        if (postImpactTicks % 8 == 0) {
            BlockPos pos = freeBlockPos(getX(), getY(), getZ());
            if (pos != null) {
                createLightning(pos);
            }
        }
        // and, the higher the charge, sometimes at a random point around it
        if (postImpactTicks % 4 == 0 && chargeAmount * 0.5f > random.nextFloat()) {
            double distance = random.nextFloat() * chargeAmount * 5.0f;
            double angle = random.nextFloat() * (float) (Math.PI * 2.0);
            BlockPos pos = freeBlockPos(getX() + Mth.cos((float) angle) * distance, getY(), getZ() + Mth.sin((float) angle) * distance);
            if (pos != null) {
                createLightning(pos);
            }
        }
    }

    private void createLightning(BlockPos pos) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt == null) {
            return;
        }
        bolt.moveTo(Vec3.atBottomCenterOf(pos.above()));
        bolt.setCause(getOwner() instanceof ServerPlayer player ? player : null);
        server.addFreshEntity(bolt);
        server.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.WEATHER, 5.0f, 1.0f);
    }

    /** Nearest block (this one, then 3 down, then 3 up) that is solid and has air or fire above it; null if none. */
    @Nullable
    private BlockPos freeBlockPos(double x, double y, double z) {
        BlockPos base = BlockPos.containing(x, y, z);
        if (isFree(base)) {
            return base;
        }
        for (int i = 1; i <= FREE_POS_RANGE; i++) {
            if (isFree(base.below(i))) {
                return base.below(i);
            }
        }
        for (int i = 1; i <= FREE_POS_RANGE; i++) {
            if (isFree(base.above(i))) {
                return base.above(i);
            }
        }
        return null;
    }

    private boolean isFree(BlockPos pos) {
        return !level().getBlockState(pos).isAir()
                && (level().getBlockState(pos.above()).isAir() || level().getBlockState(pos.above()).is(Blocks.FIRE));
    }

    // --- trail and persistence ---

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        super.writeSpawnData(buf);
        buf.writeByte(type.ordinal());
        buf.writeFloat(chargeAmount);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        super.readSpawnData(buf);
        type = Type.values()[Mth.clamp(buf.readByte(), 0, Type.values().length - 1)];
        chargeAmount = buf.readFloat();
        switch (type) {
            case ARCANE -> startTrail("MagicRifleProjectileTrail_Arcane", 0.5f + chargeAmount * 2.5f);
            case FIRE -> startTrail("MagicRifleProjectileTrail_Fire", 0.5f + chargeAmount * 1.5f);
            case LIGHTNING -> startTrail("MagicRifleProjectileTrail_Lightning", 0.5f + chargeAmount * 2.5f);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("type", (byte) type.ordinal());
        tag.putFloat("chargeAmount", chargeAmount);
        tag.putInt("post_impact_ticks", postImpactTicks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        type = Type.values()[Mth.clamp(tag.getByte("type"), 0, Type.values().length - 1)];
        chargeAmount = tag.getFloat("chargeAmount");
        postImpactTicks = tag.getInt("post_impact_ticks");
    }

    public record Factory(Type type) implements ChargedProjectileFactory<GenericProjectile> {
        @Override
        public GenericProjectile createCharged(ShotParams params, float charge, int ammoConsumed) {
            return new MagicRifleProjectile(params, type, charge);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return switch (type) {
                case ARCANE -> TGDamageCategory.ENERGY;
                case FIRE -> TGDamageCategory.FIRE;
                case LIGHTNING -> TGDamageCategory.LIGHTNING;
            };
        }
    }
}
