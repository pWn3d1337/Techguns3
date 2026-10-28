package techguns.data.legacy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.ModList;
import techguns.Techguns;
import techguns.data.legacy.LegacyBook.Book;
import techguns.data.legacy.LegacyBook.Category;
import techguns.data.legacy.LegacyBook.Entry;

/**
 * The Techguns manual for both optional guide book mods, from the one converted Techguns 2 book ({@link LegacyBook}):
 * the Patchouli book (book in data, content as a resource pack), the Modonomicon book ({@link ModonomiconBook}), the
 * book item model and the crafting recipes. Techguns 2 crafted the manual from an iron ingot, a glass pane and stone
 * bullets or pistol rounds; with both mods installed the recipe makes the Modonomicon book (it has the 26.x versions).
 */
public class LegacyBookProvider implements DataProvider {
    private static final String BOOK_ID = "techguns:" + LegacyBook.BOOK;

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookup;

    public LegacyBookProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        this.output = output;
        this.lookup = lookup;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Book book = LegacyBook.load().orElse(null);
        if (book == null) {
            return CompletableFuture.completedFuture(null);
        }
        if (!ModList.get().isLoaded("modonomicon")) {
            throw new IllegalStateException("The book data generator needs Modonomicon on the runtime classpath (localRuntime)");
        }
        return lookup.thenCompose(registries -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            Path data = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(Techguns.MODID);
            Path assets = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(Techguns.MODID);
            writePatchouli(book, data, assets, cache, futures);
            ModonomiconBook.write(book, output, registries,
                    (path, json) -> futures.add(DataProvider.saveStable(cache, json, data.resolve(path))));
            futures.add(DataProvider.saveStable(cache, recipe("modonomicon:modonomicon", "modonomicon:book_id", modLoaded("modonomicon")),
                    data.resolve("recipe/techguns_manual_modonomicon.json")));
            JsonObject notModonomicon = new JsonObject();
            notModonomicon.addProperty("type", "neoforge:not");
            notModonomicon.add("value", modLoaded("modonomicon"));
            futures.add(DataProvider.saveStable(cache, recipe("patchouli:guide_book", "patchouli:book", modLoaded("patchouli"), notModonomicon),
                    data.resolve("recipe/techguns_manual_patchouli.json")));
            JsonObject model = new JsonObject();
            model.addProperty("parent", "minecraft:item/generated");
            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", "techguns:item/" + LegacyBook.BOOK);
            model.add("textures", textures);
            futures.add(DataProvider.saveStable(cache, model, assets.resolve("models/item/" + LegacyBook.BOOK + ".json")));
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    /** The Techguns 2 book format, ids remapped; Patchouli 1.21 wants namespaced page types and categories. */
    private static void writePatchouli(Book book, Path data, Path assets, CachedOutput cache, List<CompletableFuture<?>> futures) {
        JsonObject json = new JsonObject();
        json.addProperty("name", LegacyBook.NAME);
        json.addProperty("landing_text", LegacyBook.LANDING_TEXT);
        json.addProperty("version", 1);
        json.addProperty("creative_tab", "techguns:main");
        json.addProperty("model", BOOK_ID);
        json.addProperty("book_texture", "techguns:textures/gui/tgmanual_textures.png");
        json.addProperty("nameplate_color", "000000");
        json.addProperty("open_sound", "minecraft:item.armor.equip_iron");
        json.addProperty("flip_sound", "minecraft:ui.button.click");
        json.addProperty("use_resource_pack", true);
        futures.add(DataProvider.saveStable(cache, json, data.resolve("patchouli_books/" + LegacyBook.BOOK + "/book.json")));

        Path content = assets.resolve("patchouli_books/" + LegacyBook.BOOK + "/en_us");
        for (Category category : book.categories()) {
            JsonObject categoryJson = new JsonObject();
            categoryJson.addProperty("name", category.name());
            categoryJson.addProperty("description", category.description());
            categoryJson.addProperty("icon", BuiltInRegistries.ITEM.getKey(category.icon()).toString());
            categoryJson.addProperty("sortnum", category.sortnum());
            futures.add(DataProvider.saveStable(cache, categoryJson, content.resolve("categories/" + category.id() + ".json")));
        }
        for (Entry entry : book.entries()) {
            JsonObject entryJson = new JsonObject();
            entryJson.addProperty("name", entry.name());
            entryJson.addProperty("icon", BuiltInRegistries.ITEM.getKey(entry.icon()).toString());
            entryJson.addProperty("category", "techguns:" + entry.category());
            JsonArray pages = new JsonArray();
            for (JsonObject page : entry.pages()) {
                JsonObject copy = page.deepCopy();
                copy.addProperty("type", "patchouli:" + page.get("type").getAsString());
                if (LegacyBook.string(copy, "text") == null) {
                    copy.remove("text");
                }
                pages.add(copy);
            }
            entryJson.add("pages", pages);
            futures.add(DataProvider.saveStable(cache, entryJson, content.resolve("entries/" + entry.id() + ".json")));
        }
    }

    private static JsonObject modLoaded(String modid) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", modid);
        return condition;
    }

    /** Shapeless: iron ingot, glass pane, stone bullets or pistol rounds; the book item with the manual's id. */
    private static JsonObject recipe(String item, String component, JsonObject... conditions) {
        JsonObject recipe = new JsonObject();
        JsonArray conditionArray = new JsonArray();
        for (JsonObject condition : conditions) {
            conditionArray.add(condition);
        }
        recipe.add("neoforge:conditions", conditionArray);
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemIngredient("minecraft:iron_ingot"));
        ingredients.add(itemIngredient("minecraft:glass_pane"));
        JsonArray ammo = new JsonArray();
        ammo.add(itemIngredient("techguns:stone_bullets"));
        ammo.add(itemIngredient("techguns:pistol_rounds"));
        ingredients.add(ammo);
        recipe.add("ingredients", ingredients);
        JsonObject result = new JsonObject();
        result.addProperty("id", item);
        result.addProperty("count", 1);
        JsonObject components = new JsonObject();
        components.addProperty(component, BOOK_ID);
        result.add("components", components);
        recipe.add("result", result);
        return recipe;
    }

    private static JsonObject itemIngredient(String item) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", item);
        return ingredient;
    }

    @Override
    public String getName() {
        return "Techguns manual (Patchouli, Modonomicon)";
    }
}
