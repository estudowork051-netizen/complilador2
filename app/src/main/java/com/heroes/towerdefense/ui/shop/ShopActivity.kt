package com.heroes.towerdefense.ui.shop

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.heroes.towerdefense.R
import com.heroes.towerdefense.audio.SoundManager
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.game.economy.EconomyManager
import com.heroes.towerdefense.game.progression.BaseEvolutionManager
import com.heroes.towerdefense.game.progression.EvolutionResult
import com.heroes.towerdefense.game.progression.HeroUnlockManager
import com.heroes.towerdefense.game.progression.HeroUnlockResult
import com.heroes.towerdefense.game.shop.PurchaseResult
import com.heroes.towerdefense.game.shop.ShopItemStatus
import com.heroes.towerdefense.game.shop.ShopManager

/**
 * Functional Kingdom Shop screen (Tela da Loja do Reino).
 * Coordinates base fortress evolution, hero recruitments, and permanent tactical upgrades.
 */
class ShopActivity : AppCompatActivity() {

    private lateinit var preferences: GamePreferences
    private lateinit var economyManager: EconomyManager
    private lateinit var baseManager: BaseEvolutionManager
    private lateinit var heroUnlockManager: HeroUnlockManager
    private lateinit var shopManager: ShopManager
    private var lastActionTimeMs: Long = 0L

    // Header / Balance
    private lateinit var tvShopCoins: TextView
    private lateinit var tvShopBaseLevel: TextView
    private lateinit var tvShopFeedbackMessage: TextView

    // Category 1: Base Evolution
    private lateinit var tvBaseEvolveTitle: TextView
    private lateinit var tvBaseEvolveDesc: TextView
    private lateinit var tvBaseEvolveCost: TextView
    private lateinit var btnEvolveBase: Button

    // Category 2: Hero Recruitment
    private lateinit var tvHeroLyraStatus: TextView
    private lateinit var btnBuyHeroLyra: Button

    private lateinit var tvHeroBrakkStatus: TextView
    private lateinit var btnBuyHeroBrakk: Button

    private lateinit var tvHeroNyraStatus: TextView
    private lateinit var btnBuyHeroNyra: Button

    private lateinit var tvHeroTikoStatus: TextView
    private lateinit var btnBuyHeroTiko: Button

    private lateinit var tvHeroVesperStatus: TextView
    private lateinit var btnBuyHeroVesper: Button

    // Category 3: Permanent Upgrades
    private lateinit var tvUpgradeTowerDamageTitle: TextView
    private lateinit var tvUpgradeTowerDamageDesc: TextView
    private lateinit var tvUpgradeTowerDamageCost: TextView
    private lateinit var btnUpgradeTowerDamage: Button

    private lateinit var tvUpgradeStartingGoldTitle: TextView
    private lateinit var tvUpgradeStartingGoldDesc: TextView
    private lateinit var tvUpgradeStartingGoldCost: TextView
    private lateinit var btnUpgradeStartingGold: Button

    private lateinit var tvUpgradeWallFortificationTitle: TextView
    private lateinit var tvUpgradeWallFortificationDesc: TextView
    private lateinit var tvUpgradeWallFortificationCost: TextView
    private lateinit var btnUpgradeWallFortification: Button

    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        initManagers()
        initViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun initManagers() {
        preferences = GamePreferences(this)
        SoundManager.init(preferences)
        economyManager = EconomyManager(preferences)
        baseManager = BaseEvolutionManager(preferences, economyManager)
        heroUnlockManager = HeroUnlockManager(preferences, economyManager, baseManager)
        shopManager = ShopManager(preferences, economyManager, baseManager, heroUnlockManager)
    }

