class_name EnvironmentThemeConfig
extends RefCounted

## EnvironmentThemeConfig — 3D Biome Configurations for Heroes Tower Defense
## Mirrors EnvironmentTheme.kt with 3D materials, lighting, sky, and atmospheric particles.

enum BiomeType {
	HIGHLAND_FOREST = 1,
	WHISPERING_WOODS = 2,
	ANCIENT_RUINS = 3,
	CRYSTAL_CAVERNS = 4,
	VOLCANIC_FORTRESS = 5,
	ASTRAL_PEAK = 6
}

static func get_biome_data(stage_number: int) -> Dictionary:
	match stage_number:
		1, BiomeType.HIGHLAND_FOREST:
			return {
				"stage": 1,
				"name": "O Desfiladeiro dos Goblins",
				"subtitle": "Entrada das Terras Altas",
				# Ground & Road Colors
				"ground_color": Color(0.106, 0.200, 0.125, 1.0), # #1B3320
				"ground_accent": Color(0.137, 0.267, 0.169, 1.0), # #23442B
				"road_color": Color(0.549, 0.431, 0.290, 1.0),   # #8C6E4A
				"road_border": Color(0.353, 0.267, 0.180, 1.0),  # #5A442E
				# Hazard / Liquid
				"hazard_color": Color(0.145, 0.388, 0.922, 0.8), # Crystal River #2563EB
				"hazard_glow": Color(0.376, 0.647, 0.980, 1.0),
				"hazard_emissive": false,
				# Props
				"foliage_color": Color(0.082, 0.502, 0.239, 1.0),# #15803D
				"foliage_sec": Color(0.086, 0.396, 0.204, 1.0),
				"trunk_wood": Color(0.471, 0.208, 0.059, 1.0),
				"rock_color": Color(0.278, 0.333, 0.412, 1.0),   # Slate granite
				# 3D Lighting & Sky
				"sky_top": Color(0.251, 0.451, 0.686, 1.0),
				"sky_horizon": Color(0.651, 0.753, 0.851, 1.0),
				"ambient_light": Color(0.35, 0.40, 0.48, 1.0),
				"sun_color": Color(1.0, 0.96, 0.90, 1.0),
				"sun_energy": 1.35,
				"fog_color": Color(0.65, 0.75, 0.85, 1.0),
				"fog_density": 0.005,
				# Torch / Brazier
				"torch_color": Color(0.984, 0.749, 0.141, 1.0),  # Warm amber torch
				"torch_energy": 2.2,
				# Atmospheric Particle
				"particle_color": Color(0.992, 0.902, 0.541, 0.8), # Golden pollen
				"particle_type": "POLLEN",
				"particle_speed": 0.6
			}
		2, BiomeType.WHISPERING_WOODS:
			return {
				"stage": 2,
				"name": "Floresta dos Sussurros",
				"subtitle": "Vegetação Densa e Emboscadas",
				"ground_color": Color(0.059, 0.161, 0.118, 1.0), # #0F291E
				"ground_accent": Color(0.086, 0.220, 0.169, 1.0),
				"road_color": Color(0.420, 0.357, 0.263, 1.0),   # #6B5B43
				"road_border": Color(0.290, 0.247, 0.180, 1.0),
				"hazard_color": Color(0.051, 0.580, 0.533, 0.8), # Misty Stream #0D9488
				"hazard_glow": Color(0.176, 0.831, 0.749, 1.0),
				"hazard_emissive": true,
				"foliage_color": Color(0.016, 0.471, 0.341, 1.0),# Emerald foliage
				"foliage_sec": Color(0.024, 0.373, 0.275, 1.0),
				"trunk_wood": Color(0.361, 0.239, 0.180, 1.0),
				"rock_color": Color(0.200, 0.255, 0.333, 1.0),
				"sky_top": Color(0.08, 0.22, 0.20, 1.0),
				"sky_horizon": Color(0.18, 0.45, 0.40, 1.0),
				"ambient_light": Color(0.18, 0.38, 0.32, 1.0),
				"sun_color": Color(0.85, 0.98, 0.92, 1.0),
				"sun_energy": 1.10,
				"fog_color": Color(0.15, 0.40, 0.35, 1.0),
				"fog_density": 0.012,
				"torch_color": Color(0.404, 0.910, 0.976, 1.0),  # Cyan spectral lantern
				"torch_energy": 2.5,
				"particle_color": Color(0.404, 0.910, 0.976, 0.9), # Cyan fireflies
				"particle_type": "FIREFLIES",
				"particle_speed": 0.9
			}
		3, BiomeType.ANCIENT_RUINS:
			return {
				"stage": 3,
				"name": "Ruínas Ancestrais",
				"subtitle": "Santuário Abandonado",
				"ground_color": Color(0.118, 0.141, 0.169, 1.0), # #1E242B Ancient granite
				"ground_accent": Color(0.157, 0.188, 0.227, 1.0),
				"road_color": Color(0.278, 0.333, 0.412, 1.0),   # #475569 Paved stone
				"road_border": Color(0.200, 0.255, 0.333, 1.0),
				"hazard_color": Color(0.298, 0.114, 0.584, 0.85),# Void energy pool #4C1D95
				"hazard_glow": Color(0.545, 0.361, 0.965, 1.0),
				"hazard_emissive": true,
				"foliage_color": Color(0.200, 0.255, 0.333, 1.0),# Weathered ruins stone
				"foliage_sec": Color(0.118, 0.161, 0.231, 1.0),
				"trunk_wood": Color(0.278, 0.333, 0.412, 1.0),
				"rock_color": Color(0.392, 0.455, 0.545, 1.0),
				"sky_top": Color(0.15, 0.12, 0.28, 1.0),
				"sky_horizon": Color(0.38, 0.25, 0.52, 1.0),
				"ambient_light": Color(0.28, 0.24, 0.38, 1.0),
				"sun_color": Color(0.92, 0.85, 1.0, 1.0),
				"sun_energy": 1.15,
				"fog_color": Color(0.28, 0.18, 0.42, 1.0),
				"fog_density": 0.009,
				"torch_color": Color(0.753, 0.518, 0.988, 1.0),  # Violet witchfire
				"torch_energy": 2.6,
				"particle_color": Color(0.753, 0.518, 0.988, 0.85), # Spectral mist
				"particle_type": "SPECTRAL_MIST",
				"particle_speed": 0.4
			}
		4, BiomeType.CRYSTAL_CAVERNS:
			return {
				"stage": 4,
				"name": "Cavernas de Cristal",
				"subtitle": "Subsolo Escuro",
				"ground_color": Color(0.059, 0.090, 0.165, 1.0), # #0F172A Subterranean basalt
				"ground_accent": Color(0.118, 0.161, 0.231, 1.0),
				"road_color": Color(0.200, 0.255, 0.333, 1.0),   # #334155
				"road_border": Color(0.118, 0.161, 0.231, 1.0),
				"hazard_color": Color(0.008, 0.518, 0.780, 0.9), # Subterranean pool #0284C7
				"hazard_glow": Color(0.220, 0.741, 0.973, 1.0),
				"hazard_emissive": true,
				"foliage_color": Color(0.008, 0.518, 0.780, 1.0),# Crystal clusters
				"foliage_sec": Color(0.012, 0.412, 0.631, 1.0),
				"trunk_wood": Color(0.118, 0.161, 0.231, 1.0),
				"rock_color": Color(0.118, 0.161, 0.231, 1.0),
				"sky_top": Color(0.03, 0.05, 0.12, 1.0),
				"sky_horizon": Color(0.06, 0.14, 0.28, 1.0),
				"ambient_light": Color(0.15, 0.22, 0.35, 1.0),
				"sun_color": Color(0.65, 0.88, 1.0, 1.0),
				"sun_energy": 0.95,
				"fog_color": Color(0.05, 0.12, 0.25, 1.0),
				"fog_density": 0.015,
				"torch_color": Color(0.220, 0.741, 0.973, 1.0),  # Arcane crystal pulse
				"torch_energy": 2.8,
				"particle_color": Color(0.220, 0.741, 0.973, 0.9), # Crystal dust
				"particle_type": "CRYSTAL_DUST",
				"particle_speed": 0.5
			}
		5, BiomeType.VOLCANIC_FORTRESS:
			return {
				"stage": 5,
				"name": "Fortaleza Vulcânica",
				"subtitle": "Muralhas da Caldeira",
				"ground_color": Color(0.110, 0.063, 0.059, 1.0), # #1C100F Scorched obsidian
				"ground_accent": Color(0.165, 0.086, 0.078, 1.0),
				"road_color": Color(0.247, 0.149, 0.137, 1.0),   # #3F2623 Iron slab road
				"road_border": Color(0.165, 0.086, 0.078, 1.0),
				"hazard_color": Color(0.863, 0.149, 0.149, 1.0), # Molten lava river #DC2626
				"hazard_glow": Color(0.976, 0.451, 0.086, 1.0),
				"hazard_emissive": true,
				"foliage_color": Color(0.271, 0.102, 0.012, 1.0),# Charred deadwood
				"foliage_sec": Color(0.153, 0.059, 0.016, 1.0),
				"trunk_wood": Color(0.110, 0.098, 0.090, 1.0),
				"rock_color": Color(0.161, 0.145, 0.141, 1.0),
				"sky_top": Color(0.18, 0.05, 0.03, 1.0),
				"sky_horizon": Color(0.42, 0.15, 0.06, 1.0),
				"ambient_light": Color(0.35, 0.18, 0.12, 1.0),
				"sun_color": Color(1.0, 0.75, 0.55, 1.0),
				"sun_energy": 1.25,
				"fog_color": Color(0.35, 0.12, 0.05, 1.0),
				"fog_density": 0.011,
				"torch_color": Color(0.976, 0.451, 0.086, 1.0),  # Blazing magma brazier
				"torch_energy": 3.2,
				"particle_color": Color(0.976, 0.451, 0.086, 0.95),# Volcanic embers
				"particle_type": "VOLCANIC_EMBERS",
				"particle_speed": 1.2
			}
		6, BiomeType.ASTRAL_PEAK:
			return {
				"stage": 6,
				"name": "Pico do Dragão",
				"subtitle": "Dimensão Astral & Covil do Tirano",
				"ground_color": Color(0.043, 0.059, 0.149, 1.0), # #0B0F26 Cosmic stardust
				"ground_accent": Color(0.075, 0.102, 0.251, 1.0),
				"road_color": Color(0.118, 0.106, 0.294, 1.0),   # #1E1B4B Constellation lane
				"road_border": Color(0.090, 0.075, 0.231, 1.0),
				"hazard_color": Color(0.486, 0.227, 0.929, 0.9), # Nebula abyss #7C3AED
				"hazard_glow": Color(0.753, 0.518, 0.988, 1.0),
				"hazard_emissive": true,
				"foliage_color": Color(0.220, 0.741, 0.973, 1.0),# Celestial crystals
				"foliage_sec": Color(0.118, 0.251, 0.686, 1.0),
				"trunk_wood": Color(0.118, 0.106, 0.294, 1.0),
				"rock_color": Color(0.106, 0.133, 0.212, 1.0),
				"sky_top": Color(0.04, 0.03, 0.15, 1.0),
				"sky_horizon": Color(0.22, 0.12, 0.38, 1.0),
				"ambient_light": Color(0.24, 0.20, 0.42, 1.0),
				"sun_color": Color(0.95, 0.75, 1.0, 1.0),
				"sun_energy": 1.20,
				"fog_color": Color(0.18, 0.10, 0.32, 1.0),
				"fog_density": 0.008,
				"torch_color": Color(0.957, 0.247, 0.369, 1.0),  # Solar flare beacons
				"torch_energy": 3.0,
				"particle_color": Color(0.506, 0.549, 0.973, 0.9), # Astral stardust
				"particle_type": "ASTRAL_STARDUST",
				"particle_speed": 0.8
			}
		_:
			return get_biome_data(1)
