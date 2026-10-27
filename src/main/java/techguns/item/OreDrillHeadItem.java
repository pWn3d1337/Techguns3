package techguns.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Ore Drill head (Techguns 2 {@code OREDRILLHEAD_*}): fits drills of its size, mines clusters up to its mining level
 * (steel 1, obsidian steel 2, carbon 3) plus the drill radius.
 */
public class OreDrillHeadItem extends Item {
    /** Drill sizes by drill radius (Techguns 2 {@code getDrillRadius}: 0 mini drill, 1-2 medium, 3-4 large). */
    public enum Size {
        SMALL, MEDIUM, LARGE;

        public static Size forDrillRadius(int drillRadius) {
            return drillRadius == 0 ? SMALL : drillRadius <= 2 ? MEDIUM : LARGE;
        }
    }

    private final Size size;
    private final int miningLevel;

    public OreDrillHeadItem(Properties properties, Size size, int miningLevel) {
        super(properties.stacksTo(1));
        this.size = size;
        this.miningLevel = miningLevel;
    }

    public Size size() {
        return size;
    }

    public int miningLevel() {
        return miningLevel;
    }

    /** The head's mining level on a drill of {@code drillRadius}, or -1 if it doesn't fit. */
    public static int miningLevel(ItemStack stack, int drillRadius) {
        return stack.getItem() instanceof OreDrillHeadItem head && head.size == Size.forDrillRadius(drillRadius) ? head.miningLevel : -1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("techguns.oredrill.drillsize." + size.name().toLowerCase(java.util.Locale.ROOT)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("techguns.oredrill.mininglevel", miningLevel).withStyle(ChatFormatting.GRAY));
    }
}
