package techguns.data.legacy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import techguns.Techguns;
import techguns.data.TGLanguageProvider;

/**
 * The Techguns 2 translations ({@code src/legacy/lang/<locale>.lang}) carried over to the Techguns 3 keys: a text
 * gets the translation of the Techguns 2 key whose English text is the same (keys were renamed, the texts mostly
 * kept); where several Techguns 2 keys share the text, the most common translation. Texts that are new or changed stay
 * English (the game falls back to {@code en_us}), and so do translations whose format arguments differ.
 */
public class LegacyLanguageProvider extends LanguageProvider {
    /** The languages Techguns 2 shipped besides English. */
    public static final List<String> LOCALES = List.of("de_de", "ru_ru");

    private static final Pattern PLACEHOLDER = Pattern.compile("%(\\d+\\$)?[sdf%]");

    private final PackOutput output;
    private final String locale;

    public LegacyLanguageProvider(PackOutput output, String locale) {
        super(output, Techguns.MODID, locale);
        this.output = output;
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        Path dir = LegacyFiles.dir().map(d -> d.resolve("lang")).orElse(null);
        if (dir == null) {
            return;
        }
        Map<String, String> legacyEnglish = read(dir.resolve("en_us.lang"));
        Map<String, String> legacy = read(dir.resolve(locale + ".lang"));
        // English text -> translation -> how many Techguns 2 keys use it (keys sorted, so ties go to the first key)
        Map<String, Map<String, Integer>> byText = new HashMap<>();
        new TreeMap<>(legacyEnglish).forEach((key, text) -> {
            String translation = legacy.get(key);
            if (translation != null && !translation.isBlank()) {
                byText.computeIfAbsent(text.strip(), t -> new java.util.LinkedHashMap<>()).merge(translation, 1, Integer::sum);
            }
        });
        TGLanguageProvider.english(output).forEach((key, text) -> {
            Map<String, Integer> translations = byText.get(text.strip());
            if (translations != null) {
                String best = null;
                int count = 0;
                for (Map.Entry<String, Integer> t : translations.entrySet()) {
                    if (t.getValue() > count) {
                        best = t.getKey();
                        count = t.getValue();
                    }
                }
                // a translation with other format arguments would break the text
                if (placeholders(best).equals(placeholders(text))) {
                    add(key, best);
                }
            }
        });
    }

    private static List<String> placeholders(String text) {
        return PLACEHOLDER.matcher(text).results().map(m -> m.group()).sorted().toList();
    }

    /** A Techguns 2 {@code .lang} file: {@code key=text} lines, {@code #} comments. */
    private static Map<String, String> read(Path file) {
        Map<String, String> entries = new HashMap<>();
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                int eq = line.indexOf('=');
                if (line.isBlank() || line.startsWith("#") || eq < 0) {
                    continue;
                }
                entries.put(line.substring(0, eq).strip(), line.substring(eq + 1));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return entries;
    }
}
