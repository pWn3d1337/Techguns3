package techguns.core.view;

/** Field of view helpers (Techguns 2 {@code TGEventHandler.handleFovEvent}). */
public final class FovMath {
    private FovMath() {
    }

    /**
     * Techguns 2 "LockSpeedDependantFov": removes the vanilla speed term from a FOV modifier, so speed bonuses (power
     * armor, potions) don't widen the view; sprinting gets a fixed multiplier instead.
     *
     * @param modifier      FOV modifier computed by vanilla (and other mods)
     * @param movementSpeed the player's movement speed attribute value
     * @param walkingSpeed  the player's base walking speed (abilities)
     * @param sprinting     whether the player sprints
     * @param sprintFov     fixed sprint multiplier (Techguns 2 default 1.15)
     */
    public static float lockSpeedFov(float modifier, float movementSpeed, float walkingSpeed, boolean sprinting, float sprintFov) {
        if (walkingSpeed != 0.0f) {
            // vanilla AbstractClientPlayer#getFieldOfViewModifier: f *= (speed / walkingSpeed + 1) / 2
            float speedTerm = (movementSpeed / walkingSpeed + 1.0f) / 2.0f;
            if (speedTerm > 0.0f && Float.isFinite(speedTerm)) {
                modifier /= speedTerm;
            }
        }
        return sprinting ? modifier * sprintFov : modifier;
    }
}
