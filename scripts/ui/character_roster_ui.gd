class_name CharacterRosterUI
extends Control

## Tela de Personagens, Raridades, Loja e Seleção de Equipe (Mobile Vertical)

@onready var character_list_container: VBoxContainer = $Margin/VBox/Scroll/CharList
@onready var close_button: Button = $Margin/VBox/Header/BackButton

# Filtros de Raridade
@onready var filter_all_btn: Button = $Margin/VBox/FilterBar/FilterAll
@onready var filter_common_btn: Button = $Margin/VBox/FilterBar/FilterCommon
@onready var filter_rare_btn: Button = $Margin/VBox/FilterBar/FilterRare
@onready var filter_epic_btn: Button = $Margin/VBox/FilterBar/FilterEpic
@onready var filter_legendary_btn: Button = $Margin/VBox/FilterBar/FilterLegendary

# Painel Central de Detalhes
@onready var char_name_label: Label = $Margin/VBox/DetailPanel/Header/NameLabel
@onready var char_title_label: Label = $Margin/VBox/DetailPanel/Header/TitleLabel
@onready var char_rarity_label: Label = $Margin/VBox/DetailPanel/Header/RarityLabel
@onready var char_avatar: TextureRect = $Margin/VBox/DetailPanel/Header/AvatarRect
@onready var char_role_label: Label = $Margin/VBox/DetailPanel/Header/RoleLabel
@onready var char_desc_label: Label = $Margin/VBox/DetailPanel/DescriptionLabel

# Atributos
@onready var level_label: Label = $Margin/VBox/DetailPanel/StatsGrid/LevelLabel
@onready var damage_label: Label = $Margin/VBox/DetailPanel/StatsGrid/DamageLabel
@onready var speed_label: Label = $Margin/VBox/DetailPanel/StatsGrid/SpeedLabel
@onready var range_label: Label = $Margin/VBox/DetailPanel/StatsGrid/RangeLabel
@onready var skill_damage_label: Label = $Margin/VBox/DetailPanel/StatsGrid/SkillDamageLabel

# Painel de Habilidades
@onready var skill_1_name: Label = $Margin/VBox/DetailPanel/SkillsBox/Skill1/Name
@onready var skill_1_desc: Label = $Margin/VBox/DetailPanel/SkillsBox/Skill1/Desc
@onready var skill_2_name: Label = $Margin/VBox/DetailPanel/SkillsBox/Skill2/Name
@onready var skill_2_desc: Label = $Margin/VBox/DetailPanel/SkillsBox/Skill2/Desc

# Ações
@onready var upgrade_button: Button = $Margin/VBox/DetailPanel/Actions/UpgradeButton
@onready var equip_button: Button = $Margin/VBox/DetailPanel/Actions/EquipButton
@onready var unlock_button: Button = $Margin/VBox/DetailPanel/Actions/UnlockButton
@onready var team_slots_label: Label = $Margin/VBox/DetailPanel/Actions/SlotsLabel
@onready var feedback_label: Label = $Margin/VBox/DetailPanel/FeedbackLabel

var selected_char_id: String = "kael"
var current_rarity_filter: String = "all"

func _ready() -> void:
	close_button.pressed.connect(_on_close_pressed)
	upgrade_button.pressed.connect(_on_upgrade_pressed)
	equip_button.pressed.connect(_on_equip_pressed)
	unlock_button.pressed.connect(_on_unlock_pressed)
	
	if filter_all_btn: filter_all_btn.pressed.connect(func(): _set_rarity_filter("all"))
	if filter_common_btn: filter_common_btn.pressed.connect(func(): _set_rarity_filter("common"))
	if filter_rare_btn: filter_rare_btn.pressed.connect(func(): _set_rarity_filter("rare"))
	if filter_epic_btn: filter_epic_btn.pressed.connect(func(): _set_rarity_filter("epic"))
	if filter_legendary_btn: filter_legendary_btn.pressed.connect(func(): _set_rarity_filter("legendary"))
	
	ProgressionManager.character_upgraded.connect(func(_c, _l): refresh_roster())
	ProgressionManager.character_unlocked.connect(func(_c): refresh_roster())
	ProgressionManager.team_updated.connect(func(_t): refresh_roster())
	ProgressionManager.coins_changed.connect(func(_c): _update_action_buttons())
	ProgressionManager.gems_changed.connect(func(_g): _update_action_buttons())

func _set_rarity_filter(filter_name: String) -> void:
	current_rarity_filter = filter_name
	AudioManager.play_sfx("click")
	_build_character_list()

