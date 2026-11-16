"""Textures of the Techguns 3 standalone tech (generators, Electric Furnace, cables, pipes, wrench).

Pure standard library and deterministic: the same output on every run. Run from the repository root:

    python -I tools/tex_transport.py src/main/resources/assets/techguns/textures

Writes (all 32x32 unless noted, in the Techguns 2 machine style: noisy dark metal, bevelled frames, rivets):
  block/{combustion_generator,liquid_generator,electric_furnace}_{front,front_on,top}.png
  block/solar_panel_{top,side,frame}.png                              solar panel (top tiles seamlessly)
  block/pipe/{cable,fluid_pipe,item_pipe}_{basic,advanced,elite}.png   pipe part atlas, see PIPE LAYOUT
  block/pipe/connector_{neutral,input,output,disabled}.png            connector plate atlas (disabled: core marks only)
  gui/{combustion_generator,liquid_generator,electric_furnace}_gui.png  256x256, panel from blast_furnace_gui.png
  gui/emptyslots/emptyslot_bucket.png                                 16x16
  item/wrench.png

PIPE LAYOUT (model units of 1/16 block; 1 unit = 2 texels of the 32x32 texture):
  tube side   u 0..3,  v 0..16  (the tube runs along v)
  core face   u 4..8,  v 0..4
  tube end    u 8..11, v 0..3
CONNECTOR LAYOUT: plate face u 0..5, v 0..5; plate edge u 0..5, v 6..7 (its middle, v 6.5..7, is plain mode colour:
the side mode marks on bare core faces use it).
Model coordinates must match techguns.transport.PipeParts and TGPipeModelProvider.
"""
import os
import struct
import sys
import zlib


# --- PNG ---------------------------------------------------------------------------------------------------------

def read_png(path):
    data = open(path, 'rb').read()
    pos, idat = 8, b''
    while pos < len(data):
        length, = struct.unpack('>I', data[pos:pos + 4])
        kind = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if kind == b'IHDR':
            w, h, depth, ctype = struct.unpack('>IIBB', chunk[:10])
        elif kind == b'IDAT':
            idat += chunk
    bpp = {6: 4, 2: 3, 4: 2, 0: 1}[ctype]
    raw = zlib.decompress(idat)
    stride = w * bpp
    rows, prev, p = [], bytearray(stride), 0
    for _ in range(h):
        filt = raw[p]
        p += 1
        line = bytearray(raw[p:p + stride])
        p += stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if filt == 1:
                line[x] = (line[x] + a) & 255
            elif filt == 2:
                line[x] = (line[x] + b) & 255
            elif filt == 3:
                line[x] = (line[x] + (a + b) // 2) & 255
            elif filt == 4:
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                line[x] = (line[x] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        rows.append(line)
        prev = line
    img = Img(w, h)
    for y in range(h):
        for x in range(w):
            v = tuple(rows[y][x * bpp:(x + 1) * bpp])
            if bpp == 3:
                v += (255,)
            elif bpp == 1:
                v = (v[0], v[0], v[0], 255)
            elif bpp == 2:
                v = (v[0], v[0], v[0], v[1])
            img.px[y][x] = v
    return img


def write_png(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    raw = b''.join(b'\0' + bytes(c for x in range(img.w) for c in img.px[y][x]) for y in range(img.h))

    def chunk(kind, data):
        return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff)

    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', img.w, img.h, 8, 6, 0, 0, 0))
                + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b''))
    print('wrote', path)


# --- drawing -----------------------------------------------------------------------------------------------------

CLEAR = (0, 0, 0, 0)


class Img:
    def __init__(self, w, h, fill=CLEAR):
        self.w, self.h = w, h
        self.px = [[fill for _ in range(w)] for _ in range(h)]

    def set(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[y][x] = rgba(c)

    def get(self, x, y):
        return self.px[y][x]

    def rect(self, x0, y0, x1, y1, c):
        """Fills x0..x1, y0..y1 inclusive."""
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)


class Rng:
    """Small deterministic LCG, seeded by a name."""

    def __init__(self, name):
        self.s = zlib.crc32(name.encode()) or 1

    def next(self):
        self.s = (self.s * 1103515245 + 12345) & 0x7fffffff
        return self.s / 0x7fffffff

    def range(self, lo, hi):
        return lo + (hi - lo) * self.next()


