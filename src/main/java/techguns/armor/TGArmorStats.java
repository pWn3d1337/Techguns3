package techguns.armor;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.world.item.ArmorItem;
import techguns.core.damage.TGDamageCategory;

/**
 * Armor values of a Techguns armor material (Techguns 2 {@code TGArmorMaterial}). Values are for a full set; each
 * piece gets its share by slot factor. By default physical and projectile armor equal the base value, poison/fire/
 * explosion/energy/ice/lightning/dark are 75% of it and radiation is 0.
 */
public final class TGArmorStats {
    /** Sum of the vanilla per-slot durability weights (13 + 15 + 16 + 11). */
    private static final int VANILLA_DURABILITY_FACTOR_SUM = 55;
    /** Techguns 2 elemental default: 75% of the base armor. */
    private static final float ELEMENTAL_FACTOR = 0.75f;

    private final String name;
    private final int baseDurability;
    private final float toughness;
    private final Map<TGDamageCategory, Float> armor = new EnumMap<>(TGDamageCategory.class);
    private final Map<ArmorItem.Type, Float> armorSplit = new EnumMap<>(ArmorItem.Type.class);
    private final Map<ArmorItem.Type, Float> durabilitySplit = new EnumMap<>(ArmorItem.Type.class);

    /**
     * @param name           material name, also the armor texture name ({@code textures/models/armor/<name>_layer_1.png})
     * @param baseDurability Techguns 2 base durability; a piece gets {@code round(split * 55 * baseDurability)}
     * @param armor          full-set physical armor
     */
    public TGArmorStats(String name, int baseDurability, float armor, float toughness) {
        this.name = name;
        this.baseDurability = baseDurability;
        this.toughness = toughness;
        for (TGDamageCategory category : TGDamageCategory.values()) {
            if (category.isResistable()) {
                this.armor.put(category, switch (category) {
                    case PHYSICAL, PROJECTILE -> armor;
                    case RADIATION -> 0.0f;
                    default -> armor * ELEMENTAL_FACTOR;
                });
            }
        }
        armorSplit.put(ArmorItem.Type.HELMET, 0.25f);
        armorSplit.put(ArmorItem.Type.CHESTPLATE, 0.30f);
        armorSplit.put(ArmorItem.Type.LEGGINGS, 0.25f);
        armorSplit.put(ArmorItem.Type.BOOTS, 0.20f);
        for (ArmorItem.Type type : armorSplit.keySet()) {
            durabilitySplit.put(type, 0.25f);
        }
    }

    public TGArmorStats set(TGDamageCategory category, float value) {
        armor.put(category, value);
        return this;
    }

    /** Fire, lightning, ice and energy. */
    public TGArmorStats elemental(float value) {
        return set(TGDamageCategory.FIRE, value).set(TGDamageCategory.LIGHTNING, value).set(TGDamageCategory.ICE, value)
                .set(TGDamageCategory.ENERGY, value);
    }

    public String name() {
        return name;
    }

    public float toughness() {
        return toughness;
    }

    /** Armor of one piece against a category. */
    public float armor(ArmorItem.Type type, TGDamageCategory category) {
        return armor.getOrDefault(category, 0.0f) * armorSplit.getOrDefault(type, 0.0f);
    }

    public int durability(ArmorItem.Type type) {
        return Math.round(durabilitySplit.getOrDefault(type, 0.0f) * VANILLA_DURABILITY_FACTOR_SUM * baseDurability);
    }
}
