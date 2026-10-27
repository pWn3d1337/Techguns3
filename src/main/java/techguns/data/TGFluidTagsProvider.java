package techguns.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import techguns.TGIds;
import techguns.Techguns;
import techguns.registry.TGTags;

/** Fluid tags: the Reaction Chamber's liquid redstone and liquid ender (lava, as in Techguns 2 without Thermal). */
public class TGFluidTagsProvider extends FluidTagsProvider {
    public TGFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper) {
        super(output, lookupProvider, Techguns.MODID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(TGTags.Fluids.LIQUID_REDSTONE).addTag(FluidTags.LAVA).addOptionalTag(TGIds.of("c", "redstone"));
        tag(TGTags.Fluids.LIQUID_ENDER).addTag(FluidTags.LAVA).addOptionalTag(TGIds.of("c", "ender"));
        // Techguns 2 read the fuels and oil from other mods' Forge fluids
        tag(TGTags.Fluids.DRILL_FUEL).addOptionalTag(TGIds.of("c", "fuel")).addOptionalTag(TGIds.of("c", "diesel"))
                .addOptionalTag(TGIds.of("c", "biodiesel"));
        tag(TGTags.Fluids.DRILL_OIL).addOptionalTag(TGIds.of("c", "oil")).addOptionalTag(TGIds.of("c", "crude_oil"));
    }
}
