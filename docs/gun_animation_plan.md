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
| `mesh_*` | Empty bones (pivot 0) where a mesh geo cubes can't express is drawn: the display file's `meshes` (OBJ file and rotation per bone), else `TGModel#renderMesh`; the grenade launcher's OBJ meshes (implemented). |
| `*_translucent` / `*_cutout` | Bone (and its children) drawn in a second pass with that render type and the part's texture; replaces the split files of NDR and GML (implemented). |
| `ammo_*` | Variant bones shown only for the matching loaded ammo (rocket launcher rocket), instead of a separate geo file and texture swap. |
| any bone in `bone_textures` | Bone (and its children) drawn in its own pass with its own texture and render type from the display JSON: by mining head level or loaded ammo (drill/hammer `head`, chainsaw `blade1`/`blade2`, launcher `rocket`). Replaces their split geo files (implemented). |

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
| `query.tg_ammo_count` | Loaded rounds (implemented; added for belts). |
| `query.tg_time` | World time in seconds (implemented; for loops, replaces `tg_spin`). |
| `query.tg_gui` | 1 when drawn as a GUI icon (implemented; hides the M4 Infiltrator laser in icons). |
| `query.tg_first_person` | 1 when drawn in first person (implemented; the LMG belt feeds only there). |
| `query.tg_in_hand` | 1 when held, 0 for GUI icons, on the ground and in frames (implemented; power hammer head). |
| `query.tg_grapple` | Grappling hook of the holder: 0 in the gun, 1 launched, -1 attached or pulling (implemented; grappling hook). |
| `query.tg_locked` | 1 in first person once the lock-on is complete (implemented; GML holo sight). |
| `query.tg_spin` | Barrel spin angle / speed (dropped, see Progress). |

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

### Display JSON (`assets/techguns/gun_display/<gun>.json`, hand-maintained in `src/main/resources` since phase 7)
Only what isn't motion:
- muzzle/charge/idle effect type, scale and jitter, `muzzle_follows_animation`;
- scope overlay and its scale;
- player arm pose;
- the shared animations used (names) and the `tg_recoil_move` / `tg_recoil_turn` multipliers;
- `bone_textures`: bone name -> `render_type`, `head_level_textures`, `ammo_textures` (same fields as a `parts` entry); without a match the bone uses the camo or `texture`;
- `meshes`: mesh bone name -> `model` (OBJ file), `flip_v`, `rotation`.

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
   - Rocket launcher rocket → `ammo_*` bones (done differently: the rocket keeps its own file and per-ammo texture, see Progress).
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
    - `recoil` / `reload` (and `charge`, added later): animation, `move` (model pixels) and `turn` (degrees) for `first_person`, `aiming` (optional) and `third_person`; plus `muzzle_follows_animation`.
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
- **2026-11-04, conversion seeds and the first turned data-driven gun (revolver):**
  - `/tgdebug convertgun` (`GunConversion`) seeds the held gun's source files from its code renderer, once, when the gun moves to its model files; they are hand-owned from then on.
    - `root` pivot of every geo file of the gun: the hand origin, from the base translation in the geo frame (`RenderGunBase#exportRootPivot`; turned for `RenderGunBase90`).
    - `aim` animation in `animations/item/<gun>.animation.json` (other animations kept, Blockbench tab layout): `root` moves to the ADS shift in model pixels (`RenderGunBase#exportAim`).
    - Reproduces the pistol's hand-made pivot and `aim` exactly.
  - **Revolver** is data-driven (`DATA_DRIVEN`): seeded pivot `[1.1, -3.2, -5.6]` and `aim`, display file and item model `display` block from datagen.
  - **Fixes on the way:**
    - The item model export ran without the geo bridge (datagen), so a turned gun got the Java 90 degree chain. It now always uses the geo frame (`setGeoBaseRotation`, `geoBaseTranslation`).
    - The display transform was applied with `PoseStack#mulPose(Matrix4f)`. With a scale it recomputes the normal matrix at 1/scale length, and GeckoLib does not renormalise normals, so data-driven guns were lit too bright (2.7 times for the revolver; 1.7 for the pistol, missed before). It is now applied as translate, rotate and uniform scale.
    - JOML 1.10.5's `getNormalizedRotation` returns a non-unit quaternion for a scaled matrix; the scale is taken out first.
  - **Verified in game, revolver data path vs Java fallback:** idle, reload at 0.05 / 0.15 / 0.25 / 0.4 (matches, ease-in within the known Euler approximation), full aim, hotbar icons (identical).
  - Known geo vs Java difference, not from this work: the Java model lights the end faces of mirrored boxes from the inside (darker when aiming, where they face the camera); the geo files light them from the outside.
