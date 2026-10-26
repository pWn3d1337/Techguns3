package techguns.gametest;

import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
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
import techguns.armor.ArmorPowerEvents;
import techguns.armor.TGArmorItem;
import techguns.armor.TGPoweredArmorItem;
import techguns.core.damage.TGDamageCategory;
import techguns.registry.TGAmmo;
import techguns.registry.TGArmors;
import techguns.registry.TGAttributes;
import techguns.registry.TGComponents;

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

    @GameTest(template = ARENA)
    public static void everyArmorPieceHasArmorAndDurability(GameTestHelper helper) {
        for (var holder : TGArmors.armors()) {
            ItemStack stack = new ItemStack(holder.get());
            String id = holder.getId().getPath();
            helper.assertTrue(modifier(stack, Attributes.ARMOR) > 0.0, id + " should have armor");
            helper.assertTrue(stack.getMaxDamage() > 0, id + " should have durability");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void commandoHelmetHasOxygenGearAndWaterMining(GameTestHelper helper) {
        ItemStack helmet = new ItemStack(TGArmors.T2_COMMANDO_HELMET.get());
        helper.assertTrue(near(modifier(helmet, TGAttributes.OXYGEN_GEAR), 1.0), "Commando helmet oxygen gear should be 1");
        helper.assertTrue(near(modifier(helmet, TGAttributes.WATER_MINING_SPEED_BONUS), 1.25), "Commando helmet water mining should be 1.25");
        helper.assertTrue(near(modifier(helmet, TGAttributes.GUN_ACCURACY), 0.05), "Commando helmet gun accuracy should be 0.05");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void hazmatBootsHaveFallProtectionAndRadiationResistance(GameTestHelper helper) {
        ItemStack boots = new ItemStack(TGArmors.HAZMAT_BOOTS.get());
        helper.assertTrue(near(modifier(boots, TGAttributes.FALL_DAMAGE_REDUCTION), 0.1), "Hazmat boots fall reduction should be 0.1");
        helper.assertTrue(near(modifier(boots, TGAttributes.FALL_FREE_HEIGHT), 0.5), "Hazmat boots free height should be 0.5");
        helper.assertTrue(near(modifier(boots, TGAttributes.RADIATION_RESISTANCE), 1.0), "Hazmat boots radiation resistance should be 1");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void minerHelmetHasMiningBonus(GameTestHelper helper) {
        ItemStack helmet = new ItemStack(TGArmors.T1_MINER_HELMET.get());
        helper.assertTrue(near(modifier(helmet, TGAttributes.MINING_SPEED_BONUS), 0.05), "Miner helmet mining bonus should be 0.05");
        helper.assertTrue(near(modifier(helmet, TGAttributes.SPEED_BONUS), 0.08), "Miner helmet speed bonus should be 0.08");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void hazmatSuitHasElementalArmorAndRadiation(GameTestHelper helper) {
        ItemStack chest = new ItemStack(TGArmors.HAZMAT_CHESTPLATE.get());
        // radiation 20 * 30% = 6, poison 20 * 30% = 6; regular armor does not count against either
        helper.assertTrue(near(modifier(chest, TGAttributes.bonusArmor(TGDamageCategory.RADIATION)), 6.0), "Hazmat chest radiation armor should be 6");
        helper.assertTrue(near(modifier(chest, TGAttributes.bonusArmor(TGDamageCategory.POISON)), 6.0), "Hazmat chest poison armor should be 6");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void poweredChestplateBonusesDependOnPower(GameTestHelper helper) {
        ItemStack empty = new ItemStack(TGArmors.T3_POWER_CHESTPLATE.get());
        ItemStack powered = new ItemStack(TGArmors.T3_POWER_CHESTPLATE.get());
        powered.set(TGComponents.ARMOR_POWER.get(), TGArmors.T3_POWER_CHESTPLATE.get().maxPower());

        helper.assertTrue(near(modifier(powered, TGAttributes.SPEED_BONUS), 0.05), "Powered speed bonus should be 0.05");
        helper.assertTrue(near(modifier(powered, TGAttributes.MINING_SPEED_BONUS), 0.05), "Powered mining bonus should be 0.05");
        helper.assertTrue(near(modifier(powered, Attributes.MAX_HEALTH), 2.0), "Powered health bonus should be 2");
        helper.assertTrue(near(modifier(powered, Attributes.KNOCKBACK_RESISTANCE), 0.35), "Powered knockback resistance should be 0.35");
        helper.assertTrue(near(modifier(powered, TGAttributes.RADIATION_RESISTANCE), 0.75), "Powered radiation resistance should be 0.75");
        // unpowered values of the two-value setters are all 0, single-value setters are powered only
        helper.assertTrue(near(modifier(empty, TGAttributes.SPEED_BONUS), 0.0), "Unpowered speed bonus should be 0");
        helper.assertTrue(near(modifier(empty, TGAttributes.MINING_SPEED_BONUS), 0.0), "Unpowered mining bonus should be 0");
        helper.assertTrue(near(modifier(empty, Attributes.MAX_HEALTH), 0.0), "Unpowered health bonus should be 0");
        helper.assertTrue(near(modifier(empty, Attributes.KNOCKBACK_RESISTANCE), 0.0), "Unpowered knockback resistance should be 0");
        helper.assertTrue(near(modifier(empty, TGAttributes.RADIATION_RESISTANCE), 0.75), "Radiation resistance should not depend on power");
        // armor values do not depend on power
        helper.assertTrue(near(modifier(powered, Attributes.ARMOR), modifier(empty, Attributes.ARMOR)), "Armor should not depend on power");
        helper.assertTrue(modifier(empty, Attributes.ARMOR) > 0.0, "Unpowered chestplate should still give armor");
        helper.assertTrue(near(modifier(powered, TGAttributes.bonusArmor(TGDamageCategory.FIRE)),
                modifier(empty, TGAttributes.bonusArmor(TGDamageCategory.FIRE))), "Elemental armor should not depend on power");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void consumptionTickRefillsFromBatteryAndSharesPower(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack chest = new ItemStack(TGArmors.STEAM_CHESTPLATE.get());
        ItemStack boots = new ItemStack(TGArmors.STEAM_BOOTS.get());
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        player.setItemSlot(EquipmentSlot.FEET, boots);
        player.getInventory().add(new ItemStack(TGAmmo.COMPRESSED_AIR_TANK.get()));

        ArmorPowerEvents.consumptionTick(player, false);

        int bootsMax = TGArmors.STEAM_BOOTS.get().maxPower();
        int chestMax = TGArmors.STEAM_CHESTPLATE.get().maxPower();
        helper.assertTrue(TGPoweredArmorItem.power(boots) == bootsMax, "Boots should be topped up to " + bootsMax + ", was " + TGPoweredArmorItem.power(boots));
        helper.assertTrue(TGPoweredArmorItem.power(chest) == chestMax - bootsMax,
                "Chestplate should hold " + (chestMax - bootsMax) + ", was " + TGPoweredArmorItem.power(chest));
        helper.assertFalse(player.getInventory().contains(new ItemStack(TGAmmo.COMPRESSED_AIR_TANK.get())), "The full tank should be used up");
        helper.assertTrue(player.getInventory().contains(new ItemStack(TGAmmo.COMPRESSED_AIR_TANK_EMPTY.get())), "An empty tank should be given back");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void consumptionTickUsesOnePowerPerWornPiece(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack chest = new ItemStack(TGArmors.T3_POWER_CHESTPLATE.get());
        ItemStack helmet = new ItemStack(TGArmors.T3_POWER_HELMET.get());
        ItemStack steamBoots = new ItemStack(TGArmors.STEAM_BOOTS.get());
        chest.set(TGComponents.ARMOR_POWER.get(), 100);
        helmet.set(TGComponents.ARMOR_POWER.get(), 10);
        steamBoots.set(TGComponents.ARMOR_POWER.get(), 10);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        player.setItemSlot(EquipmentSlot.HEAD, helmet);
        player.setItemSlot(EquipmentSlot.FEET, steamBoots);

        ArmorPowerEvents.consumptionTick(player, true);

        helper.assertTrue(TGPoweredArmorItem.power(chest) == 99, "Chestplate should lose 1, was " + TGPoweredArmorItem.power(chest));
        helper.assertTrue(TGPoweredArmorItem.power(helmet) == 9, "Helmet should lose 1, was " + TGPoweredArmorItem.power(helmet));
        helper.assertTrue(TGPoweredArmorItem.power(steamBoots) == 9, "Boots should lose 1, was " + TGPoweredArmorItem.power(steamBoots));
        helper.succeed();
    }
}
