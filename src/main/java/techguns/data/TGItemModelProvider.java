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
        basicItem(TGItems.WORLDGEN_TEST_TOOL.get());
        basicItem(TGItems.BUILDING_SCAN_TOOL.get());
        techguns.registry.TGNpcs.npcs().forEach(npc -> withExistingParent(npc.egg().getId().getPath(), mcLoc("item/template_spawn_egg")));
        TGAmmo.flatAmmo().forEach(item -> basicItem(item.get()));
        // steam and power armor are drawn as their armor model (Techguns 2 RenderArmorItem)
        TGArmors.armors().stream().filter(item -> !TGArmors.modelIconArmors().contains(item)).forEach(item -> basicItem(item.get()));
        TGArmors.modelIconArmors().forEach(this::codeRendered);
        TGSlotItems.items().forEach(item -> basicItem(item.get()));
        TGSlotItems.components().forEach(item -> basicItem(item.get()));
        // drawn by the shield renderer; vanilla shield display transforms and blocking pose. Overrides aren't inherited from
        // the parent, so each shield needs its own blocking override and model (vanilla item/shield_blocking)
        TGArmors.shields().forEach(item -> {
            String path = item.getId().getPath();
            ModelFile blocking = withExistingParent(path + "_blocking", mcLoc("item/shield_blocking"));
            withExistingParent(path, mcLoc("item/shield")).override().predicate(mcLoc("blocking"), 1.0f).model(blocking).end();
        });

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
