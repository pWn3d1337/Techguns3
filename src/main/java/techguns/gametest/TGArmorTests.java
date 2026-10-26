package techguns.gametest;

import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.Techguns;
import techguns.armor.ArmorBonusEvents;
import techguns.armor.TGArmorItem;
import techguns.core.damage.TGDamageCategory;
import techguns.registry.TGArmors;
import techguns.registry.TGAttributes;

/**
 * Techguns armor (Phase 4 batch 4a): per-category armor as attributes, toughness and bonuses, worn-out pieces give
 * nothing and never break, bonus attributes act like Techguns 2.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGArmorTests {
    private static final String ARENA = "empty_8x4x8";
    private static final double EPSILON = 1.0e-4;

    /** Sum of an attribute's modifiers on a stack. */
    private static double modifier(ItemStack stack, Holder<Attribute> attribute) {
        return stack.getItem().getDefaultAttributeModifiers(stack).modifiers().stream()
                .filter(entry -> entry.attribute().value() == attribute.value())
                .mapToDouble(entry -> entry.modifier().amount()).sum();
    }

    private static boolean near(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    @GameTest(template = ARENA)
    public static void pieceArmorFollowsCategoryValues(GameTestHelper helper) {
        ItemStack vest = new ItemStack(TGArmors.T1_COMBAT_CHESTPLATE.get());

        double armor = modifier(vest, Attributes.ARMOR);
        double projectile = modifier(vest, TGAttributes.bonusArmor(TGDamageCategory.PROJECTILE));
        double fire = modifier(vest, TGAttributes.bonusArmor(TGDamageCategory.FIRE));
        double radiation = modifier(vest, TGAttributes.bonusArmor(TGDamageCategory.RADIATION));

        // set: 15 physical/projectile, 11.25 elemental, 0 radiation; chest share 30%
        helper.assertTrue(near(armor, 4.5), "Vest armor should be 4.5, was " + armor);
        helper.assertTrue(near(projectile, 0.0), "Projectile armor equals physical, bonus should be 0, was " + projectile);
        // fire: 3.375 total, regular armor counts half (2.25) -> bonus 1.125
        helper.assertTrue(near(fire, 1.125), "Fire bonus should be 1.125, was " + fire);
        // radiation: 0 total, regular armor does not count -> no modifier
        helper.assertTrue(near(radiation, 0.0), "Radiation bonus should be 0, was " + radiation);
        helper.assertTrue(near(modifier(vest, Attributes.ARMOR_TOUGHNESS), 0.5), "Vest toughness should be 0.5");
        helper.assertTrue(near(modifier(vest, Attributes.KNOCKBACK_RESISTANCE), 0.2), "Vest knockback resistance should be 0.2");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void durabilityAndRepairFollowTechguns2(GameTestHelper helper) {
        ItemStack helmet = new ItemStack(TGArmors.T1_COMBAT_HELMET.get());
        TGArmorItem item = TGArmors.T1_COMBAT_HELMET.get();

        // round(0.25 * 55 * 60)
        helper.assertTrue(helmet.getMaxDamage() == 825, "Helmet durability should be 825, was " + helmet.getMaxDamage());
        helper.assertTrue(item.isValidRepairItem(helmet, new ItemStack(Items.IRON_INGOT)), "Iron should repair the helmet");
        helper.assertFalse(item.isValidRepairItem(helmet, new ItemStack(Items.LEATHER)), "Leather should not repair the helmet");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void wornOutArmorGivesNothingAndNeverBreaks(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack boots = new ItemStack(TGArmors.T1_COMBAT_BOOTS.get());
        boots.setDamageValue(boots.getMaxDamage() - 3);

        boots.hurtAndBreak(10, helper.getLevel(), player, item -> { });

        helper.assertFalse(boots.isEmpty(), "Worn-out armor should not break");
        helper.assertTrue(boots.getDamageValue() == boots.getMaxDamage() - 1, "Damage should stop one below max, was " + boots.getDamageValue());
        helper.assertTrue(TGArmorItem.isWornOut(boots), "The boots should be worn out");
        helper.assertTrue(modifier(boots, Attributes.ARMOR) == 0.0, "Worn-out boots should give no armor");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void speedBonusDoublesWhileSprinting(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getAttribute(TGAttributes.SPEED_BONUS).addTransientModifier(
                new AttributeModifier(TGIds.id("test_speed"), 0.1, AttributeModifier.Operation.ADD_VALUE));

        double walking = ArmorBonusEvents.speedBonus(player);
        player.setSprinting(true);
        double sprinting = ArmorBonusEvents.speedBonus(player);

        helper.assertTrue(near(walking, 0.1) && near(sprinting, 0.2), "Speed bonus should be 0.1 walking, 0.2 sprinting, was "
                + walking + " / " + sprinting);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void fallProtectionReducesFallDistance(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getAttribute(TGAttributes.FALL_FREE_HEIGHT).addTransientModifier(
                new AttributeModifier(TGIds.id("test_free"), 1.0, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(TGAttributes.FALL_DAMAGE_REDUCTION).addTransientModifier(
                new AttributeModifier(TGIds.id("test_reduction"), 0.2, AttributeModifier.Operation.ADD_VALUE));

        LivingFallEvent event = NeoForge.EVENT_BUS.post(new LivingFallEvent(player, 11.0f, 1.0f));

        // (11 - 1) * (1 - 0.2)
        helper.assertTrue(near(event.getDistance(), 8.0), "Fall distance should be 8, was " + event.getDistance());
        helper.succeed();
    }
}
