package techguns.core.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FovMathTest {
    private static final float EPS = 1e-5f;
    private static final float WALK = 0.1f;

    /** What vanilla computes for a walking player with the given movement speed attribute. */
    private static float vanilla(float movementSpeed) {
        return (movementSpeed / WALK + 1.0f) / 2.0f;
    }

    @Test
    void normalSpeedStaysUnchanged() {
        assertEquals(1.0f, FovMath.lockSpeedFov(vanilla(0.1f), 0.1f, WALK, false, 1.15f), EPS);
    }

    @Test
    void speedBonusNoLongerWidensTheView() {
        // power armor: +50% movement speed
        assertEquals(1.0f, FovMath.lockSpeedFov(vanilla(0.15f), 0.15f, WALK, false, 1.15f), EPS);
    }

    @Test
    void sprintingGetsTheFixedMultiplier() {
        // vanilla sprinting adds 30% speed; with a speed bonus on top the result stays the fixed value
        assertEquals(1.15f, FovMath.lockSpeedFov(vanilla(0.13f), 0.13f, WALK, true, 1.15f), EPS);
        assertEquals(1.15f, FovMath.lockSpeedFov(vanilla(0.2f), 0.2f, WALK, true, 1.15f), EPS);
    }

    @Test
    void otherModifiersAreKept() {
        // flying (x1.1) and a drawn bow (x0.85) are not speed dependent
        float other = 1.1f * 0.85f;
        assertEquals(other, FovMath.lockSpeedFov(other * vanilla(0.15f), 0.15f, WALK, false, 1.15f), EPS);
    }

    @Test
    void zeroWalkingSpeedIsLeftAlone() {
        assertEquals(1.0f, FovMath.lockSpeedFov(1.0f, 0.1f, 0.0f, false, 1.15f), EPS);
    }
}
