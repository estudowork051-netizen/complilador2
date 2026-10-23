extends Node

## Gerenciador Central de Progressão e Economia (Singleton Autoload)
## Controla moedas, evolução de personagens, estruturas da base central e seleção de equipe.

signal coins_changed(new_amount: int)
signal gems_changed(new_amount: int)
signal base_exp_changed(current_exp: int, needed_exp: int, level: int)
signal character_upgraded(char_id: String, new_level: int)
signal character_unlocked(char_id: String)
signal structure_upgraded(structure_id: String, new_level: int)
signal team_updated(new_team: Array)
signal defense_slot_unlocked(new_total_slots: int)
signal quest_updated(quest_id: String)

const MAX_BASE_LEVEL: int = 10
const MAX_STRUCTURE_LEVEL: int = 10

# Tabela Oficial de Progressão de Espaços por Nível da Base:
# Níveis 1-2: 1 espaço
# Níveis 3-4: 2 espaços (Kael e Lyra)
# Níveis 5-6: 3 espaços
# Níveis 7-8: 4 espaços
# Níveis 9-10: 5 espaços
const BASE_SLOTS_TABLE: Dictionary = {
	1: 1,
	2: 1,
	3: 2,
	4: 2,
	5: 3,
	6: 3,
	7: 4,
	8: 4,
	9: 5,
	10: 5
}

# Custos de evolução de estruturas da base (nível 1->2 até 9->10)
const STRUCTURE_UPGRADE_COSTS: Array[int] = [
	120,  # 1 -> 2
	260,  # 2 -> 3
	480,  # 3 -> 4
	800,  # 4 -> 5
	1250, # 5 -> 6
	1850, # 6 -> 7
	2600, # 7 -> 8
	3600, # 8 -> 9
	4900  # 9 -> 10
]

# Dados e catálogos
var character_definitions: Dictionary = {}
var structure_definitions: Dictionary = {}

func _ready() -> void:
	_init_catalogs()

func _init_catalogs() -> void:
	# Registro dos 6 personagens do universo do jogo
	character_definitions["kael"] = CharacterData.create_kael()
	character_definitions["lyra"] = CharacterData.create_lyra()
	character_definitions["thorne"] = CharacterData.create_thorne()
	character_definitions["elysia"] = CharacterData.create_elysia()
	character_definitions["boran"] = CharacterData.create_boran()
	character_definitions["vesper"] = CharacterData.create_vesper()
	
	# Registro das 4 estruturas da Base Central
	structure_definitions["command_center"] = {
		"name": "Centro de Comando",
		"description": "Núcleo estratégico da fortaleza. Controla o nível geral da base e expande os espaços de defesa para posicionar heróis.",
		"icon": "res://assets/icons/base.svg",
		"perk_desc": "Libera novos espaços de combate nos níveis 3, 5, 7 e 9."
	}
	structure_definitions["workshop"] = {
		"name": "Oficina Bélica",
		"description": "Forja equipamentos arcanos e aprimoramentos para os defensores.",
		"icon": "res://assets/icons/upgrade.svg",
		"perk_desc": "+5% de dano e +2.5% de velocidade de ataque básica por nível."
	}
	structure_definitions["academy"] = {
		"name": "Academia Mística",
		"description": "Estudo aprofundado dos feitiços e técnicas de combate.",
		"icon": "res://assets/icons/skill_arcane_blast.svg",
		"perk_desc": "+6% de dano das habilidades e -3% de tempo de recarga por nível."
	}
	structure_definitions["treasury"] = {
		"name": "Tesouraria Real",
		"description": "Gerencia o fluxo de recursos, espólios e recompensas de batalha.",
		"icon": "res://assets/icons/coin.svg",
		"perk_desc": "+10% de moedas obtidas por inimigos e fases completadas por nível."
	}

# --- ECONOMIA: MOEDAS ---
func get_coins() -> int:
	return SaveManager.current_save.get("coins", 0)

func add_coins(amount: int, apply_treasury_bonus: bool = false) -> void:
	if amount <= 0:
		return
	
	var final_amount: int = amount
	if apply_treasury_bonus:
		var bonus_pct: float = get_treasury_bonus_pct()
		final_amount = int(amount * (1.0 + bonus_pct / 100.0))
		
	var new_coins: int = get_coins() + final_amount
	SaveManager.current_save["coins"] = new_coins
	SaveManager.save_game()
	coins_changed.emit(new_coins)

func spend_coins(amount: int) -> bool:
	if amount < 0:
		return false
	var current := get_coins()
	if current < amount:
		return false
	
	SaveManager.current_save["coins"] = current - amount
	SaveManager.save_game()
	coins_changed.emit(current - amount)
	return true

