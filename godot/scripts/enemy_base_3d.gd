class_name EnemyBase3D
extends PathFollow3D

## Base 3D Articulated Enemy Controller — Heroes Tower Defense
## Drives PathFollow3D navigation along the 3D battlefield road,
## counter-swinging articulated limbs, hit reactions, and death collapses.

signal defeated(enemy_node: Node3D)

enum State {
	WALK,
	ATTACK,
	HIT,
	DEATH
}

@export var enemy_type: String = "goblin"
@export var display_name: String = "Goblin"
@export var max_health: int = 100
@export var move_speed: float = 2.0
@export var attack_damage: int = 12
@export var bounty_coins: int = 10

@onready var model_root: Node3D = $ModelInstance if has_node("ModelInstance") else self
@onready var health_bar: ProgressBar = $HealthBar3D/SubViewport/ProgressBar if has_node("HealthBar3D/SubViewport/ProgressBar") else null

var current_health: int = 100
var current_state: State = State.WALK

var _walk_time: float = 0.0
var _state_timer: float = 0.0
var _hit_flinch_timer: float = 0.0

var _torso_node: Node3D = null
var _head_node: Node3D = null
var _left_arm_node: Node3D = null
var _right_arm_node: Node3D = null
var _left_leg_node: Node3D = null
var _right_leg_node: Node3D = null
var _weapon_node: Node3D = null

func _ready() -> void:
	current_health = max_health
	loop = false
	rotation_mode = PathFollow3D.ROTATION_ORIENTED
	_find_articulated_nodes(model_root)
	if health_bar:
		health_bar.max_value = max_health
		health_bar.value = current_health

func _process(delta: float) -> void:
	if current_state == State.DEATH:
		_process_death(delta)
		return

	if current_state == State.HIT:
		_state_timer += delta
		if _state_timer >= 0.2:
			current_state = State.WALK
			_state_timer = 0.0

	_handle_movement(delta)
	_handle_articulated_walk(delta)

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
		elif "weapon" in c_name or "club" in c_name or "sword" in c_name or "axe" in c_name or "staff" in c_name:
			_weapon_node = child as Node3D

		if child.get_child_count() > 0:
			_find_articulated_nodes(child)

func _handle_movement(delta: float) -> void:
	if current_state != State.WALK:
		return
	progress += move_speed * delta
	if progress_ratio >= 1.0:
		# Enemy reached kingdom base
		queue_free()

func _handle_articulated_walk(delta: float) -> void:
	_walk_time += delta * (move_speed * 3.5)

	# Leg counter-stride
	if _left_leg_node and _right_leg_node:
		_left_leg_node.rotation.x = sin(_walk_time) * 0.45
		_right_leg_node.rotation.x = -sin(_walk_time) * 0.45

	# Arm and weapon swing
	if _left_arm_node:
		_left_arm_node.rotation.x = -sin(_walk_time) * 0.35
	if _right_arm_node:
		_right_arm_node.rotation.x = sin(_walk_time) * 0.4
	if _weapon_node:
		_weapon_node.rotation.x = sin(_walk_time) * 0.2

	# Torso vertical cadence bob
	if _torso_node:
		_torso_node.position.y = abs(sin(_walk_time * 2.0)) * 0.06
	if _head_node:
		_head_node.rotation.x = sin(_walk_time * 2.0) * 0.04

func take_damage(amount: int) -> void:
	if current_state == State.DEATH:
		return
	current_health = maxi(0, current_health - amount)
	current_state = State.HIT
	_state_timer = 0.0

	if health_bar:
		health_bar.value = current_health

	if current_health <= 0:
		current_state = State.DEATH
		_state_timer = 0.0
		defeated.emit(self)

func _process_death(delta: float) -> void:
	_state_timer += delta
	if model_root:
		model_root.rotation.x = lerpf(model_root.rotation.x, -1.3, delta * 4.0)
		model_root.position.y = lerpf(model_root.position.y, -0.3, delta * 3.0)
	if _state_timer >= 1.5:
		queue_free()

func is_dead() -> bool:
	return current_state == State.DEATH or current_health <= 0
