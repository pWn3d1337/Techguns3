package techguns.core.transport;

/**
 * Splits an amount evenly over several receivers that each take at most a limit ("water filling"): every receiver gets
 * the same share, and what a full receiver cannot take goes to the others. Integer leftovers go to the first receivers.
 */
public final class EnergySplit {
    private EnergySplit() {
    }

    /**
     * @param amount what is to be handed out
     * @param accept the most each receiver takes (not changed)
     * @return the share of each receiver; the shares never exceed {@code accept} and sum to at most {@code amount}
     */
    public static int[] waterFill(int amount, int[] accept) {
        int n = accept.length;
        int[] give = new int[n];
        int open = 0;
        for (int limit : accept) {
            if (limit > 0) {
                open++;
            }
        }
        int left = Math.max(0, amount);
        while (left > 0 && open > 0) {
            int share = Math.max(1, left / open);
            int handed = 0;
            open = 0;
            for (int i = 0; i < n && left - handed > 0; i++) {
                int room = accept[i] - give[i];
                if (room <= 0) {
                    continue;
                }
                int add = Math.min(Math.min(share, room), left - handed);
                give[i] += add;
                handed += add;
                if (give[i] < accept[i]) {
                    open++;
                }
            }
            if (handed == 0) {
                break;
            }
            left -= handed;
        }
        return give;
    }
}
