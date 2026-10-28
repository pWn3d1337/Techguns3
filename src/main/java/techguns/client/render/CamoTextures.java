package techguns.client.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import techguns.camo.CamoData;
import techguns.camo.TGCamo;

/**
 * Camo textures for the renderers, cached per item and camo name: the camo registry lookup ({@link CamoData#current})
 * streams and sorts the whole registry, too slow for every gun, shield and slot item drawn each frame.
 */
public final class CamoTextures {
    private record Key(Item item, String camo) {
    }

    private static final Map<Key, Optional<ResourceLocation>> TEXTURES = new HashMap<>();

    private CamoTextures() {
    }

    /** Clears the cache when the camo registry may have changed (joining a world or server). */
    public static void init(IEventBus gameBus) {
        gameBus.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> TEXTURES.clear());
    }

    /** The texture of the stack's camo; empty if its item has no camos, no world is loaded or the camo has no texture. */
    public static Optional<ResourceLocation> of(ItemStack stack) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return Optional.empty();
        }
        return TEXTURES.computeIfAbsent(new Key(stack.getItem(), CamoData.name(stack)),
                key -> CamoData.current(level.registryAccess(), stack).flatMap(TGCamo::texture));
    }
}
