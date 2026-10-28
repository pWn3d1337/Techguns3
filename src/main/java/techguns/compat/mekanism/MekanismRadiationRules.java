package techguns.compat.mekanism;

import net.minecraft.world.entity.EquipmentSlot;

/** The numbers of the Mekanism radiation bridge ({@link MekanismRadiation}), without Mekanism types. */
public final class MekanismRadiationRules {
    private MekanismRadiationRules() {
    }

    /** Mekanism's shielding share of an armor slot (its {@code ItemHazmatSuitArmor.getShieldingByArmor}). */
    public static double slotShare(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0.25;
            case CHEST -> 0.4;
            case LEGS -> 0.2;
            case FEET -> 0.15;
            default -> 0.0;
        };
    }

    /** Share of Mekanism radiation kept per second under rad regeneration of this amplifier. */
    public static double decay(int amplifier) {
        return Math.max(0.0, 1.0 - 0.02 * (amplifier + 1));
    }
}
