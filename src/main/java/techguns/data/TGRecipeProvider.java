package techguns.data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import techguns.TGIds;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.MeleeToolItem;
import techguns.gun.ammo.AmmoType;
import techguns.recipe.AmmoChangeRecipe;
import techguns.recipe.MetalPressRecipe;
import techguns.recipe.MiningHeadUpgradeRecipe;
import techguns.registry.TGAmmo;
import techguns.registry.TGArmors;
import techguns.registry.TGBlocks;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;
import techguns.registry.TGSlotItems;
import techguns.registry.TGTags;

/**
 * Crafting and cooking recipes. Recipes that need Techguns machines are added in Phase 5.
 */
public class TGRecipeProvider extends RecipeProvider {
    public TGRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        smeltingAndBlasting(output, List.of(TGItems.RAW_TIN.get(), TGBlocks.TIN_ORE.get(), TGBlocks.DEEPSLATE_TIN_ORE.get()),
                TGItems.TIN_INGOT.get(), 0.7f, "tin_ingot");
        smeltingAndBlasting(output, List.of(TGItems.RAW_LEAD.get(), TGBlocks.LEAD_ORE.get(), TGBlocks.DEEPSLATE_LEAD_ORE.get()),
                TGItems.LEAD_INGOT.get(), 0.8f, "lead_ingot");
        blastingOnly(output, List.of(TGItems.RAW_TITANIUM.get(), TGBlocks.TITANIUM_ORE.get(), TGBlocks.DEEPSLATE_TITANIUM_ORE.get()),
                TGItems.TITANIUM_INGOT.get(), 1.5f, 200, "titanium_ingot");
        blastingOnly(output, List.of(TGItems.RAW_STEEL_INGOT.get()), TGItems.STEEL_INGOT.get(), 1.2f, 200, "steel_ingot");
        blastingOnly(output, List.of(TGItems.RAW_OBSIDIAN_STEEL_INGOT.get()), TGItems.OBSIDIAN_STEEL_INGOT.get(), 1.5f, 200,
                "obsidian_steel_ingot");
        smeltingAndBlasting(output, List.of(TGItems.RAW_RUBBER.get()), TGItems.RUBBER_BAR.get(), 0.5f, "rubber_bar");

