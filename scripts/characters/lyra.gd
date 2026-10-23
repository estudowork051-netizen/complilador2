class_name LyraCharacter
extends DefenderBase

## Lyra — Arqueira das Sombras
## Ataque rápido e eliminação de alvos prioritários à distância.

func _ready() -> void:
	character_id = "lyra"
	super._ready()

func _execute_basic_attack(target: EnemyBase) -> void:
	super._execute_basic_attack(target)
	
	var arrow := ShadowArrow.new()
	var spawn_pos := global_position + Vector2(12, -10)
	arrow.setup(spawn_pos, target, current_damage, 620.0)
	
	var container := get_tree().current_scene.find_child("ProjectilesContainer", true, false)
	if container:
		container.add_child(arrow)
	else:
		get_parent().add_child(arrow)

func _execute_skill_1(target_pos: Vector2) -> void:
	var final_pos := target_pos
	if final_pos == Vector2.ZERO:
		if is_instance_valid(target_enemy) and not target_enemy.is_dead:
			final_pos = target_enemy.global_position
		else:
			final_pos = _find_enemy_cluster_center()
			
	var rain := ArrowRainEffect.new()
	var dmg := current_skill_damage * 1.15
	rain.setup(final_pos, dmg, 95.0)
	
	var container := get_tree().current_scene.find_child("EffectsContainer", true, false)
	if container:
		container.add_child(rain)
	else:
		get_parent().add_child(rain)

func _execute_skill_2(target_pos: Vector2) -> void:
	var dir := Vector2.RIGHT
	if target_pos != Vector2.ZERO and target_pos != global_position:
		dir = (target_pos - global_position).normalized()
	elif is_instance_valid(target_enemy) and not target_enemy.is_dead:
		dir = (target_enemy.global_position - global_position).normalized()
		
	var piercing_shot := PiercingShotProjectile.new()
	var dmg := current_skill_damage * 1.6
	piercing_shot.setup(global_position + Vector2(16, -10), dir, dmg, 720.0)
	
	var container := get_tree().current_scene.find_child("ProjectilesContainer", true, false)
	if container:
		container.add_child(piercing_shot)
	else:
		get_parent().add_child(piercing_shot)

func _find_enemy_cluster_center() -> Vector2:
	var enemies := get_tree().get_nodes_in_group("enemies")
	if enemies.is_empty():
		return global_position + Vector2(180, 0)
		
	var avg_pos := Vector2.ZERO
	var count := 0
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			avg_pos += node.global_position
			count += 1
			
	if count > 0:
		return avg_pos / float(count)
	return global_position + Vector2(180, 0)
