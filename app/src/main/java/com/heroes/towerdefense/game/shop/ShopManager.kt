package com.heroes.towerdefense.game.shop

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.game.economy.EconomyManager
import com.heroes.towerdefense.game.progression.BaseEvolutionManager
import com.heroes.towerdefense.game.progression.HeroUnlockManager

sealed class PurchaseResult {
    data class Success(val message: String, val newBalance: Int) : PurchaseResult()
    data class Error(val reason: String) : PurchaseResult()
}

enum class ShopItemStatus {
    AVAILABLE,
    LOCKED,
    PURCHASED,
    MAX_LEVEL
}

data class ShopUpgradeInfo(
    val id: String,
    val name: String,
    val description: String,
    val currentLevel: Int,
    val maxLevel: Int,
    val nextLevelCost: Int,
    val status: ShopItemStatus,
    val benefitDescription: String
)

/**
 * Functional Kingdom Shop coordinator.
 * Integrates base evolution, hero recruitment, and permanent tactical upgrades.
 */
class ShopManager(
    val preferences: GamePreferences,
    val economyManager: EconomyManager,
    val baseManager: BaseEvolutionManager,
    val heroUnlockManager: HeroUnlockManager
) {

    companion object {
        const val UPGRADE_TOWER_DAMAGE = "upgrade_tower_damage"
        const val UPGRADE_STARTING_GOLD = "upgrade_starting_gold"
        const val UPGRADE_WALL_FORTIFICATION = "upgrade_wall_fortification"
    }

    /**
     * Gets current info for the 3 permanent upgrades.
     */
    fun getUpgradeInfo(upgradeId: String): ShopUpgradeInfo? {
        return when (upgradeId) {
            UPGRADE_TOWER_DAMAGE -> {
                val currentLvl = preferences.towerDamageUpgradeLevel
                val isMax = currentLvl >= GameConstants.UPGRADE_MAX_LEVEL
                val cost = if (!isMax) GameConstants.TOWER_DAMAGE_UPGRADE_COSTS[currentLvl] else 0
                val status = when {
                    isMax -> ShopItemStatus.MAX_LEVEL
                    economyManager.canAfford(cost) -> ShopItemStatus.AVAILABLE
                    else -> ShopItemStatus.LOCKED
                }
                ShopUpgradeInfo(
                    id = UPGRADE_TOWER_DAMAGE,
                    name = "Forja Arcana",
                    description = "Aprimora os projéteis e magias de todas as torres defensivas (+10% de dano por nível).",
                    currentLevel = currentLvl,
                    maxLevel = GameConstants.UPGRADE_MAX_LEVEL,
                    nextLevelCost = cost,
                    status = status,
                    benefitDescription = if (isMax) "+30% de Dano em todas as Torres (MÁXIMO)"
                    else "+${(currentLvl + 1) * 10}% de Dano em todas as Torres"
                )
            }
            UPGRADE_STARTING_GOLD -> {
                val currentLvl = preferences.startingGoldUpgradeLevel
                val isMax = currentLvl >= GameConstants.UPGRADE_MAX_LEVEL
                val cost = if (!isMax) GameConstants.STARTING_GOLD_UPGRADE_COSTS[currentLvl] else 0
                val status = when {
                    isMax -> ShopItemStatus.MAX_LEVEL
                    economyManager.canAfford(cost) -> ShopItemStatus.AVAILABLE
                    else -> ShopItemStatus.LOCKED
                }
                ShopUpgradeInfo(
                    id = UPGRADE_STARTING_GOLD,
                    name = "Provisões de Guerra",
                    description = "Aumenta o ouro inicial disponível nas fases (+50 de ouro de combate por nível).",
                    currentLevel = currentLvl,
                    maxLevel = GameConstants.UPGRADE_MAX_LEVEL,
                    nextLevelCost = cost,
                    status = status,
                    benefitDescription = if (isMax) "+150 Ouro Inicial em Combate (MÁXIMO)"
                    else "+${(currentLvl + 1) * 50} Ouro Inicial em Combate"
                )
            }
            UPGRADE_WALL_FORTIFICATION -> {
                val currentLvl = preferences.wallFortificationUpgradeLevel
                val isMax = currentLvl >= GameConstants.UPGRADE_MAX_LEVEL
                val cost = if (!isMax) GameConstants.WALL_FORTIFICATION_UPGRADE_COSTS[currentLvl] else 0
                val status = when {
                    isMax -> ShopItemStatus.MAX_LEVEL
                    economyManager.canAfford(cost) -> ShopItemStatus.AVAILABLE
                    else -> ShopItemStatus.LOCKED
                }
                ShopUpgradeInfo(
                    id = UPGRADE_WALL_FORTIFICATION,
                    name = "Muralhas de Pedra",
                    description = "Reforça as defesas periféricas da fortaleza com muralhas adicionais (+2 vidas por nível).",
                    currentLevel = currentLvl,
                    maxLevel = GameConstants.UPGRADE_MAX_LEVEL,
                    nextLevelCost = cost,
                    status = status,
                    benefitDescription = if (isMax) "+6 Vidas Adicionais na Base (MÁXIMO)"
                    else "+${(currentLvl + 1) * 2} Vidas Adicionais na Base"
                )
            }
            else -> null
        }
    }

    /**
     * Purchases a permanent upgrade.
     */
    fun buyUpgrade(upgradeId: String): PurchaseResult {
        val info = getUpgradeInfo(upgradeId) ?: return PurchaseResult.Error("Melhoria não encontrada.")

        if (info.currentLevel >= info.maxLevel) {
            return PurchaseResult.Error("${info.name} já atingiu o nível máximo permito (${info.maxLevel}).")
        }

        val cost = info.nextLevelCost
        if (!economyManager.canAfford(cost)) {
            val balance = economyManager.getBalance()
            return PurchaseResult.Error("Moedas insuficientes. Custo: $cost moedas (Saldo: $balance).")
        }

        val spent = economyManager.spendCoins(cost)
        if (!spent) {
            return PurchaseResult.Error("Falha na dedução de moedas.")
        }

        when (upgradeId) {
            UPGRADE_TOWER_DAMAGE -> preferences.towerDamageUpgradeLevel += 1
            UPGRADE_STARTING_GOLD -> preferences.startingGoldUpgradeLevel += 1
            UPGRADE_WALL_FORTIFICATION -> preferences.wallFortificationUpgradeLevel += 1
        }

        val updated = getUpgradeInfo(upgradeId)!!
        return PurchaseResult.Success(
            message = "${info.name} aprimorado para o Nível ${updated.currentLevel}! (${updated.benefitDescription})",
            newBalance = economyManager.getBalance()
        )
    }

    // =========================================================================
    // Combat Stat Multipliers and Bonuses
    // =========================================================================

    fun getTowerDamageMultiplier(): Float = 1.0f + (preferences.towerDamageUpgradeLevel * 0.10f)

    fun getStartingGoldBonus(): Int = preferences.startingGoldUpgradeLevel * 50

    fun getWallFortificationHpBonus(): Int = preferences.wallFortificationUpgradeLevel * 2

    fun getTotalBaseHealthBonus(): Int = baseManager.getHealthBonus() + getWallFortificationHpBonus()
}
