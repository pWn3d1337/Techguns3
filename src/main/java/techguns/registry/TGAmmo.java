package techguns.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import techguns.gun.ammo.AmmoType;

/**
 * Ammo items and the ammo types guns load. Ammo items live in {@link TGItems#ITEMS}.
 */
public final class TGAmmo {
    /** Ammo items with a flat item texture. */
    private static final List<DeferredItem<Item>> FLAT_AMMO = new ArrayList<>();
    /** Ammo items rendered with a 3D model (magazines); models come with the gun renderers (Phase 2b). */
    private static final List<DeferredItem<Item>> MODEL_AMMO = new ArrayList<>();

    // --- Items ---
    public static final DeferredItem<Item> PISTOL_ROUNDS = flat("pistol_rounds", 64);
    public static final DeferredItem<Item> PISTOL_ROUNDS_INCENDIARY = flat("pistol_rounds_incendiary", 64);
    public static final DeferredItem<Item> RIFLE_ROUNDS = flat("rifle_rounds", 64);
    public static final DeferredItem<Item> RIFLE_ROUNDS_INCENDIARY = flat("rifle_rounds_incendiary", 64);
    public static final DeferredItem<Item> PISTOL_MAGAZINE = flat("pistol_magazine", 16);
    public static final DeferredItem<Item> PISTOL_MAGAZINE_EMPTY = flat("pistol_magazine_empty", 16);
    public static final DeferredItem<Item> PISTOL_MAGAZINE_INCENDIARY = flat("pistol_magazine_incendiary", 16);
    public static final DeferredItem<Item> SMG_MAGAZINE = flat("smg_magazine", 16);
    public static final DeferredItem<Item> SMG_MAGAZINE_EMPTY = flat("smg_magazine_empty", 16);
    public static final DeferredItem<Item> SMG_MAGAZINE_INCENDIARY = flat("smg_magazine_incendiary", 16);
    public static final DeferredItem<Item> ASSAULT_RIFLE_MAGAZINE = model("assault_rifle_magazine", 16);
    public static final DeferredItem<Item> ASSAULT_RIFLE_MAGAZINE_EMPTY = model("assault_rifle_magazine_empty", 16);
    public static final DeferredItem<Item> ASSAULT_RIFLE_MAGAZINE_INCENDIARY = model("assault_rifle_magazine_incendiary", 16);
    public static final DeferredItem<Item> LMG_MAGAZINE = model("lmg_magazine", 16);
    public static final DeferredItem<Item> LMG_MAGAZINE_EMPTY = model("lmg_magazine_empty", 16);
    public static final DeferredItem<Item> LMG_MAGAZINE_INCENDIARY = model("lmg_magazine_incendiary", 16);
    public static final DeferredItem<Item> MINIGUN_DRUM = flat("minigun_drum", 16);
    public static final DeferredItem<Item> MINIGUN_DRUM_EMPTY = flat("minigun_drum_empty", 16);
    public static final DeferredItem<Item> MINIGUN_DRUM_INCENDIARY = flat("minigun_drum_incendiary", 16);
    public static final DeferredItem<Item> ENERGY_CELL = flat("energy_cell", 16);
    public static final DeferredItem<Item> ENERGY_CELL_EMPTY = flat("energy_cell_empty", 16);
    public static final DeferredItem<Item> ROCKET = model("rocket", 8);
    public static final DeferredItem<Item> SHOTGUN_ROUNDS = flat("shotgun_rounds", 64);
    public static final DeferredItem<Item> SHOTGUN_ROUNDS_INCENDIARY = flat("shotgun_rounds_incendiary", 64);
    public static final DeferredItem<Item> SNIPER_ROUNDS = flat("sniper_rounds", 64);
    public static final DeferredItem<Item> SNIPER_ROUNDS_INCENDIARY = flat("sniper_rounds_incendiary", 64);
    public static final DeferredItem<Item> SNIPER_ROUNDS_EXPLOSIVE = flat("sniper_rounds_explosive", 64);
    public static final DeferredItem<Item> ADVANCED_ROUNDS = flat("advanced_rounds", 64);
    public static final DeferredItem<Item> AS50_MAGAZINE = model("as50_magazine", 16);
    public static final DeferredItem<Item> AS50_MAGAZINE_EMPTY = model("as50_magazine_empty", 16);
    public static final DeferredItem<Item> AS50_MAGAZINE_INCENDIARY = model("as50_magazine_incendiary", 16);
    public static final DeferredItem<Item> AS50_MAGAZINE_EXPLOSIVE = model("as50_magazine_explosive", 16);
    public static final DeferredItem<Item> ADVANCED_MAGAZINE = flat("advanced_magazine", 16);
    public static final DeferredItem<Item> ADVANCED_MAGAZINE_EMPTY = flat("advanced_magazine_empty", 16);
    public static final DeferredItem<Item> GAUSS_RIFLE_SLUGS = flat("gauss_rifle_slugs", 64);
    public static final DeferredItem<Item> REDSTONE_BATTERY = flat("redstone_battery", 16);
    public static final DeferredItem<Item> REDSTONE_BATTERY_EMPTY = flat("redstone_battery_empty", 16);
    public static final DeferredItem<Item> NUCLEAR_POWER_CELL = flat("nuclear_power_cell", 16);
    public static final DeferredItem<Item> NUCLEAR_POWER_CELL_EMPTY = flat("nuclear_power_cell_empty", 16);
    public static final DeferredItem<Item> NETHER_CHARGE = flat("nether_charge", 16);
    public static final DeferredItem<Item> ROCKET_NUKE = model("rocket_nuke", 8);
    public static final DeferredItem<Item> ROCKET_HIGH_VELOCITY = model("rocket_high_velocity", 8);
    public static final DeferredItem<Item> GRENADE_40MM = flat("grenade_40mm", 16);
    public static final DeferredItem<Item> FUEL_TANK = flat("fuel_tank", 16);
    public static final DeferredItem<Item> FUEL_TANK_EMPTY = flat("fuel_tank_empty", 16);
    public static final DeferredItem<Item> BIO_TANK = flat("bio_tank", 16);
    public static final DeferredItem<Item> BIO_TANK_EMPTY = flat("bio_tank_empty", 16);
    public static final DeferredItem<Item> STONE_BULLETS = flat("stone_bullets", 64);
    public static final DeferredItem<Item> MAGIC_BULLET = flat("magic_bullet", 64);
    public static final DeferredItem<Item> MAGIC_BULLET_FIRE = flat("magic_bullet_fire", 64);
    public static final DeferredItem<Item> MAGIC_BULLET_LIGHTNING = flat("magic_bullet_lightning", 64);

