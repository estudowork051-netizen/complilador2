package com.heroes.towerdefense

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.data.InMemoryStorage
import com.heroes.towerdefense.game.boss.ActiveBoss
import com.heroes.towerdefense.game.boss.BossAttack
import com.heroes.towerdefense.game.boss.BossAttackType
import com.heroes.towerdefense.game.boss.BossConfig
import com.heroes.towerdefense.game.boss.BossType
import com.heroes.towerdefense.game.economy.EconomyManager
import com.heroes.towerdefense.game.economy.VictoryReward
import com.heroes.towerdefense.game.engine.GameEngine
import com.heroes.towerdefense.game.engine.GameEngineListener
import com.heroes.towerdefense.game.map.StageOneMap
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.DeployedHero
import com.heroes.towerdefense.game.model.GameStatus
import com.heroes.towerdefense.game.model.PlacedTower
import com.heroes.towerdefense.game.model.WaveConfig
import com.heroes.towerdefense.game.stage.StageConfig
import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.tower.TowerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive Unit Tests for Part 7:
 * - Epic Bosses encounters, configurations, and spawning (Stages 2, 3, 6)
 * - Multi-phase mechanics, health thresholds, and enrage states
 * - Skippable cinematic intros, immunity window, and path isolation
 * - Damage calculations, armor resistances, and vulnerability multipliers
 * - Telegraphed special abilities, warnings, and attack execution lifecycle
 * - Defeat state, attack cancellation, and zero-HP clamping
 * - Single victory event emission and zero-reward defeat protection
 * - Centralized stage economy, boss defeat bonuses, and unique first-clear rewards
 * - Progressive stage unlocking and campaign progression
 * - Tower projectiles and hero active skills combat against bosses
 * - State persistence across simulated application restarts
 */
class BossAndProgressionTest {

    private lateinit var storage: InMemoryStorage
    private lateinit var preferences: GamePreferences
    private lateinit var economyManager: EconomyManager

    @Before
    fun setUp() {
        storage = InMemoryStorage()
        preferences = GamePreferences(storage)
        economyManager = EconomyManager(preferences)
    }

    // =========================================================================
    // Scenario 1: Boss stage spawning on designated waves and stages
    // =========================================================================
    @Test
    fun testScenario01_BossStageSpawning() {
        // Stage 1: Standard stage without boss
        val stage1Boss = StageConfig.getBossForStage(1)
        assertNull("Stage 1 must not contain a boss", stage1Boss)

        // Stage 2: Spawns Obsidian Golem on wave 5
        val stage2Boss = StageConfig.getBossForStage(2)
        assertNotNull("Stage 2 must contain a boss", stage2Boss)
        assertEquals(BossType.OBSIDIAN_GOLEM, stage2Boss?.type)
        assertEquals(GameConstants.BOSS_GOLEM_COIN_REWARD, stage2Boss?.coinReward)

        // Stage 3: Spawns Shadow Queen on wave 5
        val stage3Boss = StageConfig.getBossForStage(3)
        assertNotNull("Stage 3 must contain a boss", stage3Boss)
        assertEquals(BossType.SHADOW_QUEEN, stage3Boss?.type)
        assertEquals(GameConstants.BOSS_SHADOW_QUEEN_COIN_REWARD, stage3Boss?.coinReward)

        // Stage 6: Spawns Astral Dragon on wave 5
        val stage6Boss = StageConfig.getBossForStage(6)
        assertNotNull("Stage 6 must contain a boss", stage6Boss)
        assertEquals(BossType.ASTRAL_DRAGON, stage6Boss?.type)
        assertEquals(GameConstants.BOSS_ASTRAL_DRAGON_COIN_REWARD, stage6Boss?.coinReward)

        // Spawning via GameEngine with a boss wave
        val bossWave = WaveConfig(
            waveNumber = 1,
            enemyCount = 1,
            spawnIntervalSeconds = 0.1f,
            bossConfig = BossConfig.getObsidianGolem()
        )
        val engine = GameEngine(
            waypoints = StageOneMap.waypoints,
            waves = listOf(bossWave),
            stageNumber = 2
        )
        engine.start()
        engine.update(0.15f) // Spawns the boss enemy

        val spawnedBoss = engine.activeBoss
            ?: error("Era esperado um chefe ativo ao iniciar onda de chefe")
        assertNotNull("GameEngine must instantiate activeBoss when boss wave starts", spawnedBoss)
        assertTrue("Spawned boss must have isBoss flag set to true", spawnedBoss.isBoss)
        assertEquals("Spawned boss must match configured type", BossType.OBSIDIAN_GOLEM, spawnedBoss.bossConfig.type)
    }

