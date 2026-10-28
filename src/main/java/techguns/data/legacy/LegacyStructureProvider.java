package techguns.data.legacy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import techguns.TGIds;
import techguns.world.structure.dungeon.DungeonPreset;
import techguns.world.structure.dungeon.SegmentType;
import techguns.block.Door3x3Block;
import techguns.world.LegacyPaletteProcessor;

/**
 * Converts the Techguns 2 scanned structures to structure templates ({@code data/techguns/structure/*.nbt}). Input is
 * {@code src/legacy/structure_dump.json}: per structure class the scanned blocks ({@code x, y, z, palette index}) and
 * the palette ({@code MBlock}s with all fields), dumped from the Techguns 2 runtime by
 * {@code src/legacy/tools/StructureDump.java.txt}.
 * <p>
 * Vanilla block states go through the vanilla 1.12 data fixer, Techguns ones through {@link LegacyBlockStates}.
 * Chests get their loot table, spawners their mobs; random, biome and per-structure palette entries keep all their
 * options as NBT for {@link LegacyPaletteProcessor}. Positions the scan doesn't list stay untouched, scanned air
 * clears the terrain.
 */
public class LegacyStructureProvider implements DataProvider {
    /** Minecraft 1.12.2 data version. */
    private static final int DATA_VERSION_1_12 = 1343;
    /** Techguns 2 structure class to template name. */
    private static final Map<String, String> STRUCTURES = new LinkedHashMap<>();

    static {
        String[][] names = {
                {"FactoryHouseSmall", "factory_house_small"}, {"SmallTrainstation", "small_trainstation"}, {"SmallMine", "small_mine"},
                {"GasStation", "gas_station"}, {"PoliceStation", "police_station"}, {"SurvivorHideout", "survivor_hideout"},
                {"OreClusterSpike", "ore_cluster_spike"}, {"OreClusterMeteorBasis", "ore_cluster_meteor_basis"},
                {"DesertOilCluster", "desert_oil_cluster"}, {"AircraftCarrier", "aircraft_carrier"},
                {"NetherAltarSmall", "nether_altar_small"}, {"NetherAltarMedium", "nether_altar_medium"},
                {"NetherSoulPlatform", "nether_soul_platform"}, {"NetherLoot01", "nether_loot_01"}, {"NetherAcidHole", "nether_acid_hole"},
                {"NetherOreClusterSmall", "nether_ore_cluster_small"}, {"NetherOreClusterCastle", "nether_ore_cluster_castle"},
                {"NetherGhastSpawner", "nether_ghast_spawner"}, {"NetherDungeonEntrance", "nether_dungeon_entrance"}};
        for (String[] name : names) {
            STRUCTURES.put("techguns.world.structures." + name[0], name[1]);
        }
    }

    /** Techguns 2 entity ids to Techguns 3. */
    private static final Map<String, String> ENTITIES = Map.ofEntries(
            Map.entry("techguns:armysoldier", "techguns:army_soldier"), Map.entry("techguns:commando", "techguns:commando"),
            Map.entry("techguns:zombiesoldier", "techguns:zombie_soldier"), Map.entry("techguns:zombiefarmer", "techguns:zombie_farmer"),
            Map.entry("techguns:zombieminer", "techguns:zombie_miner"), Map.entry("techguns:zombiepoliceman", "techguns:zombie_policeman"),
            Map.entry("techguns:zombiepigmansoldier", "techguns:zombie_pigman_soldier"), Map.entry("techguns:cyberdemon", "techguns:cyber_demon"),
            Map.entry("techguns:ghastling", "techguns:ghastling"), Map.entry("techguns:alienbug", "techguns:alien_bug"),
            Map.entry("techguns:bandit", "techguns:bandit"), Map.entry("techguns:attackhelicopter", "techguns:attack_helicopter"));

    private final PackOutput.PathProvider structures;

