package techguns.registry;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
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

    private TGAttributes() {
    }

    /** Bonus armor attribute for a category, or {@code null} for {@link TGDamageCategory#UNRESISTABLE}. */
    public static Holder<Attribute> bonusArmor(TGDamageCategory category) {
        return BONUS_ARMOR.get(category);
    }

    static void addToAllLivingEntities(EntityAttributeModificationEvent event) {
        for (var type : event.getTypes()) {
            for (Holder<Attribute> attribute : BONUS_ARMOR.values()) {
                if (!event.has(type, attribute)) {
                    event.add(type, attribute);
                }
            }
        }
    }
}
