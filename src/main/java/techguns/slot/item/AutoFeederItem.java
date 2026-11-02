package techguns.slot.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import techguns.TGConfig;
import techguns.item.AmmoBagData;
import techguns.item.StorageBag;
import techguns.registry.TGComponents;
import techguns.slot.AutoFeeding;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/**
 * Auto-Feeder, the replacement of the Techguns 2 auto-feeder slots of the Techguns inventory tab: a bundle-like food
 * container ({@link StorageBag}) worn in the mouth slot. While worn it feeds the hungry player its stored food like the
 * food slots of the inventory tab do ({@link AutoFeeding}); the leftover food points are kept on the Auto-Feeder.
 * Config {@code autoFeeder}.
 */
public class AutoFeederItem extends TGSlotItem implements StorageBag {
    private static final int BAR_COLOR = FastColor.ARGB32.color(255, 214, 140, 60);

    private final int capacity;

    public AutoFeederItem(Properties properties, int capacity) {
        super(properties, TGSlotType.MOUTH, 0);
        this.capacity = capacity;
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public boolean canStore(ItemStack stack) {
        return stack.getFoodProperties(null) != null;
    }

    public static int foodLeft(ItemStack feeder) {
        return feeder.getOrDefault(TGComponents.AUTO_FEEDER_FOOD_LEFT.get(), 0);
    }

    private static void setFoodLeft(ItemStack feeder, int points) {
        if (points > 0) {
            feeder.set(TGComponents.AUTO_FEEDER_FOOD_LEFT.get(), points);
        } else {
            feeder.remove(TGComponents.AUTO_FEEDER_FOOD_LEFT.get());
        }
    }

    @Override
    public void slotTick(Player player, ItemStack feeder) {
        super.slotTick(player, feeder);
        if (!player.level().isClientSide && TGConfig.AUTO_FEEDER.get() && feed(player, feeder)) {
            ExtraSlots.markChanged(player);
        }
    }

    /**
     * One {@link AutoFeeding} step from the first stored food, the kept food points first.
     *
     * @return whether the player was fed
     */
    public static boolean feed(Player player, ItemStack feeder) {
        return AutoFeeding.feed(player, new AutoFeeding.Source() {
            @Override
            public int foodLeft() {
                return AutoFeederItem.foodLeft(feeder);
            }

            @Override
            public void setFoodLeft(int points) {
                AutoFeederItem.setFoodLeft(feeder, points);
            }

            @Override
            public ItemStack takeMeal(Player player) {
                List<ItemStack> stored = AmmoBagData.contents(feeder).items();
                if (stored.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                ItemStack meal = stored.get(0).copyWithCount(1);
                return meal.getFoodProperties(player) != null && AmmoBagData.remove(feeder, meal, 1) > 0 ? meal : ItemStack.EMPTY;
            }
        });
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack feeder, Slot slot, ClickAction action, Player player) {
        return StorageBag.stackedOnOther(feeder, slot, action, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack feeder, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return StorageBag.otherStackedOnMe(feeder, other, slot, action, player, access);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack feeder) {
        return StorageBag.tooltipImage(feeder);
    }

    @Override
    public void appendHoverText(ItemStack feeder, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(StorageBag.fullness(feeder));
        super.appendHoverText(feeder, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.techguns.auto_feeder").withStyle(ChatFormatting.DARK_GREEN));
        int left = foodLeft(feeder);
        if (left > 0) {
            tooltip.add(Component.translatable("tooltip.techguns.auto_feeder.left", left).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack feeder) {
        return StorageBag.isBarVisible(feeder);
    }

    @Override
    public int getBarWidth(ItemStack feeder) {
        return StorageBag.barWidth(feeder);
    }

    @Override
    public int getBarColor(ItemStack feeder) {
        return BAR_COLOR;
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        StorageBag.onDestroyed(entity);
    }
}
