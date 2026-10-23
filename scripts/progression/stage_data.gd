class_name StageData
extends RefCounted

## Modelo de Dados para Fases da Campanha (Fases 1 a 10)

var stage_id: int = 1
var title: String = ""
var description: String = ""
var required_stage_id: int = 0 # 0 significa sem pré-requisito
var reward_coins: int = 150
var reward_exp: int = 60
var bonus_reward_coins: int = 50 # Recompensa por terminar com vida da base >= 80%
var scenario_theme: String = "forest" # forest, ruins, volcano, fortress, temple
var scenario_name: String = "Floresta Encantada"

# Array de Ondas: cada onda é um Array de dicionários {"type": String, "delay": float}
var waves: Array[Array] = []

static func create_stage(id: int) -> StageData:
	var s := StageData.new()
	s.stage_id = id
	s.required_stage_id = id - 1 if id > 1 else 0
	
	if id <= 2:
		s.scenario_theme = "forest"
		s.scenario_name = "Floresta Encantada"
	elif id <= 4:
		s.scenario_theme = "ruins"
		s.scenario_name = "Ruínas Antigas"
	elif id <= 6:
		s.scenario_theme = "volcano"
		s.scenario_name = "Vale Vulcânico"
	elif id <= 8:
		s.scenario_theme = "fortress"
		s.scenario_name = "Fortaleza Sombria"
	else:
		s.scenario_theme = "temple"
		s.scenario_name = "Templo Arcano"
	
	match id:
		1:
			s.title = "Fase 1 — Encruzilhada da Coragem"
			s.description = "Entrada da cordilheira protegida. Batedores goblins avançam em patrulhas leves com guerreiros orcs."
			s.reward_coins = 140
			s.reward_exp = 50
			s.bonus_reward_coins = 40
			s.waves = [
				[{"type": "goblin", "delay": 1.4}, {"type": "goblin", "delay": 1.4}, {"type": "goblin", "delay": 1.4}],
				[{"type": "goblin", "delay": 1.2}, {"type": "goblin", "delay": 1.2}, {"type": "orc", "delay": 1.8}],
				[{"type": "goblin", "delay": 1.0}, {"type": "orc", "delay": 1.5}, {"type": "goblin", "delay": 1.0}, {"type": "orc", "delay": 1.5}],
				[{"type": "goblin", "delay": 0.9}, {"type": "goblin", "delay": 0.9}, {"type": "orc", "delay": 1.4}, {"type": "orc", "delay": 1.4}],
				[{"type": "goblin", "delay": 0.8}, {"type": "orc", "delay": 1.2}, {"type": "orc", "delay": 1.2}, {"type": "orc", "delay": 1.5}]
			]
		2:
			s.title = "Fase 2 — Trilha dos Espinheiros"
			s.description = "Vegetação densa onde as primeiras gárgulas aladas surgem sobrevoando os espinheiros."
			s.reward_coins = 180
			s.reward_exp = 70
			s.bonus_reward_coins = 50
			s.waves = [
				[{"type": "goblin", "delay": 1.2}, {"type": "flying", "delay": 1.5}, {"type": "goblin", "delay": 1.2}],
				[{"type": "flying", "delay": 1.4}, {"type": "orc", "delay": 1.6}, {"type": "flying", "delay": 1.4}],
				[{"type": "goblin", "delay": 1.0}, {"type": "goblin", "delay": 1.0}, {"type": "flying", "delay": 1.2}, {"type": "orc", "delay": 1.5}],
				[{"type": "flying", "delay": 1.1}, {"type": "flying", "delay": 1.1}, {"type": "orc", "delay": 1.3}, {"type": "orc", "delay": 1.3}],
				[{"type": "goblin", "delay": 0.8}, {"type": "flying", "delay": 1.0}, {"type": "orc", "delay": 1.2}, {"type": "flying", "delay": 1.0}, {"type": "orc", "delay": 1.2}]
			]
		3:
			s.title = "Fase 3 — Passo Rochoso"
			s.description = "Garganta estreita onde os primeiros Brutos Blindados colocam à prova o poder de corte dos guardiões."
			s.reward_coins = 230
			s.reward_exp = 90
			s.bonus_reward_coins = 65
			s.waves = [
				[{"type": "orc", "delay": 1.5}, {"type": "goblin", "delay": 1.0}, {"type": "orc", "delay": 1.5}],
				[{"type": "goblin", "delay": 1.0}, {"type": "brute", "delay": 2.2}, {"type": "goblin", "delay": 1.0}],
				[{"type": "orc", "delay": 1.3}, {"type": "flying", "delay": 1.2}, {"type": "brute", "delay": 2.0}],
				[{"type": "goblin", "delay": 0.8}, {"type": "goblin", "delay": 0.8}, {"type": "brute", "delay": 1.8}, {"type": "orc", "delay": 1.4}],
				[{"type": "flying", "delay": 1.0}, {"type": "brute", "delay": 1.8}, {"type": "flying", "delay": 1.0}, {"type": "brute", "delay": 1.8}],
				[{"type": "orc", "delay": 1.1}, {"type": "brute", "delay": 1.6}, {"type": "brute", "delay": 1.6}, {"type": "orc", "delay": 1.2}]
			]
		4:
			s.title = "Fase 4 — Pântano das Brumas"
			s.description = "Terreno alagado envolto em névoa negra, povoado por feiticeiros sombrios e gárgulas."
			s.reward_coins = 290
			s.reward_exp = 115
			s.bonus_reward_coins = 80
			s.waves = [
				[{"type": "goblin", "delay": 1.0}, {"type": "sorcerer", "delay": 1.8}, {"type": "goblin", "delay": 1.0}],
				[{"type": "flying", "delay": 1.2}, {"type": "sorcerer", "delay": 1.6}, {"type": "flying", "delay": 1.2}],
				[{"type": "orc", "delay": 1.2}, {"type": "sorcerer", "delay": 1.5}, {"type": "brute", "delay": 2.0}],
				[{"type": "flying", "delay": 1.0}, {"type": "sorcerer", "delay": 1.4}, {"type": "flying", "delay": 1.0}, {"type": "sorcerer", "delay": 1.4}],
				[{"type": "goblin", "delay": 0.8}, {"type": "brute", "delay": 1.8}, {"type": "sorcerer", "delay": 1.3}, {"type": "brute", "delay": 1.8}],
				[{"type": "flying", "delay": 0.9}, {"type": "orc", "delay": 1.1}, {"type": "sorcerer", "delay": 1.2}, {"type": "brute", "delay": 1.6}, {"type": "sorcerer", "delay": 1.2}]
			]
		5:
			s.title = "Fase 5 — Ruínas da Fortaleza Antiga"
			s.description = "Baluarte abandonado onde hordas organizadas tentam romper as fortificações em 7 ondas contínuas."
			s.reward_coins = 360
			s.reward_exp = 145
			s.bonus_reward_coins = 100
			s.waves = [
				[{"type": "goblin", "delay": 0.9}, {"type": "orc", "delay": 1.3}, {"type": "goblin", "delay": 0.9}],
				[{"type": "orc", "delay": 1.2}, {"type": "flying", "delay": 1.1}, {"type": "orc", "delay": 1.2}],
				[{"type": "brute", "delay": 2.0}, {"type": "sorcerer", "delay": 1.5}, {"type": "brute", "delay": 2.0}],
				[{"type": "flying", "delay": 0.9}, {"type": "flying", "delay": 0.9}, {"type": "orc", "delay": 1.1}, {"type": "sorcerer", "delay": 1.3}],
				[{"type": "brute", "delay": 1.7}, {"type": "brute", "delay": 1.7}, {"type": "flying", "delay": 0.9}, {"type": "flying", "delay": 0.9}],
				[{"type": "orc", "delay": 1.0}, {"type": "sorcerer", "delay": 1.2}, {"type": "brute", "delay": 1.6}, {"type": "sorcerer", "delay": 1.2}],
				[{"type": "brute", "delay": 1.5}, {"type": "flying", "delay": 0.8}, {"type": "brute", "delay": 1.5}, {"type": "sorcerer", "delay": 1.2}, {"type": "brute", "delay": 1.5}]
			]
		6:
			s.title = "Fase 6 — Desfiladeiro dos Ventos"
			s.description = "Ventos cortantes que impulsionam grandes revoadas de gárgulas em coordenação com brutos terrestres."
			s.reward_coins = 440
			s.reward_exp = 180
			s.bonus_reward_coins = 120
			s.waves = [
				[{"type": "flying", "delay": 0.9}, {"type": "flying", "delay": 0.9}, {"type": "flying", "delay": 0.9}],
				[{"type": "goblin", "delay": 0.8}, {"type": "brute", "delay": 1.8}, {"type": "flying", "delay": 0.9}],
				[{"type": "flying", "delay": 0.8}, {"type": "flying", "delay": 0.8}, {"type": "orc", "delay": 1.1}, {"type": "brute", "delay": 1.6}],
				[{"type": "sorcerer", "delay": 1.3}, {"type": "flying", "delay": 0.8}, {"type": "sorcerer", "delay": 1.3}, {"type": "brute", "delay": 1.5}],
				[{"type": "flying", "delay": 0.7}, {"type": "flying", "delay": 0.7}, {"type": "flying", "delay": 0.7}, {"type": "brute", "delay": 1.4}],
				[{"type": "brute", "delay": 1.4}, {"type": "sorcerer", "delay": 1.1}, {"type": "brute", "delay": 1.4}, {"type": "flying", "delay": 0.7}],
				[{"type": "flying", "delay": 0.6}, {"type": "flying", "delay": 0.6}, {"type": "brute", "delay": 1.3}, {"type": "sorcerer", "delay": 1.0}, {"type": "brute", "delay": 1.3}]
			]
		7:
			s.title = "Fase 7 — Minas Esquecidas"
			s.description = "Subterrâneo cavernoso onde guerreiros orcs e brutos avançam em 8 ondas massivas."
			s.reward_coins = 530
			s.reward_exp = 220
			s.bonus_reward_coins = 140
			s.waves = [
				[{"type": "orc", "delay": 1.0}, {"type": "orc", "delay": 1.0}, {"type": "brute", "delay": 1.8}],
				[{"type": "goblin", "delay": 0.7}, {"type": "goblin", "delay": 0.7}, {"type": "brute", "delay": 1.6}],
				[{"type": "flying", "delay": 0.8}, {"type": "sorcerer", "delay": 1.2}, {"type": "brute", "delay": 1.5}],
				[{"type": "orc", "delay": 0.9}, {"type": "orc", "delay": 0.9}, {"type": "sorcerer", "delay": 1.1}, {"type": "brute", "delay": 1.4}],
				[{"type": "brute", "delay": 1.4}, {"type": "brute", "delay": 1.4}, {"type": "flying", "delay": 0.7}, {"type": "flying", "delay": 0.7}],
				[{"type": "sorcerer", "delay": 1.0}, {"type": "orc", "delay": 0.8}, {"type": "sorcerer", "delay": 1.0}, {"type": "brute", "delay": 1.3}],
				[{"type": "flying", "delay": 0.6}, {"type": "brute", "delay": 1.2}, {"type": "flying", "delay": 0.6}, {"type": "brute", "delay": 1.2}],
				[{"type": "brute", "delay": 1.2}, {"type": "brute", "delay": 1.2}, {"type": "sorcerer", "delay": 0.9}, {"type": "brute", "delay": 1.2}, {"type": "orc", "delay": 0.8}]
			]
		8:
			s.title = "Fase 8 — Bosque das Sombras"
			s.description = "Floresta corrompida onde feiticeiros conjuram legiões ágeis apoiadas por brutos blindados."
			s.reward_coins = 640
			s.reward_exp = 270
			s.bonus_reward_coins = 170
			s.waves = [
				[{"type": "sorcerer", "delay": 1.2}, {"type": "flying", "delay": 0.7}, {"type": "sorcerer", "delay": 1.2}],
				[{"type": "orc", "delay": 0.9}, {"type": "brute", "delay": 1.5}, {"type": "flying", "delay": 0.7}],
				[{"type": "sorcerer", "delay": 1.0}, {"type": "sorcerer", "delay": 1.0}, {"type": "brute", "delay": 1.4}],
				[{"type": "flying", "delay": 0.6}, {"type": "flying", "delay": 0.6}, {"type": "orc", "delay": 0.8}, {"type": "brute", "delay": 1.3}],
				[{"type": "brute", "delay": 1.3}, {"type": "sorcerer", "delay": 0.9}, {"type": "brute", "delay": 1.3}, {"type": "flying", "delay": 0.6}],
				[{"type": "goblin", "delay": 0.5}, {"type": "goblin", "delay": 0.5}, {"type": "brute", "delay": 1.2}, {"type": "sorcerer", "delay": 0.9}],
				[{"type": "brute", "delay": 1.2}, {"type": "flying", "delay": 0.5}, {"type": "sorcerer", "delay": 0.8}, {"type": "brute", "delay": 1.2}],
				[{"type": "sorcerer", "delay": 0.8}, {"type": "brute", "delay": 1.1}, {"type": "brute", "delay": 1.1}, {"type": "sorcerer", "delay": 0.8}, {"type": "flying", "delay": 0.5}]
			]
		9:
			s.title = "Fase 9 — Portão da Cidadela Caída"
			s.description = "A antecâmara da fortaleza ancestral. Batalhões pesados marcham sem trégua em 9 ondas implacáveis."
			s.reward_coins = 770
			s.reward_exp = 330
			s.bonus_reward_coins = 200
			s.waves = [
				[{"type": "orc", "delay": 0.8}, {"type": "brute", "delay": 1.3}, {"type": "flying", "delay": 0.6}],
				[{"type": "flying", "delay": 0.6}, {"type": "flying", "delay": 0.6}, {"type": "brute", "delay": 1.3}],
				[{"type": "sorcerer", "delay": 0.9}, {"type": "sorcerer", "delay": 0.9}, {"type": "brute", "delay": 1.2}],
				[{"type": "brute", "delay": 1.2}, {"type": "brute", "delay": 1.2}, {"type": "flying", "delay": 0.5}],
				[{"type": "goblin", "delay": 0.5}, {"type": "orc", "delay": 0.7}, {"type": "brute", "delay": 1.1}, {"type": "sorcerer", "delay": 0.8}],
				[{"type": "flying", "delay": 0.5}, {"type": "sorcerer", "delay": 0.8}, {"type": "flying", "delay": 0.5}, {"type": "brute", "delay": 1.1}],
				[{"type": "brute", "delay": 1.1}, {"type": "brute", "delay": 1.1}, {"type": "sorcerer", "delay": 0.7}, {"type": "flying", "delay": 0.5}],
				[{"type": "sorcerer", "delay": 0.7}, {"type": "brute", "delay": 1.0}, {"type": "sorcerer", "delay": 0.7}, {"type": "brute", "delay": 1.0}],
				[{"type": "brute", "delay": 0.9}, {"type": "brute", "delay": 0.9}, {"type": "sorcerer", "delay": 0.6}, {"type": "flying", "delay": 0.4}, {"type": "brute", "delay": 0.9}]
			]
		10:
			s.title = "Fase 10 — Covil do Ogro Ancestral"
			s.description = "A batalha definitiva pela salvação do reino! 10 ondas ferozes que culminam na invasão do Ogro das Ruínas!"
			s.reward_coins = 1100
			s.reward_exp = 500
			s.bonus_reward_coins = 300
			s.waves = [
				[{"type": "orc", "delay": 0.8}, {"type": "orc", "delay": 0.8}, {"type": "brute", "delay": 1.3}],
				[{"type": "flying", "delay": 0.5}, {"type": "flying", "delay": 0.5}, {"type": "brute", "delay": 1.2}],
				[{"type": "sorcerer", "delay": 0.8}, {"type": "sorcerer", "delay": 0.8}, {"type": "flying", "delay": 0.5}],
				[{"type": "brute", "delay": 1.1}, {"type": "brute", "delay": 1.1}, {"type": "orc", "delay": 0.7}],
				[{"type": "flying", "delay": 0.5}, {"type": "flying", "delay": 0.5}, {"type": "sorcerer", "delay": 0.7}, {"type": "brute", "delay": 1.0}],
				[{"type": "brute", "delay": 1.0}, {"type": "sorcerer", "delay": 0.7}, {"type": "brute", "delay": 1.0}, {"type": "flying", "delay": 0.4}],
				[{"type": "flying", "delay": 0.4}, {"type": "flying", "delay": 0.4}, {"type": "brute", "delay": 0.9}, {"type": "sorcerer", "delay": 0.6}],
				[{"type": "brute", "delay": 0.9}, {"type": "brute", "delay": 0.9}, {"type": "brute", "delay": 0.9}, {"type": "sorcerer", "delay": 0.6}],
				[{"type": "sorcerer", "delay": 0.6}, {"type": "sorcerer", "delay": 0.6}, {"type": "brute", "delay": 0.8}, {"type": "flying", "delay": 0.4}, {"type": "brute", "delay": 0.8}],
				# ONDA 10 CLÍMAX: O CHEFE OGRO DAS RUÍNAS COM ESCOLTA DE ELITE!
				[{"type": "brute", "delay": 1.2}, {"type": "sorcerer", "delay": 1.0}, {"type": "boss_ogre", "delay": 3.0}, {"type": "flying", "delay": 0.8}, {"type": "brute", "delay": 1.2}]
			]
	return s
