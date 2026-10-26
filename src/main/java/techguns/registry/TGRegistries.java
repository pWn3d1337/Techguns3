package techguns.registry;

import net.neoforged.bus.api.IEventBus;

/**
 * Registers all Techguns deferred registers on the mod event bus.
 */
public final class TGRegistries {
    private TGRegistries() {
    }

    public static void register(IEventBus modEventBus) {
        // load item holders defined outside TGItems before the item register is attached
        TGAmmo.init();
        TGGuns.init();

        TGBlocks.BLOCKS.register(modEventBus);
        TGItems.ITEMS.register(modEventBus);
        TGEntities.ENTITY_TYPES.register(modEventBus);
        TGCreativeTabs.TABS.register(modEventBus);
        TGAttributes.ATTRIBUTES.register(modEventBus);
        TGComponents.COMPONENTS.register(modEventBus);
        TGAttachments.ATTACHMENTS.register(modEventBus);
        TGSounds.SOUNDS.register(modEventBus);
        TGParticleTypes.PARTICLE_TYPES.register(modEventBus);
        TGRecipes.SERIALIZERS.register(modEventBus);

        modEventBus.addListener(TGAttributes::addToAllLivingEntities);
    }
}
