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
