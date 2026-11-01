package techguns.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.minecraft.world.level.material.Fluids;
import techguns.Techguns;
import techguns.block.OreDrillScaffoldBlock;
import techguns.machine.OreDrillBlockEntity;
import techguns.machine.OreDrillSchematic;
import techguns.machine.TGMachineBlockEntity;
import techguns.multiblock.MultiblockPart;
import techguns.multiblock.MultiblockPartBlock;
import techguns.recipe.OreClusterRecipe;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;
import techguns.registry.TGRecipes;

/**
 * Ore Drill (Phase 5c3): drill shapes, size rules, mining an ore cluster, the cobblestone fallback, fuel and the
 * cluster definitions.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGOreDrillTests {
    private static final String ARENA = "empty_8x10x8";
    /** The drills point down from here. */
    private static final BlockPos CONTROLLER = new BlockPos(3, 8, 3);

    private static Player player(GameTestHelper helper) {
        return helper.makeMockPlayer(GameType.SURVIVAL);
    }

    /** Controller, one rod, the cluster below. */
    private static OreDrillBlockEntity miniDrill(GameTestHelper helper, Block cluster) {
        helper.setBlock(CONTROLLER, TGBlocks.ORE_DRILL_CONTROLLER.get());
        helper.setBlock(CONTROLLER.below(), TGBlocks.ORE_DRILL_ROD.get());
        helper.setBlock(CONTROLLER.below(2), cluster);
        return (OreDrillBlockEntity) helper.getBlockEntity(CONTROLLER);
    }

    /**
     * A 3x3 drill (radius 1): two 3x3 engine layers, {@code rods} rods, then {@code clusters} coal clusters (one under
     * the rods, the others around it). With {@code complete} the frame and scaffold are added by the schematic.
     */
    private static OreDrillBlockEntity drill3x3(GameTestHelper helper, int rods, int clusters, boolean complete) {
        helper.setBlock(CONTROLLER, TGBlocks.ORE_DRILL_CONTROLLER.get());
        for (int h = 1; h <= 2; h++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    helper.setBlock(CONTROLLER.below(h).offset(x, 0, z), TGBlocks.ORE_DRILL_ENGINE.get());
                }
            }
        }
        for (int h = 3; h < 3 + rods; h++) {
            helper.setBlock(CONTROLLER.below(h), TGBlocks.ORE_DRILL_ROD.get());
        }
        BlockPos cluster = CONTROLLER.below(3 + rods);
        BlockPos[] around = {cluster, cluster.west(), cluster.east(), cluster.north(), cluster.south()};
        for (int i = 0; i < clusters; i++) {
            helper.setBlock(around[i], TGBlocks.ORE_CLUSTER_COAL.get());
        }
        if (complete) {
            for (MultiblockPart part : OreDrillSchematic.INSTANCE.parts(helper.getLevel(), helper.absolutePos(CONTROLLER), Direction.UP)) {
                if (!part.isAir() && helper.getLevel().isEmptyBlock(part.pos())) {
                    helper.getLevel().setBlockAndUpdate(part.pos(), part.block().defaultBlockState());
                }
            }
        }
        return (OreDrillBlockEntity) helper.getBlockEntity(CONTROLLER);
    }

    @GameTest(template = ARENA)
    public static void miniDrillForms(GameTestHelper helper) {
        OreDrillBlockEntity drill = miniDrill(helper, TGBlocks.ORE_CLUSTER_COAL.get());
        helper.assertTrue(drill.tryForm(player(helper), Direction.NORTH), "Controller, rod and cluster form a mini drill");
        helper.assertTrue(drill.getDrillDirection() == Direction.DOWN && drill.getEngines() == 0 && drill.getRods() == 1,
                "A mini drill points at the cluster with one rod");
        helper.assertTrue(drill.drillRadius() == 0, "Drill radius 0");
        helper.assertTrue(helper.getBlockState(CONTROLLER.below()).getValue(MultiblockPartBlock.FORMED), "The rod is formed");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void drillNeedsCluster(GameTestHelper helper) {
        OreDrillBlockEntity drill = miniDrill(helper, Blocks.STONE);
        helper.assertFalse(drill.tryForm(player(helper), Direction.NORTH), "Without a cluster at the end the drill doesn't form");
        helper.setBlock(CONTROLLER.below(), Blocks.AIR);
        helper.assertFalse(drill.tryForm(player(helper), Direction.NORTH), "A controller alone doesn't form");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void largeDrillFormsWithFrame(GameTestHelper helper) {
        OreDrillBlockEntity drill = drill3x3(helper, 3, 1, true);
        helper.assertTrue(drill.tryForm(player(helper), Direction.UP), "A complete 3x3 drill forms");
        helper.assertTrue(drill.getEngines() == 2 && drill.getRods() == 3 && drill.getRadius() == 1 && drill.drillRadius() == 2,
                "Two engine layers, three rods, radius 1");
        int frames = 0;
        int scaffolds = 0;
        int plates = 0;
        for (BlockPos pos : drill.getLinkedParts()) {
            BlockState state = helper.getLevel().getBlockState(pos);
            helper.assertTrue(state.getValue(MultiblockPartBlock.FORMED), "Part " + pos + " is formed");
            if (state.is(TGBlocks.ORE_DRILL_FRAME.get())) {
                frames++;
            } else if (state.is(TGBlocks.ORE_DRILL_SCAFFOLD.get())) {
                if (state.getValue(OreDrillScaffoldBlock.HIDDEN)) {
                    plates++;
                } else {
                    scaffolds++;
                }
            }
        }
        // Techguns 2: a 5x5x5 box, frame on its 12 edges, scaffold on the 4 side faces, the drill plate around the last rod
        helper.assertTrue(frames == 44 && scaffolds == 36 && plates == 8, "Frame 44, scaffold 36, plate 8; got " + frames + "/" + scaffolds + "/" + plates);
        helper.assertTrue(helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(CONTROLLER.below().east(2)), null) != null,
                "The frame takes fuel");
        BlockState scaffold = helper.getBlockState(CONTROLLER.below(3).east(2));
        helper.assertTrue(scaffold.skipRendering(scaffold, Direction.UP), "Formed scaffold hides faces towards formed scaffold");
        helper.assertFalse(scaffold.skipRendering(TGBlocks.ORE_DRILL_FRAME.get().defaultBlockState().setValue(MultiblockPartBlock.FORMED, true),
                Direction.UP), "Formed scaffold shows faces towards the frame");
        helper.assertFalse(scaffold.skipRendering(scaffold.setValue(OreDrillScaffoldBlock.HIDDEN, true), Direction.UP),
                "Formed scaffold shows faces towards the drill plate");
        BlockState loose = TGBlocks.ORE_DRILL_SCAFFOLD.get().defaultBlockState();
        helper.assertFalse(loose.skipRendering(loose, Direction.UP), "Unformed scaffold shows all faces");
        BlockPos scaffoldPos = helper.absolutePos(CONTROLLER.below(3).east(2));
        helper.assertTrue(scaffold.getShadeBrightness(helper.getLevel(), scaffoldPos) == 1.0f
                && scaffold.propagatesSkylightDown(helper.getLevel(), scaffoldPos), "Scaffold is lit and shaded like glass");

        helper.setBlock(CONTROLLER.below(3).east(2), Blocks.AIR);
        helper.assertFalse(drill.isFormed(), "Breaking the scaffold unforms the drill");
        helper.assertFalse(helper.getBlockState(CONTROLLER.below()).getValue(MultiblockPartBlock.FORMED), "The engines are unformed again");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void drillNeedsFrameAndRodLength(GameTestHelper helper) {
        OreDrillBlockEntity drill = drill3x3(helper, 3, 1, false);
        helper.assertFalse(drill.tryForm(player(helper), Direction.UP), "Without frame and scaffold the drill doesn't form");
        helper.assertFalse(helper.getBlockState(CONTROLLER.below()).getValue(MultiblockPartBlock.FORMED), "Nothing is linked");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void drillRejectsShortRods(GameTestHelper helper) {
        // radius 1 needs at least 3 rods (the drill diameter)
        OreDrillBlockEntity drill = drill3x3(helper, 2, 1, false);
        helper.assertTrue(OreDrillSchematic.layout(helper.getLevel(), helper.absolutePos(CONTROLLER)).isEmpty(), "Two rods are too short");
        helper.assertFalse(drill.tryForm(player(helper), Direction.UP), "and don't form");
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void largeDrillMinesCoalCluster(GameTestHelper helper) {
        OreDrillBlockEntity drill = drill3x3(helper, 3, 5, true);
        drill.tryForm(player(helper), Direction.UP);
        drill.getEnergy().setEnergy(OreDrillBlockEntity.ENERGY_CAPACITY);
        drill.getInventory().setStackInSlot(OreDrillBlockEntity.SLOT_DRILL, new ItemStack(TGItems.ORE_DRILL_HEAD_MEDIUM_CARBON.get()));
        helper.runAfterDelay(2, () -> {
            // carbon 3 + drill radius 2 - coal 0 = 5; cluster 5 (up to the drill length 5): (15 + 12.5) * 10 = 275 ores/h
            int total = drill.getData().get(TGMachineBlockEntity.DATA_TOTAL_TIME);
            helper.assertTrue(total == (int) (60.0 / 275.0 * 1200.0), "275 ores per hour, got time " + total);
            int power = drill.getData().get(OreDrillBlockEntity.DATA_POWER_LOW);
            helper.assertTrue(power == (int) (8 * 275.0 * 1.2 * 0.1f), "8 FE per ore/h, radius bonus 1.2, coal 0.1; got " + power);
        });
        helper.runAfterDelay(270, () -> {
            ItemStack out = drill.getInventory().getStackInSlot(OreDrillBlockEntity.SLOT_OUTPUT_FIRST);
            helper.assertTrue(out.is(Items.COAL_ORE) || out.is(Items.DIAMOND), "The coal cluster gives coal ore (or a diamond), got " + out);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 500)
    public static void weakHeadMinesCobblestone(GameTestHelper helper) {
        OreDrillBlockEntity drill = miniDrill(helper, TGBlocks.ORE_CLUSTER_URANIUM.get());
        drill.tryForm(player(helper), Direction.NORTH);
        drill.getEnergy().setEnergy(OreDrillBlockEntity.ENERGY_CAPACITY);
        drill.getInventory().setStackInSlot(OreDrillBlockEntity.SLOT_DRILL, new ItemStack(TGItems.ORE_DRILL_HEAD_SMALL_STEEL.get()));
        helper.runAfterDelay(410, () -> {
            helper.assertTrue(drill.getInventory().getStackInSlot(OreDrillBlockEntity.SLOT_OUTPUT_FIRST).is(Items.COBBLESTONE),
                    "Steel (1) on uranium (3) gives cobblestone");
            int used = OreDrillBlockEntity.ENERGY_CAPACITY - drill.getEnergy().getEnergyStored();
            helper.assertTrue(used >= 400 * 24, "400 ticks at 24 FE, used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA)
    public static void wrongHeadSizeDoesNothing(GameTestHelper helper) {
        OreDrillBlockEntity drill = miniDrill(helper, TGBlocks.ORE_CLUSTER_COAL.get());
        drill.tryForm(player(helper), Direction.NORTH);
        drill.getEnergy().setEnergy(OreDrillBlockEntity.ENERGY_CAPACITY);
        drill.getInventory().setStackInSlot(OreDrillBlockEntity.SLOT_DRILL, new ItemStack(TGItems.ORE_DRILL_HEAD_MEDIUM_STEEL.get()));
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(drill.headMiningLevel() == -1, "A medium head doesn't fit a mini drill");
            helper.assertFalse(drill.isWorking(), "and the drill doesn't run");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA)
    public static void drillBurnsFuel(GameTestHelper helper) {
        OreDrillBlockEntity drill = miniDrill(helper, TGBlocks.ORE_CLUSTER_URANIUM.get());
        drill.tryForm(player(helper), Direction.NORTH);
        IItemHandlerModifiable inv = drill.getInventory();
        inv.setStackInSlot(OreDrillBlockEntity.SLOT_DRILL, new ItemStack(TGItems.ORE_DRILL_HEAD_SMALL_STEEL.get()));
        helper.assertTrue(inv.isItemValid(OreDrillBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL)), "Coal is furnace fuel");
        helper.assertFalse(inv.isItemValid(OreDrillBlockEntity.SLOT_FUEL, new ItemStack(Items.IRON_INGOT)), "Iron isn't");
        inv.setStackInSlot(OreDrillBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL));
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(inv.getStackInSlot(OreDrillBlockEntity.SLOT_FUEL).isEmpty(), "The coal was burnt");
            helper.assertTrue(drill.getFuelBufferMax() == 1600 && drill.getFuelBuffer() > 1550 && drill.getFuelBuffer() < 1600,
                    "Coal fills 1600 fuel units, one used per tick; got " + drill.getFuelBuffer());
            helper.assertTrue(drill.getEnergy().getEnergyStored() == 0 && drill.isWorking(), "The drill runs without FE");
            IFluidHandler tanks = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(CONTROLLER), null);
            helper.assertTrue(tanks != null && tanks.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE) == 1000,
                    "Lava is liquid fuel");
            helper.assertTrue(tanks.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.SIMULATE) == 0, "Water isn't");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA)
    public static void drillUnformsWithoutCluster(GameTestHelper helper) {
        OreDrillBlockEntity drill = miniDrill(helper, TGBlocks.ORE_CLUSTER_COAL.get());
        drill.tryForm(player(helper), Direction.NORTH);
        helper.setBlock(CONTROLLER.below(2), Blocks.STONE);
        helper.runAfterDelay(2, () -> {
            helper.assertFalse(drill.isFormed(), "The drill breaks apart when the cluster is gone");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA)
    public static void everyClusterHasADefinition(GameTestHelper helper) {
        List<OreClusterRecipe> recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(TGRecipes.ORE_CLUSTER.get()).stream()
                .map(RecipeHolder::value).toList();
        for (var cluster : TGBlocks.ORE_CLUSTERS) {
            helper.assertTrue(recipes.stream().filter(r -> r.cluster() == cluster.get()).count() == 1, cluster.getId() + " has one definition");
        }
        OreClusterRecipe common = recipes.stream().filter(r -> r.cluster() == TGBlocks.ORE_CLUSTER_COMMON_METAL.get()).findFirst().orElseThrow();
        helper.assertTrue(common.miningLevel() == 0 && common.oreMultiplier() == 5.0f, "Common metal: mining level 0, 5x ores");
        helper.assertTrue(common.entries().stream().map(OreClusterRecipe.Entry::itemOutput).anyMatch(s -> s.is(TGBlocks.TIN_ORE.get().asItem())),
                "Common metal gives tin ore");
        helper.assertTrue(TGBlocks.ORE_CLUSTER_COAL.get().defaultDestroyTime() < 0, "Clusters are unbreakable");
        OreClusterRecipe gems = recipes.stream().filter(r -> r.cluster() == TGBlocks.ORE_CLUSTER_COMMON_GEM.get()).findFirst().orElseThrow();
        helper.assertTrue(gems.entries().stream().noneMatch(e -> e.itemOutput().is(net.minecraft.world.item.Items.BARRIER)),
                "An empty optional tag (certus quartz without its mod) gives nothing, not a barrier");
        helper.succeed();
    }
}
