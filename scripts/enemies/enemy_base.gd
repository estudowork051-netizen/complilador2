class_name EnemyBase
extends PathFollow2D

## Classe Base para Inimigos do Tower Defense
## Movimenta-se ao longo de um Path2D, recebe dano, aplica debuffs e ataca o objetivo central.

signal enemy_died(enemy: EnemyBase)
signal enemy_reached_base(enemy: EnemyBase, damage: int)

@export var enemy_id: String = "generic_enemy"
@export var enemy_name: String = "Inimigo"
@export var max_hp: float = 50.0
@export var base_speed: float = 80.0
@export var damage_to_base: int = 10
@export var bounty_coins: int = 20

var current_hp: float = 50.0
var current_speed: float = 80.0
var is_dead: bool = false
var has_reached_base: bool = false

# Dicionário de lentidões ativas: { "source_id": slow_factor }
var _active_slows: Dictionary = {}

# Nós visuais internos
var sprite: Sprite2D
var hp_bar: ProgressBar
var damage_label: Label
var flash_timer: float = 0.0

func _ready() -> void:
	loop = false
	rotates = false
	current_hp = max_hp
	current_speed = base_speed
	_setup_visuals()

func _setup_visuals() -> void:
	# Sprite
	sprite = Sprite2D.new()
	sprite.name = "Sprite"
	add_child(sprite)
	
	# Barra de Vida
	hp_bar = ProgressBar.new()
	hp_bar.name = "HPBar"
	hp_bar.show_percentage = false
	hp_bar.custom_minimum_size = Vector2(48, 6)
	hp_bar.position = Vector2(-24, -36)
	hp_bar.max_value = max_hp
	hp_bar.value = current_hp
	
	# Estilo da barra de vida
	var bg_style := StyleBoxFlat.new()
	bg_style.bg_color = Color(0.15, 0.15, 0.2, 0.8)
	bg_style.set_corner_radius_all(2)
	hp_bar.add_theme_stylebox_override("background", bg_style)
	
	var fill_style := StyleBoxFlat.new()
	fill_style.bg_color = Color(0.85, 0.2, 0.2, 1.0)
	fill_style.set_corner_radius_all(2)
	hp_bar.add_theme_stylebox_override("fill", fill_style)
	
	add_child(hp_bar)

func set_enemy_texture(texture_path: String) -> void:
	if ResourceLoader.exists(texture_path):
		var tex := load(texture_path) as Texture2D
		if tex and sprite:
			sprite.texture = tex

func _process(delta: float) -> void:
	if is_dead:
		return
		
	# Movimento ao longo da rota
	progress += current_speed * delta
	
	# Feedback de flash de dano
	if flash_timer > 0.0:
		flash_timer -= delta
		if flash_timer <= 0.0 and sprite:
			sprite.modulate = Color.WHITE
			
	# Chegada à Base Central
	if progress_ratio >= 0.999 and not has_reached_base:
		has_reached_base = true
		_on_reach_base()

func take_damage(amount: float) -> void:
	if is_dead or has_reached_base:
		return
		
	current_hp -= amount
	if hp_bar:
		hp_bar.value = maxf(0.0, current_hp)
		
	# Flash visual de impacto
	if sprite:
		sprite.modulate = Color(1.8, 0.4, 0.4, 1.0)
		flash_timer = 0.12
		
	_show_damage_popup(amount)
	
	if current_hp <= 0.0:
		die()

func _show_damage_popup(amount: float) -> void:
	var label := Label.new()
	label.text = "-%d" % int(amount)
	label.position = Vector2(randf_range(-15, 15), -45)
	label.modulate = Color(1.0, 0.85, 0.3, 1.0)
	label.add_theme_font_size_override("font_size", 13)
	add_child(label)
	
	var tween := create_tween()
	tween.tween_property(label, "position:y", label.position.y - 25.0, 0.4)
	tween.parallel().tween_property(label, "modulate:a", 0.0, 0.4)
	tween.tween_callback(label.queue_free)

func apply_slow(factor: float, source_id: String) -> void:
	_active_slows[source_id] = factor
	_recalculate_speed()

func remove_slow(source_id: String) -> void:
	if _active_slows.has(source_id):
		_active_slows.erase(source_id)
		_recalculate_speed()

func _recalculate_speed() -> void:
	if _active_slows.is_empty():
		current_speed = base_speed
		if sprite:
			sprite.modulate = Color.WHITE
		return
		
	var slowest := 1.0
	for f in _active_slows.values():
		if f < slowest:
			slowest = f
			
	current_speed = base_speed * slowest
	if sprite and flash_timer <= 0.0:
		sprite.modulate = Color(0.6, 0.8, 1.5, 1.0) # Tonalidade azulada indicando lentidão

func die() -> void:
	if is_dead:
		return
	is_dead = true
	
	# Concede recompensas
	ProgressionManager.add_coins(bounty_coins, true)
	ProgressionManager.update_quest_progress("quest_kill_50", 1)
	AudioManager.play_sfx("coin")
	
	enemy_died.emit(self)
	
	# Animação de dissolução / queda
	var tween := create_tween()
	tween.tween_property(self, "scale", Vector2(0.1, 0.1), 0.2)
	tween.parallel().tween_property(self, "modulate:a", 0.0, 0.2)
	tween.tween_callback(queue_free)

func _on_reach_base() -> void:
	if is_dead:
		return
	is_dead = true
	enemy_reached_base.emit(self, damage_to_base)
	queue_free()
