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
        TGArmors.init();
        TGSlotItems.init();
        TGNpcs.init();
        TGFluids.init(modEventBus);

        TGBlocks.BLOCKS.register(modEventBus);
        TGArmors.MATERIALS.register(modEventBus);
        TGItems.ITEMS.register(modEventBus);
        TGEntities.ENTITY_TYPES.register(modEventBus);
        TGCreativeTabs.TABS.register(modEventBus);
        TGAttributes.ATTRIBUTES.register(modEventBus);
        TGComponents.COMPONENTS.register(modEventBus);
        TGAttachments.ATTACHMENTS.register(modEventBus);
        TGSounds.SOUNDS.register(modEventBus);
        TGParticleTypes.PARTICLE_TYPES.register(modEventBus);
        TGRecipes.SERIALIZERS.register(modEventBus);
        TGRecipes.TYPES.register(modEventBus);
        TGBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        TGMenus.MENUS.register(modEventBus);
        TGBiomeModifiers.SERIALIZERS.register(modEventBus);
        TGStructureProcessors.PROCESSORS.register(modEventBus);
        TGStructures.STRUCTURE_TYPES.register(modEventBus);
        TGStructures.PIECE_TYPES.register(modEventBus);

        modEventBus.addListener(TGAttributes::addToAllLivingEntities);
        modEventBus.addListener(TGCamos::register);
        modEventBus.addListener(TGEntities::registerAttributes);
        modEventBus.addListener(TGNpcs::registerAttributes);
        TGBlockEntities.init(modEventBus);
    }
}
