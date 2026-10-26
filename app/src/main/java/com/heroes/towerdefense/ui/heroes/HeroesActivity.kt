package com.heroes.towerdefense.ui.heroes

import android.content.Intent
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
import com.heroes.towerdefense.game.progression.HeroSelectionResult
import com.heroes.towerdefense.game.progression.HeroUnlockManager
import com.heroes.towerdefense.ui.shop.ShopActivity

/**
 * Heroes Collection and Roster screen (Tela de Coleção de Heróis).
 * Displays all 6 canonical fantasy heroes, their lock/unlock states,
 * base level requirements, and lets the player choose the active deployed hero.
 */
class HeroesActivity : AppCompatActivity() {

    private lateinit var preferences: GamePreferences
    private lateinit var economyManager: EconomyManager
    private lateinit var baseManager: BaseEvolutionManager
    private lateinit var heroUnlockManager: HeroUnlockManager

    // Top status
    private lateinit var tvHeroesPlayerCoins: TextView
    private lateinit var tvHeroesSelectedHero: TextView

    // Hero item views
    private lateinit var avatarHeroKael: HeroAvatarView
    private lateinit var tvHeroStatusKael: TextView
    private lateinit var btnSelectHeroKael: Button

    private lateinit var avatarHeroLyra: HeroAvatarView
    private lateinit var tvHeroStatusLyra: TextView
    private lateinit var btnSelectHeroLyra: Button

    private lateinit var avatarHeroBrakk: HeroAvatarView
    private lateinit var tvHeroStatusBrakk: TextView
    private lateinit var btnSelectHeroBrakk: Button

    private lateinit var avatarHeroNyra: HeroAvatarView
    private lateinit var tvHeroStatusNyra: TextView
    private lateinit var btnSelectHeroNyra: Button

    private lateinit var avatarHeroTiko: HeroAvatarView
    private lateinit var tvHeroStatusTiko: TextView
    private lateinit var btnSelectHeroTiko: Button

