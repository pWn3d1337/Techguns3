package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;

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
    public static final DeferredItem<Item> RAW_STEEL_INGOT = simple("raw_steel_ingot");
    public static final DeferredItem<Item> RAW_OBSIDIAN_STEEL_INGOT = simple("raw_obsidian_steel_ingot");

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

    private TGItems() {
    }

    private static DeferredItem<Item> simple(String name) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem(name);
        SIMPLE_ITEMS.add(item);
        return item;
    }

    public static List<DeferredItem<Item>> simpleItems() {
        return Collections.unmodifiableList(SIMPLE_ITEMS);
    }
}
