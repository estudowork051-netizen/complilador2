class_name VesperCharacter
extends DefenderBase

## Vesper — Assassino de Longo Alcance
## Atirador furtivo com alcance extremo, focado em abater chefes e alvos de alta resistência.

func _ready() -> void:
	character_id = "vesper"
	super._ready()

func _execute_basic_attack(target: EnemyBase) -> void:
	super._execute_basic_attack(target)
	
	var bullet := ProjectileBase.new()
	bullet.setup(global_position + Vector2(16, -10), target, current_damage, 850.0)
	bullet.set_projectile_texture("res://assets/effects/sniper_bullet.svg")
	
	var container := get_tree().current_scene.find_child("ProjectilesContainer", true, false)
	if container:
		container.add_child(bullet)
	else:
		get_parent().add_child(bullet)

func _execute_skill_1(_target_pos: Vector2) -> void:
	# Encontra o inimigo de maior vida atual (chefe ou bruto blindado)
	var highest_hp_enemy: EnemyBase = null
	var max_hp_found := -1.0
	
	var enemies := get_tree().get_nodes_in_group("enemies")
	for node in enemies:
		if node is EnemyBase and is_instance_valid(node) and not node.is_dead:
			if node.current_hp > max_hp_found:
				max_hp_found = node.current_hp
				highest_hp_enemy = node
				
	if highest_hp_enemy:
		var snipe := LethalSnipeProjectile.new()
		snipe.setup(global_position + Vector2(18, -12), highest_hp_enemy, current_skill_damage * 2.2)
		var container := get_tree().current_scene.find_child("ProjectilesContainer", true, false)
		if container:
			container.add_child(snipe)
		else:
			get_parent().add_child(snipe)

func _execute_skill_2(target_pos: Vector2) -> void:
	var final_pos := target_pos
	if final_pos == Vector2.ZERO:
		if is_instance_valid(target_enemy) and not target_enemy.is_dead:
			final_pos = target_enemy.global_position
		else:
			final_pos = global_position + Vector2(140, 0)
			
	var smoke := SmokeVeilArea.new()
	smoke.setup(final_pos, 5.0, 110.0)
	
	var container := get_tree().current_scene.find_child("EffectsContainer", true, false)
	if container:
		container.add_child(smoke)
	else:
		get_parent().add_child(smoke)
