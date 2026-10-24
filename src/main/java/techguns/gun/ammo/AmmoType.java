package techguns.gun.ammo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * What a gun loads: one or more ammo items per reload (e.g. a magazine), the empty items returned
 * after reloading, and named variants (default, incendiary, ...). Items are referenced lazily as
 * {@link ItemLike} so ammo types can be built while items are still being registered.
 */
public final class AmmoType {
    public static final String DEFAULT = "default";
    public static final String INCENDIARY = "incendiary";
    public static final String EXPLOSIVE = "explosive";
    public static final String NUKE = "nuke";
    public static final String HIGH_VELOCITY = "high_velocity";

    /**
     * @param key    variant name stored on the gun
     * @param ammo   items consumed per reload, one per slot
     * @param bullet loose rounds returned when unloading, one per slot (may be empty)
     */
    public record Variant(String key, List<ItemLike> ammo, List<ItemLike> bullet) {
    }

    private final List<Variant> variants = new ArrayList<>();
    /** Items returned after reloading, one per slot (e.g. the empty magazine); null entries = nothing. */
    private final List<ItemLike> emptyItems;
    /** Bullets that fill one magazine; 0 = not refillable from loose rounds. */
    private final int bulletsPerMag;

    private AmmoType(List<ItemLike> ammo, List<ItemLike> bullet, List<ItemLike> emptyItems, int bulletsPerMag) {
        this.variants.add(new Variant(DEFAULT, ammo, bullet));
        this.emptyItems = emptyItems;
        this.bulletsPerMag = bulletsPerMag;
    }

    /** Single ammo item that is consumed completely (shells, rockets). */
    public static AmmoType simple(ItemLike ammo) {
        return new AmmoType(List.of(ammo), List.of(ammo), Collections.singletonList(null), 0);
    }

    /** Ammo item that leaves an empty item behind (magazines, energy cells). */
    public static AmmoType withEmpty(ItemLike ammo, ItemLike empty, ItemLike bullet, int bulletsPerMag) {
        return new AmmoType(List.of(ammo), Collections.singletonList(bullet), List.of(empty), bulletsPerMag);
    }

    public AmmoType addVariant(String key, ItemLike ammo, ItemLike bullet) {
        variants.add(new Variant(key, List.of(ammo), Collections.singletonList(bullet)));
        return this;
    }

    public List<Variant> variants() {
        return Collections.unmodifiableList(variants);
    }

    public Variant variant(String key) {
        for (Variant variant : variants) {
            if (variant.key().equals(key)) {
                return variant;
            }
        }
        return variants.getFirst();
    }

    public boolean hasVariant(String key) {
        return variants.stream().anyMatch(v -> v.key().equals(key));
    }

    /** Ammo items consumed by one reload of the given variant. */
    public List<ItemStack> ammoStacks(String variantKey) {
        return variant(variantKey).ammo().stream().map(ItemStack::new).toList();
    }

    /** Items given back after a reload; empty stacks where nothing is returned. */
    public List<ItemStack> emptyStacks() {
        return emptyItems.stream().map(item -> item == null ? ItemStack.EMPTY : new ItemStack(item)).toList();
    }

    /** Variant key for an ammo item, or {@link #DEFAULT}. */
    public String variantKeyFor(ItemStack stack) {
        for (Variant variant : variants) {
            for (ItemLike ammo : variant.ammo()) {
                if (stack.is(ammo.asItem())) {
                    return variant.key();
                }
            }
        }
        return DEFAULT;
    }

    public int bulletsPerMag() {
        return bulletsPerMag;
    }
}
