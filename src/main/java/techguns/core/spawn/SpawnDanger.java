package techguns.core.spawn;

/**
 * Techguns 2 {@code TGSpawnManager} danger levels: the farther from the world spawn, the more dangerous NPCs may
 * spawn; rough biomes add to it.
 */
public final class SpawnDanger {
    /** Highest danger level a spawn table holds (Techguns 2 {@code TGNpcSpawnTable(5)}). */
    public static final int MAX = 5;

    private SpawnDanger() {
    }

    /**
     * Danger 0..3 from the horizontal distance to the world spawn; {@code levels} are the three distance limits, halved
     * in the Nether.
     */
    public static int fromDistance(double distance, boolean nether, int[] levels) {
        float factor = nether ? 0.5f : 1.0f;
        for (int i = 0; i < levels.length; i++) {
            if (distance < levels[i] * factor) {
                return i;
            }
        }
        return levels.length;
    }
}
