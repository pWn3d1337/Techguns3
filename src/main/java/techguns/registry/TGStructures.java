package techguns.registry;

import java.util.ArrayList;
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
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.TGIds;
import techguns.Techguns;
import techguns.world.structure.AlienBugNestPiece;
import techguns.world.structure.AlienBugNestStructure;
import techguns.world.structure.AvoidingSpreadPlacement;
import techguns.world.structure.LegacyTemplatePiece;
import techguns.world.structure.LegacyTemplateStructure;
import techguns.world.structure.MilitaryBasePiece;
import techguns.world.structure.MilitaryBaseStructure;

/**
 * Structure and structure piece types, and the generated structures and structure sets: Techguns 2
 * {@code TGStructureSpawnRegister} (weights) and {@code WorldGenTGStructureSpawn} (a small structure every 16 chunks, a
 * medium one every 32, a big one every 64, in the overworld and the Nether), as random spread sets.
 */
public final class TGStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Techguns.MODID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Techguns.MODID);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENT_TYPES = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT,
            Techguns.MODID);

    public static final Supplier<StructureType<LegacyTemplateStructure>> LEGACY_TEMPLATE = STRUCTURE_TYPES.register("legacy_template",
            () -> () -> LegacyTemplateStructure.CODEC);
    public static final Supplier<StructureType<AlienBugNestStructure>> ALIEN_BUG_NEST = STRUCTURE_TYPES.register("alien_bug_nest",
            () -> () -> AlienBugNestStructure.CODEC);
    public static final Supplier<StructureType<MilitaryBaseStructure>> MILITARY_BASE = STRUCTURE_TYPES.register("military_base",
            () -> () -> MilitaryBaseStructure.CODEC);

    public static final Supplier<StructurePieceType> LEGACY_TEMPLATE_PIECE = PIECE_TYPES.register("legacy_template",
            () -> (StructurePieceType.StructureTemplateType) LegacyTemplatePiece::new);
    public static final Supplier<StructurePieceType> ALIEN_BUG_NEST_PIECE = PIECE_TYPES.register("alien_bug_nest",
            () -> (StructurePieceType.ContextlessType) AlienBugNestPiece::new);
    public static final Supplier<StructurePieceType> MILITARY_BASE_PIECE = PIECE_TYPES.register("military_base",
            () -> (StructurePieceType.ContextlessType) MilitaryBasePiece::new);

    public static final Supplier<StructurePlacementType<AvoidingSpreadPlacement>> AVOIDING_SPREAD = PLACEMENT_TYPES.register("avoiding_spread",
            () -> () -> AvoidingSpreadPlacement.CODEC);

    /** Biomes of the overworld surface structures (oceans are rejected when generating). */
    public static final TagKey<Biome> HAS_OVERWORLD_SURFACE = TagKey.create(Registries.BIOME, TGIds.id("has_structure/overworld_surface"));
    /** Techguns 2 {@code DESERTS_ONLY}: deserts, badlands and wastelands. */
    public static final TagKey<Biome> HAS_ALIEN_BUG_NEST = TagKey.create(Registries.BIOME, TGIds.id("has_structure/alien_bug_nest"));

    /** Biomes of the Nether structures. */
    public static final TagKey<Biome> HAS_NETHER = TagKey.create(Registries.BIOME, TGIds.id("has_structure/nether"));

    /** Techguns 2 {@code StructureLandType.WATER}: the aircraft carrier. */
    public static final TagKey<Biome> HAS_OCEAN = TagKey.create(Registries.BIOME, TGIds.id("has_structure/ocean"));

    /**
     * A converted Techguns 2 template structure (template of the same name): Techguns 2 weight, set and placement values.
     *
     * @param foundation Techguns 2 {@code placeFoundation} range (0: none)
     * @param clearAbove Techguns 2 {@code cleanUpwards} range
     * @param toggle     the config switch it depends on
     */
    public record Template(String name, Size size, int weight, int yOffset, int foundation, int clearAbove, LegacyTemplateStructure.Placement placement,
                           Optional<LegacyTemplateStructure.Toggle> toggle) {
        Template(String name, Size size, int weight, int yOffset, int foundation, int clearAbove) {
            this(name, size, weight, yOffset, foundation, clearAbove,
                    size.nether ? LegacyTemplateStructure.Placement.CAVE : LegacyTemplateStructure.Placement.SURFACE, Optional.empty());
        }

        Template oreClusters() {
            return new Template(name, size, weight, yOffset, foundation, clearAbove, placement, Optional.of(LegacyTemplateStructure.Toggle.ORE_CLUSTERS));
        }

        public ResourceKey<Structure> key() {
            return ResourceKey.create(Registries.STRUCTURE, TGIds.id(name));
        }

        TagKey<Biome> biomes() {
            return placement == LegacyTemplateStructure.Placement.WATER ? HAS_OCEAN : size.nether ? HAS_NETHER : HAS_OVERWORLD_SURFACE;
        }
    }

    /**
     * Techguns 2 {@code StructureSize} grids per dimension: spacing in chunks; a structure lands in the first half of
     * its cell. A set keeps away from the bigger ones, which took its place in the Techguns 2 grid: 2 chunks from a
     * medium start, {@link #BIG_CLEARANCE} from a big one.
     */
    public enum Size {
        SMALL("overworld_small", 16, 51531853, false), MEDIUM("overworld_medium", 32, 51531854, false), BIG("overworld_big", 64, 51531857, false),
        NETHER_SMALL("nether_small", 16, 51531855, true), NETHER_MEDIUM("nether_medium", 32, 51531856, true);

        public final ResourceKey<StructureSet> set;
        public final int spacing;
        public final int salt;
        public final boolean nether;

        Size(String set, int spacing, int salt, boolean nether) {
            this.set = ResourceKey.create(Registries.STRUCTURE_SET, TGIds.id(set));
            this.spacing = spacing;
            this.salt = salt;
            this.nether = nether;
        }

        /** The bigger set this one keeps away from. */
        Optional<Size> bigger() {
            return switch (this) {
                case SMALL -> Optional.of(MEDIUM);
                case MEDIUM -> Optional.of(BIG);
                case NETHER_SMALL -> Optional.of(NETHER_MEDIUM);
                default -> Optional.empty();
            };
        }
    }

    public static final List<Template> OVERWORLD = List.of(
            new Template("factory_house_small", Size.SMALL, 10, -1, 3, 7),
            new Template("small_trainstation", Size.SMALL, 10, -1, 1, 7),
            new Template("small_mine", Size.SMALL, 10, -6, 0, 4),
            new Template("gas_station", Size.SMALL, 10, -1, 3, 7),
            new Template("police_station", Size.MEDIUM, 10, -1, 3, 7),
            new Template("survivor_hideout", Size.MEDIUM, 10, -1, 3, 7),
            // Techguns 2 ore cluster structures: no foundation, the meteor sunk 5 deeper with 30 blocks cleared
            new Template("ore_cluster_spike", Size.MEDIUM, 10, -1, 0, 6).oreClusters(),
            new Template("ore_cluster_meteor_basis", Size.MEDIUM, 5, -6, 0, 30).oreClusters(),
            // Techguns 2 AircraftCarrier: on water, 3 below the water surface
            new Template("aircraft_carrier", Size.BIG, 1, -3, 0, 0, LegacyTemplateStructure.Placement.WATER, Optional.empty()));

    /**
     * Techguns 2 Nether structures: cave floor height - 1 + their {@code hoffset}, {@code placeFoundationNether} 16
     * (the small ore cluster 2), no clean-up. Techguns 2 gave the ore cluster castle weight 1000 in the medium table.
     */
    public static final List<Template> NETHER = List.of(
            new Template("nether_altar_small", Size.NETHER_SMALL, 10, -3, 16, 0),
            new Template("nether_soul_platform", Size.NETHER_SMALL, 10, -3, 16, 0),
            new Template("nether_loot_01", Size.NETHER_SMALL, 10, 0, 16, 0),
            new Template("nether_acid_hole", Size.NETHER_SMALL, 10, -2, 16, 0),
            new Template("nether_ore_cluster_small", Size.NETHER_SMALL, 10, 0, 2, 0).oreClusters(),
            new Template("nether_altar_medium", Size.NETHER_MEDIUM, 10, -2, 16, 0),
            new Template("nether_ghast_spawner", Size.NETHER_MEDIUM, 10, -3, 16, 0),
            new Template("nether_ore_cluster_castle", Size.NETHER_MEDIUM, 1000, -2, 16, 0).oreClusters());

    public static final ResourceKey<Structure> ALIEN_BUG_NEST_KEY = ResourceKey.create(Registries.STRUCTURE, TGIds.id("alien_bug_nest"));
    /** Techguns 2 weight of the bug nest in the medium table. */
    private static final int ALIEN_BUG_NEST_WEIGHT = 20;

    public static final ResourceKey<Structure> MILITARY_BASE_KEY = ResourceKey.create(Registries.STRUCTURE, TGIds.id("military_base"));
    /** Techguns 2 weight of the military base in the big table. */
    private static final int MILITARY_BASE_WEIGHT = 1;
    /**
     * Chunks a smaller structure keeps away from a big one's start: the big ones reach 5 (camp) to 7 (carrier) chunks
     * from it, a medium structure 2.
     */
    private static final int BIG_CLEARANCE = 7;

    private TGStructures() {
    }

    private static List<Template> all() {
        List<Template> all = new ArrayList<>(OVERWORLD);
        all.addAll(NETHER);
        return all;
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        for (Template t : all()) {
            context.register(t.key(), new LegacyTemplateStructure(settings(biomes.getOrThrow(t.biomes()), t.size().nether), TGIds.id(t.name()), t.yOffset(),
                    t.foundation(), t.clearAbove(), 3, t.placement(), t.toggle()));
        }
        context.register(ALIEN_BUG_NEST_KEY, new AlienBugNestStructure(settings(biomes.getOrThrow(HAS_ALIEN_BUG_NEST), false)));
        // after the trees, which the camp clears (Techguns 2 spawned its structures after the decoration)
        context.register(MILITARY_BASE_KEY, new MilitaryBaseStructure(new Structure.StructureSettings(biomes.getOrThrow(HAS_OVERWORLD_SURFACE), Map.of(),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION, TerrainAdjustment.NONE)));
    }

    private static Structure.StructureSettings settings(HolderSet<Biome> biomes, boolean underground) {
        return new Structure.StructureSettings(biomes, Map.of(),
                underground ? GenerationStep.Decoration.UNDERGROUND_STRUCTURES : GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE);
    }

    public static void bootstrapSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        HolderGetter<StructureSet> sets = context.lookup(Registries.STRUCTURE_SET);
        for (Size size : Size.values()) {
            List<StructureSet.StructureSelectionEntry> entries = new ArrayList<>();
            for (Template t : all()) {
                if (t.size() == size) {
                    entries.add(StructureSet.entry(structures.getOrThrow(t.key()), t.weight()));
                }
            }
            if (size == Size.MEDIUM) {
                entries.add(StructureSet.entry(structures.getOrThrow(ALIEN_BUG_NEST_KEY), ALIEN_BUG_NEST_WEIGHT));
            }
            if (size == Size.BIG) {
                entries.add(StructureSet.entry(structures.getOrThrow(MILITARY_BASE_KEY), MILITARY_BASE_WEIGHT));
            }
            List<StructurePlacement.ExclusionZone> avoid = new ArrayList<>();
            for (Optional<Size> bigger = size.bigger(); bigger.isPresent(); bigger = bigger.get().bigger()) {
                avoid.add(new StructurePlacement.ExclusionZone(sets.getOrThrow(bigger.get().set), bigger.get() == Size.BIG ? BIG_CLEARANCE : 2));
            }
            context.register(size.set, new StructureSet(entries, new AvoidingSpreadPlacement(Vec3i.ZERO,
                    StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, size.salt, size.spacing, size.spacing / 2,
                    RandomSpreadType.LINEAR, avoid)));
        }
    }
}
