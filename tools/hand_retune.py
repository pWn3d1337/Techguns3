"""Lowers the hand bones out of the gun: python tools/hand_retune.py [--grip F] [--dry] <gun> [...].

support hand (hand_left): at its z, or further toward the muzzle past deep parts (magazines), with the arm's top face just
under the gun's underside;
grip hand (hand_right): on the lower part of the pistol grip (the deepest part within 14 px behind the hand origin), or
under the gun at the hand origin when there is no grip below it.
Rotations are kept. Guns whose support hand holds a top handle are skipped for that hand."""
import importlib.util
import json
import math
import pathlib
import sys

TOOLS = pathlib.Path(__file__).resolve().parent
GEO = TOOLS.parent / "src/main/resources/assets/techguns/geo/item"


def rot(rx, ry, rz):
    # Blockbench Bedrock convention in Blockbench space (x mirrored): angles (-rx, -ry, rz), applied Z*Y*X
    ax, ay, az = math.radians(-rx), math.radians(-ry), math.radians(rz)
    cx, sx, cy, sy, cz, sz = math.cos(ax), math.sin(ax), math.cos(ay), math.sin(ay), math.cos(az), math.sin(az)
    Rx = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    Ry = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    Rz = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]
    mul = lambda A, B: [[sum(A[i][k] * B[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
    return mul(Rz, mul(Ry, Rx))


def apply(R, v):
    return [sum(R[i][k] * v[k] for k in range(3)) for i in range(3)]


def transforms(bones):
    """bone name -> function mapping a file-space point to model (file) space at rest."""
    by = {b["name"]: b for b in bones}
    cache = {}

    def fn(name):
        if name in cache:
            return cache[name]
        b = by[name]
        p = b.get("pivot", [0, 0, 0])
        R = rot(*b.get("rotation", [0, 0, 0]))
        parent = fn(b["parent"]) if b.get("parent") in by else (lambda v: v)

        def f(v, p=p, R=R, parent=parent):
            # to Blockbench space (x mirrored), rotate about the pivot, back
            q = [-v[0] - -p[0], v[1] - p[1], v[2] - p[2]]
            q = apply(R, q)
            q = [-(q[0] + -p[0]), q[1] + p[1], q[2] + p[2]]
            return parent(q)
        cache[name] = f
        return f
    return fn


def boxes(gun):
    geo = json.load(open(GEO / f"{gun}.geo.json"))["minecraft:geometry"][0]
    bones = geo["bones"]
    fn = transforms(bones)
    out = []
    for b in bones:
        if b["name"].startswith("hand_"):
            continue
        f = fn(b["name"])
        for c in b.get("cubes", []):
            o, s = c["origin"], c["size"]
            pts = []
            cr = c.get("rotation")
            for dx in (0, s[0]):
                for dy in (0, s[1]):
                    for dz in (0, s[2]):
                        v = [o[0] + dx, o[1] + dy, o[2] + dz]
                        if cr:
                            cp = c.get("pivot", [0, 0, 0])
                            q = [-v[0] + cp[0], v[1] - cp[1], v[2] - cp[2]]
                            q = apply(rot(*cr), q)
                            v = [-(q[0] - cp[0]), q[1] + cp[1], q[2] + cp[2]]
                        pts.append(f(v))
            xs, ys, zs = zip(*pts)
            out.append((min(xs), max(xs), min(ys), max(ys), min(zs), max(zs), b["name"]))
    return {b["name"]: b for b in bones}, out


def underside(bx, z, xc, half=3.0, dz=1.5):
    """Lowest y of the geometry around (xc, z): cubes overlapping x in [xc-half, xc+half] and z in [z-dz, z+dz]."""
    ys = [b[2] for b in bx if b[0] <= xc + half and b[1] >= xc - half and b[4] <= z + dz and b[5] >= z - dz]
    return min(ys) if ys else None


spec = importlib.util.spec_from_file_location("hand_bones", TOOLS / "hand_bones.py")
hb = importlib.util.module_from_spec(spec)
spec.loader.exec_module(hb)

# support hand kept: top handles, and bulky guns whose support hand was placed by hand to show
TOP_HANDLE = {"chainsaw", "minigun", "mining_drill", "tfg", "guided_missile_launcher", "rocket_launcher"}


def r1(v):
    return round(v, 2)


def retune(gun, dry):
    bones, bx = boxes(gun)
    # magazines hang below the gun but are not held
    bx = [b for b in bx if "mag" not in b[6].lower()]
    path = GEO / f"{gun}.geo.json"
    scale = hb.first_person_scale(gun, 0.5)
    half = 2.0 / scale  # half the width of a wide arm in gun pixels
    clear = 0.8 * half  # the tilted arm's top face above its axis, plus a little air
    root = bones["root"]["pivot"]
    xc = root[0]
    out = []

    grip = bones.get("hand_right")
    if grip:
        # deepest part behind the hand origin: the pistol grip
        cands = []
        for z10 in range(int(root[2] * 10) - 20, int(root[2] * 10) + 140, 5):
            z = z10 / 10
            u = underside(bx, z, xc, half=2.0, dz=0.5)
            if u is not None:
                cands.append((u, z))
        if cands:
            low, _ = min(cands)
            zs = [z for u, z in cands if u <= low + 1.0]
            zg = sum(zs) / len(zs)
            if low < root[1] - 3:
                # a grip hangs below the hand origin: the hand on its lower third
                y = low + (root[1] - low) * GRIP_FRACTION
            else:
                under_root = underside(bx, root[2], xc, half=2.0, dz=1.0)
                zg = root[2]
                y = (under_root if under_root is not None else root[1]) - clear
            out.append(("hand_right", [xc, r1(y), r1(zg)], grip["rotation"]))

    sup = bones.get("hand_left")
    if sup and gun not in TOP_HANDLE:
        z = sup["pivot"][2]
        muzzle = bones["muzzle"]["pivot"][2] if "muzzle" in bones else root[2] - 30
        step = -0.5 if muzzle < root[2] else 0.5
        u = underside(bx, z, xc)
        # past magazines and other deep parts toward the muzzle
        while u is not None and u < root[1] - 3 and abs(z - root[2]) < 0.8 * abs(muzzle - root[2]):
            z += step
            u = underside(bx, z, xc)
        if u is not None:
            out.append(("hand_left", [xc, r1(u - clear), r1(z)], sup["rotation"]))

    for name, pivot, rotation in out:
        old = bones[name]["pivot"]
        print(f"{gun}: {name} {old} -> {pivot}")
        if not dry:
            hb.set_bone(path, name, pivot, rotation, 0.5)


GRIP_FRACTION = 0.3

if __name__ == "__main__":
    dry = "--dry" in sys.argv
    if "--grip" in sys.argv:
        # --grip <fraction>: the grip hand at that height of the grip (0 = bottom, 1 = hand origin)
        GRIP_FRACTION = float(sys.argv[sys.argv.index("--grip") + 1])
        del sys.argv[sys.argv.index("--grip"):sys.argv.index("--grip") + 2]
    for g in [a for a in sys.argv[1:] if not a.startswith("--")]:
        retune(g, dry)
