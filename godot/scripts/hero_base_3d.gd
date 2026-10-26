class_name HeroBase3D
extends Node3D

## Base 3D Articulated Hero Controller — Heroes Tower Defense
## Drives skeletal hierarchy animations (Idle, Windup, Attack, Skill, Hit, Defeat),
## tactical targeting AI, skill cooldowns, and visual combat feedback.

signal attacked(target: Node3D, damage: int)
signal skill_activated(skill_name: String)
signal defeated()

enum State {
	IDLE,
	WINDUP,
	ATTACK,
	RECOVERY,
	SKILL,
	HIT,
	DEFEAT
}

@export var hero_id: String = "hero_kael"
@export var hero_name: String = "Kael"
@export var max_health: int = 500
@export var attack_damage: int = 45
@export var attack_range: float = 4.5
@export var attack_interval: float = 1.4
@export var skill_cooldown_max: float = 12.0

# Node references (configured dynamically or via scene tree)
@onready var model_root: Node3D = $ModelInstance if has_node("ModelInstance") else self
@onready var anim_player: AnimationPlayer = $AnimationPlayer if has_node("AnimationPlayer") else null

var current_health: int = 500
var current_state: State = State.IDLE

var _cooldown_timer: float = 0.0
var _skill_timer: float = 0.0
var _state_timer: float = 0.0
var _idle_time: float = 0.0
var _hit_flinch_timer: float = 0.0
var _current_target: Node3D = null

# Cached articulated nodes from GLB model
var _torso_node: Node3D = null
var _head_node: Node3D = null
var _left_arm_node: Node3D = null
var _right_arm_node: Node3D = null
var _left_leg_node: Node3D = null
var _right_leg_node: Node3D = null
var _weapon_node: Node3D = null

func _ready() -> void:
	current_health = max_health
	_find_articulated_nodes(model_root)
	_setup_fallback_animations()

func _process(delta: float) -> void:
	if current_state == State.DEFEAT:
		_process_defeat(delta)
		return

	_cooldown_timer = maxf(0.0, _cooldown_timer - delta)
	_skill_timer = maxf(0.0, _skill_timer - delta)

	_process_state(delta)
	_handle_articulated_motion(delta)

	if _cooldown_timer <= 0.0 and current_state == State.IDLE:
		_evaluate_combat()

func _find_articulated_nodes(root: Node) -> void:
	for child in root.get_children():
		var c_name = child.name.to_lower()
		if "torso" in c_name:
			_torso_node = child as Node3D
		elif "head" in c_name:
			_head_node = child as Node3D
		elif "leftarm" in c_name or "left_arm" in c_name:
			_left_arm_node = child as Node3D
		elif "rightarm" in c_name or "right_arm" in c_name:
			_right_arm_node = child as Node3D
		elif "leftleg" in c_name or "left_leg" in c_name:
			_left_leg_node = child as Node3D
		elif "rightleg" in c_name or "right_leg" in c_name:
			_right_leg_node = child as Node3D
		elif "weapon" in c_name or "sword" in c_name or "bow" in c_name:
			_weapon_node = child as Node3D

		if child.get_child_count() > 0:
			_find_articulated_nodes(child)

func _process_state(delta: float) -> void:
	_state_timer += delta

	match current_state:
		State.IDLE:
			_idle_time += delta * 2.5
		State.WINDUP:
			if _state_timer >= 0.35:
				_execute_strike()
		State.ATTACK:
			if _state_timer >= 0.25:
				current_state = State.RECOVERY
				_state_timer = 0.0
		State.RECOVERY:
			if _state_timer >= 0.3:
				current_state = State.IDLE
				_state_timer = 0.0
		State.SKILL:
			if _state_timer >= 0.9:
				current_state = State.IDLE
				_state_timer = 0.0
		State.HIT:
			if _state_timer >= 0.22:
				current_state = State.IDLE
				_state_timer = 0.0

