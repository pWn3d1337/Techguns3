package techguns.core.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SolarOutputTest {
    @Test
    void fullRateInSunlight() {
        assertEquals(8.0, SolarOutput.perTick(8, true, true, true));
    }

    @Test
    void eighthInMoonlight() {
        assertEquals(1.0, SolarOutput.perTick(8, true, true, false));
    }

    @Test
    void nothingWithoutSky() {
        assertEquals(0.0, SolarOutput.perTick(8, false, true, true));
        assertEquals(0.0, SolarOutput.perTick(8, true, false, true));
    }

    @Test
    void fractionsAddUp() {
        double[] remainder = {0.0};
        int total = 0;
        for (int i = 0; i < 16; i++) {
            total += SolarOutput.produce(SolarOutput.perTick(4, true, true, false), remainder);
        }
        // 4 / 8 = 0.5 FE per tick
        assertEquals(8, total);
    }
}
