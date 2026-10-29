package techguns.compat.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.slot.ExtraSlotProvider;
import techguns.slot.TGSlotType;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Extra slots backed by Curios' {@code head}/{@code back}/{@code hands} slots and the Techguns {@code mouth} slot
 * (first index), including Curios' visibility toggle. Only loaded when Curios is installed; Curios syncs its inventory
 * and render states itself.
 */
// MIGRATION(26.x): Curios API (ICuriosItemHandler / stacks handlers) differs per version.
public final class CuriosExtraSlots implements ExtraSlotProvider {
    private CuriosExtraSlots() {
    }

    public static ExtraSlotProvider create() {
        return new CuriosExtraSlots();
    }

    @Override
    public ItemStack get(Player player, TGSlotType slot) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.getStacksHandler(slot.curiosId()))
                .map(ICurioStacksHandler::getStacks)
                .filter(stacks -> stacks.getSlots() > 0)
                .map(stacks -> stacks.getStackInSlot(0))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean set(Player player, TGSlotType slot, ItemStack stack) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.getStacksHandler(slot.curiosId()))
                .filter(stacks -> stacks.getSlots() > 0)
                .map(stacks -> {
                    stacks.getStacks().setStackInSlot(0, stack);
                    return true;
                })
                .orElse(false);
    }

    /** The slot's render toggle (the eye button of the Curios screen), synced by Curios to everybody tracking the player. */
    @Override
    public boolean isRendered(Player player, TGSlotType slot) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.getStacksHandler(slot.curiosId()))
                .map(ICurioStacksHandler::getRenders)
                .filter(renders -> !renders.isEmpty())
                .map(renders -> renders.get(0))
                .orElse(true);
    }

    /** Switches the slot's render toggle like the Curios screen's button (GameTests); false if the player has no such slot. */
    public static boolean setRendered(Player player, TGSlotType slot, boolean rendered) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.getStacksHandler(slot.curiosId()))
                .filter(stacks -> !stacks.getRenders().isEmpty())
                .map(stacks -> {
                    stacks.getRenders().set(0, rendered);
                    return true;
                })
                .orElse(false);
    }
}
