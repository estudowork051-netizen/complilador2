class_name DarkSorcerer
extends EnemyBase

func _ready() -> void:
	enemy_id = "dark_sorcerer"
	enemy_name = "Feiticeiro das Trevas"
	max_hp = 90.0
	base_speed = 85.0
	damage_to_base = 15
	bounty_coins = 40
	super._ready()
	set_enemy_texture("res://assets/sprites/enemy_sorcerer.svg")