func _handle_articulated_motion(_delta: float) -> void:
	match current_state:
		State.IDLE:
			var breath: float = sin(_idle_time) * 0.04
			if _torso_node:
				_torso_node.position.y = breath
				_torso_node.rotation.z = cos(_idle_time * 0.5) * 0.02
			if _head_node:
				_head_node.rotation.x = sin(_idle_time) * 0.03
			if _right_arm_node:
				_right_arm_node.rotation.x = sin(_idle_time) * 0.08
			if _weapon_node:
				_weapon_node.rotation.z = cos(_idle_time) * 0.05
		State.WINDUP:
			# Pull weapon back, twist torso
			if _torso_node:
				_torso_node.rotation.y = lerpf(_torso_node.rotation.y, 0.45, 0.2)
			if _right_arm_node:
				_right_arm_node.rotation.x = lerpf(_right_arm_node.rotation.x, -1.2, 0.25)
			if _weapon_node:
				_weapon_node.rotation.x = lerpf(_weapon_node.rotation.x, -0.6, 0.25)
		State.ATTACK:
			# Fast slash/thrust forward
			if _torso_node:
				_torso_node.rotation.y = lerpf(_torso_node.rotation.y, -0.4, 0.4)
			if _right_arm_node:
				_right_arm_node.rotation.x = lerpf(_right_arm_node.rotation.x, 1.3, 0.45)
			if _weapon_node:
				_weapon_node.rotation.x = lerpf(_weapon_node.rotation.x, 0.8, 0.45)
		State.RECOVERY:
			# Smoothly ease back toward neutral
			if _torso_node:
				_torso_node.rotation.y = lerpf(_torso_node.rotation.y, 0.0, 0.15)
			if _right_arm_node:
				_right_arm_node.rotation.x = lerpf(_right_arm_node.rotation.x, 0.0, 0.15)
			if _weapon_node:
				_weapon_node.rotation.x = lerpf(_weapon_node.rotation.x, 0.0, 0.15)
		State.SKILL:
			# Hero levitates/raises both arms in ultimate charge
			if _torso_node:
				_torso_node.position.y = 0.35 + sin(_state_timer * 12.0) * 0.05
			if _left_arm_node:
				_left_arm_node.rotation.x = -1.4
			if _right_arm_node:
				_right_arm_node.rotation.x = -1.4
		State.HIT:
			if _torso_node:
				_torso_node.rotation.x = -0.35
			if _head_node:
				_head_node.rotation.x = -0.45

func _evaluate_combat() -> void:
	# Scan for closest active enemy in range
	var bf: BattlefieldPrototype = get_tree().current_scene as BattlefieldPrototype
	if not bf:
		return

	var closest: Node3D = null
	var min_dist: float = attack_range

	for enemy in bf.get_active_enemies():
		if not is_instance_valid(enemy):
			continue
		if enemy.has_method("is_dead") and enemy.is_dead():
			continue
		var dist = global_position.distance_to(enemy.global_position)
		if dist <= min_dist:
			min_dist = dist
			closest = enemy

	if closest != null:
		_current_target = closest
		# Turn toward target
		var dir = (closest.global_position - global_position).normalized()
		rotation.y = atan2(dir.x, dir.z)
		current_state = State.WINDUP
		_state_timer = 0.0

func _execute_strike() -> void:
	current_state = State.ATTACK
	_state_timer = 0.0
	_cooldown_timer = attack_interval

	if _current_target and is_instance_valid(_current_target):
		if _current_target.has_method("take_damage"):
			_current_target.take_damage(attack_damage)
		attacked.emit(_current_target, attack_damage)

func trigger_skill() -> void:
	if _skill_timer > 0.0 or current_state == State.DEFEAT:
		return
	current_state = State.SKILL
	_state_timer = 0.0
	_skill_timer = skill_cooldown_max
	skill_activated.emit("UltimateSkill")

func take_damage(amount: int) -> void:
	if current_state == State.DEFEAT:
		return
	current_health = maxi(0, current_health - amount)
	current_state = State.HIT
	_state_timer = 0.0

	if current_health <= 0:
		current_state = State.DEFEAT
		_state_timer = 0.0
		defeated.emit()

func _process_defeat(delta: float) -> void:
	_state_timer += delta
	# Gradual collapse to ground
	if model_root:
		model_root.rotation.x = lerpf(model_root.rotation.x, -1.2, delta * 3.0)
		model_root.position.y = lerpf(model_root.position.y, -0.4, delta * 2.0)
	if _state_timer > 3.0:
		queue_free()

func is_alive() -> bool:
	return current_state != State.DEFEAT and current_health > 0

func _setup_fallback_animations() -> void:
	# AnimationPlayer support placeholder if present
	if anim_player:
		if not anim_player.has_animation("idle"):
			pass
