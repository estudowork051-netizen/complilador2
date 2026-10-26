class_name LivingEnvironment
extends Node3D

## LivingEnvironment — Controller for Organic Living Environmental Animations
## Handles wind-swayed trees and foliage, flickering torches and braziers,
## hazard surface wave ripples, and atmospheric particle emissions.

@export var wind_strength: float = 0.08
@export var wind_speed: float = 2.4
@export var enable_particles: bool = true

var _trees: Array[Node3D] = []
var _torches: Array[Dictionary] = []
var _hazards: Array[MeshInstance3D] = []
var _particle_systems: Array[CPUParticles3D] = []

var _time_elapsed: float = 0.0

func _ready() -> void:
	_scan_environmental_nodes()

func _scan_environmental_nodes() -> void:
	_trees.clear()
	_torches.clear()
	_hazards.clear()
	_particle_systems.clear()
	
	for child in find_children("*", "Node3D", true, false):
		var node_name = child.name.to_lower()
		
		# Trees & Foliage
		if "tree" in node_name or "foliage" in node_name or "pillar" in node_name:
			if child is Node3D and not (child is Light3D):
				_trees.append(child)
		
		# Torches & Braziers with lights
		if "torch" in node_name or "brazier" in node_name or "beacon" in node_name:
			var light: OmniLight3D = null
			for sub in child.find_children("*", "OmniLight3D", true, false):
				light = sub as OmniLight3D
				break
			if light:
				_torches.append({
					"node": child,
					"light": light,
					"base_energy": light.light_energy,
					"base_color": light.light_color,
					"seed": randf() * 100.0
				})
		
		# Hazard surfaces (river, lava, void abyss)
		if "hazard" in node_name or "river" in node_name or "lava" in node_name or "water" in node_name:
			if child is MeshInstance3D:
				_hazards.append(child)
		
		# Ambient particles
		if child is CPUParticles3D:
			_particle_systems.append(child)

func _process(delta: float) -> void:
	_time_elapsed += delta
	_update_tree_sway()
	_update_torch_flicker()
	_update_hazard_waves()

## Harmonic wind sway for trees and foliage
func _update_tree_sway() -> void:
	for i in range(_trees.size()):
		var tree = _trees[i]
		if not is_instance_valid(tree):
			continue
		var phase = _time_elapsed * wind_speed + float(i) * 0.73
		var sway_x = sin(phase) * wind_strength
		var sway_z = cos(phase * 0.8) * wind_strength * 0.5
		tree.rotation.x = sway_x
		tree.rotation.z = sway_z

## Flickering flames and pulsing light for torches and braziers
func _update_torch_flicker() -> void:
	for t in _torches:
		var light = t["light"] as OmniLight3D
		if not is_instance_valid(light):
			continue
		var s = t["seed"]
		var flicker = sin((_time_elapsed * 12.0) + s) * 0.15 + cos((_time_elapsed * 23.0) + s) * 0.08
		light.light_energy = clampf(t["base_energy"] + flicker, 0.5, 5.0)

## Ripple and wave oscillation for rivers and hazard liquids
func _update_hazard_waves() -> void:
	for h in _hazards:
		if not is_instance_valid(h):
			continue
		var wave = sin(_time_elapsed * 1.8) * 0.015
		h.position.y = 0.02 + wave

## Updates ambient particle emission rate based on graphic quality
func set_particle_quality(tier: int) -> void:
	# tier: 0 = Low (off), 1 = Medium (half), 2 = High (full)
	for p in _particle_systems:
		if not is_instance_valid(p):
			continue
		if tier == 0:
			p.emitting = false
		elif tier == 1:
			p.emitting = true
			p.amount = 16
		else:
			p.emitting = true
			p.amount = 32
