class_name ArcherTower
extends Node3D

## Archer Tower (Torre de Arqueiros) — Heroes Tower Defense
## 3D Isometric Defensive Bastion with targeting AI, aim rotation, ballistic projectile firing, and recoil.

@export var attack_range: float = 6.5
@export var attack_cooldown: float = 1.2
@export var projectile_scene: PackedScene

@onready var turret_head: Node3D = $TurretHead
@onready var bow_mount: Node3D = $TurretHead/BowMount
@onready var fire_point: Marker3D = $TurretHead/BowMount/FirePoint
@onready var range_ring: MeshInstance3D = $RangeRing

var _cooldown_timer: float = 0.0
var _current_target: Node3D = null
var _recoil_timer: float = 0.0
var _base_bow_pos: Vector3

func _ready() -> void:
	if bow_mount:
		_base_bow_pos = bow_mount.position
	if projectile_scene == null:
		var scene_res = load("res://scenes/projectile_arrow.tscn")
		if scene_res:
			projectile_scene = scene_res

func _process(delta: float) -> void:
	_cooldown_timer = maxf(0.0, _cooldown_timer - delta)
	_handle_recoil_animation(delta)
	
	_find_target()
	if _current_target and is_instance_valid(_current_target):
		_aim_at_target(delta)
		if _cooldown_timer <= 0.0:
			_shoot()

func _find_target() -> void:
	# If current target is dead or out of range, clear it
	if _current_target:
		if not is_instance_valid(_current_target) or global_position.distance_to(_current_target.global_position) > attack_range:
			_current_target = null
	
	if _current_target != null:
		return
	
	# Find closest enemy from battlefield manager
	var battlefield: BattlefieldPrototype = get_tree().current_scene as BattlefieldPrototype
	if not battlefield:
		return
	
	var closest_dist: float = attack_range
	var closest_enemy: Node3D = null
	
	for enemy in battlefield.get_active_enemies():
		if not is_instance_valid(enemy):
			continue
		if enemy.has_method("is_dead") and enemy.is_dead():
			continue
		var dist = global_position.distance_to(enemy.global_position)
		if dist <= closest_dist:
			closest_dist = dist
			closest_enemy = enemy
	
	_current_target = closest_enemy

func _aim_at_target(delta: float) -> void:
	if not turret_head or not _current_target:
		return
	var target_pos = _current_target.global_position
	target_pos.y = turret_head.global_position.y # Rotate only on horizontal plane
	var target_transform = turret_head.global_transform.looking_at(target_pos, Vector3.UP)
	turret_head.global_transform = turret_head.global_transform.interpolate_with(target_transform, delta * 10.0)

func _shoot() -> void:
	if not _current_target or projectile_scene == null:
		return
	
	_cooldown_timer = attack_cooldown
	_recoil_timer = 0.20 # 200ms recoil kickback
	
	# Instantiate and launch physical ballistic arrow
	var projectile: ProjectileArrow = projectile_scene.instantiate() as ProjectileArrow
	if not projectile:
		return
	
	var spawn_pos = fire_point.global_position if fire_point else turret_head.global_position + Vector3(0, 0.5, 0)
	get_tree().current_scene.add_child(projectile)
	projectile.global_position = spawn_pos
	projectile.launch(spawn_pos, _current_target)

func _handle_recoil_animation(delta: float) -> void:
	if not bow_mount:
		return
	if _recoil_timer > 0.0:
		_recoil_timer -= delta
		var kick_progress = _recoil_timer / 0.20
		bow_mount.position = _base_bow_pos - Vector3(0, 0, kick_progress * 0.15)
	else:
		bow_mount.position = _base_bow_pos
