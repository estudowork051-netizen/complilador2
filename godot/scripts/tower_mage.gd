class_name MageTower
extends Node3D

## Mage Tower (Torre de Magos) — Heroes Tower Defense
## 3D Isometric Arcane Spire with levitating crystal, rune charge, and homing magic bolts.

@export var attack_range: float = 7.0
@export var attack_cooldown: float = 1.6
@export var projectile_scene: PackedScene

@onready var crystal_mesh: Node3D = get_node_or_null("SpireColumn/LevitatingCrystal") if has_node("SpireColumn/LevitatingCrystal") else get_node_or_null("CrystalMount")
@onready var fire_point: Marker3D = get_node_or_null("SpireColumn/LevitatingCrystal/FirePoint") if has_node("SpireColumn/LevitatingCrystal/FirePoint") else get_node_or_null("CrystalMount/FirePoint")
@onready var charge_light: OmniLight3D = get_node_or_null("SpireColumn/ChargeLight")

var _cooldown_timer: float = 0.0
var _current_target: Node3D = null
var _crystal_base_y: float = 2.2
var _anim_time: float = 0.0

func _ready() -> void:
	if projectile_scene == null:
		var scene_res = load("res://scenes/projectile_magic.tscn")
		if scene_res:
			projectile_scene = scene_res

func _process(delta: float) -> void:
	_anim_time += delta
	_cooldown_timer = maxf(0.0, _cooldown_timer - delta)
	
	# Levitating crystal animation
	if crystal_mesh:
		crystal_mesh.position.y = _crystal_base_y + sin(_anim_time * 3.5) * 0.15
		crystal_mesh.rotate_y(delta * 2.0)
	
	# Glow pulse
	if charge_light:
		charge_light.light_energy = 1.0 + sin(_anim_time * 6.0) * 0.5
	
	_find_target()
	if _current_target and is_instance_valid(_current_target):
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

func _shoot() -> void:
	if not _current_target or projectile_scene == null:
		return
	
	_cooldown_timer = attack_cooldown
	
	var projectile: ProjectileMagic = projectile_scene.instantiate() as ProjectileMagic
	if not projectile:
		return
	
	var spawn_pos = fire_point.global_position if fire_point else global_position + Vector3(0, 2.2, 0)
	get_tree().current_scene.add_child(projectile)
	projectile.launch(spawn_pos, _current_target)
