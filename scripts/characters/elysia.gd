class_name ElysiaCharacter
extends DefenderBase

## Elysia — Maga Elemental
## Especialista em feitiços pirocinéticos em área, chuva de meteoros e muralhas de chamas.

func _ready() -> void:
	character_id = "elysia"
	super._ready()

func _execute_basic_attack(target: EnemyBase) -> void:
	super._execute_basic_attack(target)
	
	var fireball := ProjectileBase.new()
	fireball.setup(global_position + Vector2(10, -12), target, current_damage, 480.0)
	fireball.set_projectile_texture("res://assets/effects/fireball.svg")
	
	var container := get_tree().current_scene.find_child("ProjectilesContainer", true, false)
	if container:
		container.add_child(fireball)
	else:
		get_parent().add_child(fireball)

func _execute_skill_1(target_pos: Vector2) -> void:
	var final_pos := target_pos
	if final_pos == Vector2.ZERO:
		if is_instance_valid(target_enemy) and not target_enemy.is_dead:
			final_pos = target_enemy.global_position
		else:
			final_pos = global_position + Vector2(150, 0)
			
	var meteor := MeteorStrikeEffect.new()
	meteor.setup(final_pos, current_skill_damage * 1.5, 120.0)
	
	var container := get_tree().current_scene.find_child("EffectsContainer", true, false)
	if container:
		container.add_child(meteor)
	else:
		get_parent().add_child(meteor)

func _execute_skill_2(target_pos: Vector2) -> void:
	var final_pos := target_pos
	if final_pos == Vector2.ZERO:
		if is_instance_valid(target_enemy) and not target_enemy.is_dead:
			final_pos = target_enemy.global_position
		else:
			final_pos = global_position + Vector2(120, 0)
			
	var flame_wall := FlameWallArea.new()
	flame_wall.setup(final_pos, 6.0, current_skill_damage * 0.45)
	
	var container := get_tree().current_scene.find_child("EffectsContainer", true, false)
	if container:
		container.add_child(flame_wall)
	else:
		get_parent().add_child(flame_wall)
