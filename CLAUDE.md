# Techguns 3 — working notes for AI-assisted development

Port of Techguns (1.12.2 Forge) to Minecraft **1.21.1 / NeoForge 21.1.x**, later 26.x.
The plan and all design decisions live in [Techguns3_design.md](Techguns3_design.md) — read the relevant section before starting a phase.

## Language
Everything is English: code, comments, commit messages, docs, replies.

## Reference sources
- 1.12.2 Forge (complete feature set, MCP names): `F:\Techguns3Work\Techguns2`
- 1.19.4 Fabric port (gun mechanics + rendering, Yarn names): `F:\Techguns3Work\Techguns-Fabric`
- Rule: use the Fabric port if the feature exists there, else 1.12. Translate names via Appendix A of the design doc.

## Build & run (Java 21, Gradle wrapper)
| Task | Command |
|---|---|
| Build | `./gradlew build` |
| Data generation (writes `src/generated/resources`) | `./gradlew runData` |
| GameTests (headless, exits when done) | `./gradlew runGameTestServer` |
| Dev client | `./gradlew runClient` |
| Dedicated server | `./gradlew runServer` |

Disable the dev-only MCP bridge with `-Puse_mcpfabric=false` (CI does this).

## In-game testing via MCP
The dev client loads **MCP Fabric** (`mcpfabric`, dev-only `localRuntime` dependency, never shipped).
It opens a loopback-only bridge on `127.0.0.1:25599`; the bearer token is in `run/config/mcpfabric.config.json`.
The `mcpfabric` MCP server exposes tools to inspect the world, run commands, read chat/events and take screenshots.
Use it to verify rendering, GUIs and gameplay after `runClient`; use GameTests for logic.
- Test world: `TG Test` (creative, superflat, commands on) — join with `join_world` (`sp:TG Test`) from the title screen.
- "Pause on lost focus" is disabled in the dev client (F3+P), so the game keeps running while the window is in the background.
- Call `get_status` first; screenshots need the player in a world with no screen open (`close_screen`).

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

## Conventions
- Generate JSON (models, recipes, loot, tags, lang) with datagen; don't hand-write it.
- Every ported unit gets a GameTest where logic is testable.
- Track porting progress in `docs/port_status.md`.
- Commit messages: imperative subject line, English.
