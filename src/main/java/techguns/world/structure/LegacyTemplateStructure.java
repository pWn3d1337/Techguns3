package techguns.world.structure;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import techguns.TGConfig;
import techguns.registry.TGStructures;
import techguns.world.LegacyPaletteProcessor;

/**
 * A converted Techguns 2 structure: one template in a random rotation, its footprint starting at the chunk corner.
 * <ul>
 * <li>{@link Placement#SURFACE} (Techguns 2 {@code spawnStructureWorldgen}): only where the ground under the footprint
 * (sampled every 4 blocks) varies by at most {@code heightTolerance}, with no liquid on top and not in oceans; the
 * height is the average first free block above the ground.</li>
 * <li>{@link Placement#CAVE} (Techguns 2 {@code spawnStructureCaveWorldgen}, the Nether): on a cave floor with at least
 * 10 blocks of air above it between y 20 and 100, found at the four chunk corners within 10 blocks of each other; the
 * height is the average floor block. The foundation stops at solid ground.</li>
 * <li>{@link Placement#WATER} (Techguns 2 {@code AircraftCarrier}): the four footprint corners on water within 2 blocks
 * of each other; the height is their median first free block above the water.</li>
 * </ul>
 * The base layer sits at that height plus {@code yOffset}. Structures with a {@code config} switch only spawn while it
 * is on.
 *
 * @param yOffset        base layer height relative to the placement height (Techguns 2 surface: -1, the small mine -6;
 *                       cave: -1 plus the structure's {@code hoffset})
 * @param foundation     how many blocks the base layer is extended down into replaceable blocks
 * @param clearAbove     how many blocks above the base layer are cleared first
 */
