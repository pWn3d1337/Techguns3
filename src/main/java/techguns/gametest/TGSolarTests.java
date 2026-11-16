package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.machine.MetalPressBlockEntity;
import techguns.machine.SolarArray;
import techguns.machine.SolarPanelBlock;
import techguns.machine.SolarPanelBlockEntity;
import techguns.registry.TGBlocks;

/** Solar Panels (Techguns 3): sky only, arrays share one pool, connected states, pushing into machines. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGSolarTests {
    private static final String ARENA = "empty_8x4x8";

    private static SolarPanelBlockEntity panel(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, TGBlocks.SOLAR_PANEL.get());
        return (SolarPanelBlockEntity) helper.getBlockEntity(pos);
    }

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void solarPanelMakesPowerUnderTheSky(GameTestHelper helper) {
        SolarPanelBlockEntity panel = panel(helper, new BlockPos(2, 1, 2));
        // day or night: at least the moonlight rate
        helper.succeedWhen(() -> helper.assertTrue(panel.array() != null && panel.array().getEnergyStored() > 0,
                "A panel under open sky should make power"));
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void solarPanelNeedsTheSky(GameTestHelper helper) {
        SolarPanelBlockEntity panel = panel(helper, new BlockPos(2, 1, 2));
        helper.setBlock(new BlockPos(2, 3, 2), Blocks.STONE);
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(panel.array().getEnergyStored() == 0, "A covered panel should make nothing");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 40)
    public static void solarPanelsShareOnePool(GameTestHelper helper) {
        BlockPos first = new BlockPos(1, 1, 2);
        BlockPos last = new BlockPos(4, 1, 2);
        for (int x = 1; x <= 4; x++) {
            panel(helper, new BlockPos(x, 1, 2));
            helper.setBlock(new BlockPos(x, 3, 2), Blocks.STONE);
        }
        helper.runAfterDelay(2, () -> {
            SolarArray array = ((SolarPanelBlockEntity) helper.getBlockEntity(first)).array();
            helper.assertTrue(array.panels() == 4, "Four panels in a row should form one array, got " + array.panels());
            helper.assertTrue(array.getMaxEnergyStored() == 4 * SolarPanelBlockEntity.CAPACITY, "The array should pool the capacity");
            array.setEnergy(10000);
            IEnergyStorage far = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(last), Direction.UP);
            helper.assertTrue(far != null && far.extractEnergy(Integer.MAX_VALUE, false) == 10000,
                    "The far panel should hand out the whole pool at once");
            BlockState state = helper.getBlockState(new BlockPos(2, 1, 2));
            helper.assertTrue(state.getValue(SolarPanelBlock.EAST) && state.getValue(SolarPanelBlock.WEST)
                    && !state.getValue(SolarPanelBlock.NORTH), "Inner panels should connect east and west only");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void solarArrayKeepsEnergyWhenAPanelGoes(GameTestHelper helper) {
        for (int x = 1; x <= 3; x++) {
            panel(helper, new BlockPos(x, 1, 2));
            helper.setBlock(new BlockPos(x, 3, 2), Blocks.STONE);
        }
        helper.runAfterDelay(2, () -> ((SolarPanelBlockEntity) helper.getBlockEntity(new BlockPos(1, 1, 2))).array().setEnergy(9000));
        helper.runAfterDelay(4, () -> helper.setBlock(new BlockPos(3, 1, 2), Blocks.AIR));
        helper.runAfterDelay(6, () -> {
            SolarArray array = ((SolarPanelBlockEntity) helper.getBlockEntity(new BlockPos(1, 1, 2))).array();
            helper.assertTrue(array.panels() == 2, "Two panels should be left");
            helper.assertTrue(array.getEnergyStored() == 6000, "The two panels should keep their two thirds, got " + array.getEnergyStored());
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void solarArrayPushesIntoMachines(GameTestHelper helper) {
        for (int x = 1; x <= 2; x++) {
            panel(helper, new BlockPos(x, 1, 2));
            helper.setBlock(new BlockPos(x, 3, 2), Blocks.STONE);
        }
        helper.setBlock(new BlockPos(3, 1, 2), TGBlocks.METAL_PRESS.get());
        MetalPressBlockEntity press = (MetalPressBlockEntity) helper.getBlockEntity(new BlockPos(3, 1, 2));
        helper.runAfterDelay(2, () -> ((SolarPanelBlockEntity) helper.getBlockEntity(new BlockPos(1, 1, 2))).array().setEnergy(5000));
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(press.getEnergy().getEnergyStored() == 5000, "The array should push its pool into the press");
            helper.succeed();
        });
    }
}
