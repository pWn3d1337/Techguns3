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
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import techguns.Techguns;
import techguns.machine.MetalPressBlockEntity;
import techguns.machine.RedstoneMode;
import techguns.machine.TGMachineBlockEntity;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;

/**
 * Machines (Phase 5b1): the Metal Press presses by recipe with power, splits stacks, runs several operations at once
 * with stack upgrades, honours redstone mode and security; automation inserts by the slot rules and only extracts
 * results.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGMachineTests {
    private static final String ARENA = "empty_8x4x8";
    private static final BlockPos PRESS = new BlockPos(2, 2, 2);
    /** One Metal Press operation plus some slack. */
    private static final int OPERATION_TICKS = MetalPressBlockEntity.TIME + 5;

    private static MetalPressBlockEntity press(GameTestHelper helper) {
        helper.setBlock(PRESS, TGBlocks.METAL_PRESS.get());
        return (MetalPressBlockEntity) helper.getBlockEntity(PRESS);
    }

    private static Player playerAt(GameTestHelper helper, BlockPos pos) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos abs = helper.absolutePos(pos);
        player.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 1.5);
        return player;
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void metalPressMakesPlatesWithPower(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        press.getEnergy().setEnergy(MetalPressBlockEntity.ENERGY_CAPACITY);
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT2, new ItemStack(Items.IRON_INGOT));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            ItemStack out = press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(TGItems.IRON_PLATE.get()) && out.getCount() == 2, "Two iron ingots should make 2 iron plates, got " + out);
            int used = MetalPressBlockEntity.ENERGY_CAPACITY - press.getEnergy().getEnergyStored();
            helper.assertTrue(used == MetalPressBlockEntity.TIME * MetalPressBlockEntity.POWER_PER_TICK,
                    "One operation should use 100 x 20 FE, used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void metalPressNeedsPower(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT2, new ItemStack(Items.IRON_INGOT));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            helper.assertTrue(press.isWorking(), "The job should have started (inputs taken)");
            helper.assertTrue(press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT).isEmpty(),
                    "Without power the press should not finish");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void metalPressAcceptsSwappedInputs(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        press.getEnergy().setEnergy(MetalPressBlockEntity.ENERGY_CAPACITY);
        // recipe is iron plate + flint; the inputs are in the other order
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.FLINT));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT2, new ItemStack(TGItems.IRON_PLATE.get()));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            ItemStack out = press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(TGItems.MECHANICAL_PARTS_IRON.get()), "Swapped inputs should still press, got " + out);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void metalPressAutoSplitAndStackUpgrade(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        Player player = playerAt(helper, PRESS);
        press.getEnergy().setEnergy(MetalPressBlockEntity.ENERGY_CAPACITY);
        helper.assertTrue(press.pressButton(player, MetalPressBlockEntity.BUTTON_AUTO_SPLIT), "Auto split should switch on");
        // 3 upgrades: up to 4 operations at once; 8 ingots split 4 / 4
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_UPGRADE, new ItemStack(TGItems.MACHINE_UPGRADE_STACK.get(), 3));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT, 8));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            ItemStack out = press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(TGItems.IRON_PLATE.get()) && out.getCount() == 8, "8 split ingots should make 8 plates at once, got " + out);
            int used = MetalPressBlockEntity.ENERGY_CAPACITY - press.getEnergy().getEnergyStored();
            helper.assertTrue(used == 4 * MetalPressBlockEntity.TIME * MetalPressBlockEntity.POWER_PER_TICK,
                    "Four operations at once should use four times the power, used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 300)
    public static void metalPressWaitsForRedstone(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        Player player = playerAt(helper, PRESS);
        press.getEnergy().setEnergy(MetalPressBlockEntity.ENERGY_CAPACITY);
        press.pressButton(player, TGMachineBlockEntity.BUTTON_REDSTONE);
        helper.assertTrue(press.getRedstoneMode() == RedstoneMode.HIGH, "One press should require a high signal");
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT2, new ItemStack(Items.IRON_INGOT));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            helper.assertTrue(press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT).isEmpty(),
                    "Without a signal the press should not work");
            helper.setBlock(PRESS.relative(Direction.EAST), Blocks.REDSTONE_BLOCK);
            helper.runAfterDelay(OPERATION_TICKS, () -> {
                helper.assertTrue(press.hasSignal(), "The press should see the redstone block");
                helper.assertFalse(press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT).isEmpty(),
                        "With a signal the press should finish");
                helper.succeed();
            });
        });
    }

    @GameTest(template = ARENA)
    public static void ownerOnlyLocksOthersOut(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        Player owner = playerAt(helper, PRESS);
        Player other = playerAt(helper, PRESS);
        press.setOwner(owner);

        helper.assertFalse(press.pressButton(other, TGMachineBlockEntity.BUTTON_SECURITY), "Only the owner may change security");
        helper.assertTrue(press.pressButton(owner, TGMachineBlockEntity.BUTTON_SECURITY), "The owner may change security");
        helper.assertTrue(press.mayAccess(owner), "The owner keeps access");
        helper.assertFalse(press.mayAccess(other), "Others are locked out in owner-only mode");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void automationFollowsSlotRules(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        IItemHandler items = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(PRESS), null);
        IEnergyStorage energy = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(PRESS), null);
        helper.assertTrue(items != null && energy != null, "The press should expose items and energy");

        helper.assertTrue(energy.receiveEnergy(500, false) == 500 && energy.extractEnergy(100, false) == 0,
                "Power goes in, but not out");
        ItemStack dirt = items.insertItem(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.DIRT), false);
        helper.assertTrue(dirt.getCount() == 1, "Dirt is no Metal Press input");
        ItemStack ingot = items.insertItem(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT), false);
        helper.assertTrue(ingot.isEmpty(), "An iron ingot is a Metal Press input");
        helper.assertTrue(items.extractItem(MetalPressBlockEntity.SLOT_INPUT1, 1, false).isEmpty(), "Inputs can't be pulled out");
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT, new ItemStack(TGItems.IRON_PLATE.get(), 2));
        helper.assertTrue(items.extractItem(MetalPressBlockEntity.SLOT_OUTPUT, 2, false).getCount() == 2, "Results can be pulled out");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void brokenPressDropsContents(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT, 5));
        helper.destroyBlock(PRESS);
        helper.assertItemEntityCountIs(Items.IRON_INGOT, PRESS, 2.0, 5);
        helper.succeed();
    }
}
