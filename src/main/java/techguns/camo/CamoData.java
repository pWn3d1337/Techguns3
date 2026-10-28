package techguns.camo;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGCamos;
import techguns.registry.TGComponents;

/**
 * Camo of an item stack: the {@code techguns:camo} component holds the camo name; without it (or with an unknown
 * name) the item shows its default camo (order 0).
 */
public final class CamoData {
    private CamoData() {
    }

    /** All camos of the stack's item, in cycling order; empty if it has none. */
    public static List<TGCamo> camos(HolderLookup.Provider registries, ItemStack stack) {
        Optional<? extends HolderLookup.RegistryLookup<TGCamo>> lookup = registries.lookup(TGCamos.KEY);
        if (lookup.isEmpty()) {
            return List.of();
        }
        return lookup.get().listElements().map(holder -> holder.value())
                .filter(camo -> camo.item().value() == stack.getItem())
                .sorted(Comparator.comparingInt(TGCamo::order))
                .toList();
    }

    /** The stack's current camo, or empty if its item has no camos. */
    public static Optional<TGCamo> current(HolderLookup.Provider registries, ItemStack stack) {
        List<TGCamo> camos = camos(registries, stack);
        if (camos.isEmpty()) {
            return Optional.empty();
        }
        String name = name(stack);
        return Optional.of(camos.stream().filter(camo -> camo.name().equals(name)).findFirst().orElse(camos.getFirst()));
    }

    /** The camo name stored on the stack ("" without one); may name a camo that no longer exists, see {@link #current}. */
    public static String name(ItemStack stack) {
        return stack.getOrDefault(TGComponents.CAMO.get(), "");
    }

    /** Switches to the next (or previous) camo, wrapping around; returns the new camo. */
    public static Optional<TGCamo> cycle(HolderLookup.Provider registries, ItemStack stack, boolean back) {
        List<TGCamo> camos = camos(registries, stack);
        if (camos.isEmpty()) {
            return Optional.empty();
        }
        int index = camos.indexOf(current(registries, stack).orElseThrow());
        TGCamo next = camos.get(Math.floorMod(index + (back ? -1 : 1), camos.size()));
        set(stack, next);
        return Optional.of(next);
    }

    public static void set(ItemStack stack, TGCamo camo) {
        if (camo.order() == 0) {
            stack.remove(TGComponents.CAMO.get());
        } else {
            stack.set(TGComponents.CAMO.get(), camo.name());
        }
    }
}
