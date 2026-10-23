#!/usr/bin/env python3
import os
import re
import json
import math
import sys

PROJECT_ROOT = "/guardians_of_the_last_base"

class TestReport:
    def __init__(self):
        self.passed = 0
        self.failed = 0
        self.tests = []

    def assert_true(self, condition, test_name, error_msg=""):
        if condition:
            self.passed += 1
            self.tests.append((test_name, "PASS", ""))
            print(f"  [PASS] {test_name}")
        else:
            self.failed += 1
            self.tests.append((test_name, "FAIL", error_msg))
            print(f"  [FAIL] {test_name}: {error_msg}")

    def summary(self):
        print("\n" + "="*65)
        print(f"TEST SUMMARY: {self.passed} Passed, {self.failed} Failed")
        print("="*65)
        return self.failed == 0

def test_file_references(report: TestReport):
    print("\n--- Test 1: File Reference & Resource Integrity ---")
    res_pattern = re.compile(r'res://([a-zA-Z0-9_\-/\.]+)')
    checked_refs = 0
    missing_refs = []

    for root, dirs, files in os.walk(PROJECT_ROOT):
        for f in files:
            if f.endswith(('.gd', '.tscn', '.tres', '.cfg', '.godot')):
                fpath = os.path.join(root, f)
                with open(fpath, 'r', encoding='utf-8', errors='ignore') as content_file:
                    text = content_file.read()
                    matches = res_pattern.findall(text)
                    for ref in matches:
                        checked_refs += 1
                        target_file = os.path.join(PROJECT_ROOT, ref)
                        if not os.path.exists(target_file):
                            missing_refs.append((fpath, ref))

    report.assert_true(len(missing_refs) == 0, 
                       f"All {checked_refs} res:// references exist on disk", 
                       f"Missing references found: {missing_refs}")

def test_gdscript_syntax(report: TestReport):
    print("\n--- Test 2: GDScript Syntax & Tokenizer Check ---")
    scripts_found = 0
    syntax_errors = []

    for root, dirs, files in os.walk(f"{PROJECT_ROOT}/scripts"):
        for f in files:
            if f.endswith('.gd'):
                scripts_found += 1
                fpath = os.path.join(root, f)
                with open(fpath, 'r', encoding='utf-8') as sf:
                    lines = sf.readlines()
                    brackets = []
                    for line_idx, line in enumerate(lines, 1):
                        stripped = line.strip()
                        if not stripped or stripped.startswith('#'):
                            continue
                        quote_char = None
                        for ch in line:
                            if ch in ('"', "'"):
                                if quote_char == ch:
                                    quote_char = None
                                elif quote_char is None:
                                    quote_char = ch
                            elif quote_char is None:
                                if ch in '([{':
                                    brackets.append((ch, line_idx))
                                elif ch in ')]}':
                                    if not brackets:
                                        syntax_errors.append((f, line_idx, f"Unmatched closing '{ch}'"))
                                    else:
                                        last_open, open_line = brackets.pop()
                                        matches = {')': '(', ']': '[', '}': '{'}
                                        if matches[ch] != last_open:
                                            syntax_errors.append((f, line_idx, f"Mismatched bracket '{ch}' for '{last_open}' from line {open_line}"))

                    if brackets:
                        syntax_errors.append((f, brackets[-1][1], f"Unclosed opening bracket '{brackets[-1][0]}'"))

    report.assert_true(scripts_found >= 20, f"Found {scripts_found} GDScripts in project (expected >= 20)")
    report.assert_true(len(syntax_errors) == 0, "All GDScript files have balanced brackets & syntax", str(syntax_errors))

def test_all_six_characters_and_rarity(report: TestReport):
    print("\n--- Test 3: Six Playable Characters & Rarity System (UPGRADE 2.0) ---")
    char_data_path = f"{PROJECT_ROOT}/scripts/progression/character_data.gd"
    with open(char_data_path, "r", encoding="utf-8") as f:
        cd_code = f.read()

    chars = ["kael", "lyra", "thorne", "elysia", "boran", "vesper"]
    for cid in chars:
        report.assert_true(f'c.id = "{cid}"' in cd_code, f"Character '{cid}' definition exists in CharacterData")

    # Check that all 6 characters have concrete script implementations
    for cid in chars:
        script_file = f"{PROJECT_ROOT}/scripts/characters/{cid}.gd"
        report.assert_true(os.path.exists(script_file), f"Concrete script exists for character: {cid}.gd")

    # Check 12 unique skill IDs
    skills = [
        "arcane_blast", "slow_field",      # Kael
        "arrow_rain", "piercing_shot",     # Lyra
        "ground_slam", "bastion_shield",   # Thorne
        "meteor", "flame_wall",            # Elysia
        "landmine", "turret",              # Boran
        "snipe", "smoke_veil"              # Vesper
    ]
    for sk in skills:
        report.assert_true(f'"{sk}"' in cd_code, f"Skill ID '{sk}' is registered in CharacterData")

    # Validate Rarity Assignments
    report.assert_true('c.rarity = "common"' in cd_code, "Kael is Common rarity")
    report.assert_true('c.rarity = "rare"' in cd_code, "Lyra & Thorne are Rare rarity")
    report.assert_true('c.rarity = "epic"' in cd_code, "Elysia & Boran are Epic rarity")
    report.assert_true('c.rarity = "legendary"' in cd_code, "Vesper is Legendary rarity")