    private lateinit var avatarHeroVesper: HeroAvatarView
    private lateinit var tvHeroStatusVesper: TextView
    private lateinit var btnSelectHeroVesper: Button

    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heroes)

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
        economyManager = EconomyManager(preferences)
        baseManager = BaseEvolutionManager(preferences, economyManager)
        heroUnlockManager = HeroUnlockManager(preferences, economyManager, baseManager)
    }

    private fun initViews() {
        tvHeroesPlayerCoins = findViewById(R.id.tvHeroesPlayerCoins)
        tvHeroesSelectedHero = findViewById(R.id.tvHeroesSelectedHero)

        avatarHeroKael = findViewById(R.id.avatarHeroKael)
        tvHeroStatusKael = findViewById(R.id.tvHeroStatusKael)
        btnSelectHeroKael = findViewById(R.id.btnSelectHeroKael)

        avatarHeroLyra = findViewById(R.id.avatarHeroLyra)
        tvHeroStatusLyra = findViewById(R.id.tvHeroStatusLyra)
        btnSelectHeroLyra = findViewById(R.id.btnSelectHeroLyra)

        avatarHeroBrakk = findViewById(R.id.avatarHeroBrakk)
        tvHeroStatusBrakk = findViewById(R.id.tvHeroStatusBrakk)
        btnSelectHeroBrakk = findViewById(R.id.btnSelectHeroBrakk)

        avatarHeroNyra = findViewById(R.id.avatarHeroNyra)
        tvHeroStatusNyra = findViewById(R.id.tvHeroStatusNyra)
        btnSelectHeroNyra = findViewById(R.id.btnSelectHeroNyra)

        avatarHeroTiko = findViewById(R.id.avatarHeroTiko)
        tvHeroStatusTiko = findViewById(R.id.tvHeroStatusTiko)
        btnSelectHeroTiko = findViewById(R.id.btnSelectHeroTiko)

        avatarHeroVesper = findViewById(R.id.avatarHeroVesper)
        tvHeroStatusVesper = findViewById(R.id.tvHeroStatusVesper)
        btnSelectHeroVesper = findViewById(R.id.btnSelectHeroVesper)

        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            SoundManager.playClick()
            finish()
        }

        btnSelectHeroKael.setOnClickListener { handleHeroSelect("hero_kael") }
        btnSelectHeroLyra.setOnClickListener { handleHeroSelect("hero_lyra") }
        btnSelectHeroBrakk.setOnClickListener { handleHeroSelect("hero_brakk") }
        btnSelectHeroNyra.setOnClickListener { handleHeroSelect("hero_nyra") }
        btnSelectHeroTiko.setOnClickListener { handleHeroSelect("hero_tiko") }
        btnSelectHeroVesper.setOnClickListener { handleHeroSelect("hero_vesper") }
    }

    private fun handleHeroSelect(heroId: String) {
        if (!heroUnlockManager.isHeroUnlocked(heroId)) {
            SoundManager.playEnemyHit()
            val req = heroUnlockManager.getRequirement(heroId)
            val name = req?.heroName ?: heroId
            val levelReq = req?.requiredBaseLevel ?: 1
            Toast.makeText(
                this,
                "$name está bloqueado! Requer Base Nível $levelReq e recrutamento na Loja.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        when (val result = heroUnlockManager.selectHero(heroId)) {
            is HeroSelectionResult.Success -> {
                SoundManager.playHeroSkill(heroId)
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                refreshUi()
            }
            is HeroSelectionResult.Error -> {
                SoundManager.playEnemyHit()
                Toast.makeText(this, result.reason, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshUi() {
        tvHeroesPlayerCoins.text = "💰 ${economyManager.getBalance()} Moedas"
        val activeHeroId = heroUnlockManager.getSelectedHeroId()
        val activeName = heroUnlockManager.getRequirement(activeHeroId)?.heroName ?: "Kael"
        tvHeroesSelectedHero.text = "⚔ Ativo: $activeName"

        updateHeroRow("hero_kael", avatarHeroKael, tvHeroStatusKael, btnSelectHeroKael, activeHeroId)
        updateHeroRow("hero_lyra", avatarHeroLyra, tvHeroStatusLyra, btnSelectHeroLyra, activeHeroId)
        updateHeroRow("hero_brakk", avatarHeroBrakk, tvHeroStatusBrakk, btnSelectHeroBrakk, activeHeroId)
        updateHeroRow("hero_nyra", avatarHeroNyra, tvHeroStatusNyra, btnSelectHeroNyra, activeHeroId)
        updateHeroRow("hero_tiko", avatarHeroTiko, tvHeroStatusTiko, btnSelectHeroTiko, activeHeroId)
        updateHeroRow("hero_vesper", avatarHeroVesper, tvHeroStatusVesper, btnSelectHeroVesper, activeHeroId)
    }

    private fun updateHeroRow(
        heroId: String,
        avatarView: HeroAvatarView,
        tvStatus: TextView,
        btnSelect: Button,
        activeHeroId: String
    ) {
        val isUnlocked = heroUnlockManager.isHeroUnlocked(heroId)
        val isSelected = heroId == activeHeroId
        val req = heroUnlockManager.getRequirement(heroId)

        avatarView.setHeroId(heroId, isUnlocked = isUnlocked)

        when {
            isSelected -> {
                tvStatus.text = "⚔ Herói Ativo em Batalha"
                tvStatus.setTextColor(getColor(R.color.gold_primary))
                btnSelect.text = "SELECIONADO"
                btnSelect.isEnabled = false
                btnSelect.alpha = 1.0f
            }
            isUnlocked -> {
                tvStatus.text = "Desbloqueado e Pronto"
                tvStatus.setTextColor(getColor(R.color.emerald_primary))
                btnSelect.text = "SELECIONAR"
                btnSelect.isEnabled = true
                btnSelect.alpha = 1.0f
            }
            else -> {
                val reqLevel = req?.requiredBaseLevel ?: 1
                val cost = req?.cost ?: 0
                tvStatus.text = "Bloqueado • Requer Base Nv $reqLevel ($cost 💰 na Loja)"
                tvStatus.setTextColor(getColor(R.color.crimson_light))
                btnSelect.text = "BLOQUEADO"
                btnSelect.isEnabled = true // allows clicking to show requirement message / guidance
                btnSelect.alpha = 0.5f
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        SoundManager.playClick()
        finish()
        return true
    }
}
