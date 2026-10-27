package techguns.world;

import javax.annotation.Nullable;

import java.util.Locale;
import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.common.Tags;
import techguns.registry.TGStructureProcessors;

/**
 * Resolves the Techguns 2 palette entries that the structure converter stored as block NBT ({@link #KEY}) instead of
 * a fixed state: random variants ({@code MultiMBlock}), biome variants ({@code MBlockBiomeColorType}, camo nets) and
 * per-structure rolls ({@code MultiMMBlockIndexRoll}: one index for the whole structure, e.g. the ore cluster type).
 * Without this processor the template's state (the first variant, as in Techguns 2) is placed.
 */
public final class LegacyPaletteProcessor extends StructureProcessor {
    /** Takes the biome colors from the biome at the block (when its chunk is readable). */
    public static final LegacyPaletteProcessor INSTANCE = new LegacyPaletteProcessor(Optional.empty());
    public static final MapCodec<LegacyPaletteProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeColor.CODEC.optionalFieldOf("biome_color").forGetter(p -> p.biomeColor)).apply(instance, LegacyPaletteProcessor::new));

    /** Block NBT key of the option tree. */
    public static final String KEY = "techguns_legacy";
    // option tree: exactly one of these keys per option
    public static final String STATE = "state";
    public static final String BLOCK_ENTITY = "nbt";
    public static final String WEIGHTED = "weighted";
    public static final String WEIGHT = "w";
    public static final String OPTION = "option";
    public static final String BIOME = "biome";
    public static final String ROLL = "roll";
    public static final String ROLL_WEIGHTS = "roll_weights";
    public static final String ALT = "alt";
    public static final String ALT_PER_INDEX = "alt_per_index";
    public static final String ALT_CHANCE = "alt_chance";

    /** Techguns 2 {@code BiomeColorType} order of the biome variants. */
    public enum BiomeColor implements StringRepresentable {
        WOODLAND, SNOW, DESERT, NETHER;

        public static final StringRepresentable.EnumCodec<BiomeColor> CODEC = StringRepresentable.fromEnum(BiomeColor::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        /** Techguns 2 {@code getBiomeColorTypeFromBiome}. */
        public static BiomeColor of(Holder<Biome> biome) {
            if (biome.is(BiomeTags.IS_NETHER)) {
                return NETHER;
            }
            if (biome.is(Tags.Biomes.IS_SNOWY)) {
                return SNOW;
            }
            if (biome.is(Tags.Biomes.IS_DESERT) || biome.is(BiomeTags.IS_BADLANDS) || biome.is(Tags.Biomes.IS_SANDY)) {
                return DESERT;
            }
            return WOODLAND;
        }
    }

    private final Optional<BiomeColor> biomeColor;

    /** @param biomeColor the colors of the whole structure, chosen where it was placed */
    public LegacyPaletteProcessor(Optional<BiomeColor> biomeColor) {
        this.biomeColor = biomeColor;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pivot, StructureTemplate.StructureBlockInfo original,
                                                            StructureTemplate.StructureBlockInfo current, StructurePlaceSettings settings) {
        CompoundTag nbt = current.nbt();
        if (nbt == null || !nbt.contains(KEY, Tag.TAG_COMPOUND)) {
            return current;
        }
        // during world generation only the chunks around the one being generated are readable
        BiomeColor color = biomeColor.orElseGet(() -> level.hasChunkAt(current.pos()) ? BiomeColor.of(level.getBiome(current.pos())) : BiomeColor.WOODLAND);
        Context context = new Context(settings.getRandom(current.pos()), Mth.getSeed(offset), color);
        CompoundTag chosen = context.resolve(nbt.getCompound(KEY));
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), chosen.getCompound(STATE));
        CompoundTag blockEntity = chosen.contains(BLOCK_ENTITY, Tag.TAG_COMPOUND) ? chosen.getCompound(BLOCK_ENTITY) : null;
        return new StructureTemplate.StructureBlockInfo(current.pos(), state, blockEntity);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TGStructureProcessors.LEGACY_PALETTE.get();
    }

    /**
     * @param random        per block
     * @param structureSeed the same for every block of one structure (the roll index)
     */
    private record Context(RandomSource random, long structureSeed, BiomeColor biome) {
        CompoundTag resolve(CompoundTag option) {
            if (option.contains(WEIGHTED)) {
                ListTag options = option.getList(WEIGHTED, Tag.TAG_COMPOUND);
                int total = 0;
                for (int i = 0; i < options.size(); i++) {
                    total += options.getCompound(i).getInt(WEIGHT);
                }
                int roll = random.nextInt(Math.max(total, 1));
                for (int i = 0; i < options.size(); i++) {
                    roll -= options.getCompound(i).getInt(WEIGHT);
                    if (roll < 0) {
                        return resolve(options.getCompound(i).getCompound(OPTION));
                    }
                }
                return resolve(options.getCompound(options.size() - 1).getCompound(OPTION));
            }
            if (option.contains(BIOME)) {
                ListTag options = option.getList(BIOME, Tag.TAG_COMPOUND);
                return resolve(options.getCompound(Math.min(biome.ordinal(), options.size() - 1)));
            }
            if (option.contains(ROLL)) {
                ListTag options = option.getList(ROLL, Tag.TAG_COMPOUND);
                int index = Math.min(rollIndex(option.getIntArray(ROLL_WEIGHTS)), options.size() - 1);
                if (option.contains(ALT_CHANCE) && random.nextFloat() < option.getFloat(ALT_CHANCE)) {
                    if (option.contains(ALT_PER_INDEX)) {
                        ListTag alts = option.getList(ALT_PER_INDEX, Tag.TAG_COMPOUND);
                        return resolve(alts.getCompound(Math.min(index, alts.size() - 1)));
                    }
                    return resolve(option.getCompound(ALT));
                }
                return resolve(options.getCompound(index));
            }
            return option;
        }

        /** The same index for every block of a structure: a fresh draw from the structure's seed each time. */
        private int rollIndex(int[] weights) {
            RandomSource draw = RandomSource.create(structureSeed);
            int total = 0;
            for (int w : weights) {
                total += w;
            }
            int roll = draw.nextInt(Math.max(total, 1));
            for (int i = 0; i < weights.length; i++) {
                roll -= weights[i];
                if (roll < 0) {
                    return i;
                }
            }
            return 0;
        }
    }
}
