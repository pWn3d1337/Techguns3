"""Dust item textures of Techguns 3 (one ore ground into two dust in the Grinder).

Pure standard library and deterministic: the same output on every run. Run from the repository root:

    python -I tools/tex_dusts.py src/main/resources/assets/techguns/textures

Writes item/<material>_dust.png (16x16) for every material in MATERIALS: one shared heap shape with grains, shaded
from the top left like the vanilla items, tinted per material from a dark, a mid and a light colour.
"""
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
    'titanium': ((70, 76, 104), (142, 152, 190), (214, 222, 250)),
    'uranium': ((40, 78, 26), (96, 168, 52), (186, 240, 110)),
}

SIZE = 16


def write_png(px, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    raw = b''.join(b'\0' + bytes(c for x in range(SIZE) for c in px[y][x]) for y in range(SIZE))

    def chunk(kind, data):
        return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff)

    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', SIZE, SIZE, 8, 6, 0, 0, 0))
                + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b''))
    print('wrote', path)


class Rng:
    """Small deterministic LCG, seeded by a name."""

    def __init__(self, name):
        self.s = zlib.crc32(name.encode()) or 1

    def next(self):
        self.s = (self.s * 1103515245 + 12345) & 0x7fffffff
        return self.s / 0x7fffffff


def heap_shape():
    """Shade per texel: None outside the heap, else 0 (shadow) .. 1 (highlight). The same for every material."""
    rng = Rng('dust_heap')
    shade = [[None] * SIZE for _ in range(SIZE)]
    # a wide mound: half ellipse over a flat base, centre x 7.5, base row 13, peak row 3
    for y in range(SIZE):
        for x in range(SIZE):
            dx = (x - 7.5) / 7.0
            top = 13.0 - 9.0 * max(0.0, 1.0 - dx * dx) ** 1.3
            if y < round(top) or y > 13 or abs(dx) > 1.0:
                continue
            # light from the top left: higher and further left is brighter, the base is darker
            height = (13.0 - y) / 9.0
            s = 0.25 + 0.55 * height - 0.35 * dx + 0.15
            if y == round(top):
                s += 0.15
            s += (rng.next() - 0.5) * 0.3
            shade[y][x] = max(0.0, min(1.0, s))
    # ragged outline: drop some edge texels, scatter loose grains beside the base
    for y in range(SIZE):
        for x in range(SIZE):
            if shade[y][x] is None:
                continue
            edge = any(not (0 <= x + ox < SIZE and 0 <= y + oy < SIZE) or shade[y + oy][x + ox] is None
                       for ox, oy in ((1, 0), (-1, 0), (0, -1)))
            if edge and y < 11 and rng.next() < 0.12:
                shade[y][x] = None
    for x, y, s in ((0, 12, 0.3), (15, 12, 0.15), (1, 11, 0.35)):
        if shade[y][x] is None:
            shade[y][x] = s
    # bright grains on the lit side, dark grains in the shadow
    for _ in range(9):
        x, y = int(rng.next() * SIZE), int(rng.next() * SIZE)
        if shade[y][x] is not None:
            shade[y][x] = 1.0 if x < 8 else 0.0
    return shade


def mix(a, b, t):
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def tint(shade, colours):
    dark, mid, light = colours
    px = [[(0, 0, 0, 0)] * SIZE for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            s = shade[y][x]
            if s is None:
                continue
            c = mix(dark, mid, s * 2.0) if s < 0.5 else mix(mid, light, (s - 0.5) * 2.0)
            px[y][x] = tuple(int(max(0, min(255, round(v)))) for v in c) + (255,)
    # a dark rim along the base of the heap, like the vanilla item outlines
    for x in range(SIZE):
        if px[13][x][3]:
            px[13][x] = tuple(int(v * 0.6) for v in dark) + (255,)
    return px


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(1)
    shape = heap_shape()
    for material, colours in MATERIALS.items():
        write_png(tint(shape, colours), os.path.join(sys.argv[1], 'item', material + '_dust.png'))


if __name__ == '__main__':
    main()