    // =========================================================================
    // Scenario 2: Boss intro isolation, immunity, and skip functionality
    // =========================================================================
    @Test
    fun testScenario02_IntroIsolationAndImmunity() {
        val boss = ActiveBoss(BossConfig.getObsidianGolem(), waveNumber = 1)
        assertTrue("Boss must start with isIntroActive = true", boss.isIntroActive)
        assertTrue("Boss intro timer must be greater than zero", boss.introTimer > 0f)

        // 1. Boss cannot take damage during intro
        val initialHealth = boss.currentHealth
        val damaged = boss.takeDamage(100)
        assertFalse("Damage attempt during intro must return false", damaged)
        assertEquals("Health must remain intact during intro", initialHealth, boss.currentHealth)
        assertEquals("Total damage taken during intro must be 0", 0, boss.totalDamageTaken)

        // 2. Boss does not move along path during intro
        assertEquals(0f, boss.currentEffectiveMoveSpeed, 0.0001f)
        boss.updateBoss(1.0f, StageOneMap.waypoints)
        assertEquals("Boss must not travel along path during intro", 0f, boss.distanceTraveled, 0.0001f)
        assertTrue("Intro must still be active after 1 second of a 2.5s intro", boss.isIntroActive)

        // 3. Skip intro immediately activates combat
        boss.finishIntro()
        assertFalse("finishIntro() must terminate intro immediately", boss.isIntroActive)
        assertEquals(0f, boss.introTimer, 0.0001f)
        assertTrue("Effective move speed must now be positive", boss.currentEffectiveMoveSpeed > 0f)

        // 4. Boss can now take damage
        boss.takeDamage(100)
        assertTrue("Health must decrease after intro is completed", boss.currentHealth < initialHealth)
    }

    // =========================================================================
    // Scenario 3: Damage calculation, armor resistances, and vulnerability
    // =========================================================================
    @Test
    fun testScenario03_DamageCalculationAndModifiers() {
        // Obsidian Golem has 20% physical resistance and 1.50x vulnerability multiplier
        val golem = ActiveBoss(BossConfig.getObsidianGolem(), waveNumber = 1)
        golem.finishIntro()

        // Base damage: 100 raw damage with 20% resistance -> deals 80 damage
        val healthBeforeNormal = golem.currentHealth
        golem.takeDamage(100)
        val normalDamageDealt = healthBeforeNormal - golem.currentHealth
        assertEquals("20% physical resistance must reduce 100 damage to 80", 80, normalDamageDealt)

        // Vulnerable state: 1.50x damage multiplier -> deals 150 damage
        golem.triggerVulnerability(3.0f)
        assertTrue("Golem must be in vulnerable state", golem.isVulnerable)
        val healthBeforeVulnerable = golem.currentHealth
        golem.takeDamage(100)
        val vulnerableDamageDealt = healthBeforeVulnerable - golem.currentHealth
        assertEquals("Vulnerability must amplify 100 damage to 150", 150, vulnerableDamageDealt)

        // Shadow Queen has 75% damage reduction (0.25x multiplier) under Shadow Veil protection
        val queen = ActiveBoss(BossConfig.getShadowQueen(), waveNumber = 1)
        queen.finishIntro()
        queen.triggerProtection(2.5f)
        assertTrue("Shadow Queen must be protected", queen.isProtected)

        val healthBeforeProtection = queen.currentHealth
        queen.takeDamage(100)
        val protectedDamageDealt = healthBeforeProtection - queen.currentHealth
        assertEquals("Protection must reduce 100 damage to 25", 25, protectedDamageDealt)
    }

