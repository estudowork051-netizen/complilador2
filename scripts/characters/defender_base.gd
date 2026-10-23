class_name DefenderBase
extends Node2D

## Classe Base Compartilhada para Personagens Defensores
## Controla ciclo de mira, ataques, tempos de recarga e estados visuais.

enum VisualState {
	IDLE,
	ATTACK,
	SKILL_1,
	SKILL_2,
	HIT
}

signal skill_cooldown_updated(char_id: String, skill_index: int, current_cd: float, max_cd: float)
signal defender_attacked(defender: DefenderBase, target: EnemyBase)

@export var character_id: String = ""
var character_name: String = ""
var level: int = 1

# Atributos Efetivos (Calculados com bônus da base)
var current_damage: float = 25.0
var current_attack_speed: float = 1.0
var current_range: float = 220.0
var current_skill_damage: float = 70.0

# Recargas
var skill_1_cd_max: float = 7.0
var skill_1_cd_current: float = 0.0

var skill_2_cd_max: float = 12.0
var skill_2_cd_current: float = 0.0

var current_visual_state: VisualState = VisualState.IDLE
var attack_cooldown_timer: float = 0.0
var target_enemy: EnemyBase = null

var char_data: CharacterData

# Componentes visuais
var sprite: Sprite2D
var range_circle: Node2D
var state_timer: float = 0.0

func setup_from_progression(char_key: String) -> void:
	character_id = char_key
	char_data = ProgressionManager.get_character_def(char_key)
	level = ProgressionManager.get_character_level(char_key)
	
	if char_data:
		character_name = char_data.name
		var ws_bonus := ProgressionManager.get_workshop_bonus_pct()
		var ac_dmg_bonus := ProgressionManager.get_academy_damage_bonus_pct()
		var ac_cdr := ProgressionManager.get_academy_cdr_pct()
		
		current_damage = char_data.calculate_damage(level, ws_bonus)
		current_attack_speed = char_data.calculate_attack_speed(level, ws_bonus)
		current_range = char_data.calculate_range(level)
		current_skill_damage = char_data.calculate_skill_damage(level, ac_dmg_bonus)
		
		if char_data.skills.size() > 0:
			var base_cd_1: float = char_data.skills[0].get("base_cooldown", 8.0)
			skill_1_cd_max = char_data.calculate_cooldown(base_cd_1, level, ac_cdr)
		if char_data.skills.size() > 1:
			var base_cd_2: float = char_data.skills[1].get("base_cooldown", 12.0)
			skill_2_cd_max = char_data.calculate_cooldown(base_cd_2, level, ac_cdr)
			
	_refresh_visuals()

func _ready() -> void:
	_setup_nodes()
	if character_id != "":
		setup_from_progression(character_id)

func _setup_nodes() -> void:
	# Círculo de alcance de ataque
	range_circle = Node2D.new()
	range_circle.name = "RangeCircle"
	add_child(range_circle)
	
	# Sprite do herói
	sprite = Sprite2D.new()
	sprite.name = "HeroSprite"
	sprite.position = Vector2(0, -10)
	add_child(sprite)

func _refresh_visuals() -> void:
	if char_data and ResourceLoader.exists(char_data.sprite_path):
		var tex := load(char_data.sprite_path) as Texture2D
		if tex and sprite:
			sprite.texture = tex
	queue_redraw()

func _draw() -> void:
	# Desenha anel de base mística
	draw_circle(Vector2(0, 8), 24.0, Color(0.1, 0.2, 0.4, 0.45))
	draw_arc(Vector2(0, 8), 24.0, 0.0, TAU, 32, Color(0.4, 0.8, 1.0, 0.8), 2.0)

func _process(delta: float) -> void:
	# Atualiza tempos de recarga das habilidades
	_process_cooldowns(delta)
	
	# Atualiza timers de estado visual
	_process_visual_state(delta)
	
	# Ciclo de mira e ataque básico
	_process_combat(delta)

func _process_cooldowns(delta: float) -> void:
	var updated_1 := false
	if skill_1_cd_current > 0.0:
		skill_1_cd_current = maxf(0.0, skill_1_cd_current - delta)
		updated_1 = true
	if updated_1:
		skill_cooldown_updated.emit(character_id, 0, skill_1_cd_current, skill_1_cd_max)
		
	var updated_2 := false
	if skill_2_cd_current > 0.0:
		skill_2_cd_current = maxf(0.0, skill_2_cd_current - delta)
		updated_2 = true
	if updated_2:
		skill_cooldown_updated.emit(character_id, 1, skill_2_cd_current, skill_2_cd_max)

