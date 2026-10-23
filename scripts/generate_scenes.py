#!/usr/bin/env python3
import os

SCENES_DIR = "/guardians_of_the_last_base/scenes"

def generate_combat_scene():
    content = """[gd_scene load_steps=5 format=3 uid="uid://c8ycombat001"]

[ext_resource type="Script" path="res://scripts/combat/combat_manager.gd" id="1_combat_mgr"]
[ext_resource type="Script" path="res://scripts/ui/combat_ui.gd" id="2_combat_ui"]
[ext_resource type="Texture2D" path="res://assets/sprites/base_fortress.svg" id="3_base_fort"]
[ext_resource type="Texture2D" path="res://assets/icons/coin.svg" id="4_coin_icon"]

[sub_resource type="Curve2D" id="Curve2D_path"]
_data = {
"points": PackedVector2Array(0, 0, 0, 0, 40, 360, -30, 0, 30, 0, 220, 260, -30, -20, 30, 20, 360, 380, -30, 20, 30, -20, 500, 360, -20, -20, 20, 20, 660, 310, -30, -20, 30, 20, 780, 440, -30, 20, 30, -20, 940, 360, 0, 0, 0, 0, 1140, 360)
}

[node name="CombatStage" type="Node2D"]
script = ExtResource("1_combat_mgr")

[node name="Background" type="ColorRect" parent="."]
offset_right = 1280.0
offset_bottom = 720.0
color = Color(0.0823529, 0.101961, 0.141176, 1)

[node name="BattlefieldFloor" type="ColorRect" parent="."]
offset_left = 20.0
offset_top = 80.0
offset_right = 1260.0
offset_bottom = 600.0
color = Color(0.109804, 0.141176, 0.196078, 1)

[node name="RoadVisual" type="Line2D" parent="."]
points = PackedVector2Array(40, 360, 220, 260, 360, 380, 500, 360, 660, 310, 780, 440, 940, 360, 1140, 360)
width = 44.0
default_color = Color(0.231373, 0.282353, 0.380392, 1)
joint_mode = 2
begin_cap_mode = 2
end_cap_mode = 2

[node name="RoadBorder" type="Line2D" parent="."]
points = PackedVector2Array(40, 360, 220, 260, 360, 380, 500, 360, 660, 310, 780, 440, 940, 360, 1140, 360)
width = 48.0
default_color = Color(0.337255, 0.415686, 0.541176, 0.5)
joint_mode = 2
begin_cap_mode = 2
end_cap_mode = 2

[node name="SpawnPortal" type="Node2D" parent="."]
position = Vector2(40, 360)

[node name="PortalOuter" type="ColorRect" parent="SpawnPortal"]
offset_left = -20.0
offset_top = -35.0
offset_right = 20.0
offset_bottom = 35.0
color = Color(0.6, 0.1, 0.7, 0.8)

[node name="PortalInner" type="ColorRect" parent="SpawnPortal"]
offset_left = -12.0
offset_top = -25.0
offset_right = 12.0
offset_bottom = 25.0
color = Color(0.9, 0.4, 1.0, 1)

[node name="BaseFortress" type="Node2D" parent="."]
position = Vector2(1140, 360)

[node name="Sprite2D" type="Sprite2D" parent="BaseFortress"]
position = Vector2(0, -20)
texture = ExtResource("3_base_fort")

[node name="DefenseSlotMarkers" type="Node2D" parent="."]

[node name="Slot0" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 340.0
offset_top = 220.0
offset_right = 380.0
offset_bottom = 260.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="Slot1" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 340.0
offset_top = 460.0
offset_right = 380.0
offset_bottom = 500.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="Slot2" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 560.0
offset_top = 200.0
offset_right = 600.0
offset_bottom = 240.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="Slot3" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 560.0
offset_top = 480.0
offset_right = 600.0
offset_bottom = 520.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="Slot4" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 780.0
offset_top = 210.0
offset_right = 820.0
offset_bottom = 250.0
color = Color(0.18, 0.35, 0.55, 0.4)

[node name="Slot5" type="ColorRect" parent="DefenseSlotMarkers"]
offset_left = 780.0
offset_top = 470.0
offset_right = 820.0
offset_bottom = 510.0
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
offset_bottom = 70.0
grow_horizontal = 2

[node name="HBox" type="HBoxContainer" parent="CombatUI/TopBar"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_left = 20.0
offset_right = -20.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 24

[node name="BaseHPContainer" type="HBoxContainer" parent="CombatUI/TopBar/HBox"]
layout_mode = 2
theme_override_constants/separation = 10

[node name="IconLabel" type="Label" parent="CombatUI/TopBar/HBox/BaseHPContainer"]
layout_mode = 2
text = "Fortaleza:"

[node name="HPBar" type="ProgressBar" parent="CombatUI/TopBar/HBox/BaseHPContainer"]
custom_minimum_size = Vector2(180, 24)
layout_mode = 2
size_flags_vertical = 4
max_value = 100.0
value = 100.0
show_percentage = false

[node name="HPLabel" type="Label" parent="CombatUI/TopBar/HBox/BaseHPContainer"]
layout_mode = 2
text = "100 / 100"

[node name="WaveContainer" type="HBoxContainer" parent="CombatUI/TopBar/HBox"]
layout_mode = 2

[node name="WaveLabel" type="Label" parent="CombatUI/TopBar/HBox/WaveContainer"]
layout_mode = 2
text = "Onda 1 / 3"

[node name="EnemiesContainer" type="HBoxContainer" parent="CombatUI/TopBar/HBox"]
layout_mode = 2

[node name="EnemiesLabel" type="Label" parent="CombatUI/TopBar/HBox/EnemiesContainer"]
layout_mode = 2
text = "Inimigos: 0"

[node name="CoinsContainer" type="HBoxContainer" parent="CombatUI/TopBar/HBox"]
layout_mode = 2
theme_override_constants/separation = 6

[node name="CoinIcon" type="TextureRect" parent="CombatUI/TopBar/HBox/CoinsContainer"]
custom_minimum_size = Vector2(30, 30)
layout_mode = 2
size_flags_vertical = 4
texture = ExtResource("4_coin_icon")
expand_mode = 1

[node name="CoinsLabel" type="Label" parent="CombatUI/TopBar/HBox/CoinsContainer"]
layout_mode = 2
text = "350"

[node name="ControlsContainer" type="HBoxContainer" parent="CombatUI/TopBar/HBox"]
layout_mode = 2
size_flags_horizontal = 10
theme_override_constants/separation = 12

[node name="SpeedButton" type="Button" parent="CombatUI/TopBar/HBox/ControlsContainer"]
custom_minimum_size = Vector2(60, 44)
layout_mode = 2
size_flags_vertical = 4
text = "1x"

[node name="PauseButton" type="Button" parent="CombatUI/TopBar/HBox/ControlsContainer"]
custom_minimum_size = Vector2(80, 44)
layout_mode = 2
size_flags_vertical = 4
text = "Pausar"

[node name="BottomBar" type="Panel" parent="CombatUI"]
anchors_preset = 12
anchor_top = 1.0
anchor_right = 1.0
anchor_bottom = 1.0
offset_top = -115.0
grow_horizontal = 2
grow_vertical = 0

[node name="SkillsContainer" type="HBoxContainer" parent="CombatUI/BottomBar"]
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -260.0
offset_top = -45.0
offset_right = 260.0
offset_bottom = 45.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 30
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
custom_minimum_size = Vector2(360, 260)
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -180.0
offset_top = -130.0
offset_right = 180.0
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
theme_override_constants/separation = 18
alignment = 1

[node name="Title" type="Label" parent="CombatUI/Modals/PauseModal/Panel/VBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 26
text = "Jogo Pausado"
horizontal_alignment = 1

[node name="ResumeButton" type="Button" parent="CombatUI/Modals/PauseModal/Panel/VBox"]
custom_minimum_size = Vector2(200, 44)
layout_mode = 2
size_flags_horizontal = 4
text = "Continuar Batalha"

[node name="RestartButton" type="Button" parent="CombatUI/Modals/PauseModal/Panel/VBox"]
custom_minimum_size = Vector2(200, 44)
layout_mode = 2
size_flags_horizontal = 4
text = "Reiniciar Batalha"

[node name="QuitButton" type="Button" parent="CombatUI/Modals/PauseModal/Panel/VBox"]
custom_minimum_size = Vector2(200, 44)
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
custom_minimum_size = Vector2(480, 330)
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -240.0
offset_top = -165.0
offset_right = 240.0
offset_bottom = 165.0
grow_horizontal = 2
grow_vertical = 2

[node name="VBox" type="VBoxContainer" parent="CombatUI/Modals/VictoryModal/Panel"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 14
alignment = 1

[node name="Title" type="Label" parent="CombatUI/Modals/VictoryModal/Panel/VBox"]
layout_mode = 2
theme_override_colors/font_color = Color(1, 0.843137, 0, 1)
theme_override_font_sizes/font_size = 32
text = "VITÓRIA!"
horizontal_alignment = 1

[node name="SubTitle" type="Label" parent="CombatUI/Modals/VictoryModal/Panel/VBox"]
layout_mode = 2
text = "A base foi protegida com sucesso!"
horizontal_alignment = 1

[node name="Rewards" type="Label" parent="CombatUI/Modals/VictoryModal/Panel/VBox"]
layout_mode = 2
theme_override_colors/font_color = Color(0.4, 1, 0.4, 1)
theme_override_font_sizes/font_size = 17
text = "+140 Ouro Real  |  +50 EXP da Base"
horizontal_alignment = 1

[node name="ButtonsHBox" type="HBoxContainer" parent="CombatUI/Modals/VictoryModal/Panel/VBox"]
layout_mode = 2
theme_override_constants/separation = 12
alignment = 1

[node name="ReplayButton" type="Button" parent="CombatUI/Modals/VictoryModal/Panel/VBox/ButtonsHBox"]
custom_minimum_size = Vector2(130, 48)
layout_mode = 2
text = "Repetir Fase"

[node name="NextStageButton" type="Button" parent="CombatUI/Modals/VictoryModal/Panel/VBox/ButtonsHBox"]
custom_minimum_size = Vector2(140, 48)
layout_mode = 2
text = "Próxima Fase"

[node name="ContinueButton" type="Button" parent="CombatUI/Modals/VictoryModal/Panel/VBox/ButtonsHBox"]
custom_minimum_size = Vector2(140, 48)
layout_mode = 2
text = "Voltar à Base"

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
custom_minimum_size = Vector2(460, 300)
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -230.0
offset_top = -150.0
offset_right = 230.0
offset_bottom = 150.0
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
theme_override_font_sizes/font_size = 32
text = "DERROTA"
horizontal_alignment = 1

[node name="SubTitle" type="Label" parent="CombatUI/Modals/DefeatModal/Panel/VBox"]
layout_mode = 2
text = "A Fortaleza sucumbiu aos invasores."
horizontal_alignment = 1

[node name="RetryButton" type="Button" parent="CombatUI/Modals/DefeatModal/Panel/VBox"]
custom_minimum_size = Vector2(200, 48)
layout_mode = 2
size_flags_horizontal = 4
text = "Tentar Novamente"

[node name="QuitButton" type="Button" parent="CombatUI/Modals/DefeatModal/Panel/VBox"]
custom_minimum_size = Vector2(200, 48)
layout_mode = 2
size_flags_horizontal = 4
text = "Voltar ao Menu Principal"
"""
    with open(f"{SCENES_DIR}/combat/combat_stage.tscn", "w", encoding="utf-8") as f:
        f.write(content.strip())
    print("Created combat_stage.tscn")

