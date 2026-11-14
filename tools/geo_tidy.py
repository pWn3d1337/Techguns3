"""Undoes Blockbench's formatting changes in exported gun geo files: python tools/geo_tidy.py [--base REV] [--dry] <gun|file> [...].

A Blockbench export (Ctrl+S on an opened .geo.json, or Export Bedrock Geometry) rewrites the whole file: bones in
outliner order, 0.0 written as 0, recomputed visible bounds, format_version, per-face UVs spread over many lines, and
"mirror" dropped on per-face UV cubes (where it has no effect). That buries the real edit in a diff of hundreds of
lines. This script compares the file with its committed version (git HEAD, or --base REV) and writes the committed
text back with only the real changes spliced in:
- the committed header (format_version, description) and bone order, with new bones appended in export order;
- every bone that is unchanged (numerically, ignoring "mirror" on per-face UV cubes) as its committed text;
- changed and new bones from the export, floats rounded to 4 decimals, in the committed file's indentation (per-face
  UV entries on one line when the committed file has them that way).
The description is taken from the export only when its identifier or texture size changed. A file that is not in git
is only reformatted."""
import json
import pathlib
import re
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
GEO = ROOT / "src/main/resources/assets/techguns/geo/item"
FACES = {"north", "south", "east", "west", "up", "down"}


def committed(path, rev):
    try:
        rel = path.resolve().relative_to(ROOT).as_posix()
    except ValueError:  # outside the repository
        return None
    r = subprocess.run(["git", "show", f"{rev}:{rel}"], cwd=ROOT, capture_output=True, text=True, encoding="utf-8")
    return r.stdout.replace("\r\n", "\n") if r.returncode == 0 else None


def normalized(bone):
    """The bone without differences Blockbench introduces that don't change the model."""
    b = json.loads(json.dumps(bone))
    for c in b.get("cubes", []):
        if isinstance(c.get("uv"), dict):
            c.pop("mirror", None)  # mirror only affects box UV
    return b


def rounded(v):
    if isinstance(v, float):
        v = round(v, 4)
        return int(v) if v == int(v) else v
    if isinstance(v, list):
        return [rounded(x) for x in v]
    if isinstance(v, dict):
        return {k: rounded(x) for k, x in v.items()}
    return v


def bone_spans(text):
    """(start, end) of each object in the first geometry's bones array, in file order."""
    at = text.index("[", text.index('"bones"'))
    spans, depth, start, in_str, i = [], 0, None, False, at + 1
    while i < len(text):
        ch = text[i]
        if in_str:
            if ch == "\\":
                i += 1
            elif ch == '"':
                in_str = False
        elif ch == '"':
            in_str = True
        elif ch == "{":
            if depth == 0:
                start = i
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                spans.append((start, i + 1))
        elif ch == "]" and depth == 0:
            return spans
        i += 1
    raise SystemExit("bones array not closed")


def dump(v, unit, level, inline_faces, key=None):
    """json.dumps(indent=unit) at the given nesting level; face objects on one line when inline_faces."""
    if inline_faces and key in FACES and isinstance(v, dict):
        return json.dumps(v, separators=(", ", ": "))
    pad, inner = unit * level, unit * (level + 1)
    if isinstance(v, dict) and v:
        items = [f"{inner}{json.dumps(k)}: {dump(x, unit, level + 1, inline_faces, k)}" for k, x in v.items()]
        return "{\n" + ",\n".join(items) + "\n" + pad + "}"
    if isinstance(v, list) and v:
        return "[\n" + ",\n".join(inner + dump(x, unit, level + 1, inline_faces) for x in v) + "\n" + pad + "]"
    return json.dumps(v)


def tidy(path, rev, dry):
    text = path.read_text(encoding="utf-8").replace("\r\n", "\n")
    new = json.loads(text)
    base_text = committed(path, rev)
    ng = new["minecraft:geometry"][0]
    if base_text is None:
        out = json.dumps(new, indent=2) + ("\n" if text.endswith("\n") else "")
        print(f"{path.name}: not in {rev}, reformatted only")
    else:
        base = json.loads(base_text)
        bg = base["minecraft:geometry"][0]
        spans = bone_spans(base_text)
        if len(spans) != len(bg["bones"]):
            raise SystemExit(f"{path.name}: could not split the committed bones")
        first = spans[0][0]
        line_start = base_text.rindex("\n", 0, first) + 1
        pad = base_text[line_start:first]
        unit = "\t" if pad.startswith("\t") else "  "
        level = len(pad) // len(unit)
        inline_faces = re.search(r'"(north|south|east|west|up|down)": \{"uv"', base_text) is not None

        new_bones = {b["name"]: b for b in ng["bones"]}
        parts, changed, removed = [], [], []
        for b, (s, e) in zip(bg["bones"], spans):
            n = new_bones.pop(b["name"], None)
            if n is None:
                removed.append(b["name"])
            elif normalized(n) == normalized(b):
                parts.append(base_text[s:e])
            else:
                parts.append(dump(rounded(n), unit, level, inline_faces))
                changed.append(b["name"])
        added = [b["name"] for b in ng["bones"] if b["name"] in new_bones]
        parts += [dump(rounded(new_bones[n]), unit, level, inline_faces) for n in added]
        body = base_text[:first] + (",\n" + pad).join(parts) + base_text[spans[-1][1]:]

        nd, bd = ng["description"], bg["description"]
        if any(nd.get(k) != bd.get(k) for k in ("identifier", "texture_width", "texture_height")):
            d = json.loads(body)
            d["minecraft:geometry"][0]["description"] = nd
            body = json.dumps(d, indent=unit)
            print(f"{path.name}: description changed, file reformatted")
        out = body
        print(f"{path.name}: changed {changed or '-'}, added {added or '-'}, removed {removed or '-'}")
    check = json.loads(out)["minecraft:geometry"][0]["bones"]
    if [rounded(normalized(b)) for b in sorted(check, key=lambda b: b["name"])] != \
            [rounded(normalized(b)) for b in sorted(ng["bones"], key=lambda b: b["name"])]:
        raise SystemExit(f"{path.name}: tidied bones differ from the export, file left as it is")
    if not dry and out != text:
        path.write_text(out, encoding="utf-8", newline="\n")


def resolve(arg):
    p = pathlib.Path(arg)
    if p.suffix == ".json":
        return p
    return GEO / f"{arg}.geo.json"


if __name__ == "__main__":
    args = sys.argv[1:]
    dry = "--dry" in args
    rev = "HEAD"
    if "--base" in args:
        i = args.index("--base")
        rev = args[i + 1]
        del args[i:i + 2]
    files = [resolve(a) for a in args if not a.startswith("--")]
    if not files:
        print(__doc__)
        sys.exit(1)
    for f in files:
        if not f.is_file():
            print(f"{f}: no such file")
            continue
        tidy(f, rev, dry)