    // --- Ammo types ---
    public static final AmmoType PISTOL_ROUNDS_TYPE = AmmoType.simple(PISTOL_ROUNDS)
            .addVariant(AmmoType.INCENDIARY, PISTOL_ROUNDS_INCENDIARY, PISTOL_ROUNDS_INCENDIARY);
    public static final AmmoType RIFLE_ROUNDS_TYPE = AmmoType.simple(RIFLE_ROUNDS)
            .addVariant(AmmoType.INCENDIARY, RIFLE_ROUNDS_INCENDIARY, RIFLE_ROUNDS_INCENDIARY);
    public static final AmmoType PISTOL_MAGAZINE_TYPE = AmmoType.withEmpty(PISTOL_MAGAZINE, PISTOL_MAGAZINE_EMPTY, PISTOL_ROUNDS, 3)
            .addVariant(AmmoType.INCENDIARY, PISTOL_MAGAZINE_INCENDIARY, PISTOL_ROUNDS_INCENDIARY);
    public static final AmmoType SMG_MAGAZINE_TYPE = AmmoType.withEmpty(SMG_MAGAZINE, SMG_MAGAZINE_EMPTY, PISTOL_ROUNDS, 2)
            .addVariant(AmmoType.INCENDIARY, SMG_MAGAZINE_INCENDIARY, PISTOL_ROUNDS_INCENDIARY);
    public static final AmmoType ASSAULT_RIFLE_MAGAZINE_TYPE = AmmoType.withEmpty(ASSAULT_RIFLE_MAGAZINE, ASSAULT_RIFLE_MAGAZINE_EMPTY, RIFLE_ROUNDS, 3)
            .addVariant(AmmoType.INCENDIARY, ASSAULT_RIFLE_MAGAZINE_INCENDIARY, RIFLE_ROUNDS_INCENDIARY);
    public static final AmmoType LMG_MAGAZINE_TYPE = AmmoType.withEmpty(LMG_MAGAZINE, LMG_MAGAZINE_EMPTY, RIFLE_ROUNDS, 8)
            .addVariant(AmmoType.INCENDIARY, LMG_MAGAZINE_INCENDIARY, RIFLE_ROUNDS_INCENDIARY);
    public static final AmmoType MINIGUN_DRUM_TYPE = AmmoType.withEmpty(MINIGUN_DRUM, MINIGUN_DRUM_EMPTY, RIFLE_ROUNDS, 16)
            .addVariant(AmmoType.INCENDIARY, MINIGUN_DRUM_INCENDIARY, RIFLE_ROUNDS_INCENDIARY);
    public static final AmmoType ENERGY_CELL_TYPE = AmmoType.withEmpty(ENERGY_CELL, ENERGY_CELL_EMPTY, null, 0);
    public static final AmmoType ROCKETS_TYPE = AmmoType.simple(ROCKET)
            .addVariant(AmmoType.NUKE, ROCKET_NUKE, ROCKET_NUKE)
            .addVariant(AmmoType.HIGH_VELOCITY, ROCKET_HIGH_VELOCITY, ROCKET_HIGH_VELOCITY);
    /** Guided missile launchers can't fire nukes. */
    public static final AmmoType ROCKETS_NO_NUKES_TYPE = AmmoType.simple(ROCKET)
            .addVariant(AmmoType.HIGH_VELOCITY, ROCKET_HIGH_VELOCITY, ROCKET_HIGH_VELOCITY);
    public static final AmmoType GRENADES_40MM_TYPE = AmmoType.simple(GRENADE_40MM);
    public static final AmmoType FUEL_TANK_TYPE = AmmoType.withEmpty(FUEL_TANK, FUEL_TANK_EMPTY, null, 0);
    public static final AmmoType BIO_TANK_TYPE = AmmoType.withEmpty(BIO_TANK, BIO_TANK_EMPTY, null, 0);
    public static final AmmoType STONE_BULLETS_TYPE = AmmoType.simple(STONE_BULLETS);
    public static final AmmoType MAGIC_BULLETS_TYPE = AmmoType.simple(MAGIC_BULLET)
            .addVariant(AmmoType.FIRE, MAGIC_BULLET_FIRE, MAGIC_BULLET_FIRE)
            .addVariant(AmmoType.LIGHTNING, MAGIC_BULLET_LIGHTNING, MAGIC_BULLET_LIGHTNING);
    public static final AmmoType SHOTGUN_ROUNDS_TYPE = AmmoType.simple(SHOTGUN_ROUNDS)
            .addVariant(AmmoType.INCENDIARY, SHOTGUN_ROUNDS_INCENDIARY, SHOTGUN_ROUNDS_INCENDIARY);
    public static final AmmoType AS50_MAGAZINE_TYPE = AmmoType.withEmpty(AS50_MAGAZINE, AS50_MAGAZINE_EMPTY, SNIPER_ROUNDS, 2)
            .addVariant(AmmoType.INCENDIARY, AS50_MAGAZINE_INCENDIARY, SNIPER_ROUNDS_INCENDIARY)
            .addVariant(AmmoType.EXPLOSIVE, AS50_MAGAZINE_EXPLOSIVE, SNIPER_ROUNDS_EXPLOSIVE);
    public static final AmmoType ADVANCED_MAGAZINE_TYPE = AmmoType.withEmpty(ADVANCED_MAGAZINE, ADVANCED_MAGAZINE_EMPTY, ADVANCED_ROUNDS, 3);
    public static final AmmoType REDSTONE_BATTERY_TYPE = AmmoType.withEmpty(REDSTONE_BATTERY, REDSTONE_BATTERY_EMPTY, null, 0);
    public static final AmmoType NUCLEAR_POWER_CELL_TYPE = AmmoType.withEmpty(NUCLEAR_POWER_CELL, NUCLEAR_POWER_CELL_EMPTY, null, 0);
    public static final AmmoType NETHER_CHARGE_TYPE = AmmoType.simple(NETHER_CHARGE);
    /** Gauss rifle: one slug pack plus one energy cell per reload; the cell comes back empty. */
    public static final AmmoType GAUSS_RIFLE_TYPE = AmmoType.multi(List.of(GAUSS_RIFLE_SLUGS, ENERGY_CELL),
            Arrays.asList(null, ENERGY_CELL_EMPTY), Arrays.asList(GAUSS_RIFLE_SLUGS, null), 1);

