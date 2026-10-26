package techguns.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Bundle-like container for ammo. Right-click the bag on an item in an inventory slot to store it, right-click it
 * on an empty slot to take stacks out, use it to drop everything. Capacity is counted in rounds (see
 * {@link AmmoBagContents}). Contents live in the {@code ammo_bag_contents} component, accessed via {@link AmmoBagData}.
 */
// MIGRATION(26.x): the click/tooltip/bar overrides below and ItemUtils.onContainerDestroyed were reworked in 1.21.2+.
public class AmmoBagItem extends Item {
    private static final int BAR_COLOR = FastColor.ARGB32.color(255, 102, 102, 255);

    private final int capacity;

    public AmmoBagItem(Properties properties, int capacity) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
    }

    /** Capacity in rounds. */
    public int capacity() {
        return capacity;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack bag, Slot slot, ClickAction action, Player player) {
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
        } else if (AmmoBagData.isBag(slotStack)) {
            AmmoBagData.transferAll(bag, slotStack);
            playInsertSound(player);
        } else if (AmmoBagData.isAmmo(slotStack)) {
            int amount = AmmoBagData.maxAmountToAdd(bag, slotStack);
            if (amount > 0) {
                AmmoBagData.insert(bag, slot.safeTake(slotStack.getCount(), amount, player));
                playInsertSound(player);
            }
        }
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bag, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        List<ItemStack> items = AmmoBagData.clear(bag);
        if (items.isEmpty()) {
            return InteractionResultHolder.fail(bag);
        }
        player.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + level.getRandom().nextFloat() * 0.4F);
        if (player instanceof ServerPlayer) {
            items.forEach(stack -> player.drop(stack, true));
        }
        return InteractionResultHolder.sidedSuccess(bag, level.isClientSide());
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack bag) {
        AmmoBagContents contents = AmmoBagData.contents(bag);
        return Optional.of(new AmmoBagTooltip(contents.items(), contents.occupancy(), capacity));
    }

    @Override
    public void appendHoverText(ItemStack bag, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.minecraft.bundle.fullness", AmmoBagData.occupancy(bag), capacity).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack bag) {
        return AmmoBagData.occupancy(bag) > 0;
    }

    @Override
    public int getBarWidth(ItemStack bag) {
        return Math.min(1 + 12 * AmmoBagData.occupancy(bag) / capacity, 13);
    }

    @Override
    public int getBarColor(ItemStack bag) {
        return BAR_COLOR;
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
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
