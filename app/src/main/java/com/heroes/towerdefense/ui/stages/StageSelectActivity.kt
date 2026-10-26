package com.heroes.towerdefense.ui.stages

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.heroes.towerdefense.R
import com.heroes.towerdefense.audio.SoundManager
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.ui.preparation.StagePreparationActivity

/**
 * Stage Selection screen (Tela de Fases).
 * Allows selection of unlocked stages and prevents starting matches on locked stages.
 * Dynamically updates card appearance, alpha, and audio feedback.
 */
class StageSelectActivity : AppCompatActivity() {

    private lateinit var preferences: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage_select)
        preferences = GamePreferences(this)
        SoundManager.init(preferences)

        setupStageCards()
        setupBackButton()
    }

    override fun onResume() {
        super.onResume()
        setupStageCards()
    }

    private fun setupStageCards() {
        val cardStage1 = findViewById<CardView>(R.id.cardStage1)
        val cardStage2 = findViewById<CardView>(R.id.cardStage2)
        val cardStage3 = findViewById<CardView>(R.id.cardStage3)
        val cardStage4 = findViewById<CardView>(R.id.cardStage4)
        val cardStage5 = findViewById<CardView>(R.id.cardStage5)
        val cardStage6 = findViewById<CardView>(R.id.cardStage6)

        val cards = listOf(cardStage1, cardStage2, cardStage3, cardStage4, cardStage5, cardStage6)
        val highestUnlocked = preferences.highestUnlockedStage

        cards.forEachIndexed { index, cardView ->
            val stageNumber = index + 1
            val isUnlocked = stageNumber <= highestUnlocked

            // Visual feedback: dim locked stages, highlight unlocked stages
            cardView.alpha = if (isUnlocked) 1.0f else 0.55f

            if (isUnlocked) {
                cardView.setOnClickListener {
                    SoundManager.playClick()
                    val intent = Intent(this, StagePreparationActivity::class.java).apply {
                        putExtra("extra_stage_number", stageNumber)
                    }
                    startActivity(intent)
                }
            } else {
                cardView.setOnClickListener {
                    SoundManager.playEnemyHit()
                    Toast.makeText(this, getString(R.string.stage_locked_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBackButton() {
        val btnBackToMenu = findViewById<Button>(R.id.btnBackToMenu)
        btnBackToMenu.setOnClickListener {
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
