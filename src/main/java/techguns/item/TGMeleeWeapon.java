package techguns.item;

import net.minecraft.world.item.ItemStack;
import techguns.core.damage.DeathType;

/**
 * Items that hit with the vanilla player attack but kill with a Techguns death effect (Techguns 2
 * {@code IToolTGDamageSystem}: the combat knife and crowbar gore, the powered melee tools theirs).
 */
public interface TGMeleeWeapon {
    DeathType meleeDeathType(ItemStack stack);

    /** Chance of the death effect on a kill, 0 to 1. */
    float meleeGoreChance();
}