# --- ECONOMIA: GEMAS ---
func get_gems() -> int:
	return SaveManager.current_save.get("gems", 30)

func add_gems(amount: int) -> void:
	if amount <= 0:
		return
	var new_gems: int = get_gems() + amount
	SaveManager.current_save["gems"] = new_gems
	SaveManager.save_game()
	gems_changed.emit(new_gems)

func spend_gems(amount: int) -> bool:
	if amount < 0:
		return false
	var current := get_gems()
	if current < amount:
		return false
	SaveManager.current_save["gems"] = current - amount
	SaveManager.save_game()
	gems_changed.emit(current - amount)
	return true

# --- BASE E ESTRUTURAS ---
func get_base_level() -> int:
	return SaveManager.current_save.get("base_level", 1)

func get_base_exp() -> int:
	return SaveManager.current_save.get("base_exp", 0)

func get_base_exp_needed() -> int:
	return SaveManager.current_save.get("base_exp_needed", 100)

func add_base_exp(amount: int) -> void:
	if amount <= 0:
		return
	var cur_exp: int = get_base_exp() + amount
	var cur_lvl: int = get_base_level()
	var old_lvl: int = cur_lvl
	var needed: int = get_base_exp_needed()
	
	while cur_exp >= needed and cur_lvl < MAX_BASE_LEVEL:
		cur_exp -= needed
		cur_lvl += 1
		needed = int(needed * 1.5)
	
	SaveManager.current_save["base_level"] = cur_lvl
	SaveManager.current_save["base_exp"] = cur_exp
	SaveManager.current_save["base_exp_needed"] = needed
	SaveManager.save_game()
	base_exp_changed.emit(cur_exp, needed, cur_lvl)
	
	if cur_lvl > old_lvl:
		_check_milestone_unlocks(cur_lvl, old_lvl)
		_ensure_valid_team_slots()

func get_unlocked_defense_slots() -> int:
	var lvl: int = get_base_level()
	return BASE_SLOTS_TABLE.get(lvl, 1)

func _check_milestone_unlocks(new_lvl: int, old_lvl: int) -> void:
	var old_slots: int = BASE_SLOTS_TABLE.get(old_lvl, 1)
	var new_slots: int = BASE_SLOTS_TABLE.get(new_lvl, 1)
	if new_slots > old_slots:
		defense_slot_unlocked.emit(new_slots)
	
	for lvl in range(old_lvl + 1, new_lvl + 1):
		match lvl:
			3:
				if not is_character_unlocked("lyra"):
					unlock_character_free("lyra")
			5:
				if not is_character_unlocked("thorne"):
					unlock_character_free("thorne")
			7:
				if not is_character_unlocked("elysia"):
					unlock_character_free("elysia")
			9:
				if not is_character_unlocked("boran"):
					unlock_character_free("boran")
			10:
				if not is_character_unlocked("vesper"):
					unlock_character_free("vesper")

func get_structure_level(structure_id: String) -> int:
	var struct_dict: Dictionary = SaveManager.current_save.get("structures", {})
	return struct_dict.get(structure_id, 1)

func get_structure_upgrade_cost(structure_id: String) -> int:
	var cur_level: int = get_structure_level(structure_id)
	if cur_level < 1 or cur_level >= MAX_STRUCTURE_LEVEL:
		return 0
	return STRUCTURE_UPGRADE_COSTS[cur_level - 1]

func can_upgrade_structure(structure_id: String) -> bool:
	var cur_level: int = get_structure_level(structure_id)
	if cur_level >= MAX_STRUCTURE_LEVEL:
		return false
	
	var cost := get_structure_upgrade_cost(structure_id)
	if get_coins() < cost:
		return false
		
	# Requisito de nível do Centro de Comando para as outras estruturas
	if structure_id != "command_center":
		var cc_level := get_structure_level("command_center")
		if cur_level >= cc_level:
			return false
			
	return true

func upgrade_structure(structure_id: String) -> bool:
	if not can_upgrade_structure(structure_id):
		return false
		
	var cost := get_structure_upgrade_cost(structure_id)
	if not spend_coins(cost):
		return false
		
	var cur_level: int = get_structure_level(structure_id)
	var new_level: int = cur_level + 1
	SaveManager.current_save["structures"][structure_id] = new_level
	
	if structure_id == "command_center":
		SaveManager.current_save["base_level"] = new_level
		# Verifica se precisa sincronizar slots
		_ensure_valid_team_slots()
		
	SaveManager.save_game()
	structure_upgraded.emit(structure_id, new_level)
	return true

