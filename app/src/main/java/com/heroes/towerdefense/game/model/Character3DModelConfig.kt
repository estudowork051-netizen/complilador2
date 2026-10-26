package com.heroes.towerdefense.game.model

/**
 * 3D Stylized Visual Archetype specification for Heroes in Heroes Tower Defense
 * (Extreme Visual Upgrade — Etapa 2/3).
 *
 * Maps high-fidelity 3D glTF 2.0 (.glb) models, articulated skeletal hierarchy nodes,
 * weapon attachments, and supported animation states.
 */
data class Hero3DArchetype(
    val heroId: String,
    val heroName: String,
    val title: String,
    val modelFileName: String,
    val scenePath: String,
    val articulatedParts: List<String>,
    val weaponType: String,
    val scaleFactor: Float = 1.0f,
    val supportedAnimations: List<HeroAnimState> = listOf(
        HeroAnimState.IDLE,
        HeroAnimState.WINDUP,
        HeroAnimState.STRIKE,
        HeroAnimState.RECOVERY,
        HeroAnimState.SKILL_CAST,
        HeroAnimState.VICTORY
    )
)

/**
 * 3D Stylized Visual Archetype specification for Common Enemies and Epic Bosses
 * (Extreme Visual Upgrade — Etapa 2/3).
 */
data class Enemy3DArchetype(
    val visualType: EnemyVisualType,
    val modelFileName: String,
    val scenePath: String,
    val articulatedParts: List<String>,
    val isBoss: Boolean,
    val scaleFactor: Float = 1.0f,
    val supportedAnimations: List<EnemyAnimState> = listOf(
        EnemyAnimState.IDLE,
        EnemyAnimState.WALK,
        EnemyAnimState.ATTACK,
        EnemyAnimState.HIT,
        EnemyAnimState.DEATH
    )
)

/**
 * 3D Stylized Visual Archetype for Detailed Defensive Bastions / Towers
 * (Extreme Visual Upgrade — Etapa 2/3).
 */
data class Tower3DArchetype(
    val towerType: String,
    val displayName: String,
    val modelFileName: String,
    val scenePath: String,
    val detailedComponents: List<String>,
    val hasRecoilAnimation: Boolean = true,
    val baseScale: Float = 1.0f
)

/**
 * Central registry linking gameplay entities with their corresponding
 * 3D glTF (.glb) assets, scenes, and articulated animation trees.
 */
object Character3DModelRegistry {

    val HERO_ARCHETYPES: Map<String, Hero3DArchetype> = mapOf(
        "hero_kael" to Hero3DArchetype(
            heroId = "hero_kael",
            heroName = "Kael",
            title = "Guerreiro de Fogo",
            modelFileName = "hero_kael.glb",
            scenePath = "res://scenes/heroes/hero_kael.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "Cape", "Weapon"),
            weaponType = "Flaming Greatsword",
            scaleFactor = 1.0f
        ),
        "hero_lyra" to Hero3DArchetype(
            heroId = "hero_lyra",
            heroName = "Lyra",
            title = "Arqueira Arcana",
            modelFileName = "hero_lyra.glb",
            scenePath = "res://scenes/heroes/hero_lyra.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "Quiver", "Weapon"),
            weaponType = "Composite Longbow",
            scaleFactor = 0.95f
        ),
        "hero_brakk" to Hero3DArchetype(
            heroId = "hero_brakk",
            heroName = "Brakk",
            title = "Guardião",
            modelFileName = "hero_brakk.glb",
            scenePath = "res://scenes/heroes/hero_brakk.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "Weapon"),
            weaponType = "Colossal Warhammer",
            scaleFactor = 1.15f
        ),
        "hero_nyra" to Hero3DArchetype(
            heroId = "hero_nyra",
            heroName = "Nyra",
            title = "Assassina das Sombras",
            modelFileName = "hero_nyra.glb",
            scenePath = "res://scenes/heroes/hero_nyra.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "Cape", "Weapon"),
            weaponType = "Twin Shadow Daggers",
            scaleFactor = 0.92f
        ),
        "hero_tiko" to Hero3DArchetype(
            heroId = "hero_tiko",
            heroName = "Tiko",
            title = "Engenheiro",
            modelFileName = "hero_tiko.glb",
            scenePath = "res://scenes/heroes/hero_tiko.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "Backpack", "Weapon"),
            weaponType = "Master Spanner & Wrist Launcher",
            scaleFactor = 0.90f
        ),
        "hero_vesper" to Hero3DArchetype(
            heroId = "hero_vesper",
            heroName = "Vesper",
            title = "Maga Astral",
            modelFileName = "hero_vesper.glb",
            scenePath = "res://scenes/heroes/hero_vesper.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "RobeSkirt", "Weapon"),
            weaponType = "Astral Crescent Staff",
            scaleFactor = 0.98f
        )
    )

