# Gun animations in models — migration plan (draft, 2026-11-03)

Branch: `gun-anim-geckolib`. Follows the full-GeckoLib decision in design doc §9.1.

## Goal

A gun is fully described by data, and **all motion is defined in the model and animation files**. Code only supplies the inputs (when a shot fires, timer progress, ammo, spin speed) and draws what isn't model geometry (effects, scope overlay).

The files per gun:
- one Blockbench model (`geo/item/<gun>.geo.json`);
- the item model JSON with Blockbench's Display settings (hold positions);
- keyframed animations (`animations/item/<gun>.animation.json`, plus shared ones);
- a small display JSON for effects and flags.

Addons and resource packs can then add or reskin guns without Java. The Java models (`ModelXxx`, `TGModelPart`), the `transformPart` / `animateGeoBones` code and the per-gun numbers in `TGItemRendererSetup` go away.

## Where things are today

| What | Where | Count |
|---|---|---|
| Gun renderers | `TGItemRendererSetup` (60 registrations), `RenderGunBase` / `RenderGunBase90` (90° base rotation), `RenderMiningTool`, `RenderFlamethrower`, `RenderGunBaseObj` (grenade launcher, OBJ) | 46 guns |
| Whole-gun recoil | `GunAnimation` presets in code: `genericRecoil` ×29, `swayRecoil` ×2, `pulseRifleRecoil` ×1, scope recoil; strength per gun via `setRecoilAnim(anim, move, turn)` | 32 guns |
| Whole-gun reload | `genericReload` (default) ×40, `breechReload` ×6 | 46 guns |
| Part motion in code | `TGModel.transformPart` / `animateGeoBones` in 16 models: AUG, chainsaw, combat shotgun, grappling hook, Grim Reaper, GML, LMG, Lasergun 2, M4 Infiltrator, minigun, mining drill, NDR, pistol, power hammer, SCAR, TFG | 16 |
| Split geo files (`_1`, `_2`) | Needed because a part moves in code, renders translucent/cutout (NDR, GML) or has a per-ammo texture (rocket launcher rocket) | 12 guns |
| Keyframe animations | `m4.animation.json` (reload) only | 1 |
| Muzzle flash / charge / idle FX position | Hand-tuned numbers per gun: `setMuzzleFx(x, y, z, scale, xLeft)`, `setMuzzleFXPos3P(y, z)`, `setChargeFx…`, jitter. Drawn in the hand pose *without* the gun's own transforms, so the flash does not follow recoil | ~40 guns |

## Target design

### Bone naming convention
Shared animations and code find bones by name. Every gun uses:

| Bone | Purpose |
|---|---|
| `root` | Whole gun; shared recoil/reload animations move this. |
| `magazine`, `bolt`, `slide`, `barrel`, `hammer`, `cylinder`, `belt`, `drum` | Standard moving parts, so shared part animations fit many guns. |
| `muzzle`, `muzzle_2`, … | Empty marker bones at the barrel tip: muzzle flash (below). |
| `charge`, `idle_fx` | Empty marker bones for the charge effect and idle FX (flamethrower pilot flame). |
| `*_glow` | Drawn full bright (already supported). |
| `*_translucent` / `*_cutout` | Bone drawn in a second pass with that render type; replaces the split files of NDR and GML. |
| `ammo_*` | Variant bones shown only for the matching loaded ammo (rocket launcher rocket), instead of a separate geo file and texture swap. |

Missing bones are fine: GeckoLib skips animation tracks whose bone the model lacks.

### Model orientation
`RenderGunBase90` turns half the guns by 90° in code. The converter bakes that turn into the geo file, so every gun points the same way (barrel along −Z, Blockbench "north"). Without that, a shared `root` animation would kick sideways on half the guns.

### What moves where

