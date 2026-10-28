package techguns.data.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.klikli_dev.modonomicon.api.datagen.MultiblockProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookCategoryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookEntityPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookMultiblockPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.book.BookDisplayMode;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import techguns.TGIds;
import techguns.Techguns;
import techguns.data.legacy.LegacyBook.Book;
import techguns.data.legacy.LegacyBook.Entry;

/**
 * The manual as a Modonomicon book (optional mod, the only class using its datagen API, loaded only by the data
 * generator): index display like the Patchouli book, texts as lang keys ({@link LegacyBook#lang}) holding markdown,
 * multiblocks as Modonomicon dense multiblocks.
 */
final class ModonomiconBook {
    private ModonomiconBook() {
    }

    /** Writes {@code <data path>, json} pairs below {@code data/techguns/}. */
    static void write(Book book, PackOutput output, HolderLookup.Provider lookup, BiConsumer<String, JsonObject> save) {
        Multiblocks multiblocks = new Multiblocks(output);
        String root = "modonomicon/books/" + LegacyBook.BOOK + "/";
        String prefix = "book.techguns." + LegacyBook.BOOK;
        BookModel model = BookModel.create(TGIds.id(LegacyBook.BOOK), prefix + ".name")
                .withTooltip(prefix + ".tooltip")
                .withDescription(prefix + ".description")
                .withCreativeTab(TGIds.id("main"))
                .withModel(TGIds.id(LegacyBook.BOOK))
                .withDisplayMode(BookDisplayMode.INDEX);

        Map<String, BookCategoryModel> categories = new HashMap<>();
        for (LegacyBook.Category category : book.categories()) {
            BookCategoryModel categoryModel = BookCategoryModel.create(TGIds.id(category.id()), LegacyBook.categoryKey(category.id()) + ".name")
                    .withDescription(LegacyBook.categoryKey(category.id()) + ".description")
                    .withIcon(BookIconModel.create(category.icon()))
                    .withSortNumber(category.sortnum())
                    .withDisplayMode(BookDisplayMode.INDEX);
            categories.put(category.id(), categoryModel);
            model.withCategory(categoryModel);
        }

        for (Entry entry : book.entries()) {
            BookCategoryModel category = categories.get(entry.category());
            BookEntryModel entryModel = BookEntryModel.create(TGIds.id(entry.id()), LegacyBook.entryKey(entry) + ".name")
                    .withIcon(BookIconModel.create(entry.icon()))
                    .withSortNumber(entry.sortnum())
                    .withCategory(category);
            List<BookPageModel<?>> pages = new ArrayList<>();
            for (int i = 0; i < entry.pages().size(); i++) {
                pages.add(page(entry, i, multiblocks, save));
            }
            entryModel.withPages(pages);
            category.withEntry(entryModel);
            save.accept(root + "entries/" + entry.id() + ".json", entryModel.toJson(lookup));
        }
        for (BookCategoryModel category : model.getCategories()) {
            save.accept(root + "categories/" + category.getId().getPath() + ".json", category.toJson(lookup));
        }
        save.accept(root + "book.json", model.toJson(lookup));
    }

    private static BookPageModel<?> page(Entry entry, int index, Multiblocks multiblocks, BiConsumer<String, JsonObject> save) {
        JsonObject page = entry.pages().get(index);
        String title = key(entry, index, page, "title");
        String text = key(entry, index, page, "text");
        BookPageModel<?> model = switch (page.get("type").getAsString()) {
            case "text" -> {
                BookTextPageModel textPage = BookTextPageModel.create().withText(text == null ? "" : text);
                // Patchouli titles an entry's first text page with the entry name, Modonomicon doesn't
                yield title != null ? textPage.withTitle(title) : index == 0 ? textPage.withTitle(LegacyBook.entryKey(entry) + ".name") : textPage;
            }
            case "crafting" -> {
                BookCraftingRecipePageModel crafting = BookCraftingRecipePageModel.create().withRecipeId1(page.get("recipe").getAsString());
                if (page.has("recipe2")) {
                    crafting.withRecipeId2(page.get("recipe2").getAsString());
                }
                yield text == null ? crafting : crafting.withText(text);
            }
            case "spotlight" -> {
                BookSpotlightPageModel spotlight = BookSpotlightPageModel.create()
                        .withItem(BuiltInRegistries.ITEM.get(TGIds.parse(page.get("item").getAsString())));
                if (title != null) {
                    spotlight.withTitle(title);
                }
                yield text == null ? spotlight : spotlight.withText(text);
            }
            case "image" -> {
                BookImagePageModel image = BookImagePageModel.create().withImages(LegacyBook.strings(page.getAsJsonArray("images")).stream()
                        .map(TGIds::parse).toArray(ResourceLocation[]::new));
                if (title != null) {
                    image.withTitle(title);
                }
                yield text == null ? image : image.withText(text);
            }
            case "entity" -> {
                BookEntityPageModel entity = BookEntityPageModel.create().withEntityId(page.get("entity").getAsString());
                String name = key(entry, index, page, "name");
                if (name != null) {
                    entity.withEntityName(name);
                }
                yield text == null ? entity : entity.withText(text);
            }
            case "multiblock" -> {
                String id = "book/" + entry.path() + "_" + index;
                save.accept("modonomicon/multiblocks/" + id + ".json", multiblocks.dense(page.getAsJsonObject("multiblock")));
                BookMultiblockPageModel multiblock = BookMultiblockPageModel.create().withMultiblockId(TGIds.id(id));
                String name = key(entry, index, page, "name");
                if (name != null) {
                    multiblock.withMultiblockName(name);
                }
                yield text == null ? multiblock : multiblock.withText(text);
            }
            default -> throw new IllegalStateException(entry.id() + ": no Modonomicon page for " + page.get("type"));
        };
        if (page.has("anchor")) {
            model.withAnchor(page.get("anchor").getAsString());
        }
        return model;
    }

    /** The lang key of a page text, or null if the page has none (the same rule as {@link LegacyBook#lang}). */
    private static String key(Entry entry, int index, JsonObject page, String field) {
        return LegacyBook.string(page, field) == null ? null : LegacyBook.pageKey(entry, index, field);
    }

    /** Only for Modonomicon's multiblock builder (protected in its provider); never run as a provider. */
    private static final class Multiblocks extends MultiblockProvider {
        Multiblocks(PackOutput output) {
            super(output, Techguns.MODID);
        }

        @Override
        public void buildMultiblocks() {
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            throw new UnsupportedOperationException();
        }

        /** A Patchouli multiblock (layers top first, the same order Modonomicon's dense format uses). */
        JsonObject dense(JsonObject patchouli) {
            DenseMultiblockBuilder builder = new DenseMultiblockBuilder();
            for (JsonElement layer : patchouli.getAsJsonArray("pattern")) {
                builder.layer(LegacyBook.strings((JsonArray) layer).toArray(String[]::new));
            }
            JsonObject mapping = patchouli.getAsJsonObject("mapping");
            for (String key : mapping.keySet()) {
                Block block = BuiltInRegistries.BLOCK.get(TGIds.parse(mapping.get(key).getAsString()));
                builder.block(key.charAt(0), () -> block);
            }
            return builder.build();
        }
    }
}
