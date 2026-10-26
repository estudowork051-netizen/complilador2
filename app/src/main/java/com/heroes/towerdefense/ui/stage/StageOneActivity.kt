package com.heroes.towerdefense.ui.stage

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.heroes.towerdefense.R
import com.heroes.towerdefense.audio.SoundManager
import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.game.boss.ActiveBoss
import com.heroes.towerdefense.game.boss.BossAttack
import com.heroes.towerdefense.game.economy.EconomyManager
import com.heroes.towerdefense.game.engine.GameEngine
import com.heroes.towerdefense.game.engine.GameEngineListener
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.DeployedHero
import com.heroes.towerdefense.game.model.GameStatus
import com.heroes.towerdefense.game.model.PlacedTower
import com.heroes.towerdefense.game.model.TowerSpot
import com.heroes.towerdefense.game.progression.BaseEvolutionManager
import com.heroes.towerdefense.game.progression.HeroUnlockManager
import com.heroes.towerdefense.game.shop.ShopManager
import com.heroes.towerdefense.game.stage.StageConfig
import com.heroes.towerdefense.game.view.EnemyAvatarView
import com.heroes.towerdefense.game.view.StageOneCanvasView
import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.hero.HeroSkill
import com.heroes.towerdefense.model.tower.TowerType
import java.util.Locale

/**
 * Playable stage screen for campaign stages (Stages 1 through 6).
 * Coordinates 60 FPS game loop, HUD updates, hero skills activation,
 * tower construction panel, boss encounters, Pause, Victory, and Defeat states.
 */
class StageOneActivity : AppCompatActivity(), GameEngineListener {

    private lateinit var gameEngine: GameEngine
    private lateinit var canvasView: StageOneCanvasView

    private lateinit var preferences: GamePreferences
    private lateinit var economyManager: EconomyManager
    private lateinit var baseManager: BaseEvolutionManager
    private lateinit var heroUnlockManager: HeroUnlockManager
    private lateinit var shopManager: ShopManager
    private var currentMatchRunId: String = ""
    private var stageNumber: Int = 1

    // HUD views
    private lateinit var tvHudLives: TextView
    private lateinit var tvHudWave: TextView
    private lateinit var tvHudEnemies: TextView
    private lateinit var tvHudTowers: TextView
    private lateinit var btnPause: Button

    // Dedicated Boss Top HUD (Part 7)
    private lateinit var cardBossHud: CardView
    private lateinit var tvBossHudName: TextView
    private lateinit var tvBossHudPhase: TextView
    private lateinit var tvBossHudHealth: TextView
    private lateinit var tvBossWarning: TextView

    // Boss Cinematic Intro Overlay (Part 7)
    private lateinit var layoutBossIntroOverlay: FrameLayout
    private lateinit var viewBossIntroAvatar: EnemyAvatarView
    private lateinit var tvBossIntroTitle: TextView
    private lateinit var tvBossIntroName: TextView
    private lateinit var tvBossIntroSubtitle: TextView
    private lateinit var tvBossIntroDesc: TextView
    private lateinit var btnSkipBossIntro: Button

    // Bottom Hero Skills HUD (Part 5)
    private lateinit var tvHeroHudName: TextView
    private lateinit var tvHeroHudRarity: TextView
    private lateinit var btnHeroSkill1: Button
    private lateinit var btnHeroSkill2: Button

    // Bottom Tower Construction Panel
    private lateinit var cardBuildPanel: CardView
    private lateinit var layoutConstructTower: LinearLayout
    private lateinit var layoutTowerDetails: LinearLayout
    private lateinit var tvBuildPanelSubtitle: TextView
    private lateinit var btnBuildArcher: Button
    private lateinit var btnBuildMage: Button
    private lateinit var btnBuildCannon: Button
    private lateinit var btnCancelBuild: Button
    private lateinit var btnCloseTowerInfo: Button
    private lateinit var tvTowerInfoStats: TextView

    // Modal Overlays
    private lateinit var layoutPauseOverlay: FrameLayout
    private lateinit var layoutVictoryOverlay: FrameLayout
    private lateinit var layoutDefeatOverlay: FrameLayout

    // Victory/Defeat stats
    private lateinit var tvVictorySummaryHealth: TextView
    private lateinit var tvVictorySummaryReward: TextView
    private lateinit var tvDefeatSummaryWave: TextView

    // Frame Loop Handler
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isLoopRunning = false
    private var lastFrameTimeNanos: Long = 0L

