package techguns.core.transport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class EnergySplitTest {
    @Test
    void splitsEvenly() {
        assertArrayEquals(new int[]{100, 100, 100}, EnergySplit.waterFill(300, new int[]{1000, 1000, 1000}));
    }

    @Test
    void fullReceiversPassTheirShareOn() {
        assertArrayEquals(new int[]{50, 125, 125}, EnergySplit.waterFill(300, new int[]{50, 1000, 1000}));
    }

    @Test
    void neverMoreThanAcceptedOrOffered() {
        int[] give = EnergySplit.waterFill(1000, new int[]{10, 0, 20});
        assertArrayEquals(new int[]{10, 0, 20}, give);
        int[] small = EnergySplit.waterFill(5, new int[]{100, 100, 100});
        assertEquals(5, Arrays.stream(small).sum());
    }

    @Test
    void nothingToGive() {
        assertArrayEquals(new int[]{0, 0}, EnergySplit.waterFill(0, new int[]{10, 10}));
        assertArrayEquals(new int[0], EnergySplit.waterFill(10, new int[0]));
    }
}
