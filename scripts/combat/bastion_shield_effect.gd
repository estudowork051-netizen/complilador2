class_name BastionShieldEffect
extends Node2D

## Habilidade 2 de Thorne — Escudo Baluarte
## Ergue um domo radiante que recupera a integridade da fortaleza e desacelera invasores próximos.

var duration: float = 6.0
var heal_amount: int = 15
var time_left: float = 6.0
var ring_sprite: Sprite2D

func setup(pos: Vector2, dur: float = 6.0, heal: int = 15) -> void:
	global_position = pos
	duration = dur
	time_left = dur
	heal_amount = heal

func _ready() -> void:
	AudioManager.play_sfx("shield_up")
	
	ring_sprite = Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/slow_field_fx.svg"):
		ring_sprite.texture = load("res://assets/effects/slow_field_fx.svg")
	ring_sprite.position = Vector2.ZERO
	ring_sprite.modulate = Color(0.4, 0.9, 2.0, 0.8)
	ring_sprite.scale = Vector2(1.5, 1.5)
	add_child(ring_sprite)
	
	# Restaura parte da vida da fortaleza
	var combat_mgr := get_tree().current_scene.get_node_or_null(".")
	if combat_mgr and "current_base_health" in combat_mgr:
		combat_mgr.current_base_health = mini(combat_mgr.MAX_BASE_HEALTH, combat_mgr.current_base_health + heal_amount)
		if combat_mgr.has_signal("base_health_changed"):
			combat_mgr.base_health_changed.emit(combat_mgr.current_base_health, combat_mgr.MAX_BASE_HEALTH)

func _process(delta: float) -> void:
	time_left -= delta
	if ring_sprite:
		ring_sprite.rotation += 0.8 * delta
	if time_left <= 0.0:
		var tween := create_tween()
		tween.tween_property(self, "modulate:a", 0.0, 0.3)
		tween.tween_callback(queue_free)
