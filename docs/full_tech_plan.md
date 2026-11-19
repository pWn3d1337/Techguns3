# Full tech (standalone tech mod) — plan and handoff

Branch: `full_tech` (cut from `master` at `5a764a2`). Read this first when you pick the work up on another PC or in a
new session; the technical design is in [Techguns3_design.md](../Techguns3_design.md) §4.8 (and the Q7 amendment),
progress in [port_status.md](port_status.md) ("Standalone tech" table).

## Goal

Techguns 2 had no power source: its machines took FE from other mods. `full_tech` makes Techguns 3 usable as a
**standalone tech mod**: its own generators, an electric furnace, cables, fluid pipes and item pipes, plus ore
processing. Everything stays plain FE and NeoForge capabilities, so machines and cables of other mods (Mekanism,
IE, ...) keep working with ours.

## Decisions made with the user (keep them)

**Generators and machines**
- Generators: Combustion Generator (furnace fuels) and Liquid Fuel Generator (`techguns:fluid_fuel` data map; lava).
  Later added: Solar Panel and Wind Turbine.
- An Electric Furnace (vanilla smelting with FE).

**Pipes and cables**
- **Kinds and tiers:** cables, fluid pipes and item pipes, 3 tiers each (basic, advanced, elite). New kinds such as
  heat or gas must be addable later; they are registered in `TGPipeKinds`.
- **Stacking:** several pipe kinds can share one block. A kind alone in its block sits **centred**. Only when kinds share
  a block do the others move to their own lanes (cable > item > fluid keeps the centre). Moved segments join centred
  neighbours with **diagonal** connector pieces. Visual rules: no gaps, nothing sticking out, no z-fighting.
- **Side modes:** neutral, output, input and disabled, set by clicking the arm end with the wrench or crowbar (or any
  `c:tools/wrench`), like Mekanism.
  - On a bare core face (a side with no connection) a small colour mark shows the mode.
  - Disabled is shown in grey, not red.
- **Mixed tiers** connect into one network. Each same-tier area (zone) limits only the flow that passes through it.
- **Item pipes** render no items and move items instantly, a per-tier count per interval. Targets are served
  round-robin, nearest first.
- **No buffers** in pipes; only segments and modes are saved.

**Solar Panel**
- **Shape:** slab height, with a connected texture so panels side by side read as one surface.
- **Arrays:** panels side by side form an implicit multiblock with unlimited transfer inside it. A field of 4096 panels
  costs about +0.2 ms/tick.
- **Output:** full rate in sunlight, 1/8 in moonlight, open sky only.
- **Biome multipliers:** from the `techguns:solar_efficiency` biome data map (values in the design doc).
- **Screen:** right-click opens a screen with a power bar, the array size, its output, and the biome type with its
  multiplier.
- **Power out:** through the panel sides and bottom only. Cables accept it on neutral and input sides, not on output
  or disabled sides.

**Wind Turbine**
- **Shape:** 1x1 vertical-axis rotor that stacks vertically only. No turbine directly beside another (placement is
  refused). Column output scales linearly with its segments, uncapped.
- **Model** (procedural, `WindTurbineRenderer`):
  - Three wide helical blades that together make one winding per block, seamless across segments.
  - One strut set per blade at half height, in every segment.
  - Four variants: single, bottom, middle, top. The bottom one stands on a generator housing. The top is open: no cap,
    a round shaft ending at 3/4 height in a dome tip.
  - Blade corners are rounded at both column ends. Blade ends and edges are shaded like the blade skin.
- **Power out:** only the bottom segment generates, and it pushes through its sides and bottom.
- **Output:** depends on height (from sea level), biome (`techguns:wind_efficiency`) and weather.

**Ore dusts**
- Dusts for iron, gold, copper, tin, lead, titanium and uranium, as 16 px icons. Titanium dust sparkles and uranium
  dust glows (animated, the glow not on the base).
- **Grinder:** 1 ore or raw ore gives 2 dust, **except titanium** (no Grinder recipe; titanium dust has no source on
  purpose).
- **Smelting:** every dust smelts and blasts into its ingot except uranium, which has no ingot.

## Done (all on `full_tech`, see `git log master..full_tech`)

