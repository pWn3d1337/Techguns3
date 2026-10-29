package techguns.debug;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import techguns.TGConfig;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;

/**
 * The Techguns 2 debug tools ({@code TGConfig.debug}): always registered, so clients and servers with different
 * settings still connect, but only in the creative tab and the recipe viewer, and only usable, with {@code debug}
 * on in the common config.
 */
public final class TGDebug {
    private static final List<Supplier<? extends ItemLike>> ITEMS = List.of(TGItems.WORLDGEN_TEST_TOOL, TGItems.BUILDING_SCAN_TOOL,
            TGBlocks.AIR_MARKER, TGBlocks.ANTI_AIR_MARKER, TGBlocks.INTERIOR_MARKER, TGBlocks.DUNGEON_SCANNER, TGBlocks.DUNGEON_GENERATOR);

    private TGDebug() {
    }

    public static boolean enabled() {
        return TGConfig.DEBUG.get();
    }

    /** True if the debug tools are on; otherwise tells {@code player} so (above the hotbar). */
    public static boolean check(@Nullable Player player) {
        if (enabled()) {
            return true;
        }
        if (player != null) {
            player.displayClientMessage(Component.translatable("techguns.debug.disabled").withStyle(ChatFormatting.RED), true);
        }
        return false;
    }

    /** The debug items, hidden from the creative tab and the recipe viewer unless debug is on. */
    public static List<Item> items() {
        return ITEMS.stream().map(item -> item.get().asItem()).toList();
    }

    public static boolean isDebugItem(Item item) {
        return items().contains(item);
    }
}
