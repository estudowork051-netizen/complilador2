class_name BoranCharacter
extends DefenderBase

## Boran — Engenheiro de Armadilhas
## Constrói minas de proximidade táticas e torretas autônomas no campo de batalha.

func _ready() -> void:
	character_id = "boran"
	super._ready()

func _execute_basic_attack(target: EnemyBase) -> void:
	super._execute_basic_attack(target)
	
	var bolt := ProjectileBase.new()
	bolt.setup(global_position + Vector2(10, -8), target, current_damage, 520.0)
	bolt.set_projectile_texture("res://assets/effects/arcane_projectile.svg")
	
	var container := get_tree().current_scene.find_child("ProjectilesContainer", true, false)
	if container:
		container.add_child(bolt)
	else:
		get_parent().add_child(bolt)

func _execute_skill_1(target_pos: Vector2) -> void:
	var final_pos := target_pos
	if final_pos == Vector2.ZERO:
		if is_instance_valid(target_enemy) and not target_enemy.is_dead:
			final_pos = target_enemy.global_position
		else:
			final_pos = global_position + Vector2(100, 0)
			
	var mine := ProximityMine.new()
	mine.setup(final_pos, current_skill_damage * 1.4)
	
	var container := get_tree().current_scene.find_child("EffectsContainer", true, false)
	if container:
		container.add_child(mine)
	else:
		get_parent().add_child(mine)

func _execute_skill_2(target_pos: Vector2) -> void:
	var final_pos := target_pos
	if final_pos == Vector2.ZERO:
		final_pos = global_position + Vector2(40, -30)
		
	var turret := ClockworkTurret.new()
	turret.setup(final_pos, 8.0, current_damage * 0.75)
	
	var container := get_tree().current_scene.find_child("EffectsContainer", true, false)
	if container:
		container.add_child(turret)
	else:
		get_parent().add_child(turret)
