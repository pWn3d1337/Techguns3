package techguns.gametest;

import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.TGConfig;
import techguns.Techguns;
import techguns.item.AmmoBagData;
import techguns.registry.TGItems;
import techguns.slot.item.AutoFeederItem;
import techguns.player.TGPlayerData;
import techguns.registry.TGAmmo;
import techguns.registry.TGAttributes;
import techguns.registry.TGSlotItems;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/**
 * Extra slot gear: charge use, refills and the bonuses worn gear gives. The slot items are exercised through their
 * own tick; the attribute checks go through {@link ExtraSlots}, so they hold for Curios and the fallback slots alike.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGSlotItemTests {
    private static final String ARENA = "empty_8x4x8";

    /** Puts the gear into its slot; false if the active slot provider has no such slot for a mock player. */
    private static boolean equip(Player player, ItemStack stack) {
        TGSlotItem item = (TGSlotItem) stack.getItem();
        return ExtraSlots.set(player, item.slot(), stack) && ExtraSlots.get(player, item.slot()).getItem() == stack.getItem();
    }

    private static double bonus(Player player, Holder<Attribute> attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }

    private static void tick(Player player) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(player));
    }

    @GameTest(template = ARENA)
    public static void gasMaskRemovesPoisonAndWitherForOneChargeEach(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack mask = new ItemStack(TGSlotItems.GAS_MASK.get());
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));

        ((TGSlotItem) mask.getItem()).slotTick(player, mask);

        helper.assertTrue(!player.hasEffect(MobEffects.POISON), "Poison should be removed");
        helper.assertTrue(!player.hasEffect(MobEffects.WITHER), "Wither should be removed");
        helper.assertTrue(mask.getDamageValue() == 2, "Two effects should cost 2 charge, damage " + mask.getDamageValue());
        helper.assertTrue(((TGSlotItem) mask.getItem()).activeBonuses(player, mask).containsKey(TGAttributes.RADIATION_RESISTANCE),
                "The gas mask should give radiation resistance");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void emptyGasMaskDoesNothing(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack mask = new ItemStack(TGSlotItems.GAS_MASK.get());
        mask.setDamageValue(mask.getMaxDamage());
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));

        ((TGSlotItem) mask.getItem()).slotTick(player, mask);

        helper.assertTrue(player.hasEffect(MobEffects.POISON), "A worn-out mask should not remove poison");
        helper.assertTrue(((TGSlotItem) mask.getItem()).activeBonuses(player, mask).isEmpty(), "A worn-out mask gives no bonus");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void scubaTanksRefillAirOnlyWithOxygenGear(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack tanks = new ItemStack(TGSlotItems.SCUBA_TANKS.get());
        TGSlotItem item = (TGSlotItem) tanks.getItem();

        player.setAirSupply(100);
        item.slotTick(player, tanks);
        helper.assertTrue(player.getAirSupply() == 100 && tanks.getDamageValue() == 0, "Without oxygen gear the tanks do nothing");

        // the tactical mask gives oxygen gear; standing in for it with a modifier of the same size
        helper.assertTrue(((TGSlotItem) new ItemStack(TGSlotItems.TACTICAL_MASK.get()).getItem())
                .activeBonuses(player, new ItemStack(TGSlotItems.TACTICAL_MASK.get())).containsKey(TGAttributes.OXYGEN_GEAR),
                "The tactical mask should give oxygen gear");
        player.getAttribute(TGAttributes.OXYGEN_GEAR).addTransientModifier(
                new AttributeModifier(TGIds.id("test_oxygen_gear"), 1.0, AttributeModifier.Operation.ADD_VALUE));
        item.slotTick(player, tanks);
        helper.assertTrue(player.getAirSupply() == 120, "The tanks should add 20 air, got " + player.getAirSupply());
        helper.assertTrue(tanks.getDamageValue() == 1, "Refilling should cost 1 charge, damage " + tanks.getDamageValue());

        player.setAirSupply(300);
        item.slotTick(player, tanks);
        helper.assertTrue(player.getAirSupply() == 300 && tanks.getDamageValue() == 1, "Plenty of air needs no refill");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void jumpPackBonusesNeedTheJetpackToggle(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pack = new ItemStack(TGSlotItems.JUMP_PACK.get());
        TGSlotItem item = (TGSlotItem) pack.getItem();
        TGPlayerData data = TGPlayerData.of(player);

        data.set(TGPlayerData.Toggle.JETPACK, true);
        helper.assertTrue(item.activeBonuses(player, pack).get(TGAttributes.JUMP_BONUS) == 0.8, "Jump bonus 0.8 with the toggle on");
        helper.assertTrue(item.activeBonuses(player, pack).get(TGAttributes.FALL_FREE_HEIGHT) == 8.0, "Fall free height 8");
        helper.assertTrue(item.activeBonuses(player, pack).get(TGAttributes.FALL_DAMAGE_REDUCTION) == 0.2, "Fall damage reduction 0.2");

        data.set(TGPlayerData.Toggle.JETPACK, false);
        helper.assertTrue(item.activeBonuses(player, pack).isEmpty(), "No bonus with the toggle off");

        data.set(TGPlayerData.Toggle.JETPACK, true);
        item.onBonusAction(player, pack, TGAttributes.JUMP_BONUS);
        helper.assertTrue(pack.getDamageValue() == 1, "A boosted jump costs 1 charge");

        if (equip(player, pack)) {
            tick(player);
            helper.assertTrue(Math.abs(bonus(player, TGAttributes.JUMP_BONUS) - 0.8) < 1.0e-6, "Worn pack should give the jump attribute");
            data.set(TGPlayerData.Toggle.JETPACK, false);
            tick(player);
            helper.assertTrue(bonus(player, TGAttributes.JUMP_BONUS) == 0.0, "Toggle off should remove the jump attribute");
        }
        data.set(TGPlayerData.Toggle.JETPACK, true);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void antiGravPackGrantsCreativeFlight(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pack = new ItemStack(TGSlotItems.ANTIGRAV_PACK.get());
        TGSlotItem item = (TGSlotItem) pack.getItem();

        helper.assertTrue(item.activeBonuses(player, pack).get(NeoForgeMod.CREATIVE_FLIGHT) == 1.0, "Charged pack gives creative flight");
        helper.assertTrue(item.activeBonuses(player, pack).get(TGAttributes.FLY_SPEED_BONUS) == 0.25, "Fly speed bonus 0.25");

        player.getAbilities().flying = true;
        item.slotTick(player, pack);
        helper.assertTrue(pack.getDamageValue() == 1, "Flying costs 1 charge per tick, damage " + pack.getDamageValue());
        player.getAbilities().flying = false;
        item.slotTick(player, pack);
        helper.assertTrue(pack.getDamageValue() == 1, "Standing on the ground costs nothing");

        pack.setDamageValue(pack.getMaxDamage());
        helper.assertTrue(item.activeBonuses(player, pack).isEmpty(), "Empty pack gives nothing");

        ItemStack worn = new ItemStack(TGSlotItems.ANTIGRAV_PACK.get());
        if (equip(player, worn)) {
            tick(player);
            helper.assertTrue(bonus(player, NeoForgeMod.CREATIVE_FLIGHT) == 1.0, "Worn pack should set the creative_flight attribute");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void workingGlovesSpeedUpMining(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gloves = new ItemStack(TGSlotItems.WORKING_GLOVES.get());
        TGSlotItem item = (TGSlotItem) gloves.getItem();

        helper.assertTrue(item.activeBonuses(player, gloves).get(TGAttributes.MINING_SPEED_BONUS) == 0.10, "Mining speed bonus 0.10");
        if (equip(player, gloves)) {
            tick(player);
            helper.assertTrue(Math.abs(bonus(player, TGAttributes.MINING_SPEED_BONUS) - 0.10) < 1.0e-6,
                    "Worn gloves should set the mining speed attribute");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void emptyNightVisionGogglesRechargeFromBattery(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack goggles = new ItemStack(TGSlotItems.NIGHT_VISION_GOGGLES.get());
        goggles.setDamageValue(goggles.getMaxDamage());
        TGSlotItem item = (TGSlotItem) goggles.getItem();
        player.getInventory().add(new ItemStack(TGAmmo.REDSTONE_BATTERY.get()));

        item.slotTick(player, goggles);

        helper.assertTrue(goggles.getDamageValue() == 0, "The goggles should be fully charged, damage " + goggles.getDamageValue());
        helper.assertTrue(player.getInventory().countItem(TGAmmo.REDSTONE_BATTERY.get()) == 0, "The battery should be used up");
        helper.assertTrue(player.getInventory().countItem(TGAmmo.REDSTONE_BATTERY_EMPTY.get()) == 1, "An empty battery should come back");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void nightVisionGogglesDrainOnlyWithTheToggle(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack goggles = new ItemStack(TGSlotItems.NIGHT_VISION_GOGGLES.get());
        TGSlotItem item = (TGSlotItem) goggles.getItem();
        TGPlayerData data = TGPlayerData.of(player);

        data.set(TGPlayerData.Toggle.NIGHT_VISION, false);
        item.slotTick(player, goggles);
        helper.assertTrue(goggles.getDamageValue() == 0, "No drain with the toggle off");

        data.set(TGPlayerData.Toggle.NIGHT_VISION, true);
        item.slotTick(player, goggles);
        helper.assertTrue(goggles.getDamageValue() == 1, "1 charge per tick with the toggle on");
        data.set(TGPlayerData.Toggle.NIGHT_VISION, false);
        helper.succeed();
    }

    /** The Auto-Feeder takes only food, up to three stacks, and is worn in the mouth slot. */
    @GameTest(template = ARENA)
    public static void autoFeederStoresFood(GameTestHelper helper) {
        ItemStack feeder = new ItemStack(TGSlotItems.AUTO_FEEDER.get());
        helper.assertTrue(((TGSlotItem) feeder.getItem()).slot() == TGSlotType.MOUTH, "The Auto-Feeder is mouth gear");
        ItemStack iron = new ItemStack(Items.IRON_INGOT, 10);
        helper.assertTrue(AmmoBagData.insert(feeder, iron) == 0, "No iron in the Auto-Feeder");
        helper.assertTrue(AmmoBagData.insert(feeder, new ItemStack(Items.BREAD, 64)) == 64, "Bread goes in");
        helper.assertTrue(AmmoBagData.insert(feeder, new ItemStack(Items.COOKED_BEEF, 200)) == 128, "Three stacks in all");
        helper.assertTrue(AmmoBagData.insert(new ItemStack(TGItems.AMMO_BOX.get()), new ItemStack(Items.BREAD)) == 0, "No food in ammo boxes");
        helper.succeed();
    }

    /**
     * Techguns 2 auto-feeder: a hungry player eats the first stored food; the food points that don't fit are kept on the
     * Auto-Feeder and fed first; a full player isn't fed; config off feeds nothing.
     */
    @GameTest(template = ARENA)
    public static void autoFeederFeedsHungryPlayer(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack feeder = new ItemStack(TGSlotItems.AUTO_FEEDER.get());
        AmmoBagData.insert(feeder, new ItemStack(Items.COOKED_BEEF, 2));
        TGSlotItem item = (TGSlotItem) feeder.getItem();
        player.getFoodData().setFoodLevel(10);
        item.slotTick(player, feeder);
        helper.assertTrue(player.getFoodData().getFoodLevel() == 18 && AmmoBagData.occupancy(feeder) == 1, "A steak (8) eaten at 10");
        item.slotTick(player, feeder);
        helper.assertTrue(player.getFoodData().getFoodLevel() == 20 && AmmoBagData.occupancy(feeder) == 0, "The second steak fills the bar");
        helper.assertTrue(AutoFeederItem.foodLeft(feeder) == 6, "6 points kept, kept " + AutoFeederItem.foodLeft(feeder));
        item.slotTick(player, feeder);
        helper.assertTrue(AutoFeederItem.foodLeft(feeder) == 6, "A full player isn't fed");
        player.getFoodData().setFoodLevel(16);
        item.slotTick(player, feeder);
        helper.assertTrue(player.getFoodData().getFoodLevel() == 20 && AutoFeederItem.foodLeft(feeder) == 2, "The kept points are fed first");

        AmmoBagData.insert(feeder, new ItemStack(Items.BREAD));
        player.getFoodData().setFoodLevel(10);
        boolean before = TGConfig.AUTO_FEEDER.get();
        TGConfig.AUTO_FEEDER.set(false);
        try {
            item.slotTick(player, feeder);
            helper.assertTrue(player.getFoodData().getFoodLevel() == 10, "Auto-feeder off: nothing eaten");
        } finally {
            TGConfig.AUTO_FEEDER.set(before);
        }
        helper.succeed();
    }

    /** Food that leaves a bowl gives the bowl back like eating it by hand. */
    @GameTest(template = ARENA)
    public static void autoFeederGivesBowlsBack(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack feeder = new ItemStack(TGSlotItems.AUTO_FEEDER.get());
        AmmoBagData.insert(feeder, new ItemStack(Items.MUSHROOM_STEW));
        player.getFoodData().setFoodLevel(5);
        AutoFeederItem.feed(player, feeder);
        helper.assertTrue(player.getFoodData().getFoodLevel() == 11, "Stew (6) eaten");
        helper.assertTrue(player.getInventory().countItem(Items.BOWL) == 1, "The bowl is back in the inventory");
        helper.succeed();
    }

    /** With Curios, the slot's visibility toggle decides whether the gear is drawn; the gear keeps working. */
    @GameTest(template = ARENA)
    public static void curiosVisibilityToggleHidesGear(GameTestHelper helper) {
        if (!ExtraSlots.usesCurios()) {
            helper.succeed();
            return;
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(ExtraSlots.isRendered(player, TGSlotType.BACK), "Gear is drawn by default");
        helper.assertTrue(techguns.compat.curios.CuriosExtraSlots.setRendered(player, TGSlotType.BACK, false), "The mock player has a back slot");
        helper.assertTrue(!ExtraSlots.isRendered(player, TGSlotType.BACK), "Hidden in Curios: not drawn");
        helper.assertTrue(ExtraSlots.isRendered(player, TGSlotType.FACE), "Other slots stay visible");
        helper.succeed();
    }
}
