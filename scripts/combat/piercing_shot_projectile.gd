class_name PiercingShotProjectile
extends Node2D

## Projétil da Habilidade Especial 2 de Lyra — Disparo Perfurante
## Atravessa todos os inimigos no seu trajeto retilíneo, garantindo 1 único golpe por alvo.

var direction: Vector2 = Vector2.RIGHT
var speed: float = 650.0
var damage: float = 100.0
var max_range: float = 650.0
var distance_traveled: float = 0.0

# Dicionário para garantir que cada inimigo receba dano apenas uma vez
var _already_hit_enemies: Dictionary = {}

var sprite: Sprite2D

func setup(origin_pos: Vector2, dir: Vector2, dmg: float, spd: float = 650.0) -> void:
	global_position = origin_pos
	direction = dir.normalized()
	damage = dmg
	speed = spd
	rotation = direction.angle()

func _ready() -> void:
	AudioManager.play_sfx("piercing")
	
	sprite = Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/piercing_beam.svg"):
		sprite.texture = load("res://assets/effects/piercing_beam.svg")
	sprite.position = Vector2.ZERO
	add_child(sprite)

func _process(delta: float) -> void:
	var step := speed * delta
	global_position += direction * step
	distance_traveled += step
	
	# Verifica colisão com inimigos ao longo do percurso
	_check_piercing_hits()
	
	if distance_traveled >= max_range:
		queue_free()

func _check_piercing_hits() -> void:
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			if not _already_hit_enemies.has(node):
				# Distância para o centro do feixe perfurante
				if global_position.distance_to(node.global_position) <= 32.0:
					_already_hit_enemies[node] = true
					node.take_damage(damage)
					_spawn_hit_spark(node.global_position)

func _spawn_hit_spark(pos: Vector2) -> void:
	var spark := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/shadow_arrow.svg"):
		spark.texture = load("res://assets/effects/shadow_arrow.svg")
	spark.global_position = pos
	spark.scale = Vector2(1.8, 1.8)
	spark.modulate = Color(0.8, 1.5, 0.5, 1.0)
	get_parent().add_child(spark)
	
	var t := spark.create_tween()
	t.tween_property(spark, "scale", Vector2(2.5, 2.5), 0.15)
	t.parallel().tween_property(spark, "modulate:a", 0.0, 0.15)
	t.tween_callback(spark.queue_free)
