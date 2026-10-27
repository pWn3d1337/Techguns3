package techguns.registry;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.TGIds;
import techguns.Techguns;
import techguns.world.LegacyPaletteProcessor;

/** Structure processor types and the generated processor lists. */
public final class TGStructureProcessors {
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Techguns.MODID);

    public static final Supplier<StructureProcessorType<LegacyPaletteProcessor>> LEGACY_PALETTE = PROCESSORS.register("legacy_palette",
            () -> () -> LegacyPaletteProcessor.CODEC);

    /** For the structures converted from Techguns 2. */
    public static final ResourceKey<StructureProcessorList> LEGACY = ResourceKey.create(Registries.PROCESSOR_LIST, TGIds.id("legacy"));

    private TGStructureProcessors() {
    }

    public static void bootstrap(BootstrapContext<StructureProcessorList> context) {
        context.register(LEGACY, new StructureProcessorList(List.of(LegacyPaletteProcessor.INSTANCE)));
    }
}
