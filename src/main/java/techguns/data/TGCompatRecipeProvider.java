package techguns.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import techguns.Techguns;

/**
 * Tier 2 compat (design doc §5.4): recipes for other mods' machines, written as their JSON with a
 * {@code neoforge:mod_loaded} condition, so no code or dependency is needed. Only what those mods lack: ore processing,
 * steel, bronze and their own plates already work through the {@code c:} tags. Added are the Techguns plates their
 * presses don't make and the obsidian steel alloy (Techguns: Blast Furnace, steel + obsidian). Formats copied from the
 * recipes in Create 6.0.10, Mekanism 10.7.19, Immersive Engineering 12.4.2, Modern Industrialization 2.5.11 and
 * Oritech 1.2.13; {@code -Pcompat_mods=true} runs the GameTests with those mods to check every recipe loads.
 */
public class TGCompatRecipeProvider implements DataProvider {
    public static final List<String> MODS = List.of("create", "mekanism", "immersiveengineering", "modern_industrialization", "oritech");

    private final PackOutput output;
    private final List<CompletableFuture<?>> futures = new ArrayList<>();
    private CachedOutput cache;

    public TGCompatRecipeProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.cache = cache;
        futures.clear();
        create();
        immersiveEngineering();
        modernIndustrialization();
        mekanism();
        oritech();
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /** Pressing: Create presses iron, copper, gold and brass sheets, with Immersive Engineering also its steel and lead plates. */
    private void create() {
        for (String metal : new String[] {"tin", "bronze", "obsidian_steel", "titanium", "steel", "lead"}) {
            JsonObject recipe = recipe("create:pressing");
            recipe.add("ingredients", array(tag("c:ingots/" + metal)));
            recipe.add("results", array(id("techguns:" + metal + "_plate", 1)));
            boolean ieMakesIt = metal.equals("steel") || metal.equals("lead");
            save("create", "pressing/" + metal + "_plate", recipe, ieMakesIt ? new JsonObject[] {loaded("create"), not(loaded("immersiveengineering"))}
                    : new JsonObject[] {loaded("create")});
        }
        JsonObject mixing = recipe("create:mixing");
        mixing.addProperty("heat_requirement", "heated");
        mixing.add("ingredients", array(tag("c:ingots/steel"), tag("c:obsidians")));
        mixing.add("results", array(id("techguns:obsidian_steel_ingot", 1)));
        save("create", "mixing/obsidian_steel_ingot", mixing, loaded("create"));
    }

    /** Metal press plates for the metals Immersive Engineering has none of; obsidian steel in the alloy kiln and arc furnace. */
    private void immersiveEngineering() {
        for (String metal : new String[] {"obsidian_steel", "titanium"}) {
            JsonObject recipe = recipe("immersiveengineering:metal_press");
            recipe.addProperty("energy", 2400);
            recipe.add("input", tag("c:ingots/" + metal));
            recipe.addProperty("mold", "immersiveengineering:mold_plate");
            recipe.add("result", id("techguns:" + metal + "_plate", 1));
            save("immersiveengineering", "metal_press/" + metal + "_plate", recipe, loaded("immersiveengineering"));
        }
        JsonObject alloy = recipe("immersiveengineering:alloy");
        alloy.add("input0", tag("c:ingots/steel"));
        alloy.add("input1", tag("c:obsidians"));
        alloy.add("result", id("techguns:obsidian_steel_ingot", 1));
        save("immersiveengineering", "alloy/obsidian_steel_ingot", alloy, loaded("immersiveengineering"));

        JsonObject arc = recipe("immersiveengineering:arc_furnace");
        arc.add("additives", array(tag("c:obsidians")));
        arc.addProperty("energy", 51200);
        arc.add("input", tag("c:ingots/steel"));
        arc.add("results", array(id("techguns:obsidian_steel_ingot", 1)));
        arc.addProperty("time", 100);
        save("immersiveengineering", "arc_furnace/obsidian_steel_ingot", arc, loaded("immersiveengineering"));
    }

