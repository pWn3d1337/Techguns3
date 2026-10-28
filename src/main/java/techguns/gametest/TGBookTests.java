package techguns.gametest;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.klikli_dev.modonomicon.data.BookDataManager;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.Techguns;

/**
 * The Techguns manual for the optional guide book mods (both are on the dev runtime): one book recipe for the
 * installed mod, the Patchouli book registered, and every recipe, item and multiblock the generated Modonomicon pages
 * show exists (the Patchouli pages come from the same converted book, their content is client-only).
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGBookTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA)
    public static void manualRecipeForTheInstalledBookMod(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        boolean modonomicon = ModList.get().isLoaded("modonomicon");
        boolean patchouli = ModList.get().isLoaded("patchouli");
        helper.assertTrue(recipes.byKey(TGIds.id("techguns_manual_modonomicon")).isPresent() == modonomicon,
                "Modonomicon manual recipe only with Modonomicon");
        helper.assertTrue(recipes.byKey(TGIds.id("techguns_manual_patchouli")).isPresent() == (patchouli && !modonomicon),
                "Patchouli manual recipe only with Patchouli and without Modonomicon");
        var resources = helper.getLevel().getServer().getResourceManager();
        helper.assertTrue(resources.getResource(TGIds.id("patchouli_books/techguns_manual/book.json")).isPresent(), "Patchouli book.json");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void modonomiconPagesShowExistingThings(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var recipes = server.getRecipeManager();
        var resources = server.getResourceManager();
        Map<ResourceLocation, Resource> entries = resources.listResources("modonomicon/books/techguns_manual/entries",
                id -> id.getPath().endsWith(".json"));
        helper.assertTrue(entries.size() == 39, "39 manual entries, got " + entries.size());
        int recipesChecked = 0;
        for (var file : entries.entrySet()) {
            JsonObject entry = parse(file.getValue());
            helper.assertTrue(item(entry.getAsJsonObject("icon").get("item").getAsString()), file.getKey() + ": icon item");
            for (JsonElement element : entry.getAsJsonArray("pages")) {
                JsonObject page = element.getAsJsonObject();
                for (String key : new String[] {"recipe_id_1", "recipe_id_2"}) {
                    if (page.has(key)) {
                        var recipe = recipes.byKey(TGIds.parse(page.get(key).getAsString()));
                        helper.assertTrue(recipe.isPresent() && recipe.get().value().getType() == RecipeType.CRAFTING,
                                file.getKey() + ": crafting recipe " + page.get(key));
                        recipesChecked++;
                    }
                }
                if (page.has("item")) {
                    helper.assertTrue(item(page.getAsJsonObject("item").get("item").getAsString()), file.getKey() + ": spotlight item");
                }
                if (page.has("multiblock_id")) {
                    // Modonomicon writes ids in the book's namespace without it
                    String multiblock = page.get("multiblock_id").getAsString();
                    ResourceLocation id = multiblock.contains(":") ? TGIds.parse(multiblock) : TGIds.id(multiblock);
                    helper.assertTrue(resources.getResource(TGIds.of(id.getNamespace(), "modonomicon/multiblocks/" + id.getPath() + ".json")).isPresent(),
                            file.getKey() + ": multiblock " + id);
                }
            }
        }
        helper.assertTrue(recipesChecked == 70, "70 recipe slots on the crafting pages, got " + recipesChecked);
        if (ModList.get().isLoaded("modonomicon")) {
            helper.assertTrue(Modonomicon.loaded(helper.getLevel()), "Modonomicon loaded the manual with its entries");
        }
        helper.succeed();
    }

    private static boolean item(String id) {
        return BuiltInRegistries.ITEM.get(TGIds.parse(id)) != Items.AIR;
    }

    private static JsonObject parse(Resource resource) {
        try (Reader reader = resource.openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Only touched with Modonomicon installed. */
    private static final class Modonomicon {
        static boolean loaded(Level level) {
            // Modonomicon links entries to their books once a world is there (first player join); build them now
            BookDataManager books = BookDataManager.get();
            if (!books.areBooksBuilt()) {
                books.tryBuildBooks(level);
            }
            var book = books.getBook(TGIds.id("techguns_manual"));
            return book != null && book.getEntry(TGIds.id("multiblock_machines/fabricator")) != null
                    && book.getEntry(TGIds.id("multiblock_machines/reaction_chamber")) != null;
        }
    }
}
