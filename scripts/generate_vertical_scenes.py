#!/usr/bin/env python3
import os

SCENES_DIR = "/guardians_of_the_last_base/scenes"

def generate_vertical_combat_scene():
    content = """[gd_scene load_steps=5 format=3 uid="uid://c8ycombat001"]

[ext_resource type="Script" path="res://scripts/combat/combat_manager.gd" id="1_combat_mgr"]
[ext_resource type="Script" path="res://scripts/ui/combat_ui.gd" id="2_combat_ui"]
[ext_resource type="Texture2D" path="res://assets/sprites/base_fortress.svg" id="3_base_fort"]
[ext_resource type="Texture2D" path="res://assets/icons/coin.svg" id="4_coin_icon"]

[sub_resource type="Curve2D" id="Curve2D_path"]
_data = {
"points": PackedVector2Array(0, 0, 0, 0, 360, 130, -40, -20, 40, 20, 200, 260, 40, -20, -40, 20, 520, 390, -40, -20, 40, 20, 200, 530, 40, -20, -40, 20, 520, 680, 0, -30, 0, 30, 360, 840, 0, 0, 0, 0, 360, 930)
}

[node name="CombatStage" type="Node2D"]
script = ExtResource("1_combat_mgr")

[node name="Background" type="ColorRect" parent="."]
offset_right = 720.0
offset_bottom = 1280.0
color = Color(0.0666667, 0.0823529, 0.129412, 1)

[node name="BattlefieldFloor" type="ColorRect" parent="."]
offset_left = 12.0
offset_top = 105.0
offset_right = 708.0
offset_bottom = 1000.0
color = Color(0.109804, 0.141176, 0.196078, 1)

[node name="RoadVisual" type="Line2D" parent="."]
points = PackedVector2Array(360, 130, 200, 260, 520, 390, 200, 530, 520, 680, 360, 840, 360, 930)
width = 46.0
default_color = Color(0.231373, 0.282353, 0.380392, 1)
joint_mode = 2
begin_cap_mode = 2
end_cap_mode = 2

[node name="RoadBorder" type="Line2D" parent="."]
points = PackedVector2Array(360, 130, 200, 260, 520, 390, 200, 530, 520, 680, 360, 840, 360, 930)
width = 50.0
default_color = Color(0.337255, 0.415686, 0.541176, 0.5)
joint_mode = 2
begin_cap_mode = 2
end_cap_mode = 2

[node name="SpawnPortal" type="Node2D" parent="."]
position = Vector2(360, 130)

[node name="PortalOuter" type="ColorRect" parent="SpawnPortal"]
offset_left = -28.0
offset_top = -18.0
offset_right = 28.0
offset_bottom = 18.0
color = Color(0.6, 0.1, 0.7, 0.8)

[node name="PortalInner" type="ColorRect" parent="SpawnPortal"]
offset_left = -20.0
offset_top = -12.0
offset_right = 20.0
offset_bottom = 12.0
color = Color(0.9, 0.4, 1.0, 1)

[node name="BaseFortress" type="Node2D" parent="."]
position = Vector2(360, 930)

[node name="Sprite2D" type="Sprite2D" parent="BaseFortress"]
position = Vector2(0, -10)
scale = Vector2(1.1, 1.1)
texture = ExtResource("3_base_fort")

[node name="DefenseSlotMarkers" type="Node2D" parent="."]

[node name="Slot0" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 340.0
offset_top = 240.0
offset_right = 380.0
offset_bottom = 280.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="Slot1" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 340.0
offset_top = 410.0
offset_right = 380.0
offset_bottom = 450.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="Slot2" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 340.0
offset_top = 580.0
offset_right = 380.0
offset_bottom = 620.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="Slot3" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 140.0
offset_top = 430.0
offset_right = 180.0
offset_bottom = 470.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="Slot4" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 540.0
offset_top = 590.0
offset_right = 580.0
offset_bottom = 630.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="Slot5" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 340.0
offset_top = 750.0
offset_right = 380.0
offset_bottom = 790.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="EnemyPath" type="Path2D" parent="."]
curve = SubResource("Curve2D_path")

[node name="DefendersContainer" type="Node2D" parent="."]

[node name="EffectsContainer" type="Node2D" parent="."]

[node name="ProjectilesContainer" type="Node2D" parent="."]

[node name="CombatUI" type="CanvasLayer" parent="."]
script = ExtResource("2_combat_ui")

[node name="TopBar" type="Panel" parent="CombatUI"]
anchors_preset = 10
anchor_right = 1.0
offset_bottom = 96.0
grow_horizontal = 2

[node name="VBox" type="VBoxContainer" parent="CombatUI/TopBar"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_left = 14.0
offset_top = 6.0
offset_right = -14.0
offset_bottom = -6.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 4

[node name="HBoxRow1" type="HBoxContainer" parent="CombatUI/TopBar/VBox"]
layout_mode = 2
theme_override_constants/separation = 12

[node name="BaseHPContainer" type="HBoxContainer" parent="CombatUI/TopBar/VBox/HBoxRow1"]
layout_mode = 2
theme_override_constants/separation = 6

[node name="IconLabel" type="Label" parent="CombatUI/TopBar/VBox/HBoxRow1/BaseHPContainer"]
layout_mode = 2
theme_override_font_sizes/font_size = 14
text = "Fortaleza:"

[node name="HPBar" type="ProgressBar" parent="CombatUI/TopBar/VBox/HBoxRow1/BaseHPContainer"]
custom_minimum_size = Vector2(140, 20)
layout_mode = 2
size_flags_vertical = 4
max_value = 100.0
value = 100.0
show_percentage = false

[node name="HPLabel" type="Label" parent="CombatUI/TopBar/VBox/HBoxRow1/BaseHPContainer"]
layout_mode = 2
theme_override_font_sizes/font_size = 13
text = "100/100"

[node name="CoinsContainer" type="HBoxContainer" parent="CombatUI/TopBar/VBox/HBoxRow1"]
layout_mode = 2
size_flags_horizontal = 10
theme_override_constants/separation = 4

[node name="CoinIcon" type="TextureRect" parent="CombatUI/TopBar/VBox/HBoxRow1/CoinsContainer"]
custom_minimum_size = Vector2(24, 24)
layout_mode = 2
size_flags_vertical = 4
texture = ExtResource("4_coin_icon")
expand_mode = 1

[node name="CoinsLabel" type="Label" parent="CombatUI/TopBar/VBox/HBoxRow1/CoinsContainer"]
layout_mode = 2
theme_override_colors/font_color = Color(1, 0.9, 0.3, 1)
theme_override_font_sizes/font_size = 14
text = "350"

[node name="ControlsContainer" type="HBoxContainer" parent="CombatUI/TopBar/VBox/HBoxRow1"]
layout_mode = 2
theme_override_constants/separation = 6

[node name="SpeedButton" type="Button" parent="CombatUI/TopBar/VBox/HBoxRow1/ControlsContainer"]
custom_minimum_size = Vector2(48, 36)
layout_mode = 2
size_flags_vertical = 4
theme_override_font_sizes/font_size = 13
text = "1x"

[node name="PauseButton" type="Button" parent="CombatUI/TopBar/VBox/HBoxRow1/ControlsContainer"]
custom_minimum_size = Vector2(62, 36)
layout_mode = 2
size_flags_vertical = 4
theme_override_font_sizes/font_size = 13
text = "Pausar"

[node name="HBoxRow2" type="HBoxContainer" parent="CombatUI/TopBar/VBox"]
layout_mode = 2
theme_override_constants/separation = 16

[node name="WaveContainer" type="HBoxContainer" parent="CombatUI/TopBar/VBox/HBoxRow2"]
layout_mode = 2

[node name="WaveLabel" type="Label" parent="CombatUI/TopBar/VBox/HBoxRow2/WaveContainer"]
layout_mode = 2
theme_override_colors/font_color = Color(0.9, 0.8, 0.3, 1)
theme_override_font_sizes/font_size = 14
text = "Onda 1 / 5"

[node name="EnemiesContainer" type="HBoxContainer" parent="CombatUI/TopBar/VBox/HBoxRow2"]
layout_mode = 2
size_flags_horizontal = 10

[node name="EnemiesLabel" type="Label" parent="CombatUI/TopBar/VBox/HBoxRow2/EnemiesContainer"]
layout_mode = 2
theme_override_colors/font_color = Color(0.8, 0.85, 0.9, 1)
theme_override_font_sizes/font_size = 13
text = "Inimigos: 0"

[node name="BottomBar" type="Panel" parent="CombatUI"]
anchors_preset = 12
anchor_top = 1.0
anchor_right = 1.0
anchor_bottom = 1.0
offset_top = -130.0
grow_horizontal = 2
grow_vertical = 0

[node name="SkillsContainer" type="HBoxContainer" parent="CombatUI/BottomBar"]
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -340.0
offset_top = -55.0
offset_right = 340.0
offset_bottom = 55.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 16
alignment = 1

[node name="Modals" type="Control" parent="CombatUI"]
layout_mode = 3
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
mouse_filter = 2

[node name="PauseModal" type="ColorRect" parent="CombatUI/Modals"]
visible = false
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
color = Color(0, 0, 0, 0.75)

[node name="Panel" type="Panel" parent="CombatUI/Modals/PauseModal"]
custom_minimum_size = Vector2(340, 260)
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -170.0
offset_top = -130.0
offset_right = 170.0
offset_bottom = 130.0
grow_horizontal = 2
grow_vertical = 2

[node name="VBox" type="VBoxContainer" parent="CombatUI/Modals/PauseModal/Panel"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 16
alignment = 1

[node name="Title" type="Label" parent="CombatUI/Modals/PauseModal/Panel/VBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 24
text = "Batalha Pausada"
horizontal_alignment = 1

[node name="ResumeButton" type="Button" parent="CombatUI/Modals/PauseModal/Panel/VBox"]
custom_minimum_size = Vector2(220, 46)
layout_mode = 2
size_flags_horizontal = 4
text = "Continuar Batalha"

[node name="RestartButton" type="Button" parent="CombatUI/Modals/PauseModal/Panel/VBox"]
custom_minimum_size = Vector2(220, 46)
layout_mode = 2
size_flags_horizontal = 4
text = "Reiniciar Fase"

[node name="QuitButton" type="Button" parent="CombatUI/Modals/PauseModal/Panel/VBox"]
custom_minimum_size = Vector2(220, 46)
layout_mode = 2
size_flags_horizontal = 4
text = "Voltar ao Menu"

[node name="VictoryModal" type="ColorRect" parent="CombatUI/Modals"]
visible = false
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
color = Color(0, 0, 0, 0.8)

[node name="Panel" type="Panel" parent="CombatUI/Modals/VictoryModal"]
custom_minimum_size = Vector2(340, 340)
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -170.0
offset_top = -170.0
offset_right = 170.0
offset_bottom = 170.0
grow_horizontal = 2
grow_vertical = 2

[node name="VBox" type="VBoxContainer" parent="CombatUI/Modals/VictoryModal/Panel"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 12
alignment = 1

[node name="Title" type="Label" parent="CombatUI/Modals/VictoryModal/Panel/VBox"]
layout_mode = 2
theme_override_colors/font_color = Color(1, 0.843137, 0, 1)
theme_override_font_sizes/font_size = 30
text = "VITÓRIA!"
horizontal_alignment = 1

[node name="SubTitle" type="Label" parent="CombatUI/Modals/VictoryModal/Panel/VBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 14
text = "A base foi protegida com sucesso!"
horizontal_alignment = 1

[node name="Rewards" type="Label" parent="CombatUI/Modals/VictoryModal/Panel/VBox"]
layout_mode = 2
theme_override_colors/font_color = Color(0.4, 1, 0.4, 1)
theme_override_font_sizes/font_size = 15
text = "+140 Ouro  |  +5 Gemas  |  +50 EXP"
horizontal_alignment = 1

[node name="NextStageButton" type="Button" parent="CombatUI/Modals/VictoryModal/Panel/VBox"]
custom_minimum_size = Vector2(220, 46)
layout_mode = 2
size_flags_horizontal = 4
text = "Próxima Fase"

[node name="ReplayButton" type="Button" parent="CombatUI/Modals/VictoryModal/Panel/VBox"]
custom_minimum_size = Vector2(220, 46)
layout_mode = 2
size_flags_horizontal = 4
text = "Repetir Fase"

[node name="ContinueButton" type="Button" parent="CombatUI/Modals/VictoryModal/Panel/VBox"]
custom_minimum_size = Vector2(220, 46)
layout_mode = 2
size_flags_horizontal = 4
text = "Voltar ao Menu"

[node name="DefeatModal" type="ColorRect" parent="CombatUI/Modals"]
visible = false
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
color = Color(0.1, 0, 0, 0.85)

[node name="Panel" type="Panel" parent="CombatUI/Modals/DefeatModal"]
custom_minimum_size = Vector2(340, 280)
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -170.0
offset_top = -140.0
offset_right = 170.0
offset_bottom = 140.0
grow_horizontal = 2
grow_vertical = 2

[node name="VBox" type="VBoxContainer" parent="CombatUI/Modals/DefeatModal/Panel"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 16
alignment = 1

[node name="Title" type="Label" parent="CombatUI/Modals/DefeatModal/Panel/VBox"]
layout_mode = 2
theme_override_colors/font_color = Color(1, 0.2, 0.2, 1)
theme_override_font_sizes/font_size = 30
text = "DERROTA"
horizontal_alignment = 1

[node name="SubTitle" type="Label" parent="CombatUI/Modals/DefeatModal/Panel/VBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 14
text = "A Fortaleza sucumbiu aos invasores."
horizontal_alignment = 1

[node name="RetryButton" type="Button" parent="CombatUI/Modals/DefeatModal/Panel/VBox"]
custom_minimum_size = Vector2(220, 48)
layout_mode = 2
size_flags_horizontal = 4
text = "Tentar Novamente"

[node name="QuitButton" type="Button" parent="CombatUI/Modals/DefeatModal/Panel/VBox"]
custom_minimum_size = Vector2(220, 48)
layout_mode = 2
size_flags_horizontal = 4
text = "Voltar ao Menu Principal"
"""
    path = os.path.join(SCENES_DIR, "combat/combat_stage.tscn")
    with open(path, "w", encoding="utf-8") as f:
        f.write(content.strip())
    print("Generated vertical: scenes/combat/combat_stage.tscn")