# Bônus das estruturas
func get_workshop_bonus_pct() -> float:
	var lvl := get_structure_level("workshop")
	return float((lvl - 1) * 5.0)

func get_academy_damage_bonus_pct() -> float:
	var lvl := get_structure_level("academy")
	return float((lvl - 1) * 6.0)

func get_academy_cdr_pct() -> float:
	var lvl := get_structure_level("academy")
	return float((lvl - 1) * 3.0)

func get_treasury_bonus_pct() -> float:
	var lvl := get_structure_level("treasury")
	return float((lvl - 1) * 10.0)

# --- PERSONAGENS ---
func get_character_def(char_id: String) -> CharacterData:
	return character_definitions.get(char_id, null)

func is_character_unlocked(char_id: String) -> bool:
	var chars: Dictionary = SaveManager.current_save.get("characters", {})
	if chars.has(char_id):
		return chars[char_id].get("unlocked", false)
	return false

func get_character_level(char_id: String) -> int:
	var chars: Dictionary = SaveManager.current_save.get("characters", {})
	if chars.has(char_id):
		return chars[char_id].get("level", 1)
	return 1

func get_character_upgrade_cost(char_id: String) -> int:
	var lvl := get_character_level(char_id)
	return CharacterData.get_upgrade_cost(lvl)

func can_upgrade_character(char_id: String) -> bool:
	if not is_character_unlocked(char_id):
		return false
	var lvl := get_character_level(char_id)
	if lvl >= CharacterData.MAX_LEVEL:
		return false
	var cost := get_character_upgrade_cost(char_id)
	return get_coins() >= cost

func upgrade_character(char_id: String) -> bool:
	if not can_upgrade_character(char_id):
		return false
	
	var cost := get_character_upgrade_cost(char_id)
	if not spend_coins(cost):
		return false
		
	var chars: Dictionary = SaveManager.current_save.get("characters", {})
	var cur_lvl: int = chars[char_id].get("level", 1)
	var new_lvl: int = cur_lvl + 1
	chars[char_id]["level"] = new_lvl
	
	SaveManager.save_game()
	character_upgraded.emit(char_id, new_lvl)
	return true

# Requisitos de desbloqueio e recrutamento
func get_character_unlock_req(char_id: String) -> Dictionary:
	var def := get_character_def(char_id)
	if def != null:
		return {
			"base_level": def.unlock_base_level,
			"cost_coins": def.recruit_cost_coins,
			"cost_gems": def.recruit_cost_gems,
			"name": def.name,
			"rarity": def.rarity
		}
	return {"base_level": 1, "cost_coins": 0, "cost_gems": 0, "name": "", "rarity": "common"}

func can_unlock_character(char_id: String) -> bool:
	return can_recruit_character(char_id)

func can_recruit_character(char_id: String) -> bool:
	if is_character_unlocked(char_id):
		return false
	var def := get_character_def(char_id)
	if def == null:
		return false
	if get_base_level() < def.unlock_base_level:
		return false
	if get_coins() < def.recruit_cost_coins or get_gems() < def.recruit_cost_gems:
		return false
	return true

func recruit_character(char_id: String) -> bool:
	if not can_recruit_character(char_id):
		return false
	var def := get_character_def(char_id)
	if def == null:
		return false
	if def.recruit_cost_coins > 0:
		if not spend_coins(def.recruit_cost_coins):
			return false
	if def.recruit_cost_gems > 0:
		if not spend_gems(def.recruit_cost_gems):
			return false
	return unlock_character_free(char_id)

func unlock_character(char_id: String) -> bool:
	return recruit_character(char_id)

func unlock_character_free(char_id: String) -> bool:
	var chars: Dictionary = SaveManager.current_save.get("characters", {})
	if not chars.has(char_id):
		chars[char_id] = {"unlocked": true, "level": 1}
	else:
		chars[char_id]["unlocked"] = true
		chars[char_id]["level"] = 1
		
	SaveManager.save_game()
	character_unlocked.emit(char_id)
	return true

# --- MISSÕES LOCAIS ---
func get_quests() -> Dictionary:
	return SaveManager.current_save.get("quests", {})

func update_quest_progress(quest_id: String, amount: int = 1) -> void:
	var quests: Dictionary = get_quests()
	if not quests.has(quest_id):
		return
	var q: Dictionary = quests[quest_id]
	if q.get("claimed", false):
		return
	var cur: int = q.get("progress", 0)
	var target: int = q.get("target", 1)
	q["progress"] = mini(cur + amount, target)
	SaveManager.save_game()
	quest_updated.emit(quest_id)