        buildNuggetRecipes(output);
        buildMaterialRecipes(output);
        buildComponentRecipes(output);
        buildAmmoRecipes(output);
        buildGunRecipes(output);
        buildArmorRecipes(output);
        buildShieldRecipes(output);
        buildBlockRecipes(output);
        buildMachineRecipes(output);
        buildSlotItemRecipes(output);
    }

    /** Techguns 2 armor recipes. */
    private void buildArmorRecipes(RecipeOutput output) {
        Item cloth = TGItems.HEAVY_CLOTH.get();
        armor(output, TGArmors.T1_COMBAT_HELMET.get(), cloth, b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth), "iii", "c c");
        armor(output, TGArmors.T1_COMBAT_CHESTPLATE.get(), cloth, b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth),
                "c c", "iii", "ccc");
        armor(output, TGArmors.T1_COMBAT_LEGGINGS.get(), cloth, b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth),
                "iii", "c c", "c c");
        armor(output, TGArmors.T1_COMBAT_BOOTS.get(), cloth, b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth), "c c", "i i");

        // T1 miner (dyeYellow helmet, nuggetIron boots)
        Consumer<ShapedRecipeBuilder> minerKeys = b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth);
        armor(output, TGArmors.T1_MINER_HELMET.get(), cloth, b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth)
                .define('y', Tags.Items.DYES_YELLOW), "iyi", "c c");
        armor(output, TGArmors.T1_MINER_CHESTPLATE.get(), cloth, minerKeys, "c c", "ccc", "ici");
        armor(output, TGArmors.T1_MINER_LEGGINGS.get(), cloth, minerKeys, "ici", "c c", "c c");
        armor(output, TGArmors.T1_MINER_BOOTS.get(), cloth, b -> b.define('i', Tags.Items.NUGGETS_IRON).define('c', cloth), "c c", "i i");

        // T1 scout
        Consumer<ShapedRecipeBuilder> clothOnly = b -> b.define('c', cloth);
        armor(output, TGArmors.T1_SCOUT_HELMET.get(), cloth, clothOnly, "ccc", "c c");
        armor(output, TGArmors.T1_SCOUT_CHESTPLATE.get(), cloth, clothOnly, "c c", "ccc", "ccc");
        armor(output, TGArmors.T1_SCOUT_LEGGINGS.get(), cloth, clothOnly, "ccc", "c c", "c c");
        armor(output, TGArmors.T1_SCOUT_BOOTS.get(), cloth, clothOnly, "c c", "c c");

        // T2 combat
        TagKey<Item> obsidianSteel = TGTags.Items.ingot("obsidian_steel");
        Consumer<ShapedRecipeBuilder> combatKeys = b -> b.define('i', obsidianSteel).define('h', cloth);
        armor(output, TGArmors.T2_COMBAT_HELMET.get(), cloth, combatKeys, "iii", "h h");
        armor(output, TGArmors.T2_COMBAT_CHESTPLATE.get(), cloth, combatKeys, "h h", "iii", "iii");
        armor(output, TGArmors.T2_COMBAT_LEGGINGS.get(), cloth, combatKeys, "ihi", "i i", "h h");
        armor(output, TGArmors.T2_COMBAT_BOOTS.get(), cloth, combatKeys, "h h", "i i");

        // T2 commando
        Item rubber = TGItems.RUBBER_BAR.get();
        Consumer<ShapedRecipeBuilder> commandoKeys = b -> b.define('i', obsidianSteel).define('h', rubber);
        armor(output, TGArmors.T2_COMMANDO_HELMET.get(), rubber, b -> b.define('i', obsidianSteel).define('h', rubber)
                .define('g', Tags.Items.GLASS_PANES), "ihi", "hgh");
        armor(output, TGArmors.T2_COMMANDO_CHESTPLATE.get(), rubber, commandoKeys, "h h", "hih", "iii");
        armor(output, TGArmors.T2_COMMANDO_LEGGINGS.get(), rubber, commandoKeys, "hih", "i i", "h h");
        armor(output, TGArmors.T2_COMMANDO_BOOTS.get(), rubber, commandoKeys, "i i", "h h");

        // T3 combat
        Item fibers = TGItems.CARBON_FIBERS.get();
        Consumer<ShapedRecipeBuilder> t3Keys = b -> b.define('p', TGTags.Items.plate("carbon")).define('f', fibers);
        armor(output, TGArmors.T3_COMBAT_HELMET.get(), fibers, t3Keys, "ppp", "f f");
        armor(output, TGArmors.T3_COMBAT_CHESTPLATE.get(), fibers, t3Keys, "p p", "ppp", "fpf");
        armor(output, TGArmors.T3_COMBAT_LEGGINGS.get(), fibers, t3Keys, "pfp", "p p", "f f");
        armor(output, TGArmors.T3_COMBAT_BOOTS.get(), fibers, t3Keys, "f f", "p p");

        // T2 riot: upgrade of the matching T2 combat piece
        Item treated = TGItems.TREATED_LEATHER.get();
        Consumer<ShapedRecipeBuilder> riotKeys = b -> b.define('p', TGTags.Items.plate("lead")).define('l', treated);
        armor(output, TGArmors.T2_RIOT_HELMET.get(), treated,
                b -> riotKeys.accept(b.define('h', TGArmors.T2_COMBAT_HELMET.get())), "php", "l l");
        armor(output, TGArmors.T2_RIOT_CHESTPLATE.get(), treated,
                b -> riotKeys.accept(b.define('c', TGArmors.T2_COMBAT_CHESTPLATE.get())), "p p", "lcl", "lpl");
        armor(output, TGArmors.T2_RIOT_LEGGINGS.get(), treated,
                b -> riotKeys.accept(b.define('h', TGArmors.T2_COMBAT_LEGGINGS.get())), "lhl", "p p", "l l");
        armor(output, TGArmors.T2_RIOT_BOOTS.get(), treated,
                b -> riotKeys.accept(b.define('h', TGArmors.T2_COMBAT_BOOTS.get())), "lhl", "p p");

        // Beret
        armor(output, TGArmors.T2_BERET_ITEM.get(), cloth, clothOnly, " cc", "c c");

        // Hazmat suit
        Item fiber = TGItems.PROTECTIVE_FIBER.get();
        Consumer<ShapedRecipeBuilder> hazmatKeys = b -> b.define('f', fiber);
        armor(output, TGArmors.HAZMAT_HELMET.get(), fiber, hazmatKeys, "fff", "f f");
        armor(output, TGArmors.HAZMAT_CHESTPLATE.get(), fiber, hazmatKeys, "f f", "fff", "fff");
        armor(output, TGArmors.HAZMAT_LEGGINGS.get(), fiber, hazmatKeys, "fff", "f f", "f f");
        armor(output, TGArmors.HAZMAT_BOOTS.get(), fiber, hazmatKeys, "f f", "f f");

        // Steam armor
        Item steamPlate = TGItems.STEAM_ARMOR_PLATE.get();
        Consumer<ShapedRecipeBuilder> steamKeys = b -> b.define('s', steamPlate);
        armor(output, TGArmors.STEAM_HELMET.get(), steamPlate, steamKeys, "sss", "s s");
        armor(output, TGArmors.STEAM_CHESTPLATE.get(), steamPlate, steamKeys, "s s", "sss", "sss");
        armor(output, TGArmors.STEAM_LEGGINGS.get(), steamPlate, steamKeys, "sss", "s s", "s s");
        armor(output, TGArmors.STEAM_BOOTS.get(), steamPlate, steamKeys, "s s", "s s");

        // T3 power armor
        Item powerPlating = TGItems.POWER_PLATING.get();
        Item elite = TGItems.CIRCUIT_BOARD_ELITE.get();
        Item engine = TGItems.ELECTRIC_ENGINE.get();
        Map<Character, Object> powerKeys = Map.of('p', powerPlating, 'c', elite, 'e', engine, 'g', Tags.Items.GLASS_PANES);
        usedKeys(output, TGArmors.T3_POWER_HELMET.get(), powerPlating, powerKeys, "cpc", "pgp");
        usedKeys(output, TGArmors.T3_POWER_CHESTPLATE.get(), powerPlating, powerKeys, "p p", "ece", "ppp");
        usedKeys(output, TGArmors.T3_POWER_LEGGINGS.get(), powerPlating, powerKeys, "pcp", "e e", "p p");
        usedKeys(output, TGArmors.T3_POWER_BOOTS.get(), powerPlating, powerKeys, "e e", "p p");

        // T3 miner (HEV suit)
        Map<Character, Object> hevKeys = Map.of('p', TGTags.Items.plate("carbon"), 'c', elite,
                'l', TGTags.Items.plate("titanium"), 'g', Tags.Items.GLASS_PANES);
        usedKeys(output, TGArmors.T3_MINER_HELMET.get(), elite, hevKeys, "pcp", "lgl");
        usedKeys(output, TGArmors.T3_MINER_CHESTPLATE.get(), elite, hevKeys, "p p", "lcl", "lpl");
        usedKeys(output, TGArmors.T3_MINER_LEGGINGS.get(), elite, hevKeys, "ppp", "c c", "l l");
        usedKeys(output, TGArmors.T3_MINER_BOOTS.get(), elite, hevKeys, "l l", "p p");

        // T3 exo suit: upgrade of the matching T3 combat piece
        exoSuit(output, TGArmors.T3_EXO_HELMET.get(), TGArmors.T3_COMBAT_HELMET.get(), 1);
        exoSuit(output, TGArmors.T3_EXO_CHESTPLATE.get(), TGArmors.T3_COMBAT_CHESTPLATE.get(), 2);
        exoSuit(output, TGArmors.T3_EXO_LEGGINGS.get(), TGArmors.T3_COMBAT_LEGGINGS.get(), 2);
        exoSuit(output, TGArmors.T3_EXO_BOOTS.get(), TGArmors.T3_COMBAT_BOOTS.get(), 1);
        // Techguns 2 has no crafting recipes for the T4 praetor and T4 power armor.
    }

    /** Techguns 2 recipes of the extra slot gear (face, back and hand items). */
    private void buildSlotItemRecipes(RecipeOutput output) {
        Item cloth = TGItems.HEAVY_CLOTH.get();
        Item rubber = TGItems.RUBBER_BAR.get();
        Item plastic = TGItems.PLASTIC_SHEET.get();
        Item basicCircuit = TGItems.CIRCUIT_BOARD.get();
        Item filter = TGSlotItems.GAS_MASK_FILTER.get();
        Item backpack = TGSlotItems.GLIDER_BACKPACK.get();
        Item wing = TGSlotItems.GLIDER_WING.get();
        TagKey<Item> ironPlate = TGTags.Items.plate("iron");

        // glider
        slotShaped(output, backpack, cloth, "glider_backpack", Map.of('h', cloth, 's', Tags.Items.INGOTS_IRON), "hhh", "shs", "hhh");
        slotShaped(output, wing, cloth, "glider_wing", Map.of('h', cloth, 's', Tags.Items.INGOTS_IRON), "sss", "hhh", "hhh");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGSlotItems.GLIDER.get())
                .requires(backpack).requires(wing, 2).unlockedBy(getHasName(wing), has(wing)).save(output, TGIds.id("glider"));

        // gas mask; a filter refills a used mask
        slotShaped(output, filter, Items.COAL, "gas_mask_filter", Map.of('i', Tags.Items.NUGGETS_IRON, 'c', Items.COAL), "iii", "ici", "iii");
        slotShaped(output, TGSlotItems.GAS_MASK.get(), filter, "gas_mask", Map.of('r', rubber, 'f', filter, 'g', Tags.Items.GLASS_PANES),
                "grg", "rrr", "rfr");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGSlotItems.GAS_MASK.get())
                .requires(TGSlotItems.GAS_MASK.get()).requires(filter).unlockedBy(getHasName(filter), has(filter))
                .save(output, TGIds.id("gas_mask_refilter"));

        // night vision goggles; with an empty battery they come out empty
        Map<Character, Object> goggleKeys = Map.of('r', rubber, 'h', cloth, 'c', basicCircuit, 'e', TGAmmo.REDSTONE_BATTERY,
                'g', Tags.Items.GLASS_BLOCKS, 's', Tags.Items.INGOTS_IRON);
        slotShaped(output, TGSlotItems.NIGHT_VISION_GOGGLES.get(), basicCircuit, "night_vision_goggles", goggleKeys, "rhr", "cec", "gsg");
        ItemStack emptyGoggles = new ItemStack(TGSlotItems.NIGHT_VISION_GOGGLES.get());
        emptyGoggles.setDamageValue(emptyGoggles.getMaxDamage());
        StackShapedRecipeBuilder emptyBuilder = StackShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, emptyGoggles, "rhr", "cec", "gsg")
                .define('r', rubber).define('h', cloth).define('c', basicCircuit).define('e', TGAmmo.REDSTONE_BATTERY_EMPTY)
                .define('g', Tags.Items.GLASS_BLOCKS).define('s', Tags.Items.INGOTS_IRON);
        emptyBuilder.unlockedBy(getHasName(basicCircuit), has(basicCircuit)).save(output, TGIds.id("night_vision_goggles_empty"));

        // jetpack from a glider or a jump pack
        Map<Character, Object> jetpackKeys = Map.of('f', TGAmmo.FUEL_TANK, 'p', TGTags.Items.plate("obsidian_steel"),
                'r', TGAmmo.ROCKET, 'm', TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL);
        slotShaped(output, TGSlotItems.JETPACK.get(), TGAmmo.FUEL_TANK.get(), "jetpack_from_glider",
                with(jetpackKeys, 'g', TGSlotItems.GLIDER), "f f", "pgp", "rmr");
        slotShaped(output, TGSlotItems.JETPACK.get(), TGAmmo.FUEL_TANK.get(), "jetpack_from_jump_pack",
                with(jetpackKeys, 'g', TGSlotItems.JUMP_PACK), "f f", "pgp", "rmr");

        slotShaped(output, TGSlotItems.ANTIGRAV_PACK.get(), TGItems.ANTIGRAV_CORE.get(), "antigrav_pack",
                Map.of('t', TGTags.Items.plate("titanium"), 'e', TGAmmo.NUCLEAR_POWER_CELL, 'b', backpack,
                        'c', TGItems.CIRCUIT_BOARD_ELITE, 'm', TGItems.MECHANICAL_PARTS_CARBON, 'a', TGItems.ANTIGRAV_CORE),
                "tet", "cac", "mbm");
        slotShaped(output, TGSlotItems.SCUBA_TANKS.get(), TGAmmo.COMPRESSED_AIR_TANK.get(), "scuba_tanks",
                Map.of('p', plastic, 'a', TGAmmo.COMPRESSED_AIR_TANK, 'i', ironPlate), "p p", "aia", "p p");
        slotShaped(output, TGSlotItems.JUMP_PACK.get(), backpack, "jump_pack",
                Map.of('p', ironPlate, 'c', TGAmmo.COMPRESSED_AIR_TANK, 'n', Tags.Items.NUGGETS_IRON, 'b', backpack), "n n", "pbp", "c c");
        slotShaped(output, TGSlotItems.OXYGEN_MASK.get(), plastic, "oxygen_mask", Map.of('p', plastic, 'r', rubber), " p ", "rpr");

        // tactical mask, both arrangements
        Map<Character, Object> tacticalKeys = Map.of('p', plastic, 'n', TGSlotItems.NIGHT_VISION_GOGGLES, 'c', basicCircuit,
                'g', TGSlotItems.GAS_MASK, 'o', TGSlotItems.OXYGEN_MASK, 'e', TGAmmo.ENERGY_CELL);
        slotShaped(output, TGSlotItems.TACTICAL_MASK.get(), TGSlotItems.OXYGEN_MASK.get(), "tactical_mask", tacticalKeys, "ngo", "cpc", "pep");
        slotShaped(output, TGSlotItems.TACTICAL_MASK.get(), TGSlotItems.OXYGEN_MASK.get(), "tactical_mask_mirrored", tacticalKeys,
                "ogn", "cpc", "pep");

        slotShaped(output, TGSlotItems.WORKING_GLOVES.get(), cloth, "working_gloves", Map.of('h', cloth), " h", "hh");
    }

    private static Map<Character, Object> with(Map<Character, Object> keys, char symbol, Object ingredient) {
        Map<Character, Object> copy = new java.util.HashMap<>(keys);
        copy.put(symbol, ingredient);
        return copy;
    }

    /** Shaped recipe with an explicit id; keys are items, item holders or item tags. */
    private static void slotShaped(RecipeOutput output, Item result, ItemLike unlock, String id, Map<Character, ?> keys, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result);
        for (String row : pattern) {
            builder.pattern(row);
        }
        keys.forEach((symbol, ingredient) -> {
            if (ingredient instanceof TagKey<?> tag) {
                @SuppressWarnings("unchecked")
                TagKey<Item> itemTag = (TagKey<Item>) tag;
                builder.define(symbol, itemTag);
            } else {
                builder.define(symbol, (ItemLike) ingredient);
            }
        });
        builder.unlockedBy(getHasName(unlock), has(unlock)).save(output, TGIds.id(id));
    }

    /** Shaped armor recipe that only defines the keys the pattern uses. */
    private static void usedKeys(RecipeOutput output, Item armor, ItemLike unlock, Map<Character, Object> keys, String... pattern) {
        String joined = String.join("", pattern);
        armor(output, armor, unlock, b -> keys.forEach((symbol, ingredient) -> {
            if (joined.indexOf(symbol) >= 0) {
                if (ingredient instanceof TagKey<?> tag) {
                    @SuppressWarnings("unchecked")
                    TagKey<Item> itemTag = (TagKey<Item>) tag;
                    b.define(symbol, itemTag);
                } else {
                    b.define(symbol, (ItemLike) ingredient);
                }
            }
        }), pattern);
    }

    private static void exoSuit(RecipeOutput output, Item exo, Item combat, int titaniumIngots) {
        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, exo)
                .requires(combat).requires(TGItems.MECHANICAL_PARTS_CARBON.get()).requires(TGItems.CIRCUIT_BOARD_ELITE.get())
                .requires(TGItems.ELECTRIC_ENGINE.get());
        for (int i = 0; i < titaniumIngots; i++) {
            builder.requires(TGTags.Items.ingot("titanium"));
        }
        builder.unlockedBy(getHasName(combat), has(combat)).save(output, TGIds.id(getItemName(exo)));
    }

    /** Techguns 2 machines, the machine upgrade and the machine recipes. */
    private void buildMachineRecipes(RecipeOutput output) {
        // Metal Press: Techguns 2 has a block-of-iron and an iron-plate version
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.METAL_PRESS.get())
                .pattern("ibi").pattern("iei").pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON).define('b', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('e', TGItems.ELECTRIC_ENGINE.get()).define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(TGItems.ELECTRIC_ENGINE.get()), has(TGItems.ELECTRIC_ENGINE.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.METAL_PRESS.get())
                .pattern("ibi").pattern("iei").pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON).define('b', TGTags.Items.plate("iron"))
                .define('e', TGItems.ELECTRIC_ENGINE.get()).define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(TGItems.ELECTRIC_ENGINE.get()), has(TGItems.ELECTRIC_ENGINE.get()))
                .save(output, TGIds.id("metal_press_alt"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.MACHINE_UPGRADE_STACK.get())
                .pattern("pgp").pattern("ncn").pattern("pnp")
                .define('p', TGTags.Items.plate("iron")).define('n', Tags.Items.INGOTS_GOLD).define('g', Tags.Items.DYES_GREEN)
                .define('c', Items.CHEST)
                .unlockedBy(getHasName(TGBlocks.METAL_PRESS.get()), has(TGBlocks.METAL_PRESS.get())).save(output);

        // Techguns 2 TGMachineRecipes, in order; all allow swapped inputs
        metalPress(output, "tin_plate", ingot("tin"), ingot("tin"), TGItems.TIN_PLATE.get(), 2);
        metalPress(output, "copper_plate", Ingredient.of(Tags.Items.INGOTS_COPPER), Ingredient.of(Tags.Items.INGOTS_COPPER), TGItems.COPPER_PLATE.get(), 2);
        metalPress(output, "bronze_plate", ingot("bronze"), ingot("bronze"), TGItems.BRONZE_PLATE.get(), 2);
        metalPress(output, "iron_plate", Ingredient.of(Tags.Items.INGOTS_IRON), Ingredient.of(Tags.Items.INGOTS_IRON), TGItems.IRON_PLATE.get(), 2);
        metalPress(output, "steel_plate", ingot("steel"), ingot("steel"), TGItems.STEEL_PLATE.get(), 2);
        metalPress(output, "lead_plate", ingot("lead"), ingot("lead"), TGItems.LEAD_PLATE.get(), 2);
        metalPress(output, "mechanical_parts_iron", plate("iron"), Ingredient.of(Items.FLINT), TGItems.MECHANICAL_PARTS_IRON.get(), 1);
        metalPress(output, "mechanical_parts_obsidian_steel", plate("obsidian_steel"), Ingredient.of(Tags.Items.GEMS_QUARTZ),
                TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get(), 1);
        metalPress(output, "mechanical_parts_carbon", plate("carbon"), Ingredient.of(Items.BLAZE_ROD), TGItems.MECHANICAL_PARTS_CARBON.get(), 2);
        metalPress(output, "copper_wire", plate("copper"), plate("copper"), TGItems.COPPER_WIRE.get(), 8);
        metalPress(output, "carbon_plate", Ingredient.of(TGItems.CARBON_FIBERS.get()), Ingredient.of(TGItems.CARBON_FIBERS.get()), TGItems.CARBON_PLATE.get(), 2);
        metalPress(output, "advanced_rounds", plate("obsidian_steel"), Ingredient.of(TGItems.TGX.get()), TGAmmo.ADVANCED_ROUNDS.get(), 16);
        metalPress(output, "obsidian_steel_plate", ingot("obsidian_steel"), ingot("obsidian_steel"), TGItems.OBSIDIAN_STEEL_PLATE.get(), 2);
        metalPress(output, "steam_armor_plate", plate("steel"), plate("bronze"), TGItems.STEAM_ARMOR_PLATE.get(), 1);
        metalPress(output, "gold_wire", Ingredient.of(Tags.Items.INGOTS_GOLD), Ingredient.of(Tags.Items.INGOTS_GOLD), TGItems.GOLD_WIRE.get(), 2);
        metalPress(output, "grenade_40mm", plate("iron"), Ingredient.of(Items.TNT), TGAmmo.GRENADE_40MM.get(), 16);
        metalPress(output, "titanium_plate", ingot("titanium"), ingot("titanium"), TGItems.TITANIUM_PLATE.get(), 2);
        metalPress(output, "gauss_rifle_slugs", plate("obsidian_steel"), plate("titanium"), TGAmmo.GAUSS_RIFLE_SLUGS.get(), 4);
        metalPress(output, "sniper_rounds_explosive", Ingredient.of(TGAmmo.SNIPER_ROUNDS_INCENDIARY.get()), Ingredient.of(TGItems.TGX.get()),
                TGAmmo.SNIPER_ROUNDS_EXPLOSIVE.get(), 1);
    }

    private static Ingredient ingot(String material) {
        return Ingredient.of(TGTags.Items.ingot(material));
    }

    private static Ingredient plate(String material) {
        return Ingredient.of(TGTags.Items.plate(material));
    }

    private static void metalPress(RecipeOutput output, String name, Ingredient first, Ingredient second, ItemLike result, int count) {
        output.accept(TGIds.id("metal_press/" + name), new MetalPressRecipe(first, second, true, new ItemStack(result, count)), null);
    }

    /** Techguns 2 decorative blocks; other variants of a group come from the Camo Bench. */
    private void buildBlockRecipes(RecipeOutput output) {
        Ingredient anyConcrete = Ingredient.of(Items.WHITE_CONCRETE, Items.ORANGE_CONCRETE, Items.MAGENTA_CONCRETE, Items.LIGHT_BLUE_CONCRETE,
                Items.YELLOW_CONCRETE, Items.LIME_CONCRETE, Items.PINK_CONCRETE, Items.GRAY_CONCRETE, Items.LIGHT_GRAY_CONCRETE, Items.CYAN_CONCRETE,
                Items.PURPLE_CONCRETE, Items.BLUE_CONCRETE, Items.BROWN_CONCRETE, Items.GREEN_CONCRETE, Items.RED_CONCRETE, Items.BLACK_CONCRETE);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TGBlocks.CONCRETE_BROWN.get(), 16)
                .pattern("ccc").pattern("cbc").pattern("ccc")
                .define('c', anyConcrete).define('b', Items.IRON_BARS)
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS)).save(output, TGIds.id("concrete_brown"));

        // neon lights: 16 double neon lights
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TGBlocks.NEONTUBES2.get(), 16)
                .pattern("nnn").pattern("gsg").pattern("nnn")
                .define('n', Tags.Items.NUGGETS_IRON).define('g', Tags.Items.GLASS_PANES).define('s', Tags.Items.DUSTS_GLOWSTONE)
                .unlockedBy("has_glowstone_dust", has(Tags.Items.DUSTS_GLOWSTONE)).save(output, TGIds.id("neontubes2"));
        // nether metal: 16 plates
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TGBlocks.NETHERMETAL_PANEL.get(), 16)
                .pattern("nsn").pattern("sis").pattern("nsn")
                .define('n', Items.NETHERRACK).define('s', Items.STONE).define('i', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_netherrack", has(Items.NETHERRACK)).save(output, TGIds.id("nethermetal_panel"));
        // metal panel: 32 red containers from iron or tin plates
        for (String plate : List.of("iron", "tin")) {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TGBlocks.CONTAINER_RED.get(), 32)
                    .pattern("sss").pattern("psp").pattern("sss")
                    .define('s', Items.STONE).define('p', TGTags.Items.plate(plate))
                    .unlockedBy("has_" + plate + "_plate", has(TGTags.Items.plate(plate))).save(output, TGIds.id("container_red_from_" + plate + "_plate"));
        }
        buildShapedBlockRecipes(output);
        // stairs: 6 from a block, and back
        stairs(output, TGBlocks.PANEL_LARGE_BORDER_STAIRS.get(), TGBlocks.PANEL_LARGE_BORDER.get());
        stairs(output, TGBlocks.STEELFRAME_DARK_STAIRS.get(), TGBlocks.STEELFRAME_DARK.get());
        stairs(output, TGBlocks.CONCRETE_BROWN_LIGHT_STAIRS.get(), TGBlocks.CONCRETE_BROWN_LIGHT.get());
        stairs(output, TGBlocks.CONCRETE_GREY_DARK_STAIRS.get(), TGBlocks.CONCRETE_GREY_DARK.get());
    }

    /**
     * Phase 5a2 (Techguns 2 Recipewriter). Not crafted yet: the desert/snow camo nets and the shiny/rusty/carbon ladders
     * (Camo Bench), crates and the slimy ladder (no recipe in 1.12).
     */
    private void buildShapedBlockRecipes(RecipeOutput output) {
        // lamps: 16 from iron nuggets, glass panes and redstone; yellow and white convert into each other
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.LAMP_YELLOW.get(), 16)
                .pattern("iii").pattern("grg").pattern("ggg")
                .define('i', Tags.Items.NUGGETS_IRON).define('r', Tags.Items.DUSTS_REDSTONE).define('g', Tags.Items.GLASS_PANES)
                .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE)).save(output, TGIds.id("lamp_yellow"));
        swap(output, TGBlocks.LAMP_YELLOW.get(), TGBlocks.LAMP_WHITE.get());
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.LAMP_YELLOW_LANTERN.get(), 16)
                .pattern("iii").pattern("grg").pattern("iii")
                .define('i', Tags.Items.NUGGETS_IRON).define('r', Tags.Items.DUSTS_REDSTONE).define('g', Tags.Items.GLASS_PANES)
                .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE)).save(output, TGIds.id("lamp_yellow_lantern"));
        swap(output, TGBlocks.LAMP_YELLOW_LANTERN.get(), TGBlocks.LAMP_WHITE_LANTERN.get());

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, TGBlocks.SANDBAGS.get(), 8)
                .requires(TGItems.RUBBER_BAR.get()).requires(Ingredient.of(Tags.Items.SANDS), 8)
                .unlockedBy("has_rubber_bar", has(TGItems.RUBBER_BAR.get())).save(output, TGIds.id("sandbags"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.CAMO_NET_WOOD.get(), 8)
                .pattern("sds").pattern("sds")
                .define('s', Tags.Items.RODS_WOODEN).define('d', Items.DIRT)
                .unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN)).save(output, TGIds.id("camo_net_wood"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.CAMO_NET_TOP_WOOD.get(), 16)
                .pattern("wdw").pattern("dsd").pattern("wdw")
                .define('w', Tags.Items.RODS_WOODEN).define('d', Items.DIRT).define('s', Tags.Items.STRINGS)
                .unlockedBy("has_string", has(Tags.Items.STRINGS)).save(output, TGIds.id("camo_net_top_wood"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TGBlocks.BUNKER_DOOR.get(), 2)
                .pattern("pp").pattern("pp").pattern("pp")
                .define('p', TGTags.Items.plate("iron"))
                .unlockedBy("has_iron_plate", has(TGTags.Items.plate("iron"))).save(output, TGIds.id("bunker_door"));

        for (String plate : List.of("iron", "tin")) {
            TagKey<Item> plates = TGTags.Items.plate(plate);
            String suffix = "_from_" + plate + "_plate";
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TGBlocks.DOOR_METAL_3X3.get(), 2)
                    .pattern("ppp").pattern("sps").pattern("ppp").define('s', Items.PISTON).define('p', plates)
                    .unlockedBy("has_" + plate + "_plate", has(plates)).save(output, TGIds.id("door_metal_3x3" + suffix));
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TGBlocks.DOOR_HANGAR_UP_3X3.get(), 2)
                    .pattern("sps").pattern("ppp").pattern("ppp").define('s', Items.PISTON).define('p', plates)
                    .unlockedBy("has_" + plate + "_plate", has(plates)).save(output, TGIds.id("door_hangar_up_3x3" + suffix));
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TGBlocks.DOOR_HANGAR_DOWN_3X3.get(), 2)
                    .pattern("ppp").pattern("ppp").pattern("sps").define('s', Items.PISTON).define('p', plates)
                    .unlockedBy("has_" + plate + "_plate", has(plates)).save(output, TGIds.id("door_hangar_down_3x3" + suffix));
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.LADDER_METAL.get(), 16)
                    .pattern("iii").pattern(" i ").pattern("iii").define('i', plates)
                    .unlockedBy("has_" + plate + "_plate", has(plates)).save(output, TGIds.id("ladder_metal" + suffix));
        }
        swap(output, TGBlocks.DOOR_HANGAR_UP_3X3.get(), TGBlocks.DOOR_HANGAR_DOWN_3X3.get());
    }

    /** Two blocks that convert into each other with a shapeless recipe (Techguns 2 lamp/door variants). */
    private static void swap(RecipeOutput output, ItemLike first, ItemLike second) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, second).requires(first)
                .unlockedBy(getHasName(first), has(first)).save(output, TGIds.id(getItemName(second) + "_from_" + getItemName(first)));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, first).requires(second)
                .unlockedBy(getHasName(second), has(second)).save(output, TGIds.id(getItemName(first) + "_from_" + getItemName(second)));
    }

    /** Techguns 2 stairs recipes: 6 stairs from a block, and one stair back into a block. */
    private static void stairs(RecipeOutput output, ItemLike stairs, ItemLike block) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 6)
                .pattern("b  ").pattern("bb ").pattern("bbb")
                .define('b', block).unlockedBy(getHasName(block), has(block)).save(output, TGIds.id(getItemName(stairs)));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block)
                .requires(stairs).unlockedBy(getHasName(stairs), has(stairs)).save(output, TGIds.id(getItemName(block) + "_from_stairs"));
    }

    /** Techguns 2 shields: a vanilla shield framed by the material, with glass panes. */
    private void buildShieldRecipes(RecipeOutput output) {
        shield(output, TGArmors.RIOT_SHIELD.get(), TGTags.Items.ingot("steel"), "igi", "isi", "igi");
        shield(output, TGArmors.BALLISTIC_SHIELD.get(), TGTags.Items.plate("obsidian_steel"), "igi", "isi", "iii");
        shield(output, TGArmors.ADVANCED_SHIELD.get(), TGTags.Items.plate("carbon"), "igi", "isi", "iii");
        // not in Techguns 2: a ballistic shield plated with steel
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGArmors.HEAVY_SHIELD.get())
                .pattern("pgp").pattern("psp").pattern("ppp")
                .define('p', TGTags.Items.plate("steel")).define('g', Tags.Items.GLASS_PANES).define('s', TGArmors.BALLISTIC_SHIELD.get())
                .unlockedBy("has_ballistic_shield", has(TGArmors.BALLISTIC_SHIELD.get())).save(output, TGIds.id("heavy_shield"));
    }

    private static void shield(RecipeOutput output, Item shield, TagKey<Item> material, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, shield);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define('i', material).define('g', Tags.Items.GLASS_PANES).define('s', Items.SHIELD)
                .unlockedBy("has_shield", has(Items.SHIELD)).save(output, TGIds.id(getItemName(shield)));
    }

    private static void armor(RecipeOutput output, Item armor, ItemLike unlock, Consumer<ShapedRecipeBuilder> keys, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, armor);
        for (String row : pattern) {
            builder.pattern(row);
        }
        keys.accept(builder);
        builder.unlockedBy(getHasName(unlock), has(unlock)).save(output, TGIds.id(getItemName(armor)));
    }

    /** 9 nuggets and 1 ingot, both ways. */
    private void buildNuggetRecipes(RecipeOutput output) {
        nuggetsAndIngot(output, Items.COPPER_INGOT, TGItems.COPPER_NUGGET.get(), TGTags.Items.nugget("copper"), "copper");
        nuggetsAndIngot(output, TGItems.LEAD_INGOT.get(), TGItems.LEAD_NUGGET.get(), TGTags.Items.nugget("lead"), "lead");
        nuggetsAndIngot(output, TGItems.STEEL_INGOT.get(), TGItems.STEEL_NUGGET.get(), TGTags.Items.nugget("steel"), "steel");
    }

    private void buildMaterialRecipes(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TGItems.RAW_STEEL_INGOT.get(), 2)
                .requires(Ingredient.of(Tags.Items.INGOTS_IRON), 2).requires(Items.COAL)
                .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON)).save(output, TGIds.id("raw_steel_ingot"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TGItems.RAW_STEEL_INGOT.get(), 2)
                .requires(Ingredient.of(Tags.Items.INGOTS_IRON), 2).requires(Items.CHARCOAL)
                .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON)).save(output, TGIds.id("raw_steel_ingot_from_charcoal"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TGItems.RAW_OBSIDIAN_STEEL_INGOT.get())
                .requires(TGTags.Items.ingot("steel")).requires(Items.OBSIDIAN)
                .unlockedBy("has_steel_ingot", has(TGTags.Items.ingot("steel"))).save(output, TGIds.id("raw_obsidian_steel_ingot"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TGItems.RAW_RUBBER.get())
                .requires(ItemTags.LOGS).requires(Items.BOWL)
                .unlockedBy("has_bowl", has(Items.BOWL)).save(output, TGIds.id("raw_rubber"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TGItems.CARBON_FIBERS.get())
                .requires(Items.DIAMOND).requires(Items.BLAZE_POWDER)
                .unlockedBy("has_diamond", has(Items.DIAMOND)).save(output, TGIds.id("carbon_fibers"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TGItems.TREATED_LEATHER.get())
                .requires(Items.LEATHER, 2).requires(Tags.Items.SLIME_BALLS).requires(Items.GUNPOWDER)
                .unlockedBy("has_leather", has(Items.LEATHER)).save(output, TGIds.id("treated_leather"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.BIOMASS.get(), 4)
                .pattern(" g ").pattern("gbg").pattern(" g ")
                .define('g', Tags.Items.DYES_GREEN).define('b', Tags.Items.SLIME_BALLS)
                .unlockedBy("has_slime_ball", has(Tags.Items.SLIME_BALLS)).save(output, TGIds.id("biomass"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.COPPER_WIRE.get())
                .pattern(" cc").pattern(" c ").pattern("cc ")
                .define('c', TGTags.Items.nugget("copper"))
                .unlockedBy("has_copper_nugget", has(TGTags.Items.nugget("copper"))).save(output, TGIds.id("copper_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.GOLD_WIRE.get())
                .pattern(" cc").pattern(" c ").pattern("cc ")
                .define('c', Tags.Items.NUGGETS_GOLD)
                .unlockedBy("has_gold_nugget", has(Tags.Items.NUGGETS_GOLD)).save(output, TGIds.id("gold_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.HEAVY_CLOTH.get(), 3)
                .pattern(" w ").pattern("wlw").pattern(" w ")
                .define('w', ItemTags.WOOL).define('l', Items.LEATHER)
                .unlockedBy("has_leather", has(Items.LEATHER)).save(output, TGIds.id("heavy_cloth"));
        // TGX has two variants (lapis and gunpowder swapped), as in the original
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.TGX.get(), 4)
                .pattern("glg").pattern("lbl").pattern("glg")
                .define('b', Items.BLAZE_POWDER).define('g', Items.GUNPOWDER).define('l', Items.LAPIS_LAZULI)
                .unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER)).save(output, TGIds.id("tgx"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.TGX.get(), 4)
                .pattern("glg").pattern("lbl").pattern("glg")
                .define('b', Items.BLAZE_POWDER).define('g', Items.LAPIS_LAZULI).define('l', Items.GUNPOWDER)
                .unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER)).save(output, TGIds.id("tgx_alt"));
    }

    private void buildComponentRecipes(RecipeOutput output) {
        TagKey<Item> carbonPlate = TGTags.Items.plate("carbon");
        TagKey<Item> obsidianSteel = TGTags.Items.ingot("obsidian_steel");
        TagKey<Item> iron = Tags.Items.INGOTS_IRON;

        // Mechanical parts
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.MECHANICAL_PARTS_IRON.get(), 2)
                .pattern(" i ").pattern("ifi").pattern(" i ")
                .define('f', Items.FLINT).define('i', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT)).save(output, TGIds.id("mechanical_parts_iron"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get(), 2)
                .pattern(" i ").pattern("ifi").pattern(" i ")
                .define('f', Items.QUARTZ).define('i', obsidianSteel)
                .unlockedBy("has_obsidian_steel_ingot", has(obsidianSteel)).save(output, TGIds.id("mechanical_parts_obsidian_steel"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.MECHANICAL_PARTS_CARBON.get(), 2)
                .pattern(" i ").pattern("ifi").pattern(" i ")
                .define('f', Items.BLAZE_ROD).define('i', carbonPlate)
                .unlockedBy("has_carbon_plate", has(carbonPlate)).save(output, TGIds.id("mechanical_parts_carbon"));

        // Receivers
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.IRON_RECEIVER.get())
                .pattern("iii").pattern(" mn").pattern("  n")
                .define('i', iron).define('m', TGItems.MECHANICAL_PARTS_IRON.get()).define('n', Tags.Items.NUGGETS_IRON)
                .unlockedBy("has_mechanical_parts_iron", has(TGItems.MECHANICAL_PARTS_IRON.get())).save(output, TGIds.id("iron_receiver"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.STEEL_RECEIVER.get())
                .pattern("iii").pattern(" mn").pattern("  n")
                .define('i', TGTags.Items.ingot("steel")).define('m', TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get())
                .define('n', TGTags.Items.nugget("steel"))
                .unlockedBy("has_mechanical_parts_obsidian_steel", has(TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get()))
                .save(output, TGIds.id("steel_receiver"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.OBSIDIAN_STEEL_RECEIVER.get())
                .pattern("iii").pattern(" mi").pattern("  i")
                .define('i', obsidianSteel).define('m', TGItems.MECHANICAL_PARTS_CARBON.get())
                .unlockedBy("has_mechanical_parts_carbon", has(TGItems.MECHANICAL_PARTS_CARBON.get()))
                .save(output, TGIds.id("obsidian_steel_receiver"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.CARBON_RECEIVER.get())
                .pattern("ppp").pattern(" mi").pattern("  i")
                .define('p', carbonPlate).define('i', obsidianSteel).define('m', TGItems.MECHANICAL_PARTS_CARBON.get())
                .unlockedBy("has_mechanical_parts_carbon", has(TGItems.MECHANICAL_PARTS_CARBON.get()))
                .save(output, TGIds.id("carbon_receiver"));

        // Barrels
        barrel(output, TGItems.STONE_BARREL.get(), Ingredient.of(Items.STONE), has(Items.STONE));
        barrel(output, TGItems.IRON_BARREL.get(), Ingredient.of(Items.IRON_INGOT), has(Items.IRON_INGOT));
        barrel(output, TGItems.OBSIDIAN_STEEL_BARREL.get(), Ingredient.of(TGItems.OBSIDIAN_STEEL_INGOT.get()),
                has(TGItems.OBSIDIAN_STEEL_INGOT.get()));
        barrel(output, TGItems.CARBON_BARREL.get(), Ingredient.of(carbonPlate), has(carbonPlate));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.LASER_BARREL.get())
                .pattern("fff").pattern("ggl").pattern("fff")
                .define('f', Items.GOLD_INGOT).define('g', Tags.Items.GLASS_BLOCKS).define('l', TGItems.LASER_FOCUS.get())
                .unlockedBy("has_laser_focus", has(TGItems.LASER_FOCUS.get())).save(output, TGIds.id("laser_barrel"));

        // Stocks
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.WOOD_STOCK.get())
                .pattern("ww").pattern(" w")
                .define('w', ItemTags.LOGS)
                .unlockedBy("has_logs", has(ItemTags.LOGS)).save(output, TGIds.id("wood_stock"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.PLASTIC_STOCK.get())
                .pattern("ppp").pattern("  p")
                .define('p', TGItems.PLASTIC_SHEET.get())
                .unlockedBy("has_plastic_sheet", has(TGItems.PLASTIC_SHEET.get())).save(output, TGIds.id("plastic_stock"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.CARBON_STOCK.get())
                .pattern("iii").pattern(" pp")
                .define('i', carbonPlate).define('p', TGItems.PLASTIC_SHEET.get())
                .unlockedBy("has_carbon_plate", has(carbonPlate)).save(output, TGIds.id("carbon_stock"));

        // Electronics
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.CIRCUIT_BOARD.get(), 4)
                .pattern("cgc").pattern("rir").pattern("cgc")
                .define('r', Items.REDSTONE).define('c', TGItems.COPPER_WIRE.get()).define('g', Tags.Items.DYES_GREEN).define('i', iron)
                .unlockedBy("has_copper_wire", has(TGItems.COPPER_WIRE.get())).save(output, TGIds.id("circuit_board"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TGItems.CIRCUIT_BOARD_ADVANCED.get())
                .requires(TGItems.CIRCUIT_BOARD.get()).requires(TGItems.GOLD_WIRE.get()).requires(Items.LAPIS_LAZULI)
                .unlockedBy("has_circuit_board", has(TGItems.CIRCUIT_BOARD.get())).save(output, TGIds.id("circuit_board_advanced"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.CIRCUIT_BOARD_ELITE.get(), 2)
                .pattern("cqc").pattern("ril").pattern("cgc")
                .define('q', Items.QUARTZ).define('r', Items.REDSTONE).define('c', TGItems.GOLD_WIRE.get())
                .define('g', Items.GHAST_TEAR).define('i', carbonPlate).define('l', Items.LAPIS_LAZULI)
                .unlockedBy("has_gold_wire", has(TGItems.GOLD_WIRE.get())).save(output, TGIds.id("circuit_board_elite"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.COIL.get())
                .pattern(" wi").pattern("wiw").pattern("iw ")
                .define('w', TGItems.COPPER_WIRE.get()).define('i', iron)
                .unlockedBy("has_copper_wire", has(TGItems.COPPER_WIRE.get())).save(output, TGIds.id("coil"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TGItems.CYBERNETIC_PARTS.get())
                .requires(Items.SOUL_SAND).requires(Items.REDSTONE).requires(TGItems.GOLD_WIRE.get()).requires(TGItems.PLASTIC_SHEET.get())
                .unlockedBy("has_gold_wire", has(TGItems.GOLD_WIRE.get())).save(output, TGIds.id("cybernetic_parts"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.ELECTRIC_ENGINE.get())
                .pattern("wrw").pattern("imi").pattern("wrw")
                .define('r', Items.REDSTONE).define('w', TGItems.COPPER_WIRE.get()).define('i', iron)
                .define('m', TGItems.MECHANICAL_PARTS_IRON.get())
                .unlockedBy("has_mechanical_parts_iron", has(TGItems.MECHANICAL_PARTS_IRON.get())).save(output, TGIds.id("electric_engine"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.LASER_FOCUS.get())
                .pattern("rrr").pattern("rdr").pattern("rrr")
                .define('r', Items.REDSTONE).define('d', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND)).save(output, TGIds.id("laser_focus"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.PUMP_MECHANISM.get())
                .pattern("nnn").pattern("gpg").pattern("nnn")
                .define('p', Items.PISTON).define('g', Tags.Items.GLASS_BLOCKS).define('n', Items.IRON_INGOT)
                .unlockedBy("has_piston", has(Items.PISTON)).save(output, TGIds.id("pump_mechanism"));

        // Armor plating
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.POWER_PLATING.get(), 2)
                .pattern("tmt").pattern("bcb").pattern("pmp")
                .define('p', carbonPlate).define('b', TGItems.CIRCUIT_BOARD.get()).define('c', TGItems.CYBERNETIC_PARTS.get())
                .define('t', TGTags.Items.ingot("titanium")).define('m', TGItems.MECHANICAL_PARTS_CARBON.get())
                .unlockedBy("has_cybernetic_parts", has(TGItems.CYBERNETIC_PARTS.get())).save(output, TGIds.id("power_plating"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.POWER_PLATING_MK2.get(), 4)
                .pattern("tmt").pattern("bcb").pattern("pmp")
                .define('p', TGItems.POWER_PLATING.get()).define('b', TGItems.CIRCUIT_BOARD_ADVANCED.get())
                .define('c', TGItems.CYBERNETIC_PARTS.get()).define('t', Tags.Items.INGOTS_NETHERITE)
                .define('m', TGItems.MECHANICAL_PARTS_CARBON.get())
                .unlockedBy("has_power_plating", has(TGItems.POWER_PLATING.get())).save(output, TGIds.id("power_plating_mk2"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.NETHER_ARMOR_PLATING.get(), 4)
                .pattern(" i ").pattern("cbc").pattern("tpt")
                .define('p', carbonPlate).define('b', TGItems.CIRCUIT_BOARD_ADVANCED.get())
                .define('c', TGItems.CYBERNETIC_PARTS.get()).define('t', Tags.Items.INGOTS_NETHERITE)
                .define('i', TGTags.Items.ingot("titanium"))
                .unlockedBy("has_circuit_board_advanced", has(TGItems.CIRCUIT_BOARD_ADVANCED.get()))
                .save(output, TGIds.id("nether_armor_plating"));

        // Emitters
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.RAD_EMITTER.get())
                .pattern(" c ").pattern("lul").pattern("lpl")
                .define('p', TGItems.MECHANICAL_PARTS_CARBON.get()).define('c', TGItems.CIRCUIT_BOARD_ADVANCED.get())
                .define('u', TGItems.ENRICHED_URANIUM.get()).define('l', TGTags.Items.ingot("lead"))
                .unlockedBy("has_enriched_uranium", has(TGItems.ENRICHED_URANIUM.get())).save(output, TGIds.id("rad_emitter"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.SONIC_EMITTER.get())
                .pattern(" t ").pattern("cpc").pattern("tbt")
                .define('p', TGItems.MECHANICAL_PARTS_CARBON.get()).define('b', TGItems.CIRCUIT_BOARD_ADVANCED.get())
                .define('c', TGItems.COIL.get()).define('t', TGTags.Items.ingot("titanium"))
                .unlockedBy("has_coil", has(TGItems.COIL.get())).save(output, TGIds.id("sonic_emitter"));
    }

    private void buildAmmoRecipes(RecipeOutput output) {
        TagKey<Item> steelIngot = TGTags.Items.ingot("steel");
        TagKey<Item> steelNugget = TGTags.Items.nugget("steel");
        Item mechParts = TGItems.MECHANICAL_PARTS_IRON.get();

        // Empty magazines
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.PISTOL_MAGAZINE_EMPTY.get(), 4)
                .pattern("ii ").pattern("imi").pattern("igi")
                .define('g', Tags.Items.INGOTS_IRON).define('i', Tags.Items.NUGGETS_IRON).define('m', mechParts)
                .unlockedBy("has_mechanical_parts_iron", has(mechParts)).save(output, TGIds.id("pistol_magazine_empty"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.SMG_MAGAZINE_EMPTY.get(), 4)
                .pattern("ii ").pattern("ii ").pattern("imi")
                .define('i', Tags.Items.NUGGETS_IRON).define('m', mechParts)
                .unlockedBy("has_mechanical_parts_iron", has(mechParts)).save(output, TGIds.id("smg_magazine_empty"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get(), 4)
                .pattern("ss ").pattern("ss ").pattern("sms")
                .define('s', steelNugget).define('m', mechParts)
                .unlockedBy("has_mechanical_parts_iron", has(mechParts)).save(output, TGIds.id("assault_rifle_magazine_empty"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.LMG_MAGAZINE_EMPTY.get(), 4)
                .pattern("ii ").pattern("imi").pattern("iii")
                .define('i', steelIngot).define('m', mechParts)
                .unlockedBy("has_mechanical_parts_iron", has(mechParts)).save(output, TGIds.id("lmg_magazine_empty"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.MINIGUN_DRUM_EMPTY.get(), 4)
                .pattern("sss").pattern("pmp").pattern("sss")
                .define('p', TGItems.PLASTIC_SHEET.get()).define('s', steelIngot)
                .define('m', TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get())
                .unlockedBy("has_mechanical_parts_obsidian_steel", has(TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get()))
                .save(output, TGIds.id("minigun_drum_empty"));

        // Refill: empty magazine + loose rounds
        refill(output, TGAmmo.PISTOL_MAGAZINE.get(), TGAmmo.PISTOL_MAGAZINE_EMPTY.get(), TGAmmo.PISTOL_ROUNDS.get(), 3, "pistol_magazine");
        refill(output, TGAmmo.PISTOL_MAGAZINE_INCENDIARY.get(), TGAmmo.PISTOL_MAGAZINE_EMPTY.get(),
                TGAmmo.PISTOL_ROUNDS_INCENDIARY.get(), 3, "pistol_magazine_incendiary");
        refill(output, TGAmmo.SMG_MAGAZINE.get(), TGAmmo.SMG_MAGAZINE_EMPTY.get(), TGAmmo.PISTOL_ROUNDS.get(), 2, "smg_magazine");
        refill(output, TGAmmo.SMG_MAGAZINE_INCENDIARY.get(), TGAmmo.SMG_MAGAZINE_EMPTY.get(),
                TGAmmo.PISTOL_ROUNDS_INCENDIARY.get(), 2, "smg_magazine_incendiary");
        refill(output, TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(), TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get(), TGAmmo.RIFLE_ROUNDS.get(), 3,
                "assault_rifle_magazine");
        refill(output, TGAmmo.ASSAULT_RIFLE_MAGAZINE_INCENDIARY.get(), TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get(),
                TGAmmo.RIFLE_ROUNDS_INCENDIARY.get(), 3, "assault_rifle_magazine_incendiary");
        // LMG: 8 rounds around the empty magazine (shaped), or 8 incendiary rounds shapeless
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.LMG_MAGAZINE.get())
                .pattern("bbb").pattern("bmb").pattern("bbb")
                .define('b', TGAmmo.RIFLE_ROUNDS.get()).define('m', TGAmmo.LMG_MAGAZINE_EMPTY.get())
                .unlockedBy("has_lmg_magazine_empty", has(TGAmmo.LMG_MAGAZINE_EMPTY.get())).save(output, TGIds.id("lmg_magazine"));
        refill(output, TGAmmo.LMG_MAGAZINE_INCENDIARY.get(), TGAmmo.LMG_MAGAZINE_EMPTY.get(), TGAmmo.RIFLE_ROUNDS_INCENDIARY.get(), 8,
                "lmg_magazine_incendiary");

        TagKey<Item> obsidianSteel = TGTags.Items.ingot("obsidian_steel");
        TagKey<Item> titanium = TGTags.Items.ingot("titanium");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.AS50_MAGAZINE_EMPTY.get(), 4)
                .pattern("s s").pattern("s s").pattern("sms")
                .define('s', steelIngot).define('m', mechParts)
                .unlockedBy("has_mechanical_parts_iron", has(mechParts)).save(output, TGIds.id("as50_magazine_empty"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.ADVANCED_MAGAZINE_EMPTY.get(), 4)
                .pattern("i i").pattern("i i").pattern("imi")
                .define('i', obsidianSteel).define('m', TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get())
                .unlockedBy("has_mechanical_parts_obsidian_steel", has(TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get()))
                .save(output, TGIds.id("advanced_magazine_empty"));
        refill(output, TGAmmo.AS50_MAGAZINE.get(), TGAmmo.AS50_MAGAZINE_EMPTY.get(), TGAmmo.SNIPER_ROUNDS.get(), 2, "as50_magazine");
        refill(output, TGAmmo.AS50_MAGAZINE_INCENDIARY.get(), TGAmmo.AS50_MAGAZINE_EMPTY.get(), TGAmmo.SNIPER_ROUNDS_INCENDIARY.get(), 2,
                "as50_magazine_incendiary");
        refill(output, TGAmmo.AS50_MAGAZINE_EXPLOSIVE.get(), TGAmmo.AS50_MAGAZINE_EMPTY.get(), TGAmmo.SNIPER_ROUNDS_EXPLOSIVE.get(), 2,
                "as50_magazine_explosive");
        refill(output, TGAmmo.ADVANCED_MAGAZINE.get(), TGAmmo.ADVANCED_MAGAZINE_EMPTY.get(), TGAmmo.ADVANCED_ROUNDS.get(), 3,
                "advanced_magazine");
        // shotgun and sniper rounds come from the ammo bench (Phase 5); explosive sniper rounds have no Fabric recipe
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.ADVANCED_ROUNDS.get(), 16)
                .pattern("i").pattern("t")
                .define('i', obsidianSteel).define('t', TGItems.TGX.get())
                .unlockedBy("has_tgx", has(TGItems.TGX.get())).save(output, TGIds.id("advanced_rounds"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.GAUSS_RIFLE_SLUGS.get(), 4)
                .pattern("i").pattern("t")
                .define('i', titanium).define('t', obsidianSteel)
                .unlockedBy("has_titanium_ingot", has(titanium)).save(output, TGIds.id("gauss_rifle_slugs"));

        // Batch 3c power sources
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.REDSTONE_BATTERY.get(), 2)
                .pattern("nwn").pattern("nrn").pattern("nrn")
                .define('r', Items.REDSTONE).define('w', TGItems.COPPER_WIRE.get()).define('n', TGTags.Items.nugget("copper"))
                .unlockedBy("has_copper_wire", has(TGItems.COPPER_WIRE.get())).save(output, TGIds.id("redstone_battery"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.REDSTONE_BATTERY.get())
                .requires(TGAmmo.REDSTONE_BATTERY_EMPTY.get()).requires(Items.REDSTONE, 2)
                .unlockedBy("has_redstone_battery_empty", has(TGAmmo.REDSTONE_BATTERY_EMPTY.get()))
                .save(output, TGIds.id("redstone_battery_refill"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.NUCLEAR_POWER_CELL_EMPTY.get(), 2)
                .pattern("wgw").pattern("prp").pattern("prp")
                .define('p', TGTags.Items.ingot("lead")).define('r', Items.REDSTONE_BLOCK).define('w', TGItems.GOLD_WIRE.get())
                .define('g', TGItems.CIRCUIT_BOARD.get())
                .unlockedBy("has_circuit_board", has(TGItems.CIRCUIT_BOARD.get())).save(output, TGIds.id("nuclear_power_cell_empty"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.NUCLEAR_POWER_CELL.get())
                .requires(TGAmmo.NUCLEAR_POWER_CELL_EMPTY.get()).requires(TGItems.ENRICHED_URANIUM.get())
                .unlockedBy("has_nuclear_power_cell_empty", has(TGAmmo.NUCLEAR_POWER_CELL_EMPTY.get()))
                .save(output, TGIds.id("nuclear_power_cell"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.NETHER_CHARGE.get(), 4)
                .requires(Items.NETHERRACK).requires(Items.SOUL_SAND).requires(Items.LAVA_BUCKET)
                .unlockedBy("has_soul_sand", has(Items.SOUL_SAND)).save(output, TGIds.id("nether_charge"));

        // Energy cells
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.ENERGY_CELL.get(), 4)
                .pattern("gwg").pattern("prp").pattern("prp")
                .define('p', TGItems.PLASTIC_SHEET.get()).define('r', Items.REDSTONE_BLOCK)
                .define('w', TGItems.COPPER_WIRE.get()).define('g', Tags.Items.INGOTS_GOLD)
                .unlockedBy("has_copper_wire", has(TGItems.COPPER_WIRE.get())).save(output, TGIds.id("energy_cell"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.ENERGY_CELL.get())
                .requires(TGAmmo.ENERGY_CELL_EMPTY.get()).requires(Items.REDSTONE, 4)
                .unlockedBy("has_energy_cell_empty", has(TGAmmo.ENERGY_CELL_EMPTY.get())).save(output, TGIds.id("energy_cell_refill"));

        // Rounds and rockets
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.PISTOL_ROUNDS_INCENDIARY.get(), 4)
                .requires(TGAmmo.PISTOL_ROUNDS.get(), 4).requires(Items.BLAZE_POWDER)
                .unlockedBy("has_pistol_rounds", has(TGAmmo.PISTOL_ROUNDS.get())).save(output, TGIds.id("pistol_rounds_incendiary"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.RIFLE_ROUNDS_INCENDIARY.get(), 4)
                .requires(TGAmmo.RIFLE_ROUNDS.get(), 4).requires(Items.BLAZE_POWDER)
                .unlockedBy("has_rifle_rounds", has(TGAmmo.RIFLE_ROUNDS.get())).save(output, TGIds.id("rifle_rounds_incendiary"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.SHOTGUN_ROUNDS_INCENDIARY.get(), 8)
                .requires(TGAmmo.SHOTGUN_ROUNDS.get(), 8).requires(Items.BLAZE_POWDER)
                .unlockedBy("has_shotgun_rounds", has(TGAmmo.SHOTGUN_ROUNDS.get())).save(output, TGIds.id("shotgun_rounds_incendiary"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.SNIPER_ROUNDS_INCENDIARY.get(), 2)
                .requires(TGAmmo.SNIPER_ROUNDS.get(), 2).requires(Items.BLAZE_POWDER)
                .unlockedBy("has_sniper_rounds", has(TGAmmo.SNIPER_ROUNDS.get())).save(output, TGIds.id("sniper_rounds_incendiary"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.ROCKET.get(), 4)
                .pattern(" s ").pattern("sts").pattern("sgs")
                .define('s', Tags.Items.NUGGETS_IRON).define('t', Items.TNT).define('g', Items.GUNPOWDER)
                .unlockedBy("has_tnt", has(Items.TNT)).save(output, TGIds.id("rocket"));
        // nuke rockets need the tactical nuke warhead, high velocity rockets the chemical laboratory (Phase 5)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.GRENADE_40MM.get(), 12)
                .pattern(" i ").pattern("sts").pattern("sgs")
                .define('s', Tags.Items.NUGGETS_IRON).define('t', Items.TNT).define('g', Items.GUNPOWDER).define('i', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_tnt", has(Items.TNT)).save(output, TGIds.id("grenade_40mm"));
        // Batch 3e ammo (the magic rifle's bullets have no recipe in Fabric)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.FUEL_TANK_EMPTY.get(), 4)
                .pattern("p").pattern("g").pattern("p")
                .define('p', TGItems.PLASTIC_SHEET.get()).define('g', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_plastic_sheet", has(TGItems.PLASTIC_SHEET.get())).save(output, TGIds.id("fuel_tank_empty"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.FUEL_TANK.get(), 2)
                .requires(TGAmmo.FUEL_TANK_EMPTY.get(), 2).requires(Items.LAVA_BUCKET)
                .unlockedBy("has_fuel_tank_empty", has(TGAmmo.FUEL_TANK_EMPTY.get())).save(output, TGIds.id("fuel_tank"));
        // Batch 3g: rifle round stacks refill the minigun drum
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.RIFLE_ROUNDS_STACK.get())
                .requires(TGAmmo.RIFLE_ROUNDS.get(), 4)
                .unlockedBy("has_rifle_rounds", has(TGAmmo.RIFLE_ROUNDS.get())).save(output, TGIds.id("rifle_rounds_stack"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.RIFLE_ROUNDS_STACK_INCENDIARY.get())
                .requires(TGAmmo.RIFLE_ROUNDS_INCENDIARY.get(), 4)
                .unlockedBy("has_rifle_rounds_incendiary", has(TGAmmo.RIFLE_ROUNDS_INCENDIARY.get()))
                .save(output, TGIds.id("rifle_rounds_stack_incendiary"));
        refill(output, TGAmmo.MINIGUN_DRUM.get(), TGAmmo.MINIGUN_DRUM_EMPTY.get(), TGAmmo.RIFLE_ROUNDS_STACK.get(), 4, "minigun_drum");
        refill(output, TGAmmo.MINIGUN_DRUM_INCENDIARY.get(), TGAmmo.MINIGUN_DRUM_EMPTY.get(), TGAmmo.RIFLE_ROUNDS_STACK_INCENDIARY.get(), 4,
                "minigun_drum_incendiary");

        // Batch 3f: compressed air tanks and tool mining heads
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.COMPRESSED_AIR_TANK_EMPTY.get(), 4)
                .pattern(" l ").pattern("i i").pattern("i i")
                .define('l', Items.LEVER).define('i', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON)).save(output, TGIds.id("compressed_air_tank_empty"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.COMPRESSED_AIR_TANK.get())
                .requires(TGAmmo.COMPRESSED_AIR_TANK_EMPTY.get()).requires(Items.WATER_BUCKET).requires(Items.COAL)
                .unlockedBy("has_compressed_air_tank_empty", has(TGAmmo.COMPRESSED_AIR_TANK_EMPTY.get()))
                .save(output, TGIds.id("compressed_air_tank"));
        TagKey<Item> obsidianSteelIngot = TGTags.Items.ingot("obsidian_steel");
        TagKey<Item> carbonPlate = TGTags.Items.plate("carbon");
        miningHead(output, TGItems.CHAINSAW_BLADES_STEEL.get(), steelIngot, TGItems.MECHANICAL_PARTS_IRON.get(), " i", "ip", " i");
        miningHead(output, TGItems.CHAINSAW_BLADES_OBSIDIAN.get(), obsidianSteelIngot, TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get(),
                " i", "ip", " i");
        miningHead(output, TGItems.CHAINSAW_BLADES_CARBON.get(), carbonPlate, TGItems.MECHANICAL_PARTS_CARBON.get(), " i", "ip", " i");
        miningHead(output, TGItems.MINING_DRILL_HEAD_OBSIDIAN.get(), obsidianSteelIngot, null, " ii", "iii", " ii");
        miningHead(output, TGItems.MINING_DRILL_HEAD_CARBON.get(), carbonPlate, null, " ii", "iii", " ii");
        miningHead(output, TGItems.POWER_HAMMER_HEAD_STEEL.get(), steelIngot, null, "i  ", "iii", "i  ");
        miningHead(output, TGItems.POWER_HAMMER_HEAD_OBSIDIAN.get(), obsidianSteelIngot, null, "i  ", "iii", "i  ");
        miningHead(output, TGItems.POWER_HAMMER_HEAD_CARBON.get(), carbonPlate, null, "i  ", "iii", "i  ");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.BIO_TANK_EMPTY.get())
                .pattern("sss").pattern("sgs").pattern("sgs")
                .define('s', steelNugget).define('g', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_steel_nugget", has(steelNugget)).save(output, TGIds.id("bio_tank_empty"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.BIO_TANK.get())
                .requires(TGAmmo.BIO_TANK_EMPTY.get()).requires(TGItems.BIOMASS.get())
                .unlockedBy("has_bio_tank_empty", has(TGAmmo.BIO_TANK_EMPTY.get())).save(output, TGIds.id("bio_tank"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.STONE_BULLETS.get(), 16)
                .requires(Items.COBBLESTONE, 3).requires(Items.GUNPOWDER)
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER)).save(output, TGIds.id("stone_bullets"));

        // hand grenades (Techguns 2)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGGuns.STIELGRANATE.get(), 16)
                .pattern(" it").pattern(" wi").pattern("i  ")
                .define('i', Tags.Items.INGOTS_IRON).define('w', ItemTags.PLANKS).define('t', Items.TNT)
                .unlockedBy("has_tnt", has(Items.TNT)).save(output, TGIds.id("stielgranate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGGuns.FRAG_GRENADE.get(), 16)
                .pattern(" if").pattern("iti").pattern(" i ")
                .define('i', steelIngot).define('f', Items.FLINT_AND_STEEL).define('t', Items.TNT)
                .unlockedBy("has_tnt", has(Items.TNT)).save(output, TGIds.id("frag_grenade"));
    }

    /** Shapeless: empty magazine plus {@code rounds} loose rounds gives the filled magazine. */
    private static void refill(RecipeOutput output, ItemLike result, ItemLike empty, ItemLike round, int rounds, String id) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, result)
                .requires(empty).requires(round, rounds)
                .unlockedBy(getHasName(empty), has(empty)).save(output, TGIds.id(id));
    }

    private void buildGunRecipes(RecipeOutput output) {
        TagKey<Item> steelIngot = TGTags.Items.ingot("steel");
        Item obsBarrel = TGItems.OBSIDIAN_STEEL_BARREL.get();
        Item steelReceiver = TGItems.STEEL_RECEIVER.get();
        Item obsReceiver = TGItems.OBSIDIAN_STEEL_RECEIVER.get();
        Item ironReceiver = TGItems.IRON_RECEIVER.get();
        Item plasticStock = TGItems.PLASTIC_STOCK.get();
        Item woodStock = TGItems.WOOD_STOCK.get();
        Item plastic = TGItems.PLASTIC_SHEET.get();
        Item glassPane = Items.GLASS_PANE;

        // M4: loaded / empty / incendiary differ only in the magazine and the result's ammo
        Item m4 = TGGuns.M4.get();
        Consumer<StackShapedRecipeBuilder> m4Keys = b -> b.define('r', Items.REDSTONE).define('b', obsBarrel).define('s', plasticStock)
                .define('f', steelReceiver).define('g', glassPane).define('i', Tags.Items.INGOTS_IRON).define('n', Tags.Items.NUGGETS_IRON);
        gun(output, m4, 30, AmmoType.DEFAULT, "m4", TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(), m4Keys, "gr", "bfs", "nmi");
        gun(output, m4, 0, AmmoType.DEFAULT, "m4_empty", TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get(), m4Keys, "gr", "bfs", "nmi");
        gun(output, m4, 30, AmmoType.INCENDIARY, "m4_incendiary", TGAmmo.ASSAULT_RIFLE_MAGAZINE_INCENDIARY.get(), m4Keys,
                "gr", "bfs", "nmi");

        // Pistol
        Item pistol = TGGuns.PISTOL.get();
        Consumer<StackShapedRecipeBuilder> pistolKeys = b -> b.define('p', plastic).define('b', obsBarrel).define('s', steelIngot)
                .define('f', TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get());
        gun(output, pistol, 18, AmmoType.DEFAULT, "pistol", TGAmmo.PISTOL_MAGAZINE.get(), pistolKeys, "bsp", "fp", "m");
        gun(output, pistol, 0, AmmoType.DEFAULT, "pistol_empty", TGAmmo.PISTOL_MAGAZINE_EMPTY.get(), pistolKeys, "bsp", "fp", "m");
        gun(output, pistol, 18, AmmoType.INCENDIARY, "pistol_incendiary", TGAmmo.PISTOL_MAGAZINE_INCENDIARY.get(), pistolKeys,
                "bsp", "fp", "m");

        // Lasergun
        Item laser = TGGuns.LASERGUN.get();
        Consumer<StackShapedRecipeBuilder> laserKeys = b -> b.define('p', TGItems.MECHANICAL_PARTS_CARBON.get())
                .define('b', TGItems.LASER_BARREL.get()).define('r', obsReceiver).define('s', plasticStock)
                .define('d', Items.REDSTONE).define('g', glassPane);
        gun(output, laser, 45, AmmoType.DEFAULT, "lasergun", TGAmmo.ENERGY_CELL.get(), laserKeys, "gd", "brs", "mp");
        gun(output, laser, 0, AmmoType.DEFAULT, "lasergun_empty", TGAmmo.ENERGY_CELL_EMPTY.get(), laserKeys, "gd", "brs", "mp");

        // Rocket launcher: the loaded one has a rocket left of the barrels
        Item rocketLauncher = TGGuns.ROCKET_LAUNCHER.get();
        Consumer<StackShapedRecipeBuilder> rocketKeys = b -> b.define('b', obsBarrel).define('f', steelReceiver);
        gun(output, rocketLauncher, 1, AmmoType.DEFAULT, "rocket_launcher", TGAmmo.ROCKET.get(), rocketKeys, "mbb", "f");
        gun(output, rocketLauncher, 0, AmmoType.DEFAULT, "rocket_launcher_empty", null, rocketKeys, "bb", "f");

        // Revolver and bolt action: empty only (Fabric has no loaded recipe)
        gun(output, TGGuns.REVOLVER.get(), 0, AmmoType.DEFAULT, "revolver_empty", null,
                b -> b.define('b', TGItems.IRON_BARREL.get()).define('s', woodStock).define('f', Items.FLINT_AND_STEEL)
                        .define('m', TGItems.MECHANICAL_PARTS_IRON.get()),
                "bmf", "s");
        gun(output, TGGuns.BOLT_ACTION.get(), 0, AmmoType.DEFAULT, "bolt_action_empty", null,
                b -> b.define('b', obsBarrel).define('s', woodStock).define('f', ironReceiver)
                        .define('g', Tags.Items.GLASS_BLOCKS).define('i', Tags.Items.INGOTS_IRON),
                "gi", "bfs");

        gun(output, TGGuns.AK47.get(), 30, AmmoType.DEFAULT, "ak47", TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(),
                b -> b.define('b', obsBarrel).define('s', woodStock).define('f', ironReceiver).define('w', ItemTags.LOGS),
                "bfs", "wm");
        gun(output, TGGuns.AUG.get(), 30, AmmoType.DEFAULT, "aug", TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(),
                b -> b.define('p', steelIngot).define('b', obsBarrel).define('s', plasticStock).define('f', steelReceiver)
                        .define('g', glassPane).define('i', plastic),
                "gpg", "bfs", "im");
        gun(output, TGGuns.SCAR.get(), 20, AmmoType.DEFAULT, "scar", TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(),
                b -> b.define('p', plastic).define('r', obsReceiver).define('b', obsBarrel).define('s', plasticStock)
                        .define('d', Items.DIAMOND).define('g', Tags.Items.GLASS_BLOCKS),
                "gpd", "brs", "m");
        gun(output, TGGuns.THOMPSON.get(), 20, AmmoType.DEFAULT, "thompson", TGAmmo.SMG_MAGAZINE.get(),
                b -> b.define('b', TGItems.IRON_BARREL.get()).define('s', woodStock).define('f', ironReceiver),
                "bfs", "m");
        gun(output, TGGuns.MAC10.get(), 32, AmmoType.DEFAULT, "mac10", TGAmmo.SMG_MAGAZINE.get(),
                b -> b.define('p', plastic).define('r', steelReceiver).define('b', obsBarrel).define('i', Tags.Items.NUGGETS_IRON),
                "pi", "bri", "m");
        gun(output, TGGuns.VECTOR.get(), 25, AmmoType.DEFAULT, "vector", TGAmmo.SMG_MAGAZINE.get(),
                b -> b.define('r', obsReceiver).define('b', obsBarrel).define('s', plasticStock).define('d', Items.REDSTONE)
                        .define('g', glassPane),
                "gd", "brs", "m");
        gun(output, TGGuns.LMG.get(), 100, AmmoType.DEFAULT, "lmg", TGAmmo.LMG_MAGAZINE.get(),
                b -> b.define('r', Items.REDSTONE).define('b', obsBarrel).define('s', plasticStock).define('f', obsReceiver)
                        .define('g', glassPane),
                "gr", "bfs", "m");
        gun(output, TGGuns.MINIGUN.get(), 200, AmmoType.DEFAULT, "minigun", TGAmmo.MINIGUN_DRUM.get(),
                b -> b.define('b', obsBarrel).define('r', obsReceiver).define('e', TGItems.ELECTRIC_ENGINE.get()),
                "bbe", "bbr", "bbm");

        // --- Batch 3b ---
        TagKey<Item> obsidianSteel = TGTags.Items.ingot("obsidian_steel");
        TagKey<Item> titanium = TGTags.Items.ingot("titanium");
        Item carbonBarrel = TGItems.CARBON_BARREL.get();
        Item carbonReceiver = TGItems.CARBON_RECEIVER.get();
        Item carbonStock = TGItems.CARBON_STOCK.get();
        Item advancedCircuit = TGItems.CIRCUIT_BOARD_ADVANCED.get();

        // Shotguns: empty only (Fabric has no loaded recipe)
        gun(output, TGGuns.SAWED_OFF.get(), 0, AmmoType.DEFAULT, "sawed_off_empty", null,
                b -> b.define('b', TGItems.IRON_BARREL.get()).define('f', Items.FLINT_AND_STEEL).define('w', woodStock),
                "bfw", "bf ");
        gun(output, TGGuns.COMBAT_SHOTGUN.get(), 0, AmmoType.DEFAULT, "combat_shotgun_empty", null,
                b -> b.define('b', obsBarrel).define('s', plasticStock).define('f', steelReceiver).define('i', steelIngot),
                "bfs", "i  ");

        Item as50 = TGGuns.AS50.get();
        Consumer<StackShapedRecipeBuilder> as50Keys = b -> b.define('p', obsidianSteel).define('b', obsBarrel).define('t', plasticStock)
                .define('d', Items.DIAMOND).define('f', obsReceiver);
        gun(output, as50, 10, AmmoType.DEFAULT, "as50", TGAmmo.AS50_MAGAZINE.get(), as50Keys, "dpd", "bbf", " mt");
        gun(output, as50, 0, AmmoType.DEFAULT, "as50_empty", TGAmmo.AS50_MAGAZINE_EMPTY.get(), as50Keys, "dpd", "bbf", " mt");
        gun(output, as50, 10, AmmoType.INCENDIARY, "as50_incendiary", TGAmmo.AS50_MAGAZINE_INCENDIARY.get(), as50Keys,
                "dpd", "bbf", " mt");
        gun(output, as50, 10, AmmoType.EXPLOSIVE, "as50_explosive", TGAmmo.AS50_MAGAZINE_EXPLOSIVE.get(), as50Keys,
                "dpd", "bbf", " mt");

        // Gauss rifle: the loaded one takes slugs ('a') and a full cell ('e'), the empty one an empty cell
        Item gauss = TGGuns.GAUSS_RIFLE.get();
        Consumer<StackShapedRecipeBuilder> gaussKeys = b -> b.define('p', titanium).define('b', TGItems.GAUSS_BARREL.get())
                .define('r', carbonReceiver).define('s', carbonStock).define('c', advancedCircuit).define('d', Items.DIAMOND);
        gun(output, gauss, 8, AmmoType.DEFAULT, "gauss_rifle",  null,
                gaussKeys.andThen(b -> b.define('a', TGAmmo.GAUSS_RIFLE_SLUGS.get()).define('e', TGAmmo.ENERGY_CELL.get())),
                "dpc", "brs", " ae");
        gun(output, gauss, 0, AmmoType.DEFAULT, "gauss_rifle_empty", null,
                gaussKeys.andThen(b -> b.define('e', TGAmmo.ENERGY_CELL_EMPTY.get())), "dpc", "brs", "  e");

        Item blaster = TGGuns.BLASTER_RIFLE.get();
        Consumer<StackShapedRecipeBuilder> blasterKeys = b -> b.define('p', TGTags.Items.plate("carbon"))
                .define('b', TGItems.LASER_BARREL.get()).define('r', carbonReceiver).define('c', advancedCircuit)
                .define('s', carbonStock).define('g', Tags.Items.GLASS_BLOCKS);
        gun(output, blaster, 50, AmmoType.DEFAULT, "blaster_rifle", TGAmmo.ENERGY_CELL.get(), blasterKeys, "pcg", "brs", " m ");
        gun(output, blaster, 0, AmmoType.DEFAULT, "blaster_rifle_empty", TGAmmo.ENERGY_CELL_EMPTY.get(), blasterKeys,
                "pcg", "brs", " m ");
        // blaster shotgun: no Fabric recipe

        Item pulse = TGGuns.PULSE_RIFLE.get();
        Consumer<StackShapedRecipeBuilder> pulseKeys = b -> b.define('p', titanium).define('b', carbonBarrel).define('r', carbonReceiver)
                .define('s', carbonStock).define('c', advancedCircuit).define('d', Items.DIAMOND);
        gun(output, pulse, 12, AmmoType.DEFAULT, "pulse_rifle", TGAmmo.ADVANCED_MAGAZINE.get(), pulseKeys, "dpc", "brs", " m ");
        gun(output, pulse, 0, AmmoType.DEFAULT, "pulse_rifle_empty", TGAmmo.ADVANCED_MAGAZINE_EMPTY.get(), pulseKeys,
                "dpc", "brs", " m ");

        Item pdw = TGGuns.PDW.get();
        Consumer<StackShapedRecipeBuilder> pdwKeys = b -> b.define('b', carbonBarrel).define('r', carbonReceiver).define('s', carbonStock);
        gun(output, pdw, 40, AmmoType.DEFAULT, "pdw", TGAmmo.ADVANCED_MAGAZINE.get(), pdwKeys, "brs", " m ");
        gun(output, pdw, 0, AmmoType.DEFAULT, "pdw_empty", TGAmmo.ADVANCED_MAGAZINE_EMPTY.get(), pdwKeys, "brs", " m ");

        // --- Batch 3e (the magic rifle has no recipe in Fabric) ---
        Item flamethrower = TGGuns.FLAMETHROWER.get();
        Consumer<StackShapedRecipeBuilder> flamethrowerKeys = b -> b.define('p', TGItems.PUMP_MECHANISM.get()).define('r', ironReceiver)
                .define('s', plasticStock).define('f', Items.FLINT_AND_STEEL);
        gun(output, flamethrower, 100, AmmoType.DEFAULT, "flamethrower", TGAmmo.FUEL_TANK.get(), flamethrowerKeys, "prs", "fm ");
        gun(output, flamethrower, 0, AmmoType.DEFAULT, "flamethrower_empty", TGAmmo.FUEL_TANK_EMPTY.get(), flamethrowerKeys, "prs", "fm ");
        Item biogun = TGGuns.BIOGUN.get();
        Consumer<StackShapedRecipeBuilder> biogunKeys = b -> b.define('r', steelReceiver).define('s', plasticStock)
                .define('f', TGItems.PUMP_MECHANISM.get());
        gun(output, biogun, 30, AmmoType.DEFAULT, "biogun", TGAmmo.BIO_TANK.get(), biogunKeys, "frs", " m ");
        gun(output, biogun, 0, AmmoType.DEFAULT, "biogun_empty", TGAmmo.BIO_TANK_EMPTY.get(), biogunKeys, "frs", " m ");
        Item tfg = TGGuns.TFG.get();
        Consumer<StackShapedRecipeBuilder> tfgKeys = b -> b.define('p', TGItems.PLASMA_GENERATOR.get())
                .define('b', TGItems.SHIELDED_TITANIUM_BARREL.get()).define('r', TGItems.TITANIUM_RECEIVER.get())
                .define('t', TGTags.Items.ingot("titanium"));
        gun(output, tfg, 20, AmmoType.DEFAULT, "tfg", TGAmmo.NUCLEAR_POWER_CELL.get(), tfgKeys, "t  ", "bpr", "tm ");
        gun(output, tfg, 0, AmmoType.DEFAULT, "tfg_empty", TGAmmo.NUCLEAR_POWER_CELL_EMPTY.get(), tfgKeys, "t  ", "bpr", "tm ");
        gun(output, TGGuns.HAND_CANNON.get(), 0, AmmoType.DEFAULT, "hand_cannon_empty", null,
                b -> b.define('b', TGItems.STONE_BARREL.get()).define('s', woodStock).define('f', Items.FLINT_AND_STEEL), "bfs");

        // --- Batch 3d ---
        gun(output, rocketLauncher, 1, AmmoType.HIGH_VELOCITY, "rocket_launcher_high_velocity", TGAmmo.ROCKET_HIGH_VELOCITY.get(), rocketKeys,
                "mbb", "f");
        gun(output, rocketLauncher, 1, AmmoType.NUKE, "rocket_launcher_nuke", TGAmmo.ROCKET_NUKE.get(), rocketKeys, "mbb", "f");
        gun(output, TGGuns.GRENADE_LAUNCHER.get(), 0, AmmoType.DEFAULT, "grenade_launcher_empty", null,
                b -> b.define('p', steelIngot).define('b', obsBarrel).define('r', steelReceiver).define('s', plasticStock),
                "brs", " p ");
        Item guided = TGGuns.GUIDED_MISSILE_LAUNCHER.get();
        Consumer<StackShapedRecipeBuilder> guidedKeys = b -> b.define('b', obsBarrel).define('c', TGItems.CIRCUIT_BOARD_ADVANCED.get())
                .define('f', obsReceiver).define('g', Tags.Items.GLASS_BLOCKS);
        gun(output, guided, 1, AmmoType.DEFAULT, "guided_missile_launcher", TGAmmo.ROCKET.get(), guidedKeys, " gc", "mbb", " f ");
        gun(output, guided, 1, AmmoType.HIGH_VELOCITY, "guided_missile_launcher_high_velocity", TGAmmo.ROCKET_HIGH_VELOCITY.get(), guidedKeys,
                " gc", "mbb", " f ");
        gun(output, guided, 0, AmmoType.DEFAULT, "guided_missile_launcher_empty", null, guidedKeys, "gc", "bb", "f ");
        // the Grim Reaper: four guided missile launchers, their rockets carry over
        transfer(output, TGGuns.GRIM_REAPER.get(), "grim_reaper",
                b -> b.define('r', guided).define('c', TGItems.CARBON_RECEIVER.get()), "rr ", "rrc");

        // --- Batch 3c ---
        Item laserPistol = TGGuns.LASER_PISTOL.get();
        Consumer<StackShapedRecipeBuilder> laserPistolKeys = b -> b.define('p', plastic).define('b', TGItems.LASER_BARREL.get())
                .define('s', TGTags.Items.nugget("steel")).define('c', advancedCircuit).define('o', obsidianSteel);
        gun(output, laserPistol, 20, AmmoType.DEFAULT, "laser_pistol", TGAmmo.REDSTONE_BATTERY.get(), laserPistolKeys, "obo", "scm", "ssp");
        gun(output, laserPistol, 0, AmmoType.DEFAULT, "laser_pistol_empty", TGAmmo.REDSTONE_BATTERY_EMPTY.get(), laserPistolKeys,
                "obo", "scm", "ssp");

        Item ndr = TGGuns.NUCLEAR_DEATHRAY.get();
        Consumer<StackShapedRecipeBuilder> ndrKeys = b -> b.define('r', carbonReceiver).define('s', carbonStock).define('t', titanium)
                .define('e', TGItems.RAD_EMITTER.get()).define('l', TGTags.Items.ingot("lead"));
        gun(output, ndr, 40, AmmoType.DEFAULT, "nuclear_deathray", TGAmmo.NUCLEAR_POWER_CELL.get(), ndrKeys, "ltl", "ers", " m ");
        gun(output, ndr, 0, AmmoType.DEFAULT, "nuclear_deathray_empty", TGAmmo.NUCLEAR_POWER_CELL_EMPTY.get(), ndrKeys,
                "ltl", "ers", " m ");

        Item tesla = TGGuns.TESLA_GUN.get();
        Consumer<StackShapedRecipeBuilder> teslaKeys = b -> b.define('r', carbonReceiver).define('c', TGItems.COIL.get()).define('s', carbonStock)
                .define('d', Items.REDSTONE).define('g', glassPane);
        gun(output, tesla, 25, AmmoType.DEFAULT, "tesla_gun", TGAmmo.ENERGY_CELL.get(), teslaKeys, " gd", "crs", " m ");
        gun(output, tesla, 0, AmmoType.DEFAULT, "tesla_gun_empty", TGAmmo.ENERGY_CELL_EMPTY.get(), teslaKeys, " gd", "crs", " m ");

        Item mib = TGGuns.MIB_GUN.get();
        Consumer<StackShapedRecipeBuilder> mibKeys = b -> b.define('p', titanium).define('b', TGItems.LASER_BARREL.get())
                .define('s', TGItems.SONIC_EMITTER.get()).define('f', TGItems.MECHANICAL_PARTS_CARBON.get());
        gun(output, mib, 20, AmmoType.DEFAULT, "mib_gun", TGAmmo.ENERGY_CELL.get(), mibKeys, "bsp", " fp", "  m");
        gun(output, mib, 0, AmmoType.DEFAULT, "mib_gun_empty", TGAmmo.ENERGY_CELL_EMPTY.get(), mibKeys, "bsp", " fp", "  m");

        // nether blaster: loaded only (the charge is used up); the alien blaster has no Fabric recipe
        gun(output, TGGuns.NETHER_BLASTER.get(), 10, AmmoType.DEFAULT, "nether_blaster", TGAmmo.NETHER_CHARGE.get(),
                b -> b.define('p', obsidianSteel).define('b', obsBarrel).define('c', TGItems.CYBERNETIC_PARTS.get())
                        .define('s', TGItems.PUMP_MECHANISM.get()),
                "cpc", "bms", "cpc");

        Item sonic = TGGuns.SONIC_SHOTGUN.get();
        Consumer<StackShapedRecipeBuilder> sonicKeys = b -> b.define('p', titanium).define('r', carbonReceiver).define('s', carbonStock)
                .define('e', TGItems.SONIC_EMITTER.get());
        gun(output, sonic, 8, AmmoType.DEFAULT, "sonic_shotgun", TGAmmo.ENERGY_CELL.get(), sonicKeys, " p ", "ers", " m ");
        gun(output, sonic, 0, AmmoType.DEFAULT, "sonic_shotgun_empty", TGAmmo.ENERGY_CELL_EMPTY.get(), sonicKeys, " p ", "ers", " m ");

        // --- Batch 3f: tools, crafted empty with any of their heads (head swaps: mining head upgrade recipe, batch 3g) ---
        Consumer<StackShapedRecipeBuilder> chainsawKeys = b -> b.define('p', plastic).define('r', ironReceiver).define('s', steelIngot);
        tool(output, TGGuns.CHAINSAW.get(), TGAmmo.FUEL_TANK_EMPTY.get(), chainsawKeys, "  p", "hsr", "  m");
        tool(output, TGGuns.MINING_DRILL.get(), TGAmmo.FUEL_TANK_EMPTY.get(), chainsawKeys, "  p", "hsr", "  m");
        tool(output, TGGuns.POWER_HAMMER.get(), TGAmmo.COMPRESSED_AIR_TANK_EMPTY.get(),
                b -> b.define('p', Items.PISTON).define('r', ironReceiver).define('i', TGItems.MECHANICAL_PARTS_IRON.get()),
                " i ", "hpr", " im");
        // shishkebap: Techguns 2 recipe (no Fabric port)
        gun(output, TGGuns.SHISHKEBAP.get(), 0, AmmoType.DEFAULT, "shishkebap_empty", TGAmmo.FUEL_TANK_EMPTY.get(),
                b -> b.define('p', TGTags.Items.plate("obsidian_steel")).define('f', TGItems.PUMP_MECHANISM.get()).define('s', plastic),
                "p ", "pf", "sm");
        // the grappling hook has no recipe in Fabric or Techguns 2

        // --- Batch 3g: guns made from guns keep their rounds; special recipes ---
        transfer(output, TGGuns.GOLDEN_REVOLVER.get(), "golden_revolver",
                b -> b.define('r', TGGuns.REVOLVER.get()).define('g', Tags.Items.INGOTS_GOLD), "ggg", "grg", "ggg");
        transfer(output, TGGuns.M4_INFILTRATOR.get(), "m4_infiltrator",
                b -> b.define('g', Tags.Items.GLASS_BLOCKS).define('p', steelIngot).define('w', ItemTags.WOOL)
                        .define('m', TGGuns.M4.get()).define('r', Items.REDSTONE).define('i', steelIngot),
                "gpg", "wwm", "rri");
        SpecialRecipeBuilder.special(AmmoChangeRecipe::new).save(output, TGIds.id("ammo_change"));
        SpecialRecipeBuilder.special(MiningHeadUpgradeRecipe::new).save(output, TGIds.id("mining_head_upgrade"));
    }

    /** A {@link techguns.recipe.TransferAmmoRecipe}: the result gun gets the rounds of the input guns. */
    private static void transfer(RecipeOutput output, Item gun, String id, Consumer<StackShapedRecipeBuilder> keys, String... pattern) {
        ItemStack result = new ItemStack(gun);
        GunData.setAmmo(result, new GunAmmo(0, AmmoType.DEFAULT));
        StackShapedRecipeBuilder builder = StackShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result, pattern).transferAmmo();
        keys.accept(builder);
        builder.unlockedBy(getHasName(gun), has(gun)).save(output, TGIds.id(id));
    }

    /** One empty tool recipe per mining head; the head ('h') sets the result's head level. */
    private static void tool(RecipeOutput output, Item item, ItemLike tank, Consumer<StackShapedRecipeBuilder> keys, String... pattern) {
        MeleeToolItem tool = (MeleeToolItem) item;
        for (int level = 0; level < tool.heads().size(); level++) {
            ItemStack result = new ItemStack(item);
            GunData.setAmmo(result, new GunAmmo(0, AmmoType.DEFAULT));
            tool.setHeadLevel(result, level);
            Item head = tool.heads().get(level).item().get();
            StackShapedRecipeBuilder builder = StackShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result, pattern);
            keys.accept(builder);
            builder.define('h', head).define('m', tank).unlockedBy(getHasName(head), has(head))
                    .save(output, TGIds.id(getItemName(item) + "_" + getItemName(head)));
        }
    }

    private static void miningHead(RecipeOutput output, ItemLike head, TagKey<Item> material, ItemLike parts, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, head);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define('i', material);
        if (parts != null) {
            builder.define('p', parts);
        }
        builder.unlockedBy("has_material", has(material)).save(output, TGIds.id(getItemName(head)));
    }

    /**
     * A gun recipe whose result carries the loaded-ammo component. {@code magazine} (may be null) is
     * defined as 'm' in the pattern.
     */
    private static void gun(RecipeOutput output, Item gun, int loaded, String variant, String id, ItemLike magazine,
                            Consumer<StackShapedRecipeBuilder> keys, String... pattern) {
        ItemStack result = new ItemStack(gun);
        GunData.setAmmo(result, new GunAmmo(loaded, variant));
        StackShapedRecipeBuilder builder = StackShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result, pattern);
        keys.accept(builder);
        if (magazine != null) {
            builder.define('m', magazine);
        }
        builder.unlockedBy(getHasName(gun), has(gun)).save(output, TGIds.id(id));
    }

    /** Two rows of three with an empty middle row. */
    private static void barrel(RecipeOutput output, ItemLike barrel, Ingredient material,
                               net.minecraft.advancements.Criterion<?> trigger) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, barrel)
                .pattern("sss").pattern("   ").pattern("sss")
                .define('s', material)
                .unlockedBy("has_material", trigger).save(output, TGIds.id(getItemName(barrel)));
    }

    /** 9 nuggets to an ingot and back, ids {@code techguns:<material>_ingot_from_nuggets} / {@code _nugget_from_ingot}. */
    private static void nuggetsAndIngot(RecipeOutput output, ItemLike ingot, ItemLike nugget, TagKey<Item> nuggetTag, String material) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot).requires(Ingredient.of(nuggetTag), 9)
                .unlockedBy(getHasName(nugget), has(nugget)).save(output, TGIds.id(material + "_ingot_from_nuggets"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9).requires(ingot)
                .unlockedBy(getHasName(ingot), has(ingot)).save(output, TGIds.id(material + "_nugget_from_ingot"));
    }

    /** Blast furnace only recipes, ids {@code techguns:<result>_from_blasting_<input>}. */
    private static void blastingOnly(RecipeOutput output, List<ItemLike> inputs, ItemLike result, float experience, int time, String group) {
        for (ItemLike input : inputs) {
            SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, result, experience, time)
                    .group(group).unlockedBy(getHasName(input), has(input))
                    .save(output, TGIds.id(getItemName(result) + "_from_blasting_" + getItemName(input)));
        }
    }

    /**
     * Furnace (200 ticks) and blast furnace (100 ticks) recipes for each input, with ids
     * {@code techguns:<result>_from_smelting_<input>} / {@code _from_blasting_}.
     */
    private static void smeltingAndBlasting(RecipeOutput output, List<ItemLike> inputs, ItemLike result, float experience, String group) {
        for (ItemLike input : inputs) {
            String suffix = getItemName(result) + "_from_%s_" + getItemName(input);
            SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, result, experience, 200)
                    .group(group).unlockedBy(getHasName(input), has(input))
                    .save(output, TGIds.id(suffix.formatted("smelting")));
            SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, result, experience, 100)
                    .group(group).unlockedBy(getHasName(input), has(input))
                    .save(output, TGIds.id(suffix.formatted("blasting")));
        }
    }
}
