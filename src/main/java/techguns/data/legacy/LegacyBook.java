package techguns.data.legacy;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import techguns.TGIds;

/**
 * The Techguns 2 manual (a Patchouli book, copied to {@code src/legacy/book/techguns_manual}) read into one book
 * source with Techguns 3 ids, for the Patchouli and the Modonomicon exporters. Entries get the id
 * {@code <category>/<name>}; pages keep their Patchouli JSON with items, recipes, multiblock blocks, entities and
 * links remapped. {@link #markdown} converts the Patchouli text macros for Modonomicon.
 */
public final class LegacyBook {
    public static final String BOOK = "techguns_manual";
    public static final String NAME = "Techguns Manual";
    /** Techguns 2 had "WIP" here. */
    public static final String LANDING_TEXT = "The Techguns manual: weapons, armor, machines and how to progress from the first gun to the endgame.";

    public record Category(String id, String name, String description, Item icon, int sortnum) {
    }

    /** {@code sortnum}: Patchouli sorts entries without one by name; Modonomicon gets that order explicitly. */
    public record Entry(String id, String category, String name, Item icon, int sortnum, List<JsonObject> pages) {
        public String path() {
            return id.substring(id.indexOf('/') + 1);
        }
    }

    public record Book(List<Category> categories, List<Entry> entries) {
    }

    /** Techguns 2 block and item metas used as icons and spotlight items (blocks: their 1.12 machine type order). */
    private static final Map<String, String> META_ITEMS = Map.ofEntries(
            Map.entry("basicmachine:0", "ammo_press"), Map.entry("basicmachine:1", "metal_press"),
            Map.entry("basicmachine:2", "chem_lab"), Map.entry("basicmachine:3", "turret"),
            Map.entry("simplemachine:8", "camo_bench"), Map.entry("simplemachine:9", "repair_bench"),
            Map.entry("simplemachine:10", "charging_station"), Map.entry("simplemachine:11", "blast_furnace"),
            Map.entry("simplemachine2:8", "grinder"), Map.entry("simplemachine2:9", "upgrade_bench"),
            Map.entry("multiblockmachine:2", "fabricator_controller"), Map.entry("multiblockmachine:5", "reaction_chamber_controller"),
            Map.entry("orecluster:1", "ore_cluster_common_metal"), Map.entry("oredrill:4", "ore_drill_controller"),
            Map.entry("item_door3x3:0", "door_metal_3x3"), Map.entry("item_door3x3:1", "door_hangar_up_3x3"));

    /** Items Techguns 3 doesn't have, with the icon used instead (pages showing them are turned into text). */
    private static final Map<String, String> MISSING_ITEMS = Map.of("crowbar", "door_metal_3x3");

    /** Entry ids that changed, and the dead links of Techguns 2 that pointed at an entry under another name. */
    private static final Map<String, String> ENTRY_RENAMES = Map.of("machines/reactionchamber", "machines/reaction_chamber");
    private static final Map<String, String> LINK_ALIASES = Map.of("fabricator", "machines/fabricator", "reactionchamber", "machines/reaction_chamber");

    /** Patchouli key names of Techguns 2 to the Techguns 3 key mappings, with their English name for Modonomicon. */
    private static final Map<String, String[]> KEYS = Map.of(
            "techguns.key.forceReload", new String[] {"key.techguns.force_reload", "Force Reload"},
            "techguns.key.toggleSafemode", new String[] {"key.techguns.toggle_safe_mode", "Toggle Safe Mode"},
            "techguns.key.toggleStepassist", new String[] {"key.techguns.toggle_step_assist", "Toggle Step Assist"},
            "techguns.key.toggleNightvision", new String[] {"key.techguns.toggle_night_vision", "Toggle Night Vision"});

    private static final Pattern MACRO = Pattern.compile("\\$\\(([^)]*)\\)");
    private static final Pattern NBT_ITEM = Pattern.compile("id:'([a-z0-9_:]+)'");

    private LegacyBook() {
    }

    /** The converted book, or empty outside the data generator run. */
    public static Optional<Book> load() {
        return LegacyFiles.dir().map(dir -> read(dir.resolve("book").resolve(BOOK).resolve("en_us")));
    }

