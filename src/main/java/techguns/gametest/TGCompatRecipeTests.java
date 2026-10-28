package techguns.gametest;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.Techguns;
import techguns.data.TGCompatRecipeProvider;

/**
 * Tier 2 compat recipes: each one is loaded exactly when its mod conditions hold. Without the mods this checks the
 * conditions hide them; with {@code -Pcompat_mods=true} (the mods on the dev runtime) it checks every recipe parses in
 * its mod's format, since a recipe that fails to parse is dropped.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGCompatRecipeTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA)
    public static void compatRecipesLoadWithTheirMods(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var files = server.getResourceManager().listResources("recipe/compat", id -> id.getNamespace().equals(Techguns.MODID)
                && id.getPath().endsWith(".json"));
        helper.assertTrue(files.size() == 15, "15 compat recipes, got " + files.size());
        for (var file : files.entrySet()) {
            String path = file.getKey().getPath();
            String mod = path.split("/")[2];
            helper.assertTrue(TGCompatRecipeProvider.MODS.contains(mod), path + ": compat folder of a planned mod");
            ResourceLocation id = TGIds.id(path.substring("recipe/".length(), path.length() - ".json".length()));
            boolean expected = conditionsHold(parse(file.getValue()));
            helper.assertTrue(server.getRecipeManager().byKey(id).isPresent() == expected,
                    id + (expected ? " should load (conditions hold), but failed to parse" : " should be hidden by its conditions"));
        }
        helper.succeed();
    }

    /** The {@code neoforge:mod_loaded} / {@code neoforge:not} conditions the provider writes. */
    private static boolean conditionsHold(JsonObject recipe) {
        for (JsonElement condition : recipe.getAsJsonArray("neoforge:conditions")) {
            if (!holds(condition.getAsJsonObject())) {
                return false;
            }
        }
        return true;
    }

    private static boolean holds(JsonObject condition) {
        return switch (condition.get("type").getAsString()) {
            case "neoforge:mod_loaded" -> ModList.get().isLoaded(condition.get("modid").getAsString());
            case "neoforge:not" -> !holds(condition.getAsJsonObject("value"));
            default -> throw new IllegalStateException("Unexpected condition " + condition);
        };
    }

    private static JsonObject parse(Resource resource) {
        try (Reader reader = resource.openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
