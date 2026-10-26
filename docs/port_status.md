# Port status

Tracks every Techguns content entry through the port. Status values:
`—` not started · `logic` · `rendered` · `tested` · `done`.
Sources: **F** = Fabric 1.19.4 port, **12** = Techguns 2 (1.12.2).

## Phases

| Phase | Status |
|---|---|
| 0 — Project setup | done |
| 1 — Core infrastructure | done (machine-only recipes follow in Phase 5) |
| 2 — Gun framework | done pending review (3 guns with FX and death effects in SP and on a dedicated server; all guns rendered with GeckoLib, Q3 decided) |
| 3 — All guns, ammo, grenades, tools | in progress (batches 3a bullet guns, 3b shotguns/sniper/advanced/blaster, 3c energy guns, 3d explosives) |
| 4 — Armor & player gear | — |
| 5 — Blocks & machines | — |
| 6 — NPCs & factions | — |
| 7 — World generation | — |
| 8 — Remaining systems & integrations | — |

## Content

Per-item tables (guns, ammo, armor, blocks, machines, NPCs, structures) are added at the start of the phase that ports them.

### Phase 1 — core systems

| Unit | Source | Status | Notes |
|---|---|---|---|
| Damage system (types, category tags, armor formula, penetration, bonus armor attributes, PvP/NPC factors) | 12 + F | tested | JUnit + GameTests; knockback multiplier and death effects follow with projectiles (Phase 2) |
| Sound events (170) + sounds.json + .ogg | F (+12 for blocks/items/machines/npcs) | done | |
| Key mappings (force reload, safe mode, step assist) | F | logic | handlers come with guns / armor |
| Data component & attachment registries | — | done | empty, filled by later phases |
| Creative tab | — | done | |

### Phase 1 — materials, components, ores

| Group | Items | Status | Notes |
|---|---|---|---|
| Ores (tin, lead, titanium, uranium; stone + deepslate) | 8 blocks | rendered | no worldgen yet (Phase 7); copper ore dropped (vanilla copper) |
| Raw materials, ingots, nuggets | 16 | rendered | raw_steel / raw_obsidian_steel are interim Fabric recipes until the blast furnace (Phase 5) |
| Plates | 9 | rendered | **no recipe yet** — metal press (Phase 5) |
| Other materials (rubber, plastic, wires, TGX, …) | 14 | rendered | plastic_sheet, raw_plastic, yellowcake, enriched_uranium, quartz_rod, bronze_ingot: machine recipes (Phase 5) |
| Components (receivers, barrels, stocks, circuits, emitters, …) | 37 | rendered | recipes ported from Fabric; titanium_receiver, gauss/shielded barrels, steam_armor_plate, plasma_generator, antigrav_core, protective_fiber, heat_ray_focus, uv_emitter wait for machines |

### Phase 2 — gun framework

| Unit | Source | Status | Notes |
|---|---|---|---|
| Gun logic (GunStats record, ammo types, fire/reload, client prediction, payloads, input, zoom) | F + 12 | tested | GameTests: firing, fire delay, reload, loose rounds, laser, rocket |
| M4 | F | rendered | bullets + tracer, muzzle flash; first-person position to be confirmed by the author |
| Lasergun | F | rendered | hit-scan beam, energy damage, custom additive shader |
| Rocket Launcher | F | rendered | explosion via vanilla blast (no vanilla particles) + TG falloff damage; rocket, exhaust trail and explosion FX checked in game |
| Assault rifle magazine, energy cells, rocket item | F | rendered | |
| Gun item render pipeline (GunRenderer abstraction, RenderGunBase/90, GunAnimation, muzzle FX) | F | rendered | |
| FX render types + `particle_alpha` core shader | F | rendered | port of Fabric TGRenderHelper |
| Projectile renderers (bullet, beam, rocket) | F | rendered | beams and tracers drawn in the late render pass |
| GeckoLib gun models (Q3: full GeckoLib): M4, Lasergun, Rocket Launcher + rocket exported from the Java models, pixel-identical; M4 keyframed reload | — | rendered | design doc §9.1; GeckoLib 4.9.3 required; `/tgdebug geo`, `exportgeo`, `reloadanim` |
| Dedicated server run (client connected over the network) | — | tested | `runServer -Puse_mcpfabric=false`, offline mode; firing, ammo, impacts, beams, rocket FX and death FX over the network; no client classes loaded on the server |
| Techguns particle system, FX payloads, late render pass (bullet/laser impacts, rocket exhaust + explosion) | F | rendered | `/tgdebug fx <name>` spawns an FX at the crosshair |
| Death effects: gore gibs from the mob's model parts + blood FX, laser/bio FX, corpse hidden on gore; rocket blast without vanilla particles | F + 12 | rendered | gore chance roll from 1.12; laser/bio corpses burn/rot and fade (no mixin: `RenderLivingEvent.Pre`) |
| Dev test hooks: `/tgdebug fire`, `projectiles`, `slowbeams`, `zoom`, `fx`, `fxstats`, `alwaysdeath`, `view`, `geo`, `exportgeo`, `reloadanim` | — | done | dev builds only (MCP testing) |

### Phase 3 — all guns, ammo, grenades, tools

Batches: design doc Phase 3. Each gun: Fabric stats, Java model exported to GeckoLib geo, GameTests (fires and hits, reloads from its ammo, incendiary ignites).