    private static Book read(Path root) {
        List<Category> categories = new ArrayList<>();
        try (Stream<Path> files = Files.list(root.resolve("categories"))) {
            for (Path file : files.sorted().toList()) {
                JsonObject json = parse(file);
                String id = file.getFileName().toString().replace(".json", "");
                categories.add(new Category(id, json.get("name").getAsString(), json.get("description").getAsString(),
                        item(json.get("icon").getAsString()), json.get("sortnum").getAsInt()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        categories.sort(Comparator.comparingInt(Category::sortnum));

        // first pass: the new id of every legacy entry, for the links
        Map<String, JsonObject> legacy = new LinkedHashMap<>();
        Map<String, String> ids = new HashMap<>();
        Path entries = root.resolve("entries");
        try (Stream<Path> files = Files.walk(entries)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".json")).sorted().toList()) {
                String legacyId = entries.relativize(file).toString().replace('\\', '/').replace(".json", "");
                JsonObject json = parse(file);
                String renamed = ENTRY_RENAMES.getOrDefault(legacyId, legacyId);
                String name = renamed.substring(renamed.lastIndexOf('/') + 1);
                ids.put(legacyId, json.get("category").getAsString() + "/" + name);
                legacy.put(legacyId, json);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        ENTRY_RENAMES.forEach((old, renamed) -> ids.put(renamed, ids.get(old)));
        LINK_ALIASES.forEach((alias, target) -> ids.put(alias, ids.get(target)));

        Map<String, Set<String>> anchors = new HashMap<>();
        List<Entry> result = new ArrayList<>();
        legacy.forEach((legacyId, json) -> {
            String id = ids.get(legacyId);
            List<JsonObject> pages = new ArrayList<>();
            for (JsonElement element : json.getAsJsonArray("pages")) {
                JsonObject page = convertPage(element.getAsJsonObject().deepCopy(), ids);
                if (page.has("anchor")) {
                    anchors.computeIfAbsent(id, k -> new HashSet<>()).add(page.get("anchor").getAsString());
                }
                pages.add(page);
            }
            result.add(new Entry(id, json.get("category").getAsString(), json.get("name").getAsString(),
                    item(json.get("icon").getAsString()), 0, pages));
        });
        validateLinks(result, anchors);

        // Patchouli order: by name within a category
        result.sort(Comparator.comparing(Entry::category).thenComparing(Entry::name));
        List<Entry> sorted = new ArrayList<>();
        Map<String, Integer> next = new HashMap<>();
        for (Entry entry : result) {
            int sortnum = next.merge(entry.category(), 1, Integer::sum) - 1;
            sorted.add(new Entry(entry.id(), entry.category(), entry.name(), entry.icon(), sortnum, entry.pages()));
        }
        return new Book(categories, sorted);
    }

    private static JsonObject convertPage(JsonObject page, Map<String, String> ids) {
        for (String key : new String[] {"text", "title"}) {
            if (page.has(key)) {
                page.addProperty(key, relink(page.get(key).getAsString(), ids));
            }
        }
        for (String key : new String[] {"recipe", "recipe2"}) {
            if (page.has(key)) {
                page.addProperty(key, recipe(page.get(key).getAsString()));
            }
        }
        if (page.has("item")) {
            String legacyItem = page.get("item").getAsString();
            if (MISSING_ITEMS.containsKey(legacyItem.substring(legacyItem.indexOf(':') + 1))) {
                // Techguns 3 has no such item: keep the text for the author to rewrite
                page.addProperty("type", "text");
                page.remove("item");
            } else {
                page.addProperty("item", BuiltInRegistries.ITEM.getKey(item(legacyItem)).toString());
            }
        }
        if (page.has("multiblock")) {
            JsonObject mapping = page.getAsJsonObject("multiblock").getAsJsonObject("mapping");
            for (String key : List.copyOf(mapping.keySet())) {
                mapping.addProperty(key, block(mapping.get(key).getAsString()));
            }
        }
        if (page.has("entity")) {
            // 1.21 item NBT: lower-case count
            String entity = page.get("entity").getAsString().replace("Count:1", "count:1");
            Matcher items = NBT_ITEM.matcher(entity);
            while (items.find()) {
                if (BuiltInRegistries.ITEM.get(TGIds.parse(items.group(1))) == Items.AIR) {
                    throw new IllegalStateException("Book entity page wears an unknown item: " + items.group(1));
                }
            }
            page.addProperty("entity", entity);
        }
        return page;
    }

    /** Rewrites {@code $(l:target)} links to the new entry ids and {@code $(k:...)} to the Techguns 3 key mappings. */
    private static String relink(String text, Map<String, String> ids) {
        Matcher matcher = MACRO.matcher(text);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String macro = matcher.group(1);
            String replacement = matcher.group();
            if (macro.startsWith("l:")) {
                String target = macro.substring(2);
                int hash = target.indexOf('#');
                String entry = hash < 0 ? target : target.substring(0, hash);
                String id = ids.get(entry);
                if (id == null) {
                    throw new IllegalStateException("Book link to an unknown entry: " + target);
                }
                replacement = "$(l:" + id + (hash < 0 ? "" : target.substring(hash)) + ")";
            } else if (macro.startsWith("k:")) {
                String[] key = KEYS.get(macro.substring(2));
                if (key == null) {
                    throw new IllegalStateException("Book key macro without a Techguns 3 key mapping: " + macro);
                }
                replacement = "$(k:" + key[0] + ")";
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private static void validateLinks(List<Entry> entries, Map<String, Set<String>> anchors) {
        Set<String> known = new HashSet<>();
        entries.forEach(entry -> known.add(entry.id()));
        for (Entry entry : entries) {
            for (JsonObject page : entry.pages()) {
                for (String key : new String[] {"text", "title"}) {
                    if (!page.has(key)) {
                        continue;
                    }
                    Matcher matcher = MACRO.matcher(page.get(key).getAsString());
                    while (matcher.find()) {
                        if (!matcher.group(1).startsWith("l:")) {
                            continue;
                        }
                        String target = matcher.group(1).substring(2);
                        int hash = target.indexOf('#');
                        String id = hash < 0 ? target : target.substring(0, hash);
                        if (!known.contains(id) || hash >= 0 && !anchors.getOrDefault(id, Set.of()).contains(target.substring(hash + 1))) {
                            throw new IllegalStateException(entry.id() + ": dead book link " + target);
                        }
                    }
                }
            }
        }
    }

    /** {@code techguns:name} or {@code techguns:name:meta} of Techguns 2 to the Techguns 3 item. */
    private static Item item(String legacy) {
        String[] parts = legacy.split(":");
        String path = parts[1];
        int meta = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        String key = path + ":" + meta;
        Item item;
        if (META_ITEMS.containsKey(key)) {
            item = BuiltInRegistries.ITEM.get(TGIds.id(META_ITEMS.get(key)));
        } else if (MISSING_ITEMS.containsKey(path)) {
            item = BuiltInRegistries.ITEM.get(TGIds.id(MISSING_ITEMS.get(path)));
        } else {
            item = LegacyItems.item(parts[0] + ":" + path, meta);
        }
        if (item == null || item == Items.AIR) {
            throw new IllegalStateException("Book item without a Techguns 3 item: " + legacy);
        }
        return item;
    }

    /** Techguns 2 recipe ids: machines were {@code <block>_<meta>_<name>}. */
    private static String recipe(String legacy) {
        String path = legacy.substring(legacy.indexOf(':') + 1)
                .replaceFirst("^(basicmachine|simplemachine2?|multiblockmachine)_\\d+_", "")
                .replaceFirst("^oredrill_\\d+_", "ore_drill_")
                .replace("reactionchamber", "reaction_chamber")
                .replace("armor_bench", "upgrade_bench");
        return TGIds.id(path).toString();
    }

    /** Techguns 2 multiblock states ({@code techguns:oredrill[machinetype=rod]}) to the Techguns 3 block. */
    private static String block(String legacy) {
        Matcher matcher = Pattern.compile("techguns:(\\w+)\\[(?:machinetype|type)=(\\w+)]").matcher(legacy);
        if (!matcher.matches()) {
            throw new IllegalStateException("Unknown book multiblock block: " + legacy);
        }
        String name = switch (matcher.group(1)) {
            case "multiblockmachine" -> matcher.group(2).replace("reactionchamber", "reaction_chamber");
            case "oredrill" -> "ore_drill_" + matcher.group(2);
            case "orecluster" -> "ore_cluster_" + matcher.group(2);
            default -> throw new IllegalStateException("Unknown book multiblock block: " + legacy);
        };
        Block block = BuiltInRegistries.BLOCK.get(TGIds.id(name));
        if (block == Blocks.AIR) {
            throw new IllegalStateException("Book multiblock block without a Techguns 3 block: " + legacy);
        }
        return TGIds.id(name).toString();
    }

    /**
     * Patchouli text macros as Modonomicon markdown: links to {@code entry://} links, highlights to bold, list items
     * to markdown lists, {@code $(br)} to a hard line break (or the end of a list) and {@code $(br2)} to a paragraph.
     * Markdown characters in the text are escaped.
     */
    public static String markdown(String text) {
        StringBuilder out = new StringBuilder();
        Matcher matcher = MACRO.matcher(text);
        int last = 0;
        boolean bold = false;
        boolean list = false;
        String link = null;
        StringBuilder linkText = null;
        while (true) {
            boolean found = matcher.find();
            String plain = escape(text.substring(last, found ? matcher.start() : text.length()));
            if (list && plain.isBlank() && found) {
                plain = "";
            }
            (linkText != null ? linkText : out).append(plain);
            if (!found) {
                break;
            }
            last = matcher.end();
            String macro = matcher.group(1);
            StringBuilder target = linkText != null ? linkText : out;
            if (macro.startsWith("l:")) {
                link = macro.substring(2);
                linkText = new StringBuilder();
            } else if (macro.equals("/l")) {
                if (link != null) {
                    String label = linkText.toString().strip();
                    // Modonomicon: '#' would be a page number, '@' names a page anchor
                    out.append('[').append(label).append("](entry://").append(link.replace('#', '@')).append(')');
                }
                link = null;
                linkText = null;
            } else if (macro.startsWith("k:")) {
                String key = macro.substring(2);
                String name = KEYS.values().stream().filter(k -> k[0].equals(key)).map(k -> k[1]).findFirst().orElse(key);
                target.append(name);
            } else if (macro.equals("thing") || macro.equals("l") || macro.equals("9")) {
                if (!bold) {
                    trimEnd(target);
                    if (target.length() > 0 && !Character.isWhitespace(target.charAt(target.length() - 1))) {
                        target.append(' ');
                    }
                    target.append("**");
                    bold = true;
                }
            } else if (macro.isEmpty()) {
                if (bold) {
                    trimEnd(target);
                    target.append("**");
                    bold = false;
                }
            } else if (macro.equals("li")) {
                trimBreaks(out);
                out.append(out.length() == 0 || list ? "\n" : "\n\n").append("- ");
                list = true;
            } else if (macro.equals("br")) {
                trimBreaks(out);
                out.append(list ? "\n\n" : "\\\n");
                list = false;
            } else if (macro.equals("br2")) {
                trimBreaks(out);
                out.append("\n\n");
                list = false;
            } else {
                throw new IllegalStateException("Unknown Patchouli macro in the book: $(" + macro + ")");
            }
        }
        if (bold) {
            trimEnd(out);
            out.append("**");
        }
        trimBreaks(out);
        return out.toString().strip().replaceAll("\n +", "\n").replaceAll("\n- +", "\n- ").replaceAll("^- +", "- ").replaceAll(" {2,}", " ");
    }

    /** Drops trailing spaces and line breaks: a hard break ({@code \}) before a new block would show as a backslash. */
    private static void trimBreaks(StringBuilder text) {
        while (text.length() > 0 && " \n".indexOf(text.charAt(text.length() - 1)) >= 0
                || text.length() > 1 && text.charAt(text.length() - 1) == '\\' && text.charAt(text.length() - 2) != '\\') {
            text.setLength(text.length() - 1);
        }
    }

    private static void trimEnd(StringBuilder text) {
        while (text.length() > 0 && text.charAt(text.length() - 1) == ' ') {
            text.setLength(text.length() - 1);
        }
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("*", "\\*").replace("_", "\\_").replace("[", "\\[").replace("]", "\\]")
                .replace("<", "\\<");
    }

    /**
     * Modonomicon lang keys of the book (its texts are translation keys) with the English text; the lang value goes
     * through {@code String.format}, so {@code %} is doubled.
     */
    public static void lang(Book book, BiConsumer<String, String> add) {
        String prefix = "book.techguns." + BOOK;
        add.accept(prefix + ".name", NAME);
        add.accept(prefix + ".tooltip", "A guide to Techguns");
        add.accept(prefix + ".description", LANDING_TEXT);
        for (Category category : book.categories()) {
            add.accept(categoryKey(category.id()) + ".name", category.name());
            add.accept(categoryKey(category.id()) + ".description", category.description());
        }
        for (Entry entry : book.entries()) {
            add.accept(entryKey(entry) + ".name", entry.name());
            for (int i = 0; i < entry.pages().size(); i++) {
                JsonObject page = entry.pages().get(i);
                for (String key : new String[] {"title", "name", "text"}) {
                    if (page.has(key) && !page.get(key).getAsString().isBlank()) {
                        add.accept(pageKey(entry, i, key), markdown(page.get(key).getAsString()).replace("%", "%%"));
                    }
                }
            }
        }
    }

    public static String categoryKey(String category) {
        return "book.techguns." + BOOK + "." + category;
    }

    public static String entryKey(Entry entry) {
        return categoryKey(entry.category()) + "." + entry.path();
    }

    public static String pageKey(Entry entry, int page, String field) {
        return entryKey(entry) + ".page" + page + "." + field;
    }

    /** {@code text} of a JSON page, or null if it has none (Techguns 2 left some empty). */
    public static String string(JsonObject page, String key) {
        return page.has(key) && !page.get(key).getAsString().isBlank() ? page.get(key).getAsString() : null;
    }

    public static List<String> strings(JsonArray array) {
        List<String> out = new ArrayList<>();
        array.forEach(e -> out.add(e.getAsString()));
        return out;
    }

    private static JsonObject parse(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