    private val frameRunnable = object : Runnable {
        override fun run() {
            if (!isLoopRunning) return

            val now = System.nanoTime()
            if (lastFrameTimeNanos > 0L) {
                val deltaSeconds = ((now - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
                gameEngine.update(deltaSeconds)
                canvasView.invalidate()
                updateHud()
            }
            lastFrameTimeNanos = now

            if (gameEngine.status == GameStatus.RUNNING) {
                mainHandler.postDelayed(this, 16L) // ~60 FPS
            } else {
                isLoopRunning = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage_one)

        initViews()
        initEngine()
        setupListeners()
        setupBackPressHandler()

        // Start match
        gameEngine.start()
        startLoop()
    }

    private fun initViews() {
        canvasView = findViewById(R.id.canvasGameMap)
        tvHudLives = findViewById(R.id.tvHudLives)
        tvHudWave = findViewById(R.id.tvHudWave)
        tvHudEnemies = findViewById(R.id.tvHudEnemies)
        tvHudTowers = findViewById(R.id.tvHudTowers)
        btnPause = findViewById(R.id.btnPause)

        // Hero Skills HUD (Part 5)
        tvHeroHudName = findViewById(R.id.tvHeroHudName)
        tvHeroHudRarity = findViewById(R.id.tvHeroHudRarity)
        btnHeroSkill1 = findViewById(R.id.btnHeroSkill1)
        btnHeroSkill2 = findViewById(R.id.btnHeroSkill2)

        // Bottom Build Panel
        cardBuildPanel = findViewById(R.id.cardBuildPanel)
        layoutConstructTower = findViewById(R.id.layoutConstructTower)
        layoutTowerDetails = findViewById(R.id.layoutTowerDetails)
        tvBuildPanelSubtitle = findViewById(R.id.tvBuildPanelSubtitle)
        btnBuildArcher = findViewById(R.id.btnBuildArcher)
        btnBuildMage = findViewById(R.id.btnBuildMage)
        btnBuildCannon = findViewById(R.id.btnBuildCannon)
        btnCancelBuild = findViewById(R.id.btnCancelBuild)
        btnCloseTowerInfo = findViewById(R.id.btnCloseTowerInfo)
        tvTowerInfoStats = findViewById(R.id.tvTowerInfoStats)

        layoutPauseOverlay = findViewById(R.id.layoutPauseOverlay)
        layoutVictoryOverlay = findViewById(R.id.layoutVictoryOverlay)
        layoutDefeatOverlay = findViewById(R.id.layoutDefeatOverlay)

        // Dedicated Boss Top HUD (Part 7)
        cardBossHud = findViewById(R.id.cardBossHud)
        tvBossHudName = findViewById(R.id.tvBossHudName)
        tvBossHudPhase = findViewById(R.id.tvBossHudPhase)
        tvBossHudHealth = findViewById(R.id.tvBossHudHealth)
        tvBossWarning = findViewById(R.id.tvBossWarning)

        // Boss Intro Overlay (Part 7)
        layoutBossIntroOverlay = findViewById(R.id.layoutBossIntroOverlay)
        viewBossIntroAvatar = findViewById(R.id.viewBossIntroAvatar)
        tvBossIntroTitle = findViewById(R.id.tvBossIntroTitle)
        tvBossIntroName = findViewById(R.id.tvBossIntroName)
        tvBossIntroSubtitle = findViewById(R.id.tvBossIntroSubtitle)
        tvBossIntroDesc = findViewById(R.id.tvBossIntroDesc)
        btnSkipBossIntro = findViewById(R.id.btnSkipBossIntro)

        tvVictorySummaryHealth = findViewById(R.id.tvVictorySummaryHealth)
        tvVictorySummaryReward = findViewById(R.id.tvVictorySummaryReward)
        tvDefeatSummaryWave = findViewById(R.id.tvDefeatSummaryWave)
    }

    private fun initEngine() {
        preferences = GamePreferences(this)
        economyManager = EconomyManager(preferences)
        baseManager = BaseEvolutionManager(preferences, economyManager)
        heroUnlockManager = HeroUnlockManager(preferences, economyManager, baseManager)
        shopManager = ShopManager(preferences, economyManager, baseManager, heroUnlockManager)

        stageNumber = intent.getIntExtra("extra_stage_number", 1)
        currentMatchRunId = "run_stage${stageNumber}_${System.currentTimeMillis()}"

        val stageWaves = StageConfig.getWavesForStage(stageNumber)
        val stageInfo = StageConfig.getStageInfo(stageNumber)

        val heroId = intent.getStringExtra("extra_hero_id") ?: heroUnlockManager.getSelectedHeroId()
        val selectedHero = Hero.getPlannedHeroes().find { it.id == heroId } ?: Hero.getPlannedHeroes().first()

        val totalBaseHealth = stageInfo.startingLives + shopManager.getTotalBaseHealthBonus()
        val towerMultiplier = shopManager.getTowerDamageMultiplier()
        val startingGold = stageInfo.startingGold + shopManager.getStartingGoldBonus()

        gameEngine = GameEngine(
            stageNumber = stageNumber,
            waves = stageWaves,
            maxBaseHealth = totalBaseHealth,
            listener = this,
            selectedHero = selectedHero,
            towerDamageMultiplier = towerMultiplier,
            startingGoldBonus = startingGold
        )
        gameEngine.deployHero(selectedHero, 0.42f, 0.48f)
        canvasView.gameEngine = gameEngine
        canvasView.setStage(stageNumber)
        canvasView.graphicQuality = preferences.graphicQuality

        tvHeroHudName.text = selectedHero.name
        tvHeroHudRarity.text = selectedHero.rarity.badgeLabel
        tvHeroHudRarity.setTextColor(selectedHero.rarity.colorInt)

        canvasView.onSpotClickedListener = { spot ->
            onSpotSelected(spot)
        }

        canvasView.onEmptyGroundClickedListener = {
            cancelSpotSelection()
        }

        // Initialize audio system (Part 8)
        SoundManager.init(preferences)
        SoundManager.startMusic()
    }

    private fun setupListeners() {
        // Pause Button
        btnPause.setOnClickListener {
            pauseGame()
        }

        // Hero Active Skills (Part 5)
        btnHeroSkill1.setOnClickListener {
            val castSuccess = gameEngine.castHeroSkill(0)
            if (castSuccess) {
                canvasView.invalidate()
                updateHud()
            }
        }

        btnHeroSkill2.setOnClickListener {
            val castSuccess = gameEngine.castHeroSkill(1)
            if (castSuccess) {
                canvasView.invalidate()
                updateHud()
            }
        }

        // Tower Construction Buttons
        btnBuildArcher.setOnClickListener {
            buildTowerOnSelectedSpot(TowerType.ARCHER_TOWER)
        }

        btnBuildMage.setOnClickListener {
            buildTowerOnSelectedSpot(TowerType.MAGE_TOWER)
        }

        btnBuildCannon.setOnClickListener {
            buildTowerOnSelectedSpot(TowerType.CANNON_TOWER)
        }

        btnCancelBuild.setOnClickListener {
            cancelSpotSelection()
        }

        btnCloseTowerInfo.setOnClickListener {
            cancelSpotSelection()
        }

        // Pause Overlay Buttons
        findViewById<Button>(R.id.btnResume).setOnClickListener {
            resumeGame()
        }

        findViewById<Button>(R.id.btnRestartFromPause).setOnClickListener {
            restartGame()
        }

        findViewById<Button>(R.id.btnExitFromPause).setOnClickListener {
            finish()
        }

        // Victory Overlay Buttons
        findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            restartGame()
        }

        findViewById<Button>(R.id.btnExitFromVictory).setOnClickListener {
            finish()
        }

        // Defeat Overlay Buttons
        findViewById<Button>(R.id.btnTryAgain).setOnClickListener {
            restartGame()
        }

        findViewById<Button>(R.id.btnExitFromDefeat).setOnClickListener {
            finish()
        }

        // Boss Intro Overlay Skip Button (Part 7)
        btnSkipBossIntro.setOnClickListener {
            gameEngine.skipBossIntro()
            layoutBossIntroOverlay.visibility = View.GONE
        }
    }

    private fun onSpotSelected(spot: TowerSpot) {
        val tower = spot.placedTower
        if (tower == null) {
            // Spot is empty: show construction choices
            layoutConstructTower.visibility = View.VISIBLE
            layoutTowerDetails.visibility = View.GONE
            val remaining = gameEngine.remainingTowersToBuild
            tvBuildPanelSubtitle.text = if (remaining > 0) {
                getString(R.string.build_panel_subtitle) + " ($remaining disponíveis)"
            } else {
                getString(R.string.tower_limit_reached)
            }
            btnBuildArcher.isEnabled = remaining > 0
            btnBuildMage.isEnabled = remaining > 0
            btnBuildCannon.isEnabled = remaining > 0
        } else {
            // Spot is occupied: show tower info
            layoutConstructTower.visibility = View.GONE
            layoutTowerDetails.visibility = View.VISIBLE
            tvTowerInfoStats.text = "${tower.name} • Dano: ${tower.damage} • Disparos: ${tower.totalShotsFired} • Dano Total: ${tower.totalDamageDealt}"
        }
        cardBuildPanel.visibility = View.VISIBLE
    }

    private fun buildTowerOnSelectedSpot(type: TowerType) {
        val spotId = canvasView.selectedSpotId ?: return
        val tower = gameEngine.buildTower(spotId, type)
        if (tower != null) {
            cancelSpotSelection()
            updateHud()
            canvasView.invalidate()
        }
    }

    private fun cancelSpotSelection() {
        canvasView.selectedSpotId = null
        cardBuildPanel.visibility = View.GONE
        canvasView.invalidate()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (layoutBossIntroOverlay.visibility == View.VISIBLE) {
                    gameEngine.skipBossIntro()
                    layoutBossIntroOverlay.visibility = View.GONE
                    return
                }
                if (cardBuildPanel.visibility == View.VISIBLE) {
                    cancelSpotSelection()
                    return
                }
                when (gameEngine.status) {
                    GameStatus.RUNNING -> pauseGame()
                    GameStatus.PAUSED -> resumeGame()
                    GameStatus.VICTORY, GameStatus.DEFEAT -> finish()
                    else -> finish()
                }
            }
        })
    }

    private fun startLoop() {
        if (!isLoopRunning && gameEngine.status == GameStatus.RUNNING) {
            isLoopRunning = true
            lastFrameTimeNanos = System.nanoTime()
            mainHandler.post(frameRunnable)
        }
    }

    private fun stopLoop() {
        isLoopRunning = false
        mainHandler.removeCallbacks(frameRunnable)
        lastFrameTimeNanos = 0L
    }

    private fun pauseGame() {
        cancelSpotSelection()
        gameEngine.pause()
        stopLoop()
        SoundManager.pauseMusic()
        layoutPauseOverlay.visibility = View.VISIBLE
    }

    private fun resumeGame() {
        layoutPauseOverlay.visibility = View.GONE
        gameEngine.resume()
        SoundManager.startMusic()
        startLoop()
    }

    private fun restartGame() {
        cancelSpotSelection()
        layoutPauseOverlay.visibility = View.GONE
        layoutVictoryOverlay.visibility = View.GONE
        layoutDefeatOverlay.visibility = View.GONE
        layoutBossIntroOverlay.visibility = View.GONE
        cardBossHud.visibility = View.GONE
        stopLoop()
        currentMatchRunId = "run_stage${stageNumber}_${System.currentTimeMillis()}"
        gameEngine.restart()
        startLoop()
        updateHud()
    }

    private fun updateHud() {
        tvHudLives.text = getString(R.string.gameplay_hud_lives, gameEngine.baseHealth, gameEngine.maxBaseHealth)
        tvHudWave.text = getString(R.string.gameplay_hud_wave, gameEngine.currentWaveNumber, gameEngine.totalWavesCount)
        tvHudEnemies.text = getString(R.string.gameplay_hud_enemies, gameEngine.totalEnemiesRemainingInWave)
        tvHudTowers.text = getString(R.string.gameplay_hud_towers, gameEngine.towersBuiltCount, gameEngine.maxTowersAllowed)

        // Dedicated Boss Top HUD update (Part 7)
        val boss = gameEngine.activeBoss
        if (boss != null && boss.isAlive && !boss.isIntroActive) {
            cardBossHud.visibility = View.VISIBLE
            tvBossHudName.text = "${boss.name} • ${boss.bossConfig.title}"
            tvBossHudPhase.text = "FASE ${boss.currentPhase}"
            val pct = (boss.healthFraction * 100).toInt()
            val statusNote = when {
                boss.isVulnerable -> " [VULNERÁVEL +50%]"
                boss.isProtected -> " [MANTO DAS SOMBRAS]"
                else -> ""
            }
            tvBossHudHealth.text = "HP: ${boss.currentHealth} / ${boss.maxHealth} ($pct%)$statusNote"

            if (boss.isTelegraphing && boss.activeTelegraphAttack != null) {
                tvBossWarning.visibility = View.VISIBLE
                tvBossWarning.text = boss.activeTelegraphAttack!!.telegraphWarningMessage
            } else {
                tvBossWarning.visibility = View.GONE
            }
        } else {
            cardBossHud.visibility = View.GONE
        }

        // Hero Skills HUD update (Part 5)
        val hero = gameEngine.deployedHero
        if (hero != null) {
            val skill1 = hero.getSkill(0)
            if (skill1 != null) {
                if (skill1.isReady) {
                    btnHeroSkill1.text = "${skill1.skill.name}\nPRONTO"
                    btnHeroSkill1.isEnabled = true
                    btnHeroSkill1.alpha = 1.0f
                } else {
                    val remaining = String.format(Locale.US, "%.1fs", skill1.currentCooldown)
                    btnHeroSkill1.text = "${skill1.skill.name}\n$remaining"
                    btnHeroSkill1.isEnabled = false
                    btnHeroSkill1.alpha = 0.6f
                }
            }

            val skill2 = hero.getSkill(1)
            if (skill2 != null) {
                if (skill2.isReady) {
                    btnHeroSkill2.text = "${skill2.skill.name}\nPRONTO"
                    btnHeroSkill2.isEnabled = true
                    btnHeroSkill2.alpha = 1.0f
                } else {
                    val remaining = String.format(Locale.US, "%.1fs", skill2.currentCooldown)
                    btnHeroSkill2.text = "${skill2.skill.name}\n$remaining"
                    btnHeroSkill2.isEnabled = false
                    btnHeroSkill2.alpha = 0.6f
                }
            }
        }
    }

    // =========================================================================
    // GameEngineListener Implementation
    // =========================================================================

    override fun onBaseHealthChanged(currentHealth: Int, maxHealth: Int) {
        tvHudLives.text = getString(R.string.gameplay_hud_lives, currentHealth, maxHealth)
    }

    override fun onWaveStarted(waveNumber: Int, totalWaves: Int, enemiesInWave: Int) {
        tvHudWave.text = getString(R.string.gameplay_hud_wave, waveNumber, totalWaves)
        tvHudEnemies.text = getString(R.string.gameplay_hud_enemies, enemiesInWave)
        updateHud()
    }

    override fun onTowerBuilt(tower: PlacedTower) {
        SoundManager.playTowerShoot(tower.type)
        val w = canvasView.width.toFloat()
        val h = canvasView.height.toFloat()
        if (w > 0f && h > 0f) {
            canvasView.spawnBuildingDust(tower.x * w, tower.y * h)
            canvasView.triggerScreenShake(intensity = 4f, duration = 0.15f)
        }
        updateHud()
    }

    override fun onEnemyDamaged(enemy: ActiveEnemy, damage: Int, isKilled: Boolean) {
        SoundManager.playEnemyHit()
        val w = canvasView.width.toFloat()
        val h = canvasView.height.toFloat()
        if (w > 0f && h > 0f) {
            canvasView.spawnSparks(enemy.x * w, enemy.y * h, Color.parseColor("#EF4444"), 4)
        }
    }

    override fun onEnemyDefeated(enemy: ActiveEnemy) {
        SoundManager.playEnemyDefeat()
        val w = canvasView.width.toFloat()
        val h = canvasView.height.toFloat()
        if (w > 0f && h > 0f) {
            canvasView.spawnEnemyDefeatSparks(enemy.x * w, enemy.y * h, enemy.visualType)
        }
    }

    override fun onHeroSkillCast(hero: DeployedHero, skill: HeroSkill, targetsHit: List<ActiveEnemy>) {
        SoundManager.playHeroSkill(hero.hero.id)
        val w = canvasView.width.toFloat()
        val h = canvasView.height.toFloat()
        if (w > 0f && h > 0f) {
            canvasView.spawnHeroSkillBurst(hero.x * w, hero.y * h, skill.vfxColor)
        }
        updateHud()
    }

    override fun onBossSpawned(boss: ActiveBoss) {
        updateHud()
    }

    override fun onBossIntroStarted(boss: ActiveBoss) {
        SoundManager.playBossIntro()
        viewBossIntroAvatar.setEnemyType(boss.visualType)
        tvBossIntroTitle.text = "⚠️ CHEFE APROXIMA-SE ⚠️"
        tvBossIntroName.text = boss.bossConfig.name
        tvBossIntroSubtitle.text = boss.bossConfig.title
        tvBossIntroDesc.text = boss.bossConfig.introCinematicMessage.ifBlank { boss.bossConfig.description }
        layoutBossIntroOverlay.visibility = View.VISIBLE
        updateHud()
    }

    override fun onBossIntroFinished(boss: ActiveBoss) {
        layoutBossIntroOverlay.visibility = View.GONE
        updateHud()
    }

    override fun onBossPhaseChanged(boss: ActiveBoss, newPhase: Int) {
        updateHud()
    }

    override fun onBossAttackTelegraphed(boss: ActiveBoss, attack: BossAttack) {
        updateHud()
    }

    override fun onBossAttackExecuted(boss: ActiveBoss, attack: BossAttack) {
        val w = canvasView.width.toFloat()
        val h = canvasView.height.toFloat()
        canvasView.triggerScreenShake(intensity = 10f, duration = 0.30f)
        if (w > 0f && h > 0f) {
            canvasView.spawnDebris(boss.x * w, boss.y * h, Color.parseColor("#F97316"), count = 6)
        }
        updateHud()
    }

    override fun onBossDefeated(boss: ActiveBoss) {
        SoundManager.playBossDefeat()
        val w = canvasView.width.toFloat()
        val h = canvasView.height.toFloat()
        if (w > 0f && h > 0f) {
            canvasView.spawnHeroSkillBurst(boss.x * w, boss.y * h, Color.parseColor("#F59E0B"))
        }
        cardBossHud.visibility = View.GONE
        updateHud()
    }

    override fun onStatusChanged(status: GameStatus) {
        when (status) {
            GameStatus.VICTORY -> {
                cancelSpotSelection()
                stopLoop()
                SoundManager.playVictory()
                val w = canvasView.width.toFloat()
                val h = canvasView.height.toFloat()
                if (w > 0f && h > 0f) {
                    canvasView.spawnVictoryFireworks(w, h)
                }
                canvasView.invalidate()
                tvVictorySummaryHealth.text = getString(R.string.victory_summary_health, gameEngine.baseHealth, gameEngine.maxBaseHealth)

                // Mark stage as completed and advance progression
                preferences.completedStages = preferences.completedStages + stageNumber
                if (stageNumber + 1 > preferences.highestUnlockedStage) {
                    preferences.highestUnlockedStage = stageNumber + 1
                }

                // Check boss defeat
                val bossConfig = StageConfig.getBossForStage(stageNumber)
                val hasBoss = bossConfig != null
                val bossId = bossConfig?.type?.name
                val customBossReward = bossConfig?.coinReward

                // Claim victory economy reward exactly once per run
                val reward = economyManager.claimVictoryReward(
                    runId = currentMatchRunId,
                    livesRemaining = gameEngine.baseHealth,
                    maxLives = gameEngine.maxBaseHealth,
                    defeatedBoss = hasBoss,
                    bossId = bossId,
                    customBossBonus = customBossReward,
                    stageNumber = stageNumber
                )
                if (reward != null) {
                    val parts = mutableListOf<String>()
                    parts.add("+${reward.baseReward} Base")
                    if (reward.perfectClearBonus > 0) parts.add("+${reward.perfectClearBonus} Perfeito")
                    if (reward.bossDefeatBonus > 0) parts.add("+${reward.bossDefeatBonus} Chefe")
                    if (reward.firstClearBonus > 0) parts.add("+${reward.firstClearBonus} 1ª Vitória")
                    tvVictorySummaryReward.text = "💰 Recompensa: +${reward.totalCoins} Moedas (${parts.joinToString(", ")})"
                } else {
                    tvVictorySummaryReward.text = "💰 Recompensa desta partida já foi registrada."
                }

                layoutVictoryOverlay.visibility = View.VISIBLE
            }
            GameStatus.DEFEAT -> {
                cancelSpotSelection()
                stopLoop()
                SoundManager.playDefeat()
                canvasView.invalidate()
                tvDefeatSummaryWave.text = getString(R.string.defeat_summary_waves, gameEngine.currentWaveNumber)
                layoutDefeatOverlay.visibility = View.VISIBLE
            }
            GameStatus.PAUSED -> {
                stopLoop()
            }
            GameStatus.RUNNING -> {
                startLoop()
            }
            else -> {}
        }
    }

    // =========================================================================
    // Lifecycle Management
    // =========================================================================

    override fun onPause() {
        super.onPause()
        SoundManager.pauseMusic()
        if (gameEngine.status == GameStatus.RUNNING) {
            pauseGame()
        } else {
            stopLoop()
        }
    }

    override fun onResume() {
        super.onResume()
        if (gameEngine.status == GameStatus.RUNNING) {
            SoundManager.startMusic()
            startLoop()
        }
    }

    override fun onDestroy() {
        stopLoop()
        SoundManager.stopMusic()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
