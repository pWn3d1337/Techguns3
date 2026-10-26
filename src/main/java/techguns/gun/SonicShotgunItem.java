package techguns.gun;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import techguns.entity.projectile.SonicShotgunProjectile;

/**
 * Sonic shotgun (Fabric {@code SonicShotgun}): one shot is a centre segment plus two rings of 5 and 10 segments,
 * 5 and 10 degrees off the centre, that share their hit list so every target is hit once.
 */
public class SonicShotgunItem extends GunItem {
    private static final int RING_BASE_COUNT = 5;
    private static final int RINGS = 2;
    private static final float RING_ANGLE_DEGREES = 5.0f;

    public SonicShotgunItem(Properties properties, GunStats stats, GunSounds sounds, ProjectileSelector projectiles) {
        super(properties, stats, sounds, projectiles);
    }

    @Override
    protected void fireProjectiles(Level level, LivingEntity shooter, ItemStack stack, float spreadMultiplier, float damageMultiplier,
                                   FirePos firePos) {
        IntSet hit = new IntOpenHashSet();
        GunStats.Projectile projectile = stats().projectile();
        SonicShotgunProjectile main = (SonicShotgunProjectile) createProjectile(level, shooter, stack,
                projectile.spread() * spreadMultiplier, damageMultiplier, firePos);
        main.joinShot(hit, true);
        level.addFreshEntity(main);

        Vec3 forward = main.getDeltaMovement().normalize();
        Vec3 side = forward.cross(new Vec3(0, 1, 0)).normalize();
        for (int ring = 1; ring <= RINGS; ring++) {
            int count = RING_BASE_COUNT * ring;
            double step = Mth.TWO_PI / count;
            Vec3 tilted = rotate(forward, side, RING_ANGLE_DEGREES * ring * Mth.DEG_TO_RAD);
            for (int i = 0; i < count; i++) {
                SonicShotgunProjectile segment = (SonicShotgunProjectile) createProjectile(level, shooter, stack, 0.0f,
                        damageMultiplier, firePos);
                segment.joinShot(hit, false);
                segment.redirect(rotate(tilted, forward, step * (i + 1)));
                level.addFreshEntity(segment);
            }
        }
    }

    /** Rodrigues rotation of {@code v} around the unit vector {@code axis}. */
    private static Vec3 rotate(Vec3 v, Vec3 axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return v.scale(cos).add(axis.cross(v).scale(sin)).add(axis.scale(axis.dot(v) * (1.0 - cos)));
    }
}
