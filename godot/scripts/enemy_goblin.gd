class_name EnemyGoblin
extends PathFollow3D

## Goblin Raider (Goblin Saqueador) — Heroes Tower Defense
## Articulated 3D character with procedural walk cycle, path navigation, hit flinch, and physical death collapse.

signal defeated(enemy_node: Node3D)

@export var max_health: int = 100
@export var move_speed: float = 2.0 # Units per second along path

@onready var character_root: Node3D = $CharacterRoot
@onready var body_mesh: MeshInstance3D = $CharacterRoot/Torso/BodyMesh
@onready var left_leg: Node3D = $CharacterRoot/LeftLeg
@onready var right_leg: Node3D = $CharacterRoot/RightLeg
@onready var right_arm: Node3D = $CharacterRoot/RightArm
@onready var head: Node3D = $CharacterRoot/Head
@onready var health_bar: ProgressBar = $CharacterRoot/HealthBar3D/SubViewport/ProgressBar

var current_health: int = 100
var _is_dead: Boolean = false
var _walk_time: float = 0.0
var _hit_flinch_timer: float = 0.0
var _death_timer: float = 0.0

func _ready() -> void:
	current_health = max_health
	loop = false
	rotation_mode = PathFollow3D.ROTATION_ORIENTED
	if health_bar:
		health_bar.max_value = max_health
		health_bar.value = current_health

func _process(delta: float) -> void:
	if _is_dead:
		_handle_death_animation(delta)
		return
	
	_handle_movement(delta)
	_handle_walk_cycle(delta)
	_handle_hit_flinch(delta)

func _handle_movement(delta: float) -> void:
	progress += move_speed * delta
	if progress_ratio >= 1.0:
		# Reached end fortress
		print("[Godot Prototype] Goblin reached player fortress!")
		queue_free()

func _handle_walk_cycle(delta: float) -> void:
	_walk_time += delta * 8.0 # Cycle cadence
	
	# Articulated leg sway
	if left_leg and right_leg:
		left_leg.rotation.x = sin(_walk_time) * 0.45
		right_leg.rotation.x = -sin(_walk_time) * 0.45
	
	# Articulated arm swing holding spiked club
	if right_arm:
		right_arm.rotation.x = sin(_walk_time) * 0.35
		right_arm.rotation.z = -0.2 + cos(_walk_time * 0.5) * 0.1
	
	# Torso & Head natural vertical bobbing
	if character_root:
		character_root.position.y = abs(sin(_walk_time * 2.0)) * 0.08
	if head:
		head.rotation.x = sin(_walk_time * 2.0) * 0.05

func _handle_hit_flinch(delta: float) -> void:
	if _hit_flinch_timer > 0.0:
		_hit_flinch_timer -= delta
		if character_root:
			var flinch = sin(_hit_flinch_timer * 40.0) * 0.12
			character_root.position.z = flinch
	elif character_root and not _is_dead:
		character_root.position.z = 0.0

func take_damage(amount: int) -> void:
	if _is_dead:
		return
	
	current_health = maxi(0, current_health - amount)
	_hit_flinch_timer = 0.22
	
	if health_bar:
		health_bar.value = current_health
	
	# Visual hit flash
	if body_mesh and body_mesh.get_active_material(0):
		var mat = body_mesh.get_active_material(0)
		if mat is StandardMaterial3D:
			mat.albedo_color = Color(1.0, 0.2, 0.2) # Flash red
			get_tree().create_timer(0.08).timeout.connect(func():
				if is_instance_valid(mat):
					mat.albedo_color = Color(0.09, 0.55, 0.24) # Restore goblin green
			)
	
	if current_health <= 0:
		_start_death()

func _start_death() -> void:
	_is_dead = true
	_death_timer = 0.60
	emit_signal("defeated", self)
	_spawn_death_debris()

func _handle_death_animation(delta: float) -> void:
	_death_timer -= delta
	if character_root:
		# Collapse backward to the ground
		character_root.rotation.x = lerpf(character_root.rotation.x, -PI * 0.45, delta * 6.0)
		character_root.position.y = lerpf(character_root.position.y, -0.4, delta * 4.0)
		# Fade scale slightly
		character_root.scale = character_root.scale.lerp(Vector3.ZERO, delta * 2.5)
	
	if _death_timer <= 0.0:
		queue_free()

func _spawn_death_debris() -> void:
	# Spawns physical debris fragments
	var debris_particles = CPUParticles3D.new()
	debris_particles.emitting = true
	debris_particles.one_shot = true
	debris_particles.explosiveness = 0.95
	debris_particles.amount = 16
	debris_particles.lifetime = 0.6
	debris_particles.direction = Vector3(0, 1, 0)
	debris_particles.spread = 60.0
	debris_particles.initial_velocity_min = 2.5
	debris_particles.initial_velocity_max = 5.0
	debris_particles.color = Color(0.2, 0.6, 0.25)
	
	var mesh = BoxMesh.new()
	mesh.size = Vector3(0.08, 0.08, 0.08)
	debris_particles.mesh = mesh
	
	get_tree().current_scene.add_child(debris_particles)
	debris_particles.global_position = global_position + Vector3(0, 0.4, 0)
	
	get_tree().create_timer(0.7).timeout.connect(debris_particles.queue_free)

func is_dead() -> bool:
	return _is_dead
