class_name ShadowArrow
extends ProjectileBase

func _ready() -> void:
	super._ready()
	set_projectile_texture("res://assets/effects/shadow_arrow.svg")

func _spawn_impact_vfx() -> void:
	AudioManager.play_sfx("arrow")
	var spark := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/shadow_arrow.svg"):
		spark.texture = load("res://assets/effects/shadow_arrow.svg")
	spark.global_position = global_position
	spark.modulate = Color(0.4, 1.2, 0.6, 0.8)
	get_parent().add_child(spark)
	
	var tween := spark.create_tween()
	tween.tween_property(spark, "scale", Vector2(1.8, 1.8), 0.15)
	tween.parallel().tween_property(spark, "modulate:a", 0.0, 0.15)
	tween.tween_callback(spark.queue_free)
