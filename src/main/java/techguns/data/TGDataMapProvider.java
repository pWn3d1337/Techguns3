package techguns.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.DataMapProvider;
import techguns.TGIds;
import techguns.radiation.ItemRadiation;
import techguns.registry.TGAmmo;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;
import techguns.registry.TGTags;

/** Data maps: Techguns 2 {@code TGRadiationSystem.postInit} radioactive items, liquid fuels. */
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

        // liquid fuels of the generators and the ore drill: furnace burn ticks per millibucket
        builder(techguns.machine.FluidFuel.DATA_MAP).add(net.minecraft.world.level.material.Fluids.LAVA.builtInRegistryHolder(),
                new techguns.machine.FluidFuel(techguns.machine.OreDrillBlockEntity.LAVA_FUEL_VALUE), false);
        solarEfficiency();
        windEfficiency();
    }

    /**
     * Solar panel biome multipliers. A biome in several tags takes the entry with the highest priority, so the list runs
     * from the general climate fallbacks (low priority) to the most specific biome types (high priority).
     */
    private void solarEfficiency() {
        var solar = builder(techguns.machine.BiomeEfficiency.SOLAR);
        String[][] rows = {
                // type, tag path under c:, multiplier; most specific last. The climate fallbacks come first: they only
                // count for biomes no named type matches.
                {"wet", "is_wet/overworld", "0.9"},
                {"cold", "is_cold/overworld", "0.9"},
                {"hot", "is_hot/overworld", "1.25"},
                {"dry", "is_dry/overworld", "1.5"},
                {"mushroom", "is_mushroom", "0.9"},
                {"ocean", "is_ocean", "1.0"},
                {"icy", "is_icy", "0.75"},
                {"snowy", "is_snowy", "0.75"},
                {"taiga", "is_taiga", "0.9"},
                {"spooky", "is_spooky", "0.75"},
                {"swamp", "is_swamp", "0.75"},
                {"jungle", "is_jungle", "0.8"},
                {"plains", "is_plains", "1.1"},
                {"beach", "is_beach", "1.25"},
                {"mountain_peak", "is_mountain/peak", "1.5"},
                {"savanna", "is_savanna", "1.5"},
                {"badlands", "is_badlands", "2.0"},
                {"desert", "is_desert", "2.0"},
        };
        for (int i = 0; i < rows.length; i++) {
            String[] row = rows[i];
            // later rows are more specific: higher priority
            solar.add(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, TGIds.of("c", row[1])),
                    new techguns.machine.BiomeEfficiency(row[0], Float.parseFloat(row[2]), (i + 1) * 10), false);
        }
    }

    /**
     * Wind turbine biome multipliers, built like the solar ones: general vegetation fallbacks first, the most specific
     * (windswept, mountains, open water) last. The height factor already favours mountains, so peaks get a modest bonus.
     */
    private void windEfficiency() {
        var wind = builder(techguns.machine.BiomeEfficiency.WIND);
        for (int i = 0; i < WIND_ROWS.length; i++) {
            String[] row = WIND_ROWS[i];
            wind.add(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, TGIds.of("c", row[1])),
                    new techguns.machine.BiomeEfficiency(row[0], Float.parseFloat(row[2]), (i + 1) * 10), false);
        }
    }

    /** Wind biome types: type, tag path under c:, multiplier; most specific last. */
    public static final String[][] WIND_ROWS = {
            {"dense", "is_dense_vegetation/overworld", "0.75"},
            {"sparse", "is_sparse_vegetation/overworld", "1.15"},
            {"forest", "is_forest", "0.75"},
            {"taiga", "is_taiga", "0.8"},
            {"swamp", "is_swamp", "0.8"},
            {"jungle", "is_jungle", "0.5"},
            {"river", "is_river", "1.1"},
            {"plains", "is_plains", "1.2"},
            {"savanna", "is_savanna", "1.2"},
            {"desert", "is_desert", "1.2"},
            {"badlands", "is_badlands", "1.25"},
            {"hill", "is_hill", "1.25"},
            {"plateau", "is_plateau", "1.3"},
            {"snowy_plains", "is_snowy_plains", "1.3"},
            {"mountain_slope", "is_mountain/slope", "1.3"},
            {"beach", "is_beach", "1.4"},
            {"stony_shores", "is_stony_shores", "1.4"},
            {"ocean", "is_ocean", "1.5"},
            {"mountain_peak", "is_mountain/peak", "1.5"},
            {"windswept", "is_windswept", "1.75"},
    };

    /** Solar biome types in the order of the design table (most specific first), for lang and tests. */
    public static final String[] SOLAR_TYPES = {"desert", "badlands", "savanna", "mountain_peak", "beach", "dry", "hot", "plains", "jungle",
            "swamp", "spooky", "taiga", "snowy", "icy", "ocean", "mushroom", "cold", "wet"};

    private static net.minecraft.core.Holder<net.minecraft.world.item.Item> item(ItemLike block) {
        return block.asItem().builtInRegistryHolder();
    }
}
