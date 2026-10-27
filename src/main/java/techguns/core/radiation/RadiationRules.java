package techguns.core.radiation;

/**
 * Techguns 2 radiation rules ({@code TGRadiationSystem}, {@code RadiationPotion}): a player collects radiation
 * (0 to {@link #MAX}) while the radiation effect is on them, the effect level minus their radiation resistance per
 * second; above the poisoning stages they get sick, at the maximum they take damage.
 */
public final class RadiationRules {
    public static final int MAX = 1000;
    /** Hunger. */
    public static final int MINOR_POISONING = 500;
    /** Stronger hunger, mining fatigue and weakness. */
    public static final int SEVERE_POISONING = 750;
    /** Nausea, slowness and {@link #LETHAL_DAMAGE} per second. */
    public static final int LETHAL_POISONING = 1000;
    public static final float LETHAL_DAMAGE = 2.0f;
    /** Radiation a player loses when dying. */
    public static final int LOST_ON_DEATH = 200;
    /** Ticks between the checks for radioactive items in the inventory (the effect lasts as long). */
    public static final int INVENTORY_INTERVAL = 60;
    /** Mobs take this much damage per point of dose. */
    public static final float MOB_DAMAGE_PER_DOSE = 0.5f;

    public enum Stage {
        NONE, MINOR, SEVERE, LETHAL
    }

    private RadiationRules() {
    }

    /** Radiation per second from the effect: its level minus the resistance, at least 0. */
    public static int dose(int amplifier, double resistance) {
        return Math.clamp(amplifier + 1 - (int) resistance, 0, MAX);
    }

    public static int add(int level, int amount) {
        return Math.clamp((long) level + amount, 0, MAX);
    }

    public static Stage stage(int level) {
        if (level >= LETHAL_POISONING) {
            return Stage.LETHAL;
        }
        if (level >= SEVERE_POISONING) {
            return Stage.SEVERE;
        }
        return level >= MINOR_POISONING ? Stage.MINOR : Stage.NONE;
    }

    /**
     * Techguns 2 {@code applyRadToEntities}: full strength within the inner radius, then linear to the outer strength
     * at the radius; -1 outside. Techguns 2 had the falloff the wrong way round (outer strength right past the inner
     * radius, rising to full strength at the edge).
     */
    public static int strengthAt(double distance, double radius, double innerRadius, int strength, int outerStrength) {
        if (distance >= radius) {
            return -1;
        }
        if (distance <= innerRadius) {
            return strength;
        }
        double factor = (distance - innerRadius) / (radius - innerRadius);
        return (int) Math.round(strength + (outerStrength - strength) * factor);
    }

    /** Techguns 2 {@code EntityRadiation}: a radiation area keeps its strength for half its time, then fades to 0. */
    public static int fade(int age, int duration, int strength) {
        if (age * 2 < duration) {
            return strength;
        }
        double factor = 1.0 - (age - duration * 0.5) / (duration * 0.5);
        return Math.max((int) Math.round(strength * factor), 0);
    }
}
