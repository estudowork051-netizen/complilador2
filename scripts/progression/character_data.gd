class_name CharacterData
extends RefCounted

## Definição de atributos, fórmulas de evolução e habilidades de um personagem

var id: String = ""
var name: String = ""
var title: String = ""
var description: String = ""
var role: String = ""
var avatar_path: String = ""
var sprite_path: String = ""

# Atributos Base (Nível 1)
var base_hp: float = 100.0
var base_damage: float = 25.0
var base_attack_speed: float = 1.0 # Ataques por segundo
var base_range: float = 220.0
var base_skill_damage: float = 60.0
var base_cooldown_mod: float = 1.0

# Sistema de Raridade e Identidade
var rarity: String = "common" # "common", "rare", "epic", "legendary"
var rarity_name: String = "Comum"
var rarity_color: Color = Color(0.7, 0.75, 0.8)
var recruit_cost_coins: int = 0
var recruit_cost_gems: int = 0
var unlock_base_level: int = 1

# Habilidades (índice 0 = Skill 1, índice 1 = Skill 2)
var skills: Array[Dictionary] = []

# Custo de evolução por nível (índice 0 = lv 1->2, ..., índice 8 = lv 9->10)
const UPGRADE_COSTS: Array[int] = [
	80,   # Lv 1 -> 2
	160,  # Lv 2 -> 3
	280,  # Lv 3 -> 4
	450,  # Lv 4 -> 5
	680,  # Lv 5 -> 6
	980,  # Lv 6 -> 7
	1380, # Lv 7 -> 8
	1900, # Lv 8 -> 9
	2600  # Lv 9 -> 10
]

const MAX_LEVEL: int = 10

static func get_upgrade_cost(current_level: int) -> int:
	if current_level < 1 or current_level >= MAX_LEVEL:
		return 0
	return UPGRADE_COSTS[current_level - 1]

func calculate_damage(level: int, workshop_bonus_pct: float = 0.0) -> float:
	# +12% de dano por nível acima do nível 1
	var lvl_mult := 1.0 + (level - 1) * 0.12
	var workshop_mult := 1.0 + (workshop_bonus_pct / 100.0)
	return base_damage * lvl_mult * workshop_mult

func calculate_attack_speed(level: int, workshop_bonus_pct: float = 0.0) -> float:
	# +5% de velocidade de ataque a cada nível
	var lvl_mult := 1.0 + (level - 1) * 0.05
	var workshop_mult := 1.0 + ((workshop_bonus_pct * 0.5) / 100.0)
	return base_attack_speed * lvl_mult * workshop_mult

func calculate_range(level: int) -> float:
	# +3% de alcance a cada 2 níveis
	var lvl_bonus := floorf((level - 1) / 2.0) * 8.0
	return base_range + lvl_bonus

func calculate_skill_damage(level: int, academy_bonus_pct: float = 0.0) -> float:
	# +15% de dano de habilidade por nível
	var lvl_mult := 1.0 + (level - 1) * 0.15
	var academy_mult := 1.0 + (academy_bonus_pct / 100.0)
	return base_skill_damage * lvl_mult * academy_mult

func calculate_cooldown(base_cd: float, level: int, academy_cdr_pct: float = 0.0) -> float:
	# Redução de recarga: -2% por nível + bônus da Academia
	var lvl_cdr := (level - 1) * 0.02
	var total_cdr := clampf(lvl_cdr + (academy_cdr_pct / 100.0), 0.0, 0.50)
	return base_cd * (1.0 - total_cdr)

