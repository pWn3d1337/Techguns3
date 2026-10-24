# Port status

Tracks every Techguns content entry through the port. Status values:
`—` not started · `logic` · `rendered` · `tested` · `done`.
Sources: **F** = Fabric 1.19.4 port, **12** = Techguns 2 (1.12.2).

## Phases

| Phase | Status |
|---|---|
| 0 — Project setup | done |
| 1 — Core infrastructure | done (machine-only recipes follow in Phase 5) |
| 2 — Gun framework | — |
| 3 — All guns, ammo, grenades, tools | — |
| 4 — Armor & player gear | — |
| 5 — Blocks & machines | — |
| 6 — NPCs & factions | — |
| 7 — World generation | — |
| 8 — Remaining systems & integrations | — |

## Content

Per-item tables (guns, ammo, armor, blocks, machines, NPCs, structures) are added at the start of the phase that ports them.

### Phase 1 — core systems

| Unit | Source | Status | Notes |
|---|---|---|---|
| Damage system (types, category tags, armor formula, penetration, bonus armor attributes, PvP/NPC factors) | 12 + F | tested | JUnit + GameTests; knockback multiplier and death effects follow with projectiles (Phase 2) |
| Sound events (170) + sounds.json + .ogg | F (+12 for blocks/items/machines/npcs) | done | |
| Key mappings (force reload, safe mode, step assist) | F | logic | handlers come with guns / armor |
| Data component & attachment registries | — | done | empty, filled by later phases |
| Creative tab | — | done | |

### Phase 1 — materials, components, ores

| Group | Items | Status | Notes |
|---|---|---|---|
| Ores (tin, lead, titanium, uranium; stone + deepslate) | 8 blocks | rendered | no worldgen yet (Phase 7); copper ore dropped (vanilla copper) |
| Raw materials, ingots, nuggets | 16 | rendered | raw_steel / raw_obsidian_steel are interim Fabric recipes until the blast furnace (Phase 5) |
| Plates | 9 | rendered | **no recipe yet** — metal press (Phase 5) |
| Other materials (rubber, plastic, wires, TGX, …) | 14 | rendered | plastic_sheet, raw_plastic, yellowcake, enriched_uranium, quartz_rod, bronze_ingot: machine recipes (Phase 5) |
| Components (receivers, barrels, stocks, circuits, emitters, …) | 37 | rendered | recipes ported from Fabric; titanium_receiver, gauss/shielded barrels, steam_armor_plate, plasma_generator, antigrav_core, protective_fiber, heat_ray_focus, uv_emitter wait for machines |
