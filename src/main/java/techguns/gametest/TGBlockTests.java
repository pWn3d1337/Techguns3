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
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import techguns.block.CamoNetBlock;
import techguns.block.Door3x3Block;
import techguns.block.Door3x3BlockEntity;
import techguns.compat.team.TGTeams;
import techguns.machine.RedstoneMode;
import net.minecraft.server.level.ServerPlayer;
import techguns.block.MilitaryCrateBlock;
import techguns.block.SandbagsBlock;
import techguns.block.TGLampBlock;
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

    // --- Phase 5a2: shaped decorative blocks ---

    private static List<DeferredBlock<? extends Block>> allShaped() {
        List<DeferredBlock<? extends Block>> all = new ArrayList<>();
        all.addAll(TGBlocks.LAMPS);
        all.addAll(TGBlocks.LANTERNS);
        all.addAll(TGBlocks.LADDERS);
        all.add(TGBlocks.SLIMY_LADDER);
        all.addAll(TGBlocks.CAMO_NETS);
        all.addAll(TGBlocks.CAMO_NETS_TOP);
        all.add(TGBlocks.SANDBAGS);
        all.addAll(TGBlocks.MILITARY_CRATES);
        all.add(TGBlocks.BUNKER_DOOR);
        all.addAll(TGBlocks.DOORS_3X3);
        return all;
    }

    @GameTest(template = ARENA)
    public static void shapedVariantListsAreComplete(GameTestHelper helper) {
        helper.assertTrue(TGBlocks.LAMPS.size() == 2 && TGBlocks.LANTERNS.size() == 2, "2 lamps and 2 lanterns");
        helper.assertTrue(TGBlocks.LADDERS.size() == 4, "4 ladders");
        helper.assertTrue(TGBlocks.CAMO_NETS.size() == 3 && TGBlocks.CAMO_NETS_TOP.size() == 3, "3 camo nets and 3 top nets");
        helper.assertTrue(TGBlocks.MILITARY_CRATES.size() == 9, "9 military crates");
        helper.assertTrue(TGBlocks.DOORS_3X3.size() == 4, "4 3x3 doors");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void everyShapedBlockHasAnItem(GameTestHelper helper) {
        for (DeferredBlock<? extends Block> block : allShaped()) {
            Item item = block.get().asItem();
            helper.assertTrue(item != Items.AIR, block.getId() + " has no block item");
            helper.assertTrue(BuiltInRegistries.ITEM.getKey(item).equals(block.getId()), block.getId() + " item has the wrong id");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void shapedBlocksDropThemselves(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        List<DeferredBlock<? extends Block>> plain = new ArrayList<>();
        plain.addAll(TGBlocks.LAMPS);
        plain.addAll(TGBlocks.LANTERNS);
        plain.addAll(TGBlocks.LADDERS);
        plain.add(TGBlocks.SLIMY_LADDER);
        plain.addAll(TGBlocks.CAMO_NETS);
        plain.addAll(TGBlocks.CAMO_NETS_TOP);
        plain.add(TGBlocks.SANDBAGS);
        plain.add(TGBlocks.BUNKER_DOOR);
        for (DeferredBlock<? extends Block> block : plain) {
            List<ItemStack> drops = Block.getDrops(block.get().defaultBlockState(), level, pos, null, player, pickaxe);
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(block.get().asItem()), block.getId() + " should drop itself, got " + drops);
        }
        // a door drops one item, from its centre block only
        for (DeferredBlock<Door3x3Block> door : TGBlocks.DOORS_3X3) {
            BlockState master = door.get().partState(false, 1, 1);
            List<ItemStack> drops = Block.getDrops(master, level, pos, null, player, ItemStack.EMPTY);
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(door.get().asItem()), door.getId() + " centre should drop the door");
            for (int col = 0; col < 3; col++) {
                for (int row = 0; row < 3; row++) {
                    if (col != 1 || row != 1) {
                        helper.assertTrue(Block.getDrops(door.get().partState(false, col, row), level, pos, null, player, ItemStack.EMPTY).isEmpty(),
                                door.getId() + " part " + col + "/" + row + " should drop nothing by itself");
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void lampsAndLanternsEmitLight(GameTestHelper helper) {
        for (DeferredBlock<? extends Block> block : TGBlocks.LAMPS) {
            helper.assertTrue(block.get().defaultBlockState().getLightEmission() == 15, block.getId() + " should emit light 15");
        }
        for (DeferredBlock<? extends Block> block : TGBlocks.LANTERNS) {
            helper.assertTrue(block.get().defaultBlockState().getLightEmission() == 15, block.getId() + " should emit light 15");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void laddersAreClimbable(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        List<DeferredBlock<? extends Block>> ladders = new ArrayList<>(TGBlocks.LADDERS);
        ladders.add(TGBlocks.SLIMY_LADDER);
        for (DeferredBlock<? extends Block> ladder : ladders) {
            BlockState state = ladder.get().defaultBlockState();
            helper.assertTrue(state.is(BlockTags.CLIMBABLE), ladder.getId() + " is not in minecraft:climbable");
            helper.assertTrue(state.isLadder(helper.getLevel(), pos, player), ladder.getId() + " should be climbable");
        }
        // Techguns 2 ladders are 2/16 thick and need no wall; the slimy ladder is a vanilla ladder
        BlockState south = TGBlocks.LADDER_METAL.get().defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
        helper.assertTrue(Math.abs(south.getShape(helper.getLevel(), pos).max(Direction.Axis.Z) - 0.125) < 1.0e-6, "A south ladder is 2/16 thick");
        helper.setBlock(new BlockPos(2, 1, 2), south);
        helper.assertBlock(new BlockPos(2, 1, 2), block -> block == TGBlocks.LADDER_METAL.get(), () -> "A ladder without a wall should stay");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void lampsNeedASupport(GameTestHelper helper) {
        BlockPos floor = new BlockPos(2, 1, 2);
        BlockPos lampPos = new BlockPos(2, 2, 2);
        helper.setBlock(floor, Blocks.STONE);
        helper.setBlock(lampPos, TGBlocks.LAMP_YELLOW.get().defaultBlockState().setValue(TGLampBlock.FACING, Direction.DOWN));
        helper.assertBlock(lampPos, block -> block == TGBlocks.LAMP_YELLOW.get(), () -> "A lamp on stone should stay");
        helper.assertTrue(!TGBlocks.LAMP_YELLOW.get().defaultBlockState().setValue(TGLampBlock.FACING, Direction.NORTH)
                .canSurvive(helper.getLevel(), helper.absolutePos(lampPos)), "There is no support north of the lamp");
        helper.setBlock(floor, Blocks.AIR);
        helper.assertBlock(lampPos, block -> block == Blocks.AIR, () -> "A lamp pops off when its support is removed");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void lanternHangsFromAnySide(GameTestHelper helper) {
        BlockPos lanternPos = new BlockPos(2, 2, 2);
        helper.setBlock(new BlockPos(2, 3, 2), Blocks.STONE);
        // placed by hand, as if hanging from the ceiling
        helper.setBlock(lanternPos, TGBlocks.LAMP_WHITE_LANTERN.get().defaultBlockState().setValue(BlockStateProperties.UP, true));
        helper.assertBlock(lanternPos, block -> block == TGBlocks.LAMP_WHITE_LANTERN.get(), () -> "A lantern under a ceiling should stay");
        helper.setBlock(new BlockPos(2, 3, 2), Blocks.AIR);
        helper.assertBlock(lanternPos, block -> block == Blocks.AIR, () -> "A lantern without any support pops off");
        helper.succeed();
    }

    /** Right-click the top face of the block at {@code clicked} with {@code stack}. */
    private static InteractionResult useOnTop(GameTestHelper helper, Player player, ItemStack stack, BlockPos clicked) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos abs = helper.absolutePos(clicked);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
        return stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    @GameTest(template = ARENA)
    public static void camoNetsConnectToEachOther(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int x = 1; x <= 3; x++) {
            helper.setBlock(new BlockPos(x, 0, 2), Blocks.STONE);
        }
        useOnTop(helper, player, new ItemStack(TGBlocks.CAMO_NET_WOOD.get()), new BlockPos(1, 0, 2));
        useOnTop(helper, player, new ItemStack(TGBlocks.CAMO_NET_SNOW.get()), new BlockPos(2, 0, 2));
        useOnTop(helper, player, new ItemStack(TGBlocks.CAMO_NET_DESERT.get()), new BlockPos(3, 0, 2));
        BlockState first = helper.getBlockState(new BlockPos(1, 1, 2));
        BlockState middle = helper.getBlockState(new BlockPos(2, 1, 2));
        helper.assertTrue(first.getBlock() == TGBlocks.CAMO_NET_WOOD.get() && first.getValue(CamoNetBlock.EAST) && !first.getValue(CamoNetBlock.WEST),
                "The first net connects east");
        helper.assertTrue(middle.getValue(CamoNetBlock.EAST) && middle.getValue(CamoNetBlock.WEST) && !middle.getValue(CamoNetBlock.NORTH),
                "Nets of different types connect east and west");
        BlockPos firstPos = helper.absolutePos(new BlockPos(1, 1, 2));
        helper.assertTrue(first.getCollisionShape(helper.getLevel(), firstPos).max(Direction.Axis.X) == 1.0,
                "A connected net has a collision arm to the block edge");
        helper.assertTrue(TGBlocks.CAMO_NET_WOOD.get().defaultBlockState().getCollisionShape(helper.getLevel(), firstPos).max(Direction.Axis.X) < 0.6,
                "A lone net is a thin post");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void topNetsAndSandbagsConnect(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int x = 1; x <= 2; x++) {
            helper.setBlock(new BlockPos(x, 0, 2), Blocks.STONE);
        }
        useOnTop(helper, player, new ItemStack(TGBlocks.CAMO_NET_TOP_WOOD.get()), new BlockPos(1, 0, 2));
        useOnTop(helper, player, new ItemStack(TGBlocks.CAMO_NET_TOP_SNOW.get()), new BlockPos(2, 0, 2));
        helper.assertTrue(helper.getBlockState(new BlockPos(1, 1, 2)).getValue(CamoNetBlock.EAST), "Top nets connect");
        helper.assertTrue(helper.getBlockState(new BlockPos(2, 1, 2)).getValue(CamoNetBlock.WEST), "Top nets of different types connect");

        for (int x = 1; x <= 2; x++) {
            helper.setBlock(new BlockPos(x, 0, 5), Blocks.STONE);
        }
        useOnTop(helper, player, new ItemStack(TGBlocks.SANDBAGS.get()), new BlockPos(1, 0, 5));
        useOnTop(helper, player, new ItemStack(TGBlocks.SANDBAGS.get()), new BlockPos(2, 0, 5));
        BlockState bags = helper.getBlockState(new BlockPos(1, 1, 5));
        helper.assertTrue(bags.getBlock() == TGBlocks.SANDBAGS.get() && bags.getValue(SandbagsBlock.EAST) && !bags.getValue(SandbagsBlock.WEST),
                "Sandbags connect to each other");
        helper.setBlock(new BlockPos(0, 1, 5), Blocks.STONE);
        helper.assertTrue(helper.getBlockState(new BlockPos(1, 1, 5)).getValue(SandbagsBlock.WEST), "Sandbags connect to a solid block");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void militaryCratesDropLootNotThemselves(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        ItemStack silk = new ItemStack(Items.IRON_AXE);
        silk.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        for (DeferredBlock<MilitaryCrateBlock> crate : TGBlocks.MILITARY_CRATES) {
            BlockState state = crate.get().defaultBlockState();
            List<ItemStack> loot = Block.getDrops(state, level, pos, null, player, axe);
            helper.assertTrue(!loot.isEmpty(), crate.getId() + " should drop loot");
            helper.assertTrue(loot.stream().noneMatch(stack -> stack.is(crate.get().asItem())), crate.getId() + " should not drop itself, got " + loot);
            List<ItemStack> silked = Block.getDrops(state, level, pos, null, player, silk);
            helper.assertTrue(silked.size() == 1 && silked.get(0).is(crate.get().asItem()), crate.getId() + " should drop itself with Silk Touch");
        }
        helper.succeed();
    }

    // --- 3x3 doors ---

    /** Places a door by hand (without the item): nine parts around {@code master}. */
    private static void placeDoor(GameTestHelper helper, DeferredBlock<Door3x3Block> door, BlockPos master, boolean zplane) {
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                helper.setBlock(Door3x3Block.partPos(master, zplane, col, row), door.get().partState(zplane, col, row));
            }
        }
    }

    /** The door item stands a door on a floor (Techguns 2) or hangs it under a ceiling; side faces place nothing. */
    @GameTest(template = "empty_32x12x32")
    public static void doorItemPlacesOnFloorAndCeiling(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos floor = new BlockPos(8, 1, 8);
        BlockPos ceiling = new BlockPos(16, 6, 8);
        helper.setBlock(floor, Blocks.STONE);
        helper.setBlock(ceiling, Blocks.STONE);
        helper.assertTrue(useDoorItem(helper, player, TGBlocks.DOOR_HANGAR_UP_3X3, floor, Direction.UP).consumesAction(), "Placed on a floor");
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_HANGAR_UP_3X3, floor.above(2), false, false), "The door stands on the floor");
        helper.assertTrue(useDoorItem(helper, player, TGBlocks.DOOR_HANGAR_UP_3X3, ceiling, Direction.DOWN).consumesAction(), "Placed under a ceiling");
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_HANGAR_UP_3X3, ceiling.below(2), false, false), "The door hangs below the ceiling");
        helper.assertTrue(!useDoorItem(helper, player, TGBlocks.DOOR_HANGAR_UP_3X3, ceiling, Direction.EAST).consumesAction(), "Side faces place nothing");
        helper.succeed();
    }

    private static InteractionResult useDoorItem(GameTestHelper helper, Player player, DeferredBlock<Door3x3Block> door, BlockPos relative, Direction face) {
        ItemStack stack = new ItemStack(door.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos abs = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).relative(face, 0.5), face, abs, false);
        return stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private static boolean doorIs(GameTestHelper helper, DeferredBlock<Door3x3Block> door, BlockPos master, boolean zplane, boolean open) {
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                BlockState state = helper.getBlockState(Door3x3Block.partPos(master, zplane, col, row));
                if (state.getBlock() != door.get() || state.getValue(Door3x3Block.OPEN) != open) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockHitResult hit(GameTestHelper helper, BlockPos relative) {
        BlockPos abs = helper.absolutePos(relative);
        return new BlockHitResult(Vec3.atCenterOf(abs), Direction.EAST, abs, false);
    }

    /** Uses the block at the relative position like a right-click with an empty hand. */
    private static void use(GameTestHelper helper, Player player, BlockPos relative) {
        helper.getBlockState(relative).useWithoutItem(helper.getLevel(), player, hit(helper, relative));
    }

    @GameTest(template = ARENA)
    public static void doorItemPlacesNineBlocks(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(new Vec3(0.5, 1, 4.5));
        player.moveTo(at.x, at.y, at.z, -90.0f, 0.0f); // looking east, so the door plane is the Z axis
        helper.setBlock(new BlockPos(3, 0, 4), Blocks.STONE);
        ItemStack stack = new ItemStack(TGBlocks.DOOR_METAL_3X3.get());
        InteractionResult result = useOnTop(helper, player, stack, new BlockPos(3, 0, 4));
        helper.assertTrue(result.consumesAction(), "Placing the door should work");
        helper.assertTrue(stack.isEmpty(), "Placing consumes the item");
        BlockPos master = new BlockPos(3, 2, 4);
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_METAL_3X3, master, true, false), "The door should stand closed over 3 blocks along Z and 3 high");
        helper.assertTrue(Door3x3Block.isMaster(helper.getBlockState(master)), "The centre block is the master");
        helper.assertTrue(helper.getBlockState(new BlockPos(3, 1, 3)).getValue(Door3x3Block.COL) == 0
                && helper.getBlockState(new BlockPos(3, 1, 5)).getValue(Door3x3Block.COL) == 2, "Columns grow along the plane (south)");
        // no room: a second door in the same place is refused and keeps the item
        ItemStack second = new ItemStack(TGBlocks.DOOR_HANGAR_UP_3X3.get());
        useOnTop(helper, player, second, new BlockPos(3, 0, 4));
        helper.assertTrue(second.getCount() == 1, "A door that does not fit is not placed");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void doorOpensAndClosesByUse(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos master = new BlockPos(3, 2, 4);
        placeDoor(helper, TGBlocks.DOOR_METAL_3X3, master, true);
        BlockPos corner = Door3x3Block.partPos(master, true, 0, 0);
        BlockPos middle = Door3x3Block.partPos(master, true, 1, 1);
        BlockPos absMiddle = helper.absolutePos(middle);
        helper.assertTrue(!helper.getBlockState(middle).getCollisionShape(helper.getLevel(), absMiddle).isEmpty(), "A closed door collides");

        use(helper, player, corner);
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_METAL_3X3, master, true, true), "Using any part opens the whole door");
        helper.assertTrue(helper.getBlockState(middle).getCollisionShape(helper.getLevel(), absMiddle).isEmpty(), "An open door has no collision");
        helper.assertTrue(helper.getBlockState(middle).getShape(helper.getLevel(), absMiddle).isEmpty(), "The open tech door has no centre outline");
        helper.assertTrue(!helper.getBlockState(corner).getShape(helper.getLevel(), helper.absolutePos(corner)).isEmpty(),
                "The open tech door keeps its side strips");

        use(helper, player, middle);
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_METAL_3X3, master, true, false), "Using it again closes the door");
        helper.assertTrue(!helper.getBlockState(middle).getCollisionShape(helper.getLevel(), absMiddle).isEmpty(), "A closing door collides at once");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void openDoorKeepsWaterOut(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos master = new BlockPos(3, 2, 4);
        placeDoor(helper, TGBlocks.DOOR_METAL_3X3, master, true);
        use(helper, player, master);
        helper.setBlock(new BlockPos(2, 1, 4), Blocks.WATER);
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(doorIs(helper, TGBlocks.DOOR_METAL_3X3, master, true, true), "Water must not wash an open door away");
            helper.succeed();
        });
    }

    private static boolean moving(GameTestHelper helper, BlockPos master) {
        return helper.getBlockState(master).getValue(Door3x3Block.MOVING);
    }

    private static Door3x3BlockEntity doorEntity(GameTestHelper helper, BlockPos master) {
        if (helper.getBlockEntity(master) instanceof Door3x3BlockEntity door) {
            return door;
        }
        throw new net.minecraft.gametest.framework.GameTestAssertException("No door block entity at the master " + master);
    }

    /** 1.12 OPENING / OPENED / CLOSING / CLOSED: the door moves for a second, turns around when used mid-way, closes on its own. */
    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void doorMovesBeforeItIsOpen(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos master = new BlockPos(3, 2, 4);
        placeDoor(helper, TGBlocks.DOOR_HANGAR_DOWN_3X3, master, false);
        helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(Door3x3Block.partPos(master, false, 0, 0))) == null,
                "Only the master has a block entity");
        Door3x3BlockEntity door = doorEntity(helper, master);
        use(helper, player, master);
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_HANGAR_DOWN_3X3, master, false, true) && moving(helper, master), "The door starts opening");
        helper.assertTrue(door.openness(0.0f) == 0.0f, "An opening door starts closed");
        int quarter = Door3x3Block.MOVE_TICKS / 4;
        helper.runAfterDelay(quarter, () -> {
            helper.assertTrue(Math.abs(door.openness(0.0f) - 0.25f) < 0.06f, "A quarter of the way open, got " + door.openness(0.0f));
            use(helper, player, master);
            helper.assertTrue(doorIs(helper, TGBlocks.DOOR_HANGAR_DOWN_3X3, master, false, false) && moving(helper, master), "Used mid-way it turns around");
            helper.assertTrue(Math.abs(door.openness(0.0f) - 0.25f) < 0.06f, "Turning around keeps the door where it is, got " + door.openness(0.0f));
        });
        // closed a quarter of a movement later; open it again and wait for the auto close
        int closed = quarter * 2 + 2;
        helper.runAfterDelay(closed, () -> {
            helper.assertTrue(doorIs(helper, TGBlocks.DOOR_HANGAR_DOWN_3X3, master, false, false) && !moving(helper, master), "The door has closed");
            use(helper, player, master);
        });
        int open = closed + Door3x3Block.MOVE_TICKS + 2;
        helper.runAfterDelay(open, () -> helper.assertTrue(doorIs(helper, TGBlocks.DOOR_HANGAR_DOWN_3X3, master, false, true) && !moving(helper, master),
                "The door is fully open after " + Door3x3Block.MOVE_TICKS + " ticks"));
        helper.runAfterDelay(open + Door3x3Block.AUTO_CLOSE_TICKS - 6, () -> helper.assertTrue(
                doorIs(helper, TGBlocks.DOOR_HANGAR_DOWN_3X3, master, false, true), "The auto close counts from fully open"));
        helper.runAfterDelay(open + Door3x3Block.AUTO_CLOSE_TICKS + 2, () -> {
            helper.assertTrue(doorIs(helper, TGBlocks.DOOR_HANGAR_DOWN_3X3, master, false, false) && moving(helper, master),
                    "An unpowered open door closes on its own");
            helper.succeed();
        });
    }

    /** A mock player (not in the level) next to the door, in range of its settings. */
    private static Player playerNear(GameTestHelper helper, BlockPos master) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(master).add(2.0, -1.0, 0.0));
        player.moveTo(at.x, at.y, at.z);
        return player;
    }

    /** Techguns 2 door modes: only redstone doors follow redstone, as their redstone behaviour says. */
    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void doorFollowsRedstone(GameTestHelper helper) {
        BlockPos master = new BlockPos(3, 2, 4);
        placeDoor(helper, TGBlocks.DOOR_NETHER_3X3, master, true);
        Door3x3BlockEntity door = doorEntity(helper, master);
        Player player = playerNear(helper, master);
        BlockPos power = new BlockPos(3, 1, 6); // next to the south column, bottom row
        helper.setBlock(power, Blocks.REDSTONE_BLOCK);
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_NETHER_3X3, master, true, false), "A manual door ignores redstone");
        helper.assertTrue(door.pressButton(player, Door3x3BlockEntity.BUTTON_MODE) && door.getMode() == Door3x3BlockEntity.Mode.REDSTONE,
                "The mode button switches to redstone");
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_NETHER_3X3, master, true, false), "Redstone behaviour 'ignore' leaves the door as it is");
        door.pressButton(player, Door3x3BlockEntity.BUTTON_REDSTONE);
        helper.assertTrue(door.getRedstoneMode() == RedstoneMode.HIGH, "Redstone behaviour high");
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_NETHER_3X3, master, true, true), "A redstone signal at any part opens the door");
        // no auto close for redstone doors
        helper.runAfterDelay(Door3x3Block.MOVE_TICKS + Door3x3Block.AUTO_CLOSE_TICKS + 5, () -> {
            helper.assertTrue(doorIs(helper, TGBlocks.DOOR_NETHER_3X3, master, true, true), "A powered door stays open");
            helper.setBlock(power, Blocks.AIR);
            helper.assertTrue(doorIs(helper, TGBlocks.DOOR_NETHER_3X3, master, true, false), "Removing the signal closes the door");
            door.pressButton(player, Door3x3BlockEntity.BUTTON_REDSTONE);
            helper.assertTrue(doorIs(helper, TGBlocks.DOOR_NETHER_3X3, master, true, true), "Redstone behaviour low opens it without a signal");
            helper.succeed();
        });
    }

    /** Techguns 2 player detector: opens for players near it, closes a second after they left. */
    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void doorPlayerDetector(GameTestHelper helper) {
        BlockPos master = new BlockPos(3, 2, 4);
        placeDoor(helper, TGBlocks.DOOR_METAL_3X3, master, true);
        Door3x3BlockEntity door = doorEntity(helper, master);
        Player setter = playerNear(helper, master);
        door.pressButton(setter, Door3x3BlockEntity.BUTTON_MODE);
        door.pressButton(setter, Door3x3BlockEntity.BUTTON_MODE);
        helper.assertTrue(door.getMode() == Door3x3BlockEntity.Mode.PLAYER_DETECTOR, "Third mode: player detector");
        // a plain mock player in the level (a mock server player would join like a client)
        Player walker = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 far = helper.absoluteVec(new Vec3(0.5, 1, 0.5)).add(0, 0, -20);
        Vec3 near = helper.absoluteVec(Vec3.atBottomCenterOf(master).add(1.5, -1.0, 0.0));
        walker.moveTo(far.x, far.y, far.z);
        helper.getLevel().addFreshEntity(walker);
        helper.runAfterDelay(Door3x3BlockEntity.DETECTOR_INTERVAL * 2, () -> {
            helper.assertTrue(doorIs(helper, TGBlocks.DOOR_METAL_3X3, master, true, false), "No player near: closed");
            walker.moveTo(near.x, near.y, near.z);
        });
        int opened = Door3x3BlockEntity.DETECTOR_INTERVAL * 4;
        helper.runAfterDelay(opened, () -> {
            helper.assertTrue(doorIs(helper, TGBlocks.DOOR_METAL_3X3, master, true, true), "A player near opens the door");
            walker.moveTo(far.x, far.y, far.z);
        });
        helper.runAfterDelay(opened + Door3x3Block.MOVE_TICKS + Door3x3BlockEntity.DETECTOR_INTERVAL, () -> helper.assertTrue(
                doorIs(helper, TGBlocks.DOOR_METAL_3X3, master, true, true), "The door stays open a second after the player left"));
        helper.runAfterDelay(opened + 3 * Door3x3Block.MOVE_TICKS + Door3x3BlockEntity.DETECTOR_INTERVAL, () -> {
            helper.assertTrue(doorIs(helper, TGBlocks.DOOR_METAL_3X3, master, true, false), "Then it closes");
            walker.discard();
            helper.succeed();
        });
    }

    /** Owner and security like the machines: an owner-only door keeps others out of it and its settings. */
    @GameTest(template = ARENA)
    public static void doorSecurityKeepsOthersOut(GameTestHelper helper) {
        BlockPos master = new BlockPos(3, 2, 4);
        placeDoor(helper, TGBlocks.DOOR_METAL_3X3, master, true);
        Door3x3BlockEntity door = doorEntity(helper, master);
        Player owner = playerNear(helper, master);
        Player other = playerNear(helper, master);
        door.setOwner(owner);
        helper.assertTrue(!door.pressButton(other, Door3x3BlockEntity.BUTTON_SECURITY), "Only the owner changes the security");
        door.pressButton(owner, Door3x3BlockEntity.BUTTON_SECURITY);
        if (door.getSecurity() != TGTeams.SECURITY_OWNER) {
            // with a team mod the levels go public, allies, team, owner
            door.pressButton(owner, Door3x3BlockEntity.BUTTON_SECURITY);
            door.pressButton(owner, Door3x3BlockEntity.BUTTON_SECURITY);
        }
        helper.assertTrue(door.getSecurity() == TGTeams.SECURITY_OWNER, "Owner only");
        use(helper, other, master);
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_METAL_3X3, master, true, false), "Others can't open an owner-only door");
        helper.assertTrue(!door.pressButton(other, Door3x3BlockEntity.BUTTON_MODE), "Others can't change its settings");
        use(helper, owner, master);
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_METAL_3X3, master, true, true), "The owner opens it");
        helper.succeed();
    }

    /**
     * Techguns 2: sneak + use with a wrench (the crowbar) is the door screen's (the event is taken before the block's
     * use, a fake player gets no screen), without sneaking the wrench opens the door like a hand.
     */
    @GameTest(template = ARENA)
    public static void wrenchOpensDoorSettings(GameTestHelper helper) {
        BlockPos master = new BlockPos(3, 2, 4);
        placeDoor(helper, TGBlocks.DOOR_METAL_3X3, master, true);
        ServerPlayer player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        Vec3 near = helper.absoluteVec(Vec3.atBottomCenterOf(master).add(2.0, -1.0, 0.0));
        player.moveTo(near.x, near.y, near.z);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(techguns.registry.TGItems.CROWBAR.get()));
        BlockPos corner = helper.absolutePos(Door3x3Block.partPos(master, true, 0, 0));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(corner), Direction.EAST, corner, false);
        player.setShiftKeyDown(true);
        var event = net.neoforged.neoforge.common.CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, corner, hit);
        helper.assertTrue(event.isCanceled() && event.getCancellationResult().consumesAction(), "Sneak + wrench is taken for the door screen");
        player.setShiftKeyDown(false);
        event = net.neoforged.neoforge.common.CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, corner, hit);
        helper.assertTrue(!event.isCanceled(), "Without sneaking the wrench uses the door");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void hangarDoorsOpenTogether(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos first = new BlockPos(3, 2, 1);
        BlockPos second = new BlockPos(3, 2, 4);
        placeDoor(helper, TGBlocks.DOOR_HANGAR_UP_3X3, first, true);
        placeDoor(helper, TGBlocks.DOOR_HANGAR_UP_3X3, second, true);
        use(helper, player, first);
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_HANGAR_UP_3X3, first, true, true), "The used hangar door opens");
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_HANGAR_UP_3X3, second, true, true), "The hangar door next to it opens as well");
        // hangar up: only the top row keeps an outline, nothing collides
        BlockState bottom = helper.getBlockState(Door3x3Block.partPos(first, true, 1, 0));
        helper.assertTrue(bottom.getShape(helper.getLevel(), helper.absolutePos(first)).isEmpty(), "The retracted hangar door leaves the bottom free");
        helper.assertTrue(!helper.getBlockState(Door3x3Block.partPos(first, true, 1, 2)).getShape(helper.getLevel(), helper.absolutePos(first)).isEmpty(),
                "The retracted hangar door keeps its top frame");

        // a tech door does not pull its neighbours along
        BlockPos metalA = new BlockPos(6, 2, 1);
        BlockPos metalB = new BlockPos(6, 2, 4);
        placeDoor(helper, TGBlocks.DOOR_METAL_3X3, metalA, true);
        placeDoor(helper, TGBlocks.DOOR_METAL_3X3, metalB, true);
        use(helper, player, metalA);
        helper.assertTrue(doorIs(helper, TGBlocks.DOOR_METAL_3X3, metalA, true, true) && doorIs(helper, TGBlocks.DOOR_METAL_3X3, metalB, true, false),
                "Only hangar doors open together");
        helper.succeed();
    }

    private static int doorItems(GameTestHelper helper, DeferredBlock<Door3x3Block> door) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(2)).stream()
                .filter(entity -> entity.getItem().is(door.get().asItem())).mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private static int doorParts(GameTestHelper helper, DeferredBlock<Door3x3Block> door, BlockPos master, boolean zplane) {
        int parts = 0;
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                if (helper.getBlockState(Door3x3Block.partPos(master, zplane, col, row)).getBlock() == door.get()) {
                    parts++;
                }
            }
        }
        return parts;
    }

    @GameTest(template = ARENA)
    public static void doorBreaksAsAWhole(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos master = new BlockPos(3, 2, 4);

        // a player mining a side part: the whole door goes away and exactly one door item drops
        placeDoor(helper, TGBlocks.DOOR_HANGAR_DOWN_3X3, master, true);
        BlockPos absCorner = helper.absolutePos(Door3x3Block.partPos(master, true, 2, 0));
        TGBlocks.DOOR_HANGAR_DOWN_3X3.get().playerWillDestroy(level, absCorner, level.getBlockState(absCorner), player);
        level.removeBlock(absCorner, false);
        helper.assertTrue(doorParts(helper, TGBlocks.DOOR_HANGAR_DOWN_3X3, master, true) == 0, "Breaking one part removes the whole door");
        helper.assertTrue(doorItems(helper, TGBlocks.DOOR_HANGAR_DOWN_3X3) == 1, "Mining a door drops exactly one door item");

        // destroyed from the centre (e.g. an explosion): one drop; from a side part without a player: the door vanishes without drops
        placeDoor(helper, TGBlocks.DOOR_METAL_3X3, master, false);
        level.destroyBlock(helper.absolutePos(master), true);
        helper.assertTrue(doorParts(helper, TGBlocks.DOOR_METAL_3X3, master, false) == 0, "Destroying the centre removes the whole door");
        helper.assertTrue(doorItems(helper, TGBlocks.DOOR_METAL_3X3) == 1, "The centre drops the door");
        placeDoor(helper, TGBlocks.DOOR_NETHER_3X3, master, false);
        level.destroyBlock(helper.absolutePos(Door3x3Block.partPos(master, false, 0, 2)), true);
        helper.assertTrue(doorParts(helper, TGBlocks.DOOR_NETHER_3X3, master, false) == 0, "Destroying a side part removes the whole door");
        helper.assertTrue(doorItems(helper, TGBlocks.DOOR_NETHER_3X3) == 0, "A side part does not drop by itself");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void bunkerDoorDoubleDoorsOpenTogether(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockState lower = TGBlocks.BUNKER_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
        BlockPos left = new BlockPos(2, 1, 3);
        BlockPos right = new BlockPos(3, 1, 3); // east of a north facing, left hinged door
        placeBunkerDoor(helper, left, lower.setValue(DoorBlock.HINGE, DoorHingeSide.LEFT));
        placeBunkerDoor(helper, right, lower.setValue(DoorBlock.HINGE, DoorHingeSide.RIGHT));
        use(helper, player, left);
        helper.assertTrue(helper.getBlockState(left).getValue(DoorBlock.OPEN), "The used bunker door opens");
        helper.assertTrue(helper.getBlockState(right).getValue(DoorBlock.OPEN), "The bunker door next to it opens as well");
        helper.assertTrue(helper.getBlockState(right.above()).getValue(DoorBlock.OPEN), "Both halves open");
        use(helper, player, right.above());
        helper.assertTrue(!helper.getBlockState(left).getValue(DoorBlock.OPEN) && !helper.getBlockState(right).getValue(DoorBlock.OPEN),
                "Using the other door closes both");
        helper.succeed();
    }

    private static void placeBunkerDoor(GameTestHelper helper, BlockPos lowerPos, BlockState lowerState) {
        helper.setBlock(lowerPos, lowerState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        helper.setBlock(lowerPos.above(), lowerState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }
}