| Unit | Source | Status | Notes |
|---|---|---|---|
| 3a guns: revolver, golden revolver, pistol, AKM, The Infiltrator, AUG, SCAR, Thompson, MAC-10, Vector, LMG, bolt action, minigun | F | rendered | 39 generated GameTests; GeckoLib geo models pixel-identical to the Java models in game (SCAR: 1 px scope edges); bolt/slide/barrel/belt animations driven from Java; minigun keeps its spin animation running (`continuousAnimation`); MAC-10 hand type `ONE_POINT_FIVE_HANDED` |
| 3a ammo: pistol/rifle rounds (+incendiary), pistol/SMG/AR/LMG magazines, minigun drum (+empty, +incendiary) | F | rendered | incendiary ammo has an uncommon (yellow) name instead of the Fabric colour code |
| 3a recipes: guns (plain, empty, incendiary; ammo in the result), empty magazines, magazine refills, energy cell, rocket | F | logic | 38 recipes; minigun drum refill waits for the rifle rounds stack item; golden revolver/Infiltrator use the ammo transfer recipe (batch 3g) |
| 3b guns: double barrel shotgun, combat shotgun, AS50, gauss rifle, blaster rifle, blaster shotgun, pulse rifle, PDW | F | rendered | generated GameTests (fires and hits, reloads and returns empties, incendiary, AS50 explosive blast); checked in game: models, AS50 scope, explosive rounds, gauss, blaster bolts, blue impacts |
| 3b projectile kinds: explosive (Techguns 2 blast, power 1.5, block damage only outside safe mode), advanced (blue tracer + blue impacts), blaster (energy damage, laser death, LaserGunImpact), gauss (blue impacts + GaussRifleImpact_Block, GaussProjectileTrail) | F + 12 | logic | Fabric dropped the explosive blast; ported from 1.12 |
| 3b ammo: shotgun rounds (+incendiary), sniper rounds (+incendiary, +explosive), advanced rounds, AS50 magazine (+empty, +incendiary, +explosive), advanced magazine (+empty), gauss rifle slugs (loaded together with an energy cell) | F | logic | shotgun/sniper rounds need the ammo bench (Phase 5); explosive sniper rounds have no Fabric recipe |
| 3b recipes: AS50 (4 variants), gauss rifle, blaster rifle, pulse rifle, PDW (loaded + empty), shotguns (empty), magazines, advanced rounds, slugs, incendiary rounds | F | logic | blaster shotgun has no Fabric recipe |
| 3b geo models (exported, in game checked by the author) | — | rendered | fine tuning may follow |
| 3c guns: laser pistol, nuclear death ray, tesla gun, alien blaster, De-Atomizer (MIB gun), nether blaster, sonic rifle | F | rendered | generated GameTests plus nether blaster ignites and sonic wave pierces; checked in game: models, laser/NDR/tesla beams, alien blaster trail + impact, sonic wave through two targets. Projectile trails start from the spawn data (NeoForge applies it after the client join event). The nether blaster reload sound `guns.cyberdemonblasterreload` has no sound file in Techguns 2 or Fabric either |
| 3c projectiles: NDR beam (15 damage ticks, follows the shooter, looped fire sound, muzzle beam FX), tesla beam (lightning damage, TeslaFlare), FX-only projectiles (de-atomizer, alien blaster, nether blaster: particle trail + impact FX, no sprite), sonic wave (centre + 2 rings, shared hit list, passes through entities, knockback) | F | logic | tesla no-knockback waits for the knockback multiplier; Fabric declares tesla chain-lightning constants but never uses them; not ported |
| 3c ammo: redstone battery (+empty), nuclear power cell (+depleted), nether charge; recipes for all 3c guns (loaded + empty) and power sources | F | logic | alien blaster has no Fabric recipe |
| Model part transforms match Techguns 2: rotation order Z, Y, X and children inherit the parent rotation (the Fabric port differed); NDR, LMG and MAC-10 geo models re-exported | 12 | rendered | checked in game, geo and Java identical |
| 3d guns: grenade launcher, guided missile launcher, Grim Reaper; rocket launcher nuke and high velocity ammo | F + 12 | logic | rocket variants, guided missiles (incl. high velocity) and the Grim Reaper from Techguns 2 where Fabric had TODOs; awaiting in-game check |
| 3d explosion system: `TGExplosion` port (falloff, no damage through walls, block breaking by blast resistance, drop chance) used by rockets, missiles and grenades | 12 | tested | nuke radiation area and nuke/tesla knockback multipliers follow with the radiation system / knockback multiplier |
| 3d lock-on: hold use to lock (client `LockOnTracker`, beeps, HUD corners + target name), target sent with the shot and checked on the server; missiles turn up to 9 degrees per tick | F + 12 | logic | |
| 3d grenades: 40 mm launcher grenade (3 bounces, OBJ mesh), stick grenade and frag grenade (wind-up throw, Techguns 2) | F + 12 | logic | OBJ guns/grenades use `ObjMesh` (no GeckoLib version) |
| 3d recipes: 40 mm grenades, hand grenades, grenade launcher (empty), guided missile launcher (3), rocket launcher high velocity / nuke | F + 12 | logic | nuke rocket needs the tactical nuke warhead, high velocity rocket the chemical laboratory (Phase 5), Grim Reaper the ammo transfer recipe (3g) |
