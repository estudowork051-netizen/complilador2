class_name SmokeVeilArea
extends Node2D

## Habilidade 2 de Vesper — Gaiola Sombria / Cortina de Fumaça
## Nuvem negra que desorienta os inimigos, reduzindo drasticamente sua velocidade em 60% por 5 segundos.

var duration: float = 5.0
var radius: float = 100.0
var time_left: float = 5.0
var unique_id: String = ""

var _affected_enemies: Dictionary = {}
var fx_sprite: Sprite2D

func setup(pos: Vector2, dur: float = 5.0, rad: float = 100.0) -> void:
	global_position = pos
	duration = dur
	time_left = dur
	radius = rad
	unique_id = "smoke_veil_%d" % get_instance_id()

func _ready() -> void:
	fx_sprite = Sprite2D.new()
	if ResourceLoader.exists("res://assets/icons/skill_smoke_veil.svg"):
		fx_sprite.texture = load("res://assets/icons/skill_smoke_veil.svg")
	fx_sprite.position = Vector2.ZERO
	fx_sprite.scale = Vector2(2.5, 2.5)
	fx_sprite.modulate = Color(0.1, 0.1, 0.1, 0.8)
	add_child(fx_sprite)

func _process(delta: float) -> void:
	time_left -= delta
	if fx_sprite:
		fx_sprite.rotation += 0.5 * delta
		
	var in_range: Dictionary = {}
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			if global_position.distance_to(node.global_position) <= radius:
				in_range[node] = true
				if not _affected_enemies.has(node):
					_affected_enemies[node] = true
					node.apply_slow(0.40, unique_id) # -60% vel
					
	var to_clean := []
	for e in _affected_enemies.keys():
		if not is_instance_valid(e) or e.is_dead or not in_range.has(e):
			to_clean.append(e)
			if is_instance_valid(e) and not e.is_dead:
				e.remove_slow(unique_id)
				
	for rem in to_clean:
		_affected_enemies.erase(rem)
		
	if time_left <= 0.0:
		_cleanup()

func _cleanup() -> void:
	for e in _affected_enemies.keys():
		if is_instance_valid(e) and not e.is_dead:
			e.remove_slow(unique_id)
	_affected_enemies.clear()
	
	if fx_sprite:
		var t := create_tween()
		t.tween_property(fx_sprite, "modulate:a", 0.0, 0.3)
		t.tween_callback(queue_free)
	else:
		queue_free()

func _exit_tree() -> void:
	for e in _affected_enemies.keys():
		if is_instance_valid(e) and not e.is_dead:
			e.remove_slow(unique_id)
