# Techguns 3 — working notes for AI-assisted development

Port of Techguns (1.12.2 Forge) to Minecraft **1.21.1 / NeoForge 21.1.x**, later 26.x.
The plan and all design decisions live in [Techguns3_design.md](Techguns3_design.md) — read the relevant section before starting a phase.
Current session state, next steps and open questions: [docs/session_handoff.md](docs/session_handoff.md) — read it first when resuming.

## Language
Everything is English: code, comments, commit messages, docs, replies.

## Reference sources
- 1.12.2 Forge (complete feature set, MCP names): `F:\Techguns3Work\Techguns2`
- 1.19.4 Fabric port (gun mechanics + rendering, Yarn names): `F:\Techguns3Work\Techguns-Fabric`
- Rule: the Fabric port is the source for gun mechanics and effects only. For everything else (armor, gear, machines, NPCs, worldgen, ...) Techguns 2 (1.12) is the source; Fabric only for features 1.12 lacks. Translate names via Appendix A of the design doc.

## Build & run (Java 21, Gradle wrapper)
| Task | Command |
|---|---|
| Build | `./gradlew build` |
| Data generation (writes `src/generated/resources`) | `./gradlew runData` |
| GameTests (headless, exits when done) | `./gradlew runGameTestServer` |
| Dev client | `./gradlew runClient` |
| Dedicated server | `./gradlew runServer` |
| Release upload (dry run without tokens/project ids, see README) | `./gradlew publishMods` |

Disable the dev-only MCP bridge with `-Puse_mcpfabric=false` (CI does this).
The mod declares `neo_min_version` (gradle.properties) as its oldest NeoForge; check it with `-Pneo_version=<neo_min_version>` (CI does this in its own job; MCP Fabric needs a newer NeoForge, so combine with `-Puse_mcpfabric=false`).
FTB Library (with Architectury) is always on the dev runtime for its inventory sidebar buttons (game mode, day, night, rain; ops only).
Add `-Pcompat_mods=true` to put the compat mods (Create, Mekanism, IE, MI, Oritech, FTB Teams) on the dev runtime, e.g. to check the compat recipes with `runGameTestServer`; `-Popac=true` adds Open Parties and Claims (dev client only, it crashes the GameTest server); `-Pdecor_mods=true` adds the decorative block mods (Rechiseled, Chisel Reborn, Factory Blocks, Laboratory Blocks) that structures and the chiseling groups use; `-Plootr=true` adds Lootr (per-player structure loot; dev client only, the structure GameTests check the vanilla chests it replaces).

## In-game testing via MCP
The dev client loads **MCP Fabric** (`mcpfabric`, dev-only `localRuntime` dependency, never shipped).
It opens a loopback-only bridge on `127.0.0.1:25599`; the bearer token is in `run/config/mcpfabric.config.json`.
The `mcpfabric` MCP server exposes tools to inspect the world, run commands, read chat/events and take screenshots.
Use it to verify rendering, GUIs and gameplay after `runClient`; use GameTests for logic.
- Test world: `TestworldFlat` (creative, superflat, commands on) — join with `join_world` (`sp:TestworldFlat`) from the title screen.
- "Pause on lost focus" is disabled in the dev client (F3+P), so the game keeps running while the window is in the background.
- Call `get_status` first; screenshots need the player in a world with no screen open (`close_screen`).
- Dev-only `/tgdebug` client commands help where MCP has no input: `fire <ticks>`, `aim <ticks>` (hold use: lock-on, charge guns, grenades), `zoom`, `view` (F5), `fx <name>`, `fxstats`, `projectiles`, `slowbeams <ticks>`, `alwaysdeath`, `geo` (geo/Java model), `exportgeo`, `reloadanim <ticks>`, `teams` (runs every team source on the integrated server), `bench <gun> <shots per tick> <seconds>` (load test: fires on the integrated server, reports tick/frame times and peak projectile/particle counts), `stats` (the client's stats of the held gun or armor piece, to check datapack overrides), `tooltip [detailed]` (the held item's tooltip in chat, detailed as with shift; MCP cannot hover), `key <safe_mode|step_assist|night_vision|jetpack|force_reload>` (presses a Techguns key binding), `mouse <x> <y> <delay ms> [click]` (after the delay hovers or left-clicks at GUI coordinates relative to the open container screen; send it, then open the screen; several can be queued), `jei <category>` (opens a JEI category, e.g. `metal_press`, or `held` for the recipes using the held item; send it with `send_chat`, the screen shows a moment later).
- Dedicated server test: start `./gradlew runServer -Puse_mcpfabric=false` (the server must not take the client's bridge port), then `runClient` and `join_world` the `localhost` entry in `run/servers.dat`. For a second player add `./gradlew runClient2 -Puse_mcpfabric=false` (user `Dev2`, game dir `run2/`, joins `localhost` itself). Server and client share `run/` (offline mode; `Dev` is op via `run/ops.json`). MCP server-side tools don't reach a dedicated server — use chat commands as the op player.

## Code layout (package `techguns`)
See design doc §4.1. Key rules from §4.7 (migration readiness for 26.x):
- Identifiers only via `TGIds` — never construct `ResourceLocation` directly.
- Payloads only in `network/`, registered in `TGNetwork`.
- Config values only via `TGConfig` fields.
- Game logic in `core/` with minimal Minecraft types and no client classes.
- Renderers use extract-state-then-render; draw through the TG draw facade; no raw GL.
- Item data via typed accessors (`GunData.of(stack)`), not scattered `stack.get(...)`.
- Optional mod integrations only in `compat/`, behind TG interfaces.
- Mark uses of volatile APIs with `// MIGRATION(26.x): <note>` and list them in `docs/migration_26.md`.
- Mixins only as a last resort, each with a comment explaining why.

## Orchestrator & subagents
The main session (Opus) orchestrates; delegate simple, bounded tasks to cheaper subagents (design doc §8.1):
- **Haiku**: read-only searches in the reference repos, data extraction (stats, recipes, lang keys).
- **Sonnet**: mechanical ports following an existing reference implementation, datagen entries, GameTest boilerplate, diff reviews.
- **Opus (self)**: architecture, rendering, cross-cutting systems, first unit of each kind, verification, commits.
- Briefs name source/target files, the reference to copy, conventions and the done criteria. Verify every result (diff, build, GameTests; MCP only for visual work) before committing. Subagents never commit; parallel writers need separate worktrees.

## Verification (proportional to the change)
- Default: local `./gradlew build`; add `runData` / `runGameTestServer` when data or logic changed.
- Don't wait for CI on every simple step — push and continue; check CI before a phase/PR is done or when build scripts, the workflow or dependencies changed.
- MCP screenshots / in-game checks only when needed: rendering, GUIs, models/textures, FX, or behaviour GameTests can't cover.

## Conventions
- Generate JSON (models, recipes, loot, tags, lang) with datagen; don't hand-write it.
- Every ported unit gets a GameTest where logic is testable.
- Track porting progress in `docs/port_status.md`.
- Commit messages: imperative subject line, English.
