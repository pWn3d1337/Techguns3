package techguns.gametest;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.armor.TGArmorItem;
import techguns.core.damage.TGDamageCategory;
import techguns.gun.GunItem;
import techguns.gun.GunStats;
import techguns.network.StatPayloads;
import techguns.registry.TGArmors;
import techguns.registry.TGAttributes;
import techguns.registry.TGGuns;
import techguns.stats.ArmorStatOverride;
import techguns.stats.StatOverrideLoader;
import techguns.stats.StatOverrides;

/**
 * Datapack stat overrides for guns and armor (replace the Techguns 2 CraftTweaker tweakers). The overrides are global,
 * so every test sets them, checks and restores them in the same call: other tests never see them.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGStatOverrideTests {
    private static final String ARENA = "empty_8x4x8";
    private static final double EPSILON = 1.0e-4;

    private static ResourceLocation id(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    private static JsonElement json(String text) {
        return JsonParser.parseString(text);
    }

    private static double modifier(ItemStack stack, Holder<Attribute> attribute) {
        return stack.getItem().getDefaultAttributeModifiers(stack).modifiers().stream()
                .filter(entry -> entry.attribute().value() == attribute.value())
                .mapToDouble(entry -> entry.modifier().amount()).sum();
    }

    private static boolean near(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    @GameTest(template = ARENA)
    public static void gunOverrideChangesOnlyTheListedFields(GameTestHelper helper) {
        GunItem m4 = TGGuns.M4.get();
        GunStats base = m4.defaultStats();
        Map<ResourceLocation, GunStats> loaded = StatOverrideLoader.loadGuns(Map.of(id(m4),
                json("{\"damage\": {\"damage\": 20.0}, \"firing\": {\"clip_size\": 50}}")));
        Map<ResourceLocation, GunStats> previous = StatOverrides.gunsById();
        try {
            StatOverrides.setGuns(loaded);
            GunStats stats = m4.stats();
            helper.assertTrue(stats.damage().damage() == 20.0f, "Damage should be overridden, was " + stats.damage().damage());
            helper.assertTrue(stats.firing().clipSize() == 50, "Clip size should be overridden, was " + stats.firing().clipSize());
            helper.assertTrue(stats.damage().damageMin() == base.damage().damageMin()
                    && stats.firing().fireDelay() == base.firing().fireDelay()
                    && stats.projectile().equals(base.projectile()), "Fields left out should keep the built-in values");
            helper.assertTrue(TGGuns.AK47.get().stats() == TGGuns.AK47.get().defaultStats(), "Other guns should keep their stats");
        } finally {
            StatOverrides.setGuns(previous);
        }
        helper.assertTrue(m4.stats() == base, "Removing the override should restore the built-in stats");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void brokenOverridesAreSkipped(GameTestHelper helper) {
        Map<ResourceLocation, GunStats> guns = StatOverrideLoader.loadGuns(Map.of(
                ResourceLocation.withDefaultNamespace("stone"), json("{\"damage\": {\"damage\": 20.0}}"),
                id(TGGuns.M4.get()), json("{\"firing\": {\"clip_size\": \"many\"}}"),
                id(TGGuns.AK47.get()), json("[1, 2]")));
        helper.assertTrue(guns.isEmpty(), "Overrides for a non-gun, a wrong type and a non-object should be skipped: " + guns.keySet());
        Map<ResourceLocation, ArmorStatOverride> armor = StatOverrideLoader.loadArmor(Map.of(
                id(TGGuns.M4.get()), json("{\"toughness\": 1.0}"),
                id(TGArmors.T1_COMBAT_CHESTPLATE.get()), json("{\"armor\": {\"unresistable\": 5.0}}")));
        helper.assertTrue(armor.isEmpty(), "Armor overrides for a gun and an unresistable category should be skipped: " + armor.keySet());
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void armorOverrideChangesArmorToughnessAndBonuses(GameTestHelper helper) {
        TGArmorItem boots = TGArmors.T3_POWER_BOOTS.get();
        float fire = boots.armorValue(TGDamageCategory.FIRE);
        double mining = boots.bonus(TGAttributes.MINING_SPEED_BONUS);
        Map<ResourceLocation, ArmorStatOverride> loaded = StatOverrideLoader.loadArmor(Map.of(id(boots), json("""
                {"armor": {"physical": 9.0}, "toughness": 3.0,
                 "bonuses": {"techguns:speed_bonus": 0.5, "techguns:jump_bonus": 0.0, "minecraft:generic.luck": 2.0},
                 "unpowered_bonuses": {"techguns:speed_bonus": 0.02}}""")));
        helper.assertTrue(loaded.size() == 1, "The override should load");
        Map<ResourceLocation, ArmorStatOverride> previous = StatOverrides.armorById();
        try {
            StatOverrides.setArmor(loaded);
            helper.assertTrue(boots.armorValue(TGDamageCategory.PHYSICAL) == 9.0f, "Physical armor should be overridden");
            helper.assertTrue(boots.armorValue(TGDamageCategory.FIRE) == fire, "Fire armor should keep its value");
            helper.assertTrue(boots.toughness() == 3.0f, "Toughness should be overridden");
            helper.assertTrue(near(boots.bonus(TGAttributes.SPEED_BONUS), 0.5), "Speed bonus should be overridden");
            helper.assertTrue(boots.bonus(TGAttributes.JUMP_BONUS) == 0.0, "A 0 should remove the jump bonus");
            helper.assertTrue(near(boots.bonus(TGAttributes.MINING_SPEED_BONUS), mining), "Unlisted bonuses should stay");
            helper.assertTrue(near(boots.bonus(Attributes.LUCK), 2.0), "A new bonus should be added");

            // an empty powered piece gives the unpowered bonuses; the attribute modifiers follow the override
            ItemStack empty = new ItemStack(boots);
            helper.assertTrue(near(modifier(empty, TGAttributes.SPEED_BONUS), 0.02), "Unpowered speed bonus should be overridden, was "
                    + modifier(empty, TGAttributes.SPEED_BONUS));
            helper.assertTrue(near(modifier(empty, Attributes.ARMOR), 9.0), "Armor modifier should follow the override");
            helper.assertTrue(near(modifier(empty, Attributes.ARMOR_TOUGHNESS), 3.0), "Toughness modifier should follow the override");
        } finally {
            StatOverrides.setArmor(previous);
        }
        helper.assertTrue(boots.armorValue(TGDamageCategory.PHYSICAL) != 9.0f, "Removing the override should restore the armor");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void overridesSurviveTheSyncPayload(GameTestHelper helper) {
        Map<ResourceLocation, GunStats> guns = StatOverrideLoader.loadGuns(Map.of(id(TGGuns.M4.get()), json("{\"damage\": {\"damage\": 20.0}}")));
        Map<ResourceLocation, ArmorStatOverride> armor = StatOverrideLoader.loadArmor(Map.of(id(TGArmors.T3_POWER_BOOTS.get()),
                json("{\"armor\": {\"fire\": 2.0}, \"bonuses\": {\"techguns:speed_bonus\": 0.5}}")));
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        StatPayloads.SyncStatOverrides.STREAM_CODEC.encode(buf, new StatPayloads.SyncStatOverrides(guns, armor));
        StatPayloads.SyncStatOverrides copy = StatPayloads.SyncStatOverrides.STREAM_CODEC.decode(buf);
        helper.assertTrue(copy.guns().equals(guns), "Gun overrides should survive the payload");
        helper.assertTrue(copy.armor().equals(armor), "Armor overrides should survive the payload: " + copy.armor() + " vs " + armor);
        helper.succeed();
    }
}
