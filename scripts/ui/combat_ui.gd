class_name CombatUI
extends CanvasLayer

## Interface Gráfica Definitiva de Combate Adaptada para Mobile
## Exibe barra de vida da base, ondas, barra de vida de chefes, habilidades ativas, velocidade e modais.

@onready var combat_manager: CombatManager = get_parent()

# Top Bar
@onready var base_hp_bar: ProgressBar = $TopBar/HBox/BaseHPContainer/HPBar
@onready var base_hp_label: Label = $TopBar/HBox/BaseHPContainer/HPLabel
@onready var wave_label: Label = $TopBar/HBox/WaveContainer/WaveLabel
@onready var enemies_label: Label = $TopBar/HBox/EnemiesContainer/EnemiesLabel
@onready var coins_label: Label = $TopBar/HBox/CoinsContainer/CoinsLabel

# Controles rápidos
@onready var speed_button: Button = $TopBar/HBox/ControlsContainer/SpeedButton
@onready var pause_button: Button = $TopBar/HBox/ControlsContainer/PauseButton

# Barra inferior de Habilidades
@onready var skills_container: HBoxContainer = $BottomBar/SkillsContainer

# Modais
@onready var pause_modal: Control = $Modals/PauseModal
@onready var victory_modal: Control = $Modals/VictoryModal
@onready var defeat_modal: Control = $Modals/DefeatModal

# Barra de Chefe Dinâmica e Banner Cinematográfico
var boss_container: VBoxContainer
var boss_name_label: Label
var boss_bar: ProgressBar
var boss_alert_banner: PanelContainer
var boss_alert_title: Label
var boss_alert_subtitle: Label

var skill_button_nodes: Dictionary = {}

func _ready() -> void:
	_create_boss_ui()
	_connect_signals()
	_update_coins(ProgressionManager.get_coins())
	_build_skills_bar()
	
	pause_modal.visible = false
	victory_modal.visible = false
	defeat_modal.visible = false

func _create_boss_ui() -> void:
	# Banner Cinematográfico de Alerta de Chefe
	boss_alert_banner = PanelContainer.new()
	boss_alert_banner.name = "BossAlertBanner"
	boss_alert_banner.anchors_preset = Control.PRESET_CENTER
	boss_alert_banner.anchor_left = 0.5
	boss_alert_banner.anchor_top = 0.38
	boss_alert_banner.anchor_right = 0.5
	boss_alert_banner.anchor_bottom = 0.38
	boss_alert_banner.offset_left = -300.0
	boss_alert_banner.offset_right = 300.0
	boss_alert_banner.offset_top = -65.0
	boss_alert_banner.offset_bottom = 65.0
	boss_alert_banner.pivot_offset = Vector2(300, 65)
	boss_alert_banner.visible = false
	
	var alert_style := StyleBoxFlat.new()
	alert_style.bg_color = Color(0.12, 0.02, 0.02, 0.94)
	alert_style.border_color = Color(1.0, 0.25, 0.25, 1.0)
	alert_style.set_border_width_all(3)
	alert_style.set_corner_radius_all(14)
	boss_alert_banner.add_theme_stylebox_override("panel", alert_style)
	
	var alert_vbox := VBoxContainer.new()
	alert_vbox.alignment = BoxContainer.ALIGNMENT_CENTER
	alert_vbox.theme_override_constants.separation = 6
	boss_alert_banner.add_child(alert_vbox)
	
	boss_alert_title = Label.new()
	boss_alert_title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	boss_alert_title.add_theme_font_size_override("font_size", 22)
	boss_alert_title.add_theme_color_override("font_color", Color(1.0, 0.25, 0.25, 1.0))
	boss_alert_title.text = "⚠ ALERTA: CHEFE SE APROXIMANDO ⚠"
	alert_vbox.add_child(boss_alert_title)
	
	boss_alert_subtitle = Label.new()
	boss_alert_subtitle.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	boss_alert_subtitle.add_theme_font_size_override("font_size", 16)
	boss_alert_subtitle.add_theme_color_override("font_color", Color(1.0, 0.85, 0.4, 1.0))
	boss_alert_subtitle.text = "Ogro das Ruínas — Devastador Ancestral"
	alert_vbox.add_child(boss_alert_subtitle)
	
	add_child(boss_alert_banner)

	# Barra de Vida do Chefe
	boss_container = VBoxContainer.new()
	boss_container.name = "BossContainer"
	boss_container.anchors_preset = Control.PRESET_CENTER_TOP
	boss_container.anchor_left = 0.5
	boss_container.anchor_right = 0.5
	boss_container.offset_left = -260.0
	boss_container.offset_top = 108.0
	boss_container.offset_right = 260.0
	boss_container.offset_bottom = 158.0
	boss_container.visible = false
	add_child(boss_container)
	
	boss_name_label = Label.new()
	boss_name_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	boss_name_label.add_theme_font_size_override("font_size", 15)
	boss_name_label.add_theme_color_override("font_color", Color(1.0, 0.3, 0.3, 1.0))
	boss_name_label.text = "CHEFE"
	boss_container.add_child(boss_name_label)
	
	boss_bar = ProgressBar.new()
	boss_bar.custom_minimum_size = Vector2(520, 20)
	boss_bar.show_percentage = true
	
	var bg_st := StyleBoxFlat.new()
	bg_st.bg_color = Color(0.1, 0.1, 0.15, 0.9)
	bg_st.set_corner_radius_all(4)
	boss_bar.add_theme_stylebox_override("background", bg_st)
	
	var fill_st := StyleBoxFlat.new()
	fill_st.bg_color = Color(0.95, 0.15, 0.15, 1.0)
	fill_st.set_corner_radius_all(4)
	boss_bar.add_theme_stylebox_override("fill", fill_st)
	
	boss_container.add_child(boss_bar)

