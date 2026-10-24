package techguns.core.damage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TGArmorFormulaTest {
    private static final float EPS = 1e-5f;

    @Test
    void noArmorKeepsFullDamage() {
        assertEquals(10f, TGArmorFormula.damageAfterArmor(10f, 0f, 0f, 0f), EPS);
    }

    @Test
    void armorReducesLinearly() {
        // 10 armor absorbs 10/25 = 40 %
        assertEquals(6f, TGArmorFormula.damageAfterArmor(10f, 10f, 0f, 0f), EPS);
    }

    @Test
    void effectiveArmorIsCapped() {
        // 30 armor is capped to 24 -> 96 % absorbed
        assertEquals(0.4f, TGArmorFormula.damageAfterArmor(10f, 30f, 0f, 0f), EPS);
    }

    @Test
    void penetrationIsMultipliedPerPieceAndCounteredByToughness() {
        // penetration 1 -> 4 armor ignored, toughness 2 reduces that to 2 -> 8 effective armor
        assertEquals(10f * (1f - 8f / 25f), TGArmorFormula.damageAfterArmor(10f, 10f, 2f, 1f), EPS);
    }

    @Test
    void penetrationCannotMakeArmorNegative() {
        assertEquals(10f, TGArmorFormula.damageAfterArmor(10f, 2f, 0f, 5f), EPS);
    }

    @Test
    void categoryFactorsApply() {
        assertEquals(10f, TGArmorFormula.armorAgainst(TGDamageCategory.PROJECTILE, 10f, 0f, false), EPS);
        assertEquals(5f, TGArmorFormula.armorAgainst(TGDamageCategory.ENERGY, 10f, 0f, false), EPS);
        assertEquals(0f, TGArmorFormula.armorAgainst(TGDamageCategory.POISON, 10f, 0f, false), EPS);
        assertEquals(20f, TGArmorFormula.armorAgainst(TGDamageCategory.FIRE, 10f, 0f, true), EPS);
    }

    @Test
    void bonusArmorAddsAndCannotGoBelowZero() {
        assertEquals(8f, TGArmorFormula.armorAgainst(TGDamageCategory.ENERGY, 10f, 3f, false), EPS);
        assertEquals(0f, TGArmorFormula.armorAgainst(TGDamageCategory.ENERGY, 10f, -9f, false), EPS);
    }

    @Test
    void unresistableIgnoresAllArmor() {
        assertEquals(0f, TGArmorFormula.armorAgainst(TGDamageCategory.UNRESISTABLE, 10f, 50f, false), EPS);
    }
}
