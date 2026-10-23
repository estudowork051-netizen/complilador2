class_name ArrowRainEffect
extends Node2D

## Efeito da Habilidade Especial 1 de Lyra — Chuva de Flechas
## Lança uma saraivada que atinge todos os inimigos na área

var damage: float = 65.0
var radius: float = 90.0

func setup(pos: Vector2, dmg: float, rad: float = 90.0) -> void:
	global_position = pos
	damage = dmg
	radius = rad

func _ready() -> void:
	_execute_rain()

func _execute_rain() -> void:
	AudioManager.play_sfx("arrow")
	
	# Cria flechas caindo
	for i in range(6):
		var offset := Vector2(randf_range(-radius * 0.7, radius * 0.7), randf_range(-radius * 0.7, radius * 0.7))
		var arrow := Sprite2D.new()
		if ResourceLoader.exists("res://assets/effects/shadow_arrow.svg"):
			arrow.texture = load("res://assets/effects/shadow_arrow.svg")
		arrow.position = offset + Vector2(0, -60)
		arrow.rotation_degrees = 90.0
		add_child(arrow)
		
		var t := create_tween()
		t.tween_property(arrow, "position:y", offset.y, 0.15 + (i * 0.04))
		t.tween_property(arrow, "modulate:a", 0.0, 0.1)
		t.tween_callback(arrow.queue_free)
		
	# Delay sutil para impacto no solo
	get_tree().create_timer(0.2).timeout.connect(_apply_damage)

func _apply_damage() -> void:
	AudioManager.play_sfx("explosion")
	
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			if global_position.distance_to(node.global_position) <= radius:
				node.take_damage(damage)
				
	# Anel de impacto no chão
	var ring := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/slow_field_fx.svg"):
		ring.texture = load("res://assets/effects/slow_field_fx.svg")
	ring.position = Vector2.ZERO
	ring.modulate = Color(0.2, 1.2, 0.4, 0.8)
	ring.scale = Vector2(0.2, 0.2)
	add_child(ring)
	
	var tween := create_tween()
	var final_scale := (radius / 64.0)
	tween.tween_property(ring, "scale", Vector2(final_scale, final_scale), 0.3)
	tween.parallel().tween_property(ring, "modulate:a", 0.0, 0.3)
	tween.tween_callback(queue_free)