func _connect_signals() -> void:
	combat_manager.base_health_changed.connect(_on_base_hp_changed)
	combat_manager.wave_started.connect(_on_wave_started)
	combat_manager.wave_enemies_updated.connect(_on_wave_enemies_updated)
	combat_manager.battle_victory.connect(_on_victory)
	combat_manager.battle_victory_with_rewards.connect(_on_victory_rewards)
	combat_manager.battle_defeat.connect(_on_defeat)
	combat_manager.boss_spawned.connect(_on_boss_spawned)
	if combat_manager.has_signal("boss_cinematic_started"):
		combat_manager.boss_cinematic_started.connect(_on_boss_cinematic)
	combat_manager.boss_health_changed.connect(_on_boss_health_changed)
	
	ProgressionManager.coins_changed.connect(_update_coins)
	
	speed_button.pressed.connect(_on_speed_toggle_pressed)
	pause_button.pressed.connect(_on_pause_pressed)
	
	# Modais
	var resume_btn: Button = pause_modal.find_child("ResumeButton", true, false)
	if resume_btn: resume_btn.pressed.connect(_on_resume_pressed)
		
	var restart_btn: Button = pause_modal.find_child("RestartButton", true, false)
	if restart_btn:
		restart_btn.pressed.connect(func():
			AudioManager.play_sfx("click")
			GameStateManager.start_combat(combat_manager.stage_id)
		)
		
	var quit_btn: Button = pause_modal.find_child("QuitButton", true, false)
	if quit_btn: quit_btn.pressed.connect(_on_quit_to_menu_pressed)
		
	var victory_cont_btn: Button = victory_modal.find_child("ContinueButton", true, false)
	if victory_cont_btn: victory_cont_btn.pressed.connect(_on_quit_to_menu_pressed)
	
	var next_stage_btn: Button = victory_modal.find_child("NextStageButton", true, false)
	if next_stage_btn:
		next_stage_btn.pressed.connect(func():
			AudioManager.play_sfx("click")
			var next_id := combat_manager.stage_id + 1
			if next_id <= 10:
				GameStateManager.start_combat(next_id)
			else:
				GameStateManager.go_to_main_menu()
		)
		
	var replay_btn: Button = victory_modal.find_child("ReplayButton", true, false)
	if replay_btn:
		replay_btn.pressed.connect(func():
			AudioManager.play_sfx("click")
			GameStateManager.start_combat(combat_manager.stage_id)
		)
		
	var defeat_retry_btn: Button = defeat_modal.find_child("RetryButton", true, false)
	if defeat_retry_btn: defeat_retry_btn.pressed.connect(_on_retry_pressed)
		
	var defeat_quit_btn: Button = defeat_modal.find_child("QuitButton", true, false)
	if defeat_quit_btn: defeat_quit_btn.pressed.connect(_on_quit_to_menu_pressed)

