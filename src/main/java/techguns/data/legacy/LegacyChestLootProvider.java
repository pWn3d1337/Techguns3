package techguns.data.legacy;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import techguns.TGIds;
import techguns.Techguns;

/**
 * Converts the Techguns 2 chest loot tables ({@code src/legacy/loot/chests}, copied from Techguns 2) to
 * {@code techguns:chests/*}: same pools, rolls, weights and counts; items through {@link LegacyItems}. Entries whose
 * item is not ported yet stay as empty entries so the odds of the others don't change.
 */
public class LegacyChestLootProvider implements LootTableSubProvider {
    /** Techguns 2 nested crate block tables to the Techguns 3 crate contents tables ({@code techguns:crates/*}). */
    private static final Map<String, String> NESTED = Map.of(
            "techguns:blocks/military_crate_ammo", "techguns:crates/military_crate_ammo",
            "techguns:blocks/military_crate_armor", "techguns:crates/military_crate_armor",
            "techguns:blocks/military_crate_explosives", "techguns:crates/military_crate_explosive",
            "techguns:blocks/military_crate_generic", "techguns:crates/military_crate_generic_oak",
            "techguns:blocks/military_crate_gun", "techguns:crates/military_crate_gun",
            "techguns:blocks/military_crate_medical", "techguns:crates/military_crate_medical");

    @SuppressWarnings("unused")
    public LegacyChestLootProvider(HolderLookup.Provider registries) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        Path dir = LegacyFiles.dir().map(d -> d.resolve("loot/chests")).orElse(null);
        if (dir == null) {
            return;
        }
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.sorted().toList()) {
                String name = file.getFileName().toString().replace(".json", "");
                try (Reader reader = Files.newBufferedReader(file)) {
                    output.accept(ResourceKey.create(Registries.LOOT_TABLE, TGIds.id("chests/" + name)), table(JsonParser.parseReader(reader).getAsJsonObject()));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the legacy loot tables in " + dir, e);
        }
    }

    private static LootTable.Builder table(JsonObject json) {
        LootTable.Builder table = LootTable.lootTable();
        for (JsonElement poolJson : json.getAsJsonArray("pools")) {
            JsonObject pool = poolJson.getAsJsonObject();
            LootPool.Builder builder = LootPool.lootPool().setRolls(number(pool.get("rolls")));
            if (pool.has("bonus_rolls")) {
                builder.setBonusRolls(number(pool.get("bonus_rolls")));
            }
            for (JsonElement entryJson : pool.getAsJsonArray("entries")) {
                builder.add(entry(entryJson.getAsJsonObject()));
            }
            table.withPool(builder);
        }
        return table;
    }

    private static LootPoolEntryContainer.Builder<?> entry(JsonObject json) {
        int weight = json.has("weight") ? json.get("weight").getAsInt() : 1;
        String name = json.get("name").getAsString();
        if (json.get("type").getAsString().equals("loot_table")) {
            String nested = NESTED.get(name);
            if (nested == null) {
                throw new IllegalArgumentException("No mapping for legacy loot table " + name);
            }
            int colon = nested.indexOf(':');
            return NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,
                    TGIds.of(nested.substring(0, colon), nested.substring(colon + 1)))).setWeight(weight);
        }
        int meta = 0;
        NumberProvider count = null;
        for (JsonElement functionJson : json.has("functions") ? json.getAsJsonArray("functions").asList() : List.<JsonElement>of()) {
            JsonObject function = functionJson.getAsJsonObject();
            switch (function.get("function").getAsString()) {
                case "set_data" -> meta = function.get("data").getAsInt();
                case "set_count" -> count = number(function.get("count"));
                default -> throw new IllegalArgumentException("Unsupported legacy loot function " + function);
            }
        }
        Item item = LegacyItems.item(name, meta);
        if (item == null) {
            Techguns.LOGGER.warn("Legacy loot: {}@{} is not ported, kept as an empty entry", name, meta);
            return EmptyLootItem.emptyItem().setWeight(weight);
        }
        LootItem.Builder<?> builder = LootItem.lootTableItem(item).setWeight(weight);
        if (count != null) {
            builder.apply(SetItemCountFunction.setCount(count));
        }
        return builder;
    }

    private static NumberProvider number(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return ConstantValue.exactly(json.getAsFloat());
        }
        JsonObject range = json.getAsJsonObject();
        return UniformGenerator.between(range.get("min").getAsFloat(), range.get("max").getAsFloat());
    }
}
