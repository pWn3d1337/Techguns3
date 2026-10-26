package techguns.data;

import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;

public class TGBlockLootProvider extends BlockLootSubProvider {
    public TGBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        add(TGBlocks.BIOBLOB.get(), noDrop());
        add(TGBlocks.TIN_ORE.get(), block -> createOreDrop(block, TGItems.RAW_TIN.get()));
        add(TGBlocks.DEEPSLATE_TIN_ORE.get(), block -> createOreDrop(block, TGItems.RAW_TIN.get()));
        add(TGBlocks.LEAD_ORE.get(), block -> createOreDrop(block, TGItems.RAW_LEAD.get()));
        add(TGBlocks.DEEPSLATE_LEAD_ORE.get(), block -> createOreDrop(block, TGItems.RAW_LEAD.get()));
        add(TGBlocks.TITANIUM_ORE.get(), block -> createOreDrop(block, TGItems.RAW_TITANIUM.get()));
        add(TGBlocks.DEEPSLATE_TITANIUM_ORE.get(), block -> createOreDrop(block, TGItems.RAW_TITANIUM.get()));
        add(TGBlocks.URANIUM_ORE.get(), block -> createOreDrop(block, TGItems.RAW_URANIUM.get()));
        add(TGBlocks.DEEPSLATE_URANIUM_ORE.get(), block -> createOreDrop(block, TGItems.RAW_URANIUM.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return TGBlocks.BLOCKS.getEntries().stream().map(DeferredHolder::value).map(Block.class::cast)::iterator;
    }
}
