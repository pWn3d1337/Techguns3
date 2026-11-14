"""Adds the first-person hand bones to gun geo files (docs/gun_animation_plan.md, "Hand bones").

    python tools/hand_bones.py [--grip-only] [--scale S] <geo files>
    python tools/hand_bones.py --set <geo file> <hand_right|hand_left> <x,y,z pivot> <rx,ry,rz rotation>

Each file gets
  hand_right: the grip hand, pivot at the root pivot (the hand origin of the gun's hold);
  hand_left:  the support hand (two-handed guns; left out with --grip-only), 2.5 px under the muzzle height, 40 % of the
              way from the root pivot to the muzzle pivot.
Both are children of root and hold one preview cube the size of the player's arm (4 x 12 x 4 px divided by the
gun's first-person display scale, read from models/item/<gun>.json, else --scale): the arm extends from the bone pivot
along the bone's +Y. The game draws the player's arm there instead of the cube. The default rotations aim the arms back
toward the camera; tune them per gun in game (/tgdebug handpreview) or in Blockbench.

Files that already have a hand bone are skipped. The text is edited in place (the bones are appended to the bones
array), so the rest of the file keeps its layout.
"""
import json
import pathlib
import re
import sys

ASSETS = pathlib.Path(__file__).resolve().parent.parent / "src/main/resources/assets/techguns"
# default aim of the arms (Bedrock bone rotation, degrees): the grip arm back toward the camera and down, the support
# arm down and to the left (tuned on the pistol and the M4)
GRIP_ROTATION = [-100, 0, 0]
SUPPORT_ROTATION = [-130, 55, 0]


def first_person_scale(gun, fallback):
    model = ASSETS / "models/item" / (gun + ".json")
    if model.exists():
        display = json.loads(model.read_text(encoding="utf-8")).get("display", {})
        scale = display.get("firstperson_righthand", {}).get("scale")
        if scale:
            return scale[0]
    return fallback


def r(v):
    v = round(v, 4)
    return int(v) if v == int(v) else v


def bone(name, pivot, rotation, scale):
    w, h = 4 / scale, 12 / scale
    cube = {
        "origin": [r(pivot[0] - w / 2), r(pivot[1]), r(pivot[2] - w / 2)],
        "size": [r(w), r(h), r(w)],
        "uv": [0, 0],
    }
    return {"name": name, "parent": "root", "pivot": [r(p) for p in pivot], "rotation": rotation, "cubes": [cube]}


def add(path, grip_only, fallback_scale):
    text = path.read_text(encoding="utf-8")
    geo = json.loads(text)["minecraft:geometry"][0]
    bones = {b["name"]: b for b in geo["bones"]}
    if any(n.startswith("hand_") for n in bones):
        print(f"{path.name}: has hand bones, skipped")
        return
    root = bones["root"]["pivot"]
    scale = first_person_scale(path.name.removesuffix(".geo.json"), fallback_scale)
    new = [bone("hand_right", root, GRIP_ROTATION, scale)]
    if not grip_only:
        muzzle = bones["muzzle"]["pivot"] if "muzzle" in bones else [root[0], root[1] + 2, root[2] - 16]
        support = [root[0], muzzle[1] - 2.5, root[2] + 0.4 * (muzzle[2] - root[2])]
        new.append(bone("hand_left", support, SUPPORT_ROTATION, scale))
    # the bones array closes the geometry object: ...}  ]  }  ]  }
    m = re.search(r"\}(\s*)\]\s*\}\s*\]\s*\}\s*$", text)
    if not m:
        raise SystemExit(f"{path}: bones array end not found")
    indent = re.search(r"\n([ \t]*)\{\s*\n?\s*\"name\"", text)
    pad = indent.group(1) if indent else "        "
    unit = "\t" if pad.startswith("\t") else "  "
    added = "".join(",\n" + pad + json.dumps(b, indent=unit).replace("\n", "\n" + pad) for b in new)
    path.write_text(text[:m.start() + 1] + added + text[m.start() + 1:], encoding="utf-8")
    json.loads(path.read_text(encoding="utf-8"))
    print(f"{path.name}: scale {scale}, " + ", ".join(f"{b['name']} {b['pivot']}" for b in new))


def bone_span(text, name):
    """Start and end (exclusive) of the bone object named name in the file text."""
    at = text.index(f'"name": "{name}"')
    start = text.rindex("{", 0, at)
    depth = 0
    for i in range(start, len(text)):
        depth += {"{": 1, "}": -1}.get(text[i], 0)
        if depth == 0:
            return start, i + 1
    raise SystemExit(f"{name}: unbalanced braces")


def set_bone(path, name, pivot, rotation, fallback_scale):
    """Moves and turns a hand bone, keeping its preview cube on the pivot; other keys (parent) are kept."""
    text = path.read_text(encoding="utf-8")
    start, end = bone_span(text, name)
    old = json.loads(text[start:end])
    scale = first_person_scale(path.name.removesuffix(".geo.json"), fallback_scale)
    new = bone(name, pivot, rotation, scale)
    new["parent"] = old.get("parent", "root")
    line_start = text.rindex("\n", 0, start) + 1
    pad = text[line_start:start]
    unit = "\t" if pad.startswith("\t") else "  "
    text = text[:start] + json.dumps(new, indent=unit).replace("\n", "\n" + pad) + text[end:]
    path.write_text(text, encoding="utf-8")
    json.loads(text)
    print(f"{path.name}: {name} pivot {new['pivot']} rotation {rotation}")


def main(args):
    if args and args[0] == "--set":
        # --set <geo file> <bone> x,y,z rx,ry,rz
        floats = lambda s: [r(float(v)) for v in s.split(",")]
        set_bone(pathlib.Path(args[1]), args[2], floats(args[3]), floats(args[4]), 0.5)
        return
    grip_only = "--grip-only" in args
    fallback = 0.5
    if "--scale" in args:
        fallback = float(args[args.index("--scale") + 1])
        del args[args.index("--scale"):args.index("--scale") + 2]
    for a in args:
        if not a.startswith("--"):
            add(pathlib.Path(a), grip_only, fallback)


if __name__ == "__main__":
    main(sys.argv[1:])
