package techguns.armor;

import java.util.List;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import techguns.core.damage.TGDamageCategory;

/**
 * Tooltip image of a Techguns armor piece (Fabric {@code TGArmorTooltipData}): its armor against each damage category,
 * drawn as one row of icons by {@code ClientArmorTooltip}.
 */
public record TGArmorTooltip(List<Float> values) implements TooltipComponent {
    /** Icon order of {@code textures/gui/armor_value_icons.png}. */
    public static final List<TGDamageCategory> CATEGORIES = List.of(TGDamageCategory.PHYSICAL, TGDamageCategory.PROJECTILE,
            TGDamageCategory.ENERGY, TGDamageCategory.EXPLOSION, TGDamageCategory.FIRE, TGDamageCategory.ICE, TGDamageCategory.LIGHTNING,
            TGDamageCategory.POISON, TGDamageCategory.DARK, TGDamageCategory.RADIATION);
}
