package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import techguns.machine.ReactionChamberBlockEntity;
import techguns.machine.TGMachineBlock;
import techguns.registry.TGFluids;
import techguns.Techguns;
import techguns.machine.FabricatorBlockEntity;
import techguns.multiblock.MultiblockPartBlock;
import techguns.multiblock.MultiblockPartBlockEntity;
import techguns.registry.TGAmmo;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;

/**
 * Multiblocks (Phase 5c): forming and unforming, part links and capabilities, the Fabricator working only when formed.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGMultiblockTests {
    private static final String ARENA = "empty_8x4x8";
    /** Controller; formed from its south side, the housings stand north and west of it. */
    private static final BlockPos CONTROLLER = new BlockPos(3, 2, 3);
    private static final Direction SIDE = Direction.SOUTH;

    private static BlockPos[] housings() {
        BlockPos behind = CONTROLLER.north();
        return new BlockPos[] {behind, CONTROLLER.west(), behind.west()};
    }

    /** Builds the Fabricator; {@code skipGlass} leaves one glass block out. */
    private static FabricatorBlockEntity buildFabricator(GameTestHelper helper, boolean skipGlass) {
        helper.setBlock(CONTROLLER, TGBlocks.FABRICATOR_CONTROLLER.get());
        if (!skipGlass) {
            helper.setBlock(CONTROLLER.above(), TGBlocks.FABRICATOR_GLASS.get());
        }
        for (BlockPos pos : housings()) {
            helper.setBlock(pos, TGBlocks.FABRICATOR_HOUSING.get());
            helper.setBlock(pos.above(), TGBlocks.FABRICATOR_GLASS.get());
        }
        return (FabricatorBlockEntity) helper.getBlockEntity(CONTROLLER);
    }

    private static Player player(GameTestHelper helper) {
        return helper.makeMockPlayer(GameType.SURVIVAL);
    }

    @GameTest(template = ARENA)
    public static void fabricatorFormsAndUnforms(GameTestHelper helper) {
        FabricatorBlockEntity fabricator = buildFabricator(helper, false);
        helper.assertFalse(fabricator.tryForm(player(helper), Direction.UP), "The Fabricator can't be formed from the top");
        helper.assertTrue(fabricator.tryForm(player(helper), SIDE), "A complete Fabricator should form");
        helper.assertTrue(fabricator.isFormed() && fabricator.getDirection() == SIDE, "Formed from the south side");
        helper.assertTrue(helper.getBlockState(CONTROLLER).getValue(MultiblockPartBlock.FORMED), "The controller shows as formed");
        for (BlockPos pos : housings()) {
            helper.assertTrue(helper.getBlockState(pos).getValue(MultiblockPartBlock.FORMED), "Housing " + pos + " is formed");
            MultiblockPartBlockEntity part = (MultiblockPartBlockEntity) helper.getBlockEntity(pos);
            helper.assertTrue(helper.absolutePos(CONTROLLER).equals(part.getMasterPos()), "Housing " + pos + " links the controller");
        }
        IItemHandler housingItems = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(housings()[0]), null);
        IItemHandler glassItems = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(CONTROLLER.above()), null);
        helper.assertTrue(housingItems != null && housingItems.getSlots() == 6, "Housings expose the Fabricator inventory");
        helper.assertTrue(glassItems == null, "Glass exposes nothing");
        helper.assertTrue(helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(housings()[1]), null) != null,
                "Housings take power");

        helper.setBlock(housings()[2].above(), Blocks.AIR);
        helper.assertFalse(fabricator.isFormed(), "Breaking a part unforms the Fabricator");
        for (BlockPos pos : housings()) {
            helper.assertFalse(helper.getBlockState(pos).getValue(MultiblockPartBlock.FORMED), "Housing " + pos + " is unformed again");
        }
        helper.assertFalse(helper.getBlockState(CONTROLLER).getValue(MultiblockPartBlock.FORMED), "The controller is unformed again");
        helper.assertTrue(helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(housings()[0]), null) == null,
                "Unlinked housings expose nothing");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void fabricatorNeedsAllParts(GameTestHelper helper) {
        FabricatorBlockEntity fabricator = buildFabricator(helper, true);
        helper.assertFalse(fabricator.tryForm(player(helper), SIDE), "A missing glass block stops forming");
        helper.assertFalse(helper.getBlockState(housings()[0]).getValue(MultiblockPartBlock.FORMED), "Nothing is linked");
        helper.succeed();
    }

    private static void loadEnergyCellRecipe(FabricatorBlockEntity fabricator) {
        fabricator.getEnergy().setEnergy(FabricatorBlockEntity.ENERGY_CAPACITY);
        IItemHandlerModifiable inv = fabricator.getInventory();
        inv.setStackInSlot(FabricatorBlockEntity.SLOT_INPUT, new ItemStack(Items.GOLD_INGOT));
        inv.setStackInSlot(FabricatorBlockEntity.SLOT_WIRES, new ItemStack(TGItems.COPPER_WIRE.get()));
        inv.setStackInSlot(FabricatorBlockEntity.SLOT_POWDER, new ItemStack(Items.REDSTONE, 3));
        inv.setStackInSlot(FabricatorBlockEntity.SLOT_PLATE, new ItemStack(TGItems.PLASTIC_SHEET.get()));
    }

    /** Every Fabricator recipe's ingredients fit the slot the recipe puts them in (slots follow the item tags). */
    @GameTest(template = ARENA)
    public static void fabricatorRecipesFitTheirSlots(GameTestHelper helper) {
        IItemHandlerModifiable inv = buildFabricator(helper, false).getInventory();
        for (var holder : helper.getLevel().getRecipeManager().getAllRecipesFor(techguns.registry.TGRecipes.FABRICATOR.get())) {
            for (int slot = FabricatorBlockEntity.SLOT_INPUT; slot <= FabricatorBlockEntity.SLOT_PLATE; slot++) {
                for (ItemStack stack : holder.value().slot(slot).ingredient().getItems()) {
                    helper.assertTrue(inv.isItemValid(slot, stack), holder.id() + ": " + stack.getItem() + " does not fit slot " + slot);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void fabricatorMakesEnergyCell(GameTestHelper helper) {
        FabricatorBlockEntity fabricator = buildFabricator(helper, false);
        fabricator.tryForm(player(helper), SIDE);
        loadEnergyCellRecipe(fabricator);
        IItemHandlerModifiable inv = fabricator.getInventory();
        helper.assertTrue(inv.isItemValid(FabricatorBlockEntity.SLOT_WIRES, new ItemStack(TGItems.GOLD_WIRE.get())), "Wires go into the wire slot");
        helper.assertFalse(inv.isItemValid(FabricatorBlockEntity.SLOT_INPUT, new ItemStack(Items.REDSTONE)), "Redstone is a powder, not an input");

        helper.runAfterDelay(FabricatorBlockEntity.TIME + 5, () -> {
            helper.assertTrue(inv.getStackInSlot(FabricatorBlockEntity.SLOT_OUTPUT).is(TGAmmo.ENERGY_CELL_EMPTY.get()),
                    "Gold, copper wire, redstone and plastic make an empty energy cell");
            helper.assertTrue(inv.getStackInSlot(FabricatorBlockEntity.SLOT_POWDER).isEmpty(), "The recipe takes 3 redstone");
            int used = FabricatorBlockEntity.ENERGY_CAPACITY - fabricator.getEnergy().getEnergyStored();
            helper.assertTrue(used == FabricatorBlockEntity.TIME * FabricatorBlockEntity.POWER_PER_TICK, "100 ticks at 80 FE, used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void unformedFabricatorDoesNothing(GameTestHelper helper) {
        FabricatorBlockEntity fabricator = buildFabricator(helper, false);
        loadEnergyCellRecipe(fabricator);
        helper.runAfterDelay(FabricatorBlockEntity.TIME + 5, () -> {
            helper.assertTrue(fabricator.getInventory().getStackInSlot(FabricatorBlockEntity.SLOT_OUTPUT).isEmpty(), "An unformed Fabricator doesn't work");
            helper.assertTrue(fabricator.getEnergy().getEnergyStored() == FabricatorBlockEntity.ENERGY_CAPACITY, "and uses no power");
            helper.succeed();
        });
    }

    // --- Reaction Chamber ---

    /** Controller at the front; formed from its north side the chamber extends south (+z). */
    private static final BlockPos RC_CONTROLLER = new BlockPos(2, 0, 1);
    private static final Direction RC_SIDE = Direction.NORTH;

    private static ReactionChamberBlockEntity buildReactionChamber(GameTestHelper helper) {
        helper.setBlock(RC_CONTROLLER, TGBlocks.REACTION_CHAMBER_CONTROLLER.get());
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                BlockPos column = new BlockPos(x, 0, z);
                if (!column.equals(RC_CONTROLLER)) {
                    helper.setBlock(column, TGBlocks.REACTION_CHAMBER_HOUSING.get());
                }
                helper.setBlock(column.above(1), TGBlocks.REACTION_CHAMBER_GLASS.get());
                helper.setBlock(column.above(2), TGBlocks.REACTION_CHAMBER_GLASS.get());
                helper.setBlock(column.above(3), TGBlocks.REACTION_CHAMBER_HOUSING.get());
            }
        }
        ReactionChamberBlockEntity chamber = (ReactionChamberBlockEntity) helper.getBlockEntity(RC_CONTROLLER);
        helper.assertTrue(chamber.tryForm(player(helper), RC_SIDE), "A complete Reaction Chamber should form");
        return chamber;
    }

    /** Titanium reaction: titanium ore, heat ray, 3 buckets of acid, intensity 5, one reaction tick, no instability. */
    private static void loadTitanium(ReactionChamberBlockEntity chamber, int intensity) {
        chamber.getEnergy().setEnergy(ReactionChamberBlockEntity.ENERGY_CAPACITY);
        chamber.setLiquidLevel(3);
        chamber.setIntensity(intensity);
        chamber.getTank().fill(new FluidStack(TGFluids.ACID.get(), 3000), IFluidHandler.FluidAction.EXECUTE);
        chamber.getInventory().setStackInSlot(ReactionChamberBlockEntity.SLOT_FOCUS, new ItemStack(TGItems.HEAT_RAY_FOCUS.get()));
        chamber.getInventory().setStackInSlot(ReactionChamberBlockEntity.SLOT_INPUT, new ItemStack(TGBlocks.TITANIUM_ORE.get()));
    }

    @GameTest(template = ARENA)
    public static void reactionChamberFormsWithConnectors(GameTestHelper helper) {
        ReactionChamberBlockEntity chamber = buildReactionChamber(helper);
        helper.assertTrue(chamber.chamberDirection() == Direction.SOUTH, "The chamber lies behind the used side");
        var level = helper.getLevel();
        BlockPos sideConnector = helper.absolutePos(new BlockPos(2, 0, 3));
        BlockPos energyConnector = helper.absolutePos(new BlockPos(2, 3, 2));
        BlockPos plainHousing = helper.absolutePos(new BlockPos(1, 0, 1));
        helper.assertTrue(level.getCapability(Capabilities.FluidHandler.BLOCK, sideConnector, null) != null, "Side connectors take fluids");
        helper.assertTrue(level.getCapability(Capabilities.ItemHandler.BLOCK, sideConnector, null) != null, "Side connectors take items");
        helper.assertTrue(level.getCapability(Capabilities.EnergyStorage.BLOCK, energyConnector, null) != null, "The top connector takes power");
        helper.assertTrue(level.getCapability(Capabilities.EnergyStorage.BLOCK, sideConnector, null) == null, "Side connectors take no power");
        helper.assertTrue(level.getCapability(Capabilities.ItemHandler.BLOCK, plainHousing, null) == null, "Plain housings expose nothing");
        helper.assertTrue(helper.getBlockState(RC_CONTROLLER).getValue(TGMachineBlock.FACING) == Direction.SOUTH,
                "The formed controller faces into the chamber (turns its model)");
        helper.succeed();
    }

    /** Formed parts keep a full collision box; only the outline follows the slimmer Techguns 2 part boxes. */
    @GameTest(template = ARENA)
    public static void formedReactionChamberPartsAreSolid(GameTestHelper helper) {
        ReactionChamberBlockEntity chamber = buildReactionChamber(helper);
        var level = helper.getLevel();
        for (BlockPos pos : chamber.getLinkedParts()) {
            BlockState state = level.getBlockState(pos);
            helper.assertTrue(state.getValue(MultiblockPartBlock.FORMED), "Part at " + pos + " is formed");
            helper.assertTrue(Block.isShapeFullBlock(state.getCollisionShape(level, pos, CollisionContext.empty())),
                    "Formed part at " + pos + " collides as a full block");
        }
        // the glass edge left of the centre is slimmer in outline (Techguns 2 getBBforSlave)
        BlockPos edgeGlass = helper.absolutePos(new BlockPos(1, 1, 2));
        helper.assertFalse(Block.isShapeFullBlock(level.getBlockState(edgeGlass).getShape(level, edgeGlass)), "The outline keeps the part box");
        helper.succeed();
    }

    /** Multiblock glass is lit like vanilla glass: no neighbour AO, sky light passes, inner faces between glass hidden. */
    @GameTest(template = ARENA)
    public static void multiblockGlassActsLikeGlass(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        var level = helper.getLevel();
        for (Block glass : new Block[] {TGBlocks.FABRICATOR_GLASS.get(), TGBlocks.REACTION_CHAMBER_GLASS.get()}) {
            helper.setBlock(pos, glass);
            BlockPos abs = helper.absolutePos(pos);
            BlockState state = level.getBlockState(abs);
            helper.assertTrue(state.getShadeBrightness(level, abs) == 1.0f, glass + " casts no ambient occlusion");
            helper.assertTrue(state.propagatesSkylightDown(level, abs), glass + " lets sky light through");
            helper.assertTrue(state.getLightBlock(level, abs) == 0, glass + " blocks no light");
            helper.assertTrue(state.skipRendering(state, Direction.UP), glass + " hides faces towards the same glass");
            helper.assertFalse(state.skipRendering(state.setValue(MultiblockPartBlock.FORMED, true), Direction.UP),
                    glass + " shows faces towards a formed glass block (it draws no model)");
            helper.assertFalse(state.skipRendering(Blocks.GLASS.defaultBlockState(), Direction.UP), glass + " shows faces towards other glass");
        }
        BlockState housing = TGBlocks.REACTION_CHAMBER_HOUSING.get().defaultBlockState();
        helper.assertTrue(housing.getShadeBrightness(level, helper.absolutePos(pos)) < 1.0f, "Housings still shade like solid blocks");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void reactionChamberTankFollowsLiquidLevel(GameTestHelper helper) {
        ReactionChamberBlockEntity chamber = buildReactionChamber(helper);
        var tank = chamber.getTank();
        helper.assertTrue(tank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE) == 0, "Liquid level 0 takes nothing");
        chamber.setLiquidLevel(2);
        helper.assertTrue(tank.fill(new FluidStack(Fluids.WATER, 5000), IFluidHandler.FluidAction.EXECUTE) == 2000, "Level 2 takes two buckets");
        helper.assertTrue(tank.drain(1000, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "Nothing drains down to the level");
        chamber.setLiquidLevel(1);
        helper.assertTrue(tank.drain(5000, IFluidHandler.FluidAction.EXECUTE).getAmount() == 1000, "Above the level drains");
        helper.succeed();
    }

    /** Buckets used on any formed part fill the tank up to the liquid level and take out only what is above it. */
    @GameTest(template = ARENA)
    public static void reactionChamberTakesBucketsOnParts(GameTestHelper helper) {
        ReactionChamberBlockEntity chamber = buildReactionChamber(helper);
        chamber.setLiquidLevel(2);
        var level = helper.getLevel();
        BlockPos glass = helper.absolutePos(new BlockPos(2, 1, 2));
        Player player = player(helper);
        java.util.function.Supplier<ItemStack> use = () -> {
            level.getBlockState(glass).useItemOn(player.getMainHandItem(), level, player, net.minecraft.world.InteractionHand.MAIN_HAND,
                    new net.minecraft.world.phys.BlockHitResult(glass.getCenter(), Direction.UP, glass, false));
            return player.getMainHandItem();
        };
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        helper.assertTrue(use.get().is(Items.BUCKET) && chamber.getTank().getFluidAmount() == 1000, "Below the level a water bucket fills in");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        helper.assertTrue(use.get().is(Items.BUCKET) && chamber.getTank().getFluidAmount() == 1000, "Below the level an empty bucket takes nothing");
        chamber.setLiquidLevel(0);
        helper.assertTrue(use.get().is(Items.WATER_BUCKET) && chamber.getTank().isEmpty(), "Above the level an empty bucket takes out");
        helper.succeed();
    }

    /** The dump button removes only the liquid above the level. */
    @GameTest(template = ARENA)
    public static void reactionChamberDumpsExcessLiquid(GameTestHelper helper) {
        ReactionChamberBlockEntity chamber = buildReactionChamber(helper);
        Player player = player(helper);
        player.setPos(helper.absoluteVec(RC_CONTROLLER.getCenter()));
        chamber.setLiquidLevel(3);
        chamber.getTank().fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);
        chamber.pressButton(player, ReactionChamberBlockEntity.BUTTON_DUMP);
        helper.assertTrue(chamber.getTank().getFluidAmount() == 3000, "At the level nothing is dumped");
        chamber.setLiquidLevel(1);
        chamber.pressButton(player, ReactionChamberBlockEntity.BUTTON_DUMP);
        helper.assertTrue(chamber.getTank().getFluidAmount() == 1000, "Above the level only the excess is dumped, was "
                + chamber.getTank().getFluidAmount());
        chamber.setLiquidLevel(0);
        chamber.pressButton(player, ReactionChamberBlockEntity.BUTTON_DUMP);
        helper.assertTrue(chamber.getTank().isEmpty(), "At level 0 everything is dumped");
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void reactionChamberMakesTitanium(GameTestHelper helper) {
        ReactionChamberBlockEntity chamber = buildReactionChamber(helper);
        loadTitanium(chamber, 5);
        helper.runAfterDelay(ReactionChamberBlockEntity.REACTION_TICK + 5, () -> {
            IItemHandlerModifiable inv = chamber.getInventory();
            int titanium = 0;
            int ironOre = 0;
            for (int i = ReactionChamberBlockEntity.SLOT_OUTPUT_FIRST; i <= ReactionChamberBlockEntity.SLOT_OUTPUT_LAST; i++) {
                titanium += inv.getStackInSlot(i).is(TGItems.RAW_TITANIUM.get()) ? inv.getStackInSlot(i).getCount() : 0;
                ironOre += inv.getStackInSlot(i).is(Items.IRON_ORE) ? inv.getStackInSlot(i).getCount() : 0;
            }
            helper.assertTrue(titanium == 2 && ironOre == 1, "One reaction tick makes 2 raw titanium and iron ore, got " + titanium + "/" + ironOre);
            helper.assertTrue(chamber.getTank().getFluidAmount() == 2900, "The reaction uses 100 mB acid, left " + chamber.getTank().getFluidAmount());
            int used = ReactionChamberBlockEntity.ENERGY_CAPACITY - chamber.getEnergy().getEnergyStored();
            helper.assertTrue(used == 25000, "One reaction tick costs 25000 FE, used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 300)
    public static void reactionChamberFailsAtWrongIntensity(GameTestHelper helper) {
        ReactionChamberBlockEntity chamber = buildReactionChamber(helper);
        loadTitanium(chamber, 4);
        helper.runAfterDelay(5, () -> {
            helper.assertFalse(chamber.isReacting(), "A reaction only starts at its intensity");
            chamber.setIntensity(5);
        });
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(chamber.isReacting(), "At intensity 5 the reaction starts");
            chamber.setIntensity(4);
        });
        // two reaction ticks without completion: the titanium recipe breaks the item
        helper.runAfterDelay(10 + 2 * ReactionChamberBlockEntity.REACTION_TICK + 5, () -> {
            helper.assertFalse(chamber.isReacting(), "The reaction is over");
            for (int i = ReactionChamberBlockEntity.SLOT_OUTPUT_FIRST; i <= ReactionChamberBlockEntity.SLOT_OUTPUT_LAST; i++) {
                helper.assertTrue(chamber.getInventory().getStackInSlot(i).isEmpty(), "A failed reaction gives nothing");
            }
            helper.assertTrue(chamber.getInventory().getStackInSlot(ReactionChamberBlockEntity.SLOT_INPUT).isEmpty(), "The ore is lost");
            helper.assertTrue(chamber.isFormed(), "Break item risk keeps the chamber");
            helper.succeed();
        });
    }
}

