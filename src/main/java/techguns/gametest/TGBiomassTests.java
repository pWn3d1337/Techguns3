package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
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
import techguns.block.BioBlobBlock;
import techguns.entity.projectile.BioGunProjectile;
import techguns.registry.TGBlocks;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;

/** Thrown biomass (Techguns 3): a snowball-like bio gun glob with half the bio gun's damage that leaves the smallest blob. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGBiomassTests {
    private static final String ARENA = "empty_8x4x8";

    /** Survival mock player at the west end of the arena, looking east (towards +x), throwing one of two biomass. */
    private static Player throwBiomass(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(new Vec3(0.5, 1, 4.5));
        player.moveTo(pos.x, pos.y, pos.z, -90.0f, 0.0f);
        player.setYHeadRot(-90.0f);
        ItemStack stack = new ItemStack(TGItems.BIOMASS.get(), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(stack.getCount() == 1, "Throwing should use one biomass, count=" + stack.getCount());
        helper.assertTrue(player.getCooldowns().isOnCooldown(TGItems.BIOMASS.get()), "Throwing should start the use cooldown");
        return player;
    }

    @GameTest(template = ARENA)
    public static void thrownBiomassLeavesSmallBlob(GameTestHelper helper) {
        // a wall at eye height in the line of the throw
        helper.setBlock(new BlockPos(4, 2, 4), Blocks.STONE);
        throwBiomass(helper);

        BlockPos blobPos = new BlockPos(3, 2, 4);
        helper.succeedWhen(() -> {
            BlockState state = helper.getBlockState(blobPos);
            helper.assertTrue(state.is(TGBlocks.BIOBLOB.get()), "The throw should leave a bio blob in front of the wall, found " + state);
            helper.assertTrue(state.getValue(BioBlobBlock.SIZE) == 0, "The blob should have the smallest size, got " + state.getValue(BioBlobBlock.SIZE));
            helper.assertTrue(state.getValue(BioBlobBlock.FACING) == Direction.EAST, "The blob should sit on the wall's west face");
        });
    }

    @GameTest(template = ARENA)
    public static void thrownBiomassDealsHalfBiogunDamage(GameTestHelper helper) {
        // husks are undead: no poison damage on top of the hit
        Zombie husk = TGTestTargets.husk(helper, new BlockPos(4, 1, 4));
        throwBiomass(helper);

        float expected = TGGuns.BIOGUN.get().stats().damage().damage() * BioGunProjectile.THROWN_DAMAGE_FACTOR;
        helper.succeedWhen(() -> {
            float lost = husk.getMaxHealth() - husk.getHealth();
            // the husk's 2 armor points may take a little off
            helper.assertTrue(lost > expected * 0.8f && lost <= expected, "The throw should deal about " + expected + " damage, dealt " + lost);
        });
    }
}
