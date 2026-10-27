package techguns.entity.projectile;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.BaseFireBlock;
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
import techguns.entity.ai.GunShooter;
import techguns.gun.FirePos;
import techguns.gun.ProjectileFactory;
import techguns.gun.ShotParams;
import techguns.gun.ammo.DamageModifier;
import techguns.core.fx.EntityCondition;
import techguns.network.FxClientHandler;
import techguns.network.FxPayloads;
import techguns.network.TGNetwork;
import techguns.registry.TGEntities;
import techguns.registry.TGParticleTypes;

/**
 * Standard bullet: moves in a straight line (plus optional gravity), hits the first entity or block in its
 * path, damage falls off with distance (Techguns 2 {@code GenericProjectile}).
 */
public class GenericProjectile extends Projectile implements IEntityWithComplexSpawn {
    /** Spread is given as a fraction; this scales it to degrees. */
    private static final float SPREAD_TO_DEGREES = 40.0f;

    /** What happens on hit for each bullet type (Fabric {@code GenericProjectileType}). */
    public enum Kind {
        DEFAULT(TGDamageCategory.PROJECTILE, DeathType.GORE, false),
        INCENDIARY(TGDamageCategory.FIRE, DeathType.DEFAULT, false),
        /** Small blast on impact (Techguns 2 {@code GenericProjectileExplosive}; Fabric only changed the damage type). */
        EXPLOSIVE(TGDamageCategory.EXPLOSION, DeathType.GORE, false),
        /** Advanced rounds (PDW, pulse rifle): blue tracer and impacts. */
        ADVANCED(TGDamageCategory.PROJECTILE, DeathType.GORE, true),
        /** Blaster bolts: energy damage, laser impact. */
        BLASTER(TGDamageCategory.ENERGY, DeathType.LASER, false),
        /** Gauss slugs: blue impacts plus the gauss impact flash and trail. */
        GAUSS(TGDamageCategory.PROJECTILE, DeathType.GORE, true, null, "GaussProjectileTrail", true),
        // FX-only projectiles (Fabric GenericProjectileFX): no bullet sprite, a particle trail and an impact FX
        /** MIB gun. */
        DEATOMIZER(TGDamageCategory.ENERGY, DeathType.GORE, "BlueBlasterExplosion", "BlueBlasterTrail"),
        /** Alien blaster; sets blocks on fire when the gun does block damage. */
        ALIEN_BLASTER(TGDamageCategory.FIRE, DeathType.DEFAULT, "AlienExplosion", "AlienBlasterTrail"),
        /** Nether blaster; sets targets on fire. */
        NETHER_BLASTER(TGDamageCategory.FIRE, DeathType.DEFAULT, "CyberdemonBlasterImpact", "CyberDemonBlasterTrail"),
        /** Flamethrower flame; sets targets on fire, may set blocks on fire, goes out in water. */
        FLAME(TGDamageCategory.FIRE, DeathType.DEFAULT, "FlamethrowerImpact", "FlamethrowerTrail"),
        /** Hand cannon stone ball. */
        STONE(TGDamageCategory.PROJECTILE, DeathType.GORE, false);

        public final TGDamageCategory category;
        public final DeathType deathType;
        /** Uses the blue bullet impact particles. */
        public final boolean blueImpact;
        /** Particle system played on any hit instead of the bullet impact; null = bullet impact by block sound. */
        @Nullable
        public final String impactFx;
        /** Particle system attached to the projectile on the client; null = none. */
        @Nullable
        public final String trailFx;
        /** Draws the bullet sprite. */
        public final boolean visible;

        Kind(TGDamageCategory category, DeathType deathType, boolean blueImpact) {
            this(category, deathType, blueImpact, null, null, true);
        }

        Kind(TGDamageCategory category, DeathType deathType, String impactFx, String trailFx) {
            this(category, deathType, false, impactFx, trailFx, false);
        }

        Kind(TGDamageCategory category, DeathType deathType, boolean blueImpact, @Nullable String impactFx, @Nullable String trailFx,
             boolean visible) {
            this.category = category;
            this.deathType = deathType;
            this.blueImpact = blueImpact;
            this.impactFx = impactFx;
            this.trailFx = trailFx;
            this.visible = visible;
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
    protected boolean dying;
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
            double offset = shooter instanceof GunShooter gunShooter ? gunShooter.bulletSideOffset() : GunShooter.DEFAULT_BULLET_SIDE_OFFSET;
            double side = firePos == FirePos.RIGHT ? -offset : offset;
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

    protected void updateRotationFromMotion() {
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
            setDeltaMovement(motion.scale(waterDrag()));
        }
        if (gravity > 0.0) {
            setDeltaMovement(getDeltaMovement().add(0, -gravity, 0));
        }
        if (kind == Kind.FLAME && isInWater()) {
            removeAfterHit();
            return;
        }
        updateRotationFromMotion();

        if (tickCount >= lifetime) {
            removeAfterHit();
        }
    }

    /** Velocity factor per tick while in water. */
    protected double waterDrag() {
        return 0.85;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        // never the shooter (Techguns 2): slow projectiles start inside the shooter's inflated hitbox
        if (!super.canHitEntity(target) || target == getOwner()) {
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
            if (kind == Kind.EXPLOSIVE) {
                explode(result.getLocation());
            }
            if (kind.impactFx != null && level() instanceof ServerLevel server) {
                TGNetwork.sendFxNear(server, result.getLocation(), IMPACT_FX_RADIUS, kind.impactFx);
            }
        }
        removeAfterHit();
    }