func _update_coins(new_coins: int) -> void:
	if coins_label:
		coins_label.text = str(new_coins)

func _on_base_hp_changed(current: int, max_val: int) -> void:
	if base_hp_bar:
		base_hp_bar.max_value = max_val
		base_hp_bar.value = current
	if base_hp_label:
		base_hp_label.text = "%d / %d" % [current, max_val]

func _on_wave_started(current_wave: int, total_waves: int) -> void:
	if wave_label:
		wave_label.text = "Onda %d / %d" % [current_wave, total_waves]

func _on_wave_enemies_updated(alive_count: int, pending_count: int) -> void:
	if enemies_label:
		enemies_label.text = "Inimigos: %d (Restantes: %d)" % [alive_count, pending_count]

func _on_boss_cinematic(boss_name: String, boss_title: String, max_hp: float) -> void:
	if boss_alert_banner:
		boss_alert_subtitle.text = "%s — %s" % [boss_name, boss_title]
		boss_alert_banner.visible = true
		boss_alert_banner.modulate.a = 0.0
		boss_alert_banner.scale = Vector2(0.7, 0.7)
		
		var t := create_tween()
		t.tween_property(boss_alert_banner, "modulate:a", 1.0, 0.25)
		t.parallel().tween_property(boss_alert_banner, "scale", Vector2(1.05, 1.05), 0.25)
		t.tween_property(boss_alert_banner, "scale", Vector2(1.0, 1.0), 0.15)
		t.tween_interval(1.4)
		t.tween_property(boss_alert_banner, "modulate:a", 0.0, 0.35)
		t.tween_callback(func(): boss_alert_banner.visible = false)
		
	_on_boss_spawned(boss_name, max_hp)

func _on_boss_spawned(boss_name: String, max_hp: float) -> void:
	boss_container.visible = true
	boss_container.modulate.a = 1.0
	boss_name_label.text = "⚔ %s ⚔" % boss_name.to_upper()
	boss_bar.max_value = max_hp
	boss_bar.value = max_hp

func _on_boss_health_changed(current: float, max_val: float) -> void:
	if boss_bar:
		boss_bar.max_value = max_val
		boss_bar.value = maxf(0.0, current)
	if current <= 0.0:
		var t := create_tween()
		t.tween_property(boss_container, "modulate:a", 0.0, 0.4)
		t.tween_callback(func(): boss_container.visible = false)

func _build_skills_bar() -> void:
	for child in skills_container.get_children():
		child.queue_free()
	skill_button_nodes.clear()
	
	var team: Array = ProgressionManager.get_team_slots()
	for char_id in team:
		if char_id == "":
			continue
		var char_data: CharacterData = ProgressionManager.get_character_def(char_id)
		if not char_data:
			continue
			
		for skill_idx in range(char_data.skills.size()):
			var skill_info: Dictionary = char_data.skills[skill_idx]
			_create_skill_slot(char_id, skill_idx, skill_info)
			
	for def in combat_manager.spawned_defenders:
		def.skill_cooldown_updated.connect(_on_skill_cooldown_updated)