def rgba(c):
    c = tuple(int(max(0, min(255, round(v)))) for v in c)
    return c if len(c) == 4 else c + (255,)


def scale(c, f):
    return (c[0] * f, c[1] * f, c[2] * f)


def mix(a, b, t):
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def noise_rect(img, rng, x0, y0, x1, y1, base, var):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            img.set(x, y, scale(base, 1.0 + rng.range(-var, var)))


def shade_rect(img, x0, y0, x1, y1, f):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            c = img.get(x, y)
            img.set(x, y, scale(c, f) + (c[3],))


def bevel(img, x0, y0, x1, y1, raised=True, strength=0.35):
    """Lightens the top/left edge and darkens the bottom/right edge (inverted for a recess)."""
    hi, lo = 1.0 + strength, 1.0 - strength
    if not raised:
        hi, lo = lo, hi
    for x in range(x0, x1 + 1):
        shade_rect(img, x, y0, x, y0, hi)
        shade_rect(img, x, y1, x, y1, lo)
    for y in range(y0 + 1, y1):
        shade_rect(img, x0, y, x0, y, hi)
        shade_rect(img, x1, y, x1, y, lo)


METAL = (78, 78, 82)
PANEL = (178, 172, 160)


def machine_base(name):
    """The shared machine face: noisy dark metal with a bevelled double frame and corner rivets."""
    rng = Rng(name)
    img = Img(32, 32)
    noise_rect(img, rng, 0, 0, 31, 31, METAL, 0.10)
    noise_rect(img, rng, 0, 0, 31, 1, (130, 130, 132), 0.06)
    noise_rect(img, rng, 0, 0, 1, 31, (130, 130, 132), 0.06)
    bevel(img, 0, 0, 31, 31, True, 0.45)
    bevel(img, 2, 2, 29, 29, False, 0.30)
    for rx, ry in ((3, 3), (27, 3), (3, 27), (27, 27)):
        img.set(rx, ry, (170, 170, 170))
        img.set(rx + 1, ry, (120, 120, 120))
        img.set(rx, ry + 1, (120, 120, 120))
        img.set(rx + 1, ry + 1, (45, 45, 45))
    return img, rng


def panel(img, rng, x0, y0, x1, y1):
    noise_rect(img, rng, x0, y0, x1, y1, PANEL, 0.06)
    bevel(img, x0, y0, x1, y1, True, 0.25)


def recess(img, rng, x0, y0, x1, y1, base=(32, 32, 34)):
    noise_rect(img, rng, x0, y0, x1, y1, base, 0.10)
    bevel(img, x0 - 1, y0 - 1, x1 + 1, y1 + 1, False, 0.35)


def glow(t):
    """Fire colours from t = 0 (deep red) to 1 (yellow-white)."""
    return mix((150, 30, 10), (255, 140, 30), min(1, t * 1.6)) if t < 0.6 else mix((255, 140, 30), (255, 235, 150), (t - 0.6) / 0.4)


# --- machine faces -----------------------------------------------------------------------------------------------

def combustion_front(lit):
    img, rng = machine_base('combustion_front')
    panel(img, rng, 5, 5, 26, 11)
    for x in range(8, 24, 3):
        img.rect(x, 7, x + 1, 9, (60, 58, 54))
    recess(img, rng, 6, 15, 25, 26)
    for x in range(6, 26):
        bar = (x - 6) % 3 == 0
        for y in range(15, 27):
            if bar:
                img.set(x, y, scale((120, 120, 124), 1.0 + rng.range(-0.08, 0.08)) if not lit else (170, 130, 100))
            elif lit:
                img.set(x, y, glow((y - 15) / 11.0 * 0.9 + rng.range(0, 0.15)))
            else:
                img.set(x, y, scale((22, 20, 20), 1.0 + rng.range(-0.2, 0.2)))
    img.rect(6, 27, 25, 27, (110, 110, 112))
    return img


