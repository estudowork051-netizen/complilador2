package com.heroes.towerdefense.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.heroes.towerdefense.R
import com.heroes.towerdefense.audio.SoundManager
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.ui.heroes.HeroesActivity
import com.heroes.towerdefense.ui.preparation.StagePreparationActivity
import com.heroes.towerdefense.ui.settings.SettingsActivity
import com.heroes.towerdefense.ui.shop.ShopActivity
import com.heroes.towerdefense.ui.stages.StageSelectActivity

/**
 * Main menu screen for Heroes Tower Defense.
 * Coordinates navigation across all primary game sections:
 * JOGAR, HERÓIS, FASES, LOJA, CONFIGURAÇÕES, and SAIR.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var preferences: GamePreferences
    private lateinit var tvPlayerCoins: TextView
    private lateinit var tvBaseLevel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = GamePreferences(this)
        SoundManager.init(preferences)

        tvPlayerCoins = findViewById(R.id.tvPlayerCoins)
        tvBaseLevel = findViewById(R.id.tvBaseLevel)

        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        SoundManager.init(preferences)
        SoundManager.startMusic()
        updateStatusBar()
    }

    override fun onPause() {
        super.onPause()
        SoundManager.pauseMusic()
    }

    private fun updateStatusBar() {
        val completedCount = preferences.completedStages.size
        val hero = com.heroes.towerdefense.model.hero.Hero.getPlannedHeroes().find { it.id == preferences.selectedHeroId }
            ?: com.heroes.towerdefense.model.hero.Hero.getPlannedHeroes().first()
        tvPlayerCoins.text = "💰 ${preferences.playerCoins} Moedas"
        tvBaseLevel.text = "⚔ ${hero.name} • 🏰 Base Nv ${preferences.baseLevel} • 🏆 $completedCount/6"
    }

    private fun setupButtons() {
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnHeroes = findViewById<Button>(R.id.btnHeroes)
        val btnStages = findViewById<Button>(R.id.btnStages)
        val btnShop = findViewById<Button>(R.id.btnShop)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        val btnExit = findViewById<Button>(R.id.btnExit)

        // 1. JOGAR opens Match Preparation for Stage 1 (or highest available stage)
        btnPlay.setOnClickListener {
            SoundManager.playClick()
            val targetStage = preferences.highestUnlockedStage.coerceIn(1, 6)
            val intent = Intent(this, StagePreparationActivity::class.java).apply {
                putExtra("extra_stage_number", targetStage)
            }
            startActivity(intent)
        }

        // 2. HERÓIS opens Heroes Collection
        btnHeroes.setOnClickListener {
            SoundManager.playClick()
            val intent = Intent(this, HeroesActivity::class.java)
            startActivity(intent)
        }

        // 3. FASES opens Campaign Stages Selection
        btnStages.setOnClickListener {
            SoundManager.playClick()
            val intent = Intent(this, StageSelectActivity::class.java)
            startActivity(intent)
        }

        // 4. LOJA opens Kingdom Shop
        btnShop.setOnClickListener {
            SoundManager.playClick()
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
        }

        // 5. CONFIGURAÇÕES opens Settings
        btnSettings.setOnClickListener {
            SoundManager.playClick()
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // 6. SAIR cleanly closes the game
        btnExit.setOnClickListener {
            SoundManager.playClick()
            SoundManager.stopMusic()
            finishAffinity()
        }
    }
}