func refresh_roster() -> void:
	_build_character_list()
	select_character(selected_char_id)

func _build_character_list() -> void:
	for child in character_list_container.get_children():
		child.queue_free()
		
	var all_ids := ["kael", "lyra", "thorne", "elysia", "boran", "vesper"]
	for id in all_ids:
		var char_data: CharacterData = ProgressionManager.get_character_def(id)
		if not char_data:
			continue
			
		if current_rarity_filter != "all" and char_data.rarity != current_rarity_filter:
			continue
			
		var is_unlocked := ProgressionManager.is_character_unlocked(id)
		var btn := Button.new()
		btn.custom_minimum_size = Vector2(0, 58)
		btn.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		
		var team: Array = ProgressionManager.get_team_slots()
		var in_team := team.has(id)
		var status_str := "  [EM COMBATE]" if in_team else ""
		
		if is_unlocked:
			var lvl := ProgressionManager.get_character_level(id)
			btn.text = "★ %s (Nv. %d) — %s%s" % [char_data.name, lvl, char_data.rarity_name, status_str]
		else:
			btn.text = "🔒 %s [%s] — Requer Nv. Base %d" % [char_data.name, char_data.rarity_name, char_data.unlock_base_level]
			
		var style := StyleBoxFlat.new()
		style.bg_color = Color(0.14, 0.17, 0.24, 0.95) if is_unlocked else Color(0.10, 0.11, 0.15, 0.85)
		style.border_color = char_data.rarity_color if id == selected_char_id else char_data.rarity_color.darkened(0.5)
		style.set_border_width_all(2 if id == selected_char_id else 1)
		style.set_corner_radius_all(8)
		btn.add_theme_stylebox_override("normal", style)
		
		btn.pressed.connect(func(): 
			AudioManager.play_sfx("click")
			select_character(id)
		)
		character_list_container.add_child(btn)

func select_character(char_id: String) -> void:
	selected_char_id = char_id
	var char_data: CharacterData = ProgressionManager.get_character_def(char_id)
	if not char_data:
		return
		
	var is_unlocked := ProgressionManager.is_character_unlocked(char_id)
	var lvl := ProgressionManager.get_character_level(char_id)
	
	char_name_label.text = char_data.name
	char_title_label.text = char_data.title
	char_rarity_label.text = "[ %s ]" % char_data.rarity_name.to_upper()
	char_rarity_label.add_theme_color_override("font_color", char_data.rarity_color)
	char_role_label.text = "Função: %s" % char_data.role
	char_desc_label.text = char_data.description
	
	if ResourceLoader.exists(char_data.avatar_path):
		char_avatar.texture = load(char_data.avatar_path)
		
	# Atributos atuais e preview do próximo nível
	var ws_bonus := ProgressionManager.get_workshop_bonus_pct()
	var ac_dmg := ProgressionManager.get_academy_damage_bonus_pct()
	
	var cur_dmg := char_data.calculate_damage(lvl, ws_bonus)
	var cur_spd := char_data.calculate_attack_speed(lvl, ws_bonus)
	var cur_rng := char_data.calculate_range(lvl)
	var cur_sk_dmg := char_data.calculate_skill_damage(lvl, ac_dmg)
	
	if lvl < CharacterData.MAX_LEVEL:
		var nxt_dmg := char_data.calculate_damage(lvl + 1, ws_bonus)
		var nxt_spd := char_data.calculate_attack_speed(lvl + 1, ws_bonus)
		var nxt_rng := char_data.calculate_range(lvl + 1)
		var nxt_sk_dmg := char_data.calculate_skill_damage(lvl + 1, ac_dmg)
		
		level_label.text = "Nível: %d -> %d" % [lvl, lvl + 1]
		damage_label.text = "Dano: %.1f -> %.1f" % [cur_dmg, nxt_dmg]
		speed_label.text = "Vel. Ataque: %.2f -> %.2f/s" % [cur_spd, nxt_spd]
		range_label.text = "Alcance: %d -> %d" % [int(cur_rng), int(nxt_rng)]
		skill_damage_label.text = "Dano Habilidade: %.1f -> %.1f" % [cur_sk_dmg, nxt_sk_dmg]
	else:
		level_label.text = "Nível: %d (MÁXIMO)" % lvl
		damage_label.text = "Dano: %.1f" % cur_dmg
		speed_label.text = "Vel. Ataque: %.2f/s" % cur_spd
		range_label.text = "Alcance: %d" % int(cur_rng)
		skill_damage_label.text = "Dano Habilidade: %.1f" % cur_sk_dmg
		
	# Habilidades
	if char_data.skills.size() > 0:
		skill_1_name.text = "1. " + char_data.skills[0].get("name", "")
		skill_1_desc.text = char_data.skills[0].get("description", "")
	if char_data.skills.size() > 1:
		skill_2_name.text = "2. " + char_data.skills[1].get("name", "")
		skill_2_desc.text = char_data.skills[1].get("description", "")
		
	_update_action_buttons()