    val ENEMY_ARCHETYPES: Map<EnemyVisualType, Enemy3DArchetype> = mapOf(
        EnemyVisualType.GOBLIN to Enemy3DArchetype(
            visualType = EnemyVisualType.GOBLIN,
            modelFileName = "enemy_goblin.glb",
            scenePath = "res://scenes/enemies/enemy_goblin.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "Weapon"),
            isBoss = false,
            scaleFactor = 0.85f
        ),
        EnemyVisualType.SKELETON to Enemy3DArchetype(
            visualType = EnemyVisualType.SKELETON,
            modelFileName = "enemy_skeleton.glb",
            scenePath = "res://scenes/enemies/enemy_skeleton.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "Weapon"),
            isBoss = false,
            scaleFactor = 0.95f
        ),
        EnemyVisualType.ORC to Enemy3DArchetype(
            visualType = EnemyVisualType.ORC,
            modelFileName = "enemy_orc.glb",
            scenePath = "res://scenes/enemies/enemy_orc.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "Weapon"),
            isBoss = false,
            scaleFactor = 1.15f
        ),
        EnemyVisualType.DARK_MAGE to Enemy3DArchetype(
            visualType = EnemyVisualType.DARK_MAGE,
            modelFileName = "enemy_dark_mage.glb",
            scenePath = "res://scenes/enemies/enemy_dark_mage.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "Weapon"),
            isBoss = false,
            scaleFactor = 1.0f
        ),
        EnemyVisualType.OBSIDIAN_GOLEM to Enemy3DArchetype(
            visualType = EnemyVisualType.OBSIDIAN_GOLEM,
            modelFileName = "boss_obsidian_golem.glb",
            scenePath = "res://scenes/bosses/boss_obsidian_golem.tscn",
            articulatedParts = listOf("Torso", "Head", "LeftArm", "RightArm", "LeftLeg", "RightLeg"),
            isBoss = true,
            scaleFactor = 2.2f,
            supportedAnimations = listOf(
                EnemyAnimState.IDLE,
                EnemyAnimState.WALK,
                EnemyAnimState.ATTACK,
                EnemyAnimState.HIT,
                EnemyAnimState.DEATH,
                EnemyAnimState.SPECIAL,
                EnemyAnimState.PHASE_TRANSITION
            )
        ),
        EnemyVisualType.SHADOW_QUEEN to Enemy3DArchetype(
            visualType = EnemyVisualType.SHADOW_QUEEN,
            modelFileName = "boss_shadow_queen.glb",
            scenePath = "res://scenes/bosses/boss_shadow_queen.tscn",
            articulatedParts = listOf("Torso", "Head", "Wings", "LeftArm", "RightArm", "Weapon"),
            isBoss = true,
            scaleFactor = 1.7f,
            supportedAnimations = listOf(
                EnemyAnimState.IDLE,
                EnemyAnimState.WALK,
                EnemyAnimState.ATTACK,
                EnemyAnimState.HIT,
                EnemyAnimState.DEATH,
                EnemyAnimState.SPECIAL,
                EnemyAnimState.PHASE_TRANSITION
            )
        ),
        EnemyVisualType.ASTRAL_DRAGON to Enemy3DArchetype(
            visualType = EnemyVisualType.ASTRAL_DRAGON,
            modelFileName = "boss_astral_dragon.glb",
            scenePath = "res://scenes/bosses/boss_astral_dragon.tscn",
            articulatedParts = listOf("Torso", "Neck", "Head", "Tail", "Wings"),
            isBoss = true,
            scaleFactor = 2.5f,
            supportedAnimations = listOf(
                EnemyAnimState.IDLE,
                EnemyAnimState.WALK,
                EnemyAnimState.ATTACK,
                EnemyAnimState.HIT,
                EnemyAnimState.DEATH,
                EnemyAnimState.SPECIAL,
                EnemyAnimState.PHASE_TRANSITION
            )
        )
    )

    val TOWER_ARCHETYPES: Map<String, Tower3DArchetype> = mapOf(
        "ARCHER" to Tower3DArchetype(
            towerType = "ARCHER",
            displayName = "Torre de Arqueiros",
            modelFileName = "tower_archer_detailed.glb",
            scenePath = "res://scenes/tower_archer_detailed.tscn",
            detailedComponents = listOf("BaseStone", "TimberGallery", "BattlementPlatform", "RoofCanopy", "TurretMount"),
            hasRecoilAnimation = true,
            baseScale = 1.0f
        ),
        "MAGE" to Tower3DArchetype(
            towerType = "MAGE",
            displayName = "Torre de Magos",
            modelFileName = "tower_mage_detailed.glb",
            scenePath = "res://scenes/tower_mage_detailed.tscn",
            detailedComponents = listOf("BaseStone", "SpiralPillars", "RunicDais", "FocusCrystal"),
            hasRecoilAnimation = false,
            baseScale = 1.0f
        ),
        "CANNON" to Tower3DArchetype(
            towerType = "CANNON",
            displayName = "Torre Canhoneira",
            modelFileName = "tower_cannon_detailed.glb",
            scenePath = "res://scenes/tower_cannon_detailed.tscn",
            detailedComponents = listOf("BaseStone", "Parapet", "TurretChassis", "CannonBarrel"),
            hasRecoilAnimation = true,
            baseScale = 1.0f
        )
    )

    fun getHeroArchetype(heroId: String): Hero3DArchetype? = HERO_ARCHETYPES[heroId]
    fun getEnemyArchetype(type: EnemyVisualType): Enemy3DArchetype? = ENEMY_ARCHETYPES[type]
    fun getTowerArchetype(towerType: String): Tower3DArchetype? = TOWER_ARCHETYPES[towerType.uppercase()]
}
