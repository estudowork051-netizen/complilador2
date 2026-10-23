extends Node

## Gerenciador Central de Estado do Jogo (Singleton Autoload)
## Controla transições de tela e estados: Menu, Preparação, Combate, Pausa, Vitória, Derrota.

enum State {
	MENU,
	PREPARATION,
	COMBAT,
	PAUSED,
	VICTORY,
	DEFEAT
}

signal state_changed(old_state: State, new_state: State)
signal game_paused(is_paused: bool)

var current_state: State = State.MENU
var previous_state: State = State.MENU
var current_stage_id: int = 1
var is_game_paused: bool = false
var game_speed: float = 1.0

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS

func change_state(new_state: State) -> void:
	if current_state == new_state:
		return
	
	previous_state = current_state
	current_state = new_state
	
	# Gerenciamento de pausa conforme o estado
	if new_state == State.PAUSED:
		set_paused(true)
	elif previous_state == State.PAUSED and new_state != State.PAUSED:
		set_paused(false)
		
	state_changed.emit(previous_state, current_state)

func set_paused(paused: bool) -> void:
	is_game_paused = paused
	get_tree().paused = paused
	game_paused.emit(paused)

func toggle_pause() -> void:
	if current_state == State.COMBAT:
		change_state(State.PAUSED)
	elif current_state == State.PAUSED:
		change_state(State.COMBAT)

func set_game_speed(speed: float) -> void:
	game_speed = clampf(speed, 0.5, 3.0)
	Engine.time_scale = game_speed

func get_current_state() -> State:
	return current_state

func get_state_name(state_val: State) -> String:
	match state_val:
		State.MENU: return "MENU"
		State.PREPARATION: return "PREPARATION"
		State.COMBAT: return "COMBAT"
		State.PAUSED: return "PAUSED"
		State.VICTORY: return "VICTORY"
		State.DEFEAT: return "DEFEAT"
	return "UNKNOWN"

# Transições de Cenas
func go_to_main_menu() -> void:
	set_game_speed(1.0)
	set_paused(false)
	change_state(State.MENU)
	get_tree().change_scene_to_file("res://scenes/menus/main_menu.tscn")

func start_combat(stage_id: int = 1) -> void:
	current_stage_id = stage_id
	set_game_speed(1.0)
	set_paused(false)
	change_state(State.COMBAT)
	get_tree().change_scene_to_file("res://scenes/combat/combat_stage.tscn")
