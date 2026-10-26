#!/usr/bin/env python3
"""
Comprehensive 3D Asset Generator for Heroes Tower Defense (Etapas 1/3 and 2/3).
Generates binary glTF 2.0 (.glb) stylized 3D models with vertex normals,
materials, and colors for:

Etapa 1/3 Scenery:
  1. terrain_chunk.glb
  2. bridge_stone.glb
  3. tree_stylized.glb
  4. rock_boulder.glb
  5. ruin_pillar.glb
  6. crystal_cluster.glb
  7. torch_brazier.glb
  8. fortress_castle.glb
  9. summon_portal.glb

Etapa 2/3 Characters, Enemies, Bosses & Detailed Towers:
  10. hero_kael.glb              - Fire Warrior with plate armor, greatsword & flame crest
  11. hero_lyra.glb              - Arcane Archer with composite longbow, quiver & tiara
  12. hero_brakk.glb             - Heavy Guardian with granite/iron plate & colossal warhammer
  13. hero_nyra.glb              - Shadow Assassin with cowl, dual serrated daggers & capelet
  14. hero_tiko.glb              - Master Engineer with steam boiler backpack, goggles & wrench
  15. hero_vesper.glb            - Astral Mage with cosmic robe, astral crescent staff & star orb
  16. enemy_goblin.glb           - Goblin Raider with pointed ears, ragged tunic & spiked club
  17. enemy_skeleton.glb         - Cursed Skeleton with ivory skull, ribcage, broadsword & buckler
  18. enemy_orc.glb              - Orc Warrior with tusked jaw, horned pauldron & double battleaxe
  19. enemy_dark_mage.glb        - Dark Mage with pointed cowl, purple robe & void skull staff
  20. boss_obsidian_golem.glb    - Colossal Volcanic Titan with basalt armor, magma core & fists
  21. boss_shadow_queen.glb      - Ethereal Shadow Sovereign with gothic gown, crown & twin rapiers
  22. boss_astral_dragon.glb     - Celestial Dragon with quadruped frame, wide wings & star tail
  23. tower_archer_detailed.glb  - 3-tier medieval watchtower with battlement, arbalest & flag
  24. tower_mage_detailed.glb    - Arcane observatory with runic dais, spiral spires & floating crystal
  25. tower_cannon_detailed.glb  - Fortified iron bastion with swivel siege bombard & recoil dampers
"""

import os
import json
import struct
import math

