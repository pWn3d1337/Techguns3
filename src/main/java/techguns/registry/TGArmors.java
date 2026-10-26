package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
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

/**
 * Techguns armor sets (Techguns 2 {@code TGArmors}: values, bonuses and repair items). Vanilla armor materials only
 * carry the equip sound, enchantability and texture layers; armor values come from {@link TGArmorStats}.
 */
public final class TGArmors {
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Techguns.MODID);

    private static final List<DeferredItem<TGArmorItem>> ARMORS = new ArrayList<>();

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
        String name = stats.name() + "_" + switch (type) {
            case HELMET -> "helmet";
            case CHESTPLATE -> "chestplate";
            case LEGGINGS -> "leggings";
            case BOOTS -> "boots";
            case BODY -> "body";
        };
        DeferredItem<TGArmorItem> item = TGItems.ITEMS.registerItem(name,
                properties -> setup.apply(new TGArmorItem(material, type, properties, stats)));
        ARMORS.add(item);
        return item;
    }
}
