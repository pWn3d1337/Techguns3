"""Dust item textures of Techguns 3 (one ore ground into two dust in the Grinder).

Pure standard library and deterministic: the same output on every run. Run from the repository root:

    python -I tools/tex_dusts.py src/main/resources/assets/techguns/textures

Writes item/<material>_dust.png (32x32, like most Techguns items) for every material in MATERIALS: one shared heap
shape with coarse and fine grains and a dark outline, shaded from the top left like the vanilla items, tinted per
material from a dark, a mid and a light colour. Two materials are animated like their ores (a vertical frame strip and
a .png.mcmeta next to it): titanium sparkles with white glints that flare up one after another, uranium glows with a
slow yellow-green pulse.
"""
import math
import os
import struct
import sys
import zlib


# material: (dark, mid, light) -- the shades of the heap, from shadow to highlight
MATERIALS = {
    'iron': ((98, 72, 60), (176, 146, 128), (232, 212, 196)),
    'gold': ((140, 84, 18), (232, 182, 46), (255, 244, 140)),
    'copper': ((112, 52, 32), (196, 108, 72), (240, 170, 130)),
    'tin': ((104, 112, 108), (184, 194, 188), (240, 246, 240)),
    'lead': ((44, 46, 62), (92, 96, 120), (148, 154, 178)),
    'titanium': ((56, 58, 96), (118, 126, 180), (186, 196, 240)),
    'uranium': ((36, 72, 22), (92, 162, 48), (176, 234, 100)),
}

SIZE = 32
# heap: centre column, base row, peak height and half width (texels)
CX = 15.5
BASE = 27
HEIGHT = 19.0
HALF = 14.5

# animation frames and ticks per frame (interpolated like the titanium and uranium ores)
SPARKLE_FRAMES = 16
SPARKLE_FRAMETIME = 3
GLOW_FRAMES = 8
GLOW_FRAMETIME = 4


def write_png(px, path):
    """px: rows of RGBA tuples (any height, SIZE wide)."""
    os.makedirs(os.path.dirname(path), exist_ok=True)
    height = len(px)
    raw = b''.join(b'\0' + bytes(c for x in range(SIZE) for c in px[y][x]) for y in range(height))

    def chunk(kind, data):
        return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff)

    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', SIZE, height, 8, 6, 0, 0, 0))
                + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b''))
    print('wrote', path)


def write_mcmeta(path, frametime):
    with open(path + '.mcmeta', 'w', newline='\n') as f:
        f.write('{\n  "animation": {\n    "frametime": %d,\n    "interpolate": true\n  }\n}\n' % frametime)
    print('wrote', path + '.mcmeta')


def remove_mcmeta(path):
    if os.path.exists(path + '.mcmeta'):
        os.remove(path + '.mcmeta')


class Rng:
    """Small deterministic LCG, seeded by a name."""

    def __init__(self, name):
        self.s = zlib.crc32(name.encode()) or 1

    def next(self):
        self.s = (self.s * 1103515245 + 12345) & 0x7fffffff
        return self.s / 0x7fffffff


def heap_top(x):
    dx = (x - CX) / HALF
    if abs(dx) > 1.0:
        return None
    return BASE - HEIGHT * max(0.0, 1.0 - dx * dx) ** 1.3


