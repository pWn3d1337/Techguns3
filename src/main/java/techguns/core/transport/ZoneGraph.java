package techguns.core.transport;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.LongToIntFunction;
import java.util.function.LongFunction;

/**
 * The pipes of one network as a graph of nodes (packed positions), grouped into zones: connected nodes of the same tier.
 * Every zone has a flow budget per tick (its tier's rate); a transfer between two zones takes the widest path through
 * the zone graph (the path whose smallest remaining budget is largest, then the fewest zones) and uses budget in every
 * zone on it, so a slow section only limits the flow that has to pass through it. Pure Java, no Minecraft types.
 */
public final class ZoneGraph {
    private final long[] nodes;
    private final Map<Long, Integer> index = new HashMap<>();
    private final int[][] links;
    private final int[] zoneOf;
    private final int[] zoneTier;
    private final int[][] zoneLinks;

    /**
     * @param nodes      node positions
     * @param tier       tier of a node
     * @param neighbours linked node positions of a node (others than nodes of this graph are ignored)
     */
    public ZoneGraph(long[] nodes, LongToIntFunction tier, LongFunction<long[]> neighbours) {
        this.nodes = nodes.clone();
        for (int i = 0; i < nodes.length; i++) {
            index.put(nodes[i], i);
        }
        links = new int[nodes.length][];
        for (int i = 0; i < nodes.length; i++) {
            links[i] = Arrays.stream(neighbours.apply(nodes[i])).mapToInt(p -> index.getOrDefault(p, -1)).filter(j -> j >= 0).toArray();
        }
        zoneOf = new int[nodes.length];
        Arrays.fill(zoneOf, -1);
        List<Integer> tiers = new ArrayList<>();
        for (int start = 0; start < nodes.length; start++) {
            if (zoneOf[start] >= 0) {
                continue;
            }
            int zone = tiers.size();
            int t = tier.applyAsInt(nodes[start]);
            tiers.add(t);
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(start);
            zoneOf[start] = zone;
            while (!queue.isEmpty()) {
                int n = queue.poll();
                for (int m : links[n]) {
                    if (zoneOf[m] < 0 && tier.applyAsInt(nodes[m]) == t) {
                        zoneOf[m] = zone;
                        queue.add(m);
                    }
                }
            }
        }
        zoneTier = tiers.stream().mapToInt(Integer::intValue).toArray();
        List<Set<Integer>> zl = new ArrayList<>();
        for (int z = 0; z < zoneTier.length; z++) {
            zl.add(new TreeSet<>());
        }
        for (int n = 0; n < nodes.length; n++) {
            for (int m : links[n]) {
                if (zoneOf[n] != zoneOf[m]) {
                    zl.get(zoneOf[n]).add(zoneOf[m]);
                }
            }
        }
        zoneLinks = new int[zoneTier.length][];
        for (int z = 0; z < zoneTier.length; z++) {
            zoneLinks[z] = zl.get(z).stream().mapToInt(Integer::intValue).toArray();
        }
    }

    public int nodeCount() {
        return nodes.length;
    }

    public int zoneCount() {
        return zoneTier.length;
    }

    /** Zone of the node at {@code pos}, or -1. */
    public int zoneAt(long pos) {
        Integer i = index.get(pos);
        return i == null ? -1 : zoneOf[i];
    }

    public int zoneTier(int zone) {
        return zoneTier[zone];
    }

    /** Hop distances from the node at {@code pos} to every reachable node position. */
    public Map<Long, Integer> distancesFrom(long pos) {
        Map<Long, Integer> dist = new HashMap<>();
        Integer start = index.get(pos);
        if (start == null) {
            return dist;
        }
        int[] d = new int[nodes.length];
        Arrays.fill(d, -1);
        d[start] = 0;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            int n = queue.poll();
            dist.put(nodes[n], d[n]);
            for (int m : links[n]) {
                if (d[m] < 0) {
                    d[m] = d[n] + 1;
                    queue.add(m);
                }
            }
        }
        return dist;
    }

    /**
     * The widest path of zones from {@code from} to {@code to} under the remaining {@code budget} of each zone.
     *
     * @return the zones on the path (both ends included), or an empty array if no path has budget left
     */
    public int[] widestPath(int from, int to, int[] budget) {
        int n = zoneTier.length;
        int[] width = new int[n];
        int[] hops = new int[n];
        int[] prev = new int[n];
        boolean[] done = new boolean[n];
        Arrays.fill(prev, -1);
        width[from] = budget[from];
        while (true) {
            int best = -1;
            for (int z = 0; z < n; z++) {
                if (!done[z] && width[z] > 0 && (best < 0 || width[z] > width[best] || width[z] == width[best] && hops[z] < hops[best])) {
                    best = z;
                }
            }
            if (best < 0) {
                return new int[0];
            }
            if (best == to) {
                break;
            }
            done[best] = true;
            for (int next : zoneLinks[best]) {
                int w = Math.min(width[best], budget[next]);
                if (!done[next] && (w > width[next] || w == width[next] && w > 0 && hops[best] + 1 < hops[next])) {
                    width[next] = w;
                    hops[next] = hops[best] + 1;
                    prev[next] = best;
                }
            }
        }
        int[] path = new int[hops[to] + 1];
        for (int z = to, i = path.length - 1; i >= 0; z = prev[z], i--) {
            path[i] = z;
        }
        return path;
    }

    /** The smallest remaining budget on {@code path}, 0 for an empty path. */
    public static int bottleneck(int[] path, int[] budget) {
        if (path.length == 0) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        for (int z : path) {
            min = Math.min(min, budget[z]);
        }
        return min;
    }

    /** Uses {@code amount} of the budget of every zone on {@code path}. */
    public static void use(int[] path, int[] budget, int amount) {
        for (int z : path) {
            budget[z] = Math.max(0, budget[z] - amount);
        }
    }
}
