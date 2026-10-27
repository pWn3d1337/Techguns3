package techguns.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.gun.FirePos;
import techguns.gun.ProjectileFactory;
import techguns.gun.ShotParams;
import techguns.network.FxPayloads;
import techguns.network.TGNetwork;
import techguns.registry.TGEntities;

/**
 * Hit-scan beam (lasers, tesla, death ray): traces its full length instantly, deals damage during the first
 * {@code damageTicks} ticks, and stays alive briefly so the beam can be rendered (Techguns 2 {@code GenericBeamProjectile}).
 */
public class GenericBeamProjectile extends GenericProjectile {
    /** Ticks a beam stays visible. */
    public static final int BEAM_LIFETIME = 10;
    /** Dev testing only (/tgdebug slowbeams): keeps beams visible longer; 0 = normal. */
    public static int debugLifetimeOverride = 0;
    private static final float FORWARD_OFFSET = 0.35f;
    private static final float SIDE_OFFSET = 0.16f;

    public enum BeamType {
        LASER(TGDamageCategory.ENERGY, "LaserGunImpact"),
        NDR(TGDamageCategory.RADIATION, "BeamGunImpactFX"),
        TESLA(TGDamageCategory.LIGHTNING, "TeslaFlare");

        public final TGDamageCategory category;
        /** Particle system played where the beam hits (Techguns 2 {@code impactFX} of the beam factory). */
        public final String impactFx;

        BeamType(TGDamageCategory category, String impactFx) {
            this.category = category;
            this.impactFx = impactFx;
        }
    }

    private BeamType beamType = BeamType.LASER;
    private int damageTicks = 1;
    private boolean moveWithShooter;
    private FirePos firePos = FirePos.CENTER;
    /** Visible beam length (blocks), from the last trace. */
    private double length;
    private boolean hitSomething;

    public GenericBeamProjectile(EntityType<? extends GenericBeamProjectile> type, Level level) {
        super(type, level);
    }

    protected GenericBeamProjectile(ShotParams params, BeamType beamType, int damageTicks, boolean moveWithShooter) {
        super(TGEntities.GENERIC_BEAM_PROJECTILE.get(), params);
        this.beamType = beamType;
        this.damageTicks = damageTicks;
        this.moveWithShooter = moveWithShooter;
        this.firePos = params.firePos();
        this.lifetime = debugLifetimeOverride > 0 ? debugLifetimeOverride : BEAM_LIFETIME;
        alignWithShooter();
    }

    /** Beam starts at the muzzle and points along the shooter's view. */
    private void alignWithShooter() {
        if (!(getOwner() instanceof LivingEntity shooter)) {
            return;
        }
        setYRot(shooter.getYHeadRot());
        setXRot(shooter.getXRot());
        float side = switch (firePos) {
            case RIGHT -> SIDE_OFFSET;
            case LEFT -> -SIDE_OFFSET;
            default -> 0f;
        };
        Vec3 look = Vec3.directionFromRotation(getXRot(), getYRot());
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 pos = shooter.getEyePosition().add(look.scale(FORWARD_OFFSET)).add(0, -0.1, 0).add(right.scale(side));
        setPos(pos);
        xo = pos.x;
        yo = pos.y;
        zo = pos.z;
        setDeltaMovement(look.scale(speed));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    public void tick() {
        if (moveWithShooter) {
            alignWithShooter();
        }
        // The server deals damage during damageTicks. The client traces every tick for the visible beam length,
        // because the spawn packet is written after the server's first tick (damageTicks may already be 0 there).
        if (level().isClientSide || damageTicks-- > 0) {
            Vec3 end = trace();
            setBoundingBox(new AABB(position(), end));
        }
        if (tickCount >= lifetime) {
            discard();
        }
        baseTick();
    }

    /** Traces the beam, hits the first entity or block, returns the end point. */
    private Vec3 trace() {
        Vec3 from = position();
        // along the rotation, not the velocity: vanilla motion sync clamps velocity to 3.9 blocks/tick on clients
        Vec3 to = from.add(Vec3.directionFromRotation(getXRot(), getYRot()).scale(speed));
        HitResult blockHit = level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            to = blockHit.getLocation();
        }
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level(), this, from, to,
                new AABB(from, to).inflate(1.0), this::canHitEntity);
        HitResult hit = entityHit != null ? entityHit : blockHit;
        hitSomething = false;
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
            to = hit.getLocation();
        }
        length = from.distanceTo(to);
        return to;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide && !hitSomething) {
            hitSomething = true;
            Entity target = result.getEntity();
            if (getOwner() instanceof LivingEntity owner) {
                owner.setLastHurtMob(target);
            }
            if (target.hurt(createDamageSource(), damage) && beamType == BeamType.NDR && target instanceof LivingEntity living) {
                techguns.radiation.RadiationSources.ndrHit(living);
            }
            if (level() instanceof ServerLevel server) {
                Vec3 pos = result.getLocation();
                sendImpactFx(server, pos, Vec3.ZERO);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level() instanceof ServerLevel server) {
            // the impact FX flies away from the surface
            Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal()).scale(0.1);
            sendImpactFx(server, result.getLocation(), normal);
        }
    }

    private void sendImpactFx(ServerLevel server, Vec3 pos, Vec3 motion) {
        TGNetwork.sendFxNear(server, pos.x, pos.y, pos.z, IMPACT_FX_RADIUS,
                new FxPayloads.SpawnFx(beamType.impactFx, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z));
    }

    @Override
    protected TGDamageSource createDamageSource() {
        TGDamageSource source = switch (beamType) {
            case NDR -> TGDamage.radiation(level(), this, getOwner(), DeathType.LASER);
            // Fabric TeslaProjectile also disables knockback; follows with the knockback multiplier
            case TESLA -> TGDamage.lightning(level(), this, getOwner(), DeathType.LASER);
            default -> TGDamage.energy(level(), this, getOwner(), DeathType.LASER);
        };
        return source.setArmorPenetration(penetration);
    }

    @Override
    protected void removeAfterHit() {
        // beams live for their full (short) lifetime so they can be rendered
    }

    public double length() {
        return length;
    }

    public BeamType beamType() {
        return beamType;
    }

    /** Fade-out progress 0..1 for rendering. */
    public float age(float partialTick) {
        return Mth.clamp((tickCount + partialTick) / lifetime, 0f, 1f);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        super.writeSpawnData(buf);
        buf.writeByte(beamType.ordinal());
        buf.writeVarInt(damageTicks);
        buf.writeBoolean(moveWithShooter);
        buf.writeByte(firePos.ordinal());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        super.readSpawnData(buf);
        // the base class derives yaw and pitch from the motion in arrow convention (yaw = atan2(x, z), pitch positive = up);
        // beams trace and render with the view convention (yaw = atan2(-x, z), pitch positive = down), like the server side
        setYRot(-getYRot());
        setXRot(-getXRot());
        yRotO = getYRot();
        xRotO = getXRot();
        beamType = BeamType.values()[buf.readByte()];
        damageTicks = buf.readVarInt();
        moveWithShooter = buf.readBoolean();
        firePos = FirePos.values()[buf.readByte()];
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("beam_type", (byte) beamType.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        beamType = BeamType.values()[Mth.clamp(tag.getByte("beam_type"), 0, BeamType.values().length - 1)];
    }

    public record Factory(BeamType beamType, int damageTicks, boolean moveWithShooter) implements ProjectileFactory<GenericBeamProjectile> {
        @Override
        public GenericBeamProjectile create(ShotParams params) {
            return new GenericBeamProjectile(params, beamType, damageTicks, moveWithShooter);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return beamType.category;
        }
    }
}
