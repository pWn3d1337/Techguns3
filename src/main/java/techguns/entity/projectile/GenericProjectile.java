package techguns.entity.projectile;

import javax.annotation.Nullable;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGDamageCategory;
import techguns.core.gun.GunMath;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.gun.FirePos;
import techguns.gun.ProjectileFactory;
import techguns.gun.ShotParams;
import techguns.gun.ammo.DamageModifier;
import techguns.registry.TGEntities;

/**
 * Standard bullet: moves in a straight line (plus optional gravity), hits the first entity or block in its
 * path, damage falls off with distance (Techguns 2 {@code GenericProjectile}).
 */
public class GenericProjectile extends Projectile implements IEntityWithComplexSpawn {
    /** Sideways distance of the muzzle from the eye when firing from the left or right hand. */
    private static final float SIDE_OFFSET = 0.16f;
    /** Spread is given as a fraction; this scales it to degrees. */
    private static final float SPREAD_TO_DEGREES = 40.0f;

    /** What happens on hit for each bullet type. */
    public enum Kind {
        DEFAULT(TGDamageCategory.PROJECTILE, DeathType.GORE),
        INCENDIARY(TGDamageCategory.FIRE, DeathType.DEFAULT);

        public final TGDamageCategory category;
        public final DeathType deathType;

        Kind(TGDamageCategory category, DeathType deathType) {
            this.category = category;
            this.deathType = deathType;
        }
    }

    protected float damage;
    protected float damageMin;
    protected float dropStart;
    protected float dropEnd;
    protected float penetration;
    protected float speed = 1.0f;
    protected int lifetime = 100;
    protected double gravity;
    protected float radius;
    protected boolean blockDamage;
    protected Kind kind = Kind.DEFAULT;
    protected Vec3 start = Vec3.ZERO;

    /** Client side: keep the bullet a few ticks after impact so the trail can fade out. */
    private boolean dying;
    private int dyingTicks;

    public GenericProjectile(EntityType<? extends GenericProjectile> type, Level level) {
        super(type, level);
    }

    protected GenericProjectile(EntityType<? extends GenericProjectile> type, ShotParams params) {
        this(type, params.level());
        this.damage = params.damage();
        this.damageMin = params.damageMin();
        this.dropStart = params.dropStart();
        this.dropEnd = params.dropEnd();
        this.penetration = params.penetration();
        this.speed = params.speed();
        this.lifetime = params.lifetime();
        this.gravity = params.gravity();
        this.radius = params.radius();
        this.blockDamage = params.blockDamage();
        this.setOwner(params.shooter());
        aimFrom(params.shooter(), params.spread(), params.firePos());
    }

    /** Places the projectile at the shooter's muzzle and sets its velocity along the (spread) view direction. */
    protected void aimFrom(LivingEntity shooter, float spread, FirePos firePos) {
        Vec3 pos = shooter.getEyePosition().add(0, -0.1, 0);
        if (firePos != FirePos.CENTER) {
            float yawRad = shooter.getYRot() * Mth.DEG_TO_RAD;
            double side = firePos == FirePos.RIGHT ? -SIDE_OFFSET : SIDE_OFFSET;
            pos = pos.add(Mth.cos(yawRad) * side, 0, Mth.sin(yawRad) * side);
        }
        setPos(pos);
        this.start = pos;

        float yaw = shooter.getYRot() + (spread - 2.0f * random.nextFloat() * spread) * SPREAD_TO_DEGREES;
        float pitch = shooter.getXRot() + (spread - 2.0f * random.nextFloat() * spread) * SPREAD_TO_DEGREES;
        Vec3 velocity = Vec3.directionFromRotation(pitch, yaw).scale(speed);
        Vec3 shooterMotion = shooter.getDeltaMovement();
        setDeltaMovement(velocity.add(shooterMotion.x, shooter.onGround() ? 0.0 : shooterMotion.y, shooterMotion.z));
        updateRotationFromMotion();
        this.yRotO = getYRot();
        this.xRotO = getXRot();
    }