def liquid_front(lit):
    img, rng = machine_base('liquid_front')
    # fuel gauge window on the left
    recess(img, rng, 5, 5, 10, 26, (40, 52, 58))
    for y in range(13, 27):
        img.set(6, y, (210, 150, 50) if lit else (150, 105, 40))
        for x in range(7, 11):
            img.set(x, y, scale((190, 130, 40) if lit else (130, 90, 30), 1.0 + rng.range(-0.06, 0.06)))
    for y in range(5, 27, 4):
        img.set(10, y, (200, 200, 200))
    img.rect(6, 5, 6, 12, (90, 120, 130))
    # control panel and lamp
    panel(img, rng, 14, 5, 26, 12)
    img.rect(16, 7, 21, 7, (70, 66, 60))
    img.rect(16, 9, 19, 9, (70, 66, 60))
    img.rect(23, 7, 24, 8, (60, 230, 80) if lit else (90, 30, 30))
    # burner grille
    recess(img, rng, 14, 16, 26, 26)
    for x in range(14, 27):
        for y in range(16, 27):
            if (x - 14) % 3 == 0:
                img.set(x, y, (160, 120, 90) if lit else (115, 115, 118))
            elif lit:
                img.set(x, y, glow((y - 16) / 10.0 * 0.85 + rng.range(0, 0.15)))
            else:
                img.set(x, y, (24, 22, 22))
    return img


def furnace_front(lit):
    img, rng = machine_base('furnace_front')
    noise_rect(img, rng, 5, 5, 26, 25, (96, 96, 100), 0.07)
    bevel(img, 5, 5, 26, 25, True, 0.3)
    recess(img, rng, 8, 8, 23, 22, (26, 28, 32))
    for i, y in enumerate((11, 14, 17, 20)):
        for x in range(9, 23):
            if lit:
                img.set(x, y, glow(0.75 + rng.range(0, 0.2)))
                for dy in (-1, 1):
                    c = img.get(x, y + dy)
                    img.set(x, y + dy, mix(c[:3], (190, 70, 30), 0.45))
            else:
                img.set(x, y, scale((92, 72, 62), 1.0 + rng.range(-0.1, 0.1)))
    # glass glint
    img.set(10, 9, (90, 100, 110))
    img.set(11, 9, (70, 80, 90))
    # handle
    img.rect(10, 27, 21, 28, (170, 170, 172))
    img.rect(10, 28, 21, 28, (90, 90, 92))
    return img


def top_face(name, kind):
    img, rng = machine_base(name)
    if kind == 'exhaust':
        recess(img, rng, 9, 9, 22, 22, (28, 28, 30))
        for y in range(9, 23):
            for x in range(9, 23):
                if (x - 9) % 3 == 0 or (y - 9) % 3 == 0:
                    img.set(x, y, (110, 110, 114))
    elif kind == 'cap':
        for y in range(32):
            for x in range(32):
                d = ((x - 15.5) ** 2 + (y - 15.5) ** 2) ** 0.5
                if d < 6.5:
                    img.set(x, y, scale((175, 160, 60), 1.0 + rng.range(-0.06, 0.06) - (d / 6.5) * 0.25))
                elif d < 7.6:
                    img.set(x, y, (40, 40, 42))
        img.rect(12, 15, 19, 16, (120, 105, 40))
        img.rect(15, 12, 16, 19, (120, 105, 40))
    else:
        for y in range(8, 24, 4):
            recess(img, rng, 8, y, 23, y + 1, (26, 26, 28))
    return img


# --- GUIs --------------------------------------------------------------------------------------------------------

PANEL_GREY = (198, 198, 198)


def slot(img, x, y, w=18, h=18):
    img.rect(x, y, x + w - 1, y + h - 1, (139, 139, 139))
    img.rect(x, y, x + w - 2, y, (55, 55, 55))
    img.rect(x, y, x, y + h - 2, (55, 55, 55))
    img.rect(x + 1, y + h - 1, x + w - 1, y + h - 1, (255, 255, 255))
    img.rect(x + w - 1, y + 1, x + w - 1, y + h - 1, (255, 255, 255))


