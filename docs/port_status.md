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
| 3 — All guns, ammo, grenades, tools | done (batches 3a bullet guns, 3b shotguns/sniper/advanced/blaster, 3c energy guns, 3d explosives, 3e charge and fuel weapons, 3f tools, 3g ammo bag and recipe types) |
| 4 — Armor & player gear | in progress (planned batches 4a–4f, design doc Phase 4) |
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
| 3a recipes: guns (plain, empty, incendiary; ammo in the result), empty magazines, magazine refills, energy cell, rocket | F | logic | 38 recipes; minigun drum refill and golden revolver/Infiltrator/Grim Reaper added in 3g |
| 3b guns: double barrel shotgun, combat shotgun, AS50, gauss rifle, blaster rifle, blaster shotgun, pulse rifle, PDW | F | rendered | generated GameTests (fires and hits, reloads and returns empties, incendiary, AS50 explosive blast); checked in game: models, AS50 scope, explosive rounds, gauss, blaster bolts, blue impacts |
| 3b projectile kinds: explosive (Techguns 2 blast, power 1.5, block damage only outside safe mode), advanced (blue tracer + blue impacts), blaster (energy damage, laser death, LaserGunImpact), gauss (blue impacts + GaussRifleImpact_Block, GaussProjectileTrail) | F + 12 | logic | Fabric dropped the explosive blast; ported from 1.12 |
| 3b ammo: shotgun rounds (+incendiary), sniper rounds (+incendiary, +explosive), advanced rounds, AS50 magazine (+empty, +incendiary, +explosive), advanced magazine (+empty), gauss rifle slugs (loaded together with an energy cell) | F | logic | shotgun/sniper rounds need the ammo bench (Phase 5); explosive sniper rounds have no Fabric recipe |
| 3b recipes: AS50 (4 variants), gauss rifle, blaster rifle, pulse rifle, PDW (loaded + empty), shotguns (empty), magazines, advanced rounds, slugs, incendiary rounds | F | logic | blaster shotgun has no Fabric recipe |
| 3b geo models (exported, in game checked by the author) | — | rendered | fine tuning may follow |
| 3c guns: laser pistol, nuclear death ray, tesla gun, alien blaster, De-Atomizer (MIB gun), nether blaster, sonic rifle | F | rendered | generated GameTests plus nether blaster ignites and sonic wave pierces; checked in game: models, laser/NDR/tesla beams, alien blaster trail + impact, sonic wave through two targets. Projectile trails start from the spawn data (NeoForge applies it after the client join event). The nether blaster reload sound `guns.cyberdemonblasterreload` has no sound file in Techguns 2 or Fabric either |
| 3c projectiles: NDR beam (15 damage ticks, follows the shooter, looped fire sound, muzzle beam FX), tesla beam (lightning damage, TeslaFlare), FX-only projectiles (de-atomizer, alien blaster, nether blaster: particle trail + impact FX, no sprite), sonic wave (centre + 2 rings, shared hit list, passes through entities, knockback) | F | logic | tesla no-knockback waits for the knockback multiplier; Fabric declares tesla chain-lightning constants but never uses them; not ported |
| 3c ammo: redstone battery (+empty), nuclear power cell (+depleted), nether charge; recipes for all 3c guns (loaded + empty) and power sources | F | logic | alien blaster has no Fabric recipe |
| Model part transforms match Techguns 2: rotation order Z, Y, X and children inherit the parent rotation (the Fabric port differed); NDR, LMG and MAC-10 geo models re-exported | 12 | rendered | checked in game, geo and Java identical |
| 3d guns: grenade launcher, guided missile launcher, Grim Reaper; rocket launcher nuke and high velocity ammo | F + 12 | logic | rocket variants, guided missiles (incl. high velocity) and the Grim Reaper from Techguns 2 where Fabric had TODOs; checked in game |
| 3d explosion system: `TGExplosion` port (falloff, no damage through walls, block breaking by blast resistance, drop chance) used by rockets, missiles and grenades | 12 | tested | nuke radiation area and nuke/tesla knockback multipliers follow with the radiation system / knockback multiplier |
| 3d lock-on: hold use to lock (client `LockOnTracker`, beeps, HUD corners + target name), target sent with the shot and checked on the server; missiles turn up to 9 degrees per tick | F + 12 | logic | |
| 3d grenades: 40 mm launcher grenade (3 bounces, OBJ mesh), stick grenade and frag grenade (wind-up throw, Techguns 2) | F + 12 | logic | OBJ guns/grenades use `ObjMesh` (no GeckoLib version) |
| 3d recipes: 40 mm grenades, hand grenades, grenade launcher (empty), guided missile launcher (3), rocket launcher high velocity / nuke | F + 12 | logic | nuke rocket needs the tactical nuke warhead, high velocity rocket the chemical laboratory (Phase 5), Grim Reaper the ammo transfer recipe (3g) |
| 3e guns: flamethrower, biogun, TFG, magic rifle (Fabric only), hand cannon | F + 12 | tested | checked in game (magic rifle: normal, charged and fire-bullet shots) |
| 3e charge guns (`ChargeGunItem`): left click = normal shot, hold use = charge, release = charged shot using ammo in proportion to the charge; charge sound and charge FX run on every client from the synced using-item state; charge progress drives the gun renderer | F | tested | GameTests: full/partial charge ammo use, ammo-limited charge, charged TFG shot, normal shot |
| 3e projectiles: flame (fire, ignites, may light blocks, goes out in water), stone ball, bio blob shot (+ bio blob block), TFG plasma ball (charge-sized blast), magic rifle bullets (arcane / fire / lightning) | F | logic | magic rifle charge FX "tfgcharge" exists in no FX file (Fabric typo), so it has none |
| 3e ammo and recipes: fuel tank, bio tank (+empty), stone bullets, magic bullets (fire, lightning); flamethrower, biogun, TFG (loaded + empty), hand cannon (empty) | F | logic | magic rifle and magic bullets have no recipe in Fabric |
| 3f tools (`MeleeToolItem`): chainsaw, mining drill, power hammer, shishkebap (Techguns 2 only); fuel/air powers damage and mining, every hit and mined block uses a round, unpowered = weak club; mining heads (`MINING_HEAD` component) raise level, damage, speed; Techguns 2 sword attack speed | F + 12 | tested | GameTests: damage per fuel/head, fuel per hit (none in creative), mining level per head, fuel per mined block, power hammer charge ammo |
| 3f drill area mining: (2r+1)² plane at the speed of its hardest block, sneaking mines one block (NeoForge break speed/break events instead of mixins); plane outline client-side | F | tested | GameTest: plane facing the player, none while sneaking |
| 3f grappling hook, chainsaw/power hammer projectiles | F | tested | GameTests: tool shots hit, hook attaches to a wall; pull checked in game |
| 3f recipes: compressed air tank (+empty), mining heads, tools (empty, one per head), shishkebap (Techguns 2) | F + 12 | logic | head swap on an existing tool = mining head upgrade recipe (batch 3g); grappling hook has no recipe |
| 3g Ammo Bag: small (256) / large (512) ammo box, capacity weighted by stack size, only `#techguns:ammo`; bundle-style insert/extract and tooltip, fill bar; reloads count and take ammo from bags (loose items first), returned ammo and picked-up ammo go into bags | F | tested | GameTests: capacity, stack-size weight, non-ammo rejected, consume through bags, gun reload from a bag. No recipe in Fabric or Techguns 2 (creative only for now) |
| 3g recipe types: `ammo_change` (gun + ammo of another variant, old rounds come back), `mining_head_upgrade` (tool + head, old head comes back), `transfer_ammo` (shaped, result keeps the input guns' rounds: golden revolver, Infiltrator, Grim Reaper); Fabric `crafting_shaped_nbt` = vanilla shaped results with components | F | tested | GameTests: variant change + returned rounds/full magazine, head swap, golden revolver keeps rounds. `camo_change` (Camo Bench) and `ammobench_crafting` move to Phase 5 with their machines |
| 3g rifle rounds stack (+incendiary), minigun drum refill | F | logic | |
| Per-gun GameTests (exit criterion): every gun and tool fires and hits (tools at close range), reloads from its own ammo and returns the empties; incendiary/explosive variants; both hand grenades explode | — | tested | `TGBulletGunTests` generator + `TGExplosiveTests`; 149 GameTests |

### Phase 4 — armor & player gear

Batches: design doc Phase 4. Source: Techguns 2 for all values; Fabric for the armor tooltip, model code reference and the camo design.

| Unit | Source | Status | Notes |
|---|---|---|---|
| 4a armor framework (`TGArmorItem`, `TGArmorStats`): per-category armor as attributes (regular armor = physical, `techguns:armor.<category>` = the rest), toughness, 1.12 slot split and durability, worn-out pieces give nothing and never break, anvil repair item | 12 | tested | GameTests: category values, durability/repair, worn-out |
| 4a armor bonus attributes (speed x2 sprinting, water speed, jump, fall reduction/free height, mining, water mining, gun accuracy, cooling, oxygen gear, radiation resistance) applied like Techguns 2 | 12 | tested | night vision and step assist (toggle keys) follow with the items that have them; oxygen gear acts with scuba tanks (4f), radiation resistance with the radiation system |
| 4a armor tooltip: durability, toughness, bonuses + Fabric icon row of category values | 12 + F | logic | not checked in game (MCP cannot hover) |
| 4a T1 combat set | 12 | rendered | |
| 4b plain sets: T1 scout/miner, hazmat, T2 combat/commando/riot, T3 combat, beret (29 items, recipes, lang) | 12 | rendered | default camo textures only (variants in 4d) |
| 4b custom armor models: riot coat (`coat_0..3`), beret (`IClientItemExtensions#getHumanoidArmorModel`) | 12 | rendered | |
| 4c powered armor framework (`TGPoweredArmorItem`, `armor_power` component): powered/unpowered bonuses, chest refills from a battery item and tops up the other pieces of its power type, 1 power per piece per second of movement, action costs (jump 5 legs + steam jump sound, fall >3 blocks 5 boots, cooling 1 chest); radiation resistance independent of power | 12 | tested | GameTests: bonuses by power, battery swap/sharing, per-second use; checked in game |
| 4c step assist: `step_assist` bonus + toggle key (V) + saved player setting (`techguns:player_data` attachment, synced on login/respawn/dimension change) | 12 | rendered | step height 1.1 checked in game |
| 4c powered sets: steam, T3 power, T3 miner (HEV), T3 exo, T4 praetor, T4 power (24 items, recipes from 1.12 where they exist) | 12 | rendered | T4 sets have no 1.12 recipe; 1.12 3D inventory icons (render hack) replaced by plate icons; hide face/back/glove slot flags follow with 4f |
| 4c armor models: steam, T3 power, exo suit, T4 power mk2 (`TGArmorModels`, 1.12 values incl. its mirror-flag order) | 12 | rendered | |
| 4d camo system: datapack registry `techguns:camo` (named ids per item, synced to clients, datagen `TGCamoProvider`), `techguns:camo` component, gun renderer and armor textures use the current camo, sneak + use cycles armor camo (Techguns 2), tooltip shows the camo | F + 12 | tested | GameTests: cycling/wrap, unknown name falls back, armor sneak-use + texture; checked in game. Gun camo changes need the Camo Bench (Phase 5) |
| 4d camo lists: 11 guns (incl. Fabric-only magic rifle camos) and 14 armor groups, 1.12 textures and names, Fabric ids where they agree | 12 + F | rendered | T1 scout and T4 mk2 camo names invented (no 1.12/Fabric names); camo item icons do not change (1.12 had none) |
