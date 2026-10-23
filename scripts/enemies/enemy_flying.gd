class_name EnemyFlying
extends EnemyBase

## Arquétipo 4: Criatura Voadora (Gárgula das Sombras)
## Desloca-se pelo ar sobrevoando o caminho, com oscilação vertical contínua e projeção de sombra.

var flight_time: float = 0.0

func _ready() -> void:
	enemy_id = "flying_gargoyle"
	enemy_name = "Gárgula Alada"
	max_hp = 75.0
	base_speed = 105.0
	damage_to_base = 14
	bounty_coins = 32
	super._ready()
	set_enemy_texture("res://assets/sprites/enemy_gargoyle.svg")

func _process(delta: float) -> void:
	super._process(delta)
	if is_dead:
		return
		
	# Efeito visual de voo batendo asas (oscilação vertical suave)
	flight_time += delta
	if sprite:
		sprite.position.y = -14.0 + sin(flight_time * 7.0) * 8.0
		# Asas batem variando a escala horizontal
		sprite.scale.x = 1.0 + cos(flight_time * 14.0) * 0.15
