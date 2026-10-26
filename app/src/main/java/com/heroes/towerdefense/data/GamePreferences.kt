package com.heroes.towerdefense.data

import android.content.Context
import com.heroes.towerdefense.core.GameConstants

/**
 * Manages game settings, player economy, base progression, hero unlocks,
 * and permanent upgrades persistence.
 *
 * Supports both Android SharedPreferences (via Context) and arbitrary KeyValueStorage
 * (e.g. InMemoryStorage for zero-dependency unit tests).
 */
class GamePreferences(private val storage: KeyValueStorage) {

    constructor(context: Context) : this(SharedPrefsStorage(context))

    companion object {
        private const val KEY_MASTER_VOLUME = "key_master_volume"
        private const val KEY_SFX_VOLUME = "key_sfx_volume"
        private const val KEY_MUSIC_ENABLED = "key_music_enabled"
        private const val KEY_VIBRATION_ENABLED = "key_vibration_enabled"
        private const val KEY_HIGHEST_UNLOCKED_STAGE = "key_highest_unlocked_stage"

        // Part 6 — Economy, Base, Heroes, Upgrades
        private const val KEY_PLAYER_COINS = "key_player_coins"
        private const val KEY_BASE_LEVEL = "key_base_level"
        private const val KEY_UNLOCKED_HEROES = "key_unlocked_heroes"
        private const val KEY_SELECTED_HERO_ID = "key_selected_hero_id"
        private const val KEY_COMPLETED_STAGES = "key_completed_stages"
        private const val KEY_REWARDED_MATCH_RUNS = "key_rewarded_match_runs"
        private const val KEY_UPGRADE_TOWER_DAMAGE = "key_upgrade_tower_damage"
        private const val KEY_UPGRADE_STARTING_GOLD = "key_upgrade_starting_gold"
        private const val KEY_UPGRADE_WALL_FORTIFICATION = "key_upgrade_wall_fortification"

        // Part 7 — Bosses & Advanced Stage Progression
        private const val KEY_DEFEATED_BOSSES = "key_defeated_bosses"
        private const val KEY_FIRST_CLEAR_REWARDS = "key_first_clear_rewards"

        // Upgrade Visual Part 3 — Graphics Quality
        private const val KEY_GRAPHIC_QUALITY = "key_graphic_quality"
    }

    var graphicQuality: com.heroes.towerdefense.game.map.GraphicQuality
        get() {
            val name = storage.getString(KEY_GRAPHIC_QUALITY, com.heroes.towerdefense.game.map.GraphicQuality.HIGH.name)
                ?: com.heroes.towerdefense.game.map.GraphicQuality.HIGH.name
            return try {
                com.heroes.towerdefense.game.map.GraphicQuality.valueOf(name)
            } catch (e: Exception) {
                com.heroes.towerdefense.game.map.GraphicQuality.HIGH
            }
        }
        set(value) = storage.putString(KEY_GRAPHIC_QUALITY, value.name)

    var masterVolume: Int
        get() = storage.getInt(KEY_MASTER_VOLUME, GameConstants.DEFAULT_MASTER_VOLUME)
        set(value) {
            val clamped = value.coerceIn(GameConstants.MIN_VOLUME, GameConstants.MAX_VOLUME)
            storage.putInt(KEY_MASTER_VOLUME, clamped)
        }

    var sfxVolume: Int
        get() = storage.getInt(KEY_SFX_VOLUME, GameConstants.DEFAULT_SFX_VOLUME)
        set(value) {
            val clamped = value.coerceIn(GameConstants.MIN_VOLUME, GameConstants.MAX_VOLUME)
            storage.putInt(KEY_SFX_VOLUME, clamped)
        }

    var isMusicEnabled: Boolean
        get() = storage.getBoolean(KEY_MUSIC_ENABLED, GameConstants.DEFAULT_MUSIC_ENABLED)
        set(value) = storage.putBoolean(KEY_MUSIC_ENABLED, value)

    var isVibrationEnabled: Boolean
        get() = storage.getBoolean(KEY_VIBRATION_ENABLED, GameConstants.DEFAULT_VIBRATION_ENABLED)
        set(value) = storage.putBoolean(KEY_VIBRATION_ENABLED, value)

    var highestUnlockedStage: Int
        get() = storage.getInt(KEY_HIGHEST_UNLOCKED_STAGE, GameConstants.STAGE_ONE_ID)
        set(value) = storage.putInt(KEY_HIGHEST_UNLOCKED_STAGE, value.coerceAtLeast(1))

    // =========================================================================
    // Economy & Currency
    // =========================================================================

    var playerCoins: Int
        get() = storage.getInt(KEY_PLAYER_COINS, GameConstants.DEFAULT_PLAYER_COINS).coerceAtLeast(0)
        set(value) {
            storage.putInt(KEY_PLAYER_COINS, value.coerceAtLeast(0))
        }

    // =========================================================================
    // Base Progression
    // =========================================================================

    var baseLevel: Int
        get() = storage.getInt(KEY_BASE_LEVEL, 1).coerceIn(1, GameConstants.BASE_MAX_LEVEL)
        set(value) {
            val clamped = value.coerceIn(1, GameConstants.BASE_MAX_LEVEL)
            storage.putInt(KEY_BASE_LEVEL, clamped)
        }

