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

Disable the dev-only MCP bridge with `-Puse_mcpfabric=false` (CI does this).
Add `-Pcompat_mods=true` to put the tier 2 compat mods (Create, Mekanism, IE, MI, Oritech) on the dev runtime, e.g. to check the compat recipes with `runGameTestServer`.

## In-game testing via MCP
The dev client loads **MCP Fabric** (`mcpfabric`, dev-only `localRuntime` dependency, never shipped).
It opens a loopback-only bridge on `127.0.0.1:25599`; the bearer token is in `run/config/mcpfabric.config.json`.
The `mcpfabric` MCP server exposes tools to inspect the world, run commands, read chat/events and take screenshots.
Use it to verify rendering, GUIs and gameplay after `runClient`; use GameTests for logic.
- Test world: `TestworldFlat` (creative, superflat, commands on) — join with `join_world` (`sp:TestworldFlat`) from the title screen.
- "Pause on lost focus" is disabled in the dev client (F3+P), so the game keeps running while the window is in the background.
- Call `get_status` first; screenshots need the player in a world with no screen open (`close_screen`).
- Dev-only `/tgdebug` client commands help where MCP has no input: `fire <ticks>`, `aim <ticks>` (hold use: lock-on, charge guns, grenades), `zoom`, `view` (F5), `fx <name>`, `fxstats`, `projectiles`, `slowbeams <ticks>`, `alwaysdeath`, `geo` (geo/Java model), `exportgeo`, `reloadanim <ticks>`, `jei <category>` (opens a JEI category, e.g. `metal_press`; send it with `send_chat`, the screen shows a moment later).
- Dedicated server test: start `./gradlew runServer -Puse_mcpfabric=false` (the server must not take the client's bridge port), then `runClient` and `join_world` the `localhost` entry in `run/servers.dat`. Server and client share `run/` (offline mode; `Dev` is op via `run/ops.json`). MCP server-side tools don't reach a dedicated server — use chat commands as the op player.

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
