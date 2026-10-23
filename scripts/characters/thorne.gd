class_name ThorneCharacter
extends DefenderBase

## Thorne — Guerreiro Protetor
## Defensor pesado que utiliza martelo sísmico e ergue barreiras defensivas para a base.

func _ready() -> void:
	character_id = "thorne"
	super._ready()

func _execute_basic_attack(target: EnemyBase) -> void:
	super._execute_basic_attack(target)
	AudioManager.play_sfx("explosion")
	
	# Ataque contundente corpo-a-corpo / choque frontal
	if is_instance_valid(target) and not target.is_dead:
		target.take_damage(current_damage)
		
	var spark := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/shockwave_ground.svg"):
		spark.texture = load("res://assets/effects/shockwave_ground.svg")
	spark.global_position = target.global_position if is_instance_valid(target) else global_position
	spark.scale = Vector2(0.3, 0.3)
	spark.modulate = Color(1.8, 1.2, 0.4, 1.0)
	get_parent().add_child(spark)
	
	var t := spark.create_tween()
	t.tween_property(spark, "scale", Vector2(0.8, 0.8), 0.15)
	t.parallel().tween_property(spark, "modulate:a", 0.0, 0.15)
	t.tween_callback(spark.queue_free)

func _execute_skill_1(target_pos: Vector2) -> void:
	var final_pos := target_pos
	if final_pos == Vector2.ZERO:
		if is_instance_valid(target_enemy) and not target_enemy.is_dead:
			final_pos = target_enemy.global_position
		else:
			final_pos = global_position + Vector2(100, 0)
			
	var slam := GroundSlamEffect.new()
	slam.setup(final_pos, current_skill_damage * 1.3, 130.0)
	
	var container := get_tree().current_scene.find_child("EffectsContainer", true, false)
	if container:
		container.add_child(slam)
	else:
		get_parent().add_child(slam)

func _execute_skill_2(_target_pos: Vector2) -> void:
	var shield := BastionShieldEffect.new()
	shield.setup(global_position, 6.0, 20)
	
	var container := get_tree().current_scene.find_child("EffectsContainer", true, false)
	if container:
		container.add_child(shield)
	else:
		get_parent().add_child(shield)
