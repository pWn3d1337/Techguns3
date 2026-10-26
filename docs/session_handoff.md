# Session handoff (2026-10-26)

State of the AI-assisted porting session, so it can continue on another PC (same `F:` mapping).
Read this together with [CLAUDE.md](../CLAUDE.md), [Techguns3_design.md](../Techguns3_design.md) (plan, decisions) and
[port_status.md](port_status.md) (what is done, per batch, with notes on what was not ported).

## Where we are

- Branch: `phase5-machines` (base `master`). Phase 4 (PR pWn3d1337/Techguns3#5) is merged into `master`.
- Phase 5 progress (one commit per batch):

  | Batch | Content | Commit |
  |---|---|---|
  | 5a1 | Decorative cube blocks | `3778bd8` |
  | 5a2 | Shaped blocks (lamps, ladders, nets, sandbags, crates, doors) + GameTest target fixes | `ebf2f80` |
  | fix | Extra slot modifier map per side (Phase 4 crash: CME with client + integrated server thread) | `100afd2` |
  | 5b1 | Machine framework + Metal Press + machine upgrade item | `0e7122b` |
  | 5b2a | Ammo Press, Blast Furnace, Grinder | `ca87676` |
  | 5b2b | Chem Lab (fluid tanks), Charging Station, creeper acid fluid | committed with this handoff |

- All 216 GameTests pass; each batch was checked in game via MCP.

## Next steps

1. **5b3: benches**: Repair Bench (1.12 repair material costs for armor and shields), Camo Bench (1.12 scope for blocks, guns
   and armor, with a Fabric-style list for picking the named camo), Upgrade Bench (the 9 armor upgrade items:
   protection / projectile / blast protection, tiers 1-3), Ammo Bench (Fabric's unpowered stonecutter-style bench and
   `ammobench_crafting`). Grinder armor recycling (1.12 `GrinderRecipeGenericArmor`: repair materials scaled by
   durability) also lands here, because it needs the repair materials.
2. 5c multiblocks (Fabricator, Reaction Chamber, Ore Drill), 5d turrets / explosive charges / camp flag, 5e JEI.
3. Open a Phase 5 PR when Phase 5 is done (or earlier if the user asks).

## Open decision for the user

- **Raw steel / raw obsidian steel**: port_status lists `raw_steel_ingot` / `raw_obsidian_steel_ingot` as interim Fabric
  recipes "until the blast furnace". The Blast Furnace now exists. Asked the user whether to remove those recipes and
  items or keep them as an alternative. **No answer yet.**

## Decisions made in this session (also in the design doc)

- Q6: port **both** Ammo Press (1.12, powered, build plans) and the Fabric Ammo Bench (unpowered).
- Camo Bench: 1.12 scope with a Fabric-style named camo list.
- Reference priority: Fabric port only for gun mechanics/effects; Techguns 2 (1.12) for everything else.
- 5b was split into 5b1 (framework + Metal Press), 5b2a (item machines), 5b2b (fluid machines), 5b3 (benches).

## Machine framework (package `techguns.machine`), for the next batches

- `TGMachineBlockEntity`: inventory with slot rules (`canPlace`, `isOutputSlot`, `upgradeSlot`), insert-only FE buffer
  (`MachineEnergy`), redstone mode, owner + owner-only security, one `MachineOperation` (outputs, multiplier, time,
  power per tick) with progress; hooks `checkAndStartOperation`, `finishOperation`, `idleTick` (server, no job),
  `clientIdleTick`, `playWorkSound` (client), `useItem`/`canUseItem` (held item on the block, e.g. buckets),
  `pressMachineButton` (menu buttons), extra synced data (`extraDataCount`/`getExtraData`); helpers `has`, `consume`,
  `canOutput`, `deliver`, `canInsertAll`/`insertAll`, `maxStackMultiplier`.
- `TGMachineBlock`: facing (renderer-drawn machines store the look direction, block-model machines face the placer),
  owner on placement, menu, drops contents. `RenderShape.ENTITYBLOCK_ANIMATED` for Java-model machines, `MODEL` for
  JSON/OBJ block models with renderer extras.
- `TGMachineMenu` (data slots, buttons via `clickMenuButton`, slot icons), `TGMachineScreen` (1.12 GUI textures, power
  bar, redstone/security buttons, `IconButton`, `drawFluid`, `tankTooltip`).
- Recipes are data-driven types in `techguns.recipe` (`MachineInput`, `MachineRecipe`), generated in `TGRecipeProvider`.
- 1.12 Java machine models are converted with a small script (`convmodel.py`, see below) into vanilla
  `LayerDefinition`s (`client/model/machine`), drawn by `TGMachineRenderer` (+ translucent pass).
- Client-only effects from common code go through hook interfaces installed by the client (`MachineClientEffects`,
  `GunClientEffects`).

## Environment and workflow notes

- Java 21: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot"` (Git Bash).
- Build + tests: `./gradlew build runGameTestServer -Puse_mcpfabric=false`; data: `./gradlew runData`.
- Dev client: `./gradlew runClient` in the background; ready when the log shows "Sound engine started", world joined
  when `run/logs/latest.log` shows "Dev joined". Join with MCP `join_world` `sp:TestworldFlat`.
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