| Motion | Today | Target |
|---|---|---|
| Part motion (magazine, bolt, slide, pump, belt, barrels, chain, drill head) | `transformPart` / `animateGeoBones` code | keyframes on the part bones |
| Whole-gun recoil, reload tilt | `GunAnimation` presets | keyframes on `root` (needs the normalized orientation) |
| Aim down sights | `setAdsOffsets` numbers | `aim` animation on `root`, played by zoom progress (also backwards when zooming out) |
| Hold positions (first/third person, GUI, ground, item frame) | `setTransformTranslations`, base scale/translation, GUI scale, `RenderGunBase90` | Blockbench Display settings → `display` block of the item model JSON, tuned visually in Blockbench |
| Muzzle / charge / idle effect position | per-gun offset numbers | marker bones |
| Effect and sound timing (reload sounds, shell eject, flash) | fixed in code | sound and instruction keyframes (e.g. `muzzle_flash` at 0.0, `eject_shell` at 0.15) |
| Variable motion (spin-up, ammo left, charge) | code per model | Molang in keyframes; code only supplies the query values |

**Stays in code**, because it is not model motion:
- **When things happen:** firing, reload start, zoom, charge (gameplay and stats).
- **Durations:** every animation is stretched to the gun's real stats (decision 4).
- **Effect drawing:** muzzle flash quads, beams, scope overlay. Position and timing come from the files.
- **Player arm pose:** rifle or pistol hold, one setting in the display JSON.
- **Camera:** FOV zoom while scoped. There is no screen-shake recoil today.

### Animations
**Playback: sampled by Techguns, not GeckoLib controllers.**
- Every gun animation is a pure function of the existing timers: animation time = timer progress × file length.
- A small interpolator reads GeckoLib's parsed keyframes (`Keyframe` is a public record with Molang values; linear, Catmull-Rom and step) and sets the bone poses before the model is drawn.
- **Exact durations:** stretching to the stat durations is exact.
- **Reversible:** `aim` can run backwards.
- **Stateless:** no animation state per holder or hand. The keyed-animatable workaround of §9.1 goes away, and NPCs and turrets cost nothing extra.
- Several animations layer in a fixed order (hold → aim → reload → fire → loop), each adding to the bone pose.
- Instruction and sound keyframes fire when the sampled time passes them.
- **Fallback:** if the pistol prototype shows the own sampler is more work than expected, use GeckoLib controllers with our own clock (`GeoAnimatable.getTick` returns the timer-based time).

| Animation | Names | Time from |
|---|---|---|
| fire | `fire`, `fire_b` (alternating second shot, e.g. double barrel), `fire_last` (empty mag: slide stays back) | recoil timer, started by `ClientGunController.onFired`; stretched to the recoil time, capped by the fire interval |
| reload | `reload`, `reload_empty` (optional) | reload timer, stretched to the gun's reload time |
| aim | `aim` | zoom progress (0..1, either direction) |
| charge | `charge` | charge progress (gauss rifle, TFG) |
| loop | `spin`, `idle` | looping; speed from a Molang query (minigun spin-up, chainsaw chain, drill head) |

Lookup order per animation name:
1. the gun's own file;
2. the files listed in its display JSON (`"animations": ["techguns:common"]`);
3. `common.animation.json`.

A gun without a `reload` of its own gets the shared one.

**Shared file `animations/item/common.animation.json`:** `recoil_generic`, `recoil_sway`, `recoil_pulse`, `recoil_scope`, `reload_generic`, `reload_breech`, `reload_mag` (drops `magazine`, pulls `bolt`). These are the `GunAnimation` presets redone as keyframes on `root`.

**Molang queries** (registered via `MathParser.registerVariable`, set before each gun render):

| Query | Meaning |
|---|---|
| `query.tg_recoil_move`, `query.tg_recoil_turn` | Per-gun strength of a shared recoil from the display JSON (replaces the `setRecoilAnim` numbers; default 1). A gun with its own `fire` keyframes doesn't need them. |
| `query.tg_fire`, `query.tg_reload`, `query.tg_charge`, `query.tg_zoom` | 0..1 progress of the existing timers. |
| `query.tg_ammo` | Loaded ammo fraction (magazine bullets, LMG belt length, Grim Reaper). |
| `query.tg_spin` | Barrel spin angle / speed. |

