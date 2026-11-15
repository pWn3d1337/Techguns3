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

    /**
     * Points inside the diagonal adapter (as {@code techguns.transport.PipeParts#adapter}) on the face of {@code axis}:
     * a tube-wide piece from this lane's tube to the centre tube, turned 45 degrees in the face plane.
     */
    private static List<double[]> adapterPoints(Lane lane, int axis, boolean positive) {
        List<double[]> points = new ArrayList<>();
        double m = (lane.center + Lane.B.center) / 2;
        double half = Math.abs(lane.center - Lane.B.center) * Math.sqrt(2) / 2 + Lane.TUBE_HALF;
        double t = Lane.TUBE_HALF;
        double d0 = positive ? 16 - 2 * t : 0;
        int u = (axis + 1) % 3;
        int v = (axis + 2) % 3;
        for (double a = -half + 0.01; a <= half - 0.01; a += 0.25) {
            for (double b = -t + 0.01; b <= t - 0.01; b += 0.25) {
                for (double d = d0 + 0.01; d <= d0 + 2 * t - 0.01; d += 0.5) {
                    double[] p = new double[3];
                    // along the diagonal (1,1)/sqrt2 by a, across it (-1,1)/sqrt2 by b
                    p[u] = m + (a - b) / Math.sqrt(2);
                    p[v] = m + (a + b) / Math.sqrt(2);
                    p[axis] = d;
                    points.add(p);
                }
            }
        }
        return points;
    }

    private static boolean inside(double[] p, double[] box) {
        return p[0] > box[0] && p[0] < box[3] && p[1] > box[1] && p[1] < box[4] && p[2] > box[2] && p[2] < box[5];
    }

    /** Parts of the centre lane except the arm and plate on the adapter's face (no link there: the neighbour differs). */
    private static List<double[]> centreParts(int axis, boolean positive) {
        List<double[]> parts = parts(Lane.B);
        // parts() adds per axis: positive arm, negative arm, positive plate, negative plate after the core
        int base = 1 + axis * 4;
        List<double[]> skip = List.of(parts.get(base + (positive ? 0 : 1)), parts.get(base + (positive ? 2 : 3)));
        parts.removeAll(skip);
        return parts;
    }

    @Test
    void adaptersStayClearOfTheOtherKinds() {
        for (Lane lane : new Lane[]{Lane.A, Lane.C}) {
            Lane other = lane == Lane.A ? Lane.C : Lane.A;
            for (int axis = 0; axis < 3; axis++) {
                for (boolean positive : new boolean[]{true, false}) {
                    List<double[]> blocked = new ArrayList<>(parts(other));
                    blocked.addAll(centreParts(axis, positive));
                    for (double[] p : adapterPoints(lane, axis, positive)) {
                        for (int i = 0; i < 3; i++) {
                            assertTrue(p[i] >= 0 && p[i] <= 16, "adapter of " + lane + " leaves the block");
                        }
                        for (double[] box : blocked) {
                            assertFalse(inside(p, box), "adapter of " + lane + " cuts into another kind's part");
                        }
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