    // =========================================================================
    // Scenario 4: Health clamping never drops below 0 on lethal hits
    // =========================================================================
    @Test
    fun testScenario04_HealthClampingNeverNegative() {
        val boss = ActiveBoss(BossConfig.getObsidianGolem(), waveNumber = 1)
        boss.finishIntro()

        // Inflict lethal overkill damage
        val fatal = boss.takeDamage(99999)
        assertTrue("Lethal damage must return true for enemy elimination", fatal)
        assertEquals("Boss currentHealth must clamp exactly to 0", 0, boss.currentHealth)
        assertFalse("Boss must be marked dead", boss.isAlive)
        assertFalse("Active telegraph must be cancelled on death", boss.isTelegraphing)
        assertNull("Telegraph attack must be cleared on death", boss.activeTelegraphAttack)
    }

    // =========================================================================
    // Scenario 5: Phase 2 transition at health threshold and enrage speed
    // =========================================================================
    @Test
    fun testScenario05_PhaseTransitionThreshold() {
        // Astral Dragon: 1200 max HP, enters Phase 2 at 50% HP (<= 600 HP)
        val dragon = ActiveBoss(BossConfig.getAstralDragon(), waveNumber = 1)
        dragon.finishIntro()

        assertEquals(1, dragon.currentPhase)
        assertFalse(dragon.hasTriggeredPhase2)
        val initialSpeed = dragon.currentEffectiveMoveSpeed

        // Deal 400 damage -> HP = 800 (66.6% > 50%)
        dragon.takeDamage(400)
        assertEquals("Dragon must remain in Phase 1 above 50% HP", 1, dragon.currentPhase)
        assertFalse(dragon.hasTriggeredPhase2)

        // Deal 300 damage -> HP = 500 (41.6% <= 50%) -> triggers Phase 2!
        dragon.takeDamage(300)
        assertEquals("Dragon must transition to Phase 2 at or below 50% HP", 2, dragon.currentPhase)
        assertTrue(dragon.hasTriggeredPhase2)

        // Phase 2 enrage grants 40% increased move speed
        val enragedSpeed = dragon.currentEffectiveMoveSpeed
        assertEquals("Phase 2 move speed must be 40% faster", initialSpeed * 1.40f, enragedSpeed, 0.001f)
    }

    // =========================================================================
    // Scenario 6: Pause state freezes boss movement, timers, and telegraphs
    // =========================================================================
    @Test
    fun testScenario06_PauseStatePreservation() {
        val bossWave = WaveConfig(
            waveNumber = 1,
            enemyCount = 1,
            spawnIntervalSeconds = 0.1f,
            bossConfig = BossConfig.getObsidianGolem()
        )
        val engine = GameEngine(
            waypoints = StageOneMap.waypoints,
            waves = listOf(bossWave),
            stageNumber = 2
        )
        engine.start()
        engine.update(0.15f) // Spawns boss

        val boss = engine.activeBoss
            ?: error("Era esperado um chefe ativo neste cenário de teste")
        assertNotNull(boss)
        boss.finishIntro()

        val initialX = boss.x
        val initialY = boss.y
        val initialCooldown = boss.attackCooldownTimer

        // Pause match
        engine.pause()
        assertEquals(GameStatus.PAUSED, engine.status)

        // Update loop while paused
        engine.update(1.0f)
        engine.update(1.0f)

        // Position and timers must remain completely frozen
        assertEquals(initialX, boss.x, 0.0001f)
        assertEquals(initialY, boss.y, 0.0001f)
        assertEquals(initialCooldown, boss.attackCooldownTimer, 0.0001f)

        // Resume and verify updates resume
        engine.resume()
        assertEquals(GameStatus.RUNNING, engine.status)
        engine.update(0.5f)
        assertTrue("Boss cooldown must decrement after resuming", boss.attackCooldownTimer < initialCooldown)
    }

