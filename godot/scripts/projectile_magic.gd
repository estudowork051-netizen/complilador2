class_name ProjectileMagic
extends Area3D

## Projectile Magic (Míssil Arcano / Projétil Mágico) — Heroes Tower Defense
## High-velocity arcane energy missile with homing, pulsating cyan/violet glow, and stardust trail.

@export var damage: int = 55
@export var speed: float = 16.0

@onready var trail_particles: CPUParticles3D = $TrailParticles
@onready var core_mesh: MeshInstance3D = $CoreMesh
@onready var glow_light: OmniLight3D = $GlowLight

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
	_is_flying = true

func _physics_process(delta: float) -> void:
	if not _is_flying:
		return
	
	_alive_timer += delta
	if _alive_timer > 3.0:
		queue_free()
		return
	
	# Track target or last known position
	var target_pos = _target_last_pos
	if is_instance_valid(_target_node):
		target_pos = _target_node.global_position + Vector3(0, 0.5, 0)
		_target_last_pos = target_pos
	
	var dir = (target_pos - global_position).normalized()
	global_position += dir * speed * delta
	
	# Spin crystal core
	if core_mesh:
		core_mesh.rotate_y(delta * 12.0)
		core_mesh.rotate_z(delta * 8.0)
	
	if global_position.distance_to(target_pos) < 0.6:
		_impact(_target_node)

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
	
	if hit_target and is_instance_valid(hit_target) and hit_target.has_method("take_damage"):
		hit_target.take_damage(damage)
	
	_spawn_arcane_burst()
	
	var battlefield = get_tree().current_scene as BattlefieldPrototype
	if battlefield:
		battlefield.trigger_screen_shake(0.12, 0.20)
	
	queue_free()

func _spawn_arcane_burst() -> void:
	var burst = CPUParticles3D.new()
	burst.emitting = true
	burst.one_shot = true
	burst.explosiveness = 1.0
	burst.amount = 18
	burst.lifetime = 0.45
	burst.direction = Vector3(0, 1, 0)
	burst.spread = 90.0
	burst.initial_velocity_min = 2.5
	burst.initial_velocity_max = 5.5
	burst.color = Color(0.22, 0.74, 0.97) # Cyan energy
	
	var mesh = BoxMesh.new()
	mesh.size = Vector3(0.06, 0.06, 0.06)
	burst.mesh = mesh
	
	get_tree().current_scene.add_child(burst)
	burst.global_position = global_position
	
	get_tree().create_timer(0.5).timeout.connect(burst.queue_free)
