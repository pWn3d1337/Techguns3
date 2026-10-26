package techguns.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.core.damage.TGDamageCategory;

/**
 * Techguns attributes. Every living entity gets one bonus armor attribute per resistable damage category
 * ({@code techguns:armor.<category>}), added on top of its regular armor scaled by the category's armor factor.
 */
public final class TGAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Techguns.MODID);

    private static final Map<TGDamageCategory, Holder<Attribute>> BONUS_ARMOR = new EnumMap<>(TGDamageCategory.class);

    static {
        for (TGDamageCategory category : TGDamageCategory.values()) {
            if (category.isResistable()) {
                String name = "armor." + category.id();
                BONUS_ARMOR.put(category, ATTRIBUTES.register(name,
                        () -> new RangedAttribute("attribute.name." + Techguns.MODID + "." + name, 0.0, -1024.0, 1024.0).setSyncable(true)));
            }
        }
    }

    // --- Player bonuses of armor and extra-slot gear (Techguns 2 TGArmorBonus), summed over all worn pieces ---

    /** Movement speed bonus, a fraction (0.1 = +10%); doubled while sprinting. */
    public static final Holder<Attribute> SPEED_BONUS = bonus("speed_bonus", 10.0);
    /** Extra movement speed in water or lava, a fraction. */
    public static final Holder<Attribute> WATER_SPEED_BONUS = bonus("water_speed_bonus", 10.0);
    /** Upward velocity added to every jump. */
    public static final Holder<Attribute> JUMP_BONUS = bonus("jump_bonus", 10.0);
    /** Fraction of fall damage avoided (1 = none). */
    public static final Holder<Attribute> FALL_DAMAGE_REDUCTION = bonus("fall_damage_reduction", 1.0);
    /** Fall distance in blocks that causes no damage on top of vanilla's. */
    public static final Holder<Attribute> FALL_FREE_HEIGHT = bonus("fall_free_height", 1024.0);
    /** Block breaking speed bonus, a fraction. */
    public static final Holder<Attribute> MINING_SPEED_BONUS = bonus("mining_speed_bonus", 100.0);
    /** Extra block breaking speed bonus in water or lava, a fraction. */
    public static final Holder<Attribute> WATER_MINING_SPEED_BONUS = bonus("water_mining_speed_bonus", 100.0);
    /** Gun spread reduction, a fraction (1 = no spread). */
    public static final Holder<Attribute> GUN_ACCURACY = bonus("gun_accuracy", 1.0);
    /** At 1 or more, burning players are extinguished. */
    public static final Holder<Attribute> COOLING_SYSTEM = bonus("cooling_system", 1024.0);
    /** At 1 or more, scuba tanks supply air under water (Techguns 2 OXYGEN_GEAR). */
    public static final Holder<Attribute> OXYGEN_GEAR = bonus("oxygen_gear", 1024.0);
    /** Radiation resistance (used by the radiation system). */
    public static final Holder<Attribute> RADIATION_RESISTANCE = bonus("radiation_resistance", 1024.0);

    private static final List<Holder<Attribute>> PLAYER_BONUSES = List.of(SPEED_BONUS, WATER_SPEED_BONUS, JUMP_BONUS,
            FALL_DAMAGE_REDUCTION, FALL_FREE_HEIGHT, MINING_SPEED_BONUS, WATER_MINING_SPEED_BONUS, GUN_ACCURACY, COOLING_SYSTEM,
            OXYGEN_GEAR, RADIATION_RESISTANCE);

    private static Holder<Attribute> bonus(String name, double max) {
        return ATTRIBUTES.register(name,
                () -> new RangedAttribute("attribute.name." + Techguns.MODID + "." + name, 0.0, 0.0, max).setSyncable(true));
    }

    private TGAttributes() {
    }

    /** Bonus armor attribute for a category, or {@code null} for {@link TGDamageCategory#UNRESISTABLE}. */
    public static Holder<Attribute> bonusArmor(TGDamageCategory category) {
        return BONUS_ARMOR.get(category);
    }

    static void addToAllLivingEntities(EntityAttributeModificationEvent event) {
        for (Holder<Attribute> attribute : PLAYER_BONUSES) {
            if (!event.has(EntityType.PLAYER, attribute)) {
                event.add(EntityType.PLAYER, attribute);
            }
        }
        for (var type : event.getTypes()) {
            for (Holder<Attribute> attribute : BONUS_ARMOR.values()) {
                if (!event.has(type, attribute)) {
                    event.add(type, attribute);
                }
            }
        }
    }
}
