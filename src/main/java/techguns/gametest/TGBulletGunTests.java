package techguns.gametest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import techguns.TGIds;
import techguns.Techguns;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.GunItem;
import techguns.gun.ammo.AmmoType;
import techguns.registry.TGGuns;

/**
 * The same checks for every projectile and beam gun (Phase 3 batches 3a-3c): a shot uses one round and hits, an empty gun
 * reloads from its own ammo items and gives back the empty ones, incendiary ammo sets the target on fire, explosive
 * ammo also hurts what stands next to the target.
 */
@GameTestHolder(Techguns.MODID)
public class TGBulletGunTests {
    private static final String ARENA = TGIds.id("empty_8x4x8").toString();

    private static final List<DeferredItem<GunItem>> GUNS = List.of(
            TGGuns.REVOLVER, TGGuns.GOLDEN_REVOLVER, TGGuns.PISTOL, TGGuns.AK47, TGGuns.M4_INFILTRATOR, TGGuns.AUG,
            TGGuns.SCAR, TGGuns.THOMPSON, TGGuns.MAC10, TGGuns.VECTOR, TGGuns.LMG, TGGuns.BOLT_ACTION, TGGuns.MINIGUN,
            TGGuns.SAWED_OFF, TGGuns.COMBAT_SHOTGUN, TGGuns.AS50, TGGuns.GAUSS_RIFLE, TGGuns.BLASTER_RIFLE, TGGuns.BLASTER_SHOTGUN,
            TGGuns.PULSE_RIFLE, TGGuns.PDW,
            TGGuns.LASER_PISTOL, TGGuns.NUCLEAR_DEATHRAY, TGGuns.TESLA_GUN, TGGuns.ALIEN_BLASTER, TGGuns.MIB_GUN, TGGuns.NETHER_BLASTER,
            TGGuns.SONIC_SHOTGUN);

    @GameTestGenerator
    public static Collection<TestFunction> bulletGunTests() {
        List<TestFunction> tests = new ArrayList<>();
        for (DeferredItem<GunItem> gun : GUNS) {
            String name = gun.getId().getPath();
            tests.add(test(name + "_fires_and_hits", gun, TGBulletGunTests::firesAndHits));
            tests.add(test(name + "_reloads_from_ammo", gun, TGBulletGunTests::reloadsFromAmmo));
            if (gun.get().ammoType().hasVariant(AmmoType.INCENDIARY)) {
                tests.add(test(name + "_incendiary_ignites", gun, TGBulletGunTests::incendiaryIgnites));
            }
            if (gun.get().ammoType().hasVariant(AmmoType.EXPLOSIVE)) {
                tests.add(test(name + "_explosive_blast", gun, TGBulletGunTests::explosiveBlast));
            }
        }
        tests.add(test("nether_blaster_ignites", TGGuns.NETHER_BLASTER, (helper, gun) -> ignites(helper, gun, AmmoType.DEFAULT)));
        tests.add(test("sonic_shotgun_pierces", TGGuns.SONIC_SHOTGUN, TGBulletGunTests::pierces));
        return tests;
    }

    private static TestFunction test(String name, DeferredItem<GunItem> gun, BiConsumer<GameTestHelper, GunItem> check) {
        return new TestFunction("techguns_bullet_guns", "techguns.bulletguns." + name, ARENA, 100, 0L, true,
                helper -> check.accept(helper, gun.get()));
    }

    /** Survival mock player at the west end of the arena, looking east (towards +x). */
    private static Player shooter(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(new Vec3(0.5, 1, 4.5));
        player.moveTo(pos.x, pos.y, pos.z, -90.0f, 0.0f);
        player.setYHeadRot(-90.0f);
        return player;
    }

    private static ItemStack hold(Player player, GunItem gun, int loaded, String variant) {
        ItemStack stack = new ItemStack(gun);
        GunData.setAmmo(stack, new GunAmmo(loaded, variant));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return stack;
    }

    private static Zombie target(GameTestHelper helper) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(5, 1, 4));
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        return zombie;
    }

    private static void firesAndHits(GameTestHelper helper, GunItem gun) {
        Player player = shooter(helper);
        int clip = gun.stats().firing().clipSize();
        ItemStack stack = hold(player, gun, clip, AmmoType.DEFAULT);
        Zombie zombie = target(helper);
        float health = zombie.getHealth();

        gun.shoot(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.assertTrue(GunData.loaded(stack) == clip - 1, "One round should be used, loaded=" + GunData.loaded(stack));
        helper.succeedWhen(() -> helper.assertTrue(zombie.getHealth() < health, "Zombie was not hit"));
    }

    private static void reloadsFromAmmo(GameTestHelper helper, GunItem gun) {
        Player player = shooter(helper);
        ItemStack stack = hold(player, gun, 0, AmmoType.DEFAULT);
        List<ItemStack> ammo = gun.ammoType().ammoStacks(AmmoType.DEFAULT);
        ammo.forEach(a -> player.getInventory().add(a.copyWithCount(2)));

        gun.shoot(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.assertTrue(GunData.loaded(stack) > 0, "Gun should be reloaded from " + ammo);
        for (ItemStack a : ammo) {
            helper.assertTrue(player.getInventory().countItem(a.getItem()) < 2, "Ammo should be used: " + a);
        }
        for (ItemStack empty : gun.ammoType().emptyStacks()) {
            helper.assertTrue(empty.isEmpty() || player.getInventory().countItem(empty.getItem()) > 0, "Should get back " + empty);
        }
        helper.succeed();
    }

    private static void incendiaryIgnites(GameTestHelper helper, GunItem gun) {
        helper.assertTrue(gun.ammoType().hasVariant(AmmoType.INCENDIARY), "Gun should accept incendiary ammo");
        ignites(helper, gun, AmmoType.INCENDIARY);
    }

    private static void ignites(GameTestHelper helper, GunItem gun, String variant) {
        Player player = shooter(helper);
        ItemStack stack = hold(player, gun, gun.stats().firing().clipSize(), variant);
        Zombie zombie = target(helper);

        gun.shoot(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.succeedWhen(() -> helper.assertTrue(zombie.isOnFire(), "The hit should set the zombie on fire"));
    }

    /** The sonic wave passes through the first target and hits the one behind it. */
    private static void pierces(GameTestHelper helper, GunItem gun) {
        Player player = shooter(helper);
        ItemStack stack = hold(player, gun, gun.stats().firing().clipSize(), AmmoType.DEFAULT);
        Zombie front = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 4));
        Zombie back = target(helper);
        float frontHealth = front.getHealth();
        float backHealth = back.getHealth();

        gun.shoot(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.succeedWhen(() -> {
            helper.assertTrue(front.getHealth() < frontHealth, "Front zombie was not hit");
            helper.assertTrue(back.getHealth() < backHealth, "Wave should pass through to the zombie behind");
        });
    }

    private static void explosiveBlast(GameTestHelper helper, GunItem gun) {
        Player player = shooter(helper);
        ItemStack stack = hold(player, gun, gun.stats().firing().clipSize(), AmmoType.EXPLOSIVE);
        Zombie zombie = target(helper);
        // out of the line of fire, inside the blast radius
        Zombie neighbour = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(5, 1, 6));
        float health = neighbour.getHealth();

        gun.shoot(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.succeedWhen(() -> helper.assertTrue(neighbour.getHealth() < health, "Blast should hurt the zombie next to the target"));
    }
}
