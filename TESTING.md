# Testing Techguns 3

Techguns 2 (Minecraft 1.12.2) is being ported to **Minecraft 1.21.1 / NeoForge**. The port is in alpha and changes
every day, and we need testers: people who played Techguns 2 and can tell when something feels off, and people who
play with today's mods and can check how Techguns fits in with them.

## Get the build

1. Install **Minecraft 1.21.1** with **NeoForge 21.1.238** or newer.
2. Add **[GeckoLib](https://modrinth.com/mod/geckolib) 4.9.3** or newer (required).
3. Download the jar from the **[latest dev build](https://github.com/pWn3d1337/Techguns3/releases/tag/dev)**.

The dev build is rebuilt on every change to `master` and the previous one is removed, so grab it again before you
test. Every build has passed the automated GameTests. The build number is in the jar name
(`techguns-1.21.1-3.0.0-alpha.1-dev.<n>.jar`), so please include it in reports.

> **Use a new world, and don't use one you care about.** Techguns 2 worlds and items can't be carried over, and
> alpha builds can break saves between versions.

Recommended optional mods: [Curios](https://modrinth.com/mod/curios) (extra slots), [JEI](https://modrinth.com/mod/jei)
(all machine recipes) and [Patchouli](https://modrinth.com/mod/patchouli) or
[Modonomicon](https://modrinth.com/mod/modonomicon) (the Techguns manual).

## What to test

### Compared with Techguns 2
If you remember how something worked in 1.12, tell us when it works differently in 3.0.
- **Guns:** reloading, ammo variants, zoom, recoil, muzzle flashes, beams, death effects, dual-wielding one-handed
  guns, gun upgrades, the ammo bag. Shift-hover shows the full gun stats.
- **Armor and gear:** power armor tiers, jetpacks and gliders, night vision, step assist, gas and oxygen masks, face,
  back and hand slots. Safe mode is on **B**: with it on, your guns don't damage blocks.
- **HUD:** ammo, power, jetpack fuel and radiation, bottom right as in 1.12.
- **Machines:** all benches and presses, plus the Fabricator, Reaction Chamber and Ore Drill multiblocks. Machines
  now run on **FE** instead of RF.
- **NPCs and world:** the 20 NPC types and bosses, ores, the Techguns 2 structures, the aircraft carrier, the
  military base and the castle maze dungeons.
- **Radiation:** sources, shielding and cures.

Gun, armor and machine numbers are still the Techguns 2 values. Feedback on balance is welcome, and a balancing
pass will come later.

### New in Techguns 3
- **Nether fortress:** the Techguns 2 Nether dungeon, now underground with an entrance hut.
- **Cities:** the Techguns 2 procedural city, which 1.12 never spawned.
- **Auto-Feeder:** worn in the new mouth slot, it feeds you when you get hungry.
- **Connected textures** with [Fusion](https://modrinth.com/mod/fusion-connected-textures) installed.
- **Datapack overrides** for gun and armor stats, replacing the CraftTweaker support.
- **Newer vanilla:** structures keep away from villages, outposts, bastions and the like; animals flee when shot;
  breezes deflect Techguns projectiles.

### Mod integrations
These only matter if you have the mod installed. Please test them in the packs you actually play.
- **Curios:** the extra slots, and its visibility toggle hides worn gear.
- **JEI:** recipe categories for every machine.
- **Create, Mekanism, Immersive Engineering, Modern Industrialization, Oritech:** recipes for Techguns plates and
  alloys.
- **Mekanism:** radiation works both ways between the two mods.
- **FTB Teams, Open Parties and Claims, vanilla teams:** friendly fire, turret targeting, machine security.
- **Lootr:** every player gets their own loot from Techguns structure chests.
- **Factory Blocks, Chisel Reborn, Laboratory Blocks:** structures use their blocks. **Rechiseled** chisels between
  the Techguns block variants.

Also useful: **dedicated servers** and **multiplayer** (turrets, teams, NPCs, machines shared between players).

The full feature list is in [CHANGELOG.md](CHANGELOG.md).

## Known gaps
- Some manual pages still describe Techguns 2 features that changed (the inventory tab buttons, RF).
- No balancing pass yet.

## Reporting
Open an issue at **[github.com/pWn3d1337/Techguns3/issues](https://github.com/pWn3d1337/Techguns3/issues)** with:
- the dev build number (from the jar name) and your mod list
- what you did, what happened, and what you expected (or how it worked in Techguns 2)
- `logs/latest.log`, or the crash report from `crash-reports/`, plus screenshots or a clip for anything visual

Thanks for testing!
