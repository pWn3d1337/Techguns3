package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.block.BioBlobBlock;

/**
 * Techguns blocks. Every block registered with {@link #withItem} also gets a block item.
 */
public final class TGBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Techguns.MODID);

    private static final List<DeferredBlock<? extends Block>> BLOCKS_WITH_ITEM = new ArrayList<>();
    private static final List<DeferredBlock<? extends Block>> CUBE_BLOCKS = new ArrayList<>();
    private static final List<DeferredBlock<StairBlock>> STAIR_BLOCKS = new ArrayList<>();

    // --- Ores ---
    public static final DeferredBlock<DropExperienceBlock> TIN_ORE = withItem("tin_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), stoneOre(3.0f)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_TIN_ORE = withItem("deepslate_tin_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), deepslateOre(4.5f)));

    public static final DeferredBlock<DropExperienceBlock> LEAD_ORE = withItem("lead_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), stoneOre(4.0f)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_LEAD_ORE = withItem("deepslate_lead_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), deepslateOre(6.0f)));
    public static final DeferredBlock<DropExperienceBlock> TITANIUM_ORE = withItem("titanium_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), stoneOre(5.0f)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_TITANIUM_ORE = withItem("deepslate_titanium_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), deepslateOre(7.5f)));
    public static final DeferredBlock<DropExperienceBlock> URANIUM_ORE = withItem("uranium_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), stoneOre(5.0f).lightLevel(state -> 4)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_URANIUM_ORE = withItem("deepslate_uranium_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), deepslateOre(7.5f).lightLevel(state -> 4)));

    // --- Bio gun ---
    /** Drops nothing (see the block loot provider). Glows with its size. */
    public static final DeferredBlock<BioBlobBlock> BIOBLOB = withItem("bioblob",
            () -> new BioBlobBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).noOcclusion().strength(0.0f)
                    .sound(SoundType.SLIME_BLOCK).emissiveRendering((state, level, pos) -> true)
                    .lightLevel(state -> (state.getValue(BioBlobBlock.SIZE) + 1) * 2)));

    // --- Decorative (Phase 5a; one block per Techguns 2 meta variant) ---
    public static final DeferredBlock<Block> CONCRETE_BROWN = decorative("concrete_brown", concrete());
    public static final DeferredBlock<Block> CONCRETE_BROWN_LIGHT = decorative("concrete_brown_light", concrete());
    public static final DeferredBlock<Block> CONCRETE_GREY = decorative("concrete_grey", concrete());
    public static final DeferredBlock<Block> CONCRETE_GREY_DARK = decorative("concrete_grey_dark", concrete());
    public static final DeferredBlock<Block> CONCRETE_BROWN_PIPES = decorative("concrete_brown_pipes", concrete());
    public static final DeferredBlock<Block> CONCRETE_BROWN_LIGHT_SCAFF = decorative("concrete_brown_light_scaff", concrete());
    /** Techguns 2 {@code concrete} variants, in meta order; the Camo Bench cycles through them. */
    public static final List<DeferredBlock<Block>> CONCRETE = List.of(CONCRETE_BROWN, CONCRETE_BROWN_LIGHT, CONCRETE_GREY, CONCRETE_GREY_DARK,
            CONCRETE_BROWN_PIPES, CONCRETE_BROWN_LIGHT_SCAFF);

    // metal panel (TGMetalPanelType): iron, hardness 8
    public static final DeferredBlock<Block> CONTAINER_RED = decorative("container_red", metal());
    public static final DeferredBlock<Block> CONTAINER_GREEN = decorative("container_green", metal());
    public static final DeferredBlock<Block> CONTAINER_BLUE = decorative("container_blue", metal());
    public static final DeferredBlock<Block> CONTAINER_ORANGE = decorative("container_orange", metal());
    public static final DeferredBlock<Block> PANEL_LARGE_BORDER = decorative("panel_large_border", metal());
    public static final DeferredBlock<Block> STEELFRAME_BLUE = decorative("steelframe_blue", metal());
    public static final DeferredBlock<Block> STEELFRAME_DARK = decorative("steelframe_dark", metal());
    public static final DeferredBlock<Block> STEELFRAME_SCAFFOLD = decorative("steelframe_scaffold", metal());
    /** Techguns 2 {@code metalpanel} variants, in meta order. */
    public static final List<DeferredBlock<Block>> METAL_PANEL = List.of(CONTAINER_RED, CONTAINER_GREEN, CONTAINER_BLUE, CONTAINER_ORANGE,
            PANEL_LARGE_BORDER, STEELFRAME_BLUE, STEELFRAME_DARK, STEELFRAME_SCAFFOLD);

    // nether metal (EnumNetherMetalType): iron, hardness 8; the lava pillar glows
    public static final DeferredBlock<Block> NETHERMETAL_PANEL = decorative("nethermetal_panel", metal());
    public static final DeferredBlock<Block> NETHERMETAL_GRATE1 = decorative("nethermetal_grate1", metal());
    public static final DeferredBlock<Block> NETHERMETAL_GRATE2 = decorative("nethermetal_grate2", metal());
    public static final DeferredBlock<Block> NETHERMETAL_GREY_DARK = decorative("nethermetal_grey_dark", metal());
    public static final DeferredBlock<Block> NETHERMETAL_GREY = decorative("nethermetal_grey", metal());
    public static final DeferredBlock<Block> NETHERMETAL_GREY_TILES = decorative("nethermetal_grey_tiles", metal());
    public static final DeferredBlock<Block> NETHERMETAL_BORDER_RED = decorative("nethermetal_border_red", metal());
    public static final DeferredBlock<Block> NETHERMETAL_PLATE_BLACK = decorative("nethermetal_plate_black", metal());
    public static final DeferredBlock<Block> NETHERMETAL_PLATE_RED = decorative("nethermetal_plate_red", metal());
    /** Lava behind a cutout frame; the only cube here with a custom model (see the block state provider). */
    public static final DeferredBlock<Block> NETHERMETAL_BORDER_LAVA = decorative("nethermetal_border_lava", metal().lightLevel(state -> 15));
    /** Techguns 2 {@code nethermetal} variants, in meta order. */
    public static final List<DeferredBlock<Block>> NETHER_METAL = List.of(NETHERMETAL_PANEL, NETHERMETAL_GRATE1, NETHERMETAL_GRATE2,
            NETHERMETAL_GREY_DARK, NETHERMETAL_GREY, NETHERMETAL_GREY_TILES, NETHERMETAL_BORDER_RED, NETHERMETAL_PLATE_BLACK,
            NETHERMETAL_PLATE_RED, NETHERMETAL_BORDER_LAVA);

    // neon lights (EnumLightblockType): glass, light level 15, hardness 4
    public static final DeferredBlock<Block> NEONTUBES2 = decorative("neontubes2", neon());
    public static final DeferredBlock<Block> NEONTUBES2_ROTATED = decorative("neontubes2_rotated", neon());
    public static final DeferredBlock<Block> NEONTUBES4 = decorative("neontubes4", neon());
    public static final DeferredBlock<Block> NEONTUBES4_ROTATED = decorative("neontubes4_rotated", neon());
    public static final DeferredBlock<Block> NEONSQUARE_WHITE = decorative("neonsquare_white", neon());
    /** Techguns 2 {@code neonlights} variants, in meta order. */
    public static final List<DeferredBlock<Block>> NEON_LIGHTS = List.of(NEONTUBES2, NEONTUBES2_ROTATED, NEONTUBES4, NEONTUBES4_ROTATED,
            NEONSQUARE_WHITE);

    // bug nest (plain blocks in Techguns 2: no falling, no bouncing)
    /** Techguns 2 {@code sand_hard}: sand, hardness 3, dug with a shovel. */
    public static final DeferredBlock<Block> BUGNEST_SAND = decorative("bugnest_sand", BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
            .instrument(NoteBlockInstrument.SNARE).strength(3.0f, 15.0f).sound(SoundType.SAND));
    /** Techguns 2 {@code slimy}: slime sound, hardness 4, pickaxe. */
    public static final DeferredBlock<Block> BUGNEST_EGGS = decorative("bugnest_eggs", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN)
            .requiresCorrectToolForDrops().strength(4.0f, 20.0f).sound(SoundType.SLIME_BLOCK));

    // stairs (BlockTGStairs; list order: type 1 = 1.12 meta 7, type 2 = meta 15)
    public static final DeferredBlock<StairBlock> PANEL_LARGE_BORDER_STAIRS = stairs("panel_large_border_stairs", PANEL_LARGE_BORDER, metal());
    public static final DeferredBlock<StairBlock> STEELFRAME_DARK_STAIRS = stairs("steelframe_dark_stairs", STEELFRAME_DARK, metal());
    public static final DeferredBlock<StairBlock> CONCRETE_GREY_DARK_STAIRS = stairs("concrete_grey_dark_stairs", CONCRETE_GREY_DARK,
            stairConcrete());
    public static final DeferredBlock<StairBlock> CONCRETE_BROWN_LIGHT_STAIRS = stairs("concrete_brown_light_stairs", CONCRETE_BROWN_LIGHT,
            stairConcrete());
    /** Techguns 2 {@code stairs_metal}: steel panel, carbon panel. */
    public static final List<DeferredBlock<StairBlock>> METAL_STAIRS = List.of(PANEL_LARGE_BORDER_STAIRS, STEELFRAME_DARK_STAIRS);
    /** Techguns 2 {@code stairs_concrete}: dark, light. */
    public static final List<DeferredBlock<StairBlock>> CONCRETE_STAIRS = List.of(CONCRETE_GREY_DARK_STAIRS, CONCRETE_BROWN_LIGHT_STAIRS);

    private TGBlocks() {
    }

    /** Techguns 2 {@code concrete}: rock, hardness 8 (blast resistance like 1.12: hardness * 5). */
    private static BlockBehaviour.Properties concrete() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(8.0f, 40.0f);
    }

    /** Techguns 2 iron material, metal sound, hardness 8. */
    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(8.0f, 40.0f)
                .sound(SoundType.METAL);
    }

    /** Techguns 2 {@code neonlights}: glass, light level 15 (setLightLevel(1f)), hardness 4. */
    private static BlockBehaviour.Properties neon() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops().strength(4.0f, 20.0f)
                .sound(SoundType.GLASS).lightLevel(state -> 15);
    }

    /** Techguns 2 {@code stairs_concrete}: rock, hardness 6. */
    private static BlockBehaviour.Properties stairConcrete() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(6.0f, 30.0f);
    }

    /** Stairs with the base block's texture; they drop themselves (datagen). */
    private static DeferredBlock<StairBlock> stairs(String name, DeferredBlock<Block> base, BlockBehaviour.Properties properties) {
        DeferredBlock<StairBlock> block = withItem(name, () -> new StairBlock(base.get().defaultBlockState(), properties));
        STAIR_BLOCKS.add(block);
        return block;
    }

    /** Stairs of the decorative blocks (datagen). */
    public static List<DeferredBlock<StairBlock>> stairBlocks() {
        return Collections.unmodifiableList(STAIR_BLOCKS);
    }

    /** A plain full block with item that drops itself and uses {@code textures/block/<name>.png} on all sides. */
    private static DeferredBlock<Block> decorative(String name, BlockBehaviour.Properties properties) {
        DeferredBlock<Block> block = withItem(name, () -> new Block(properties));
        CUBE_BLOCKS.add(block);
        return block;
    }

    /** Full cube blocks with one texture that drop themselves (datagen). */
    public static List<DeferredBlock<? extends Block>> cubeBlocks() {
        return Collections.unmodifiableList(CUBE_BLOCKS);
    }

    private static BlockBehaviour.Properties stoneOre(float hardness) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(hardness, 3.0f);
    }

    private static BlockBehaviour.Properties deepslateOre(float hardness) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(hardness, 3.0f).sound(SoundType.DEEPSLATE);
    }

    private static <B extends Block> DeferredBlock<B> withItem(String name, java.util.function.Supplier<B> factory) {
        DeferredBlock<B> block = BLOCKS.register(name, factory);
        TGItems.ITEMS.registerSimpleBlockItem(block);
        BLOCKS_WITH_ITEM.add(block);
        return block;
    }

    /** All blocks that have a block item, in registration order. */
    public static List<DeferredBlock<? extends Block>> blocksWithItem() {
        return Collections.unmodifiableList(BLOCKS_WITH_ITEM);
    }
}
