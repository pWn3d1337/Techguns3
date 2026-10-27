package techguns.entity.npc;

import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGArmorProvider;
import techguns.entity.projectile.GenericProjectile;
import techguns.entity.projectile.RocketProjectile;
import techguns.gun.FirePos;
import techguns.gun.ShotParams;
import techguns.network.FxPayloads;
import techguns.network.TGNetwork;
import techguns.registry.TGSounds;

/**
 * Techguns 2 {@code AttackHelicopter} ({@code GenericFlyingMob}): a gunship that hovers 24 blocks above the ground,
 * wanders, turns toward the nearest player and fires bursts of five bullets followed by a rocket. It spins down for
 * five seconds when it dies and explodes at the end.
 */
public class AttackHelicopter extends TGNpc implements TGArmorProvider {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(AttackHelicopter.class, EntityDataSerializers.BOOLEAN);
    public static final int MAX_DEATH_TIME = 100;
    private static final int TARGET_FLY_HEIGHT = 24;
    private static final int ROTOR_SOUND_INTERVAL = 61;
    /** Techguns 2 {@code getBulletOffsetSide}: the rocket pods sit on the stub wings. */
    private static final float ROCKET_SIDE_OFFSET = 2.0f;

    private int rotorSoundTick;
    private int pendingExperience;

    public AttackHelicopter(EntityType<? extends TGNpc> type, Level level, NpcDefinition definition) {
        super(type, level, definition);
        this.moveControl = new FlyingMoveControl(this);
        this.lookControl = new FixedLookControl(this);
        setNoGravity(true);
    }

