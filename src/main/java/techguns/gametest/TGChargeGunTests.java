package techguns.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.entity.projectile.TFGProjectile;
import techguns.gun.ChargeGunItem;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.ammo.AmmoType;
import techguns.registry.TGGuns;

/**
 * Charge guns (Phase 3 batch 3e): holding the use key charges, releasing fires one charged shot that uses ammo in
 * proportion to the charge.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGChargeGunTests {
    private static final String ARENA = "empty_8x4x8";

    /** Survival mock player at the west end of the arena, looking east (towards +x). */
    private static Player shooter(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(new Vec3(0.5, 1, 4.5));
        player.moveTo(pos.x, pos.y, pos.z, -90.0f, 0.0f);
        player.setYHeadRot(-90.0f);
        return player;
    }

    private static ItemStack hold(Player player, ChargeGunItem gun, int loaded) {
        ItemStack stack = new ItemStack(gun);
        GunData.setAmmo(stack, new GunAmmo(loaded, AmmoType.DEFAULT));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return stack;
    }

    /** Releases the use key after {@code ticks} ticks of charging. */
    private static void release(GameTestHelper helper, Player player, ChargeGunItem gun, ItemStack stack, int ticks) {
        gun.releaseUsing(stack, helper.getLevel(), player, gun.getUseDuration(stack, player) - ticks);
    }

    @GameTest(template = ARENA)
    public static void fullChargeUsesFullAmmo(GameTestHelper helper) {
        Player player = shooter(helper);
        ChargeGunItem gun = (ChargeGunItem) TGGuns.TFG.get();
        ItemStack stack = hold(player, gun, 20);

        release(helper, player, gun, stack, 200);

        helper.assertTrue(GunData.loaded(stack) == 10, "A full TFG charge should use 10 rounds, loaded=" + GunData.loaded(stack));
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void partialChargeUsesLessAmmo(GameTestHelper helper) {
        Player player = shooter(helper);
        ChargeGunItem gun = (ChargeGunItem) TGGuns.TFG.get();
        ItemStack stack = hold(player, gun, 20);

        // 15 of 60 ticks: a quarter charge, ceil(0.25 * 10) = 3 rounds
        release(helper, player, gun, stack, 15);

        helper.assertTrue(GunData.loaded(stack) == 17, "A quarter TFG charge should use 3 rounds, loaded=" + GunData.loaded(stack));
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void chargeIsLimitedByLoadedAmmo(GameTestHelper helper) {
        Player player = shooter(helper);
        ChargeGunItem gun = (ChargeGunItem) TGGuns.TFG.get();
        ItemStack stack = hold(player, gun, 4);

        release(helper, player, gun, stack, 200);

        helper.assertTrue(GunData.loaded(stack) == 0, "The charge should use the remaining 4 rounds, loaded=" + GunData.loaded(stack));
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void chargedTfgShotHurtsTarget(GameTestHelper helper) {
        Player player = shooter(helper);
        ChargeGunItem gun = (ChargeGunItem) TGGuns.TFG.get();
        ItemStack stack = hold(player, gun, 20);
        Zombie zombie = TGTestTargets.husk(helper, new BlockPos(6, 1, 4));

        release(helper, player, gun, stack, 200);

        List<TFGProjectile> shots = helper.getLevel().getEntitiesOfClass(TFGProjectile.class, new AABB(player.blockPosition()).inflate(4));
        helper.assertTrue(shots.size() == 1, "One charged TFG shot expected, found " + shots.size());
        helper.succeedWhen(() -> helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "Charged TFG shot should hurt the zombie"));
    }

    @GameTest(template = ARENA)
    public static void normalShotStillWorks(GameTestHelper helper) {
        Player player = shooter(helper);
        ChargeGunItem gun = (ChargeGunItem) TGGuns.BIOGUN.get();
        ItemStack stack = hold(player, gun, 30);
        Zombie zombie = TGTestTargets.husk(helper, new BlockPos(5, 1, 4));

        gun.shoot(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.assertTrue(GunData.loaded(stack) == 29, "A normal biogun shot uses one round, loaded=" + GunData.loaded(stack));
        helper.succeedWhen(() -> helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "Biogun shot should hurt the zombie"));
    }
}