func _process_visual_state(delta: float) -> void:
	if current_visual_state != VisualState.IDLE:
		state_timer -= delta
		if state_timer <= 0.0:
			_set_visual_state(VisualState.IDLE)
	else:
		# Respiração ociosa suave
		if sprite:
			sprite.scale.y = 1.0 + sin(Time.get_ticks_msec() * 0.005) * 0.04

func _set_visual_state(new_state: VisualState, duration: float = 0.3) -> void:
	current_visual_state = new_state
	state_timer = duration
	
	if not sprite:
		return
		
	match new_state:
		VisualState.IDLE:
			sprite.modulate = Color.WHITE
			sprite.position = Vector2(0, -10)
		VisualState.ATTACK:
			sprite.modulate = Color(1.3, 1.3, 1.1, 1.0)
			# Pequeno recuo/avanço de ataque
			var tween := create_tween()
			tween.tween_property(sprite, "position:x", 6.0, duration * 0.3)
			tween.tween_property(sprite, "position:x", 0.0, duration * 0.7)
		VisualState.SKILL_1:
			sprite.modulate = Color(1.5, 0.8, 1.8, 1.0)
			var tween := create_tween()
			tween.tween_property(sprite, "scale", Vector2(1.2, 1.2), duration * 0.4)
			tween.tween_property(sprite, "scale", Vector2(1.0, 1.0), duration * 0.6)
		VisualState.SKILL_2:
			sprite.modulate = Color(0.8, 1.8, 1.2, 1.0)
			var tween := create_tween()
			tween.tween_property(sprite, "scale", Vector2(1.25, 1.25), duration * 0.4)
			tween.tween_property(sprite, "scale", Vector2(1.0, 1.0), duration * 0.6)
		VisualState.HIT:
			sprite.modulate = Color(2.0, 0.4, 0.4, 1.0)

func _process_combat(delta: float) -> void:
	attack_cooldown_timer -= delta
	
	# Verifica se o alvo atual ainda é válido e está no alcance
	if not _is_target_valid(target_enemy):
		target_enemy = _find_best_target()
		
	# Ataca se estiver pronto
	if attack_cooldown_timer <= 0.0 and target_enemy != null:
		var interval := 1.0 / maxf(0.1, current_attack_speed)
		attack_cooldown_timer = interval
		_execute_basic_attack(target_enemy)

func _is_target_valid(target: EnemyBase) -> bool:
	if not is_instance_valid(target) or target.is_dead or target.has_reached_base:
		return false
	return global_position.distance_to(target.global_position) <= current_range

func _find_best_target() -> EnemyBase:
	var best_enemy: EnemyBase = null
	var highest_progress: float = -1.0
	
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead and not node.has_reached_base:
			var dist := global_position.distance_to(node.global_position)
			if dist <= current_range:
				# Prioriza o inimigo que está mais à frente no caminho (maior perigo para a base)
				if node.progress_ratio > highest_progress:
					highest_progress = node.progress_ratio
					best_enemy = node
					
	return best_enemy

func _execute_basic_attack(_target: EnemyBase) -> void:
	_set_visual_state(VisualState.ATTACK, 0.25)
	defender_attacked.emit(self, _target)

func can_use_skill(skill_idx: int) -> bool:
	if skill_idx == 0:
		return skill_1_cd_current <= 0.0
	elif skill_idx == 1:
		return skill_2_cd_current <= 0.0
	return false

func use_skill(skill_idx: int, target_pos: Vector2 = Vector2.ZERO) -> bool:
	if not can_use_skill(skill_idx):
		return false
		
	if skill_idx == 0:
		skill_1_cd_current = skill_1_cd_max
		_set_visual_state(VisualState.SKILL_1, 0.4)
		_execute_skill_1(target_pos)
		skill_cooldown_updated.emit(character_id, 0, skill_1_cd_current, skill_1_cd_max)
		ProgressionManager.update_quest_progress("quest_skills_10", 1)
		return true
	elif skill_idx == 1:
		skill_2_cd_current = skill_2_cd_max
		_set_visual_state(VisualState.SKILL_2, 0.4)
		_execute_skill_2(target_pos)
		skill_cooldown_updated.emit(character_id, 1, skill_2_cd_current, skill_2_cd_max)
		ProgressionManager.update_quest_progress("quest_skills_10", 1)
		return true
		
	return false

# Métodos virtuais para as subclasses
func _execute_skill_1(_target_pos: Vector2) -> void:
	pass

func _execute_skill_2(_target_pos: Vector2) -> void:
	pass
