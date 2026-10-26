package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.TGIds;
import techguns.Techguns;
import techguns.armor.TGArmorItem;
import techguns.armor.TGArmorStats;
import techguns.core.damage.TGDamageCategory;

/**
 * Techguns armor sets (Techguns 2 {@code TGArmors}: values, bonuses and repair items). Vanilla armor materials only
 * carry the equip sound, enchantability and texture layers; armor values come from {@link TGArmorStats}.
 */
public final class TGArmors {
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Techguns.MODID);

    private static final List<DeferredItem<TGArmorItem>> ARMORS = new ArrayList<>();

    // Custom armor model ids (Techguns 2 ARMORMODEL_*), drawn by the client's TGArmorModels
    public static final ResourceLocation MODEL_COAT_0 = TGIds.id("coat_0");
    public static final ResourceLocation MODEL_COAT_1 = TGIds.id("coat_1");
    public static final ResourceLocation MODEL_COAT_2 = TGIds.id("coat_2");
    public static final ResourceLocation MODEL_COAT_3 = TGIds.id("coat_3");
    public static final ResourceLocation MODEL_BERET = TGIds.id("beret_0");

    // --- T1 ---
    public static final TGArmorStats T1_COMBAT_STATS = new TGArmorStats("t1_combat", 60, 15.0f, 0.5f);
    private static final Holder<ArmorMaterial> T1_COMBAT = material(T1_COMBAT_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T1_COMBAT_HELMET = armor(T1_COMBAT, ArmorItem.Type.HELMET, T1_COMBAT_STATS,
            item -> item.repairItem(() -> Items.IRON_INGOT).knockbackResistance(0.05f));
    public static final DeferredItem<TGArmorItem> T1_COMBAT_CHESTPLATE = armor(T1_COMBAT, ArmorItem.Type.CHESTPLATE, T1_COMBAT_STATS,
            item -> item.repairItem(() -> Items.IRON_INGOT).knockbackResistance(0.20f));
    public static final DeferredItem<TGArmorItem> T1_COMBAT_LEGGINGS = armor(T1_COMBAT, ArmorItem.Type.LEGGINGS, T1_COMBAT_STATS,
            item -> item.repairItem(() -> Items.IRON_INGOT).knockbackResistance(0.10f));
    public static final DeferredItem<TGArmorItem> T1_COMBAT_BOOTS = armor(T1_COMBAT, ArmorItem.Type.BOOTS, T1_COMBAT_STATS,
            item -> item.repairItem(() -> Items.IRON_INGOT).knockbackResistance(0.05f));

    // --- T1 scout ---
    public static final TGArmorStats T1_SCOUT_STATS = new TGArmorStats("t1_scout", 60, 13.0f, 0.0f);
    private static final Holder<ArmorMaterial> T1_SCOUT = material(T1_SCOUT_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T1_SCOUT_HELMET = armor(T1_SCOUT, ArmorItem.Type.HELMET, T1_SCOUT_STATS, "t1_scout",
            item -> item.speed(0.125f, 0.02f).repairItem(() -> TGItems.HEAVY_CLOTH.get()));
    public static final DeferredItem<TGArmorItem> T1_SCOUT_CHESTPLATE = armor(T1_SCOUT, ArmorItem.Type.CHESTPLATE, T1_SCOUT_STATS, "t1_scout",
            item -> item.speed(0.125f, 0.02f).repairItem(() -> TGItems.HEAVY_CLOTH.get()));
    public static final DeferredItem<TGArmorItem> T1_SCOUT_LEGGINGS = armor(T1_SCOUT, ArmorItem.Type.LEGGINGS, T1_SCOUT_STATS, "t1_scout",
            item -> item.speed(0.125f, 0.02f).repairItem(() -> TGItems.HEAVY_CLOTH.get()));
    public static final DeferredItem<TGArmorItem> T1_SCOUT_BOOTS = armor(T1_SCOUT, ArmorItem.Type.BOOTS, T1_SCOUT_STATS, "t1_scout",
            item -> item.speed(0.125f, 0.1f).fallProtection(0.2f, 1.0f).repairItem(() -> TGItems.HEAVY_CLOTH.get()));

    // --- T1 miner ---
    public static final TGArmorStats T1_MINER_STATS = new TGArmorStats("t1_miner", 60, 13.0f, 0.0f);
    private static final Holder<ArmorMaterial> T1_MINER = material(T1_MINER_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T1_MINER_HELMET = armor(T1_MINER, ArmorItem.Type.HELMET, T1_MINER_STATS, "t1_miner",
            item -> item.speed(0.08f, 0.0f).mining(0.05f).repairItem(() -> Items.IRON_INGOT));
    public static final DeferredItem<TGArmorItem> T1_MINER_CHESTPLATE = armor(T1_MINER, ArmorItem.Type.CHESTPLATE, T1_MINER_STATS, "t1_miner",
            item -> item.speed(0.08f, 0.0f).mining(0.05f).repairItem(() -> Items.IRON_INGOT));
    public static final DeferredItem<TGArmorItem> T1_MINER_LEGGINGS = armor(T1_MINER, ArmorItem.Type.LEGGINGS, T1_MINER_STATS, "t1_miner",
            item -> item.speed(0.08f, 0.0f).mining(0.05f).repairItem(() -> Items.IRON_INGOT));
    public static final DeferredItem<TGArmorItem> T1_MINER_BOOTS = armor(T1_MINER, ArmorItem.Type.BOOTS, T1_MINER_STATS, "t1_miner",
            item -> item.speed(0.08f, 0.1f).mining(0.05f).repairItem(() -> Items.IRON_INGOT).fallProtection(0.2f, 1.0f));

    // --- T2 hazmat ---
    public static final TGArmorStats T2_HAZMAT_STATS = new TGArmorStats("hazmatsuit", 80, 10.0f, 0.0f).elemental(16.0f)
            .set(TGDamageCategory.EXPLOSION, 10.0f).set(TGDamageCategory.POISON, 20.0f).set(TGDamageCategory.RADIATION, 20.0f);
    private static final Holder<ArmorMaterial> T2_HAZMAT = material(T2_HAZMAT_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> HAZMAT_HELMET = armor(T2_HAZMAT, ArmorItem.Type.HELMET, T2_HAZMAT_STATS, "hazmat",
            item -> item.radiationResistance(1.0f).repairItem(() -> TGItems.PROTECTIVE_FIBER.get()));
    public static final DeferredItem<TGArmorItem> HAZMAT_CHESTPLATE = armor(T2_HAZMAT, ArmorItem.Type.CHESTPLATE, T2_HAZMAT_STATS, "hazmat",
            item -> item.radiationResistance(1.0f).repairItem(() -> TGItems.PROTECTIVE_FIBER.get()));
    public static final DeferredItem<TGArmorItem> HAZMAT_LEGGINGS = armor(T2_HAZMAT, ArmorItem.Type.LEGGINGS, T2_HAZMAT_STATS, "hazmat",
            item -> item.radiationResistance(1.0f).repairItem(() -> TGItems.PROTECTIVE_FIBER.get()));
    public static final DeferredItem<TGArmorItem> HAZMAT_BOOTS = armor(T2_HAZMAT, ArmorItem.Type.BOOTS, T2_HAZMAT_STATS, "hazmat",
            item -> item.radiationResistance(1.0f).repairItem(() -> TGItems.PROTECTIVE_FIBER.get()).fallProtection(0.1f, 0.5f));

    // --- T2 combat ---
    public static final TGArmorStats T2_COMBAT_STATS = new TGArmorStats("t2_combat", 72, 18.0f, 1.0f);
    private static final Holder<ArmorMaterial> T2_COMBAT = material(T2_COMBAT_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T2_COMBAT_HELMET = armor(T2_COMBAT, ArmorItem.Type.HELMET, T2_COMBAT_STATS, "t2_combat",
            item -> item.speed(0.1f, 0.0f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).knockbackResistance(0.10f));
    public static final DeferredItem<TGArmorItem> T2_COMBAT_CHESTPLATE = armor(T2_COMBAT, ArmorItem.Type.CHESTPLATE, T2_COMBAT_STATS, "t2_combat",
            item -> item.speed(0.1f, 0.0f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).knockbackResistance(0.25f));
    public static final DeferredItem<TGArmorItem> T2_COMBAT_LEGGINGS = armor(T2_COMBAT, ArmorItem.Type.LEGGINGS, T2_COMBAT_STATS, "t2_combat",
            item -> item.speed(0.1f, 0.0f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).knockbackResistance(0.15f));
    public static final DeferredItem<TGArmorItem> T2_COMBAT_BOOTS = armor(T2_COMBAT, ArmorItem.Type.BOOTS, T2_COMBAT_STATS, "t2_combat",
            item -> item.speed(0.1f, 0.1f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).knockbackResistance(0.10f));

    // --- T2 commando ---
    public static final TGArmorStats T2_COMMANDO_STATS = new TGArmorStats("t2_commando", 72, 18.0f, 1.0f).elemental(16.0f)
            .set(TGDamageCategory.EXPLOSION, 16.0f).set(TGDamageCategory.POISON, 10.0f).set(TGDamageCategory.RADIATION, 5.0f);
    private static final Holder<ArmorMaterial> T2_COMMANDO = material(T2_COMMANDO_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T2_COMMANDO_HELMET = armor(T2_COMMANDO, ArmorItem.Type.HELMET, T2_COMMANDO_STATS, "t2_commando",
            item -> item.speed(0.10f, 0.0f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).radiationResistance(0.25f).waterMining(1.25f).gunAccuracy(0.05f).knockbackResistance(0.05f).oxygenGear(1.0f));
    public static final DeferredItem<TGArmorItem> T2_COMMANDO_CHESTPLATE = armor(T2_COMMANDO, ArmorItem.Type.CHESTPLATE, T2_COMMANDO_STATS, "t2_commando",
            item -> item.speed(0.10f, 0.0f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).radiationResistance(0.25f).waterMining(1.25f).gunAccuracy(0.05f).knockbackResistance(0.20f));
    public static final DeferredItem<TGArmorItem> T2_COMMANDO_LEGGINGS = armor(T2_COMMANDO, ArmorItem.Type.LEGGINGS, T2_COMMANDO_STATS, "t2_commando",
            item -> item.speed(0.10f, 0.0f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).radiationResistance(0.25f).waterMining(1.25f).gunAccuracy(0.05f).knockbackResistance(0.10f));
    public static final DeferredItem<TGArmorItem> T2_COMMANDO_BOOTS = armor(T2_COMMANDO, ArmorItem.Type.BOOTS, T2_COMMANDO_STATS, "t2_commando",
            item -> item.speed(0.10f, 0.1f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).radiationResistance(0.25f).waterMining(1.25f).gunAccuracy(0.05f).knockbackResistance(0.05f).fallProtection(0.2f, 1.0f));

    // --- T2 riot ---
    public static final TGArmorStats T2_RIOT_STATS = new TGArmorStats("t2_riot", 96, 18.5f, 1.5f)
            .set(TGDamageCategory.FIRE, 18.0f).set(TGDamageCategory.EXPLOSION, 18.0f).set(TGDamageCategory.ENERGY, 18.0f)
            .set(TGDamageCategory.ICE, 17.0f).set(TGDamageCategory.LIGHTNING, 17.0f).set(TGDamageCategory.POISON, 16.0f)
            .set(TGDamageCategory.RADIATION, 16.0f);
    private static final Holder<ArmorMaterial> T2_RIOT = material(T2_RIOT_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T2_RIOT_HELMET = armor(T2_RIOT, ArmorItem.Type.HELMET, T2_RIOT_STATS, "t2_riot",
            item -> item.speed(0.12f, 0.0f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).radiationResistance(0.5f).knockbackResistance(0.125f).armorModel(MODEL_COAT_0));
    public static final DeferredItem<TGArmorItem> T2_RIOT_CHESTPLATE = armor(T2_RIOT, ArmorItem.Type.CHESTPLATE, T2_RIOT_STATS, "t2_riot",
            item -> item.speed(0.12f, 0.0f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).radiationResistance(0.5f).knockbackResistance(0.275f).armorModel(MODEL_COAT_1));
    public static final DeferredItem<TGArmorItem> T2_RIOT_LEGGINGS = armor(T2_RIOT, ArmorItem.Type.LEGGINGS, T2_RIOT_STATS, "t2_riot",
            item -> item.speed(0.13f, 0.0f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).radiationResistance(0.5f).knockbackResistance(0.175f).armorModel(MODEL_COAT_2));
    public static final DeferredItem<TGArmorItem> T2_RIOT_BOOTS = armor(T2_RIOT, ArmorItem.Type.BOOTS, T2_RIOT_STATS, "t2_riot",
            item -> item.speed(0.13f, 0.15f).repairItem(() -> TGItems.OBSIDIAN_STEEL_INGOT.get()).radiationResistance(0.5f).knockbackResistance(0.125f).armorModel(MODEL_COAT_3));

    // --- T3 combat ---
    public static final TGArmorStats T3_COMBAT_STATS = new TGArmorStats("t3_combat", 240, 21.0f, 2.0f)
            .set(TGDamageCategory.FIRE, 18.0f).set(TGDamageCategory.EXPLOSION, 18.0f).set(TGDamageCategory.ENERGY, 19.0f)
            .set(TGDamageCategory.ICE, 18.0f).set(TGDamageCategory.LIGHTNING, 18.0f).set(TGDamageCategory.POISON, 10.0f)
            .set(TGDamageCategory.RADIATION, 12.0f);
    private static final Holder<ArmorMaterial> T3_COMBAT = material(T3_COMBAT_STATS, 0, SoundEvents.ARMOR_EQUIP_CHAIN);

    public static final DeferredItem<TGArmorItem> T3_COMBAT_HELMET = armor(T3_COMBAT, ArmorItem.Type.HELMET, T3_COMBAT_STATS, "t3_combat",
            item -> item.speed(0.1f, 0.0f).repairItem(() -> TGItems.CARBON_PLATE.get()).radiationResistance(0.25f).knockbackResistance(0.15f));
    public static final DeferredItem<TGArmorItem> T3_COMBAT_CHESTPLATE = armor(T3_COMBAT, ArmorItem.Type.CHESTPLATE, T3_COMBAT_STATS, "t3_combat",
            item -> item.speed(0.1f, 0.0f).repairItem(() -> TGItems.CARBON_PLATE.get()).radiationResistance(0.25f).knockbackResistance(0.30f));
    public static final DeferredItem<TGArmorItem> T3_COMBAT_LEGGINGS = armor(T3_COMBAT, ArmorItem.Type.LEGGINGS, T3_COMBAT_STATS, "t3_combat",
            item -> item.speed(0.1f, 0.0f).repairItem(() -> TGItems.CARBON_PLATE.get()).radiationResistance(0.25f).knockbackResistance(0.20f));
    public static final DeferredItem<TGArmorItem> T3_COMBAT_BOOTS = armor(T3_COMBAT, ArmorItem.Type.BOOTS, T3_COMBAT_STATS, "t3_combat",
            item -> item.speed(0.1f, 0.1f).repairItem(() -> TGItems.CARBON_PLATE.get()).radiationResistance(0.25f).knockbackResistance(0.15f));

    // --- T2 beret (helmet only) ---
    public static final TGArmorStats T2_BERET_STATS = new TGArmorStats("t2_beret", 60, 8.0f, 0.0f);
    private static final Holder<ArmorMaterial> T2_BERET = material(T2_BERET_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T2_BERET_ITEM = armorNamed("t2_beret", T2_BERET, ArmorItem.Type.HELMET, T2_BERET_STATS,
            item -> item.speed(0.1f, 0.0f).repairItem(() -> TGItems.HEAVY_CLOTH.get())
                    .armorModel(MODEL_BERET).armorTexture(TGIds.id("textures/models/armor/beret_texture.png")));

    private TGArmors() {
    }

    /** Loads the item holders before the item register is attached. */
    public static void init() {
    }

    public static List<DeferredItem<TGArmorItem>> armors() {
        return Collections.unmodifiableList(ARMORS);
    }

    private static Holder<ArmorMaterial> material(TGArmorStats stats, int enchantability, Holder<SoundEvent> equipSound) {
        // defense, toughness and knockback resistance are 0: TGArmorItem adds its own modifiers
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            defense.put(type, 0);
        }
        return MATERIALS.register(stats.name(), () -> new ArmorMaterial(defense, enchantability, equipSound, () -> Ingredient.EMPTY,
                List.of(new ArmorMaterial.Layer(TGIds.id(stats.name()))), 0.0f, 0.0f));
    }

    private static DeferredItem<TGArmorItem> armor(Holder<ArmorMaterial> material, ArmorItem.Type type, TGArmorStats stats,
                                                   Function<TGArmorItem, TGArmorItem> setup) {
        return armor(material, type, stats, stats.name(), setup);
    }

    /** @param prefix item id prefix, differs from the texture name ({@code stats.name()}) for some sets (hazmat) */
    private static DeferredItem<TGArmorItem> armor(Holder<ArmorMaterial> material, ArmorItem.Type type, TGArmorStats stats,
                                                   String prefix, Function<TGArmorItem, TGArmorItem> setup) {
        String name = prefix + "_" + switch (type) {
            case HELMET -> "helmet";
            case CHESTPLATE -> "chestplate";
            case LEGGINGS -> "leggings";
            case BOOTS -> "boots";
            case BODY -> "body";
        };
        return armorNamed(name, material, type, stats, setup);
    }

    private static DeferredItem<TGArmorItem> armorNamed(String name, Holder<ArmorMaterial> material, ArmorItem.Type type,
                                                        TGArmorStats stats, Function<TGArmorItem, TGArmorItem> setup) {
        DeferredItem<TGArmorItem> item = TGItems.ITEMS.registerItem(name,
                properties -> setup.apply(new TGArmorItem(material, type, properties, stats)));
        ARMORS.add(item);
        return item;
    }
}
