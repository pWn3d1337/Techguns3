package techguns.client.render.item;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/**
 * Item → renderer registry for items drawn by code (builtin/entity item models).
 */
public final class TGItemRenderers {
    private static final Map<Item, TGItemRenderer> RENDERERS = new HashMap<>();

    private TGItemRenderers() {
    }

    public static void register(ItemLike item, TGItemRenderer renderer) {
        RENDERERS.put(item.asItem(), renderer);
    }

    @Nullable
    public static TGItemRenderer get(Item item) {
        return RENDERERS.get(item);
    }

    public static Iterable<Item> items() {
        return RENDERERS.keySet();
    }
}
