package techguns.armor;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import techguns.core.damage.TGDamageCategory;
import techguns.registry.TGSounds;

/**
 * Defensive stats of a shield against Techguns damage (Techguns 2 {@code ShieldStats}): the fraction blocked per
 * damage category, a flat damage threshold and the material's impact sound. Vanilla damage is blocked as usual.
 */
public final class ShieldStats {
    /** Techguns 2 {@code ShieldMaterial}: picks the block sound. */
    public enum Material {
        WOOD(TGSounds.BULLET_IMPACT_WOOD), STONE(TGSounds.BULLET_IMPACT_STONE), METAL(TGSounds.BULLET_IMPACT_METAL),
        GLASS(TGSounds.BULLET_IMPACT_GLASS), GROUND(TGSounds.BULLET_IMPACT_DIRT), NONE(null);

        @Nullable
        private final Supplier<SoundEvent> sound;

        Material(@Nullable Supplier<SoundEvent> sound) {
            this.sound = sound;
        }

        @Nullable
        public SoundEvent sound() {
            return sound == null ? null : sound.get();
        }
    }

    public static final ShieldStats VANILLA_SHIELD = new ShieldStats(Material.WOOD, 0.0, 1.0, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.0, 0.0);
    /** Other mods' shields. */
    public static final ShieldStats DEFAULT = new ShieldStats(Material.METAL, 0.0, 1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.25, 0.25);
    public static final ShieldStats RIOT_SHIELD = new ShieldStats(Material.GLASS, 0.0, 1.0, 0.65, 0.60, 0.60, 0.60, 0.60, 0.60, 0.60, 0.40, 0.40);
    public static final ShieldStats BALLISTIC_SHIELD = new ShieldStats(Material.METAL, 1.5, 1.0, 0.8, 0.70, 0.70, 0.70, 0.70, 0.70, 0.70, 0.60, 0.60);
    /**
     * Not in Techguns 2 (it only had the model): heavy steel plating between the ballistic and the advanced shield,
     * strongest against projectiles and explosions, glass sound replaced by metal.
     */
    public static final ShieldStats HEAVY_SHIELD = new ShieldStats(Material.METAL, 2.0, 1.0, 0.85, 0.75, 0.80, 0.70, 0.70, 0.75, 0.70, 0.65, 0.65);
    public static final ShieldStats ADVANCED_SHIELD = new ShieldStats(Material.METAL, 3.0, 1.0, 0.9, 0.80, 0.80, 0.80, 0.80, 0.80, 0.80, 0.75, 0.75);

    private final Material material;
    private final double damageThreshold;
    private final Map<TGDamageCategory, Double> rates = new EnumMap<>(TGDamageCategory.class);

    /** Rates in Techguns 2 order: physical, projectile, fire, explosion, energy, poison, ice, lightning, radiation, dark. */
    private ShieldStats(Material material, double damageThreshold, double physical, double projectile, double fire, double explosion,
                        double energy, double poison, double ice, double lightning, double radiation, double dark) {
        this.material = material;
        this.damageThreshold = damageThreshold;
        rates.put(TGDamageCategory.PHYSICAL, physical);
        rates.put(TGDamageCategory.PROJECTILE, projectile);
        rates.put(TGDamageCategory.FIRE, fire);
        rates.put(TGDamageCategory.EXPLOSION, explosion);
        rates.put(TGDamageCategory.ENERGY, energy);
        rates.put(TGDamageCategory.POISON, poison);
        rates.put(TGDamageCategory.ICE, ice);
        rates.put(TGDamageCategory.LIGHTNING, lightning);
        rates.put(TGDamageCategory.RADIATION, radiation);
        rates.put(TGDamageCategory.DARK, dark);
    }

    /** Stats of a shield item: Techguns shields carry their own, the vanilla shield, else {@link #DEFAULT}. */
    public static ShieldStats of(Item item) {
        if (item instanceof TGShieldItem shield) {
            return shield.stats();
        }
        return item == Items.SHIELD ? VANILLA_SHIELD : DEFAULT;
    }

    public Material material() {
        return material;
    }

    public double damageThreshold() {
        return damageThreshold;
    }

    /** Fraction blocked against a category (0 for unresistable). */
    public double rate(TGDamageCategory category) {
        return rates.getOrDefault(category, 0.0);
    }
}