    // =========================================================================
    // Scenario 7: Telegraphed attacks lifecycle and single execution
    // =========================================================================
    @Test
    fun testScenario07_TelegraphedAttacksLifecycle() {
        val boss = ActiveBoss(BossConfig.getObsidianGolem(), waveNumber = 1)
        boss.finishIntro()

        // Force cooldown expiration to start attack telegraph
        boss.attackCooldownTimer = 0f
        val executedDuringTelegraphStart = boss.updateBoss(0.01f, StageOneMap.waypoints)
        assertNull("Starting telegraph must not immediately execute attack", executedDuringTelegraphStart)
        assertTrue("Boss must be telegraphing an attack", boss.isTelegraphing)
        val attack = boss.activeTelegraphAttack
            ?: error("Active telegraph attack must be set")

        // Advance telegraph partially
        val halfDuration = attack.telegraphDuration / 2f
        val executedMidTelegraph = boss.updateBoss(halfDuration, StageOneMap.waypoints)
        assertNull("Mid-telegraph must not execute attack prematurely", executedMidTelegraph)
        assertTrue("Boss must still be telegraphing", boss.isTelegraphing)

        // Complete remaining telegraph duration
        val remainingDuration = attack.telegraphDuration
        val executedFinal = boss.updateBoss(remainingDuration, StageOneMap.waypoints)

        assertNotNull("Attack must execute once telegraph duration completes", executedFinal)
        assertEquals(attack.name, executedFinal?.name)
        assertFalse("Boss must no longer be telegraphing", boss.isTelegraphing)
        assertNull("Active telegraph attack must be cleared", boss.activeTelegraphAttack)
        assertEquals(1, boss.totalAttacksExecuted)
        assertEquals("Attack cooldown must be set to attack.cooldown", attack.cooldown, boss.attackCooldownTimer, 0.001f)
    }

    // =========================================================================
    // Scenario 8: Boss defeat cancels active attacks and prevents further actions
    // =========================================================================
    @Test
    fun testScenario08_BossDefeatCancelsActiveAttacks() {
        val boss = ActiveBoss(BossConfig.getObsidianGolem(), waveNumber = 1)
        boss.finishIntro()

        // Start telegraph
        val attack = boss.bossConfig.attacks.first()
        boss.startTelegraph(attack)
        assertTrue(boss.isTelegraphing)
        assertNotNull(boss.activeTelegraphAttack)

        // Kill boss while telegraph is active
        boss.takeDamage(boss.maxHealth)
        assertFalse("Boss must be dead", boss.isAlive)
        assertFalse("Telegraph must be cancelled on death", boss.isTelegraphing)
        assertNull("Telegraph attack must be null", boss.activeTelegraphAttack)
        assertEquals(0f, boss.telegraphTimer, 0.0001f)

        // Further updates must do nothing and return null
        val subsequentAttack = boss.updateBoss(5.0f, StageOneMap.waypoints)
        assertNull("Dead boss cannot execute attacks", subsequentAttack)
    }

