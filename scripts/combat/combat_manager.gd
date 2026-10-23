class_name CombatManager
extends Node2D

## Gerenciador Central de Batalha (Combate Tower Defense)
## Coordena o caminho, ondas de fases 1 a 10, objetivo central, defensores e condições de vitória/derrota.

signal base_health_changed(current: int, max_val: int)
signal wave_started(wave_number: int, total_waves: int)
signal wave_enemies_updated(alive_count: int, pending_count: int)
signal battle_victory()
signal battle_victory_with_rewards(rewards: Dictionary)
signal battle_defeat()
signal boss_spawned(boss_name: String, max_hp: float)
signal boss_health_changed(current: float, max_val: float)
signal boss_cinematic_started(boss_name: String, boss_title: String, max_hp: float)

const MAX_BASE_HEALTH: int = 100
var current_base_health: int = MAX_BASE_HEALTH

# Posições Verticais dos Espaços de Defesa (720x1280)
const DEFENDER_SLOT_POSITIONS: Array[Vector2] = [
	Vector2(360, 260), # Central superior
	Vector2(360, 430), # Central meio
	Vector2(360, 600), # Central inferior
	Vector2(160, 450), # Flanco esquerdo
	Vector2(560, 610), # Flanco direito
	Vector2(360, 770)  # Baluarte final antes da base
]

var stage_id: int = 1
var stage_data: StageData
var waves_data: Array[Array] = []

var current_wave_idx: int = 0
var current_spawn_idx: int = 0
var spawn_timer: float = 0.0
var wave_break_timer: float = 2.0
var is_spawning_wave: bool = false
var battle_ended: bool = false

# Contadores de inimigos
var alive_enemies_count: int = 0
var pending_in_wave: int = 0

# Referências de nós
@onready var enemy_path: Path2D = $EnemyPath
@onready var defenders_container: Node2D = $DefendersContainer
@onready var projectiles_container: Node2D = $ProjectilesContainer
@onready var effects_container: Node2D = $EffectsContainer
@onready var base_fortress_sprite: Sprite2D = $BaseFortress/Sprite2D

var spawned_defenders: Array[DefenderBase] = []

func _ready() -> void:
	stage_id = GameStateManager.current_stage_id
	stage_data = StageData.create_stage(stage_id)
	waves_data = stage_data.waves
	
	current_base_health = MAX_BASE_HEALTH
	base_health_changed.emit(current_base_health, MAX_BASE_HEALTH)
	_setup_scenario_theme()
	_setup_defenders()
	_prepare_next_wave()

func _setup_defenders() -> void:
	var team: Array = ProgressionManager.get_team_slots()
	var unlocked_slots := ProgressionManager.get_unlocked_defense_slots()
	var slots_to_use := mini(team.size(), unlocked_slots)
	
	for i in range(slots_to_use):
		var char_id: String = team[i]
		if char_id == "":
			continue
			
		var pos: Vector2 = DEFENDER_SLOT_POSITIONS[i]
		var defender_node: DefenderBase = null
		
		match char_id:
			"kael":
				defender_node = KaelCharacter.new()
			"lyra":
				defender_node = LyraCharacter.new()
			"thorne":
				defender_node = ThorneCharacter.new()
			"elysia":
				defender_node = ElysiaCharacter.new()
			"boran":
				defender_node = BoranCharacter.new()
			"vesper":
				defender_node = VesperCharacter.new()
			_:
				defender_node = DefenderBase.new()
				
		defender_node.name = "Defender_%s_%d" % [char_id, i]
		defender_node.position = pos
		defender_node.add_to_group("defenders")
		defenders_container.add_child(defender_node)
		defender_node.setup_from_progression(char_id)
		spawned_defenders.append(defender_node)

func _process(delta: float) -> void:
	if battle_ended:
		return
		
	# Gerenciamento de ondas
	if is_spawning_wave:
		spawn_timer -= delta
		if spawn_timer <= 0.0:
			_spawn_next_enemy_in_wave()
	else:
		# Intervalo entre ondas
		if current_wave_idx < waves_data.size():
			wave_break_timer -= delta
			if wave_break_timer <= 0.0:
				_start_current_wave()

func _prepare_next_wave() -> void:
	if current_wave_idx >= waves_data.size():
		return
	is_spawning_wave = false
	wave_break_timer = 2.0
	pending_in_wave = waves_data[current_wave_idx].size()
	wave_enemies_updated.emit(alive_enemies_count, pending_in_wave)

func _start_current_wave() -> void:
	is_spawning_wave = true
	current_spawn_idx = 0
	spawn_timer = 0.5
	wave_started.emit(current_wave_idx + 1, waves_data.size())

func _spawn_next_enemy_in_wave() -> void:
	var wave: Array = waves_data[current_wave_idx]
	if current_spawn_idx >= wave.size():
		is_spawning_wave = false
		return
		
	var enemy_info: Dictionary = wave[current_spawn_idx]
	var enemy_type: String = enemy_info.get("type", "goblin")
	var delay: float = enemy_info.get("delay", 1.2)
	
	_spawn_enemy(enemy_type)
	
	current_spawn_idx += 1
	pending_in_wave -= 1
	wave_enemies_updated.emit(alive_enemies_count, pending_in_wave)
	
	if current_spawn_idx < wave.size():
		spawn_timer = delay
	else:
		is_spawning_wave = false

