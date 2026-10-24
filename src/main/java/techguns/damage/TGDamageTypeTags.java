package techguns.damage;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import techguns.TGIds;
import techguns.core.damage.TGDamageCategory;

/**
 * One damage type tag per {@link TGDamageCategory}: {@code techguns:category/<category>}.
 * Other mods and datapacks can add their damage types to these tags to control how Techguns armor treats them.
 */
public final class TGDamageTypeTags {
    private static final Map<TGDamageCategory, TagKey<DamageType>> CATEGORY_TAGS = new EnumMap<>(TGDamageCategory.class);

    static {
        for (TGDamageCategory category : TGDamageCategory.values()) {
            CATEGORY_TAGS.put(category, TagKey.create(Registries.DAMAGE_TYPE, TGIds.id("category/" + category.id())));
        }
    }

    private TGDamageTypeTags() {
    }

    public static TagKey<DamageType> category(TGDamageCategory category) {
        return CATEGORY_TAGS.get(category);
    }
}
