class_name GoblinScout
extends EnemyBase

func _ready() -> void:
	enemy_id = "goblin_scout"
	enemy_name = "Batedor Goblin"
	max_hp = 45.0
	base_speed = 115.0
	damage_to_base = 8
	bounty_coins = 12
	super._ready()
	set_enemy_texture("res://assets/sprites/enemy_goblin.svg")
