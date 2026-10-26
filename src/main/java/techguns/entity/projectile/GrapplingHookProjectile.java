package techguns.entity.projectile;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import techguns.core.damage.TGDamageCategory;
import techguns.gun.ChargedProjectileFactory;
import techguns.gun.FirePos;
import techguns.gun.GrapplingHookItem;
import techguns.gun.GunItem;
import techguns.gun.ShotParams;
import techguns.registry.TGEntities;
import techguns.registry.TGSounds;

/**
 * Grappling hook (Fabric {@code GrapplingHookProjectile}): flies until it hits a block or an entity, then pulls the
 * shooter towards it (or, when the shooter sneaks, the hit entity towards the shooter) for as long as the shooter keeps
 * using the grappling hook. The logic runs on the server; the client only predicts the flight and reads the status.
 */
public class GrapplingHookProjectile extends GenericProjectile {
    /** Fabric {@code GrapplingStatus}. */
    public enum Status {
        NONE, LAUNCHING, GRAPPLING_BLOCK, GRAPPLING_ENTITY, PULL_ENTITY
    }

    private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(GrapplingHookProjectile.class, EntityDataSerializers.BYTE);

    /** Blocks per tick above {@link #PULL_MAX_SPEED_DISTANCE}. */
    private static final float PULL_SPEED = 2.0f;
    /** Pull at full speed above this distance, slower (proportional to the distance) below. */
    private static final float PULL_MAX_SPEED_DISTANCE = 5.0f;
    /** The pull ends at this distance. */
    private static final float PULL_TARGET_DISTANCE = 1.0f;
    private static final int MAX_PULL_TICKS = 100;

    private FirePos firePos = FirePos.CENTER;
    // server only
    private int pullTicks = MAX_PULL_TICKS;
    @Nullable
    private Entity targetEntity;
    @Nullable
    private BlockPos targetBlock;
    @Nullable
    private Vec3 targetPos;

    public GrapplingHookProjectile(EntityType<? extends GrapplingHookProjectile> type, Level level) {
        super(type, level);
    }

    protected GrapplingHookProjectile(ShotParams params) {
        super(TGEntities.GRAPPLING_HOOK_PROJECTILE.get(), params);
        this.firePos = params.firePos();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATUS, (byte) Status.LAUNCHING.ordinal());
    }

    public Status status() {
        return Status.values()[entityData.get(STATUS)];
    }

    private void setStatus(Status status) {
        entityData.set(STATUS, (byte) status.ordinal());
    }

    public FirePos firePos() {
        return firePos;
    }

    /** The shooter is still holding the use key of a grappling hook. */
    private boolean isShooterGrappling() {
        return getOwner() instanceof LivingEntity shooter && shooter.isAlive() && shooter.isUsingItem()
                && shooter.getUseItem().getItem() instanceof GrapplingHookItem;
    }

    private void stopGrappling() {
        setStatus(Status.NONE);
        targetBlock = null;
        targetEntity = null;
        targetPos = null;
    }

    @Override
    public void tick() {
        Status status = status();
        if (level().isClientSide) {
            if (status == Status.LAUNCHING) {
                super.tick();
            } else {
                // position follows the server updates
                baseTick();
            }
            return;
        }
        switch (status) {
            case LAUNCHING -> {
                if (isShooterGrappling()) {
                    super.tick();
                } else {
                    stopGrappling();
                }
            }
            case NONE -> discard();
            default -> {
                baseTick();
                grapple(status);
            }
        }
    }

    private void grapple(Status status) {
        if (!isShooterGrappling() || pullTicks-- <= 0) {
            stopGrappling();
            return;
        }
        Entity owner = getOwner();
        switch (status) {
            case GRAPPLING_ENTITY -> {
                if (targetEntity != null && targetEntity.isAlive()) {
                    Vec3 dst = targetEntity.getEyePosition();
                    pull(owner, dst);
                    setPos(dst);
                } else {
                    stopGrappling();
                }
            }
            case GRAPPLING_BLOCK -> {
                if (targetPos != null && targetBlock != null && !level().getBlockState(targetBlock).isAir()) {
                    pull(owner, targetPos);
                } else {
                    stopGrappling();
                }
            }
            case PULL_ENTITY -> {
                if (targetEntity != null && targetEntity.isAlive()) {
                    pull(targetEntity, owner.position());
                    setPos(targetEntity.getX(), targetEntity.getY() + targetEntity.getEyeHeight() * 0.5, targetEntity.getZ());
                } else {
                    stopGrappling();
                }
            }
            default -> {
            }
        }
    }

    /** Moves {@code entity} towards {@code dst}: full speed far away, slowing down when close, stopping at the target distance. */
    private void pull(Entity entity, Vec3 dst) {
        Vec3 diff = dst.subtract(entity.position());
        double dist = diff.length();
        if (dist <= PULL_TARGET_DISTANCE) {
            entity.setDeltaMovement(Vec3.ZERO);
            entity.hurtMarked = true;
            stopGrappling();
        } else {
            float speed = dist >= PULL_MAX_SPEED_DISTANCE ? PULL_SPEED : (float) (PULL_SPEED * dist / PULL_MAX_SPEED_DISTANCE);
            entity.setDeltaMovement(diff.normalize().scale(speed));
            entity.hurtMarked = true;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        stopMotion(result.getLocation());
        if (level().isClientSide) {
            return;
        }
        Entity target = result.getEntity();
        if (getOwner() instanceof LivingEntity owner) {
            owner.setLastHurtMob(target);
        }
        target.hurt(createDamageSource(), currentDamage());
        if (isShooterGrappling() && target.isAlive()) {
            targetEntity = target;
            setStatus(getOwner().isShiftKeyDown() ? Status.PULL_ENTITY : Status.GRAPPLING_ENTITY);
            playPullSound();
        } else {
            setStatus(Status.NONE);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        stopMotion(result.getLocation());
        if (level().isClientSide) {
            return;
        }
        if (!level().getBlockState(result.getBlockPos()).isAir() && !getOwner().isShiftKeyDown()) {
            targetBlock = result.getBlockPos();
            targetPos = result.getDirection() == Direction.UP ? result.getLocation().add(0, 0.15, 0) : result.getLocation();
            setStatus(Status.GRAPPLING_BLOCK);
            playPullSound();
        } else {
            setStatus(Status.NONE);
        }
    }

    /** The hook stays where it hit (or follows its target) instead of being removed. */
    private void stopMotion(Vec3 pos) {
        setPos(pos);
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void updateRotationFromMotion() {
        // a stuck hook keeps the direction it hit with
        if (getDeltaMovement().lengthSqr() > 1.0E-7) {
            super.updateRotationFromMotion();
        }
    }

    private void playPullSound() {
        Entity owner = getOwner();
        level().playSound(null, owner.getX(), owner.getEyeY(), owner.getZ(), TGSounds.GRAPPLING_HOOK_PULL.get(), SoundSource.PLAYERS,
                GunItem.SOUND_VOLUME, 1.0f);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        super.writeSpawnData(buf);
        buf.writeByte(firePos.ordinal());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        super.readSpawnData(buf);
        firePos = FirePos.values()[Math.max(0, Math.min(FirePos.values().length - 1, buf.readByte()))];
    }

    /** Creates the hook; the grappling hook item shoots one when the use key is pressed (a charged release does nothing). */
    public record Factory() implements ChargedProjectileFactory<GenericProjectile> {
        @Override
        public GenericProjectile createCharged(ShotParams params, float charge, int ammoConsumed) {
            return new GrapplingHookProjectile(params);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return TGDamageCategory.PROJECTILE;
        }
    }
}