    private void updateRotationFromMotion() {
        Vec3 motion = getDeltaMovement();
        setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(motion.y, motion.horizontalDistance()) * Mth.RAD_TO_DEG));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (dying) {
            if (++dyingTicks > 2) {
                discard();
            }
            return;
        }

        Vec3 from = position();
        Vec3 to = from.add(getDeltaMovement());
        HitResult blockHit = level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            to = blockHit.getLocation();
        }
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level(), this, from, to,
                getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.0), this::canHitEntity);
        HitResult hit = entityHit != null ? entityHit : blockHit;
        if (hit.getType() != HitResult.Type.MISS && !isRemoved()) {
            onHit(hit);
            if (isRemoved() || dying) {
                return;
            }
        }

        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        if (isInWater()) {
            setDeltaMovement(motion.scale(0.85));
        }
        if (gravity > 0.0) {
            setDeltaMovement(getDeltaMovement().add(0, -gravity, 0));
        }
        updateRotationFromMotion();

        if (tickCount >= lifetime) {
            removeAfterHit();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (!super.canHitEntity(target)) {
            return false;
        }
        // respect PvP settings
        return !(target instanceof Player targetPlayer && getOwner() instanceof Player owner && !owner.canHarmPlayer(targetPlayer));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide) {
            Entity target = result.getEntity();
            Entity owner = getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                livingOwner.setLastHurtMob(target);
            }
            if (target.hurt(createDamageSource(), currentDamage()) && target instanceof LivingEntity living) {
                onHitLiving(living);
            }
        }
        removeAfterHit();
    }

    /** Extra effects after a successful hit (burning etc.). */
    protected void onHitLiving(LivingEntity target) {
        if (kind == Kind.INCENDIARY && !target.fireImmune()) {
            target.igniteForSeconds(3);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel server) {
            impactEffects(server, result);
        }
        removeAfterHit();
    }

    /** Placeholder impact feedback until the Techguns particle system is ported (Phase 2c). */
    protected void impactEffects(ServerLevel level, BlockHitResult result) {
        BlockState state = level.getBlockState(result.getBlockPos());
        Vec3 pos = result.getLocation();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.x, pos.y, pos.z, 6, 0.05, 0.05, 0.05, 0.1);
        SoundType sound = state.getSoundType(level, result.getBlockPos(), this);
        level.playSound(null, pos.x, pos.y, pos.z, sound.getHitSound(), SoundSource.BLOCKS, 0.6f, 1.2f);
    }

    protected TGDamageSource createDamageSource() {
        TGDamageSource source = switch (kind) {
            case INCENDIARY -> TGDamage.fire(level(), this, getOwner(), kind.deathType);
            default -> TGDamage.bullet(level(), this, getOwner(), kind.deathType);
        };
        return source.setArmorPenetration(penetration);
    }

    /** Damage after falloff over the distance travelled so far. */
    protected float currentDamage() {
        return GunMath.damageAtDistance(damage, damageMin, dropStart, dropEnd, start.distanceTo(position()));
    }

    /** Server: remove now. Client: keep briefly for the trail. */
    protected void removeAfterHit() {
        if (level().isClientSide) {
            dying = true;
            setDeltaMovement(Vec3.ZERO);
        } else {
            discard();
        }
    }

    public GenericProjectile setKind(Kind kind) {
        this.kind = kind;
        return this;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128.0 * 128.0;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        buf.writeFloat(speed);
        buf.writeVarInt(lifetime);
        buf.writeDouble(gravity);
        buf.writeByte(kind.ordinal());
        buf.writeDouble(start.x);
        buf.writeDouble(start.y);
        buf.writeDouble(start.z);
        Vec3 motion = getDeltaMovement();
        buf.writeDouble(motion.x);
        buf.writeDouble(motion.y);
        buf.writeDouble(motion.z);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        speed = buf.readFloat();
        lifetime = buf.readVarInt();
        gravity = buf.readDouble();
        kind = Kind.values()[buf.readByte()];
        start = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        // exact velocity: the vanilla spawn packet clamps and quantizes it
        setDeltaMovement(buf.readDouble(), buf.readDouble(), buf.readDouble());
        updateRotationFromMotion();
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", damage);
        tag.putFloat("damage_min", damageMin);
        tag.putFloat("drop_start", dropStart);
        tag.putFloat("drop_end", dropEnd);
        tag.putFloat("penetration", penetration);
        tag.putFloat("speed", speed);
        tag.putInt("lifetime", lifetime);
        tag.putDouble("gravity", gravity);
        tag.putByte("kind", (byte) kind.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("damage");
        damageMin = tag.getFloat("damage_min");
        dropStart = tag.getFloat("drop_start");
        dropEnd = tag.getFloat("drop_end");
        penetration = tag.getFloat("penetration");
        speed = tag.getFloat("speed");
        lifetime = tag.getInt("lifetime");
        gravity = tag.getDouble("gravity");
        kind = Kind.values()[Mth.clamp(tag.getByte("kind"), 0, Kind.values().length - 1)];
        start = position();
    }

    /** Factory for guns firing standard bullets of one kind. */
    public record Factory(Kind kind, @Nullable DamageModifier ammoModifier) implements ProjectileFactory<GenericProjectile> {
        public Factory(Kind kind) {
            this(kind, null);
        }

        @Override
        public GenericProjectile create(ShotParams params) {
            return new GenericProjectile(TGEntities.GENERIC_PROJECTILE.get(), params).setKind(kind);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return kind.category;
        }

        @Override
        public DamageModifier modifier() {
            return ammoModifier != null ? ammoModifier : DamageModifier.NONE;
        }
    }
}
