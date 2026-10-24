package techguns.core.gun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GunMathTest {
    private static final float EPS = 1e-5f;

    @Test
    void fullDamageBeforeDropStart() {
        assertEquals(8f, GunMath.damageAtDistance(8f, 6f, 25f, 40f, 10.0), EPS);
        assertEquals(8f, GunMath.damageAtDistance(8f, 6f, 25f, 40f, 25.0), EPS);
    }

    @Test
    void linearFalloffBetweenStartAndEnd() {
        // halfway between 25 and 40 -> halfway between 8 and 6
        assertEquals(7f, GunMath.damageAtDistance(8f, 6f, 25f, 40f, 32.5), EPS);
    }

    @Test
    void minimumDamageAfterDropEnd() {
        assertEquals(6f, GunMath.damageAtDistance(8f, 6f, 25f, 40f, 100.0), EPS);
    }

    @Test
    void noDropWhenEndIsZero() {
        assertEquals(8f, GunMath.damageAtDistance(8f, 1f, 0f, 0f, 100.0), EPS);
    }

    @Test
    void lifetimeScalesWithSpeed() {
        assertEquals(34, GunMath.lifetimeTicks(100f, 3f));
        assertEquals(1, GunMath.lifetimeTicks(100f, 100f));
    }

    @Test
    void shotsPerBullet() {
        // single item reload, no magazine: the whole clip
        assertEquals(30f, GunMath.shotsPerBullet(30, 1, 0), EPS);
        // magazine filled by 3 bullets -> 10 shots per bullet
        assertEquals(10f, GunMath.shotsPerBullet(30, 1, 3), EPS);
        // shotgun loading 8 shells individually -> 1 shot per shell
        assertEquals(1f, GunMath.shotsPerBullet(8, 8, 0), EPS);
    }
}
