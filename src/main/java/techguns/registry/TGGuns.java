package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import techguns.entity.projectile.GenericBeamProjectile;
import techguns.entity.projectile.GenericProjectile;
import techguns.entity.projectile.RocketProjectile;
import techguns.gun.GunItem;
import techguns.gun.GunSounds;
import techguns.gun.GunStats;
import techguns.gun.HoldType;
import techguns.gun.ProjectileSelector;
import techguns.gun.ammo.DamageModifier;

/**
 * All guns. Stats use the Techguns 2 values; see {@link GunStats#builder} for the parameter order.
 */
public final class TGGuns {
    private static final float PENETRATION_LOW = 0.5f;
    private static final float PENETRATION_MED = 1.0f;
    private static final float PENETRATION_MED_HIGH = 2.0f;

    private static final float MAX_RANGE_PISTOL = 40;
    private static final float MAX_RANGE_RIFLE = 60;
    private static final float MAX_RANGE_RIFLE_LONG = 75;
    private static final float MAX_RANGE_SNIPER = 90;

    private static final List<DeferredItem<GunItem>> GUNS = new ArrayList<>();

    // --- Projectile selectors ---
    private static final GenericProjectile.Factory BULLET = new GenericProjectile.Factory(GenericProjectile.Kind.DEFAULT);
    private static final GenericProjectile.Factory BULLET_INCENDIARY = new GenericProjectile.Factory(GenericProjectile.Kind.INCENDIARY, DamageModifier.INCENDIARY_BULLET);

    private static final ProjectileSelector ASSAULT_RIFLE_MAG_PROJECTILES = new ProjectileSelector(TGAmmo.ASSAULT_RIFLE_MAGAZINE_TYPE, BULLET);
    private static final GenericBeamProjectile.Factory LASER_BEAM = new GenericBeamProjectile.Factory(GenericBeamProjectile.BeamType.LASER, 1, false);
    private static final ProjectileSelector LASERGUN_PROJECTILES = new ProjectileSelector(TGAmmo.ENERGY_CELL_TYPE, LASER_BEAM);
    private static final ProjectileSelector ROCKET_PROJECTILES = new ProjectileSelector(TGAmmo.ROCKETS_TYPE, new RocketProjectile.Factory());

    // --- Guns ---
    public static final DeferredItem<GunItem> M4 = gun("m4",
            GunStats.builder(false, 3, 30, 45, 8.0f, MAX_RANGE_RIFLE, 0.05f)
                    .speed(3.0f).zoom(0.75f, true, 0.75f, false).damageDrop(25, 40, 6.0f)
                    .penetration(PENETRATION_LOW).muzzleFlashTicks(4).build(),
            GunSounds.of(TGSounds.M4_FIRE, TGSounds.M4_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> LASERGUN = gun("lasergun",
            GunStats.builder(false, 5, 45, 45, 12.0f, MAX_RANGE_SNIPER, 0.0f)
                    .speed(100.0f).zoom(0.75f, true, 0.75f, false).build(),
            GunSounds.of(TGSounds.LASERGUN_FIRE, TGSounds.LASERGUN_RELOAD), LASERGUN_PROJECTILES);

    public static final DeferredItem<GunItem> ROCKET_LAUNCHER = gun("rocket_launcher",
            GunStats.builder(true, 10, 1, 40, 50.0f, 200, 0.05f)
                    .gravity(0.01).speed(1.0f).recoilTicks(10).damageDrop(2.0f, 4.0f, 10f).forwardOffset(0.35f)
                    .holdType(HoldType.BOW).build(),
            GunSounds.of(TGSounds.ROCKET_FIRE, TGSounds.ROCKET_RELOAD), ROCKET_PROJECTILES);

    private TGGuns() {
    }

    private static DeferredItem<GunItem> gun(String name, GunStats stats, GunSounds sounds, ProjectileSelector projectiles) {
        DeferredItem<GunItem> gun = TGItems.ITEMS.register(name, () -> new GunItem(new Item.Properties(), stats, sounds, projectiles));
        GUNS.add(gun);
        return gun;
    }

    public static List<DeferredItem<GunItem>> guns() {
        return Collections.unmodifiableList(GUNS);
    }

    /** Forces class loading so the guns register. */
    static void init() {
    }
}
