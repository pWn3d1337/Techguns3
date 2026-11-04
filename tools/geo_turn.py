"""Turns a gun's Bedrock geo file 90 degrees about Y so its barrel points along -Z like the other guns.

Usage: python geo_turn.py <file.geo.json>... (rewrites the files in place)

The guns drawn by RenderGunBase90 were exported from Java models whose barrel runs along Java x; the renderer turned
them in code. This bakes that turn N = Ry(90) (Java model space J: pixels, y down) into the file, so the renderer can
use the normal base rotation (see RenderGunBase90 and GeoGunModel#turned for the Java side).

File space F = diag(1, -1, 1) J, bone rotations are the Java angles (Rz Ry Rx, see TGGeoExporter). Per bone:
pivot' = N pivot, rotation' = N R N^-1; per cube the box is turned and stays axis aligned (flat cubes instead get a cube
rotation, see turn_flat_cube). A turned cube's faces land
on other sides and the top and bottom textures turn with them, which box UV cannot express, so every turned cube gets
per-face UV with uv_rotation. Its UVs are found by emulating GeckoLib's quad building (BakedModelFactory, GeoQuad):
for each new face, the UV of each corner must equal the UV the old face had at the same (turned) corner.
"""
import json
import math
import sys

import numpy as np

D = np.diag([1.0, -1.0, 1.0])  # J <-> F
E = np.diag([-1.0, 1.0, 1.0])  # F -> GeckoLib's model space G (x mirrored on load)
N = np.array([[0.0, 0.0, 1.0], [0.0, 1.0, 0.0], [-1.0, 0.0, 0.0]])  # Ry(90)
Q_F = D @ N @ D
Q_G = E @ Q_F @ E

DIRS = {'west': (-1, 0, 0), 'east': (1, 0, 0), 'north': (0, 0, -1), 'south': (0, 0, 1), 'up': (0, 1, 0), 'down': (0, -1, 0)}
ORDER = ['north', 'south', 'west', 'east', 'up', 'down']  # GeckoLib's buildQuads order is irrelevant here


def rx(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])


def ry(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])


def rz(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, -s, 0], [s, c, 0], [0, 0, 1]])


def euler_zyx(r):
    """Degrees (x, y, z) with r = Rz Ry Rx."""
    b = math.asin(max(-1.0, min(1.0, -r[2][0])))
    if abs(math.cos(b)) > 1e-6:
        a = math.atan2(r[2][1], r[2][2])
        c = math.atan2(r[1][0], r[0][0])
    else:
        a = math.atan2(-r[1][2], r[1][1])
        c = 0.0
    out = [math.degrees(a), math.degrees(b), math.degrees(c)]
    assert np.allclose(rz(math.radians(out[2])) @ ry(math.radians(out[1])) @ rx(math.radians(out[0])), r, atol=1e-9)
    return out


def vertex_set(o, s, inf):
    """GeckoLib's VertexSet in G: o = G origin (min corner), s = size."""
    x0, y0, z0 = o[0] - inf, o[1] - inf, o[2] - inf
    x1, y1, z1 = o[0] + s[0] + inf, o[1] + s[1] + inf, o[2] + s[2] + inf
    return {
        'blb': (x0, y0, z0), 'brb': (x0, y0, z1), 'tlb': (x0, y1, z0), 'trb': (x0, y1, z1),
        'tlf': (x1, y1, z0), 'trf': (x1, y1, z1), 'blf': (x1, y0, z0), 'brf': (x1, y0, z1),
    }


QUADS = {
    'west': ['trb', 'tlb', 'blb', 'brb'], 'east': ['tlf', 'trf', 'brf', 'blf'],
    'north': ['tlb', 'tlf', 'blf', 'blb'], 'south': ['trf', 'trb', 'brb', 'brf'],
    'up': ['trb', 'trf', 'tlf', 'tlb'], 'down': ['blb', 'blf', 'brf', 'brb'],
}


