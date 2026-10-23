class_name MainMenuUI
extends Control

## Controlador do Menu Principal do Jogo (Vertical Mobile)
## Exibe cabeçalho com recursos da base, herói em destaque, missões e navegação.

# Cabeçalho
@onready var base_level_label: Label = $TopHeader/HBox/BaseContainer/BaseLevelLabel
@onready var base_exp_bar: ProgressBar = $TopHeader/HBox/BaseContainer/ExpBar
@onready var coins_label: Label = $TopHeader/HBox/CoinsContainer/CoinsLabel
@onready var gems_label: Label = $TopHeader/HBox/GemsContainer/GemsLabel

# Botões Principais de Navegação
@onready var play_button: Button = $CenterMenu/VBox/PlayButton
@onready var stages_button: Button = $CenterMenu/VBox/StagesButton
@onready var characters_button: Button = $CenterMenu/VBox/CharactersButton
@onready var base_upgrade_button: Button = $CenterMenu/VBox/BaseUpgradeButton
@onready var quests_button: Button = $CenterMenu/VBox/QuestsButton
@onready var settings_button: Button = $TopHeader/HBox/SettingsButton

# Showcase do Herói em Destaque
@onready var featured_hero_avatar: TextureRect = $CenterMenu/HeroShowcase/AvatarRect
@onready var featured_hero_name: Label = $CenterMenu/HeroShowcase/HeroName
@onready var featured_hero_title: Label = $CenterMenu/HeroShowcase/HeroTitle
@onready var featured_hero_rarity: Label = $CenterMenu/HeroShowcase/RarityBadge

# Telas / Modais
@onready var stage_select_modal: Control = $Modals/StageSelectModal
@onready var character_roster_screen: Control = $SubScreens/CharacterRosterScreen
@onready var base_upgrade_screen: Control = $SubScreens/BaseUpgradeScreen
@onready var quests_modal: Control = $Modals/QuestsModal
@onready var settings_modal: Control = $Modals/SettingsModal

func _ready() -> void:
	GameStateManager.change_state(GameStateManager.State.MENU)
	_connect_events()
	_refresh_header_data()
	_refresh_featured_hero()
	_close_all_subscreens()

func _process(_delta: float) -> void:
	# Efeito sutil de respiração no herói em destaque
	if featured_hero_avatar:
		featured_hero_avatar.scale = Vector2(1.0, 1.0) + Vector2(0.02, 0.02) * sin(Time.get_ticks_msec() * 0.003)

func _connect_events() -> void:
	play_button.pressed.connect(_on_play_pressed)
	stages_button.pressed.connect(_on_stages_pressed)
	characters_button.pressed.connect(_on_characters_pressed)
	base_upgrade_button.pressed.connect(_on_base_upgrade_pressed)
	if quests_button: quests_button.pressed.connect(_on_quests_pressed)
	if settings_button: settings_button.pressed.connect(_on_settings_pressed)
	
	ProgressionManager.coins_changed.connect(func(_val): _refresh_header_data())
	ProgressionManager.gems_changed.connect(func(_val): _refresh_header_data())
	ProgressionManager.base_exp_changed.connect(func(_c, _n, _l): _refresh_header_data())
	ProgressionManager.team_updated.connect(func(_t): _refresh_featured_hero())
	ProgressionManager.quest_updated.connect(func(_q): _populate_quests_list())
	
	# Modal de Fases
	var close_stage_btn: Button = stage_select_modal.find_child("CloseModalButton", true, false)
	if close_stage_btn:
		close_stage_btn.pressed.connect(func():
			AudioManager.play_sfx("click")
			stage_select_modal.visible = false
		)
		
	# Modal de Missões
	if quests_modal:
		var close_quests_btn: Button = quests_modal.find_child("CloseQuestsButton", true, false)
		if close_quests_btn:
			close_quests_btn.pressed.connect(func():
				AudioManager.play_sfx("click")
				quests_modal.visible = false
			)
		
	# Modal de Configurações
	var reset_btn: Button = settings_modal.find_child("ResetProgressButton", true, false)
	if reset_btn:
		reset_btn.pressed.connect(func():
			AudioManager.play_sfx("upgrade")
			SaveManager.reset_game()
			_refresh_header_data()
			_refresh_featured_hero()
		)
	var close_settings_btn: Button = settings_modal.find_child("CloseSettingsButton", true, false)
	if close_settings_btn:
		close_settings_btn.pressed.connect(func():
			AudioManager.play_sfx("click")
			settings_modal.visible = false
		)