    // =========================================================================
    // Heroes Progression
    // =========================================================================

    var unlockedHeroes: Set<String>
        get() {
            val stored = storage.getStringSet(KEY_UNLOCKED_HEROES, setOf("hero_kael"))
            return if (stored.isEmpty()) setOf("hero_kael") else stored
        }
        set(value) {
            val safeSet = if (value.isEmpty()) setOf("hero_kael") else value
            storage.putStringSet(KEY_UNLOCKED_HEROES, safeSet)
        }

    var selectedHeroId: String
        get() {
            val heroId = storage.getString(KEY_SELECTED_HERO_ID, "hero_kael") ?: "hero_kael"
            // Ensure selected hero is currently unlocked
            return if (unlockedHeroes.contains(heroId)) heroId else "hero_kael"
        }
        set(value) {
            val target = if (unlockedHeroes.contains(value)) value else "hero_kael"
            storage.putString(KEY_SELECTED_HERO_ID, target)
        }

    // =========================================================================
    // Stage Completions & Run Deduplication
    // =========================================================================

    var completedStages: Set<Int>
        get() {
            val stored = storage.getStringSet(KEY_COMPLETED_STAGES, emptySet())
            return stored.mapNotNull { it.toIntOrNull() }.toSet()
        }
        set(value) {
            storage.putStringSet(KEY_COMPLETED_STAGES, value.map { it.toString() }.toSet())
        }

    var rewardedMatchRuns: Set<String>
        get() = storage.getStringSet(KEY_REWARDED_MATCH_RUNS, emptySet())
        set(value) = storage.putStringSet(KEY_REWARDED_MATCH_RUNS, value)

    // =========================================================================
    // Permanent Upgrades
    // =========================================================================

    var towerDamageUpgradeLevel: Int
        get() = storage.getInt(KEY_UPGRADE_TOWER_DAMAGE, 0).coerceIn(0, GameConstants.UPGRADE_MAX_LEVEL)
        set(value) = storage.putInt(KEY_UPGRADE_TOWER_DAMAGE, value.coerceIn(0, GameConstants.UPGRADE_MAX_LEVEL))

    var startingGoldUpgradeLevel: Int
        get() = storage.getInt(KEY_UPGRADE_STARTING_GOLD, 0).coerceIn(0, GameConstants.UPGRADE_MAX_LEVEL)
        set(value) = storage.putInt(KEY_UPGRADE_STARTING_GOLD, value.coerceIn(0, GameConstants.UPGRADE_MAX_LEVEL))

    var wallFortificationUpgradeLevel: Int
        get() = storage.getInt(KEY_UPGRADE_WALL_FORTIFICATION, 0).coerceIn(0, GameConstants.UPGRADE_MAX_LEVEL)
        set(value) = storage.putInt(KEY_UPGRADE_WALL_FORTIFICATION, value.coerceIn(0, GameConstants.UPGRADE_MAX_LEVEL))

    // =========================================================================
    // Part 7 — Bosses & Advanced Stage Progression
    // =========================================================================

    var defeatedBosses: Set<String>
        get() = storage.getStringSet(KEY_DEFEATED_BOSSES, emptySet())
        set(value) = storage.putStringSet(KEY_DEFEATED_BOSSES, value)

    var firstClearRewardedStages: Set<Int>
        get() {
            val stored = storage.getStringSet(KEY_FIRST_CLEAR_REWARDS, emptySet())
            return stored.mapNotNull { it.toIntOrNull() }.toSet()
        }
        set(value) {
            storage.putStringSet(KEY_FIRST_CLEAR_REWARDS, value.map { it.toString() }.toSet())
        }

    fun isBossDefeated(bossId: String): Boolean = defeatedBosses.contains(bossId)

    fun markBossDefeated(bossId: String) {
        defeatedBosses = defeatedBosses + bossId
    }

    fun isFirstClearClaimed(stageNumber: Int): Boolean = firstClearRewardedStages.contains(stageNumber)

    fun markFirstClearClaimed(stageNumber: Int) {
        firstClearRewardedStages = firstClearRewardedStages + stageNumber
    }

    fun resetToDefaults() {
        storage.clear()
        storage.putInt(KEY_MASTER_VOLUME, GameConstants.DEFAULT_MASTER_VOLUME)
        storage.putInt(KEY_SFX_VOLUME, GameConstants.DEFAULT_SFX_VOLUME)
        storage.putBoolean(KEY_MUSIC_ENABLED, GameConstants.DEFAULT_MUSIC_ENABLED)
        storage.putBoolean(KEY_VIBRATION_ENABLED, GameConstants.DEFAULT_VIBRATION_ENABLED)
        storage.putInt(KEY_PLAYER_COINS, GameConstants.DEFAULT_PLAYER_COINS)
        storage.putInt(KEY_BASE_LEVEL, 1)
        storage.putStringSet(KEY_UNLOCKED_HEROES, setOf("hero_kael"))
        storage.putString(KEY_SELECTED_HERO_ID, "hero_kael")
        storage.putStringSet(KEY_DEFEATED_BOSSES, emptySet())
        storage.putStringSet(KEY_FIRST_CLEAR_REWARDS, emptySet())
        storage.putString(KEY_GRAPHIC_QUALITY, com.heroes.towerdefense.game.map.GraphicQuality.HIGH.name)
    }
}
