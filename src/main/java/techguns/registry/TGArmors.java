package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.TGIds;
import techguns.Techguns;
import techguns.armor.TGArmorItem;
import techguns.armor.ShieldStats;
import techguns.armor.TGArmorStats;
import techguns.armor.TGShieldItem;
import techguns.armor.TGPoweredArmorItem;
import techguns.armor.TGPoweredArmorItem.PowerType;
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
    public static final ResourceLocation MODEL_STEAM_0 = TGIds.id("steam_armor_0");
    public static final ResourceLocation MODEL_STEAM_1 = TGIds.id("steam_armor_1");
    public static final ResourceLocation MODEL_STEAM_2 = TGIds.id("steam_armor_2");
    public static final ResourceLocation MODEL_POWER_0 = TGIds.id("power_armor_0");
    public static final ResourceLocation MODEL_POWER_1 = TGIds.id("power_armor_1");
    public static final ResourceLocation MODEL_POWER_2 = TGIds.id("power_armor_2");
    public static final ResourceLocation MODEL_EXO_0 = TGIds.id("exo_suit_0");
    public static final ResourceLocation MODEL_EXO_1 = TGIds.id("exo_suit_1");
    public static final ResourceLocation MODEL_EXO_2 = TGIds.id("exo_suit_2");
    public static final ResourceLocation MODEL_POWER_MK2_0 = TGIds.id("power_armor_mk2_0");
    public static final ResourceLocation MODEL_POWER_MK2_1 = TGIds.id("power_armor_mk2_1");
    public static final ResourceLocation MODEL_POWER_MK2_2 = TGIds.id("power_armor_mk2_2");

    // --- T1 ---
    public static final TGArmorStats T1_COMBAT_STATS = new TGArmorStats("t1_combat", 60, 15.0f, 0.5f);
    private static final Holder<ArmorMaterial> T1_COMBAT = material(T1_COMBAT_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T1_COMBAT_HELMET = armor(T1_COMBAT, ArmorItem.Type.HELMET, T1_COMBAT_STATS,
            item -> item.repairMats(() -> Items.IRON_INGOT, () -> TGItems.HEAVY_CLOTH.get()).knockbackResistance(0.05f));
    public static final DeferredItem<TGArmorItem> T1_COMBAT_CHESTPLATE = armor(T1_COMBAT, ArmorItem.Type.CHESTPLATE, T1_COMBAT_STATS,
            item -> item.repairMats(() -> Items.IRON_INGOT, () -> TGItems.HEAVY_CLOTH.get()).knockbackResistance(0.20f));
    public static final DeferredItem<TGArmorItem> T1_COMBAT_LEGGINGS = armor(T1_COMBAT, ArmorItem.Type.LEGGINGS, T1_COMBAT_STATS,
            item -> item.repairMats(() -> Items.IRON_INGOT, () -> TGItems.HEAVY_CLOTH.get()).knockbackResistance(0.10f));
    public static final DeferredItem<TGArmorItem> T1_COMBAT_BOOTS = armor(T1_COMBAT, ArmorItem.Type.BOOTS, T1_COMBAT_STATS,
            item -> item.repairMats(() -> Items.IRON_INGOT, () -> TGItems.HEAVY_CLOTH.get()).knockbackResistance(0.05f));

    // --- T1 scout ---
    public static final TGArmorStats T1_SCOUT_STATS = new TGArmorStats("t1_scout", 60, 13.0f, 0.0f);
    private static final Holder<ArmorMaterial> T1_SCOUT = material(T1_SCOUT_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T1_SCOUT_HELMET = armor(T1_SCOUT, ArmorItem.Type.HELMET, T1_SCOUT_STATS, "t1_scout",
            item -> item.speed(0.125f, 0.02f).repairMats(null, () -> TGItems.HEAVY_CLOTH.get()));
    public static final DeferredItem<TGArmorItem> T1_SCOUT_CHESTPLATE = armor(T1_SCOUT, ArmorItem.Type.CHESTPLATE, T1_SCOUT_STATS, "t1_scout",
            item -> item.speed(0.125f, 0.02f).repairMats(null, () -> TGItems.HEAVY_CLOTH.get()));
    public static final DeferredItem<TGArmorItem> T1_SCOUT_LEGGINGS = armor(T1_SCOUT, ArmorItem.Type.LEGGINGS, T1_SCOUT_STATS, "t1_scout",
            item -> item.speed(0.125f, 0.02f).repairMats(null, () -> TGItems.HEAVY_CLOTH.get()));
    public static final DeferredItem<TGArmorItem> T1_SCOUT_BOOTS = armor(T1_SCOUT, ArmorItem.Type.BOOTS, T1_SCOUT_STATS, "t1_scout",
            item -> item.speed(0.125f, 0.1f).fallProtection(0.2f, 1.0f).repairMats(null, () -> TGItems.HEAVY_CLOTH.get()));

    // --- T1 miner ---
    public static final TGArmorStats T1_MINER_STATS = new TGArmorStats("t1_miner", 60, 13.0f, 0.0f);
    private static final Holder<ArmorMaterial> T1_MINER = material(T1_MINER_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T1_MINER_HELMET = armor(T1_MINER, ArmorItem.Type.HELMET, T1_MINER_STATS, "t1_miner",
            item -> item.speed(0.08f, 0.0f).mining(0.05f).repairMats(() -> Items.IRON_INGOT, () -> TGItems.HEAVY_CLOTH.get()));
    public static final DeferredItem<TGArmorItem> T1_MINER_CHESTPLATE = armor(T1_MINER, ArmorItem.Type.CHESTPLATE, T1_MINER_STATS, "t1_miner",
            item -> item.speed(0.08f, 0.0f).mining(0.05f).repairMats(() -> Items.IRON_INGOT, () -> TGItems.HEAVY_CLOTH.get()));
    public static final DeferredItem<TGArmorItem> T1_MINER_LEGGINGS = armor(T1_MINER, ArmorItem.Type.LEGGINGS, T1_MINER_STATS, "t1_miner",
            item -> item.speed(0.08f, 0.0f).mining(0.05f).repairMats(() -> Items.IRON_INGOT, () -> TGItems.HEAVY_CLOTH.get()).repairCount(2));
    public static final DeferredItem<TGArmorItem> T1_MINER_BOOTS = armor(T1_MINER, ArmorItem.Type.BOOTS, T1_MINER_STATS, "t1_miner",
            item -> item.speed(0.08f, 0.1f).mining(0.05f).repairMats(() -> Items.IRON_INGOT, () -> TGItems.HEAVY_CLOTH.get()).fallProtection(0.2f, 1.0f));

    // --- T2 hazmat ---
    public static final TGArmorStats T2_HAZMAT_STATS = new TGArmorStats("hazmatsuit", 80, 10.0f, 0.0f).elemental(16.0f)
            .set(TGDamageCategory.EXPLOSION, 10.0f).set(TGDamageCategory.POISON, 20.0f).set(TGDamageCategory.RADIATION, 20.0f);
    private static final Holder<ArmorMaterial> T2_HAZMAT = material(T2_HAZMAT_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> HAZMAT_HELMET = armor(T2_HAZMAT, ArmorItem.Type.HELMET, T2_HAZMAT_STATS, "hazmat",
            item -> item.radiationResistance(1.0f).repairMats(null, () -> TGItems.PROTECTIVE_FIBER.get()));
    public static final DeferredItem<TGArmorItem> HAZMAT_CHESTPLATE = armor(T2_HAZMAT, ArmorItem.Type.CHESTPLATE, T2_HAZMAT_STATS, "hazmat",
            item -> item.radiationResistance(1.0f).repairMats(null, () -> TGItems.PROTECTIVE_FIBER.get()));
    public static final DeferredItem<TGArmorItem> HAZMAT_LEGGINGS = armor(T2_HAZMAT, ArmorItem.Type.LEGGINGS, T2_HAZMAT_STATS, "hazmat",
            item -> item.radiationResistance(1.0f).repairMats(null, () -> TGItems.PROTECTIVE_FIBER.get()));
    public static final DeferredItem<TGArmorItem> HAZMAT_BOOTS = armor(T2_HAZMAT, ArmorItem.Type.BOOTS, T2_HAZMAT_STATS, "hazmat",
            item -> item.radiationResistance(1.0f).repairMats(null, () -> TGItems.PROTECTIVE_FIBER.get()).fallProtection(0.1f, 0.5f));

    // --- T2 combat ---
    public static final TGArmorStats T2_COMBAT_STATS = new TGArmorStats("t2_combat", 72, 18.0f, 1.0f);
    private static final Holder<ArmorMaterial> T2_COMBAT = material(T2_COMBAT_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T2_COMBAT_HELMET = armor(T2_COMBAT, ArmorItem.Type.HELMET, T2_COMBAT_STATS, "t2_combat",
            item -> item.speed(0.1f, 0.0f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.HEAVY_CLOTH.get()).knockbackResistance(0.10f));
    public static final DeferredItem<TGArmorItem> T2_COMBAT_CHESTPLATE = armor(T2_COMBAT, ArmorItem.Type.CHESTPLATE, T2_COMBAT_STATS, "t2_combat",
            item -> item.speed(0.1f, 0.0f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.HEAVY_CLOTH.get()).knockbackResistance(0.25f));
    public static final DeferredItem<TGArmorItem> T2_COMBAT_LEGGINGS = armor(T2_COMBAT, ArmorItem.Type.LEGGINGS, T2_COMBAT_STATS, "t2_combat",
            item -> item.speed(0.1f, 0.0f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.HEAVY_CLOTH.get()).knockbackResistance(0.15f));
    public static final DeferredItem<TGArmorItem> T2_COMBAT_BOOTS = armor(T2_COMBAT, ArmorItem.Type.BOOTS, T2_COMBAT_STATS, "t2_combat",
            item -> item.speed(0.1f, 0.1f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.HEAVY_CLOTH.get()).knockbackResistance(0.10f));

    // --- T2 commando ---
    public static final TGArmorStats T2_COMMANDO_STATS = new TGArmorStats("t2_commando", 72, 18.0f, 1.0f).elemental(16.0f)
            .set(TGDamageCategory.EXPLOSION, 16.0f).set(TGDamageCategory.POISON, 10.0f).set(TGDamageCategory.RADIATION, 5.0f);
    private static final Holder<ArmorMaterial> T2_COMMANDO = material(T2_COMMANDO_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T2_COMMANDO_HELMET = armor(T2_COMMANDO, ArmorItem.Type.HELMET, T2_COMMANDO_STATS, "t2_commando",
            item -> item.speed(0.10f, 0.0f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.RUBBER_BAR.get()).radiationResistance(0.25f).waterMining(1.25f).gunAccuracy(0.05f).knockbackResistance(0.05f).oxygenGear(1.0f));
    public static final DeferredItem<TGArmorItem> T2_COMMANDO_CHESTPLATE = armor(T2_COMMANDO, ArmorItem.Type.CHESTPLATE, T2_COMMANDO_STATS, "t2_commando",
            item -> item.speed(0.10f, 0.0f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.RUBBER_BAR.get()).radiationResistance(0.25f).waterMining(1.25f).gunAccuracy(0.05f).knockbackResistance(0.20f));
    public static final DeferredItem<TGArmorItem> T2_COMMANDO_LEGGINGS = armor(T2_COMMANDO, ArmorItem.Type.LEGGINGS, T2_COMMANDO_STATS, "t2_commando",
            item -> item.speed(0.10f, 0.0f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.RUBBER_BAR.get()).radiationResistance(0.25f).waterMining(1.25f).gunAccuracy(0.05f).knockbackResistance(0.10f));
    public static final DeferredItem<TGArmorItem> T2_COMMANDO_BOOTS = armor(T2_COMMANDO, ArmorItem.Type.BOOTS, T2_COMMANDO_STATS, "t2_commando",
            item -> item.speed(0.10f, 0.1f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.RUBBER_BAR.get()).radiationResistance(0.25f).waterMining(1.25f).gunAccuracy(0.05f).knockbackResistance(0.05f).fallProtection(0.2f, 1.0f));

    // --- T2 riot ---
    public static final TGArmorStats T2_RIOT_STATS = new TGArmorStats("t2_riot", 96, 18.5f, 1.5f)
            .set(TGDamageCategory.FIRE, 18.0f).set(TGDamageCategory.EXPLOSION, 18.0f).set(TGDamageCategory.ENERGY, 18.0f)
            .set(TGDamageCategory.ICE, 17.0f).set(TGDamageCategory.LIGHTNING, 17.0f).set(TGDamageCategory.POISON, 16.0f)
            .set(TGDamageCategory.RADIATION, 16.0f);
    private static final Holder<ArmorMaterial> T2_RIOT = material(T2_RIOT_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T2_RIOT_HELMET = armor(T2_RIOT, ArmorItem.Type.HELMET, T2_RIOT_STATS, "t2_riot",
            item -> item.speed(0.12f, 0.0f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.TREATED_LEATHER.get()).radiationResistance(0.5f).knockbackResistance(0.125f).armorModel(MODEL_COAT_0));
    public static final DeferredItem<TGArmorItem> T2_RIOT_CHESTPLATE = armor(T2_RIOT, ArmorItem.Type.CHESTPLATE, T2_RIOT_STATS, "t2_riot",
            item -> item.speed(0.12f, 0.0f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.TREATED_LEATHER.get()).radiationResistance(0.5f).knockbackResistance(0.275f).armorModel(MODEL_COAT_1));
    public static final DeferredItem<TGArmorItem> T2_RIOT_LEGGINGS = armor(T2_RIOT, ArmorItem.Type.LEGGINGS, T2_RIOT_STATS, "t2_riot",
            item -> item.speed(0.13f, 0.0f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.TREATED_LEATHER.get()).radiationResistance(0.5f).knockbackResistance(0.175f).armorModel(MODEL_COAT_2));
    public static final DeferredItem<TGArmorItem> T2_RIOT_BOOTS = armor(T2_RIOT, ArmorItem.Type.BOOTS, T2_RIOT_STATS, "t2_riot",
            item -> item.speed(0.13f, 0.15f).repairMats(() -> TGItems.OBSIDIAN_STEEL_INGOT.get(), () -> TGItems.TREATED_LEATHER.get()).radiationResistance(0.5f).knockbackResistance(0.125f).armorModel(MODEL_COAT_3));

    // --- T3 combat ---
    public static final TGArmorStats T3_COMBAT_STATS = new TGArmorStats("t3_combat", 240, 21.0f, 2.0f)
            .set(TGDamageCategory.FIRE, 18.0f).set(TGDamageCategory.EXPLOSION, 18.0f).set(TGDamageCategory.ENERGY, 19.0f)
            .set(TGDamageCategory.ICE, 18.0f).set(TGDamageCategory.LIGHTNING, 18.0f).set(TGDamageCategory.POISON, 10.0f)
            .set(TGDamageCategory.RADIATION, 12.0f);
    private static final Holder<ArmorMaterial> T3_COMBAT = material(T3_COMBAT_STATS, 0, SoundEvents.ARMOR_EQUIP_CHAIN);

    public static final DeferredItem<TGArmorItem> T3_COMBAT_HELMET = armor(T3_COMBAT, ArmorItem.Type.HELMET, T3_COMBAT_STATS, "t3_combat",
            item -> item.speed(0.1f, 0.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CARBON_FIBERS.get()).radiationResistance(0.25f).knockbackResistance(0.15f));
    public static final DeferredItem<TGArmorItem> T3_COMBAT_CHESTPLATE = armor(T3_COMBAT, ArmorItem.Type.CHESTPLATE, T3_COMBAT_STATS, "t3_combat",
            item -> item.speed(0.1f, 0.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CARBON_FIBERS.get()).radiationResistance(0.25f).knockbackResistance(0.30f));
    public static final DeferredItem<TGArmorItem> T3_COMBAT_LEGGINGS = armor(T3_COMBAT, ArmorItem.Type.LEGGINGS, T3_COMBAT_STATS, "t3_combat",
            item -> item.speed(0.1f, 0.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CARBON_FIBERS.get()).radiationResistance(0.25f).knockbackResistance(0.20f));
    public static final DeferredItem<TGArmorItem> T3_COMBAT_BOOTS = armor(T3_COMBAT, ArmorItem.Type.BOOTS, T3_COMBAT_STATS, "t3_combat",
            item -> item.speed(0.1f, 0.1f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CARBON_FIBERS.get()).radiationResistance(0.25f).knockbackResistance(0.15f));

    // --- T1 steam armor ---
    public static final TGArmorStats T1_STEAM_STATS = new TGArmorStats("steam_armor", 200, 19.0f, 1.0f).elemental(17.0f)
            .set(TGDamageCategory.EXPLOSION, 18.0f).set(TGDamageCategory.POISON, 10.0f).set(TGDamageCategory.RADIATION, 6.0f);
    private static final Holder<ArmorMaterial> T1_STEAM = material(T1_STEAM_STATS, 0, SoundEvents.ARMOR_EQUIP_IRON);

    public static final DeferredItem<TGPoweredArmorItem> STEAM_HELMET = powered(T1_STEAM, ArmorItem.Type.HELMET, T1_STEAM_STATS, "steam",
            PowerType.STEAM, 10, item -> item.mining(0.05f, 0.0f).health(1, 0).speed(0.05f, 0.03f, 0.0f, 0.0f)
                    .radiationResistance(0.5f).repairMats(() -> TGItems.STEAM_ARMOR_PLATE.get(), () -> TGItems.STEAM_ARMOR_PLATE.get()).armorModel(MODEL_STEAM_2).armorTexture(TGIds.id("textures/models/armor/steam_armor.png")).knockbackResistance(0.15f).hideFaceSlot());
    public static final DeferredItem<TGPoweredArmorItem> STEAM_CHESTPLATE = powered(T1_STEAM, ArmorItem.Type.CHESTPLATE, T1_STEAM_STATS, "steam",
            PowerType.STEAM, 3600, item -> item.speed(0.05f, 0.03f, 0.0f, 0.0f).mining(0.05f, 0.0f).health(2, 0)
                    .battery(() -> TGAmmo.COMPRESSED_AIR_TANK.get(), () -> TGAmmo.COMPRESSED_AIR_TANK_EMPTY.get())
                    .radiationResistance(0.5f).repairMats(() -> TGItems.STEAM_ARMOR_PLATE.get(), () -> TGItems.STEAM_ARMOR_PLATE.get()).armorModel(MODEL_STEAM_0).armorTexture(TGIds.id("textures/models/armor/steam_armor.png")).knockbackResistance(0.30f).hideBackSlot().hideHandSlot());
    public static final DeferredItem<TGPoweredArmorItem> STEAM_LEGGINGS = powered(T1_STEAM, ArmorItem.Type.LEGGINGS, T1_STEAM_STATS, "steam",
            PowerType.STEAM, 10, item -> item.speed(0.05f, 0.03f, 0.0f, 0.0f).mining(0.05f, 0.0f).health(1, 0)
                    .radiationResistance(0.5f).repairMats(() -> TGItems.STEAM_ARMOR_PLATE.get(), () -> TGItems.STEAM_ARMOR_PLATE.get()).armorModel(MODEL_STEAM_1).armorTexture(TGIds.id("textures/models/armor/steam_armor.png")).knockbackResistance(0.20f));
    public static final DeferredItem<TGPoweredArmorItem> STEAM_BOOTS = powered(T1_STEAM, ArmorItem.Type.BOOTS, T1_STEAM_STATS, "steam",
            PowerType.STEAM, 10, item -> item.speed(0.05f, 0.15f, 0.0f, 0.0f).mining(0.05f, 0.0f).fallProtection(0.2f, 1.0f, 0.0f, 0.0f).health(1, 0)
                    .stepAssist(1.0f, 0.0f)
                    .radiationResistance(0.5f).repairMats(() -> TGItems.STEAM_ARMOR_PLATE.get(), () -> TGItems.STEAM_ARMOR_PLATE.get()).armorModel(MODEL_STEAM_0).armorTexture(TGIds.id("textures/models/armor/steam_armor.png")).knockbackResistance(0.15f));

    // --- T3 power armor ---
    public static final TGArmorStats T3_POWER_STATS = new TGArmorStats("powerarmor", 360, 22.0f, 2.5f)
            .set(TGDamageCategory.FIRE, 19.0f).set(TGDamageCategory.EXPLOSION, 21.0f).set(TGDamageCategory.ENERGY, 20.0f)
            .set(TGDamageCategory.ICE, 19.0f).set(TGDamageCategory.LIGHTNING, 19.0f).set(TGDamageCategory.POISON, 15.0f)
            .set(TGDamageCategory.RADIATION, 17.0f);
    private static final Holder<ArmorMaterial> T3_POWER = material(T3_POWER_STATS, 0, SoundEvents.ARMOR_EQUIP_IRON);

    public static final DeferredItem<TGPoweredArmorItem> T3_POWER_HELMET = powered(T3_POWER, ArmorItem.Type.HELMET, T3_POWER_STATS, "t3_power",
            PowerType.RF, 10, item -> item.mining(0.05f, 0.0f).health(1, 0).speed(0.05f, 0.03f, 0.0f, 0.0f)
                    .radiationResistance(0.75f).repairMats(() -> TGItems.POWER_PLATING.get(), () -> TGItems.GOLD_WIRE.get()).armorModel(MODEL_POWER_2).armorTexture(TGIds.id("textures/models/armor/powerarmor.png")).knockbackResistance(0.20f).hideFaceSlot());
    public static final DeferredItem<TGPoweredArmorItem> T3_POWER_CHESTPLATE = powered(T3_POWER, ArmorItem.Type.CHESTPLATE, T3_POWER_STATS, "t3_power",
            PowerType.RF, 3600, item -> item.speed(0.05f, 0.03f, 0.0f, 0.0f).mining(0.05f, 0.0f).health(2, 0)
                    .battery(() -> TGAmmo.ENERGY_CELL.get(), () -> TGAmmo.ENERGY_CELL_EMPTY.get())
                    .radiationResistance(0.75f).repairMats(() -> TGItems.POWER_PLATING.get(), () -> TGItems.GOLD_WIRE.get()).armorModel(MODEL_POWER_0).armorTexture(TGIds.id("textures/models/armor/powerarmor.png")).knockbackResistance(0.35f).hideHandSlot());
    public static final DeferredItem<TGPoweredArmorItem> T3_POWER_LEGGINGS = powered(T3_POWER, ArmorItem.Type.LEGGINGS, T3_POWER_STATS, "t3_power",
            PowerType.RF, 10, item -> item.speed(0.05f, 0.03f, 0.0f, 0.0f).mining(0.05f, 0.0f).health(1, 0)
                    .radiationResistance(0.75f).repairMats(() -> TGItems.POWER_PLATING.get(), () -> TGItems.GOLD_WIRE.get()).armorModel(MODEL_POWER_1).armorTexture(TGIds.id("textures/models/armor/powerarmor.png")).knockbackResistance(0.25f));
    public static final DeferredItem<TGPoweredArmorItem> T3_POWER_BOOTS = powered(T3_POWER, ArmorItem.Type.BOOTS, T3_POWER_STATS, "t3_power",
            PowerType.RF, 10, item -> item.speed(0.05f, 0.15f, 0.0f, 0.0f).mining(0.05f, 0.0f).fallProtection(0.2f, 1.0f, 0.0f, 0.0f).health(1, 0)
                    .stepAssist(1.0f, 0.0f)
                    .radiationResistance(0.75f).repairMats(() -> TGItems.POWER_PLATING.get(), () -> TGItems.GOLD_WIRE.get()).armorModel(MODEL_POWER_0).armorTexture(TGIds.id("textures/models/armor/powerarmor.png")).knockbackResistance(0.20f));

    // --- T3 miner (HEV suit) ---
    public static final TGArmorStats T3_MINER_STATS = new TGArmorStats("hevsuit", 300, 20.0f, 2.0f).elemental(20.0f)
            .set(TGDamageCategory.EXPLOSION, 20.0f).set(TGDamageCategory.POISON, 20.0f).set(TGDamageCategory.RADIATION, 20.0f);
    private static final Holder<ArmorMaterial> T3_MINER = material(T3_MINER_STATS, 0, SoundEvents.ARMOR_EQUIP_IRON);

    public static final DeferredItem<TGPoweredArmorItem> T3_MINER_HELMET = powered(T3_MINER, ArmorItem.Type.HELMET, T3_MINER_STATS, "t3_miner",
            PowerType.RF, 10, item -> item.speed(0.1f, 0.3f, 0.0f, 0.0f).waterSpeed(1.25f, 0.0f).mining(0.1f, 0.0f).waterMining(1.25f, 0.0f)
                    .oxygenGear(1.0f, 0.0f)
                    .radiationResistance(1.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CIRCUIT_BOARD_ELITE.get()).knockbackResistance(0.10f).hideFaceSlot());
    public static final DeferredItem<TGPoweredArmorItem> T3_MINER_CHESTPLATE = powered(T3_MINER, ArmorItem.Type.CHESTPLATE, T3_MINER_STATS, "t3_miner",
            PowerType.RF, 10000, item -> item.battery(() -> TGAmmo.ENERGY_CELL.get(), () -> TGAmmo.ENERGY_CELL_EMPTY.get())
                    .speed(0.1f, 0.03f, 0.0f, 0.0f).waterSpeed(1.25f, 0.0f).mining(0.1f, 0.0f).waterMining(1.25f, 0.0f)
                    .cooling(1.0f, 0.0f)
                    .radiationResistance(1.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CIRCUIT_BOARD_ELITE.get()).knockbackResistance(0.25f).hideHandSlot());
    public static final DeferredItem<TGPoweredArmorItem> T3_MINER_LEGGINGS = powered(T3_MINER, ArmorItem.Type.LEGGINGS, T3_MINER_STATS, "t3_miner",
            PowerType.RF, 10, item -> item.speed(0.1f, 0.03f, 0.0f, 0.0f).waterSpeed(1.25f, 0.0f).mining(0.1f, 0.0f).waterMining(1.25f, 0.0f)
                    .radiationResistance(1.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CIRCUIT_BOARD_ELITE.get()).knockbackResistance(0.15f));
    public static final DeferredItem<TGPoweredArmorItem> T3_MINER_BOOTS = powered(T3_MINER, ArmorItem.Type.BOOTS, T3_MINER_STATS, "t3_miner",
            PowerType.RF, 10, item -> item.speed(0.1f, 0.15f, 0.0f, 0.0f).waterSpeed(1.25f, 0.0f).mining(0.1f, 0.0f).waterMining(1.25f, 0.0f)
                    .stepAssist(1.0f, 0.0f).fallProtection(0.5f, 3.0f, 0.0f, 0.0f)
                    .radiationResistance(1.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CIRCUIT_BOARD_ELITE.get()).knockbackResistance(0.10f));

    // --- T3 exo suit ---
    public static final TGArmorStats T3_EXO_STATS = new TGArmorStats("t3_exo", 300, 21.0f, 2.0f)
            .set(TGDamageCategory.FIRE, 18.5f).set(TGDamageCategory.EXPLOSION, 18.5f).set(TGDamageCategory.ENERGY, 19.0f)
            .set(TGDamageCategory.ICE, 18.5f).set(TGDamageCategory.LIGHTNING, 18.5f).set(TGDamageCategory.POISON, 10.0f)
            .set(TGDamageCategory.RADIATION, 12.0f);
    private static final Holder<ArmorMaterial> T3_EXO = material(T3_EXO_STATS, 0, SoundEvents.ARMOR_EQUIP_CHAIN);

    public static final DeferredItem<TGPoweredArmorItem> T3_EXO_HELMET = powered(T3_EXO, ArmorItem.Type.HELMET, T3_EXO_STATS, "t3_exo",
            PowerType.RF, 10, item -> item.speed(0.2f, 0.1f, 0.0f, 0.0f).mining(0.075f, 0.0f)
                    .radiationResistance(0.5f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CARBON_FIBERS.get()).armorModel(MODEL_EXO_0).knockbackResistance(0.15f));
    public static final DeferredItem<TGPoweredArmorItem> T3_EXO_CHESTPLATE = powered(T3_EXO, ArmorItem.Type.CHESTPLATE, T3_EXO_STATS, "t3_exo",
            PowerType.RF, 7200, item -> item.speed(0.2f, 0.1f, 0.0f, 0.0f).mining(0.075f, 0.0f)
                    .battery(() -> TGAmmo.ENERGY_CELL.get(), () -> TGAmmo.ENERGY_CELL_EMPTY.get())
                    .radiationResistance(0.5f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CARBON_FIBERS.get()).armorModel(MODEL_EXO_2).knockbackResistance(0.30f));
    public static final DeferredItem<TGPoweredArmorItem> T3_EXO_LEGGINGS = powered(T3_EXO, ArmorItem.Type.LEGGINGS, T3_EXO_STATS, "t3_exo",
            PowerType.RF, 10, item -> item.speed(0.2f, 0.1f, 0.0f, 0.0f).mining(0.075f, 0.0f)
                    .radiationResistance(0.5f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CARBON_FIBERS.get()).armorModel(MODEL_EXO_1).knockbackResistance(0.20f));
    public static final DeferredItem<TGPoweredArmorItem> T3_EXO_BOOTS = powered(T3_EXO, ArmorItem.Type.BOOTS, T3_EXO_STATS, "t3_exo",
            PowerType.RF, 10, item -> item.speed(0.2f, 0.3f, 0.0f, 0.0f).mining(0.075f, 0.0f).fallProtection(0.5f, 5.0f, 0.0f, 0.0f)
                    .stepAssist(1.0f, 0.0f)
                    .radiationResistance(0.5f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CARBON_FIBERS.get()).armorModel(MODEL_EXO_2).knockbackResistance(0.15f));

    // --- T4 praetor (Nether combat) ---
    public static final TGArmorStats T4_PRAETOR_STATS = new TGArmorStats("t4_praetor", 360, 22.0f, 3.0f).elemental(21.0f)
            .set(TGDamageCategory.EXPLOSION, 21.0f).set(TGDamageCategory.POISON, 21.0f).set(TGDamageCategory.RADIATION, 20.0f);
    private static final Holder<ArmorMaterial> T4_PRAETOR = material(T4_PRAETOR_STATS, 0, SoundEvents.ARMOR_EQUIP_IRON);

    public static final DeferredItem<TGPoweredArmorItem> T4_PRAETOR_HELMET = powered(T4_PRAETOR, ArmorItem.Type.HELMET, T4_PRAETOR_STATS, "t4_praetor",
            PowerType.RF, 10, item -> item.speed(0.15f, 0.3f, 0.0f, 0.0f).waterSpeed(1.25f, 0.0f).mining(0.15f, 0.0f).waterMining(1.25f, 0.0f)
                    .oxygenGear(1.0f, 0.0f)
                    .radiationResistance(1.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CIRCUIT_BOARD_ELITE.get()).knockbackResistance(0.20f).hideFaceSlot());
    public static final DeferredItem<TGPoweredArmorItem> T4_PRAETOR_CHESTPLATE = powered(T4_PRAETOR, ArmorItem.Type.CHESTPLATE, T4_PRAETOR_STATS, "t4_praetor",
            PowerType.RF, 10000, item -> item.battery(() -> TGAmmo.ENERGY_CELL.get(), () -> TGAmmo.ENERGY_CELL_EMPTY.get())
                    .speed(0.15f, 0.03f, 0.0f, 0.0f).waterSpeed(1.25f, 0.0f).mining(0.15f, 0.0f).waterMining(1.25f, 0.0f)
                    .cooling(1.0f, 0.0f)
                    .radiationResistance(1.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CIRCUIT_BOARD_ELITE.get()).knockbackResistance(0.35f).hideHandSlot());
    public static final DeferredItem<TGPoweredArmorItem> T4_PRAETOR_LEGGINGS = powered(T4_PRAETOR, ArmorItem.Type.LEGGINGS, T4_PRAETOR_STATS, "t4_praetor",
            PowerType.RF, 10, item -> item.speed(0.15f, 0.03f, 0.0f, 0.0f).waterSpeed(1.25f, 0.0f).mining(0.15f, 0.0f).waterMining(1.25f, 0.0f)
                    .radiationResistance(1.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CIRCUIT_BOARD_ELITE.get()).knockbackResistance(0.25f));
    public static final DeferredItem<TGPoweredArmorItem> T4_PRAETOR_BOOTS = powered(T4_PRAETOR, ArmorItem.Type.BOOTS, T4_PRAETOR_STATS, "t4_praetor",
            PowerType.RF, 10, item -> item.speed(0.15f, 0.15f, 0.0f, 0.0f).waterSpeed(1.25f, 0.0f).mining(0.15f, 0.0f).waterMining(1.25f, 0.0f)
                    .stepAssist(1.0f, 0.0f).fallProtection(0.5f, 3.0f, 0.0f, 0.0f)
                    .radiationResistance(1.0f).repairMats(() -> TGItems.CARBON_PLATE.get(), () -> TGItems.CIRCUIT_BOARD_ELITE.get()).knockbackResistance(0.20f));

    // --- T4 power armor Mk2 ---
    public static final TGArmorStats T4_POWER_STATS = new TGArmorStats("powerarmor_mk2_darkgrey", 460, 23.0f, 3.5f).elemental(21.0f)
            .set(TGDamageCategory.EXPLOSION, 22.0f).set(TGDamageCategory.POISON, 19.0f).set(TGDamageCategory.RADIATION, 20.0f);
    private static final Holder<ArmorMaterial> T4_POWER = material(T4_POWER_STATS, 0, SoundEvents.ARMOR_EQUIP_IRON);

    public static final DeferredItem<TGPoweredArmorItem> T4_POWER_HELMET = powered(T4_POWER, ArmorItem.Type.HELMET, T4_POWER_STATS, "t4_power",
            PowerType.RF, 10, item -> item.mining(0.1f, 0.0f).health(2, 0).speed(0.05f, 0.03f, 0.0f, 0.0f)
                    .radiationResistance(1.25f).repairMats(() -> TGItems.POWER_PLATING.get(), () -> TGItems.GOLD_WIRE.get()).armorModel(MODEL_POWER_MK2_2).armorTexture(TGIds.id("textures/models/armor/powerarmor_mk2_darkgrey.png")).knockbackResistance(0.20f).hideFaceSlot());
    public static final DeferredItem<TGPoweredArmorItem> T4_POWER_CHESTPLATE = powered(T4_POWER, ArmorItem.Type.CHESTPLATE, T4_POWER_STATS, "t4_power",
            PowerType.RF, 3600, item -> item.speed(0.05f, 0.03f, 0.0f, 0.0f).mining(0.1f, 0.0f).health(3, 0)
                    .battery(() -> TGAmmo.ENERGY_CELL.get(), () -> TGAmmo.ENERGY_CELL_EMPTY.get())
                    .radiationResistance(1.25f).repairMats(() -> TGItems.POWER_PLATING.get(), () -> TGItems.GOLD_WIRE.get()).armorModel(MODEL_POWER_MK2_0).armorTexture(TGIds.id("textures/models/armor/powerarmor_mk2_darkgrey.png")).knockbackResistance(0.35f).hideHandSlot());
    public static final DeferredItem<TGPoweredArmorItem> T4_POWER_LEGGINGS = powered(T4_POWER, ArmorItem.Type.LEGGINGS, T4_POWER_STATS, "t4_power",
            PowerType.RF, 10, item -> item.speed(0.05f, 0.03f, 0.0f, 0.0f).mining(0.1f, 0.0f).health(2, 0)
                    .radiationResistance(1.25f).repairMats(() -> TGItems.POWER_PLATING.get(), () -> TGItems.GOLD_WIRE.get()).armorModel(MODEL_POWER_MK2_1).armorTexture(TGIds.id("textures/models/armor/powerarmor_mk2_darkgrey.png")).knockbackResistance(0.25f));
    public static final DeferredItem<TGPoweredArmorItem> T4_POWER_BOOTS = powered(T4_POWER, ArmorItem.Type.BOOTS, T4_POWER_STATS, "t4_power",
            PowerType.RF, 10, item -> item.speed(0.05f, 0.15f, 0.0f, 0.0f).mining(0.1f, 0.0f).fallProtection(0.4f, 1.0f, 0.0f, 0.0f).health(2, 0)
                    .stepAssist(1.0f, 0.0f)
                    .radiationResistance(1.25f).repairMats(() -> TGItems.POWER_PLATING.get(), () -> TGItems.GOLD_WIRE.get()).armorModel(MODEL_POWER_MK2_0).armorTexture(TGIds.id("textures/models/armor/powerarmor_mk2_darkgrey.png")).knockbackResistance(0.20f));

    // --- T2 beret (helmet only) ---
    public static final TGArmorStats T2_BERET_STATS = new TGArmorStats("t2_beret", 60, 8.0f, 0.0f);
    private static final Holder<ArmorMaterial> T2_BERET = material(T2_BERET_STATS, 0, SoundEvents.ARMOR_EQUIP_LEATHER);

    public static final DeferredItem<TGArmorItem> T2_BERET_ITEM = armorNamed("t2_beret", T2_BERET, ArmorItem.Type.HELMET, T2_BERET_STATS,
            item -> item.speed(0.1f, 0.0f).repairMats(() -> TGItems.HEAVY_CLOTH.get(), () -> TGItems.HEAVY_CLOTH.get())
                    .armorModel(MODEL_BERET).armorTexture(TGIds.id("textures/models/armor/beret_texture.png")));

    // --- Shields (Techguns 2 GenericShield: durability, stats, repair material) ---
    private static final List<DeferredItem<TGShieldItem>> SHIELDS = new ArrayList<>();
    public static final DeferredItem<TGShieldItem> RIOT_SHIELD = shield("riot_shield", 1200, ShieldStats.RIOT_SHIELD, TGTags.Items.ingot("steel"));
    public static final DeferredItem<TGShieldItem> BALLISTIC_SHIELD = shield("ballistic_shield", 2400, ShieldStats.BALLISTIC_SHIELD,
            TGTags.Items.plate("obsidian_steel"));
    /** Not in Techguns 2 (model only); stats and recipe are new, see ShieldStats.HEAVY_SHIELD. */
    public static final DeferredItem<TGShieldItem> HEAVY_SHIELD = shield("heavy_shield", 2800, ShieldStats.HEAVY_SHIELD,
            TGTags.Items.plate("steel"));
    public static final DeferredItem<TGShieldItem> ADVANCED_SHIELD = shield("advanced_shield", 3200, ShieldStats.ADVANCED_SHIELD,
            TGTags.Items.plate("carbon"));

    public static List<DeferredItem<TGShieldItem>> shields() {
        return Collections.unmodifiableList(SHIELDS);
    }

    /** Techguns 2 shields need 3 of their repair material for a full Repair Bench repair. */
    private static DeferredItem<TGShieldItem> shield(String name, int durability, ShieldStats stats, TagKey<Item> repair) {
        DeferredItem<TGShieldItem> item = TGItems.ITEMS.registerItem(name, properties -> new TGShieldItem(properties, durability, stats, repair, 3));
        SHIELDS.add(item);
        return item;
    }

    private TGArmors() {
    }

    /** Loads the item holders before the item register is attached. */
    public static void init() {
    }

    public static List<DeferredItem<TGArmorItem>> armors() {
        return Collections.unmodifiableList(ARMORS);
    }

    /** Armor drawn as its 3D armor model in the inventory and hands (Techguns 2 {@code RenderArmorItem}: steam, T3 and T4 power armor). */
    public static List<DeferredItem<? extends TGArmorItem>> modelIconArmors() {
        return List.of(STEAM_HELMET, STEAM_CHESTPLATE, STEAM_LEGGINGS, STEAM_BOOTS,
                T3_POWER_HELMET, T3_POWER_CHESTPLATE, T3_POWER_LEGGINGS, T3_POWER_BOOTS,
                T4_POWER_HELMET, T4_POWER_CHESTPLATE, T4_POWER_LEGGINGS, T4_POWER_BOOTS);
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

    /** @param prefix item id prefix, differs from the texture name ({@code stats.name()}) for powered sets */
    private static DeferredItem<TGPoweredArmorItem> powered(Holder<ArmorMaterial> material, ArmorItem.Type type, TGArmorStats stats,
                                                            String prefix, PowerType powerType, int maxPower,
                                                            Consumer<TGPoweredArmorItem> setup) {
        String name = prefix + "_" + switch (type) {
            case HELMET -> "helmet";
            case CHESTPLATE -> "chestplate";
            case LEGGINGS -> "leggings";
            case BOOTS -> "boots";
            case BODY -> "body";
        };
        DeferredItem<TGPoweredArmorItem> item = TGItems.ITEMS.registerItem(name, properties -> {
            TGPoweredArmorItem armor = new TGPoweredArmorItem(material, type, properties, stats, powerType, maxPower);
            setup.accept(armor);
            return armor;
        });
        registerArmor(item);
        return item;
    }

    @SuppressWarnings("unchecked")
    private static void registerArmor(DeferredItem<? extends TGArmorItem> item) {
        ARMORS.add((DeferredItem<TGArmorItem>) item);
    }
}
