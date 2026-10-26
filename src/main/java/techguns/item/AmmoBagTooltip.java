package techguns.item;

import java.util.List;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/** Tooltip image data of an Ammo Bag: the stored stacks, rendered by {@code ClientAmmoBagTooltip}. */
public record AmmoBagTooltip(List<ItemStack> items, int occupancy, int capacity) implements TooltipComponent {
}
