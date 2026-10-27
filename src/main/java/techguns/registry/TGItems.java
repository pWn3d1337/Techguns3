package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.item.AmmoBagItem;

/**
 * Techguns items. Plain material/component items are registered with {@link #simple} and get their
 * model, creative tab entry and default English name from datagen automatically.
 */
public final class TGItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Techguns.MODID);

    /** Items using a flat {@code item/generated} model with a texture at {@code textures/item/<name>.png}. */
    private static final List<DeferredItem<Item>> SIMPLE_ITEMS = new ArrayList<>();

    // --- Raw materials ---
    public static final DeferredItem<Item> RAW_TIN = simple("raw_tin");
    public static final DeferredItem<Item> RAW_LEAD = simple("raw_lead");
    public static final DeferredItem<Item> RAW_TITANIUM = simple("raw_titanium");
    public static final DeferredItem<Item> RAW_URANIUM = simple("raw_uranium");

    // --- Ingots ---
    public static final DeferredItem<Item> TIN_INGOT = simple("tin_ingot");
    public static final DeferredItem<Item> LEAD_INGOT = simple("lead_ingot");
    public static final DeferredItem<Item> BRONZE_INGOT = simple("bronze_ingot");
    public static final DeferredItem<Item> STEEL_INGOT = simple("steel_ingot");
    public static final DeferredItem<Item> OBSIDIAN_STEEL_INGOT = simple("obsidian_steel_ingot");
    public static final DeferredItem<Item> TITANIUM_INGOT = simple("titanium_ingot");

    // --- Nuggets ---
    public static final DeferredItem<Item> COPPER_NUGGET = simple("copper_nugget");
    public static final DeferredItem<Item> LEAD_NUGGET = simple("lead_nugget");
    public static final DeferredItem<Item> STEEL_NUGGET = simple("steel_nugget");

    // --- Plates ---
    public static final DeferredItem<Item> IRON_PLATE = simple("iron_plate");
    public static final DeferredItem<Item> COPPER_PLATE = simple("copper_plate");
    public static final DeferredItem<Item> TIN_PLATE = simple("tin_plate");
    public static final DeferredItem<Item> BRONZE_PLATE = simple("bronze_plate");
    public static final DeferredItem<Item> STEEL_PLATE = simple("steel_plate");
    public static final DeferredItem<Item> OBSIDIAN_STEEL_PLATE = simple("obsidian_steel_plate");
    public static final DeferredItem<Item> LEAD_PLATE = simple("lead_plate");
    public static final DeferredItem<Item> CARBON_PLATE = simple("carbon_plate");
    public static final DeferredItem<Item> TITANIUM_PLATE = simple("titanium_plate");

    // --- Other materials ---
    public static final DeferredItem<Item> PLASTIC_SHEET = simple("plastic_sheet");
    public static final DeferredItem<Item> RUBBER_BAR = simple("rubber_bar");
    public static final DeferredItem<Item> RAW_RUBBER = simple("raw_rubber");
    public static final DeferredItem<Item> RAW_PLASTIC = simple("raw_plastic");
    public static final DeferredItem<Item> YELLOWCAKE = simple("yellowcake");
    public static final DeferredItem<Item> ENRICHED_URANIUM = simple("enriched_uranium");
    public static final DeferredItem<Item> QUARTZ_ROD = simple("quartz_rod");
    public static final DeferredItem<Item> TGX = simple("tgx");
    public static final DeferredItem<Item> TREATED_LEATHER = simple("treated_leather");
    public static final DeferredItem<Item> HEAVY_CLOTH = simple("heavy_cloth");
    public static final DeferredItem<Item> BIOMASS = simple("biomass");
    public static final DeferredItem<Item> COPPER_WIRE = simple("copper_wire");
    public static final DeferredItem<Item> GOLD_WIRE = simple("gold_wire");
    public static final DeferredItem<Item> CARBON_FIBERS = simple("carbon_fibers");

    // --- Components ---
    public static final DeferredItem<Item> IRON_RECEIVER = simple("iron_receiver");
    public static final DeferredItem<Item> STEEL_RECEIVER = simple("steel_receiver");
    public static final DeferredItem<Item> OBSIDIAN_STEEL_RECEIVER = simple("obsidian_steel_receiver");
    public static final DeferredItem<Item> CARBON_RECEIVER = simple("carbon_receiver");
    public static final DeferredItem<Item> TITANIUM_RECEIVER = simple("titanium_receiver");
    public static final DeferredItem<Item> STONE_BARREL = simple("stone_barrel");
    public static final DeferredItem<Item> IRON_BARREL = simple("iron_barrel");
    public static final DeferredItem<Item> OBSIDIAN_STEEL_BARREL = simple("obsidian_steel_barrel");
    public static final DeferredItem<Item> CARBON_BARREL = simple("carbon_barrel");
    public static final DeferredItem<Item> LASER_BARREL = simple("laser_barrel");
    public static final DeferredItem<Item> GAUSS_BARREL = simple("gauss_barrel");
    public static final DeferredItem<Item> SHIELDED_TITANIUM_BARREL = simple("shielded_titanium_barrel");
    public static final DeferredItem<Item> WOOD_STOCK = simple("wood_stock");
    public static final DeferredItem<Item> PLASTIC_STOCK = simple("plastic_stock");
    public static final DeferredItem<Item> CARBON_STOCK = simple("carbon_stock");
    public static final DeferredItem<Item> STEAM_ARMOR_PLATE = simple("steam_armor_plate");
    public static final DeferredItem<Item> MECHANICAL_PARTS_IRON = simple("mechanical_parts_iron");
    public static final DeferredItem<Item> MECHANICAL_PARTS_OBSIDIAN_STEEL = simple("mechanical_parts_obsidian_steel");
    public static final DeferredItem<Item> MECHANICAL_PARTS_CARBON = simple("mechanical_parts_carbon");
    public static final DeferredItem<Item> CIRCUIT_BOARD = simple("circuit_board");
    public static final DeferredItem<Item> CIRCUIT_BOARD_ELITE = simple("circuit_board_elite");
    public static final DeferredItem<Item> CIRCUIT_BOARD_ADVANCED = simple("circuit_board_advanced");
    public static final DeferredItem<Item> POWER_PLATING = simple("power_plating");
    public static final DeferredItem<Item> POWER_PLATING_MK2 = simple("power_plating_mk2");
    public static final DeferredItem<Item> NETHER_ARMOR_PLATING = simple("nether_armor_plating");
    public static final DeferredItem<Item> COIL = simple("coil");
    public static final DeferredItem<Item> CYBERNETIC_PARTS = simple("cybernetic_parts");
    public static final DeferredItem<Item> ELECTRIC_ENGINE = simple("electric_engine");
    public static final DeferredItem<Item> LASER_FOCUS = simple("laser_focus");
    public static final DeferredItem<Item> PUMP_MECHANISM = simple("pump_mechanism");
    public static final DeferredItem<Item> RAD_EMITTER = simple("rad_emitter");
    public static final DeferredItem<Item> SONIC_EMITTER = simple("sonic_emitter");
    public static final DeferredItem<Item> ANTIGRAV_CORE = simple("antigrav_core");
    public static final DeferredItem<Item> PLASMA_GENERATOR = simple("plasma_generator");
    // Mining heads of the melee tools (batch 3f)
    public static final DeferredItem<Item> CHAINSAW_BLADES_STEEL = simple("chainsaw_blades_steel");
    public static final DeferredItem<Item> CHAINSAW_BLADES_OBSIDIAN = simple("chainsaw_blades_obsidian");
    public static final DeferredItem<Item> CHAINSAW_BLADES_CARBON = simple("chainsaw_blades_carbon");
    public static final DeferredItem<Item> POWER_HAMMER_HEAD_STEEL = simple("power_hammer_head_steel");
    public static final DeferredItem<Item> POWER_HAMMER_HEAD_OBSIDIAN = simple("power_hammer_head_obsidian");
    public static final DeferredItem<Item> POWER_HAMMER_HEAD_CARBON = simple("power_hammer_head_carbon");
    public static final DeferredItem<Item> MINING_DRILL_HEAD_OBSIDIAN = simple("mining_drill_head_obsidian");
    public static final DeferredItem<Item> MINING_DRILL_HEAD_CARBON = simple("mining_drill_head_carbon");
    public static final DeferredItem<Item> PROTECTIVE_FIBER = simple("protective_fiber");
    public static final DeferredItem<Item> HEAT_RAY_FOCUS = simple("heat_ray_focus");
    public static final DeferredItem<Item> UV_EMITTER = simple("uv_emitter");

    // --- Machines ---
    /** Techguns 2 {@code machinestackupgrade}: each one lets a machine run one more operation at once (stack of 7). */
    public static final DeferredItem<Item> MACHINE_UPGRADE_STACK = simple("machine_upgrade_stack", new Item.Properties().stacksTo(7));

    // --- Armor upgrades (Upgrade Bench: protection, projectile and blast protection I-III) ---
    public static final DeferredItem<Item> UPGRADE_PROTECTION_1 = simple("upgrade_protection_1");
    public static final DeferredItem<Item> UPGRADE_PROJECTILE_PROTECTION_1 = simple("upgrade_projectile_protection_1");
    public static final DeferredItem<Item> UPGRADE_BLAST_PROTECTION_1 = simple("upgrade_blast_protection_1");
    public static final DeferredItem<Item> UPGRADE_PROTECTION_2 = simple("upgrade_protection_2");
    public static final DeferredItem<Item> UPGRADE_PROJECTILE_PROTECTION_2 = simple("upgrade_projectile_protection_2");
    public static final DeferredItem<Item> UPGRADE_BLAST_PROTECTION_2 = simple("upgrade_blast_protection_2");
    public static final DeferredItem<Item> UPGRADE_PROTECTION_3 = simple("upgrade_protection_3");
    public static final DeferredItem<Item> UPGRADE_PROJECTILE_PROTECTION_3 = simple("upgrade_projectile_protection_3");
    public static final DeferredItem<Item> UPGRADE_BLAST_PROTECTION_3 = simple("upgrade_blast_protection_3");

    // --- Turret armor plates (Techguns 2 TURRET_ARMOR_*, tier 1-4) ---
    public static final DeferredItem<Item> TURRET_ARMOR_IRON = simple("turret_armor_iron");
    public static final DeferredItem<Item> TURRET_ARMOR_STEEL = simple("turret_armor_steel");
    public static final DeferredItem<Item> TURRET_ARMOR_OBSIDIAN_STEEL = simple("turret_armor_obsidian_steel");
    public static final DeferredItem<Item> TURRET_ARMOR_CARBON = simple("turret_armor_carbon");

    // --- Ore Drill heads (Techguns 2 OREDRILLHEAD_*): size by drill, mining level steel 1, obsidian steel 2, carbon 3 ---
    private static final List<DeferredItem<techguns.item.OreDrillHeadItem>> ORE_DRILL_HEADS = new ArrayList<>();
    public static final DeferredItem<techguns.item.OreDrillHeadItem> ORE_DRILL_HEAD_SMALL_STEEL = drillHead("small_steel", techguns.item.OreDrillHeadItem.Size.SMALL, 1);
    public static final DeferredItem<techguns.item.OreDrillHeadItem> ORE_DRILL_HEAD_SMALL_OBSIDIAN_STEEL = drillHead("small_obsidian_steel",
            techguns.item.OreDrillHeadItem.Size.SMALL, 2);
    public static final DeferredItem<techguns.item.OreDrillHeadItem> ORE_DRILL_HEAD_SMALL_CARBON = drillHead("small_carbon", techguns.item.OreDrillHeadItem.Size.SMALL, 3);
    public static final DeferredItem<techguns.item.OreDrillHeadItem> ORE_DRILL_HEAD_MEDIUM_STEEL = drillHead("medium_steel", techguns.item.OreDrillHeadItem.Size.MEDIUM, 1);
    public static final DeferredItem<techguns.item.OreDrillHeadItem> ORE_DRILL_HEAD_MEDIUM_OBSIDIAN_STEEL = drillHead("medium_obsidian_steel",
            techguns.item.OreDrillHeadItem.Size.MEDIUM, 2);
    public static final DeferredItem<techguns.item.OreDrillHeadItem> ORE_DRILL_HEAD_MEDIUM_CARBON = drillHead("medium_carbon", techguns.item.OreDrillHeadItem.Size.MEDIUM, 3);
    public static final DeferredItem<techguns.item.OreDrillHeadItem> ORE_DRILL_HEAD_LARGE_STEEL = drillHead("large_steel", techguns.item.OreDrillHeadItem.Size.LARGE, 1);
    public static final DeferredItem<techguns.item.OreDrillHeadItem> ORE_DRILL_HEAD_LARGE_OBSIDIAN_STEEL = drillHead("large_obsidian_steel",
            techguns.item.OreDrillHeadItem.Size.LARGE, 2);
    public static final DeferredItem<techguns.item.OreDrillHeadItem> ORE_DRILL_HEAD_LARGE_CARBON = drillHead("large_carbon", techguns.item.OreDrillHeadItem.Size.LARGE, 3);

    private TGItems() {
    }

    private static DeferredItem<techguns.item.OreDrillHeadItem> drillHead(String name, techguns.item.OreDrillHeadItem.Size size, int miningLevel) {
        DeferredItem<techguns.item.OreDrillHeadItem> item = ITEMS.registerItem("ore_drill_head_" + name,
                props -> new techguns.item.OreDrillHeadItem(props, size, miningLevel));
        ORE_DRILL_HEADS.add(item);
        return item;
    }

    public static List<DeferredItem<techguns.item.OreDrillHeadItem>> oreDrillHeads() {
        return Collections.unmodifiableList(ORE_DRILL_HEADS);
    }

    // --- Ammo Bags (capacity in rounds; a full stack counts 64) ---
    public static final DeferredItem<Item> AMMO_BOX = ITEMS.registerItem("ammo_box", props -> new AmmoBagItem(props, 64 * 4));
    public static final DeferredItem<Item> AMMO_BOX_LARGE = ITEMS.registerItem("ammo_box_large", props -> new AmmoBagItem(props, 64 * 8));

    private static DeferredItem<Item> simple(String name) {
        return simple(name, new Item.Properties());
    }

    private static DeferredItem<Item> simple(String name, Item.Properties properties) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem(name, properties);
        SIMPLE_ITEMS.add(item);
        return item;
    }

    public static List<DeferredItem<Item>> simpleItems() {
        return Collections.unmodifiableList(SIMPLE_ITEMS);
    }
}
