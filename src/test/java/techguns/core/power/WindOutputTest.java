package techguns.core.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WindOutputTest {
    private static final double EPS = 1e-9;

    @Test
    void calmDeepBelowSeaLevel() {
        assertEquals(0.0, WindOutput.heightFactor(-24), EPS);
        assertEquals(0.0, WindOutput.heightFactor(-100), EPS);
    }

    @Test
    void halfAtSeaLevelRisingWithHeight() {
        assertEquals(0.25, WindOutput.heightFactor(-12), EPS);
        assertEquals(0.5, WindOutput.heightFactor(0), EPS);
        assertEquals(1.0, WindOutput.heightFactor(64), EPS);
        assertEquals(1.5, WindOutput.heightFactor(128), EPS);
    }

    @Test
    void heightFactorIsCapped() {
        assertEquals(2.0, WindOutput.heightFactor(192), EPS);
        assertEquals(2.0, WindOutput.heightFactor(1000), EPS);
    }

    @Test
    void weatherAddsUp() {
        assertEquals(1.0, WindOutput.weatherFactor(0, 0), EPS);
        assertEquals(1.25, WindOutput.weatherFactor(1, 0), EPS);
        assertEquals(1.5, WindOutput.weatherFactor(1, 1), EPS);
        assertEquals(1.5, WindOutput.weatherFactor(5, 5), EPS);
    }

    @Test
    void outputScalesWithSegmentsBiomeAndWeather() {
        assertEquals(10.0 * 3.0 * 1.5 * 1.25, WindOutput.perTick(10, 3.0, 1.5, 1.25, true), EPS);
        assertEquals(0.0, WindOutput.perTick(10, 3.0, 1.5, 1.25, false), EPS);
    }

    @Test
    void rotorSpeedIsCapped() {
        assertEquals(6.0f, WindOutput.degreesPerTick(1.0), 1e-6f);
        assertEquals(24.0f, WindOutput.degreesPerTick(10.0), 1e-6f);
        assertEquals(0.0f, WindOutput.degreesPerTick(-1.0), 1e-6f);
    }
}
