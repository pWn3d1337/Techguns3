package techguns.client.particle;

import net.minecraft.util.RandomSource;

/** Small math helpers shared by the particle classes (Techguns 2 {@code MathUtil}). */
final class FxMath {
    static final double D2R = Math.PI / 180.0;
    static final double R2D = 180.0 / Math.PI;

    private FxMath() {
    }

    static int randomInt(RandomSource rand, int min, int max) {
        if (max >= min) {
            return min + rand.nextInt((max - min) + 1);
        }
        return max + rand.nextInt((min - max) + 1);
    }

    static float randomFloat(RandomSource rand, float min, float max) {
        return min + (rand.nextFloat() * (max - min));
    }
}
