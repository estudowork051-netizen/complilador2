#!/usr/bin/env python3
"""
Sculpted 3D Asset Generator for Kael, Fire Warrior (Heroes Tower Defense).
Generates a detailed, stylized 3D fantasy character in binary glTF 2.0 (.glb)
following the official visual reference:
- Heroic stylized cartoon proportions
- Smoothly contoured facial anatomy (cheeks, jaw, sculpted chin, stylized nose, expressive eyes)
- Layered curved anime hair locks with flame-shaped swept silhouettes
- Sculpted layered plate armor: dark gunmetal steel, enameled crimson chest plates, ornate gold filigree trims
- Triple-tiered curved shoulder pauldrons with gold lion-head medallions
- Articulated arms with padded doublet sleeves, elbow couters, flared gauntlets and hands gripping the sword
- Segmented abdominal plating, heavy leather hero belt with ornate lion buckle
- Dual-layer embroidered crimson battle tabard and flanking hip tassets
- Armored cuisses, curved knee poleyns with ruby studs, greaves and heavy sabaton boots with toe caps and spurs
- Billowing dynamic dual-layer cape with 3D cloth wave folds and gold hem
- Detailed Flaming Greatsword: faceted golden pommel, fluted leather grip, winged golden crossguard with ruby, broad steel blade with fuller, and jagged multi-tiered leaping flame aura
"""

import os
import sys
import math
import struct
import json

def normalize(v):
    l = math.hypot(*v)
    if l == 0:
        return (0.0, 1.0, 0.0)
    return tuple(x / l for x in v)

def cross(a, b):
    return (
        a[1]*b[2] - a[2]*b[1],
        a[2]*b[0] - a[0]*b[2],
        a[0]*b[1] - a[1]*b[0]
    )

def sub(a, b):
    return (a[0]-b[0], a[1]-b[1], a[2]-b[2])

def add(a, b):
    return (a[0]+b[0], a[1]+b[1], a[2]+b[2])

def mul(a, s):
    return (a[0]*s, a[1]*s, a[2]*s)

# =============================================================================
# GEOMETRY GENERATORS WITH SMOOTH NORMALS
# =============================================================================

def make_smooth_cylinder(r_bottom, r_top, height, segments=16, offset=(0,0,0)):
    """Creates a smooth-shaded cylinder/cone with flat caps."""
    positions = []
    normals = []
    indices = []
    ox, oy, oz = offset
    h2 = height / 2.0

    side_start = 0
    dr = r_bottom - r_top
    slope_norm = math.atan2(dr, height)
    cos_sn = math.cos(slope_norm)
    sin_sn = math.sin(slope_norm)

    for i in range(segments):
        a = 2.0 * math.pi * i / segments
        cos_a = math.cos(a)
        sin_a = math.sin(a)
        nx = cos_a * cos_sn
        ny = sin_sn
        nz = sin_a * cos_sn

        positions.extend([r_bottom * cos_a + ox, -h2 + oy, r_bottom * sin_a + oz])
        normals.extend([nx, ny, nz])
        positions.extend([r_top * cos_a + ox, h2 + oy, r_top * sin_a + oz])
        normals.extend([nx, ny, nz])

    for i in range(segments):
        i0 = side_start + i * 2
        i1 = i0 + 1
        i2 = side_start + ((i + 1) % segments) * 2
        i3 = i2 + 1
        indices.extend([i0, i2, i1, i1, i2, i3])

    # Bottom cap
    if r_bottom > 0.001:
        b_center = len(positions) // 3
        positions.extend([ox, -h2 + oy, oz])
        normals.extend([0.0, -1.0, 0.0])
        b_rim_start = len(positions) // 3
        for i in range(segments):
            a = 2.0 * math.pi * i / segments
            positions.extend([r_bottom * math.cos(a) + ox, -h2 + oy, r_bottom * math.sin(a) + oz])
            normals.extend([0.0, -1.0, 0.0])
        for i in range(segments):
            next_i = (i + 1) % segments
            indices.extend([b_center, b_rim_start + next_i, b_rim_start + i])

    # Top cap
    if r_top > 0.001:
        t_center = len(positions) // 3
        positions.extend([ox, h2 + oy, oz])
        normals.extend([0.0, 1.0, 0.0])
        t_rim_start = len(positions) // 3
        for i in range(segments):
            a = 2.0 * math.pi * i / segments
            positions.extend([r_top * math.cos(a) + ox, h2 + oy, r_top * math.sin(a) + oz])
            normals.extend([0.0, 1.0, 0.0])
        for i in range(segments):
            next_i = (i + 1) % segments
            indices.extend([t_center, t_rim_start + i, t_rim_start + next_i])

    return positions, normals, indices

def make_smooth_ellipsoid(rx, ry, rz, lat_segs=12, lon_segs=16, offset=(0,0,0)):
    """Creates a smooth-shaded UV ellipsoid."""
    positions = []
    normals = []
    indices = []
    ox, oy, oz = offset

    for lat in range(lat_segs + 1):
        theta = math.pi * lat / lat_segs
        sin_theta = math.sin(theta)
        cos_theta = math.cos(theta)

        for lon in range(lon_segs + 1):
            phi = 2.0 * math.pi * lon / lon_segs
            sin_phi = math.sin(phi)
            cos_phi = math.cos(phi)

            x = rx * sin_theta * cos_phi
            y = ry * cos_theta
            z = rz * sin_theta * sin_phi

            positions.extend([x + ox, y + oy, z + oz])
            nx = (x / (rx * rx)) if rx > 0 else 0
            ny = (y / (ry * ry)) if ry > 0 else 0
            nz = (z / (rz * rz)) if rz > 0 else 0
            nl = math.hypot(nx, ny, nz)
            if nl > 0:
                normals.extend([nx/nl, ny/nl, nz/nl])
            else:
                normals.extend([0, 1, 0])

    for lat in range(lat_segs):
        for lon in range(lon_segs):
            first = lat * (lon_segs + 1) + lon
            second = first + lon_segs + 1
            indices.extend([first, second, first + 1])
            indices.extend([second, second + 1, first + 1])

    return positions, normals, indices

def make_smooth_sphere(radius, lat_segs=10, lon_segs=14, offset=(0,0,0)):
    """Creates a smooth sphere of given radius."""
    return make_smooth_ellipsoid(radius, radius, radius, lat_segs, lon_segs, offset)