    /** Techguns 2 values: health 100, follow range 128, armor 16, toughness 5. */
    public static AttributeSupplier.Builder createHelicopterAttributes() {
        return TGNpc.createAttributes(100, 0.7, 2, 128, 5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
    }

    public boolean isAttacking() {
        return entityData.get(ATTACKING);
    }

    void setAttacking(boolean attacking) {
        entityData.set(ATTACKING, attacking);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(5, new RandomFlyGoal(this));
        goalSelector.addGoal(7, new LookAroundGoal(this));
        goalSelector.addGoal(7, new HelicopterAttackGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** No gun in hand: the guns are part of the helicopter. */
    @Override
    protected void updateCombatGoal() {
    }

    @Override
    public float bulletSideOffset() {
        return ROCKET_SIDE_OFFSET;
    }

    @Override
    public float tgArmorAgainst(TGDamageCategory category) {
        return switch (category) {
            case LIGHTNING, ENERGY -> 10.0f;
            case FIRE, ICE, PHYSICAL, PROJECTILE, POISON, RADIATION -> 20.0f;
            default -> 0.0f;
        };
    }

    @Override
    public float tgPenetrationResistance(TGDamageCategory category) {
        return category == TGDamageCategory.PROJECTILE ? 0.5f : 0.0f;
    }

    @Override
    public void tick() {
        super.tick();
        if (--rotorSoundTick <= 0 && deathTime == 0) {
            playSound(TGSounds.HELICOPTER_ROTOR.get(), 6.0f, 1.0f);
            rotorSoundTick = ROTOR_SOUND_INTERVAL;
        }
    }

    /** Vanilla {@code FlyingMob.travel}: no gravity, air drag in all directions. */
    @Override
    public void travel(Vec3 travelVector) {
        if (isControlledByLocalInstance()) {
            if (isInWater() || isInLava()) {
                moveRelative(0.02f, travelVector);
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().scale(isInWater() ? 0.8 : 0.5));
            } else {
                float friction = onGround() ? level().getBlockState(getBlockPosBelowThatAffectsMyMovement()).getFriction(level(),
                        getBlockPosBelowThatAffectsMyMovement(), this) * 0.91f : 0.91f;
                float acceleration = 0.16277137f / (friction * friction * friction);
                moveRelative(onGround() ? 0.1f * acceleration : 0.02f, travelVector);
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().scale(friction));
            }
        }
        calculateEntityAnimation(false);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 10.0f;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    /**
     * Experience drops when the wreck explodes, not when it is shot down (Techguns 2 {@code onDeathUpdate}). Decided at
     * death like vanilla, since the player hit timer runs out during the five seconds of falling.
     */
    @Override
    protected void dropExperience(@Nullable Entity attacker) {
        if (level() instanceof ServerLevel server && !wasExperienceConsumed() && (isAlwaysExperienceDropper()
                || lastHurtByPlayerTime > 0 && shouldDropExperience() && server.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) {
            pendingExperience = EventHooks.getExperienceDrop(this, lastHurtByPlayer, getExperienceReward(server, attacker));
        }
    }

    /** Techguns 2 {@code onDeathUpdate}: five seconds of spinning down (drawn by the renderer), then the explosion. */
    @Override
    protected void tickDeath() {
        if (deathTime == 0) {
            playSound(TGSounds.HELICOPTER_DEATH.get(), 6.0f, 1.0f);
        }
        deathTime++;
        if (deathTime >= MAX_DEATH_TIME && level() instanceof ServerLevel server && !isRemoved()) {
            ExperienceOrb.award(server, position(), pendingExperience);
            playSound(TGSounds.HELICOPTER_EXPLODE.get(), 6.0f, 1.0f);
            TGNetwork.sendFxNear(server, getX(), getY() - 8.0, getZ(), 100.0,
                    new FxPayloads.SpawnFx("HelicopterDeathExplosion", getX(), getY() - 8.0, getZ()));
            level().broadcastEntityEvent(this, (byte) 60);
            remove(RemovalReason.KILLED);
        }
    }

    /** One bullet of the burst: Techguns 2 damage 12, falling to 8 between 30 and 40 blocks. */
    public void fireBullet() {
        level().addFreshEntity(new GenericProjectile.Factory(GenericProjectile.Kind.DEFAULT).create(shot(FirePos.CENTER, 0.0f)));
    }

    /** A rocket from one of the wing pods: bullet stats plus an explosion radius of 4 that leaves the blocks intact. */
    void fireRocket() {
        level().addFreshEntity(new RocketProjectile.Factory().create(shot(random.nextBoolean() ? FirePos.LEFT : FirePos.RIGHT, 4.0f)));
    }

    private ShotParams shot(FirePos firePos, float radius) {
        return new ShotParams(null, level(), this, 12.0f, 8.0f, 30.0f, 40.0f, 0.25f, 1.0f, 100, 0.05f, 0.0, radius, false, firePos, null);
    }

    /** Techguns 2 {@code FlyingMobMoveHelper} (the ghast's): accelerate toward the wanted position unless blocked. */
    static class FlyingMoveControl extends MoveControl {
        private final AttackHelicopter helicopter;
        private int courseChangeCooldown;

        FlyingMoveControl(AttackHelicopter helicopter) {
            super(helicopter);
            this.helicopter = helicopter;
        }

        @Override
        public void tick() {
            if (operation != Operation.MOVE_TO) {
                return;
            }
            if (courseChangeCooldown-- <= 0) {
                courseChangeCooldown += helicopter.getRandom().nextInt(5) + 2;
                Vec3 offset = new Vec3(wantedX - helicopter.getX(), wantedY - helicopter.getY(), wantedZ - helicopter.getZ());
                double distance = offset.length();
                offset = offset.normalize();
                if (canReach(offset, Mth.ceil(distance))) {
                    helicopter.setDeltaMovement(helicopter.getDeltaMovement().add(offset.scale(0.1)));
                } else {
                    operation = Operation.WAIT;
                }
            }
        }

        private boolean canReach(Vec3 direction, int steps) {
            AABB box = helicopter.getBoundingBox();
            for (int i = 1; i < steps; i++) {
                box = box.move(direction);
                if (!helicopter.level().noCollision(helicopter, box)) {
                    return false;
                }
            }
            return true;
        }
    }

    /** The goals set yaw and pitch directly; the default look control would reset the pitch every tick. */
    static class FixedLookControl extends LookControl {
        FixedLookControl(AttackHelicopter helicopter) {
            super(helicopter);
        }

        @Override
        public void tick() {
        }
    }

    /** Techguns 2 {@code AIRandomFly}: a new spot within 16 blocks, 24 blocks above the ground there. */
    static class RandomFlyGoal extends Goal {
        private final AttackHelicopter helicopter;

        RandomFlyGoal(AttackHelicopter helicopter) {
            this.helicopter = helicopter;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl move = helicopter.getMoveControl();
            if (!move.hasWanted()) {
                return true;
            }
            double distanceSquared = helicopter.distanceToSqr(move.getWantedX(), move.getWantedY(), move.getWantedZ());
            return distanceSquared < 1.0 || distanceSquared > 3600.0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = helicopter.getRandom();
            double x = helicopter.getX() + (random.nextFloat() * 2.0f - 1.0f) * 16.0f;
            double z = helicopter.getZ() + (random.nextFloat() * 2.0f - 1.0f) * 16.0f;
            int ground = helicopter.level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
            helicopter.getMoveControl().setWantedPosition(x, ground + TARGET_FLY_HEIGHT, z, 1.0);
        }
    }

    /** Techguns 2 {@code AILookAround}: face the flight direction, or the target within 96 blocks with the gun on it. */
    static class LookAroundGoal extends Goal {
        private final AttackHelicopter helicopter;

        LookAroundGoal(AttackHelicopter helicopter) {
            this.helicopter = helicopter;
            setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = helicopter.getTarget();
            if (target == null) {
                Vec3 motion = helicopter.getDeltaMovement();
                if (motion.horizontalDistanceSqr() > 1.0E-6) {
                    setRotation((float) (-Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG), 0.0f);
                }
            } else if (target.distanceToSqr(helicopter) < 96.0 * 96.0) {
                double dx = target.getX() - helicopter.getX();
                double dz = target.getZ() - helicopter.getZ();
                double dy = target.getY(0.5) - helicopter.getEyeY();
                float yaw = (float) (-Mth.atan2(dx, dz) * Mth.RAD_TO_DEG);
                // the gun only swings downward
                float pitch = Mth.clamp((float) (-Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG), 0.0f, 90.0f);
                setRotation(yaw, pitch);
            }
        }

        private void setRotation(float yaw, float pitch) {
            helicopter.setYRot(yaw);
            helicopter.yBodyRot = yaw;
            helicopter.yHeadRot = yaw;
            helicopter.setXRot(pitch);
        }
    }

    /** Techguns 2 {@code AIHelicopterAttack}: five bullets from tick 14, a rocket at tick 35, then a pause. */
    static class HelicopterAttackGoal extends Goal {
        private final AttackHelicopter helicopter;
        private int attackTimer;

        HelicopterAttackGoal(AttackHelicopter helicopter) {
            this.helicopter = helicopter;
        }

        @Override
        public boolean canUse() {
            return helicopter.getTarget() != null;
        }

        @Override
        public void start() {
            attackTimer = 0;
        }

        @Override
        public void stop() {
            helicopter.setAttacking(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = helicopter.getTarget();
            if (target != null && target.distanceToSqr(helicopter) < 64.0 * 64.0 && helicopter.getSensing().hasLineOfSight(target)) {
                attackTimer++;
                if (attackTimer >= 14 && attackTimer < 24 && attackTimer % 2 == 0) {
                    if (attackTimer == 14) {
                        playGunSound(TGSounds.HELICOPTER_BURST.get());
                    }
                    helicopter.fireBullet();
                } else if (attackTimer == 35) {
                    playGunSound(TGSounds.ROCKET_FIRE.get());
                    helicopter.fireRocket();
                } else if (attackTimer > 35) {
                    attackTimer = -30;
                }
            } else if (attackTimer > 0) {
                attackTimer--;
            }
            helicopter.setAttacking(attackTimer > 10);
        }

        private void playGunSound(SoundEvent sound) {
            helicopter.level().playSound(null, helicopter.getX(), helicopter.getY(), helicopter.getZ(), sound, SoundSource.HOSTILE, 8.0f, 1.0f);
        }
    }
}
