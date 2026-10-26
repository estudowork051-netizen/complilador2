class_name BattlefieldPrototype
extends Node3D

## Heroes Tower Defense — Godot 4 Extreme 3D Isometric Battlefield (Etapa 1/3)
## Orchestrates 3D isometric environment, canonical isometric camera with orthographic/perspective toggle,
## dynamic 6-biome loader with atmospheric lighting, 8 tactical tower spots, path navigation,
## holographic placement preview, and combat validation.

const EnvironmentThemeConfig = preload("res://scripts/environment_theme.gd")
const IsometricCoordinateConverter = preload("res://scripts/coordinate_converter.gd")

@export var current_stage: int = 1
@export var goblin_scene: PackedScene
@export var spawn_interval: float = 3.2
@export var max_enemies_in_wave: int = 6

@onready var camera: Camera3D = $Camera3D
@onready var world_env: WorldEnvironment = $WorldEnvironment
@onready var sun_light: DirectionalLight3D = $DirectionalLight3D
@onready var path_3d: Path3D = $Path3D
@onready var biome_container: Node3D = $BiomeContainer
@onready var status_label: Label = $UI/StatusLabel
@onready var placement_preview: Node3D = $PlacementPreview
@onready var preview_disc: MeshInstance3D = $PlacementPreview/RangeDisc

var _active_biome_node: Node3D = null
var _spawn_timer: float = 0.0
var _enemies_spawned: int = 0
var _enemies_defeated: int = 0
var _active_enemies: Array[Node3D] = []

# Graphic Quality: 0 = Low, 1 = Medium, 2 = High (Default)
var _graphic_quality: int = 2
var _is_orthographic: bool = false

# Preloaded biome scenes
var _biome_scenes: Dictionary = {}

func _ready() -> void:
	print("[Godot 3D] Initializing 3D Isometric Battlefield — Etapa 1/3")
	_preload_biome_scenes()
	_setup_canonical_path()
	_apply_biome(current_stage)
	_setup_camera()
	_update_ui()

func _preload_biome_scenes() -> void:
	var biome_paths = {
		1: "res://scenes/biomes/highland_forest.tscn",
		2: "res://scenes/biomes/whispering_woods.tscn",
		3: "res://scenes/biomes/ancient_ruins.tscn",
		4: "res://scenes/biomes/crystal_caverns.tscn",
		5: "res://scenes/biomes/volcanic_fortress.tscn",
		6: "res://scenes/biomes/astral_peak.tscn"
	}
	for stage_num in biome_paths:
		var scene_res = load(biome_paths[stage_num])
		if scene_res:
			_biome_scenes[stage_num] = scene_res

## Sets up the 8 canonical 3D waypoints in Path3D matching StageOneMap.waypoints
func _setup_canonical_path() -> void:
	var waypoints = IsometricCoordinateConverter.get_stage1_waypoints_3d()
	var curve = Curve3D.new()
	for pt in waypoints:
		curve.add_point(pt)
	path_3d.curve = curve
	print("[Godot 3D] Configured canonical StageOneMap Path3D with %d waypoints." % waypoints.size())

func _setup_camera() -> void:
	if camera.has_method("apply_camera_mode"):
		camera.apply_camera_mode(_is_orthographic)

## Dynamically swaps the active 3D biome environment and updates atmospheric lighting
func set_biome(stage_number: int) -> void:
	current_stage = clampi(stage_number, 1, 6)
	_apply_biome(current_stage)
	_update_ui()

func _apply_biome(stage_number: int) -> void:
	var data = EnvironmentThemeConfig.get_biome_data(stage_number)
	
	# 1. Update Sky & Environment
	if world_env and world_env.environment:
		var env = world_env.environment
		if env.sky and env.sky.sky_material is ProceduralSkyMaterial:
			var sky_mat = env.sky.sky_material as ProceduralSkyMaterial
			sky_mat.sky_top_color = data["sky_top"]
			sky_mat.sky_horizon_color = data["sky_horizon"]
		env.ambient_light_color = data["ambient_light"]
	
	# 2. Update Directional Sun Light
	if sun_light:
		sun_light.light_color = data["sun_color"]
		sun_light.light_energy = data["sun_energy"]
	
	# 3. Swap 3D Biome Scene
	if _active_biome_node != null:
		_active_biome_node.queue_free()
		_active_biome_node = null
	
	if _biome_scenes.has(stage_number):
		var packed_scene: PackedScene = _biome_scenes[stage_number]
		_active_biome_node = packed_scene.instantiate()
		biome_container.add_child(_active_biome_node)
		if _active_biome_node.has_method("set_particle_quality"):
			_active_biome_node.set_particle_quality(_graphic_quality)
	
	print("[Godot 3D] Biome switched to Stage %d: %s (%s)" % [stage_number, data["name"], data["subtitle"]])

func _process(delta: float) -> void:
	_handle_spawning(delta)
	_cleanup_enemies()

func _handle_spawning(delta: float) -> void:
	if _enemies_spawned >= max_enemies_in_wave:
		return
	
	_spawn_timer += delta
	if _spawn_timer >= spawn_interval:
		_spawn_timer = 0.0
		_spawn_goblin()

