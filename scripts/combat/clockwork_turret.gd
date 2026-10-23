class_name ClockworkTurret
extends Node2D

## Habilidade 2 de Boran — Torreta Mecânica Auxiliar
## Posiciona uma torreta que atira rajadas de estilhaços automaticamente por 8 segundos.

var duration: float = 8.0
var dps: float = 24.0
var fire_rate: float = 0.4 # Disparos por segundo (intervalo de 0.4s)
var range_radius: float = 200.0

var time_left: float = 8.0
var fire_timer: float = 0.0

var sprite: Sprite2D

func setup(pos: Vector2, dur: float = 8.0, damage: float = 24.0) -> void:
	global_position = pos
	duration = dur
	time_left = dur
	dps = damage

func _ready() -> void:
	AudioManager.play_sfx("turret_shot")
	
	sprite = Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/turret_sprite.svg"):
		sprite.texture = load("res://assets/effects/turret_sprite.svg")
	sprite.position = Vector2.ZERO
	add_child(sprite)

func _process(delta: float) -> void:
	time_left -= delta
	fire_timer -= delta
	
	if fire_timer <= 0.0:
		fire_timer = fire_rate
		_shoot_nearest_enemy()
		
	if time_left <= 0.0:
		var t := create_tween()
		t.tween_property(self, "modulate:a", 0.0, 0.25)
		t.tween_callback(queue_free)

func _shoot_nearest_enemy() -> void:
	var best_enemy: EnemyBase = null
	var min_dist := range_radius
	
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			var d := global_position.distance_to(node.global_position)
			if d <= min_dist:
				min_dist = d
				best_enemy = node
				
	if best_enemy:
		AudioManager.play_sfx("turret_shot")
		best_enemy.take_damage(dps)
		if sprite:
			sprite.rotation = (best_enemy.global_position - global_position).angle()
