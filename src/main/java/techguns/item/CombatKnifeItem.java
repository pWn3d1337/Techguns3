package techguns.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import techguns.core.damage.DeathType;

/**
 * Techguns 2 combat knife ({@code TGSword} of {@code TG_STEEL}): a steel sword, 6.5 damage at sword speed.
 * <p>
 * Simplified vs 1.12: hits deal the vanilla player attack (Techguns 2 tools dealt its physical damage type); the gore
 * death effect is kept.
 */
public class CombatKnifeItem extends SwordItem implements TGMeleeWeapon {
    public CombatKnifeItem(Properties properties) {
        super(TGToolTiers.STEEL, properties.attributes(SwordItem.createAttributes(TGToolTiers.STEEL, 3, -2.4f)));
    }

    @Override
    public DeathType meleeDeathType(ItemStack stack) {
        return TGToolTiers.DEATH_TYPE;
    }

    @Override
    public float meleeGoreChance() {
        return TGToolTiers.GORE_CHANCE;
    }
}