func _spawn_goblin() -> void:
	if goblin_scene == null:
		var loaded: PackedScene = load("res://scenes/enemy_goblin.tscn")
		if loaded:
			goblin_scene = loaded
	
	if goblin_scene == null:
		return
	
	var goblin = goblin_scene.instantiate()
	path_3d.add_child(goblin)
	_active_enemies.append(goblin)
	_enemies_spawned += 1
	
	if goblin.has_signal("defeated"):
		goblin.connect("defeated", Callable(self, "_on_enemy_defeated"))
	
	_update_ui()

func _on_enemy_defeated(_enemy_node: Node3D) -> void:
	_enemies_defeated += 1
	trigger_screen_shake(0.20, 0.25)
	_update_ui()

func trigger_screen_shake(intensity: float = 0.35, duration: float = 0.30) -> void:
	if camera.has_method("trigger_shake"):
		camera.trigger_shake(intensity, duration)

func toggle_camera_projection() -> void:
	_is_orthographic = !_is_orthographic
	if camera.has_method("apply_camera_mode"):
		camera.apply_camera_mode(_is_orthographic)
	_update_ui()

func set_graphic_quality(quality_tier: int) -> void:
	_graphic_quality = clampi(quality_tier, 0, 2)
	if _active_biome_node and _active_biome_node.has_method("set_particle_quality"):
		_active_biome_node.set_particle_quality(_graphic_quality)
	_update_ui()

func _cleanup_enemies() -> void:
	_active_enemies = _active_enemies.filter(func(e): return is_instance_valid(e))

func get_active_enemies() -> Array[Node3D]:
	return _active_enemies

## Spawns any of the 6 canonical 3D articulated heroes into the battlefield
func spawn_hero(hero_id: String, world_pos: Vector3) -> Node3D:
	var scene_path = "res://scenes/heroes/%s.tscn" % hero_id
	var scene = load(scene_path) as PackedScene
	if scene:
		var hero = scene.instantiate() as Node3D
		add_child(hero)
		hero.global_position = world_pos
		return hero
	return null

## Spawns any of the 4 common articulated enemies along Path3D
func spawn_enemy_type(enemy_type: String) -> Node3D:
	var scene_path = "res://scenes/enemies/enemy_%s.tscn" % enemy_type
	var scene = load(scene_path) as PackedScene
	if scene:
		var enemy = scene.instantiate() as Node3D
		path_3d.add_child(enemy)
		_active_enemies.append(enemy)
		if enemy.has_signal("defeated"):
			enemy.connect("defeated", Callable(self, "_on_enemy_defeated"))
		return enemy
	return null

## Spawns any of the 3 epic articulated bosses along Path3D
func spawn_boss(boss_type: String) -> Node3D:
	var scene_path = "res://scenes/bosses/boss_%s.tscn" % boss_type
	var scene = load(scene_path) as PackedScene
	if scene:
		var boss = scene.instantiate() as Node3D
		path_3d.add_child(boss)
		_active_enemies.append(boss)
		if boss.has_signal("boss_defeated"):
			boss.connect("boss_defeated", Callable(self, "_on_enemy_defeated"))
		return boss
	return null

## Displays holographic placement preview at tactical spot
func show_placement_preview(world_pos: Vector3, is_valid: bool = true, range_radius: float = 4.5) -> void:
	if placement_preview:
		placement_preview.visible = true
		placement_preview.global_position = world_pos
		if preview_disc and preview_disc.material_override is StandardMaterial3D:
			var mat = preview_disc.material_override as StandardMaterial3D
			if is_valid:
				mat.albedo_color = Color(0.1, 0.8, 1.0, 0.35)
				mat.emission = Color(0.1, 0.8, 1.0, 1.0)
			else:
				mat.albedo_color = Color(1.0, 0.2, 0.2, 0.35)
				mat.emission = Color(1.0, 0.2, 0.2, 1.0)
		preview_disc.scale = Vector3(range_radius, 1.0, range_radius)

func hide_placement_preview() -> void:
	if placement_preview:
		placement_preview.visible = false

func _update_ui() -> void:
	if status_label:
		var data = EnvironmentThemeConfig.get_biome_data(current_stage)
		var proj_str = "Ortográfica (2.5D Clássico)" if _is_orthographic else "Perspectiva Isométrica (38° FOV)"
		var qual_str = ["LOW (Econômico)", "MEDIUM (Equilibrado)", "HIGH (Máximo 60FPS)"][_graphic_quality]
		status_label.text = "HEROES TOWER DEFENSE — ETAPA 1/3: CENÁRIOS 3D & CÂMERA ISOMÉTRICA\n" + \
			"Bioma Atual [%d/6]: %s — %s\n" % [current_stage, data["name"], data["subtitle"]] + \
			"Câmera: %s (Pitch: -35.26°, Yaw: 45°) | Qualidade: %s\n" % [proj_str, qual_str] + \
			"Inimigos Gerados: %d/%d | Derrotados: %d | Torres Ativas: 3 (Arqueiro, Mago, Canhão)" % \
			[_enemies_spawned, max_enemies_in_wave, _enemies_defeated]