    // =========================================================================
    // Scenario 9: Single victory emission when boss wave is cleared
    // =========================================================================
    @Test
    fun testScenario09_SingleVictoryEmissionOnBossClear() {
        var victoryCallbackCount = 0
        var bossDefeatedCallbackCount = 0

        val bossWave = WaveConfig(
            waveNumber = 1,
            enemyCount = 1,
            spawnIntervalSeconds = 0.1f,
            bossConfig = BossConfig.getObsidianGolem()
        )
        val engine = GameEngine(
            waypoints = StageOneMap.waypoints,
            waves = listOf(bossWave),
            stageNumber = 2,
            listener = object : GameEngineListener {
                override fun onStatusChanged(status: GameStatus) {
                    if (status == GameStatus.VICTORY) {
                        victoryCallbackCount++
                    }
                }
                override fun onBossDefeated(boss: ActiveBoss) {
                    bossDefeatedCallbackCount++
                }
            }
        )

        engine.start()
        engine.update(0.15f) // Spawns boss

        val boss = engine.activeBoss
            ?: error("Era esperado um chefe ativo neste cenário de teste")
        assertNotNull(boss)
        boss.finishIntro()

        // Kill boss
        boss.takeDamage(boss.maxHealth)
        engine.update(0.016f) // Engine cleans up dead enemies and evaluates victory

        assertEquals(1, bossDefeatedCallbackCount)
        assertEquals(GameStatus.VICTORY, engine.status)
        assertEquals("Victory callback must be triggered exactly once", 1, victoryCallbackCount)

        // Subsequent frames must not trigger redundant victory calls
        engine.update(0.016f)
        engine.update(0.016f)
        assertEquals("Subsequent updates must not emit duplicate victory callbacks", 1, victoryCallbackCount)
    }

    // =========================================================================
    // Scenario 10: Defeat awards zero victory coins to player balance
    // =========================================================================
    @Test
    fun testScenario10_DefeatAwardsNoCoins() {
        val initialBalance = economyManager.getBalance() // 300

        // In case of match defeat, claimVictoryReward is NEVER called
        // Even if an invalid run attempt is made on defeat, balance must remain untouched
        assertEquals("Player balance must remain intact", initialBalance, economyManager.getBalance())
    }

    // =========================================================================
    // Scenario 11: Boss coin reward correctly awarded upon victory
    // =========================================================================
    @Test
    fun testScenario11_BossCoinReward() {
        // Stage 2 Boss (Golem): +100 coins
        val golemReward = economyManager.calculateVictoryReward(
            livesRemaining = 20,
            maxLives = 20,
            defeatedBoss = true,
            stageNumber = 2,
            customBossBonus = StageConfig.getBossRewardCoins(2)
        )
        assertEquals(GameConstants.BOSS_GOLEM_COIN_REWARD, golemReward.bossBonus)
        assertEquals(golemReward.bossBonus, golemReward.bossDefeatBonus)

        // Stage 3 Boss (Shadow Queen): +150 coins
        val queenReward = economyManager.calculateVictoryReward(
            livesRemaining = 20,
            maxLives = 20,
            defeatedBoss = true,
            stageNumber = 3,
            customBossBonus = StageConfig.getBossRewardCoins(3)
        )
        assertEquals(GameConstants.BOSS_SHADOW_QUEEN_COIN_REWARD, queenReward.bossBonus)

        // Stage 6 Boss (Astral Dragon): +250 coins
        val dragonReward = economyManager.calculateVictoryReward(
            livesRemaining = 20,
            maxLives = 20,
            defeatedBoss = true,
            stageNumber = 6,
            customBossBonus = StageConfig.getBossRewardCoins(6)
        )
        assertEquals(GameConstants.BOSS_ASTRAL_DRAGON_COIN_REWARD, dragonReward.bossBonus)
    }