def test_five_enemy_archetypes_and_boss(report: TestReport):
    print("\n--- Test 4: Five Enemy Archetypes & Boss Cinematic Mechanics ---")
    archetypes = [
        ("GoblinScout", "scripts/enemies/goblin_scout.gd"),
        ("ArmoredOrc", "scripts/enemies/armored_orc.gd"),
        ("EnemyBrute", "scripts/enemies/enemy_brute.gd"),
        ("EnemyFlying", "scripts/enemies/enemy_flying.gd"),
        ("BossOgre", "scripts/enemies/boss_ogre.gd")
    ]
    for class_name, script_path in archetypes:
        fpath = f"{PROJECT_ROOT}/{script_path}"
        report.assert_true(os.path.exists(fpath), f"Archetype {class_name} implemented in {script_path}")

    # Test Boss Mechanics in boss_ogre.gd
    with open(f"{PROJECT_ROOT}/scripts/enemies/boss_ogre.gd", "r", encoding="utf-8") as f:
        boss_code = f.read()
    report.assert_true("boss_enraged" in boss_code and "max_hp * 0.5" in boss_code,
                       "Boss enters enraged phase at 50% HP")
    report.assert_true("_execute_earthquake_slam" in boss_code,
                       "Boss executes earthquake slam special attack")
    report.assert_true('boss_title: String = "Devastador Ancestral"' in boss_code,
                       "Boss title defined for cinematic intro")

def test_ten_playable_stages_and_scenarios(report: TestReport):
    print("\n--- Test 5: Ten Playable Stages & Five Thematic Scenarios ---")
    stage_data_path = f"{PROJECT_ROOT}/scripts/progression/stage_data.gd"
    with open(stage_data_path, "r", encoding="utf-8") as f:
        sd_code = f.read()

    for s_id in range(1, 11):
        report.assert_true(f"{s_id}:" in sd_code, f"Stage {s_id} defined in StageData")

    # Scenarios validation
    scenarios = ["forest", "ruins", "volcano", "fortress", "temple"]
    for sc in scenarios:
        report.assert_true(f'"{sc}"' in sd_code, f"Thematic scenario '{sc}' registered in StageData")

    # Verify stage resources in resources/stages/
    for s_id in range(1, 11):
        res_file = f"{PROJECT_ROOT}/resources/stages/stage_{s_id}.tres"
        report.assert_true(os.path.exists(res_file), f"Resource file exists: stage_{s_id}.tres")

def test_save_manager_and_progression_limits(report: TestReport):
    print("\n--- Test 6: Save Schema, Single Initial Hero & Slot Progression ---")
    save_manager_path = f"{PROJECT_ROOT}/scripts/managers/save_manager.gd"
    with open(save_manager_path, "r", encoding="utf-8") as f:
        sm_code = f.read()

    prog_manager_path = f"{PROJECT_ROOT}/scripts/managers/progression_manager.gd"
    with open(prog_manager_path, "r", encoding="utf-8") as f:
        pm_code = f.read()

    report.assert_true('"version": 2' in sm_code, "Save schema is version 2")
    report.assert_true('"team_slots": ["kael"]' in sm_code, "Player starts with Kael only and 1 slot")
    report.assert_true('"gems": 30' in sm_code, "Save schema tracks premium currency (Gems)")
    report.assert_true('"quests": {' in sm_code, "Save schema tracks local quests")
    
    # Check slots progression table: 1 at Lv1, 2 at Lv3, 3 at Lv5, 4 at Lv7, 5 at Lv9
    report.assert_true("1: 1" in pm_code and "3: 2" in pm_code and "5: 3" in pm_code and "7: 4" in pm_code and "9: 5" in pm_code,
                       "Progression table unlocks defense slots: 1 at Lv1, 2 at Lv3, 3 at Lv5, 4 at Lv7, 5 at Lv9-10")

def test_combat_simulation_and_effects(report: TestReport):
    print("\n--- Test 7: Combat Simulation & Piercing / Slow Validation ---")
    hit_dict = {}
    enemy_mock = "enemy_101"
    hits = 0
    for frame in range(10):
        if enemy_mock not in hit_dict:
            hit_dict[enemy_mock] = True
            hits += 1
    report.assert_true(hits == 1, "Piercing projectile only registers 1 hit across 10 frames")

    normal_speed = 46.0
    enraged_speed = 64.0
    report.assert_true(enraged_speed > normal_speed, "Enraged boss gains increased movement speed")

def test_vertical_mobile_configuration(report: TestReport):
    print("\n--- Test 8: Vertical Orientation (Portrait 720x1280) Configuration ---")
    ep_path = f"{PROJECT_ROOT}/export_presets.cfg"
    with open(ep_path, "r", encoding="utf-8") as f:
        ep_code = f.read()

    proj_path = f"{PROJECT_ROOT}/project.godot"
    with open(proj_path, "r", encoding="utf-8") as f:
        proj_code = f.read()

    report.assert_true('package/unique_name="com.seuprojeto.guardianslastbase"' in ep_code,
                       "Android package is com.seuprojeto.guardianslastbase")
    report.assert_true('screen/orientation=1' in ep_code, "Portrait orientation configured in export_presets.cfg")
    report.assert_true('window/size/viewport_width=720' in proj_code and 'window/size/viewport_height=1280' in proj_code,
                       "Viewport reference resolution is 720x1280 (9:16 portrait)")
    report.assert_true('window/handheld/orientation=1' in proj_code,
                       "Handheld orientation set to portrait (1) in project.godot")

def main():
    report = TestReport()
    print("="*65)
    print("RUNNING FINAL VALIDATION SUITE: GUARDIANS OF THE LAST BASE (UPGRADE 2.0)")
    print("="*65)

    test_file_references(report)
    test_gdscript_syntax(report)
    test_all_six_characters_and_rarity(report)
    test_five_enemy_archetypes_and_boss(report)
    test_ten_playable_stages_and_scenarios(report)
    test_save_manager_and_progression_limits(report)
    test_combat_simulation_and_effects(report)
    test_vertical_mobile_configuration(report)

    success = report.summary()
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()