### Muzzle and effect positions from bones
Yes: the flash position comes from the `muzzle` bone instead of the hand-tuned numbers.
- While GeckoLib renders the model, the bridge captures the pose matrix at the marker bones: override `renderRecursively`, or enable tracking matrices on those bones and read `getModelSpaceMatrix`. The matrix already contains the hand pose, base scale and rotation, and the bone animation.
- The flash is drawn at that matrix, with only scale, jitter and effect type left in the display JSON. One position then serves first person, third person, left hand, offhand and NPC/turret holders.
- This replaces `setMuzzleFx` x/y/z/xLeft, `setMuzzleFXPos3P` y/z, and likewise the charge and idle FX positions.
- **Change:** the flash then moves with recoil (today it stays still while the gun kicks). Per gun, `"muzzle_follows_animation": false` keeps the old look (see decision 1).
- Several barrels: `muzzle`, `muzzle_2`. The fire pass picks the barrel by shot index (double shotgun, alternating guns). The minigun keeps one bone outside the spinning barrel.
- **Scope / ADS:** the flash is not drawn while scoped, as today.
- **Projectile spawn stays server-side** at the eye offset (`GenericProjectile`, `GenericBeamProjectile` `SIDE_OFFSET` 0.16): the server has no models.
  - Optional later: a datagen step reads each gun's `muzzle` bone and writes a spawn offset into the gun stats.
  - Optional later, client only: the owner's first-person beam start piece is drawn from the muzzle bone, so lasers visibly leave the barrel.

### Hold positions (item model JSON)
- The hold positions move to the `display` block of `models/item/<gun>.json` (parent `builtin/entity`), exactly what Blockbench's Display tab edits and exports.
- The renderer applies that transform per `ItemDisplayContext` instead of `setTransformTranslations` and the base scale/rotation code.
- **Conversion:** a one-off `/tgdebug` export composes today's code transforms per context (base translation, scale, rotation, per-context translations, GUI scale) into Blockbench display values. Datagen then writes them.
- First-person recoil, reload and aim are then relative to the Display pose, and the Display pose is tuned visually in Blockbench.

### Display JSON (`assets/techguns/gun_display/<gun>.json`, generated by datagen)
Only what isn't motion:
- muzzle/charge/idle effect type, scale and jitter, `muzzle_follows_animation`;
- scope overlay and its scale;
- player arm pose;
- the shared animations used (names) and the `tg_recoil_move` / `tg_recoil_turn` multipliers.

A resource pack can override this file, and an addon gun needs only geo, item model, texture, animation and display files.

## Phases

1. **Infrastructure (Opus)**
   - Generalise `GeckoLibGunRenderer`:
     - one geo per gun;
     - per-bone render passes (`_translucent`, `_cutout`, `_glow`);
     - `ammo_*` bone visibility;
     - the keyframe sampler with layering, instruction and sound keyframes;
     - Molang queries;
     - marker bone matrix capture;
     - hold positions from the item model `display` block.
   - Display JSON loader (client reload listener) and datagen provider.
   - `/tgdebug` export of today's per-context transforms and ADS offsets as display values / `aim` keyframes. The first datagen run writes today's values, so nothing changes visually.
2. **Prototype: pistol**
   - Merge `pistol` + `pistol_1`.
   - `slide` keyframed for `fire` / `fire_last`, `reload_mag` from the shared file, `aim`, `muzzle` bone, hold positions from its item model.
   - Decides sampler vs. GeckoLib controllers with our own clock.
   - Compare with the old renderer in game via `/tgdebug geo`.
3. **Shared animations**
   - Recreate the `GunAnimation` presets in `common.animation.json` on `root`.
   - Bake the 90° orientation into all geo files (re-run the exporter with the turn applied; redo the M4 animation pivots).
   - Check side by side on one gun per preset: M4 generic, sway gun, pulse rifle, a breech-reload gun, scope recoil.