static func create_kael() -> CharacterData:
	var c := CharacterData.new()
	c.id = "kael"
	c.name = "Kael"
	c.title = "Guardião Arcano"
	c.role = "Dano Mágico & Controle"
	c.description = "Mago arcano treinado nas antigas bibliotecas da Cidadela. Seus feitiços concentram pura energia mística para aniquilar e desacelerar hordas invasoras."
	c.avatar_path = "res://assets/icons/kael_avatar.svg"
	c.sprite_path = "res://assets/sprites/kael_sprite.svg"
	c.rarity = "common"
	c.rarity_name = "Comum"
	c.rarity_color = Color(0.75, 0.8, 0.85)
	c.recruit_cost_coins = 0
	c.recruit_cost_gems = 0
	c.unlock_base_level = 1
	c.base_damage = 28.0
	c.base_attack_speed = 1.0 # 1 disparo por segundo
	c.base_range = 230.0
	c.base_skill_damage = 75.0
	
	c.skills = [
		{
			"id": "arcane_blast",
			"name": "Explosão Arcana",
			"icon": "res://assets/icons/skill_arcane_blast.svg",
			"description": "Lança uma esfera densa de energia arcana que detona na área de impacto, infligindo dano massivo em múltiplos inimigos.",
			"base_cooldown": 7.0,
			"radius": 90.0,
			"base_damage": 85.0
		},
		{
			"id": "slow_field",
			"name": "Campo de Lentidão",
			"icon": "res://assets/icons/skill_slow_field.svg",
			"description": "Conjura uma zona gravitacional mística no caminho dos monstros, reduzindo a velocidade dos inimigos em 50% por 6 segundos.",
			"base_cooldown": 12.0,
			"duration": 6.0,
			"radius": 110.0,
			"slow_factor": 0.50
		}
	]
	return c

static func create_lyra() -> CharacterData:
	var c := CharacterData.new()
	c.id = "lyra"
	c.name = "Lyra"
	c.title = "Arqueira das Sombras"
	c.role = "Ataque Rápido & Alvos Prioritários"
	c.description = "Rastreadora furtiva da Floresta Noturna. Seus disparos precisos eliminam ameaças distantes com cadência implacável e flechas perfurantes."
	c.avatar_path = "res://assets/icons/lyra_avatar.svg"
	c.sprite_path = "res://assets/sprites/lyra_sprite.svg"
	c.rarity = "rare"
	c.rarity_name = "Raro"
	c.rarity_color = Color(0.18, 0.62, 0.98)
	c.recruit_cost_coins = 400
	c.recruit_cost_gems = 15
	c.unlock_base_level = 3
	c.base_damage = 18.0
	c.base_attack_speed = 1.55 # Cadência rápida (1.55 disparos/seg)
	c.base_range = 270.0
	c.base_skill_damage = 50.0
	
	c.skills = [
		{
			"id": "arrow_rain",
			"name": "Chuva de Flechas",
			"icon": "res://assets/icons/skill_arrow_rain.svg",
			"description": "Dispara uma saraivada para o alto que cai como chuva mortífera na área selecionada, atingindo todos os alvos.",
			"base_cooldown": 9.0,
			"radius": 85.0,
			"base_damage": 60.0
		},
		{
			"id": "piercing_shot",
			"name": "Disparo Perfurante",
			"icon": "res://assets/icons/skill_piercing_shot.svg",
			"description": "Carrega uma flecha envolta em éter sombrio que atravessa todos os inimigos em linha reta, perfurando suas defesas.",
			"base_cooldown": 10.0,
			"range": 450.0,
			"base_damage": 95.0
		}
	]
	return c

static func create_thorne() -> CharacterData:
	var c := CharacterData.new()
	c.id = "thorne"
	c.name = "Thorne"
	c.title = "Guerreiro Protetor"
	c.role = "Dano Contundente & Atordoamento"
	c.description = "Veterano blindado que esmaga os inimigos que se aproximam da base com marteladas sísmicas e ergue domos protetores."
	c.avatar_path = "res://assets/icons/thorne_avatar.svg"
	c.sprite_path = "res://assets/sprites/thorne_sprite.svg"
	c.rarity = "rare"
	c.rarity_name = "Raro"
	c.rarity_color = Color(0.18, 0.8, 0.44)
	c.recruit_cost_coins = 600
	c.recruit_cost_gems = 20
	c.unlock_base_level = 5
	c.base_damage = 38.0
	c.base_attack_speed = 0.85
	c.base_range = 190.0
	c.base_skill_damage = 90.0
	c.skills = [
		{
			"id": "ground_slam",
			"name": "Golpe Sísmico",
			"icon": "res://assets/icons/skill_ground_slam.svg",
			"description": "Golpeia o chão causando tremor em área e atordoando invasores.",
			"base_cooldown": 8.0,
			"radius": 120.0,
			"base_damage": 90.0
		},
		{
			"id": "bastion_shield",
			"name": "Escudo Baluarte",
			"icon": "res://assets/icons/skill_bastion_shield.svg",
			"description": "Ergue barreiras que recuperam vida da base e protegem defensores.",
			"base_cooldown": 14.0,
			"duration": 6.0,
			"base_damage": 0.0
		}
	]
	return c

