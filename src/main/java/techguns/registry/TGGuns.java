package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import techguns.entity.projectile.GenericBeamProjectile;
import techguns.entity.projectile.GenericProjectile;
import techguns.entity.projectile.RocketProjectile;
import techguns.entity.projectile.SonicShotgunProjectile;
import techguns.gun.GunHandType;
import techguns.gun.GunItem;
import techguns.gun.GunSounds;
import techguns.gun.GunStats;
import techguns.gun.HoldType;
import techguns.gun.ProjectileSelector;
import techguns.gun.SonicShotgunItem;
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
    private static final ProjectileSelector SHOTGUN_ROUNDS_PROJECTILES = new ProjectileSelector(TGAmmo.SHOTGUN_ROUNDS_TYPE, BULLET, BULLET_INCENDIARY);
    private static final ProjectileSelector AS50_MAG_PROJECTILES = new ProjectileSelector(TGAmmo.AS50_MAGAZINE_TYPE, BULLET, BULLET_INCENDIARY,
            new GenericProjectile.Factory(GenericProjectile.Kind.EXPLOSIVE));
    private static final ProjectileSelector ADVANCED_MAG_PROJECTILES = new ProjectileSelector(TGAmmo.ADVANCED_MAGAZINE_TYPE,
            new GenericProjectile.Factory(GenericProjectile.Kind.ADVANCED));
    private static final ProjectileSelector GAUSS_PROJECTILES = new ProjectileSelector(TGAmmo.GAUSS_RIFLE_TYPE,
            new GenericProjectile.Factory(GenericProjectile.Kind.GAUSS));
    private static final ProjectileSelector BLASTER_PROJECTILES = new ProjectileSelector(TGAmmo.ENERGY_CELL_TYPE,
            new GenericProjectile.Factory(GenericProjectile.Kind.BLASTER));
    private static final GenericBeamProjectile.Factory LASER_BEAM = new GenericBeamProjectile.Factory(GenericBeamProjectile.BeamType.LASER, 1, false);
    private static final ProjectileSelector LASER_PISTOL_PROJECTILES = new ProjectileSelector(TGAmmo.REDSTONE_BATTERY_TYPE, LASER_BEAM);
    private static final ProjectileSelector NDR_PROJECTILES = new ProjectileSelector(TGAmmo.NUCLEAR_POWER_CELL_TYPE,
            new GenericBeamProjectile.Factory(GenericBeamProjectile.BeamType.NDR, 15, true));
    private static final ProjectileSelector TESLA_PROJECTILES = new ProjectileSelector(TGAmmo.ENERGY_CELL_TYPE,
            new GenericBeamProjectile.Factory(GenericBeamProjectile.BeamType.TESLA, 1, false));
    private static final ProjectileSelector ALIEN_BLASTER_PROJECTILES = new ProjectileSelector(TGAmmo.ENERGY_CELL_TYPE,
            new GenericProjectile.Factory(GenericProjectile.Kind.ALIEN_BLASTER));
    private static final ProjectileSelector DEATOMIZER_PROJECTILES = new ProjectileSelector(TGAmmo.ENERGY_CELL_TYPE,
            new GenericProjectile.Factory(GenericProjectile.Kind.DEATOMIZER));
    private static final ProjectileSelector NETHER_BLASTER_PROJECTILES = new ProjectileSelector(TGAmmo.NETHER_CHARGE_TYPE,
            new GenericProjectile.Factory(GenericProjectile.Kind.NETHER_BLASTER));
    private static final ProjectileSelector SONIC_SHOTGUN_PROJECTILES = new ProjectileSelector(TGAmmo.ENERGY_CELL_TYPE,
            new SonicShotgunProjectile.Factory());
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

    // --- Batch 3b: shotguns, sniper, advanced and blaster guns (Fabric TGuns stats) ---
    public static final DeferredItem<GunItem> SAWED_OFF = gun("sawed_off",
            GunStats.builder(true, 4, 2, 28, 4.0f, 10, 0.01f)
                    .ammoPerReload(2).shotgun(7, 0.2f, false).damageDrop(1, 4, 1.5f).speed(1.5f).build(),
            GunSounds.of(TGSounds.SAWEDOFF_FIRE, TGSounds.SAWEDOFF_RELOAD), SHOTGUN_ROUNDS_PROJECTILES);

    public static final DeferredItem<GunItem> COMBAT_SHOTGUN = gun("combat_shotgun",
            GunStats.builder(true, 14, 8, 50, 4.0f, 15, 0.01f)
                    .ammoPerReload(8).shotgun(7, 0.15f, false).recoilTicks(12).speed(1.5f).damageDrop(2, 5, 1.5f)
                    .penetration(PENETRATION_LOW).build(),
            GunSounds.of(TGSounds.COMBATSHOTGUN_FIRE, TGSounds.COMBATSHOTGUN_RELOAD).withRechamber(TGSounds.COMBATSHOTGUN_RECHAMBER),
            SHOTGUN_ROUNDS_PROJECTILES);

    public static final DeferredItem<GunItem> AS50 = gun("as50",
            GunStats.builder(true, 10, 10, 80, 32.0f, MAX_RANGE_SNIPER, 0.0625f)
                    .damageDrop(40, 60, 24.0f).zoom(0.35f, true, 0.125f, true).speed(3.5f).recoilTicks(10)
                    .penetration(PENETRATION_MED_HIGH).build(),
            GunSounds.of(TGSounds.AS50_FIRE, TGSounds.AS50_RELOAD), AS50_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> GAUSS_RIFLE = gun("gauss_rifle",
            GunStats.builder(true, 30, 8, 60, 40.0f, MAX_RANGE_SNIPER, 0.025f)
                    .zoom(0.35f, true, 0.0f, true).speed(4.0f).recoilTicks(8).forwardOffset(0.45f)
                    .penetration(PENETRATION_MED_HIGH).build(),
            GunSounds.of(TGSounds.GAUSS_RIFLE_FIRE, TGSounds.GAUSS_RIFLE_RELOAD).withRechamber(TGSounds.GAUSS_RIFLE_RECHAMBER),
            GAUSS_PROJECTILES);

    public static final DeferredItem<GunItem> BLASTER_RIFLE = gun("blaster_rifle",
            GunStats.builder(false, 5, 50, 45, 10.0f, MAX_RANGE_RIFLE, 0.025f)
                    .zoom(0.5f, true, 0.75f, true).damageDrop(25, 35, 8.0f).penetration(PENETRATION_MED).build(),
            GunSounds.of(TGSounds.BLASTER_RIFLE_FIRE, TGSounds.LASERGUN_RELOAD), BLASTER_PROJECTILES);

    public static final DeferredItem<GunItem> BLASTER_SHOTGUN = gun("blaster_shotgun",
            // Fabric sets the bullet speed twice; the second call (2.0) wins
            GunStats.builder(false, 7, 40, 45, 6.0f, 30, 0.1f)
                    .shotgun(4, 0.15f, false).speed(2.0f).zoom(0.75f, true, 0.75f, false).build(),
            GunSounds.of(TGSounds.LASERGUN_FIRE, TGSounds.LASERGUN_RELOAD), BLASTER_PROJECTILES);

    public static final DeferredItem<GunItem> PULSE_RIFLE = gun("pulse_rifle",
            GunStats.builder(false, 7, 12, 45, 10.0f, MAX_RANGE_RIFLE_LONG, 0.024f)
                    .damageDrop(30, 45, 8.0f).zoom(0.35f, true, 0.5f, true).speed(3.25f).recoilTicks(8)
                    .penetration(PENETRATION_MED).shotgun(2, 0.015f, true).muzzleFlashTicks(4).build(),
            GunSounds.of(TGSounds.PULSE_RIFLE_FIRE, TGSounds.PULSE_RIFEL_RELOAD), ADVANCED_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> PDW = gun("pdw",
            GunStats.builder(false, 1, 40, 40, 5.0f, MAX_RANGE_PISTOL, 0.03f)
                    .damageDrop(18, 25, 3.0f).penetration(PENETRATION_MED).handType(GunHandType.ONE_POINT_FIVE_HANDED)
                    .muzzleFlashTicks(2).build(),
            GunSounds.of(TGSounds.PDW_FIRE, TGSounds.PDW_RELOAD), ADVANCED_MAG_PROJECTILES);

    // --- Batch 3c: energy guns (Fabric TGuns stats) ---
    public static final DeferredItem<GunItem> LASER_PISTOL = gun("laser_pistol",
            GunStats.builder(false, 6, 20, 40, 9.0f, MAX_RANGE_SNIPER, 0.025f)
                    .speed(100.0f).handType(GunHandType.ONE_HANDED).build(),
            GunSounds.of(TGSounds.LASER_PISTOL_FIRE, TGSounds.LASER_PISTOL_RELOAD), LASER_PISTOL_PROJECTILES);

    public static final DeferredItem<GunItem> NUCLEAR_DEATHRAY = gun("nuclear_deathray",
            properties -> new GunItem(properties,
                    GunStats.builder(false, 5, 40, 50, 6.0f, MAX_RANGE_SNIPER, 0.0f)
                            .recoilTicks(10).continuousAnimation().speed(100.0f).penetration(PENETRATION_MED).damageDrop(20, 40, 1.0f)
                            .build(),
                    GunSounds.of(TGSounds.BEAMGUN_FIRE, TGSounds.LASERGUN_RELOAD).withLoop(TGSounds.BEAMGUN_START, 10), NDR_PROJECTILES)
                    .withFireFx("BeamGunMuzzleFX", 0.16f, 0f, 0.8f));

    public static final DeferredItem<GunItem> TESLA_GUN = gun("tesla_gun",
            GunStats.builder(false, 8, 25, 45, 12.0f, MAX_RANGE_RIFLE, 0.0f)
                    .zoom(0.75f, true, 1.0f, false).speed(80.0f).muzzleFlashTicks(10).build(),
            GunSounds.of(TGSounds.TESLA_FIRE, TGSounds.TESLA_RELOAD), TESLA_PROJECTILES);

    public static final DeferredItem<GunItem> ALIEN_BLASTER = gun("alien_blaster",
            GunStats.builder(false, 8, 10, 35, 16.0f, MAX_RANGE_PISTOL, 0.0f)
                    .speed(1.0f).muzzleFlashTicks(10).penetration(PENETRATION_MED).handType(GunHandType.ONE_HANDED).build(),
            GunSounds.of(TGSounds.ALIENBLASTER_FIRE, TGSounds.ALIENBLASTER_RELOAD), ALIEN_BLASTER_PROJECTILES);

    public static final DeferredItem<GunItem> MIB_GUN = gun("mib_gun",
            GunStats.builder(true, 8, 20, 45, 16.0f, MAX_RANGE_PISTOL, 0.035f)
                    .damageDrop(20, 30, 8.0f).speed(1.5f).penetration(PENETRATION_MED).handType(GunHandType.ONE_HANDED).build(),
            GunSounds.of(TGSounds.MIBGUN_FIRE, TGSounds.MIBGUN_RELOAD), DEATOMIZER_PROJECTILES);

    public static final DeferredItem<GunItem> NETHER_BLASTER = gun("nether_blaster",
            GunStats.builder(false, 8, 10, 35, 14.0f, MAX_RANGE_RIFLE, 0.0f)
                    .speed(1.5f).muzzleFlashTicks(10).penetration(PENETRATION_LOW).damageDrop(15, 30, 8.0f)
                    .handType(GunHandType.ONE_POINT_FIVE_HANDED).build(),
            GunSounds.of(TGSounds.NETHERBLASTER_FIRE, TGSounds.NETHERBLASTER_RELOAD), NETHER_BLASTER_PROJECTILES);

    public static final DeferredItem<GunItem> SONIC_SHOTGUN = gun("sonic_shotgun",
            properties -> new SonicShotgunItem(properties,
                    GunStats.builder(true, 12, 8, 40, 25.0f, 20, 0.0f)
                            .speed(0.75f).gravity(0.0).damageDrop(5, 15, 5.0f).penetration(PENETRATION_MED).build(),
                    GunSounds.of(TGSounds.SONIC_SHOTGUN_FIRE, TGSounds.SONIC_SHOTGUN_RELOAD), SONIC_SHOTGUN_PROJECTILES));

    private TGGuns() {
    }

    private static DeferredItem<GunItem> gun(String name, GunStats stats, GunSounds sounds, ProjectileSelector projectiles) {
        return gun(name, properties -> new GunItem(properties, stats, sounds, projectiles));
    }

    /** Guns with their own item class or extra item setup. */
    private static DeferredItem<GunItem> gun(String name, Function<Item.Properties, GunItem> factory) {
        DeferredItem<GunItem> gun = TGItems.ITEMS.register(name, () -> factory.apply(new Item.Properties()));
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
