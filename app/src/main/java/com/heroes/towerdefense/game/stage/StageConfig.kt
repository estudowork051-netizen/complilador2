package com.heroes.towerdefense.game.stage

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.game.boss.BossConfig
import com.heroes.towerdefense.game.model.WaveConfig
import com.heroes.towerdefense.model.stage.StageInfo

/**
 * Centralized progression configuration for all campaign stages (Stages 1 through 6).
 * Defines wave progression, enemy composition, boss encounters, coin rewards, and first clear bonuses.
 */
object StageConfig {

    /**
     * Returns wave list configured for the requested stage.
     */
    fun getWavesForStage(stageNumber: Int): List<WaveConfig> = when (stageNumber) {
        1 -> WaveConfig.getStageOneWaves()
        2 -> listOf(
            WaveConfig(waveNumber = 1, enemyCount = 6, spawnIntervalSeconds = 1.2f, enemySpeed = 0.15f),
            WaveConfig(waveNumber = 2, enemyCount = 8, spawnIntervalSeconds = 1.1f, enemySpeed = 0.16f),
            WaveConfig(waveNumber = 3, enemyCount = 10, spawnIntervalSeconds = 1.0f, enemySpeed = 0.17f),
            WaveConfig(waveNumber = 4, enemyCount = 12, spawnIntervalSeconds = 0.9f, enemySpeed = 0.18f),
            WaveConfig(
                waveNumber = 5,
                enemyCount = 4,
                spawnIntervalSeconds = 1.2f,
                enemySpeed = 0.18f,
                bossConfig = BossConfig.getObsidianGolem()
            )
        )
        3 -> listOf(
            WaveConfig(waveNumber = 1, enemyCount = 7, spawnIntervalSeconds = 1.1f, enemySpeed = 0.16f),
            WaveConfig(waveNumber = 2, enemyCount = 9, spawnIntervalSeconds = 1.0f, enemySpeed = 0.17f),
            WaveConfig(waveNumber = 3, enemyCount = 11, spawnIntervalSeconds = 0.9f, enemySpeed = 0.18f),
            WaveConfig(waveNumber = 4, enemyCount = 13, spawnIntervalSeconds = 0.8f, enemySpeed = 0.19f),
            WaveConfig(
                waveNumber = 5,
                enemyCount = 4,
                spawnIntervalSeconds = 1.1f,
                enemySpeed = 0.19f,
                bossConfig = BossConfig.getShadowQueen()
            )
        )
        4 -> listOf(
            WaveConfig(waveNumber = 1, enemyCount = 8, spawnIntervalSeconds = 1.0f, enemySpeed = 0.17f),
            WaveConfig(waveNumber = 2, enemyCount = 10, spawnIntervalSeconds = 0.9f, enemySpeed = 0.18f),
            WaveConfig(waveNumber = 3, enemyCount = 12, spawnIntervalSeconds = 0.8f, enemySpeed = 0.19f),
            WaveConfig(waveNumber = 4, enemyCount = 14, spawnIntervalSeconds = 0.8f, enemySpeed = 0.20f),
            WaveConfig(waveNumber = 5, enemyCount = 16, spawnIntervalSeconds = 0.7f, enemySpeed = 0.22f)
        )
        5 -> listOf(
            WaveConfig(waveNumber = 1, enemyCount = 9, spawnIntervalSeconds = 1.0f, enemySpeed = 0.17f),
            WaveConfig(waveNumber = 2, enemyCount = 11, spawnIntervalSeconds = 0.9f, enemySpeed = 0.18f),
            WaveConfig(waveNumber = 3, enemyCount = 13, spawnIntervalSeconds = 0.8f, enemySpeed = 0.19f),
            WaveConfig(waveNumber = 4, enemyCount = 15, spawnIntervalSeconds = 0.7f, enemySpeed = 0.20f),
            WaveConfig(waveNumber = 5, enemyCount = 18, spawnIntervalSeconds = 0.7f, enemySpeed = 0.21f)
        )
        6 -> listOf(
            WaveConfig(waveNumber = 1, enemyCount = 8, spawnIntervalSeconds = 1.0f, enemySpeed = 0.18f),
            WaveConfig(waveNumber = 2, enemyCount = 10, spawnIntervalSeconds = 0.9f, enemySpeed = 0.19f),
            WaveConfig(waveNumber = 3, enemyCount = 12, spawnIntervalSeconds = 0.8f, enemySpeed = 0.20f),
            WaveConfig(waveNumber = 4, enemyCount = 14, spawnIntervalSeconds = 0.8f, enemySpeed = 0.21f),
            WaveConfig(
                waveNumber = 5,
                enemyCount = 5,
                spawnIntervalSeconds = 1.0f,
                enemySpeed = 0.21f,
                bossConfig = BossConfig.getAstralDragon()
            )
        )
        else -> WaveConfig.getStageOneWaves()
    }

    /**
     * Returns base coin rewards for clearing a stage.
     */
    fun getBaseCoinReward(stageNumber: Int): Int = when (stageNumber) {
        1 -> GameConstants.STAGE_ONE_VICTORY_REWARD_COINS
        2 -> 200
        3 -> 250
        4 -> 300
        5 -> 350
        6 -> 450
        else -> GameConstants.STAGE_ONE_VICTORY_REWARD_COINS
    }

    /**
     * Returns unique bonus granted only on the player's first victory in that stage.
     */
    fun getFirstClearBonus(stageNumber: Int): Int = when (stageNumber) {
        1 -> GameConstants.STAGE_ONE_FIRST_CLEAR_BONUS_COINS
        2 -> GameConstants.STAGE_TWO_FIRST_CLEAR_BONUS_COINS
        3 -> GameConstants.STAGE_THREE_FIRST_CLEAR_BONUS_COINS
        4 -> GameConstants.STAGE_FOUR_FIRST_CLEAR_BONUS_COINS
        5 -> GameConstants.STAGE_FIVE_FIRST_CLEAR_BONUS_COINS
        6 -> GameConstants.STAGE_SIX_FIRST_CLEAR_BONUS_COINS
        else -> 50
    }

    /**
     * Returns StageInfo definition for a given stage number.
     */
    fun getStageInfo(stageNumber: Int): StageInfo =
        StageInfo.getCampaignStages().find { it.stageNumber == stageNumber }
            ?: StageInfo.getStageOne()

    /**
     * Returns the BossConfig associated with this stage, if any.
     */
    fun getBossForStage(stageNumber: Int): BossConfig? =
        getWavesForStage(stageNumber).firstOrNull { it.bossConfig != null }?.bossConfig

    /**
     * Returns the bonus coin reward granted for defeating the stage boss.
     */
    fun getBossRewardCoins(stageNumber: Int): Int =
        getBossForStage(stageNumber)?.coinReward ?: 0
}
