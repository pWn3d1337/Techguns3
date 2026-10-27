package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
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
}
