package techguns.client.render.item;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import techguns.TGIds;

/**
 * The Techguns manual's book item model ({@code models/item/techguns_manual.json}, generated with the book). Patchouli
 * loads it by itself; Modonomicon looks the model up as {@code techguns:techguns_manual#inventory}, which only exists
 * for registered items, so the standalone model is baked and also put under that name. Without Modonomicon the alias
 * is unused.
 */
// MIGRATION(26.x): item models become data-driven item definitions; Modonomicon's book model lookup changes with them.
public final class GuideBookModel {
    private static final ModelResourceLocation STANDALONE = ModelResourceLocation.standalone(TGIds.id("item/techguns_manual"));
    private static final ModelResourceLocation INVENTORY = new ModelResourceLocation(TGIds.id("techguns_manual"), "inventory");

    private GuideBookModel() {
    }

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        event.register(STANDALONE);
    }

    public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        BakedModel model = event.getModels().get(STANDALONE);
        if (model != null) {
            event.getModels().put(INVENTORY, model);
        }
    }
}
