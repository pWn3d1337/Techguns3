package techguns.core.transport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * The lanes of the pipe block never overlap: every part of a lane (core, arms, plates on all six sides) is disjoint
 * from every part of another lane. Mirrors the geometry of {@code techguns.transport.PipeParts} without Minecraft types.
 */
class LaneLayoutTest {
    private static double[] box(Lane lane, double half, int axis, double lo, double hi) {
        double[] b = {lane.center - half, lane.center - half, lane.center - half, lane.center + half, lane.center + half, lane.center + half};
        if (axis >= 0) {
            b[axis] = lo;
            b[axis + 3] = hi;
        }
        return b;
    }

    private static List<double[]> parts(Lane lane) {
        List<double[]> parts = new ArrayList<>();
        parts.add(box(lane, Lane.CORE_HALF, -1, 0, 0));
        for (int axis = 0; axis < 3; axis++) {
            parts.add(box(lane, Lane.TUBE_HALF, axis, lane.center + Lane.CORE_HALF, 16));
            parts.add(box(lane, Lane.TUBE_HALF, axis, 0, lane.center - Lane.CORE_HALF));
            parts.add(box(lane, Lane.PLATE_HALF, axis, 16 - Lane.PLATE_DEPTH, 16));
            parts.add(box(lane, Lane.PLATE_HALF, axis, 0, Lane.PLATE_DEPTH));
        }
        return parts;
    }

    private static boolean overlap(double[] a, double[] b) {
        for (int i = 0; i < 3; i++) {
            if (a[i + 3] <= b[i] || b[i + 3] <= a[i]) {
                return false;
            }
        }
        return true;
    }

    @Test
    void lanesAreDisjoint() {
        Lane[] lanes = Lane.values();
        for (int i = 0; i < lanes.length; i++) {
            for (int j = i + 1; j < lanes.length; j++) {
                for (double[] a : parts(lanes[i])) {
                    for (double[] b : parts(lanes[j])) {
                        assertFalse(overlap(a, b), lanes[i] + " and " + lanes[j] + " overlap");
                    }
                }
            }
        }
    }

    @Test
    void partsStayInsideTheBlock() {
        for (Lane lane : Lane.values()) {
            for (double[] b : parts(lane)) {
                for (int i = 0; i < 3; i++) {
                    assertTrue(b[i] >= 0 && b[i + 3] <= 16 && b[i] <= b[i + 3], lane + " part outside the block");
                }
            }
        }
    }
}
