package techguns.data;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
import techguns.Techguns;
import techguns.registry.TGAmmo;
import techguns.registry.TGArmors;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;
import techguns.registry.TGSlotItems;

public class TGItemModelProvider extends ItemModelProvider {
    public TGItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Techguns.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        TGItems.simpleItems().forEach(item -> basicItem(item.get()));
        TGItems.oreDrillHeads().forEach(item -> basicItem(item.get()));
        handheldItem(TGItems.CROWBAR.get());
        handheldItem(TGItems.COMBAT_KNIFE.get());
        techguns.registry.TGNpcs.npcs().forEach(npc -> withExistingParent(npc.egg().getId().getPath(), mcLoc("item/template_spawn_egg")));
        TGAmmo.flatAmmo().forEach(item -> basicItem(item.get()));
        TGArmors.armors().forEach(item -> basicItem(item.get()));
        TGSlotItems.items().forEach(item -> basicItem(item.get()));
        TGSlotItems.components().forEach(item -> basicItem(item.get()));
        // drawn by the shield renderer; vanilla shield display transforms and blocking pose
        TGArmors.shields().forEach(item -> withExistingParent(item.getId().getPath(), mcLoc("item/shield")));

        // drawn by code (TGItemRenderer); no display transforms, the renderer positions the model itself
        TGGuns.guns().forEach(this::codeRendered);
        TGAmmo.modelAmmo().forEach(this::codeRendered);
        codeRendered(TGGuns.STIELGRANATE);
        codeRendered(TGGuns.FRAG_GRENADE);
    }

    // MIGRATION(26.x): becomes an item model definition with a special model renderer (1.21.4+).
    private void codeRendered(DeferredItem<? extends Item> item) {
        getBuilder(item.getId().getPath()).parent(new ModelFile.UncheckedModelFile("builtin/entity")).guiLight(net.minecraft.client.renderer.block.model.BlockModel.GuiLight.FRONT);
    }
}
