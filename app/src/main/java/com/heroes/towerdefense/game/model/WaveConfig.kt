package com.heroes.towerdefense.game.model

import com.heroes.towerdefense.game.boss.BossConfig

/**
 * Configuration of an enemy wave in campaign stages.
 */
data class WaveConfig(
    val waveNumber: Int,
    val enemyCount: Int,
    val spawnIntervalSeconds: Float = 1.2f,
    val enemySpeed: Float = 0.16f, // Normalized distance per second
    val bossConfig: BossConfig? = null
) {
    val isBossWave: Boolean
        get() = bossConfig != null
    init {
        require(waveNumber >= 1) { "Wave number must be at least 1" }
        require(enemyCount > 0) { "Enemy count must be greater than zero" }
        require(spawnIntervalSeconds > 0f) { "Spawn interval must be positive" }
        require(enemySpeed > 0f) { "Enemy speed must be positive" }
    }

    companion object {
        /**
         * The canonical 5 waves configured as required by Part 3:
         * Wave 1: 5 enemies
         * Wave 2: 7 enemies
         * Wave 3: 9 enemies
         * Wave 4: 11 enemies
         * Wave 5: 13 enemies
         */
        fun getStageOneWaves(): List<WaveConfig> = listOf(
            WaveConfig(waveNumber = 1, enemyCount = 5, spawnIntervalSeconds = 1.2f, enemySpeed = 0.15f),
            WaveConfig(waveNumber = 2, enemyCount = 7, spawnIntervalSeconds = 1.1f, enemySpeed = 0.16f),
            WaveConfig(waveNumber = 3, enemyCount = 9, spawnIntervalSeconds = 1.0f, enemySpeed = 0.17f),
            WaveConfig(waveNumber = 4, enemyCount = 11, spawnIntervalSeconds = 0.9f, enemySpeed = 0.18f),
            WaveConfig(waveNumber = 5, enemyCount = 13, spawnIntervalSeconds = 0.8f, enemySpeed = 0.20f)
        )
    }
}
