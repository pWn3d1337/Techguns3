# Migration notes for 26.x

Running list of every place that uses an API expected to change on the jump from 1.21.1 to 26.x.
Each entry matches a `// MIGRATION(26.x): ...` comment in the code. See design doc §4.7.

| File | Note |
|---|---|
| `src/main/java/techguns/TGIds.java` | `ResourceLocation` renamed `Identifier` in newer Mojang mappings — only this class creates identifiers. |
