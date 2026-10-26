package techguns.gametest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredBlock;
import techguns.Techguns;
import techguns.registry.TGBlocks;

/**
 * Decorative blocks (Phase 5a): every block has an item, drops itself when mined with the right tool and is
 * mineable with the intended tool; light emitting blocks emit light 15.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGBlockTests {
    private static final String ARENA = "empty_8x4x8";

    private static List<DeferredBlock<? extends Block>> allDecorative() {
        List<DeferredBlock<? extends Block>> all = new ArrayList<>(TGBlocks.cubeBlocks());
        all.addAll(TGBlocks.stairBlocks());
        return all;
    }

    @GameTest(template = ARENA)
    public static void everyDecorativeBlockHasAnItem(GameTestHelper helper) {
        for (DeferredBlock<? extends Block> block : allDecorative()) {
            Item item = block.get().asItem();
            helper.assertTrue(item != Items.AIR, block.getId() + " has no block item");
            helper.assertTrue(BuiltInRegistries.ITEM.getKey(item).equals(block.getId()), block.getId() + " item has the wrong id");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void everyDecorativeBlockDropsItselfWhenMined(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        for (DeferredBlock<? extends Block> block : allDecorative()) {
            BlockState state = block.get().defaultBlockState();
            // hardened bug nest sand is dug with a shovel, everything else is mined with a pickaxe
            ItemStack tool = new ItemStack(block == TGBlocks.BUGNEST_SAND ? Items.IRON_SHOVEL : Items.IRON_PICKAXE);
            List<ItemStack> drops = Block.getDrops(state, level, pos, null, player, tool);
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(block.get().asItem()), block.getId() + " should drop itself, got " + drops);
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void decorativeBlocksNeedThePickaxe(GameTestHelper helper) {
        for (DeferredBlock<? extends Block> block : allDecorative()) {
            BlockState state = block.get().defaultBlockState();
            if (block == TGBlocks.BUGNEST_SAND) {
                helper.assertTrue(state.is(BlockTags.MINEABLE_WITH_SHOVEL), block.getId() + " should be mineable with a shovel");
                continue;
            }
            helper.assertTrue(state.is(BlockTags.MINEABLE_WITH_PICKAXE), block.getId() + " is not in minecraft:mineable/pickaxe");
            helper.assertTrue(state.requiresCorrectToolForDrops(), block.getId() + " should require the correct tool");
        }
        for (DeferredBlock<? extends Block> stairs : TGBlocks.stairBlocks()) {
            helper.assertTrue(stairs.get().defaultBlockState().is(BlockTags.STAIRS), stairs.getId() + " is not in minecraft:stairs");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void lightBlocksEmitLight(GameTestHelper helper) {
        for (DeferredBlock<Block> neon : TGBlocks.NEON_LIGHTS) {
            helper.assertTrue(neon.get().defaultBlockState().getLightEmission() == 15, neon.getId() + " should emit light 15");
        }
        helper.assertTrue(TGBlocks.NETHERMETAL_BORDER_LAVA.get().defaultBlockState().getLightEmission() == 15,
                "The lava pillar should emit light 15");
        helper.assertTrue(TGBlocks.NETHERMETAL_PANEL.get().defaultBlockState().getLightEmission() == 0, "Other nether metal should not glow");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void variantListsAreComplete(GameTestHelper helper) {
        helper.assertTrue(TGBlocks.METAL_PANEL.size() == 8, "metal panel has 8 variants");
        helper.assertTrue(TGBlocks.NETHER_METAL.size() == 10, "nether metal has 10 variants");
        helper.assertTrue(TGBlocks.NEON_LIGHTS.size() == 5, "neon lights has 5 variants");
        helper.assertTrue(TGBlocks.METAL_STAIRS.size() == 2 && TGBlocks.CONCRETE_STAIRS.size() == 2, "2 metal and 2 concrete stairs");
        helper.succeed();
    }
}