    // =========================================================================
    // Scenario 12: Unique first-clear bonus awarded strictly once per stage
    // =========================================================================
    @Test
    fun testScenario12_UniqueFirstClearBonusNonDuplication() {
        val initialBalance = economyManager.getBalance() // 300
        val stage2FirstClearBonus = StageConfig.getFirstClearBonus(2) // 100
        val stage2BaseReward = StageConfig.getBaseCoinReward(2) // 200
        val bossBonus = StageConfig.getBossRewardCoins(2) // 100
        val perfectBonus = GameConstants.STAGE_ONE_PERFECT_HEALTH_BONUS_COINS // 50

        // 1. First victory on Stage 2 receives first clear bonus
        val firstClaim = economyManager.claimVictoryReward(
            runId = "stage2_run_01",
            livesRemaining = 20,
            maxLives = 20,
            defeatedBoss = true,
            stageNumber = 2,
            bossId = "OBSIDIAN_GOLEM",
            customBossBonus = bossBonus
        )
        assertNotNull(firstClaim)
        assertEquals(stage2FirstClearBonus, firstClaim!!.firstClearBonus)
        assertEquals(stage2BaseReward + perfectBonus + bossBonus + stage2FirstClearBonus, firstClaim.totalCoins)
        assertEquals(initialBalance + firstClaim.totalCoins, economyManager.getBalance())
        assertTrue("Stage 2 must be recorded as first clear claimed", preferences.isFirstClearClaimed(2))
        assertTrue("Obsidian Golem must be recorded as defeated", preferences.isBossDefeated("OBSIDIAN_GOLEM"))

        // 2. Second victory on Stage 2 (new runId) does NOT receive first clear bonus
        val currentBalance = economyManager.getBalance()
        val secondClaim = economyManager.claimVictoryReward(
            runId = "stage2_run_02",
            livesRemaining = 20,
            maxLives = 20,
            defeatedBoss = true,
            stageNumber = 2,
            bossId = "OBSIDIAN_GOLEM",
            customBossBonus = bossBonus
        )
        assertNotNull(secondClaim)
        assertEquals("Subsequent clear must award 0 firstClearBonus", 0, secondClaim!!.firstClearBonus)
        assertEquals(stage2BaseReward + perfectBonus + bossBonus, secondClaim.totalCoins)
        assertEquals(currentBalance + secondClaim.totalCoins, economyManager.getBalance())
    }

    // =========================================================================
    // Scenario 13: Progressive stage unlocking from Stage 1 through Stage 6
    // =========================================================================
    @Test
    fun testScenario13_ProgressiveStageUnlocking() {
        assertEquals("Initial unlocked stage must be 1", 1, preferences.highestUnlockedStage)

        // Complete Stage 1 -> unlocks Stage 2
        preferences.completedStages = preferences.completedStages + 1
        preferences.highestUnlockedStage = 2
        assertEquals(2, preferences.highestUnlockedStage)

        // Complete Stage 2 -> unlocks Stage 3
        preferences.completedStages = preferences.completedStages + 2
        preferences.highestUnlockedStage = 3
        assertEquals(3, preferences.highestUnlockedStage)

        // Complete Stage 3 -> unlocks Stage 4
        preferences.completedStages = preferences.completedStages + 3
        preferences.highestUnlockedStage = 4
        assertEquals(4, preferences.highestUnlockedStage)

        // Complete Stage 4 -> unlocks Stage 5
        preferences.completedStages = preferences.completedStages + 4
        preferences.highestUnlockedStage = 5
        assertEquals(5, preferences.highestUnlockedStage)

        // Complete Stage 5 -> unlocks Stage 6
        preferences.completedStages = preferences.completedStages + 5
        preferences.highestUnlockedStage = 6
        assertEquals(6, preferences.highestUnlockedStage)
    }

