package techguns.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import techguns.TGIds;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;
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
