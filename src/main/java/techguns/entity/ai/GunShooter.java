package techguns.entity.ai;

import net.minecraft.world.entity.LivingEntity;

/** A mob that attacks with a Techguns gun (Techguns 2 {@code IRangedAttackMob} + {@code INPCTechgunsShooter}). */
public interface GunShooter {
    /** One attack on {@code target}; {@code distanceFactor} is the distance relative to the attack range (0.1..1). */
    void performGunAttack(LivingEntity target, float distanceFactor);

    /** Sideways distance of the left/right muzzle from the eyes (Techguns 2 {@code getBulletOffsetSide}). */
    default float bulletSideOffset() {
        return DEFAULT_BULLET_SIDE_OFFSET;
    }

    float DEFAULT_BULLET_SIDE_OFFSET = 0.16f;
}