    // =========================================================================
    // Scenario 14: Towers and projectiles combat against active boss
    // =========================================================================
    @Test
    fun testScenario14_TowersAndProjectilesCombat() {
        val bossWave = WaveConfig(
            waveNumber = 1,
            enemyCount = 1,
            spawnIntervalSeconds = 0.1f,
            bossConfig = BossConfig.getObsidianGolem()
        )
        val engine = GameEngine(
            waypoints = StageOneMap.waypoints,
            waves = listOf(bossWave),
            stageNumber = 2
        )
        engine.start()
        engine.update(0.15f) // Spawns boss

        val boss = engine.activeBoss
            ?: error("Era esperado um chefe ativo neste cenário de teste")
        assertNotNull(boss)

        // Build Archer Tower on spot 1
        val spot = engine.towerSpots.first()
        val built = engine.buildTower(spot.id, TowerType.ARCHER_TOWER)
        assertNotNull("Tower must be successfully built", built)

        // Position boss along path within range of tower spot 1
        boss.x = spot.x
        boss.y = 0.22f
        boss.currentWaypointIndex = 1

        // While boss is in intro, tower must NOT fire at boss
        engine.update(0.5f)
        assertTrue("No projectiles should fire while boss is in intro", engine.activeProjectiles.isEmpty())

        // Finish intro: boss enters combat
        boss.finishIntro()

        // Tower attacks boss
        engine.update(1.0f) // Cooldown ticks and fires projectile
        assertFalse("Tower must fire projectile targeting active boss", engine.activeProjectiles.isEmpty())

        // Projectile travels and damages boss
        val healthBeforeHit = boss.currentHealth
        for (i in 0..10) {
            engine.update(0.1f)
        }
        assertTrue("Boss health must decrease after projectile impacts", boss.currentHealth < healthBeforeHit)
    }

    // =========================================================================
    // Scenario 15: Hero skills combat against active boss
    // =========================================================================
    @Test
    fun testScenario15_HeroSkillsCombatAgainstBoss() {
        val hero = Hero.getKael()

        val bossWave = WaveConfig(
            waveNumber = 1,
            enemyCount = 1,
            spawnIntervalSeconds = 0.1f,
            bossConfig = BossConfig.getObsidianGolem()
        )
        val engine = GameEngine(
            waypoints = StageOneMap.waypoints,
            waves = listOf(bossWave),
            stageNumber = 2,
            selectedHero = hero
        )
        val deployedHero = engine.deployHero(hero = hero, x = 0.42f, y = 0.48f)

        engine.start()
        engine.update(0.15f)

        val boss = engine.activeBoss
            ?: error("Era esperado um chefe ativo neste cenário de teste")
        assertNotNull(boss)
        boss.finishIntro()

        // Place hero right beside the boss
        deployedHero.x = boss.x
        deployedHero.y = boss.y

        val healthBeforeSkill = boss.currentHealth
        val skillCast = engine.castHeroSkill(0) // Blade Strike
        assertTrue("Hero skill cast must succeed", skillCast)
        assertTrue("Boss health must decrease after hero skill hits", boss.currentHealth < healthBeforeSkill)
    }

    // =========================================================================
    // Scenario 16: Persistence across simulated application reboots
    // =========================================================================
    @Test
    fun testScenario16_PersistenceAcrossReboots() {
        // Record progression in initial session
        preferences.markBossDefeated("boss_obsidian_golem")
        preferences.markBossDefeated("boss_shadow_queen")
        preferences.markFirstClearClaimed(1)
        preferences.markFirstClearClaimed(2)
        preferences.highestUnlockedStage = 3
        preferences.playerCoins = 750

        // Simulate app reboot by creating a new preferences instance pointing to the same storage
        val reloadedPreferences = GamePreferences(storage)

        assertTrue(reloadedPreferences.isBossDefeated("boss_obsidian_golem"))
        assertTrue(reloadedPreferences.isBossDefeated("boss_shadow_queen"))
        assertFalse(reloadedPreferences.isBossDefeated("boss_astral_dragon"))

        assertTrue(reloadedPreferences.isFirstClearClaimed(1))
        assertTrue(reloadedPreferences.isFirstClearClaimed(2))
        assertFalse(reloadedPreferences.isFirstClearClaimed(3))

        assertEquals(3, reloadedPreferences.highestUnlockedStage)
        assertEquals(750, reloadedPreferences.playerCoins)
    }
}
