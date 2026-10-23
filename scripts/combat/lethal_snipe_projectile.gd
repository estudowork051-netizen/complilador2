class_name LethalSnipeProjectile
extends Node2D

## Habilidade 1 de Vesper — Tiro Fatal
## Disparo balístico de longo alcance com mira direta na maior ameaça ativa.

var target_enemy: EnemyBase = null
var target_pos: Vector2 = Vector2.ZERO
var speed: float = 900.0
var damage: float = 180.0

var sprite: Sprite2D

func setup(origin_pos: Vector2, target: EnemyBase, dmg: float = 180.0) -> void:
	global_position = origin_pos
	target_enemy = target
	damage = dmg
	if is_instance_valid(target):
		target_pos = target.global_position

func _ready() -> void:
	AudioManager.play_sfx("sniper_shot")
	
	sprite = Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/sniper_bullet.svg"):
		sprite.texture = load("res://assets/effects/sniper_bullet.svg")
	sprite.position = Vector2.ZERO
	add_child(sprite)

func _process(delta: float) -> void:
	if is_instance_valid(target_enemy) and not target_enemy.is_dead:
		target_pos = target_enemy.global_position
		
	var dir := (target_pos - global_position).normalized()
	rotation = dir.angle()
	global_position += dir * speed * delta
	
	if global_position.distance_to(target_pos) <= 20.0:
		_impact()

func _impact() -> void:
	if is_instance_valid(target_enemy) and not target_enemy.is_dead:
		target_enemy.take_damage(damage)
		
	var spark := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/sniper_bullet.svg"):
		spark.texture = load("res://assets/effects/sniper_bullet.svg")
	spark.global_position = global_position
	spark.scale = Vector2(2.0, 2.0)
	spark.modulate = Color(2.0, 0.4, 0.4, 1.0)
	get_parent().add_child(spark)
	
	var t := spark.create_tween()
	t.tween_property(spark, "scale", Vector2(3.0, 3.0), 0.15)
	t.parallel().tween_property(spark, "modulate:a", 0.0, 0.15)
	t.tween_callback(spark.queue_free)
	
	queue_free()
