#!/usr/bin/env python3
import os

RES_DIR = "/guardians_of_the_last_base/resources"

def generate_resources():
    os.makedirs(f"{RES_DIR}/characters", exist_ok=True)
    os.makedirs(f"{RES_DIR}/stages", exist_ok=True)
    os.makedirs(f"{RES_DIR}/upgrades", exist_ok=True)

    chars = [
        ("kael", "Kael", "Guardião Arcano", "Dano Mágico & Controle", 28.0, 1.0, 230.0, 75.0, "kael_avatar.svg", "kael_sprite.svg"),
        ("lyra", "Lyra", "Arqueira das Sombras", "Ataque Rápido & Alvos Prioritários", 18.0, 1.55, 270.0, 50.0, "lyra_avatar.svg", "lyra_sprite.svg"),
        ("thorne", "Thorne", "Guerreiro Protetor", "Dano Contundente & Atordoamento", 38.0, 0.85, 190.0, 90.0, "thorne_avatar.svg", "thorne_sprite.svg"),
        ("elysia", "Elysia", "Maga Elemental", "Dano Contínuo & Incêndio", 24.0, 1.15, 240.0, 80.0, "elysia_avatar.svg", "elysia_sprite.svg"),
        ("boran", "Boran", "Engenheiro de Armadilhas", "Armadilhas & Engenharia Tática", 22.0, 1.35, 210.0, 85.0, "boran_avatar.svg", "boran_sprite.svg"),
        ("vesper", "Vesper", "Assassino de Longo Alcance", "Execução de Chefes & Críticos", 48.0, 0.70, 350.0, 160.0, "vesper_avatar.svg", "vesper_sprite.svg")
    ]

    for cid, name, title, role, dmg, spd, rng, sk_dmg, av, sp in chars:
        with open(f"{RES_DIR}/characters/{cid}.tres", "w", encoding="utf-8") as f:
            f.write(f"""[gd_resource type="Resource" format=3 uid="uid://{cid}res00"]

[resource]
metadata/character_id = "{cid}"
metadata/name = "{name}"
metadata/title = "{title}"
metadata/role = "{role}"
metadata/base_damage = {dmg}
metadata/base_attack_speed = {spd}
metadata/base_range = {rng}
metadata/base_skill_damage = {sk_dmg}
metadata/avatar_path = "res://assets/icons/{av}"
metadata/sprite_path = "res://assets/sprites/{sp}"
""")

    stages = [
        (1, "Encruzilhada da Coragem", 5, 140, 50),
        (2, "Trilha dos Espinheiros", 5, 180, 70),
        (3, "Passo Rochoso", 6, 230, 90),
        (4, "Pântano das Brumas", 6, 290, 115),
        (5, "Ruínas da Fortaleza Antiga", 7, 360, 145),
        (6, "Desfiladeiro dos Ventos", 7, 440, 180),
        (7, "Minas Esquecidas", 8, 530, 220),
        (8, "Bosque das Sombras", 8, 640, 270),
        (9, "Portão da Cidadela Caída", 9, 770, 330),
        (10, "Covil do Ogro Ancestral", 10, 1100, 500)
    ]

    for sid, title, waves_cnt, gold, exp in stages:
        with open(f"{RES_DIR}/stages/stage_{sid}.tres", "w", encoding="utf-8") as f:
            f.write(f"""[gd_resource type="Resource" format=3 uid="uid://stg{sid}res00"]

[resource]
metadata/stage_id = {sid}
metadata/title = "{title}"
metadata/waves_count = {waves_cnt}
metadata/reward_coins = {gold}
metadata/reward_exp = {exp}
metadata/scene_path = "res://scenes/combat/combat_stage.tscn"
""")

    print("All 6 character and 10 stage resources created!")

if __name__ == "__main__":
    generate_resources()