public class LegacyTemplateStructure extends Structure {
    public static final MapCodec<LegacyTemplateStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            ResourceLocation.CODEC.fieldOf("template").forGetter(s -> s.template),
            Codec.INT.optionalFieldOf("y_offset", -1).forGetter(s -> s.yOffset),
            Codec.INT.optionalFieldOf("foundation", 3).forGetter(s -> s.foundation),
            Codec.INT.optionalFieldOf("clear_above", 7).forGetter(s -> s.clearAbove),
            Codec.INT.optionalFieldOf("height_tolerance", 3).forGetter(s -> s.heightTolerance),
            Placement.CODEC.optionalFieldOf("placement", Placement.SURFACE).forGetter(s -> s.placement),
            Toggle.CODEC.optionalFieldOf("config").forGetter(s -> s.toggle)
    ).apply(instance, LegacyTemplateStructure::new));

    public enum Placement implements StringRepresentable {
        SURFACE, CAVE, WATER;

        public static final Codec<Placement> CODEC = StringRepresentable.fromEnum(Placement::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** Config switches a structure can depend on. */
    public enum Toggle implements StringRepresentable {
        ORE_CLUSTERS(TGConfig.SPAWN_ORE_CLUSTER_STRUCTURES::get);

        public static final Codec<Toggle> CODEC = StringRepresentable.fromEnum(Toggle::values);

        private final BooleanSupplier enabled;

        Toggle(BooleanSupplier enabled) {
            this.enabled = enabled;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** Techguns 2 {@code getValidSpawnYWater}: the corners' heights may differ by less than this. */
    private static final int WATER_HEIGHT_TOLERANCE = 2;
    /** Techguns 2 {@code WorldGenTGStructureSpawn.NETHER_STRUCT_MIN_Y / MAX_Y} and the air needed above a cave floor. */
    private static final int CAVE_MIN_Y = 20;
    private static final int CAVE_MAX_Y = 100;
    private static final int CAVE_AIR = 10;

    /** Techguns 2 {@code getStep}: ground sample spacing. */
    private static final int SAMPLE_STEP = 4;

    private final ResourceLocation template;
    private final int yOffset;
    private final int foundation;
    private final int clearAbove;
    private final int heightTolerance;
    private final Placement placement;
    private final Optional<Toggle> toggle;

    public LegacyTemplateStructure(StructureSettings settings, ResourceLocation template, int yOffset, int foundation, int clearAbove, int heightTolerance,
                                   Placement placement, Optional<Toggle> toggle) {
        super(settings);
        this.template = template;
        this.yOffset = yOffset;
        this.foundation = foundation;
        this.clearAbove = clearAbove;
        this.heightTolerance = heightTolerance;
        this.placement = placement;
        this.toggle = toggle;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (toggle.isPresent() && !toggle.get().enabled.getAsBoolean()) {
            return Optional.empty();
        }
        StructureTemplate structure = context.structureTemplateManager().getOrCreate(template);
        Vec3i size = structure.getSize();
        Rotation rotation = Rotation.getRandom(context.random());
        BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
        BoundingBox footprint = structure.getBoundingBox(LegacyTemplatePiece.settings(rotation, pivot, LegacyPaletteProcessor.BiomeColor.WOODLAND),
                BlockPos.ZERO);
        // shift so the rotated footprint starts at the chunk corner
        int originX = context.chunkPos().getMinBlockX() - footprint.minX();
        int originZ = context.chunkPos().getMinBlockZ() - footprint.minZ();
        int x0 = context.chunkPos().getMinBlockX();
        int z0 = context.chunkPos().getMinBlockZ();
        int sizeX = footprint.getXSpan();
        int sizeZ = footprint.getZSpan();

        Optional<Integer> ground = switch (placement) {
            case CAVE -> caveHeight(context, x0, z0);
            case WATER -> waterHeight(context, x0, z0, sizeX, sizeZ);
            case SURFACE -> groundHeight(context, x0, z0, sizeX, sizeZ, heightTolerance).filter(y -> !isOcean(context, x0 + sizeX / 2, y, z0 + sizeZ / 2));
        };
        if (ground.isEmpty()) {
            return Optional.empty();
        }
        LegacyPaletteProcessor.BiomeColor biomeColor = LegacyPaletteProcessor.BiomeColor.of(biome(context, x0 + sizeX / 2, ground.get(), z0 + sizeZ / 2));
        BlockPos origin = new BlockPos(originX, ground.get() + yOffset, originZ);
        BlockPos center = new BlockPos(x0 + sizeX / 2, ground.get(), z0 + sizeZ / 2);
        return Optional.of(new GenerationStub(center, pieces -> pieces.addPiece(
                new LegacyTemplatePiece(context.structureTemplateManager(), template, origin, rotation, pivot, biomeColor, foundation, clearAbove,
                        placement == Placement.CAVE))));
    }

    /** Techguns 2 {@code getValidSpawnYWater}: water at the four footprint corners, close in height; their median. */
    static Optional<Integer> waterHeight(GenerationContext context, int x0, int z0, int sizeX, int sizeZ) {
        int[] heights = new int[4];
        int i = 0;
        for (int[] corner : new int[][] {{0, 0}, {sizeX, 0}, {0, sizeZ}, {sizeX, sizeZ}}) {
            int x = x0 + corner[0];
            int z = z0 + corner[1];
            int height = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            if (context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState()).getBlock(height - 1).getFluidState().isEmpty()) {
                return Optional.empty();
            }
            heights[i++] = height;
        }
        for (int h : heights) {
            if (Math.abs(h - heights[0]) >= WATER_HEIGHT_TOLERANCE) {
                return Optional.empty();
            }
        }
        java.util.Arrays.sort(heights);
        return Optional.of((heights[1] + heights[2]) / 2);
    }

    /** Techguns 2 {@code getCaveHeight}: the cave floor at the four chunk corners, if all are close enough. */
    static Optional<Integer> caveHeight(GenerationContext context, int x0, int z0) {
        int[] heights = {airFloor(context, x0, z0), airFloor(context, x0 + 15, z0), airFloor(context, x0, z0 + 15), airFloor(context, x0 + 15, z0 + 15)};
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int sum = 0;
        for (int h : heights) {
            if (h < CAVE_MIN_Y || h > CAVE_MAX_Y) {
                return Optional.empty();
            }
            min = Math.min(min, h);
            max = Math.max(max, h);
            sum += h;
        }
        return max - min <= CAVE_AIR ? Optional.of(sum / heights.length) : Optional.empty();
    }

    /** Techguns 2 {@code getAirHeight}: from y 100 down, the first non-air block with at least 10 air blocks above. */
    private static int airFloor(GenerationContext context, int x, int z) {
        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        int air = 0;
        for (int y = CAVE_MAX_Y; y > CAVE_MIN_Y; y--) {
            if (column.getBlock(y).isAir()) {
                air++;
            } else {
                if (air >= CAVE_AIR) {
                    return y;
                }
                air = 0;
            }
        }
        return -1;
    }

    /**
     * Techguns 2 {@code getValidSpawnYArea}: the average height of the first free block above the ground, sampled every
     * {@link #SAMPLE_STEP} blocks over the footprint (edges included); empty if a sample is a liquid or the samples
     * differ by more than {@code tolerance}.
     */
    static Optional<Integer> groundHeight(GenerationContext context, int x0, int z0, int sizeX, int sizeZ, int tolerance) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        long sum = 0;
        int count = 0;
        for (int dx = 0; ; dx = Math.min(dx + SAMPLE_STEP, sizeX)) {
            for (int dz = 0; ; dz = Math.min(dz + SAMPLE_STEP, sizeZ)) {
                int x = x0 + dx;
                int z = z0 + dz;
                int height = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                if (!context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState()).getBlock(height - 1)
                        .getFluidState().isEmpty()) {
                    return Optional.empty();
                }
                min = Math.min(min, height);
                max = Math.max(max, height);
                sum += height;
                count++;
                if (dz == sizeZ) {
                    break;
                }
            }
            if (dx == sizeX) {
                break;
            }
        }
        return max - min > tolerance ? Optional.empty() : Optional.of((int) (sum / count));
    }

    /** Techguns 2 {@code StructureLandType}: land structures never spawn in ocean biomes. */
    static boolean isOcean(GenerationContext context, int x, int y, int z) {
        return biome(context, x, y, z).is(BiomeTags.IS_OCEAN);
    }

    static Holder<Biome> biome(GenerationContext context, int x, int y, int z) {
        return context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), context.randomState().sampler());
    }

    @Override
    public StructureType<?> type() {
        return TGStructures.LEGACY_TEMPLATE.get();
    }
}
