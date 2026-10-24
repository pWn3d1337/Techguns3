# Techguns 3 — Design & Port Plan

Status: **Draft v0.2** (2026-10-24) — base version decided (1.21.1), to be refined step by step.

Techguns 3 is an AI-assisted port of Techguns (1.12.2, Forge) to modern Minecraft, done by the original author.

| | |
|---|---|
| First target | Minecraft **1.21.1**, **NeoForge 21.1.x** (Java 21) — decided, see Q1 |
| Later targets | Newest 26.x at that time (one direct jump, not every 1.21.x minor). Code is written migration-ready, see §4.7 |
| Reference A | `F:\Techguns3Work\Techguns2` — 1.12.2 Forge, full feature set, 761 Java files |
| Reference B | `F:\Techguns3Work\Techguns-Fabric` — 1.19.4 Fabric (Yarn), unfinished, gun mechanics + rendering working |

---

## 1. Goals and non-goals

**Goals**
- Feature parity with Techguns 2 (guns, armor, machines, NPCs, structures, radiation, damage system), rebuilt on modern, data-driven Minecraft systems.
- A clean architecture that survives multiple future MC version jumps with as little rework as possible (rendering in particular).
- Use the Fabric port as the primary source for anything rendering- and gun-mechanics related; use 1.12 as the source for everything the Fabric port never got to.
- Make the port AI-friendly: small, well-defined porting units, consistent conventions, datagen and automated tests so changes can be verified without manual play every time.

**Non-goals (for now)**
- World/save compatibility with 1.12 worlds. Not feasible; not attempted.
- Multi-loader (Fabric + NeoForge) builds in phase 1. Keep the door open (see §4.6), don't pay for it yet.
- Rebalancing. Port first with 1.12 numbers, rebalance in a later pass.

---

## 2. Inventory of what exists

### 2.1 Techguns 2 (1.12.2) — package sizes

| Package | Files | Notes |
|---|---|---|
| `client` | 186 | Java models (Techne-style `ModelBase`), gun renderers, particles system, screen FX |
| `world` | 76 | Ore gen, ~40 hand-coded structures, maze dungeon generator, `.ser` dungeon presets |
| `gui` | 72 | Machine GUIs, player tabs (extra slots), config GUI |
| `blocks` | 65 | Meta-enum blocks (concrete, lamps, ladders, camo nets, doors, ore clusters, machines) |
| `entities` | 65 | 26 NPCs, 30 projectile types, AI, spawn handling |
| `tileentities` | 57 | Machines, multiblock master/slave, turrets, doors, spawners, explosive charges |
| `items` | 55 | Guns, ammo, armor, powered armor, shields, additional-slot items, tools |
| `plugins` | 51 | JEI, CraftTweaker, Chisel, FTB Lib teams, Thermal Expansion |
| `packets` | 24 | |
| `util`, `api`, `recipes`, `capabilities`, `radiation`, `damagesystem`, … | ~90 | |

Content counts (1.12):
- **44 guns** (`TGuns.java`): ballistic, energy, charged (biogun, TFG, guided missile, power hammer, chainsaw), melee/tools (mining drill, shishkebap), grenades.
- **30 projectile classes**, **26 NPCs** (zombies, soldiers, bandits, super mutants, cyber demon, attack helicopter, alien bugs, turrets, …).
- **Machines**: Ammo Press, Metal Press, Chem Lab, Turret, Charging Station, Camo Bench, Repair Bench, Blast Furnace, Grinder, Armor/Upgrade Bench, Explosive Charges, Camp Flag, Dungeon Scanner/Generator; multiblocks Fabricator, Reaction Chamber, Ore Drill.
- **Player data** (`TGExtendedPlayer`): extra Face / Back / Hand slots, plus additional inventory, synced via `DataParameter`.
- **Assets**: 689 PNG, 405 OGG, 18 OBJ, particle-system definitions, Patchouli book, structure files, 3 serialized dungeon presets (`.ser`).

### 2.2 Techguns-Fabric (1.19.4) — what's reusable

- **Done-ish**: gun item framework (`GenericGun`, `GenericGunCharge`, melee charge, mining drill, chainsaw, power hammer, sonic shotgun, guided missile launcher), **43 gun entries** incl. new ones not in 1.12 (**Grappling Hook, Magic Rifle, Blaster Shotgun**), 18 projectile classes (consolidated vs 1.12, e.g. `GenericBeamProjectile`, `GenericProjectileFX`), 46 gun models, gun renderers (`RenderGunBase`, `RenderGunBase90`, `RenderGunBaseObj`, flamethrower, LMG mag, rocket), screen effects, particle system, death effects/gibs, custom OBJ loader, armor (incl. powered armor & bonuses), camo system, 8 NPCs, Ammo Bench + Camo Bench (new stonecutter-style screens), custom recipes (NBT shaped, ammo change, camo change, mining-head upgrade), REI plugin, Cloth Config.
- **Relies on 31 mixins** for hooks (held item rendering, arm pose, player model, HUD, mouse, camera). Many of these map to NeoForge events/extensions and will *not* need mixins on NeoForge (see §4.3).
- **Missing**: almost all machines and multiblocks, most NPCs, worldgen/structures, radiation, factions, turrets, doors, extra slots.
- **Naming**: Yarn mappings → must be translated to Mojmap/Parchment names (see Appendix A).

### 2.3 Source-of-truth rule

For each feature: **Fabric port if it exists there, else 1.12**. Fabric code is already in the modern rendering paradigm (PoseStack/VertexConsumer, ModelPart, BakedModel), which is the expensive part to redo.

---

## 3. Platform: what changed in Minecraft/NeoForge since 1.12 (and since 1.19.4)

These are the systems the port must be built on. Items marked **★** are particularly relevant for Techguns.

