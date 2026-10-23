extends Node

## Gerenciador de Salvamento Local (Singleton Autoload)
## Persiste o progresso do jogador em formato JSON localmente (user://savegame.json).

const SAVE_FILE_PATH: String = "user://savegame.json"

signal save_loaded()
signal save_updated()

var current_save: Dictionary = {}

func _ready() -> void:
	load_game()

func get_default_data() -> Dictionary:
	return {
		"version": 2,
		"coins": 350,
		"gems": 30,
		"base_level": 1,
		"base_exp": 0,
		"base_exp_needed": 100,
		"structures": {
			"command_center": 1,
			"workshop": 1,
			"academy": 1,
			"treasury": 1
		},
		"characters": {
			"kael": {"unlocked": true, "level": 1},
			"lyra": {"unlocked": false, "level": 1},
			"thorne": {"unlocked": false, "level": 1},
			"elysia": {"unlocked": false, "level": 1},
			"boran": {"unlocked": false, "level": 1},
			"vesper": {"unlocked": false, "level": 1}
		},
		"team_slots": ["kael"],
		"completed_stages": [],
		"stage_stars": {},
		"quests": {
			"quest_stage_1": {"title": "Primeira Linha de Defesa", "desc": "Conclua a Fase 1 da Campanha", "target": 1, "progress": 0, "claimed": false, "reward_coins": 150, "reward_gems": 5},
			"quest_kill_50": {"title": "Caçador de Monstros", "desc": "Derrote 50 invasores em batalha", "target": 50, "progress": 0, "claimed": false, "reward_coins": 180, "reward_gems": 8},
			"quest_skills_10": {"title": "Poder Ancestral", "desc": "Use 10 habilidades especiais de heróis", "target": 10, "progress": 0, "claimed": false, "reward_coins": 200, "reward_gems": 10},
			"quest_base_lv2": {"title": "Fortaleza Inabalável", "desc": "Evolua o Centro de Comando para Nível 2", "target": 2, "progress": 1, "claimed": false, "reward_coins": 250, "reward_gems": 15},
			"quest_boss_ogre": {"title": "Assassino de Titãs", "desc": "Derrote o Ogro das Ruínas", "target": 1, "progress": 0, "claimed": false, "reward_coins": 350, "reward_gems": 25}
		},
		"settings": {
			"music_volume": 0.8,
			"sfx_volume": 0.8,
			"vibration": true
		},
		"stats": {
			"enemies_killed": 0,
			"bosses_defeated": 0,
			"skills_used": 0,
			"total_coins_earned": 350,
			"total_gems_earned": 30
		}
	}

func load_game() -> void:
	if not FileAccess.file_exists(SAVE_FILE_PATH):
		current_save = get_default_data()
		save_game()
		save_loaded.emit()
		return
	
	var file := FileAccess.open(SAVE_FILE_PATH, FileAccess.READ)
	if not file:
		current_save = get_default_data()
		save_loaded.emit()
		return
		
	var content := file.get_as_text()
	var json := JSON.new()
	var error := json.parse(content)
	
	if error == OK and json.data is Dictionary:
		current_save = _merge_defaults(json.data, get_default_data())
	else:
		current_save = get_default_data()
		save_game()
		
	save_loaded.emit()

func save_game() -> bool:
	var file := FileAccess.open(SAVE_FILE_PATH, FileAccess.WRITE)
	if not file:
		push_error("Falha ao abrir arquivo para salvar: " + SAVE_FILE_PATH)
		return false
		
	var json_string := JSON.stringify(current_save, "\t")
	file.store_string(json_string)
	save_updated.emit()
	return true

func reset_game() -> void:
	current_save = get_default_data()
	save_game()
	save_loaded.emit()

func _merge_defaults(loaded: Dictionary, defaults: Dictionary) -> Dictionary:
	var merged := defaults.duplicate(true)
	for key in loaded.keys():
		if merged.has(key):
			if loaded[key] is Dictionary and merged[key] is Dictionary:
				merged[key] = _merge_dict(loaded[key], merged[key])
			else:
				merged[key] = loaded[key]
		else:
			merged[key] = loaded[key]
	return merged

func _merge_dict(source: Dictionary, target: Dictionary) -> Dictionary:
	var res := target.duplicate(true)
	for k in source.keys():
		if res.has(k) and source[k] is Dictionary and res[k] is Dictionary:
			res[k] = _merge_dict(source[k], res[k])
		else:
			res[k] = source[k]
	return res
