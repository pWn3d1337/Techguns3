# Session handoff (2026-10-27)

State of the AI-assisted porting session, so it can continue on another PC (same `F:` mapping).
Read this together with [CLAUDE.md](../CLAUDE.md), [Techguns3_design.md](../Techguns3_design.md) (plan, decisions) and
[port_status.md](port_status.md) (what is done, per batch, with notes on what was not ported).

## Where we are

- Branch: `phase6-npcs` (base `master`). Phase 5 (PR pWn3d1337/Techguns3#6) is merged into `master`.
- Phase 5 progress (one commit per batch):

  | Batch | Content | Commit |
  |---|---|---|
  | 5a1 | Decorative cube blocks | `3778bd8` |
  | 5a2 | Shaped blocks (lamps, ladders, nets, sandbags, crates, doors) + GameTest target fixes | `ebf2f80` |
  | fix | Extra slot modifier map per side (Phase 4 crash: CME with client + integrated server thread) | `100afd2` |
  | 5b1 | Machine framework + Metal Press + machine upgrade item | `0e7122b` |
  | 5b2a | Ammo Press, Blast Furnace, Grinder | `ca87676` |
  | 5b2b | Chem Lab (fluid tanks), Charging Station, creeper acid fluid | `c9a082e` |
  | 5b3 | Repair, Camo, Upgrade and Ammo Bench, armor upgrades, Grinder armor recycling | `7341f36` |
  | 5c1 | Multiblock framework + Fabricator, Fabric fallback recipes removed | `af3babb` |
  | 5c2 | Reaction Chamber | `2cf2c17` |
  | 5c3 | Ore Drill + ore clusters | `3c01ee6` |
  | 5d | Explosive charges, turrets (camp flag dropped: no 1.12 implementation) | `a8726f5` |
  | 5e | JEI plugin (optional) | `c1d0c5d` (Phase 5 merged, PR #6) |
  | 6a | NPC framework + undead NPCs | `04a9f8b` |
  | 6b | Human NPCs | `32eabb9` |
  | 6c | Super mutants | `200da58` |
  | 6d | Nether and bosses: cyber demon, ghastling, attack helicopter | `60bc107` |
  | 6e | Alien bugs | not committed yet |

- All 268 GameTests pass; each batch was checked in game via MCP.

## Next steps

1. Commit 6e (when the user asks), then 6f natural spawning and the TG spawner (plan in the design doc, Phase 6 batches). The alien bug nest structure belongs to Phase 7.
2. Later: EMI plugin, JEI views of the special crafting recipes.
3. Open the Phase 6 PR when 6f is done (or earlier if the user asks).

## Open decision for the user

- None.

## Working rule

- The user works on other assets (e.g. the ice shard rifle in `art/`, `geo/item/`, `textures/guns/`) in the same tree: stage batch files explicitly, never `git add -A`.

## Decisions made in this session (also in the design doc)

- Fabric fallback crafting recipes for machine-made items removed; Techguns 2 recipes only (laser focus follows with the Reaction Chamber).
- Raw steel / raw obsidian steel ingots (Fabric interim) removed: steel and obsidian steel only from the Blast Furnace (2026-10-27).
- Q6: port **both** Ammo Press (1.12, powered, build plans) and the Fabric Ammo Bench (unpowered).
- Camo Bench: 1.12 scope with a Fabric-style named camo list.
- Reference priority: Fabric port only for gun mechanics/effects; Techguns 2 (1.12) for everything else.
- 5b was split into 5b1 (framework + Metal Press), 5b2a (item machines), 5b2b (fluid machines), 5b3 (benches).

## Machine framework (package `techguns.machine`), for the next batches

- `TGMachineBlockEntity`: inventory with slot rules (`canPlace`, `isOutputSlot`, `upgradeSlot`), insert-only FE buffer
  (`MachineEnergy`), redstone mode, owner + owner-only security, one `MachineOperation` (outputs, multiplier, time,
  power per tick) with progress; hooks `checkAndStartOperation`, `finishOperation`, `idleTick` (server, no job),
  `clientIdleTick`, `playWorkSound` (client), `useItem`/`canUseItem` (held item on the block, e.g. buckets),
  `keepOperation` (cancel a running job, Ore Drill head change), `pressMachineButton` (menu buttons), extra synced data (`extraDataCount`/`getExtraData`); helpers `has`, `consume`,
  `canOutput`, `deliver`, `canInsertAll`/`insertAll`, `maxStackMultiplier`.
- `TGMachineBlock`: facing (renderer-drawn machines store the look direction, block-model machines face the placer),
  owner on placement, menu, drops contents. `RenderShape.ENTITYBLOCK_ANIMATED` for Java-model machines, `MODEL` for
  JSON/OBJ block models with renderer extras.
- `TGMachineMenu` (data slots, buttons via `clickMenuButton`, slot icons), `TGMachineScreen` (1.12 GUI textures, power
  bar, redstone/security buttons, `IconButton`, `drawFluid`, `tankTooltip`).
- Multiblocks (`techguns.multiblock`): `MultiblockSchematic` (`canFormFromSide`, `check` for a size/target message,
  `parts`), `MultiblockPart` (block or air, connector, `hidden`), `MultiblockMasterBlockEntity` (`tryForm`/`unform`,
  linked parts, works only formed), `MultiblockPartBlock` (`FORMED`, `formedState`/`unformedState` hooks), one shared
  `MULTIBLOCK_PART` block entity type (add every new part block to it).
- Recipes are data-driven types in `techguns.recipe` (`MachineInput`, `MachineRecipe`), generated in `TGRecipeProvider`.
- 1.12 Java machine models are converted with a small script (`convmodel.py`, see below) into vanilla
  `LayerDefinition`s (`client/model/machine`), drawn by `TGMachineRenderer` (+ translucent pass).
- Client-only effects from common code go through hook interfaces installed by the client (`MachineClientEffects`,
  `GunClientEffects`).

## Environment and workflow notes

- Java 21: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot"` (Git Bash). Installed on both PCs; on the second PC the user/machine `JAVA_HOME` points to it.
- Build + tests: `./gradlew build runGameTestServer -Puse_mcpfabric=false`; data: `./gradlew runData`.
- Dev client runs windowed at 2560x1080 (`--width/--height` in `build.gradle`, `fullscreen:false` in the git-ignored `run/options.txt`) so it doesn't cover the screen.
- Dev client: `./gradlew runClient` in the background; ready when the log shows "Sound engine started", world joined
  when `run/logs/latest.log` shows "Dev joined". Join with MCP `join_world` `sp:TestworldFlat`.
- MCP on a new PC: Node.js 20+, then `claude mcp add mcpfabric --scope user -e MCPFABRIC_URL=http://127.0.0.1:25599 -e MCPFABRIC_TOKEN=<token> -- node F:/Techguns3Work/tools/mcpfabric/mcp-server/dist/index.js` (server already built on F:). The token must match `run/config/mcpfabric.config.json` (git-ignored); `enableUiControl` is on there for `get_ui`/`click_widget`. The MCP `container_transfer take` cannot take from machine (`SlotItemHandler`) slots; use GameTests for that.
- Never kill all java processes; stop only Techguns3 clients (filter `Win32_Process` command lines on
  `fml.modFolders` and `Techguns3`). Don't print the MCP token.
- Shell quirk on Windows: long bash heredocs sometimes fail with "unexpected EOF". Write Python edit scripts to the
  scratchpad with the Write tool and run them, or use `<<'PY'` heredocs for short ones.
- The model converter (1.12 `ModelRenderer` constructor → `addOrReplaceChild` lines) was a scratchpad script. It is
  saved as [tools/convmodel.py](../tools/convmodel.py) so it survives the move.
- Commits: imperative English subject; trailer `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`; commit or
  push only when the user asks (the user has been saying "commit and go on with ..." per batch).

## Claude memory (recreate on the new PC)

These live outside the repo in `C:\Users\<user>\.claude\projects\F--Techguns3Work-Techguns3\memory\`. Recreate them
there (or just keep following them):

- **English only**: replies, commits, docs and code always in English, even for German prompts.
- **Default branch `master`**: not `main`; PR base is `master`.
- **Visual testing allowed**: since the Max plan (2026-10-26), do MCP in-game checks yourself (proportional to the
  change); if token limits come up again, switch back to manual test checklists.
- **In-game test setup**: after joining the world, `kill @e[type=slime]`, `gamerule doMobSpawning false`,
  `gamerule doDaylightCycle false`, `time set noon` before firing tests.
- **Reference source priority**: Fabric only for gun mechanics/effects; 1.12 for armor, gear, machines and everything
  else.
