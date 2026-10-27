package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.minecraft.ChatFormatting;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import techguns.entity.projectile.GenericBeamProjectile;
import techguns.core.damage.DeathType;
import techguns.entity.projectile.BioGunProjectile;
import techguns.entity.projectile.ChainsawProjectile;
import techguns.entity.projectile.GenericProjectile;
import techguns.entity.projectile.GrapplingHookProjectile;
import techguns.entity.projectile.GrenadeProjectile;
import techguns.entity.projectile.GuidedMissileProjectile;
import techguns.entity.projectile.MagicRifleProjectile;
import techguns.entity.projectile.RocketProjectile;
import techguns.entity.projectile.SonicShotgunProjectile;
import techguns.entity.projectile.TFGProjectile;
import techguns.gun.GunHandType;
import techguns.gun.GunItem;
import techguns.gun.GunSounds;
import techguns.gun.ChargeGunItem;
import techguns.gun.GrapplingHookItem;
import techguns.gun.GrenadeItem;
import techguns.gun.GunStats;
import techguns.gun.HoldType;
import techguns.gun.LockOnGunItem;
import techguns.gun.MeleeToolItem;
import techguns.gun.ProjectileSelector;
import techguns.gun.SonicShotgunItem;
import techguns.gun.ammo.DamageModifier;

/**
 * All guns. Stats use the Techguns 2 values; see {@link GunStats#builder} for the parameter order.
 */
public final class TGGuns {
    /** Fabric {@code TGuns} AI attack ranges. */
    private static final float RANGE_MELEE = 3.0f;
    private static final float RANGE_CLOSE = 12.0f;
    private static final float RANGE_SHORT = 18.0f;
    private static final float RANGE_MEDIUM = 24.0f;
    private static final float RANGE_FAR = 36.0f;

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
    private static final ProjectileSelector ROCKET_PROJECTILES = new ProjectileSelector(TGAmmo.ROCKETS_TYPE, new RocketProjectile.Factory(),
            new RocketProjectile.Factory(RocketProjectile.Variant.NUKE, DamageModifier.ROCKET_NUKE),
            new RocketProjectile.Factory(RocketProjectile.Variant.HIGH_VELOCITY, DamageModifier.ROCKET_HIGH_VELOCITY));
    // Techguns 2 guided missiles; Fabric left the high velocity one as a TODO
    private static final ProjectileSelector GUIDED_MISSILE_PROJECTILES = new ProjectileSelector(TGAmmo.ROCKETS_NO_NUKES_TYPE,
            new GuidedMissileProjectile.Factory(), new GuidedMissileProjectile.Factory(DamageModifier.ROCKET_HIGH_VELOCITY));
    private static final ProjectileSelector FLAMETHROWER_PROJECTILES = new ProjectileSelector(TGAmmo.FUEL_TANK_TYPE,
            new GenericProjectile.Factory(GenericProjectile.Kind.FLAME));
    private static final ProjectileSelector STONE_BULLET_PROJECTILES = new ProjectileSelector(TGAmmo.STONE_BULLETS_TYPE,
            new GenericProjectile.Factory(GenericProjectile.Kind.STONE));
    private static final ProjectileSelector BIOGUN_PROJECTILES = new ProjectileSelector(TGAmmo.BIO_TANK_TYPE, new BioGunProjectile.Factory());
    private static final ProjectileSelector TFG_PROJECTILES = new ProjectileSelector(TGAmmo.NUCLEAR_POWER_CELL_TYPE, new TFGProjectile.Factory());
    private static final ProjectileSelector MAGIC_RIFLE_PROJECTILES = new ProjectileSelector(TGAmmo.MAGIC_BULLETS_TYPE,
            new MagicRifleProjectile.Factory(MagicRifleProjectile.Type.ARCANE), new MagicRifleProjectile.Factory(MagicRifleProjectile.Type.FIRE),
            new MagicRifleProjectile.Factory(MagicRifleProjectile.Type.LIGHTNING));
    private static final ProjectileSelector CHAINSAW_PROJECTILES = new ProjectileSelector(TGAmmo.FUEL_TANK_TYPE,
            new ChainsawProjectile.Factory(ChainsawProjectile.Type.CHAINSAW));
    private static final ProjectileSelector POWER_HAMMER_PROJECTILES = new ProjectileSelector(TGAmmo.COMPRESSED_AIR_TANK_TYPE,
            new ChainsawProjectile.Factory(ChainsawProjectile.Type.POWER_HAMMER));
    private static final ProjectileSelector GRAPPLING_HOOK_PROJECTILES = new ProjectileSelector(TGAmmo.COMPRESSED_AIR_TANK_TYPE,
            new GrapplingHookProjectile.Factory());
    private static final ProjectileSelector GRENADE_40MM_PROJECTILES = new ProjectileSelector(TGAmmo.GRENADES_40MM_TYPE,
            new GrenadeProjectile.Factory(GrenadeProjectile.Type.LAUNCHER_40MM));

