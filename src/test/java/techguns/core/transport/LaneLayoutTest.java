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

    /** Fittings of the adapter (as {@code PipeParts#adapterJoints}): cubes a bit larger than the core at the face, 4 deep. */
    private static List<double[]> joints(Lane lane, int axis, boolean positive) {
        List<double[]> joints = new ArrayList<>();
        for (double c : new double[]{lane.center, Lane.B.center}) {
            double h = 2.25 + 0.1 * axis;
            double[] b = {c - h, c - h, c - h, c + h, c + h, c + h};
            b[axis] = positive ? 12 : 0;
            b[axis + 3] = positive ? 16 : 4;
            joints.add(b);
        }
        return joints;
    }

    /**
     * Points of the diagonal piece (as {@code PipeParts#adapter}) on the face of {@code axis}, from centre to centre,
     * tube wide, 0.5 inside the fitting depth; {@code step} 0 gives only its corners.
     */
    private static List<double[]> diagonalPoints(Lane lane, int axis, boolean positive, double step) {
        List<double[]> points = new ArrayList<>();
        double m = (lane.center + Lane.B.center) / 2;
        double half = Math.abs(lane.center - Lane.B.center) * Math.sqrt(2) / 2;
        double t = Lane.TUBE_HALF;
        double d0 = positive ? 12.5 : 0.5;
        double d1 = positive ? 15.5 : 3.5;
        int u = (axis + 1) % 3;
        int v = (axis + 2) % 3;
        double[] as = step == 0 ? new double[]{-half, half} : range(-half, half, step);
        double[] bs = step == 0 ? new double[]{-t, t} : range(-t, t, step);
        double[] ds = step == 0 ? new double[]{d0, d1} : range(d0, d1, step);
        for (double a : as) {
            for (double b : bs) {
                for (double d : ds) {
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

    private static double[] range(double from, double to, double step) {
        int n = (int) Math.floor((to - from) / step) + 1;
        double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            values[i] = Math.min(to, from + i * step);
        }
        return values;
    }

    private static boolean within(double[] p, double[] box) {
        double e = 1e-6;
        return p[0] >= box[0] - e && p[0] <= box[3] + e && p[1] >= box[1] - e && p[1] <= box[4] + e && p[2] >= box[2] - e && p[2] <= box[5] + e;
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
                    for (double[] p : diagonalPoints(lane, axis, positive, 0.25)) {
                        for (double[] box : blocked) {
                            assertFalse(inside(p, box), "adapter of " + lane + " cuts into another kind's part");
                        }
                    }
                    for (double[] joint : joints(lane, axis, positive)) {
                        for (double[] box : blocked) {
                            assertFalse(overlap(joint, box), "adapter fitting of " + lane + " cuts into another kind's part");
                        }
                        for (int i = 0; i < 3; i++) {
                            assertTrue(joint[i] >= 0 && joint[i + 3] <= 16, "adapter fitting leaves the block");
                        }
                    }
                }
            }
        }
    }

    @Test
    void fittingsShareNoFacePlaneWithTheCore() {
        for (Lane lane : new Lane[]{Lane.A, Lane.C}) {
            double[] core = parts(lane).get(0);
            for (int axis = 0; axis < 3; axis++) {
                for (boolean positive : new boolean[]{true, false}) {
                    for (double[] joint : joints(lane, axis, positive)) {
                        if (!overlap(joint, core)) {
                            continue;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (i == axis) {
                                continue;
                            }
                            assertTrue(joint[i] != core[i] && joint[i + 3] != core[i + 3],
                                    "a fitting of " + lane + " shares a face plane with the core it overlaps");
                        }
                    }
                }
            }
        }
    }

    @Test
    void overlappingFittingsShareNoFacePlane() {
        for (Lane lane : new Lane[]{Lane.A, Lane.C}) {
            List<double[]> all = new ArrayList<>();
            for (int axis = 0; axis < 3; axis++) {
                for (boolean positive : new boolean[]{true, false}) {
                    all.addAll(joints(lane, axis, positive));
                }
            }
            for (int i = 0; i < all.size(); i++) {
                for (int j = i + 1; j < all.size(); j++) {
                    double[] a = all.get(i);
                    double[] b = all.get(j);
                    if (!overlap(a, b)) {
                        continue;
                    }
                    for (int k = 0; k < 6; k++) {
                        assertTrue(a[k] != b[k], "two overlapping fittings of " + lane + " share a face plane");
                    }
                }
            }
        }
    }

    @Test
    void diagonalEndsHideInTheFittings() {
        for (Lane lane : new Lane[]{Lane.A, Lane.C}) {
            for (int axis = 0; axis < 3; axis++) {
                for (boolean positive : new boolean[]{true, false}) {
                    List<double[]> joints = joints(lane, axis, positive);
                    for (double[] corner : diagonalPoints(lane, axis, positive, 0)) {
                        assertTrue(joints.stream().anyMatch(j -> within(corner, j)),
                                "a corner of the diagonal piece of " + lane + " sticks out of its fittings");
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