    /** Modern Industrialization has plates of all Techguns metals except obsidian steel. */
    private void modernIndustrialization() {
        JsonObject compressor = recipe("modern_industrialization:compressor");
        compressor.addProperty("duration", 200);
        compressor.addProperty("eu", 2);
        compressor.add("item_inputs", array(amount(tag("c:ingots/obsidian_steel"), 1)));
        compressor.add("item_outputs", array(amount(item("techguns:obsidian_steel_plate"), 1)));
        save("modern_industrialization", "compressor/obsidian_steel_plate", compressor, loaded("modern_industrialization"));

        JsonObject blast = recipe("modern_industrialization:blast_furnace");
        blast.addProperty("duration", 400);
        blast.addProperty("eu", 4);
        blast.add("item_inputs", array(amount(tag("c:ingots/steel"), 1), amount(tag("c:obsidians"), 1)));
        blast.add("item_outputs", array(amount(item("techguns:obsidian_steel_ingot"), 1)));
        save("modern_industrialization", "blast_furnace/obsidian_steel_ingot", blast, loaded("modern_industrialization"));
    }

    /** Steel infused with refined obsidian. */
    private void mekanism() {
        JsonObject infusing = recipe("mekanism:metallurgic_infusing");
        JsonObject chemical = new JsonObject();
        chemical.addProperty("amount", 20);
        chemical.addProperty("tag", "mekanism:refined_obsidian");
        infusing.add("chemical_input", chemical);
        JsonObject input = tag("c:ingots/steel");
        input.addProperty("count", 1);
        infusing.add("item_input", input);
        infusing.add("output", id("techguns:obsidian_steel_ingot", 1));
        infusing.addProperty("per_tick_usage", false);
        save("mekanism", "metallurgic_infusing/obsidian_steel_ingot", infusing, loaded("mekanism"));
    }

    private void oritech() {
        JsonObject foundry = recipe("oritech:foundry");
        foundry.add("ingredients", array(tag("c:ingots/steel"), tag("c:obsidians")));
        foundry.add("results", array(id("techguns:obsidian_steel_ingot", 1)));
        foundry.addProperty("time", 80);
        save("oritech", "foundry/obsidian_steel_ingot", foundry, loaded("oritech"));
    }

    private void save(String mod, String name, JsonObject recipe, JsonObject... conditions) {
        JsonObject json = new JsonObject();
        JsonArray conditionArray = new JsonArray();
        for (JsonObject condition : conditions) {
            conditionArray.add(condition);
        }
        json.add("neoforge:conditions", conditionArray);
        recipe.entrySet().forEach(e -> json.add(e.getKey(), e.getValue()));
        Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(Techguns.MODID + "/recipe/compat/" + mod + "/" + name + ".json");
        futures.add(DataProvider.saveStable(cache, json, path));
    }

    private static JsonObject recipe(String type) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", type);
        return recipe;
    }

    private static JsonObject loaded(String modid) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", modid);
        return condition;
    }

    private static JsonObject not(JsonObject condition) {
        JsonObject not = new JsonObject();
        not.addProperty("type", "neoforge:not");
        not.add("value", condition);
        return not;
    }

    private static JsonObject tag(String tag) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("tag", tag);
        return ingredient;
    }

    private static JsonObject item(String item) {
        JsonObject stack = new JsonObject();
        stack.addProperty("item", item);
        return stack;
    }

    /** An item stack in the 1.21 {@code id} / {@code count} form. */
    private static JsonObject id(String item, int count) {
        JsonObject stack = new JsonObject();
        stack.addProperty("id", item);
        if (count != 1) {
            stack.addProperty("count", count);
        }
        return stack;
    }

    /** Modern Industrialization's item ingredients and outputs carry an {@code amount}. */
    private static JsonObject amount(JsonObject entry, int amount) {
        entry.addProperty("amount", amount);
        return entry;
    }

    private static JsonArray array(JsonObject... entries) {
        JsonArray array = new JsonArray();
        for (JsonObject entry : entries) {
            array.add(entry);
        }
        return array;
    }

    @Override
    public String getName() {
        return "Techguns compat recipes";
    }
}