    private fun initViews() {
        tvShopCoins = findViewById(R.id.tvShopCoins)
        tvShopBaseLevel = findViewById(R.id.tvShopBaseLevel)
        tvShopFeedbackMessage = findViewById(R.id.tvShopFeedbackMessage)

        tvBaseEvolveTitle = findViewById(R.id.tvBaseEvolveTitle)
        tvBaseEvolveDesc = findViewById(R.id.tvBaseEvolveDesc)
        tvBaseEvolveCost = findViewById(R.id.tvBaseEvolveCost)
        btnEvolveBase = findViewById(R.id.btnEvolveBase)

        tvHeroLyraStatus = findViewById(R.id.tvHeroLyraStatus)
        btnBuyHeroLyra = findViewById(R.id.btnBuyHeroLyra)

        tvHeroBrakkStatus = findViewById(R.id.tvHeroBrakkStatus)
        btnBuyHeroBrakk = findViewById(R.id.btnBuyHeroBrakk)

        tvHeroNyraStatus = findViewById(R.id.tvHeroNyraStatus)
        btnBuyHeroNyra = findViewById(R.id.btnBuyHeroNyra)

        tvHeroTikoStatus = findViewById(R.id.tvHeroTikoStatus)
        btnBuyHeroTiko = findViewById(R.id.btnBuyHeroTiko)

        tvHeroVesperStatus = findViewById(R.id.tvHeroVesperStatus)
        btnBuyHeroVesper = findViewById(R.id.btnBuyHeroVesper)

        tvUpgradeTowerDamageTitle = findViewById(R.id.tvUpgradeTowerDamageTitle)
        tvUpgradeTowerDamageDesc = findViewById(R.id.tvUpgradeTowerDamageDesc)
        tvUpgradeTowerDamageCost = findViewById(R.id.tvUpgradeTowerDamageCost)
        btnUpgradeTowerDamage = findViewById(R.id.btnUpgradeTowerDamage)

        tvUpgradeStartingGoldTitle = findViewById(R.id.tvUpgradeStartingGoldTitle)
        tvUpgradeStartingGoldDesc = findViewById(R.id.tvUpgradeStartingGoldDesc)
        tvUpgradeStartingGoldCost = findViewById(R.id.tvUpgradeStartingGoldCost)
        btnUpgradeStartingGold = findViewById(R.id.btnUpgradeStartingGold)

        tvUpgradeWallFortificationTitle = findViewById(R.id.tvUpgradeWallFortificationTitle)
        tvUpgradeWallFortificationDesc = findViewById(R.id.tvUpgradeWallFortificationDesc)
        tvUpgradeWallFortificationCost = findViewById(R.id.tvUpgradeWallFortificationCost)
        btnUpgradeWallFortification = findViewById(R.id.btnUpgradeWallFortification)

        btnBack = findViewById(R.id.btnBack)
    }

