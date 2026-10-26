class_name IsometricCoordinateConverter
extends RefCounted

## IsometricCoordinateConverter — Bridge between 2D Normalized Screen Space and Godot 3D World Space
## Maps canonical StageOneMap (0.0..1.0, 0.0..1.0) into 3D isometric battlefield coordinates (X, Y, Z).

# Battlefield 3D bounding dimensions
const BATTLEFIELD_WIDTH_3D: float = 14.0   # X axis (-7.0 to +7.0)
const BATTLEFIELD_LENGTH_3D: float = 20.0  # Z axis (-10.0 to +10.0)
const TERRAIN_BASE_ELEVATION: float = 0.0  # Y axis
const ROAD_ELEVATION: float = 0.04         # Slightly above terrain
const TOWER_BASE_ELEVATION: float = 0.0    # Ground mount

## Converts normalized 2D coordinate (nx in 0..1, ny in 0..1) to 3D world position (X, Y, Z)
static func normalized_to_world_3d(norm_pos: Vector2, elevation: float = 0.0) -> Vector3:
	var world_x: float = (norm_pos.x - 0.5) * BATTLEFIELD_WIDTH_3D
	var world_z: float = (norm_pos.y - 0.5) * BATTLEFIELD_LENGTH_3D
	return Vector3(world_x, elevation, world_z)

## Converts 3D world position (X, Y, Z) back to normalized 2D screen coordinate (nx, ny)
static func world_3d_to_normalized(world_pos: Vector3) -> Vector2:
	var nx: float = (world_pos.x / BATTLEFIELD_WIDTH_3D) + 0.5
	var ny: float = (world_pos.z / BATTLEFIELD_LENGTH_3D) + 0.5
	return Vector2(clampf(nx, 0.0, 1.0), clampf(ny, 0.0, 1.0))

## Returns the 8 canonical 3D waypoints matching StageOneMap.waypoints
static func get_stage1_waypoints_3d() -> Array[Vector3]:
	var norm_waypoints: Array[Vector2] = [
		Vector2(0.18, 0.06), # 0: Spawn Portal (Northwest)
		Vector2(0.18, 0.22), # 1: Upper forest clearing
		Vector2(0.82, 0.22), # 2: East across river bridge
		Vector2(0.82, 0.48), # 3: Eastern cliffs descent
		Vector2(0.20, 0.48), # 4: Rocky canyon bend
		Vector2(0.20, 0.74), # 5: Castle approach
		Vector2(0.55, 0.74), # 6: Fortified gates turn
		Vector2(0.55, 0.90)  # 7: Castle Base Destination (South)
	]
	var waypoints_3d: Array[Vector3] = []
	for p in norm_waypoints:
		waypoints_3d.append(normalized_to_world_3d(p, ROAD_ELEVATION))
	return waypoints_3d

## Returns the 8 canonical 3D tactical tower spots matching StageOneMap.defaultTowerSpots
static func get_stage1_tower_spots_3d() -> Array[Dictionary]:
	var raw_spots = [
		{"id": "spot_1", "nx": 0.50, "ny": 0.11, "name": "North Clearing"},
		{"id": "spot_2", "nx": 0.32, "ny": 0.35, "name": "Center-West Clearing"},
		{"id": "spot_3", "nx": 0.50, "ny": 0.35, "name": "Center Meadow"},
		{"id": "spot_4", "nx": 0.68, "ny": 0.35, "name": "Center-East Clearing"},
		{"id": "spot_5", "nx": 0.50, "ny": 0.61, "name": "Lower-Center Meadow"},
		{"id": "spot_6", "nx": 0.75, "ny": 0.61, "name": "Lower-East Ridge"},
		{"id": "spot_7", "nx": 0.35, "ny": 0.85, "name": "Castle Southwest"},
		{"id": "spot_8", "nx": 0.75, "ny": 0.85, "name": "Castle Southeast"}
	]
	var spots_3d: Array[Dictionary] = []
	for s in raw_spots:
		var pos_3d = normalized_to_world_3d(Vector2(s["nx"], s["ny"]), TOWER_BASE_ELEVATION)
		spots_3d.append({
			"id": s["id"],
			"name": s["name"],
			"norm_pos": Vector2(s["nx"], s["ny"]),
			"position_3d": pos_3d
		})
	return spots_3d
