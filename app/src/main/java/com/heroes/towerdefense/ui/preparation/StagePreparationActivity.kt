package com.heroes.towerdefense.ui.preparation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.heroes.towerdefense.R
import com.heroes.towerdefense.audio.SoundManager
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.game.stage.StageConfig
import com.heroes.towerdefense.ui.stage.StageOneActivity

/**
 * Match Preparation screen (Tela de Preparação da Partida).
 * Displays stage info, tactical map preview placeholder, and starts the gameplay stage.
 */
class StagePreparationActivity : AppCompatActivity() {

    private var stageNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage_preparation)

        stageNumber = intent.getIntExtra("extra_stage_number", 1)
        bindStageData()
        setupButtons()
    }

    private fun bindStageData() {
        val stageInfo = StageConfig.getStageInfo(stageNumber)
        findViewById<TextView>(R.id.tvStageName).text = stageInfo.title
        findViewById<TextView>(R.id.tvMissionBriefing).text = stageInfo.description
        findViewById<TextView>(R.id.tvIntelWaves).text = "Ondas: ${stageInfo.totalWaves}"
        findViewById<TextView>(R.id.tvIntelGold).text = "Ouro Inicial: ${stageInfo.startingGold}"
        findViewById<TextView>(R.id.tvIntelLives).text = "Vidas da Base: ${stageInfo.startingLives}"

        // Active Hero Preview (Visual Upgrade Part 1)
        val preferences = GamePreferences(this)
        val heroId = preferences.selectedHeroId
        val hero = com.heroes.towerdefense.model.hero.Hero.getPlannedHeroes().find { it.id == heroId }
            ?: com.heroes.towerdefense.model.hero.Hero.getPlannedHeroes().first()

        findViewById<com.heroes.towerdefense.ui.heroes.HeroAvatarView>(R.id.avatarPrepActiveHero).setHero(hero, isUnlocked = true)
        findViewById<TextView>(R.id.tvPrepActiveHeroName).text = "${hero.name} • ${hero.title}"
        val tvRarity = findViewById<TextView>(R.id.tvPrepActiveHeroRarity)
        tvRarity.text = "Raridade: ${hero.rarity.badgeLabel}"
        tvRarity.setTextColor(hero.rarity.colorInt)
    }

    private fun setupButtons() {
        val btnStartStage = findViewById<Button>(R.id.btnStartStage)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnStartStage.setOnClickListener {
            SoundManager.playClick()
            val preferences = GamePreferences(this)
            val intent = Intent(this, StageOneActivity::class.java).apply {
                putExtra("extra_stage_number", stageNumber)
                putExtra("extra_hero_id", preferences.selectedHeroId)
            }
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            SoundManager.playClick()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        SoundManager.playClick()
        finish()
        return true
    }
}
