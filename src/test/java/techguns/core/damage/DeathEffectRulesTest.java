package techguns.core.damage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeathEffectRulesTest {
    @Test
    void defaultDeathTypeNeverPlays() {
        assertFalse(DeathEffectRules.playsDeathEffect(DeathType.DEFAULT, true, 1.0f, 0.0f));
    }

    @Test
    void rollBelowChancePlays() {
        assertTrue(DeathEffectRules.playsDeathEffect(DeathType.GORE, true, 0.5f, 0.49f));
        assertTrue(DeathEffectRules.playsDeathEffect(DeathType.LASER, true, 1.0f, 0.99f));
    }

    @Test
    void rollAtOrAboveChanceDoesNotPlay() {
        assertFalse(DeathEffectRules.playsDeathEffect(DeathType.GORE, true, 0.5f, 0.5f));
        assertFalse(DeathEffectRules.playsDeathEffect(DeathType.BIO, true, 0.0f, 0.0f));
    }

    @Test
    void bloodlessEntitiesSkipOnlyGore() {
        assertFalse(DeathEffectRules.playsDeathEffect(DeathType.GORE, false, 1.0f, 0.0f));
        assertTrue(DeathEffectRules.playsDeathEffect(DeathType.LASER, false, 1.0f, 0.0f));
        assertTrue(DeathEffectRules.playsDeathEffect(DeathType.BIO, false, 1.0f, 0.0f));
    }
}