FLAME = [
    "......#.......",
    "......#.......",
    ".....##.......",
    ".....###......",
    "....####...#..",
    "....#####..#..",
    "...######.##..",
    "..#########...",
    "..##########..",
    ".###########..",
    ".############.",
    ".############.",
    "..##########..",
    "...########...",
]

def stamp(img, x, y, mask, colour_fn):
    for dy, row in enumerate(mask):
        for dx, ch in enumerate(row):
            if ch == '#':
                img.set(x + dx, y + dy, colour_fn(dx, dy))


def arrow_mask():
    """A clean 24x17 progress arrow (shaft and head)."""
    rows = []
    for y in range(17):
        row = ''
        for x in range(24):
            shaft = 6 <= y <= 10 and x < 15
            head = x >= 15 and abs(y - 8) <= (23 - x)
            row += '#' if shaft or head else '.'
        rows.append(row)
    return rows


def gui_base(textures):
    img = read_png(os.path.join(textures, 'gui', 'blast_furnace_gui.png'))
    img.rect(14, 15, 171, 80, PANEL_GREY)
    img.rect(176, 0, 255, 255, CLEAR)
    img.rect(0, 167, 175, 255, CLEAR)
    return img


def flame_sprite(img):
    stamp(img, 176, 0, FLAME, lambda dx, dy: glow(1.0 - dy / 13.0 * 0.9))


def empty_flame(img, x, y):
    stamp(img, x, y, FLAME, lambda dx, dy: (139, 139, 139))


def tank(img, x, y):
    slot(img, x, y, 18, 50)
    img.rect(x + 1, y + 1, x + 16, y + 48, (55, 55, 55))


def tank_scale(img):
    for i in range(0, 48, 6):
        length = 6 if i % 24 == 0 else 3
        img.rect(176 + 16 - length, 31 + i, 176 + 15, 31 + i, (40, 40, 40, 200))


def gui_combustion(textures):
    img = gui_base(textures)
    slot(img, 79, 52)
    empty_flame(img, 81, 35)
    flame_sprite(img)
    return img


def gui_liquid(textures):
    img = gui_base(textures)
    tank(img, 44, 17)
    slot(img, 115, 16)
    slot(img, 115, 52)
    # down arrow from the full to the empty container
    for y in range(36, 50):
        w = 0 if y < 44 else (49 - y)
        img.rect(123 - w, y, 124 + w, y, (139, 139, 139))
    empty_flame(img, 81, 36)
    flame_sprite(img)
    tank_scale(img)
    return img


def gui_furnace(textures):
    img = gui_base(textures)
    slot(img, 55, 34)
    slot(img, 111, 30, 26, 26)
    stamp(img, 79, 35, arrow_mask(), lambda dx, dy: (139, 139, 139))
    stamp(img, 176, 14, arrow_mask(), lambda dx, dy: (255, 255, 255))
    return img


def bucket_icon():
    img = Img(16, 16)
    c = (25, 32, 34)
    for y in range(5, 15):
        inset = (y - 5) // 4
        img.rect(3 + inset, y, 12 - inset, y, c)
    for x in range(3, 13):
        if x in (3, 12):
            img.rect(x, 2, x, 5, c)
    img.rect(4, 1, 11, 1, c)
    return img


# --- pipes -------------------------------------------------------------------------------------------------------

TIERS = ('basic', 'advanced', 'elite')
CABLE_BAND = {'basic': (205, 112, 60), 'advanced': (232, 192, 64), 'elite': (70, 205, 225)}
FLUID_METAL = {'basic': (172, 172, 178), 'advanced': (118, 124, 136), 'elite': (205, 210, 220)}
ITEM_METAL = {'basic': (190, 196, 202), 'advanced': (182, 132, 72), 'elite': (62, 62, 68)}
CYLINDER = (0.55, 0.82, 1.0, 1.0, 0.85, 0.62)


