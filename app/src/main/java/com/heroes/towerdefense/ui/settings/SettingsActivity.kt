package com.heroes.towerdefense.ui.settings

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.heroes.towerdefense.R
import com.heroes.towerdefense.audio.SoundManager
import com.heroes.towerdefense.data.GamePreferences

/**
 * Settings screen for audio and gameplay preferences.
 * Fully functional with real-time updates and persistence in SharedPreferences.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var preferences: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferences = GamePreferences(this)
        SoundManager.init(preferences)

        setupVolumeControls()
        setupToggles()
        setupBackButton()
    }

    private fun setupVolumeControls() {
        val seekMasterVolume = findViewById<SeekBar>(R.id.seekMasterVolume)
        val tvMasterPercent = findViewById<TextView>(R.id.tvMasterVolumePercent)
        val seekSfxVolume = findViewById<SeekBar>(R.id.seekSfxVolume)
        val tvSfxPercent = findViewById<TextView>(R.id.tvSfxVolumePercent)

        // Initialize from preferences
        val currentMaster = preferences.masterVolume
        seekMasterVolume.progress = currentMaster
        tvMasterPercent.text = getString(R.string.settings_saved_notice).let { "$currentMaster%" }

        seekMasterVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvMasterPercent.text = "$progress%"
                if (fromUser) {
                    preferences.masterVolume = progress
                    SoundManager.updateVolume(progress, preferences.sfxVolume, preferences.isMusicEnabled)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                SoundManager.playClick()
            }
        })

        // SFX Volume
        val currentSfx = preferences.sfxVolume
        seekSfxVolume.progress = currentSfx
        tvSfxPercent.text = "$currentSfx%"

        seekSfxVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSfxPercent.text = "$progress%"
                if (fromUser) {
                    preferences.sfxVolume = progress
                    SoundManager.updateVolume(preferences.masterVolume, progress, preferences.isMusicEnabled)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                SoundManager.playClick()
            }
        })
    }

    private fun setupToggles() {
        val switchMusic = findViewById<SwitchMaterial>(R.id.switchMusic)
        val switchVibration = findViewById<SwitchMaterial>(R.id.switchVibration)

        switchMusic.isChecked = preferences.isMusicEnabled
        switchMusic.setOnCheckedChangeListener { _, isChecked ->
            preferences.isMusicEnabled = isChecked
            SoundManager.updateVolume(preferences.masterVolume, preferences.sfxVolume, isChecked)
            if (isChecked) {
                SoundManager.startMusic()
            } else {
                SoundManager.pauseMusic()
            }
        }

        switchVibration.isChecked = preferences.isVibrationEnabled
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            preferences.isVibrationEnabled = isChecked
            SoundManager.playClick()
        }
    }

    private fun setupBackButton() {
        val btnBack = findViewById<Button>(R.id.btnBack)
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
