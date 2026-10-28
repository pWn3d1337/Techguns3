# Changelog

All notable changes to Techguns 3. Versions follow `<major>.<minor>.<patch>[-alpha.N|-beta.N]`; the jar and the
release pages add the Minecraft version. The release tooling (`./gradlew publishMods`) uploads the section of the
current version as the release notes.

## [3.0.0-alpha.1] - unreleased

First alpha of Techguns 3: Techguns 2 (Minecraft 1.12.2) ported to Minecraft 1.21.1 / NeoForge 21.1, with the gun
mechanics and effects of the Fabric 1.19.4 port. Worlds and items from Techguns 2 can't be carried over.

### Content
- 44 guns and tools across bullet, shotgun, sniper, energy, explosive, flame, charge and melee types, plus stick and
  frag grenades: reloading, ammo variants, zooming, recoil, muzzle flashes, beams and particle effects, death effects
  on kills, gun upgrades and the ammo bag. Guns are GeckoLib models.
- Armor sets up to tier 4 power armor, with powered armor, night vision, step assist, jetpacks, gliders, gas and
  oxygen masks and other face, back and hand gear in extra slots.
- Machines: Metal Press, Ammo Press, Blast Furnace, Grinder, Chem Lab, Charging Station, Repair, Camo, Upgrade and
  Ammo Bench, and the Fabricator, Reaction Chamber and Ore Drill multiblocks. Machines run on Forge Energy (FE).
- Decorative blocks, lamps, ladders, nets, sandbags, crates and doors; explosive charges; turrets.
- 20 NPC types in factions: zombie and skeleton soldiers, human soldiers and bandits, super mutants, alien bugs and
  the bosses (cyber demon, ghastling, attack helicopter); natural spawning and spawner blocks.
- World generation: tin, lead, titanium and other ores, ore clusters, the Techguns 2 overworld and Nether
  structures, the aircraft carrier, the military base and castle maze dungeons, all with Techguns loot.
- Radiation with sources, shielding and cures.
- Advancements as a progression guide and the Techguns manual as a guide book.
- English, German and Russian translations.
- Datapack overrides for gun and armor stats ([docs/stat_overrides.md](docs/stat_overrides.md)), replacing the Techguns 2
  CraftTweaker tweakers.
- Client option to keep the FOV steady with speed bonuses and while sprinting (on by default, like Techguns 2).

### New in Techguns 3 (not in Techguns 2)
- Nether fortress: the Techguns 2 Nether dungeon, now generated underground with an entrance hut.
- Cities: the Techguns 2 procedural city, which Techguns 2 never spawned.
- Optional integrations: Curios for the extra slots (Techguns uses its own slots without it), JEI recipe categories
  for all machines, the guide book for both Patchouli and Modonomicon, Create, Mekanism, Immersive Engineering,
  Modern Industrialization and Oritech recipes for Techguns plates and alloys, vanilla teams, FTB Teams and Open
  Parties and Claims for friendly fire and turret targeting, machine security levels, and a two-way Mekanism
  radiation bridge.
- Decorative mod blocks: where Factory Blocks, Chisel Reborn or Laboratory Blocks are installed, structures use their
  blocks like Techguns 2 used Chisel's (factory plates, hazard stripes, scaffolds, lab walls, patterned stone); Rechiseled
  can chisel between the Techguns camo net, metal panel, neon light, concrete and ladder variants. All optional, with
  the vanilla look as fallback (config `decorModBlocks`).

### Known gaps
- Some guide book pages still describe Techguns 2 features that changed (crowbar and wrench, the inventory tab
  buttons, the door configuration screen, RF).
- Gun, armor and machine numbers are the Techguns 2 values; a balancing pass is still to come.
