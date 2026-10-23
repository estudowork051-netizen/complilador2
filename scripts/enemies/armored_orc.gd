class_name ArmoredOrc
extends EnemyBase

func _ready() -> void:
	enemy_id = "armored_orc"
	enemy_name = "Orc Blindado"
	max_hp = 140.0
	base_speed = 65.0
	damage_to_base = 20
	bounty_coins = 28
	super._ready()
	set_enemy_texture("res://assets/sprites/enemy_orc.svg")
