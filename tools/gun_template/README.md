# Gun template (Blockbench)

A minimal gun to start new gun models from. It uses the M4's hand origin, hold positions, muzzle height, aim
animation and recoil/reload strengths, so a gun built on it sits in the hand like the M4.

| File | Goes to (for a gun `<gun>`) |
|---|---|
| `gun_template.bbmodel` | Blockbench project (Bedrock model format, box UV, 128x64): open this, *File > Save As* `<gun>.bbmodel` |
| `gun_template.geo.json` | `assets/techguns/geo/item/<gun>.geo.json` (*File > Export > Export Bedrock Geometry*) |
| `gun_template.animation.json` | `assets/techguns/animations/item/<gun>.animation.json` (*Animation > Export Animations*) |
| `gun_template.png` | `assets/techguns/textures/guns/<gun>.png` |
| `gun_template.item_model.json` | `assets/techguns/models/item/<gun>.json` (hold positions, copied from the M4) |
| `gun_template.gun_display.json` | `assets/techguns/techguns/gun_display/<gun>.json` (set `texture` to the gun's texture) |

The json and png files are what the `.bbmodel` exports; they are here so the set can be copied and tried without
Blockbench.

## Bones

| Bone | Pivot | Purpose |
|---|---|---|
| `root` | `[-0.5, -1.6, 0]` | Whole gun. The pivot is the hand origin: keep it, or the gun sits elsewhere in the hand. Shared recoil and reload animations move this bone. |
| `magazine` | magazine well | Moving part; the template `reload` drops and tilts it. |
| `bolt` | bolt | Moving part; the template `reload` pulls it back. |
| `sight_glow` | red dot `[-0.5, 10, 0]` | Any bone whose name contains `_glow` is drawn full bright. The dot sits in an open ring sight (cubes in `root`), at the M4 red dot height, so `aim` puts it on the crosshair. |
| `muzzle` | barrel tip | Empty marker: the muzzle flash is drawn here. Keep it empty and move it to the new barrel tip. |
| `hand_right` | grip | The player's grip hand in first person. The pivot is the centre of the hand end of the arm; the arm runs along the bone's +Y toward the shoulder, so the bone rotation aims it. The cube is an arm-sized preview (4 x 12 x 4 px divided by the first-person display scale, 8 x 24 x 8 for the M4's 0.5) and is not drawn in game: move and turn the bone until the preview lies where the arm should be. |
| `hand_left` | handguard | The support hand, drawn on two-handed guns (`hand_type` two-handed); same rules. Both hands are authored for a right-handed hold and mirrored for left-handed players and offhand guns. Parent a hand to a moving part (a pump) to let it follow that part's keyframes. |

Other standard names (`slide`, `barrel`, `hammer`, `cylinder`, `belt`, `drum`, `charge`, `idle_fx`, `mesh_*`,
`*_translucent`, `*_cutout`, `ammo_*`) are listed in `docs/gun_animation_plan.md` (Bone naming convention). Bones
that a gun doesn't need can be deleted; animation tracks of missing bones are skipped.

Orientation: the barrel points north (-Z in the geo file), up is +Y, the gun's centre line is x = -0.5 (geo).

## M4 reference

The group `root > m4_reference` holds the M4 with its own texture, hidden and not exported (eye icon and the
*Export* toggle are off). Show it to compare size and position; it never ends up in the exported geo file.

## Animations

- `reload` (2.25 s): magazine out and in, bolt pulled. Stretched to the gun's reload time in game.
- `aim` (1 s): moves `root` so the sight line lands in the screen centre; played by the zoom progress. Retune the
  last keyframe when the sight height changes.
- No `fire`: recoil comes from the shared `recoil_generic` (strengths in the display file). Add a `fire` animation for
  own recoil or part motion. Shared animations: `animations/item/common.animation.json`.

## Notes

- Hold positions: the Bedrock format has no Display tab; edit the `display` block of the item model json (values
  in Blockbench Display units), or tune them in the GeckoLib plugin's animated item format.
- Texture: box UV, each part has its own color so the UV islands are easy to find. Paint over it.
- A bone that needs its own texture (a head that changes with the mining head level, a round that changes with the
  loaded ammo) stays in the same model: list it under `bone_textures` in the display json.
- A new gun item still needs its registration in code (`TGGuns`, or an addon's own item registry). It needs no renderer
  code: every gun without a setup in `TGItemRendererSetup` is drawn from these files. An addon gun keeps its files in
  its own namespace (`assets/<namespace>/geo/item/<gun>.geo.json`, `animations/item/`, `models/item/`,
  `techguns/gun_display/`).
- Muzzle flash, charge effect and scope overlay name screen effect files (`techguns/screen_effect/<name>.json`, e.g.
  `rifle`, `laser`, `sniper_scope`); a gun with its own flash adds a file there in its namespace and names it
  `<namespace>:<name>`.
- Hand bones in game: `/tgdebug handpreview` draws the preview cubes next to the arms; `python tools/hand_bones.py
  --set <geo file> <hand_right|hand_left> x,y,z rx,ry,rz` moves a hand and keeps its preview cube on the pivot;
  `python tools/hand_retune.py <gun>` places both hands under the handguard and on the grip from the model geometry,
  as a starting point. The arms can be turned off in the client config (`firstPersonArms`).
- Blockbench's orbit view makes a hand look higher than in game, where the arm runs toward the camera and mostly
  leaves the screen. To judge a hand as the game shows it, hold the gun in game, run `/tgdebug eyepos` and set
  Blockbench's camera to the printed `eye` (perspective, fov 70, looking along -Z; *View > Angle* can save it).
- Editing an existing gun: open its `.geo.json` in Blockbench (*File > Open Model*), edit, save or export over it, then
  run `python tools/geo_tidy.py <gun>`. Blockbench rewrites the whole file (bone order, `0.0` as `0`, visible bounds,
  `mirror` on per-face UV cubes); the script restores the committed version of everything that didn't change, so the
  diff shows only the edited bones (`--dry` reports without writing, `--base <rev>` compares with another commit).
- After a resource reload (F3+T) search the log for `Gun data check:`: missing files, bone names the geo file lacks
  and unknown animation names are listed there, one line each, with a summary line at the end.