    private fun isDebounced(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastActionTimeMs < 350L) {
            return true
        }
        lastActionTimeMs = now
        return false
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            SoundManager.playClick()
            finish()
        }

        // Base evolution
        btnEvolveBase.setOnClickListener {
            if (isDebounced()) return@setOnClickListener
            when (val result = baseManager.evolveBase()) {
                is EvolutionResult.Success -> {
                    SoundManager.playEnemyDefeat()
                    showMessage(result.message, isError = false)
                }
                is EvolutionResult.Error -> {
                    SoundManager.playEnemyHit()
                    showMessage(result.reason, isError = true)
                }
            }
            refreshUi()
        }

        // Hero Recruitments
        btnBuyHeroLyra.setOnClickListener {
            if (isDebounced()) return@setOnClickListener
            handleHeroRecruit("hero_lyra")
        }

        btnBuyHeroBrakk.setOnClickListener {
            if (isDebounced()) return@setOnClickListener
            handleHeroRecruit("hero_brakk")
        }

        btnBuyHeroNyra.setOnClickListener {
            if (isDebounced()) return@setOnClickListener
            handleHeroRecruit("hero_nyra")
        }

        btnBuyHeroTiko.setOnClickListener {
            if (isDebounced()) return@setOnClickListener
            handleHeroRecruit("hero_tiko")
        }

        btnBuyHeroVesper.setOnClickListener {
            if (isDebounced()) return@setOnClickListener
            handleHeroRecruit("hero_vesper")
        }

        // Permanent Upgrades
        btnUpgradeTowerDamage.setOnClickListener {
            if (isDebounced()) return@setOnClickListener
            handleUpgradePurchase(ShopManager.UPGRADE_TOWER_DAMAGE)
        }

        btnUpgradeStartingGold.setOnClickListener {
            if (isDebounced()) return@setOnClickListener
            handleUpgradePurchase(ShopManager.UPGRADE_STARTING_GOLD)
        }

        btnUpgradeWallFortification.setOnClickListener {
            if (isDebounced()) return@setOnClickListener
            handleUpgradePurchase(ShopManager.UPGRADE_WALL_FORTIFICATION)
        }
    }

    private fun handleHeroRecruit(heroId: String) {
        when (val result = heroUnlockManager.unlockHero(heroId)) {
            is HeroUnlockResult.Success -> {
                SoundManager.playHeroSkill(heroId)
                showMessage(result.message, isError = false)
            }
            is HeroUnlockResult.Error -> {
                SoundManager.playEnemyHit()
                showMessage(result.reason, isError = true)
            }
        }
        refreshUi()
    }

    private fun handleUpgradePurchase(upgradeId: String) {
        when (val result = shopManager.buyUpgrade(upgradeId)) {
            is PurchaseResult.Success -> {
                SoundManager.playClick()
                showMessage(result.message, isError = false)
            }
            is PurchaseResult.Error -> {
                SoundManager.playEnemyHit()
                showMessage(result.reason, isError = true)
            }
        }
        refreshUi()
    }

    private fun showMessage(text: String, isError: Boolean) {
        tvShopFeedbackMessage.text = text
        tvShopFeedbackMessage.setTextColor(
            if (isError) getColor(R.color.crimson_light) else getColor(R.color.gold_light)
        )
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun refreshUi() {
        val balance = economyManager.getBalance()
        val baseLevel = baseManager.getCurrentLevel()

        tvShopCoins.text = "💰 Saldo: $balance Moedas"
        tvShopBaseLevel.text = "🏰 Base Nv $baseLevel"

        // Base evolution section
        tvBaseEvolveTitle.text = "Fortaleza Central (Nível $baseLevel de ${baseManager.getMaxLevel()})"
        val bonus = baseManager.getHealthBonus()
        if (baseManager.isMaxLevel()) {
            tvBaseEvolveDesc.text = "Benefício Atual: +$bonus Vidas Máximas na Base (Nível Máximo Atingido)."
            tvBaseEvolveCost.text = "Fortaleza no Nível Máximo"
            btnEvolveBase.text = "NÍVEL MÁXIMO"
            btnEvolveBase.isEnabled = false
            btnEvolveBase.alpha = 0.6f
        } else {
            val nextCost = baseManager.getNextLevelCost()
            val nextBonus = baseManager.getHealthBonus(baseLevel + 1)
            tvBaseEvolveDesc.text = "Benefício Atual: +$bonus Vidas Máximas.\nPróximo Nível: +$nextBonus Vidas • Desbloqueia novas opções na Loja."
            tvBaseEvolveCost.text = "Custo de Evolução: $nextCost Moedas"
            btnEvolveBase.text = "EVOLUIR FORTALEZA ($nextCost 💰)"
            val canEvolve = economyManager.canAfford(nextCost)
            btnEvolveBase.isEnabled = canEvolve
            btnEvolveBase.alpha = if (canEvolve) 1.0f else 0.5f
        }

        // Hero Recruitments
        updateHeroCard("hero_lyra", tvHeroLyraStatus, btnBuyHeroLyra)
        updateHeroCard("hero_brakk", tvHeroBrakkStatus, btnBuyHeroBrakk)
        updateHeroCard("hero_nyra", tvHeroNyraStatus, btnBuyHeroNyra)
        updateHeroCard("hero_tiko", tvHeroTikoStatus, btnBuyHeroTiko)
        updateHeroCard("hero_vesper", tvHeroVesperStatus, btnBuyHeroVesper)

        // Permanent Upgrades
        updateUpgradeCard(ShopManager.UPGRADE_TOWER_DAMAGE, tvUpgradeTowerDamageTitle, tvUpgradeTowerDamageDesc, tvUpgradeTowerDamageCost, btnUpgradeTowerDamage)
        updateUpgradeCard(ShopManager.UPGRADE_STARTING_GOLD, tvUpgradeStartingGoldTitle, tvUpgradeStartingGoldDesc, tvUpgradeStartingGoldCost, btnUpgradeStartingGold)
        updateUpgradeCard(ShopManager.UPGRADE_WALL_FORTIFICATION, tvUpgradeWallFortificationTitle, tvUpgradeWallFortificationDesc, tvUpgradeWallFortificationCost, btnUpgradeWallFortification)
    }

    private fun updateHeroCard(heroId: String, tvStatus: TextView, btnAction: Button) {
        val isUnlocked = heroUnlockManager.isHeroUnlocked(heroId)
        val req = heroUnlockManager.getRequirement(heroId) ?: return

        if (isUnlocked) {
            tvStatus.text = "Status: Recrutado e Disponível"
            tvStatus.setTextColor(getColor(R.color.emerald_primary))
            btnAction.text = "RECRUTADO"
            btnAction.isEnabled = false
            btnAction.alpha = 0.5f
        } else {
            val (canUnlock, reason) = heroUnlockManager.canUnlockHero(heroId)
            if (canUnlock) {
                tvStatus.text = "Preço: ${req.cost} Moedas • Pronto para recrutar!"
                tvStatus.setTextColor(getColor(R.color.gold_light))
                btnAction.text = "RECRUTAR (${req.cost} 💰)"
                btnAction.isEnabled = true
                btnAction.alpha = 1.0f
            } else {
                tvStatus.text = reason
                tvStatus.setTextColor(getColor(R.color.crimson_light))
                btnAction.text = "BLOQUEADO"
                btnAction.isEnabled = false
                btnAction.alpha = 0.5f
            }
        }
    }

    private fun updateUpgradeCard(
        upgradeId: String,
        tvTitle: TextView,
        tvDesc: TextView,
        tvCost: TextView,
        btnAction: Button
    ) {
        val info = shopManager.getUpgradeInfo(upgradeId) ?: return

        tvTitle.text = "${info.name} (Nível ${info.currentLevel} de ${info.maxLevel})"
        tvDesc.text = "${info.description}\nBenefício: ${info.benefitDescription}"

        when (info.status) {
            ShopItemStatus.MAX_LEVEL -> {
                tvCost.text = "Nível Máximo Atingido"
                tvCost.setTextColor(getColor(R.color.gold_primary))
                btnAction.text = "MÁXIMO"
                btnAction.isEnabled = false
                btnAction.alpha = 0.5f
            }
            ShopItemStatus.AVAILABLE -> {
                tvCost.text = "Custo: ${info.nextLevelCost} Moedas"
                tvCost.setTextColor(getColor(R.color.gold_light))
                btnAction.text = "APRIMORAR (${info.nextLevelCost} 💰)"
                btnAction.isEnabled = true
                btnAction.alpha = 1.0f
            }
            ShopItemStatus.LOCKED -> {
                tvCost.text = "Custo: ${info.nextLevelCost} Moedas (Saldo insuficiente)"
                tvCost.setTextColor(getColor(R.color.crimson_light))
                btnAction.text = "APRIMORAR (${info.nextLevelCost} 💰)"
                btnAction.isEnabled = false
                btnAction.alpha = 0.5f
            }
            ShopItemStatus.PURCHASED -> {
                tvCost.text = "Adquirido"
                btnAction.text = "ADQUIRIDO"
                btnAction.isEnabled = false
                btnAction.alpha = 0.5f
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        SoundManager.playClick()
        finish()
        return true
    }
}
