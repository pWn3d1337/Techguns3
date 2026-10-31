package techguns.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
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
        Map<DeferredItem<? extends Item>, List<String>> layered = layeredArmorIcons();
        TGArmors.armors().stream().filter(item -> !TGArmors.modelIconArmors().contains(item) && !layered.containsKey(item))
                .forEach(item -> basicItem(item.get()));
        layered.forEach((item, layers) -> {
            ItemModelBuilder builder = withExistingParent(item.getId().getPath(), mcLoc("item/generated"));
            for (int i = 0; i < layers.size(); i++) {
                builder.texture("layer" + i, modLoc("item/" + layers.get(i)));
            }
        });
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

    /**
     * Armor icons split into layers that are tinted per camo (Fabric port item models; colors in {@code TGCamoProvider}).
     * Layer i gets tint index i; T3 exo reuses some T3 combat layers. Single-layer tinted icons (T1 miner and scout
     * leggings, scout and T2 combat boots) stay plain {@code basicItem} models with a grey texture.
     */
    // MIGRATION(26.x): item tints become `tints` entries of the item model definition (1.21.4+).
    private static Map<DeferredItem<? extends Item>, List<String>> layeredArmorIcons() {
        Map<DeferredItem<? extends Item>, List<String>> layers = new LinkedHashMap<>();
        layers.put(TGArmors.T1_MINER_HELMET, List.of("t1_miner_helmet", "t1_miner_helmet_overlay"));
        layers.put(TGArmors.T1_MINER_CHESTPLATE, List.of("t1_miner_chestplate", "t1_miner_chestplate_overlay"));
        layers.put(TGArmors.T1_SCOUT_HELMET, numbered("t1_scout_helmet", 2));
        layers.put(TGArmors.T1_SCOUT_CHESTPLATE, numbered("t1_scout_chestplate", 3));
        layers.put(TGArmors.T2_COMBAT_HELMET, numbered("t2_combat_helmet", 3));
        layers.put(TGArmors.T2_COMBAT_CHESTPLATE, numbered("t2_combat_chestplate", 3));
        layers.put(TGArmors.T2_COMBAT_LEGGINGS, numbered("t2_combat_leggings", 3));
        layers.put(TGArmors.HAZMAT_HELMET, numbered("hazmat_helmet", 2));
        layers.put(TGArmors.HAZMAT_CHESTPLATE, numbered("hazmat_chestplate", 2));
        layers.put(TGArmors.HAZMAT_LEGGINGS, numbered("hazmat_leggings", 1));
        layers.put(TGArmors.T3_COMBAT_HELMET, numbered("t3_combat_helmet", 3));
        layers.put(TGArmors.T3_COMBAT_CHESTPLATE, numbered("t3_combat_chestplate", 3));
        layers.put(TGArmors.T3_COMBAT_LEGGINGS, numbered("t3_combat_leggings", 2));
        layers.put(TGArmors.T3_COMBAT_BOOTS, numbered("t3_combat_boots", 2));
        layers.put(TGArmors.T3_EXO_HELMET, List.of("t3_combat_helmet_layer1", "t3_exo_helmet_layer2", "t3_combat_helmet_layer3", "t3_exo_helmet_layer4"));
        layers.put(TGArmors.T3_EXO_CHESTPLATE, List.of("t3_exo_chestplate_layer1", "t3_combat_chestplate_layer2", "t3_combat_chestplate_layer3",
                "t3_exo_chestplate_layer4"));
        layers.put(TGArmors.T3_EXO_LEGGINGS, numbered("t3_exo_leggings", 3));
        layers.put(TGArmors.T3_EXO_BOOTS, numbered("t3_exo_boots", 3));
        layers.put(TGArmors.T3_MINER_HELMET, numbered("t3_miner_helmet", 3));
        layers.put(TGArmors.T3_MINER_CHESTPLATE, numbered("t3_miner_chestplate", 2));
        layers.put(TGArmors.T3_MINER_LEGGINGS, numbered("t3_miner_leggings", 2));
        layers.put(TGArmors.T3_MINER_BOOTS, numbered("t3_miner_boots", 2));
        layers.put(TGArmors.T4_NANO_HELMET, numbered("t4_nano_suit_helmet", 3));
        layers.put(TGArmors.T4_NANO_CHESTPLATE, numbered("t4_nano_suit_chestplate", 3));
        layers.put(TGArmors.T4_NANO_LEGGINGS, numbered("t4_nano_suit_leggings", 3));
        layers.put(TGArmors.T4_NANO_BOOTS, numbered("t4_nano_suit_boots", 3));
        layers.put(TGArmors.T2_BERET_ITEM, numbered("t2_beret", 2));
        return layers;
    }

    /** {@code <base>_layer1} .. {@code <base>_layer<count>} */
    private static List<String> numbered(String base, int count) {
        return java.util.stream.IntStream.rangeClosed(1, count).mapToObj(i -> base + "_layer" + i).toList();
    }

    // MIGRATION(26.x): becomes an item model definition with a special model renderer (1.21.4+).
    private void codeRendered(DeferredItem<? extends Item> item) {
        getBuilder(item.getId().getPath()).parent(new ModelFile.UncheckedModelFile("builtin/entity")).guiLight(net.minecraft.client.renderer.block.model.BlockModel.GuiLight.FRONT);
    }
}
