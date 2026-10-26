package techguns.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import techguns.Techguns;
import techguns.registry.TGCamos;
import techguns.damage.TGDamageTypes;

/**
 * Entry point for all data generators. Run with {@code gradlew runData}.
 */
public final class TGDataGenerators {
    private TGDataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        RegistrySetBuilder datapackEntries = new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, TGDamageTypes::bootstrap)
                .add(TGCamos.KEY, TGCamoProvider::bootstrap);
        DatapackBuiltinEntriesProvider datapackProvider = generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), datapackEntries, Set.of(Techguns.MODID)));
        CompletableFuture<HolderLookup.Provider> lookup = datapackProvider.getRegistryProvider();

        // Server data
        generator.addProvider(event.includeServer(), new TGDamageTypeTagsProvider(output, lookup, fileHelper));
        TGBlockTagsProvider blockTags = generator.addProvider(event.includeServer(), new TGBlockTagsProvider(output, lookup, fileHelper));
        generator.addProvider(event.includeServer(), new TGItemTagsProvider(output, lookup, blockTags.contentsGetter(), fileHelper));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(TGBlockLootProvider::new, LootContextParamSets.BLOCK)), lookup));
        generator.addProvider(event.includeServer(), new TGRecipeProvider(output, lookup));

        // Client assets
        generator.addProvider(event.includeClient(), new TGBlockStateProvider(output, fileHelper));
        generator.addProvider(event.includeClient(), new TGItemModelProvider(output, fileHelper));
        generator.addProvider(event.includeClient(), new TGLanguageProvider(output));
    }
}
