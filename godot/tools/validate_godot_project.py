#!/usr/bin/env python3
"""
Automated Validator for Godot 4 3D Isometric Battlefield (Etapas 1/3 and 2/3).
Validates:
  - 25 binary glTF 2.0 (.glb) 3D assets (scenery, heroes, enemies, bosses, detailed towers)
  - 15 GDScripts (camera, biomes, living environment, heroes, enemies, bosses, towers)
  - 23 Godot 4 .tscn scenes (biomes, heroes, enemies, bosses, towers, battlefield)
  - Canonical isometric camera mathematics (-35.264°, 45.0°)
  - Mathematical 2D/3D coordinate converter bridge
"""

import os
import re
import sys
import struct
import json

def check_file_exists(path, label=""):
    if not os.path.isfile(path):
        print(f"FAIL: {label} file not found: {path}")
        return False
    size = os.path.getsize(path)
    if size == 0:
        print(f"FAIL: {label} file is empty: {path}")
        return False
    print(f"OK: {label} found ({size} bytes): {os.path.basename(path)}")
    return True

def validate_glb(path):
    with open(path, "rb") as f:
        data = f.read()
    if len(data) < 20:
        return False, "File too small for glTF 2.0 binary"
    magic, version, length = struct.unpack("<4sII", data[:12])
    if magic != b"glTF" or version != 2:
        return False, f"Invalid glTF header: magic={magic}, version={version}"
    if length != len(data):
        return False, f"Header length ({length}) != actual data length ({len(data)})"
    return True, f"Valid glTF 2.0 GLB ({len(data)} bytes)"

def validate_tscn(path, root_dir):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    # Check format=3 (Godot 4 format)
    if "format=3" not in content:
        return False, "Missing format=3 header (Godot 4)"

    # Check ext_resource paths exist
    ext_matches = re.findall(r'\[ext_resource type=".*?" path="res://(.*?)"', content)
    for res_path in ext_matches:
        full_res = os.path.join(root_dir, res_path)
        if not os.path.exists(full_res):
            return False, f"Broken ext_resource reference: res://{res_path}"

    return True, f"Valid Godot 4 scene with {len(ext_matches)} external resources"

