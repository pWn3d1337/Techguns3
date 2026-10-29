package techguns.slot.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import techguns.TGConfig;
import techguns.item.AmmoBagData;
import techguns.item.StorageBag;
import techguns.registry.TGComponents;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/**
 * Auto-Feeder, the replacement of the Techguns 2 auto-feeder slots of the Techguns inventory tab: a bundle-like food
 * container ({@link StorageBag}) worn in the mouth slot. While worn, whenever the player is hungry (Techguns 2: food
 * level 19 or less) it feeds them the first stored food like eating it (effects, bowls, stats); what doesn't fit into
 * the food bar is kept on the Auto-Feeder and fed first next time, so nothing is wasted (Techguns 2 {@code foodleft}).
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
     * Techguns 2 auto-feeder ({@code TGTickHandler}): one step per tick while the player is hungry, the kept food points
     * first, else the first stored food. Players without hunger (creative, spectator) aren't fed.
     *
     * @return whether the player was fed
     */
    public static boolean feed(Player player, ItemStack feeder) {
        FoodData food = player.getFoodData();
        int needed = FoodConstants.MAX_FOOD - food.getFoodLevel();
        if (needed <= 0 || player.getAbilities().invulnerable) {
            return false;
        }
        int left = foodLeft(feeder);
        if (left > 0) {
            int points = Math.min(left, needed);
            // the saturation came with the meal
            food.eat(points, 0.0f);
            setFoodLeft(feeder, left - points);
            return true;
        }
        List<ItemStack> stored = AmmoBagData.contents(feeder).items();
        if (stored.isEmpty()) {
            return false;
        }
        ItemStack meal = stored.get(0).copyWithCount(1);
        FoodProperties properties = meal.getFoodProperties(player);
        if (properties == null || AmmoBagData.remove(feeder, meal, 1) <= 0) {
            return false;
        }
        int before = food.getFoodLevel();
        // eaten like by hand: nutrition, saturation, effects, item specials (honey, chorus fruit), stats and the burp
        ItemStack rest = meal.finishUsingItem(player.level(), player);
        setFoodLeft(feeder, before + properties.nutrition() - FoodConstants.MAX_FOOD);
        if (!rest.isEmpty() && !player.getInventory().add(rest)) {
            player.drop(rest, false);
        }
        return true;
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