- **2026-11-04, charge pull-back from files (biogun):**
  - The first person pull-back while charging (`setChargeTranslationAmount`) is a third whole-gun motion in the display file: `charge`, sampled on the charge progress like `recoil` and `reload`.
    - `common.animation.json` has `charge_generic`: `root` moves linearly by one unit along +z.
    - Datagen writes it only for charge guns (`ChargeGunItem`) with a pull-back: `move` = translation in model pixels at the first person scale, third person `move` 0 (the code path never pulls back there).
    - The gun's own `charge` animation (part motion, e.g. the TFG rails) still plays in every view, on top.
  - **Biogun** is data-driven (turned gun, charge gun): seeded with `/tgdebug convertgun` (pivot `[0.6, -3.2, 5.6]`, `aim`), display file with `charge` move 1.6, item model `display` block.
  - `/tgdebug pose charge <progress>` freezes the charge progress.
  - **Verified in game, biogun data path vs Java fallback:** charge 1.0 and 0 in first person, identical gun pixels (the ground behind the gun differs between screenshots); third person charge 1.0 by eye.
- **2026-11-04, Molang queries:**
  - `GunQueries` sets the query values of each draw before its animations are sampled: `query.tg_fire`, `query.tg_reload`, `query.tg_charge`, `query.tg_zoom` (timer progress, 0..1), `query.tg_ammo` (loaded fraction of the magazine), `query.tg_ammo_count` (loaded rounds) and `query.tg_time` (world time in seconds with the partial tick, for loops). `q.` works as well; GeckoLib's ternary and comparisons work in the expressions (`q.tg_ammo_count > 3 ? 1 : 0`).
    - GeckoLib creates a variable the first time a file names it, so load order does not matter.
  - The gun renderer now passes its animation input for every in-hand draw, also on the code path (parts only: no aim, mirror or hold turns there), so the ammo fraction knows the magazine size.
  - Dropped from the table above: `tg_recoil_move` / `tg_recoil_turn` (the display file's `move` / `turn` do it) and `tg_spin` (the minigun barrel turns once per recoil, a plain `fire` keyframe; a spin-up loop can use `tg_time` and `tg_fire`).
  - **Verified in game** with a temporary biogun animation reading every query: the pose followed charge, ammo (full vs empty) and time.
- **2026-11-05, sound, particle and instruction keyframes:**
  - `GunAnimationEvents` fires the Blockbench "Sound", "Particle" and "Instructions" timelines (`sound_effects`, `particle_effects`, `timeline`) of the timer-driven layers (`reload` and the reload motion on the reload timer; `fire` / `fire_last` and the recoil motion on the recoil timer) when the sampled time passes them.
    - Once per holder, hand, animation and timer run: the run is the timer's start (`GunRenderState` `fireCycle` / `reloadCycle`, passed in `GeoAnimInput.Events`), so drawing the gun several times per frame (split geo files, first and third person) fires nothing twice.
    - No events without a running timer: not for `aim`, `charge`, frozen `/tgdebug pose` draws or the held `fire_last` pose. An event on the very last frame can be missed (the timer can end between frames).
    - The local player's third person draw while the camera is in first person (inventory screen model) fires nothing.
  - **Sound:** a sound event id (`techguns:` when no namespace), played at the holder for this client only. Shot and reload start sounds stay server-side; keyframe sounds are timed details on top.
  - **Particle:** a Techguns FX name (`TGFX`, as `/tgdebug fx`); the locator is a marker bone (default `muzzle`), else the holder's eyes. Spawned after the draw at the bone's world position (first person: view space turned by the camera rotation; third person: camera-relative world).
  - **Instructions:** `name args`, several separated by `;`. Built in: `sound <id> [volume] [pitch]` (GeckoLib's sound keyframes have no volume or pitch). Others via `GunAnimationEvents.registerInstruction`; unknown names are logged once.
  - `/tgdebug animevents` prints every fired event (and the particle position relative to the eyes) to chat.
  - No gun uses them yet: today's guns have no timed sounds or effects besides the server-side shot and reload sounds.
  - **Verified in game** with temporary pistol `fire` keyframes (sound at 0.1, particle at 0.0 on `muzzle`, two instructions at 0.3): each fired once per shot in order; the particle landed at the muzzle (first person 0.20 right, 0.28 down, 1.21 ahead of the eyes; third person 0.12 right, 0.23 down, 0.82 ahead); the unknown instruction was logged once.
- **2026-11-05, phase 4: guns without part motion (23 guns):**
  - `/tgdebug convertgun` also places the `muzzle` marker bone (child of `root`) in the first geo file: at the next first person frame of the gun at rest it maps the old first person flash offset (`setMuzzleFx` x/y/z) through the drawn pose into model space (`RenderGunBase#placeMuzzle`; the bridge reports each geo file's model origin as `#model:<id>`). The flash then lands exactly where it was (checked: the M4's bone puts it at its old offset). `/tgdebug convertgun muzzle` does only that; on a gun that already has a bone it reports the computed and existing positions instead (the pistol's existing bone round-trips exactly, so the axis convention is right; its hand-placed bone, at the barrel tip, stays).
  - Data-driven now: M4, AK47, MAC-10, Vector, Thompson, bolt action, golden revolver, AS50, sawed-off, hand cannon, lasergun, blaster rifle, gauss rifle, PDW, pulse rifle, laser pistol, Tesla gun, alien blaster, MIB gun, Nether blaster, sonic shotgun, Shishkebap (no flash, no bone), magic rifle. Each: `DATA_DRIVEN`, `runData` (display file and item model `display` block), `convertgun` (root pivot, `aim`, `muzzle`).
  - **Verified in game, data path vs Java fallback, first person `fire 0.3`, pixel diff:** 0 to 4 differing gun pixels for 22 guns; the AK47 35 (one small patch, the known mirrored-box lighting difference). Aim (magic rifle) identical. Reload poses differ only at the edges during the ease-in (the known Euler approximation of the tilted reload axes).
  - **M4 reload:** its own `reload` animation (from the GeckoLib prototype) also rolled `root` by -18 degrees, which Techguns 2 and the Java fallback never had; the roll is dropped (author's decision), the animation keeps the magazine and bolt motion. Whole-gun motion comes only from the display file's `reload`.
  - Third person: the flash now sits at the bone, so it differs from the separately tuned third person offsets (`setMuzzleFXPos3P`); checked by eye on the M4 (at the muzzle).
- **2026-11-05, phase 5 start: bolts and pump (SCAR, AUG, combat shotgun):**
  - `tools/geo_merge.py` merges a split part file (`<gun>_1.geo.json`) into the main geo file under a new group bone (child of `root`, pivot 0) and deletes it; it writes the `geo_turn.py` layout. Merged: SCAR and AUG under `bolt`, combat shotgun under `pump`. The part's own bones were renamed (`bolt_body`, `pump_body`), since GeckoLib bone names must be unique case-insensitively in Blockbench.
  - The `transformPart` motion is now keyframes on the group bone (Java blocks x 16 = pixels): SCAR `fire` bolt back 9.6 px at 0.4, AUG 8 px, combat shotgun `fire` pump back 6.4 px between 0.65 and 0.95 and `reload` between 0.9 and 1.0. Linear, exact like the code.
  - **New layer `hold`:** sampled at its start in every draw, under all others: the static pose of a model, for what the geo format can't store. The SCAR bolt's static render scale (1, 0.7, 0.7) was applied by code bones to the part file only; it is now a `hold` scale keyframe on `bolt_body`. The static scales of the other models are still code bones (`TGModel#animateGeoBones`) until phase 7 moves them to `hold` the same way.
  - Display files, item model `display` blocks and `convertgun` seeds (root pivot, `aim`, `muzzle`) as in phase 4. The Java fallback keeps its separate part model (and `/tgdebug exportgeo` still writes the `_1` files) until phase 7.
  - **Verified in game, data path vs Java fallback, pixel diff:** SCAR `fire 0.3` bolt identical (before the `hold` fix the bolt was 1.4 times too big); the remaining 1400 px are the scope parts' known shear approximation (logged at startup, also at rest). AUG `fire 0.3` identical. Combat shotgun `fire 0.8` (pump peak) identical; `reload 0.95` differs only by the known reload tilt approximation (`reload 0.9`, pump at rest, differs as much).
- **2026-11-05, phase 5: minigun, blaster shotgun (Lasergun 2), Grim Reaper, M4 Infiltrator:**
  - `GeoGunModel#motionInFiles()`: the geo draw skips the Java model's `transformPart` (still used by the Java fallback).
  - **Minigun:** `minigun_1` merged under `barrels`; `fire` turns it once, linear, `[0, 0, -360]` (the sign checked in game at `fire 0.1`: +360 turned the wrong way; the turned gun's Java x axis is the geo z axis and the y flip of the file reverses the angle).
  - **Blaster shotgun:** `blaster_shotgun_1` merged under `pump` (`Handguard` renamed `pump_body`). The Fabric port's pump (Techguns 2 had none) runs from fire 0.15 to 0.95 at the speed of a 0.3 stroke, so after 0.45 the handguard slid up to 7 px forward and snapped back; the keyframes have the intended stroke: 2 px forward at 0.3, back at 0.45 (and 0.9 to 1.0 of the reload, peak at 0.95).
  - **Grim Reaper:** its `transformPart` turned every part 180 degrees about y; the turn is baked into `grim_reaper.geo.json` (`tools/geo_turn.py` twice).
  - **M4 Infiltrator:** the laser beam is hidden in inventory icons by its `hold` animation, scale `1 - q.tg_gui` on `LaserBeam` / `LaserBeam01`; the code override (`ModelM4Infiltrator#animateGeoBones`) is gone. New query `query.tg_gui` (1 for GUI icons).
  - **Verified in game, data path vs Java fallback, pixel diff:** minigun `fire 0.1` identical, blaster shotgun `fire 0.3` (pump peak) 26 px, Grim Reaper identical (also its hotbar icon), M4 Infiltrator identical in first person; its hotbar icon draws without the beam, the hand view with it.
- **2026-11-05, phase 5: TFG, LMG:**
  - **TFG:** `hold` puts the rails at their rest offsets (side rails 2 px out, upper/lower halves 0.9 px apart); `charge` pulls the rails in from 0 to 0.1 and closes the halves from 0.11 to 0.2 (the `f1`/`f2` factors of `ModelTFG#render`). `ModelTFG#animateGeoBones` is gone.
  - **LMG belt:** `hold` scales `Bullet00` to 0 (a marker) and each `Bullet0i` to `(q.tg_ammo_count > i || (q.tg_ammo_count == i && q.tg_fire > 0)) && (q.tg_reload == 0 || q.tg_reload > 0.5)`; `fire` moves each bullet to the pivot of the one before it from 0 to 0.25, scaled by the new `q.tg_first_person` (the feed shows only in first person). `ModelLMG#animateGeoBones` is gone.
  - Not animated draws (GUI, ground, frames) now carry the stack's loaded rounds (`GeoAnimInput.still(gui, ammo)`), so an icon shows the belt the gun has, as the Java model did.
  - **Verified in game, data path vs Java fallback, pixel diff:** TFG `charge 0.05`, `0.15`, `0.5` identical (0 to 1 px; 0.05 vs 0.15 differ by ~13000 px, so the rails do move in view); LMG `fire 0.1` belt identical, `reload 0.3` and its hotbar icon identical.
- **2026-11-05, phase 5: mining tools (chainsaw, power hammer, mining drill):**
  - Their head files (`<tool>_1`) stay split: render part 1 takes the texture of the installed mining head (`RenderMiningTool`), and a GeckoLib model has one texture.
  - **A split part file shares the gun's animation file:** `GeckoLibGunRenderer` maps geo id `<gun>_N` to `animations/item/<gun>.animation.json`, so one file per gun drives every part (root keys move all parts alike; a part's own bones are only found in its file). `convertgun` already set the `root` pivot of every geo file of the gun.
  - **Chainsaw:** `hold` scales `blade1` / `blade2` by `math.floor(q.tg_fire * 40 + 0.5) % 2 == 0` / `== 1` (the two blade frames of `ModelChainsaw`); its `animateGeoBones` is gone.
  - **Mining drill:** `mining_drill_1` has a `head` group bone (pivot at the old spin axis, file (0.64, -1.6, 0)); `fire` turns it `[0, 0, -360]` as the minigun.
  - **Power hammer:** `power_hammer_1` has a `head` group bone; `hold` puts it at `3.2 + 3.2 * q.tg_in_hand` px along z (rest 6.4 in hand, 3.2 in GUI, on the ground and in frames, like the old `headProgress` 0 / 0.25) and `fire` moves it -6.4 at 0.5 and back. New query `query.tg_in_hand`.
  - `motionInFiles()` on the drill and the hammer (their `transformPart` stays for the Java fallback).
  - **Verified in game, data path vs Java fallback, pixel diff (time fixed: lighting drifts with the day cycle otherwise):** drill `fire 0.1`, hammer `fire 0.25`, chainsaw `fire 0.03` and `0.05` (both blade frames) identical in first person; hotbar icons: hammer 1 px, chainsaw 28 px, drill 19 px (display transform rounding, as with the phase 4 guns).
- **2026-11-05, phase 5: grappling hook:**
  - `grappling_hook_1` (hook) and `_2` (wheel) are merged into `grappling_hook.geo.json` (`tools/geo_merge.py`) under the group bones `hook` and `wheel`; one render part, `motionInFiles()`.
  - New query `query.tg_grapple` from the holder's hook entity (`GunRenderState#grappleStatus`, only looked up for the grappling hook; the Java model uses it too).
  - `hold` scales `hook` by `q.tg_grapple == 0` (gone while launched or attached) and turns `wheel1` about its pivot by `q.tg_grapple * ((q.tg_time * 1800) % 360)` on x (90 degrees per tick, unrolling while launched, rolling back while attached), as `ModelGrapplingHook#transformPart`.
  - **Verified in game, data path vs Java fallback, pixel diff (hook in flight, `tick freeze`, `/tgdebug pose charge 0.5`, the use time keeps running under the freeze):** idle and in-flight gun identical above the wheel; at the wheel ~300 px of 1-px edges and z-fighting slivers (the Java path computes the angle from a float game time, 1/16 tick steps at a million ticks); the opposite spin sign differs by ~1300 px. Hotbar icon identical.
- **2026-11-05, phase 5: NDR, GML (render pass bones); phase 5 done:**
  - **Render pass bones:** a bone whose name ends in `_translucent` or `_cutout` is left out of the main draw and drawn with its children in a second pass, `entityTranslucent` / `entityCutout` with the part's texture (`TGModel#setPartTexture`, set by `RenderGunBase` around each part). The pass is a GeckoLib `reRender` after the main draw: it keeps the pose (no second animation step), and only then switches the buffer source to the other render type (switching mid-draw would end the main buffer's batch).
  - **NDR:** `nuclear_deathray_1` merged under `rings_translucent`; `hold` scales it by `q.tg_fire > 0 ? (0.9 + math.sin(q.tg_fire * 360) * 0.1) : 0` (GeckoLib's `math.sin` takes degrees), as `ModelNDR#transformPart`.
  - **GML:** `guided_missile_launcher_1` merged under `holo_cutout` (pivot at the holo, file (5, 3, -23.5)); `hold` scales it 0.5 and shows `Holo2_glow` / `Holo2_1_glow` by `1 - q.tg_locked` / `q.tg_locked`. New query `query.tg_locked` (`GunRenderState#locked`, as the Java model's `isLocked`); the `HoloPart#animateGeoBones` override is gone.
  - Both `motionInFiles()` and data-driven (`convertgun`: root pivot, `aim`, `muzzle`).
  - **Verified in game, data path vs Java fallback, pixel diff:** NDR idle identical; NDR `fire 0.25` rings identical, ~1000 px of 1 to 3 px edge shifts on the gun from `recoil_sway` (the first data-driven sway gun; its Euler turns approximate the preset's tilted axis, like the reload poses); GML idle and locked (pig target, `/tgdebug aim`) identical in first person; NDR hotbar icon identical, GML icon 32 px (display transform rounding).
  - **Phase 5 is done:** AUG, SCAR, combat shotgun and pistol keep `transformPart` only for render part 1, which their merged geo files no longer have, so the geo path never runs it (Java fallback only). The only split geo files left are the mining tool heads (per-head texture).
- **2026-11-05, phase 6: rocket launcher:**
  - **No `ammo_*` bones:** the rockets have their own 64x32 textures per ammo (`rocket`, `rocket_hv`, `rocket_nuke`), and the launcher texture has camo variants (`rocketlauncher_1`); baking the rockets into the gun texture would mean copying them into every camo. So the rocket stays a split file with its ammo texture (`ModelRocketLauncher#getRenderType`), like the mining tool heads.
  - `rocket_launcher_rocket.geo.json` is renamed `rocket_launcher_1.geo.json`, so it shares `rocket_launcher.animation.json`; its bones sit under a new `rocket` group bone (a scale on `root` would hit the launcher too).
  - `hold` scales `rocket` by `(q.tg_reload == 0 && q.tg_ammo_count > 0) || q.tg_reload > 0.5` (loaded, or past the reload's midpoint). This replaces `GeoGunModel#setVisibility`, its only user, which is gone (the Java fallback always drew the rocket).
  - `motionInFiles()`, data-driven (`convertgun`: root pivot in both files, `aim`, `muzzle`).
  - **Verified in game, data path vs Java fallback, pixel diff:** loaded idle identical, hotbar icon 6 px; reload 0.3 hides the rocket, reload 0.7 shows it and matches the Java fallback.
- **2026-11-05, phase 6: flamethrower:**
  - `idle_fx` marker bone at the muzzle (the pilot flame used the muzzle flash offsets). `RenderFlamethrower` draws the pilot flame at that marker in first and third person, else at the old offsets (Java fallback). The under-water check stays in code.
  - `motionInFiles()`, data-driven (`convertgun`: root pivot, `aim`, `muzzle`; `idle_fx` copied from `muzzle`).
  - **Verified in game, data path vs Java fallback:** first person gun identical, the pilot flame starts at the same spot. Third person: the flame now sits on the nozzle; the old hand-tuned third person offset was slightly off-center.
- **2026-11-05, phase 6: mining tools, grenade launcher; phase 6 done:**
  - **Mining tools:** nothing left for queries. Their motion moved to the model files in phase 5; `RenderMiningTool` only picks the head texture of part 1 by head level, which a query can't do (one texture per GeckoLib model), like the rocket launcher's ammo textures.
  - **Grenade launcher, OBJ meshes on mesh bones** (decision 3): the triangulated OBJ (324 triangles) pairs into 160 quads, but 19 planar polygons are not rectangles. The hexagonal drum housing ends and pentagonal barrel ends have slanted edges and detailed texture (815 colors on the housing ends), which flat rotated cubes can't map with per-face UV.
    - New bone kind `mesh_*` (`TGGeoBridge.MESH`, reported like a marker). After the geo draw, `GeoGunModel` calls `TGModel#renderMesh(bone, pose, ...)` for each mesh bone of the part, with a pose stack at the bone's pose (normal matrix: inverse transpose scaled by the cube root of its absolute determinant, as `PoseStack#scale` keeps the sign of the GUI's mirroring pose; with the sign lost the icon was dark).
    - `grenade_launcher.geo.json`: `root`, `mesh_frame`, `drum` (pivot (0, -32, 0), the old turn axis) with `mesh_drum`, plus `muzzle` from `convertgun`. `ModelGrenadeLauncher#renderMesh` turns by Ry(90) (the OBJ points along x) and draws the frame or cylinder mesh; `render` stays the Java fallback.
    - `fire` turns `drum` `[0, 0, -60]` (one chamber per shot). `RenderGunBaseObj` uses the normal base rotation for a geo draw. Geo pixels are 8x the usual size (base scale 0.125 kept), e.g. the muzzle at z -138.
    - **Verified in game, data path vs Java fallback, pixel diff:** idle, `fire 0.25` (drum turn) and `zoom 1` identical in first person, hotbar icon identical; third person by eye.
- **2026-11-06, phase 7 start: code still read at runtime (author: every visual setting from JSON):**
  - Survey: on the data path the code placement is already bypassed. Still read from code: the static part scales (reflection over the Java models every draw), the muzzle offsets of biogun and revolver (no `muzzle` bone), the Magic Rifle charge effect (offsets only), the effect types/scales/jitter, the scope overlay and its recoil (`GunAnimation`), and the part textures (mining heads, rocket ammo).
  - **Static part scales** (Vector, SCAR, Shishkebap, mining drill; 37 bones) are constant `hold` keyframes now (scale, plus the offset that keeps the Java scale origin). `TGModel#animateGeoBones` no longer applies them. Verified in game against the Java fallback: Vector, Shishkebap and mining drill identical, SCAR differs by 1-px edges on the scope parts (the shear approximation of scaled rotated parts, as before).
  - **Marker bones:** `muzzle` for biogun and revolver, `charge` for the Magic Rifle (`/tgdebug convertgun charge`). The charge effect is drawn at the `charge` marker in first and third person (old offsets only without one). Checked by eye: the charge circle sits on the muzzle as before.
- **2026-11-06, phase 7: visual settings in the display JSON.** New fields of `gun_display/<gun>.json`, written by datagen from the code renderers:
  - `texture` (all parts) and `parts` (one entry per drawn geo part: `render_type` solid/cutout/translucent, `head_level_textures` for the powered melee tools, `ammo_textures` for the rocket launcher's rocket). The part count now comes from the file.
  - `muzzle_flash`, `charge_effect`, `idle_effect` (flamethrower pilot flame, loops every second, not under water): `effect` by name (`ScreenEffects`: `rifle`, `gun`, `laser`, `tech_scope`, ...), `scale`, `third_person_scale`, `jitter` {x, y, scale}. Drawn at the `muzzle` / `charge` / `idle_fx` bones.
  - `scope`: `effect`, `scale`, and `recoil` = an animation strength. The overlay recoil samples the animation's `root` bone (`TGGeoBridge#applyRootMotion`) instead of playing a `GunAnimation`.
  - Code renderers without a display file keep their fields (converted to the same effect records) until the fallback is deleted.
  - Checked in game: M4 flash with jitter, SCAR scope overlay with recoil, flamethrower pilot flame, mining drill head texture, rocket launcher rocket texture. Build and 378 GameTests pass.
- **2026-11-06, phase 7: gun JSON out of datagen.** The 44 `gun_display/<gun>.json` and gun item models (`models/item/<gun>.json`, with the Blockbench display transforms) moved from `src/generated` to `src/main/resources` and are edited by hand now. `TGGunDisplayProvider`, the renderers' `exportDisplay` / `exportItemTransforms` and their helpers are gone; `TGItemModelProvider` no longer writes gun models. Build, runData and 378 GameTests pass.
- **2026-11-08: per-bone textures, split files gone.** `gun_display` gets `bone_textures` (bone name -> render type, head level and ammo textures). `GeckoLibGunRenderer` leaves those bones out of the main draw and draws each in its own pass (`TGGeoBridge#render` takes the bone render types; markers inside a pass bone are reported by its pass). `chainsaw_1`, `mining_drill_1`, `power_hammer_1` and `rocket_launcher_1` are merged into their main geo files (bones under `root`, no name clashes); every gun is one geo file now. Checked in game: rocket texture follows the loaded ammo (nuke), drill and hammer heads follow the head level (carbon), all four guns draw complete in item frames and in hand.
- **2026-11-08: guns without renderer code.** Every `GunItem` (any mod) that `TGItemRendererSetup` doesn't set up gets `RenderGunBase.dataDriven`: a `GeoGunModel.dataOnly` model (no Java model behind it; draws nothing while geo models are off or the files are missing) and everything else from its geo, animation, item model and display files. Geo ids take a namespace (`<namespace>:<gun>` loads `assets/<namespace>/geo/item/<gun>.geo.json` and its animation file), so addon guns keep their files in their own namespace. The 46 existing setups stay as the Java fallback (author decision). Checked in game by routing M4, rocket launcher, mining drill and flamethrower through the new path: hold, aim, fire flash and recoil, rocket and drill head textures, pilot flame, hotbar icons and third person match.
- **2026-11-08: last code hooks closed.** `gun_display` gets `meshes`: mesh bone -> OBJ `model`, `flip_v` (default true), `rotation` (degrees, applied z, y, x). `GeoGunModel` draws those itself and only asks the Java model's `renderMesh` for mesh bones the file doesn't name. The grenade launcher's frame and drum moved there (Ry(90), as the code did). The grappling hook needed nothing: its hook hides through the `tg_grapple` Molang scale in `hold`, and `isPartVisible` only served the old split part. Checked in game with both routed through `RenderGunBase.dataDriven`: launcher frame and drum draw, the drum turns on fire; the hook draws. Every gun can now be drawn without its Java model.
- **Next:**
  - Author decision (2026-11-06): the Java gun models, the fallback, `GunAnimation`, the conversion tools and `RenderGunBase90` stay for now; delete them only when the author asks.
  - Phase 7 docs done (design doc section 9.1, `port_status.md`, `migration_26.md`); phase 7 is complete apart from the deferred deletions.

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
3. **Grenade launcher:** convert the OBJ to geo if the shape can be kept. Check with a side-by-side screenshot. It stays an OBJ exception only if the shape cannot be matched with boxes and per-face UVs. **Result (2026-11-05):** it can't (hexagonal and pentagonal end faces with detailed texture; GeckoLib 4 parses `poly_mesh` but never draws it). Author's choice: the OBJ meshes are drawn at mesh bones of a geo file, so its motion is still in the animation files.
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
