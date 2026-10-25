package techguns.registry;

import java.util.ArrayList;
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
    public static final AmmoType ROCKETS_TYPE = AmmoType.simple(ROCKET);

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
        return name.endsWith("_" + AmmoType.INCENDIARY) || name.endsWith("_" + AmmoType.EXPLOSIVE)
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
