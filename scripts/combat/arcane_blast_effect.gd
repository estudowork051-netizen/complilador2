class_name ArcaneBlastEffect
extends Node2D

## Efeito da Habilidade Especial 1 de Kael — Explosão Arcana

var damage: float = 85.0
var radius: float = 95.0

func setup(pos: Vector2, dmg: float, rad: float = 95.0) -> void:
	global_position = pos
	damage = dmg
	radius = rad

func _ready() -> void:
	_trigger_blast()

func _trigger_blast() -> void:
	AudioManager.play_sfx("explosion")
	
	# Aplica dano a todos os inimigos no raio da explosão
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			if global_position.distance_to(node.global_position) <= radius:
				node.take_damage(damage)
				
	# Visual da explosão
	var fx := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/arcane_blast_fx.svg"):
		fx.texture = load("res://assets/effects/arcane_blast_fx.svg")
	fx.position = Vector2.ZERO
	fx.scale = Vector2(0.2, 0.2)
	add_child(fx)
	
	var tween := create_tween()
	var target_scale := (radius / 48.0)
	tween.tween_property(fx, "scale", Vector2(target_scale, target_scale), 0.35).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	tween.parallel().tween_property(fx, "modulate:a", 0.0, 0.35)
	tween.tween_callback(queue_free)
