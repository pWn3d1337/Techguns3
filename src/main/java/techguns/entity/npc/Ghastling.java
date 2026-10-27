package techguns.entity.npc;

import java.util.EnumSet;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.pathfinder.PathType;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGArmorProvider;
import techguns.gun.FirePos;
import techguns.gun.GunItem;
import techguns.registry.TGGuns;

/**
 * Techguns 2 {@code Ghastling}: a small walking ghast of the nether that fires bursts of three alien blaster bolts like
 * a blaze its fireballs, and bites up close. No armor against Techguns damage (Techguns 2 armor 0; the armor
 * attribute is for show).
 */
public class Ghastling extends TGNpc implements TGArmorProvider {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Ghastling.class, EntityDataSerializers.BOOLEAN);
    /** Techguns 2 bolt damage 6; the alien blaster's damage is scaled down to it. */
    private static final float BOLT_DAMAGE = 6.0f;

    public Ghastling(EntityType<? extends TGNpc> type, Level level, NpcDefinition definition) {
        super(type, level, definition);
        setPathfindingMalus(PathType.WATER, -1.0f);
        setPathfindingMalus(PathType.LAVA, 8.0f);
        setPathfindingMalus(PathType.DANGER_FIRE, 0.0f);
        setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0f);
    }

    /** Techguns 2 set health, follow range and armor; speed and attack stayed at the 1.12 defaults (0.7, 2). */
    public static AttributeSupplier.Builder createGhastlingAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.FOLLOW_RANGE, 64).add(Attributes.ARMOR, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.7).add(Attributes.ATTACK_DAMAGE, 2);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
    }

    /** Shows the shooting ghast texture. */
    public boolean isAttacking() {
        return entityData.get(ATTACKING);
    }

    void setAttacking(boolean attacking) {
        entityData.set(ATTACKING, attacking);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(4, new BoltAttackGoal(this));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0f));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** No gun in hand: the bolt attack is its own goal. */
    @Override
    protected void updateCombatGoal() {
    }

    @Override
    public float tgArmorAgainst(TGDamageCategory category) {
        return 0.0f;
    }

    @Override
    protected float getSoundVolume() {
        return 10.0f;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    /** One alien blaster bolt at the target, from the middle of the ghastling. */
    void fireBolt() {
        ItemStack blaster = new ItemStack(TGGuns.ALIEN_BLASTER.get());
        if (blaster.getItem() instanceof GunItem gun) {
            gun.fireFromNpc(this, blaster, BOLT_DAMAGE / gun.stats().damage().damage(), 1.0f, FirePos.CENTER);
        }
    }

    /** Techguns 2 {@code AIFireballAttack} (the blaze attack): melee up close, else a wind-up and three bolts. */
    static class BoltAttackGoal extends Goal {
        private final Ghastling ghastling;
        private int attackStep;
        private int attackTime;

        BoltAttackGoal(Ghastling ghastling) {
            this.ghastling = ghastling;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ghastling.getTarget();
            return target != null && target.isAlive() && ghastling.canAttack(target);
        }

        @Override
        public void start() {
            attackStep = 0;
        }

        @Override
        public void stop() {
            ghastling.setAttacking(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            attackTime--;
            LivingEntity target = ghastling.getTarget();
            if (target == null) {
                return;
            }
            double distanceSquared = ghastling.distanceToSqr(target);
            double follow = ghastling.getAttributeValue(Attributes.FOLLOW_RANGE);
            if (distanceSquared < 4.0) {
                if (attackTime <= 0) {
                    attackTime = 20;
                    ghastling.doHurtTarget(target);
                }
                ghastling.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0);
            } else if (distanceSquared < follow * follow) {
                if (attackTime <= 0) {
                    attackStep++;
                    if (attackStep == 1) {
                        attackTime = 30;
                        ghastling.setAttacking(true);
                    } else if (attackStep <= 4) {
                        attackTime = 6;
                    } else {
                        attackTime = 50;
                        attackStep = 0;
                        ghastling.setAttacking(false);
                    }
                    if (attackStep > 1) {
                        ghastling.level().levelEvent(null, LevelEvent.SOUND_BLAZE_FIREBALL, ghastling.blockPosition(), 0);
                        ghastling.lookAt(target, 360.0f, 360.0f);
                        ghastling.fireBolt();
                    }
                }
                ghastling.getLookControl().setLookAt(target, 10.0f, 10.0f);
            } else {
                ghastling.getNavigation().stop();
                ghastling.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0);
            }
        }
    }
}