def quad_vertices(vs, direction, box_uv, mirror):
    q = direction
    if direction == 'west':
        q = 'east' if mirror else 'west'
    elif direction == 'east':
        q = 'west' if mirror else 'east'
    elif direction == 'up':
        q = 'down' if mirror and not box_uv else 'up'
    elif direction == 'down':
        q = 'up' if mirror and not box_uv else 'down'
    return [vs[k] for k in QUADS[q]]


def rotate_uvs(rot, u, v, uw, vh):
    return [
        [(u, v), (uw, v), (uw, vh), (u, vh)],
        [(uw, v), (uw, vh), (u, vh), (u, v)],
        [(uw, vh), (u, vh), (u, v), (uw, v)],
        [(u, vh), (u, v), (uw, v), (uw, vh)],
    ][rot]


def build_uvs(u, v, us, vs, rot, mirror):
    """GeoQuad.build in pixels: the UV of each of the 4 quad vertices."""
    uw, vh = u + us, v + vs
    if not mirror:
        u, uw = uw, u
    return rotate_uvs(rot, u, v, uw, vh)


def box_face(uv, size, direction):
    x, y, z = (math.floor(size[0]), math.floor(size[1]), math.floor(size[2]))
    u, v = uv
    return {
        'west': ((u + z + x, v + z), (z, y)), 'east': ((u, v + z), (z, y)),
        'north': ((u + z, v + z), (x, y)), 'south': ((u + z + x + z, v + z), (x, y)),
        'up': ((u + z, v), (x, z)), 'down': ((u + z + x, v + z), (x, -z)),
    }[direction]


def old_quads(cube):
    """The cube's GeckoLib quads in G: direction -> (normal, [(position, uv)])."""
    o, s = cube['origin'], cube['size']
    inf = cube.get('inflate', 0.0)
    mirror = cube.get('mirror') is True
    vs = vertex_set((-(o[0] + s[0]), o[1], o[2]), s, inf)
    box = isinstance(cube['uv'], list)
    quads = {}
    for d in DIRS:
        if box:
            (u, v), (us, vsz) = box_face(cube['uv'], s, d)
            rot = 0
        else:
            face = cube['uv'].get(d)
            if face is None:
                continue
            (u, v), (us, vsz) = face['uv'], face.get('uv_size', [0, 0])
            rot = (face.get('uv_rotation', 0) % 360) // 90
        verts = quad_vertices(vs, d, box, mirror)
        uvs = build_uvs(u, v, us, vsz, rot, mirror)
        n = np.array(DIRS[d], dtype=float)
        if mirror:
            n = n * np.array([-1.0, 1.0, 1.0])
        quads[d] = (n, list(zip(verts, uvs)))
    return quads


def close(a, b):
    return all(abs(x - y) < 1e-4 for x, y in zip(a, b))


def num(v):
    v = round(float(v), 6)
    if v == 0:
        return 0
    return int(v) if v == int(v) else v


def turn_flat_cube(cube):
    """A flat cube's two faces on one plane both draw and the later one wins; a cube rotation about the origin keeps the
    quads and their order exactly (rebuilt faces would swap which one is drawn last)."""
    new = dict(cube)
    rot = cube.get('rotation', [0, 0, 0])
    assert rot[0] == 0 and rot[2] == 0 and cube.get('pivot', [0, 0, 0]) == [0, 0, 0], cube
    y = (rot[1] + 90) % 360
    new.pop('rotation', None)
    new.pop('pivot', None)
    if y:
        new['pivot'] = [0, 0, 0]
        new['rotation'] = [0, y, 0]
    return new


