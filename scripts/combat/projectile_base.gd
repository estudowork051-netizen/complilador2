class_name ProjectileBase
extends Node2D

## Classe Base para Projéteis de Ataque dos Defensores

var target_enemy: EnemyBase = null
var target_pos: Vector2 = Vector2.ZERO
var speed: float = 400.0
var damage: float = 20.0
var max_lifetime: float = 3.0
var lifetime: float = 0.0

var sprite: Sprite2D

func _ready() -> void:
	sprite = Sprite2D.new()
	sprite.name = "Sprite"
	add_child(sprite)

func set_projectile_texture(texture_path: String) -> void:
	if ResourceLoader.exists(texture_path):
		var tex := load(texture_path) as Texture2D
		if tex and sprite:
			sprite.texture = tex

func setup(origin_pos: Vector2, target: EnemyBase, dmg: float, spd: float = 400.0) -> void:
	global_position = origin_pos
	target_enemy = target
	damage = dmg
	speed = spd
	if is_instance_valid(target):
		target_pos = target.global_position

func _process(delta: float) -> void:
	lifetime += delta
	if lifetime >= max_lifetime:
		queue_free()
		return
		
	if is_instance_valid(target_enemy) and not target_enemy.is_dead:
		target_pos = target_enemy.global_position
		
	var dir := (target_pos - global_position).normalized()
	rotation = dir.angle()
	global_position += dir * speed * delta
	
	if global_position.distance_to(target_pos) <= maxf(12.0, speed * delta * 1.5):
		_on_hit()

func _on_hit() -> void:
	if is_instance_valid(target_enemy) and not target_enemy.is_dead:
		target_enemy.take_damage(damage)
	_spawn_impact_vfx()
	queue_free()

func _spawn_impact_vfx() -> void:
	pass
