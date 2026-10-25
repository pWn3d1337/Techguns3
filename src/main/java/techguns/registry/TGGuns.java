package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import techguns.entity.projectile.GenericBeamProjectile;
import techguns.entity.projectile.GenericProjectile;
import techguns.entity.projectile.RocketProjectile;
import techguns.gun.GunHandType;
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

    private static final ProjectileSelector PISTOL_ROUNDS_PROJECTILES = new ProjectileSelector(TGAmmo.PISTOL_ROUNDS_TYPE, BULLET, BULLET_INCENDIARY);
    private static final ProjectileSelector RIFLE_ROUNDS_PROJECTILES = new ProjectileSelector(TGAmmo.RIFLE_ROUNDS_TYPE, BULLET, BULLET_INCENDIARY);
    private static final ProjectileSelector PISTOL_MAG_PROJECTILES = new ProjectileSelector(TGAmmo.PISTOL_MAGAZINE_TYPE, BULLET, BULLET_INCENDIARY);
    private static final ProjectileSelector SMG_MAG_PROJECTILES = new ProjectileSelector(TGAmmo.SMG_MAGAZINE_TYPE, BULLET, BULLET_INCENDIARY);
    private static final ProjectileSelector ASSAULT_RIFLE_MAG_PROJECTILES = new ProjectileSelector(TGAmmo.ASSAULT_RIFLE_MAGAZINE_TYPE, BULLET, BULLET_INCENDIARY);
    private static final ProjectileSelector LMG_MAG_PROJECTILES = new ProjectileSelector(TGAmmo.LMG_MAGAZINE_TYPE, BULLET, BULLET_INCENDIARY);
    private static final ProjectileSelector MINIGUN_DRUM_PROJECTILES = new ProjectileSelector(TGAmmo.MINIGUN_DRUM_TYPE, BULLET, BULLET_INCENDIARY);
    private static final GenericBeamProjectile.Factory LASER_BEAM = new GenericBeamProjectile.Factory(GenericBeamProjectile.BeamType.LASER, 1, false);
    private static final ProjectileSelector LASERGUN_PROJECTILES = new ProjectileSelector(TGAmmo.ENERGY_CELL_TYPE, LASER_BEAM);
    private static final ProjectileSelector ROCKET_PROJECTILES = new ProjectileSelector(TGAmmo.ROCKETS_TYPE, new RocketProjectile.Factory());

    // --- Guns ---
    public static final DeferredItem<GunItem> M4 = gun("m4",
            GunStats.builder(false, 3, 30, 45, 8.0f, MAX_RANGE_RIFLE, 0.05f)
                    .speed(3.0f).zoom(0.75f, true, 0.75f, false).damageDrop(25, 40, 6.0f)
                    .penetration(PENETRATION_LOW).muzzleFlashTicks(4).build(),
            GunSounds.of(TGSounds.M4_FIRE, TGSounds.M4_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    // --- Batch 3a: bullet guns (Fabric TGuns stats) ---
    public static final DeferredItem<GunItem> REVOLVER = gun("revolver",
            GunStats.builder(true, 6, 6, 45, 8.0f, MAX_RANGE_PISTOL, 0.025f)
                    .damageDrop(12, 20, 6.0f).speed(2.0f).recoilTicks(6).handType(GunHandType.ONE_HANDED).build(),
            GunSounds.of(TGSounds.REVOLVER_FIRE, TGSounds.REVOLVER_RELOAD), PISTOL_ROUNDS_PROJECTILES);

    public static final DeferredItem<GunItem> GOLDEN_REVOLVER = gun("golden_revolver",
            GunStats.builder(true, 8, 6, 45, 14.0f, MAX_RANGE_PISTOL, 0.015f)
                    .damageDrop(12, 30, 6.0f).recoilTicks(8).speed(1.75f).handType(GunHandType.ONE_HANDED).build(),
            GunSounds.of(TGSounds.REVOLVER_GOLDEN_FIRE, TGSounds.REVOLVER_RELOAD), PISTOL_ROUNDS_PROJECTILES);

    public static final DeferredItem<GunItem> PISTOL = gun("pistol",
            // Fabric calls setDamageDrop twice; the second call wins
            GunStats.builder(true, 4, 18, 35, 8.0f, MAX_RANGE_PISTOL, 0.025f)
                    .speed(2.0f).damageDrop(18, 25, 5.0f).handType(GunHandType.ONE_HANDED).recoilTicks(3).build(),
            GunSounds.of(TGSounds.PISTOL_FIRE, TGSounds.PISTOL_RELOAD), PISTOL_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> AK47 = gun("ak47",
            GunStats.builder(false, 3, 30, 45, 9.0f, MAX_RANGE_RIFLE, 0.030f)
                    .damageDrop(20, 30, 5.0f).penetration(PENETRATION_LOW).speed(2.5f).muzzleFlashTicks(4).build(),
            GunSounds.of(TGSounds.AK_FIRE, TGSounds.AK_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> M4_INFILTRATOR = gun("m4_infiltrator",
            GunStats.builder(false, 3, 30, 45, 8.0f, MAX_RANGE_RIFLE, 0.010f)
                    .zoom(0.50f, true, 0.5f, true).damageDrop(25, 35, 6.0f).speed(2.0f).silenced()
                    .penetration(PENETRATION_LOW).build(),
            GunSounds.of(TGSounds.M4_SILENCED_FIRE, TGSounds.M4_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> AUG = gun("aug",
            GunStats.builder(false, 3, 30, 45, 8.0f, MAX_RANGE_RIFLE, 0.010f)
                    .speed(3.25f).zoom(0.50f, true, 0.5f, true).damageDrop(30, 45, 7.0f).penetration(PENETRATION_LOW)
                    .muzzleFlashTicks(4).build(),
            GunSounds.of(TGSounds.AUG_FIRE, TGSounds.AUG_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> SCAR = gun("scar",
            GunStats.builder(false, 4, 20, 45, 12.0f, MAX_RANGE_RIFLE_LONG, 0.015f)
                    .zoom(0.65f, true, 0.5f, true).damageDrop(35, 60, 10.0f).penetration(PENETRATION_MED).speed(3.25f)
                    .muzzleFlashTicks(5).build(),
            GunSounds.of(TGSounds.SCAR_FIRE, TGSounds.SCAR_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> THOMPSON = gun("thompson",
            GunStats.builder(false, 3, 20, 40, 5.0f, MAX_RANGE_PISTOL, 0.05f)
                    .speed(1.75f).damageDrop(15, 24, 3.0f).muzzleFlashTicks(4).build(),
            GunSounds.of(TGSounds.THOMPSON_FIRE, TGSounds.THOMSPON_RELOAD), SMG_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> MAC10 = gun("mac10",
            GunStats.builder(false, 2, 32, 40, 5.0f, MAX_RANGE_PISTOL, 0.05f)
                    .damageDrop(15, 24, 3.0f).recoilTicks(2).muzzleFlashTicks(3).speed(2.0f)
                    .handType(GunHandType.ONE_POINT_FIVE_HANDED).build(),
            GunSounds.of(TGSounds.MAC10_FIRE, TGSounds.M4_RELOAD), SMG_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> VECTOR = gun("vector",
            GunStats.builder(false, 2, 25, 40, 6.0f, MAX_RANGE_PISTOL, 0.05f)
                    .zoom(0.75f, true, 0.35f, false).damageDrop(17, 25, 4.0f).speed(2.0f).recoilTicks(2)
                    .muzzleFlashTicks(3).penetration(PENETRATION_LOW).build(),
            GunSounds.of(TGSounds.VECTOR_FIRE, TGSounds.VECTOR_RELOAD), SMG_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> LMG = gun("lmg",
            GunStats.builder(false, 2, 100, 100, 8.0f, MAX_RANGE_RIFLE_LONG, 0.020f)
                    .zoom(0.75f, true, 0.75f, false).damageDrop(40, 60, 6.0f).penetration(PENETRATION_LOW).speed(3.0f)
                    .muzzleFlashTicks(2).recoilTicks(3).build(),
            GunSounds.of(TGSounds.LMG_FIRE, TGSounds.LMG_RELOAD), LMG_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> BOLT_ACTION = gun("bolt_action",
            GunStats.builder(true, 25, 6, 50, 16.0f, MAX_RANGE_SNIPER, 0.05f)
                    .zoom(0.35f, true, 0.125f, true).speed(3.25f).recoilTicks(15).damageDrop(40, 60, 10.0f)
                    .penetration(PENETRATION_LOW).build(),
            GunSounds.of(TGSounds.BOLT_ACTION_FIRE, TGSounds.BOLT_ACTION_RELOAD).withRechamber(TGSounds.BOLT_ACTION_RECHAMBER),
            RIFLE_ROUNDS_PROJECTILES);

    public static final DeferredItem<GunItem> MINIGUN = gun("minigun",
            GunStats.builder(false, 0, 200, 100, 5.0f, MAX_RANGE_RIFLE_LONG, 0.025f)
                    .damageDrop(30, 50, 3.0f).penetration(PENETRATION_LOW).speed(3.0f).muzzleFlashTicks(4)
                    .continuousAnimation().build(),
            GunSounds.of(TGSounds.MINIGUN_FIRE, TGSounds.MINIGUN_RELOAD), MINIGUN_DRUM_PROJECTILES);

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
