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
            IEnergyStorage far = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(last), Direction.NORTH);
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

    @GameTest(template = ARENA, timeoutTicks = 40)
    public static void solarArraysNextToEachOtherMerge(GameTestHelper helper) {
        panel(helper, new BlockPos(1, 1, 2));
        panel(helper, new BlockPos(2, 1, 2));
        helper.runAfterDelay(2, () -> {
            // the far panel waits for an array while the middle one joins the existing array right away
            panel(helper, new BlockPos(4, 1, 2));
            panel(helper, new BlockPos(3, 1, 2));
        });
        helper.runAfterDelay(4, () -> {
            SolarArray first = ((SolarPanelBlockEntity) helper.getBlockEntity(new BlockPos(1, 1, 2))).array();
            SolarArray last = ((SolarPanelBlockEntity) helper.getBlockEntity(new BlockPos(4, 1, 2))).array();
            helper.assertTrue(first == last && first.panels() == 4, "Panels in one row should end up in one array, got "
                    + first.panels() + " and " + last.panels());
            helper.succeed();
        });
    }

    private static techguns.machine.SolarEfficiency efficiency(GameTestHelper helper, net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> biome) {
        return techguns.machine.SolarEfficiency.of(helper.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getHolderOrThrow(biome));
    }

    @GameTest(template = ARENA, timeoutTicks = 20)
    public static void solarBiomeMultipliers(GameTestHelper helper) {
        // the specific type wins over the climate fallbacks (desert is hot and dry as well)
        Object[][] expected = {
                {net.minecraft.world.level.biome.Biomes.DESERT, "desert", 2.0f},
                {net.minecraft.world.level.biome.Biomes.ERODED_BADLANDS, "badlands", 2.0f},
                {net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU, "savanna", 1.5f},
                {net.minecraft.world.level.biome.Biomes.JAGGED_PEAKS, "mountain_peak", 1.5f},
                {net.minecraft.world.level.biome.Biomes.BEACH, "beach", 1.25f},
                {net.minecraft.world.level.biome.Biomes.PLAINS, "plains", 1.1f},
                {net.minecraft.world.level.biome.Biomes.JUNGLE, "jungle", 0.8f},
                {net.minecraft.world.level.biome.Biomes.MANGROVE_SWAMP, "swamp", 0.75f},
                {net.minecraft.world.level.biome.Biomes.DARK_FOREST, "spooky", 0.75f},
                {net.minecraft.world.level.biome.Biomes.ICE_SPIKES, "snowy", 0.75f},
                {net.minecraft.world.level.biome.Biomes.MUSHROOM_FIELDS, "mushroom", 0.9f},
                {net.minecraft.world.level.biome.Biomes.FOREST, "default", 1.0f},
        };
        for (Object[] row : expected) {
            @SuppressWarnings("unchecked")
            techguns.machine.SolarEfficiency e = efficiency(helper,
                    (net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>) row[0]);
            helper.assertTrue(e.type().equals(row[1]) && e.multiplier() == (float) row[2],
                    row[0] + " should be " + row[1] + " x" + row[2] + ", got " + e.type() + " x" + e.multiplier());
        }
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void solarOutputUsesTheBiome(GameTestHelper helper) {
        SolarPanelBlockEntity panel = panel(helper, new BlockPos(2, 1, 2));
        helper.runAfterDelay(25, () -> {
            float factor = techguns.machine.SolarEfficiency.of(helper.getLevel().getBiome(helper.absolutePos(new BlockPos(2, 1, 2)))).multiplier();
            double expected = factor * techguns.core.power.SolarOutput.perTick(techguns.TGConfig.SOLAR_PANEL_FE_PER_TICK.get(), true, true,
                    helper.getLevel().isDay());
            helper.assertTrue(Math.abs(panel.array().lastPerTick() - expected) < 1e-6,
                    "A panel should make the base output times its biome multiplier: " + panel.array().lastPerTick() + " vs " + expected);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 40)
    public static void solarPanelsJoinOnlyOnce(GameTestHelper helper) {
        for (int x = 1; x <= 3; x++) {
            panel(helper, new BlockPos(x, 1, 2));
        }
        helper.runAfterDelay(2, () -> {
            SolarPanelBlockEntity middle = (SolarPanelBlockEntity) helper.getBlockEntity(new BlockPos(2, 1, 2));
            // a chunk load can run onLoad on a panel an array already took in
            middle.onLoad();
            middle.onLoad();
            SolarArray array = middle.array();
            helper.assertTrue(array.panels() == 3 && array.getMaxEnergyStored() == 3 * SolarPanelBlockEntity.CAPACITY,
                    "A panel must count once, got " + array.panels());
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 40)
    public static void solarMenuShowsTheArray(GameTestHelper helper) {
        for (int x = 1; x <= 3; x++) {
            panel(helper, new BlockPos(x, 1, 2));
            helper.setBlock(new BlockPos(x, 3, 2), Blocks.STONE);
        }
        helper.runAfterDelay(2, () -> {
            SolarPanelBlockEntity middle = (SolarPanelBlockEntity) helper.getBlockEntity(new BlockPos(2, 1, 2));
            middle.array().setEnergy(70000 - 58000);
            net.minecraft.world.entity.player.Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            techguns.machine.SolarPanelMenu menu = new techguns.machine.SolarPanelMenu(0, player.getInventory(), middle);
            helper.assertTrue(menu.panels() == 3, "The menu should show three panels, got " + menu.panels());
            helper.assertTrue(menu.capacity() == 3 * SolarPanelBlockEntity.CAPACITY, "The menu should show the array capacity");
            helper.assertTrue(menu.energy() == 12000, "The menu should show the pooled energy, got " + menu.energy());
            helper.assertTrue(menu.output() == 0.0, "A covered array makes nothing");
            helper.succeed();
        });
    }

    /** A covered panel with power, a cable east of it whose west side has {@code mode}, and a press after the cable. */
    private static MetalPressBlockEntity cableSetup(GameTestHelper helper, techguns.core.transport.SideMode mode) {
        panel(helper, new BlockPos(1, 1, 2));
        helper.setBlock(new BlockPos(1, 3, 2), Blocks.STONE);
        techguns.transport.TGPipeBlockEntity cable = TGTransportTests.pipe(helper, new BlockPos(2, 1, 2), techguns.transport.TGPipeKinds.CABLE, 0);
        cable.setMode(techguns.transport.TGPipeKinds.CABLE, Direction.WEST, mode);
        helper.setBlock(new BlockPos(3, 1, 2), TGBlocks.METAL_PRESS.get());
        helper.runAfterDelay(2, () -> ((SolarPanelBlockEntity) helper.getBlockEntity(new BlockPos(1, 1, 2))).array().setEnergy(4000));
        return (MetalPressBlockEntity) helper.getBlockEntity(new BlockPos(3, 1, 2));
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void solarPowersNeutralCables(GameTestHelper helper) {
        MetalPressBlockEntity press = cableSetup(helper, techguns.core.transport.SideMode.NEUTRAL);
        helper.succeedWhen(() -> helper.assertTrue(press.getEnergy().getEnergyStored() > 0, "A neutral cable side should take the panel's power"));
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void solarPowersInputCables(GameTestHelper helper) {
        MetalPressBlockEntity press = cableSetup(helper, techguns.core.transport.SideMode.INPUT);
        helper.succeedWhen(() -> helper.assertTrue(press.getEnergy().getEnergyStored() > 0, "An input cable side should take the panel's power"));
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void solarSkipsOutputAndDisabledCables(GameTestHelper helper) {
        MetalPressBlockEntity output = cableSetup(helper, techguns.core.transport.SideMode.OUTPUT);
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(output.getEnergy().getEnergyStored() == 0, "An output cable side should not get the panel's power");
            techguns.transport.TGPipeBlockEntity cable = (techguns.transport.TGPipeBlockEntity) helper.getBlockEntity(new BlockPos(2, 1, 2));
            cable.setMode(techguns.transport.TGPipeKinds.CABLE, Direction.WEST, techguns.core.transport.SideMode.DISABLED);
        });
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(output.getEnergy().getEnergyStored() == 0, "A disabled cable side should not get the panel's power");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void solarPowersBelowNotAbove(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 1, 2), TGBlocks.METAL_PRESS.get());
        helper.setBlock(new BlockPos(2, 3, 2), TGBlocks.METAL_PRESS.get());
        panel(helper, new BlockPos(2, 2, 2));
        MetalPressBlockEntity below = (MetalPressBlockEntity) helper.getBlockEntity(new BlockPos(2, 1, 2));
        MetalPressBlockEntity above = (MetalPressBlockEntity) helper.getBlockEntity(new BlockPos(2, 3, 2));
        helper.runAfterDelay(2, () -> ((SolarPanelBlockEntity) helper.getBlockEntity(new BlockPos(2, 2, 2))).array().setEnergy(4000));
        helper.runAfterDelay(6, () -> {
            helper.assertTrue(below.getEnergy().getEnergyStored() == 4000, "The machine below should get the power, got "
                    + below.getEnergy().getEnergyStored());
            helper.assertTrue(above.getEnergy().getEnergyStored() == 0, "Nothing should go out through the cell surface");
            helper.assertTrue(helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(new BlockPos(2, 2, 2)),
                    Direction.UP) == null, "The top should offer no energy access");
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
