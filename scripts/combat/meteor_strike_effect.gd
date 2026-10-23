class_name MeteorStrikeEffect
extends Node2D

## Habilidade 1 de Elysia — Meteoro Incandescente
## Impacto colossal do céu que detona em área, causando dano de fogo e rastro de cinzas.

var damage: float = 120.0
var radius: float = 110.0

func setup(pos: Vector2, dmg: float, rad: float = 110.0) -> void:
	global_position = pos
	damage = dmg
	radius = rad

func _ready() -> void:
	AudioManager.play_sfx("meteor")
	
	# Projétil do meteoro caindo do topo
	var meteor_sprite := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/fireball.svg"):
		meteor_sprite.texture = load("res://assets/effects/fireball.svg")
	meteor_sprite.position = Vector2(0, -200)
	meteor_sprite.scale = Vector2(2.5, 2.5)
	add_child(meteor_sprite)
	
	var tween := create_tween()
	tween.tween_property(meteor_sprite, "position", Vector2.ZERO, 0.25).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN)
	tween.tween_callback(func():
		meteor_sprite.queue_free()
		_detonate()
	)

func _detonate() -> void:
	AudioManager.play_sfx("explosion")
	
	# Aplica dano a todos os inimigos no raio
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			if global_position.distance_to(node.global_position) <= radius:
				node.take_damage(damage)
				
	# Onda de choque incandescente
	var blast := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/shockwave_ground.svg"):
		blast.texture = load("res://assets/effects/shockwave_ground.svg")
	blast.position = Vector2.ZERO
	blast.modulate = Color(2.5, 0.6, 0.2, 1.0)
	blast.scale = Vector2(0.3, 0.3)
	add_child(blast)
	
	var t := create_tween()
	var final_s := (radius / 64.0) * 1.5
	t.tween_property(blast, "scale", Vector2(final_s, final_s), 0.35)
	t.parallel().tween_property(blast, "modulate:a", 0.0, 0.35)
	t.tween_callback(queue_free)
