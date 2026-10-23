class_name KaelCharacter
extends DefenderBase

## Kael — Guardião Arcano
## Especialista em dano mágico equilibrado e controle de grupos com feitiços arcanos.

func _ready() -> void:
	character_id = "kael"
	super._ready()

func _execute_basic_attack(target: EnemyBase) -> void:
	super._execute_basic_attack(target)
	
	var projectile := ArcaneProjectile.new()
	var spawn_pos := global_position + Vector2(10, -15)
	projectile.setup(spawn_pos, target, current_damage, 480.0)
	
	var container := get_tree().current_scene.find_child("ProjectilesContainer", true, false)
	if container:
		container.add_child(projectile)
	else:
		get_parent().add_child(projectile)

func _execute_skill_1(target_pos: Vector2) -> void:
	var final_pos := target_pos
	if final_pos == Vector2.ZERO:
		if is_instance_valid(target_enemy) and not target_enemy.is_dead:
			final_pos = target_enemy.global_position
		else:
			final_pos = _find_enemy_cluster_center()
			
	var blast := ArcaneBlastEffect.new()
	var dmg := current_skill_damage * 1.3
	blast.setup(final_pos, dmg, 100.0)
	
	var container := get_tree().current_scene.find_child("EffectsContainer", true, false)
	if container:
		container.add_child(blast)
	else:
		get_parent().add_child(blast)

func _execute_skill_2(target_pos: Vector2) -> void:
	var final_pos := target_pos
	if final_pos == Vector2.ZERO:
		if is_instance_valid(target_enemy) and not target_enemy.is_dead:
			final_pos = target_enemy.global_position
		else:
			final_pos = _find_enemy_cluster_center()
			
	var slow_field := SlowFieldArea.new()
	slow_field.setup(final_pos, 6.0, 115.0, 0.50)
	
	var container := get_tree().current_scene.find_child("EffectsContainer", true, false)
	if container:
		container.add_child(slow_field)
	else:
		get_parent().add_child(slow_field)

func _find_enemy_cluster_center() -> Vector2:
	var enemies := get_tree().get_nodes_in_group("enemies")
	if enemies.is_empty():
		return global_position + Vector2(150, 0)
		
	var avg_pos := Vector2.ZERO
	var count := 0
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			avg_pos += node.global_position
			count += 1
			
	if count > 0:
		return avg_pos / float(count)
	return global_position + Vector2(150, 0)
