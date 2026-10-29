package techguns.item;

import java.util.List;
import java.util.Optional;

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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Bundle-like container for ammo ({@link StorageBag}). Right-click the bag on an item in an inventory slot to store it,
 * right-click it on an empty slot to take stacks out, use it to drop everything. Capacity is counted in rounds (see
 * {@link AmmoBagContents}). Contents live in the {@code ammo_bag_contents} component, accessed via {@link AmmoBagData}.
 */
public class AmmoBagItem extends Item implements StorageBag {
    private static final int BAR_COLOR = FastColor.ARGB32.color(255, 102, 102, 255);

    private final int capacity;

    public AmmoBagItem(Properties properties, int capacity) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public boolean canStore(ItemStack stack) {
        return AmmoBagData.isAmmo(stack);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack bag, Slot slot, ClickAction action, Player player) {
        return StorageBag.stackedOnOther(bag, slot, action, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bag, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return StorageBag.otherStackedOnMe(bag, other, slot, action, player, access);
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
        return StorageBag.tooltipImage(bag);
    }

    @Override
    public void appendHoverText(ItemStack bag, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(StorageBag.fullness(bag));
    }

    @Override
    public boolean isBarVisible(ItemStack bag) {
        return StorageBag.isBarVisible(bag);
    }

    @Override
    public int getBarWidth(ItemStack bag) {
        return StorageBag.barWidth(bag);
    }

    @Override
    public int getBarColor(ItemStack bag) {
        return BAR_COLOR;
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        StorageBag.onDestroyed(entity);
    }
}
