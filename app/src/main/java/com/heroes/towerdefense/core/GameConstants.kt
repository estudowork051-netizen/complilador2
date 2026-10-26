package com.heroes.towerdefense.core

/**
 * Global game constants for Heroes Tower Defense.
 */
object GameConstants {
    const val APP_NAME = "Heroes Tower Defense"
    const val GAME_VERSION = "1.0.0"

    // Default configuration values
    const val DEFAULT_MASTER_VOLUME = 80
    const val DEFAULT_SFX_VOLUME = 80
    const val DEFAULT_MUSIC_ENABLED = true
    const val DEFAULT_VIBRATION_ENABLED = true

    // Volume boundaries
    const val MIN_VOLUME = 0
    const val MAX_VOLUME = 100

    // Stage 1 configuration (Part 3 specifications: 5 waves, 20 initial lives, 250 initial gold)
    const val STAGE_ONE_ID = 1
    const val STAGE_ONE_TOTAL_WAVES = 5
    const val STAGE_ONE_INITIAL_GOLD = 250
    const val STAGE_ONE_INITIAL_LIVES = 20
    const val DAMAGE_PER_ESCAPED_ENEMY = 1

    // Wave timings
    const val ENEMY_SPAWN_INTERVAL_SECONDS = 1.2f
    const val INTER_WAVE_DELAY_SECONDS = 2.0f

    // Grid specification for future game field
    const val GRID_COLUMNS = 8
    const val GRID_ROWS = 12

    // Tower building limits and touch tolerance for Stage 1
    const val STAGE_ONE_MAX_TOWERS = 5
    const val MIN_BUILD_DISTANCE_FROM_PATH = 0.08f
    const val TOUCH_SPOT_SELECTION_RADIUS = 0.08f

    // Tower 1 — Archer (Arqueiro): Fast attack, low damage, medium range
    const val ARCHER_DAMAGE = 18
    const val ARCHER_RANGE = 0.24f
    const val ARCHER_FIRE_RATE = 1.6f // shots/sec -> cooldown = 0.625s
    const val ARCHER_PROJECTILE_SPEED = 0.85f

    // Tower 2 — Mage (Mago): Slower attack, higher damage, longer range
    const val MAGE_DAMAGE = 45
    const val MAGE_RANGE = 0.30f
    const val MAGE_FIRE_RATE = 0.8f // shots/sec -> cooldown = 1.25s
    const val MAGE_PROJECTILE_SPEED = 0.65f

    // Tower 3 — Cannon (Canhoneiro): Slow attack, high damage, area splash
    const val CANNON_DAMAGE = 70
    const val CANNON_RANGE = 0.20f
    const val CANNON_FIRE_RATE = 0.45f // shots/sec -> cooldown = 2.22s
    const val CANNON_PROJECTILE_SPEED = 0.50f
    const val CANNON_SPLASH_RADIUS = 0.12f

    // =========================================================================
    // Part 6 — Economy, Base Evolution, Hero Progression and Shop Upgrades
    // =========================================================================

    const val DEFAULT_PLAYER_COINS = 300
    const val STAGE_ONE_VICTORY_REWARD_COINS = 150
    const val STAGE_ONE_PERFECT_HEALTH_BONUS_COINS = 50
    const val BOSS_DEFEAT_REWARD_COINS = 50
    const val BOSS_GOLEM_COIN_REWARD = 100
    const val BOSS_SHADOW_QUEEN_COIN_REWARD = 150
    const val BOSS_ASTRAL_DRAGON_COIN_REWARD = 250

    // Part 7 — Stage First Clear Bonuses
    const val STAGE_ONE_FIRST_CLEAR_BONUS_COINS = 0
    const val STAGE_TWO_FIRST_CLEAR_BONUS_COINS = 100
    const val STAGE_THREE_FIRST_CLEAR_BONUS_COINS = 150
    const val STAGE_FOUR_FIRST_CLEAR_BONUS_COINS = 200
    const val STAGE_FIVE_FIRST_CLEAR_BONUS_COINS = 250
    const val STAGE_SIX_FIRST_CLEAR_BONUS_COINS = 350

    // Base Evolution System
    const val BASE_MAX_LEVEL = 5
    const val BASE_HEALTH_BONUS_PER_LEVEL = 2 // Level 1: 0, Level 2: +2, Level 3: +4, etc.

    // Base Evolution Costs per transition: 1->2: 200, 2->3: 350, 3->4: 550, 4->5: 800
    val BASE_EVOLUTION_COSTS = listOf(200, 350, 550, 800)

    // Hero Unlock Requirements & Costs
    const val HERO_KAEL_REQ_BASE_LEVEL = 1
    const val HERO_KAEL_COIN_COST = 0

    const val HERO_LYRA_REQ_BASE_LEVEL = 2
    const val HERO_LYRA_COIN_COST = 150

    const val HERO_BRAKK_REQ_BASE_LEVEL = 3
    const val HERO_BRAKK_COIN_COST = 300

    const val HERO_NYRA_REQ_BASE_LEVEL = 3
    const val HERO_NYRA_COIN_COST = 350

    const val HERO_TIKO_REQ_BASE_LEVEL = 4
    const val HERO_TIKO_COIN_COST = 500

    const val HERO_VESPER_REQ_BASE_LEVEL = 5
    const val HERO_VESPER_COIN_COST = 750

    // Permanent Upgrades (Max Level 3)
    const val UPGRADE_MAX_LEVEL = 3
    val TOWER_DAMAGE_UPGRADE_COSTS = listOf(150, 300, 500)
    val STARTING_GOLD_UPGRADE_COSTS = listOf(100, 250, 400)
    val WALL_FORTIFICATION_UPGRADE_COSTS = listOf(120, 240, 380)
}

