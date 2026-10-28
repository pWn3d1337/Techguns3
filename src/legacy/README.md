# Techguns 2 conversion inputs

Data taken from Techguns 2 (1.12.2) that the data generator converts (`./gradlew runData`; the `data` run passes this
folder as the system property `techguns.legacyDir`). Not shipped in the mod jar.

| File | Content | Converted by |
|---|---|---|
| `structure_dump.json` | Per Techguns 2 structure class (`techguns.world.structures.*`): the scanned blocks (`x, y, z, palette index`) and the palette (`MBlock`s with all fields evaluated: block state strings, random variants, loot tables, spawner settings); plus the `itemshared` meta to translation key list. | `techguns.data.legacy.LegacyStructureProvider` → `data/techguns/structure/*.nbt` |
| `loot/chests/*.json` | The Techguns 2 chest loot tables, copied unchanged from `assets/techguns/loot_tables/chests`. | `techguns.data.legacy.LegacyChestLootProvider` → `data/techguns/loot_table/chests/*.json` |
| `dungeon_dump.json` | Per Techguns 2 maze dungeon template (the `.ser` files in `assets/techguns/dungeons`, deserialized, with the presets' special blocks applied): cell size and per segment type the size, palette and blocks (`x, y, z, palette index, layer`). | `techguns.data.legacy.LegacyStructureProvider` → `data/techguns/structure/dungeon/<template>/<segment>.nbt` (castle templates) |
| `role_dump.json` | Per Techguns 2 structure class: the palette entries that are `MBlockRegister` fields (by identity), i.e. the blocks Techguns 2 swapped for Chisel blocks when Chisel was installed. | `LegacyStructureProvider` (marks them with a `decor` option, see `techguns.compat.decor.DecorRole`) |
| `ctm/*-ctm.png` | The Techguns 2 CTM sheets (connected textures, CTM mod format), copied from `assets/techguns/textures/blocks`. | `LegacyConnectedTextureProvider` → `resourcepacks/fusion` (Fusion sheets) |
| `tools/StructureDump.java.txt` | The dump tool that produced `structure_dump.json`. | — |
| `tools/RoleDump.java.txt` | The dump tool that produced `role_dump.json` (same steps, writes `run/role_dump.json`, logs `TG ROLE DUMP DONE`). | — |
| `tools/DungeonDump.java.txt` | The dump tool that produced `dungeon_dump.json`. | — |

## Re-creating the dump

1. Copy `tools/StructureDump.java.txt` to `src/main/java/techguns/devdump/StructureDump.java` in the Techguns 2
   workspace (`F:\Techguns3Work\Techguns2`).
2. Run `./gradlew runClient` there; when the title screen opens, the tool writes `run/structure_dump.json` and logs
   `TG STRUCTURE DUMP DONE`.
3. Close the client, delete the tool again, and copy the file here (without the `MBlockRegister` entry, which only holds
   shared constants).

`dungeon_dump.json` the same way with `tools/DungeonDump.java.txt` (`techguns/devdump/DungeonDump.java`, writes
`run/dungeon_dump.json`, logs `TG DUNGEON DUMP DONE`).

Only the scanned structures are in the dump. The procedural ones (barracks, bunker, castle, military base, helipad,
tanks, ...) are code in Techguns 2 and are ported separately (Phase 7e); the maze dungeon generator is ported in 7f.
