"""Merges a gun's split geo file (a part that moved in code, e.g. <gun>_1.geo.json) into its main geo file.

The part's bones go under a new group bone (child of root, pivot 0) named after the part, so keyframes can move the
part as a whole (docs/gun_animation_plan.md, phase 5). The part file must use the same texture size; its root bone is
dropped (its pivot is unused: the root bones of the exported files sit at the origin or at the hand origin, and the
group bone has no rotation). The part file is deleted.

Usage: python tools/geo_merge.py <main.geo.json> <part.geo.json> <group bone name>
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from geo_turn import dump  # same layout as the turned files (one line per face UV)


def version(v):
    return tuple(int(x) for x in v.split("."))


def main():
    main_path, part_path, group = sys.argv[1:4]
    with open(main_path, encoding="utf-8") as f:
        main_doc = json.load(f)
    with open(part_path, encoding="utf-8") as f:
        part_doc = json.load(f)
    main_geo = main_doc["minecraft:geometry"][0]
    part_geo = part_doc["minecraft:geometry"][0]
    for key in ("texture_width", "texture_height"):
        if main_geo["description"][key] != part_geo["description"][key]:
            sys.exit(f"{key} differs: {main_geo['description'][key]} vs {part_geo['description'][key]}")
    names = {b["name"] for b in main_geo["bones"]}
    if group in names:
        sys.exit(f"bone {group} already exists")
    bones = [{"name": group, "parent": "root", "pivot": [0, 0, 0]}]
    for bone in part_geo["bones"]:
        if bone["name"] == "root":
            if bone.get("cubes") or bone.get("rotation"):
                sys.exit("part root has cubes or a rotation")
            continue
        if bone["name"] in names:
            sys.exit(f"bone {bone['name']} exists in both files")
        bone = dict(bone)
        if bone.get("parent") == "root":
            bone["parent"] = group
        bones.append(bone)
    main_geo["bones"].extend(bones)
    if version(part_doc["format_version"]) > version(main_doc["format_version"]):
        main_doc["format_version"] = part_doc["format_version"]
    with open(main_path, "w", encoding="utf-8", newline="\n") as f:
        f.write(dump(main_doc))
    os.remove(part_path)
    print(f"merged {len(bones) - 1} bones of {part_path} into {main_path} under {group}")


if __name__ == "__main__":
    main()
