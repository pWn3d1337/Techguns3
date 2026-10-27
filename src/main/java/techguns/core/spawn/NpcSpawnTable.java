package techguns.core.spawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntUnaryOperator;

/**
 * Techguns 2 {@code TGNpcSpawnTable}: weighted spawn entries by danger level. A pick at danger {@code d} draws from all
 * entries of levels 0..d.
 */
public final class NpcSpawnTable<T> {
    private record Entry<T>(T type, int weight, int danger) {
    }

    private final List<Entry<T>> entries = new ArrayList<>();

    /** Entries with weight 0 are left out (Techguns 2: disabled in the config); danger is clamped to 0..{@link SpawnDanger#MAX}. */
    public NpcSpawnTable<T> add(T type, int weight, int danger) {
        if (weight > 0) {
            entries.add(new Entry<>(type, weight, Math.clamp(danger, 0, SpawnDanger.MAX)));
        }
        return this;
    }

    /** Total weight of the entries allowed at {@code danger}. */
    public int totalWeight(int danger) {
        int total = 0;
        for (Entry<T> entry : entries) {
            if (entry.danger <= danger) {
                total += entry.weight;
            }
        }
        return total;
    }

    /** A weighted pick among the entries of danger 0..{@code danger}; {@code random} returns 0..bound-1. */
    public Optional<T> pick(int danger, IntUnaryOperator random) {
        int total = totalWeight(danger);
        if (total <= 0) {
            return Optional.empty();
        }
        int roll = random.applyAsInt(total);
        for (Entry<T> entry : entries) {
            if (entry.danger <= danger) {
                roll -= entry.weight;
                if (roll < 0) {
                    return Optional.of(entry.type);
                }
            }
        }
        return Optional.empty();
    }
}
