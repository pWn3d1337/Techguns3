package techguns.core.repair;

/**
 * Repair Bench material counts (Techguns 2 {@code GenericArmor.getRepairMats}): a piece needs its full material count
 * times the fraction of durability it lost, rounded up, split between a metal and a cloth part.
 */
public final class RepairFormula {
    private RepairFormula() {
    }

    /**
     * Materials for a repair from {@code damage} lost points; {@code maxDamage - 1} lost points (worn out) cost all
     * {@code total} materials. Capped at {@code total} (Techguns 2 gave one extra for grinding undamaged armor).
     */
    public static int count(int damage, int maxDamage, int total) {
        if (damage <= 0 || total <= 0 || maxDamage <= 1) {
            return 0;
        }
        float lost = Math.min(1.0f, damage / (float) (maxDamage - 1));
        return (int) Math.ceil(total * lost);
    }

    /** Metal share of {@code count} materials, rounded up; the rest is cloth. */
    public static int metal(int count, float metalRatio) {
        return (int) Math.ceil(count * metalRatio);
    }
}
