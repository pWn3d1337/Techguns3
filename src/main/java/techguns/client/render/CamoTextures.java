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
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import techguns.camo.CamoData;
import techguns.camo.TGCamo;
import techguns.registry.TGArmors;

/**
 * Camo textures and item icon tints for the renderers, cached per item and camo name: the camo registry lookup
 * ({@link CamoData#current}) streams and sorts the whole registry, too slow for every gun, shield, slot item and armor
 * icon drawn each frame.
 */
public final class CamoTextures {
    private record Key(Item item, String camo) {
    }

    private static final Map<Key, Optional<TGCamo>> CAMOS = new HashMap<>();

    private CamoTextures() {
    }

    /** Clears the cache when the camo registry may have changed (joining a world or server). */
    public static void init(IEventBus gameBus) {
        gameBus.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> CAMOS.clear());
    }

    /** Armor icons are layered and tinted per camo (Fabric {@code TGCamos} colors, {@link TGCamo#tints}). */
    // MIGRATION(26.x): item color handlers are replaced by tint sources in the item model definition (1.21.4+).
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> camo(stack).map(camo -> camo.tint(layer)).orElse(-1),
                TGArmors.armors().stream().map(item -> item.get()).toArray(Item[]::new));
    }

    /** The texture of the stack's camo; empty if its item has no camos, no world is loaded or the camo has no texture. */
    public static Optional<ResourceLocation> of(ItemStack stack) {
        return camo(stack).flatMap(TGCamo::texture);
    }

    /** The stack's camo; empty if its item has no camos or no world is loaded. */
    private static Optional<TGCamo> camo(ItemStack stack) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return Optional.empty();
        }
        return CAMOS.computeIfAbsent(new Key(stack.getItem(), CamoData.name(stack)),
                key -> CamoData.current(level.registryAccess(), stack));
    }
}