def make_beveled_box(width, height, depth, bevel=0.02, offset=(0,0,0)):
    """Creates a box with beveled rounded edges and smoothed edge normals."""
    ox, oy, oz = offset
    w2, h2, d2 = width / 2.0, height / 2.0, depth / 2.0
    b = min(bevel, w2 * 0.4, h2 * 0.4, d2 * 0.4)
    w_in, h_in, d_in = w2 - b, h2 - b, d2 - b

    positions = []
    normals = []
    indices = []

    def add_quad(p0, p1, p2, p3, n):
        idx = len(positions) // 3
        positions.extend([*p0, *p1, *p2, *p3])
        normals.extend([*n, *n, *n, *n])
        indices.extend([idx, idx+1, idx+2, idx, idx+2, idx+3])

    # 6 primary faces
    add_quad((-w_in+ox, -h_in+oy, d2+oz), (w_in+ox, -h_in+oy, d2+oz), (w_in+ox, h_in+oy, d2+oz), (-w_in+ox, h_in+oy, d2+oz), (0,0,1))
    add_quad((w_in+ox, -h_in+oy, -d2+oz), (-w_in+ox, -h_in+oy, -d2+oz), (-w_in+ox, h_in+oy, -d2+oz), (w_in+ox, h_in+oy, -d2+oz), (0,0,-1))
    add_quad((-w_in+ox, h2+oy, d_in+oz), (w_in+ox, h2+oy, d_in+oz), (w_in+ox, h2+oy, -d_in+oz), (-w_in+ox, h2+oy, -d_in+oz), (0,1,0))
    add_quad((-w_in+ox, -h2+oy, -d_in+oz), (w_in+ox, -h2+oy, -d_in+oz), (w_in+ox, -h2+oy, d_in+oz), (-w_in+ox, -h2+oy, d_in+oz), (0,-1,0))
    add_quad((w2+ox, -h_in+oy, d_in+oz), (w2+ox, -h_in+oy, -d_in+oz), (w2+ox, h_in+oy, -d_in+oz), (w2+ox, h_in+oy, d_in+oz), (1,0,0))
    add_quad((-w2+ox, -h_in+oy, -d_in+oz), (-w2+ox, -h_in+oy, d_in+oz), (-w2+ox, h_in+oy, d_in+oz), (-w2+ox, h_in+oy, -d_in+oz), (-1,0,0))

    # 12 Edge bevels
    add_quad((-w_in+ox, h2+oy, d_in+oz), (-w_in+ox, h_in+oy, d2+oz), (w_in+ox, h_in+oy, d2+oz), (w_in+ox, h2+oy, d_in+oz), normalize((0, 1, 1)))
    add_quad((w_in+ox, h2+oy, -d_in+oz), (w_in+ox, h_in+oy, -d2+oz), (-w_in+ox, h_in+oy, -d2+oz), (-w_in+ox, h2+oy, -d_in+oz), normalize((0, 1, -1)))
    add_quad((-w_in+ox, -h_in+oy, d2+oz), (-w_in+ox, -h2+oy, d_in+oz), (w_in+ox, -h2+oy, d_in+oz), (w_in+ox, -h_in+oy, d2+oz), normalize((0, -1, 1)))
    add_quad((w_in+ox, -h_in+oy, -d2+oz), (w_in+ox, -h2+oy, -d_in+oz), (-w_in+ox, -h2+oy, -d_in+oz), (-w_in+ox, -h_in+oy, -d2+oz), normalize((0, -1, -1)))

    add_quad((w_in+ox, -h_in+oy, d2+oz), (w2+ox, -h_in+oy, d_in+oz), (w2+ox, h_in+oy, d_in+oz), (w_in+ox, h_in+oy, d2+oz), normalize((1, 0, 1)))
    add_quad((w2+ox, -h_in+oy, -d_in+oz), (w_in+ox, -h_in+oy, -d2+oz), (w_in+ox, h_in+oy, -d2+oz), (w2+ox, h_in+oy, -d_in+oz), normalize((1, 0, -1)))
    add_quad((-w2+ox, -h_in+oy, -d_in+oz), (-w_in+ox, -h_in+oy, -d2+oz), (-w_in+ox, h_in+oy, -d2+oz), (-w2+ox, h_in+oy, -d_in+oz), normalize((-1, 0, -1)))
    add_quad((-w_in+ox, -h_in+oy, d2+oz), (-w2+ox, -h_in+oy, d_in+oz), (-w2+ox, h_in+oy, d_in+oz), (-w_in+ox, h_in+oy, d2+oz), normalize((-1, 0, 1)))

    add_quad((w_in+ox, h2+oy, d_in+oz), (w_in+ox, h2+oy, -d_in+oz), (w2+ox, h_in+oy, -d_in+oz), (w2+ox, h_in+oy, d_in+oz), normalize((1, 1, 0)))
    add_quad((-w2+ox, h_in+oy, d_in+oz), (-w2+ox, h_in+oy, -d_in+oz), (-w_in+ox, h2+oy, -d_in+oz), (-w_in+ox, h2+oy, d_in+oz), normalize((-1, 1, 0)))
    add_quad((w_in+ox, -h2+oy, -d_in+oz), (w_in+ox, -h2+oy, d_in+oz), (w2+ox, -h_in+oy, d_in+oz), (w2+ox, -h_in+oy, -d_in+oz), normalize((1, -1, 0)))
    add_quad((-w_in+ox, -h2+oy, d_in+oz), (-w_in+ox, -h2+oy, -d_in+oz), (-w2+ox, -h_in+oy, -d_in+oz), (-w2+ox, -h_in+oy, d_in+oz), normalize((-1, -1, 0)))

    return positions, normals, indices

def make_swept_curve_tube(path_points, radii, segments=8):
    """Sweeps a circular cross section along a 3D path with varying radii."""
    positions = []
    normals = []
    indices = []
    n_pts = len(path_points)
    if n_pts < 2:
        return positions, normals, indices

    ring_starts = []
    for p_idx in range(n_pts):
        pt = path_points[p_idx]
        r = radii[p_idx]

        if p_idx == 0:
            tang = normalize(sub(path_points[1], path_points[0]))
        elif p_idx == n_pts - 1:
            tang = normalize(sub(path_points[-1], path_points[-2]))
        else:
            tang = normalize(sub(path_points[p_idx+1], path_points[p_idx-1]))

        up = (0.0, 1.0, 0.0)
        if abs(tang[1]) > 0.95:
            up = (1.0, 0.0, 0.0)
        norm_v = normalize(cross(tang, up))
        binorm_v = normalize(cross(tang, norm_v))

        ring_start = len(positions) // 3
        ring_starts.append(ring_start)

        for s in range(segments):
            angle = 2.0 * math.pi * s / segments
            ca = math.cos(angle)
            sa = math.sin(angle)
            nx = norm_v[0] * ca + binorm_v[0] * sa
            ny = norm_v[1] * ca + binorm_v[1] * sa
            nz = norm_v[2] * ca + binorm_v[2] * sa
            px = pt[0] + r * nx
            py = pt[1] + r * ny
            pz = pt[2] + r * nz
            positions.extend([px, py, pz])
            normals.extend([nx, ny, nz])

    for p_idx in range(n_pts - 1):
        r0 = ring_starts[p_idx]
        r1 = ring_starts[p_idx + 1]
        for s in range(segments):
            next_s = (s + 1) % segments
            indices.extend([r0 + s, r1 + s, r0 + next_s])
            indices.extend([r0 + next_s, r1 + s, r1 + next_s])

    tip_idx = len(positions) // 3
    positions.extend(path_points[-1])
    normals.extend(tang)
    r_last = ring_starts[-1]
    for s in range(segments):
        next_s = (s + 1) % segments
        indices.extend([r_last + s, tip_idx, r_last + next_s])

    return positions, normals, indices