4. **Bulk: guns without part motion (~30, Sonnet)**
   - Display JSON entries plus `muzzle` bone placement; delete their code transforms.
   - Muzzle bone positions: start from the old offsets, inverse-transformed into model space by a one-off `/tgdebug` export so they match today's look, then checked per gun in game.
5. **Part-motion guns (16, Opus first of each kind, Sonnet the rest)**
   - Replace each `transformPart` / `animateGeoBones` with keyframes or Molang:
     - minigun spin (`tg_spin`);
     - LMG belt (`tg_ammo`);
     - chainsaw / drill / power hammer (loop + fire);
     - grappling hook (3 parts → bones);
     - NDR, GML (translucent bones);
     - SCAR, AUG, combat shotgun (bolts / pump);
     - TFG, Grim Reaper, Lasergun 2, M4 Infiltrator (laser bone hidden in GUI → `query.tg_gui` or a display flag).
   - Merge the split geo files.
6. **Special renderers**
   - Rocket launcher rocket → `ammo_*` bones.
   - Flamethrower → idle FX bone.
   - Mining tools → `RenderMiningTool` logic via queries.
   - Grenade launcher: convert the OBJ to geo if the shape can be kept (decision 3).
7. **Cleanup**
   - Delete the Java gun models, `TGModelPart` gun usage, `GunAnimation` presets, the `transformPart` / `animateGeoBones` hooks, the fallback path, the `TGItemRendererSetup` transform numbers and `RenderGunBase90`.
   - Keep `TGGeoExporter` until the last gun is converted.
   - Update design doc §9.1, `port_status.md` and `migration_26.md` (GeckoLib 5 render-state rewrite of the bridge).

## Progress
- **2026-11-03, phase 1 core + pistol:**
  - `GunAnimationSampler` replaces GeckoLib's controllers. `GeckoLibGunRenderer` overrides `GeoModel#handleAnimations`: every bone is reset to its rest pose, then `aim`, `reload`, `fire`/`fire_last` and `charge` are sampled from `GeoAnimInput` (timer progress) and added, then the code bones are applied. One animatable per gun model; the keyed per-holder states are gone.
  - Marker bones (`muzzle*`, `charge*`, `idle_fx*`) are captured in `renderCubesOfBone` at their pivot. `RenderGunBase` draws the muzzle flash there when the model has a `muzzle` bone (first and third person), else at the old offsets.
  - **Pistol:** `pistol_1.geo.json` is merged into `pistol.geo.json` (`slide` group bone, `muzzle` marker). `pistol.animation.json` has `fire` and `fire_last`; the slide stays back while the gun is empty.
    - The slide travel is 2.4 px as in Techguns 2: the Fabric port's `* scale` had shrunk it to 0.15 px.
    - A geo gun listing fewer geo ids than render parts no longer draws the rest; the Java fallback still has the separate part.
  - **M4:** the M4 `reload` now lasts exactly the reload time; before, it played at its authored 2.25 s.
  - Verified in game: first person, third person, flash at the barrel tip, slide lock, M4 reload.
