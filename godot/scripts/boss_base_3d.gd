class_name BossBase3D
extends PathFollow3D

## Epic 3D Articulated Boss Controller — Heroes Tower Defense
## Drives massive boss scale, multi-phase mechanics (Phase 1, 2 Enrage, 3 Desperation),
## special area-of-effect boss skills, screen shake, and imposing defeat sequence.

signal boss_phase_changed(new_phase: int)
signal boss_special_attack(skill_name: String)
signal boss_defeated(boss_node: Node3D)

enum Phase {
	PHASE_1_STEADY,
	PHASE_2_ENRAGED,
	PHASE_3_APOCALYPSE
}

@export var boss_type: String = "obsidian_golem"
@export var display_name: String = "Golem de Obsidiana"
@export var max_health: int = 1800
@export var move_speed: float = 1.0
@export var attack_damage: int = 65
@export var bounty_coins: int = 150
@export var special_skill_name: String = "GroundSlam"

@onready var model_root: Node3D = $ModelInstance if has_node("ModelInstance") else self

var current_health: int = 1800
var current_phase: Phase = Phase.PHASE_1_STEADY
var _is_dead: bool = false

var _march_time: float = 0.0
var _skill_cooldown: float = 8.0
var _skill_timer: float = 0.0
var _special_active: bool = false
var _phase_flash_timer: float = 0.0

var _torso_node: Node3D = null
var _head_node: Node3D = null
var _left_arm_node: Node3D = null
var _right_arm_node: Node3D = null
var _wings_node: Node3D = null
var _tail_node: Node3D = null

func _ready() -> void:
	current_health = max_health
	loop = false
	rotation_mode = PathFollow3D.ROTATION_ORIENTED
	_find_articulated_nodes(model_root)

func _process(delta: float) -> void:
	if _is_dead:
		_process_boss_defeat(delta)
		return

	_skill_timer += delta
	_handle_movement(delta)
	_handle_boss_articulation(delta)

	if _skill_timer >= _skill_cooldown:
		_execute_special_attack()

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
		elif "wings" in c_name:
			_wings_node = child as Node3D
		elif "tail" in c_name:
			_tail_node = child as Node3D

		if child.get_child_count() > 0:
			_find_articulated_nodes(child)

func _handle_movement(delta: float) -> void:
	var speed_mult: float = 1.0
	if current_phase == Phase.PHASE_2_ENRAGED:
		speed_mult = 1.25
	elif current_phase == Phase.PHASE_3_APOCALYPSE:
		speed_mult = 1.45

	progress += (move_speed * speed_mult) * delta
	if progress_ratio >= 1.0:
		queue_free()

func _handle_boss_articulation(delta: float) -> void:
	_march_time += delta * 3.0

	# Heavy stomping stride
	if _torso_node:
		_torso_node.position.y = abs(sin(_march_time)) * 0.12
		_torso_node.rotation.z = sin(_march_time * 0.5) * 0.05

	if _left_arm_node and _right_arm_node:
		_left_arm_node.rotation.x = sin(_march_time) * 0.5
		_right_arm_node.rotation.x = -sin(_march_time) * 0.5

	# Dragon wings flapping
	if _wings_node:
		_wings_node.rotation.z = sin(_march_time * 2.0) * 0.35

	# Dragon tail sweeping
	if _tail_node:
		_tail_node.rotation.y = sin(_march_time * 1.5) * 0.4

func _execute_special_attack() -> void:
	_skill_timer = 0.0
	boss_special_attack.emit(special_skill_name)

	# Trigger camera shake if camera is present
	var cam = get_viewport().get_camera_3d()
	if cam and cam.has_method("apply_shake"):
		cam.apply_shake(0.35)

func take_damage(amount: int) -> void:
	if _is_dead:
		return
	current_health = maxi(0, current_health - amount)

	# Phase transitions
	var health_pct: float = float(current_health) / float(max_health)
	if health_pct <= 0.25 and current_phase != Phase.PHASE_3_APOCALYPSE:
		current_phase = Phase.PHASE_3_APOCALYPSE
		boss_phase_changed.emit(3)
	elif health_pct <= 0.60 and current_phase == Phase.PHASE_1_STEADY:
		current_phase = Phase.PHASE_2_ENRAGED
		boss_phase_changed.emit(2)

	if current_health <= 0:
		_is_dead = true
		boss_defeated.emit(self)

func _process_boss_defeat(delta: float) -> void:
	if model_root:
		model_root.rotation.x = lerpf(model_root.rotation.x, -1.4, delta * 2.0)
		model_root.position.y = lerpf(model_root.position.y, -0.6, delta * 1.5)
		model_root.scale = lerp(model_root.scale, Vector3.ZERO, delta * 0.8)

func is_dead() -> bool:
	return _is_dead or current_health <= 0
