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
- Decorative blocks, lamps, ladders, nets, sandbags, crates and doors (animated, with their settings screen; 3x3 doors also hang under ceilings); explosive
  charges; turrets; the crowbar (a pickaxe and wrench: door settings, turning and picking up machines) and the combat knife.
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
- One- and two-handed guns as in Techguns 2: pistols and other one-handed guns can be dual-wielded, a two-handed gun
  leaves the off hand unusable. Gun tooltips show all stats with shift held, and how the loaded ammo changes them.
- Auto-Feeder: worn in the new mouth slot, it feeds you its food when you get hungry, replacing the auto-feeder slots
  of the Techguns 2 inventory tab (config `autoFeeder`).
- The Techguns 2 HUD in the bottom right corner: ammo of the held guns with spare reloads, safe mode, power armor
  power, jetpack fuel, night vision and step assist state, worn-out armor and radiation (client config `showHudElements`).

### New in Techguns 3 (not in Techguns 2)
- Nether fortress: the Techguns 2 Nether dungeon, now generated underground with an entrance hut.
- Cities: the Techguns 2 procedural city, which Techguns 2 never spawned.
- Optional integrations: Curios for the extra slots (its visibility toggle hides the worn gear; Techguns uses its own slots without it), JEI recipe categories
  for all machines, the guide book for both Patchouli and Modonomicon, Create, Mekanism, Immersive Engineering,
  Modern Industrialization and Oritech recipes for Techguns plates and alloys, vanilla teams, FTB Teams and Open
  Parties and Claims for friendly fire and turret targeting, machine security levels, and a two-way Mekanism
  radiation bridge.
- Decorative mod blocks: where Factory Blocks, Chisel Reborn or Laboratory Blocks are installed, structures use their
  blocks like Techguns 2 used Chisel's (factory plates, hazard stripes, scaffolds, lab walls, patterned stone); Rechiseled
  can chisel between the Techguns camo net, metal panel, neon light, concrete and ladder variants. All optional, with
  the vanilla look as fallback (config `decorModBlocks`).
- The Techguns 2 structure debug tools (`debug` in the common config): building scan tool (saves structure templates), world gen
  test tool, structure markers, dungeon scanner and generator.
- Safe mode (key B, like Techguns 2): with it on, your guns and grenades don't damage blocks; NPC and turret shots never do.
- Connected textures with Fusion installed, like Techguns 2 had with CTM: containers, steel frames, border panels,
  concrete, nether metal and the ore drill join into larger textures.
- Fits the newer vanilla game: Techguns structures keep away from villages, pillager outposts, mansions, monuments,
  temples, Nether fortresses and bastions; animals flee when shot; bullets and energy shots knock armor stands over
  like arrows; breezes deflect Techguns projectiles like arrows (beams pass); radiation goes through wolf armor; with
  Lootr installed, every player gets their own loot from Techguns structure chests.

### Known gaps
- Some guide book pages still describe Techguns 2 features that changed (the inventory tab buttons, RF).
- Gun, armor and machine numbers are the Techguns 2 values; a balancing pass is still to come.