func _update_action_buttons() -> void:
	var char_data: CharacterData = ProgressionManager.get_character_def(selected_char_id)
	if not char_data:
		return
		
	var is_unlocked := ProgressionManager.is_character_unlocked(selected_char_id)
	var lvl := ProgressionManager.get_character_level(selected_char_id)
	var coins := ProgressionManager.get_coins()
	var gems := ProgressionManager.get_gems()
	var team: Array = ProgressionManager.get_team_slots()
	var unlocked_slots := ProgressionManager.get_unlocked_defense_slots()
	
	team_slots_label.text = "Espaços de Defesa: %d / %d Ocupados" % [team.size(), unlocked_slots]
	feedback_label.text = ""
	
	if not is_unlocked:
		upgrade_button.visible = false
		equip_button.visible = false
		unlock_button.visible = true
		
		var req_base: int = char_data.unlock_base_level
		var base_lvl := ProgressionManager.get_base_level()
		var can_recruit := ProgressionManager.can_recruit_character(selected_char_id)
		
		unlock_button.text = "Recrutar (%d Ouro, %d Gemas) — Requer Base Nv. %d" % [char_data.recruit_cost_coins, char_data.recruit_cost_gems, req_base]
		unlock_button.disabled = not can_recruit
	else:
		unlock_button.visible = false
		upgrade_button.visible = true
		equip_button.visible = true
		
		# Botão de Upgrade
		if lvl < CharacterData.MAX_LEVEL:
			var cost := ProgressionManager.get_character_upgrade_cost(selected_char_id)
			upgrade_button.text = "Melhorar Nível (%d Ouro)" % cost
			upgrade_button.disabled = (coins < cost)
		else:
			upgrade_button.text = "Nível Máximo Atingido"
			upgrade_button.disabled = true
			
		# Botão de Equipar
		var in_team := team.has(selected_char_id)
		if in_team:
			equip_button.text = "Remover da Equipe"
			equip_button.disabled = (team.size() <= 1) # Mantém pelo menos 1 herói
		else:
			equip_button.text = "Adicionar à Equipe de Defesa"
			equip_button.disabled = (team.size() >= unlocked_slots)

func _on_upgrade_pressed() -> void:
	var cost := ProgressionManager.get_character_upgrade_cost(selected_char_id)
	if ProgressionManager.upgrade_character(selected_char_id):
		AudioManager.play_sfx("upgrade")
		feedback_label.text = "Herói evoluído com sucesso! (-%d Ouro)" % cost
		feedback_label.modulate = Color(0.4, 1.2, 0.4, 1.0)
		select_character(selected_char_id)
		_build_character_list()
	else:
		feedback_label.text = "Recursos insuficientes para melhorar!"
		feedback_label.modulate = Color(1.5, 0.3, 0.3, 1.0)

func _on_equip_pressed() -> void:
	AudioManager.play_sfx("click")
	var team: Array = ProgressionManager.get_team_slots().duplicate()
	if team.has(selected_char_id):
		team.erase(selected_char_id)
		ProgressionManager.set_team_slots(team)
		feedback_label.text = "Personagem removido da defesa."
	else:
		var unlocked_slots := ProgressionManager.get_unlocked_defense_slots()
		if team.size() < unlocked_slots:
			team.append(selected_char_id)
			ProgressionManager.set_team_slots(team)
			feedback_label.text = "Personagem equipado para a defesa!"
			
	select_character(selected_char_id)
	_build_character_list()

func _on_unlock_pressed() -> void:
	if ProgressionManager.recruit_character(selected_char_id):
		AudioManager.play_sfx("upgrade")
		feedback_label.text = "Herói recrutado para o seu exército!"
		feedback_label.modulate = Color(0.4, 1.2, 0.4, 1.0)
		select_character(selected_char_id)
		_build_character_list()

func _on_close_pressed() -> void:
	AudioManager.play_sfx("click")
	visible = false