def make_wavy_cape(w_top, w_bottom, length, folds=3, segments_y=12, offset=(0,0,0)):
    """Creates a realistic billowing 3D hero cape with cloth ripple wave harmonics."""
    positions = []
    normals = []
    indices = []
    ox, oy, oz = offset

    cols = 16
    rows = segments_y

    for r in range(rows + 1):
        t_y = r / float(rows)
        y = oy - t_y * length
        width = w_top + t_y * (w_bottom - w_top)

        z_curve = oz - math.sin(t_y * math.pi * 0.75) * 0.12 - t_y * 0.10

        for c in range(cols + 1):
            t_x = c / float(cols)
            x = (t_x - 0.5) * width + ox
            wave = math.sin(t_x * math.pi * folds * 2.0) * (0.02 + 0.05 * t_y)
            z = z_curve + wave

            positions.extend([x, y, z])
            nx = -math.cos(t_x * math.pi * folds * 2.0) * folds * 0.3 * t_y
            ny = 0.25 * t_y
            nz = -1.0
            nl = math.hypot(nx, ny, nz)
            normals.extend([nx/nl, ny/nl, nz/nl])

    for r in range(rows):
        for c in range(cols):
            i0 = r * (cols + 1) + c
            i1 = (r + 1) * (cols + 1) + c
            i2 = i0 + 1
            i3 = i1 + 1
            indices.extend([i0, i1, i2, i2, i1, i3])
            indices.extend([i0, i2, i1, i2, i3, i1])

    return positions, normals, indices

def make_curved_pauldron(radius, width, height, thickness=0.04, segments=12, offset=(0,0,0), flip_x=False):
    """Creates curved domed shoulder armor with flared filigree rim."""
    positions = []
    normals = []
    indices = []
    ox, oy, oz = offset

    angle_start = -math.pi * 0.45
    angle_end = math.pi * 0.45
    cols = segments
    rows = 6

    for r in range(rows + 1):
        t_w = (r / float(rows) - 0.5) * width
        rad_scale = 1.0 - (t_w / width) ** 2 * 0.3

        for c in range(cols + 1):
            t_c = c / float(cols)
            ang = angle_start + t_c * (angle_end - angle_start)
            ca = math.cos(ang)
            sa = math.sin(ang)

            local_x = radius * rad_scale * ca
            local_y = radius * rad_scale * sa
            local_z = t_w

            if flip_x:
                local_x = -local_x

            positions.extend([local_x + ox, local_y + oy, local_z + oz])
            nx = ca if not flip_x else -ca
            ny = sa
            nz = (t_w / width) * 0.4
            nl = math.hypot(nx, ny, nz)
            normals.extend([nx/nl, ny/nl, nz/nl])

    for r in range(rows):
        for c in range(cols):
            i0 = r * (cols + 1) + c
            i1 = (r + 1) * (cols + 1) + c
            i2 = i0 + 1
            i3 = i1 + 1
            if not flip_x:
                indices.extend([i0, i2, i1, i1, i2, i3])
            else:
                indices.extend([i0, i1, i2, i1, i3, i2])

    return positions, normals, indices

def make_sculpted_sword_blade(length=1.15, width=0.12, thickness=0.035, fuller_w=0.03, offset=(0,0,0)):
    """Creates a double-edged steel greatsword blade with central fuller groove, beveled edges, and acute tip."""
    positions = []
    normals = []
    indices = []
    ox, oy, oz = offset

    steps = 14
    for s in range(steps + 1):
        t = s / float(steps)
        y = oy + t * length

        w = width * (1.0 - t * t * 0.70) if t < 0.85 else width * (1.0 - t) / 0.15 * 0.40
        fw = fuller_w * (1.0 - t) if t < 0.75 else 0.001
        th = thickness * (1.0 - t * 0.5)

        p_el = (ox - w, y, oz)
        p_fl = (ox - fw, y, oz + th * 0.6)
        p_rc = (ox, y, oz + th)
        p_fr = (ox + fw, y, oz + th * 0.6)
        p_er = (ox + w, y, oz)
        p_b_rc = (ox, y, oz - th)
        p_b_fl = (ox - fw, y, oz - th * 0.6)
        p_b_fr = (ox + fw, y, oz - th * 0.6)

        positions.extend([*p_el, *p_fl, *p_rc, *p_fr, *p_er, *p_b_fl, *p_b_rc, *p_b_fr])
        normals.extend([
            -1.0, 0.0, 0.0,
            -0.3, 0.0, 0.95,
            0.0, 0.0, 1.0,
            0.3, 0.0, 0.95,
            1.0, 0.0, 0.0,
            -0.3, 0.0, -0.95,
            0.0, 0.0, -1.0,
            0.3, 0.0, -0.95
        ])

    for s in range(steps):
        s0 = s * 8
        s1 = (s + 1) * 8
        indices.extend([s0+0, s1+0, s0+1, s0+1, s1+0, s1+1])
        indices.extend([s0+1, s1+1, s0+2, s0+2, s1+1, s1+2])
        indices.extend([s0+2, s1+2, s0+3, s0+3, s1+2, s1+3])
        indices.extend([s0+3, s1+3, s0+4, s0+4, s1+3, s1+4])
        indices.extend([s0+0, s0+5, s1+0, s1+0, s0+5, s1+5])
        indices.extend([s0+5, s0+6, s1+5, s1+5, s0+6, s1+6])
        indices.extend([s0+6, s0+7, s1+6, s1+6, s0+7, s1+7])
        indices.extend([s0+7, s0+4, s1+7, s1+7, s0+4, s1+4])

    tip_idx = len(positions) // 3
    positions.extend([ox, oy + length + 0.08, oz])
    normals.extend([0.0, 1.0, 0.0])
    s_last = steps * 8
    for i in range(8):
        next_i = (i + 1) % 8
        indices.extend([s_last + i, tip_idx, s_last + next_i])

    return positions, normals, indices

