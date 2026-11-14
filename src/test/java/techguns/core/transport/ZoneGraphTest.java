package techguns.core.transport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ZoneGraphTest {
    /** A line of nodes 0..n-1 with the given tiers. */
    private static ZoneGraph line(int... tiers) {
        long[] nodes = new long[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            nodes[i] = i;
        }
        return new ZoneGraph(nodes, pos -> tiers[(int) pos], pos -> new long[]{pos - 1, pos + 1});
    }

    @Test
    void sameTierNodesFormOneZone() {
        ZoneGraph graph = line(0, 0, 0);
        assertEquals(1, graph.zoneCount());
        assertEquals(3, graph.nodeCount());
    }

    @Test
    void tierChangesSplitZones() {
        ZoneGraph graph = line(2, 2, 0, 2);
        assertEquals(3, graph.zoneCount());
        assertEquals(graph.zoneAt(0), graph.zoneAt(1));
        assertNotEquals(graph.zoneAt(1), graph.zoneAt(2));
        assertNotEquals(graph.zoneAt(2), graph.zoneAt(3));
    }

    @Test
    void slowSectionLimitsThePathThroughIt() {
        ZoneGraph graph = line(2, 0, 2);
        int[] budget = {16384, 256, 16384};
        int[] path = graph.widestPath(graph.zoneAt(0), graph.zoneAt(2), budget);
        assertEquals(3, path.length);
        assertEquals(256, ZoneGraph.bottleneck(path, budget));
        ZoneGraph.use(path, budget, 256);
        assertEquals(0, ZoneGraph.bottleneck(graph.widestPath(graph.zoneAt(0), graph.zoneAt(2), budget), budget));
        // flow inside the fast zone is still possible
        assertEquals(16384 - 256, budget[graph.zoneAt(0)]);
    }

    @Test
    void widestPathAvoidsTheSlowBranch() {
        // a ring 0-1-2-3-0 split into a slow way (via 1) and a fast way (via 3): zones 0 | 1 | 2 | 3 by tier
        int[] tiers = {2, 0, 2, 1};
        Map<Long, long[]> links = new HashMap<>();
        links.put(0L, new long[]{1, 3});
        links.put(1L, new long[]{0, 2});
        links.put(2L, new long[]{1, 3});
        links.put(3L, new long[]{2, 0});
        ZoneGraph graph = new ZoneGraph(new long[]{0, 1, 2, 3}, pos -> tiers[(int) pos], links::get);
        // 0 and 2 are both elite but not adjacent, so they are separate zones
        assertEquals(4, graph.zoneCount());
        int[] budget = new int[4];
        budget[graph.zoneAt(0)] = 16384;
        budget[graph.zoneAt(1)] = 256;
        budget[graph.zoneAt(2)] = 16384;
        budget[graph.zoneAt(3)] = 2048;
        int[] path = graph.widestPath(graph.zoneAt(0), graph.zoneAt(2), budget);
        assertArrayEquals(new int[]{graph.zoneAt(0), graph.zoneAt(3), graph.zoneAt(2)}, path);
        assertEquals(2048, ZoneGraph.bottleneck(path, budget));
    }

    @Test
    void exhaustedBudgetGivesNoPath() {
        ZoneGraph graph = line(0, 1);
        int[] budget = {0, 100};
        assertEquals(0, graph.widestPath(graph.zoneAt(0), graph.zoneAt(1), budget).length);
    }

    @Test
    void distancesCountHops() {
        ZoneGraph graph = line(0, 0, 0, 0);
        assertEquals(3, graph.distancesFrom(0).get(3L));
        assertEquals(0, graph.distancesFrom(2).get(2L));
    }
}
