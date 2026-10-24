package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.Item;
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
    public static final DeferredItem<Item> RIFLE_ROUNDS = flat("rifle_rounds", 64);
    public static final DeferredItem<Item> ASSAULT_RIFLE_MAGAZINE = model("assault_rifle_magazine", 16);
    public static final DeferredItem<Item> ASSAULT_RIFLE_MAGAZINE_EMPTY = model("assault_rifle_magazine_empty", 16);
    public static final DeferredItem<Item> ENERGY_CELL = flat("energy_cell", 16);
    public static final DeferredItem<Item> ENERGY_CELL_EMPTY = flat("energy_cell_empty", 16);
    public static final DeferredItem<Item> ROCKET = model("rocket", 8);

    // --- Ammo types ---
    public static final AmmoType ASSAULT_RIFLE_MAGAZINE_TYPE = AmmoType.withEmpty(ASSAULT_RIFLE_MAGAZINE, ASSAULT_RIFLE_MAGAZINE_EMPTY, RIFLE_ROUNDS, 3);
    public static final AmmoType ENERGY_CELL_TYPE = AmmoType.withEmpty(ENERGY_CELL, ENERGY_CELL_EMPTY, null, 0);
    public static final AmmoType ROCKETS_TYPE = AmmoType.simple(ROCKET);

    private TGAmmo() {
    }

    private static DeferredItem<Item> flat(String name, int stackSize) {
        DeferredItem<Item> item = TGItems.ITEMS.registerSimpleItem(name, new Item.Properties().stacksTo(stackSize));
        FLAT_AMMO.add(item);
        return item;
    }

    private static DeferredItem<Item> model(String name, int stackSize) {
        DeferredItem<Item> item = TGItems.ITEMS.registerSimpleItem(name, new Item.Properties().stacksTo(stackSize));
        MODEL_AMMO.add(item);
        return item;
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
