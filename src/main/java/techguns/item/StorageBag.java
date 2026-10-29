package techguns.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;

/**
 * A bundle-like item that stores some kinds of items (Ammo Bag: ammo, Auto-Feeder: food) in the {@code ammo_bag_contents}
 * component ({@link AmmoBagData}). Capacity is counted like the Ammo Bag's ({@link AmmoBagContents}). The static
 * methods are the shared bundle behaviour the items delegate to: right-click the bag on an item in a slot to store it,
 * on an empty slot to take a stack out, right-click a stack onto the bag to store it, the contents tooltip and the fill
 * bar.
 */
// MIGRATION(26.x): the click/tooltip/bar overrides and ItemUtils.onContainerDestroyed were reworked in 1.21.2+.
public interface StorageBag {
    /** Capacity in rounds (a full stack of anything is 64). */
    int capacity();

    /** Whether the bag takes this stack. */
    boolean canStore(ItemStack stack);

    static boolean stackedOnOther(ItemStack bag, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }
        ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty()) {
            AmmoBagData.removeFirst(bag).ifPresent(removed -> {
                playRemoveSound(player);
                ItemStack left = slot.safeInsert(removed);
                if (!left.isEmpty()) {
                    AmmoBagData.insert(bag, left);
                }
            });
        } else if (slotStack.getItem() instanceof StorageBag) {
            AmmoBagData.transferAll(bag, slotStack);
            playInsertSound(player);
        } else {
            int amount = AmmoBagData.maxAmountToAdd(bag, slotStack);
            if (amount > 0) {
                AmmoBagData.insert(bag, slot.safeTake(slotStack.getCount(), amount, player));
                playInsertSound(player);
            }
        }
        return true;
    }

    static boolean otherStackedOnMe(ItemStack bag, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        if (other.isEmpty()) {
            AmmoBagData.removeFirst(bag).ifPresent(removed -> {
                playRemoveSound(player);
                access.set(removed);
            });
        } else if (AmmoBagData.insert(bag, other) > 0) {
            playInsertSound(player);
        }
        return true;
    }

    static Optional<TooltipComponent> tooltipImage(ItemStack bag) {
        AmmoBagContents contents = AmmoBagData.contents(bag);
        return Optional.of(new AmmoBagTooltip(contents.items(), contents.occupancy(), AmmoBagData.capacity(bag)));
    }

    static Component fullness(ItemStack bag) {
        return Component.translatable("item.minecraft.bundle.fullness", AmmoBagData.occupancy(bag), AmmoBagData.capacity(bag))
                .withStyle(ChatFormatting.GRAY);
    }

    static boolean isBarVisible(ItemStack bag) {
        return AmmoBagData.occupancy(bag) > 0;
    }

    static int barWidth(ItemStack bag) {
        return Math.min(1 + 12 * AmmoBagData.occupancy(bag) / Math.max(1, AmmoBagData.capacity(bag)), 13);
    }

    static void onDestroyed(ItemEntity entity) {
        List<ItemStack> contents = AmmoBagData.contents(entity.getItem()).items().stream().map(ItemStack::copy).toList();
        ItemUtils.onContainerDestroyed(entity, contents);
    }

    private static void playInsertSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playRemoveSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }
}
