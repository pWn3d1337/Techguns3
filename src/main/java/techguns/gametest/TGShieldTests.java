package techguns.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.core.damage.DeathType;
import techguns.damage.TGDamage;
import techguns.registry.TGArmors;

/**
 * Shields (Phase 4 batch 4e): blocking Techguns damage lets part of it through by the shield's stats; vanilla damage
 * stays fully blocked.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGShieldTests {
    private static final String ARENA = "empty_8x4x8";

    /** Fires the shield block event for a player using {@code shield} and returns the damage that gets through. */
    private static float through(GameTestHelper helper, ItemStack shield, DamageSource source, float damage) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.OFF_HAND, shield);
        player.startUsingItem(InteractionHand.OFF_HAND);
        LivingShieldBlockEvent event = NeoForge.EVENT_BUS.post(new LivingShieldBlockEvent(player, new DamageContainer(source, damage), true));
        return damage - event.getBlockedDamage();
    }

    @GameTest(template = ARENA)
    public static void riotShieldLetsPartOfABulletThrough(GameTestHelper helper) {
        float through = through(helper, new ItemStack(TGArmors.RIOT_SHIELD.get()),
                TGDamage.bullet(helper.getLevel(), null, null, DeathType.DEFAULT), 10.0f);

        // projectile: 65% blocked
        helper.assertTrue(Math.abs(through - 3.5f) < 1.0e-4, "3.5 of 10 bullet damage should get through, got " + through);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void advancedShieldThresholdDropsWithPenetration(GameTestHelper helper) {
        ItemStack shield = new ItemStack(TGArmors.ADVANCED_SHIELD.get());

        // 10 * (1 - 0.9) = 1, minus threshold 3 -> 0; with penetration 1 the threshold is 1 -> 0; energy 20 * 0.2 = 4 - 3 = 1
        float plain = through(helper, shield, TGDamage.bullet(helper.getLevel(), null, null, DeathType.DEFAULT), 10.0f);
        float energy = through(helper, shield, TGDamage.energy(helper.getLevel(), null, null, DeathType.DEFAULT), 20.0f);
        float piercing = through(helper, shield,
                TGDamage.energy(helper.getLevel(), null, null, DeathType.DEFAULT).setArmorPenetration(1.0f), 20.0f);

        helper.assertTrue(plain == 0.0f, "The threshold should stop a weak bullet, got " + plain);
        helper.assertTrue(Math.abs(energy - 1.0f) < 1.0e-4, "1 of 20 energy damage should get through, got " + energy);
        helper.assertTrue(Math.abs(piercing - 3.0f) < 1.0e-4, "Penetration should lower the threshold to 1, got " + piercing);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void heavyShieldBlocksEnergy(GameTestHelper helper) {
        // energy: 70% blocked -> 6 of 20, minus threshold 2 -> 4
        float energy = through(helper, new ItemStack(TGArmors.HEAVY_SHIELD.get()),
                TGDamage.energy(helper.getLevel(), null, null, DeathType.DEFAULT), 20.0f);

        helper.assertTrue(Math.abs(energy - 4.0f) < 1.0e-4, "4 of 20 energy damage should get through, got " + energy);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void vanillaDamageStaysFullyBlocked(GameTestHelper helper) {
        float vanilla = through(helper, new ItemStack(Items.SHIELD), helper.getLevel().damageSources().generic(), 10.0f);
        float bullet = through(helper, new ItemStack(Items.SHIELD), TGDamage.bullet(helper.getLevel(), null, null, DeathType.DEFAULT), 10.0f);

        helper.assertTrue(vanilla == 0.0f, "Vanilla damage should stay fully blocked, got " + vanilla);
        // vanilla shield: 35% against projectiles
        helper.assertTrue(Math.abs(bullet - 6.5f) < 1.0e-4, "A vanilla shield should let 6.5 of 10 bullet damage through, got " + bullet);
        helper.succeed();
    }
}
