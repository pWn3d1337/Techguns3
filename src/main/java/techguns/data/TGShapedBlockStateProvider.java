package techguns.data;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import techguns.Techguns;
import techguns.block.CamoNetBlock;
import techguns.block.Door3x3Block;
import techguns.block.SandbagsBlock;
import techguns.block.TGLampBlock;
import techguns.registry.TGBlocks;
import techguns.TGIds;

/**
 * Block states, block models and item models of the shaped decorative blocks (Phase 5a2). The geometry of the Techguns 2
 * models (lamp, ladder, camo net, sandbags, crate, door parents) is kept as parent models in
 * {@code src/main/resources/assets/techguns/models/block} (copied from 1.12 with the texture paths fixed); the per-type
 * models, block states and item models are generated here.
 */
public class TGShapedBlockStateProvider extends BlockStateProvider {
    private static final String CUTOUT = "cutout";
    private static final String TRANSLUCENT = "translucent";

    public TGShapedBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Techguns.MODID, existingFileHelper);
    }

    /** A distinct name: the data generator rejects two providers with the same name. */
    @Override
    public String getName() {
        return "Shaped Block States: " + Techguns.MODID;
    }

    @Override
    protected void registerStatesAndModels() {
        lamps();
        lanterns();
        ladders();
        slimyLadder();
        camoNets();
        camoNetsTop();
        sandbags();
        crates();
        bunkerDoor();
        doors3x3();
        machines();
    }

    private ModelFile existing(String name) {
        return models().getExistingFile(modLoc("block/" + name));
    }

    private ResourceLocation tex(String name) {
        return modLoc("block/" + name);
    }

    private static String path(DeferredBlock<? extends Block> block) {
        return block.getId().getPath();
    }

    // --- lamps and lanterns ---

    private void lamps() {
        lamp(TGBlocks.LAMP_YELLOW, "lamp01");
        lamp(TGBlocks.LAMP_WHITE, "lamp02");
    }

    private void lamp(DeferredBlock<TGLampBlock> block, String texture) {
        BlockModelBuilder model = models().withExistingParent("block/" + path(block), modLoc("block/lamp_base"))
                .texture("texture", tex(texture)).renderType(CUTOUT);
        getVariantBuilder(block.get()).forAllStates(state -> {
            int[] rotation = switch (state.getValue(TGLampBlock.FACING)) {
                case UP -> new int[]{180, 0};
                case NORTH -> new int[]{90, 180};
                case WEST -> new int[]{90, 90};
                case SOUTH -> new int[]{90, 0};
                case EAST -> new int[]{90, 270};
                default -> new int[]{0, 0};
            };
            return ConfiguredModel.builder().modelFile(model).rotationX(rotation[0]).rotationY(rotation[1]).build();
        });
        itemModels().withExistingParent(path(block), modLoc("block/lamp_inventory_base")).texture("tex", tex(texture));
    }

    private void lanterns() {
        lantern(TGBlocks.LAMP_YELLOW_LANTERN, "lantern01");
        lantern(TGBlocks.LAMP_WHITE_LANTERN, "lantern02");
    }

    private void lantern(DeferredBlock<? extends Block> block, String texture) {
        String name = path(block);
        ModelFile base = lanternModel(name, "lantern_base", texture);
        ModelFile side = lanternModel(name + "_side", "lantern_side_base", texture);
        ModelFile top = lanternModel(name + "_top", "lantern_top_base", texture);
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block.get());
        builder.part().modelFile(base).addModel().end();
        builder.part().modelFile(side).rotationY(270).addModel().condition(BlockStateProperties.NORTH, true).end();
        builder.part().modelFile(side).addModel().condition(BlockStateProperties.EAST, true).end();
        builder.part().modelFile(side).rotationY(90).addModel().condition(BlockStateProperties.SOUTH, true).end();
        builder.part().modelFile(side).rotationY(180).addModel().condition(BlockStateProperties.WEST, true).end();
        builder.part().modelFile(top).addModel().condition(BlockStateProperties.UP, true).end();
        builder.part().modelFile(top).rotationX(180).addModel().condition(BlockStateProperties.DOWN, true).end();
        itemModels().withExistingParent(name, modLoc("block/lantern_inventory_base"))
                .texture("side", tex(texture + "_side")).texture("top", tex(texture + "_top")).texture("particle", tex(texture + "_side"));
    }

    private ModelFile lanternModel(String name, String parent, String texture) {
        return models().withExistingParent("block/" + name, modLoc("block/" + parent))
                .texture("side", tex(texture + "_side")).texture("particle", tex(texture + "_side")).texture("top", tex(texture + "_top"))
                .renderType(CUTOUT);
    }

    // --- ladders ---

    private void ladders() {
        ladder(TGBlocks.LADDER_METAL, "metal_ladder");
        ladder(TGBlocks.LADDER_SHINY, "metal_ladder_shiny");
        ladder(TGBlocks.LADDER_RUSTY, "metal_ladder_rusty");
        ladder(TGBlocks.LADDER_CARBON, "metal_ladder_carbon");
        ladder(TGBlocks.LADDER_OXIDIZED_COPPER, "metal_ladder_oxidized_copper");
        ladder(TGBlocks.LADDER_COPPER, "metal_ladder_copper");
        ladder(TGBlocks.LADDER_HAZARD, "metal_ladder_hazard");
        ladder(TGBlocks.LADDER_NETHER, "metal_ladder_nether");
    }

    private void ladder(DeferredBlock<? extends Block> block, String texture) {
        BlockModelBuilder model = models().withExistingParent("block/" + path(block), modLoc("block/ladder_base"))
                .texture("texture", tex(texture)).renderType(CUTOUT);
        getVariantBuilder(block.get()).forAllStatesExcept(state -> {
            int y = switch (state.getValue(LadderBlock.FACING)) {
                case NORTH -> 180;
                case EAST -> 270;
                case WEST -> 90;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(model).rotationY(y).build();
        }, LadderBlock.WATERLOGGED);
        // a flat sprite like the vanilla ladder (the 1.12 block model showed only its edge in the inventory)
        itemModels().withExistingParent(path(block), mcLoc("item/generated")).texture("layer0", tex(texture));
    }

    private void slimyLadder() {
        BlockModelBuilder model = models().withExistingParent("block/slimy_ladder", modLoc("block/slimyladder")).renderType(CUTOUT);
        getVariantBuilder(TGBlocks.SLIMY_LADDER.get()).forAllStatesExcept(state -> {
            int y = switch (state.getValue(LadderBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(model).rotationX(90).rotationY(y).build();
        }, LadderBlock.WATERLOGGED);
        // the trail lies flat in its model: seen from above in the inventory, not edge-on
        itemModels().withExistingParent("slimy_ladder", modLoc("block/slimy_ladder")).transforms()
                .transform(ItemDisplayContext.GUI).rotation(90, 0, 0).end().end();
    }

    // --- camo nets ---

    /** Plank texture behind the net and the net texture per type: woodland, desert, snow. */
    private static final String[] NET_TYPES = {"wood", "desert", "snow"};
    private static final String[] NET_PLANKS = {"oak_planks", "acacia_planks", "spruce_planks"};
    private static final String[] NET_TEXTURES = {"camonet", "camonet_desert", "camonet_snow"};

    private void camoNets() {
        for (int type = 0; type < 3; type++) {
            DeferredBlock<CamoNetBlock> block = TGBlocks.CAMO_NETS.get(type);
            ResourceLocation net = tex(NET_TEXTURES[type]);
            ResourceLocation planks = mcLoc("block/" + NET_PLANKS[type]);
            ModelFile stick = netModel(block, "stick", "camonet_stick", planks, net);
            ModelFile half = netModel(block, "half", "camonet_half", planks, net);
            ModelFile full = netModel(block, "full", "camonet_full", planks, net);
            MultiPartBlockStateBuilder builder = getMultipartBuilder(block.get());
            for (int index = 0; index < 16; index++) {
                boolean n = (index & 8) != 0;
                boolean e = (index & 4) != 0;
                boolean s = (index & 2) != 0;
                boolean w = (index & 1) != 0;
                boolean straight = (e && w && !n && !s) || (n && s && !e && !w);
                if (straight) {
                    // a straight run is one full plane (east-west: y0, north-south: y270)
                    netPart(builder, full, e ? 0 : 270, n, e, s, w);
                    continue;
                }
                netPart(builder, stick, 0, n, e, s, w);
                if (e) {
                    netPart(builder, half, 0, n, e, s, w);
                }
                if (s) {
                    netPart(builder, half, 90, n, e, s, w);
                }
                if (w) {
                    netPart(builder, half, 180, n, e, s, w);
                }
                if (n) {
                    netPart(builder, half, 270, n, e, s, w);
                }
            }
            itemModels().withExistingParent(path(block), modLoc("block/camonet_inventory"))
                    .texture("0", planks).texture("1", net).texture("particle", net);
        }
    }

    private ModelFile netModel(DeferredBlock<? extends Block> block, String part, String parent, ResourceLocation planks, ResourceLocation net) {
        return models().withExistingParent("block/" + path(block) + "_" + part, modLoc("block/" + parent))
                .texture("0", planks).texture("1", net).texture("particle", net).renderType(CUTOUT);
    }

    private void netPart(MultiPartBlockStateBuilder builder, ModelFile model, int rotationY, boolean n, boolean e, boolean s, boolean w) {
        builder.part().modelFile(model).rotationY(rotationY).uvLock(true).addModel()
                .condition(CamoNetBlock.NORTH, n).condition(CamoNetBlock.EAST, e).condition(CamoNetBlock.SOUTH, s).condition(CamoNetBlock.WEST, w).end();
    }

    private void camoNetsTop() {
        // per connection index (n=8, e=4, s=2, w=1): {model, rotationY} pairs, 1.12 camonet_top.json
        String[][][] layouts = new String[16][][];
        layouts[0] = parts("center", 0);
        layouts[1] = parts("side", 180);
        layouts[2] = parts("side", 90);
        layouts[3] = parts("corner", 180);
        layouts[4] = parts("side", 0);
        layouts[5] = parts("side", 0, "side", 180);
        layouts[6] = parts("corner", 90);
        layouts[7] = parts("side2", 90);
        layouts[8] = parts("side", 270);
        layouts[9] = parts("corner", 270);
        layouts[10] = parts("side", 270, "side", 90);
        layouts[11] = parts("side2", 180);
        layouts[12] = parts("corner", 0);
        layouts[13] = parts("side2", 270);
        layouts[14] = parts("side2", 0);
        layouts[15] = parts("full", 0);
        for (int type = 0; type < 3; type++) {
            DeferredBlock<? extends Block> block = TGBlocks.CAMO_NETS_TOP.get(type);
            ResourceLocation net = tex(NET_TEXTURES[type]);
            MultiPartBlockStateBuilder builder = getMultipartBuilder(block.get());
            for (int index = 0; index < 16; index++) {
                for (String[] part : layouts[index]) {
                    ModelFile model = models().withExistingParent("block/" + path(block) + "_" + part[0], modLoc("block/camonet_top_" + part[0]))
                            .texture("0", net).texture("particle", net).renderType(CUTOUT);
                    builder.part().modelFile(model).rotationY(Integer.parseInt(part[1])).uvLock(true).addModel()
                            .condition(CamoNetBlock.NORTH, (index & 8) != 0).condition(CamoNetBlock.EAST, (index & 4) != 0)
                            .condition(CamoNetBlock.SOUTH, (index & 2) != 0).condition(CamoNetBlock.WEST, (index & 1) != 0).end();
                }
            }
            itemModels().withExistingParent(path(block), modLoc("block/camonet_top_inventory")).texture("0", net).texture("particle", net);
        }
    }

    /** Alternating model name and rotation arguments to {model, rotation} pairs. */
    private static String[][] parts(Object... modelAndRotation) {
        String[][] result = new String[modelAndRotation.length / 2][];
        for (int i = 0; i < result.length; i++) {
            result[i] = new String[]{modelAndRotation[2 * i].toString(), modelAndRotation[2 * i + 1].toString()};
        }
        return result;
    }

    // --- sandbags ---

    private void sandbags() {
        ModelFile center = existing("sandbags_center");
        ModelFile side = existing("sandbags_side");
        ModelFile corner = existing("sandbags_corner");
        MultiPartBlockStateBuilder builder = getMultipartBuilder(TGBlocks.SANDBAGS.get());
        builder.part().modelFile(center).addModel().end();
        builder.part().modelFile(side).uvLock(true).addModel().condition(SandbagsBlock.NORTH, true).end();
        builder.part().modelFile(side).rotationY(90).uvLock(true).addModel().condition(SandbagsBlock.EAST, true).end();
        builder.part().modelFile(side).rotationY(180).uvLock(true).addModel().condition(SandbagsBlock.SOUTH, true).end();
        builder.part().modelFile(side).rotationY(270).uvLock(true).addModel().condition(SandbagsBlock.WEST, true).end();
        builder.part().modelFile(corner).uvLock(true).addModel().condition(SandbagsBlock.CORNER_NE, true).end();
        builder.part().modelFile(corner).rotationY(90).uvLock(true).addModel().condition(SandbagsBlock.CORNER_ES, true).end();
        builder.part().modelFile(corner).rotationY(180).uvLock(true).addModel().condition(SandbagsBlock.CORNER_SW, true).end();
        builder.part().modelFile(corner).rotationY(270).uvLock(true).addModel().condition(SandbagsBlock.CORNER_WN, true).end();
        itemModels().withExistingParent("sandbags", modLoc("block/sandbags_inventory"));
    }

    // --- military crates ---

    private void crates() {
        // {top, side, bottom} per type in meta order; vanilla planks are in the minecraft namespace
        String[][] textures = {
                {"crate_top_dark", "military_crate_side", "military_crate_bottom"},
                {"crate_top_dark", "military_crate_gun", "military_crate_bottom"},
                {"crate_top", "armor_crate_side", "minecraft:oak_planks"},
                {"crate_top_red", "medical_crate_side", "minecraft:birch_planks"},
                {"crate_top_red", "explosive_crate", "minecraft:spruce_planks"},
                {"crate_top", "minecraft:oak_planks", "minecraft:oak_planks"},
                {"crate_top_dark", "minecraft:jungle_planks", "minecraft:jungle_planks"},
                {"crate_top_blue", "minecraft:birch_planks", "minecraft:birch_planks"},
                {"crate_top_red", "minecraft:spruce_planks", "minecraft:spruce_planks"}};
        for (int type = 0; type < textures.length; type++) {
            DeferredBlock<? extends Block> block = TGBlocks.MILITARY_CRATES.get(type);
            BlockModelBuilder model = models().withExistingParent("block/" + path(block), modLoc("block/military_crate"))
                    .texture("top", crateTexture(textures[type][0])).texture("side", crateTexture(textures[type][1]))
                    .texture("bottom", crateTexture(textures[type][2])).texture("particle", crateTexture(textures[type][2]));
            simpleBlockWithItem(block.get(), model);
        }
    }

    private ResourceLocation crateTexture(String name) {
        return name.startsWith("minecraft:") ? mcLoc("block/" + name.substring("minecraft:".length())) : tex(name);
    }

    // --- machines ---

    /** Machines are drawn by their block entity renderer; the block model only gives the particle texture. */
    private void machines() {
        machine(TGBlocks.METAL_PRESS, "metal_press");
        machine(TGBlocks.AMMO_PRESS, "ammo_press");
        modelMachine(TGBlocks.BLAST_FURNACE, "blast_furnace_base", "blast_furnace_base");
        modelMachine(TGBlocks.GRINDER, "grinder_base", "grinder_inventory_base");
        machine(TGBlocks.CHEM_LAB, "chem_lab");
        chargingStation();
        acid();
        // benches: Techguns 2 cube models (front north) and block models, Fabric Ammo Bench model
        cubeMachine(TGBlocks.CAMO_BENCH, "paintmachine_front", "paintmachine_top");
        cubeMachine(TGBlocks.REPAIR_BENCH, "repairbench_front", "repairbench_top");
        modelMachine(TGBlocks.UPGRADE_BENCH, "upgrade_bench_base", "upgrade_bench_base");
        modelMachine(TGBlocks.AMMO_BENCH, "ammo_bench_base", "ammo_bench_base");
        multiblocks();
        machine(TGBlocks.TURRET, "turret_base");
        explosiveCharge(TGBlocks.EXPLOSIVE_CHARGE, "tnt_charge");
        explosiveCharge(TGBlocks.EXPLOSIVE_CHARGE_ADVANCED, "c4_charge");
        spawner(TGBlocks.MONSTER_HOLE);
        spawner(TGBlocks.MILITARY_SPAWN_POINT);
    }

    /** Techguns 2 spawner floor plates (Blockbench models with cut-out textures). */
    private void spawner(DeferredBlock<techguns.block.TGSpawnerBlock> block) {
        ModelFile model = models().withExistingParent("block/" + path(block), modLoc("block/" + path(block) + "_base")).renderType(CUTOUT);
        simpleBlock(block.get(), model);
        itemModels().withExistingParent(path(block), modLoc("block/" + path(block)));
    }

    /** Techguns 2 charge models (wall side south), turned to the wall they stick to; armed charges change texture. */
    private void explosiveCharge(DeferredBlock<techguns.block.ExplosiveChargeBlock> block, String texture) {
        ModelFile parent = new ModelFile.UncheckedModelFile(modLoc("block/" + texture + "_base"));
        ModelFile unarmed = models().getBuilder("block/" + path(block)).parent(parent).texture("0", tex(texture)).texture("particle", tex(texture));
        ModelFile armed = models().getBuilder("block/" + path(block) + "_armed").parent(parent).texture("0", tex(texture + "_armed"))
                .texture("particle", tex(texture + "_armed"));
        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction facing = state.getValue(techguns.block.ExplosiveChargeBlock.FACING);
            return net.neoforged.neoforge.client.model.generators.ConfiguredModel.builder()
                    .modelFile(state.getValue(techguns.block.ExplosiveChargeBlock.ARMED) ? armed : unarmed)
                    .rotationX(facing == Direction.DOWN ? 270 : facing == Direction.UP ? 90 : 0)
                    .rotationY(switch (facing) {
                        case NORTH -> 180;
                        case EAST -> 270;
                        case WEST -> 90;
                        default -> 0;
                    }).build();
        });
        itemModels().withExistingParent(path(block), modLoc("block/" + path(block)));
    }

    /**
     * Techguns 2 multiblock blocks: plain cubes until formed, then only a particle texture (the controller's renderer
     * draws the machine).
     */
    private void multiblocks() {
        multiblockPart(TGBlocks.FABRICATOR_HOUSING, models().cubeAll("block/fabricator_housing", tex("fabricator_housing")), tex("fabricator_housing"));
        multiblockPart(TGBlocks.FABRICATOR_GLASS, models().cubeAll("block/fabricator_glass", tex("fabricator_glass")).renderType(CUTOUT),
                tex("fabricator_glass"));
        ResourceLocation side = tex("fabricator_controller");
        ResourceLocation housing = tex("fabricator_housing");
        multiblockPart(TGBlocks.FABRICATOR_CONTROLLER, models().cube("block/fabricator_controller", housing, housing, side, side, side, side)
                .texture("particle", side), side);
        multiblockPart(TGBlocks.REACTION_CHAMBER_HOUSING, models().cubeAll("block/reaction_chamber_housing", tex("reactionchamber_housing")),
                tex("reactionchamber_housing"));
        multiblockPart(TGBlocks.REACTION_CHAMBER_GLASS, models().cubeAll("block/reaction_chamber_glass", tex("reactionchamber_glass"))
                .renderType(CUTOUT), tex("reactionchamber_glass"));
        reactionChamberController();
        oreDrill();
    }

    /**
     * Techguns 2 Ore Drill: frame, scaffold and engines switch to their connected textures when formed; rods and the
     * drill plate disappear (the controller's renderer draws the drill bit); the controller never changes.
     */
    private void oreDrill() {
        multiblockPart(TGBlocks.ORE_DRILL_FRAME, models().cubeAll("block/ore_drill_frame", tex("drill_frame")).renderType(CUTOUT),
                models().cubeAll("block/ore_drill_frame_formed", tex("drill_frame_c")).renderType(CUTOUT));
        ModelFile scaffold = models().cubeAll("block/ore_drill_scaffold", tex("drill_scaffold")).renderType(CUTOUT);
        ModelFile scaffoldFormed = models().cubeAll("block/ore_drill_scaffold_formed", tex("drill_scaffold_c")).renderType(CUTOUT);
        ModelFile scaffoldHidden = models().getBuilder("block/ore_drill_scaffold_hidden").texture("particle", tex("drill_scaffold"));
        getVariantBuilder(TGBlocks.ORE_DRILL_SCAFFOLD.get()).forAllStates(state -> net.neoforged.neoforge.client.model.generators.ConfiguredModel
                .builder().modelFile(!state.getValue(techguns.multiblock.MultiblockPartBlock.FORMED) ? scaffold
                        : state.getValue(techguns.block.OreDrillScaffoldBlock.HIDDEN) ? scaffoldHidden : scaffoldFormed).build());
        itemModels().withExistingParent(path(TGBlocks.ORE_DRILL_SCAFFOLD), modLoc("block/ore_drill_scaffold"));
        multiblockPart(TGBlocks.ORE_DRILL_ROD, models().cubeColumn("block/ore_drill_rod", tex("drill_rod"), tex("drill_rod_top")).renderType(CUTOUT),
                tex("drill_rod"));
        multiblockPart(TGBlocks.ORE_DRILL_ENGINE, models().cubeAll("block/ore_drill_engine", tex("drill_engine")),
                models().cubeColumn("block/ore_drill_engine_formed", tex("drill_engine_c"), tex("drill_engine_top_c")));
        ModelFile controller = models().cubeAll("block/ore_drill_controller", tex("drill_engine_controller"));
        multiblockPart(TGBlocks.ORE_DRILL_CONTROLLER, controller, controller);
        TGBlocks.ORE_CLUSTERS.forEach(block -> simpleBlockWithItem(block.get(), models().cubeAll("block/" + path(block), tex(path(block)))));
    }

    /**
     * Techguns 2: the formed Reaction Chamber is the controller's OBJ model of the whole chamber (flipped v), turned
     * by the structure direction stored in the facing.
     */
    private void reactionChamberController() {
        ResourceLocation side = tex("reactionchamber_controller");
        ResourceLocation housing = tex("reactionchamber_housing");
        ModelFile unformed = models().cube("block/reaction_chamber_controller", housing, housing, side, side, side, side).texture("particle", side);
        ModelFile formed = models().getBuilder("block/reaction_chamber_controller_formed").parent(new ModelFile.UncheckedModelFile(mcLoc("block/block")))
                .customLoader(net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(modLoc("models/block/reactionchamber.obj")).flipV(true).end()
                .texture("particle", housing).renderType(TRANSLUCENT);
        getVariantBuilder(TGBlocks.REACTION_CHAMBER_CONTROLLER.get()).forAllStates(state -> {
            boolean isFormed = state.getValue(techguns.multiblock.MultiblockPartBlock.FORMED);
            return net.neoforged.neoforge.client.model.generators.ConfiguredModel.builder().modelFile(isFormed ? formed : unformed)
                    .rotationY(isFormed ? (int) state.getValue(techguns.machine.TGMachineBlock.FACING).toYRot() : 0).build();
        });
        itemModels().withExistingParent(path(TGBlocks.REACTION_CHAMBER_CONTROLLER), modLoc("block/reaction_chamber_controller"));
    }

    private void multiblockPart(DeferredBlock<? extends Block> block, ModelFile unformed, ResourceLocation particle) {
        multiblockPart(block, unformed, models().getBuilder("block/" + path(block) + "_formed").texture("particle", particle));
    }

    private void multiblockPart(DeferredBlock<? extends Block> block, ModelFile unformed, ModelFile formed) {
        // the controller's facing does not change its look (Techguns 2 had none)
        getVariantBuilder(block.get()).forAllStates(state -> net.neoforged.neoforge.client.model.generators.ConfiguredModel.builder()
                .modelFile(state.getValue(techguns.multiblock.MultiblockPartBlock.FORMED) ? formed : unformed).build());
        itemModels().withExistingParent(path(block), modLoc("block/" + path(block)));
    }

    /** Techguns 2 {@code SimpleMachine} cube: own front and top, the paint machine side everywhere else. */
    private void cubeMachine(DeferredBlock<? extends Block> block, String front, String top) {
        ResourceLocation side = tex("paintmachine_side");
        ModelFile model = models().cube("block/" + path(block), side, tex(top), tex(front), side, side, side).texture("particle", side);
        horizontalBlock(block.get(), model, 180);
        itemModels().withExistingParent(path(block), modLoc("block/" + path(block)));
    }

    /** Techguns 2 OBJ model (flipped v like the 1.12 blockstate), turned by facing; item uses the vanilla block display. */
    private void chargingStation() {
        ModelFile model = models().getBuilder("block/charging_station").parent(new ModelFile.UncheckedModelFile(mcLoc("block/block")))
                .customLoader(net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(modLoc("models/block/charging_station_centered.obj")).flipV(true).end()
                .texture("particle", tex("charging_station")).renderType(CUTOUT);
        horizontalBlock(TGBlocks.CHARGING_STATION.get(), model, 180);
        itemModels().withExistingParent("charging_station", modLoc("block/charging_station"));
    }

    /** Creeper acid: the fluid renderer draws it; the block model only gives the particle texture. */
    private void acid() {
        ModelFile model = models().getBuilder("block/creeper_acid").texture("particle", tex("acid_still"));
        getVariantBuilder(techguns.registry.TGFluids.ACID_BLOCK.get()).partialState().setModels(
                new net.neoforged.neoforge.client.model.generators.ConfiguredModel(model));
        itemModels().withExistingParent("creeper_acid_bucket", TGIds.of("neoforge", "item/bucket"))
                .customLoader(net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder::begin)
                .fluid(techguns.registry.TGFluids.ACID.get()).end();
    }

    /**
     * Machines with a Techguns 2 block model (copied as parent): turned by facing like the 1.12 blockstate (model front
     * is north); the item uses its own parent model.
     */
    private void modelMachine(DeferredBlock<? extends Block> block, String parent, String itemParent) {
        ModelFile model = models().withExistingParent("block/" + path(block), modLoc("block/" + parent)).renderType(CUTOUT);
        horizontalBlock(block.get(), model, 180);
        itemModels().withExistingParent(path(block), modLoc("block/" + itemParent));
    }

    private void machine(DeferredBlock<? extends Block> block, String particle) {
        ModelFile model = models().getBuilder("block/" + path(block)).texture("particle", tex(particle));
        getVariantBuilder(block.get()).partialState().setModels(new net.neoforged.neoforge.client.model.generators.ConfiguredModel(model));
        // Techguns 2 machine item display (builtin/entity drawn by the machine item renderer)
        itemModels().getBuilder(path(block)).parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .transforms()
                .transform(net.minecraft.world.item.ItemDisplayContext.GUI).rotation(30, 45, 0).scale(0.625f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.25f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.HEAD).rotation(0, 180, 0).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.FIXED).rotation(0, 180, 0).scale(0.5f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 315, 0).translation(0, 2.5f, 0).scale(0.375f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(75, 315, 0).translation(0, 2.5f, 0).scale(0.375f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 315, 0).scale(0.4f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 315, 0).scale(0.4f).end()
                .end();
    }

    // --- doors ---

    private void bunkerDoor() {
        doorBlockWithRenderType(TGBlocks.BUNKER_DOOR.get(), tex("bunker_door_lower"), tex("bunker_door_upper"), CUTOUT);
        itemModels().withExistingParent("bunker_door", mcLoc("item/generated")).texture("layer0", modLoc("item/item_bunkerdoor"));
    }

    private void doors3x3() {
        // closed, open and frame model, particle texture and item texture per kind; the hangar open models are .obj files
        door3x3(TGBlocks.DOOR_METAL_3X3, "techdoor3x3_full", "techdoor3x3_opened", "techdoor3x3_frame", "techdoor3x3", "item_door3x3", true);
        door3x3(TGBlocks.DOOR_HANGAR_UP_3X3, "hangar_door_closed", "hangar_door_opened", "hangar_door_frame", "hangar_door", "hangar_door_up", false);
        door3x3(TGBlocks.DOOR_HANGAR_DOWN_3X3, "hangar_door_down_closed", "hangar_door_down_opened", "hangar_door_down_frame", "hangar_door",
                "hangar_door_down", false);
        door3x3(TGBlocks.DOOR_NETHER_3X3, "techdoor3x3_full_nether", "techdoor3x3_opened_nether", "techdoor3x3_frame_nether", "techdoor3x3_nether",
                "item_door3x3_nether", true);
    }

    private void door3x3(DeferredBlock<Door3x3Block> block, String closedParent, String openParent, String frameParent, String particle,
                         String itemTexture, boolean jsonOpenModel) {
        String name = path(block);
        ModelFile closed = models().withExistingParent("block/" + name + "_closed", modLoc("block/" + closedParent)).renderType(CUTOUT);
        // the .obj wrappers carry their own render type (a child of a custom loader model would not)
        ModelFile open = jsonOpenModel
                ? models().withExistingParent("block/" + name + "_open", modLoc("block/" + openParent)).renderType(CUTOUT)
                : existing(openParent);
        // while the door moves the master shows only the frame, the renderer draws the moving parts
        ModelFile frame = models().withExistingParent("block/" + name + "_frame", modLoc("block/" + frameParent)).renderType(CUTOUT)
                .texture("particle", tex(particle));
        // the eight other blocks of the door draw nothing, only the particle texture
        ModelFile part = models().getBuilder("block/" + name + "_part").texture("particle", tex(particle));
        getVariantBuilder(block.get()).forAllStatesExcept(state -> {
            if (!Door3x3Block.isMaster(state)) {
                return ConfiguredModel.builder().modelFile(part).build();
            }
            ModelFile model = state.getValue(Door3x3Block.MOVING) ? frame : state.getValue(Door3x3Block.OPEN) ? open : closed;
            return ConfiguredModel.builder().modelFile(model)
                    .rotationY(state.getValue(Door3x3Block.ZPLANE) ? 90 : 0).build();
        }, Door3x3Block.POWERED);
        itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/" + itemTexture));
    }
}
