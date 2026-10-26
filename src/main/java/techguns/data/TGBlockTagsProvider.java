package techguns.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import techguns.Techguns;
import techguns.registry.TGBlocks;
import techguns.registry.TGTags;

public class TGBlockTagsProvider extends BlockTagsProvider {
    public TGBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Techguns.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // decorative blocks are mined with a pickaxe (Techguns 2 rock/iron materials)
        TGBlocks.cubeBlocks().stream().filter(block -> block != TGBlocks.BUGNEST_SAND)
                .forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        TGBlocks.stairBlocks().forEach(block -> {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get());
            tag(BlockTags.STAIRS).add(block.get());
        });
        // Techguns 2: hardened bug nest sand is dug with a shovel
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(TGBlocks.BUGNEST_SAND.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TGBlocks.METAL_PRESS.get(), TGBlocks.AMMO_PRESS.get(), TGBlocks.BLAST_FURNACE.get(),
                TGBlocks.GRINDER.get(), TGBlocks.CHEM_LAB.get(), TGBlocks.CHARGING_STATION.get());
        // Phase 5a2: Techguns 2 iron blocks are mined with a pickaxe (no tool needed for drops), crates are wood
        TGBlocks.LAMPS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        TGBlocks.LANTERNS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        TGBlocks.LADDERS.forEach(block -> {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get());
            tag(BlockTags.CLIMBABLE).add(block.get());
        });
        tag(BlockTags.CLIMBABLE).add(TGBlocks.SLIMY_LADDER.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(TGBlocks.SLIMY_LADDER.get());
        TGBlocks.MILITARY_CRATES.forEach(block -> tag(BlockTags.MINEABLE_WITH_AXE).add(block.get()));
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TGBlocks.BUNKER_DOOR.get());
        tag(BlockTags.DOORS).add(TGBlocks.BUNKER_DOOR.get());
        TGBlocks.DOORS_3X3.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        // Ores
        ore("tin", TGBlocks.TIN_ORE, TGBlocks.DEEPSLATE_TIN_ORE, BlockTags.NEEDS_STONE_TOOL);
        ore("lead", TGBlocks.LEAD_ORE, TGBlocks.DEEPSLATE_LEAD_ORE, BlockTags.NEEDS_IRON_TOOL);
        ore("titanium", TGBlocks.TITANIUM_ORE, TGBlocks.DEEPSLATE_TITANIUM_ORE, BlockTags.NEEDS_DIAMOND_TOOL);
        ore("uranium", TGBlocks.URANIUM_ORE, TGBlocks.DEEPSLATE_URANIUM_ORE, BlockTags.NEEDS_IRON_TOOL);
    }

    /** Common ore tags, pickaxe mining and tool tier for a stone/deepslate ore pair. */
    private void ore(String material, DeferredBlock<? extends Block> stone, DeferredBlock<? extends Block> deepslate,
                     TagKey<Block> tier) {
        tag(TGTags.Blocks.ore(material)).add(stone.get(), deepslate.get());
        tag(Tags.Blocks.ORES).addTag(TGTags.Blocks.ore(material));
        tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(stone.get());
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(deepslate.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(stone.get(), deepslate.get());
        tag(tier).add(stone.get(), deepslate.get());
    }
}