def heap_shape():
    """Shade per texel: None outside the heap, else 0 (shadow) .. 1 (highlight). The same for every material."""
    rng = Rng('dust_heap_32')
    # coarse grains: one random value per 2x2 block, so the heap reads as granular at 32 px
    coarse = [[rng.next() - 0.5 for _ in range(SIZE // 2 + 1)] for _ in range(SIZE // 2 + 1)]
    shade = [[None] * SIZE for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            top = heap_top(x)
            if top is None or y < round(top) or y > BASE:
                continue
            dx = (x - CX) / HALF
            # light from the top left: higher and further left is brighter, the base is darker
            height = (BASE - y) / HEIGHT
            s = 0.3 + 0.55 * height - 0.35 * dx + 0.1
            # the lit crest just under the surface
            if y - round(top) <= 1:
                s += 0.12
            s += coarse[y // 2][x // 2] * 0.28 + (rng.next() - 0.5) * 0.16
            shade[y][x] = max(0.0, min(1.0, s))
    # ragged outline on the upper slopes
    for y in range(SIZE):
        for x in range(SIZE):
            if shade[y][x] is None or y > BASE - 4:
                continue
            edge = any(not (0 <= x + ox < SIZE and 0 <= y + oy < SIZE) or shade[y + oy][x + ox] is None
                       for ox, oy in ((1, 0), (-1, 0), (0, -1)))
            if edge and rng.next() < 0.18:
                shade[y][x] = None
    # loose grains rolled off beside the base
    for x, y, s in ((0, BASE - 1, 0.3), (1, BASE - 2, 0.4), (30, BASE - 1, 0.15), (31, BASE, 0.1), (2, BASE, 0.35)):
        if shade[y][x] is None:
            shade[y][x] = s
    # bright grains on the lit side, dark grains in the shadow
    for _ in range(26):
        x, y = int(rng.next() * SIZE), int(rng.next() * SIZE)
        if shade[y][x] is not None:
            shade[y][x] = 1.0 if x < CX else 0.0
    return shade


def mix(a, b, t):
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def clamp(c):
    return tuple(int(max(0, min(255, round(v)))) for v in c)


def tint(shade, colours):
    dark, mid, light = colours
    px = [[(0, 0, 0, 0)] * SIZE for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            s = shade[y][x]
            if s is None:
                continue
            c = mix(dark, mid, s * 2.0) if s < 0.5 else mix(mid, light, (s - 0.5) * 2.0)
            px[y][x] = clamp(c) + (255,)
    outline = clamp(tuple(v * 0.45 for v in dark)) + (255,)
    # a dark rim along the base, and a dark outline around the outside of the heap like the Techguns 32 px items
    for x in range(SIZE):
        if px[BASE][x][3]:
            px[BASE][x] = clamp(tuple(v * 0.6 for v in dark)) + (255,)
    edges = []
    for y in range(SIZE):
        for x in range(SIZE):
            if px[y][x][3]:
                continue
            if any(0 <= x + ox < SIZE and 0 <= y + oy < SIZE and shade[y + oy][x + ox] is not None
                   for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                edges.append((x, y))
    for x, y in edges:
        px[y][x] = outline
    return px


def copy(px):
    return [list(row) for row in px]


# --- titanium: glints flaring up one after another ---

def sparkle_frames(base, shade):
    rng = Rng('titanium_sparkle')
    spots = []
    while len(spots) < 9:
        x, y = 3 + int(rng.next() * (SIZE - 6)), 6 + int(rng.next() * (BASE - 8))
        if shade[y][x] is not None and all(abs(x - sx) + abs(y - sy) > 6 for sx, sy, _ in spots):
            spots.append((x, y, len(spots) / 9.0))
    white = (255, 255, 255)
    frames = []
    for f in range(SPARKLE_FRAMES):
        px = copy(base)
        for x, y, phase in spots:
            # each glint is bright for about a quarter of the loop
            t = (f / SPARKLE_FRAMES + phase) % 1.0
            k = max(0.0, math.cos((t - 0.5) * math.pi * 3.0)) if 1.0 / 6.0 < t < 5.0 / 6.0 else 0.0
            if k <= 0.05:
                continue
            arm = 3 if k > 0.7 else 2 if k > 0.35 else 1
            points = [(0, 0, 1.0)] + [(d * sx, d * sy, 1.0 / d) for d in range(1, arm + 1)
                                      for sx, sy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
            if k > 0.7:
                points += [(sx, sy, 0.45) for sx, sy in ((1, 1), (-1, 1), (1, -1), (-1, -1))]
            for ox, oy, w in points:
                tx, ty = x + ox, y + oy
                if 0 <= tx < SIZE and 0 <= ty < SIZE and px[ty][tx][3] == 255 and shade[ty][tx] is not None:
                    c = px[ty][tx][:3]
                    px[ty][tx] = clamp(mix(c, white, min(1.0, k * w))) + (255,)
        frames.append(px)
    return frames


# --- uranium: a slow glow pulse ---

def glow_frames(shade, colours):
    dark, mid, light = colours
    hot = (226, 255, 120)
    rng = Rng('uranium_glow')
    # glowing grains with their own phase
    grains = [(int(rng.next() * SIZE), int(rng.next() * SIZE), rng.next()) for _ in range(40)]
    frames = []
    for f in range(GLOW_FRAMES):
        p = 0.5 - 0.5 * math.cos(2.0 * math.pi * f / GLOW_FRAMES)
        lit = (mix(dark, mid, 0.45 * p), mix(mid, light, 0.6 * p), mix(light, hot, 0.9 * p))
        px = tint(shade, lit)
        # the outline glows with the pulse
        for y in range(SIZE):
            for x in range(SIZE):
                if px[y][x][3] and shade[y][x] is None:
                    px[y][x] = clamp(mix(px[y][x][:3], (110, 190, 40), 0.85 * p)) + (255,)
        for x, y, phase in grains:
            if shade[y][x] is None:
                continue
            g = 0.5 - 0.5 * math.cos(2.0 * math.pi * (f / GLOW_FRAMES + phase))
            px[y][x] = clamp(mix(px[y][x][:3], (250, 255, 190), 0.3 + 0.7 * g)) + (255,)
        frames.append(px)
    return frames


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(1)
    shape = heap_shape()
    for material, colours in MATERIALS.items():
        path = os.path.join(sys.argv[1], 'item', material + '_dust.png')
        if material == 'titanium':
            frames = sparkle_frames(tint(shape, colours), shape)
            write_png([row for frame in frames for row in frame], path)
            write_mcmeta(path, SPARKLE_FRAMETIME)
        elif material == 'uranium':
            frames = glow_frames(shape, colours)
            write_png([row for frame in frames for row in frame], path)
            write_mcmeta(path, GLOW_FRAMETIME)
        else:
            write_png(tint(shape, colours), path)
            remove_mcmeta(path)


if __name__ == '__main__':
    main()
