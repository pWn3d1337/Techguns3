package techguns.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Facade for a player's extra slots. Uses Curios when it is installed, else {@link FallbackSlots} (design doc Q5).
 */
public final class ExtraSlots {
    private static ExtraSlotProvider provider = new FallbackSlots();

    private ExtraSlots() {
    }

    /** Picks the provider; call once during mod construction. */
    public static void init() {
        if (ModList.get().isLoaded("curios")) {
            provider = techguns.compat.curios.CuriosExtraSlots.create();
        }
    }

    public static boolean usesCurios() {
        return !(provider instanceof FallbackSlots);
    }

    public static ItemStack get(Player player, TGSlotType slot) {
        return provider.get(player, slot);
    }

    public static boolean set(Player player, TGSlotType slot, ItemStack stack) {
        return provider.set(player, slot, stack);
    }

    public static void markChanged(Player player) {
        provider.markChanged(player);
    }

    /** Moves a held slot item into its empty slot (Techguns 2 right-click equip); returns true if it was equipped. */
    public static boolean tryEquip(Player player, ItemStack held, TGSlotType slot) {
        if (!get(player, slot).isEmpty() || !set(player, slot, held.copy())) {
            return false;
        }
        held.setCount(0);
        return true;
    }
}
