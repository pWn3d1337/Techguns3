package techguns.entity.npc;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGArmorProvider;
import techguns.registry.TGSounds;

/**
 * Techguns 2 {@code AlienBug}: a big spider-like alien (it extended the vanilla spider) that climbs walls, leaps at
 * its target and bites; it hunts players and iron golems in any light and gives up the chase in bright light now
 * and then. Armor against Techguns damage per category, none against fire.
 */
public class AlienBug extends TGNpc implements TGArmorProvider {
    private static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(AlienBug.class, EntityDataSerializers.BOOLEAN);
    /** Entity event that starts the bite animation on the client. */
    private static final byte EVENT_BITE = 4;
    private static final int BITE_TICKS = 10;

    private int attackTimer;

    public AlienBug(EntityType<? extends TGNpc> type, Level level, NpcDefinition definition) {
        super(type, level, definition);
    }

    /** Techguns 2 values: health 20, attack 4, speed 1 (on top of the spider's follow range 16). */
    public static AttributeSupplier.Builder createAlienBugAttributes() {
        return TGNpc.createAttributes(20, 1.0, 4, 16, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLIMBING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4f));
        goalSelector.addGoal(4, new BugAttackGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new BugTargetGoal<>(this, Player.class));
        targetSelector.addGoal(3, new BugTargetGoal<>(this, IronGolem.class));
    }

    /** Bites only: the attack goal is its own. */
    @Override
    protected void updateCombatGoal() {
    }

    /** Ticks left of the bite animation. */
    public int attackTimer() {
        return attackTimer;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            entityData.set(CLIMBING, horizontalCollision);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTimer > 0) {
            attackTimer--;
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        attackTimer = BITE_TICKS;
        level().playSound(null, getX(), getY(), getZ(), TGSounds.ALIENBUG_BITE.get(), SoundSource.HOSTILE, 2.0f, 1.0f);
        level().broadcastEntityEvent(this, EVENT_BITE);
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_BITE) {
            attackTimer = BITE_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    // --- vanilla Spider: wall climbing, cobwebs, no poison ---

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public boolean onClimbable() {
        return entityData.get(CLIMBING);
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    @Override
    public float tgArmorAgainst(TGDamageCategory category) {
        return switch (category) {
            case ENERGY, EXPLOSION, ICE, LIGHTNING -> 5.0f;
            case PHYSICAL, PROJECTILE -> 10.0f;
            case POISON, RADIATION -> 20.0f;
            default -> 0.0f;
        };
    }

    /** Techguns 2 {@code AISpiderAttack}: in bright light the bug sometimes loses interest. */
    static class BugAttackGoal extends MeleeAttackGoal {
        BugAttackGoal(AlienBug bug) {
            super(bug, 1.0, true);
        }

        @Override
        public boolean canContinueToUse() {
            if (mob.getLightLevelDependentMagicValue() >= 0.5f && mob.getRandom().nextInt(100) == 0) {
                mob.setTarget(null);
                return false;
            }
            return super.canContinueToUse();
        }
    }

    /** Techguns 2 {@code AIAlienBugTarget}: hisses when it picks a target. */
    static class BugTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        BugTargetGoal(AlienBug bug, Class<T> targetClass) {
            super(bug, targetClass, true);
        }

        @Override
        public void start() {
            super.start();
            mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), TGSounds.ALIENBUG_AGGRO.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
        }
    }
}
