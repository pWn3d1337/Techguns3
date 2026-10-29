package techguns.gametest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.GunHands;
import techguns.gun.GunItem;
import techguns.gun.ShooterState;
import techguns.gun.ammo.AmmoType;
import techguns.registry.TGAmmo;
import techguns.registry.TGGuns;

/** Techguns 2 dual wield rules (a two-handed gun blocks the off hand) and the gun tooltip. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGGunHandTests {
    private static final String ARENA = "empty_8x4x8";

    private static ItemStack loaded(GunItem gun, int rounds) {
        ItemStack stack = new ItemStack(gun);
        GunData.setAmmo(stack, new GunAmmo(rounds, AmmoType.DEFAULT));
        return stack;
    }

    @GameTest(template = ARENA)
    public static void handTypesDecideTheOffhand(GameTestHelper helper) {
        ItemStack rifle = new ItemStack(TGGuns.M4.get());
        ItemStack pistol = new ItemStack(TGGuns.PISTOL.get());
        ItemStack mac10 = new ItemStack(TGGuns.MAC10.get());
        ItemStack torch = new ItemStack(Items.TORCH);
        helper.assertTrue(!GunHands.canUseOffhand(rifle, torch), "A two-handed gun in the main hand blocks the off hand");
        helper.assertTrue(!GunHands.canUseOffhand(pistol, rifle), "A two-handed gun in the off hand can't be used");
        helper.assertTrue(!GunHands.canUseOffhand(ItemStack.EMPTY, rifle), "Not even with an empty main hand");
        helper.assertTrue(GunHands.canUseOffhand(pistol, pistol), "Two one-handed guns can be dual-wielded");
        helper.assertTrue(GunHands.canUseOffhand(mac10, pistol), "One-and-a-half-handed guns can be dual-wielded");
        helper.assertTrue(GunHands.canUseOffhand(pistol, torch) && GunHands.canUseOffhand(ItemStack.EMPTY, torch), "Other items are free");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void twoHandedGunBlocksOffhandFiring(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        GunItem pistol = TGGuns.PISTOL.get();
        ItemStack offhand = loaded(pistol, 18);
        player.setItemInHand(InteractionHand.MAIN_HAND, loaded(TGGuns.M4.get(), 30));
        player.setItemInHand(InteractionHand.OFF_HAND, offhand);
        pistol.shoot(offhand, helper.getLevel(), player, InteractionHand.OFF_HAND, false);
        helper.assertTrue(GunData.loaded(offhand) == 18, "The off-hand pistol doesn't fire next to a rifle");
        GunData.setAmmo(offhand, new GunAmmo(0, AmmoType.DEFAULT));
        player.getInventory().add(new ItemStack(TGAmmo.PISTOL_MAGAZINE.get()));
        pistol.startReload(offhand, helper.getLevel(), player, InteractionHand.OFF_HAND, true);
        helper.assertTrue(ShooterState.of(player).fireDelay(InteractionHand.OFF_HAND) <= 0, "Nor does it start a reload");

        GunData.setAmmo(offhand, new GunAmmo(18, AmmoType.DEFAULT));
        player.setItemInHand(InteractionHand.MAIN_HAND, loaded(pistol, 18));
        pistol.shoot(offhand, helper.getLevel(), player, InteractionHand.OFF_HAND, false);
        helper.assertTrue(GunData.loaded(offhand) == 17, "Next to another pistol it fires");
        helper.succeed();
    }

    /** The off-hand item can't be used on air, blocks or entities; a use started before the rifle stops. */
    @GameTest(template = ARENA)
    public static void twoHandedGunBlocksOffhandUse(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
        player.moveTo(at.x, at.y, at.z);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.gameMode.useItem(player, helper.getLevel(), player.getOffhandItem(), InteractionHand.OFF_HAND);
        helper.assertTrue(player.isUsingItem(), "An empty main hand lets the shield block");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TGGuns.M4.get()));
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertTrue(!player.isUsingItem(), "Taking a rifle lowers the shield");

        player.gameMode.useItem(player, helper.getLevel(), player.getOffhandItem(), InteractionHand.OFF_HAND);
        helper.assertTrue(!player.isUsingItem(), "With a rifle the shield can't block");
        BlockPos floor = helper.absolutePos(new BlockPos(2, 0, 2));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false);
        helper.assertTrue(CommonHooks.onRightClickBlock(player, InteractionHand.OFF_HAND, floor, hit).isCanceled(), "Off-hand block use is cancelled");
        helper.assertTrue(!CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, floor, hit).isCanceled(), "The main hand still works");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    private static List<String> tooltipKeys(GunItem gun, boolean detailed) {
        List<Component> lines = new ArrayList<>();
        gun.appendGunTooltip(new ItemStack(gun), lines, detailed);
        return lines.stream().map(line -> line.getContents() instanceof TranslatableContents t ? t.getKey() : line.getString()).toList();
    }

    /** Techguns 2 tooltip: short with ammo and damage, detailed with all stats; explosives show their radius. */
    @GameTest(template = ARENA)
    public static void gunTooltipFollowsTechguns2(GameTestHelper helper) {
        List<String> short_ = tooltipKeys(TGGuns.M4.get(), false);
        helper.assertTrue(short_.equals(List.of("tooltip.techguns.gun.loaded", "tooltip.techguns.gun.ammo", "tooltip.techguns.gun.damage",
                "tooltip.techguns.shift_for_details")), "Short M4 tooltip: " + short_);
        List<String> detailed = tooltipKeys(TGGuns.M4.get(), true);
        for (String key : List.of("hand_type", "damage_type", "range_drop", "velocity", "spread_zoomed", "clip_size", "reload_time", "zoom")) {
            helper.assertTrue(detailed.contains("tooltip.techguns.gun." + key), "Detailed M4 tooltip has " + key + ": " + detailed);
        }
        helper.assertTrue(tooltipKeys(TGGuns.PISTOL.get(), true).contains("tooltip.techguns.gun.spread"), "Guns without a scope show one spread");
        helper.assertTrue(tooltipKeys(TGGuns.ROCKET_LAUNCHER.get(), true).contains("tooltip.techguns.gun.radius"), "The rocket launcher shows its radius");
        helper.assertTrue(tooltipKeys(TGGuns.LASERGUN.get(), true).contains("tooltip.techguns.gun.range"), "The laser has no damage drop");
        helper.assertTrue(tooltipKeys(TGGuns.COMBAT_SHOTGUN.get(), false).contains("tooltip.techguns.gun.damage_pellets"), "Shotguns show the pellets");
        List<String> drill = tooltipKeys(TGGuns.MINING_DRILL.get(), true);
        helper.assertTrue(drill.containsAll(List.of("tooltip.techguns.tool.mining_head", "tooltip.techguns.tool.break_speed",
                "tooltip.techguns.tool.mining_radius")), "Detailed drill tooltip: " + drill);
        helper.succeed();
    }
}
