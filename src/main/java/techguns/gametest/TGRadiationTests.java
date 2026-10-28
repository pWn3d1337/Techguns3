package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.Techguns;
import techguns.core.radiation.RadiationRules;
import techguns.radiation.ItemRadiation;
import techguns.radiation.PlayerRadiation;
import techguns.radiation.RadiationArea;
import techguns.radiation.RadiationEvents;
import techguns.radiation.RadiationSources;
import techguns.registry.TGAttributes;
import techguns.registry.TGItems;
import techguns.registry.TGMobEffects;

/** Radiation (Techguns 2 {@code TGRadiationSystem}): the rules, the effects on players and mobs, items and poisoning. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGRadiationTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA)
    public static void radiationRules(GameTestHelper helper) {
        helper.assertTrue(RadiationRules.dose(4, 0) == 5 && RadiationRules.dose(4, 2.0) == 3 && RadiationRules.dose(0, 3.0) == 0,
                "Dose is the effect level minus the resistance");
        helper.assertTrue(RadiationRules.add(990, 50) == RadiationRules.MAX && RadiationRules.add(10, -50) == 0, "Level stays in 0..1000");
        helper.assertTrue(RadiationRules.stage(499) == RadiationRules.Stage.NONE && RadiationRules.stage(500) == RadiationRules.Stage.MINOR
                && RadiationRules.stage(750) == RadiationRules.Stage.SEVERE && RadiationRules.stage(1000) == RadiationRules.Stage.LETHAL, "Stages");
        helper.assertTrue(RadiationRules.strengthAt(1, 10, 2, 9, 2) == 9 && RadiationRules.strengthAt(10, 10, 2, 9, 2) == -1
                && RadiationRules.strengthAt(6, 10, 2, 9, 2) == 6, "Area strength falls from inner to outer");
        helper.assertTrue(RadiationRules.fade(10, 100, 9) == 9 && RadiationRules.fade(75, 100, 8) == 4 && RadiationRules.fade(100, 100, 9) == 0,
                "Areas fade over their second half");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void radiationEffectIrradiatesPlayers(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        TGMobEffects.RADIATION.value().applyEffectTick(player, 4);
        helper.assertTrue(PlayerRadiation.get(player) == 5, "Radiation V gives 5 per second, got " + PlayerRadiation.get(player));

        player.getAttribute(TGAttributes.RADIATION_RESISTANCE).addTransientModifier(
                new AttributeModifier(TGIds.id("test"), 2.0, AttributeModifier.Operation.ADD_VALUE));
        TGMobEffects.RADIATION.value().applyEffectTick(player, 4);
        helper.assertTrue(PlayerRadiation.get(player) == 8, "Resistance 2 takes 2 off, got " + PlayerRadiation.get(player));

        TGMobEffects.RAD_REGENERATION.value().applyEffectTick(player, 2);
        helper.assertTrue(PlayerRadiation.get(player) == 5, "Rad regeneration III takes 3 away, got " + PlayerRadiation.get(player));

        Player creative = helper.makeMockPlayer(GameType.CREATIVE);
        TGMobEffects.RADIATION.value().applyEffectTick(creative, 4);
        helper.assertTrue(PlayerRadiation.get(creative) == 0, "Creative players collect no radiation");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void radResistanceAddsResistance(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.addEffect(new MobEffectInstance(TGMobEffects.RAD_RESISTANCE, 200, 2));
        double resistance = player.getAttributeValue(TGAttributes.RADIATION_RESISTANCE);
        helper.assertTrue(resistance == 3.0, "Rad resistance III gives 3 resistance, got " + resistance);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void radiationEffectHurtsMobs(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
        float health = zombie.getHealth();
        TGMobEffects.RADIATION.value().applyEffectTick(zombie, 4);
        helper.assertTrue(Math.abs(health - zombie.getHealth() - 2.5f) < 0.01f, "Radiation V does 2.5 damage to mobs, did " + (health - zombie.getHealth()));
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void radioactiveItemsIrradiateTheInventory(GameTestHelper helper) {
        helper.assertTrue(ItemRadiation.of(new ItemStack(TGItems.ENRICHED_URANIUM.get())) == 3
                && ItemRadiation.of(new ItemStack(TGItems.RAW_URANIUM.get())) == 1
                && ItemRadiation.of(new ItemStack(Items.IRON_INGOT)) == 0, "Item radiation data map");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(TGItems.RAW_URANIUM.get()));
        player.getInventory().add(new ItemStack(TGItems.ENRICHED_URANIUM.get()));
        RadiationEvents.inventoryRadiation(player);
        MobEffectInstance effect = player.getEffect(TGMobEffects.RADIATION);
        helper.assertTrue(effect != null && effect.getAmplifier() == 2 && effect.getDuration() == RadiationRules.INVENTORY_INTERVAL,
                "The strongest item counts: radiation III for 3 s, got " + effect);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void radiationPoisoningStages(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        PlayerRadiation.set(player, 600);
        RadiationEvents.poisoning(player);
        helper.assertTrue(player.hasEffect(MobEffects.HUNGER) && !player.hasEffect(MobEffects.WEAKNESS), "Minor poisoning: hunger");

        PlayerRadiation.set(player, 800);
        RadiationEvents.poisoning(player);
        helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS) && player.hasEffect(MobEffects.DIG_SLOWDOWN), "Severe poisoning: weakness, fatigue");

        PlayerRadiation.set(player, 1000);
        float health = player.getHealth();
        RadiationEvents.poisoning(player);
        helper.assertTrue(player.hasEffect(MobEffects.CONFUSION) && player.getHealth() < health, "Lethal poisoning: nausea and damage");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void radiationPotionsBrew(GameTestHelper helper) {
        var brewing = helper.getLevel().potionBrewing();
        ItemStack awkward = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
        ItemStack radiation = PotionContents.createItemStack(Items.POTION, TGMobEffects.RADIATION_POTION);
        helper.assertTrue(brewing.hasMix(awkward, new ItemStack(TGItems.RAW_URANIUM.get()))
                && brewing.hasMix(awkward, new ItemStack(TGItems.LEAD_INGOT.get()))
                && brewing.hasMix(radiation, new ItemStack(Items.GOLDEN_CARROT)), "Radiation potions brew");
        ItemStack mixed = brewing.mix(new ItemStack(TGItems.ENRICHED_URANIUM.get()), awkward);
        helper.assertTrue(mixed.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS).is(TGMobEffects.SEVERE_RADIATION_POTION),
                "Enriched uranium brews severe radiation");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void radMedicineCuresRadiation(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack radAway = new ItemStack(TGItems.RAD_AWAY.get());
        ItemStack rest = radAway.finishUsingItem(helper.getLevel(), player);
        MobEffectInstance regeneration = player.getEffect(TGMobEffects.RAD_REGENERATION);
        helper.assertTrue(regeneration != null && regeneration.getAmplifier() == 14 && regeneration.getDuration() == 400,
                "RadAway gives rad regeneration XV for 20 s, got " + regeneration);
        helper.assertTrue(rest.is(TGItems.INFUSION_BAG.get()), "RadAway leaves its infusion bag, got " + rest);

        Player other = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pills = new ItemStack(TGItems.RAD_PILLS.get(), 2);
        new ItemStack(TGItems.RAD_PILLS.get()).finishUsingItem(helper.getLevel(), other);
        pills.finishUsingItem(helper.getLevel(), other);
        MobEffectInstance resistance = other.getEffect(TGMobEffects.RAD_RESISTANCE);
        helper.assertTrue(resistance != null && resistance.getAmplifier() == 2 && other.hasEffect(TGMobEffects.RAD_REGENERATION),
                "Rad pills give rad resistance III and rad regeneration");
        helper.assertTrue(pills.getCount() == 1 && other.getInventory().contains(new ItemStack(Items.GLASS_BOTTLE)),
                "A pill from a stack goes, its bottle into the inventory");
        var recipes = helper.getLevel().getRecipeManager();
        helper.assertTrue(recipes.byKey(TGIds.id("chem_lab/rad_away")).isPresent() && recipes.byKey(TGIds.id("chem_lab/rad_pills")).isPresent(),
                "Chem Lab recipes for the rad medicine");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void radiationSourcesIrradiateAround(GameTestHelper helper) {
        Zombie near = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        Zombie far = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 1, 1));
        net.minecraft.world.phys.Vec3 center = near.position();
        RadiationSources.irradiate(helper.getLevel(), center, 6.0, 60, 4, 4.0, 0);
        MobEffectInstance nearEffect = near.getEffect(TGMobEffects.RADIATION);
        MobEffectInstance farEffect = far.getEffect(TGMobEffects.RADIATION);
        helper.assertTrue(nearEffect != null && nearEffect.getAmplifier() == 4, "Full strength inside the inner radius, got " + nearEffect);
        helper.assertTrue(farEffect != null && farEffect.getAmplifier() == 2, "Half way between inner radius and edge: half, got " + farEffect);

        Zombie ndrTarget = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 6));
        RadiationSources.ndrHit(ndrTarget);
        MobEffectInstance ndr = ndrTarget.getEffect(TGMobEffects.RADIATION);
        helper.assertTrue(ndr != null && ndr.getAmplifier() == 4 && ndr.getDuration() == 40, "NDR hits give radiation V for 2 s, got " + ndr);
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void radiationAreaPulses(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
        zombie.setNoAi(true);
        net.minecraft.world.phys.Vec3 at = helper.absoluteVec(new net.minecraft.world.phys.Vec3(3.5, 1, 3.5));
        RadiationArea area = RadiationArea.create(helper.getLevel(), at.x, at.y, at.z, 200, 2.0f, 3, 5.0f, 1);
        helper.getLevel().addFreshEntity(area);
        helper.succeedWhen(() -> {
            MobEffectInstance effect = zombie.getEffect(TGMobEffects.RADIATION);
            helper.assertTrue(effect != null && effect.getAmplifier() == 3, "The area gives radiation IV inside, got " + effect);
            area.discard();
        });
    }
}

