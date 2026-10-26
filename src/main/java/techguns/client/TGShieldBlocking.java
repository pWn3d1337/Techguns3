package techguns.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import techguns.TGIds;
import techguns.registry.TGArmors;

/** Vanilla only registers the blocking item property for the vanilla shield; item/shield's override needs it. */
// MIGRATION(26.x): item properties are replaced by item model definitions (1.21.4); use the using_item condition.
final class TGShieldBlocking {
    private TGShieldBlocking() {
    }

    static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> TGArmors.shields().forEach(item ->
                ItemProperties.register(item.get(), TGIds.vanilla("blocking"),
                        (stack, level, entity, seed) ->
                                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F)));
    }
}
