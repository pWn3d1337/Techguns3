package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.function.BiFunction;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.block.AmmoBenchBlock;
import techguns.block.BioBlobBlock;
import techguns.block.BunkerDoorBlock;
import techguns.block.CamoNetBlock;
import techguns.block.CamoNetTopBlock;
import techguns.block.Door3x3Block;
import techguns.block.Door3x3Item;
import techguns.block.MilitaryCrateBlock;
import techguns.block.SandbagsBlock;
import techguns.block.TGLadderBlock;
import techguns.block.TGLampBlock;
import techguns.block.TGLanternBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.resources.ResourceLocation;
import techguns.machine.TGMachineBlock;
import techguns.multiblock.MultiblockControllerBlock;
import techguns.multiblock.MultiblockPartBlock;
import techguns.block.OreDrillScaffoldBlock;

/**
 * Techguns blocks. Every block registered with {@link #withItem} also gets a block item.
 */
public final class TGBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Techguns.MODID);

    private static final List<DeferredBlock<? extends Block>> BLOCKS_WITH_ITEM = new ArrayList<>();
    private static final List<DeferredBlock<? extends Block>> CUBE_BLOCKS = new ArrayList<>();
    private static final List<DeferredBlock<StairBlock>> STAIR_BLOCKS = new ArrayList<>();
    /** Base block id -> its stairs. */
    private static final java.util.Map<ResourceLocation, DeferredBlock<StairBlock>> STAIRS_BY_BASE = new java.util.HashMap<>();

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
    /** Techguns 2 machines: iron material, hardness 4, metal sound; drawn by their block entity renderer. */
    private static BlockBehaviour.Properties machine() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(4.0f, 20.0f)
                .sound(SoundType.METAL).noOcclusion();
    }

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

    // --- Shaped decorative blocks (Phase 5a2; one block per Techguns 2 meta variant) ---
    // lamp0 (EnumLampType): glass, light level 15, hardness 4. Lamps are flat and attach to a face, lanterns hang between supports.
    public static final DeferredBlock<TGLampBlock> LAMP_YELLOW = withItem("lamp_yellow", () -> new TGLampBlock(lamp()));
    public static final DeferredBlock<TGLampBlock> LAMP_WHITE = withItem("lamp_white", () -> new TGLampBlock(lamp()));
    public static final DeferredBlock<TGLanternBlock> LAMP_YELLOW_LANTERN = withItem("lamp_yellow_lantern", () -> new TGLanternBlock(lamp()));
    public static final DeferredBlock<TGLanternBlock> LAMP_WHITE_LANTERN = withItem("lamp_white_lantern", () -> new TGLanternBlock(lamp()));
    /** Techguns 2 {@code lamp0} types yellow, white (the Camo Bench switches the colour). */
    public static final List<DeferredBlock<TGLampBlock>> LAMPS = List.of(LAMP_YELLOW, LAMP_WHITE);
    /** Techguns 2 {@code lamp0} types yellow lantern, white lantern. */
    public static final List<DeferredBlock<TGLanternBlock>> LANTERNS = List.of(LAMP_YELLOW_LANTERN, LAMP_WHITE_LANTERN);

    // ladder0 (EnumLadderType): iron, metal sound, hardness 6, climbable (tag), free-standing
    public static final DeferredBlock<TGLadderBlock> LADDER_METAL = withItem("ladder_metal", () -> new TGLadderBlock(ladder()));
    public static final DeferredBlock<TGLadderBlock> LADDER_SHINY = withItem("ladder_shiny", () -> new TGLadderBlock(ladder()));
    public static final DeferredBlock<TGLadderBlock> LADDER_RUSTY = withItem("ladder_rusty", () -> new TGLadderBlock(ladder()));
    public static final DeferredBlock<TGLadderBlock> LADDER_CARBON = withItem("ladder_carbon", () -> new TGLadderBlock(ladder()));
    /** Techguns 2 {@code ladder0} types in meta order. */
    public static final List<DeferredBlock<TGLadderBlock>> LADDERS = List.of(LADDER_METAL, LADDER_SHINY, LADDER_RUSTY, LADDER_CARBON);
    /** Techguns 2 {@code slimyladder}: a vanilla ladder (needs a wall) with a slime sound and the bug nest "slimy trail" model. */
    public static final DeferredBlock<LadderBlock> SLIMY_LADDER = withItem("slimy_ladder", () -> new LadderBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GREEN).strength(0.4f).sound(SoundType.SLIME_BLOCK).noOcclusion()));

    // camonet / camonet_top (EnumCamoNetType): cloth, hardness 2
    public static final DeferredBlock<CamoNetBlock> CAMO_NET_WOOD = withItem("camo_net_wood", () -> new CamoNetBlock(camoNet()));
    public static final DeferredBlock<CamoNetBlock> CAMO_NET_DESERT = withItem("camo_net_desert", () -> new CamoNetBlock(camoNet()));
    public static final DeferredBlock<CamoNetBlock> CAMO_NET_SNOW = withItem("camo_net_snow", () -> new CamoNetBlock(camoNet()));
    public static final List<DeferredBlock<CamoNetBlock>> CAMO_NETS = List.of(CAMO_NET_WOOD, CAMO_NET_DESERT, CAMO_NET_SNOW);
    public static final DeferredBlock<CamoNetTopBlock> CAMO_NET_TOP_WOOD = withItem("camo_net_top_wood", () -> new CamoNetTopBlock(camoNet()));
    public static final DeferredBlock<CamoNetTopBlock> CAMO_NET_TOP_DESERT = withItem("camo_net_top_desert", () -> new CamoNetTopBlock(camoNet()));
    public static final DeferredBlock<CamoNetTopBlock> CAMO_NET_TOP_SNOW = withItem("camo_net_top_snow", () -> new CamoNetTopBlock(camoNet()));
    public static final List<DeferredBlock<CamoNetTopBlock>> CAMO_NETS_TOP = List.of(CAMO_NET_TOP_WOOD, CAMO_NET_TOP_DESERT, CAMO_NET_TOP_SNOW);

    /** Techguns 2 {@code sandbags}: cloth, hardness 3, blast resistance 15. */
    public static final DeferredBlock<SandbagsBlock> SANDBAGS = withItem("sandbags", () -> new SandbagsBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BROWN).strength(3.0f, 15.0f).sound(SoundType.WOOL).noOcclusion()));

    // military_crate (EnumMilitaryCrateType): wood, hardness 4; breaking one drops loot (see the block loot provider)
    public static final DeferredBlock<MilitaryCrateBlock> MILITARY_CRATE_AMMO = crate("military_crate_ammo");
    public static final DeferredBlock<MilitaryCrateBlock> MILITARY_CRATE_GUN = crate("military_crate_gun");
    public static final DeferredBlock<MilitaryCrateBlock> MILITARY_CRATE_ARMOR = crate("military_crate_armor");
    public static final DeferredBlock<MilitaryCrateBlock> MILITARY_CRATE_MEDICAL = crate("military_crate_medical");
    public static final DeferredBlock<MilitaryCrateBlock> MILITARY_CRATE_EXPLOSIVE = crate("military_crate_explosive");
    public static final DeferredBlock<MilitaryCrateBlock> MILITARY_CRATE_GENERIC_OAK = crate("military_crate_generic_oak");
    public static final DeferredBlock<MilitaryCrateBlock> MILITARY_CRATE_GENERIC_JUNGLE = crate("military_crate_generic_jungle");
    public static final DeferredBlock<MilitaryCrateBlock> MILITARY_CRATE_GENERIC_BIRCH = crate("military_crate_generic_birch");
    public static final DeferredBlock<MilitaryCrateBlock> MILITARY_CRATE_GENERIC_SPRUCE = crate("military_crate_generic_spruce");
    /** Techguns 2 {@code military_crate} types in meta order. */
    public static final List<DeferredBlock<MilitaryCrateBlock>> MILITARY_CRATES = List.of(MILITARY_CRATE_AMMO, MILITARY_CRATE_GUN,
            MILITARY_CRATE_ARMOR, MILITARY_CRATE_MEDICAL, MILITARY_CRATE_EXPLOSIVE, MILITARY_CRATE_GENERIC_OAK, MILITARY_CRATE_GENERIC_JUNGLE,
            MILITARY_CRATE_GENERIC_BIRCH, MILITARY_CRATE_GENERIC_SPRUCE);

    // doors
    /** Techguns 2 {@code bunkerdoor} (2x1): iron, hardness 8. */
    public static final DeferredBlock<BunkerDoorBlock> BUNKER_DOOR = withItem("bunker_door", () -> BunkerDoorBlock.create(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(8.0f, 40.0f).sound(SoundType.METAL)
                    .noOcclusion().pushReaction(PushReaction.BLOCK)), (block, properties) -> new DoubleHighBlockItem(block, properties));
    /** Techguns 2 {@code door3x3} (EnumDoorType): iron, hardness 5; the doors are nine blocks big, see {@link Door3x3Block}. */
    public static final DeferredBlock<Door3x3Block> DOOR_METAL_3X3 = door3x3("door_metal_3x3", Door3x3Block.Kind.METAL);
    public static final DeferredBlock<Door3x3Block> DOOR_HANGAR_UP_3X3 = door3x3("door_hangar_up_3x3", Door3x3Block.Kind.HANGAR_UP);
    public static final DeferredBlock<Door3x3Block> DOOR_HANGAR_DOWN_3X3 = door3x3("door_hangar_down_3x3", Door3x3Block.Kind.HANGAR_DOWN);
    public static final DeferredBlock<Door3x3Block> DOOR_NETHER_3X3 = door3x3("door_nether_3x3", Door3x3Block.Kind.NETHER);
    /** Techguns 2 {@code door3x3} types in meta order. */
    public static final List<DeferredBlock<Door3x3Block>> DOORS_3X3 = List.of(DOOR_METAL_3X3, DOOR_HANGAR_UP_3X3, DOOR_HANGAR_DOWN_3X3,
            DOOR_NETHER_3X3);

    // --- Machines (Phase 5b; one block per Techguns 2 machine type) ---
    public static final DeferredBlock<TGMachineBlock> METAL_PRESS = withItem("metal_press",
            () -> new TGMachineBlock(machine(), TGBlockEntities.METAL_PRESS));
    public static final DeferredBlock<TGMachineBlock> AMMO_PRESS = withItem("ammo_press",
            () -> new TGMachineBlock(machine(), TGBlockEntities.AMMO_PRESS));
    public static final DeferredBlock<TGMachineBlock> BLAST_FURNACE = withItem("blast_furnace",
            () -> new TGMachineBlock(machine(), TGBlockEntities.BLAST_FURNACE, Shapes.block(), RenderShape.MODEL));
    public static final DeferredBlock<TGMachineBlock> GRINDER = withItem("grinder",
            () -> new TGMachineBlock(machine(), TGBlockEntities.GRINDER, Shapes.block(), RenderShape.MODEL));
    public static final DeferredBlock<TGMachineBlock> CHEM_LAB = withItem("chem_lab",
            () -> new TGMachineBlock(machine(), TGBlockEntities.CHEM_LAB));
    /** Techguns 2 OBJ model; the item on top is drawn by the renderer. */
    public static final DeferredBlock<TGMachineBlock> CHARGING_STATION = withItem("charging_station",
            () -> new TGMachineBlock(machine(), TGBlockEntities.CHARGING_STATION, Shapes.block(), RenderShape.MODEL));

    // --- Benches (Phase 5b3; unpowered, Techguns 2 block models) ---
    public static final DeferredBlock<TGMachineBlock> REPAIR_BENCH = withItem("repair_bench",
            () -> new TGMachineBlock(machine(), TGBlockEntities.REPAIR_BENCH, Shapes.block(), RenderShape.MODEL));
    public static final DeferredBlock<TGMachineBlock> CAMO_BENCH = withItem("camo_bench",
            () -> new TGMachineBlock(machine(), TGBlockEntities.CAMO_BENCH, Shapes.block(), RenderShape.MODEL));
    /** Techguns 2 {@code UpgradeBenchTileEnt.BLOCK_BB}: 13 pixels high. */
    public static final DeferredBlock<TGMachineBlock> UPGRADE_BENCH = withItem("upgrade_bench",
            () -> new TGMachineBlock(machine(), TGBlockEntities.UPGRADE_BENCH, Block.box(0, 0, 0, 16, 13, 16), RenderShape.MODEL));
    // --- Multiblocks (Phase 5c; Techguns 2 MultiBlockMachine variants, one block each) ---
    public static final DeferredBlock<MultiblockPartBlock> FABRICATOR_HOUSING = withItem("fabricator_housing",
            () -> new MultiblockPartBlock(machine()));
    public static final DeferredBlock<MultiblockPartBlock> FABRICATOR_GLASS = withItem("fabricator_glass",
            () -> new MultiblockPartBlock(machine().sound(SoundType.GLASS)));
    public static final DeferredBlock<MultiblockControllerBlock> FABRICATOR_CONTROLLER = withItem("fabricator_controller",
            () -> new MultiblockControllerBlock(machine(), TGBlockEntities.FABRICATOR));
    public static final DeferredBlock<MultiblockPartBlock> REACTION_CHAMBER_HOUSING = withItem("reaction_chamber_housing",
            () -> new MultiblockPartBlock(machine()));
    public static final DeferredBlock<MultiblockPartBlock> REACTION_CHAMBER_GLASS = withItem("reaction_chamber_glass",
            () -> new MultiblockPartBlock(machine().sound(SoundType.GLASS)));
    public static final DeferredBlock<MultiblockControllerBlock> REACTION_CHAMBER_CONTROLLER = withItem("reaction_chamber_controller",
            () -> new MultiblockControllerBlock(machine(), TGBlockEntities.REACTION_CHAMBER));
    public static final DeferredBlock<MultiblockPartBlock> ORE_DRILL_FRAME = withItem("ore_drill_frame", () -> new MultiblockPartBlock(machine()));
    public static final DeferredBlock<OreDrillScaffoldBlock> ORE_DRILL_SCAFFOLD = withItem("ore_drill_scaffold",
            () -> new OreDrillScaffoldBlock(machine()));
    public static final DeferredBlock<MultiblockPartBlock> ORE_DRILL_ROD = withItem("ore_drill_rod", () -> new MultiblockPartBlock(machine()),
            techguns.multiblock.OreDrillPartItem::new);
    public static final DeferredBlock<MultiblockPartBlock> ORE_DRILL_ENGINE = withItem("ore_drill_engine", () -> new MultiblockPartBlock(machine()));
    public static final DeferredBlock<MultiblockControllerBlock> ORE_DRILL_CONTROLLER = withItem("ore_drill_controller",
            () -> new MultiblockControllerBlock(machine(), TGBlockEntities.ORE_DRILL), techguns.multiblock.OreDrillPartItem::new);

    /** Techguns 2 turret base; base and turret are drawn by renderers. */
    public static final DeferredBlock<techguns.block.TurretBlock> TURRET = withItem("turret", () -> new techguns.block.TurretBlock(machine()));

    // --- TG spawners (Phase 6f; Techguns 2 tg_spawner hole / soldier_spawn) ---
    public static final DeferredBlock<techguns.block.TGSpawnerBlock> MONSTER_HOLE = withItem("monster_hole",
            () -> new techguns.block.TGSpawnerBlock(spawner(), () -> TGNpcs.ZOMBIE_SOLDIER.type().get()));
    public static final DeferredBlock<techguns.block.TGSpawnerBlock> MILITARY_SPAWN_POINT = withItem("military_spawn_point",
            () -> new techguns.block.TGSpawnerBlock(spawner(), () -> TGNpcs.ARMY_SOLDIER.type().get()));

    // --- Explosive charges (Phase 5d; Techguns 2 explosive_charge variants) ---
    public static final DeferredBlock<techguns.block.ExplosiveChargeBlock> EXPLOSIVE_CHARGE = withItem("explosive_charge",
            () -> new techguns.block.ExplosiveChargeBlock(charge(), techguns.machine.ExplosiveChargeBlockEntity.Kind.TNT));
    public static final DeferredBlock<techguns.block.ExplosiveChargeBlock> EXPLOSIVE_CHARGE_ADVANCED = withItem("explosive_charge_advanced",
            () -> new techguns.block.ExplosiveChargeBlock(charge(), techguns.machine.ExplosiveChargeBlockEntity.Kind.ADVANCED));

    // --- Ore clusters (Techguns 2 TGOreClusters): unbreakable Ore Drill targets, placed by worldgen (Phase 7) ---
    public static final DeferredBlock<Block> ORE_CLUSTER_COAL = oreCluster("coal");
    public static final DeferredBlock<Block> ORE_CLUSTER_COMMON_METAL = oreCluster("common_metal");
    public static final DeferredBlock<Block> ORE_CLUSTER_RARE_METAL = oreCluster("rare_metal");
    public static final DeferredBlock<Block> ORE_CLUSTER_SHINY_METAL = oreCluster("shiny_metal");
    public static final DeferredBlock<Block> ORE_CLUSTER_URANIUM = oreCluster("uranium");
    public static final DeferredBlock<Block> ORE_CLUSTER_COMMON_GEM = oreCluster("common_gem");
    public static final DeferredBlock<Block> ORE_CLUSTER_SHINY_GEM = oreCluster("shiny_gem");
    public static final DeferredBlock<Block> ORE_CLUSTER_NETHER_CRYSTAL = oreCluster("nether_crystal");
    public static final DeferredBlock<Block> ORE_CLUSTER_OIL = oreCluster("oil");
    public static final List<DeferredBlock<Block>> ORE_CLUSTERS = List.of(ORE_CLUSTER_COAL, ORE_CLUSTER_COMMON_METAL, ORE_CLUSTER_RARE_METAL,
            ORE_CLUSTER_SHINY_METAL, ORE_CLUSTER_URANIUM, ORE_CLUSTER_COMMON_GEM, ORE_CLUSTER_SHINY_GEM, ORE_CLUSTER_NETHER_CRYSTAL, ORE_CLUSTER_OIL);

    /** Fabric Ammo Bench: no block entity, the menu keeps the inputs. */
    public static final DeferredBlock<AmmoBenchBlock> AMMO_BENCH = withItem("ammo_bench", () -> new AmmoBenchBlock(machine()));

    private TGBlocks() {
    }

    /** Techguns 2 {@code lamp0}: glass, light level 15, hardness 4 (no tool needed). */
    private static BlockBehaviour.Properties lamp() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(4.0f, 20.0f).sound(SoundType.GLASS)
                .lightLevel(state -> 15).noOcclusion();
    }

    /** Techguns 2 {@code ladder0}: iron, metal sound, hardness 6. */
    private static BlockBehaviour.Properties ladder() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(6.0f, 30.0f).sound(SoundType.METAL).noOcclusion();
    }

    /** Techguns 2 {@code camonet}: cloth, hardness 2. */
    private static BlockBehaviour.Properties camoNet() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(2.0f, 10.0f).sound(SoundType.WOOL).noOcclusion();
    }

    private static DeferredBlock<MilitaryCrateBlock> crate(String name) {
        return withItem(name, () -> new MilitaryCrateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.BASS).strength(4.0f, 20.0f).sound(SoundType.WOOD).noOcclusion()));
    }

    private static DeferredBlock<Door3x3Block> door3x3(String name, Door3x3Block.Kind kind) {
        // solid also when open (no collision): fluids would otherwise flow in and wash the door away
        return withItem(name, () -> new Door3x3Block(kind, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f, 25.0f)
                .sound(SoundType.METAL).noOcclusion().forceSolidOn().pushReaction(PushReaction.BLOCK)), (block, properties) -> new Door3x3Item(block, properties));
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
        STAIRS_BY_BASE.put(base.getId(), block);
        return block;
    }

    /** The stairs made from {@code base}, if there are any. */
    public static java.util.Optional<DeferredBlock<StairBlock>> stairsOf(DeferredBlock<? extends Block> base) {
        return java.util.Optional.ofNullable(STAIRS_BY_BASE.get(base.getId()));
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

    /** Techguns 2 {@code BasicMachine} with the explosive charge's small box: iron, hardness 4, no tool needed. */
    /** Techguns 2 spawners: unbreakable, no collision, nothing drops. */
    private static BlockBehaviour.Properties spawner() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(-1.0f, 3600000.0f).noOcclusion().pushReaction(PushReaction.BLOCK);
    }

    private static BlockBehaviour.Properties charge() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f, 20.0f).sound(SoundType.METAL).noOcclusion()
                .pushReaction(PushReaction.DESTROY);
    }

    /** Techguns 2 {@code BlockOreCluster}: unbreakable, no drops. */
    private static DeferredBlock<Block> oreCluster(String name) {
        return withItem("ore_cluster_" + name, () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM).strength(-1.0f, 3600000.0f).noLootTable().isValidSpawn((s, l, p, e) -> false)));
    }

    private static BlockBehaviour.Properties stoneOre(float hardness) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(hardness, 3.0f);
    }

    private static BlockBehaviour.Properties deepslateOre(float hardness) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(hardness, 3.0f).sound(SoundType.DEEPSLATE);
    }

    /** Registers a block with a custom block item. */
    // --- Techguns 2 debug tools (hidden and unusable unless the debug config is on, see TGDebug) ---
    public static final DeferredBlock<Block> AIR_MARKER = debugBlock("air_marker", () -> new Block(marker()));
    public static final DeferredBlock<Block> ANTI_AIR_MARKER = debugBlock("anti_air_marker", () -> new Block(marker()));
    public static final DeferredBlock<techguns.debug.InteriorMarkerBlock> INTERIOR_MARKER = debugBlock("interior_marker",
            () -> new techguns.debug.InteriorMarkerBlock(marker()));
    public static final DeferredBlock<techguns.debug.DungeonToolBlock> DUNGEON_SCANNER = debugBlock("dungeon_scanner",
            () -> new techguns.debug.DungeonToolBlock(machine(), TGBlockEntities.DUNGEON_SCANNER));
    public static final DeferredBlock<techguns.debug.DungeonToolBlock> DUNGEON_GENERATOR = debugBlock("dungeon_generator",
            () -> new techguns.debug.DungeonToolBlock(machine(), TGBlockEntities.DUNGEON_GENERATOR));

    /** Techguns 2 debug markers: ground blocks that break at once. */
    private static BlockBehaviour.Properties marker() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).instabreak().sound(SoundType.STONE);
    }

    private static <B extends Block> DeferredBlock<B> debugBlock(String name, java.util.function.Supplier<B> factory) {
        return withItem(name, factory, (block, properties) -> new techguns.debug.DebugBlockItem(block, properties));
    }

    private static <B extends Block> DeferredBlock<B> withItem(String name, java.util.function.Supplier<B> factory,
                                                                BiFunction<B, Item.Properties, ? extends Item> itemFactory) {
        DeferredBlock<B> block = BLOCKS.register(name, factory);
        TGItems.ITEMS.register(name, () -> itemFactory.apply(block.get(), new Item.Properties()));
        BLOCKS_WITH_ITEM.add(block);
        return block;
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
