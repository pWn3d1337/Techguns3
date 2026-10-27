package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.block.ExplosiveChargeBlock;
import techguns.machine.ExplosiveChargeBlockEntity;
import techguns.registry.TGBlocks;

/** Mining charges (Phase 5d1): sticking to walls, settings limits, the tunnel blast and the hardness limit. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGExplosiveChargeTests {
    private static final String ARENA = "empty_8x10x8";
    /** The charge sticks to the wall north of it. */
    private static final BlockPos CHARGE = new BlockPos(3, 3, 6);

    /** A stone wall from z 1 to 5, x 1..5, y 1..5, with {@code core} at (3, 3, 3). */
    private static void wall(GameTestHelper helper, Block core) {
        for (int x = 1; x <= 5; x++) {
            for (int y = 1; y <= 5; y++) {
                for (int z = 1; z <= 5; z++) {
                    helper.setBlock(x, y, z, Blocks.STONE);
                }
            }
        }
        helper.setBlock(3, 3, 3, core);
    }

    private static ExplosiveChargeBlockEntity charge(GameTestHelper helper, Block block, Player owner) {
        helper.setBlock(CHARGE, block.defaultBlockState().setValue(ExplosiveChargeBlock.FACING, Direction.NORTH));
        ExplosiveChargeBlockEntity charge = (ExplosiveChargeBlockEntity) helper.getBlockEntity(CHARGE);
        charge.setOwner(owner);
        owner.moveTo(Vec3.atCenterOf(helper.absolutePos(CHARGE.south())));
        return charge;
    }

    @GameTest(template = ARENA)
    public static void chargeDropsWithoutWall(GameTestHelper helper) {
        helper.setBlock(CHARGE.north(), Blocks.STONE);
        helper.setBlock(CHARGE, TGBlocks.EXPLOSIVE_CHARGE.get().defaultBlockState().setValue(ExplosiveChargeBlock.FACING, Direction.NORTH));
        helper.setBlock(CHARGE.north(), Blocks.AIR);
        helper.assertBlockNotPresent(TGBlocks.EXPLOSIVE_CHARGE.get(), CHARGE);
        helper.assertItemEntityPresent(TGBlocks.EXPLOSIVE_CHARGE.get().asItem(), CHARGE, 2.0);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void chargeSettingsHaveLimits(GameTestHelper helper) {
        wall(helper, Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ExplosiveChargeBlockEntity tnt = charge(helper, TGBlocks.EXPLOSIVE_CHARGE.get(), player);
        helper.assertTrue(tnt.getRadius() == 3 && tnt.getLength() == 3 && tnt.getFuse() == 100, "Mining charge defaults: radius 3, length 3, 5 s");
        tnt.pressButton(player, ExplosiveChargeBlockEntity.BUTTON_RADIUS_UP);
        for (int i = 0; i < 5; i++) {
            tnt.pressButton(player, ExplosiveChargeBlockEntity.BUTTON_LENGTH_UP);
            tnt.pressButton(player, ExplosiveChargeBlockEntity.BUTTON_FUSE_DOWN);
        }
        helper.assertTrue(tnt.getRadius() == 3 && tnt.getLength() == 5 && tnt.getFuse() == 60, "Radius fixed at 3, length up to 5, delay down to 3 s");
        tnt.pressButton(player, ExplosiveChargeBlockEntity.BUTTON_ARM);
        helper.assertTrue(tnt.isArmed() && helper.getBlockState(CHARGE).getValue(ExplosiveChargeBlock.ARMED), "Detonate arms the charge");
        helper.assertFalse(tnt.pressButton(player, ExplosiveChargeBlockEntity.BUTTON_LENGTH_DOWN), "An armed charge can't be changed");
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void chargeBlowsTunnel(GameTestHelper helper) {
        wall(helper, Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ExplosiveChargeBlockEntity tnt = charge(helper, TGBlocks.EXPLOSIVE_CHARGE.get(), player);
        tnt.pressButton(player, ExplosiveChargeBlockEntity.BUTTON_FUSE_DOWN);
        tnt.pressButton(player, ExplosiveChargeBlockEntity.BUTTON_FUSE_DOWN);
        tnt.pressButton(player, ExplosiveChargeBlockEntity.BUTTON_ARM);
        helper.runAfterDelay(65, () -> {
            helper.assertBlockNotPresent(TGBlocks.EXPLOSIVE_CHARGE.get(), CHARGE);
            for (int x = 2; x <= 4; x++) {
                for (int y = 2; y <= 4; y++) {
                    for (int z = 3; z <= 5; z++) {
                        helper.assertBlockPresent(Blocks.AIR, x, y, z);
                    }
                    helper.assertBlockPresent(Blocks.STONE, x, y, 2);
                }
            }
            helper.assertBlockPresent(Blocks.STONE, 1, 3, 5);
            helper.assertBlockPresent(Blocks.STONE, 5, 3, 5);
            helper.assertEntityPresent(EntityType.ITEM, new BlockPos(3, 3, 4), 3.0);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void obsidianStopsMiningCharge(GameTestHelper helper) {
        wall(helper, Blocks.OBSIDIAN);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ExplosiveChargeBlockEntity tnt = charge(helper, TGBlocks.EXPLOSIVE_CHARGE.get(), player);
        tnt.pressButton(player, ExplosiveChargeBlockEntity.BUTTON_LENGTH_UP);
        tnt.arm();
        helper.runAfterDelay(105, () -> {
            helper.assertBlockPresent(Blocks.AIR, 3, 3, 4);
            helper.assertBlockPresent(Blocks.OBSIDIAN, 3, 3, 3);
            helper.assertBlockPresent(Blocks.STONE, 3, 3, 2);
            helper.assertBlockPresent(Blocks.AIR, 2, 3, 2);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void advancedChargeBreaksObsidian(GameTestHelper helper) {
        wall(helper, Blocks.OBSIDIAN);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ExplosiveChargeBlockEntity c4 = charge(helper, TGBlocks.EXPLOSIVE_CHARGE_ADVANCED.get(), player);
        helper.assertTrue(c4.getRadius() == 5 && c4.getLength() == 10, "Advanced defaults: radius 5, length 10");
        c4.arm();
        helper.runAfterDelay(105, () -> {
            helper.assertBlockPresent(Blocks.AIR, 3, 3, 3);
            helper.assertBlockPresent(Blocks.AIR, 1, 1, 1);
            helper.assertBlockPresent(Blocks.AIR, 5, 5, 1);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void chargeWithoutOwnerStillBlasts(GameTestHelper helper) {
        wall(helper, Blocks.STONE);
        helper.setBlock(CHARGE, TGBlocks.EXPLOSIVE_CHARGE.get().defaultBlockState().setValue(ExplosiveChargeBlock.FACING, Direction.NORTH));
        ExplosiveChargeBlockEntity tnt = (ExplosiveChargeBlockEntity) helper.getBlockEntity(CHARGE);
        tnt.arm();
        helper.runAfterDelay(105, () -> {
            helper.assertBlockNotPresent(TGBlocks.EXPLOSIVE_CHARGE.get(), CHARGE);
            helper.assertBlockPresent(Blocks.AIR, 3, 3, 5);
            helper.succeed();
        });
    }
}
