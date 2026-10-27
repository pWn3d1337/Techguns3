package techguns.client;

import java.util.function.Predicate;

/**
 * Lets the optional recipe viewer plugin ({@code techguns.compat.jei}) offer "show this category" to dev-only tools
 * without core client code importing it.
 */
public final class RecipeViewerHook {
    private static Predicate<String> showCategory = id -> false;

    private RecipeViewerHook() {
    }

    public static void setShowCategory(Predicate<String> action) {
        showCategory = action;
    }

    /** Opens the recipe viewer on the Techguns category {@code id} (e.g. {@code metal_press}); false without a viewer or category. */
    public static boolean showCategory(String id) {
        return showCategory.test(id);
    }
}