### 3.1 Core modding platform (NeoForge 1.21.1)
- **Toolchain**: Java 21, Gradle 8.x, **ModDevGradle** (NeoForge's new, simpler plugin; preferred over NeoGradle), official Mojang mappings + **Parchment** parameter names.
- **Registration**: `DeferredRegister` / `DeferredHolder` for everything. No meta-values — every 1.12 meta-variant (concrete types, lamp types, ore types, machine types…) becomes its own block/item. "Flattening" is a big but mechanical job.
- **★ Data Components (1.20.5)** replace item NBT. Gun state (`ammo`, `ammovariant`, `mode`, `camo`, `power`, `maxpower`, `miningHead`) becomes typed components with Codec + StreamCodec. Cleaner, auto-synced, and it's how vanilla does it from here on.
- **★ Data Attachments** replace Forge capabilities for *attaching data* (extended player, shooter values, radiation, death type, spawner NPC data). The new **capability** system only remains for *exposing* behaviour (item/fluid/energy handlers on blocks/items/entities). Note: built-in attachment sync arrived in later NeoForge versions (verify) — on 1.21.1 we sync manually via payloads, behind our own sync helper so it can be swapped for the built-in one on 26.x.
- **★ Networking**: `CustomPacketPayload` + `StreamCodec`, registered via `RegisterPayloadHandlersEvent`. All 24 packets get rewritten (mechanical).
- **Config**: `ModConfigSpec` (common/server/client), built-in config screen support in later versions.
- **Datagen**: recipes, loot tables, tags, block states, item models, lang, sounds, damage types, worldgen — **generate rather than hand-write JSON**. Very AI-friendly.
- **GameTest** framework for automated in-world tests (gun damage, ammo consumption, machine processing).
- **Mixins** are supported natively, plus Access Transformers. Prefer events/extensions first.

### 3.2 Gameplay systems
- **★ Damage types are a datapack registry (1.19.4+)** with tags (`bypasses_armor`, `is_projectile`, `is_fire`, `is_explosion`, …). Techguns' own `DamageType` enum (physical, projectile, fire, explosion, energy, ice, lightning, poison, radiation) maps naturally onto custom damage types + tags. The TG damage system (armor penetration, per-type armor values, death effects) hooks into `LivingIncomingDamageEvent` / `LivingDamageEvent`.
- **★ Armor materials are a registry (1.20.5)**, armor values via **attribute modifiers** on components. TG per-damage-type armor stats can become **custom attributes** (e.g. `techguns:energy_armor`) — composable, shows in tooltips, works with other mods.
- **★ Data-driven enchantments (1.21)** — gun-specific enchantments (e.g. extended mag, armor piercing) can be defined as datapack enchantments with effect components (relevant from 1.21 on).
- **Body armor slot** for wolves/horses (1.20.5) — NPC/animal armor extension point.
- **Recipes**: custom `RecipeType` + `RecipeSerializer` with codecs; machine recipes become datapack JSON (replaces most CraftTweaker integration for free). Smithing for upgrades is an option.
- **★ Trial Chambers / Trial Spawner / Vault (1.21)** — a vanilla model for wave-based spawners and per-player loot. Good inspiration (or direct reuse) for TG spawners and dungeon loot rooms.
- **Wind charge / mace (1.21)** — reference for knockback/area projectiles.
- **Crafter (1.21)** — vanilla automation for crafting; interacts with TG recipes for free.

### 3.3 Rendering
- **Item rendering**: gun items render via `IClientItemExtensions#getCustomRenderer` (BEWLR) on NeoForge, plus `getArmPose`, `applyForgeHandTransform` for first-person — replaces several Fabric mixins.
- **NeoForge OBJ loader** (`neoforge:obj`) is built in; Fabric's custom `TGObjLoader` likely not needed (verify feature parity: flip-v, custom texture handling).
- **Emissive layers / render types in model JSON** (NeoForge `item_layers`, per-element emissivity) — glowing energy-weapon parts without custom code.
- **Display entities** (block/item/text display, 1.19.4) and **interaction entities** — cheap holograms, decorative props, turret parts, dungeon signage.
- **Post-processing / screen effects** — night vision, radiation overlay, scopes. From 1.21.2 post chains are data-driven JSON.
- **Upcoming churn (important for architecture)**:
  - 1.21.2: entity rendering moves to **render states** (`EntityRenderState`).
  - 1.21.4: **client item model definitions** (`assets/<ns>/items/*.json`) with conditions/select/range properties and *special model renderers* — many gun visual variants (ammo state, camo, mode) can become data-driven.
  - 1.21.2+: equipment assets / `equippable` component for armor rendering.
  - 1.21.5: Blaze3D `RenderPipeline`/`GpuDevice` rewrite.
  - 1.21.6: deferred GUI rendering (`GuiRenderState`).
  - 1.21.9+: submit-node based rendering (`SubmitNodeCollector`) for entities/block entities.
  - 26.x: Mojang's new year-based versions, **unobfuscated** game jars, newer Java (25) and continued renderer work (Vibrant Visuals / possible Vulkan backend — verify when we get there).

  **Consequence:** every custom render path we write for 1.21.1 will need rework on the way to 26.x. See §4.3 and §4.7 for how we minimise this.

### 3.4 Interesting newer vanilla components for later versions (1.21.2 → 1.21.11)
Not available on 1.21.1, but design so we can adopt them:
- `use_cooldown`, `consumable` (medkits, stims, rad pills), `equippable` (face/back items), `damage_resistant`, `tooltip_display`.
- 1.21.11 weapon components (`attack_range`, `kinetic_weapon`, `piercing_weapon`, `swing_animation`, `damage_type`…) — relevant for melee tools (chainsaw, shishkebap, power hammer). Verify details at port time.
- Locator bar / waypoints (1.21.6) — possible hook for radar/GPS gear.

---

## 4. Architecture

### 4.1 Project layout (single NeoForge module, loader-isolated packages)

```
techguns/
  Techguns.java                  mod entrypoint, wires registries
  registry/                      TGBlocks, TGItems, TGEntities, TGSounds, TGComponents,
                                 TGAttachments, TGMenus, TGRecipes, TGDamageTypes, TGAttributes...
  api/                           public API (gun, damage, radiation, npc faction)
  core/                          loader-agnostic game logic (gun math, damage calc, radiation)
  gun/                           gun items, stats, projectile selectors, ammo
  entity/  projectile/  npc/     entities
  block/  blockentity/  menu/    blocks, machines, multiblocks
  armor/                         armor, powered armor, extra slots
  radiation/  faction/  worldgen/
  network/                       payloads
  data/                          datagen providers
  client/                        everything client-only (rendering, screens, input, FX)
    render/  model/  fx/  screen/  hud/
  compat/                        JEI/EMI, Curios/Accessories, Jade, KubeJS, ...
  mixin/                         only where no event/extension exists
```

Mod id: **`techguns`** (keep; existing asset paths and lang keys carry over).

### 4.2 Data-driven guns (proposal)
1.12 hardcodes ~44 guns as Java builder chains with stats. Proposal: keep **gun behaviour types in Java** (`GenericGun`, `ChargedGun`, `BeamGun`, `MeleeGun`, …) but move **stats into a datapack registry** (`data/techguns/techguns/gun_stats/m4.json`: damage, range, spread, fire rate, recoil, reload, ammo type, penetration, damage type, sounds). Benefits:
- Balancing and modpack tweaking without code (replaces `GunStatTweaker`).
- AI-assisted porting becomes "extract numbers from 1.12 → generate JSON", easy to verify.
- Server-authoritative, synced to client.
Decision needed: Q4.

### 4.3 Rendering strategy (the key decision)

Constraint: rendering is the most version-volatile area (see §3.3). Strategy:

1. **Port the Fabric render pipeline for guns first** (Java model classes + `RenderGunBase` family + `GunAnimation`), translated to Mojmap, hosted via `IClientItemExtensions`. Fastest route to "guns look right", proven design.
2. **Put a thin TG render abstraction** (details in §4.7) between gun logic and render code: `GunRenderer` interface receiving a TG-owned *render context* (transform type, recoil/reload/charge progress, muzzle flash state, hand). When vanilla moves to render states / submit nodes, only adapters change, not 44 gun renderers.
3. **Prefer data over code where vanilla supports it**: JSON/OBJ models for static parts, emissive layers in JSON, and later 1.21.4 item model definitions for variants.
4. **Evaluate GeckoLib** (see §5) for: NPCs with complex animation (helicopter, cyber demon, alien bugs), animated machines (ore drill, reaction chamber), and possibly reworked gun models with reload animations. GeckoLib tracks vanilla renderer changes for us — that's its biggest hidden value.
5. Keep Iris/Oculus (shaders) and Sodium/Embeddium compatibility in mind: avoid raw GL; use RenderTypes / VertexConsumers.

### 4.4 Player and entity data
- **Ammo storage — decided 2026-10-24:** the 1.12 TG player inventory (ammo slots in the `TGPlayerInventory` tab) is **dropped**. Use the Fabric port's **bundle-based Ammo Bag** instead (`techguns.items.AmmoBagItem`, `ammo_box` = 256 / `ammo_box_large` = 512 rounds, bundle tooltip). Reload pulls from bags in the player inventory, picked-up ammo goes into bags (`InventoryUtil` in the Fabric port). Port: contents as a data component (vanilla `BundleContents` or own codec component), logic via `GunData`-style accessors (§4.7).
- `TGExtendedPlayer` → data attachment `techguns:player_data` (extra-slot fallback inventory if no Curios, radiation, fire/reload timers, flags), copied on death/respawn as appropriate, manually synced on 1.21.1.
- Extra slots (Face / Back / Hand): **use Curios or Accessories** instead of the custom TG tab + slot system (Q5). Fallback: own attachment-backed inventory + a small screen tab.
- `TGShooterValues`, `TGDeathTypeCap`, `TGSpawnerNPCData` → attachments.

### 4.5 Machines
- One generic `TGMachineBlockEntity` base (inventory via `ItemStackHandler`, energy via NeoForge `IEnergyStorage` (FE), fluids via `FluidTank`, owner/security, redstone mode, progress), exposed through block capabilities so pipes/cables from other mods work.
- Machine recipes as custom recipe types (datapack JSON) — Ammo Press, Metal Press, Chem Lab, Fabricator, Reaction Chamber, Grinder, Blast Furnace, Charging Station, Ore Drill. Datagen from 1.12 `TGMachineRecipes`.
- Multiblocks: keep master/slave approach but with formed-state blockstates; consider a structure-matching helper (vanilla `BlockPattern`) for validation.
- Screens: `AbstractContainerMenu` + `AbstractContainerScreen`; reuse Fabric's `TGBaseScreenHandler` ideas. Reuse 1.12 GUI textures.
- Reconcile Fabric's new **Ammo Bench** with 1.12's **Ammo Press** (Q6).

### 4.6 Multi-loader stance
NeoForge-only for now. But: keep `core/` free of loader imports where cheap, and wrap loader calls (networking, registration, events) in a few helper classes. If we ever want Fabric again, an Architectury/MultiLoader split is then possible without a rewrite.

### 4.7 Migration readiness for 26.x (design rule)

Techguns 3 is built on 1.21.1, but **the later jump to 26.x must not turn into a second port.** Rule: put an abstraction layer at every place where we know (or expect) Minecraft/NeoForge APIs to change. Do **not** wrap stable things just for the sake of it (blocks, items, entities, registries, codecs, block entities are used directly).

**Known churn points and their TG layer**

| Area | What changes after 1.21.1 | TG abstraction |
|---|---|---|
| Entity / block-entity rendering | Render states (1.21.2), submit-node rendering (1.21.9+) | Write our renderers in **extract-state-then-render** style already on 1.21.1: `TGRenderState` records filled from the entity, then a renderer that only reads the state. All drawing goes through a small TG draw facade (wrapping `PoseStack` + `MultiBufferSource` now, `SubmitNodeCollector` later). |
| Gun / item rendering | Item model definitions + special model renderers (1.21.4), equipment assets | `GunRenderer` interface + immutable `GunRenderContext` (display context, hand, recoil/reload/charge progress, muzzle flash, camo, ammo state). 44 guns implement against this, never against `BlockEntityWithoutLevelRenderer` directly. One adapter hooks it into NeoForge. |
| Low-level rendering | Blaze3D `RenderPipeline` / `GpuDevice` (1.21.5), possible Vulkan backend in 26.x | No raw GL, no direct `RenderSystem` calls outside one `TGRenderTypes` class. Custom render types / shaders defined in one place. |
| GUI & HUD | Deferred GUI rendering (1.21.6) | Machine screens built from a small set of TG widgets (progress bar, energy bar, fluid tank, slot backgrounds) drawn via one helper wrapping `GuiGraphics`. HUD overlays registered in one `TGHudLayers` class. |
| Item data | New vanilla components (`use_cooldown`, `consumable`, `equippable`, weapon components…) | Typed accessors (`GunData.of(stack)`, `ArmorData.of(stack)`) instead of scattered `stack.get(...)` calls, so data can move to vanilla components in one place. |
| Player / entity data | Built-in attachment sync in newer NeoForge | `TGPlayerData` facade + own sync helper; swap internals later. |
| Networking | Payload API details | All payloads in `network/`, registered in one class, sent via `TGNetwork` helpers. |
| Identifiers & names | `ResourceLocation` → `Identifier` rename, other Mojmap renames | `TGIds.id("name")` helper; no `new ResourceLocation(...)` / factory calls spread across the code. |
| Damage | Damage-type / armor API tweaks | `TGDamage` factory for all TG damage sources; damage calculation in `core/` with plain inputs. |
| Optional mods | Library APIs differ per version (e.g. Curios vs Accessories, JEI vs EMI) | `compat/` only, behind small TG interfaces (e.g. `ExtraSlotProvider` with Curios impl + built-in fallback, recipe-viewer plugins). Core never imports a compat library. |

**Further rules**
- **Game logic in `core/`** (gun math, spread/recoil, damage calculation, radiation, recipe matching) uses as few Minecraft types as practical and no client classes → survives updates nearly untouched, and is unit-testable.
- **Event handlers stay thin**: subscribe, extract data, call into `core/`/feature code. Moving to renamed/split events is then a small change.
- **Mixins only as a last resort.** Each mixin gets a comment with *why* and *which 26.x API might replace it*.
- **Avoid deprecated and `@ApiStatus.Internal` APIs**; if unavoidable, mark them.
- **Data over code**: models (JSON/OBJ/GeckoLib), recipes, loot, gun stats, worldgen as data/datagen — data formats migrate more easily than Java code, and datagen re-generates them.
- **Marker comments**: tag every use of a known volatile API with `// MIGRATION(26.x): <note>` and keep a running list in `docs/migration_26.md`. The 26.x port then starts from that list instead of a search.
- **GameTests** for gun, damage, machine and recipe behaviour → after the jump, a green test run proves the logic still works; manual testing focuses on rendering.
- **Libraries**: prefer libraries that already exist on 26.x (§5.1: GeckoLib, Curios, JEI, Jade, Modonomicon, Player Animation Library). Libraries available only for 1.21.1 (Veil, EMI, KubeJS, CraftTweaker) stay optional compat, never hard dependencies.

---

## 5. Libraries & mod ecosystem

Versions to be pinned when setting up the project (Phase 0). Availability per MC version: §5.1. Prefer libraries that also exist on 26.x (see §4.7).

| Library | Use in Techguns 3 | Recommendation |
|---|---|---|
| **GeckoLib** | Animated entities, block entities, armor and items via Blockbench; keyframe animations, bones, sound/particle keyframes. Supported on NeoForge 1.20.6 and 1.21.x; GeckoLib 5 adopts the new render-state pipeline. | **Strong candidate** for NPCs, helicopter, animated machines. Evaluate for guns in a prototype (Phase 2b). |
| Vanilla keyframe animations (`AnimationDefinition`, Blockbench "Animation to Java" export) | Dependency-free alternative for entity animation (as used by Warden/Sniffer). | Fallback if we want zero dependencies for simple mobs. |
| **Curios API** / **Accessories** | Face / Back / Hand slots (gas masks, jetpacks, gloves, ammo pouches). Curios is the NeoForge standard; Accessories is cross-loader with Curios compat layer. | **Use Curios** (or Accessories if multi-loader becomes a goal). |
| **JEI** / **EMI** | Recipe viewers for machine recipes (1.12 had JEI, Fabric had REI). EMI is popular with modern packs and supports JEI plugins partially. | JEI first (largest install base), EMI plugin later. |
| **Jade** (or WTHIT) | Block info overlay for machine progress/energy. | Small, nice-to-have compat. |
| Patchouli / Modonomicon / **GuideME** | In-game manual. 1.12 already ships a Patchouli book. GuideME (AE2's guide system as a library) is modern and markdown-based. | Patchouli = fastest port of existing book, but only beta on 26.1 and missing on 26.2+ (§5.1). Modonomicon has full 26.x coverage → decide in Phase 8. |
| **Veil** / Lodestone | Rendering helpers: post-processing, dynamic lights, advanced particles/VFX. | Optional; evaluate for energy weapon VFX. Avoid hard dependency early. |
| Photon (LowDragLib) | Particle/VFX editor. | Probably overkill; TG has its own particle system already ported in Fabric. |
| **PlayerAnimator** / Player Animation Library | Third-person player animations (reload, aim poses, two-handed holds). | Evaluate; vanilla arm poses + NeoForge `ArmPose` extension may suffice. |
| KubeJS / CraftTweaker | Scripting integration. | Mostly obsolete thanks to datapack recipes and data-driven gun stats; add later on request. |
| FTB Teams / Open Parties and Claims | Team/ownership for turrets & factions (1.12 had FTB Lib). | Later, optional compat. |
| Cloth Config / Configured | Config GUI. | Not needed — NeoForge has config screens (and `ModConfigSpec`). |

### 5.1 Availability snapshot (NeoForge, checked 2026-10-24)

Source: NeoForge maven + Modrinth API (newest NeoForge build per game version). r = release, b = beta, a = alpha, — = none on Modrinth. JEI labels all Modrinth uploads as beta.

| | 1.20.6 | 1.21.1 | 1.21.11 | 26.1.x | 26.2 | 26.3 |
|---|---|---|---|---|---|---|
| **NeoForge** | 20.6.144 | 21.1.264 | 21.11.48 | 26.1.2.122 | 26.2.0.93 | 26.3.0.97 |
| **GeckoLib** | 4.5.4 r (2024-06, stale) | 4.9.3 r | 5.4.5 r | 5.5.2 r | 5.5.6 r | 5.5.7 r |
| **Curios** | 8.1.0 r | 9.5.1 r | 14.0.0 r | 15.0.0 r | 16.0.0 r | 17.0.0 **b** |
| **Accessories** | — | 1.1.0 b | — (last: 1.21.10) | — | — | — |
| **JEI** | 18.0.0 (2024-10, stale) | 19.69 | 27.53 | 29.47 | 30.47 | 31.16 |
| **EMI** | 1.1.22 r | 1.1.24 r | — | — | — | — |
| **Jade** | 14.2.3 r | 15.10.6 r | 21.1.7 r | 26.1.12 r | 26.2.10 r | 26.3.5 r |
| **Patchouli** | 86.1 r | 93 r | — | 94 **b** | — | — |
| **Modonomicon** | 1.73.3 r | 1.120.7 r | 1.121.0 r | 2.17.1 r | 2.5.0 r | 2.17.1 r |
| **GuideME** | — | 21.1.20 r | 21.11.1 b | 26.1.15 b | 26.2.2 a | 26.3.4 a |
| **Player Animation Library** | — | 1.1.6 r | 1.1.11 r | 1.2.8 r | 1.2.8 r | 1.2.8 b |
| **Veil** | — | 4.5.2 r | — | — | — | — |
| **KubeJS** | — | r | — | 8.0.7 b (26.1.2) | — | — |
| **CraftTweaker** | 19.0.13 r | 21.0.38 r | — | — | — | — |
| **Sodium** | — (Embeddium only) | r | b | r | r | b |
| **Iris** | — | r | r | r | r | r |

Modrinth mod counts tagged NeoForge (rough popularity indicator; multi-loader mods counted per tag):
1.20.6 ≈ 3.8k · **1.21.1 ≈ 21.6k** · 1.21.4 ≈ 9.5k · 1.21.11 ≈ 8.2k · 26.1.2 ≈ 8.2k · 26.2 ≈ 6.9k · 26.3 ≈ 4.8k.

### 5.2 Complementary tech mods (checked 2026-10-24)

Techguns usually ends up in tech-heavy modpacks; almost all of them are on 1.21.1. Downloads are Modrinth only (CurseForge is usually higher). "26.x" = any official NeoForge build for 26.1–26.3.

| Mod | Downloads | 1.21.1 NeoForge | 26.x | Relevance for Techguns |
|---|---|---|---|---|
| **Create** | 28M | ✔ | — | Biggest post-1.12 tech mod. Recipe compat (pressing, crushing, mixing). |
| **Applied Energistics 2** | 6.4M | ✔ | beta/alpha | Auto-crafting of TG items; needs standard recipe types. |
| **Ad Astra** | 6M | ✔ | — | Space mod (Galacticraft replacement on modern versions). Low overlap. |
| **Mekanism** (+ Generators) | 4M | ✔ | — | Ore processing recipes, radiation system (possible TG ↔ Mek radiation bridge). |
| **Immersive Engineering** | 3.7M | ✔ | — | Similar industrial theme; recipe compat (metal press, crusher). |
| **Refined Storage** | 3.1M | ✔ | ✔ | Storage/auto-crafting. |
| **Powah** | 2.6M | ✔ | alpha | FE power — works automatically. |
| **Modern Industrialization** | 2.1M | ✔ | — | GregTech-lite multiblocks; materials via common tags. |
| **Oritech** | 1.3M | ✔ | beta | New (2024), animated GeckoLib machines — close in feel to TG machines. |
| **Ender IO** | 1M | beta | alpha | Conduits, machines. |
| **Industrial Foregoing** | 1M | beta | — | Spiritual successor of MineFactory Reloaded. |
| **PneumaticCraft: Repressurized** | 0.8M | ✔ | — | |
| **Immersive Petroleum** | small | ✔ | — | Oil/fuel → flamethrower fuel, TG oil fields. |
| Thermal series | — | **—** (last: 1.20.1) | — | 1.12 had a Thermal plugin; postponed until Thermal updates. |
| Galacticraft | — | — | — | Only 1.12 Legacy official; Galacticraft 5 is Fabric-first pre-alpha. Not a target. |
| MineFactory Reloaded | — | — | — | Dead since 1.7.10. |

**Create add-on ecosystem** (all ✔ on 1.21.1): **Create Big Cannons** (8.2M — artillery, shells, explosives; closest thematic overlap), **Create Aeronautics** (9.5M) and **Create Propulsion: Simulated** (1M) for block-built aircraft, **Create: Diesel Generators** (6.4M — fuel fluids), **Create: New Age** (5.8M), **Create: Northstar** (space), **Create Factory Logistics**.

### 5.3 Competing gun / military mods

| Mod | Downloads | Versions | Notes |
|---|---|---|---|
| **TaCZ** (Timeless and Classics Zero) | **32.8M** | Forge 1.20.1 only | Market leader. Data-pack-driven guns (models, animations, stats) with community gun packs. **Not on 1.21.1 NeoForge.** |
| **Superb Warfare** | 3.4M | Forge 1.20.1, NeoForge 1.21.1 | New (2025), modern weapons + vehicles. Most direct competitor on our target version. |
| **Vic's Point Blank** | 1.9M | 1.21.1 → 26.1.2, all loaders | Realistic guns, attachments, animations. |
| **Scorched Guns 2** | 0.4M | Forge 1.20.1 | Successor of MrCrayfish's Gun Mod. |
| **Immersive Aircraft** / **Immersive Vehicles** | 15.7M / 0.9M | up to 26.x | Vehicles; complementary rather than competing. |

**Conclusions**
- TaCZ's absence on 1.21.1 NeoForge leaves a gap on our target version.
- TaCZ's success confirms **data-driven guns** (§4.2) as what players and packs expect. Later goal: third-party gun packs (gun stats + model + sounds as a resource/data pack).
- Techguns' differentiators — the things pure gun mods lack — must stay central: **tech progression and machines around the guns, multiblocks, NPC factions, structures, radiation, powered armor**.
- Modern gun-mod expectations to match: attachments, good first-person animations (reload/inspect), recoil feel, scopes. See §6.

### 5.4 Planned compat

Principle: **standards first, specific compat second.** Everything in tier 1 works with all mods above without mod-specific code. All mod-specific code lives in `compat/` and only loads when that mod is present (§4.7).

**Tier 1 — built in from the start (no dependency)**
- Common `c:` tags for all materials (ingots, plates, nuggets, dusts, ores, raw ores, storage blocks, fluids) — both providing and consuming them in recipes.
- **FE energy** (NeoForge `IEnergyStorage`) on all powered machines and chargeable items.
- NeoForge item and fluid handler capabilities on machines (pipes, conduits, AE2/RS interfaces work automatically).
- Standard recipe types + JSON for all TG machine recipes (auto-crafting systems and recipe viewers can see them).
- Fuel/oil fluids tagged with common fluid tags.

**Tier 2 — data-only compat (conditional recipes, no Java code)**
- Recipes for other mods' machines, loaded via NeoForge `neoforge:mod_loaded` conditions: Create (pressing, crushing, mixing), Mekanism (enriching, crushing, infusing), Immersive Engineering (metal press, crusher), Modern Industrialization, Oritech.
- Generated by datagen in Phase 5/8.

**Tier 3 — code compat (optional, later)**
| Mod | Idea | Phase |
|---|---|---|
| JEI (+ EMI) | Recipe categories for all TG machines | 5e |
| Curios | Face/back/hand slots (§4.4) | 4 |
| Jade | Machine progress, energy, owner | 5 |
| Mekanism | Radiation bridge (Mek radiation ↔ TG radiation), radiation-protection compat for hazmat suits | 8 |
| Create Big Cannons | TG explosives/materials as munition ingredients | 8+ |
| Fuel mods (Immersive Petroleum, Create: Diesel Generators) | Accept their fuels in flamethrower / generators | 8+ |
| Vanilla teams / FTB Teams / Open Parties and Claims | Team relations for targeting & ownership — see `TeamProvider` below | 6 (NPC/turret targeting) |
| KubeJS / CraftTweaker | Only if requested; datapacks cover most needs | later |

#### Team integration: `TeamProvider`

1.12 used FTB Lib (`plugins/ftbl/TeamSystemIntegration`) only to answer four questions about two player UUIDs — `isAllied`, `isAlliedNoMember`, `isTeamMember`, `isEnemy` — called from `TGNpcFactions` and `GenericGun` (who may turrets/NPCs/guns hurt or spare). FTB Lib/Utilities no longer exist; Techguns 3 replaces the hard integration with a small abstraction.

- **Interface** `compat/team/TeamProvider`: `isMember(UUID a, UUID b)`, `isAlly(a, b)`, `isEnemy(a, b)`, convenience `relation(a, b)` → `SELF | MEMBER | ALLY | NEUTRAL | ENEMY`. `TGNpcFactions`, turrets, guns and owned machines only talk to a `TGTeams` facade, never to a team mod.
- **Implementations** (all active at once; facade combines them — allied if *any* provider says allied, enemy only if *no* provider says member/ally):

| Provider | Availability | Notes |
|---|---|---|
| **Vanilla scoreboard teams** (`Entity#isAlliedTo`, `/team`) | Always | Zero-dependency fallback; respected by many mods and servers. |
| **FTB Teams** (successor of FTB Lib/Utilities teams) | 1.21.1 NeoForge (CurseForge only; used in FTB modpacks) | Members + allies. Whether an "enemy" relation still exists: verify against its API. |
| **Open Parties and Claims** (Xaero) | 24M Modrinth downloads, all loaders, 1.21.1 → 26.3 | Parties with members + allies via server API. No enemies. |

- **Enemies**: modern team mods appear to lack an enemy relation, so targeting modes become *attack all non-allies*, *hostile mobs only*, or *explicit enemies* (Techguns-own per-team/per-owner enemy list, or vanilla teams where useful).
- **Ownership**: owned machines/turrets (1.12 `BasicOwnedTileEnt`) use the same facade — configurable access for owner / team members / allies.
- Each mod provider loads only when the mod is present (§4.7); API changes on 26.x touch one adapter class.

---

## 6. New feature opportunities

Candidates, not commitments — to be triaged in a later refinement step.

- **Data-driven guns & attachments** (§4.2) — modpacks can add guns by JSON + model; later **third-party gun packs** (the TaCZ model, §5.3).
- **Gun attachments/mods system** (scopes, suppressors, extended mags) using data components; vanilla smithing table or TG Upgrade Bench as UI.
- **Custom attributes for TG armor stats** — compatible with other mods, visible in tooltips.
- **Datapack enchantments for guns** (1.21+).
- **Structures via jigsaw + structure templates**: rebuild the ~40 hand-coded structures and the maze dungeon as `.nbt` templates with template pools/processors (variety, biome-aware, datapack-overridable). Use structure sets for spacing.
- **Trial-spawner style TG spawners** and **vault-style** dungeon loot.
- **Display entities** for holograms (faction flags, machine status), decorative props.
- **Better scopes** using spyglass-like overlay + FOV events; thermal/night-vision post shaders.
- **Better first-person animations** (reload/inspect) if GeckoLib or a keyframe system is adopted for guns.
- **GameTests** for every gun/ammo combination as regression tests.
- **Advancements** as progression guide (vanilla advancement datagen).
- **Biome modifiers** for NPC spawning and ore placement — fully datapack-configurable.
- Fabric-port additions to keep: **Grappling Hook, Magic Rifle, Blaster Shotgun, Ammo Bench, new Camo Bench, Ammo Bag** (see §4.4).

### 6.1 Vanilla features since 1.12 to integrate (up to 1.21.1)

Accepted by the author in principle (2026-10-24); details refined per phase. Version = when the feature arrived in vanilla.

**Top picks**: ① gunfire noise → sculk/Warden + suppressors · ② trial-chamber-style TG dungeons & vaults · ③ smithing-template blueprints · ④ faction raids · ⑤ Gunsmith villager · ⑥ waterlogging + copper/tuff decorative blocks · ⑦ gun enchantments.

#### Combat & guns
| Feature (version) | Techguns integration | Phase |
|---|---|---|
| Sculk / vibration game events (1.17/1.19) | Gunshots, explosions, reloads emit vanilla game events → sculk sensors react, **Warden hunts loud weapons**. Suppressors / energy weapons emit weaker or no vibrations → real gameplay value for stealth gear; Ancient Cities become a stealth-with-guns challenge. | 3 |
| Crossbow hold pose (1.14) | Base for two-handed rifle arm poses (extended via NeoForge custom `ArmPose`). | 2b |
| Spyglass (1.17) | Zoom overlay / FOV handling as reference for scopes. | 2b |
| Swimming / crawling pose (1.13) | **Prone shooting** (less recoil/spread when crawling); projectile behaviour underwater (drag, reduced range). | 3 |
| Wind charge / mace / breeze (1.21) | Reference for knockback blasts and area effects (sonic shotgun, power hammer); breeze as target mob. | 3 |
| Trident (1.13) | Reference for returning / riptide-like projectiles; grappling hook behaviour. | 3 |
| Data-driven enchantments (1.21) | Gun enchantments: armor piercing, quick reload, extended mag, etc. | 3 |

#### Materials & progression
| Feature (version) | Techguns integration | Phase |
|---|---|---|
| Copper (1.17) | Drop TG's own copper, use vanilla / `c:` copper. | 1 |
| Raw ores, deepslate ores (1.17/1.18) | Raw-ore items and deepslate variants for every TG ore. | 1 / 7 |
| Netherite + smithing upgrades (1.16/1.20) | Netherite-tier armor upgrades; **smithing templates as blueprints** (e.g. "Gun Upgrade Template") found in TG structures and needed for advanced guns → ties exploration to progression. | 3–5, 7 |
| Amethyst (1.17) | Crystals / lenses for laser weapons. | 3 |
| Echo shards / sculk (1.19) | Exotic ingredients for high-tier tech (sonic shotgun, stealth gear). | 3–4 |
| Tinted glass (1.17) | Visors, goggles, machine windows. | 4 / 5 |

#### World & exploration
| Feature (version) | Techguns integration | Phase |
|---|---|---|
| New world height & caves (1.18, -64…320) | Deep underground bunkers; new ore height distributions. | 7 |
| Jigsaw structures (1.14+) | Varied military bases, cities, bunkers instead of fixed builds. | 7 |
| Trial chambers, trial spawner, vault (1.21) | Model for **TG dungeons**: waves of soldiers/mutants, per-player loot vaults, "ominous" harder variants. | 6–7 |
| Archaeology (1.20) | Suspicious sand/gravel in TG ruins & outposts: military relics, blueprints, rare ammo. | 7 |
| Ancient Cities / deep dark (1.19) | Stealth + guns (see sculk above). | 3 / 7 |
| Nether update (1.16) | New biomes, bastions, piglins → update TG nether structures & NPCs (Zombie Pigman Soldier → Zombified Piglin Soldier, gun-wielding Piglin Brute variant); piglin bartering can yield TG loot. | 6–7 |
| Data-driven dimensions (1.16+) | Optional big idea: wasteland / radiation dimension, mostly data. | later |

#### NPCs, mobs & villagers
| Feature (version) | Techguns integration | Phase |
|---|---|---|
| Raids / pillagers (1.14) | Template for **faction raids**: bandits/soldiers attack villages or player bases in waves. | 6 |
| Villager professions & job blocks (1.14) | **Gunsmith villager**, workstation = TG block (Ammo Press/Bench); trades ammo, parts, blueprints. | 6 |
| Wolf armor / body armor slot (1.20.5) | K9 armor for dogs, horse armor, armor for NPC mounts. | 4 / 6 |
| Goat horns (1.19) | Reference for alarm horns in military camps. | 6–7 |

#### Blocks & building
| Feature (version) | Techguns integration | Phase |
|---|---|---|
| Waterlogging (1.13) | Sandbags, camo nets, ladders, metal panels, doors waterloggable. | 5a |
| Copper bulbs / grates / doors, tuff bricks (1.21) | Match the TG bunker aesthetic; pair with concrete & metal panels; use in structures. | 5a / 7 |
| Hanging signs, lanterns, chains, scaffolding (1.14–1.20) | Military camp decoration in structures. | 7 |
| Crafter (1.21) | Vanilla automation of TG crafting recipes — free, just keep recipes standard. | 1+ |
| Display entities (1.19.4) | Holograms, faction flags, machine status displays. | 5 / 6 |
| Data-driven paintings & jukebox songs (1.21) | Propaganda posters, music discs as structure loot. | 7–8 |

#### Gear & items
| Feature (version) | Techguns integration | Phase |
|---|---|---|
| Bundles (`BundleContents` component, 1.20.5; item still experimental on 1.21.1) | **Ammo Bag** from the Fabric port (bundle-based, see §4.4). | 3 |
| Armor trims (1.20) | Trims on TG armor, camo as an option alongside. | 4 |
| Recovery / lodestone compass (1.16/1.19) | Reference for GPS / radar items. | 8 |
| Item cooldowns, food/consumable hooks | Medkits, stims, rad pills. | 4 / 8 |

---

## 7. Phased roadmap

Each phase ends in a runnable, testable build. Phases 3–8 can partly overlap once Phase 2 is solid.

### Phase 0 — Project setup
- ModDevGradle project, NeoForge 21.1.x for MC 1.21.1, Java 21, Parchment, `techguns` mod id, `neoforge.mods.toml`.
- Set up the abstraction-layer packages from §4.7 from the start (empty interfaces are fine), so code lands in the right place from the first feature on.
- Run configs (client, server, data, gametest), GitHub Actions build, `.gitignore`, license.
- `CLAUDE.md` with conventions for AI-assisted work (naming, package layout, mapping table, "how to port a unit" checklist).
- Copy over assets that are stable (textures, sounds, lang) — rename/flatten as needed.
- **Exit:** empty mod loads in dev client & server, datagen runs, CI green.

### Phase 1 — Core infrastructure
- Registries skeleton, creative tabs, config, networking base, data components, attachments, sounds, keybinds.
- Damage types + tags, TG damage system, custom attributes.
- Basic items (ingots, plates, components) + ores (no worldgen yet), with common `c:` tags from the start (§5.4 tier 1).
- **Exit:** items/blocks exist with models & recipes via datagen; damage system unit-tested via GameTest.

### Phase 2 — Gun framework (port from Fabric)
- 2a: `GenericGun` logic: firing, ammo & reload, magazines, ammo variants, zoom, recoil, spread, charge weapons, melee-charge, server/client fire sync, projectile selectors.
- 2b: Rendering: render abstraction (§4.3), port `RenderGunBase*`, `GunAnimation`, muzzle flash, scopes, arm poses, HUD/crosshairs, screen effects. **Prototype one gun in GeckoLib** in parallel to decide Q3.
- 2c: Projectile entities (bullet, beam, rocket, grenade, flamethrower, tesla, bio, …) + renderers + particle system + death effects/gibs.
- **Exit:** 3 representative guns fully working (M4 = ballistic, Lasergun = beam, Rocket Launcher = explosive) in SP and dedicated server.

### Phase 3 — All guns, ammo, grenades, tools
- Port remaining guns in batches (by projectile family), ammo items, magazines, Ammo Bag (§4.4), grenades, mining drill/chainsaw/power hammer/shishkebap, guided missiles, TFG.
- Fabric recipes (ammo change, camo change, mining-head upgrade) as custom recipe serializers.
- **Exit:** gun parity incl. Fabric additions; GameTests per gun.

### Phase 4 — Armor & player gear
- Armor materials, all armor sets, powered armor + energy, armor bonuses (night vision, step assist, jump, etc.), camo, shields.
- Extra slots via Curios (Q5): face (masks, goggles), back (jetpack, glider, backpacks), hand (gloves).
- **Exit:** armor parity; stats visible as attributes.

### Phase 5 — Blocks & machines
- 5a: Simple decorative blocks (flattened): concrete, metal panels, lamps, neon, ladders, camo nets, sandbags, stairs, slimy blocks, military crates, doors (2x1, 3x3 bunker).
- 5b: Single-block machines + screens + recipe types: Ammo Press/Bench, Metal Press, Chem Lab, Grinder, Blast Furnace, Charging Station, Repair Bench, Camo Bench, Upgrade/Armor Bench.
- 5c: Multiblocks: Fabricator, Reaction Chamber, Ore Drill (+ ore clusters).
- 5d: Turrets, explosive charges, camp flag.
- 5e: JEI (and later EMI) plugins.
- **Exit:** full tech progression craftable in survival.

### Phase 6 — NPCs & factions
- Base NPC with gun AI (ranged goals, strafing, reload), loadout system, factions.
- Port NPCs in families: zombies → humans (soldiers, bandits, commando, outcast) → mutants → nether/bosses (cyber demon, dictator dave, attack helicopter) → alien bugs. GeckoLib for complex ones (Q3).
- Spawning via biome modifiers + spawn placement; TG spawner block.
- **Exit:** all NPCs spawn naturally and in structures, use guns correctly.

### Phase 7 — World generation
- Ores + ore clusters as configured/placed features via datagen + biome modifiers.
- Structures: convert 1.12 hand-coded structures to `.nbt` templates (write a one-time converter running in 1.12 or reconstruct in-game), jigsaw pools, structure sets, processors, loot tables.
- Maze dungeons: convert `.ser` presets; evaluate jigsaw-based reimplementation vs porting the dungeon generator.
- **Exit:** structures generate in new worlds with correct loot and spawners.

### Phase 8 — Remaining systems & integrations
- Radiation system (attachment, mob effects, rad-away/pills, radiation sources, armor resistance).
- Guide book, advancements.
- Mod compat per §5.4: tier 2 conditional recipes (Create, Mekanism, IE, MI, Oritech) and tier 3 code compat (Mekanism radiation bridge, fuels, Create Big Cannons, team mods, scripting if requested).
- **Exit:** feature parity with Techguns 2.

### Phase 9 — Polish & first release
- Balancing pass, performance profiling (projectiles, particles), multiplayer testing, translations, release on CurseForge/Modrinth.

### Phase 10 — Version updates
- Jump from 1.21.1 directly to the newest 26.x at that time (no stop at every 1.21.x minor). Its own mini-plan: read NeoForge primers 1.21.2 → 26.x, rewrite the adapter implementations behind the §4.7 interfaces (rendering first), move data to new vanilla components where useful, re-run GameTests.
- Optionally keep a 1.21.1 maintenance branch for bugfixes.

---

## 8. AI-assisted porting workflow

- **Porting unit** = one gun / one machine / one NPC / one structure. Each unit has a checklist: logic, data (components/recipes/loot via datagen), assets, client rendering, lang, GameTest, manual check note.
- **Source lookup**: Fabric path → Mojmap translation (Appendix A) → else 1.12 path → modern equivalent.
- **Keep a porting tracker** (`docs/port_status.md`): table of every 1.12 content entry with status (not started / logic / rendered / tested / done).
- **No hand-written JSON** where datagen can produce it.
- **Verification — proportional to the change** (decided 2026-10-24):
  - Default: local `gradlew build`, plus `runData` / `runGameTestServer` when data or logic changed.
  - **Don't wait for CI on every simple step.** Push, keep working, and check the CI result later (before a phase or PR is considered done, or when a CI-relevant file changed: build scripts, workflow, dependencies).
  - **MCP screenshots / in-game checks only when needed**: new or changed rendering, GUIs, models/textures, particles/FX, or behaviour that GameTests can't cover. Not for pure logic, data or refactoring changes.
  - The author does the final visual review of rendering units.
- **One phase = one branch/PR series**; plan refined at the start of each phase.

### 8.1 Orchestrator and subagents (decided 2026-10-24)

The main session (**Claude Opus**) acts as **orchestrator**: it plans, decides, integrates and verifies. Simple, well-bounded tasks go to **cheaper subagents** when that saves cost or time without risking quality.

| Who | Model | Does |
|---|---|---|
| Orchestrator | Opus | Phase planning, architecture & abstraction layers (§4.7), rendering pipeline, cross-cutting systems (damage, networking, attachments), hard ports, design-doc updates, reviewing subagent output, final build/GameTest/MCP verification, commits. |
| Search / extraction agents | Haiku | Read-only lookups in the 1.12 and Fabric repos ("where is X", "list all uses of Y"), extracting data tables (gun stats, recipes, loot, lang keys) into structured form, inventory lists for `docs/port_status.md`. |
| Porting agents | Sonnet | Mechanical, pattern-following ports once a reference implementation exists: further items/blocks of an established kind, datagen entries, GameTest boilerplate, Yarn/MCP → Mojmap translation of self-contained classes, asset copying/renaming. |
| Review agents | Sonnet | Diff review against `CLAUDE.md` rules (TGIds, accessors, `compat/` isolation, `MIGRATION(26.x)` markers). |

**Rules**
- **Delegate only bounded work**: clear inputs, a reference example to copy, and a checkable result. Anything needing design judgement stays with the orchestrator.
- **First of a kind is done by the orchestrator** (first gun, first machine, first NPC…); following units of the same kind can go to porting agents.
- **Every brief** names the source files, target files/packages, the reference implementation to follow, the `CLAUDE.md` conventions, and what "done" means (compiles, datagen run, GameTest passes).
- **The orchestrator verifies** every subagent result: reads the diff, runs `gradlew build` / `runData` / `runGameTestServer` as relevant, and uses MCP in-game checks only for visual work (see verification rules in §8). A subagent's report is not proof.
- **No two writers on the same files.** Parallel writing agents get separate worktrees; read-only agents can run in parallel freely.
- **Subagents don't commit or push**; the orchestrator commits after verification.
- Don't spawn an agent for something a few direct tool calls settle — the cold start costs more than it saves.

---

## 9. Open questions / decisions for the author

| # | Question | Recommendation |
|---|---|---|
| Q1 | ~~Base version?~~ **Decided 2026-10-24: 1.21.1 / NeoForge 21.1.x.** Reasons: largest NeoForge mod base (≈21.6k on Modrinth vs ≈3.8k for 1.20.6), all libraries released (incl. EMI, Veil, KubeJS), NeoForge still maintained, closest to the Fabric 1.19.4 render code. Accepted cost: renderer rework on the later 26.x jump, mitigated by §4.7. | ✔ Decided |
| Q2 | Keep mod id `techguns` and asset paths? | Yes. |
| Q3 | Gun models: keep ported Java models (Fabric) vs convert to Blockbench/GeckoLib? | Port Java models first; prototype one GeckoLib gun in Phase 2b; decide based on result. Use GeckoLib for complex NPCs regardless. |
| Q4 | Data-driven gun stats (datapack registry) vs hardcoded? | Data-driven stats, Java behaviour types. |
| Q5 | Extra slots: Curios dependency vs own system? | Curios (soft or hard dependency — decide). |
| Q6 | Ammo Press (1.12) vs Ammo Bench (Fabric) — keep one, both? | Discuss. |
| Q7 | Energy: plain FE only? | FE only. |
| Q8 | Structures: faithful 1.12 rebuilds vs redesign with jigsaw variety? | Faithful first via converter, jigsaw where it adds value. |
| Q9 | Recipe viewer priority: JEI vs EMI? | JEI first. |
| Q10 | License for Techguns 3 (1.12 has `LICENSE.txt`, Fabric has `LICENSE`). | Author's call. |

---

## Appendix A — Name mapping cheat-sheet (1.12 MCP / Fabric Yarn → Mojmap)

| 1.12 (MCP) | Fabric 1.19 (Yarn) | NeoForge 1.21.1 (Mojmap) |
|---|---|---|
| `ResourceLocation` | `Identifier` | `ResourceLocation` (`fromNamespaceAndPath`; renamed `Identifier` in newer Mojang mappings (verify) — use a `TGIds` helper) |
| `NBTTagCompound` | `NbtCompound` | `CompoundTag` (items: Data Components) |
| `EntityPlayer` | `PlayerEntity` | `Player` |
| `World` | `World` | `Level` |
| `TileEntity` | `BlockEntity` | `BlockEntity` |
| `Container` | `ScreenHandler` | `AbstractContainerMenu` |
| `GuiContainer` | `HandledScreen` | `AbstractContainerScreen` |
| `DataParameter` / `EntityDataManager` | `TrackedData` / `DataTracker` | `EntityDataAccessor` / `SynchedEntityData` |
| `ModelBase`/`ModelRenderer` | `Model`/`ModelPart` | `Model`/`ModelPart` |
| GL calls / `Tessellator` | `MatrixStack`, `VertexConsumer` | `PoseStack`, `VertexConsumer` |
| `ItemCameraTransforms.TransformType` | `ModelTransformationMode` | `ItemDisplayContext` |
| `EnumHand` | `Hand` | `InteractionHand` |
| `DamageSource` (string types) | `DamageSource` + `DamageType` | `DamageSource` + `DamageType` (registry) |
| Capabilities | Mixin + interfaces | Data Attachments / Capabilities |
| `SimpleNetworkWrapper` | Fabric networking | `CustomPacketPayload` + `StreamCodec` |
| `MinecraftClient` (Yarn) | `MinecraftClient` | `Minecraft` |
| `Vec3d` | `Vec3d` | `Vec3` |
| `HeldItemRenderer` (Yarn) | `HeldItemRenderer` | `ItemInHandRenderer` |

(To be extended as porting proceeds.)

## Appendix B — Fabric mixins → NeoForge replacements (to verify in Phase 2)

| Fabric mixin | Likely NeoForge replacement |
|---|---|
| `HeldItemRendererMixin`, `ItemRendererMixin` | `IClientItemExtensions` (`getCustomRenderer`, `applyForgeHandTransform`), `RenderHandEvent` |
| `PlayerEntityRendererMixin`, `CrossbowPosingMixin`, `AnimalModelMixin`, `ModelPartMixin` | `IClientItemExtensions#getArmPose` + custom `ArmPose`, `RenderPlayerEvent` |
| `InGameHudMixin` | `RegisterGuiLayersEvent` (GUI layers), `RenderGuiLayerEvent` |
| `MouseMixin`, `MinecraftClientMixin` | `InputEvent.InteractionKeyMappingTriggered`, `ClientTickEvent`, `ComputeFovModifierEvent`, `ViewportEvent` |
| `GameRendererMixin`, `CameraAccessor` | `ViewportEvent.ComputeCameraAngles` / `ComputeFov`, post effects |
| `LivingEntityMixin`, `DamageSourceMixin` | `LivingIncomingDamageEvent`, `LivingDamageEvent`, `LivingDeathEvent` |
| `TGPlayerEntityMixin(Client)` | Data attachments + `PlayerTickEvent` |
| `ModelLoaderMixin`, `BakedModelManagerAccessor` | `ModelEvent.RegisterAdditional`, `ModelEvent.ModifyBakingResult`, `ModelEvent.RegisterGeometryLoaders` |
| `NetworkStateMixin`, `ClientPlayNetworkHandlerMixin` | Payload registration, `RegisterClientPayloadHandlersEvent`-style handlers, entity spawn data via `IEntityWithComplexSpawn` |
| `ShapedRecipeAccessor` etc. | Custom recipe serializers / access transformers |
| `WorldRendererMixin` | `RenderLevelStageEvent` |
