class_name ProximityMine
extends Node2D

## Habilidade 1 de Boran — Mina de Proximidade
## Mina mecânica depositada no percurso que detona quando um inimigo se aproxima.

var damage: float = 110.0
var trigger_radius: float = 40.0
var explosion_radius: float = 90.0
var is_primed: bool = false
var is_exploding: bool = false

var sprite: Sprite2D

func setup(pos: Vector2, dmg: float = 110.0) -> void:
	global_position = pos
	damage = dmg

func _ready() -> void:
	sprite = Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/shockwave_ground.svg"):
		sprite.texture = load("res://assets/effects/shockwave_ground.svg")
	sprite.position = Vector2.ZERO
	sprite.scale = Vector2(0.35, 0.35)
	sprite.modulate = Color(1.8, 0.4, 0.4, 0.9)
	add_child(sprite)
	
	# Tempo para armar a mina (0.5s)
	get_tree().create_timer(0.5).timeout.connect(func(): is_primed = true)

func _process(_delta: float) -> void:
	if not is_primed or is_exploding:
		return
		
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			if global_position.distance_to(node.global_position) <= trigger_radius:
				_detonate()
				break

func _detonate() -> void:
	is_exploding = true
	AudioManager.play_sfx("mine_explosion")
	
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			if global_position.distance_to(node.global_position) <= explosion_radius:
				node.take_damage(damage)
				
	var t := create_tween()
	t.tween_property(sprite, "scale", Vector2(1.5, 1.5), 0.2)
	t.parallel().tween_property(sprite, "modulate:a", 0.0, 0.2)
	t.tween_callback(queue_free)