static func create_elysia() -> CharacterData:
	var c := CharacterData.new()
	c.id = "elysia"
	c.name = "Elysia"
	c.title = "Maga Elemental"
	c.role = "Dano Contínuo & Incêndio"
	c.description = "Manipuladora das chamas primordiais que incinera trilhas inteiras de invasores com meteoros e muralhas de fogo."
	c.avatar_path = "res://assets/icons/elysia_avatar.svg"
	c.sprite_path = "res://assets/sprites/elysia_sprite.svg"
	c.rarity = "epic"
	c.rarity_name = "Épico"
	c.rarity_color = Color(0.68, 0.38, 0.95)
	c.recruit_cost_coins = 1000
	c.recruit_cost_gems = 35
	c.unlock_base_level = 7
	c.base_damage = 24.0
	c.base_attack_speed = 1.15
	c.base_range = 240.0
	c.base_skill_damage = 80.0
	c.skills = [
		{"id": "meteor", "name": "Meteoro Incandescente", "icon": "res://assets/icons/skill_meteor.svg", "base_cooldown": 8.0, "radius": 110.0, "base_damage": 120.0},
		{"id": "flame_wall", "name": "Muralha de Chamas", "icon": "res://assets/icons/skill_flame_wall.svg", "base_cooldown": 13.0, "duration": 6.0, "base_damage": 35.0}
	]
	return c

static func create_boran() -> CharacterData:
	var c := CharacterData.new()
	c.id = "boran"
	c.name = "Boran"
	c.title = "Engenheiro de Armadilhas"
	c.role = "Armadilhas & Engenharia Tática"
	c.description = "Mestre mecânico que espalha minas explosivas e implanta torretas autônomas no campo de batalha."
	c.avatar_path = "res://assets/icons/boran_avatar.svg"
	c.sprite_path = "res://assets/sprites/boran_sprite.svg"
	c.rarity = "epic"
	c.rarity_name = "Épico"
	c.rarity_color = Color(0.95, 0.55, 0.15)
	c.recruit_cost_coins = 1200
	c.recruit_cost_gems = 40
	c.unlock_base_level = 9
	c.base_damage = 22.0
	c.base_attack_speed = 1.35
	c.base_range = 210.0
	c.base_skill_damage = 85.0
	c.skills = [
		{"id": "landmine", "name": "Mina de Proximidade", "icon": "res://assets/icons/skill_minefield.svg", "base_cooldown": 9.0, "radius": 90.0, "base_damage": 110.0},
		{"id": "turret", "name": "Torreta Auxiliar", "icon": "res://assets/icons/skill_turret.svg", "base_cooldown": 15.0, "duration": 8.0, "base_damage": 24.0}
	]
	return c

static func create_vesper() -> CharacterData:
	var c := CharacterData.new()
	c.id = "vesper"
	c.name = "Vesper"
	c.title = "Assassino do Crepúsculo"
	c.role = "Execução de Chefes & Críticos"
	c.description = "Assassino espectral das sombras ancestrais cujo golpe cirúrgico perfura armaduras pesadas e cega inimigos com névoa mística."
	c.avatar_path = "res://assets/icons/vesper_avatar.svg"
	c.sprite_path = "res://assets/sprites/vesper_sprite.svg"
	c.rarity = "legendary"
	c.rarity_name = "Lendário"
	c.rarity_color = Color(1.0, 0.82, 0.0)
	c.recruit_cost_coins = 2000
	c.recruit_cost_gems = 75
	c.unlock_base_level = 10
	c.base_damage = 48.0
	c.base_attack_speed = 0.7
	c.base_range = 350.0
	c.base_skill_damage = 160.0
	c.skills = [
		{"id": "snipe", "name": "Tiro Fatal", "icon": "res://assets/icons/skill_snipe.svg", "base_cooldown": 11.0, "range": 500.0, "base_damage": 180.0},
		{"id": "smoke_veil", "name": "Cortina de Fumaça", "icon": "res://assets/icons/skill_smoke_veil.svg", "base_cooldown": 14.0, "duration": 5.0, "base_damage": 0.0}
	]
	return c