    private TGAmmo() {
    }

    private static DeferredItem<Item> flat(String name, int stackSize) {
        DeferredItem<Item> item = TGItems.ITEMS.registerSimpleItem(name, properties(name, stackSize));
        FLAT_AMMO.add(item);
        return item;
    }

    private static DeferredItem<Item> model(String name, int stackSize) {
        DeferredItem<Item> item = TGItems.ITEMS.registerSimpleItem(name, properties(name, stackSize));
        MODEL_AMMO.add(item);
        return item;
    }

    /** Special ammo variants get a coloured name (Techguns 2 used a colour code in the lang file). */
    private static Item.Properties properties(String name, int stackSize) {
        Item.Properties properties = new Item.Properties().stacksTo(stackSize);
        return name.endsWith("_" + AmmoType.INCENDIARY) || name.endsWith("_" + AmmoType.EXPLOSIVE) || name.endsWith("_" + AmmoType.HIGH_VELOCITY)
                ? properties.rarity(Rarity.UNCOMMON) : properties;
    }

    public static List<DeferredItem<Item>> flatAmmo() {
        return Collections.unmodifiableList(FLAT_AMMO);
    }

    public static List<DeferredItem<Item>> modelAmmo() {
        return Collections.unmodifiableList(MODEL_AMMO);
    }

    /** Forces class loading so the items register. */
    static void init() {
    }
}
