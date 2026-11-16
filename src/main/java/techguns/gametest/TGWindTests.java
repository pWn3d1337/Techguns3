package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGConfig;
import techguns.Techguns;
import techguns.core.power.WindOutput;
import techguns.machine.BiomeEfficiency;
import techguns.machine.MetalPressBlockEntity;
import techguns.machine.WindTurbineBlock;
import techguns.machine.WindTurbineBlockEntity;
import techguns.registry.TGBlocks;

/** Wind Turbines (Techguns 3): vertical columns, height/biome/weather output, open sky, spacing, base-only output. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGWindTests {
    private static final String ARENA = "empty_8x4x8";

    private static void column(GameTestHelper helper, int x, int z, int from, int to) {
        for (int y = from; y <= to; y++) {
            helper.setBlock(new BlockPos(x, y, z), TGBlocks.WIND_TURBINE.get().defaultBlockState()
                    .setValue(WindTurbineBlock.DOWN, y > from).setValue(WindTurbineBlock.UP, y < to));
        }
    }

    private static WindTurbineBlockEntity turbine(GameTestHelper helper, BlockPos pos) {
        return (WindTurbineBlockEntity) helper.getBlockEntity(pos);
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void windTurbineSegmentsFormOneColumn(GameTestHelper helper) {
        column(helper, 2, 2, 1, 3);
        helper.runAfterDelay(25, () -> {
            WindTurbineBlockEntity base = turbine(helper, new BlockPos(2, 1, 2));
            helper.assertTrue(base.isBase() && !turbine(helper, new BlockPos(2, 2, 2)).isBase(), "Only the lowest segment should be the base");
            helper.assertTrue(base.segments() == 3, "Three stacked segments should form one column, got " + base.segments());
            helper.assertTrue(base.capacity() == 3 * WindTurbineBlockEntity.CAPACITY, "The column should pool the capacity");
            helper.assertTrue(turbine(helper, new BlockPos(2, 3, 2)).base() == base, "The top segment should find the base");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void windTurbineOutputFollowsHeightBiomeAndWeather(GameTestHelper helper) {
        column(helper, 2, 2, 1, 3);
        helper.runAfterDelay(25, () -> {
            ServerLevel level = helper.getLevel();
            WindTurbineBlockEntity base = turbine(helper, new BlockPos(2, 1, 2));
            double sum = 0.0;
            for (int y = 1; y <= 3; y++) {
                sum += WindOutput.heightFactor(helper.absolutePos(new BlockPos(2, y, 2)).getY() + 0.5 - level.getSeaLevel());
            }
            double biome = BiomeEfficiency.wind(level.getBiome(helper.absolutePos(new BlockPos(2, 3, 2)))).multiplier();
            double weather = WindOutput.weatherFactor(level.getRainLevel(1.0f), level.getThunderLevel(1.0f));
            double expected = WindOutput.perTick(TGConfig.WIND_TURBINE_FE_PER_TICK.get(), sum, biome, weather, true);
            helper.assertTrue(base.openSky(), "The column should see the sky");
            helper.assertTrue(Math.abs(base.lastPerTick() - expected) < 1e-6, "Output should be " + expected + ", got " + base.lastPerTick());
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void windTurbineNeedsOpenSkyAboveTheTop(GameTestHelper helper) {
        column(helper, 2, 2, 1, 2);
        helper.setBlock(new BlockPos(2, 3, 2), Blocks.STONE);
        helper.runAfterDelay(25, () -> {
            WindTurbineBlockEntity base = turbine(helper, new BlockPos(2, 1, 2));
            helper.assertTrue(!base.openSky() && base.lastPerTick() == 0.0 && base.wind() == 0.0f,
                    "A covered column should make nothing and stand still");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 20)
    public static void windTurbineCannotBePlacedNextToAnother(GameTestHelper helper) {
        column(helper, 2, 2, 1, 1);
        ServerLevel level = helper.getLevel();
        BlockPos beside = helper.absolutePos(new BlockPos(3, 1, 2));
        BlockPos apart = helper.absolutePos(new BlockPos(4, 1, 2));
        BlockPlaceContext next = new BlockPlaceContext(level, null, InteractionHand.MAIN_HAND, new ItemStack(TGBlocks.WIND_TURBINE.get()),
                new BlockHitResult(Vec3.atCenterOf(beside), Direction.UP, beside, false));
        BlockPlaceContext free = new BlockPlaceContext(level, null, InteractionHand.MAIN_HAND, new ItemStack(TGBlocks.WIND_TURBINE.get()),
                new BlockHitResult(Vec3.atCenterOf(apart), Direction.UP, apart, false));
        helper.assertTrue(TGBlocks.WIND_TURBINE.get().getStateForPlacement(next) == null, "A turbine right next to another should be refused");
        helper.assertTrue(TGBlocks.WIND_TURBINE.get().getStateForPlacement(free) != null, "A turbine one block apart should be allowed");
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void windTurbinePowersThroughTheBaseOnly(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 1, 2), TGBlocks.METAL_PRESS.get());
        helper.setBlock(new BlockPos(3, 3, 2), TGBlocks.METAL_PRESS.get());
        column(helper, 2, 2, 2, 3);
        MetalPressBlockEntity below = (MetalPressBlockEntity) helper.getBlockEntity(new BlockPos(2, 1, 2));
        MetalPressBlockEntity beside = (MetalPressBlockEntity) helper.getBlockEntity(new BlockPos(3, 3, 2));
        helper.runAfterDelay(2, () -> turbine(helper, new BlockPos(2, 2, 2)).setEnergy(4000));
        helper.runAfterDelay(6, () -> {
            helper.assertTrue(below.getEnergy().getEnergyStored() >= 4000, "The machine below the base should get the power, got "
                    + below.getEnergy().getEnergyStored());
            helper.assertTrue(beside.getEnergy().getEnergyStored() == 0, "Upper segments should not give power");
            helper.assertTrue(helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(new BlockPos(2, 3, 2)),
                    Direction.EAST) == null, "Upper segments should offer no energy access");
            helper.assertTrue(helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(new BlockPos(2, 2, 2)),
                    Direction.NORTH) != null, "The base should offer energy on its sides");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void windTurbineNewBaseTakesTheOldBasesEnergy(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 2, 2), TGBlocks.WIND_TURBINE.get());
        helper.runAfterDelay(2, () -> {
            turbine(helper, new BlockPos(2, 2, 2)).setEnergy(1000);
            // placing below updates the shapes: the old base becomes an upper segment
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(2, 1, 2)), TGBlocks.WIND_TURBINE.get().defaultBlockState()
                    .setValue(WindTurbineBlock.UP, true));
        });
        helper.runAfterDelay(30, () -> {
            WindTurbineBlockEntity base = turbine(helper, new BlockPos(2, 1, 2));
            WindTurbineBlockEntity old = turbine(helper, new BlockPos(2, 2, 2));
            helper.assertTrue(!old.isBase(), "The old base should now be an upper segment");
            helper.assertTrue(old.energy() == 0 && base.energy() >= 1000, "The new base should take the energy, got " + base.energy());
            helper.succeed();
        });
    }
}
