package techguns.transport;

import java.util.List;
import java.util.function.IntSupplier;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import techguns.core.transport.Lane;

/**
 * A kind of Techguns pipe (energy, fluid, items; heat or gas later): its lane in the shared pipe block, the block
 * capability it moves, its tiers and how a network of it moves things ({@link PipeKindLogic}). Registered in
 * {@link TGPipeKinds}.
 *
 * @param name        id path ({@code techguns:<name>}), also the texture prefix ({@code block/pipe/<name>_<tier>})
 * @param itemSuffix  item name after the tier ({@code basic_<itemSuffix>})
 * @param rates       per tier: what one zone passes per tick (FE, mB) or per operation (items)
 * @param intervals   per tier: ticks between active pulls (items), 1 otherwise
 */
public record PipeKind(String name, String itemSuffix, Lane lane, BlockCapability<?, Direction> capability, List<IntSupplier> rates,
                       List<IntSupplier> intervals, PipeKindLogic logic) {
    public static final String[] TIER_NAMES = {"basic", "advanced", "elite"};

    public int tierCount() {
        return rates.size();
    }

    public int rate(int tier) {
        return rates.get(Math.max(0, Math.min(tier, rates.size() - 1))).getAsInt();
    }

    public int interval(int tier) {
        return Math.max(1, intervals.get(Math.max(0, Math.min(tier, intervals.size() - 1))).getAsInt());
    }

    /** Registry name of the item of {@code tier}. */
    public String itemName(int tier) {
        return TIER_NAMES[tier] + "_" + itemSuffix;
    }

    /** Texture name (under {@code block/pipe/}) of {@code tier}. */
    public String textureName(int tier) {
        return name + "_" + TIER_NAMES[tier];
    }

    @Override
    public String toString() {
        return name;
    }
}
