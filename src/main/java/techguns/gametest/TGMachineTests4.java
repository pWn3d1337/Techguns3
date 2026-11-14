package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGConfig;
import techguns.Techguns;
import techguns.machine.CombustionGeneratorBlockEntity;
import techguns.machine.ElectricFurnaceBlockEntity;
import techguns.machine.FluidFuel;
import techguns.machine.LiquidGeneratorBlockEntity;
import techguns.machine.MetalPressBlockEntity;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;

/** Standalone tech machines (design doc §4.8): generators and the Electric Furnace. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGMachineTests4 {
    private static final String ARENA = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(2, 2, 2);

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void combustionGeneratorBurnsCoalAndPushes(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.COMBUSTION_GENERATOR.get());
        CombustionGeneratorBlockEntity generator = (CombustionGeneratorBlockEntity) helper.getBlockEntity(POS);
        generator.getInventory().setStackInSlot(CombustionGeneratorBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL, 2));
        helper.setBlock(POS.east(), TGBlocks.METAL_PRESS.get());
        MetalPressBlockEntity press = (MetalPressBlockEntity) helper.getBlockEntity(POS.east());
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(generator.getInventory().getStackInSlot(CombustionGeneratorBlockEntity.SLOT_FUEL).getCount() == 1,
                    "One coal should burn");
            helper.assertTrue(generator.getBlockState().getValue(BlockStateProperties.LIT), "A burning generator should be lit");
            int made = TGConfig.COMBUSTION_GENERATOR_FE_PER_TICK.get();
            int got = press.getEnergy().getEnergyStored();
            helper.assertTrue(got >= made * 15 && got <= made * 21, "The press should get the generator's FE, got " + got);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 40)
    public static void combustionGeneratorKeepsFuelWhenFull(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.COMBUSTION_GENERATOR.get());
        CombustionGeneratorBlockEntity generator = (CombustionGeneratorBlockEntity) helper.getBlockEntity(POS);
        generator.getEnergy().setEnergy(CombustionGeneratorBlockEntity.ENERGY_CAPACITY);
        generator.getInventory().setStackInSlot(CombustionGeneratorBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL));
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(generator.getInventory().getStackInSlot(CombustionGeneratorBlockEntity.SLOT_FUEL).getCount() == 1,
                    "A full generator should not burn fuel");
            helper.assertTrue(generator.getEnergy().canReceive() == false, "Nothing may insert into a generator");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 40)
    public static void combustionGeneratorLeavesTheBucket(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.COMBUSTION_GENERATOR.get());
        CombustionGeneratorBlockEntity generator = (CombustionGeneratorBlockEntity) helper.getBlockEntity(POS);
        generator.getInventory().setStackInSlot(CombustionGeneratorBlockEntity.SLOT_FUEL, new ItemStack(Items.LAVA_BUCKET));
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(generator.getInventory().getStackInSlot(CombustionGeneratorBlockEntity.SLOT_FUEL).is(Items.BUCKET),
                    "The lava bucket should leave a bucket");
            helper.assertTrue(!generator.getAutomationHandler().extractItem(0, 1, true).isEmpty(), "Automation should take the bucket out");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 40)
    public static void liquidGeneratorBurnsDataMapFuels(GameTestHelper helper) {
        helper.assertTrue(FluidFuel.burnPerMb(new FluidStack(Fluids.LAVA, 1)) == 20.0f, "Lava should be a fuel from the data map");
        helper.assertTrue(FluidFuel.burnPerMb(new FluidStack(Fluids.WATER, 1)) == 0.0f, "Water is no fuel");
        helper.setBlock(POS, TGBlocks.LIQUID_GENERATOR.get());
        LiquidGeneratorBlockEntity generator = (LiquidGeneratorBlockEntity) helper.getBlockEntity(POS);
        IFluidHandler handler = generator.getFluidHandler();
        helper.assertTrue(handler.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE) == 0, "Water should not go in");
        helper.assertTrue(handler.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE) == 1000, "Lava should go in");
        helper.assertTrue(handler.drain(100, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "Nothing can be drained");
        helper.runAfterDelay(20, () -> {
            int made = TGConfig.LIQUID_GENERATOR_FE_PER_TICK.get();
            int energy = generator.getEnergy().getEnergyStored();
            helper.assertTrue(energy >= made * 15 && energy <= made * 21, "The generator should make " + made + " FE/t, has " + energy);
            // 2 burn ticks per tick, 20 per mB: one mB lasts 10 ticks
            helper.assertTrue(generator.getTank().getFluidAmount() >= 996 && generator.getTank().getFluidAmount() < 1000,
                    "Lava should burn slowly, left " + generator.getTank().getFluidAmount());
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 40)
    public static void liquidGeneratorEmptiesBuckets(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.LIQUID_GENERATOR.get());
        LiquidGeneratorBlockEntity generator = (LiquidGeneratorBlockEntity) helper.getBlockEntity(POS);
        generator.getInventory().setStackInSlot(LiquidGeneratorBlockEntity.SLOT_CONTAINER_IN, new ItemStack(Items.LAVA_BUCKET));
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(generator.getInventory().getStackInSlot(LiquidGeneratorBlockEntity.SLOT_CONTAINER_OUT).is(Items.BUCKET),
                    "The empty bucket should go to the output slot");
            helper.assertTrue(generator.getTank().getFluidAmount() + 1 >= 999, "The lava should be in the tank");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void electricFurnaceSmeltsWithUpgrades(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.ELECTRIC_FURNACE.get());
        ElectricFurnaceBlockEntity furnace = (ElectricFurnaceBlockEntity) helper.getBlockEntity(POS);
        furnace.getEnergy().setEnergy(ElectricFurnaceBlockEntity.ENERGY_CAPACITY);
        furnace.getInventory().setStackInSlot(ElectricFurnaceBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON, 5));
        furnace.getInventory().setStackInSlot(ElectricFurnaceBlockEntity.SLOT_UPGRADE, new ItemStack(TGItems.MACHINE_UPGRADE_STACK.get(), 3));
        // vanilla raw iron: 200 ticks in a furnace, 100 here
        helper.runAfterDelay(105, () -> {
            ItemStack out = furnace.getInventory().getStackInSlot(ElectricFurnaceBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(Items.IRON_INGOT) && out.getCount() == 4, "Four raw iron should smelt at once, got " + out);
            helper.assertTrue(furnace.getStoredExperience() > 0, "Smelting should collect experience");
            int used = ElectricFurnaceBlockEntity.ENERGY_CAPACITY - furnace.getEnergy().getEnergyStored();
            helper.assertTrue(used >= 100 * 4 * TGConfig.ELECTRIC_FURNACE_FE_PER_TICK.get(), "Four items should use four times the power, used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void electricFurnaceNeedsPower(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.ELECTRIC_FURNACE.get());
        ElectricFurnaceBlockEntity furnace = (ElectricFurnaceBlockEntity) helper.getBlockEntity(POS);
        furnace.getInventory().setStackInSlot(ElectricFurnaceBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON));
        helper.runAfterDelay(120, () -> {
            if (!TGConfig.MACHINES_NEED_NO_POWER.get()) {
                helper.assertTrue(furnace.getInventory().getStackInSlot(ElectricFurnaceBlockEntity.SLOT_OUTPUT).isEmpty(),
                        "Without power nothing should smelt");
            }
            helper.assertTrue(!furnace.getAutomationHandler().insertItem(ElectricFurnaceBlockEntity.SLOT_INPUT, new ItemStack(Items.STICK), true)
                    .isEmpty(), "Items without a smelting recipe should not go in");
            helper.succeed();
        });
    }
}
