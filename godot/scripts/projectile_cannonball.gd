class_name ProjectileCannonball
extends Area3D

## Projectile Cannonball (Bala de Canhão / Projétil Explosivo) — Heroes Tower Defense
## Heavy 3D cast-iron explosive shell with ballistic arc, smoke trail, AOE damage, and screen shake.

@export var damage: int = 80
@export var speed: float = 12.0
@export var splash_radius: float = 2.5
@export var gravity: float = 12.0

@onready var trail_particles: CPUParticles3D = $TrailParticles
@onready var shell_mesh: MeshInstance3D = $ShellMesh

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
	_target_last_pos = target.global_position + Vector3(0, 0.4, 0)
	
	var to_target: Vector3 = _target_last_pos - start_pos
	var distance_h: float = Vector2(to_target.x, to_target.z).length()
	var flight_time: float = maxf(0.25, distance_h / speed)
	
	var vx = to_target.x / flight_time
	var vz = to_target.z / flight_time
	var vy = (to_target.y + 0.5 * gravity * flight_time * flight_time) / flight_time
	
	_velocity = Vector3(vx, vy, vz)
	_is_flying = true

func _physics_process(delta: float) -> void:
	if not _is_flying:
		return
	
	_alive_timer += delta
	if _alive_timer > 3.5:
		queue_free()
		return
	
	_velocity.y -= gravity * delta
	global_position += _velocity * delta
	
	if shell_mesh:
		shell_mesh.rotate_x(delta * 10.0)
	
	var check_pos = _target_node.global_position + Vector3(0, 0.4, 0) if is_instance_valid(_target_node) else _target_last_pos
	if global_position.distance_to(check_pos) < 0.75 or global_position.y <= 0.0:
		_impact()

func _on_body_entered(_body: Node) -> void:
	if _is_flying:
		_impact()

func _on_area_entered(_area: Area3D) -> void:
	if _is_flying:
		_impact()

func _impact() -> void:
	if not _is_flying:
		return
	_is_flying = false
	
	# Area of Effect (AOE) damage
	var battlefield = get_tree().current_scene as BattlefieldPrototype
	if battlefield:
		for enemy in battlefield.get_active_enemies():
			if is_instance_valid(enemy) and global_position.distance_to(enemy.global_position) <= splash_radius:
				if enemy.has_method("take_damage"):
					enemy.take_damage(damage)
		battlefield.trigger_screen_shake(0.35, 0.35) # Visceral cannon screen shake
	
	_spawn_aoe_explosion()
	queue_free()

func _spawn_aoe_explosion() -> void:
	var burst = CPUParticles3D.new()
	burst.emitting = true
	burst.one_shot = true
	burst.explosiveness = 1.0
	burst.amount = 32
	burst.lifetime = 0.55
	burst.direction = Vector3(0, 1, 0)
	burst.spread = 85.0
	burst.initial_velocity_min = 3.5
	burst.initial_velocity_max = 7.0
	burst.color = Color(0.96, 0.45, 0.1) # Fire orange
	
	var mesh = BoxMesh.new()
	mesh.size = Vector3(0.08, 0.08, 0.08)
	burst.mesh = mesh
	
	get_tree().current_scene.add_child(burst)
	burst.global_position = global_position
	
	get_tree().create_timer(0.6).timeout.connect(burst.queue_free)
