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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.entity.projectile.GrenadeProjectile;
import techguns.entity.projectile.RocketProjectile;
import techguns.gun.GrenadeItem;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.GunItem;
import techguns.gun.ammo.AmmoType;
import techguns.registry.TGGuns;

/**
 * Explosives (Phase 3 batch 3d): rocket variants, the bouncing 40 mm grenade, guided missiles and hand grenades.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGExplosiveTests {
    private static final String ARENA = "empty_8x4x8";

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

    /** A zombie well outside the plain rocket's blast (radius 4) but inside the nuke's (radius 20). */
    private static Zombie farBystander(GameTestHelper helper) {
        return helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
    }

    @GameTest(template = ARENA)
    public static void nukeRocketHasLargerBlast(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = hold(player, TGGuns.ROCKET_LAUNCHER.get(), 1, AmmoType.NUKE);
        helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 4));
        Zombie bystander = farBystander(helper);

        ((GunItem) gun.getItem()).shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.succeedWhen(() -> helper.assertTrue(bystander.getHealth() < bystander.getMaxHealth(), "Nuke blast should reach the far zombie"));
    }

    @GameTest(template = ARENA)
    public static void plainRocketSparesFarBystander(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = hold(player, TGGuns.ROCKET_LAUNCHER.get(), 1, AmmoType.DEFAULT);
        Zombie target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 4));
        Zombie bystander = farBystander(helper);

        ((GunItem) gun.getItem()).shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.succeedWhen(() -> {
            helper.assertTrue(target.getHealth() < target.getMaxHealth(), "Rocket should hit its target");
            helper.assertTrue(bystander.getHealth() == bystander.getMaxHealth(), "Plain rocket blast must not reach the far zombie");
        });
    }

    @GameTest(template = ARENA)
    public static void highVelocityRocketIsFaster(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = hold(player, TGGuns.ROCKET_LAUNCHER.get(), 1, AmmoType.HIGH_VELOCITY);

        ((GunItem) gun.getItem()).shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        List<RocketProjectile> rockets = helper.getLevel().getEntitiesOfClass(RocketProjectile.class, new AABB(player.blockPosition()).inflate(4));
        helper.assertTrue(rockets.size() == 1, "One rocket expected, found " + rockets.size());
        RocketProjectile rocket = rockets.getFirst();
        helper.assertTrue(rocket.variant() == RocketProjectile.Variant.HIGH_VELOCITY, "Wrong rocket variant " + rocket.variant());
        double speed = rocket.getDeltaMovement().length();
        helper.assertTrue(Math.abs(speed - 2.0) < 0.05, "High velocity rocket should fly at 2 blocks/tick, speed=" + speed);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void launcherGrenadeBouncesOffWalls(GameTestHelper helper) {
        // wall across the arena three blocks in front of the shooter
        for (int z = 0; z < 8; z++) {
            for (int y = 1; y < 4; y++) {
                helper.setBlock(new BlockPos(3, y, z), Blocks.STONE);
            }
        }
        Player player = shooter(helper);
        ItemStack gun = hold(player, TGGuns.GRENADE_LAUNCHER.get(), 6, AmmoType.DEFAULT);

        ((GunItem) gun.getItem()).shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.succeedWhen(() -> {
            List<GrenadeProjectile> grenades = helper.getLevel().getEntitiesOfClass(GrenadeProjectile.class,
                    new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(8));
            helper.assertTrue(grenades.size() == 1 && grenades.getFirst().getDeltaMovement().x < 0, "Grenade should bounce back off the wall");
        });
    }

    @GameTest(template = ARENA)
    public static void guidedMissileHomesOnTarget(GameTestHelper helper) {
        Player player = shooter(helper);
        ItemStack gun = hold(player, TGGuns.GUIDED_MISSILE_LAUNCHER.get(), 1, AmmoType.DEFAULT);
        // off to the side: an unguided missile flying straight east would miss it
        Zombie target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 0));
        GunItem item = (GunItem) gun.getItem();

        item.shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false, item.acceptTarget(player, target));

        helper.succeedWhen(() -> helper.assertTrue(target.getHealth() < target.getMaxHealth(), "Guided missile should turn and hit the target"));
    }

    @GameTest(template = ARENA)
    public static void guidedMissileLauncherRejectsTargetsOutOfRange(GameTestHelper helper) {
        Player player = shooter(helper);
        Zombie far = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 0));
        far.setPos(far.getX() + 500, far.getY(), far.getZ());
        GunItem item = TGGuns.GUIDED_MISSILE_LAUNCHER.get();

        helper.assertTrue(item.acceptTarget(player, far) == null, "Targets beyond the lock range must be rejected");
        helper.assertTrue(TGGuns.M4.get().acceptTarget(player, far) == null, "Guns without lock-on ignore targets");
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void handGrenadeExplodes(GameTestHelper helper) {
        grenadeExplodes(helper, TGGuns.STIELGRANATE.get());
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void fragGrenadeExplodes(GameTestHelper helper) {
        grenadeExplodes(helper, TGGuns.FRAG_GRENADE.get());
    }

    private static void grenadeExplodes(GameTestHelper helper, GrenadeItem item) {
        Player player = shooter(helper);
        ItemStack grenade = new ItemStack(item, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, grenade);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 4));

        // fully wound up (30 ticks)
        item.releaseUsing(grenade, helper.getLevel(), player, item.getUseDuration(grenade, player) - 30);

        helper.assertTrue(grenade.getCount() == 1, "Throwing should use one grenade, count=" + grenade.getCount());
        helper.succeedWhen(() -> helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "Grenade blast should hurt the zombie"));
    }
}
