package techguns.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/**
 * Techguns 2 auto-feeder ({@code TGTickHandler}), shared by the food slots of the Techguns inventory tab
 * ({@link TabSlots}) and the Auto-Feeder item: one step per tick while the player is hungry (food level 19 or less),
 * the kept food points first, else the next meal. What doesn't fit into the food bar is kept and fed first next time,
 * so nothing is wasted (Techguns 2 {@code foodleft}). Players without hunger (creative, spectator) aren't fed.
 */
public final class AutoFeeding {
    private AutoFeeding() {
    }

    /** Where the food comes from. */
    public interface Source {
        /** Food points of the last meal that didn't fit into the food bar. */
        int foodLeft();

        void setFoodLeft(int points);

        /** Takes one food item out of the source, or returns {@link ItemStack#EMPTY} if there is none. */
        ItemStack takeMeal(Player player);
    }

    /** @return whether the player was fed */
    public static boolean feed(Player player, Source source) {
        FoodData food = player.getFoodData();
        int needed = FoodConstants.MAX_FOOD - food.getFoodLevel();
        if (needed <= 0 || player.getAbilities().invulnerable) {
            return false;
        }
        int left = source.foodLeft();
        if (left > 0) {
            int points = Math.min(left, needed);
            // the saturation came with the meal
            food.eat(points, 0.0f);
            source.setFoodLeft(left - points);
            return true;
        }
        ItemStack meal = source.takeMeal(player);
        FoodProperties properties = meal.getFoodProperties(player);
        if (properties == null) {
            return false;
        }
        int before = food.getFoodLevel();
        // eaten like by hand: nutrition, saturation, effects, item specials (honey, chorus fruit), stats and the burp
        ItemStack rest = meal.finishUsingItem(player.level(), player);
        source.setFoodLeft(Math.max(0, before + properties.nutrition() - FoodConstants.MAX_FOOD));
        if (!rest.isEmpty() && !player.getInventory().add(rest)) {
            player.drop(rest, false);
        }
        return true;
    }
}
