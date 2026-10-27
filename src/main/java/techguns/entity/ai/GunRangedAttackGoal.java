package techguns.entity.ai;

import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import techguns.gun.GunStats;

/**
 * Techguns 2 {@code EntityAIRangedAttack}: attacks the target in sight and in range, every {@code attackTime} ticks
 * (sooner when close), with bursts of extra shots {@code burstAttackTime} ticks apart; walks towards the target
 * when out of range (unless the shooter can't move).
 */
public class GunRangedAttackGoal extends Goal {
    private final Mob mob;
    private final GunShooter shooter;
    private final double moveSpeed;
    private final int attackTimeVariance;
    private final int maxAttackTime;
    private final float attackRange;
    private final float attackRangeSquared;
    private final int maxBurstCount;
    private final int shotDelay;
    @Nullable
    private LivingEntity target;
    private int attackTime = -1;
    private int ticksTargetSeen;
    private int burstCount;

    public <T extends Mob & GunShooter> GunRangedAttackGoal(T mob, double moveSpeed, GunStats.Ai ai) {
        this.mob = mob;
        this.shooter = mob;
        this.moveSpeed = moveSpeed;
        // Techguns 2 GenericGun.getAIAttack: variance a third of the attack time
        this.attackTimeVariance = ai.attackTime() / 3;
        this.maxAttackTime = ai.attackTime();
        this.attackRange = ai.attackRange();
        this.attackRangeSquared = attackRange * attackRange;
        this.maxBurstCount = ai.burstCount();
        this.burstCount = maxBurstCount;
        this.shotDelay = ai.burstAttackTime();
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity current = mob.getTarget();
        if (current == null || !current.isAlive()) {
            return false;
        }
        target = current;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || !mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        target = null;
        ticksTargetSeen = 0;
        attackTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }
        double distanceSquared = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean inSight = mob.getSensing().hasLineOfSight(target);
        ticksTargetSeen = inSight ? ticksTargetSeen + 1 : 0;
        if (moveSpeed > 0) {
            if (distanceSquared <= attackRangeSquared && ticksTargetSeen >= 20) {
                mob.getNavigation().stop();
            } else {
                mob.getNavigation().moveTo(target, moveSpeed);
            }
        }
        mob.getLookControl().setLookAt(target, 30.0f, 55.0f);
        if (--attackTime == 0) {
            if (distanceSquared > attackRangeSquared || !inSight) {
                return;
            }
            float f = Mth.sqrt((float) distanceSquared) / attackRange;
            shooter.performGunAttack(target, Mth.clamp(f, 0.1f, 1.0f));
            if (maxBurstCount > 0) {
                burstCount--;
            }
            if (burstCount > 0) {
                attackTime = shotDelay;
            } else {
                burstCount = maxBurstCount;
                attackTime = Mth.floor(f * (maxAttackTime - attackTimeVariance) + attackTimeVariance);
            }
        } else if (attackTime < 0) {
            float f = Mth.sqrt((float) distanceSquared) / attackRange;
            attackTime = Mth.floor(f * (maxAttackTime - attackTimeVariance) + attackTimeVariance);
        }
    }
}