| Area | Main files |
|---|---|
| Generators, Electric Furnace | `machine/CombustionGeneratorBlockEntity`, `LiquidGeneratorBlockEntity`, `TGGeneratorBlockEntity`, `ElectricFurnaceBlockEntity`, `MachineEnergy`, `FluidFuel` |
| Transport | `core/transport/*` (pure: `ZoneGraph`, `EnergySplit`, `Lane`, `SideMode`), `transport/*` (`TransportManager`, `PipeNetwork`, `*Logic`, `TGPipeBlock(Entity)`, `PipeParts`, `PipeHit`, `PipeItem`), `client/render/block/pipe/*`, `data/TGPipeModelProvider` |
| Solar | `machine/SolarPanelBlock(Entity)`, `SolarArray` (level manager, sliced ticking), `SolarPanelMenu`, `client/gui/machine/SolarPanelScreen`, `core/power/SolarOutput` |
| Wind | `machine/WindTurbineBlock(Entity)`, `WindTurbineMenu`, `client/render/block/WindTurbineRenderer`, `client/gui/machine/WindTurbineScreen`, `core/power/WindOutput` |
| Biome multipliers | `machine/BiomeEfficiency` (`SOLAR`, `WIND` data maps, priority merger), rows in `data/TGDataMapProvider` |
| Dusts | `TGItems` dusts, `data/TGRecipeProvider` (`oreGrinder`, dust smelting) |
| Art (procedural, pure stdlib) | `tools/tex_transport.py` (machines, GUIs, pipes, solar, wind), `tools/tex_dusts.py` (dusts); run with `python -I tools/<script>.py src/main/resources/assets/techguns/textures` |
| Tests | JUnit `ZoneGraphTest`, `EnergySplitTest`, `LaneLayoutTest`, `SideModeTest`, `SolarOutputTest`, `WindOutputTest`; GameTests `TGTransportTests`, `TGMachineTests4`, `TGSolarTests`, `TGWindTests`, dust Grinder tests (424 GameTests pass) |

Config: `TGConfig` sections `generators` (combustion 40, liquid 80, solar 8, wind 10 per segment, electric furnace 20
FE/t) and `transport` (cables 256/2048/16384 FE/t, fluid 100/500/2000 mB/t, items 4/16/64 per 10/10/5 ticks).

## Open / next steps

1. **Branch upkeep:** `full_tech` is behind `master` (at least `fac0f67`, `d4c0964`, `9bc3325`). Merge `master` in
   before more work, then later merge `full_tech` back via a PR to `master`.
2. **Not verified yet:**
   - dedicated server sync (pipe model data, turbine wind factor, solar screen biome line);
   - interop with other mods' cables and machines (`-Pcompat_mods=true`, e.g. Mekanism cables on our generators and
     our cables on their machines).
3. **JEI:**
   - Electric Furnace (smelting) and Combustion Generator (fuel) are registered as catalysts;
   - missing: a liquid fuel info page, and maybe info pages for solar and wind (biome and height factors).
4. **Guide book (Patchouli `techguns_manual`):** no pages yet for generators, pipes, solar, wind or dusts.
5. **Later ideas (not decided):**
   - more pipe kinds (heat, gas) through `TGPipeKinds`;
   - a fuel fluid of our own for the Liquid Fuel Generator (standalone it burns lava only);
   - upgrade slots for generators.
6. Keep `docs/port_status.md`, design doc §4.8 and this file current with every change.

## Working conventions for this branch

- **Commits:** only on `full_tech`; English, imperative subject, ending with the Co-Authored-By line. Never
  `git add -A`: the user keeps uncommitted assets in `art/`, `temp/` and some untracked textures and geo files.
- **Generated JSON:** comes from datagen (`./gradlew runData`, run alone first). Textures come from the `tools/tex_*.py`
  scripts (deterministic); change the script and rerun it, don't paint the PNGs by hand.
- **Verify:**
  - always `./gradlew build`; add `runData` and `runGameTestServer` when data or logic changed;
  - in game via the MCP bridge (`runClient`, world `TestworldFlat`) for models, GUIs and textures.
  - The flat test world's sea level is 63 and the ground is at about -60, so wind turbines need to stand above about
    y 40 to make power there.
- **The user's test world** has their own builds (a solar field, turbines on glass blocks near x 0–10, z 30–40,
  y 100–110). Check that an area is empty before any `fill`/`setblock`.
