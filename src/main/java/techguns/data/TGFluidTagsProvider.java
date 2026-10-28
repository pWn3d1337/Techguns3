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
        var fuel = tag(TGTags.Fluids.FUEL);
        // the Techguns 2 list, and the fuels of Immersive Engineering, Modern Industrialization, Oritech, Immersive
        // Petroleum and Create: Diesel Generators (8f2)
        for (String name : new String[] {"fuel", "refined_fuel", "biofuel", "biodiesel", "high_power_biodiesel", "diesel", "boosted_diesel",
                "gasoline", "kerosene", "light_fuel", "heavy_fuel", "turbofuel", "rocket_fuel"}) {
            fuel.addOptionalTag(TGIds.of("c", name));
        }
        tag(TGTags.Fluids.DRILL_OIL).addOptionalTag(TGIds.of("c", "oil")).addOptionalTag(TGIds.of("c", "crude_oil"));
    }
}
