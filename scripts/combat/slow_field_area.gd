class_name SlowFieldArea
extends Node2D

## Zona Mágica da Habilidade Especial 2 de Kael — Campo de Lentidão
## Reduz a velocidade dos inimigos que passam pela área e restaura ao término ou saída.

var duration: float = 6.0
var radius: float = 110.0
var slow_factor: float = 0.50 # Reduz velocidade para 50%
var time_left: float = 6.0
var unique_source_id: String = ""

var _affected_enemies: Dictionary = {}
var fx_sprite: Sprite2D

func setup(pos: Vector2, dur: float = 6.0, rad: float = 110.0, slow_f: float = 0.50) -> void:
	global_position = pos
	duration = dur
	time_left = dur
	radius = rad
	slow_factor = slow_f
	unique_source_id = "slow_field_%d" % get_instance_id()

func _ready() -> void:
	AudioManager.play_sfx("slow")
	
	fx_sprite = Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/slow_field_fx.svg"):
		fx_sprite.texture = load("res://assets/effects/slow_field_fx.svg")
	fx_sprite.position = Vector2.ZERO
	var initial_scale := (radius / 64.0)
	fx_sprite.scale = Vector2(initial_scale, initial_scale)
	add_child(fx_sprite)

func _process(delta: float) -> void:
	time_left -= delta
	
	# Efeito visual de rotação mística
	if fx_sprite:
		fx_sprite.rotation += 1.2 * delta
		
	# Atualiza desaceleração dos inimigos
	_update_affected_enemies()
	
	if time_left <= 0.0:
		_cleanup_and_finish()

func _update_affected_enemies() -> void:
	var current_in_range: Dictionary = {}
	var enemies := get_tree().get_nodes_in_group("enemies")
	
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			var dist := global_position.distance_to(node.global_position)
			if dist <= radius:
				current_in_range[node] = true
				if not _affected_enemies.has(node):
					_affected_enemies[node] = true
					node.apply_slow(slow_factor, unique_source_id)
					
	# Remove efeito de inimigos que saíram do raio
	var to_remove := []
	for enemy in _affected_enemies.keys():
		if not is_instance_valid(enemy) or enemy.is_dead or not current_in_range.has(enemy):
			to_remove.append(enemy)
			if is_instance_valid(enemy) and not enemy.is_dead:
				enemy.remove_slow(unique_source_id)
				
	for dead_or_exited in to_remove:
		_affected_enemies.erase(dead_or_exited)

func _cleanup_and_finish() -> void:
	# Restaura a velocidade de todos os inimigos que ainda estavam sob o efeito
	for enemy in _affected_enemies.keys():
		if is_instance_valid(enemy) and not enemy.is_dead:
			enemy.remove_slow(unique_source_id)
	_affected_enemies.clear()
	
	# Fade out visual
	if fx_sprite:
		var tween := create_tween()
		tween.tween_property(fx_sprite, "modulate:a", 0.0, 0.25)
		tween.tween_callback(queue_free)
	else:
		queue_free()

func _exit_tree() -> void:
	# Garantia adicional de restauração caso o nó seja removido da cena abruptamente
	for enemy in _affected_enemies.keys():
		if is_instance_valid(enemy) and not enemy.is_dead:
			enemy.remove_slow(unique_source_id)
