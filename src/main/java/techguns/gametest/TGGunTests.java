package techguns.gametest;

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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.GunItem;
import techguns.gun.ShooterState;
import techguns.gun.ammo.AmmoType;
import techguns.registry.TGAmmo;
import techguns.registry.TGGuns;

/**
 * Gun logic on the server: firing, ammo use, fire delay, reloading.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGGunTests {
    private static final String ARENA = "empty_8x4x8";

    /** Survival mock player at the west end of the arena, looking east (towards +x). */
    private static Player shooter(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(new Vec3(0.5, 1, 4.5));
        player.moveTo(pos.x, pos.y, pos.z, -90.0f, 0.0f);
        player.setYHeadRot(-90.0f);
        return player;
    }

    private static ItemStack m4(Player player, int loaded) {
        ItemStack stack = new ItemStack(TGGuns.M4.get());
        GunData.setAmmo(stack, new GunAmmo(loaded, AmmoType.DEFAULT));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return stack;
    }

    @GameTest(template = ARENA)
    public static void m4BulletHurtsZombie(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = m4(player, 30);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(5, 1, 4));
        float health = zombie.getHealth();

        ((GunItem) gun.getItem()).shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.assertTrue(GunData.loaded(gun) == 29, "One round should be used, loaded=" + GunData.loaded(gun));
        helper.succeedWhen(() -> helper.assertTrue(zombie.getHealth() < health, "Zombie was not hit"));
    }

    @GameTest(template = ARENA)
    public static void laserHitsInstantly(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = new ItemStack(TGGuns.LASERGUN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 4));
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        float health = zombie.getHealth();

        ((GunItem) gun.getItem()).shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        // a beam traces its full length on its first tick and damages exactly once
        helper.succeedWhen(() -> {
            float loss = health - zombie.getHealth();
            helper.assertTrue(Math.abs(loss - 12.0f) < 0.01f, "Laser should deal 12 energy damage once, dealt " + loss);
        });
    }

    @GameTest(template = ARENA)
    public static void rocketExplodesAndDamagesNearby(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = new ItemStack(TGGuns.ROCKET_LAUNCHER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        Zombie target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 4));
        Zombie bystander = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 5));

        ((GunItem) gun.getItem()).shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.succeedWhen(() -> {
            helper.assertTrue(target.getHealth() < target.getMaxHealth(), "Rocket target was not damaged");
            helper.assertTrue(bystander.getHealth() < bystander.getMaxHealth(), "Explosion should damage the bystander too");
        });
    }

    @GameTest(template = ARENA)
    public static void fireDelayBlocksImmediateSecondShot(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = m4(player, 30);
        GunItem item = (GunItem) gun.getItem();

        item.shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);
        item.shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);
        helper.assertTrue(GunData.loaded(gun) == 29, "Second shot must wait for the fire delay, loaded=" + GunData.loaded(gun));

        for (int i = 0; i < item.stats().firing().fireDelay(); i++) {
            ShooterState.of(player).tick();
        }
        item.shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);
        helper.assertTrue(GunData.loaded(gun) == 28, "Shot after the delay should fire, loaded=" + GunData.loaded(gun));
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void emptyGunReloadsFromMagazine(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = m4(player, 0);
        player.getInventory().add(new ItemStack(TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(), 2));

        ((GunItem) gun.getItem()).shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.assertTrue(GunData.loaded(gun) == 30, "Gun should be reloaded, loaded=" + GunData.loaded(gun));
        helper.assertTrue(player.getInventory().countItem(TGAmmo.ASSAULT_RIFLE_MAGAZINE.get()) == 1, "One magazine should be used");
        helper.assertTrue(player.getInventory().countItem(TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get()) == 1, "Empty magazine should be returned");
        helper.assertTrue(ShooterState.of(player).fireDelay(InteractionHand.MAIN_HAND) == 45, "Reload time should block firing");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void emptyGunWithoutAmmoStaysEmpty(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = m4(player, 0);

        ((GunItem) gun.getItem()).shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.assertTrue(GunData.loaded(gun) == 0, "Gun must stay empty without ammo");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void forcedReloadReturnsLooseRounds(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = m4(player, 20);
        player.getInventory().add(new ItemStack(TGAmmo.ASSAULT_RIFLE_MAGAZINE.get()));

        ((GunItem) gun.getItem()).startReload(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, true);

        helper.assertTrue(GunData.loaded(gun) == 30, "Gun should be full");
        // 20 rounds left, 10 shots per rifle round (3 rounds fill a 30-shot magazine) -> 2 rounds back
        helper.assertTrue(player.getInventory().countItem(TGAmmo.RIFLE_ROUNDS.get()) == 2,
                "Expected 2 rifle rounds back, got " + player.getInventory().countItem(TGAmmo.RIFLE_ROUNDS.get()));
        helper.succeed();
    }
}
