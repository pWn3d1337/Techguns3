package techguns.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Where a player's face/back/hand/mouth gear lives: Curios ({@code compat/curios}) or Techguns' own slots
 * ({@link FallbackSlots}). Core code only talks to {@link ExtraSlots}.
 */
public interface ExtraSlotProvider {
    /** The stack in the slot (the live stack, changes are kept), or {@link ItemStack#EMPTY}. */
    ItemStack get(Player player, TGSlotType slot);

    /** Puts a stack into the slot (server side); returns false if the provider has no such slot for the player. */
    boolean set(Player player, TGSlotType slot, ItemStack stack);

    /** Tells clients about a change made through {@link #set} or to the live stack (the fallback slots need it). */
    default void markChanged(Player player) {
    }

    /** Whether the slot's gear is drawn on the player (Curios' per-slot visibility toggle); the fallback slots always are. */
    default boolean isRendered(Player player, TGSlotType slot) {
        return true;
    }
}