def generate_vertical_main_menu_scene():
    content = """[gd_scene load_steps=8 format=3 uid="uid://m0mainmen001"]

[ext_resource type="Script" path="res://scripts/ui/main_menu.gd" id="1_main_menu"]
[ext_resource type="Script" path="res://scripts/ui/character_roster_ui.gd" id="2_roster_ui"]
[ext_resource type="Script" path="res://scripts/ui/base_upgrade_ui.gd" id="3_base_ui"]
[ext_resource type="Texture2D" path="res://assets/icons/coin.svg" id="4_coin_icon"]
[ext_resource type="Texture2D" path="res://assets/icons/gem.svg" id="5_gem_icon"]
[ext_resource type="Texture2D" path="res://assets/icons/kael_avatar.svg" id="6_def_avatar"]
[ext_resource type="Texture2D" path="res://assets/icons/settings.svg" id="7_settings_icon"]

[node name="MainMenu" type="Control"]
layout_mode = 3
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
script = ExtResource("1_main_menu")

[node name="Background" type="ColorRect" parent="."]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
color = Color(0.0666667, 0.0823529, 0.129412, 1)

[node name="TopHeader" type="Panel" parent="."]
custom_minimum_size = Vector2(0, 84)
layout_mode = 1
anchors_preset = 10
anchor_right = 1.0
offset_bottom = 84.0
grow_horizontal = 2

[node name="HBox" type="HBoxContainer" parent="TopHeader"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_left = 16.0
offset_right = -16.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 12

[node name="BaseContainer" type="VBoxContainer" parent="TopHeader/HBox"]
layout_mode = 2
alignment = 1

[node name="BaseLevelLabel" type="Label" parent="TopHeader/HBox/BaseContainer"]
layout_mode = 2
theme_override_colors/font_color = Color(0.96, 0.82, 0.4, 1)
theme_override_font_sizes/font_size = 15
text = "Base Nível 1"

[node name="ExpBar" type="ProgressBar" parent="TopHeader/HBox/BaseContainer"]
custom_minimum_size = Vector2(130, 14)
layout_mode = 2
max_value = 100.0
value = 0.0
show_percentage = false

[node name="CoinsContainer" type="HBoxContainer" parent="TopHeader/HBox"]
layout_mode = 2
size_flags_horizontal = 10
theme_override_constants/separation = 4

[node name="CoinIcon" type="TextureRect" parent="TopHeader/HBox/CoinsContainer"]
custom_minimum_size = Vector2(26, 26)
layout_mode = 2
size_flags_vertical = 4
texture = ExtResource("4_coin_icon")
expand_mode = 1

[node name="CoinsLabel" type="Label" parent="TopHeader/HBox/CoinsContainer"]
layout_mode = 2
theme_override_colors/font_color = Color(1, 0.9, 0.3, 1)
theme_override_font_sizes/font_size = 15
text = "350"

[node name="GemsContainer" type="HBoxContainer" parent="TopHeader/HBox"]
layout_mode = 2
theme_override_constants/separation = 4

[node name="GemIcon" type="TextureRect" parent="TopHeader/HBox/GemsContainer"]
custom_minimum_size = Vector2(24, 24)
layout_mode = 2
size_flags_vertical = 4
texture = ExtResource("5_gem_icon")
expand_mode = 1

[node name="GemsLabel" type="Label" parent="TopHeader/HBox/GemsContainer"]
layout_mode = 2
theme_override_colors/font_color = Color(0.3, 0.9, 1, 1)
theme_override_font_sizes/font_size = 15
text = "30"

[node name="SettingsButton" type="Button" parent="TopHeader/HBox"]
custom_minimum_size = Vector2(44, 44)
layout_mode = 2
size_flags_vertical = 4
icon = ExtResource("7_settings_icon")
expand_icon = true

[node name="CenterMenu" type="Control" parent="."]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_top = 84.0
grow_horizontal = 2
grow_vertical = 2

[node name="HeroShowcase" type="VBoxContainer" parent="CenterMenu"]
layout_mode = 1
anchors_preset = 5
anchor_left = 0.5
anchor_right = 0.5
offset_left = -160.0
offset_top = 24.0
offset_right = 160.0
offset_bottom = 340.0
grow_horizontal = 2
theme_override_constants/separation = 8
alignment = 1

[node name="AvatarRect" type="TextureRect" parent="CenterMenu/HeroShowcase"]
custom_minimum_size = Vector2(160, 160)
layout_mode = 2
size_flags_horizontal = 4
texture = ExtResource("6_def_avatar")
expand_mode = 1
stretch_mode = 5

[node name="HeroName" type="Label" parent="CenterMenu/HeroShowcase"]
layout_mode = 2
theme_override_colors/font_color = Color(1, 0.95, 0.8, 1)
theme_override_font_sizes/font_size = 26
text = "Kael"
horizontal_alignment = 1

[node name="HeroTitle" type="Label" parent="CenterMenu/HeroShowcase"]
layout_mode = 2
theme_override_colors/font_color = Color(0.7, 0.8, 0.95, 1)
theme_override_font_sizes/font_size = 15
text = "Guardião Arcano"
horizontal_alignment = 1

[node name="RarityBadge" type="Label" parent="CenterMenu/HeroShowcase"]
layout_mode = 2
theme_override_colors/font_color = Color(0.75, 0.8, 0.85, 1)
theme_override_font_sizes/font_size = 14
text = "✦ COMUM ✦"
horizontal_alignment = 1

[node name="VBox" type="VBoxContainer" parent="CenterMenu"]
layout_mode = 1
anchors_preset = 7
anchor_left = 0.5
anchor_top = 1.0
anchor_right = 0.5
anchor_bottom = 1.0
offset_left = -180.0
offset_top = -430.0
offset_right = 180.0
offset_bottom = -30.0
grow_horizontal = 2
grow_vertical = 0
theme_override_constants/separation = 14
alignment = 1

[node name="PlayButton" type="Button" parent="CenterMenu/VBox"]
custom_minimum_size = Vector2(0, 64)
layout_mode = 2
theme_override_font_sizes/font_size = 22
text = "⚔️ INICIAR BATALHA"

[node name="StagesButton" type="Button" parent="CenterMenu/VBox"]
custom_minimum_size = Vector2(0, 52)
layout_mode = 2
theme_override_font_sizes/font_size = 17
text = "🗺️ FASES DA CAMPANHA (1-10)"

[node name="CharactersButton" type="Button" parent="CenterMenu/VBox"]
custom_minimum_size = Vector2(0, 52)
layout_mode = 2
theme_override_font_sizes/font_size = 17
text = "🛡️ HERÓIS & LOJA"

[node name="BaseUpgradeButton" type="Button" parent="CenterMenu/VBox"]
custom_minimum_size = Vector2(0, 52)
layout_mode = 2
theme_override_font_sizes/font_size = 17
text = "🏛️ MELHORIAS DA BASE"

[node name="QuestsButton" type="Button" parent="CenterMenu/VBox"]
custom_minimum_size = Vector2(0, 52)
layout_mode = 2
theme_override_font_sizes/font_size = 17
text = "📜 MISSÕES LOCAIS"

[node name="SubScreens" type="Control" parent="."]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
mouse_filter = 2

[node name="CharacterRosterScreen" type="Control" parent="SubScreens"]
visible = false
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
script = ExtResource("2_roster_ui")

[node name="Bg" type="ColorRect" parent="SubScreens/CharacterRosterScreen"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
color = Color(0.08, 0.1, 0.14, 0.98)

[node name="Margin" type="MarginContainer" parent="SubScreens/CharacterRosterScreen"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/margin_left = 16
theme_override_constants/margin_top = 16
theme_override_constants/margin_right = 16
theme_override_constants/margin_bottom = 16

[node name="VBox" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin"]
layout_mode = 2
theme_override_constants/separation = 10

[node name="Header" type="HBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox"]
layout_mode = 2

[node name="BackButton" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/VBox/Header"]
custom_minimum_size = Vector2(100, 42)
layout_mode = 2
text = "← Voltar"

[node name="Title" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/Header"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_font_sizes/font_size = 19
text = "Coleção & Loja de Heróis"
horizontal_alignment = 1

[node name="FilterBar" type="HBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox"]
layout_mode = 2
theme_override_constants/separation = 6
alignment = 1

[node name="FilterAll" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/VBox/FilterBar"]
custom_minimum_size = Vector2(70, 36)
layout_mode = 2
theme_override_font_sizes/font_size = 12
text = "Todos"

[node name="FilterCommon" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/VBox/FilterBar"]
custom_minimum_size = Vector2(74, 36)
layout_mode = 2
theme_override_font_sizes/font_size = 12
text = "Comum"

[node name="FilterRare" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/VBox/FilterBar"]
custom_minimum_size = Vector2(70, 36)
layout_mode = 2
theme_override_font_sizes/font_size = 12
text = "Raro"

[node name="FilterEpic" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/VBox/FilterBar"]
custom_minimum_size = Vector2(70, 36)
layout_mode = 2
theme_override_font_sizes/font_size = 12
text = "Épico"

[node name="FilterLegendary" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/VBox/FilterBar"]
custom_minimum_size = Vector2(80, 36)
layout_mode = 2
theme_override_font_sizes/font_size = 12
text = "Lendário"

[node name="Scroll" type="ScrollContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox"]
custom_minimum_size = Vector2(0, 240)
layout_mode = 2
size_flags_vertical = 3

[node name="CharList" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox/Scroll"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_constants/separation = 6

[node name="DetailPanel" type="PanelContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox"]
layout_mode = 2

[node name="Header" type="HBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel"]
layout_mode = 2
theme_override_constants/separation = 12

[node name="AvatarRect" type="TextureRect" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/Header"]
custom_minimum_size = Vector2(70, 70)
layout_mode = 2
expand_mode = 1
stretch_mode = 5

[node name="InfoVBox" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/Header"]
layout_mode = 2
size_flags_horizontal = 3

[node name="NameLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/Header/InfoVBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 17
text = "Kael"

[node name="TitleLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/Header/InfoVBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 12
text = "Guardião Arcano"

[node name="RarityLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/Header/InfoVBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 12
text = "[ COMUM ]"

[node name="RoleLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/Header/InfoVBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 11
text = "Função: Mago"

[node name="DescriptionLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel"]
visible = false
layout_mode = 2
text = ""

[node name="StatsGrid" type="GridContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel"]
visible = false
layout_mode = 2
columns = 2

[node name="LevelLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/StatsGrid"]
layout_mode = 2
text = ""

[node name="DamageLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/StatsGrid"]
layout_mode = 2
text = ""

[node name="SpeedLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/StatsGrid"]
layout_mode = 2
text = ""

[node name="RangeLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/StatsGrid"]
layout_mode = 2
text = ""

[node name="SkillDamageLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/StatsGrid"]
layout_mode = 2
text = ""

[node name="SkillsBox" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel"]
visible = false
layout_mode = 2

[node name="Skill1" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/SkillsBox"]
layout_mode = 2

[node name="Name" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/SkillsBox/Skill1"]
layout_mode = 2
text = ""

[node name="Desc" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/SkillsBox/Skill1"]
layout_mode = 2
text = ""

[node name="Skill2" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/SkillsBox"]
layout_mode = 2

[node name="Name" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/SkillsBox/Skill2"]
layout_mode = 2
text = ""

[node name="Desc" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/SkillsBox/Skill2"]
layout_mode = 2
text = ""

[node name="Actions" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel"]
layout_mode = 2
theme_override_constants/separation = 6

[node name="SlotsLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/Actions"]
layout_mode = 2
theme_override_font_sizes/font_size = 12
text = "Espaços: 1 / 1"
horizontal_alignment = 1

[node name="UpgradeButton" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/Actions"]
custom_minimum_size = Vector2(0, 44)
layout_mode = 2
text = "Melhorar Nível"

[node name="EquipButton" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/Actions"]
custom_minimum_size = Vector2(0, 44)
layout_mode = 2
text = "Equipar na Defesa"

[node name="UnlockButton" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel/Actions"]
custom_minimum_size = Vector2(0, 44)
layout_mode = 2
text = "Recrutar"

[node name="FeedbackLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/VBox/DetailPanel"]
layout_mode = 2
theme_override_font_sizes/font_size = 12
horizontal_alignment = 1

[node name="BaseUpgradeScreen" type="Control" parent="SubScreens"]
visible = false
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
script = ExtResource("3_base_ui")

[node name="Bg" type="ColorRect" parent="SubScreens/BaseUpgradeScreen"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
color = Color(0.08, 0.1, 0.14, 0.98)

[node name="Margin" type="MarginContainer" parent="SubScreens/BaseUpgradeScreen"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/margin_left = 16
theme_override_constants/margin_top = 16
theme_override_constants/margin_right = 16
theme_override_constants/margin_bottom = 16

[node name="VBox" type="VBoxContainer" parent="SubScreens/BaseUpgradeScreen/Margin"]
layout_mode = 2
theme_override_constants/separation = 12

[node name="TopBar" type="HBoxContainer" parent="SubScreens/BaseUpgradeScreen/Margin/VBox"]
layout_mode = 2

[node name="CloseButton" type="Button" parent="SubScreens/BaseUpgradeScreen/Margin/VBox/TopBar"]
custom_minimum_size = Vector2(100, 42)
layout_mode = 2
text = "← Voltar"

[node name="Title" type="Label" parent="SubScreens/BaseUpgradeScreen/Margin/VBox/TopBar"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_font_sizes/font_size = 19
text = "Evolução da Base Central"
horizontal_alignment = 1

[node name="CoinsLabel" type="Label" parent="SubScreens/BaseUpgradeScreen/Margin/VBox/TopBar"]
layout_mode = 2
theme_override_colors/font_color = Color(1, 0.9, 0.3, 1)
theme_override_font_sizes/font_size = 15
text = "350 Ouro"

[node name="Scroll" type="ScrollContainer" parent="SubScreens/BaseUpgradeScreen/Margin/VBox"]
layout_mode = 2
size_flags_vertical = 3

[node name="StructuresList" type="VBoxContainer" parent="SubScreens/BaseUpgradeScreen/Margin/VBox/Scroll"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_constants/separation = 12

[node name="FeedbackLabel" type="Label" parent="SubScreens/BaseUpgradeScreen/Margin/VBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 13
horizontal_alignment = 1

[node name="Modals" type="Control" parent="."]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
mouse_filter = 2

[node name="StageSelectModal" type="ColorRect" parent="Modals"]
visible = false
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
color = Color(0, 0, 0, 0.85)

[node name="Panel" type="Panel" parent="Modals/StageSelectModal"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_left = 18.0
offset_top = 24.0
offset_right = -18.0
offset_bottom = -24.0
grow_horizontal = 2
grow_vertical = 2

[node name="VBox" type="VBoxContainer" parent="Modals/StageSelectModal/Panel"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_left = 14.0
offset_top = 14.0
offset_right = -14.0
offset_bottom = -14.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 10

[node name="HeaderHBox" type="HBoxContainer" parent="Modals/StageSelectModal/Panel/VBox"]
layout_mode = 2

[node name="Title" type="Label" parent="Modals/StageSelectModal/Panel/VBox/HeaderHBox"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_font_sizes/font_size = 20
text = "Campanha dos Guardiões (1 a 10)"

[node name="CloseModalButton" type="Button" parent="Modals/StageSelectModal/Panel/VBox/HeaderHBox"]
custom_minimum_size = Vector2(80, 38)
layout_mode = 2
text = "Fechar"

[node name="Scroll" type="ScrollContainer" parent="Modals/StageSelectModal/Panel/VBox"]
layout_mode = 2
size_flags_vertical = 3

[node name="StagesList" type="VBoxContainer" parent="Modals/StageSelectModal/Panel/VBox/Scroll"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_constants/separation = 10

[node name="QuestsModal" type="ColorRect" parent="Modals"]
visible = false
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
color = Color(0, 0, 0, 0.85)

[node name="Panel" type="Panel" parent="Modals/QuestsModal"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_left = 18.0
offset_top = 24.0
offset_right = -18.0
offset_bottom = -24.0
grow_horizontal = 2
grow_vertical = 2

[node name="VBox" type="VBoxContainer" parent="Modals/QuestsModal/Panel"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_left = 14.0
offset_top = 14.0
offset_right = -14.0
offset_bottom = -14.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 10

[node name="HeaderHBox" type="HBoxContainer" parent="Modals/QuestsModal/Panel/VBox"]
layout_mode = 2

[node name="Title" type="Label" parent="Modals/QuestsModal/Panel/VBox/HeaderHBox"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_font_sizes/font_size = 20
text = "Missões & Recompensas"

[node name="CloseQuestsButton" type="Button" parent="Modals/QuestsModal/Panel/VBox/HeaderHBox"]
custom_minimum_size = Vector2(80, 38)
layout_mode = 2
text = "Fechar"

[node name="Scroll" type="ScrollContainer" parent="Modals/QuestsModal/Panel/VBox"]
layout_mode = 2
size_flags_vertical = 3

[node name="QuestsList" type="VBoxContainer" parent="Modals/QuestsModal/Panel/VBox/Scroll"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_constants/separation = 10

[node name="SettingsModal" type="ColorRect" parent="Modals"]
visible = false
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
color = Color(0, 0, 0, 0.8)

[node name="Panel" type="Panel" parent="Modals/SettingsModal"]
custom_minimum_size = Vector2(340, 260)
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -170.0
offset_top = -130.0
offset_right = 170.0
offset_bottom = 130.0
grow_horizontal = 2
grow_vertical = 2

[node name="VBox" type="VBoxContainer" parent="Modals/SettingsModal/Panel"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 16
alignment = 1

[node name="Title" type="Label" parent="Modals/SettingsModal/Panel/VBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 22
text = "Configurações"
horizontal_alignment = 1

[node name="ResetProgressButton" type="Button" parent="Modals/SettingsModal/Panel/VBox"]
custom_minimum_size = Vector2(240, 48)
layout_mode = 2
size_flags_horizontal = 4
text = "Reiniciar Progresso do Jogo"

[node name="CloseSettingsButton" type="Button" parent="Modals/SettingsModal/Panel/VBox"]
custom_minimum_size = Vector2(240, 48)
layout_mode = 2
size_flags_horizontal = 4
text = "Fechar Configurações"
"""
    path = os.path.join(SCENES_DIR, "menus/main_menu.tscn")
    with open(path, "w", encoding="utf-8") as f:
        f.write(content.strip())
    print("Generated vertical: scenes/menus/main_menu.tscn")

if __name__ == "__main__":
    generate_vertical_combat_scene()
    generate_vertical_main_menu_scene()
    print("Vertical scenes generated successfully!")
