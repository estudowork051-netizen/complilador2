class_name ArcaneProjectile
extends ProjectileBase

func _ready() -> void:
	super._ready()
	set_projectile_texture("res://assets/effects/arcane_projectile.svg")

func _spawn_impact_vfx() -> void:
	AudioManager.play_sfx("magic")
	# Cria pequeno efeito de partículas / flash arcano
	var spark := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/arcane_projectile.svg"):
		spark.texture = load("res://assets/effects/arcane_projectile.svg")
	spark.global_position = global_position
	spark.scale = Vector2(1.5, 1.5)
	spark.modulate = Color(0.2, 0.9, 1.0, 0.9)
	get_parent().add_child(spark)
	
	var tween := spark.create_tween()
	tween.tween_property(spark, "scale", Vector2(2.8, 2.8), 0.2)
	tween.parallel().tween_property(spark, "modulate:a", 0.0, 0.2)
	tween.tween_callback(spark.queue_free)