- **2026-11-03, whole-gun motion from files (pistol):**
  - `common.animation.json` holds the `GunAnimation` presets as keyframes on `root`: `recoil_generic`, `recoil_pulse`, `recoil_scope`, `recoil_sway`, `reload_generic`, `reload_breech`.
    - The easings are exact: `easeoutquad` = `C_FAST`, `easeinoutsine` = `C_SMOOTH`, and the sway uses Molang `math.sin`.
    - Authored per unit: offsets per 1 `move`, rotations per 1 degree of `turn`.
    - Tilted reload axes are converted to Euler angles at the reference angle (40 and 55 degrees), exact there; the ease-in and ease-out deviate by up to about 2 degrees.
  - **Display file:** `assets/<ns>/techguns/gun_display/<gun>.json` (`GunDisplay`, loader `GunDisplays`).
    - `recoil` / `reload`: animation, `move` (model pixels) and `turn` (degrees) for `first_person`, `aiming` (optional) and `third_person`; plus `muzzle_follows_animation`.
    - Datagen (`TGGunDisplayProvider`) writes it from the renderer's presets (`RenderGunBase#exportDisplay`) for the guns in `TGItemRendererSetup.DATA_DRIVEN` (pistol only).
  - **Renderer:** for a gun with a display file and a drawn geo model, `RenderGunBase` skips the code presets and ADS shift and hands everything over in `GeoAnimInput`.
    - Sampled layers: `aim`, `reload` + reload motion, `fire`/`fire_last` + recoil, `charge`.
    - The left hand mirrors the `root` motion (x offset, y and z turn).
  - **Root pivot:** the `root` pivot must be the hand origin, i.e. minus the renderer's base translation (pistol: `[-0.5, -4.8, 6.4]`). Then whole-gun motion in model space equals the old hand-space motion: the gun base rotation and the bridge's 180 degree turn cancel, so the model axes are the hand axes.
  - **Pistol aim:** the pistol's `aim` animation is the default ADS shift in model pixels.
  - **Rest marker:** `muzzle_follows_animation: false` uses the marker rest pose (`muzzle#rest`: only the aim layer applied).
  - **Verified in game with `/tgdebug pose <fire|reload|zoom> <progress>`** (freezes the held gun's timers) against `/tgdebug geo` (the code path), all identical:
    - recoil peak, reload hold, full aim;
    - third person reload;
    - left hand reload.
  - **Fix found on the way:** `TGMatrixOps.rotate` passed unnormalized axes to JOML (non-unit quaternion, skewed pose); Techguns 2's `glRotatef` normalized them. Now normalized for every gun, which changes the generic and sway animations of all guns slightly back to Techguns 2.
- **2026-11-04, hold positions from the item model (pistol):**
  - The pistol's `models/item/pistol.json` has a `display` block for every view: first and third person, both hands, GUI, ground and frame.
    - Datagen writes it from the renderer's code placement (`RenderGunBase#exportItemTransforms`): context translation, GUI and frame turns, base scale, rotation and translation, and the geo bridge's turn.
    - Each view's chain is decomposed into translation (pixels), rotation (XYZ degrees, as vanilla's `rotationXYZ`, own decomposition because JOML's loses the 90 degree frame turn) and uniform scale.
    - Left-hand views are stored mirrored, because vanilla mirrors them when applying.
  - **Renderer:** for a data-driven gun, `RenderGunBase` undoes vanilla's display transform at the start, so effects and the scope overlay stay in the old frame. It applies it again instead of its code chain right before drawing the model, in geo space.
    - The two code turns about the hand that depend on state now turn the `root` bone: lowered offhand gun (`holdPitch`) and the crossbow hold in third person (`holdYaw`).
    - With `/tgdebug geo` off, the code path still runs, since the display transform is undone there too.
  - **Verified in game against the code path**, identical: first person reload, third person reload, item frame, hotbar and inventory icon; the dropped item matches apart from its spin.
  - **Open:** whether Blockbench's Display tab previews these values exactly.
    - Here the geo model origin sits at the item centre (vanilla's -0.5 and our +0.5 cancel), as GeckoLib's `GeoItemRenderer` does apart from its 0.01 lift.
    - To check once in Blockbench with the GeckoLib plugin's animated item format.
- **2026-11-04, 90 degree guns normalized:**
  - `tools/geo_turn.py` bakes the code turn of `RenderGunBase90` (Ry(90) in Java model space) into the geo files of all 22 guns (36 files, mining tools and flamethrower included). Every gun now points along -Z.
    - Bones: pivot turned, rotation conjugated (N R N^-1, recomposed as Euler angles).
    - Cubes: the box is turned and stays axis aligned. The faces land on other sides and top and bottom turn, which box UV cannot express, so turned cubes get per-face UV with `uv_rotation` (format 1.21.0, which Blockbench reads and writes). The UVs are found by emulating GeckoLib's quad building: every corner keeps the UV it had.
    - Flat cubes (a size of 0) keep their box and get a cube rotation `[0, 90, 0]` about the origin instead: both faces on their plane draw, the later one wins, and rebuilt faces would swap which is drawn last (seen on the GML's holo sight, 20% brighter).
    - Checked by the script's own test: one turn moves every quad corner and UV exactly, four turns give back the file.
  - **Renderer:** for a geo draw, `RenderGunBase90` uses the normal base rotation and turns the base translation along ((x, y, z) -> (z, y, -x)). The Java fallback (`/tgdebug geo`) keeps the 90 degree turn.
    - `GeoGunModel#turned` turns what still works in Java model space: `transformPart` (turned there and back), code bone offsets and scales, and the Java fallback for a missing geo file.
  - **Verified in game, geo vs Java fallback, pixel diff of screenshots** (0 or 1 differing pixels): revolver, minigun barrel spin (fire pose), combat shotgun pump (fire pose), TFG (code bone offsets), mining drill spin, lasergun (per-face glow cubes), GML, rocket launcher, grappling hook, and all hotbar icons. Third person only by eye: the idle arm sway moves between frames even with `tick freeze`.
  - `/tgdebug exportgeo` still writes the unturned files; a re-export of a 90 degree gun needs `geo_turn.py` again.
- **Next:**
  - generating `aim` and the root pivot per gun when converting;
  - charge translation;
  - Molang queries;
  - sound and instruction keyframes.

## Verification
- Before deleting a gun's code path, compare screenshots of the old and new render (first person idle/fire/reload, third person, GUI) via MCP.
- **Resource check:** a client reload listener logs every gun whose display JSON names a missing geo, animation or texture, or whose model lacks a `muzzle` bone while it has a muzzle effect. This runs in the dev client and is checked once per batch.
- `/tgdebug bench`: confirm no frame-time regression with many NPCs holding geo guns (§9.1 never measured it).
- GameTests are unaffected (server side), but run them anyway after cleanup.

## Decisions (author, 2026-11-03)
1. **Flash and recoil:** an option per gun.
   - Display JSON flag `"muzzle_follows_animation"`, default `true`.
   - When `false`, the effect sits at the bone's rest position: the bone's model-space pivot from the geo file, transformed by the gun pose but not by the animation. The flash then stays still while the gun kicks, as today. This works wherever the bone sits in the hierarchy.
2. **Orientation:** normalized when converting. The converter bakes the 90° turn of `RenderGunBase90` into the geo file, so every gun points along −Z and `RenderGunBase90` goes away. Done for all geo files (`tools/geo_turn.py`); `RenderGunBase90` stays until the Java fallback is deleted.
3. **Grenade launcher:** convert the OBJ to geo if the shape can be kept. Check with a side-by-side screenshot. It stays an OBJ exception only if the shape cannot be matched with boxes and per-face UVs.
4. **Display JSON in `assets`:** pure visuals can be changed by resource packs. Animation files also live in `assets`, but their length is only the shape of the motion: playback is always stretched to the gun's real durations from its stats (`data`, server-synced, datapack overridable):
   - `reload` → reload time;
   - `fire` / `recoil_*` → recoil time, capped by the fire interval;
   - `charge` → charge time;
   - loops → spin-up speed.

   A resource pack can therefore change how a reload looks, but never how long it takes.
5. **Authoring:** open, see below.

### Who makes the animations (question 5)
"Authoring" means creating the keyframes in Blockbench: which bone moves where at what time. Two kinds of work:
- **Converting existing motion: Claude.** Today's motion lives in code (`GunAnimation` presets, `transformPart`). It is converted 1:1 into keyframes by script (sample the code curve, write keyframes), so it looks the same as now. No Blockbench work needed.
- **New motion: Claude drafts, the author polishes.** Motion guns don't have today, e.g. a magazine actually dropping on the AK, or a revolver cylinder swinging out. Claude can write simple keyframes headlessly (bbmodel tools) using the standard bones. Whether it *feels* right is a judgement call: the author opens the `.bbmodel` in Blockbench and adjusts timing or poses when wanted.
