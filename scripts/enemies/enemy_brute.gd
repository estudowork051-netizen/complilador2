class_name EnemyBrute
extends EnemyBase

## Arquétipo 3: Bruto Blindado
## Inimigo massivo de alta resistência e movimentação lenta, ameaça pesada para a base central.

func _ready() -> void:
	enemy_id = "brute"
	enemy_name = "Bruto Blindado"
	max_hp = 320.0
	base_speed = 48.0
	damage_to_base = 30
	bounty_coins = 55
	super._ready()
	set_enemy_texture("res://assets/sprites/enemy_brute.svg")
