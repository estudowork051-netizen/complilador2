class_name GroundSlamEffect
extends Node2D

## Habilidade 1 de Thorne — Golpe Sísmico
## Impacto no solo que reverbera em ondas concentradas causando atordoamento e dano massivo.

var damage: float = 90.0
var radius: float = 120.0

func setup(pos: Vector2, dmg: float, rad: float = 120.0) -> void:
	global_position = pos
	damage = dmg
	radius = rad

func _ready() -> void:
	AudioManager.play_sfx("explosion")
	
	var shockwave := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/shockwave_ground.svg"):
		shockwave.texture = load("res://assets/effects/shockwave_ground.svg")
	shockwave.position = Vector2.ZERO
	shockwave.modulate = Color(1.5, 1.2, 0.4, 1.0)
	shockwave.scale = Vector2(0.2, 0.2)
	add_child(shockwave)
	
	# Aplica dano e micro-atraso nos inimigos na área
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			if global_position.distance_to(node.global_position) <= radius:
				node.take_damage(damage)
				node.apply_slow(0.2, "thorne_stun_%d" % get_instance_id())
				# Remove o mini-stun após 1.0s
				get_tree().create_timer(1.0).timeout.connect(func():
					if is_instance_valid(node) and not node.is_dead:
						node.remove_slow("thorne_stun_%d" % get_instance_id())
				)
				
	var tween := create_tween()
	var final_scale := (radius / 64.0)
	tween.tween_property(shockwave, "scale", Vector2(final_scale, final_scale), 0.3)
	tween.parallel().tween_property(shockwave, "modulate:a", 0.0, 0.3)
	tween.tween_callback(queue_free)
