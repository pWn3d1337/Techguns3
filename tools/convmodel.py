"""Converts a Techguns 2 ModelRenderer constructor (1.12 ModelBase) into vanilla PartDefinition calls.

Usage: python convmodel.py <ModelX.java> -> prints `addOrReplaceChild` lines and the texture size.
1.12 semantics: `mirror` only affects boxes added after it is set; setRotationPoint -> PartPose offset;
rotateAngle -> PartPose rotation; addBox(x,y,z,w,h,d[,inflate]).
"""
import re
import sys

src = open(sys.argv[1], encoding='utf-8').read()
ctor = src[src.index('textureWidth'):]
ctor = ctor[:ctor.index('public void render')]

tw = re.search(r'textureWidth\s*=\s*(\d+)', src).group(1)
th = re.search(r'textureHeight\s*=\s*(\d+)', src).group(1)

parts = {}
order = []


def num(s):
    s = s.strip().rstrip('Ff').rstrip('Dd')
    if s.startswith('(float)'):
        s = s[7:]
    return float(eval(s.replace('Math.PI', '3.141592653589793')))


def f(v):
    r = ('%.6f' % v).rstrip('0').rstrip('.')
    if r in ('-0', ''):
        r = '0'
    return r + 'F'


for line in ctor.splitlines():
    line = line.strip()
    m = re.match(r'(\w+)\s*=\s*new ModelRenderer\(this,\s*(\d+),\s*(\d+)\)', line)
    if m:
        n = m.group(1)
        parts[n] = dict(u=int(m.group(2)), v=int(m.group(3)), boxes=[], mirror=False, pivot=(0, 0, 0), rot=(0, 0, 0))
        order.append(n)
        continue
    m = re.match(r'(\w+)\.addBox\((.*)\);', line)
    if m and m.group(1) in parts:
        p = parts[m.group(1)]
        args = [num(a) for a in m.group(2).split(',')]
        p['boxes'].append((args, p['mirror']))
        continue
    m = re.match(r'(\w+)\.setRotationPoint\((.*)\);', line)
    if m and m.group(1) in parts:
        parts[m.group(1)]['pivot'] = tuple(num(a) for a in m.group(2).split(','))
        continue
    m = re.match(r'(\w+)\.mirror\s*=\s*(true|false);', line)
    if m and m.group(1) in parts:
        parts[m.group(1)]['mirror'] = m.group(2) == 'true'
        continue
    m = re.match(r'setRotation\((\w+),(.*)\);', line)
    if m and m.group(1) in parts:
        parts[m.group(1)]['rot'] = tuple(num(a) for a in m.group(2).split(','))
        continue
    m = re.match(r'(\w+)\.rotateAngle([XYZ])\s*=\s*(.*);', line)
    if m and m.group(1) in parts:
        r = list(parts[m.group(1)]['rot'])
        r['XYZ'.index(m.group(2))] = num(m.group(3))
        parts[m.group(1)]['rot'] = tuple(r)
        continue

print('// texture %s x %s' % (tw, th))
for n in order:
    p = parts[n]
    cube = 'CubeListBuilder.create().texOffs(%d, %d)' % (p['u'], p['v'])
    for args, mir in p['boxes']:
        if mir:
            cube += '.mirror()'
        a = args[:6]
        if len(args) > 6 and args[6] != 0:
            cube += '.addBox(%s, new CubeDeformation(%s))' % (', '.join(f(x) for x in a), f(args[6]))
        else:
            cube += '.addBox(%s)' % ', '.join(f(x) for x in a)
        if mir:
            cube += '.mirror(false)'
    pose = 'PartPose.offsetAndRotation(%s)' % ', '.join(f(x) for x in list(p['pivot']) + list(p['rot']))
    print('        root.addOrReplaceChild("%s", %s, %s);' % (n.lower(), cube, pose))