func _refresh_header_data() -> void:
	if base_level_label:
		base_level_label.text = "Base Nível %d" % ProgressionManager.get_base_level()
		
	if base_exp_bar:
		base_exp_bar.max_value = ProgressionManager.get_base_exp_needed()
		base_exp_bar.value = ProgressionManager.get_base_exp()
		
	if coins_label:
		coins_label.text = str(ProgressionManager.get_coins())
		
	if gems_label:
		gems_label.text = str(ProgressionManager.get_gems())

func _refresh_featured_hero() -> void:
	var team: Array = ProgressionManager.get_team_slots()
	var lead_id: String = team[0] if team.size() > 0 and team[0] != "" else "kael"
	var def: CharacterData = ProgressionManager.get_character_def(lead_id)
	if not def:
		return
		
	if featured_hero_name:
		featured_hero_name.text = def.name
	if featured_hero_title:
		featured_hero_title.text = def.title
	if featured_hero_rarity:
		featured_hero_rarity.text = "✦ %s ✦" % def.rarity_name.to_upper()
		featured_hero_rarity.add_theme_color_override("font_color", def.rarity_color)
	if featured_hero_avatar and ResourceLoader.exists(def.avatar_path):
		featured_hero_avatar.texture = load(def.avatar_path)

func _close_all_subscreens() -> void:
	if stage_select_modal: stage_select_modal.visible = false
	if character_roster_screen: character_roster_screen.visible = false
	if base_upgrade_screen: base_upgrade_screen.visible = false
	if quests_modal: quests_modal.visible = false
	if settings_modal: settings_modal.visible = false

func _populate_stages_list() -> void:
	var list_container: VBoxContainer = stage_select_modal.find_child("StagesList", true, false)
	if not list_container:
		return
		
	for child in list_container.get_children():
		child.queue_free()
		
	for s_id in range(1, 11):
		var data := StageData.create_stage(s_id)
		var is_unlocked := ProgressionManager.is_stage_unlocked(s_id)
		var is_completed := ProgressionManager.is_stage_completed(s_id)
		
		var stars_dict: Dictionary = SaveManager.current_save.get("stage_stars", {})
		var stars: int = stars_dict.get(str(s_id), 0)
		var star_str := (" " + "★".repeat(stars)) if stars > 0 else ""
		
		var card := PanelContainer.new()
		card.custom_minimum_size = Vector2(0, 85)
		
		var st := StyleBoxFlat.new()
		st.bg_color = Color(0.14, 0.17, 0.25, 0.95) if is_unlocked else Color(0.10, 0.12, 0.16, 0.8)
		st.border_color = Color(0.4, 0.7, 1.0, 0.8) if is_unlocked else Color(0.2, 0.25, 0.35, 0.4)
		st.set_border_width_all(2)
		st.set_corner_radius_all(8)
		card.add_theme_stylebox_override("panel", st)
		
		var hbox := HBoxContainer.new()
		hbox.add_theme_constant_override("separation", 14)
		card.add_child(hbox)
		
		var vbox := VBoxContainer.new()
		vbox.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		hbox.add_child(vbox)
		
		var title_lbl := Label.new()
		title_lbl.text = "%s%s" % [data.title, star_str]
		title_lbl.add_theme_font_size_override("font_size", 15)
		title_lbl.add_theme_color_override("font_color", Color(0.96, 0.82, 0.4, 1.0) if is_unlocked else Color(0.5, 0.55, 0.65, 1.0))
		vbox.add_child(title_lbl)
		
		var desc_lbl := Label.new()
		desc_lbl.text = "Cenário: %s | %s" % [data.scenario_name, data.description]
		desc_lbl.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		desc_lbl.add_theme_font_size_override("font_size", 11)
		desc_lbl.add_theme_color_override("font_color", Color(0.8, 0.85, 0.9, 0.9))
		vbox.add_child(desc_lbl)
		
		var info_lbl := Label.new()
		info_lbl.text = "Ondas: %d  |  + %d Ouro  |  +5 Gemas" % [data.waves.size(), data.reward_coins]
		info_lbl.add_theme_font_size_override("font_size", 11)
		info_lbl.add_theme_color_override("font_color", Color(0.4, 0.9, 1.0, 1.0))
		vbox.add_child(info_lbl)
		
		var btn := Button.new()
		btn.custom_minimum_size = Vector2(130, 48)
		btn.size_flags_vertical = Control.SIZE_SHRINK_CENTER
		
		if not is_unlocked:
			btn.text = "🔒 Bloqueado"
			btn.disabled = true
		elif is_completed:
			btn.text = "Repetir"
		else:
			btn.text = "Batalhar"
			
		btn.pressed.connect(func():
			AudioManager.play_sfx("click")
			GameStateManager.start_combat(s_id)
		)
		hbox.add_child(btn)
		list_container.add_child(card)

