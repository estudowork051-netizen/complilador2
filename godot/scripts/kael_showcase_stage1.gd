class_name KaelShowcaseStage1
extends Node3D

## Heroes Tower Defense — Showcase de Referência Visual Premium (Etapa 1)
## Demonstra o padrão visual oficial estabelecido para o Kael:
## - Iluminação cinematográfica de 3 pontos (Key, Fill, Rim Light)
## - Plataforma de pedra entalhada com chanfro
## - Câmera isométrica canônica com rotação de inspeção (turntable)
## - Materiais PBR estilizados com emissão flamejante viva

@onready var hero_instance: Node3D = $HeroKael
@onready var camera_pivot: Node3D = $CameraPivot
@onready var key_light: DirectionalLight3D = $KeyLight
@onready var rim_light: OmniLight3D = $RimLight

@export var turntable_speed: float = 0.5
@export var auto_rotate: bool = true

var _anim_time: float = 0.0

func _ready() -> void:
	print("[Kael Showcase Stage 1] Cena de referência visual inicializada.")

func _process(delta: float) -> void:
	_anim_time += delta

	if auto_rotate and camera_pivot:
		camera_pivot.rotate_y(delta * turntable_speed)

	# Pulsação suave da luz de contorno flamejante (Rim Light)
	if rim_light:
		rim_light.light_energy = 1.8 + sin(_anim_time * 4.0) * 0.4
