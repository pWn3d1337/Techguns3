package techguns.damage;

import techguns.core.damage.TGDamageCategory;

/**
 * An entity with its own armor per damage category (Techguns 2 {@code INpcTGDamageSystem.getTotalArmorAgainstType}
 * overrides, e.g. super mutants), used by the Techguns armor system instead of its armor value.
 */
public interface TGArmorProvider {
    float tgArmorAgainst(TGDamageCategory category);
}