    // --- Guns ---
    public static final DeferredItem<GunItem> M4 = gun("m4",
            GunStats.builder(false, 3, 30, 45, 8.0f, MAX_RANGE_RIFLE, 0.05f)
                    .speed(3.0f).zoom(0.75f, true, 0.75f, false).damageDrop(25, 40, 6.0f)
                    .penetration(PENETRATION_LOW).muzzleFlashTicks(4).ai(RANGE_MEDIUM, 30, 3, 3).build(),
            GunSounds.of(TGSounds.M4_FIRE, TGSounds.M4_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    // --- Batch 3a: bullet guns (Fabric TGuns stats) ---
    public static final DeferredItem<GunItem> REVOLVER = gun("revolver",
            GunStats.builder(true, 6, 6, 45, 8.0f, MAX_RANGE_PISTOL, 0.025f)
                    .damageDrop(12, 20, 6.0f).speed(2.0f).recoilTicks(6).handType(GunHandType.ONE_HANDED).ai(RANGE_SHORT, 90, 6, 20).build(),
            GunSounds.of(TGSounds.REVOLVER_FIRE, TGSounds.REVOLVER_RELOAD), PISTOL_ROUNDS_PROJECTILES);

    public static final DeferredItem<GunItem> GOLDEN_REVOLVER = gun("golden_revolver",
            GunStats.builder(true, 8, 6, 45, 14.0f, MAX_RANGE_PISTOL, 0.015f)
                    .damageDrop(12, 30, 6.0f).recoilTicks(8).speed(1.75f).handType(GunHandType.ONE_HANDED).ai(RANGE_MEDIUM, 60, 6, 15).build(),
            GunSounds.of(TGSounds.REVOLVER_GOLDEN_FIRE, TGSounds.REVOLVER_RELOAD), PISTOL_ROUNDS_PROJECTILES);

    public static final DeferredItem<GunItem> PISTOL = gun("pistol",
            // Fabric calls setDamageDrop twice; the second call wins
            GunStats.builder(true, 4, 18, 35, 8.0f, MAX_RANGE_PISTOL, 0.025f)
                    .speed(2.0f).damageDrop(18, 25, 5.0f).handType(GunHandType.ONE_HANDED).recoilTicks(3).ai(RANGE_MEDIUM, 30, 3, 10).build(),
            GunSounds.of(TGSounds.PISTOL_FIRE, TGSounds.PISTOL_RELOAD), PISTOL_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> AK47 = gun("ak47",
            GunStats.builder(false, 3, 30, 45, 9.0f, MAX_RANGE_RIFLE, 0.030f)
                    .damageDrop(20, 30, 5.0f).penetration(PENETRATION_LOW).speed(2.5f).muzzleFlashTicks(4).ai(RANGE_MEDIUM, 30, 3, 3).build(),
            GunSounds.of(TGSounds.AK_FIRE, TGSounds.AK_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> M4_INFILTRATOR = gun("m4_infiltrator",
            GunStats.builder(false, 3, 30, 45, 8.0f, MAX_RANGE_RIFLE, 0.010f)
                    .zoom(0.50f, true, 0.5f, true).damageDrop(25, 35, 6.0f).speed(2.0f).silenced()
                    .penetration(PENETRATION_LOW).ai(RANGE_MEDIUM, 30, 3, 3).build(),
            GunSounds.of(TGSounds.M4_SILENCED_FIRE, TGSounds.M4_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> AUG = gun("aug",
            GunStats.builder(false, 3, 30, 45, 8.0f, MAX_RANGE_RIFLE, 0.010f)
                    .speed(3.25f).zoom(0.50f, true, 0.5f, true).damageDrop(30, 45, 7.0f).penetration(PENETRATION_LOW)
                    .muzzleFlashTicks(4).ai(RANGE_MEDIUM, 30, 3, 3).build(),
            GunSounds.of(TGSounds.AUG_FIRE, TGSounds.AUG_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> SCAR = gun("scar",
            GunStats.builder(false, 4, 20, 45, 12.0f, MAX_RANGE_RIFLE_LONG, 0.015f)
                    .zoom(0.65f, true, 0.5f, true).damageDrop(35, 60, 10.0f).penetration(PENETRATION_MED).speed(3.25f)
                    .muzzleFlashTicks(5).ai(RANGE_MEDIUM, 30, 5, 2).build(),
            GunSounds.of(TGSounds.SCAR_FIRE, TGSounds.SCAR_RELOAD), ASSAULT_RIFLE_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> THOMPSON = gun("thompson",
            GunStats.builder(false, 3, 20, 40, 5.0f, MAX_RANGE_PISTOL, 0.05f)
                    .speed(1.75f).damageDrop(15, 24, 3.0f).muzzleFlashTicks(4).ai(RANGE_SHORT, 45, 3, 3).build(),
            GunSounds.of(TGSounds.THOMPSON_FIRE, TGSounds.THOMSPON_RELOAD), SMG_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> MAC10 = gun("mac10",
            GunStats.builder(false, 2, 32, 40, 5.0f, MAX_RANGE_PISTOL, 0.05f)
                    .damageDrop(15, 24, 3.0f).recoilTicks(2).muzzleFlashTicks(3).speed(2.0f)
                    .handType(GunHandType.ONE_POINT_FIVE_HANDED).ai(RANGE_SHORT, 35, 3, 2).build(),
            GunSounds.of(TGSounds.MAC10_FIRE, TGSounds.M4_RELOAD), SMG_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> VECTOR = gun("vector",
            GunStats.builder(false, 2, 25, 40, 6.0f, MAX_RANGE_PISTOL, 0.05f)
                    .zoom(0.75f, true, 0.35f, false).damageDrop(17, 25, 4.0f).speed(2.0f).recoilTicks(2)
                    .muzzleFlashTicks(3).penetration(PENETRATION_LOW).ai(RANGE_SHORT, 35, 3, 2).build(),
            GunSounds.of(TGSounds.VECTOR_FIRE, TGSounds.VECTOR_RELOAD), SMG_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> LMG = gun("lmg",
            GunStats.builder(false, 2, 100, 100, 8.0f, MAX_RANGE_RIFLE_LONG, 0.020f)
                    .zoom(0.75f, true, 0.75f, false).damageDrop(40, 60, 6.0f).penetration(PENETRATION_LOW).speed(3.0f)
                    .muzzleFlashTicks(2).recoilTicks(3).ai(RANGE_MEDIUM, 40, 6, 3).build(),
            GunSounds.of(TGSounds.LMG_FIRE, TGSounds.LMG_RELOAD), LMG_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> BOLT_ACTION = gun("bolt_action",
            GunStats.builder(true, 25, 6, 50, 16.0f, MAX_RANGE_SNIPER, 0.05f)
                    .zoom(0.35f, true, 0.125f, true).speed(3.25f).recoilTicks(15).damageDrop(40, 60, 10.0f)
                    .penetration(PENETRATION_LOW).ai(RANGE_FAR, 60, 0, 0).build(),
            GunSounds.of(TGSounds.BOLT_ACTION_FIRE, TGSounds.BOLT_ACTION_RELOAD).withRechamber(TGSounds.BOLT_ACTION_RECHAMBER),
            RIFLE_ROUNDS_PROJECTILES);

    public static final DeferredItem<GunItem> MINIGUN = gun("minigun",
            GunStats.builder(false, 0, 200, 100, 5.0f, MAX_RANGE_RIFLE_LONG, 0.025f)
                    .damageDrop(30, 50, 3.0f).penetration(PENETRATION_LOW).speed(3.0f).muzzleFlashTicks(4)
                    .continuousAnimation().ai(RANGE_MEDIUM, 40, 10, 1).build(),
            GunSounds.of(TGSounds.MINIGUN_FIRE, TGSounds.MINIGUN_RELOAD), MINIGUN_DRUM_PROJECTILES);

    public static final DeferredItem<GunItem> LASERGUN = gun("lasergun",
            GunStats.builder(false, 5, 45, 45, 12.0f, MAX_RANGE_SNIPER, 0.0f)
                    .speed(100.0f).zoom(0.75f, true, 0.75f, false).ai(RANGE_MEDIUM, 30, 0, 0).build(),
            GunSounds.of(TGSounds.LASERGUN_FIRE, TGSounds.LASERGUN_RELOAD), LASERGUN_PROJECTILES);

    public static final DeferredItem<GunItem> ROCKET_LAUNCHER = gun("rocket_launcher",
            GunStats.builder(true, 10, 1, 40, 50.0f, 200, 0.05f)
                    .gravity(0.01).speed(1.0f).recoilTicks(10).damageDrop(2.0f, 4.0f, 10f).forwardOffset(0.35f)
                    .holdType(HoldType.BOW).ai(RANGE_MEDIUM, 80, 0, 0).build(),
            GunSounds.of(TGSounds.ROCKET_FIRE, TGSounds.ROCKET_RELOAD), ROCKET_PROJECTILES);

    // --- Batch 3b: shotguns, sniper, advanced and blaster guns (Fabric TGuns stats) ---
    public static final DeferredItem<GunItem> SAWED_OFF = gun("sawed_off",
            GunStats.builder(true, 4, 2, 28, 4.0f, 10, 0.01f)
                    .ammoPerReload(2).shotgun(7, 0.2f, false).damageDrop(1, 4, 1.5f).speed(1.5f).ai(RANGE_CLOSE, 60, 2, 20).build(),
            GunSounds.of(TGSounds.SAWEDOFF_FIRE, TGSounds.SAWEDOFF_RELOAD), SHOTGUN_ROUNDS_PROJECTILES);

    public static final DeferredItem<GunItem> COMBAT_SHOTGUN = gun("combat_shotgun",
            GunStats.builder(true, 14, 8, 50, 4.0f, 15, 0.01f)
                    .ammoPerReload(8).shotgun(7, 0.15f, false).recoilTicks(12).speed(1.5f).damageDrop(2, 5, 1.5f)
                    .penetration(PENETRATION_LOW).ai(RANGE_CLOSE, 30, 0, 0).build(),
            GunSounds.of(TGSounds.COMBATSHOTGUN_FIRE, TGSounds.COMBATSHOTGUN_RELOAD).withRechamber(TGSounds.COMBATSHOTGUN_RECHAMBER),
            SHOTGUN_ROUNDS_PROJECTILES);

    public static final DeferredItem<GunItem> AS50 = gun("as50",
            GunStats.builder(true, 10, 10, 80, 32.0f, MAX_RANGE_SNIPER, 0.0625f)
                    .damageDrop(40, 60, 24.0f).zoom(0.35f, true, 0.125f, true).speed(3.5f).recoilTicks(10)
                    .penetration(PENETRATION_MED_HIGH).ai(RANGE_FAR, 30, 0, 0).build(),
            GunSounds.of(TGSounds.AS50_FIRE, TGSounds.AS50_RELOAD), AS50_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> GAUSS_RIFLE = gun("gauss_rifle",
            GunStats.builder(true, 30, 8, 60, 40.0f, MAX_RANGE_SNIPER, 0.025f)
                    .zoom(0.35f, true, 0.0f, true).speed(4.0f).recoilTicks(8).forwardOffset(0.45f)
                    .penetration(PENETRATION_MED_HIGH).ai(RANGE_FAR, 30, 0, 0).build(),
            GunSounds.of(TGSounds.GAUSS_RIFLE_FIRE, TGSounds.GAUSS_RIFLE_RELOAD).withRechamber(TGSounds.GAUSS_RIFLE_RECHAMBER),
            GAUSS_PROJECTILES);

    public static final DeferredItem<GunItem> BLASTER_RIFLE = gun("blaster_rifle",
            GunStats.builder(false, 5, 50, 45, 10.0f, MAX_RANGE_RIFLE, 0.025f)
                    .zoom(0.5f, true, 0.75f, true).damageDrop(25, 35, 8.0f).penetration(PENETRATION_MED).ai(RANGE_MEDIUM, 30, 5, 3).build(),
            GunSounds.of(TGSounds.BLASTER_RIFLE_FIRE, TGSounds.LASERGUN_RELOAD), BLASTER_PROJECTILES);

    public static final DeferredItem<GunItem> BLASTER_SHOTGUN = gun("blaster_shotgun",
            // Fabric sets the bullet speed twice; the second call (2.0) wins
            GunStats.builder(false, 7, 40, 45, 6.0f, 30, 0.1f)
                    .shotgun(4, 0.15f, false).speed(2.0f).zoom(0.75f, true, 0.75f, false).ai(RANGE_SHORT, 30, 0, 0).build(),
            GunSounds.of(TGSounds.LASERGUN_FIRE, TGSounds.LASERGUN_RELOAD), BLASTER_PROJECTILES);

    public static final DeferredItem<GunItem> PULSE_RIFLE = gun("pulse_rifle",
            GunStats.builder(false, 7, 12, 45, 10.0f, MAX_RANGE_RIFLE_LONG, 0.024f)
                    .damageDrop(30, 45, 8.0f).zoom(0.35f, true, 0.5f, true).speed(3.25f).recoilTicks(8)
                    .penetration(PENETRATION_MED).shotgun(2, 0.015f, true).muzzleFlashTicks(4).ai(RANGE_MEDIUM, 30, 0, 0).build(),
            GunSounds.of(TGSounds.PULSE_RIFLE_FIRE, TGSounds.PULSE_RIFEL_RELOAD), ADVANCED_MAG_PROJECTILES);

    public static final DeferredItem<GunItem> PDW = gun("pdw",
            GunStats.builder(false, 1, 40, 40, 5.0f, MAX_RANGE_PISTOL, 0.03f)
                    .damageDrop(18, 25, 3.0f).penetration(PENETRATION_MED).handType(GunHandType.ONE_POINT_FIVE_HANDED)
                    .muzzleFlashTicks(2).ai(RANGE_SHORT, 30, 4, 2).build(),
            GunSounds.of(TGSounds.PDW_FIRE, TGSounds.PDW_RELOAD), ADVANCED_MAG_PROJECTILES);

    // --- Batch 3c: energy guns (Fabric TGuns stats) ---
    public static final DeferredItem<GunItem> LASER_PISTOL = gun("laser_pistol",
            GunStats.builder(false, 6, 20, 40, 9.0f, MAX_RANGE_SNIPER, 0.025f)
                    .speed(100.0f).handType(GunHandType.ONE_HANDED).ai(RANGE_MEDIUM, 30, 0, 0).build(),
            GunSounds.of(TGSounds.LASER_PISTOL_FIRE, TGSounds.LASER_PISTOL_RELOAD), LASER_PISTOL_PROJECTILES);

    public static final DeferredItem<GunItem> NUCLEAR_DEATHRAY = gun("nuclear_deathray",
            properties -> new GunItem(properties,
                    GunStats.builder(false, 5, 40, 50, 6.0f, MAX_RANGE_SNIPER, 0.0f)
                            .recoilTicks(10).continuousAnimation().speed(100.0f).penetration(PENETRATION_MED).damageDrop(20, 40, 1.0f)
                            .ai(RANGE_MEDIUM, 40, 5, 5).build(),
                    GunSounds.of(TGSounds.BEAMGUN_FIRE, TGSounds.LASERGUN_RELOAD).withLoop(TGSounds.BEAMGUN_START, 10), NDR_PROJECTILES)
                    .withFireFx("BeamGunMuzzleFX", 0.16f, 0f, 0.8f));

    public static final DeferredItem<GunItem> TESLA_GUN = gun("tesla_gun",
            GunStats.builder(false, 8, 25, 45, 12.0f, MAX_RANGE_RIFLE, 0.0f)
                    .zoom(0.75f, true, 1.0f, false).speed(80.0f).muzzleFlashTicks(10).ai(RANGE_MEDIUM, 30, 0, 0).build(),
            GunSounds.of(TGSounds.TESLA_FIRE, TGSounds.TESLA_RELOAD), TESLA_PROJECTILES);

    public static final DeferredItem<GunItem> ALIEN_BLASTER = gun("alien_blaster",
            GunStats.builder(false, 8, 10, 35, 16.0f, MAX_RANGE_PISTOL, 0.0f)
                    .speed(1.0f).muzzleFlashTicks(10).penetration(PENETRATION_MED).handType(GunHandType.ONE_HANDED).ai(RANGE_MEDIUM, 40, 0, 0).build(),
            GunSounds.of(TGSounds.ALIENBLASTER_FIRE, TGSounds.ALIENBLASTER_RELOAD), ALIEN_BLASTER_PROJECTILES);

    public static final DeferredItem<GunItem> MIB_GUN = gun("mib_gun",
            GunStats.builder(true, 8, 20, 45, 16.0f, MAX_RANGE_PISTOL, 0.035f)
                    .damageDrop(20, 30, 8.0f).speed(1.5f).penetration(PENETRATION_MED).handType(GunHandType.ONE_HANDED).ai(RANGE_MEDIUM, 60, 0, 0).build(),
            GunSounds.of(TGSounds.MIBGUN_FIRE, TGSounds.MIBGUN_RELOAD), DEATOMIZER_PROJECTILES);

    public static final DeferredItem<GunItem> NETHER_BLASTER = gun("nether_blaster",
            GunStats.builder(false, 8, 10, 35, 14.0f, MAX_RANGE_RIFLE, 0.0f)
                    .speed(1.5f).muzzleFlashTicks(10).penetration(PENETRATION_LOW).damageDrop(15, 30, 8.0f)
                    .handType(GunHandType.ONE_POINT_FIVE_HANDED).ai(RANGE_MEDIUM, 40, 0, 0).build(),
            GunSounds.of(TGSounds.NETHERBLASTER_FIRE, TGSounds.NETHERBLASTER_RELOAD), NETHER_BLASTER_PROJECTILES);

    public static final DeferredItem<GunItem> SONIC_SHOTGUN = gun("sonic_shotgun",
            properties -> new SonicShotgunItem(properties,
                    GunStats.builder(true, 12, 8, 40, 25.0f, 20, 0.0f)
                            .speed(0.75f).gravity(0.0).damageDrop(5, 15, 5.0f).penetration(PENETRATION_MED).ai(RANGE_SHORT, 40, 0, 0).build(),
                    GunSounds.of(TGSounds.SONIC_SHOTGUN_FIRE, TGSounds.SONIC_SHOTGUN_RELOAD), SONIC_SHOTGUN_PROJECTILES));

    // --- Batch 3d: explosives ---
    public static final DeferredItem<GunItem> GRENADE_LAUNCHER = gun("grenade_launcher",
            GunStats.builder(true, 5, 6, 100, 30.0f, 160, 0.015f)
                    .speed(1.0f).ammoPerReload(6).damageDrop(1.5f, 3.0f, 12.0f).gravity(0.02).ai(RANGE_MEDIUM, 40, 3, 20).build(),
            GunSounds.of(TGSounds.GRENADE_LAUNCHER_FIRE, TGSounds.GRENADE_LAUNCHER_RELOAD), GRENADE_40MM_PROJECTILES);

    public static final DeferredItem<GunItem> GUIDED_MISSILE_LAUNCHER = gun("guided_missile_launcher",
            properties -> new LockOnGunItem(properties,
                    GunStats.builder(true, 10, 1, 40, 50.0f, 100, 0.05f)
                            .speed(1.0f).recoilTicks(10).damageDrop(2.0f, 4.0f, 20.0f).holdType(HoldType.BOW).ai(RANGE_MEDIUM, 80, 0, 0).build(),
                    GunSounds.of(TGSounds.GUIDEDMISSILE_FIRE, TGSounds.ROCKET_RELOAD), GUIDED_MISSILE_PROJECTILES, 20, 80));

    public static final DeferredItem<GunItem> GRIM_REAPER = gun("grim_reaper",
            properties -> new LockOnGunItem(properties,
                    GunStats.builder(false, 6, 4, 40, 50.0f, 200, 0.05f)
                            .speed(1.0f).recoilTicks(10).ammoPerReload(4).damageDrop(3.0f, 5.0f, 10.0f).holdType(HoldType.BOW).ai(RANGE_MEDIUM, 120, 4, 30).build(),
                    GunSounds.of(TGSounds.GUIDEDMISSILE_FIRE, TGSounds.ROCKET_RELOAD), GUIDED_MISSILE_PROJECTILES, 20, 80));

    // --- Batch 3e: charge and fuel weapons ---
    public static final DeferredItem<GunItem> FLAMETHROWER = gun("flamethrower",
            properties -> new GunItem(properties,
                    GunStats.builder(false, 2, 100, 45, 5.0f, 16, 0.05f)
                            .speed(0.5f).gravity(0.01).damageDrop(4, 16, 2.0f).recoilTicks(10).muzzleFlashTicks(10).continuousAnimation()
                            .forwardOffset(0.35f).ai(RANGE_CLOSE, 20, 5, 5).build(),
                    GunSounds.of(TGSounds.FLAMETHROWER_FIRE, TGSounds.FLAMETHROWER_RELOAD).withLoop(TGSounds.FLAMETHROWER_START, 10),
                    FLAMETHROWER_PROJECTILES)
                    .withFireFx("FlamethrowerFireFX", 0.15f, 0.05f, 0.5f));

    public static final DeferredItem<GunItem> BIOGUN = gun("biogun",
            properties -> new ChargeGunItem(properties,
                    GunStats.builder(false, 6, 30, 45, 10.0f, MAX_RANGE_PISTOL, 0.015f)
                            .speed(0.75f).gravity(0.01).penetration(PENETRATION_LOW).damageDrop(8, 15, 8.0f).forwardOffset(0.40f).ai(RANGE_SHORT, 30, 0, 0).build(),
                    GunSounds.of(TGSounds.BIOGUN_FIRE, TGSounds.BIOGUN_RELOAD), BIOGUN_PROJECTILES, 30.0f, 3)
                    .withChargeSound(TGSounds.BIOGUN_CHARGE).withChargeFx("biogunCharge", -0.12f, -0.07f, 0.27f));

    public static final DeferredItem<GunItem> TFG = gun("tfg",
            properties -> new ChargeGunItem(properties,
                    GunStats.builder(false, 5, 20, 45, 50.0f, 100, 0.015f)
                            .speed(2.0f).penetration(PENETRATION_MED_HIGH).damageDrop(8, 15, 15.0f).muzzleFlashTicks(10).recoilTicks(10)
                            .forwardOffset(0.40f).ai(RANGE_SHORT, 30, 0, 0).build(),
                    GunSounds.of(TGSounds.TFG_FIRE, TGSounds.BIOGUN_RELOAD), TFG_PROJECTILES, 60.0f, 10)
                    .withChargeSound(TGSounds.TFG_CHARGE).withChargeFx("TFGChargeStart", -0.14f, -0.10f, 0.42f));

    // Fabric only; its charge FX "tfgcharge" does not exist in any FX file, so there is none
    public static final DeferredItem<GunItem> MAGIC_RIFLE = gun("magic_rifle",
            properties -> new ChargeGunItem(properties,
                    GunStats.builder(true, 6, 1, 2, 25.0f, MAX_RANGE_RIFLE, 0.05f)
                            .speed(2.5f).damageDrop(40, 60, 10.0f).penetration(PENETRATION_LOW).ai(RANGE_FAR, 60, 0, 0).build(),
                    GunSounds.of(TGSounds.HANDGUN_FIRE, TGSounds.HANDGUN_RELOAD), MAGIC_RIFLE_PROJECTILES, 60.0f, 1)
                    .withChargeSound(TGSounds.TFG_CHARGE));

    public static final DeferredItem<GunItem> HAND_CANNON = gun("hand_cannon",
            GunStats.builder(true, 12, 1, 30, 8.0f, 25, 0.035f)
                    .speed(1.0f).gravity(0.015).damageDrop(10, 25, 5.0f).recoilTicks(12).ai(RANGE_CLOSE, 60, 0, 0).build(),
            GunSounds.of(TGSounds.HANDGUN_FIRE, TGSounds.HANDGUN_RELOAD), STONE_BULLET_PROJECTILES);

    // --- Batch 3f: tools (Techguns 2 values where Fabric differs: tool levels and the sword attack speed) ---
    public static final DeferredItem<GunItem> CHAINSAW = gun("chainsaw",
            properties -> new MeleeToolItem(properties,
                    GunStats.builder(false, 3, 300, 45, 10.0f, 2, 0.0f).recoilTicks(5).penetration(PENETRATION_MED).ai(RANGE_MELEE, 10, 0, 0).build(),
                    GunSounds.of(TGSounds.CHAINSAW_LOOP, TGSounds.POWERHAMMER_RELOAD).withLoop(TGSounds.CHAINSAW_LOOP_START, 10),
                    CHAINSAW_PROJECTILES, 1.0f, 1, 12.0f, 2.0f, 14.0f,
                    List.of(new MeleeToolItem.MiningHead(TGItems.CHAINSAW_BLADES_STEEL, ChatFormatting.GRAY),
                            new MeleeToolItem.MiningHead(TGItems.CHAINSAW_BLADES_OBSIDIAN, 1, 1.0, 1.0f, ChatFormatting.DARK_PURPLE),
                            new MeleeToolItem.MiningHead(TGItems.CHAINSAW_BLADES_CARBON, 2, 2.0, 2.0f, ChatFormatting.DARK_GRAY)),
                    MeleeToolItem.UseAction.SHOOT)
                    .toolLevel(BlockTags.MINEABLE_WITH_AXE, 3).withHitSound(TGSounds.CHAINSAW_SWING));

    public static final DeferredItem<GunItem> MINING_DRILL = gun("mining_drill",
            properties -> new MeleeToolItem(properties,
                    GunStats.builder(false, 3, 300, 45, 10.0f, 2, 0.0f).recoilTicks(5).penetration(PENETRATION_MED).ai(RANGE_MELEE, 10, 0, 0).build(),
                    GunSounds.of(TGSounds.DRILLER_LOOP, TGSounds.POWERHAMMER_RELOAD).withLoop(TGSounds.DRILLER_SWING, 10),
                    CHAINSAW_PROJECTILES, 1.0f, 1, 12.0f, 2.0f, 14.0f,
                    List.of(new MeleeToolItem.MiningHead(TGItems.MINING_DRILL_HEAD_OBSIDIAN, ChatFormatting.DARK_PURPLE),
                            new MeleeToolItem.MiningHead(TGItems.MINING_DRILL_HEAD_CARBON, 1, 1.0, 0.5f, ChatFormatting.DARK_GRAY)),
                    MeleeToolItem.UseAction.SHOOT)
                    .toolLevel(BlockTags.MINEABLE_WITH_PICKAXE, 3).toolLevel(BlockTags.MINEABLE_WITH_SHOVEL, 3).miningRadius(1)
                    .meleeDeath(DeathType.GORE, 0.5f).withHitSound(TGSounds.DRILLER_HIT));

    public static final DeferredItem<GunItem> POWER_HAMMER = gun("power_hammer",
            properties -> new MeleeToolItem(properties,
                    GunStats.builder(false, 4, 300, 45, 3.5f, 3, 0.0f).speed(1.0f).recoilTicks(12).damageDrop(3, 3, 2.5f).ai(RANGE_MELEE, 30, 0, 0).build(),
                    GunSounds.of(TGSounds.POWERHAMMER_FIRE, TGSounds.POWERHAMMER_RELOAD), POWER_HAMMER_PROJECTILES, 20.0f, 5, 6.0f, 2.0f, 12.0f,
                    List.of(new MeleeToolItem.MiningHead(TGItems.POWER_HAMMER_HEAD_STEEL, ChatFormatting.GRAY),
                            new MeleeToolItem.MiningHead(TGItems.POWER_HAMMER_HEAD_OBSIDIAN, 1, 1.0, 0.5f, ChatFormatting.DARK_PURPLE),
                            new MeleeToolItem.MiningHead(TGItems.POWER_HAMMER_HEAD_CARBON, 2, 2.0, 1.0f, ChatFormatting.DARK_GRAY)),
                    MeleeToolItem.UseAction.CHARGE)
                    .toolLevel(BlockTags.MINEABLE_WITH_PICKAXE, 2).toolLevel(BlockTags.MINEABLE_WITH_SHOVEL, 2)
                    .withHitSound(TGSounds.POWERHAMMER_IMPACT).withChargeSound(TGSounds.POWERHAMMER_CHARGE));

    public static final DeferredItem<GunItem> SHISHKEBAP = gun("shishkebap",
            properties -> new MeleeToolItem(properties,
                    GunStats.builder(false, 3, 300, 50, 12.0f, 2, 0.0f).recoilTicks(5).penetration(PENETRATION_MED).build(),
                    GunSounds.of(TGSounds.SHISHKEBAP_SWING, TGSounds.SHISHKEBAP_RELOAD), CHAINSAW_PROJECTILES, 1.0f, 1, 14.0f, 2.0f, 4.0f,
                    List.of(), MeleeToolItem.UseAction.NONE)
                    .toolLevel(BlockTags.SWORD_EFFICIENT, 3).meleeDeath(DeathType.GORE, 0.25f).meleeIgnites());

    // Fabric GrapplingHook: the use key shoots the hook at once and holding it keeps pulling
    public static final DeferredItem<GunItem> GRAPPLING_HOOK = gun("grappling_hook",
            properties -> new GrapplingHookItem(properties,
                    GunStats.builder(true, 6, 100, 25, 1.0f, MAX_RANGE_RIFLE, 0.0f)
                            .handType(GunHandType.ONE_HANDED).speed(2.5f).gravity(0.005).penetration(PENETRATION_LOW).damageDrop(8, 15, 0.5f)
                            .forwardOffset(0.40f).ai(RANGE_SHORT, 30, 0, 0).build(),
                    GunSounds.of(TGSounds.GRAPPLING_HOOK_FIRE, TGSounds.POWERHAMMER_RELOAD), GRAPPLING_HOOK_PROJECTILES, 30.0f, 1));

    // Hand grenades (Techguns 2 only): damage, full-damage radius, minimum damage, outer radius
    public static final DeferredItem<GrenadeItem> STIELGRANATE = TGItems.ITEMS.register("stielgranate",
            () -> new GrenadeItem(new Item.Properties().stacksTo(16), GrenadeProjectile.Type.STIELGRANATE, 10.0f, 3.0f, 5.0f, 5.0f, null));
    public static final DeferredItem<GrenadeItem> FRAG_GRENADE = TGItems.ITEMS.register("frag_grenade",
            () -> new GrenadeItem(new Item.Properties().stacksTo(16), GrenadeProjectile.Type.FRAG, 12.0f, 3.5f, 6.0f, 7.0f, TGSounds.GRENADE_PIN));

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