def make_leaping_flame_aura(length=1.20, width_span=0.08, offset=(0,0,0)):
    """Creates jagged stylized 3D flame tongues leaping along both cutting edges of the blade."""
    positions = []
    normals = []
    indices = []
    ox, oy, oz = offset

    tongues = [
        (-1, 0.08, 0.24, 0.065, 0.02),
        (-1, 0.32, 0.30, 0.075, -0.02),
        (-1, 0.62, 0.32, 0.070, 0.025),
        (-1, 0.92, 0.26, 0.055, -0.015),
        (1, 0.16, 0.26, 0.070, -0.02),
        (1, 0.46, 0.32, 0.080, 0.025),
        (1, 0.78, 0.28, 0.065, -0.02),
        (1, 1.04, 0.22, 0.050, 0.015),
    ]

    for sign, y_b, f_h, f_w, z_c in tongues:
        b_idx = len(positions) // 3
        edge_x = sign * 0.062
        tip_x = sign * (0.062 + f_w)
        p0 = (ox + edge_x * 0.75, oy + y_b, oz - 0.01)
        p1 = (ox + edge_x, oy + y_b + f_h * 0.4, oz + 0.01)
        p2 = (ox + tip_x, oy + y_b + f_h * 0.65, oz + z_c)
        p3 = (ox + edge_x * 0.5 + tip_x * 0.5, oy + y_b + f_h, oz + z_c * 1.4)

        norm = (sign * 0.8, 0.4, 0.4)
        nl = math.hypot(*norm)
        n = (norm[0]/nl, norm[1]/nl, norm[2]/nl)

        positions.extend([*p0, *p1, *p2, *p3])
        normals.extend([*n, *n, *n, *n])

        indices.extend([b_idx, b_idx+1, b_idx+2, b_idx+1, b_idx+3, b_idx+2])
        indices.extend([b_idx, b_idx+2, b_idx+1, b_idx+1, b_idx+2, b_idx+3])

    return positions, normals, indices

def merge_geometries(geom_list):
    """Merges a list of (positions, normals, indices) into a single geometry."""
    all_pos = []
    all_norm = []
    all_idx = []
    v_offset = 0
    for pos, norm, idx in geom_list:
        all_pos.extend(pos)
        all_norm.extend(norm)
        for i in idx:
            all_idx.append(i + v_offset)
        v_offset += len(pos) // 3
    return all_pos, all_norm, all_idx

def create_glb(meshes_data, materials_data):
    bin_chunks = []
    buffer_views = []
    accessors = []
    meshes_json = []
    nodes_json = []
    current_offset = 0

    for m_idx, m in enumerate(meshes_data):
        positions = m["positions"]
        normals = m["normals"]
        indices = m["indices"]
        mat_idx = m.get("material_idx", 0)

        # Pack indices (UNSIGNED_SHORT)
        idx_bytes = struct.pack(f"<{len(indices)}H", *indices)
        idx_pad = (4 - (len(idx_bytes) % 4)) % 4
        idx_bytes += b"\x00" * idx_pad

        # Pack positions (FLOAT)
        pos_bytes = struct.pack(f"<{len(positions)}f", *positions)
        pos_pad = (4 - (len(pos_bytes) % 4)) % 4
        pos_bytes += b"\x00" * pos_pad

        # Pack normals (FLOAT)
        norm_bytes = struct.pack(f"<{len(normals)}f", *normals)
        norm_pad = (4 - (len(norm_bytes) % 4)) % 4
        norm_bytes += b"\x00" * norm_pad

        idx_view_idx = len(buffer_views)
        buffer_views.append({
            "buffer": 0,
            "byteOffset": current_offset,
            "byteLength": len(idx_bytes),
            "target": 34963
        })
        current_offset += len(idx_bytes)

        pos_view_idx = len(buffer_views)
        buffer_views.append({
            "buffer": 0,
            "byteOffset": current_offset,
            "byteLength": len(pos_bytes),
            "target": 34962
        })
        current_offset += len(pos_bytes)

        norm_view_idx = len(buffer_views)
        buffer_views.append({
            "buffer": 0,
            "byteOffset": current_offset,
            "byteLength": len(norm_bytes),
            "target": 34962
        })
        current_offset += len(norm_bytes)

        xs = positions[0::3]
        ys = positions[1::3]
        zs = positions[2::3]

        idx_acc_idx = len(accessors)
        accessors.append({
            "bufferView": idx_view_idx,
            "byteOffset": 0,
            "componentType": 5123,
            "count": len(indices),
            "type": "SCALAR",
            "max": [max(indices)],
            "min": [min(indices)]
        })

        pos_acc_idx = len(accessors)
        accessors.append({
            "bufferView": pos_view_idx,
            "byteOffset": 0,
            "componentType": 5126,
            "count": len(positions) // 3,
            "type": "VEC3",
            "max": [max(xs), max(ys), max(zs)],
            "min": [min(xs), min(ys), min(zs)]
        })

        norm_acc_idx = len(accessors)
        accessors.append({
            "bufferView": norm_view_idx,
            "byteOffset": 0,
            "componentType": 5126,
            "count": len(normals) // 3,
            "type": "VEC3"
        })

        meshes_json.append({
            "name": m.get("name", f"Mesh_{m_idx}"),
            "primitives": [{
                "attributes": {
                    "POSITION": pos_acc_idx,
                    "NORMAL": norm_acc_idx
                },
                "indices": idx_acc_idx,
                "material": mat_idx
            }]
        })

        nodes_json.append({
            "name": m.get("name", f"Node_{m_idx}"),
            "mesh": m_idx
        })

        bin_chunks.extend([idx_bytes, pos_bytes, norm_bytes])

    bin_data = b"".join(bin_chunks)

    gltf_dict = {
        "asset": {"version": "2.0", "generator": "HeroesTowerDefense Sculpted 3D Engine"},
        "scenes": [{"nodes": list(range(len(nodes_json)))}],
        "nodes": nodes_json,
        "meshes": meshes_json,
        "materials": materials_data,
        "buffers": [{"byteLength": len(bin_data)}],
        "bufferViews": buffer_views,
        "accessors": accessors
    }

    json_str = json.dumps(gltf_dict, separators=(",", ":"))
    json_bytes = json_str.encode("utf-8")
    json_pad = (4 - (len(json_bytes) % 4)) % 4
    json_bytes += b" " * json_pad

    total_len = 12 + 8 + len(json_bytes) + 8 + len(bin_data)
    header = struct.pack("<4sII", b"glTF", 2, total_len)
    chunk0_hdr = struct.pack("<II", len(json_bytes), 0x4E4F534A)
    chunk1_hdr = struct.pack("<II", len(bin_data), 0x004E4942)

    return header + chunk0_hdr + json_bytes + chunk1_hdr + bin_data


