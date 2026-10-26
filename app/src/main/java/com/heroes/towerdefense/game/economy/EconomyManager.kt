package com.heroes.towerdefense.game.economy

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.game.stage.StageConfig

/**
 * Breakdown of coins awarded upon match victory.
 */
data class VictoryReward(
    val baseReward: Int,
    val perfectClearBonus: Int,
    val bossBonus: Int,
    val totalCoins: Int,
    val firstClearBonus: Int = 0
) {
    val bossDefeatBonus: Int get() = bossBonus
}

/**
 * Centralized economy system for Heroes Tower Defense.
 *
 * Enforces all strict business rules:
 * 1. Victory rewards are calculated and awarded exactly once per match run.
 * 2. Defeat never awards victory coins.
 * 3. Players cannot spend more coins than they possess.
 * 4. Invalid transactions are rejected without balance corruption.
 * 5. Reward calculations are centralized, deterministic, and fully testable.
 * 6. Unique first clear bonuses are awarded strictly once per stage.
 */
class EconomyManager(private val preferences: GamePreferences) {

    fun getBalance(): Int = preferences.playerCoins

    fun canAfford(cost: Int): Boolean = cost >= 0 && preferences.playerCoins >= cost

    /**
     * Safely adds coins to the player's balance.
     * Rejects negative values without modifying balance.
     */
    fun addCoins(amount: Int): Boolean {
        if (amount < 0) return false
        val current = preferences.playerCoins
        val newBalance = try {
            Math.addExact(current, amount)
        } catch (_: ArithmeticException) {
            Int.MAX_VALUE
        }
        preferences.playerCoins = newBalance
        return true
    }

    /**
     * Safely deducts coins from the player's balance.
     * Rejects non-positive costs and transactions exceeding current balance.
     */
    fun spendCoins(cost: Int): Boolean {
        if (cost <= 0) return false
        if (!canAfford(cost)) return false

        preferences.playerCoins -= cost
        return true
    }

    /**
     * Calculates the deterministic victory reward based on performance:
     * - Base stage victory reward
     * - Perfect health bonus (if 100% lives preserved)
     * - Boss defeat bonus (if a boss was cleared)
     * - First clear bonus (if cleared for the first time)
     */
    fun calculateVictoryReward(
        livesRemaining: Int,
        maxLives: Int,
        defeatedBoss: Boolean = false,
        stageNumber: Int = 1,
        isFirstClear: Boolean = false,
        customBossBonus: Int? = null
    ): VictoryReward {
        val base = StageConfig.getBaseCoinReward(stageNumber)
        val perfectBonus = if (livesRemaining >= maxLives && maxLives > 0) {
            GameConstants.STAGE_ONE_PERFECT_HEALTH_BONUS_COINS
        } else {
            0
        }
        val bossBonus = if (defeatedBoss) {
            customBossBonus ?: GameConstants.BOSS_DEFEAT_REWARD_COINS
        } else {
            0
        }
        val firstClear = if (isFirstClear) StageConfig.getFirstClearBonus(stageNumber) else 0
        val total = base + perfectBonus + bossBonus + firstClear
        return VictoryReward(
            baseReward = base,
            perfectClearBonus = perfectBonus,
            bossBonus = bossBonus,
            totalCoins = total,
            firstClearBonus = firstClear
        )
    }

    /**
     * Claims the victory reward for a match run.
     * Guarantees deduplication: if the given [runId] was already rewarded,
     * this method returns null and does NOT grant duplicate coins.
     */
    fun claimVictoryReward(
        runId: String,
        livesRemaining: Int,
        maxLives: Int,
        defeatedBoss: Boolean = false,
        stageNumber: Int = 1,
        bossId: String? = null,
        customBossBonus: Int? = null
    ): VictoryReward? {
        if (runId.isBlank()) return null

        val rewardedRuns = preferences.rewardedMatchRuns
        if (rewardedRuns.contains(runId)) {
            // Already rewarded: reject reprocessed event
            return null
        }

        val isFirstClear = !preferences.isFirstClearClaimed(stageNumber)
        val reward = calculateVictoryReward(
            livesRemaining = livesRemaining,
            maxLives = maxLives,
            defeatedBoss = defeatedBoss,
            stageNumber = stageNumber,
            isFirstClear = isFirstClear,
            customBossBonus = customBossBonus
        )
        addCoins(reward.totalCoins)

        if (isFirstClear) {
            preferences.markFirstClearClaimed(stageNumber)
        }
        if (defeatedBoss && bossId != null) {
            preferences.markBossDefeated(bossId)
        }

        // Mark run as rewarded and record stage completion
        preferences.rewardedMatchRuns = rewardedRuns + runId
        recordStageCompleted(stageNumber)

        return reward
    }

    fun recordStageCompleted(stageNumber: Int) {
        val completed = preferences.completedStages
        if (!completed.contains(stageNumber)) {
            preferences.completedStages = completed + stageNumber
            // Also update highest unlocked stage
            if (stageNumber >= preferences.highestUnlockedStage) {
                preferences.highestUnlockedStage = stageNumber + 1
            }
        }
    }

    fun isStageCompleted(stageNumber: Int): Boolean = preferences.completedStages.contains(stageNumber)
}
