package techguns.slot;

import java.util.List;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.network.TGNetwork;
import techguns.registry.TGAttachments;

/**
 * Techguns' own extra slots when Curios is absent: a three-stack inventory in the {@code techguns:extra_slots}
 * attachment (saved, kept on death like the player inventory rules say), synced to everybody tracking the player.
 */
public final class FallbackSlots implements ExtraSlotProvider {
    /** Attachment data: one stack per {@link TGSlotType}. */
    public static final class Inventory {
        public static final Codec<Inventory> CODEC = ItemStack.OPTIONAL_CODEC.listOf().xmap(list -> {
            Inventory inventory = new Inventory();
            for (int i = 0; i < list.size() && i < inventory.stacks.size(); i++) {
                inventory.stacks.set(i, list.get(i));
            }
            return inventory;
        }, inventory -> List.copyOf(inventory.stacks));

        private final NonNullList<ItemStack> stacks = NonNullList.withSize(TGSlotType.values().length, ItemStack.EMPTY);

        public ItemStack get(TGSlotType slot) {
            return stacks.get(slot.ordinal());
        }

        public void set(TGSlotType slot, ItemStack stack) {
            stacks.set(slot.ordinal(), stack);
        }

        public NonNullList<ItemStack> stacks() {
            return stacks;
        }
    }

    public static Inventory inventory(Player player) {
        return player.getData(TGAttachments.EXTRA_SLOTS);
    }

    @Override
    public ItemStack get(Player player, TGSlotType slot) {
        return inventory(player).get(slot);
    }

    @Override
    public boolean set(Player player, TGSlotType slot, ItemStack stack) {
        inventory(player).set(slot, stack);
        markChanged(player);
        return true;
    }

    @Override
    public void markChanged(Player player) {
        TGNetwork.syncExtraSlots(player);
    }
}