func _create_skill_slot(char_id: String, skill_idx: int, skill_info: Dictionary) -> void:
	var btn := Button.new()
	btn.custom_minimum_size = Vector2(76, 76)
	btn.text = ""
	
	var key := "%s_%d" % [char_id, skill_idx]
	
	var icon_path: String = skill_info.get("icon", "")
	if ResourceLoader.exists(icon_path):
		var tex := load(icon_path) as Texture2D
		btn.icon = tex
		btn.icon_alignment = HORIZONTAL_ALIGNMENT_CENTER
		btn.expand_icon = true
		
	var style_normal := StyleBoxFlat.new()
	style_normal.bg_color = Color(0.12, 0.16, 0.24, 0.9)
	style_normal.border_color = Color(0.4, 0.7, 1.0, 0.8)
	style_normal.set_border_width_all(2)
	style_normal.set_corner_radius_all(10)
	btn.add_theme_stylebox_override("normal", style_normal)
	
	var overlay := ColorRect.new()
	overlay.mouse_filter = Control.MOUSE_FILTER_IGNORE
	overlay.color = Color(0.0, 0.0, 0.0, 0.65)
	overlay.anchor_right = 1.0
	overlay.anchor_bottom = 1.0
	overlay.visible = false
	btn.add_child(overlay)
	
	var cd_label := Label.new()
	cd_label.anchor_right = 1.0
	cd_label.anchor_bottom = 1.0
	cd_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	cd_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	cd_label.add_theme_font_size_override("font_size", 20)
	cd_label.add_theme_color_override("font_color", Color(1.0, 1.0, 0.2, 1.0))
	cd_label.text = ""
	btn.add_child(cd_label)
	
	var name_label := Label.new()
	name_label.position = Vector2(0, 78)
	name_label.size = Vector2(76, 18)
	name_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	name_label.add_theme_font_size_override("font_size", 10)
	name_label.text = skill_info.get("name", "Habilidade")
	btn.add_child(name_label)
	
	btn.pressed.connect(func(): _on_skill_button_pressed(char_id, skill_idx))
	
	skills_container.add_child(btn)
	skill_button_nodes[key] = {
		"button": btn,
		"overlay": overlay,
		"label": cd_label
	}

func _on_skill_button_pressed(char_id: String, skill_idx: int) -> void:
	AudioManager.play_sfx("click")
	combat_manager.trigger_defender_skill(char_id, skill_idx)

func _on_skill_cooldown_updated(char_id: String, skill_idx: int, cur_cd: float, _max_cd: float) -> void:
	var key := "%s_%d" % [char_id, skill_idx]
	if not skill_button_nodes.has(key):
		return
		
	var data: Dictionary = skill_button_nodes[key]
	var btn: Button = data["button"]
	var overlay: ColorRect = data["overlay"]
	var label: Label = data["label"]
	
	if cur_cd > 0.0:
		btn.disabled = true
		overlay.visible = true
		label.text = "%.1f" % cur_cd
	else:
		btn.disabled = false
		overlay.visible = false
		label.text = ""

func _on_speed_toggle_pressed() -> void:
	AudioManager.play_sfx("click")
	if GameStateManager.game_speed == 1.0:
		GameStateManager.set_game_speed(2.0)
		speed_button.text = "2x"
	elif GameStateManager.game_speed == 2.0:
		GameStateManager.set_game_speed(3.0)
		speed_button.text = "3x"
	else:
		GameStateManager.set_game_speed(1.0)
		speed_button.text = "1x"

func _on_pause_pressed() -> void:
	AudioManager.play_sfx("click")
	GameStateManager.set_paused(true)
	pause_modal.visible = true

func _on_resume_pressed() -> void:
	AudioManager.play_sfx("click")
	GameStateManager.set_paused(false)
	pause_modal.visible = false

func _on_quit_to_menu_pressed() -> void:
	AudioManager.play_sfx("click")
	GameStateManager.go_to_main_menu()

func _on_retry_pressed() -> void:
	AudioManager.play_sfx("click")
	GameStateManager.start_combat(combat_manager.stage_id)

func _on_victory() -> void:
	victory_modal.visible = true

func _on_victory_rewards(rewards: Dictionary) -> void:
	victory_modal.visible = true
	var rewards_label: Label = victory_modal.find_child("Rewards", true, false)
	if rewards_label:
		var bonus_str := " (+%d Bônus Perfeito!)" % rewards.get("bonus_coins", 0) if rewards.get("achieved_bonus", false) else ""
		rewards_label.text = "+%d Ouro%s  |  +%d EXP da Base\nClassificação: %s" % [
			rewards.get("coins", 0) + rewards.get("bonus_coins", 0),
			bonus_str,
			rewards.get("exp", 0),
			"★".repeat(rewards.get("stars", 1))
		]
	var next_btn: Button = victory_modal.find_child("NextStageButton", true, false)
	if next_btn:
		next_btn.visible = (combat_manager.stage_id < 10)

func _on_defeat() -> void:
	defeat_modal.visible = true