def turn_cube(cube):
    if 0 in cube['size']:
        return turn_flat_cube(cube)
    assert 'rotation' not in cube, cube
    quads = old_quads(cube)
    o, s = np.array(cube['origin'], dtype=float), np.array(cube['size'], dtype=float)
    corners = [Q_F @ (o + s * np.array([i, j, k])) for i in (0, 1) for j in (0, 1) for k in (0, 1)]
    lo, hi = np.min(corners, axis=0), np.max(corners, axis=0)
    new = {'origin': [num(x) for x in lo], 'size': [num(x) for x in hi - lo]}
    if 'inflate' in cube:
        new['inflate'] = cube['inflate']
    inf = cube.get('inflate', 0.0)
    vs = vertex_set((-(lo[0] + (hi - lo)[0]), lo[1], lo[2]), hi - lo, inf)
    # old quads by where they lie after the turn; GeckoLib draws a mirrored per-face cube's up quad on the bottom plane
    # (and down on top), so a new face takes the old quad on its plane, by normal only where planes coincide (flat cube)
    turned = [(Q_G @ n, [(Q_G @ np.array(p), uv) for p, uv in pts]) for n, pts in quads.values()]

    def plane_key(points):
        return sorted(tuple(np.round(p, 4)) for p in points)

    faces = {}
    for d in ORDER:
        verts = quad_vertices(vs, d, False, False)
        on_plane = [(n, pts) for n, pts in turned if plane_key([p for p, _ in pts]) == plane_key(verts)]
        if not on_plane:
            continue
        facing = [pts for n, pts in on_plane if np.allclose(n, DIRS[d])]
        target = facing[0] if facing else on_plane[0][1]
        # corners of a flat face coincide, so a corner may accept the UV of any old corner at its place
        want = []
        for p in verts:
            match = [uv for tp, uv in target if close(tp, p)]
            assert match, (d, p)
            want.append(match)
        us_ = sorted({uv[0] for _, uv in target})
        vs_ = sorted({uv[1] for _, uv in target})
        found = None
        for rot in range(4):
            for u in (us_[0], us_[-1]):
                for v in (vs_[0], vs_[-1]):
                    uu = us_[-1] + us_[0] - u - u
                    vv = vs_[-1] + vs_[0] - v - v
                    got = build_uvs(u, v, uu, vv, rot, False)
                    if all(any(close(g, w) for w in ws) for g, ws in zip(got, want)):
                        found = (u, v, uu, vv, rot)
                        break
                if found:
                    break
            if found:
                break
        assert found, (d, want)
        u, v, uu, vv, rot = found
        face = {'uv': [num(u), num(v)], 'uv_size': [num(uu), num(vv)]}
        if rot:
            face['uv_rotation'] = rot * 90
        faces[d] = face
    new['uv'] = faces
    return new


def turn_bone(bone):
    p = np.array(bone['pivot'], dtype=float)
    bone['pivot'] = [num(x) for x in Q_F @ p]
    if 'rotation' in bone:
        r = [math.radians(a) for a in bone['rotation']]
        rj = rz(r[2]) @ ry(r[1]) @ rx(r[0])
        rot = euler_zyx(N @ rj @ N.T)
        if any(abs(a) > 1e-6 for a in rot):
            bone['rotation'] = [num(a) for a in rot]
        else:
            del bone['rotation']
    if 'cubes' in bone:
        bone['cubes'] = [turn_cube(c) for c in bone['cubes']]


class OneLine(str):
    pass


def dump(data):
    """Pretty JSON with each per-face UV entry on one line."""
    def fix(o):
        if isinstance(o, dict):
            if set(o) <= {'uv', 'uv_size', 'uv_rotation'} and 'uv_size' in o:
                return '@@' + json.dumps(o) + '@@'
            return {k: fix(v) for k, v in o.items()}
        if isinstance(o, list):
            return [fix(v) for v in o]
        return o
    text = json.dumps(fix(data), indent=2)
    return text.replace('"@@', '').replace('@@"', '').replace('\\"', '"') + '\n'


def turn_file(path):
    data = json.load(open(path, encoding='utf-8'))
    geo = data['minecraft:geometry'][0]
    for bone in geo['bones']:
        turn_bone(bone)
    # uv_rotation is a 1.21 geometry feature
    data['format_version'] = '1.21.0'
    open(path, 'w', encoding='utf-8', newline='\n').write(dump(data))


if __name__ == '__main__':
    for arg in sys.argv[1:]:
        turn_file(arg)
        print('turned', arg)