    /** Explosive rounds: radius {@value #EXPLOSIVE_POWER} blast, blocks only when the gun does block damage. */
    private static final float EXPLOSIVE_POWER = 1.5f;

    protected void explode(Vec3 pos) {
        if (level() instanceof ServerLevel server) {
            TGNetwork.sendFxNear(server, pos, IMPACT_FX_RADIUS, "MiningChargeBlockExplosion");
            Level.ExplosionInteraction interaction = blockDamage ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
            server.explode(this, createDamageSource(), null, pos.x, pos.y, pos.z, EXPLOSIVE_POWER, false, interaction,
                    TGParticleTypes.NONE.get(), TGParticleTypes.NONE.get(), SoundEvents.GENERIC_EXPLODE);
        }
    }

    /** Extra effects after a successful hit (burning etc.). */
    protected void onHitLiving(LivingEntity target) {
        if ((kind == Kind.INCENDIARY || kind == Kind.NETHER_BLASTER || kind == Kind.FLAME) && !target.fireImmune()) {
            target.igniteForSeconds(3);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel server) {
            impactEffects(server, result);
            if (kind == Kind.EXPLOSIVE) {
                explode(result.getLocation());
            }
            if (blockDamage && (kind == Kind.ALIEN_BLASTER || kind == Kind.FLAME)) {
                burnBlock(server, result, kind == Kind.FLAME ? 0.5 : 0.35);
            }
        }
        removeAfterHit();
    }

    /** Sets the hit face on fire with the given chance (Fabric {@code burnBlocks}, which only lights the face in front). */
    protected void burnBlock(ServerLevel level, BlockHitResult result, double chance) {
        if (random.nextDouble() > chance) {
            return;
        }
        BlockPos firePos = result.getBlockPos().relative(result.getDirection());
        if (level.isEmptyBlock(firePos) && BaseFireBlock.canBePlacedAt(level, firePos, result.getDirection())) {
            level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
        }
    }

    /** Radius (blocks) within which players get the impact sound and particles. */
    protected static final double IMPACT_FX_RADIUS = 64.0;
    /** Sends the impact sound and particles for a block hit; sound and particles depend on the block's sound type. */
    protected void impactEffects(ServerLevel level, BlockHitResult result) {
        BlockState state = level.getBlockState(result.getBlockPos());
        SoundType sound = state.getSoundType(level, result.getBlockPos(), this);
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
        if (kind == Kind.BLASTER) {
            TGNetwork.sendFxNear(level, pos, IMPACT_FX_RADIUS, "LaserGunImpact");
            return;
        }
        if (kind.impactFx != null) {
            TGNetwork.sendFxNear(level, pos, IMPACT_FX_RADIUS, kind.impactFx);
            return;
        }
        TGNetwork.sendGunImpactNear(level, new FxPayloads.GunImpactFx(impactSoundType(sound), pos.x, pos.y, pos.z, pitch, yaw,
                kind == Kind.INCENDIARY, kind.blueImpact), IMPACT_FX_RADIUS);
        if (kind == Kind.GAUSS) {
            TGNetwork.sendFxNear(level, pos.x, pos.y, pos.z, IMPACT_FX_RADIUS,
                    new FxPayloads.SpawnFx("GaussRifleImpact_Block", pos.x, pos.y, pos.z, 0, 0, 0, pitch, yaw, 1.0f));
        }
    }

    /**
     * Impact FX group of a block sound type: 0 stone, 1 wood, 2 glass, 3 metal, 4 gravel/sand, -1 everything else
     * (default FX). 5 (dirt sound only) is not sent by bullets.
     */
    protected static short impactSoundType(SoundType sound) {
        if (sound == SoundType.STONE) {
            return 0;
        } else if (sound == SoundType.WOOD || sound == SoundType.LADDER) {
            return 1;
        } else if (sound == SoundType.GLASS) {
            return 2;
        } else if (sound == SoundType.METAL || sound == SoundType.ANVIL) {
            return 3;
        } else if (sound == SoundType.GRAVEL || sound == SoundType.SAND) {
            return 4;
        }
        return -1;
    }

    protected TGDamageSource createDamageSource() {
        TGDamageSource source = switch (kind) {
            case INCENDIARY -> TGDamage.fire(level(), this, getOwner(), kind.deathType);
            case EXPLOSIVE -> TGDamage.explosion(level(), this, getOwner(), kind.deathType);
            case BLASTER -> TGDamage.energy(level(), this, getOwner(), kind.deathType);
            case DEATOMIZER -> TGDamage.energy(level(), this, getOwner(), kind.deathType).setGoreChance(1.0f);
            case ALIEN_BLASTER, NETHER_BLASTER -> TGDamage.fire(level(), this, getOwner(), kind.deathType);
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
        startTrail(kind.trailFx);
    }

    /** Client: attaches a particle trail; called once the spawn data (kind etc.) is known. */
    protected void startTrail(@Nullable String trailFx) {
        if (trailFx != null && level().isClientSide) {
            FxClientHandler.get().spawnFxOnEntity(level(), trailFx, this, 0f, 0f, 0f, false, EntityCondition.ENTITY_ALIVE);
        }
    }

    /** Client: attaches a scaled particle trail. */
    protected void startTrail(String trailFx, float scale) {
        if (level().isClientSide) {
            FxClientHandler.get().spawnFxOnEntity(level(), trailFx, this, scale, EntityCondition.ENTITY_ALIVE);
        }
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
