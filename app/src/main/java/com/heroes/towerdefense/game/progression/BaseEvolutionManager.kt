package com.heroes.towerdefense.game.progression

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.game.economy.EconomyManager

sealed class EvolutionResult {
    data class Success(
        val newLevel: Int,
        val healthBonus: Int,
        val message: String
    ) : EvolutionResult()

    data class Error(val reason: String) : EvolutionResult()
}

/**
 * Manages player fortress base progression, level transitions, costs,
 * prerequisites, and combat benefits.
 */
class BaseEvolutionManager(
    private val preferences: GamePreferences,
    private val economyManager: EconomyManager
) {

    fun getCurrentLevel(): Int = preferences.baseLevel

    fun getMaxLevel(): Int = GameConstants.BASE_MAX_LEVEL

    fun isMaxLevel(): Boolean = getCurrentLevel() >= getMaxLevel()

    /**
     * Returns the cost in coins to advance from the current level to the next.
     * Returns 0 if already at maximum level.
     */
    fun getNextLevelCost(): Int {
        val current = getCurrentLevel()
        if (current >= getMaxLevel()) return 0
        val costIndex = current - 1
        return if (costIndex in GameConstants.BASE_EVOLUTION_COSTS.indices) {
            GameConstants.BASE_EVOLUTION_COSTS[costIndex]
        } else {
            0
        }
    }

    /**
     * Calculates the extra base health granted by base level in combat:
     * Level 1: 0, Level 2: +2, Level 3: +4, Level 4: +6, Level 5: +8.
     */
    fun getHealthBonus(level: Int = getCurrentLevel()): Int {
        return (level - 1).coerceAtLeast(0) * GameConstants.BASE_HEALTH_BONUS_PER_LEVEL
    }

    /**
     * Checks if base evolution can be performed.
     */
    fun canEvolve(): Pair<Boolean, String> {
        if (isMaxLevel()) {
            return false to "A fortaleza já atingiu o nível máximo (Nível ${getMaxLevel()})."
        }
        val cost = getNextLevelCost()
        if (!economyManager.canAfford(cost)) {
            val balance = economyManager.getBalance()
            return false to "Moedas insuficientes. Custo: $cost moedas (Saldo atual: $balance)."
        }
        return true to "Pronto para evoluir para o Nível ${getCurrentLevel() + 1}."
    }

    /**
     * Attempts to evolve the fortress base.
     */
    fun evolveBase(): EvolutionResult {
        val (canEvolve, reason) = canEvolve()
        if (!canEvolve) {
            return EvolutionResult.Error(reason)
        }

        val cost = getNextLevelCost()
        val spent = economyManager.spendCoins(cost)
        if (!spent) {
            return EvolutionResult.Error("Falha na dedução de moedas para evolução.")
        }

        val nextLevel = getCurrentLevel() + 1
        preferences.baseLevel = nextLevel
        val bonus = getHealthBonus(nextLevel)

        return EvolutionResult.Success(
            newLevel = nextLevel,
            healthBonus = bonus,
            message = "Fortaleza evoluída com sucesso para o Nível $nextLevel! (+${bonus} Vida Máxima)"
        )
    }

    fun getLevelBenefitDescription(level: Int): String {
        return when (level) {
            1 -> "Nível Inicial: Acesso ao Cavaleiro Kael e 20 Vidas base."
            2 -> "+2 Vidas Máximas na Base • Desbloqueia Arqueira Lyra na Loja."
            3 -> "+4 Vidas Máximas na Base • Desbloqueia Guardião Brakk e Maga Nyra na Loja."
            4 -> "+6 Vidas Máximas na Base • Desbloqueia Artífice Tiko na Loja."
            5 -> "+8 Vidas Máximas na Base • Desbloqueia Assassina Vesper (Nível Máximo da Fortaleza)."
            else -> "+${getHealthBonus(level)} Vidas Máximas na Base."
        }
    }
}