    public LegacyStructureProvider(PackOutput output) {
        this.structures = output.createPathProvider(PackOutput.Target.DATA_PACK, "structure");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Path dir = LegacyFiles.dir().orElse(null);
        if (dir == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            JsonObject dump;
            try (Reader reader = Files.newBufferedReader(dir.resolve("structure_dump.json"))) {
                dump = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException e) {
                throw new IllegalStateException("Could not read the structure dump", e);
            }
            STRUCTURES.forEach((className, name) -> {
                CompoundTag template = new Converter(dump.getAsJsonObject(className)).convert();
                write(output, structures.file(TGIds.id(name), "nbt"), template);
            });
            JsonObject dungeons;
            try (Reader reader = Files.newBufferedReader(dir.resolve("dungeon_dump.json"))) {
                dungeons = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException e) {
                throw new IllegalStateException("Could not read the dungeon dump", e);
            }
            // maze dungeon segments: dungeon/<template>/<segment>, the same palette and block format
            for (String name : java.util.Arrays.stream(DungeonPreset.values()).flatMap(p -> p.templates.stream()).toList()) {
                JsonObject segments = dungeons.getAsJsonObject(name).getAsJsonObject("segments");
                for (SegmentType type : SegmentType.values()) {
                    CompoundTag template = new Converter(segments.getAsJsonObject(type.name())).convert();
                    write(output, structures.file(TGIds.id("dungeon/" + name + "/" + type.fileName()), "nbt"), template);
                }
            }
        });
    }

    private static void write(CachedOutput output, Path path, CompoundTag tag) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, bytes);
            byte[] data = bytes.toByteArray();
            output.writeIfNeeded(path, data, Hashing.sha1().hashBytes(data));
        } catch (IOException e) {
            throw new IllegalStateException("Could not write " + path, e);
        }
    }

    @Override
    public String getName() {
        return "Techguns 2 structures";
    }

    /** One structure: legacy palette entries to fallback states, block entity data and processor options. */
    private static final class Converter {
        private final JsonArray palette;
        private final JsonArray blocks;
        /** Upgraded state per 1.12 state string. */
        private final Map<String, BlockState> states = new HashMap<>();
        /** The roll weights of the structure: the first index roll entry's weights, shared by all its rolls. */
        @Nullable
        private int[] rollWeights;

        Converter(JsonObject structure) {
            this.palette = structure.getAsJsonArray("palette:blockList");
            this.blocks = structure.getAsJsonArray("blocks:blocks");
        }

        CompoundTag convert() {
            Set<String> legacyStates = new java.util.LinkedHashSet<>();
            for (JsonElement entry : palette) {
                collectStates(entry.getAsJsonObject(), legacyStates);
            }
            upgrade(legacyStates);

            List<Entry> entries = new ArrayList<>();
            for (JsonElement entry : palette) {
                entries.add(entry(entry.getAsJsonObject()));
            }
            Map<BlockPos, Entry> placed = new LinkedHashMap<>();
            int sizeX = 0, sizeY = 0, sizeZ = 0;
            for (JsonElement blockJson : blocks) {
                JsonArray block = blockJson.getAsJsonArray();
                BlockPos pos = new BlockPos(block.get(0).getAsInt(), block.get(1).getAsInt(), block.get(2).getAsInt());
                placed.put(pos, entries.get(block.get(3).getAsInt()));
                sizeX = Math.max(sizeX, pos.getX() + 1);
                sizeY = Math.max(sizeY, pos.getY() + 1);
                sizeZ = Math.max(sizeZ, pos.getZ() + 1);
            }
            fixDoors3x3(placed);
            return template(placed, sizeX, sizeY, sizeZ);
        }

        // --- states ---

        private void collectStates(JsonObject entry, Set<String> out) {
            out.add(entry.get("state").getAsString());
            for (Map.Entry<String, JsonElement> field : entry.entrySet()) {
                JsonElement value = field.getValue();
                if (field.getKey().equals("states") && value.isJsonArray()) {
                    value.getAsJsonArray().forEach(s -> out.add(s.getAsString()));
                } else if (value.isJsonObject() && value.getAsJsonObject().has("state")) {
                    collectStates(value.getAsJsonObject(), out);
                } else if (value.isJsonArray()) {
                    for (JsonElement e : value.getAsJsonArray()) {
                        if (e.isJsonObject() && e.getAsJsonObject().has("state")) {
                            collectStates(e.getAsJsonObject(), out);
                        }
                    }
                }
            }
        }

        /** Runs all 1.12 states through the vanilla structure data fixer at once; Techguns states are mapped instead. */
        private void upgrade(Set<String> legacyStates) {
            List<String> ordered = new ArrayList<>(legacyStates);
            ListTag legacyPalette = new ListTag();
            for (String state : ordered) {
                legacyPalette.add(parseLegacy(state));
            }
            CompoundTag legacy = new CompoundTag();
            legacy.put("palette", legacyPalette);
            legacy.put("blocks", new ListTag());
            legacy.put("entities", new ListTag());
            legacy.put("size", intList(1, 1, 1));
            legacy.putInt("DataVersion", DATA_VERSION_1_12);
            CompoundTag upgraded = DataFixTypes.STRUCTURE.updateToCurrentVersion(DataFixers.getDataFixer(), legacy, DATA_VERSION_1_12);
            ListTag upgradedPalette = upgraded.getList("palette", ListTag.TAG_COMPOUND);
            for (int i = 0; i < ordered.size(); i++) {
                CompoundTag original = legacyPalette.getCompound(i);
                BlockState state;
                if (LegacyBlockStates.isLegacyTechguns(original)) {
                    state = LegacyBlockStates.map(original);
                } else {
                    if (upgradedPalette.getCompound(i).getString("Name").contains("%%")) {
                        throw new IllegalStateException("The data fixer has no 1.13 state for " + ordered.get(i));
                    }
                    state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), upgradedPalette.getCompound(i));
                    if (state.isAir() && !ordered.get(i).equals("minecraft:air")) {
                        throw new IllegalStateException("The data fixer turned " + ordered.get(i) + " into air");
                    }
                }
                states.put(ordered.get(i), state);
            }
        }

        /** {@code minecraft:stone_slab[half=bottom,variant=stone]} to a 1.12 palette tag. */
        private static CompoundTag parseLegacy(String state) {
            CompoundTag tag = new CompoundTag();
            int bracket = state.indexOf('[');
            tag.putString("Name", bracket < 0 ? state : state.substring(0, bracket));
            if (bracket >= 0) {
                CompoundTag properties = new CompoundTag();
                for (String pair : state.substring(bracket + 1, state.length() - 1).split(",")) {
                    String[] kv = pair.split("=");
                    properties.putString(kv[0], kv[1]);
                }
                tag.put("Properties", properties);
            }
            return tag;
        }

        // --- palette entries ---

        /** A palette entry: the state placed without the processor, block entity data and the processor's options. */
        private record Entry(BlockState state, @Nullable CompoundTag blockEntity, @Nullable CompoundTag options) {
        }

        private Entry entry(JsonObject json) {
            BlockState state = states.get(json.get("state").getAsString());
            String type = json.get("type").getAsString();
            type = type.substring(type.lastIndexOf('.') + 1);
            return switch (type) {
                case "MBlockChestLoottable" -> new Entry(state, chest(json), null);
                case "MBlockTGSpawner" -> new Entry(state, tgSpawner(json), null);
                case "MBlockVanillaSpawner" -> new Entry(state, vanillaSpawner(json), null);
                case "MBlock", "AlienBugNest$MBlockSlime", "MBlockSkullBlock" -> new Entry(state, null, null);
                default -> new Entry(state, null, options(json));
            };
        }

        /** The processor option tree of a palette entry. */
        private CompoundTag options(JsonObject json) {
            String type = json.get("type").getAsString();
            type = type.substring(type.lastIndexOf('.') + 1);
            CompoundTag option = new CompoundTag();
            switch (type) {
                case "MBlock", "AlienBugNest$MBlockSlime", "MBlockSkullBlock" -> option.put(LegacyPaletteProcessor.STATE, NbtUtils.writeBlockState(states.get(json.get("state").getAsString())));
                case "MBlockChestLoottable" -> {
                    option.put(LegacyPaletteProcessor.STATE, NbtUtils.writeBlockState(states.get(json.get("state").getAsString())));
                    option.put(LegacyPaletteProcessor.BLOCK_ENTITY, chest(json));
                }
                case "MBlockTGSpawner" -> {
                    option.put(LegacyPaletteProcessor.STATE, NbtUtils.writeBlockState(states.get(json.get("state").getAsString())));
                    option.put(LegacyPaletteProcessor.BLOCK_ENTITY, tgSpawner(json));
                }
                case "MultiMBlock" -> option.put(LegacyPaletteProcessor.WEIGHTED, weighted(stateOptions(json.getAsJsonArray("states")), ints(json.getAsJsonArray("weights"))));
                case "MultiMMBlock" -> option.put(LegacyPaletteProcessor.WEIGHTED, weighted(childOptions(json.getAsJsonArray("mBlocks")), ints(json.getAsJsonArray("weights"))));
                case "MBlockBiomeColorType" -> option.put(LegacyPaletteProcessor.BIOME, list(stateOptions(json.getAsJsonArray("states"))));
                case "MBlockCamoNetTop" -> option.put(LegacyPaletteProcessor.BIOME, list(List.of(
                        stateOption("techguns:camonet_top[connection=none,type=wood]"), stateOption("techguns:camonet_top[connection=none,type=snow]"),
                        stateOption("techguns:camonet_top[connection=none,type=desert]"), stateOption("techguns:camonet_top[connection=none,type=wood]"))));
                case "MultiMMBlockIndexRoll" -> roll(option, json);
                case "MBlockOreclusterType" -> {
                    roll(option, json);
                    // Techguns 2: ores instead of the cluster block only when the entry has ores
                    if (json.has("oreMBlocks")) {
                        option.put(LegacyPaletteProcessor.ALT_PER_INDEX, list(childOptions(json.getAsJsonArray("oreMBlocks"))));
                        option.putFloat(LegacyPaletteProcessor.ALT_CHANCE, json.get("oreChance").getAsFloat());
                    }
                }
                case "MBlockOreClusterTypeOre" -> {
                    roll(option, json);
                    option.put(LegacyPaletteProcessor.ALT, options(json.getAsJsonObject("stone")));
                    option.putFloat(LegacyPaletteProcessor.ALT_CHANCE, json.get("stonechance").getAsFloat());
                }
                default -> throw new IllegalArgumentException("Unsupported legacy palette entry " + type);
            }
            return option;
        }

        private void roll(CompoundTag option, JsonObject json) {
            if (rollWeights == null) {
                rollWeights = ints(json.getAsJsonArray("weights"));
            }
            option.put(LegacyPaletteProcessor.ROLL, list(childOptions(json.getAsJsonArray("mBlocks"))));
            option.putIntArray(LegacyPaletteProcessor.ROLL_WEIGHTS, rollWeights);
        }

        private List<CompoundTag> childOptions(JsonArray children) {
            List<CompoundTag> options = new ArrayList<>();
            children.forEach(child -> options.add(options(child.getAsJsonObject())));
            return options;
        }

        private List<CompoundTag> stateOptions(JsonArray legacyStates) {
            List<CompoundTag> options = new ArrayList<>();
            legacyStates.forEach(s -> options.add(stateOption(s.getAsString())));
            return options;
        }

        private CompoundTag stateOption(String legacyState) {
            BlockState state = states.computeIfAbsent(legacyState, s -> LegacyBlockStates.map(parseLegacy(s)));
            CompoundTag option = new CompoundTag();
            option.put(LegacyPaletteProcessor.STATE, NbtUtils.writeBlockState(state));
            return option;
        }

        private static ListTag weighted(List<CompoundTag> options, int[] weights) {
            ListTag list = new ListTag();
            for (int i = 0; i < options.size(); i++) {
                CompoundTag weighted = new CompoundTag();
                weighted.putInt(LegacyPaletteProcessor.WEIGHT, weights[i]);
                weighted.put(LegacyPaletteProcessor.OPTION, options.get(i));
                list.add(weighted);
            }
            return list;
        }

        private static ListTag list(List<CompoundTag> options) {
            ListTag list = new ListTag();
            list.addAll(options);
            return list;
        }

        private static int[] ints(JsonArray array) {
            int[] values = new int[array.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = array.get(i).getAsInt();
            }
            return values;
        }

        // --- block entities ---

        private static CompoundTag chest(JsonObject json) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", "minecraft:chest");
            // the Techguns 2 chest tables keep their names (techguns:chests/*, LegacyChestLootProvider)
            tag.putString("LootTable", json.get("loottable").getAsString());
            return tag;
        }

        private static CompoundTag vanillaSpawner(JsonObject json) {
            CompoundTag entity = new CompoundTag();
            entity.putString("id", entity(json.get("entityId").getAsString()));
            CompoundTag spawnData = new CompoundTag();
            spawnData.put("entity", entity);
            CompoundTag tag = new CompoundTag();
            tag.putString("id", "minecraft:mob_spawner");
            tag.put("SpawnData", spawnData);
            return tag;
        }

        /** {@link techguns.entity.spawn.TGSpawnerBlockEntity} data. */
        private static CompoundTag tgSpawner(JsonObject json) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", "techguns:tg_spawner");
            tag.putInt("mobsLeft", json.get("mobsleft").getAsInt());
            tag.putInt("maxActive", json.get("maxActive").getAsInt());
            tag.putInt("spawnDelay", json.get("spawndelay").getAsInt());
            tag.putInt("delay", json.get("spawndelay").getAsInt());
            tag.putInt("spawnRange", json.get("spawnrange").getAsInt());
            ListTag mobTypes = new ListTag();
            JsonArray classes = json.getAsJsonArray("classes");
            JsonArray weights = json.getAsJsonArray("weights");
            for (int i = 0; i < classes.size(); i++) {
                CompoundTag mob = new CompoundTag();
                mob.putString("id", entity(classes.get(i).getAsString().replace("entity:", "")));
                mob.putInt("weight", weights.get(i).getAsInt());
                mobTypes.add(mob);
            }
            tag.put("mobTypes", mobTypes);
            String weapon = json.get("weaponOverride").getAsString();
            if (!weapon.equals("empty")) {
                // Techguns 2 item@meta x count
                String name = weapon.substring(0, weapon.indexOf('@'));
                CompoundTag stack = new CompoundTag();
                stack.putString("id", BuiltInRegistries.ITEM.getKey(java.util.Objects.requireNonNull(LegacyItems.item(name, 0), weapon)).toString());
                stack.putInt("count", 1);
                tag.put("weapon", stack);
            }
            return tag;
        }

        private static String entity(String legacy) {
            String id = ENTITIES.get(legacy);
            if (id == null) {
                throw new IllegalArgumentException("No mapping for legacy entity " + legacy);
            }
            return id;
        }

        // --- doors and output ---

        /**
         * Techguns 2 3x3 doors marked only their master block; Techguns 3 stores column and row in every part. Each
         * connected group of door blocks is one door: its parts get their place in the 3 x 3 grid.
         */
        private static void fixDoors3x3(Map<BlockPos, Entry> placed) {
            Set<BlockPos> done = new HashSet<>();
            for (BlockPos start : List.copyOf(placed.keySet())) {
                Entry entry = placed.get(start);
                if (!(entry.state().getBlock() instanceof Door3x3Block door) || !done.add(start)) {
                    continue;
                }
                List<BlockPos> parts = new ArrayList<>();
                ArrayDeque<BlockPos> queue = new ArrayDeque<>(List.of(start));
                while (!queue.isEmpty()) {
                    BlockPos pos = queue.poll();
                    parts.add(pos);
                    for (Direction direction : Direction.values()) {
                        BlockPos next = pos.relative(direction);
                        Entry other = placed.get(next);
                        if (other != null && other.state().getBlock() == door && done.add(next)) {
                            queue.add(next);
                        }
                    }
                }
                boolean zplane = entry.state().getValue(Door3x3Block.ZPLANE);
                int minAlong = parts.stream().mapToInt(p -> zplane ? p.getZ() : p.getX()).min().orElse(0);
                int minY = parts.stream().mapToInt(BlockPos::getY).min().orElse(0);
                for (BlockPos pos : parts) {
                    int col = (zplane ? pos.getZ() : pos.getX()) - minAlong;
                    int row = pos.getY() - minY;
                    placed.put(pos, new Entry(door.partState(zplane, Math.min(col, 2), Math.min(row, 2)), null, null));
                }
            }
        }

        private static CompoundTag template(Map<BlockPos, Entry> placed, int sizeX, int sizeY, int sizeZ) {
            List<BlockState> paletteStates = new ArrayList<>();
            Map<BlockState, Integer> index = new HashMap<>();
            ListTag blockList = new ListTag();
            for (Map.Entry<BlockPos, Entry> e : placed.entrySet()) {
                Entry entry = e.getValue();
                BlockState state = entry.state() == null ? Blocks.AIR.defaultBlockState() : entry.state();
                int i = index.computeIfAbsent(state, s -> {
                    paletteStates.add(s);
                    return paletteStates.size() - 1;
                });
                CompoundTag block = new CompoundTag();
                block.put("pos", intList(e.getKey().getX(), e.getKey().getY(), e.getKey().getZ()));
                block.putInt("state", i);
                CompoundTag nbt = entry.blockEntity() == null ? null : entry.blockEntity().copy();
                if (entry.options() != null) {
                    nbt = nbt == null ? new CompoundTag() : nbt;
                    nbt.put(LegacyPaletteProcessor.KEY, entry.options());
                }
                if (nbt != null) {
                    block.put("nbt", nbt);
                }
                blockList.add(block);
            }
            ListTag palette = new ListTag();
            paletteStates.forEach(s -> palette.add(NbtUtils.writeBlockState(s)));
            CompoundTag tag = new CompoundTag();
            tag.put("size", intList(sizeX, sizeY, sizeZ));
            tag.put("palette", palette);
            tag.put("blocks", blockList);
            tag.put("entities", new ListTag());
            tag.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            // round trip through the template: validates and normalizes (sorted blocks)
            StructureTemplate structure = new StructureTemplate();
            structure.load(BuiltInRegistries.BLOCK.asLookup(), tag);
            return structure.save(new CompoundTag());
        }

        private static ListTag intList(int... values) {
            ListTag list = new ListTag();
            for (int v : values) {
                list.add(IntTag.valueOf(v));
            }
            return list;
        }
    }
}