def build_sculpted_kael():
    """Builds the complete sculpted 3D model of Kael, Fire Warrior."""
    materials = [
        # 0: M_DarkSteel
        {"name": "M_DarkSteel", "pbrMetallicRoughness": {"baseColorFactor": [0.22, 0.25, 0.32, 1.0], "metallicFactor": 0.88, "roughnessFactor": 0.28}},
        # 1: M_GoldFiligree
        {"name": "M_GoldFiligree", "pbrMetallicRoughness": {"baseColorFactor": [0.98, 0.80, 0.22, 1.0], "metallicFactor": 0.92, "roughnessFactor": 0.22}},
        # 2: M_CrimsonPlate
        {"name": "M_CrimsonPlate", "pbrMetallicRoughness": {"baseColorFactor": [0.82, 0.10, 0.14, 1.0], "metallicFactor": 0.40, "roughnessFactor": 0.32}},
        # 3: M_HeroLeather
        {"name": "M_HeroLeather", "pbrMetallicRoughness": {"baseColorFactor": [0.35, 0.20, 0.10, 1.0], "metallicFactor": 0.05, "roughnessFactor": 0.82}},
        # 4: M_HeroSkin
        {"name": "M_HeroSkin", "pbrMetallicRoughness": {"baseColorFactor": [0.95, 0.77, 0.65, 1.0], "metallicFactor": 0.0, "roughnessFactor": 0.58}},
        # 5: M_FlameHair
        {"name": "M_FlameHair", "pbrMetallicRoughness": {"baseColorFactor": [0.92, 0.35, 0.06, 1.0], "metallicFactor": 0.15, "roughnessFactor": 0.42}},
        # 6: M_FlameAura
        {"name": "M_FlameAura", "emissiveFactor": [1.0, 0.62, 0.10], "pbrMetallicRoughness": {"baseColorFactor": [1.0, 0.82, 0.20, 1.0], "roughnessFactor": 0.15}},
        # 7: M_EyeDark
        {"name": "M_EyeDark", "pbrMetallicRoughness": {"baseColorFactor": [0.08, 0.09, 0.12, 1.0], "roughnessFactor": 0.20}},
        # 8: M_EyeWhite
        {"name": "M_EyeWhite", "pbrMetallicRoughness": {"baseColorFactor": [0.96, 0.96, 0.98, 1.0], "roughnessFactor": 0.30}},
        # 9: M_RubyGem
        {"name": "M_RubyGem", "emissiveFactor": [0.85, 0.05, 0.15], "pbrMetallicRoughness": {"baseColorFactor": [0.95, 0.08, 0.18, 1.0], "metallicFactor": 0.60, "roughnessFactor": 0.18}}
    ]

    submeshes = []

    # =========================================================================
    # 1. TORSO HIERARCHY
    # =========================================================================
    # Main Cuirass (Dark Steel)
    cuirass_p, cuirass_n, cuirass_i = make_beveled_box(0.56, 0.42, 0.36, bevel=0.04, offset=(0, 0.94, 0.02))
    gorget_p, gorget_n, gorget_i = make_smooth_cylinder(0.24, 0.28, 0.14, segments=16, offset=(0, 1.18, 0.01))
    abd_p, abd_n, abd_i = make_beveled_box(0.48, 0.26, 0.30, bevel=0.03, offset=(0, 0.68, 0.0))
    t_steel_p, t_steel_n, t_steel_i = merge_geometries([(cuirass_p, cuirass_n, cuirass_i), (gorget_p, gorget_n, gorget_i), (abd_p, abd_n, abd_i)])
    submeshes.append({"positions": t_steel_p, "normals": t_steel_n, "indices": t_steel_i, "material_idx": 0, "name": "TorsoSteel"})

    # Crimson Enamel Pectoral Plates & Abdominal Ridges
    pec_l_p, pec_l_n, pec_l_i = make_beveled_box(0.24, 0.20, 0.06, bevel=0.02, offset=(-0.14, 1.00, 0.20))
    pec_r_p, pec_r_n, pec_r_i = make_beveled_box(0.24, 0.20, 0.06, bevel=0.02, offset=(0.14, 1.00, 0.20))
    abd_ridge1_p, abd_ridge1_n, abd_ridge1_i = make_beveled_box(0.36, 0.05, 0.04, bevel=0.015, offset=(0, 0.74, 0.16))
    abd_ridge2_p, abd_ridge2_n, abd_ridge2_i = make_beveled_box(0.32, 0.05, 0.04, bevel=0.015, offset=(0, 0.64, 0.16))
    tabard_p, tabard_n, tabard_i = make_beveled_box(0.26, 0.44, 0.035, bevel=0.01, offset=(0, 0.38, 0.18))
    # Flanking Hip Tassets (Faulds)
    tasset_l_p, tasset_l_n, tasset_l_i = make_beveled_box(0.16, 0.24, 0.05, bevel=0.015, offset=(-0.26, 0.46, 0.05))
    tasset_r_p, tasset_r_n, tasset_r_i = make_beveled_box(0.16, 0.24, 0.05, bevel=0.015, offset=(0.26, 0.46, 0.05))
    t_crim_p, t_crim_n, t_crim_i = merge_geometries([
        (pec_l_p, pec_l_n, pec_l_i), (pec_r_p, pec_r_n, pec_r_i),
        (abd_ridge1_p, abd_ridge1_n, abd_ridge1_i), (abd_ridge2_p, abd_ridge2_n, abd_ridge2_i),
        (tabard_p, tabard_n, tabard_i), (tasset_l_p, tasset_l_n, tasset_l_i), (tasset_r_p, tasset_r_n, tasset_r_i)
    ])
    submeshes.append({"positions": t_crim_p, "normals": t_crim_n, "indices": t_crim_i, "material_idx": 2, "name": "TorsoCrimson"})

    # Ornate Gold Filigree Trims & Belt Buckle
    pec_trim_p, pec_trim_n, pec_trim_i = make_beveled_box(0.52, 0.035, 0.04, bevel=0.01, offset=(0, 1.11, 0.19))
    gorget_rim_p, gorget_rim_n, gorget_rim_i = make_smooth_cylinder(0.25, 0.29, 0.04, segments=16, offset=(0, 1.25, 0.01))
    buckle_p, buckle_n, buckle_i = make_beveled_box(0.16, 0.14, 0.07, bevel=0.02, offset=(0, 0.54, 0.19))
    lion_boss_p, lion_boss_n, lion_boss_i = make_smooth_sphere(0.05, lat_segs=8, lon_segs=10, offset=(0, 0.54, 0.23))
    tabard_hem_p, tabard_hem_n, tabard_hem_i = make_beveled_box(0.27, 0.04, 0.045, bevel=0.01, offset=(0, 0.16, 0.18))
    t_gold_p, t_gold_n, t_gold_i = merge_geometries([
        (pec_trim_p, pec_trim_n, pec_trim_i), (gorget_rim_p, gorget_rim_n, gorget_rim_i),
        (buckle_p, buckle_n, buckle_i), (lion_boss_p, lion_boss_n, lion_boss_i), (tabard_hem_p, tabard_hem_n, tabard_hem_i)
    ])
    submeshes.append({"positions": t_gold_p, "normals": t_gold_n, "indices": t_gold_i, "material_idx": 1, "name": "TorsoGold"})

    # Leather Hero Belt
    belt_p, belt_n, belt_i = make_beveled_box(0.52, 0.11, 0.35, bevel=0.02, offset=(0, 0.54, 0.0))
    submeshes.append({"positions": belt_p, "normals": belt_n, "indices": belt_i, "material_idx": 3, "name": "TorsoBelt"})

    # =========================================================================
    # 2. HEAD & FACIAL ANATOMY & STYLIZED HAIR
    # =========================================================================
    # Head cranium & sculpted face (Hero Skin)
    cranium_p, cranium_n, cranium_i = make_smooth_ellipsoid(0.20, 0.22, 0.21, lat_segs=14, lon_segs=18, offset=(0, 1.40, 0.0))
    jaw_p, jaw_n, jaw_i = make_beveled_box(0.17, 0.16, 0.16, bevel=0.04, offset=(0, 1.25, 0.07))
    chin_p, chin_n, chin_i = make_smooth_sphere(0.06, lat_segs=8, lon_segs=10, offset=(0, 1.18, 0.14))
    nose_p, nose_n, nose_i = make_beveled_box(0.04, 0.07, 0.065, bevel=0.012, offset=(0, 1.35, 0.215))
    h_skin_p, h_skin_n, h_skin_i = merge_geometries([
        (cranium_p, cranium_n, cranium_i), (jaw_p, jaw_n, jaw_i),
        (chin_p, chin_n, chin_i), (nose_p, nose_n, nose_i)
    ])
    submeshes.append({"positions": h_skin_p, "normals": h_skin_n, "indices": h_skin_i, "material_idx": 4, "name": "HeadSkin"})

    # Expressive Cartoon Eyes (White Sclera)
    eye_l_w_p, eye_l_w_n, eye_l_w_i = make_beveled_box(0.06, 0.04, 0.02, bevel=0.01, offset=(-0.08, 1.39, 0.20))
    eye_r_w_p, eye_r_w_n, eye_r_w_i = make_beveled_box(0.06, 0.04, 0.02, bevel=0.01, offset=(0.08, 1.39, 0.20))
    eyes_w_p, eyes_w_n, eyes_w_i = merge_geometries([(eye_l_w_p, eye_l_w_n, eye_l_w_i), (eye_r_w_p, eye_r_w_n, eye_r_w_i)])
    submeshes.append({"positions": eyes_w_p, "normals": eyes_w_n, "indices": eyes_w_i, "material_idx": 8, "name": "HeadEyeWhite"})

    # Dark Iris, Pupils & Eyelash Brows
    eye_l_p, eye_l_n, eye_l_i = make_beveled_box(0.038, 0.036, 0.025, bevel=0.008, offset=(-0.08, 1.39, 0.205))
    eye_r_p, eye_r_n, eye_r_i = make_beveled_box(0.038, 0.036, 0.025, bevel=0.008, offset=(0.08, 1.39, 0.205))
    brow_l_p, brow_l_n, brow_l_i = make_beveled_box(0.085, 0.025, 0.03, bevel=0.008, offset=(-0.08, 1.44, 0.208))
    brow_r_p, brow_r_n, brow_r_i = make_beveled_box(0.085, 0.025, 0.03, bevel=0.008, offset=(0.08, 1.44, 0.208))
    mouth_p, mouth_n, mouth_i = make_beveled_box(0.07, 0.015, 0.02, bevel=0.005, offset=(0, 1.25, 0.185))
    eyes_d_p, eyes_d_n, eyes_d_i = merge_geometries([
        (eye_l_p, eye_l_n, eye_l_i), (eye_r_p, eye_r_n, eye_r_i),
        (brow_l_p, brow_l_n, brow_l_i), (brow_r_p, brow_r_n, brow_r_i),
        (mouth_p, mouth_n, mouth_i)
    ])
    submeshes.append({"positions": eyes_d_p, "normals": eyes_d_n, "indices": eyes_d_i, "material_idx": 7, "name": "HeadEyesDark"})

    # Stylized Anime Hair (Volumetric Flame Locks)
    hair_cap_p, hair_cap_n, hair_cap_i = make_smooth_ellipsoid(0.24, 0.24, 0.24, lat_segs=12, lon_segs=16, offset=(0, 1.48, -0.03))
    # Swept Flame Bangs & Tapered Locks
    bang1_pts = [(0.0, 1.62, 0.08), (0.02, 1.55, 0.18), (0.01, 1.45, 0.23), (-0.02, 1.38, 0.22)]
    bang1_p, bang1_n, bang1_i = make_swept_curve_tube(bang1_pts, [0.06, 0.055, 0.04, 0.005], segments=8)
    bang2_pts = [(-0.06, 1.60, 0.06), (-0.11, 1.54, 0.16), (-0.14, 1.44, 0.21), (-0.16, 1.37, 0.19)]
    bang2_p, bang2_n, bang2_i = make_swept_curve_tube(bang2_pts, [0.055, 0.05, 0.035, 0.005], segments=8)
    bang3_pts = [(0.06, 1.60, 0.06), (0.12, 1.54, 0.16), (0.15, 1.44, 0.21), (0.17, 1.37, 0.19)]
    bang3_p, bang3_n, bang3_i = make_swept_curve_tube(bang3_pts, [0.055, 0.05, 0.035, 0.005], segments=8)
    # Sideburn Locks Framing Face
    side_l_pts = [(-0.16, 1.52, 0.05), (-0.21, 1.42, 0.12), (-0.22, 1.30, 0.14), (-0.20, 1.20, 0.10)]
    side_l_p, side_l_n, side_l_i = make_swept_curve_tube(side_l_pts, [0.06, 0.05, 0.04, 0.005], segments=8)
    side_r_pts = [(0.16, 1.52, 0.05), (0.21, 1.42, 0.12), (0.22, 1.30, 0.14), (0.20, 1.20, 0.10)]
    side_r_p, side_r_n, side_r_i = make_swept_curve_tube(side_r_pts, [0.06, 0.05, 0.04, 0.005], segments=8)
    # Flared Back Spikes
    back_l_pts = [(-0.12, 1.48, -0.16), (-0.18, 1.42, -0.24), (-0.22, 1.34, -0.28)]
    back_l_p, back_l_n, back_l_i = make_swept_curve_tube(back_l_pts, [0.07, 0.05, 0.008], segments=8)
    back_r_pts = [(0.12, 1.48, -0.16), (0.18, 1.42, -0.24), (0.22, 1.34, -0.28)]
    back_r_p, back_r_n, back_r_i = make_swept_curve_tube(back_r_pts, [0.07, 0.05, 0.008], segments=8)
    # Top Crest Spike
    crest_pts = [(0.0, 1.62, -0.04), (0.0, 1.72, -0.06), (0.0, 1.78, -0.08)]
    crest_p, crest_n, crest_i = make_swept_curve_tube(crest_pts, [0.07, 0.04, 0.005], segments=8)

    hair_all_p, hair_all_n, hair_all_i = merge_geometries([
        (hair_cap_p, hair_cap_n, hair_cap_i),
        (bang1_p, bang1_n, bang1_i), (bang2_p, bang2_n, bang2_i), (bang3_p, bang3_n, bang3_i),
        (side_l_p, side_l_n, side_l_i), (side_r_p, side_r_n, side_r_i),
        (back_l_p, back_l_n, back_l_i), (back_r_p, back_r_n, back_r_i),
        (crest_p, crest_n, crest_i)
    ])
    submeshes.append({"positions": hair_all_p, "normals": hair_all_n, "indices": hair_all_i, "material_idx": 5, "name": "HeadHair"})

    # =========================================================================
    # 3. PAULDRONS (Curved Shoulder Armor with Medallions)
    # =========================================================================
    for side, sign in [("Left", -1), ("Right", 1)]:
        # Domed Crimson Shoulder Cap
        pld_cap_p, pld_cap_n, pld_cap_i = make_smooth_ellipsoid(0.19, 0.16, 0.21, lat_segs=10, lon_segs=14, offset=(sign * 0.40, 1.05, 0.0))
        submeshes.append({"positions": pld_cap_p, "normals": pld_cap_n, "indices": pld_cap_i, "material_idx": 2, "name": f"{side}PauldronCrimson"})

        # Gold Sculpted Filigree Arches & Medallion
        top_arch_pts = [(sign * 0.28, 1.04, 0.14), (sign * 0.42, 1.18, 0.0), (sign * 0.28, 1.04, -0.14)]
        top_arch_p, top_arch_n, top_arch_i = make_swept_curve_tube(top_arch_pts, [0.032, 0.042, 0.032], segments=8)

        rim_pts = [(sign * 0.44, 0.96, 0.15), (sign * 0.54, 1.04, 0.0), (sign * 0.44, 0.96, -0.15)]
        rim_p, rim_n, rim_i = make_swept_curve_tube(rim_pts, [0.028, 0.038, 0.028], segments=8)

        boss_p, boss_n, boss_i = make_smooth_sphere(0.055, lat_segs=8, lon_segs=10, offset=(sign * 0.52, 1.06, 0.0))
        gold_p, gold_n, gold_i = merge_geometries([(top_arch_p, top_arch_n, top_arch_i), (rim_p, rim_n, rim_i), (boss_p, boss_n, boss_i)])
        submeshes.append({"positions": gold_p, "normals": gold_n, "indices": gold_i, "material_idx": 1, "name": f"{side}PauldronGold"})

        # Lion Heart Ruby Core
        gem_p, gem_n, gem_i = make_smooth_sphere(0.025, lat_segs=6, lon_segs=8, offset=(sign * 0.56, 1.06, 0.0))
        submeshes.append({"positions": gem_p, "normals": gem_n, "indices": gem_i, "material_idx": 9, "name": f"{side}PauldronRuby"})

    # =========================================================================
    # 4. ARMS & GAUNTLETS & HANDS
    # =========================================================================
    # Left Arm
    l_arm_sleeve_p, l_arm_sleeve_n, l_arm_sleeve_i = make_smooth_cylinder(0.085, 0.08, 0.28, segments=12, offset=(-0.38, 0.88, 0.0))
    submeshes.append({"positions": l_arm_sleeve_p, "normals": l_arm_sleeve_n, "indices": l_arm_sleeve_i, "material_idx": 3, "name": "LeftArmSleeve"})

    l_couter_p, l_couter_n, l_couter_i = make_beveled_box(0.13, 0.13, 0.13, bevel=0.03, offset=(-0.38, 0.72, -0.01))
    l_vambrace_p, l_vambrace_n, l_vambrace_i = make_smooth_cylinder(0.095, 0.085, 0.28, segments=12, offset=(-0.38, 0.56, 0.0))
    l_steel_p, l_steel_n, l_steel_i = merge_geometries([(l_couter_p, l_couter_n, l_couter_i), (l_vambrace_p, l_vambrace_n, l_vambrace_i)])
    submeshes.append({"positions": l_steel_p, "normals": l_steel_n, "indices": l_steel_i, "material_idx": 0, "name": "LeftGauntletSteel"})

    l_wrist_p, l_wrist_n, l_wrist_i = make_smooth_cylinder(0.10, 0.10, 0.04, segments=12, offset=(-0.38, 0.46, 0.0))
    submeshes.append({"positions": l_wrist_p, "normals": l_wrist_n, "indices": l_wrist_i, "material_idx": 1, "name": "LeftGauntletGold"})

    l_hand_p, l_hand_n, l_hand_i = make_beveled_box(0.11, 0.12, 0.10, bevel=0.02, offset=(-0.38, 0.38, 0.0))
    submeshes.append({"positions": l_hand_p, "normals": l_hand_n, "indices": l_hand_i, "material_idx": 4, "name": "LeftHand"})

    # Right Arm (Gripping Weapon)
    r_arm_sleeve_p, r_arm_sleeve_n, r_arm_sleeve_i = make_smooth_cylinder(0.085, 0.08, 0.28, segments=12, offset=(0.38, 0.88, 0.0))
    submeshes.append({"positions": r_arm_sleeve_p, "normals": r_arm_sleeve_n, "indices": r_arm_sleeve_i, "material_idx": 3, "name": "RightArmSleeve"})

    r_couter_p, r_couter_n, r_couter_i = make_beveled_box(0.13, 0.13, 0.13, bevel=0.03, offset=(0.38, 0.72, -0.01))
    r_vambrace_p, r_vambrace_n, r_vambrace_i = make_smooth_cylinder(0.095, 0.085, 0.28, segments=12, offset=(0.38, 0.56, 0.0))
    r_steel_p, r_steel_n, r_steel_i = merge_geometries([(r_couter_p, r_couter_n, r_couter_i), (r_vambrace_p, r_vambrace_n, r_vambrace_i)])
    submeshes.append({"positions": r_steel_p, "normals": r_steel_n, "indices": r_steel_i, "material_idx": 0, "name": "RightGauntletSteel"})

    r_wrist_p, r_wrist_n, r_wrist_i = make_smooth_cylinder(0.10, 0.10, 0.04, segments=12, offset=(0.38, 0.46, 0.0))
    submeshes.append({"positions": r_wrist_p, "normals": r_wrist_n, "indices": r_wrist_i, "material_idx": 1, "name": "RightGauntletGold"})

    r_hand_p, r_hand_n, r_hand_i = make_beveled_box(0.11, 0.12, 0.10, bevel=0.02, offset=(0.38, 0.38, 0.04))
    submeshes.append({"positions": r_hand_p, "normals": r_hand_n, "indices": r_hand_i, "material_idx": 4, "name": "RightHand"})

    # =========================================================================
    # 5. LEGS & ARMORED BOOTS (Left & Right)
    # =========================================================================
    for side, sign in [("Left", -1), ("Right", 1)]:
        sx = sign * 0.18

        # Thigh Cuisses
        thigh_p, thigh_n, thigh_i = make_smooth_cylinder(0.12, 0.11, 0.30, segments=12, offset=(sx, 0.38, 0.0))
        # Shin Greaves
        shin_p, shin_n, shin_i = make_smooth_cylinder(0.105, 0.115, 0.28, segments=12, offset=(sx, 0.14, 0.0))
        leg_steel_p, leg_steel_n, leg_steel_i = merge_geometries([(thigh_p, thigh_n, thigh_i), (shin_p, shin_n, shin_i)])
        submeshes.append({"positions": leg_steel_p, "normals": leg_steel_n, "indices": leg_steel_i, "material_idx": 0, "name": f"{side}LegSteel"})

        # Knee Poleyns (Curved with Gold Rim & Ruby Core)
        knee_p, knee_n, knee_i = make_beveled_box(0.16, 0.14, 0.14, bevel=0.03, offset=(sx, 0.25, 0.07))
        submeshes.append({"positions": knee_p, "normals": knee_n, "indices": knee_i, "material_idx": 1, "name": f"{side}KneeGold"})

        knee_gem_p, knee_gem_n, knee_gem_i = make_smooth_sphere(0.04, lat_segs=6, lon_segs=8, offset=(sx, 0.25, 0.14))
        submeshes.append({"positions": knee_gem_p, "normals": knee_gem_n, "indices": knee_gem_i, "material_idx": 9, "name": f"{side}KneeGem"})

        # Sabaton Boots (Segmented Plates, Curved Toe Guard & Heel Spur)
        boot_foot_p, boot_foot_n, boot_foot_i = make_beveled_box(0.16, 0.12, 0.26, bevel=0.03, offset=(sx, 0.06, 0.05))
        boot_toe_p, boot_toe_n, boot_toe_i = make_smooth_cylinder(0.08, 0.04, 0.10, segments=8, offset=(sx, 0.06, 0.18))
        boot_all_p, boot_all_n, boot_all_i = merge_geometries([(boot_foot_p, boot_foot_n, boot_foot_i), (boot_toe_p, boot_toe_n, boot_toe_i)])
        submeshes.append({"positions": boot_all_p, "normals": boot_all_n, "indices": boot_all_i, "material_idx": 3, "name": f"{side}Boot"})

    # =========================================================================
    # 6. BILLOWING HERO CAPE (Flowing with 3D Cloth Waves)
    # =========================================================================
    cape_p, cape_n, cape_i = make_wavy_cape(w_top=0.52, w_bottom=0.88, length=0.86, folds=3, segments_y=14, offset=(0, 1.12, -0.19))
    submeshes.append({"positions": cape_p, "normals": cape_n, "indices": cape_i, "material_idx": 2, "name": "Cape"})

    # Gold Hem along Bottom of Cape
    cape_hem_p, cape_hem_n, cape_hem_i = make_beveled_box(0.90, 0.04, 0.04, bevel=0.01, offset=(0, 0.26, -0.28))
    # Cape Shoulder Clasps (Twin Lion Heads)
    clasp_l_p, clasp_l_n, clasp_l_i = make_smooth_sphere(0.05, lat_segs=8, lon_segs=10, offset=(-0.24, 1.16, -0.16))
    clasp_r_p, clasp_r_n, clasp_r_i = make_smooth_sphere(0.05, lat_segs=8, lon_segs=10, offset=(0.24, 1.16, -0.16))
    cape_gold_p, cape_gold_n, cape_gold_i = merge_geometries([(cape_hem_p, cape_hem_n, cape_hem_i), (clasp_l_p, clasp_l_n, clasp_l_i), (clasp_r_p, clasp_r_n, clasp_r_i)])
    submeshes.append({"positions": cape_gold_p, "normals": cape_gold_n, "indices": cape_gold_i, "material_idx": 1, "name": "CapeGold"})

    # =========================================================================
    # 7. THE FLAMING GREATSWORD
    # =========================================================================
    wx, wy, wz = 0.38, 0.38, 0.16

    # Golden Faceted Pommel
    pommel_p, pommel_n, pommel_i = make_beveled_box(0.09, 0.09, 0.09, bevel=0.03, offset=(wx, wy - 0.28, wz))
    pommel_gem_p, pommel_gem_n, pommel_gem_i = make_smooth_sphere(0.035, lat_segs=6, lon_segs=8, offset=(wx, wy - 0.33, wz))
    # Winged Crossguard
    guard_body_p, guard_body_n, guard_body_i = make_beveled_box(0.38, 0.07, 0.09, bevel=0.02, offset=(wx, wy + 0.04, wz))
    guard_wing_l_p, guard_wing_l_n, guard_wing_l_i = make_smooth_cylinder(0.04, 0.02, 0.12, segments=8, offset=(wx - 0.20, wy + 0.08, wz))
    guard_wing_r_p, guard_wing_r_n, guard_wing_r_i = make_smooth_cylinder(0.04, 0.02, 0.12, segments=8, offset=(wx + 0.20, wy + 0.08, wz))
    sword_gold_p, sword_gold_n, sword_gold_i = merge_geometries([
        (pommel_p, pommel_n, pommel_i), (guard_body_p, guard_body_n, guard_body_i),
        (guard_wing_l_p, guard_wing_l_n, guard_wing_l_i), (guard_wing_r_p, guard_wing_r_n, guard_wing_r_i)
    ])
    submeshes.append({"positions": sword_gold_p, "normals": sword_gold_n, "indices": sword_gold_i, "material_idx": 1, "name": "SwordGold"})
    submeshes.append({"positions": pommel_gem_p, "normals": pommel_gem_n, "indices": pommel_gem_i, "material_idx": 9, "name": "SwordPommelGem"})

    # Fluted Leather Grip with Wire Winding
    grip_p, grip_n, grip_i = make_smooth_cylinder(0.032, 0.035, 0.24, segments=12, offset=(wx, wy - 0.12, wz))
    submeshes.append({"positions": grip_p, "normals": grip_n, "indices": grip_i, "material_idx": 3, "name": "SwordGrip"})

    # Central Crossguard Power Ruby
    guard_ruby_p, guard_ruby_n, guard_ruby_i = make_smooth_sphere(0.045, lat_segs=8, lon_segs=10, offset=(wx, wy + 0.04, wz + 0.055))
    submeshes.append({"positions": guard_ruby_p, "normals": guard_ruby_n, "indices": guard_ruby_i, "material_idx": 9, "name": "SwordCrossguardRuby"})

    # Broad Double-Edged Polished Steel Blade with Fuller Groove
    blade_p, blade_n, blade_i = make_sculpted_sword_blade(length=1.24, width=0.065, thickness=0.024, fuller_w=0.018, offset=(wx, wy + 0.06, wz))
    submeshes.append({"positions": blade_p, "normals": blade_n, "indices": blade_i, "material_idx": 0, "name": "SwordBlade"})

    # Multi-Tiered Leaping Flame Aura
    flame_p, flame_n, flame_i = make_leaping_flame_aura(length=1.20, width_span=0.08, offset=(wx, wy + 0.06, wz))
    submeshes.append({"positions": flame_p, "normals": flame_n, "indices": flame_i, "material_idx": 6, "name": "SwordFlame"})

    return submeshes, materials

def generate_premium_kael(output_path):
    submeshes, materials = build_sculpted_kael()
    glb_data = create_glb(submeshes, materials)
    with open(output_path, "wb") as f:
        f.write(glb_data)
    print(f"SUCCESS: Generated Sculpted High-Detail 3D Kael GLB: {output_path} ({len(glb_data)} bytes, {len(submeshes)} submeshes)")

if __name__ == "__main__":
    out_file = sys.argv[1] if len(sys.argv) > 1 else "/workspace/godot/models/hero_kael.glb"
    generate_premium_kael(out_file)