func _spawn_enemy(enemy_type: String) -> void:
	var enemy: EnemyBase = null
	match enemy_type:
		"goblin":
			enemy = GoblinScout.new()
		"orc":
			enemy = ArmoredOrc.new()
		"brute":
			enemy = EnemyBrute.new()
		"flying":
			enemy = EnemyFlying.new()
		"sorcerer":
			enemy = DarkSorcerer.new()
		"boss_ogre":
			var boss := BossOgre.new()
			boss.boss_hp_updated.connect(func(c, m): boss_health_changed.emit(c, m))
			enemy = boss
			boss_spawned.emit(boss.enemy_name, boss.max_hp)
			boss_cinematic_started.emit(boss.enemy_name, boss.boss_title, boss.max_hp)
			_trigger_boss_cinematic(boss)
		_:
			enemy = GoblinScout.new()
			
	enemy.add_to_group("enemies")
	enemy_path.add_child(enemy)

func _trigger_boss_cinematic(_boss: BossOgre) -> void:
	AudioManager.play_sfx("boss_roar")
	var orig_scale := Engine.time_scale
	Engine.time_scale = 0.35
	var tween := create_tween()
	tween.tween_interval(0.4)
	tween.tween_callback(func():
		Engine.time_scale = orig_scale
	)

func _setup_scenario_theme() -> void:
	var bg: ColorRect = get_node_or_null("Background")
	if bg and stage_data:
		match stage_data.scenario_theme:
			"forest":
				bg.color = Color(0.06, 0.14, 0.10, 1.0)
			"ruins":
				bg.color = Color(0.10, 0.09, 0.08, 1.0)
			"volcano":
				bg.color = Color(0.14, 0.04, 0.04, 1.0)
			"fortress":
				bg.color = Color(0.05, 0.07, 0.10, 1.0)
			"temple":
				bg.color = Color(0.06, 0.04, 0.12, 1.0)
	
	alive_enemies_count += 1
	enemy.enemy_died.connect(_on_enemy_defeated)
	enemy.enemy_reached_base.connect(_on_enemy_reached_base)
	wave_enemies_updated.emit(alive_enemies_count, pending_in_wave)

func _on_enemy_defeated(_enemy: EnemyBase) -> void:
	alive_enemies_count = maxi(0, alive_enemies_count - 1)
	wave_enemies_updated.emit(alive_enemies_count, pending_in_wave)
	_check_victory_condition()

func _on_enemy_reached_base(_enemy: EnemyBase, damage: int) -> void:
	alive_enemies_count = maxi(0, alive_enemies_count - 1)
	damage_base(damage)
	wave_enemies_updated.emit(alive_enemies_count, pending_in_wave)
	_check_victory_condition()

func damage_base(damage: int) -> void:
	if battle_ended:
		return
		
	current_base_health = maxi(0, current_base_health - damage)
	base_health_changed.emit(current_base_health, MAX_BASE_HEALTH)
	AudioManager.play_sfx("explosion")
	
	if base_fortress_sprite:
		var t := create_tween()
		t.tween_property(base_fortress_sprite, "modulate", Color(2.5, 0.4, 0.4, 1.0), 0.08)
		t.tween_property(base_fortress_sprite, "modulate", Color.WHITE, 0.15)
		
	if current_base_health <= 0:
		_trigger_defeat()

func _check_victory_condition() -> void:
	if battle_ended:
		return
		
	# Verifica se todas as ondas da fase foram concluídas e todos os monstros foram eliminados
	if not is_spawning_wave and current_spawn_idx >= waves_data[current_wave_idx].size():
		if alive_enemies_count == 0:
			if current_wave_idx < waves_data.size() - 1:
				current_wave_idx += 1
				_prepare_next_wave()
			else:
				_trigger_victory()

func _trigger_victory() -> void:
	if battle_ended:
		return
	battle_ended = true
	
	var base_hp_ratio := float(current_base_health) / float(MAX_BASE_HEALTH)
	var rewards := ProgressionManager.complete_stage(stage_id, base_hp_ratio)
	
	AudioManager.play_sfx("victory")
	GameStateManager.change_state(GameStateManager.State.VICTORY)
	battle_victory.emit()
	battle_victory_with_rewards.emit(rewards)

func _trigger_defeat() -> void:
	if battle_ended:
		return
	battle_ended = true
	
	AudioManager.play_sfx("defeat")
	GameStateManager.change_state(GameStateManager.State.DEFEAT)
	battle_defeat.emit()

func trigger_defender_skill(char_id: String, skill_idx: int, target_pos: Vector2 = Vector2.ZERO) -> bool:
	for def in spawned_defenders:
		if def.character_id == char_id:
			return def.use_skill(skill_idx, target_pos)
	return false
