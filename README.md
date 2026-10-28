# Techguns 3

Techguns for modern Minecraft — guns, armor, machines, NPCs and structures.
Port of Techguns 2 (1.12.2) to **Minecraft 1.21.1 / NeoForge 21.1**, in alpha.

- What's in it: [CHANGELOG.md](CHANGELOG.md)
- Design & roadmap: [Techguns3_design.md](Techguns3_design.md)
- Port progress: [docs/port_status.md](docs/port_status.md)

## Requirements

- Minecraft 1.21.1 with NeoForge 21.1.264 or newer
- [GeckoLib](https://modrinth.com/mod/geckolib) 4.9.3 or newer

Optional: [Curios](https://modrinth.com/mod/curios) (extra slots; Techguns has its own without it),
[JEI](https://modrinth.com/mod/jei) (machine recipes), [Patchouli](https://modrinth.com/mod/patchouli) or
[Modonomicon](https://modrinth.com/mod/modonomicon) (guide book). Create, Mekanism, Immersive Engineering, Modern
Industrialization, Oritech, FTB Teams and Open Parties and Claims get integrations when installed. With Factory Blocks, Chisel Reborn
or Laboratory Blocks, structures use their decorative blocks; Rechiseled can chisel between Techguns block variants.

## Building

Requires JDK 21.

```bash
./gradlew build
```

The jar is written to `build/libs/`. Development notes (run configs, GameTests, in-game testing) are in
[CLAUDE.md](CLAUDE.md).

## Releasing

1. Set the version in `gradle.properties` (`mod_version`) and fill in its section in `CHANGELOG.md`.
2. Set `curseforge_project_id` and `modrinth_project_id` in `gradle.properties` and add the `CURSEFORGE_TOKEN` and
   `MODRINTH_TOKEN` repository secrets.
3. Run the **Release** workflow (Actions tab). It builds, runs the GameTests and uploads the jar to CurseForge,
   Modrinth and a GitHub release (tag `v<mod_version>`) with the changelog section as release notes.

`./gradlew publishMods` does the same locally; without the tokens or project ids it is a dry run that writes the
release into `build/publishMods`.

## License

See [LICENSE.txt](LICENSE.txt) (Techguns Mod License).
