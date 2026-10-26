class_name ProjectileArrow
extends Area3D

## Projectile Arrow — Heroes Tower Defense
## 3D Ballistic projectile with parabolic arc, particle trail, collision detection, and impact reaction.

@export var damage: int = 35
@export var speed: float = 14.0
@export var gravity: float = 9.8

@onready var trail_particles: CPUParticles3D = $TrailParticles
@onready var mesh_instance: MeshInstance3D = $MeshInstance3D

var _velocity: Vector3 = Vector3.ZERO
var _target_node: Node3D = null
var _target_last_pos: Vector3 = Vector3.ZERO
var _is_flying: bool = false
var _alive_timer: float = 0.0

func _ready() -> void:
	connect("body_entered", Callable(self, "_on_body_entered"))
	connect("area_entered", Callable(self, "_on_area_entered"))

func launch(start_pos: Vector3, target: Node3D) -> void:
	global_position = start_pos
	_target_node = target
	_target_last_pos = target.global_position + Vector3(0, 0.5, 0)
	
	# Compute launch velocity towards target
	var to_target: Vector3 = _target_last_pos - start_pos
	var distance_h: float = Vector2(to_target.x, to_target.z).length()
	var flight_time: float = maxf(0.2, distance_h / speed)
	
	# Ballistic arc: horizontal velocity + vertical arc compensation
	var vx = to_target.x / flight_time
	var vz = to_target.z / flight_time
	var vy = (to_target.y + 0.5 * gravity * flight_time * flight_time) / flight_time
	
	_velocity = Vector3(vx, vy, vz)
	_is_flying = true
	
	# Align orientation with flight path
	if _velocity.length_squared() > 0.01:
		look_at(global_position + _velocity, Vector3.UP)

func _physics_process(delta: float) -> void:
	if not _is_flying:
		return
	
	_alive_timer += delta
	if _alive_timer > 3.0:
		queue_free()
		return
	
	# Apply gravity
	_velocity.y -= gravity * delta
	
	# Move projectile
	global_position += _velocity * delta
	
	# Orient arrow tip along velocity vector
	if _velocity.length_squared() > 0.01:
		look_at(global_position + _velocity, Vector3.UP)
	
	# Proximity check to target
	var check_pos = _target_node.global_position + Vector3(0, 0.5, 0) if is_instance_valid(_target_node) else _target_last_pos
	if global_position.distance_to(check_pos) < 0.65:
		_impact(_target_node)
	elif global_position.y <= 0.0:
		# Hit ground
		_impact(null)

func _on_body_entered(body: Node) -> void:
	if _is_flying:
		_impact(body)

func _on_area_entered(area: Area3D) -> void:
	if _is_flying:
		_impact(area.get_parent())

func _impact(hit_target: Node) -> void:
	if not _is_flying:
		return
	_is_flying = false
	
	# Apply damage if hit an enemy
	if hit_target and is_instance_valid(hit_target) and hit_target.has_method("take_damage"):
		hit_target.take_damage(damage)
	
	# Spawn impact spark debris
	_spawn_impact_burst()
	
	# Trigger camera shake
	var battlefield = get_tree().current_scene as BattlefieldPrototype
	if battlefield:
		battlefield.trigger_screen_shake(0.08, 0.15)
	
	queue_free()

func _spawn_impact_burst() -> void:
	var burst = CPUParticles3D.new()
	burst.emitting = true
	burst.one_shot = true
	burst.explosiveness = 1.0
	burst.amount = 12
	burst.lifetime = 0.4
	burst.direction = Vector3(0, 1, 0)
	burst.spread = 75.0
	burst.initial_velocity_min = 2.0
	burst.initial_velocity_max = 4.5
	burst.color = Color(1.0, 0.85, 0.3) # Golden spark
	
	var mesh = BoxMesh.new()
	mesh.size = Vector3(0.04, 0.04, 0.04)
	burst.mesh = mesh
	
	get_tree().current_scene.add_child(burst)
	burst.global_position = global_position
	
	get_tree().create_timer(0.5).timeout.connect(burst.queue_free)
