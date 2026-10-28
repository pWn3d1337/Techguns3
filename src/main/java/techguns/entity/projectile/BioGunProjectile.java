package techguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import techguns.block.BioBlobBlock;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.gun.ChargedProjectileFactory;
import techguns.gun.ShotParams;
import techguns.network.FxPayloads;
import techguns.network.TGNetwork;
import techguns.registry.TGBlocks;
import techguns.registry.TGEntities;
import techguns.registry.TGSounds;

/**
 * Bio gun glob (Fabric {@code BioGunProjectile}): poisons what it hits and leaves a {@link BioBlobBlock} on the hit
 * block when the gun does block damage; shooting an existing blob makes it grow and finally burst. The size of the
 * glob and of the blob it leaves depends on the ammo consumed by the shot ({@link #blobLevel()}, 1..3).
 */
public class BioGunProjectile extends GenericProjectile {
    /** Poison duration (ticks) and amplifier of a hit. */
    private static final int POISON_TICKS = 100;
    private static final int POISON_AMPLIFIER = 3;

    /** Ammo consumed by the shot (Fabric {@code level}). */
    protected int blobLevel = 1;

    public BioGunProjectile(EntityType<? extends BioGunProjectile> type, Level level) {
        super(type, level);
    }

    protected BioGunProjectile(ShotParams params, int blobLevel) {
        super(TGEntities.BIOGUN_PROJECTILE.get(), params);
        this.blobLevel = blobLevel;
    }

    public int blobLevel() {
        return blobLevel;
    }

    @Override
    protected void onHitLiving(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, POISON_AMPLIFIER, false, true));
    }

    @Override
    protected TGDamageSource createDamageSource() {
        // Fabric took the knockback off this damage but pushed with a separate hit, so it has the normal knockback
        return TGDamage.poison(level(), this, getOwner(), DeathType.BIO).setGoreChance(1.0f).setArmorPenetration(penetration);
    }

    @Override
    protected void impactEffects(ServerLevel level, BlockHitResult result) {
        Vec3 pos = result.getLocation();
        float pitch = 0.0f;
        float yaw = 0.0f;
        Direction face = result.getDirection();
        if (face == Direction.UP) {
            pitch = -90.0f;
        } else if (face == Direction.DOWN) {
            pitch = 90.0f;
        } else {
            yaw = face.toYRot();
        }
        level.playSound(null, pos.x, pos.y, pos.z, TGSounds.BIOGUN_IMPACT.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        TGNetwork.sendFxNear(level, pos.x, pos.y, pos.z, IMPACT_FX_RADIUS,
                new FxPayloads.SpawnFx("biogunImpact", pos.x, pos.y, pos.z, 0, 0, 0, pitch, yaw, 1.0f));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level() instanceof ServerLevel server) {
            BlockPos hitPos = result.getBlockPos();
            BlockState hitState = server.getBlockState(hitPos);
            if (hitState.is(TGBlocks.BIOBLOB.get())) {
                hitBlob(server, hitPos, hitState);
            } else {
                BlockPos blobPos = hitPos.relative(result.getDirection());
                BlockState state = server.getBlockState(blobPos);
                if (state.is(TGBlocks.BIOBLOB.get())) {
                    hitBlob(server, blobPos, state);
                } else if (blockDamage && state.canBeReplaced()) {
                    BlockState blob = TGBlocks.BIOBLOB.get().defaultBlockState()
                            .setValue(BioBlobBlock.FACING, result.getDirection().getOpposite())
                            .setValue(BioBlobBlock.SIZE, Mth.clamp(blobLevel - 1, 0, 2));
                    server.setBlockAndUpdate(blobPos, blob);
                    TGBlocks.BIOBLOB.get().scheduleShrink(server, blobPos, server.random);
                }
            }
        }
        super.onHitBlock(result);
    }

    private void hitBlob(ServerLevel server, BlockPos pos, BlockState state) {
        TGBlocks.BIOBLOB.get().hitByBioShot(server, pos, state, blobLevel, getOwner());
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved() || !isInWater()) {
            return;
        }
        Vec3 motion = getDeltaMovement();
        if (level().isClientSide) {
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.BUBBLE, getX() + motion.x * 0.75, getY() + motion.y * 0.75, getZ() + motion.z * 0.75,
                        motion.x, motion.y, motion.z);
            }
        }
        if (isUnderWater()) {
            removeAfterHit();
        }
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        super.writeSpawnData(buf);
        buf.writeByte(blobLevel);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        super.readSpawnData(buf);
        blobLevel = buf.readByte();
        startTrail("BioGunTrail");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("level", (byte) blobLevel);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        blobLevel = tag.getByte("level");
    }

    public record Factory() implements ChargedProjectileFactory<GenericProjectile> {
        @Override
        public GenericProjectile createCharged(ShotParams params, float charge, int ammoConsumed) {
            return new BioGunProjectile(params, ammoConsumed);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return TGDamageCategory.POISON;
        }
    }
}
