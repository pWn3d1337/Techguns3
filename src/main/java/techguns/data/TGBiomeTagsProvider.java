package techguns.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import techguns.Techguns;
import techguns.registry.TGStructures;
import techguns.registry.TGTags;

/** Biome tags for world generation: the extra near-surface uranium deposits and the structures' biomes. */
public class TGBiomeTagsProvider extends BiomeTagsProvider {
    public TGBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
        super(output, lookup, Techguns.MODID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(TGTags.Biomes.URANIUM_DEPOSITS).addTag(BiomeTags.IS_BADLANDS).addTag(Tags.Biomes.IS_DESERT);
        tag(TGStructures.HAS_OVERWORLD_SURFACE).addTag(Tags.Biomes.IS_OVERWORLD);
        tag(TGStructures.HAS_NETHER).addTag(Tags.Biomes.IS_NETHER);
        // Techguns 2 SANDY and WASTELAND (beaches were not SANDY there)
        tag(TGStructures.HAS_ALIEN_BUG_NEST).addTag(Tags.Biomes.IS_DESERT).addTag(BiomeTags.IS_BADLANDS).addTag(Tags.Biomes.IS_WASTELAND);
    }
}
