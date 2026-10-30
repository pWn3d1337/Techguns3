package techguns.armor;

import java.util.List;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import techguns.core.damage.TGDamageCategory;

/**
 * Tooltip image of a Techguns armor piece (Fabric {@code TGArmorTooltipData}): its armor against each damage category,
 * drawn as one row of icons by {@code ClientArmorTooltip}.
 */
/** @param percent the values are percentages (shield block rates): shown as whole numbers with a percent sign */
public record TGArmorTooltip(List<Float> values, boolean percent) implements TooltipComponent {
    public TGArmorTooltip(List<Float> values) {
        this(values, false);
    }

    /** Icon order of {@code textures/gui/armor_value_icons.png}. */
    public static final List<TGDamageCategory> CATEGORIES = List.of(TGDamageCategory.PHYSICAL, TGDamageCategory.PROJECTILE,
            TGDamageCategory.ENERGY, TGDamageCategory.EXPLOSION, TGDamageCategory.FIRE, TGDamageCategory.ICE, TGDamageCategory.LIGHTNING,
            TGDamageCategory.POISON, TGDamageCategory.DARK, TGDamageCategory.RADIATION);
}
