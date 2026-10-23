class_name BaseUpgradeUI
extends Control

## Tela de Gestão e Evolução da Base Central
## Controla Centro de Comando, Oficina Bélica, Academia Mística e Tesouraria Real.

@onready var close_button: Button = $Margin/VBox/TopBar/CloseButton
@onready var coins_label: Label = $Margin/VBox/TopBar/CoinsLabel
@onready var structures_container: VBoxContainer = $Margin/VBox/Scroll/StructuresList
@onready var feedback_label: Label = $Margin/VBox/FeedbackLabel

func _ready() -> void:
	close_button.pressed.connect(_on_close_pressed)
	ProgressionManager.structure_upgraded.connect(func(_s, _l): refresh_structures())
	ProgressionManager.coins_changed.connect(func(_c): _update_coins())
	refresh_structures()

func _update_coins() -> void:
	if coins_label:
		coins_label.text = "%d Ouro" % ProgressionManager.get_coins()

func refresh_structures() -> void:
	_update_coins()
	for child in structures_container.get_children():
		child.queue_free()
		
	var structure_ids := ["command_center", "workshop", "academy", "treasury"]
	for id in structure_ids:
		_create_structure_card(id)

func _create_structure_card(struct_id: String) -> void:
	var def: Dictionary = ProgressionManager.structure_definitions.get(struct_id, {})
	var cur_lvl := ProgressionManager.get_structure_level(struct_id)
	var max_lvl := ProgressionManager.MAX_STRUCTURE_LEVEL
	var cost := ProgressionManager.get_structure_upgrade_cost(struct_id)
	var can_up := ProgressionManager.can_upgrade_structure(struct_id)
	var coins := ProgressionManager.get_coins()
	
	var card := PanelContainer.new()
	card.custom_minimum_size = Vector2(0, 110)
	
	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.14, 0.17, 0.25, 0.95)
	style.border_color = Color(0.3, 0.5, 0.8, 0.7)
	style.set_border_width_all(2)
	style.set_corner_radius_all(8)
	card.add_theme_stylebox_override("panel", style)
	
	var hbox := HBoxContainer.new()
	hbox.add_theme_constant_override("separation", 18)
	card.add_child(hbox)
	
	# Ícone
	var icon_rect := TextureRect.new()
	icon_rect.custom_minimum_size = Vector2(80, 80)
	icon_rect.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
	icon_rect.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
	var icon_path: String = def.get("icon", "")
	if ResourceLoader.exists(icon_path):
		icon_rect.texture = load(icon_path)
	hbox.add_child(icon_rect)
	
	# Detalhes / Textos
	var vbox := VBoxContainer.new()
	vbox.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	hbox.add_child(vbox)
	
	var title_lbl := Label.new()
	title_lbl.text = "%s — Nível %d / %d" % [def.get("name", "Estrutura"), cur_lvl, max_lvl]
	title_lbl.add_theme_font_size_override("font_size", 18)
	title_lbl.add_theme_color_override("font_color", Color(1.0, 0.85, 0.3, 1.0))
	vbox.add_child(title_lbl)
	
	var desc_lbl := Label.new()
	desc_lbl.text = def.get("description", "")
	desc_lbl.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	desc_lbl.add_theme_font_size_override("font_size", 12)
	desc_lbl.add_theme_color_override("font_color", Color(0.8, 0.85, 0.9, 1.0))
	vbox.add_child(desc_lbl)
	
	var bonus_lbl := Label.new()
	var current_bonus_str := _get_current_bonus_text(struct_id, cur_lvl)
	bonus_lbl.text = "Efeito Atual: %s (%s)" % [current_bonus_str, def.get("perk_desc", "")]
	bonus_lbl.add_theme_font_size_override("font_size", 12)
	bonus_lbl.add_theme_color_override("font_color", Color(0.4, 0.9, 1.0, 1.0))
	vbox.add_child(bonus_lbl)
	
	# Botão de Upgrade
	var btn_vbox := VBoxContainer.new()
	btn_vbox.alignment = BoxContainer.ALIGNMENT_CENTER
	hbox.add_child(btn_vbox)
	
	var up_btn := Button.new()
	up_btn.custom_minimum_size = Vector2(180, 50)
	
	if cur_lvl >= max_lvl:
		up_btn.text = "Nível Máximo"
		up_btn.disabled = true
	else:
		up_btn.text = "Melhorar (%d Ouro)" % cost
		up_btn.disabled = not can_up
		
	up_btn.pressed.connect(func(): _on_upgrade_structure_clicked(struct_id))
	btn_vbox.add_child(up_btn)
	
	# Se não puder melhorar por requisito de Centro de Comando
	if cur_lvl < max_lvl and struct_id != "command_center":
		var cc_lvl := ProgressionManager.get_structure_level("command_center")
		if cur_lvl >= cc_lvl:
			var req_lbl := Label.new()
			req_lbl.text = "Requer Centro Nv. %d" % (cur_lvl + 1)
			req_lbl.add_theme_font_size_override("font_size", 10)
			req_lbl.add_theme_color_override("font_color", Color(1.0, 0.4, 0.4, 1.0))
			btn_vbox.add_child(req_lbl)
			
	structures_container.add_child(card)

func _get_current_bonus_text(struct_id: String, lvl: int) -> String:
	match struct_id:
		"command_center":
			var slots: int = ProgressionManager.BASE_SLOTS_TABLE.get(lvl, 1)
			return "%d Espaços de Defesa" % slots
		"workshop":
			return "+%.1f%% Dano Base" % ((lvl - 1) * 5.0)
		"academy":
			return "+%.1f%% Dano Hab. / -%.1f%% Recarga" % [((lvl - 1) * 6.0), ((lvl - 1) * 3.0)]
		"treasury":
			return "+%d%% Moedas Extras" % ((lvl - 1) * 10)
	return ""

func _on_upgrade_structure_clicked(struct_id: String) -> void:
	var cost := ProgressionManager.get_structure_upgrade_cost(struct_id)
	if ProgressionManager.upgrade_structure(struct_id):
		AudioManager.play_sfx("upgrade")
		if struct_id == "command_center":
			ProgressionManager.update_quest_progress("quest_base_lv2", 1)
		feedback_label.text = "Estrutura melhorada com sucesso! (-%d Ouro)" % cost
		feedback_label.modulate = Color(0.4, 1.2, 0.4, 1.0)
		refresh_structures()
	else:
		feedback_label.text = "Não foi possível melhorar a estrutura."
		feedback_label.modulate = Color(1.5, 0.3, 0.3, 1.0)

func _on_close_pressed() -> void:
	AudioManager.play_sfx("click")
	visible = false
