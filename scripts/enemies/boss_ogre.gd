class_name BossOgre
extends EnemyBase

## Arquétipo 5: Chefe — Ogro das Ruínas
## Chefe colossal com fases de comportamento por vida, rugido de fúria e ataque em área de tremor.

signal boss_hp_updated(current_hp: float, max_hp: float)
signal boss_enraged()

var is_enraged: bool = false
var boss_title: String = "Devastador Ancestral"
var slam_cooldown: float = 6.0
var slam_timer: float = 6.0

func _ready() -> void:
	enemy_id = "boss_ogre"
	enemy_name = "Ogro das Ruínas"
	max_hp = 1200.0
	base_speed = 46.0
	damage_to_base = 60
	bounty_coins = 300
	
	super._ready()
	set_enemy_texture("res://assets/sprites/enemy_boss_ogre.svg")
	
	# Ajusta escala maior para o chefe
	if sprite:
		sprite.scale = Vector2(1.3, 1.3)
	if hp_bar:
		hp_bar.custom_minimum_size = Vector2(80, 10)
		hp_bar.position = Vector2(-40, -56)
		
	# Animação de entrada do chefe com rugido
	_execute_entry_roar()

func _execute_entry_roar() -> void:
	AudioManager.play_sfx("boss_roar")
	var tween := create_tween()
	tween.tween_property(self, "scale", Vector2(1.2, 1.2), 0.25)
	tween.tween_property(self, "scale", Vector2(1.0, 1.0), 0.25)

func _process(delta: float) -> void:
	super._process(delta)
	if is_dead:
		return
		
	# Habilidade de pisada sísmica periódica
	slam_timer -= delta
	if slam_timer <= 0.0:
		slam_timer = slam_cooldown
		_execute_earthquake_slam()

func take_damage(amount: float) -> void:
	super.take_damage(amount)
	boss_hp_updated.emit(current_hp, max_hp)
	
	# Transição de Fase aos 50% de HP (Fúria Berserk)
	if not is_enraged and current_hp <= (max_hp * 0.5):
		_enter_enrage_phase()

func _enter_enrage_phase() -> void:
	is_enraged = true
	base_speed = 64.0 # Aumento de velocidade
	slam_cooldown = 4.5 # Golpes mais frequentes
	_recalculate_speed()
	
	AudioManager.play_sfx("boss_roar")
	boss_enraged.emit()
	
	# Flash e tonalidade avermelhada permanente
	if sprite:
		var t := create_tween()
		t.tween_property(sprite, "modulate", Color(2.5, 0.4, 0.4, 1.0), 0.3)
		t.tween_property(sprite, "modulate", Color(1.8, 0.6, 0.6, 1.0), 0.3)

func _execute_earthquake_slam() -> void:
	AudioManager.play_sfx("explosion")
	
	# Efeito visual de onda de choque no solo
	var shockwave := Sprite2D.new()
	if ResourceLoader.exists("res://assets/effects/shockwave_ground.svg"):
		shockwave.texture = load("res://assets/effects/shockwave_ground.svg")
	shockwave.global_position = global_position
	shockwave.scale = Vector2(0.3, 0.3)
	get_parent().add_child(shockwave)
	
	var tween := shockwave.create_tween()
	tween.tween_property(shockwave, "scale", Vector2(2.0, 2.0), 0.4)
	tween.parallel().tween_property(shockwave, "modulate:a", 0.0, 0.4)
	tween.tween_callback(shockwave.queue_free)
	
	# Perturba defensores próximos (atrasa seus ciclos de ataque)
	var defenders := get_tree().get_nodes_in_group("defenders")
	for def in defenders:
		if def is DefenderBase and is_instance_valid(def):
			if global_position.distance_to(def.global_position) <= 220.0:
				def.attack_cooldown_timer = maxf(def.attack_cooldown_timer, 0.8)
				def._set_visual_state(DefenderBase.VisualState.HIT, 0.3)

func die() -> void:
	# Bônus adicional de EXP por derrotar chefe e registro na missão
	ProgressionManager.add_base_exp(120)
	ProgressionManager.update_quest_progress("quest_boss_ogre", 1)
	super.die()