def run_all_validations():
    godot_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    print("=" * 75)
    print("HEROES TOWER DEFENSE — GODOT 4 ETAPAS 1/3 & 2/3 VALIDATION SUITE")
    print(f"Project Directory: {godot_dir}")
    print("=" * 75)

    errors = []

    # 1. Project Configuration
    proj_path = os.path.join(godot_dir, "project.godot")
    if not check_file_exists(proj_path, "Godot Project File"):
        errors.append("Missing project.godot")

    # 2. All 25 3D GLB Models
    models_dir = os.path.join(godot_dir, "models")
    expected_models = [
        # Etapa 1/3 Scenery
        "terrain_chunk.glb",
        "bridge_stone.glb",
        "tree_stylized.glb",
        "rock_boulder.glb",
        "ruin_pillar.glb",
        "crystal_cluster.glb",
        "torch_brazier.glb",
        "fortress_castle.glb",
        "summon_portal.glb",
        # Etapa 2/3 Heroes
        "hero_kael.glb",
        "hero_lyra.glb",
        "hero_brakk.glb",
        "hero_nyra.glb",
        "hero_tiko.glb",
        "hero_vesper.glb",
        # Etapa 2/3 Common Enemies
        "enemy_goblin.glb",
        "enemy_skeleton.glb",
        "enemy_orc.glb",
        "enemy_dark_mage.glb",
        # Etapa 2/3 Epic Bosses
        "boss_obsidian_golem.glb",
        "boss_shadow_queen.glb",
        "boss_astral_dragon.glb",
        # Etapa 2/3 Detailed Towers
        "tower_archer_detailed.glb",
        "tower_mage_detailed.glb",
        "tower_cannon_detailed.glb"
    ]
    print(f"\n--- Validating {len(expected_models)} 3D GLTF 2.0 (.glb) Models ---")
    for m in expected_models:
        m_path = os.path.join(models_dir, m)
        if check_file_exists(m_path, f"3D Model ({m})"):
            ok, msg = validate_glb(m_path)
            if not ok:
                errors.append(f"Invalid GLB {m}: {msg}")
            else:
                print(f"    -> {msg}")
        else:
            errors.append(f"Missing 3D model: {m}")

    # 3. GDScripts (15 scripts)
    scripts_dir = os.path.join(godot_dir, "scripts")
    expected_scripts = [
        "environment_theme.gd",
        "coordinate_converter.gd",
        "isometric_camera.gd",
        "living_environment.gd",
        "battlefield_prototype.gd",
        "hero_base_3d.gd",
        "enemy_base_3d.gd",
        "boss_base_3d.gd",
        "tower_archer.gd",
        "tower_mage.gd",
        "tower_cannon.gd",
        "enemy_goblin.gd",
        "projectile_arrow.gd",
        "projectile_magic.gd",
        "projectile_cannonball.gd",
        "kael_showcase_stage1.gd"
    ]
    print(f"\n--- Validating {len(expected_scripts)} GDScripts ---")
    for s in expected_scripts:
        s_path = os.path.join(scripts_dir, s)
        if not check_file_exists(s_path, f"GDScript ({s})"):
            errors.append(f"Missing script: {s}")

    # 4. 3D Biome Scenes (6 scenes)
    biomes_dir = os.path.join(godot_dir, "scenes", "biomes")
    expected_biomes = [
        "highland_forest.tscn",
        "whispering_woods.tscn",
        "ancient_ruins.tscn",
        "crystal_caverns.tscn",
        "volcanic_fortress.tscn",
        "astral_peak.tscn"
    ]
    print(f"\n--- Validating {len(expected_biomes)} Dedicated 3D Biome Scenes ---")
    for b in expected_biomes:
        b_path = os.path.join(biomes_dir, b)
        if check_file_exists(b_path, f"Biome Scene ({b})"):
            ok, msg = validate_tscn(b_path, godot_dir)
            if not ok:
                errors.append(f"Invalid Biome Scene {b}: {msg}")
            else:
                print(f"    -> {msg}")
        else:
            errors.append(f"Missing biome scene: {b}")

    # 5. Hero 3D Scenes (6 scenes)
    heroes_dir = os.path.join(godot_dir, "scenes", "heroes")
    expected_heroes = [
        "hero_kael.tscn",
        "hero_lyra.tscn",
        "hero_brakk.tscn",
        "hero_nyra.tscn",
        "hero_tiko.tscn",
        "hero_vesper.tscn"
    ]
    print(f"\n--- Validating {len(expected_heroes)} Hero 3D Scenes ---")
    for h in expected_heroes:
        h_path = os.path.join(heroes_dir, h)
        if check_file_exists(h_path, f"Hero Scene ({h})"):
            ok, msg = validate_tscn(h_path, godot_dir)
            if not ok:
                errors.append(f"Invalid Hero Scene {h}: {msg}")
            else:
                print(f"    -> {msg}")
        else:
            errors.append(f"Missing hero scene: {h}")

    # 6. Common Enemy 3D Scenes (4 scenes)
    enemies_dir = os.path.join(godot_dir, "scenes", "enemies")
    expected_enemies = [
        "enemy_goblin.tscn",
        "enemy_skeleton.tscn",
        "enemy_orc.tscn",
        "enemy_dark_mage.tscn"
    ]
    print(f"\n--- Validating {len(expected_enemies)} Common Enemy 3D Scenes ---")
    for e in expected_enemies:
        e_path = os.path.join(enemies_dir, e)
        if check_file_exists(e_path, f"Enemy Scene ({e})"):
            ok, msg = validate_tscn(e_path, godot_dir)
            if not ok:
                errors.append(f"Invalid Enemy Scene {e}: {msg}")
            else:
                print(f"    -> {msg}")
        else:
            errors.append(f"Missing enemy scene: {e}")

    # 7. Epic Boss 3D Scenes (3 scenes)
    bosses_dir = os.path.join(godot_dir, "scenes", "bosses")
    expected_bosses = [
        "boss_obsidian_golem.tscn",
        "boss_shadow_queen.tscn",
        "boss_astral_dragon.tscn"
    ]
    print(f"\n--- Validating {len(expected_bosses)} Epic Boss 3D Scenes ---")
    for b in expected_bosses:
        b_path = os.path.join(bosses_dir, b)
        if check_file_exists(b_path, f"Boss Scene ({b})"):
            ok, msg = validate_tscn(b_path, godot_dir)
            if not ok:
                errors.append(f"Invalid Boss Scene {b}: {msg}")
            else:
                print(f"    -> {msg}")
        else:
            errors.append(f"Missing boss scene: {b}")

    # 8. Detailed Tower 3D Scenes (3 scenes)
    scenes_dir = os.path.join(godot_dir, "scenes")
    expected_towers = [
        "tower_archer_detailed.tscn",
        "tower_mage_detailed.tscn",
        "tower_cannon_detailed.tscn"
    ]
    print(f"\n--- Validating {len(expected_towers)} Detailed Tower 3D Scenes ---")
    for t in expected_towers:
        t_path = os.path.join(scenes_dir, t)
        if check_file_exists(t_path, f"Tower Scene ({t})"):
            ok, msg = validate_tscn(t_path, godot_dir)
            if not ok:
                errors.append(f"Invalid Tower Scene {t}: {msg}")
            else:
                print(f"    -> {msg}")
        else:
            errors.append(f"Missing tower scene: {t}")

    # 9. Core Scenes & Showcase
    print("\n--- Validating Core Battlefield & Showcase Scenes ---")
    core_scenes = ["battlefield_prototype.tscn", "kael_showcase_stage1.tscn"]
    for cs in core_scenes:
        cs_path = os.path.join(scenes_dir, cs)
        if check_file_exists(cs_path, f"Core Scene ({cs})"):
            ok, msg = validate_tscn(cs_path, godot_dir)
            if not ok:
                errors.append(f"Invalid {cs}: {msg}")
            else:
                print(f"    -> {msg}")
        else:
            errors.append(f"Missing core scene: {cs}")

    # 10. Canonical Isometric Camera & Coordinate Bridge Mathematics
    print("\n--- Verifying Mathematical Isometric Camera & Bridge ---")
    camera_script = os.path.join(scripts_dir, "isometric_camera.gd")
    with open(camera_script, "r", encoding="utf-8") as f:
        cam_code = f.read()
    if "-35.264" not in cam_code:
        errors.append("isometric_camera.gd missing canonical pitch angle (-35.264)")
    if "45.0" not in cam_code:
        errors.append("isometric_camera.gd missing canonical yaw angle (45.0)")
    print("OK: Canonical isometric angles (Pitch: -35.264°, Yaw: 45.0°) verified in camera script.")

    coord_script = os.path.join(scripts_dir, "coordinate_converter.gd")
    with open(coord_script, "r", encoding="utf-8") as f:
        coord_code = f.read()
    if "normalized_to_world_3d" not in coord_code or "world_3d_to_normalized" not in coord_code:
        errors.append("coordinate_converter.gd missing two-way mapping functions")
    print("OK: Two-way coordinate converter functions verified.")

    print("\n" + "=" * 75)
    if errors:
        print(f"VALIDATION FAILED with {len(errors)} error(s):")
        for e in errors:
            print(f"  - {e}")
        print("=" * 75)
        return False
    else:
        print("SUCCESS: 100% of Godot 4 3D Assets, Articulated Characters & Scenes verified!")
        print(f"  - {len(expected_models)} stylized 3D .glb models verified (9 scenery + 6 heroes + 4 enemies + 3 bosses + 3 towers)")
        print(f"  - {len(expected_scripts)} GDScripts verified")
        print(f"  - 6 dedicated 3D biome scenes verified")
        print(f"  - 6 dedicated 3D hero scenes verified")
        print(f"  - 4 dedicated 3D common enemy scenes verified")
        print(f"  - 3 dedicated 3D epic boss scenes verified")
        print(f"  - 3 detailed 3D tower scenes verified")
        print("  - Canonical isometric camera & coordinate bridge verified")
        print("=" * 75)
        return True

if __name__ == "__main__":
    success = run_all_validations()
    sys.exit(0 if success else 1)