def create_glb(meshes_data, materials_data=None):
    """
    Creates a binary glTF 2.0 (.glb) bytes from mesh geometry data.
    meshes_data: list of dicts with:
        - positions: list of float (x, y, z triplets)
        - normals: list of float (nx, ny, nz triplets)
        - indices: list of int
        - material_idx: int (optional)
        - name: str (optional)
    """
    if materials_data is None:
        materials_data = [{"pbrMetallicRoughness": {"baseColorFactor": [0.8, 0.8, 0.8, 1.0], "roughnessFactor": 0.8, "metallicFactor": 0.1}}]

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

        # Buffer views
        idx_view_idx = len(buffer_views)
        buffer_views.append({
            "buffer": 0,
            "byteOffset": current_offset,
            "byteLength": len(idx_bytes),
            "target": 34963 # ELEMENT_ARRAY_BUFFER
        })
        current_offset += len(idx_bytes)

        pos_view_idx = len(buffer_views)
        buffer_views.append({
            "buffer": 0,
            "byteOffset": current_offset,
            "byteLength": len(pos_bytes),
            "target": 34962 # ARRAY_BUFFER
        })
        current_offset += len(pos_bytes)

        norm_view_idx = len(buffer_views)
        buffer_views.append({
            "buffer": 0,
            "byteOffset": current_offset,
            "byteLength": len(norm_bytes),
            "target": 34962 # ARRAY_BUFFER
        })
        current_offset += len(norm_bytes)

        # Min/max for positions
        xs = positions[0::3]
        ys = positions[1::3]
        zs = positions[2::3]
        min_pos = [min(xs), min(ys), min(zs)]
        max_pos = [max(xs), max(ys), max(zs)]

        # Accessors
        idx_acc_idx = len(accessors)
        accessors.append({
            "bufferView": idx_view_idx,
            "byteOffset": 0,
            "componentType": 5123, # UNSIGNED_SHORT
            "count": len(indices),
            "type": "SCALAR",
            "max": [max(indices)],
            "min": [min(indices)]
        })

        pos_acc_idx = len(accessors)
        accessors.append({
            "bufferView": pos_view_idx,
            "byteOffset": 0,
            "componentType": 5126, # FLOAT
            "count": len(positions) // 3,
            "type": "VEC3",
            "max": max_pos,
            "min": min_pos
        })

        norm_acc_idx = len(accessors)
        accessors.append({
            "bufferView": norm_view_idx,
            "byteOffset": 0,
            "componentType": 5126, # FLOAT
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
        "asset": {"version": "2.0", "generator": "HeroesTowerDefense 3D Asset Generator"},
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
    chunk0_hdr = struct.pack("<II", len(json_bytes), 0x4E4F534A) # JSON
    chunk1_hdr = struct.pack("<II", len(bin_data), 0x004E4942)  # BIN

    return header + chunk0_hdr + json_bytes + chunk1_hdr + bin_data


def make_box(width, height, depth, offset=(0, 0, 0)):
    """Generates indexed box geometry with flat normals."""
    w2, h2, d2 = width / 2.0, height / 2.0, depth / 2.0
    ox, oy, oz = offset

    faces = [
        # +Y (Top)
        ((0, 1, 0), [(-w2, h2, -d2), (w2, h2, -d2), (w2, h2, d2), (-w2, h2, d2)]),
        # -Y (Bottom)
        ((0, -1, 0), [(-w2, -h2, d2), (w2, -h2, d2), (w2, -h2, -d2), (-w2, -h2, -d2)]),
        # +Z (South)
        ((0, 0, 1), [(-w2, -h2, d2), (w2, -h2, d2), (w2, h2, d2), (-w2, h2, d2)]),
        # -Z (North)
        ((0, 0, -1), [(w2, -h2, -d2), (-w2, -h2, -d2), (-w2, h2, -d2), (w2, h2, -d2)]),
        # +X (East)
        ((1, 0, 0), [(w2, -h2, d2), (w2, -h2, -d2), (w2, h2, -d2), (w2, h2, d2)]),
        # -X (West)
        ((-1, 0, 0), [(-w2, -h2, -d2), (-w2, -h2, d2), (-w2, h2, d2), (-w2, h2, -d2)])
    ]

    positions = []
    normals = []
    indices = []

    v_offset = 0
    for norm, corners in faces:
        for c in corners:
            positions.extend([c[0] + ox, c[1] + oy, c[2] + oz])
            normals.extend(norm)
        indices.extend([v_offset, v_offset + 1, v_offset + 2, v_offset, v_offset + 2, v_offset + 3])
        v_offset += 4

    return positions, normals, indices


def make_cylinder(radius_top, radius_bottom, height, segments=8, offset=(0, 0, 0)):
    """Generates cylinder or cone geometry."""
    ox, oy, oz = offset
    h2 = height / 2.0

    positions = []
    normals = []
    indices = []

    # Side quads
    v_offset = 0
    for i in range(segments):
        a1 = 2 * math.pi * i / segments
        a2 = 2 * math.pi * (i + 1) / segments

        x1_b = radius_bottom * math.cos(a1)
        z1_b = radius_bottom * math.sin(a1)
        x2_b = radius_bottom * math.cos(a2)
        z2_b = radius_bottom * math.sin(a2)

        x1_t = radius_top * math.cos(a1)
        z1_t = radius_top * math.sin(a1)
        x2_t = radius_top * math.cos(a2)
        z2_t = radius_top * math.sin(a2)

        mid_a = (a1 + a2) / 2.0
        norm = (math.cos(mid_a), 0.2, math.sin(mid_a))
        nl = math.sqrt(norm[0]**2 + norm[1]**2 + norm[2]**2)
        norm = (norm[0]/nl, norm[1]/nl, norm[2]/nl)

        positions.extend([x1_b + ox, -h2 + oy, z1_b + oz])
        normals.extend(norm)
        positions.extend([x2_b + ox, -h2 + oy, z2_b + oz])
        normals.extend(norm)
        positions.extend([x2_t + ox, h2 + oy, z2_t + oz])
        normals.extend(norm)
        positions.extend([x1_t + ox, h2 + oy, z1_t + oz])
        normals.extend(norm)

        indices.extend([v_offset, v_offset + 1, v_offset + 2, v_offset, v_offset + 2, v_offset + 3])
        v_offset += 4

    # Top cap if radius > 0
    if radius_top > 0.001:
        top_center_idx = v_offset
        positions.extend([ox, h2 + oy, oz])
        normals.extend([0, 1, 0])
        v_offset += 1

        for i in range(segments):
            a = 2 * math.pi * i / segments
            positions.extend([radius_top * math.cos(a) + ox, h2 + oy, radius_top * math.sin(a) + oz])
            normals.extend([0, 1, 0])

        for i in range(segments):
            i1 = v_offset + i
            i2 = v_offset + ((i + 1) % segments)
            indices.extend([top_center_idx, i1, i2])
        v_offset += segments

    # Bottom cap if radius_bottom > 0
    if radius_bottom > 0.001:
        bot_center_idx = v_offset
        positions.extend([ox, -h2 + oy, oz])
        normals.extend([0, -1, 0])
        v_offset += 1

        for i in range(segments):
            a = 2 * math.pi * i / segments
            positions.extend([radius_bottom * math.cos(a) + ox, -h2 + oy, radius_bottom * math.sin(a) + oz])
            normals.extend([0, -1, 0])

        for i in range(segments):
            i1 = v_offset + i
            i2 = v_offset + ((i + 1) % segments)
            indices.extend([bot_center_idx, i2, i1])
        v_offset += segments

    return positions, normals, indices


def make_sphere(radius, rings=6, sectors=8, offset=(0, 0, 0)):
    """Generates UV sphere geometry."""
    ox, oy, oz = offset
    positions = []
    normals = []
    indices = []

    for r in range(rings + 1):
        phi = math.pi * r / rings
        for s in range(sectors + 1):
            theta = 2 * math.pi * s / sectors
            nx = math.sin(phi) * math.cos(theta)
            ny = math.cos(phi)
            nz = math.sin(phi) * math.sin(theta)
            positions.extend([radius * nx + ox, radius * ny + oy, radius * nz + oz])
            normals.extend([nx, ny, nz])

    for r in range(rings):
        for s in range(sectors):
            first = r * (sectors + 1) + s
            second = first + sectors + 1
            indices.extend([first, second, first + 1])
            indices.extend([second, second + 1, first + 1])

    return positions, normals, indices


def merge_geometries(geom_list):
    """Merges multiple (positions, normals, indices) into one."""
    all_pos = []
    all_norm = []
    all_idx = []

    vertex_count = 0
    for pos, norm, idx in geom_list:
        all_pos.extend(pos)
        all_norm.extend(norm)
        for i in idx:
            all_idx.append(vertex_count + i)
        vertex_count += len(pos) // 3

    return all_pos, all_norm, all_idx


def generate_all_assets(output_dir):
    os.makedirs(output_dir, exist_ok=True)
    generated = []

    # =========================================================================
    # ETAPA 1/3 SCENERY ASSETS (9 MODELS)
    # =========================================================================

    # 1. terrain_chunk.glb (Volumetric 3D terrain base)
    pos, norm, idx = make_box(14.0, 1.4, 22.0, (0, -0.7, 0))
    pos_b1, norm_b1, idx_b1 = make_box(0.6, 0.4, 22.0, (-6.7, 0.2, 0))
    pos_b2, norm_b2, idx_b2 = make_box(0.6, 0.4, 22.0, (6.7, 0.2, 0))
    p, n, i = merge_geometries([(pos, norm, idx), (pos_b1, norm_b1, idx_b1), (pos_b2, norm_b2, idx_b2)])
    glb_data = create_glb([{"positions": p, "normals": n, "indices": i, "name": "TerrainChunk"}],
        [{"name": "M_Terrain", "pbrMetallicRoughness": {"baseColorFactor": [0.18, 0.35, 0.22, 1.0], "roughnessFactor": 0.85}}])
    path = os.path.join(output_dir, "terrain_chunk.glb")
    with open(path, "wb") as f: f.write(glb_data)
    generated.append(path)

    # 2. bridge_stone.glb (Medieval stone bridge with arch)
    deck_p, deck_n, deck_i = make_box(4.0, 0.3, 3.2, (0, 0.15, 0))
    rail1_p, rail1_n, rail1_i = make_box(0.35, 0.6, 3.2, (-1.8, 0.5, 0))
    rail2_p, rail2_n, rail2_i = make_box(0.35, 0.6, 3.2, (1.8, 0.5, 0))
    pier1_p, pier1_n, pier1_i = make_box(3.8, 0.8, 0.5, (0, -0.4, -1.35))
    pier2_p, pier2_n, pier2_i = make_box(3.8, 0.8, 0.5, (0, -0.4, 1.35))
    p, n, i = merge_geometries([(deck_p, deck_n, deck_i), (rail1_p, rail1_n, rail1_i), (rail2_p, rail2_n, rail2_i), (pier1_p, pier1_n, pier1_i), (pier2_p, pier2_n, pier2_i)])
    glb_data = create_glb([{"positions": p, "normals": n, "indices": i, "name": "StoneBridge"}],
        [{"name": "M_StoneBridge", "pbrMetallicRoughness": {"baseColorFactor": [0.45, 0.48, 0.52, 1.0], "roughnessFactor": 0.7}}])
    path = os.path.join(output_dir, "bridge_stone.glb")
    with open(path, "wb") as f: f.write(glb_data)
    generated.append(path)

    # 3. tree_stylized.glb (Stylized 3-tier pine tree)
    trunk_p, trunk_n, trunk_i = make_cylinder(0.2, 0.28, 1.0, 6, (0, 0.5, 0))
    cone1_p, cone1_n, cone1_i = make_cylinder(0.1, 1.2, 1.1, 7, (0, 1.3, 0))
    cone2_p, cone2_n, cone2_i = make_cylinder(0.08, 0.9, 1.0, 7, (0, 2.0, 0))
    cone3_p, cone3_n, cone3_i = make_cylinder(0.02, 0.6, 0.9, 7, (0, 2.65, 0))
    p, n, i = merge_geometries([(trunk_p, trunk_n, trunk_i), (cone1_p, cone1_n, cone1_i), (cone2_p, cone2_n, cone2_i), (cone3_p, cone3_n, cone3_i)])
    glb_data = create_glb([{"positions": p, "normals": n, "indices": i, "name": "StylizedTree"}],
        [{"name": "M_Tree", "pbrMetallicRoughness": {"baseColorFactor": [0.12, 0.55, 0.25, 1.0], "roughnessFactor": 0.8}}])
    path = os.path.join(output_dir, "tree_stylized.glb")
    with open(path, "wb") as f: f.write(glb_data)
    generated.append(path)

    # 4. rock_boulder.glb (Chiseled faceted boulder)
    b1_p, b1_n, b1_i = make_box(1.2, 0.8, 1.0, (0, 0.4, 0))
    b2_p, b2_n, b2_i = make_box(0.9, 0.6, 0.8, (0.1, 0.7, -0.1))
    p, n, i = merge_geometries([(b1_p, b1_n, b1_i), (b2_p, b2_n, b2_i)])
    glb_data = create_glb([{"positions": p, "normals": n, "indices": i, "name": "RockBoulder"}],
        [{"name": "M_Rock", "pbrMetallicRoughness": {"baseColorFactor": [0.38, 0.42, 0.46, 1.0], "roughnessFactor": 0.9}}])
    path = os.path.join(output_dir, "rock_boulder.glb")
    with open(path, "wb") as f: f.write(glb_data)
    generated.append(path)

    # 5. ruin_pillar.glb (Ancient classical column)
    base_p, base_n, base_i = make_box(0.9, 0.35, 0.9, (0, 0.175, 0))
    col_p, col_n, col_i = make_cylinder(0.32, 0.35, 2.2, 8, (0, 1.4, 0))
    cap_p, cap_n, cap_i = make_box(0.85, 0.3, 0.85, (0, 2.65, 0))
    p, n, i = merge_geometries([(base_p, base_n, base_i), (col_p, col_n, col_i), (cap_p, cap_n, cap_i)])
    glb_data = create_glb([{"positions": p, "normals": n, "indices": i, "name": "RuinPillar"}],
        [{"name": "M_RuinPillar", "pbrMetallicRoughness": {"baseColorFactor": [0.55, 0.58, 0.62, 1.0], "roughnessFactor": 0.75}}])
    path = os.path.join(output_dir, "ruin_pillar.glb")
    with open(path, "wb") as f: f.write(glb_data)
    generated.append(path)

    # 6. crystal_cluster.glb (Glowing faceted crystal cluster)
    c1_p, c1_n, c1_i = make_cylinder(0.04, 0.35, 2.0, 6, (0, 1.0, 0))
    c2_p, c2_n, c2_i = make_cylinder(0.03, 0.25, 1.3, 5, (0.4, 0.65, 0.2))
    c3_p, c3_n, c3_i = make_cylinder(0.02, 0.22, 1.1, 5, (-0.35, 0.55, -0.25))
    p, n, i = merge_geometries([(c1_p, c1_n, c1_i), (c2_p, c2_n, c2_i), (c3_p, c3_n, c3_i)])
    glb_data = create_glb([{"positions": p, "normals": n, "indices": i, "name": "CrystalCluster"}],
        [{"name": "M_Crystal", "emissiveFactor": [0.2, 0.7, 0.9], "pbrMetallicRoughness": {"baseColorFactor": [0.15, 0.85, 1.0, 0.9], "roughnessFactor": 0.1, "metallicFactor": 0.3}}])
    path = os.path.join(output_dir, "crystal_cluster.glb")
    with open(path, "wb") as f: f.write(glb_data)
    generated.append(path)

    # 7. torch_brazier.glb (Stone brazier pedestal with flame)
    ped_p, ped_n, ped_i = make_cylinder(0.25, 0.35, 1.2, 6, (0, 0.6, 0))
    bowl_p, bowl_n, bowl_i = make_cylinder(0.48, 0.22, 0.3, 8, (0, 1.3, 0))
    flame_p, flame_n, flame_i = make_cylinder(0.05, 0.22, 0.5, 6, (0, 1.65, 0))
    p, n, i = merge_geometries([(ped_p, ped_n, ped_i), (bowl_p, bowl_n, bowl_i), (flame_p, flame_n, flame_i)])
    glb_data = create_glb([{"positions": p, "normals": n, "indices": i, "name": "TorchBrazier"}],
        [{"name": "M_Brazier", "emissiveFactor": [0.95, 0.65, 0.15], "pbrMetallicRoughness": {"baseColorFactor": [0.98, 0.75, 0.18, 1.0], "roughnessFactor": 0.4}}])
    path = os.path.join(output_dir, "torch_brazier.glb")
    with open(path, "wb") as f: f.write(glb_data)
    generated.append(path)

    # 8. fortress_castle.glb (Medieval castle entrance)
    gate_p, gate_n, gate_i = make_box(2.8, 2.0, 1.4, (0, 1.0, 0))
    tow1_p, tow1_n, tow1_i = make_cylinder(0.8, 0.9, 2.8, 8, (-1.8, 1.4, 0))
    tow2_p, tow2_n, tow2_i = make_cylinder(0.8, 0.9, 2.8, 8, (1.8, 1.4, 0))
    door_p, door_n, door_i = make_box(1.2, 1.4, 0.3, (0, 0.7, 0.65))
    p, n, i = merge_geometries([(gate_p, gate_n, gate_i), (tow1_p, tow1_n, tow1_i), (tow2_p, tow2_n, tow2_i), (door_p, door_n, door_i)])
    glb_data = create_glb([{"positions": p, "normals": n, "indices": i, "name": "FortressCastle"}],
        [{"name": "M_Castle", "pbrMetallicRoughness": {"baseColorFactor": [0.35, 0.38, 0.44, 1.0], "roughnessFactor": 0.8}}])
    path = os.path.join(output_dir, "fortress_castle.glb")
    with open(path, "wb") as f: f.write(glb_data)
    generated.append(path)

    # 9. summon_portal.glb (Megalithic portal)
    pil1_p, pil1_n, pil1_i = make_box(0.5, 2.4, 0.5, (-1.1, 1.2, 0))
    pil2_p, pil2_n, pil2_i = make_box(0.5, 2.4, 0.5, (1.1, 1.2, 0))
    lint_p, lint_n, lint_i = make_box(3.0, 0.5, 0.6, (0, 2.5, 0))
    vort_p, vort_n, vort_i = make_cylinder(0.85, 0.85, 0.08, 10, (0, 1.2, 0))
    p, n, i = merge_geometries([(pil1_p, pil1_n, pil1_i), (pil2_p, pil2_n, pil2_i), (lint_p, lint_n, lint_i), (vort_p, vort_n, vort_i)])
    glb_data = create_glb([{"positions": p, "normals": n, "indices": i, "name": "SummonPortal"}],
        [{"name": "M_Portal", "emissiveFactor": [0.4, 0.2, 0.8], "pbrMetallicRoughness": {"baseColorFactor": [0.4, 0.3, 0.6, 1.0], "roughnessFactor": 0.5}}])
    path = os.path.join(output_dir, "summon_portal.glb")
    with open(path, "wb") as f: f.write(glb_data)
    generated.append(path)

    # =========================================================================
    # ETAPA 2/3: 6 HEROES (3D ARTICULATED MESHES)
    # =========================================================================

    # 10. hero_kael.glb (Fire Warrior - Sword, Shield Pauldrons, Flame Crest)
    # Materials: 0=Steel Armor, 1=Gold filigree, 2=Red Cape/Tabard, 3=Fire Glow
    kael_materials = [
        {"name": "M_Steel", "pbrMetallicRoughness": {"baseColorFactor": [0.75, 0.78, 0.84, 1.0], "metallicFactor": 0.7, "roughnessFactor": 0.35}},
        {"name": "M_Gold", "pbrMetallicRoughness": {"baseColorFactor": [0.92, 0.75, 0.22, 1.0], "metallicFactor": 0.85, "roughnessFactor": 0.3}},
        {"name": "M_Crimson", "pbrMetallicRoughness": {"baseColorFactor": [0.85, 0.12, 0.15, 1.0], "roughnessFactor": 0.7}},
        {"name": "M_Flame", "emissiveFactor": [1.0, 0.45, 0.0], "pbrMetallicRoughness": {"baseColorFactor": [1.0, 0.65, 0.1, 1.0], "roughnessFactor": 0.2}}
    ]
    # Torso & belt
    torso_p, torso_n, torso_i = make_box(0.55, 0.6, 0.36, (0, 0.85, 0))
    belt_p, belt_n, belt_i = make_box(0.58, 0.14, 0.38, (0, 0.55, 0))
    t_p, t_n, t_i = merge_geometries([(torso_p, torso_n, torso_i), (belt_p, belt_n, belt_i)])
    # Head & Helmet with flaming crest
    head_p, head_n, head_i = make_cylinder(0.2, 0.22, 0.35, 8, (0, 1.35, 0))
    visor_p, visor_n, visor_i = make_box(0.28, 0.08, 0.1, (0, 1.35, 0.16))
    crest_p, crest_n, crest_i = make_box(0.06, 0.25, 0.3, (0, 1.62, 0.02))
    h_p, h_n, h_i = merge_geometries([(head_p, head_n, head_i), (visor_p, visor_n, visor_i), (crest_p, crest_n, crest_i)])
    # Limbs
    la_p, la_n, la_i = make_cylinder(0.08, 0.09, 0.42, 6, (-0.38, 0.8, 0))
    lp_p, lp_n, lp_i = make_box(0.28, 0.28, 0.28, (-0.38, 1.05, 0))
    larm_p, larm_n, larm_i = merge_geometries([(la_p, la_n, la_i), (lp_p, lp_n, lp_i)])
    ra_p, ra_n, ra_i = make_cylinder(0.08, 0.09, 0.42, 6, (0.38, 0.8, 0))
    rp_p, rp_n, rp_i = make_box(0.28, 0.28, 0.28, (0.38, 1.05, 0))
    rarm_p, rarm_n, rarm_i = merge_geometries([(ra_p, ra_n, ra_i), (rp_p, rp_n, rp_i)])
    lleg_p, lleg_n, lleg_i = make_cylinder(0.1, 0.11, 0.5, 6, (-0.18, 0.25, 0))
    rleg_p, rleg_n, rleg_i = make_cylinder(0.1, 0.11, 0.5, 6, (0.18, 0.25, 0))
    # Red hero cape
    cape_p, cape_n, cape_i = make_box(0.5, 0.85, 0.04, (0, 0.8, -0.22))
    # Flaming Greatsword
    hilt_p, hilt_n, hilt_i = make_cylinder(0.03, 0.03, 0.25, 6, (0.5, 0.7, 0.25))
    cross_p, cross_n, cross_i = make_box(0.32, 0.06, 0.08, (0.5, 0.85, 0.25))
    blade_p, blade_n, blade_i = make_box(0.1, 0.95, 0.03, (0.5, 1.35, 0.25))
    flame_blade_p, flame_blade_n, flame_blade_i = make_box(0.14, 1.0, 0.05, (0.5, 1.35, 0.25))
    sword_p, sword_n, sword_i = merge_geometries([(hilt_p, hilt_n, hilt_i), (cross_p, cross_n, cross_i), (blade_p, blade_n, blade_i), (flame_blade_p, flame_blade_n, flame_blade_i)])

    kael_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 0, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 1, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 0, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": lleg_p, "normals": lleg_n, "indices": lleg_i, "material_idx": 0, "name": "LeftLeg"},
        {"positions": rleg_p, "normals": rleg_n, "indices": rleg_i, "material_idx": 0, "name": "RightLeg"},
        {"positions": cape_p, "normals": cape_n, "indices": cape_i, "material_idx": 2, "name": "Cape"},
        {"positions": sword_p, "normals": sword_n, "indices": sword_i, "material_idx": 3, "name": "Weapon"}
    ], kael_materials)
    path = os.path.join(output_dir, "hero_kael.glb")
    with open(path, "wb") as f: f.write(kael_glb)
    generated.append(path)

    # 11. hero_lyra.glb (Arcane Archer - Elven bow, Quiver & Tiara)
    lyra_materials = [
        {"name": "M_EmeraldTunic", "pbrMetallicRoughness": {"baseColorFactor": [0.18, 0.58, 0.32, 1.0], "roughnessFactor": 0.65}},
        {"name": "M_LeatherGold", "pbrMetallicRoughness": {"baseColorFactor": [0.88, 0.72, 0.25, 1.0], "metallicFactor": 0.6, "roughnessFactor": 0.4}},
        {"name": "M_ArcaneGlow", "emissiveFactor": [0.2, 0.85, 1.0], "pbrMetallicRoughness": {"baseColorFactor": [0.3, 0.9, 1.0, 1.0], "roughnessFactor": 0.2}},
        {"name": "M_WoodQuiver", "pbrMetallicRoughness": {"baseColorFactor": [0.45, 0.28, 0.15, 1.0], "roughnessFactor": 0.8}}
    ]
    t_p, t_n, t_i = make_box(0.42, 0.55, 0.28, (0, 0.82, 0))
    head_p, head_n, head_i = make_sphere(0.18, 6, 8, (0, 1.25, 0))
    tiara_p, tiara_n, tiara_i = make_cylinder(0.2, 0.19, 0.06, 8, (0, 1.32, 0))
    h_p, h_n, h_i = merge_geometries([(head_p, head_n, head_i), (tiara_p, tiara_n, tiara_i)])
    larm_p, larm_n, larm_i = make_cylinder(0.065, 0.07, 0.4, 6, (-0.28, 0.78, 0.1))
    rarm_p, rarm_n, rarm_i = make_cylinder(0.065, 0.07, 0.4, 6, (0.28, 0.78, -0.05))
    lleg_p, lleg_n, lleg_i = make_cylinder(0.08, 0.09, 0.48, 6, (-0.14, 0.24, 0))
    rleg_p, rleg_n, rleg_i = make_cylinder(0.08, 0.09, 0.48, 6, (0.14, 0.24, 0))
    quiv_p, quiv_n, quiv_i = make_cylinder(0.08, 0.06, 0.6, 6, (0.15, 0.95, -0.2))
    bow1_p, bow1_n, bow1_i = make_cylinder(0.025, 0.02, 0.5, 6, (-0.35, 1.05, 0.2))
    bow2_p, bow2_n, bow2_i = make_cylinder(0.02, 0.025, 0.5, 6, (-0.35, 0.55, 0.2))
    bowstr_p, bowstr_n, bowstr_i = make_cylinder(0.008, 0.008, 0.95, 4, (-0.35, 0.8, 0.08))
    bow_p, bow_n, bow_i = merge_geometries([(bow1_p, bow1_n, bow1_i), (bow2_p, bow2_n, bow2_i), (bowstr_p, bowstr_n, bowstr_i)])

    lyra_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 0, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 1, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 0, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": lleg_p, "normals": lleg_n, "indices": lleg_i, "material_idx": 0, "name": "LeftLeg"},
        {"positions": rleg_p, "normals": rleg_n, "indices": rleg_i, "material_idx": 0, "name": "RightLeg"},
        {"positions": quiv_p, "normals": quiv_n, "indices": quiv_i, "material_idx": 3, "name": "Quiver"},
        {"positions": bow_p, "normals": bow_n, "indices": bow_i, "material_idx": 2, "name": "Weapon"}
    ], lyra_materials)
    path = os.path.join(output_dir, "hero_lyra.glb")
    with open(path, "wb") as f: f.write(lyra_glb)
    generated.append(path)

    # 12. hero_brakk.glb (Heavy Guardian - Granite Armor, Colossal Warhammer)
    brakk_materials = [
        {"name": "M_GranitePlate", "pbrMetallicRoughness": {"baseColorFactor": [0.35, 0.38, 0.42, 1.0], "metallicFactor": 0.4, "roughnessFactor": 0.75}},
        {"name": "M_DarkIron", "pbrMetallicRoughness": {"baseColorFactor": [0.2, 0.22, 0.25, 1.0], "metallicFactor": 0.8, "roughnessFactor": 0.4}},
        {"name": "M_GoldTrim", "pbrMetallicRoughness": {"baseColorFactor": [0.88, 0.75, 0.2, 1.0], "metallicFactor": 0.8, "roughnessFactor": 0.3}},
        {"name": "M_VisorGlow", "emissiveFactor": [0.95, 0.7, 0.1], "pbrMetallicRoughness": {"baseColorFactor": [1.0, 0.8, 0.2, 1.0], "roughnessFactor": 0.2}}
    ]
    t_p, t_n, t_i = make_box(0.85, 0.75, 0.55, (0, 0.85, 0))
    h_box_p, h_box_n, h_box_i = make_box(0.38, 0.38, 0.38, (0, 1.38, 0))
    h_vis_p, h_vis_n, h_vis_i = make_box(0.3, 0.06, 0.1, (0, 1.38, 0.2))
    h_p, h_n, h_i = merge_geometries([(h_box_p, h_box_n, h_box_i), (h_vis_p, h_vis_n, h_vis_i)])
    larm_p, larm_n, larm_i = make_box(0.42, 0.42, 0.42, (-0.55, 1.1, 0))
    rarm_p, rarm_n, rarm_i = make_box(0.38, 0.38, 0.38, (0.52, 1.1, 0))
    lleg_p, lleg_n, lleg_i = make_box(0.24, 0.5, 0.28, (-0.25, 0.25, 0))
    rleg_p, rleg_n, rleg_i = make_box(0.24, 0.5, 0.28, (0.25, 0.25, 0))
    # Colossal Warhammer
    w_shaft_p, w_shaft_n, w_shaft_i = make_cylinder(0.045, 0.045, 1.3, 6, (0.6, 0.9, 0.2))
    w_head_p, w_head_n, w_head_i = make_box(0.4, 0.38, 0.7, (0.6, 1.5, 0.2))
    w_p, w_n, w_i = merge_geometries([(w_shaft_p, w_shaft_n, w_shaft_i), (w_head_p, w_head_n, w_head_i)])

    brakk_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 0, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 3, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 1, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 1, "name": "RightArm"},
        {"positions": lleg_p, "normals": lleg_n, "indices": lleg_i, "material_idx": 0, "name": "LeftLeg"},
        {"positions": rleg_p, "normals": rleg_n, "indices": rleg_i, "material_idx": 0, "name": "RightLeg"},
        {"positions": w_p, "normals": w_n, "indices": w_i, "material_idx": 1, "name": "Weapon"}
    ], brakk_materials)
    path = os.path.join(output_dir, "hero_brakk.glb")
    with open(path, "wb") as f: f.write(brakk_glb)
    generated.append(path)

    # 13. hero_nyra.glb (Shadow Assassin - Dark Cowl, Dual Daggers & Capelet)
    nyra_materials = [
        {"name": "M_ShadowLeather", "pbrMetallicRoughness": {"baseColorFactor": [0.15, 0.1, 0.22, 1.0], "roughnessFactor": 0.6}},
        {"name": "M_VoidGlow", "emissiveFactor": [0.8, 0.1, 0.95], "pbrMetallicRoughness": {"baseColorFactor": [0.85, 0.2, 1.0, 1.0], "roughnessFactor": 0.2}},
        {"name": "M_SilverEdge", "pbrMetallicRoughness": {"baseColorFactor": [0.75, 0.8, 0.85, 1.0], "metallicFactor": 0.9, "roughnessFactor": 0.25}}
    ]
    t_p, t_n, t_i = make_box(0.4, 0.55, 0.26, (0, 0.8, 0))
    h_p, h_n, h_i = make_cylinder(0.15, 0.22, 0.32, 6, (0, 1.25, 0))
    larm_p, larm_n, larm_i = make_cylinder(0.06, 0.065, 0.38, 6, (-0.28, 0.78, 0.05))
    rarm_p, rarm_n, rarm_i = make_cylinder(0.06, 0.065, 0.38, 6, (0.28, 0.78, 0.05))
    lleg_p, lleg_n, lleg_i = make_cylinder(0.07, 0.08, 0.48, 6, (-0.13, 0.24, 0))
    rleg_p, rleg_n, rleg_i = make_cylinder(0.07, 0.08, 0.48, 6, (0.13, 0.24, 0))
    cape_p, cape_n, cape_i = make_box(0.42, 0.5, 0.04, (0, 0.75, -0.18))
    # Dual shadow daggers
    d1_p, d1_n, d1_i = make_box(0.05, 0.45, 0.02, (-0.35, 0.6, 0.2))
    d2_p, d2_n, d2_i = make_box(0.05, 0.45, 0.02, (0.35, 0.6, 0.2))
    daggers_p, daggers_n, daggers_i = merge_geometries([(d1_p, d1_n, d1_i), (d2_p, d2_n, d2_i)])

    nyra_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 0, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 1, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 0, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": lleg_p, "normals": lleg_n, "indices": lleg_i, "material_idx": 0, "name": "LeftLeg"},
        {"positions": rleg_p, "normals": rleg_n, "indices": rleg_i, "material_idx": 0, "name": "RightLeg"},
        {"positions": cape_p, "normals": cape_n, "indices": cape_i, "material_idx": 0, "name": "Cape"},
        {"positions": daggers_p, "normals": daggers_n, "indices": daggers_i, "material_idx": 1, "name": "Weapon"}
    ], nyra_materials)
    path = os.path.join(output_dir, "hero_nyra.glb")
    with open(path, "wb") as f: f.write(nyra_glb)
    generated.append(path)

    # 14. hero_tiko.glb (Master Engineer - Steam Boiler, Goggles, Giant Wrench)
    tiko_materials = [
        {"name": "M_BrassBronze", "pbrMetallicRoughness": {"baseColorFactor": [0.75, 0.55, 0.2, 1.0], "metallicFactor": 0.8, "roughnessFactor": 0.35}},
        {"name": "M_LeatherApron", "pbrMetallicRoughness": {"baseColorFactor": [0.45, 0.28, 0.15, 1.0], "roughnessFactor": 0.7}},
        {"name": "M_CyanSpark", "emissiveFactor": [0.1, 0.9, 0.8], "pbrMetallicRoughness": {"baseColorFactor": [0.2, 1.0, 0.9, 1.0], "roughnessFactor": 0.2}}
    ]
    t_p, t_n, t_i = make_box(0.48, 0.55, 0.35, (0, 0.78, 0))
    h_cap_p, h_cap_n, h_cap_i = make_cylinder(0.18, 0.2, 0.28, 8, (0, 1.2, 0))
    gogg_p, gogg_n, gogg_i = make_box(0.24, 0.08, 0.08, (0, 1.25, 0.16))
    h_p, h_n, h_i = merge_geometries([(h_cap_p, h_cap_n, h_cap_i), (gogg_p, gogg_n, gogg_i)])
    larm_p, larm_n, larm_i = make_cylinder(0.07, 0.075, 0.4, 6, (-0.32, 0.75, 0))
    rarm_p, rarm_n, rarm_i = make_cylinder(0.07, 0.075, 0.4, 6, (0.32, 0.75, 0))
    lleg_p, lleg_n, lleg_i = make_box(0.18, 0.45, 0.22, (-0.16, 0.22, 0))
    rleg_p, rleg_n, rleg_i = make_box(0.18, 0.45, 0.22, (0.16, 0.22, 0))
    # Backpack boiler
    bp_p, bp_n, bp_i = make_box(0.35, 0.45, 0.25, (0, 0.85, -0.28))
    chim_p, chim_n, chim_i = make_cylinder(0.05, 0.05, 0.4, 6, (0.1, 1.2, -0.28))
    boiler_p, boiler_n, boiler_i = merge_geometries([(bp_p, bp_n, bp_i), (chim_p, chim_n, chim_i)])
    # Wrench
    w_p, w_n, w_i = make_box(0.16, 0.7, 0.08, (0.42, 0.8, 0.2))

    tiko_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 1, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 2, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 0, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": lleg_p, "normals": lleg_n, "indices": lleg_i, "material_idx": 1, "name": "LeftLeg"},
        {"positions": rleg_p, "normals": rleg_n, "indices": rleg_i, "material_idx": 1, "name": "RightLeg"},
        {"positions": boiler_p, "normals": boiler_n, "indices": boiler_i, "material_idx": 0, "name": "Backpack"},
        {"positions": w_p, "normals": w_n, "indices": w_i, "material_idx": 0, "name": "Weapon"}
    ], tiko_materials)
    path = os.path.join(output_dir, "hero_tiko.glb")
    with open(path, "wb") as f: f.write(tiko_glb)
    generated.append(path)

    # 15. hero_vesper.glb (Astral Mage - Cosmic Robes, Astral Crescent Staff)
    vesper_materials = [
        {"name": "M_CelestialIndigo", "pbrMetallicRoughness": {"baseColorFactor": [0.12, 0.15, 0.42, 1.0], "roughnessFactor": 0.7}},
        {"name": "M_StarlightGlow", "emissiveFactor": [0.5, 0.7, 1.0], "pbrMetallicRoughness": {"baseColorFactor": [0.6, 0.8, 1.0, 1.0], "roughnessFactor": 0.2}},
        {"name": "M_GoldRune", "pbrMetallicRoughness": {"baseColorFactor": [0.95, 0.85, 0.3, 1.0], "metallicFactor": 0.8, "roughnessFactor": 0.3}}
    ]
    t_p, t_n, t_i = make_cylinder(0.25, 0.45, 0.8, 8, (0, 0.75, 0))
    h_p, h_n, h_i = make_cylinder(0.16, 0.2, 0.3, 8, (0, 1.3, 0))
    larm_p, larm_n, larm_i = make_cylinder(0.06, 0.07, 0.38, 6, (-0.28, 0.78, 0.05))
    rarm_p, rarm_n, rarm_i = make_cylinder(0.06, 0.07, 0.38, 6, (0.28, 0.78, 0.05))
    skirt_p, skirt_n, skirt_i = make_cylinder(0.4, 0.55, 0.4, 8, (0, 0.2, 0))
    # Astral staff
    st_pole_p, st_pole_n, st_pole_i = make_cylinder(0.03, 0.03, 1.6, 6, (0.45, 0.9, 0.15))
    st_orb_p, st_orb_n, st_orb_i = make_sphere(0.12, 6, 8, (0.45, 1.75, 0.15))
    st_cres_p, st_cres_n, st_cres_i = make_cylinder(0.18, 0.18, 0.04, 8, (0.45, 1.75, 0.15))
    staff_p, staff_n, staff_i = merge_geometries([(st_pole_p, st_pole_n, st_pole_i), (st_orb_p, st_orb_n, st_orb_i), (st_cres_p, st_cres_n, st_cres_i)])

    vesper_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 0, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 1, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 0, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": skirt_p, "normals": skirt_n, "indices": skirt_i, "material_idx": 0, "name": "RobeSkirt"},
        {"positions": staff_p, "normals": staff_n, "indices": staff_i, "material_idx": 1, "name": "Weapon"}
    ], vesper_materials)
    path = os.path.join(output_dir, "hero_vesper.glb")
    with open(path, "wb") as f: f.write(vesper_glb)
    generated.append(path)

    # =========================================================================
    # ETAPA 2/3: 4 COMMON ENEMIES
    # =========================================================================

    # 16. enemy_goblin.glb (Goblin Raider - Spiked Club & Pointed Ears)
    goblin_materials = [
        {"name": "M_GoblinGreen", "pbrMetallicRoughness": {"baseColorFactor": [0.22, 0.65, 0.28, 1.0], "roughnessFactor": 0.75}},
        {"name": "M_WornLeather", "pbrMetallicRoughness": {"baseColorFactor": [0.47, 0.21, 0.06, 1.0], "roughnessFactor": 0.85}},
        {"name": "M_YellowEye", "emissiveFactor": [0.98, 0.8, 0.08], "pbrMetallicRoughness": {"baseColorFactor": [1.0, 0.85, 0.1, 1.0], "roughnessFactor": 0.3}}
    ]
    t_p, t_n, t_i = make_box(0.42, 0.45, 0.3, (0, 0.65, 0.05))
    h_head_p, h_head_n, h_head_i = make_sphere(0.18, 6, 8, (0, 1.02, 0.1))
    ear1_p, ear1_n, ear1_i = make_box(0.15, 0.06, 0.04, (-0.22, 1.05, 0.05))
    ear2_p, ear2_n, ear2_i = make_box(0.15, 0.06, 0.04, (0.22, 1.05, 0.05))
    h_p, h_n, h_i = merge_geometries([(h_head_p, h_head_n, h_head_i), (ear1_p, ear1_n, ear1_i), (ear2_p, ear2_n, ear2_i)])
    larm_p, larm_n, larm_i = make_cylinder(0.06, 0.065, 0.35, 6, (-0.28, 0.65, 0))
    rarm_p, rarm_n, rarm_i = make_cylinder(0.06, 0.065, 0.35, 6, (0.28, 0.65, 0))
    lleg_p, lleg_n, lleg_i = make_cylinder(0.07, 0.08, 0.4, 6, (-0.14, 0.2, 0))
    rleg_p, rleg_n, rleg_i = make_cylinder(0.07, 0.08, 0.4, 6, (0.14, 0.2, 0))
    # Spiked club
    c_handle_p, c_handle_n, c_handle_i = make_cylinder(0.03, 0.03, 0.3, 6, (0.35, 0.5, 0.2))
    c_head_p, c_head_n, c_head_i = make_cylinder(0.09, 0.05, 0.45, 6, (0.35, 0.8, 0.2))
    club_p, club_n, club_i = merge_geometries([(c_handle_p, c_handle_n, c_handle_i), (c_head_p, c_head_n, c_head_i)])

    goblin_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 0, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 2, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 0, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": lleg_p, "normals": lleg_n, "indices": lleg_i, "material_idx": 1, "name": "LeftLeg"},
        {"positions": rleg_p, "normals": rleg_n, "indices": rleg_i, "material_idx": 1, "name": "RightLeg"},
        {"positions": club_p, "normals": club_n, "indices": club_i, "material_idx": 1, "name": "Weapon"}
    ], goblin_materials)
    path = os.path.join(output_dir, "enemy_goblin.glb")
    with open(path, "wb") as f: f.write(goblin_glb)
    generated.append(path)

    # 17. enemy_skeleton.glb (Cursed Skeleton - Ivory Skull, Broadsword & Buckler)
    skeleton_materials = [
        {"name": "M_BoneIvory", "pbrMetallicRoughness": {"baseColorFactor": [0.88, 0.85, 0.78, 1.0], "roughnessFactor": 0.65}},
        {"name": "M_RustedIron", "pbrMetallicRoughness": {"baseColorFactor": [0.4, 0.38, 0.42, 1.0], "metallicFactor": 0.6, "roughnessFactor": 0.6}},
        {"name": "M_SoulCyan", "emissiveFactor": [0.05, 0.85, 1.0], "pbrMetallicRoughness": {"baseColorFactor": [0.1, 0.9, 1.0, 1.0], "roughnessFactor": 0.2}}
    ]
    t_rib_p, t_rib_n, t_rib_i = make_box(0.35, 0.4, 0.2, (0, 0.75, 0))
    t_spine_p, t_spine_n, t_spine_i = make_cylinder(0.04, 0.04, 0.4, 6, (0, 0.75, 0))
    t_p, t_n, t_i = merge_geometries([(t_rib_p, t_rib_n, t_rib_i), (t_spine_p, t_spine_n, t_spine_i)])
    h_p, h_n, h_i = make_box(0.24, 0.26, 0.24, (0, 1.15, 0))
    larm_p, larm_n, larm_i = make_cylinder(0.04, 0.04, 0.45, 5, (-0.28, 0.7, 0))
    rarm_p, rarm_n, rarm_i = make_cylinder(0.04, 0.04, 0.45, 5, (0.28, 0.7, 0))
    lleg_p, lleg_n, lleg_i = make_cylinder(0.045, 0.045, 0.5, 5, (-0.12, 0.25, 0))
    rleg_p, rleg_n, rleg_i = make_cylinder(0.045, 0.045, 0.5, 5, (0.12, 0.25, 0))
    # Shield & Sword
    sh_p, sh_n, sh_i = make_cylinder(0.25, 0.25, 0.04, 8, (-0.35, 0.7, 0.15))
    sw_p, sw_n, sw_i = make_box(0.08, 0.75, 0.02, (0.35, 0.75, 0.15))
    wpn_p, wpn_n, wpn_i = merge_geometries([(sh_p, sh_n, sh_i), (sw_p, sw_n, sw_i)])

    skeleton_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 0, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 2, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 0, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": lleg_p, "normals": lleg_n, "indices": lleg_i, "material_idx": 0, "name": "LeftLeg"},
        {"positions": rleg_p, "normals": rleg_n, "indices": rleg_i, "material_idx": 0, "name": "RightLeg"},
        {"positions": wpn_p, "normals": wpn_n, "indices": wpn_i, "material_idx": 1, "name": "Weapon"}
    ], skeleton_materials)
    path = os.path.join(output_dir, "enemy_skeleton.glb")
    with open(path, "wb") as f: f.write(skeleton_glb)
    generated.append(path)

    # 18. enemy_orc.glb (Orc Warrior - Heavy Battleaxe & Tusked Jaw)
    orc_materials = [
        {"name": "M_OrcGreen", "pbrMetallicRoughness": {"baseColorFactor": [0.28, 0.5, 0.22, 1.0], "roughnessFactor": 0.8}},
        {"name": "M_DarkSteel", "pbrMetallicRoughness": {"baseColorFactor": [0.18, 0.2, 0.22, 1.0], "metallicFactor": 0.8, "roughnessFactor": 0.4}},
        {"name": "M_WarPaintRed", "pbrMetallicRoughness": {"baseColorFactor": [0.85, 0.15, 0.15, 1.0], "roughnessFactor": 0.6}}
    ]
    t_p, t_n, t_i = make_box(0.7, 0.65, 0.45, (0, 0.8, 0))
    h_jaw_p, h_jaw_n, h_jaw_i = make_box(0.34, 0.34, 0.34, (0, 1.25, 0.05))
    tusk1_p, tusk1_n, tusk1_i = make_box(0.04, 0.1, 0.04, (-0.1, 1.25, 0.24))
    tusk2_p, tusk2_n, tusk2_i = make_box(0.04, 0.1, 0.04, (0.1, 1.25, 0.24))
    h_p, h_n, h_i = merge_geometries([(h_jaw_p, h_jaw_n, h_jaw_i), (tusk1_p, tusk1_n, tusk1_i), (tusk2_p, tusk2_n, tusk2_i)])
    larm_p, larm_n, larm_i = make_box(0.35, 0.35, 0.35, (-0.48, 1.05, 0))
    rarm_p, rarm_n, rarm_i = make_cylinder(0.09, 0.1, 0.45, 6, (0.42, 0.8, 0))
    lleg_p, lleg_n, lleg_i = make_cylinder(0.12, 0.14, 0.52, 6, (-0.2, 0.26, 0))
    rleg_p, rleg_n, rleg_i = make_cylinder(0.12, 0.14, 0.52, 6, (0.2, 0.26, 0))
    # Double-bitted Battleaxe
    ax_sh_p, ax_sh_n, ax_sh_i = make_cylinder(0.04, 0.04, 1.1, 6, (0.45, 0.8, 0.15))
    ax_bl_p, ax_bl_n, ax_bl_i = make_box(0.48, 0.35, 0.03, (0.45, 1.2, 0.15))
    axe_p, axe_n, axe_i = merge_geometries([(ax_sh_p, ax_sh_n, ax_sh_i), (ax_bl_p, ax_bl_n, ax_bl_i)])

    orc_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 0, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 2, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 1, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": lleg_p, "normals": lleg_n, "indices": lleg_i, "material_idx": 0, "name": "LeftLeg"},
        {"positions": rleg_p, "normals": rleg_n, "indices": rleg_i, "material_idx": 0, "name": "RightLeg"},
        {"positions": axe_p, "normals": axe_n, "indices": axe_i, "material_idx": 1, "name": "Weapon"}
    ], orc_materials)
    path = os.path.join(output_dir, "enemy_orc.glb")
    with open(path, "wb") as f: f.write(orc_glb)
    generated.append(path)

    # 19. enemy_dark_mage.glb (Dark Mage - Pointed Cowl & Void Staff)
    mage_materials = [
        {"name": "M_DarkVioletRobe", "pbrMetallicRoughness": {"baseColorFactor": [0.22, 0.08, 0.28, 1.0], "roughnessFactor": 0.7}},
        {"name": "M_VoidPurpleGlow", "emissiveFactor": [0.85, 0.05, 0.95], "pbrMetallicRoughness": {"baseColorFactor": [0.9, 0.1, 1.0, 1.0], "roughnessFactor": 0.2}},
        {"name": "M_GnarledWood", "pbrMetallicRoughness": {"baseColorFactor": [0.35, 0.2, 0.1, 1.0], "roughnessFactor": 0.85}}
    ]
    t_p, t_n, t_i = make_cylinder(0.22, 0.42, 0.9, 8, (0, 0.65, 0))
    h_cowl_p, h_cowl_n, h_cowl_i = make_cylinder(0.03, 0.24, 0.45, 6, (0, 1.35, 0))
    eyes_p, eyes_n, eyes_i = make_box(0.18, 0.05, 0.06, (0, 1.25, 0.15))
    h_p, h_n, h_i = merge_geometries([(h_cowl_p, h_cowl_n, h_cowl_i), (eyes_p, eyes_n, eyes_i)])
    larm_p, larm_n, larm_i = make_cylinder(0.06, 0.065, 0.38, 6, (-0.28, 0.72, 0))
    rarm_p, rarm_n, rarm_i = make_cylinder(0.06, 0.065, 0.38, 6, (0.28, 0.72, 0))
    lleg_p, lleg_n, lleg_i = make_cylinder(0.07, 0.08, 0.3, 6, (-0.12, 0.15, 0))
    rleg_p, rleg_n, rleg_i = make_cylinder(0.07, 0.08, 0.3, 6, (0.12, 0.15, 0))
    # Staff
    st_p, st_n, st_i = make_cylinder(0.03, 0.03, 1.5, 6, (0.4, 0.8, 0.15))
    orb_p, orb_n, orb_i = make_sphere(0.14, 6, 8, (0.4, 1.55, 0.15))
    staff_p, staff_n, staff_i = merge_geometries([(st_p, st_n, st_i), (orb_p, orb_n, orb_i)])

    dark_mage_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 0, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 1, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 0, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": lleg_p, "normals": lleg_n, "indices": lleg_i, "material_idx": 0, "name": "LeftLeg"},
        {"positions": rleg_p, "normals": rleg_n, "indices": rleg_i, "material_idx": 0, "name": "RightLeg"},
        {"positions": staff_p, "normals": staff_n, "indices": staff_i, "material_idx": 1, "name": "Weapon"}
    ], mage_materials)
    path = os.path.join(output_dir, "enemy_dark_mage.glb")
    with open(path, "wb") as f: f.write(dark_mage_glb)
    generated.append(path)

    # =========================================================================
    # ETAPA 2/3: 3 EPIC BOSSES
    # =========================================================================

    # 20. boss_obsidian_golem.glb (Colossal Volcanic Titan)
    golem_materials = [
        {"name": "M_BasaltRock", "pbrMetallicRoughness": {"baseColorFactor": [0.15, 0.16, 0.18, 1.0], "roughnessFactor": 0.85}},
        {"name": "M_MoltenMagma", "emissiveFactor": [1.0, 0.35, 0.0], "pbrMetallicRoughness": {"baseColorFactor": [1.0, 0.5, 0.1, 1.0], "roughnessFactor": 0.2}},
        {"name": "M_HardenedLava", "pbrMetallicRoughness": {"baseColorFactor": [0.28, 0.12, 0.08, 1.0], "roughnessFactor": 0.7}}
    ]
    t_rock_p, t_rock_n, t_rock_i = make_box(1.6, 1.4, 1.1, (0, 1.8, 0))
    t_core_p, t_core_n, t_core_i = make_sphere(0.42, 6, 8, (0, 1.8, 0.4))
    t_p, t_n, t_i = merge_geometries([(t_rock_p, t_rock_n, t_rock_i), (t_core_p, t_core_n, t_core_i)])
    h_p, h_n, h_i = make_box(0.65, 0.55, 0.65, (0, 2.7, 0.1))
    larm_p, larm_n, larm_i = make_box(0.6, 0.6, 0.75, (-1.2, 1.2, 0.2))
    rarm_p, rarm_n, rarm_i = make_box(0.6, 0.6, 0.75, (1.2, 1.2, 0.2))
    lleg_p, lleg_n, lleg_i = make_cylinder(0.38, 0.45, 1.1, 8, (-0.55, 0.55, 0))
    rleg_p, rleg_n, rleg_i = make_cylinder(0.38, 0.45, 1.1, 8, (0.55, 0.55, 0))

    golem_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 1, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 0, "name": "Head"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 0, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": lleg_p, "normals": lleg_n, "indices": lleg_i, "material_idx": 0, "name": "LeftLeg"},
        {"positions": rleg_p, "normals": rleg_n, "indices": rleg_i, "material_idx": 0, "name": "RightLeg"}
    ], golem_materials)
    path = os.path.join(output_dir, "boss_obsidian_golem.glb")
    with open(path, "wb") as f: f.write(golem_glb)
    generated.append(path)

    # 21. boss_shadow_queen.glb (Ethereal Shadow Sovereign - Gothic Gown & Twin Rapiers)
    queen_materials = [
        {"name": "M_ObsidianGown", "pbrMetallicRoughness": {"baseColorFactor": [0.1, 0.08, 0.15, 1.0], "roughnessFactor": 0.5}},
        {"name": "M_VioletSoulfire", "emissiveFactor": [0.85, 0.15, 0.95], "pbrMetallicRoughness": {"baseColorFactor": [0.9, 0.2, 1.0, 1.0], "roughnessFactor": 0.2}},
        {"name": "M_ShadowCrown", "pbrMetallicRoughness": {"baseColorFactor": [0.88, 0.65, 0.15, 1.0], "metallicFactor": 0.85, "roughnessFactor": 0.3}}
    ]
    t_p, t_n, t_i = make_cylinder(0.3, 0.7, 1.5, 8, (0, 1.25, 0))
    h_face_p, h_face_n, h_face_i = make_sphere(0.22, 6, 8, (0, 2.3, 0))
    cr_p, cr_n, cr_i = make_cylinder(0.24, 0.18, 0.28, 6, (0, 2.55, 0))
    h_p, h_n, h_i = merge_geometries([(h_face_p, h_face_n, h_face_i), (cr_p, cr_n, cr_i)])
    wings_p, wings_n, wings_i = make_box(1.5, 1.3, 0.05, (0, 1.9, -0.3))
    larm_p, larm_n, larm_i = make_cylinder(0.07, 0.075, 0.5, 6, (-0.4, 1.5, 0.1))
    rarm_p, rarm_n, rarm_i = make_cylinder(0.07, 0.075, 0.5, 6, (0.4, 1.5, 0.1))
    # Twin shadow rapiers
    rap1_p, rap1_n, rap1_i = make_box(0.04, 0.95, 0.04, (-0.55, 1.3, 0.3))
    rap2_p, rap2_n, rap2_i = make_box(0.04, 0.95, 0.04, (0.55, 1.3, 0.3))
    rapiers_p, rapiers_n, rapiers_i = merge_geometries([(rap1_p, rap1_n, rap1_i), (rap2_p, rap2_n, rap2_i)])

    queen_glb = create_glb([
        {"positions": t_p, "normals": t_n, "indices": t_i, "material_idx": 0, "name": "Torso"},
        {"positions": h_p, "normals": h_n, "indices": h_i, "material_idx": 2, "name": "Head"},
        {"positions": wings_p, "normals": wings_n, "indices": wings_i, "material_idx": 1, "name": "Wings"},
        {"positions": larm_p, "normals": larm_n, "indices": larm_i, "material_idx": 0, "name": "LeftArm"},
        {"positions": rarm_p, "normals": rarm_n, "indices": rarm_i, "material_idx": 0, "name": "RightArm"},
        {"positions": rapiers_p, "normals": rapiers_n, "indices": rapiers_i, "material_idx": 1, "name": "Weapon"}
    ], queen_materials)
    path = os.path.join(output_dir, "boss_shadow_queen.glb")
    with open(path, "wb") as f: f.write(queen_glb)
    generated.append(path)

    # 22. boss_astral_dragon.glb (Colossal Celestial Dragon)
    dragon_materials = [
        {"name": "M_CelestialScale", "pbrMetallicRoughness": {"baseColorFactor": [0.08, 0.15, 0.35, 1.0], "metallicFactor": 0.5, "roughnessFactor": 0.45}},
        {"name": "M_StarlightMembrane", "emissiveFactor": [0.35, 0.75, 1.0], "pbrMetallicRoughness": {"baseColorFactor": [0.4, 0.8, 1.0, 1.0], "roughnessFactor": 0.2}},
        {"name": "M_CosmicHorn", "pbrMetallicRoughness": {"baseColorFactor": [0.92, 0.8, 0.3, 1.0], "metallicFactor": 0.7, "roughnessFactor": 0.3}}
    ]
    body_p, body_n, body_i = make_box(1.2, 0.9, 2.2, (0, 1.0, 0))
    neck_p, neck_n, neck_i = make_box(0.5, 0.8, 0.7, (0, 1.6, 1.2))
    head_p, head_n, head_i = make_box(0.6, 0.45, 0.9, (0, 1.9, 1.8))
    horns_p, horns_n, horns_i = make_cylinder(0.04, 0.1, 0.6, 5, (-0.2, 2.3, 1.6))
    tail_p, tail_n, tail_i = make_cylinder(0.2, 0.05, 2.0, 6, (0, 0.8, -1.8))
    # Wide wings
    lw_p, lw_n, lw_i = make_box(2.2, 0.08, 1.2, (-1.6, 1.8, 0))
    rw_p, rw_n, rw_i = make_box(2.2, 0.08, 1.2, (1.6, 1.8, 0))
    wings_p, wings_n, wings_i = merge_geometries([(lw_p, lw_n, lw_i), (rw_p, rw_n, rw_i)])

    dragon_glb = create_glb([
        {"positions": body_p, "normals": body_n, "indices": body_i, "material_idx": 0, "name": "Torso"},
        {"positions": neck_p, "normals": neck_n, "indices": neck_i, "material_idx": 0, "name": "Neck"},
        {"positions": head_p, "normals": head_n, "indices": head_i, "material_idx": 2, "name": "Head"},
        {"positions": tail_p, "normals": tail_n, "indices": tail_i, "material_idx": 0, "name": "Tail"},
        {"positions": wings_p, "normals": wings_n, "indices": wings_i, "material_idx": 1, "name": "Wings"}
    ], dragon_materials)
    path = os.path.join(output_dir, "boss_astral_dragon.glb")
    with open(path, "wb") as f: f.write(dragon_glb)
    generated.append(path)

    # =========================================================================
    # ETAPA 2/3: 3 DETAILED TOWERS
    # =========================================================================

    # 23. tower_archer_detailed.glb (3-tier medieval watchtower)
    archer_tower_materials = [
        {"name": "M_HewnStone", "pbrMetallicRoughness": {"baseColorFactor": [0.38, 0.4, 0.45, 1.0], "roughnessFactor": 0.85}},
        {"name": "M_HeavyTimber", "pbrMetallicRoughness": {"baseColorFactor": [0.45, 0.25, 0.1, 1.0], "roughnessFactor": 0.75}},
        {"name": "M_RoofGreen", "pbrMetallicRoughness": {"baseColorFactor": [0.12, 0.48, 0.22, 1.0], "roughnessFactor": 0.6}},
        {"name": "M_GoldPennant", "pbrMetallicRoughness": {"baseColorFactor": [0.92, 0.75, 0.15, 1.0], "roughnessFactor": 0.4}}
    ]
    base_p, base_n, base_i = make_cylinder(1.4, 1.6, 0.8, 8, (0, 0.4, 0))
    mid_p, mid_n, mid_i = make_box(1.9, 1.0, 1.9, (0, 1.3, 0))
    deck_p, deck_n, deck_i = make_box(2.2, 0.3, 2.2, (0, 1.95, 0))
    roof_p, roof_n, roof_i = make_cylinder(0.1, 1.8, 0.9, 6, (0, 2.9, 0))
    arbalest_p, arbalest_n, arbalest_i = make_box(0.5, 0.25, 1.1, (0, 2.2, 0))

    t_archer_glb = create_glb([
        {"positions": base_p, "normals": base_n, "indices": base_i, "material_idx": 0, "name": "BaseStone"},
        {"positions": mid_p, "normals": mid_n, "indices": mid_i, "material_idx": 1, "name": "TimberGallery"},
        {"positions": deck_p, "normals": deck_n, "indices": deck_i, "material_idx": 1, "name": "BattlementPlatform"},
        {"positions": roof_p, "normals": roof_n, "indices": roof_i, "material_idx": 2, "name": "RoofCanopy"},
        {"positions": arbalest_p, "normals": arbalest_n, "indices": arbalest_i, "material_idx": 3, "name": "TurretMount"}
    ], archer_tower_materials)
    path = os.path.join(output_dir, "tower_archer_detailed.glb")
    with open(path, "wb") as f: f.write(t_archer_glb)
    generated.append(path)

    # 24. tower_mage_detailed.glb (Arcane Observatory)
    mage_tower_materials = [
        {"name": "M_RunicMarble", "pbrMetallicRoughness": {"baseColorFactor": [0.75, 0.78, 0.85, 1.0], "roughnessFactor": 0.5}},
        {"name": "M_SpiralPillars", "pbrMetallicRoughness": {"baseColorFactor": [0.45, 0.2, 0.65, 1.0], "metallicFactor": 0.4, "roughnessFactor": 0.4}},
        {"name": "M_FloatingFocusCrystal", "emissiveFactor": [0.3, 0.8, 1.0], "pbrMetallicRoughness": {"baseColorFactor": [0.4, 0.9, 1.0, 1.0], "roughnessFactor": 0.15}}
    ]
    base_p, base_n, base_i = make_cylinder(1.5, 1.7, 0.6, 12, (0, 0.3, 0))
    sp1_p, sp1_n, sp1_i = make_cylinder(0.18, 0.25, 2.4, 6, (-0.8, 1.6, -0.4))
    sp2_p, sp2_n, sp2_i = make_cylinder(0.18, 0.25, 2.4, 6, (0.8, 1.6, -0.4))
    sp3_p, sp3_n, sp3_i = make_cylinder(0.18, 0.25, 2.4, 6, (0, 1.6, 0.9))
    spires_p, spires_n, spires_i = merge_geometries([(sp1_p, sp1_n, sp1_i), (sp2_p, sp2_n, sp2_i), (sp3_p, sp3_n, sp3_i)])
    dais_p, dais_n, dais_i = make_cylinder(1.1, 1.1, 0.1, 10, (0, 2.0, 0))
    c_up_p, c_up_n, c_up_i = make_cylinder(0.04, 0.4, 1.0, 6, (0, 2.8, 0))
    c_dn_p, c_dn_n, c_dn_i = make_cylinder(0.4, 0.04, 0.6, 6, (0, 2.2, 0))
    f_cryst_p, f_cryst_n, f_cryst_i = merge_geometries([(c_up_p, c_up_n, c_up_i), (c_dn_p, c_dn_n, c_dn_i)])

    t_mage_glb = create_glb([
        {"positions": base_p, "normals": base_n, "indices": base_i, "material_idx": 0, "name": "BaseStone"},
        {"positions": spires_p, "normals": spires_n, "indices": spires_i, "material_idx": 1, "name": "SpiralPillars"},
        {"positions": dais_p, "normals": dais_n, "indices": dais_i, "material_idx": 0, "name": "RunicDais"},
        {"positions": f_cryst_p, "normals": f_cryst_n, "indices": f_cryst_i, "material_idx": 2, "name": "FocusCrystal"}
    ], mage_tower_materials)
    path = os.path.join(output_dir, "tower_mage_detailed.glb")
    with open(path, "wb") as f: f.write(t_mage_glb)
    generated.append(path)

    # 25. tower_cannon_detailed.glb (Fortified Iron Bastion with Swivel Bombard)
    cannon_tower_materials = [
        {"name": "M_IronFortress", "pbrMetallicRoughness": {"baseColorFactor": [0.25, 0.26, 0.3, 1.0], "metallicFactor": 0.7, "roughnessFactor": 0.6}},
        {"name": "M_HeavyBombard", "pbrMetallicRoughness": {"baseColorFactor": [0.18, 0.19, 0.22, 1.0], "metallicFactor": 0.85, "roughnessFactor": 0.3}},
        {"name": "M_BronzeTrim", "pbrMetallicRoughness": {"baseColorFactor": [0.75, 0.55, 0.22, 1.0], "metallicFactor": 0.8, "roughnessFactor": 0.35}}
    ]
    base_p, base_n, base_i = make_cylinder(1.6, 1.8, 0.9, 10, (0, 0.45, 0))
    para_p, para_n, para_i = make_cylinder(1.7, 1.7, 0.5, 8, (0, 1.15, 0))
    chassis_p, chassis_n, chassis_i = make_cylinder(1.2, 1.3, 0.3, 8, (0, 1.45, 0))
    # Cannon barrel pointing forward
    bar_p, bar_n, bar_i = make_cylinder(0.32, 0.42, 1.8, 8, (0, 1.85, 0.4))
    muz_p, muz_n, muz_i = make_cylinder(0.38, 0.38, 0.2, 8, (0, 1.85, 1.25))
    barrel_p, barrel_n, barrel_i = merge_geometries([(bar_p, bar_n, bar_i), (muz_p, muz_n, muz_i)])

    t_cannon_glb = create_glb([
        {"positions": base_p, "normals": base_n, "indices": base_i, "material_idx": 0, "name": "BaseStone"},
        {"positions": para_p, "normals": para_n, "indices": para_i, "material_idx": 0, "name": "Parapet"},
        {"positions": chassis_p, "normals": chassis_n, "indices": chassis_i, "material_idx": 1, "name": "TurretChassis"},
        {"positions": barrel_p, "normals": barrel_n, "indices": barrel_i, "material_idx": 2, "name": "CannonBarrel"}
    ], cannon_tower_materials)
    path = os.path.join(output_dir, "tower_cannon_detailed.glb")
    with open(path, "wb") as f: f.write(t_cannon_glb)
    generated.append(path)

    print(f"Successfully generated all {len(generated)} 3D .glb models in {output_dir}:")
    for g in generated:
        print(f"  - {os.path.basename(g)} ({os.path.getsize(g)} bytes)")

if __name__ == "__main__":
    import sys
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "..", "models")
    generate_all_assets(os.path.abspath(out))
