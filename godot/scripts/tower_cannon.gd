class_name CannonTower
extends Node3D

## Cannon Tower (Torre Canhoneira) — Heroes Tower Defense
## 3D Isometric Fortified Bastion with swiveling iron cannon, heavy ballistic shells, recoil, and smoke.

@export var attack_range: float = 5.5
@export var attack_cooldown: float = 2.4
@export var projectile_scene: PackedScene

@onready var barrel_mount: Node3D = get_node_or_null("TurretMount/Barrel") if has_node("TurretMount/Barrel") else get_node_or_null("TurretChassis/CannonBarrel")
@onready var fire_point: Marker3D = get_node_or_null("TurretMount/Barrel/FirePoint") if has_node("TurretMount/Barrel/FirePoint") else get_node_or_null("TurretChassis/CannonBarrel/FirePoint")
@onready var turret_mount: Node3D = get_node_or_null("TurretMount") if has_node("TurretMount") else get_node_or_null("TurretChassis")

var _cooldown_timer: float = 0.0
var _current_target: Node3D = null
var _recoil_timer: float = 0.0
var _base_barrel_pos: Vector3

func _ready() -> void:
	if barrel_mount:
		_base_barrel_pos = barrel_mount.position
	if projectile_scene == null:
		var scene_res = load("res://scenes/projectile_cannonball.tscn")
		if scene_res:
			projectile_scene = scene_res

func _process(delta: float) -> void:
	_cooldown_timer = maxf(0.0, _cooldown_timer - delta)
	_handle_recoil(delta)
	
	_find_target()
	if _current_target and is_instance_valid(_current_target):
		_aim_at_target(delta)
		if _cooldown_timer <= 0.0:
			_shoot()

func _find_target() -> void:
	if _current_target:
		if not is_instance_valid(_current_target) or global_position.distance_to(_current_target.global_position) > attack_range:
			_current_target = null
	
	if _current_target != null:
		return
	
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
	if not turret_mount or not _current_target:
		return
	var target_pos = _current_target.global_position
	target_pos.y = turret_mount.global_position.y
	var target_transform = turret_mount.global_transform.looking_at(target_pos, Vector3.UP)
	turret_mount.global_transform = turret_mount.global_transform.interpolate_with(target_transform, delta * 6.0)

func _shoot() -> void:
	if not _current_target or projectile_scene == null:
		return
	
	_cooldown_timer = attack_cooldown
	_recoil_timer = 0.28 # Recoil duration
	
	var projectile: ProjectileCannonball = projectile_scene.instantiate() as ProjectileCannonball
	if not projectile:
		return
	
	var spawn_pos = fire_point.global_position if fire_point else turret_mount.global_position + Vector3(0, 0.5, 0.8)
	get_tree().current_scene.add_child(projectile)
	projectile.launch(spawn_pos, _current_target)
	
	var battlefield = get_tree().current_scene as BattlefieldPrototype
	if battlefield:
		battlefield.trigger_screen_shake(0.25, 0.25)

func _handle_recoil(delta: float) -> void:
	if not barrel_mount:
		return
	if _recoil_timer > 0.0:
		_recoil_timer -= delta
		var kick_progress = _recoil_timer / 0.28
		barrel_mount.position = _base_barrel_pos - Vector3(0, 0, kick_progress * 0.35)
	else:
		barrel_mount.position = _base_barrel_pos
