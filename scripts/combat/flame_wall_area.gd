class_name FlameWallArea
extends Node2D

## Habilidade 2 de Elysia — Muralha de Chamas
## Cria uma barreira de fogo persistente que queima continuamente os invasores no percurso.

var duration: float = 6.0
var dps: float = 35.0
var width: float = 140.0
var height: float = 70.0
var time_left: float = 6.0
var tick_timer: float = 0.5

var sprite: Sprite2D

func setup(pos: Vector2, dur: float = 6.0, damage_per_sec: float = 35.0) -> void:
	global_position = pos
	duration = dur
	time_left = dur
	dps = damage_per_sec

func _ready() -> void:
	AudioManager.play_sfx("flame")
	
	sprite = Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/flame_wall_fx.svg"):
		sprite.texture = load("res://assets/effects/flame_wall_fx.svg")
	sprite.position = Vector2.ZERO
	add_child(sprite)

func _process(delta: float) -> void:
	time_left -= delta
	tick_timer -= delta
	
	# Efeito visual de chama oscilando
	if sprite:
		sprite.scale.y = 1.0 + sin(Time.get_ticks_msec() * 0.015) * 0.15
		
	# Pulso de dano contínuo a cada 0.5s
	if tick_timer <= 0.0:
		tick_timer = 0.5
		_burn_enemies_in_area(dps * 0.5)
		
	if time_left <= 0.0:
		if sprite:
			var tween := create_tween()
			tween.tween_property(sprite, "modulate:a", 0.0, 0.3)
			tween.tween_callback(queue_free)
		else:
			queue_free()

func _burn_enemies_in_area(amount: float) -> void:
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			var offset: Vector2 = node.global_position - global_position
			if absf(offset.x) <= (width * 0.5) and absf(offset.y) <= (height * 0.5):
				node.take_damage(amount)