def pipe_texture(kind, tier):
    rng = Rng(kind + tier)
    img = Img(32, 32)
    if kind == 'cable':
        sheath, band = (40, 40, 44), CABLE_BAND[tier]
        for y in range(32):
            ring = y % 8 == 0
            for x in range(6):
                base = band if x in (2, 3) else sheath
                c = scale(base, CYLINDER[x] * (1.15 if ring else 1.0) * (1.0 + rng.range(-0.05, 0.05)))
                img.set(x, y, c)
        core_base, core_mark = (52, 52, 56), band
    elif kind == 'fluid_pipe':
        metal = FLUID_METAL[tier]
        for y in range(32):
            flange = y % 16 in (0, 1)
            for x in range(6):
                if x in (2, 3) and not flange:
                    c = scale((70, 110, 130), 0.9 + 0.2 * (x == 2)) if y % 4 else (150, 190, 205)
                else:
                    c = scale(metal, CYLINDER[x] * (0.8 if flange and y % 16 == 1 else 1.0) * (1.0 + rng.range(-0.04, 0.04)))
                img.set(x, y, c)
        core_base, core_mark = metal, (70, 110, 130)
    else:
        metal = ITEM_METAL[tier]
        for y in range(32):
            rib = y % 4 == 3
            for x in range(6):
                img.set(x, y, scale(metal, CYLINDER[x] * (0.6 if rib else 1.0) * (1.0 + rng.range(-0.05, 0.05))))
        core_base, core_mark = metal, (230, 200, 60) if tier != 'elite' else (180, 60, 220)
    # core face u 4..8 -> texels 8..15
    noise_rect(img, rng, 8, 0, 15, 7, core_base, 0.06)
    bevel(img, 8, 0, 15, 7, True, 0.35)
    img.rect(11, 3, 12, 4, core_mark)
    # tube end u 8..11 -> texels 16..21
    noise_rect(img, rng, 16, 0, 21, 5, scale(core_base, 0.9), 0.05)
    bevel(img, 16, 0, 21, 5, True, 0.3)
    img.rect(18, 2, 19, 3, core_mark if kind == 'cable' else (30, 30, 32))
    return img


CONNECTOR = {'neutral': (150, 150, 155), 'input': (60, 125, 235), 'output': (245, 145, 40), 'disabled': (108, 108, 112)}


def connector_texture(mode):
    rng = Rng('connector' + mode)
    img = Img(32, 32)
    colour = CONNECTOR[mode]
    noise_rect(img, rng, 0, 0, 9, 9, (92, 92, 96), 0.06)
    img.rect(0, 0, 9, 0, colour)
    img.rect(0, 9, 9, 9, colour)
    img.rect(0, 0, 0, 9, colour)
    img.rect(9, 0, 9, 9, colour)
    bevel(img, 0, 0, 9, 9, True, 0.25)
    recess(img, rng, 3, 3, 6, 6, (36, 36, 40))
    for bx, by in ((1, 1), (8, 1), (1, 8), (8, 8)):
        img.set(bx, by, (190, 190, 190))
    # edge strip v 6..7 -> texels 12..15
    for y in range(12, 16):
        for x in range(10):
            img.set(x, y, scale(colour, 0.75 if y in (12, 15) else 1.0))
    return img


# --- items -------------------------------------------------------------------------------------------------------