func _populate_quests_list() -> void:
	if not quests_modal:
		return
	var list_container: VBoxContainer = quests_modal.find_child("QuestsList", true, false)
	if not list_container:
		return
		
	for child in list_container.get_children():
		child.queue_free()
		
	var quests: Dictionary = ProgressionManager.get_quests()
	for q_id in quests.keys():
		var q: Dictionary = quests[q_id]
		var card := PanelContainer.new()
		card.custom_minimum_size = Vector2(0, 80)
		
		var st := StyleBoxFlat.new()
		var is_done := (q.get("progress", 0) >= q.get("target", 1))
		st.bg_color = Color(0.12, 0.18, 0.28, 0.95) if is_done else Color(0.11, 0.13, 0.18, 0.9)
		st.border_color = Color(0.2, 0.8, 0.4, 0.9) if is_done else Color(0.3, 0.4, 0.5, 0.5)
		st.set_border_width_all(2)
		st.set_corner_radius_all(8)
		card.add_theme_stylebox_override("panel", st)
		
		var hbox := HBoxContainer.new()
		hbox.add_theme_constant_override("separation", 12)
		card.add_child(hbox)
		
		var vbox := VBoxContainer.new()
		vbox.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		hbox.add_child(vbox)
		
		var title_lbl := Label.new()
		title_lbl.text = q.get("title", "Missão")
		title_lbl.add_theme_font_size_override("font_size", 15)
		title_lbl.add_theme_color_override("font_color", Color(1.0, 0.85, 0.3, 1.0))
		vbox.add_child(title_lbl)
		
		var desc_lbl := Label.new()
		desc_lbl.text = q.get("desc", "")
		desc_lbl.add_theme_font_size_override("font_size", 11)
		vbox.add_child(desc_lbl)
		
		var p_bar := ProgressBar.new()
		p_bar.custom_minimum_size = Vector2(0, 14)
		p_bar.max_value = q.get("target", 1)
		p_bar.value = q.get("progress", 0)
		p_bar.show_percentage = true
		vbox.add_child(p_bar)
		
		var claim_btn := Button.new()
		claim_btn.custom_minimum_size = Vector2(130, 44)
		claim_btn.size_flags_vertical = Control.SIZE_SHRINK_CENTER
		
		if q.get("claimed", false):
			claim_btn.text = "✓ Concluído"
			claim_btn.disabled = true
		elif is_done:
			claim_btn.text = "Coletar (%d Ouro, %d Gemas)" % [q.get("reward_coins", 0), q.get("reward_gems", 0)]
			claim_btn.pressed.connect(func():
				AudioManager.play_sfx("upgrade")
				ProgressionManager.claim_quest_reward(q_id)
				_populate_quests_list()
			)
		else:
			claim_btn.text = "%d / %d" % [q.get("progress", 0), q.get("target", 1)]
			claim_btn.disabled = true
			
		hbox.add_child(claim_btn)
		list_container.add_child(card)

func _on_play_pressed() -> void:
	AudioManager.play_sfx("click")
	_populate_stages_list()
	if stage_select_modal:
		stage_select_modal.visible = true

func _on_stages_pressed() -> void:
	AudioManager.play_sfx("click")
	_populate_stages_list()
	if stage_select_modal:
		stage_select_modal.visible = true

func _on_characters_pressed() -> void:
	AudioManager.play_sfx("click")
	_close_all_subscreens()
	if character_roster_screen:
		character_roster_screen.visible = true
		if character_roster_screen.has_method("refresh_roster"):
			character_roster_screen.refresh_roster()

func _on_base_upgrade_pressed() -> void:
	AudioManager.play_sfx("click")
	_close_all_subscreens()
	if base_upgrade_screen:
		base_upgrade_screen.visible = true
		if base_upgrade_screen.has_method("refresh_structures"):
			base_upgrade_screen.refresh_structures()

func _on_quests_pressed() -> void:
	AudioManager.play_sfx("click")
	_populate_quests_list()
	if quests_modal:
		quests_modal.visible = true

func _on_settings_pressed() -> void:
	AudioManager.play_sfx("click")
	if settings_modal:
		settings_modal.visible = true
