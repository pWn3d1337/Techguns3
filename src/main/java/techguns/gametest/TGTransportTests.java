package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGConfig;
import techguns.Techguns;
import techguns.core.transport.SideMode;
import techguns.machine.LiquidGeneratorBlockEntity;
import techguns.machine.MetalPressBlockEntity;
import techguns.machine.TGMachineBlockEntity;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;
import techguns.transport.PipeHit;
import techguns.transport.PipeKind;
import techguns.transport.PipeSegment;
import techguns.transport.TGPipeBlock;
import techguns.transport.TGPipeBlockEntity;
import techguns.transport.TGPipeKinds;

/**
 * Cables and pipes (design doc §4.8): energy, fluid and item networks move without buffers, side modes, mixed tiers
 * limited by their slowest zone, round robin item routing, several kinds in one block, wrench and pipe item use.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGTransportTests {
    private static final String ARENA = "empty_8x4x8";

    static TGPipeBlockEntity pipe(GameTestHelper helper, BlockPos pos, PipeKind kind, int tier) {
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof TGPipeBlockEntity)) {
            helper.setBlock(pos, TGBlocks.PIPE.get());
        }
        TGPipeBlockEntity pipe = (TGPipeBlockEntity) helper.getBlockEntity(pos);
        pipe.addSegment(kind, tier);
        return pipe;
    }

    /** A line of pipes from {@code from} (inclusive) along +x, {@code tiers[i]} at {@code from + i}. */
    private static void line(GameTestHelper helper, BlockPos from, PipeKind kind, int... tiers) {
        for (int i = 0; i < tiers.length; i++) {
            pipe(helper, from.east(i), kind, tiers[i]);
        }
    }

    private static <T> T at(GameTestHelper helper, BlockPos pos) {
        @SuppressWarnings("unchecked")
        T be = (T) helper.getBlockEntity(pos);
        return be;
    }

    private static LiquidGeneratorBlockEntity chargedGenerator(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, TGBlocks.LIQUID_GENERATOR.get());
        LiquidGeneratorBlockEntity generator = at(helper, pos);
        generator.getEnergy().setEnergy(LiquidGeneratorBlockEntity.ENERGY_CAPACITY);
        return generator;
    }

    private static MetalPressBlockEntity press(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, TGBlocks.METAL_PRESS.get());
        return at(helper, pos);
    }

    // --- energy ---

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void cableMovesGeneratorEnergyToMachine(GameTestHelper helper) {
        chargedGenerator(helper, new BlockPos(1, 2, 2));
        line(helper, new BlockPos(2, 2, 2), TGPipeKinds.CABLE, 0, 0, 0);
        MetalPressBlockEntity press = press(helper, new BlockPos(5, 2, 2));
        helper.succeedWhen(() -> helper.assertTrue(press.getEnergy().getEnergyStored() > 0, "The press should get FE through the cable"));
    }

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void basicSectionLimitsMixedTierLine(GameTestHelper helper) {
        // the generator pushes 400 FE/t; an elite line with one basic cable passes 256 FE/t at most
        chargedGenerator(helper, new BlockPos(1, 2, 2));
        line(helper, new BlockPos(2, 2, 2), TGPipeKinds.CABLE, 2, 0, 2);
        MetalPressBlockEntity press = press(helper, new BlockPos(5, 2, 2));
        int ticks = 20;
        helper.runAfterDelay(ticks, () -> {
            int got = press.getEnergy().getEnergyStored();
            int basic = TGConfig.CABLE_RATE_BASIC.get();
            helper.assertTrue(got > 0, "Mixed tiers should connect");
            helper.assertTrue(got <= basic * (ticks + 1), "The basic cable should limit the flow to " + basic + " FE/t, got " + got);
            helper.assertTrue(got >= basic * (ticks - 5), "The line should run at the basic rate, got " + got);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void eliteLineIsNotLimited(GameTestHelper helper) {
        chargedGenerator(helper, new BlockPos(1, 2, 2));
        line(helper, new BlockPos(2, 2, 2), TGPipeKinds.CABLE, 2, 2, 2);
        MetalPressBlockEntity press = press(helper, new BlockPos(5, 2, 2));
        int ticks = 20;
        helper.runAfterDelay(ticks, () -> {
            int got = press.getEnergy().getEnergyStored();
            helper.assertTrue(got > TGConfig.CABLE_RATE_BASIC.get() * (ticks + 1),
                    "An elite line should pass the generator's 400 FE/t, got " + got);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void cableSplitsEnergyEvenly(GameTestHelper helper) {
        chargedGenerator(helper, new BlockPos(1, 2, 2));
        line(helper, new BlockPos(2, 2, 2), TGPipeKinds.CABLE, 2, 2, 2);
        MetalPressBlockEntity a = press(helper, new BlockPos(3, 2, 1));
        MetalPressBlockEntity b = press(helper, new BlockPos(3, 2, 3));
        helper.runAfterDelay(20, () -> {
            int ea = a.getEnergy().getEnergyStored();
            int eb = b.getEnergy().getEnergyStored();
            helper.assertTrue(ea > 0 && Math.abs(ea - eb) <= 2, "Both presses should get the same FE, got " + ea + " and " + eb);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void outputSideRefusesPushes(GameTestHelper helper) {
        chargedGenerator(helper, new BlockPos(1, 2, 2));
        TGPipeBlockEntity first = pipe(helper, new BlockPos(2, 2, 2), TGPipeKinds.CABLE, 0);
        first.setMode(TGPipeKinds.CABLE, Direction.WEST, SideMode.OUTPUT);
        pipe(helper, new BlockPos(3, 2, 2), TGPipeKinds.CABLE, 0);
        MetalPressBlockEntity press = press(helper, new BlockPos(4, 2, 2));
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(press.getEnergy().getEnergyStored() == 0, "An output side should not take the generator's FE");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void disabledPipeSideCutsTheLine(GameTestHelper helper) {
        chargedGenerator(helper, new BlockPos(1, 2, 2));
        line(helper, new BlockPos(2, 2, 2), TGPipeKinds.CABLE, 0, 0);
        ((TGPipeBlockEntity) at(helper, new BlockPos(2, 2, 2))).setMode(TGPipeKinds.CABLE, Direction.EAST, SideMode.DISABLED);
        MetalPressBlockEntity press = press(helper, new BlockPos(4, 2, 2));
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(press.getEnergy().getEnergyStored() == 0, "A disabled side between two cables should cut the line");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void inputSidePullsEnergy(GameTestHelper helper) {
        LiquidGeneratorBlockEntity generator = chargedGenerator(helper, new BlockPos(1, 2, 2));
        // redstone mode "high" without a signal: the generator does not push
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos abs = helper.absolutePos(new BlockPos(1, 2, 2));
        player.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 2.5);
        helper.assertTrue(generator.pressButton(player, TGMachineBlockEntity.BUTTON_REDSTONE), "The redstone mode should switch");
        TGPipeBlockEntity first = pipe(helper, new BlockPos(2, 2, 2), TGPipeKinds.CABLE, 0);
        first.setMode(TGPipeKinds.CABLE, Direction.WEST, SideMode.INPUT);
        MetalPressBlockEntity press = press(helper, new BlockPos(3, 2, 2));
        helper.succeedWhen(() -> helper.assertTrue(press.getEnergy().getEnergyStored() > 0, "An input side should pull from the generator"));
    }

    // --- fluids ---

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void fluidPipePullsLavaIntoGenerator(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 2, 2), Blocks.LAVA_CAULDRON);
        TGPipeBlockEntity first = pipe(helper, new BlockPos(2, 2, 2), TGPipeKinds.FLUID_PIPE, 2);
        first.setMode(TGPipeKinds.FLUID_PIPE, Direction.WEST, SideMode.INPUT);
        pipe(helper, new BlockPos(3, 2, 2), TGPipeKinds.FLUID_PIPE, 2);
        helper.setBlock(new BlockPos(4, 2, 2), TGBlocks.LIQUID_GENERATOR.get());
        LiquidGeneratorBlockEntity generator = at(helper, new BlockPos(4, 2, 2));
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.CAULDRON, new BlockPos(1, 2, 2));
            helper.assertTrue(generator.getTank().getFluidAmount() > 0 || generator.getBurnRemaining() > 0,
                    "The lava should reach the generator");
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void fluidPipeRoutesPushedFluid(GameTestHelper helper) {
        line(helper, new BlockPos(2, 2, 2), TGPipeKinds.FLUID_PIPE, 1, 1);
        helper.setBlock(new BlockPos(4, 2, 2), TGBlocks.LIQUID_GENERATOR.get());
        LiquidGeneratorBlockEntity generator = at(helper, new BlockPos(4, 2, 2));
        // a block west of the first pipe pushes: needs an endpoint there, a chem lab tank
        helper.setBlock(new BlockPos(1, 2, 2), TGBlocks.CHEM_LAB.get());
        helper.runAfterDelay(5, () -> {
            IFluidHandler port = helper.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    helper.absolutePos(new BlockPos(2, 2, 2)), Direction.WEST);
            helper.assertTrue(port != null, "A neutral fluid pipe side should accept pushes");
            int filled = port.fill(new FluidStack(net.minecraft.world.level.material.Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE);
            helper.assertTrue(filled == TGConfig.FLUID_PIPE_RATE_ADVANCED.get(), "One push should pass the advanced rate, filled " + filled);
            helper.assertTrue(generator.getTank().getFluidAmount() == filled, "The pushed lava should be in the generator");
            helper.succeed();
        });
    }

    // --- items ---

    private static ChestBlockEntity chest(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));
        return at(helper, pos);
    }

    private static int count(ChestBlockEntity chest) {
        int n = 0;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            n += chest.getItem(i).getCount();
        }
        return n;
    }

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void itemPipeMovesItemsInstantly(GameTestHelper helper) {
        ChestBlockEntity source = chest(helper, new BlockPos(1, 2, 2));
        source.setItem(0, new ItemStack(Items.COBBLESTONE, 20));
        line(helper, new BlockPos(2, 2, 2), TGPipeKinds.ITEM_PIPE, 0, 0, 0);
        ((TGPipeBlockEntity) at(helper, new BlockPos(2, 2, 2))).setMode(TGPipeKinds.ITEM_PIPE, Direction.WEST, SideMode.INPUT);
        ChestBlockEntity target = chest(helper, new BlockPos(5, 2, 2));
        int perOperation = TGConfig.ITEM_PIPE_COUNT_BASIC.get();
        helper.runAfterDelay(TGConfig.ITEM_PIPE_INTERVAL_BASIC.get() + 2, () -> {
            int moved = count(target);
            helper.assertTrue(moved > 0 && moved % perOperation == 0 && moved <= 2 * perOperation,
                    "Item pipes should move " + perOperation + " items per operation, moved " + moved);
            helper.assertTrue(moved + count(source) == 20, "No items should be lost or in transit");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void itemPipeRoundRobinNearestFirst(GameTestHelper helper) {
        ChestBlockEntity source = chest(helper, new BlockPos(0, 2, 2));
        source.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
        line(helper, new BlockPos(1, 2, 2), TGPipeKinds.ITEM_PIPE, 0, 0, 0, 0, 0, 0);
        ((TGPipeBlockEntity) at(helper, new BlockPos(1, 2, 2))).setMode(TGPipeKinds.ITEM_PIPE, Direction.WEST, SideMode.INPUT);
        ChestBlockEntity near = chest(helper, new BlockPos(2, 2, 1));
        ChestBlockEntity middle = chest(helper, new BlockPos(4, 2, 1));
        ChestBlockEntity far = chest(helper, new BlockPos(6, 2, 1));
        int perOperation = TGConfig.ITEM_PIPE_COUNT_BASIC.get();
        // the order in which the chests first get items
        java.util.List<ChestBlockEntity> order = new java.util.ArrayList<>();
        helper.onEachTick(() -> {
            for (ChestBlockEntity chest : java.util.List.of(near, middle, far)) {
                if (count(chest) > 0 && !order.contains(chest)) {
                    order.add(chest);
                }
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(order.size() == 3, "All three chests should get items");
            helper.assertTrue(order.get(0) == near && order.get(1) == middle && order.get(2) == far,
                    "The chests should be served nearest first, one after another");
            helper.assertTrue(count(near) % perOperation == 0 && count(far) == perOperation, "Each operation moves " + perOperation + " items");
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void hopperPushIntoItemPipeIsRouted(GameTestHelper helper) {
        pipe(helper, new BlockPos(2, 2, 2), TGPipeKinds.ITEM_PIPE, 0);
        pipe(helper, new BlockPos(3, 2, 2), TGPipeKinds.ITEM_PIPE, 0);
        ChestBlockEntity target = chest(helper, new BlockPos(4, 2, 2));
        helper.setBlock(new BlockPos(2, 3, 2), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        HopperBlockEntity hopper = at(helper, new BlockPos(2, 3, 2));
        hopper.setItem(0, new ItemStack(Items.IRON_INGOT, 3));
        helper.succeedWhen(() -> helper.assertTrue(count(target) > 0, "A hopper pushing into an item pipe should fill the chest"));
    }

    // --- several kinds, tools ---

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void kindsShareOneBlock(GameTestHelper helper) {
        BlockPos shared = new BlockPos(2, 2, 2);
        chargedGenerator(helper, new BlockPos(1, 2, 2));
        TGPipeBlockEntity pipe = pipe(helper, shared, TGPipeKinds.CABLE, 0);
        helper.assertTrue(pipe.addSegment(TGPipeKinds.ITEM_PIPE, 0), "An item pipe should fit next to a cable");
        helper.assertTrue(pipe.addSegment(TGPipeKinds.FLUID_PIPE, 0), "A fluid pipe should fit too");
        helper.assertFalse(pipe.addSegment(TGPipeKinds.CABLE, 1), "A second cable should not fit");
        MetalPressBlockEntity press = press(helper, new BlockPos(3, 2, 2));
        helper.succeedWhen(() -> {
            helper.assertTrue(press.getEnergy().getEnergyStored() > 0, "The cable should work with other kinds in its block");
            pipe.removeSegment(TGPipeKinds.CABLE);
            helper.assertBlockPresent(TGBlocks.PIPE.get(), shared);
            helper.assertTrue(pipe.segments().size() == 2, "Removing the cable should leave the other kinds");
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 40)
    public static void lonePipesSitInTheCentreAndMoveAsideWhenStacked(GameTestHelper helper) {
        BlockPos stacked = new BlockPos(2, 2, 2);
        BlockPos alone = new BlockPos(3, 2, 2);
        TGPipeBlockEntity first = pipe(helper, stacked, TGPipeKinds.ITEM_PIPE, 0);
        TGPipeBlockEntity second = pipe(helper, alone, TGPipeKinds.ITEM_PIPE, 0);
        helper.assertTrue(first.drawnLane(TGPipeKinds.ITEM_PIPE) == techguns.core.transport.Lane.B, "A lone item pipe should be centred");
        first.addSegment(TGPipeKinds.CABLE, 0);
        first.addSegment(TGPipeKinds.FLUID_PIPE, 0);
        helper.assertTrue(first.drawnLane(TGPipeKinds.CABLE) == techguns.core.transport.Lane.B, "The cable should keep the centre");
        helper.assertTrue(first.drawnLane(TGPipeKinds.ITEM_PIPE) == techguns.core.transport.Lane.C, "The item pipe should move aside");
        helper.assertTrue(first.drawnLane(TGPipeKinds.FLUID_PIPE) == techguns.core.transport.Lane.A, "The fluid pipe should move aside");
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(first.segment(TGPipeKinds.ITEM_PIPE).adapters() == 1 << Direction.EAST.get3DDataValue(),
                    "The moved item pipe should get an adapter towards the centred one");
            helper.assertTrue(second.segment(TGPipeKinds.ITEM_PIPE).adapters() == 0, "The centred item pipe needs no adapter");
            first.removeSegment(TGPipeKinds.CABLE);
            first.removeSegment(TGPipeKinds.FLUID_PIPE);
        });
        helper.runAfterDelay(6, () -> {
            helper.assertTrue(first.segment(TGPipeKinds.ITEM_PIPE).adapters() == 0, "Back in the centre, the adapter should go");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void wrenchCyclesTheClickedSide(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        TGPipeBlockEntity pipe = pipe(helper, pos, TGPipeKinds.CABLE, 0);
        press(helper, pos.above());
        helper.runAfterDelay(3, () -> {
            PipeSegment segment = pipe.segment(TGPipeKinds.CABLE);
            helper.assertTrue(segment.endpoint(Direction.UP), "The press above should be an endpoint");
            // look at the upper arm from the south
            Vec3 abs = Vec3.atLowerCornerOf(helper.absolutePos(pos));
            Vec3 from = abs.add(0.5, 0.8, 2.5);
            PipeHit hit = PipeHit.pick(pipe, from, from.add(0, 0, -3));
            helper.assertTrue(hit != null && hit.segment() == segment && hit.side() == Direction.UP, "The ray should hit the upper arm");
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.moveTo(from.x, from.y - player.getEyeHeight(), from.z, 180.0f, 0.0f);
            PipeHit playerHit = PipeHit.pick(pipe, player);
            helper.assertTrue(playerHit != null && playerHit.side() == Direction.UP, "The player should look at the upper arm, eye "
                    + player.getEyePosition() + " view " + player.getViewVector(1.0f) + " hit " + playerHit);
            ((TGPipeBlock) TGBlocks.PIPE.get()).wrench(helper.getLevel(), helper.absolutePos(pos), player, false);
            helper.assertTrue(segment.mode(Direction.UP) == SideMode.OUTPUT, "One wrench use should switch neutral to output");
            ((TGPipeBlock) TGBlocks.PIPE.get()).wrench(helper.getLevel(), helper.absolutePos(pos), player, true);
            helper.assertBlockNotPresent(TGBlocks.PIPE.get(), pos);
            helper.assertTrue(player.getInventory().contains(new ItemStack(TGItems.pipe(TGPipeKinds.CABLE, 0).get())),
                    "Sneak + wrench should give the cable back");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 40)
    public static void pipeItemUpgradesInPlace(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        TGPipeBlockEntity pipe = pipe(helper, pos, TGPipeKinds.ITEM_PIPE, 0);
        pipe.setMode(TGPipeKinds.ITEM_PIPE, Direction.NORTH, SideMode.INPUT);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TGItems.pipe(TGPipeKinds.ITEM_PIPE, 2).get()));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.UP, helper.absolutePos(pos), false);
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        PipeSegment segment = pipe.segment(TGPipeKinds.ITEM_PIPE);
        helper.assertTrue(segment.tier() == 2, "The elite pipe should replace the basic one");
        helper.assertTrue(segment.mode(Direction.NORTH) == SideMode.INPUT, "The side modes should stay");
        helper.assertTrue(player.getInventory().contains(new ItemStack(TGItems.pipe(TGPipeKinds.ITEM_PIPE, 0).get())),
                "The basic pipe should come back");
        // a cable used on the item pipe goes into the same block
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TGItems.pipe(TGPipeKinds.CABLE, 0).get()));
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        helper.assertTrue(pipe.segment(TGPipeKinds.CABLE) != null, "A cable should join the item pipe's block");
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 20)
    public static void pipeSegmentsSurviveSaving(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        TGPipeBlockEntity pipe = pipe(helper, pos, TGPipeKinds.CABLE, 1);
        pipe.addSegment(TGPipeKinds.FLUID_PIPE, 2);
        pipe.setMode(TGPipeKinds.FLUID_PIPE, Direction.EAST, SideMode.INPUT);
        CompoundTag tag = pipe.saveWithFullMetadata(helper.getLevel().registryAccess());
        TGPipeBlockEntity copy = new TGPipeBlockEntity(helper.absolutePos(pos.east(3)), TGBlocks.PIPE.get().defaultBlockState());
        copy.loadWithComponents(tag, helper.getLevel().registryAccess());
        helper.assertTrue(copy.segment(TGPipeKinds.CABLE) != null && copy.segment(TGPipeKinds.CABLE).tier() == 1, "The cable should load");
        helper.assertTrue(copy.segment(TGPipeKinds.FLUID_PIPE).mode(Direction.EAST) == SideMode.INPUT, "Side modes should load");
        helper.succeed();
    }
}
