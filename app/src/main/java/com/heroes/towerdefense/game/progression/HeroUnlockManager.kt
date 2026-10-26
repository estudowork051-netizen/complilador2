package com.heroes.towerdefense.game.progression

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.game.economy.EconomyManager

data class HeroUnlockRequirement(
    val heroId: String,
    val heroName: String,
    val requiredBaseLevel: Int,
    val cost: Int,
    val defaultUnlocked: Boolean = false
)

sealed class HeroSelectionResult {
    data class Success(val heroId: String, val message: String) : HeroSelectionResult()
    data class Error(val reason: String) : HeroSelectionResult()
}

sealed class HeroUnlockResult {
    data class Success(val heroId: String, val message: String) : HeroUnlockResult()
    data class Error(val reason: String) : HeroUnlockResult()
}

/**
 * Manages the progressive unlocking and selection of the 6 canonical heroes.
 *
 * Rules:
 * - Kael is unlocked by default at Base Level 1.
 * - Other heroes become eligible once the fortress base achieves their required level.
 * - Eligible heroes can be recruited using coins in the Kingdom Shop.
 * - Blocked/locked heroes cannot be selected in match preparation.
 * - Unlocks and selections persist across game restarts.
 */
class HeroUnlockManager(
    private val preferences: GamePreferences,
    private val economyManager: EconomyManager,
    private val baseManager: BaseEvolutionManager
) {

    private val requirements = listOf(
        HeroUnlockRequirement(
            heroId = "hero_kael",
            heroName = "Kael",
            requiredBaseLevel = GameConstants.HERO_KAEL_REQ_BASE_LEVEL,
            cost = GameConstants.HERO_KAEL_COIN_COST,
            defaultUnlocked = true
        ),
        HeroUnlockRequirement(
            heroId = "hero_lyra",
            heroName = "Lyra",
            requiredBaseLevel = GameConstants.HERO_LYRA_REQ_BASE_LEVEL,
            cost = GameConstants.HERO_LYRA_COIN_COST
        ),
        HeroUnlockRequirement(
            heroId = "hero_brakk",
            heroName = "Brakk",
            requiredBaseLevel = GameConstants.HERO_BRAKK_REQ_BASE_LEVEL,
            cost = GameConstants.HERO_BRAKK_COIN_COST
        ),
        HeroUnlockRequirement(
            heroId = "hero_nyra",
            heroName = "Nyra",
            requiredBaseLevel = GameConstants.HERO_NYRA_REQ_BASE_LEVEL,
            cost = GameConstants.HERO_NYRA_COIN_COST
        ),
        HeroUnlockRequirement(
            heroId = "hero_tiko",
            heroName = "Tiko",
            requiredBaseLevel = GameConstants.HERO_TIKO_REQ_BASE_LEVEL,
            cost = GameConstants.HERO_TIKO_COIN_COST
        ),
        HeroUnlockRequirement(
            heroId = "hero_vesper",
            heroName = "Vesper",
            requiredBaseLevel = GameConstants.HERO_VESPER_REQ_BASE_LEVEL,
            cost = GameConstants.HERO_VESPER_COIN_COST
        )
    )

    fun getAllRequirements(): List<HeroUnlockRequirement> = requirements

    fun getRequirement(heroId: String): HeroUnlockRequirement? =
        requirements.find { it.heroId == heroId }

    fun getUnlockedHeroIds(): Set<String> = preferences.unlockedHeroes

    fun isHeroUnlocked(heroId: String): Boolean =
        preferences.unlockedHeroes.contains(heroId)

    fun getSelectedHeroId(): String = preferences.selectedHeroId

    /**
     * Selects an active hero for combat.
     * Rejects attempts to select locked heroes.
     */
    fun selectHero(heroId: String): HeroSelectionResult {
        if (!isHeroUnlocked(heroId)) {
            val req = getRequirement(heroId)
            val name = req?.heroName ?: heroId
            val levelReq = req?.requiredBaseLevel ?: 1
            return HeroSelectionResult.Error(
                "$name está bloqueado! Requer Base Nível $levelReq e recrutamento na Loja."
            )
        }

        preferences.selectedHeroId = heroId
        val name = getRequirement(heroId)?.heroName ?: heroId
        return HeroSelectionResult.Success(heroId, "$name selecionado para a batalha!")
    }

    /**
     * Verifies if a hero is eligible for unlocking.
     */
    fun canUnlockHero(heroId: String): Pair<Boolean, String> {
        if (isHeroUnlocked(heroId)) {
            return false to "Herói já está recrutado e disponível."
        }

        val req = getRequirement(heroId) ?: return false to "Herói desconhecido."

        val currentBaseLevel = baseManager.getCurrentLevel()
        if (currentBaseLevel < req.requiredBaseLevel) {
            return false to "Requer Fortaleza Nível ${req.requiredBaseLevel} (Nível atual: $currentBaseLevel)."
        }

        if (!economyManager.canAfford(req.cost)) {
            val balance = economyManager.getBalance()
            return false to "Moedas insuficientes. Custo: ${req.cost} moedas (Saldo: $balance)."
        }

        return true to "Elegível para recrutamento."
    }

    /**
     * Recruits/unlocks a hero using coins.
     */
    fun unlockHero(heroId: String): HeroUnlockResult {
        val (canUnlock, reason) = canUnlockHero(heroId)
        if (!canUnlock) {
            return HeroUnlockResult.Error(reason)
        }

        val req = getRequirement(heroId)!!
        if (req.cost > 0) {
            val spent = economyManager.spendCoins(req.cost)
            if (!spent) {
                return HeroUnlockResult.Error("Falha ao debitar moedas.")
            }
        }

        val updated = preferences.unlockedHeroes + heroId
        preferences.unlockedHeroes = updated

        return HeroUnlockResult.Success(
            heroId = heroId,
            message = "${req.heroName} foi recrutado com sucesso e está pronto para o combate!"
        )
    }
}
