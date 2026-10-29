package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGConfig;
import techguns.TGIds;
import techguns.Techguns;
import techguns.debug.DebugToolData;
import techguns.debug.DungeonScannerBlockEntity;
import techguns.debug.WorldGenTestToolItem;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;
import techguns.world.structure.DebugStructures;
import techguns.world.structure.StructureScans;
import techguns.world.structure.dungeon.DungeonDebug;
import techguns.world.structure.dungeon.DungeonPreset;
import techguns.world.structure.dungeon.SegmentType;

/** The Techguns 2 debug tools: gated by the debug config, scanning, the terrain tools and the dungeon scanner. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGDebugTests {
    private static final String ARENA = "empty_8x4x8";
    private static final String BIG = "empty_32x12x32";

    /** Runs {@code test} with the debug tools on (the config is restored afterwards). */
    private static void withDebug(Runnable test) {
        boolean before = TGConfig.DEBUG.get();
        TGConfig.DEBUG.set(true);
        try {
            test.run();
        } finally {
            TGConfig.DEBUG.set(before);
        }
    }

    private static InteractionResult useOn(GameTestHelper helper, Player player, ItemStack stack, BlockPos relative) {
        BlockPos abs = helper.absolutePos(relative);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return stack.useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false)));
    }

    @GameTest(template = ARENA)
    public static void debugToolsAreOffByDefault(GameTestHelper helper) {
        helper.assertTrue(!TGConfig.DEBUG.get(), "Debug is off by default");
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack scanTool = new ItemStack(TGItems.BUILDING_SCAN_TOOL.get());
        helper.assertTrue(useOn(helper, player, scanTool, new BlockPos(1, 0, 1)) == InteractionResult.FAIL, "Without debug the tools do nothing");
        helper.assertTrue(DebugToolData.corner(scanTool).isEmpty(), "No corner was set");
        helper.succeed();
    }

    /** Building scan tool: two corners save the box as a template; markers follow the Techguns 2 converter. */
    @GameTest(template = ARENA)
    public static void buildingScanToolSavesTemplates(GameTestHelper helper) {
        withDebug(() -> {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
            helper.setBlock(new BlockPos(2, 1, 1), TGBlocks.AIR_MARKER.get());
            helper.setBlock(new BlockPos(3, 1, 1), TGBlocks.ANTI_AIR_MARKER.get());
            ItemStack tool = new ItemStack(TGItems.BUILDING_SCAN_TOOL.get());
            tool.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Debug Test Scan"));
            useOn(helper, player, tool, new BlockPos(1, 1, 1));
            helper.assertTrue(DebugToolData.corner(tool).isPresent(), "First corner set");
            useOn(helper, player, tool, new BlockPos(3, 1, 1));
            helper.assertTrue(DebugToolData.corner(tool).isEmpty(), "The second corner runs the scan");
            ResourceLocation id = TGIds.id("scans/debug_test_scan");
            var template = helper.getLevel().getStructureManager().get(id);
            helper.assertTrue(template.isPresent() && template.get().getSize().equals(new Vec3i(3, 1, 1)), "Template " + id + " of 3 x 1 x 1 saved");

            // placed elsewhere: stone, air for the air marker, the anti-air marker's place keeps its block
            BlockPos at = new BlockPos(1, 2, 4);
            helper.setBlock(at.east(), Blocks.DIRT);
            helper.setBlock(at.east(2), Blocks.DIRT);
            StructureScans.place(helper.getLevel(), helper.absolutePos(at), id);
            helper.assertBlockPresent(Blocks.STONE, at);
            helper.assertBlockPresent(Blocks.AIR, at.east());
            helper.assertBlockPresent(Blocks.DIRT, at.east(2));
        });
        helper.succeed();
    }

    /** World gen test tool: sneak + use cycles the modes; flatten squeezes the ground, remove junk clears it. */
    @GameTest(template = BIG)
    public static void worldGenToolTerrainModes(GameTestHelper helper) {
        withDebug(() -> {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            ItemStack tool = new ItemStack(TGItems.WORLDGEN_TEST_TOOL.get());
            player.setShiftKeyDown(true);
            useOn(helper, player, tool, new BlockPos(1, 0, 1));
            helper.assertTrue(WorldGenTestToolItem.mode(tool) == WorldGenTestToolItem.Mode.CITY, "Sneak + use: next mode");
            player.setShiftKeyDown(false);

            var level = helper.getLevel();
            BlockPos min = helper.absolutePos(new BlockPos(1, 0, 1));
            // the terrain tools read the heightmaps: no barrier roof of the test arena over the area
            for (BlockPos pos : BlockPos.betweenClosed(min.offset(-3, 1, -3), min.offset(9, 16, 9))) {
                if (level.getBlockState(pos).is(Blocks.BARRIER)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                }
            }
            // a column of dirt 3 high next to flat ground
            for (int x = 0; x < 6; x++) {
                for (int z = 0; z < 6; z++) {
                    helper.setBlock(new BlockPos(1 + x, 0, 1 + z), Blocks.DIRT);
                }
            }
            for (int y = 1; y <= 3; y++) {
                helper.setBlock(new BlockPos(1, y, 1), Blocks.DIRT);
            }
            int before = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, min.getX(), min.getZ()) - 1 - min.getY();
            int beforeFlat = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, min.getX() + 3, min.getZ() + 3) - 1 - min.getY();
            DebugStructures.flatten(level, min.getX(), min.getZ(), 6, 6);
            int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, min.getX(), min.getZ()) - 1;
            helper.assertTrue(top - min.getY() <= 2, "Flatten cuts the column to the height span of 2, top at " + (top - min.getY()) + " before " + before + " flat " + beforeFlat);

            // Techguns 2 flattening puts the median ground one lower: new ground for the junk test
            helper.setBlock(new BlockPos(3, 0, 3), Blocks.DIRT);
            helper.setBlock(new BlockPos(3, 1, 3), Blocks.OAK_PLANKS);
            helper.setBlock(new BlockPos(3, 2, 3), Blocks.OAK_LEAVES);
            DebugStructures.removeJunk(level, min.getX(), min.getZ(), 6, 6);
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 1, 3));
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 2, 3));
            helper.assertBlockPresent(Blocks.DIRT, new BlockPos(3, 0, 3));
        });
        helper.succeed();
    }

    /** Dungeon scanner: scan saves all 16 segments, clear empties the grid, load brings them back, copy fills the others. */
    @GameTest(template = BIG, timeoutTicks = 200)
    public static void dungeonScannerRoundTrip(GameTestHelper helper) {
        withDebug(() -> {
            var level = helper.getLevel();
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            BlockPos scanner = new BlockPos(1, 1, 1);
            helper.setBlock(scanner, TGBlocks.DUNGEON_SCANNER.get());
            DungeonScannerBlockEntity tool = (DungeonScannerBlockEntity) helper.getBlockEntity(scanner);
            Vec3 at = helper.absoluteVec(Vec3.atCenterOf(scanner));
            player.moveTo(at.x, at.y, at.z);
            for (int i = 0; i < 3; i++) {
                tool.pressButton(player, DungeonScannerBlockEntity.BUTTON_SIZE_XZ_MINUS);
            }
            tool.pressButton(player, DungeonScannerBlockEntity.BUTTON_SIZE_Y_MINUS);
            helper.assertTrue(tool.sizeXZ() == 2 && tool.sizeY() == 3, "Segment size 2 x 3");
            BlockPos absScanner = helper.absolutePos(scanner);
            BlockPos straight = DungeonDebug.segmentOrigin(absScanner, SegmentType.STRAIGHT, 2);
            BlockPos curve = DungeonDebug.segmentOrigin(absScanner, SegmentType.CURVE, 2);
            level.setBlockAndUpdate(straight, Blocks.GOLD_BLOCK.defaultBlockState());

            helper.assertTrue(DungeonDebug.scan(level, absScanner, 2, 3, "debug_test") == SegmentType.values().length, "All segments saved");
            helper.assertTrue(DungeonDebug.templateNames(level).contains("debug_test"), "The template is listed");
            tool.pressButton(player, DungeonScannerBlockEntity.BUTTON_CLEAR);
            helper.assertTrue(level.getBlockState(straight).isAir(), "Clear empties the grid");
            helper.assertTrue(DungeonDebug.load(level, absScanner, 2, "debug_test") == SegmentType.values().length, "All segments placed");
            helper.assertTrue(level.getBlockState(straight).is(Blocks.GOLD_BLOCK), "Load brings the segment back");
            tool.pressButton(player, DungeonScannerBlockEntity.BUTTON_COPY_FIRST);
            helper.assertTrue(level.getBlockState(curve).is(Blocks.GOLD_BLOCK), "Copy puts the straight segment into the others");
        });
        helper.succeed();
    }

    /** World gen test tool's castle: a maze dungeon of the castle templates in the selected box. */
    @GameTest(template = BIG, timeoutTicks = 200)
    public static void worldGenToolBuildsCastle(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos min = helper.absolutePos(new BlockPos(1, 1, 1));
        DungeonDebug.generate(level, min, 25, 10, 25, DungeonPreset.CASTLE, level.random);
        int blocks = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, min.offset(24, 9, 24))) {
            if (!level.getBlockState(pos).isAir()) {
                blocks++;
            }
        }
        helper.assertTrue(blocks > 500, "The castle is built, " + blocks + " blocks");
        helper.succeed();
    }
}