def generate_main_menu_scene():
    content = """[gd_scene load_steps=7 format=3 uid="uid://m0mainmen001"]

[ext_resource type="Script" path="res://scripts/ui/main_menu.gd" id="1_main_menu"]
[ext_resource type="Script" path="res://scripts/ui/character_roster_ui.gd" id="2_roster_ui"]
[ext_resource type="Script" path="res://scripts/ui/base_upgrade_ui.gd" id="3_base_ui"]
[ext_resource type="Texture2D" path="res://assets/icons/coin.svg" id="4_coin_icon"]
[ext_resource type="Texture2D" path="res://assets/icons/icon.svg" id="5_game_logo"]
[ext_resource type="Texture2D" path="res://assets/icons/kael_avatar.svg" id="6_def_avatar"]

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
custom_minimum_size = Vector2(0, 72)
layout_mode = 1
anchors_preset = 10
anchor_right = 1.0
offset_bottom = 72.0
grow_horizontal = 2

[node name="HBox" type="HBoxContainer" parent="TopHeader"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_left = 24.0
offset_right = -24.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 28

[node name="GameTitleLabel" type="Label" parent="TopHeader/HBox"]
layout_mode = 2
theme_override_colors/font_color = Color(0.96, 0.82, 0.4, 1)
theme_override_font_sizes/font_size = 20
text = "Guardians of the Last Base"

[node name="BaseContainer" type="HBoxContainer" parent="TopHeader/HBox"]
layout_mode = 2
theme_override_constants/separation = 12

[node name="BaseLevelLabel" type="Label" parent="TopHeader/HBox/BaseContainer"]
layout_mode = 2
text = "Base Nível 1"

[node name="ExpBar" type="ProgressBar" parent="TopHeader/HBox/BaseContainer"]
custom_minimum_size = Vector2(160, 20)
layout_mode = 2
size_flags_vertical = 4
max_value = 100.0
value = 0.0
show_percentage = false

[node name="CoinsContainer" type="HBoxContainer" parent="TopHeader/HBox"]
layout_mode = 2
theme_override_constants/separation = 8

[node name="CoinIcon" type="TextureRect" parent="TopHeader/HBox/CoinsContainer"]
custom_minimum_size = Vector2(32, 32)
layout_mode = 2
size_flags_vertical = 4
texture = ExtResource("4_coin_icon")
expand_mode = 1

[node name="CoinsLabel" type="Label" parent="TopHeader/HBox/CoinsContainer"]
layout_mode = 2
theme_override_colors/font_color = Color(1, 0.9, 0.3, 1)
theme_override_font_sizes/font_size = 18
text = "350"

[node name="CenterMenu" type="Control" parent="."]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_top = 72.0
grow_horizontal = 2
grow_vertical = 2

[node name="LogoRect" type="TextureRect" parent="CenterMenu"]
custom_minimum_size = Vector2(180, 180)
layout_mode = 1
anchors_preset = 5
anchor_left = 0.5
anchor_right = 0.5
offset_left = -90.0
offset_top = 20.0
offset_right = 90.0
offset_bottom = 200.0
grow_horizontal = 2
texture = ExtResource("5_game_logo")
expand_mode = 1
stretch_mode = 5

[node name="VBox" type="VBoxContainer" parent="CenterMenu"]
custom_minimum_size = Vector2(340, 0)
layout_mode = 1
anchors_preset = 7
anchor_left = 0.5
anchor_top = 1.0
anchor_right = 0.5
anchor_bottom = 1.0
offset_left = -170.0
offset_top = -410.0
offset_right = 170.0
offset_bottom = -30.0
grow_horizontal = 2
grow_vertical = 0
theme_override_constants/separation = 14

[node name="PlayButton" type="Button" parent="CenterMenu/VBox"]
custom_minimum_size = Vector2(0, 60)
layout_mode = 2
theme_override_font_sizes/font_size = 20
text = "JOGAR BATALHA"

[node name="CharactersButton" type="Button" parent="CenterMenu/VBox"]
custom_minimum_size = Vector2(0, 54)
layout_mode = 2
theme_override_font_sizes/font_size = 17
text = "Personagens e Equipe"

[node name="BaseUpgradeButton" type="Button" parent="CenterMenu/VBox"]
custom_minimum_size = Vector2(0, 54)
layout_mode = 2
theme_override_font_sizes/font_size = 17
text = "Melhorar Base Central"

[node name="StagesButton" type="Button" parent="CenterMenu/VBox"]
custom_minimum_size = Vector2(0, 54)
layout_mode = 2
theme_override_font_sizes/font_size = 17
text = "Fases e Campanhas"

[node name="SettingsButton" type="Button" parent="CenterMenu/VBox"]
custom_minimum_size = Vector2(0, 50)
layout_mode = 2
theme_override_font_sizes/font_size = 16
text = "Configurações"

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
color = Color(0.07, 0.09, 0.14, 0.98)

[node name="Margin" type="MarginContainer" parent="SubScreens/CharacterRosterScreen"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/margin_left = 24
theme_override_constants/margin_top = 24
theme_override_constants/margin_right = 24
theme_override_constants/margin_bottom = 24

[node name="HBox" type="HBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin"]
layout_mode = 2
theme_override_constants/separation = 24

[node name="LeftPanel" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox"]
custom_minimum_size = Vector2(280, 0)
layout_mode = 2
theme_override_constants/separation = 12

[node name="Title" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/LeftPanel"]
layout_mode = 2
theme_override_font_sizes/font_size = 20
text = "Heróis Guardiões"

[node name="Scroll" type="ScrollContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox/LeftPanel"]
layout_mode = 2
size_flags_vertical = 3

[node name="CharList" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox/LeftPanel/Scroll"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_constants/separation = 8

[node name="BackButton" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/HBox/LeftPanel"]
custom_minimum_size = Vector2(0, 48)
layout_mode = 2
text = "Voltar ao Menu"

[node name="RightPanel" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_constants/separation = 14

[node name="Header" type="HBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel"]
layout_mode = 2
theme_override_constants/separation = 16

[node name="AvatarRect" type="TextureRect" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/Header"]
custom_minimum_size = Vector2(80, 80)
layout_mode = 2
texture = ExtResource("6_def_avatar")
expand_mode = 1
stretch_mode = 5

[node name="InfoVBox" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/Header"]
layout_mode = 2

[node name="NameLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/Header/InfoVBox"]
layout_mode = 2
theme_override_colors/font_color = Color(0.96, 0.82, 0.35, 1)
theme_override_font_sizes/font_size = 24
text = "Kael"

[node name="TitleLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/Header/InfoVBox"]
layout_mode = 2
theme_override_font_sizes/font_size = 16
text = "Guardião Arcano"

[node name="RoleLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/Header/InfoVBox"]
layout_mode = 2
theme_override_colors/font_color = Color(0.4, 0.8, 1, 1)
theme_override_font_sizes/font_size = 14
text = "Dano Mágico & Controle"

[node name="DescriptionLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel"]
layout_mode = 2
autowrap_mode = 3

[node name="StatsGrid" type="GridContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel"]
layout_mode = 2
theme_override_constants/h_separation = 30
theme_override_constants/v_separation = 8
columns = 2

[node name="LevelLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/StatsGrid"]
layout_mode = 2
text = "Nível: 1"

[node name="DamageLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/StatsGrid"]
layout_mode = 2
text = "Dano: 25.0"

[node name="SpeedLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/StatsGrid"]
layout_mode = 2
text = "Vel. Ataque: 1.0/s"

[node name="RangeLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/StatsGrid"]
layout_mode = 2
text = "Alcance: 220"

[node name="SkillDamageLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/StatsGrid"]
layout_mode = 2
text = "Dano Habilidade: 70.0"

[node name="SkillsBox" type="HBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel"]
layout_mode = 2
theme_override_constants/separation = 20

[node name="Skill1" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/SkillsBox"]
layout_mode = 2
size_flags_horizontal = 3

[node name="Name" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/SkillsBox/Skill1"]
layout_mode = 2
theme_override_colors/font_color = Color(0.7, 0.4, 1, 1)
text = "Especial 1"

[node name="Desc" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/SkillsBox/Skill1"]
layout_mode = 2
autowrap_mode = 3

[node name="Skill2" type="VBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/SkillsBox"]
layout_mode = 2
size_flags_horizontal = 3

[node name="Name" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/SkillsBox/Skill2"]
layout_mode = 2
theme_override_colors/font_color = Color(0.3, 0.9, 0.9, 1)
text = "Especial 2"

[node name="Desc" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/SkillsBox/Skill2"]
layout_mode = 2
autowrap_mode = 3

[node name="Actions" type="HBoxContainer" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel"]
layout_mode = 2
theme_override_constants/separation = 16

[node name="UpgradeButton" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/Actions"]
custom_minimum_size = Vector2(200, 50)
layout_mode = 2
text = "Melhorar Nível"

[node name="EquipButton" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/Actions"]
custom_minimum_size = Vector2(180, 50)
layout_mode = 2
text = "Equipar na Defesa"

[node name="UnlockButton" type="Button" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/Actions"]
visible = false
custom_minimum_size = Vector2(260, 50)
layout_mode = 2
text = "Desbloquear"

[node name="SlotsLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel/Actions"]
layout_mode = 2
text = "Espaços: 2 / 2"

[node name="FeedbackLabel" type="Label" parent="SubScreens/CharacterRosterScreen/Margin/HBox/RightPanel"]
layout_mode = 2
text = ""

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
color = Color(0.07, 0.09, 0.14, 0.98)

[node name="Margin" type="MarginContainer" parent="SubScreens/BaseUpgradeScreen"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/margin_left = 32
theme_override_constants/margin_top = 24
theme_override_constants/margin_right = 32
theme_override_constants/margin_bottom = 24

[node name="VBox" type="VBoxContainer" parent="SubScreens/BaseUpgradeScreen/Margin"]
layout_mode = 2
theme_override_constants/separation = 16

[node name="TopBar" type="HBoxContainer" parent="SubScreens/BaseUpgradeScreen/Margin/VBox"]
layout_mode = 2

[node name="Title" type="Label" parent="SubScreens/BaseUpgradeScreen/Margin/VBox/TopBar"]
layout_mode = 2
theme_override_colors/font_color = Color(0.96, 0.82, 0.4, 1)
theme_override_font_sizes/font_size = 24
text = "Evolução da Base Central"

[node name="CoinsLabel" type="Label" parent="SubScreens/BaseUpgradeScreen/Margin/VBox/TopBar"]
layout_mode = 2
size_flags_horizontal = 10
theme_override_colors/font_color = Color(1, 0.9, 0.3, 1)
theme_override_font_sizes/font_size = 18
text = "350 Ouro"

[node name="CloseButton" type="Button" parent="SubScreens/BaseUpgradeScreen/Margin/VBox/TopBar"]
custom_minimum_size = Vector2(140, 44)
layout_mode = 2
size_flags_horizontal = 8
text = "Voltar ao Menu"

[node name="Scroll" type="ScrollContainer" parent="SubScreens/BaseUpgradeScreen/Margin/VBox"]
layout_mode = 2
size_flags_vertical = 3

[node name="StructuresList" type="VBoxContainer" parent="SubScreens/BaseUpgradeScreen/Margin/VBox/Scroll"]
layout_mode = 2
size_flags_horizontal = 3
theme_override_constants/separation = 14

[node name="FeedbackLabel" type="Label" parent="SubScreens/BaseUpgradeScreen/Margin/VBox"]
layout_mode = 2
text = ""

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
color = Color(0, 0, 0, 0.8)

[node name="Panel" type="Panel" parent="Modals/StageSelectModal"]
custom_minimum_size = Vector2(740, 520)
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -370.0
offset_top = -260.0
offset_right = 370.0
offset_bottom = 260.0
grow_horizontal = 2
grow_vertical = 2

[node name="VBox" type="VBoxContainer" parent="Modals/StageSelectModal/Panel"]
layout_mode = 1
anchors_preset = 15
anchor_right = 1.0
anchor_bottom = 1.0
offset_left = 20.0
offset_top = 16.0
offset_right = -20.0
offset_bottom = -16.0
grow_horizontal = 2
grow_vertical = 2
theme_override_constants/separation = 12

[node name="HeaderHBox" type="HBoxContainer" parent="Modals/StageSelectModal/Panel/VBox"]
layout_mode = 2

[node name="Title" type="Label" parent="Modals/StageSelectModal/Panel/VBox/HeaderHBox"]
layout_mode = 2
theme_override_colors/font_color = Color(0.96, 0.82, 0.4, 1)
theme_override_font_sizes/font_size = 22
text = "Campanha dos Guardiões — Fases 1 a 10"

[node name="CloseModalButton" type="Button" parent="Modals/StageSelectModal/Panel/VBox/HeaderHBox"]
custom_minimum_size = Vector2(100, 36)
layout_mode = 2
size_flags_horizontal = 10
text = "Fechar"

[node name="Scroll" type="ScrollContainer" parent="Modals/StageSelectModal/Panel/VBox"]
layout_mode = 2
size_flags_vertical = 3

[node name="StagesList" type="VBoxContainer" parent="Modals/StageSelectModal/Panel/VBox/Scroll"]
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
color = Color(0, 0, 0, 0.75)

[node name="Panel" type="Panel" parent="Modals/SettingsModal"]
custom_minimum_size = Vector2(440, 320)
layout_mode = 1
anchors_preset = 8
anchor_left = 0.5
anchor_top = 0.5
anchor_right = 0.5
anchor_bottom = 0.5
offset_left = -220.0
offset_top = -160.0
offset_right = 220.0
offset_bottom = 160.0
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
text = "Configurações do Jogo"
horizontal_alignment = 1

[node name="AudioLabel" type="Label" parent="Modals/SettingsModal/Panel/VBox"]
layout_mode = 2
text = "Volume dos Efeitos Sonoros: 80%"
horizontal_alignment = 1

[node name="ResetProgressButton" type="Button" parent="Modals/SettingsModal/Panel/VBox"]
custom_minimum_size = Vector2(220, 44)
layout_mode = 2
size_flags_horizontal = 4
text = "Resetar Progresso Salvo"

[node name="CloseSettingsButton" type="Button" parent="Modals/SettingsModal/Panel/VBox"]
custom_minimum_size = Vector2(160, 44)
layout_mode = 2
size_flags_horizontal = 4
text = "Fechar"
"""
    with open(f"{SCENES_DIR}/menus/main_menu.tscn", "w", encoding="utf-8") as f:
        f.write(content.strip())
    print("Created main_menu.tscn")

if __name__ == "__main__":
    generate_combat_scene()
    generate_main_menu_scene()