def wrench_icon():
    img = Img(32, 32)
    rng = Rng('wrench')

    def seg_dist(px, py, ax, ay, bx, by):
        dx, dy = bx - ax, by - ay
        t = max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)))
        return ((px - ax - t * dx) ** 2 + (py - ay - t * dy) ** 2) ** 0.5

    for y in range(32):
        for x in range(32):
            handle = seg_dist(x + 0.5, y + 0.5, 6, 26, 19, 13) < 2.2
            head = ((x + 0.5 - 22) ** 2 + (y + 0.5 - 10) ** 2) ** 0.5 < 6.2
            jaw = seg_dist(x + 0.5, y + 0.5, 22, 10, 29, 3) < 2.6
            if (handle or head) and not jaw:
                light = 1.0 + (16 - x - y) / 40.0 + rng.range(-0.05, 0.05)
                img.set(x, y, scale((150, 156, 165), light))
    # outline
    out = Img(32, 32)
    for y in range(32):
        for x in range(32):
            out.px[y][x] = img.px[y][x]
            if img.px[y][x][3] == 0 and any(
                    0 <= x + dx < 32 and 0 <= y + dy < 32 and img.px[y + dy][x + dx][3]
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                out.px[y][x] = rgba((30, 30, 34))
    # grip
    for y in range(32):
        for x in range(32):
            if out.px[y][x][3] and img.px[y][x][3] and seg_dist(x + 0.5, y + 0.5, 6, 26, 12, 20) < 2.3:
                out.px[y][x] = rgba(scale((170, 40, 35), 1.0 + rng.range(-0.08, 0.08)))
    return out


# --- solar panel -------------------------------------------------------------------------------------------------

def solar_top():
    """Seamless cells: a grid line every 8 texels (also on the edge), so neighbouring panels read as one surface."""
    rng = Rng('solar_top')
    img = Img(32, 32)
    for y in range(32):
        for x in range(32):
            if x % 8 == 0 or y % 8 == 0:
                img.set(x, y, scale((168, 172, 182), 1.0 + rng.range(-0.04, 0.04)))
                continue
            cx, cy = x % 8, y % 8
            shade = 0.85 + 0.25 * (cx + cy) / 14.0
            c = scale((30, 55, 110), shade * (1.0 + rng.range(-0.05, 0.05)))
            if cy == 4:
                c = mix(c, (110, 140, 190), 0.45)
            if cx == 6 and cy == 2:
                c = mix(c, (200, 220, 255), 0.6)
            img.set(x, y, c)
    return img


def solar_side():
    """Slab side (the lower half is used): a metal frame with a lit top edge."""
    rng = Rng('solar_side')
    img = Img(32, 32)
    noise_rect(img, rng, 0, 0, 31, 31, (104, 106, 112), 0.07)
    for y in (16, 31):
        img.rect(0, y, 31, y, (150, 152, 160) if y == 16 else (60, 60, 64))
    for x in range(2, 32, 10):
        img.set(x, 23, (170, 170, 175))
        img.set(x + 1, 24, (55, 55, 58))
    return img


def solar_frame():
    """The raised rail around the outer edge of a panel array."""
    rng = Rng('solar_frame')
    img = Img(32, 32)
    noise_rect(img, rng, 0, 0, 31, 31, (150, 152, 160), 0.06)
    bevel(img, 0, 0, 31, 31, True, 0.25)
    return img


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(1)
    root = sys.argv[1]
    block = os.path.join(root, 'block')
    for lit, suffix in ((False, ''), (True, '_on')):
        write_png(combustion_front(lit), os.path.join(block, 'combustion_generator_front' + suffix + '.png'))
        write_png(liquid_front(lit), os.path.join(block, 'liquid_generator_front' + suffix + '.png'))
        write_png(furnace_front(lit), os.path.join(block, 'electric_furnace_front' + suffix + '.png'))
    write_png(top_face('combustion_top', 'exhaust'), os.path.join(block, 'combustion_generator_top.png'))
    write_png(top_face('liquid_top', 'cap'), os.path.join(block, 'liquid_generator_top.png'))
    write_png(top_face('furnace_top', 'vents'), os.path.join(block, 'electric_furnace_top.png'))
    write_png(solar_top(), os.path.join(block, 'solar_panel_top.png'))
    write_png(solar_side(), os.path.join(block, 'solar_panel_side.png'))
    write_png(solar_frame(), os.path.join(block, 'solar_panel_frame.png'))
    for kind in ('cable', 'fluid_pipe', 'item_pipe'):
        for tier in TIERS:
            write_png(pipe_texture(kind, tier), os.path.join(block, 'pipe', kind + '_' + tier + '.png'))
    for mode in CONNECTOR:
        write_png(connector_texture(mode), os.path.join(block, 'pipe', 'connector_' + mode + '.png'))
    gui = os.path.join(root, 'gui')
    write_png(gui_combustion(root), os.path.join(gui, 'combustion_generator_gui.png'))
    write_png(gui_liquid(root), os.path.join(gui, 'liquid_generator_gui.png'))
    write_png(gui_furnace(root), os.path.join(gui, 'electric_furnace_gui.png'))
    write_png(bucket_icon(), os.path.join(gui, 'emptyslots', 'emptyslot_bucket.png'))
    write_png(wrench_icon(), os.path.join(root, 'item', 'wrench.png'))


if __name__ == '__main__':
    main()
