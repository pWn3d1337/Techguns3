package techguns.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.Techguns;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGArmorFormula;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.registry.TGAttributes;

/**
 * Damage system tests on real entities. Armor is set as the attribute base value, since equipment attributes
 * would only apply on the entity's next tick.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGDamageTests {
    private static final float EPS = 0.01f;
    private static final float DAMAGE = 10.0f;

    private static Zombie armoredZombie(GameTestHelper helper) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(8.0);
        return zombie;
    }

    private static void assertHealthLoss(GameTestHelper helper, Zombie zombie, float expectedLoss) {
        float loss = zombie.getMaxHealth() - zombie.getHealth();
        helper.assertTrue(Math.abs(loss - expectedLoss) < EPS, "Expected damage " + expectedLoss + " but was " + loss);
    }

    @GameTest(template = "empty")
    public static void bulletUsesFullArmor(GameTestHelper helper) {
        Zombie zombie = armoredZombie(helper);
        zombie.hurt(TGDamage.bullet(helper.getLevel(), null, null, DeathType.DEFAULT), DAMAGE);
        assertHealthLoss(helper, zombie, TGArmorFormula.damageAfterArmor(DAMAGE, 8f, 0f, 0f));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void energyUsesHalfArmor(GameTestHelper helper) {
        Zombie zombie = armoredZombie(helper);
        zombie.hurt(TGDamage.energy(helper.getLevel(), null, null, DeathType.LASER), DAMAGE);
        assertHealthLoss(helper, zombie, TGArmorFormula.damageAfterArmor(DAMAGE, 4f, 0f, 0f));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void poisonIgnoresArmor(GameTestHelper helper) {
        Zombie zombie = armoredZombie(helper);
        zombie.hurt(TGDamage.poison(helper.getLevel(), null, null, DeathType.BIO), DAMAGE);
        assertHealthLoss(helper, zombie, DAMAGE);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void penetrationReducesArmor(GameTestHelper helper) {
        Zombie zombie = armoredZombie(helper);
        TGDamageSource source = TGDamage.bullet(helper.getLevel(), null, null, DeathType.DEFAULT).setArmorPenetration(1.0f);
        zombie.hurt(source, DAMAGE);
        // penetration 1 per piece -> 4 armor ignored
        assertHealthLoss(helper, zombie, TGArmorFormula.damageAfterArmor(DAMAGE, 8f, 0f, 1f));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bonusArmorAttributeApplies(GameTestHelper helper) {
        Zombie zombie = armoredZombie(helper);
        zombie.getAttribute(TGAttributes.bonusArmor(TGDamageCategory.ENERGY)).addPermanentModifier(
                new AttributeModifier(TGIds.id("test_bonus"), 6.0, AttributeModifier.Operation.ADD_VALUE));
        zombie.hurt(TGDamage.energy(helper.getLevel(), null, null, DeathType.LASER), DAMAGE);
        // 8 armor * 0.5 + 6 bonus = 10
        assertHealthLoss(helper, zombie, TGArmorFormula.damageAfterArmor(DAMAGE, 10f, 0f, 0f));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rapidHitsBypassHurtCooldown(GameTestHelper helper) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        zombie.hurt(TGDamage.bullet(helper.getLevel(), null, null, DeathType.DEFAULT), 3f);
        zombie.hurt(TGDamage.bullet(helper.getLevel(), null, null, DeathType.DEFAULT), 3f);
        assertHealthLoss(helper, zombie, 6f);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vanillaDamageIsClassified(GameTestHelper helper) {
        var sources = helper.getLevel().damageSources();
        helper.assertTrue(TGDamage.categoryOf(sources.fall()) == TGDamageCategory.UNRESISTABLE, "fall should be unresistable");
        helper.assertTrue(TGDamage.categoryOf(sources.onFire()) == TGDamageCategory.UNRESISTABLE, "onFire bypasses armor in vanilla");
        helper.assertTrue(TGDamage.categoryOf(sources.lava()) == TGDamageCategory.FIRE, "lava should be fire");
        helper.assertTrue(TGDamage.categoryOf(sources.cactus()) == TGDamageCategory.PHYSICAL, "cactus should be physical");
        helper.assertTrue(TGDamage.categoryOf(TGDamage.bullet(helper.getLevel(), null, null, DeathType.DEFAULT)) == TGDamageCategory.PROJECTILE,
                "bullet should be projectile");
        helper.succeed();
    }
}
