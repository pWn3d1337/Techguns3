package techguns.registry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.TGIds;
import techguns.Techguns;
import techguns.world.structure.AlienBugNestPiece;
import techguns.world.structure.AlienBugNestStructure;
import techguns.world.structure.LegacyTemplatePiece;
import techguns.world.structure.LegacyTemplateStructure;

/**
 * Structure and structure piece types, and the generated structures and structure sets: Techguns 2
 * {@code TGStructureSpawnRegister} (weights) and {@code WorldGenTGStructureSpawn} (a small structure every 16 chunks, a
 * medium one every 32), as random spread sets.
 */
public final class TGStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Techguns.MODID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Techguns.MODID);

    public static final Supplier<StructureType<LegacyTemplateStructure>> LEGACY_TEMPLATE = STRUCTURE_TYPES.register("legacy_template",
            () -> () -> LegacyTemplateStructure.CODEC);
    public static final Supplier<StructureType<AlienBugNestStructure>> ALIEN_BUG_NEST = STRUCTURE_TYPES.register("alien_bug_nest",
            () -> () -> AlienBugNestStructure.CODEC);

    public static final Supplier<StructurePieceType> LEGACY_TEMPLATE_PIECE = PIECE_TYPES.register("legacy_template",
            () -> (StructurePieceType.StructureTemplateType) LegacyTemplatePiece::new);
    public static final Supplier<StructurePieceType> ALIEN_BUG_NEST_PIECE = PIECE_TYPES.register("alien_bug_nest",
            () -> (StructurePieceType.ContextlessType) AlienBugNestPiece::new);

    /** Biomes of the overworld surface structures (oceans are rejected when generating). */
    public static final TagKey<Biome> HAS_OVERWORLD_SURFACE = TagKey.create(Registries.BIOME, TGIds.id("has_structure/overworld_surface"));
    /** Techguns 2 {@code DESERTS_ONLY}: deserts, badlands and wastelands. */
    public static final TagKey<Biome> HAS_ALIEN_BUG_NEST = TagKey.create(Registries.BIOME, TGIds.id("has_structure/alien_bug_nest"));

    /**
     * A converted Techguns 2 template structure: Techguns 2 weight, set and placement values.
     *
     * @param foundation Techguns 2 {@code placeFoundation} range (0: none)
     * @param clearAbove Techguns 2 {@code cleanUpwards} range
     */
    public record Template(String name, String template, Size size, int weight, int yOffset, int foundation, int clearAbove) {
        public ResourceKey<Structure> key() {
            return ResourceKey.create(Registries.STRUCTURE, TGIds.id(name));
        }
    }

    /** Techguns 2 {@code StructureSize} grids: spacing in chunks; a structure lands in the first half of its cell. */
    public enum Size {
        SMALL("overworld_small", 16, 51531853), MEDIUM("overworld_medium", 32, 51531854);

        public final ResourceKey<StructureSet> set;
        public final int spacing;
        public final int salt;

        Size(String set, int spacing, int salt) {
            this.set = ResourceKey.create(Registries.STRUCTURE_SET, TGIds.id(set));
            this.spacing = spacing;
            this.salt = salt;
        }
    }

    public static final List<Template> OVERWORLD = List.of(
            new Template("factory_house_small", "factory_house_small", Size.SMALL, 10, -1, 3, 7),
            new Template("small_trainstation", "small_trainstation", Size.SMALL, 10, -1, 1, 7),
            new Template("small_mine", "small_mine", Size.SMALL, 10, -6, 0, 4),
            new Template("gas_station", "gas_station", Size.SMALL, 10, -1, 3, 7),
            new Template("police_station", "police_station", Size.MEDIUM, 10, -1, 3, 7),
            new Template("survivor_hideout", "survivor_hideout", Size.MEDIUM, 10, -1, 3, 7));

    public static final ResourceKey<Structure> ALIEN_BUG_NEST_KEY = ResourceKey.create(Registries.STRUCTURE, TGIds.id("alien_bug_nest"));
    /** Techguns 2 weight of the bug nest in the medium table. */
    private static final int ALIEN_BUG_NEST_WEIGHT = 20;

    private TGStructures() {
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderSet<Biome> surface = biomes.getOrThrow(HAS_OVERWORLD_SURFACE);
        for (Template t : OVERWORLD) {
            context.register(t.key(), new LegacyTemplateStructure(settings(surface), TGIds.id(t.template()), t.yOffset(), t.foundation(), t.clearAbove(), 3));
        }
        context.register(ALIEN_BUG_NEST_KEY, new AlienBugNestStructure(settings(biomes.getOrThrow(HAS_ALIEN_BUG_NEST))));
    }

    private static Structure.StructureSettings settings(HolderSet<Biome> biomes) {
        return new Structure.StructureSettings(biomes, Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE);
    }

    public static void bootstrapSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        for (Size size : Size.values()) {
            List<StructureSet.StructureSelectionEntry> entries = new java.util.ArrayList<>();
            for (Template t : OVERWORLD) {
                if (t.size() == size) {
                    entries.add(StructureSet.entry(structures.getOrThrow(t.key()), t.weight()));
                }
            }
            if (size == Size.MEDIUM) {
                entries.add(StructureSet.entry(structures.getOrThrow(ALIEN_BUG_NEST_KEY), ALIEN_BUG_NEST_WEIGHT));
            }
            // a small structure stays clear of the medium ones, which took its place in Techguns 2's grid
            Optional<StructurePlacement.ExclusionZone> exclusion = size == Size.SMALL
                    ? Optional.of(new StructurePlacement.ExclusionZone(context.lookup(Registries.STRUCTURE_SET).getOrThrow(Size.MEDIUM.set), 2))
                    : Optional.empty();
            context.register(size.set, new StructureSet(entries, new RandomSpreadStructurePlacement(Vec3i.ZERO,
                    StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, size.salt, exclusion, size.spacing, size.spacing / 2,
                    RandomSpreadType.LINEAR)));
        }
    }
}
