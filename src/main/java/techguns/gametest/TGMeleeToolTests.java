package techguns.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.MeleeToolEvents;
import techguns.gun.MeleeToolItem;
import techguns.gun.ammo.AmmoType;
import techguns.registry.TGGuns;

/**
 * Powered melee tools (Phase 3 batch 3f): fuel powers damage and mining, every hit and mined block uses fuel, mining
 * heads raise level and damage, the drill mines a plane.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGMeleeToolTests {
    private static final String ARENA = "empty_8x4x8";

    /** Mock player at the west end of the arena, looking east (towards +x). */
    private static Player player(GameTestHelper helper, GameType mode) {
        Player player = helper.makeMockPlayer(mode);
        Vec3 pos = helper.absoluteVec(new Vec3(0.5, 1, 4.5));
        player.moveTo(pos.x, pos.y, pos.z, -90.0f, 0.0f);
        player.setYHeadRot(-90.0f);
        return player;
    }

    private static ItemStack tool(MeleeToolItem tool, int loaded, int head) {
        ItemStack stack = new ItemStack(tool);
        GunData.setAmmo(stack, new GunAmmo(loaded, AmmoType.DEFAULT));
        tool.setHeadLevel(stack, head);
        return stack;
    }

    private static double attackDamage(ItemStack stack) {
        return stack.getItem().getDefaultAttributeModifiers(stack).modifiers().stream()
                .filter(entry -> entry.attribute().is(Attributes.ATTACK_DAMAGE))
                .mapToDouble(entry -> entry.modifier().amount()).sum();
    }

    @GameTest(template = ARENA)
    public static void fuelAndHeadSetDamage(GameTestHelper helper) {
        MeleeToolItem chainsaw = (MeleeToolItem) TGGuns.CHAINSAW.get();
        double unpowered = attackDamage(tool(chainsaw, 0, 0));
        double powered = attackDamage(tool(chainsaw, 10, 0));
        double carbon = attackDamage(tool(chainsaw, 10, 2));

        helper.assertTrue(unpowered == 2.0, "Unpowered chainsaw damage should be 2, was " + unpowered);
        helper.assertTrue(powered == 12.0, "Powered chainsaw damage should be 12, was " + powered);
        helper.assertTrue(carbon == 14.0, "Carbon blades should add 2 damage, was " + carbon);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void poweredHitUsesFuel(GameTestHelper helper) {
        MeleeToolItem chainsaw = (MeleeToolItem) TGGuns.CHAINSAW.get();
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 4));
        ItemStack survival = tool(chainsaw, 10, 0);
        ItemStack creative = tool(chainsaw, 10, 0);

        chainsaw.hurtEnemy(survival, zombie, player(helper, GameType.SURVIVAL));
        chainsaw.hurtEnemy(creative, zombie, player(helper, GameType.CREATIVE));

        helper.assertTrue(GunData.loaded(survival) == 9, "A survival hit should use one round, loaded=" + GunData.loaded(survival));
        helper.assertTrue(GunData.loaded(creative) == 10, "A creative hit should use no fuel, loaded=" + GunData.loaded(creative));
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void miningLevelFollowsHead(GameTestHelper helper) {
        MeleeToolItem hammer = (MeleeToolItem) TGGuns.POWER_HAMMER.get();
        MeleeToolItem drill = (MeleeToolItem) TGGuns.MINING_DRILL.get();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();

        helper.assertTrue(hammer.isCorrectToolForDrops(tool(hammer, 10, 0), Blocks.DIAMOND_ORE.defaultBlockState()),
                "Steel power hammer should mine diamond ore");
        helper.assertFalse(hammer.isCorrectToolForDrops(tool(hammer, 10, 0), obsidian), "Steel power hammer should not mine obsidian");
        helper.assertTrue(hammer.isCorrectToolForDrops(tool(hammer, 10, 2), obsidian), "Carbon power hammer should mine obsidian");
        helper.assertTrue(drill.isCorrectToolForDrops(tool(drill, 10, 0), obsidian), "Obsidian drill should mine obsidian");
        helper.assertFalse(drill.isCorrectToolForDrops(tool(drill, 0, 0), stone), "An unpowered drill should not harvest");
        helper.assertTrue(drill.getDestroySpeed(tool(drill, 10, 0), stone) == 14.0f, "Powered drill should mine stone at speed 14");
        helper.assertTrue(drill.getDestroySpeed(tool(drill, 0, 0), stone) == 1.0f, "Unpowered drill should mine like a hand");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void minedBlockUsesFuel(GameTestHelper helper) {
        MeleeToolItem drill = (MeleeToolItem) TGGuns.MINING_DRILL.get();
        ItemStack stack = tool(drill, 10, 0);
        helper.setBlock(new BlockPos(2, 1, 4), Blocks.STONE);

        drill.mineBlock(stack, helper.getLevel(), Blocks.STONE.defaultBlockState(), helper.absolutePos(new BlockPos(2, 1, 4)),
                player(helper, GameType.SURVIVAL));

        helper.assertTrue(GunData.loaded(stack) == 9, "Mining a block should use one round, loaded=" + GunData.loaded(stack));
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void drillMinesPlaneUnlessSneaking(GameTestHelper helper) {
        MeleeToolItem drill = (MeleeToolItem) TGGuns.MINING_DRILL.get();
        Player player = player(helper, GameType.SURVIVAL);
        ItemStack stack = tool(drill, 10, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        // 3x3 stone wall facing the player; the eye looks at its centre
        for (int y = 1; y <= 3; y++) {
            for (int z = 3; z <= 5; z++) {
                helper.setBlock(new BlockPos(2, y, z), Blocks.STONE);
            }
        }
        BlockPos center = helper.absolutePos(new BlockPos(2, 2, 4));

        List<BlockPos> plane = MeleeToolEvents.plane(helper.getLevel(), player, stack, center);
        player.setShiftKeyDown(true);
        List<BlockPos> sneaking = MeleeToolEvents.plane(helper.getLevel(), player, stack, center);

        helper.assertTrue(plane.size() == 8, "The drill should mine the 8 blocks around the target, got " + plane.size());
        helper.assertTrue(plane.stream().allMatch(pos -> pos.getX() == center.getX()), "The plane should face the player");
        helper.assertTrue(sneaking.isEmpty(), "Sneaking should mine a single block, got " + sneaking.size());
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void powerHammerChargeUsesAir(GameTestHelper helper) {
        MeleeToolItem hammer = (MeleeToolItem) TGGuns.POWER_HAMMER.get();
        Player player = player(helper, GameType.SURVIVAL);
        ItemStack stack = tool(hammer, 20, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        hammer.releaseUsing(stack, helper.getLevel(), player, hammer.getUseDuration(stack, player) - 40);

        helper.assertTrue(GunData.loaded(stack) == 15, "A full power hammer charge should use 5 rounds, loaded=" + GunData.loaded(stack));
        helper.succeed();
    }
}