func claim_quest_reward(quest_id: String) -> bool:
	var quests: Dictionary = get_quests()
	if not quests.has(quest_id):
		return false
	var q: Dictionary = quests[quest_id]
	if q.get("claimed", false):
		return false
	if q.get("progress", 0) < q.get("target", 1):
		return false
	q["claimed"] = true
	add_coins(q.get("reward_coins", 0))
	add_gems(q.get("reward_gems", 0))
	SaveManager.save_game()
	quest_updated.emit(quest_id)
	return true

# --- EQUIPE E ESPAÇOS DE DEFESA ---
func get_team_slots() -> Array:
	return SaveManager.current_save.get("team_slots", ["kael"])

func set_team_slots(new_slots: Array) -> bool:
	var max_slots := get_unlocked_defense_slots()
	if new_slots.size() > max_slots:
		return false
	
	# Verifica se todos estão desbloqueados e sem duplicatas
	var seen := {}
	for id in new_slots:
		if not is_character_unlocked(id):
			return false
		if seen.has(id):
			return false
		seen[id] = true
		
	SaveManager.current_save["team_slots"] = new_slots
	SaveManager.save_game()
	team_updated.emit(new_slots)
	return true

func assign_character_to_slot(char_id: String, slot_index: int) -> bool:
	if not is_character_unlocked(char_id):
		return false
	var max_slots := get_unlocked_defense_slots()
	if slot_index < 0 or slot_index >= max_slots:
		return false
		
	var team: Array = get_team_slots().duplicate()
	# Remove se já estava em outro slot
	var existing_idx = team.find(char_id)
	if existing_idx != -1:
		team.remove_at(existing_idx)
		
	# Preenche com vazios se necessário
	while team.size() <= slot_index:
		team.append("")
		
	team[slot_index] = char_id
	# Limpa strings vazias
	var cleaned := []
	for id in team:
		if id != "" and not cleaned.has(id):
			cleaned.append(id)
			
	return set_team_slots(cleaned)

func _ensure_valid_team_slots() -> void:
	var max_slots := get_unlocked_defense_slots()
	var team: Array = get_team_slots()
	if team.size() > max_slots:
		team = team.slice(0, max_slots)
		set_team_slots(team)

# --- PROGRESSÃO DE FASES (CAMPANHA 1 A 10) ---
func get_completed_stages() -> Array:
	return SaveManager.current_save.get("completed_stages", [])

func is_stage_completed(stage_id: int) -> bool:
	var completed: Array = get_completed_stages()
	return completed.has(stage_id)

func is_stage_unlocked(stage_id: int) -> bool:
	if stage_id == 1:
		return true
	var completed: Array = get_completed_stages()
	return completed.has(stage_id - 1)

func complete_stage(stage_id: int, base_hp_ratio: float) -> Dictionary:
	var stage_data := StageData.create_stage(stage_id)
	var coins_reward := stage_data.reward_coins
	var exp_reward := stage_data.reward_exp
	var bonus_coins := 0
	
	# Objetivo bônus: Defender a base com pelo menos 80% de HP restante
	var achieved_bonus := (base_hp_ratio >= 0.80)
	if achieved_bonus:
		bonus_coins = stage_data.bonus_reward_coins
		
	# Concede recompensas
	add_coins(coins_reward + bonus_coins, true)
	add_gems(5)
	add_base_exp(exp_reward)
	if stage_id == 1:
		update_quest_progress("quest_stage_1", 1)
	if stage_id == 5 or stage_id == 10:
		update_quest_progress("quest_boss_ogre", 1)
	
	# Registra fase concluída
	var completed: Array = get_completed_stages().duplicate()
	if not completed.has(stage_id):
		completed.append(stage_id)
		SaveManager.current_save["completed_stages"] = completed
		
	# Registra estrelas (1 a 3)
	var stars := 1
	if base_hp_ratio >= 0.90:
		stars = 3
	elif base_hp_ratio >= 0.50:
		stars = 2
		
	if not SaveManager.current_save.has("stage_stars"):
		SaveManager.current_save["stage_stars"] = {}
	var prev_stars: int = SaveManager.current_save["stage_stars"].get(str(stage_id), 0)
	SaveManager.current_save["stage_stars"][str(stage_id)] = maxi(prev_stars, stars)
	
	SaveManager.save_game()
	
	return {
		"stage_id": stage_id,
		"coins": coins_reward,
		"bonus_coins": bonus_coins,
		"exp": exp_reward,
		"stars": stars,
		"achieved_bonus": achieved_bonus
	}
