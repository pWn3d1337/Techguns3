package techguns.registry;

import net.neoforged.bus.api.IEventBus;

/**
 * Registers all Techguns deferred registers on the mod event bus.
 */
public final class TGRegistries {
    private TGRegistries() {
    }

    public static void register(IEventBus modEventBus) {
        TGBlocks.BLOCKS.register(modEventBus);
        TGItems.ITEMS.register(modEventBus);
    }
}
