class_name IsometricCamera
extends Camera3D

## IsometricCamera — True 3D Isometric Mobile Strategy Camera
## Configures canonical isometric viewing angle (pitch: -35.264°, yaw: 45°),
## supports Orthogonal (classic 2.5D) and Perspective projection, and harmonic screen shake.

@export var is_orthogonal: bool = false
@export var ortho_size: float = 22.0
@export var perspective_fov: float = 38.0
@export var camera_distance: float = 24.0

# Canonical isometric rotation angles
# Pitch: -35.264° = -atan(1/sqrt(2)) radians = -0.6154797 rad
# Yaw:   45.0°    = pi/4 radians             = 0.785398 rad
const ISOMETRIC_PITCH_DEG: float = -35.264
const ISOMETRIC_YAW_DEG: float = 45.0

var _base_position: Vector3
var _shake_intensity: float = 0.0
var _shake_timer: float = 0.0
var _shake_duration: float = 0.0

func _ready() -> void:
	apply_camera_mode(is_orthogonal)
	reset_isometric_transform()

## Applies canonical isometric angles and positions the camera
func reset_isometric_transform() -> void:
	var pitch_rad: float = deg_to_rad(ISOMETRIC_PITCH_DEG)
	var yaw_rad: float = deg_to_rad(ISOMETRIC_YAW_DEG)
	
	# Rotation: Yaw around Y, then Pitch around X
	var rot: Basis = Basis.from_euler(Vector3(pitch_rad, yaw_rad, 0.0), EULER_ORDER_YXZ)
	
	# Position offset along camera backward vector (+Z in local camera space)
	var cam_offset: Vector3 = rot * Vector3(0.0, 0.0, camera_distance)
	# Center view slightly forward to frame vertical mobile battlefield (center around Z = 0)
	_base_position = Vector3(0.0, 0.0, 0.0) + cam_offset
	
	global_transform = Transform3D(rot, _base_position)

## Toggles between Orthographic Isometric (classic 2.5D) and Perspective Isometric
func apply_camera_mode(use_ortho: bool) -> void:
	is_orthogonal = use_ortho
	if is_orthogonal:
		projection = Camera3D.PROJECTION_ORTHOGONAL
		size = ortho_size
	else:
		projection = Camera3D.PROJECTION_PERSPECTIVE
		fov = perspective_fov
	near = 0.5
	far = 120.0

func _process(delta: float) -> void:
	_update_screen_shake(delta)

func _update_screen_shake(delta: float) -> void:
	if _shake_timer > 0.0:
		_shake_timer -= delta
		var damping: float = clampf(_shake_timer / _shake_duration, 0.0, 1.0)
		var t: float = Time.get_ticks_msec() * 0.035
		var offset_x: float = sin(t * 1.3) * _shake_intensity * damping
		var offset_y: float = cos(t * 1.7) * _shake_intensity * damping
		var offset_z: float = sin(t * 2.1) * _shake_intensity * damping * 0.5
		global_position = _base_position + Vector3(offset_x, offset_y, offset_z)
	else:
		global_position = _base_position

## Triggers camera shake with harmonic decay
func trigger_shake(intensity: float = 0.35, duration: float = 0.30) -> void:
	_shake_intensity = intensity
	_shake_duration = maxf(duration, 0.05)
	_shake_timer = _shake_duration
