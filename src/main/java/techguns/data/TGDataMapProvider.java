package techguns.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.DataMapProvider;
import techguns.radiation.ItemRadiation;
import techguns.registry.TGAmmo;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;
import techguns.registry.TGTags;

/** Item data maps: Techguns 2 {@code TGRadiationSystem.postInit} radioactive items. */
public class TGDataMapProvider extends DataMapProvider {
    public TGDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void gather() {
        var radiation = builder(ItemRadiation.DATA_MAP);
        radiation.add(TGItems.ENRICHED_URANIUM, new ItemRadiation(3), false);
        radiation.add(TGItems.YELLOWCAKE, new ItemRadiation(1), false);
        radiation.add(TGItems.ANTIGRAV_CORE, new ItemRadiation(4), false);
        radiation.add(TGItems.PLASMA_GENERATOR, new ItemRadiation(4), false);
        radiation.add(TGItems.TACTICAL_NUKE_WARHEAD, new ItemRadiation(1), false);
        radiation.add(TGAmmo.ROCKET_NUKE, new ItemRadiation(1), false);
        // Techguns 2: every uranium ore and ingot (ore dictionary); raw uranium is the ore's drop now
        radiation.add(TGItems.RAW_URANIUM, new ItemRadiation(1), false);
        radiation.add(item(TGBlocks.URANIUM_ORE), new ItemRadiation(1), false);
        radiation.add(item(TGBlocks.DEEPSLATE_URANIUM_ORE), new ItemRadiation(1), false);
        radiation.add(TGTags.Items.ore("uranium"), new ItemRadiation(1), false);
        radiation.add(TGTags.Items.ingot("uranium"), new ItemRadiation(1), false);
        radiation.add(TGTags.Items.rawMaterial("uranium"), new ItemRadiation(1), false);
    }

    private static net.minecraft.core.Holder<net.minecraft.world.item.Item> item(ItemLike block) {
        return block.asItem().builtInRegistryHolder();
    }
}
