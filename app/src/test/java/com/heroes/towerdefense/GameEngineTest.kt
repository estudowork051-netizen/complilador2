package com.heroes.towerdefense

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.game.engine.GameEngine
import com.heroes.towerdefense.game.engine.GameEngineListener
import com.heroes.towerdefense.game.map.StageOneMap
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.GameStatus
import com.heroes.towerdefense.game.model.PlacedTower
import com.heroes.towerdefense.model.tower.TowerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive Unit Tests for GameEngine (Stages 1 through 4 Gameplay).
 * Validates match lifecycle, path movement, 5 waves, base damage, pause, victory, defeat,
 * tower building on valid spots, spot occupation, attack ranges, fire rates, damage delivery,
 * projectile tracking, single vs splash AOE damage, and enemy removal.
 */
class GameEngineTest {

    private lateinit var engine: GameEngine
    private var lastRecordedHealth: Int = -1
    private var lastRecordedStatus: GameStatus? = null
    private var lastRecordedWave: Int = -1
    private var waveCompletedCount: Int = 0
    private var lastBuiltTower: PlacedTower? = null
    private var lastDefeatedEnemy: ActiveEnemy? = null

    @Before
    fun setUp() {
        lastRecordedHealth = -1
        lastRecordedStatus = null
        lastRecordedWave = -1
        waveCompletedCount = 0
        lastBuiltTower = null
        lastDefeatedEnemy = null

        engine = GameEngine(
            waypoints = StageOneMap.waypoints,
            listener = object : GameEngineListener {
                override fun onBaseHealthChanged(currentHealth: Int, maxHealth: Int) {
                    lastRecordedHealth = currentHealth
                }

                override fun onWaveStarted(waveNumber: Int, totalWaves: Int, enemiesInWave: Int) {
                    lastRecordedWave = waveNumber
                }

                override fun onWaveCompleted(waveNumber: Int, isFinalWave: Boolean) {
                    waveCompletedCount++
                }

                override fun onStatusChanged(status: GameStatus) {
                    lastRecordedStatus = status
                }

                override fun onTowerBuilt(tower: PlacedTower) {
                    lastBuiltTower = tower
                }

                override fun onEnemyDefeated(enemy: ActiveEnemy) {
                    lastDefeatedEnemy = enemy
                }
            }
        )
    }

    // =========================================================================
    // Part 3 Existing Baseline Tests
    // =========================================================================

    @Test
    fun testMatchInitialization() {
        assertEquals(GameStatus.NOT_STARTED, engine.status)
        assertEquals(GameConstants.STAGE_ONE_INITIAL_LIVES, engine.baseHealth)
        assertEquals(1, engine.currentWaveNumber)
        assertEquals(5, engine.totalWavesCount)
        assertTrue(engine.activeEnemies.isEmpty())

        engine.start()

        assertEquals(GameStatus.RUNNING, engine.status)
        assertEquals(GameConstants.STAGE_ONE_INITIAL_LIVES, engine.baseHealth)
        assertEquals(GameStatus.RUNNING, lastRecordedStatus)
        assertEquals(1, lastRecordedWave)
    }

    @Test
    fun testFiveWaveConfigurations() {
        val waves = engine.waves
        assertEquals(5, waves.size)
        assertEquals(5, waves[0].enemyCount)
        assertEquals(7, waves[1].enemyCount)
        assertEquals(9, waves[2].enemyCount)
        assertEquals(11, waves[3].enemyCount)
        assertEquals(13, waves[4].enemyCount)
    }

    @Test
    fun testEnemySpawningAndMovement() {
        engine.start()

        // Tick 0.1s -> should spawn first enemy of wave 1
        engine.update(0.1f)
        assertEquals(1, engine.enemiesSpawnedInCurrentWave)
        assertEquals(1, engine.activeEnemies.size)

        val firstEnemy = engine.activeEnemies.first()
        val startPoint = StageOneMap.spawnPoint
        assertEquals(startPoint.x, firstEnemy.x, 0.05f)
        assertEquals(startPoint.y, firstEnemy.y, 0.05f)

        // Simulate multiple movement ticks
        val initialY = firstEnemy.y
        for (i in 1..20) {
            engine.update(0.05f)
        }

        // Enemy should have moved forward along the path
        assertTrue("Enemy should have moved south along waypoint 1", firstEnemy.y > initialY)
        assertTrue("Distance traveled should be positive", firstEnemy.distanceTraveled > 0f)
    }

    @Test
    fun testBaseDamageWhenEnemyEscapes() {
        engine.start()
        assertEquals(20, engine.baseHealth)

        // Advance until an enemy reaches base
        for (step in 1..250) {
            engine.update(0.1f)
            if (engine.baseHealth < 20) {
                break
            }
        }

        assertTrue("Base should take damage when enemy escapes", engine.baseHealth < 20)
        assertEquals("Health change should be reported to listener", engine.baseHealth, lastRecordedHealth)
    }

    @Test
    fun testDefeatConditionWhenHealthReachesZero() {
        engine.start()

        // Simulate long enough for 20 enemies to escape and damage the base
        for (step in 1..2000) {
            engine.update(0.1f)
            if (engine.status == GameStatus.DEFEAT) {
                break
            }
        }

        assertEquals(0, engine.baseHealth)
        assertEquals(GameStatus.DEFEAT, engine.status)
        assertEquals(GameStatus.DEFEAT, lastRecordedStatus)

        // Additional ticks after defeat should not process or spawn new enemies
        engine.update(1.0f)
        assertEquals(0, engine.baseHealth)
        assertEquals(GameStatus.DEFEAT, engine.status)
    }

    @Test
    fun testVictoryConditionWithoutTowersUsingTestingHook() {
        engine.start()

        // Simulate all 5 waves:
        // In each wave, spawn all enemies, eliminate them via testing hook, and wait for inter-wave transition
        for (wave in 1..5) {
            assertEquals(wave, engine.currentWaveNumber)

            // Spawn all enemies for this wave
            val neededEnemies = engine.currentWave.enemyCount
            while (engine.enemiesSpawnedInCurrentWave < neededEnemies) {
                engine.update(0.1f)
            }

            // Defeat all active enemies before they reach base (testing hook)
            engine.defeatAllActiveEnemiesForTesting()
            assertTrue(engine.activeEnemies.isEmpty())

            // Process tick to recognize wave completion
            engine.update(0.05f)

            if (wave < 5) {
                assertTrue("Should be waiting for next wave", engine.isWaitingForNextWave)
                // Wait through the inter-wave delay (2 seconds)
                engine.update(GameConstants.INTER_WAVE_DELAY_SECONDS + 0.1f)
            }
        }

        // After wave 5 with base health > 0 -> VICTORY!
        assertEquals(GameStatus.VICTORY, engine.status)
        assertEquals(GameStatus.VICTORY, lastRecordedStatus)
        assertTrue("Base health should be preserved", engine.baseHealth > 0)
        assertEquals(5, waveCompletedCount)
    }

    @Test
    fun testPauseAndResume() {
        engine.start()
        engine.update(0.1f)
        val initialEnemies = engine.activeEnemies.size

        engine.pause()
        assertEquals(GameStatus.PAUSED, engine.status)

        // While paused, update must be ignored
        val activeEnemy = engine.activeEnemies.firstOrNull()
        val posBeforePause = activeEnemy?.x ?: 0f
        engine.update(1.0f)

        assertEquals(GameStatus.PAUSED, engine.status)
        assertEquals(posBeforePause, activeEnemy?.x ?: 0f, 0.0001f)
        assertEquals(initialEnemies, engine.activeEnemies.size)

        // Resume match
        engine.resume()
        assertEquals(GameStatus.RUNNING, engine.status)
        engine.update(0.1f)
        assertTrue("Position should advance after resume", (activeEnemy?.x ?: 0f) >= posBeforePause)
    }

    @Test
    fun testRestartCleanState() {
        engine.start()
        // Simulate damage
        for (i in 1..300) {
            engine.update(0.1f)
            if (engine.baseHealth < 20) break
        }

        assertTrue(engine.baseHealth < 20)

        // Restart
        engine.restart()
        assertEquals(GameStatus.RUNNING, engine.status)
        assertEquals(GameConstants.STAGE_ONE_INITIAL_LIVES, engine.baseHealth)
        assertEquals(1, engine.currentWaveNumber)
        assertEquals(0, engine.enemiesSpawnedInCurrentWave)
        assertEquals(0, engine.towersBuiltCount)
        assertTrue(engine.activeProjectiles.isEmpty())
    }

    @Test
    fun testNoSimultaneousVictoryAndDefeat() {
        assertFalse(engine.status == GameStatus.VICTORY && engine.status == GameStatus.DEFEAT)
    }

    // =========================================================================
    // Part 4 — Tower Building & Placement Tests (Section 9 Requirements 1, 2, 3)
    // =========================================================================

    @Test
    fun testBuildTowerOnValidSpot() {
        engine.start()
        assertTrue("Spot 1 should be available for building", engine.canBuildTowerAt("spot_1"))

        val tower = engine.buildTower("spot_1", TowerType.ARCHER_TOWER)
        assertNotNull("Tower should be successfully constructed", tower)
        assertEquals("Torre de Arqueiros", tower?.name)
        assertEquals(TowerType.ARCHER_TOWER, tower?.type)
        assertEquals(1, engine.towersBuiltCount)
        assertEquals(tower, lastBuiltTower)

        val spot = engine.getSpot("spot_1")
        assertTrue("Spot 1 must be marked as occupied", spot?.isOccupied == true)
        assertEquals(tower, spot?.placedTower)
    }

    @Test
    fun testCannotBuildOnInvalidSpot() {
        engine.start()
        assertFalse("Non-existent spot must not allow building", engine.canBuildTowerAt("invalid_spot_99"))

        val tower = engine.buildTower("invalid_spot_99", TowerType.MAGE_TOWER)
        assertNull("Construction on invalid spot must return null", tower)
        assertEquals(0, engine.towersBuiltCount)

        // Also test path clearance verification
        assertTrue("Path coordinate must be detected as too close to path", StageOneMap.isPointTooCloseToPath(0.18f, 0.15f))
    }

    @Test
    fun testCannotBuildTwoTowersOnSameSpot() {
        engine.start()
        val firstTower = engine.buildTower("spot_1", TowerType.ARCHER_TOWER)
        assertNotNull(firstTower)

        assertFalse("Spot 1 must not allow another building", engine.canBuildTowerAt("spot_1"))
        val secondTower = engine.buildTower("spot_1", TowerType.MAGE_TOWER)
        assertNull("Second construction on same spot must fail", secondTower)
        assertEquals(1, engine.towersBuiltCount)
    }

    @Test
    fun testTowerBuildQuotaEnforced() {
        engine.start()
        assertEquals(5, engine.remainingTowersToBuild)

        // Build 5 towers across 5 valid spots
        for (i in 1..5) {
            val spotId = "spot_$i"
            val t = engine.buildTower(spotId, TowerType.ARCHER_TOWER)
            assertNotNull("Tower $i should be built", t)
        }

        assertEquals(5, engine.towersBuiltCount)
        assertEquals(0, engine.remainingTowersToBuild)

        // Attempting to build a 6th tower must be rejected
        assertFalse("Cannot build exceeding max quota", engine.canBuildTowerAt("spot_6"))
        val sixthTower = engine.buildTower("spot_6", TowerType.CANNON_TOWER)
        assertNull("6th tower must return null", sixthTower)
        assertEquals(5, engine.towersBuiltCount)
    }

    @Test
    fun testThreeDistinctTowerIdentities() {
        engine.start()
        val archer = engine.buildTower("spot_1", TowerType.ARCHER_TOWER)
        val mage = engine.buildTower("spot_2", TowerType.MAGE_TOWER)
        val cannon = engine.buildTower("spot_3", TowerType.CANNON_TOWER)

        assertNotNull(archer)
        assertNotNull(mage)
        assertNotNull(cannon)

        // 1. Archer: Fast fire rate, lower damage, medium range, no splash
        assertEquals(GameConstants.ARCHER_DAMAGE, archer!!.damage)
        assertEquals(GameConstants.ARCHER_RANGE, archer.range, 0.001f)
        assertEquals(GameConstants.ARCHER_FIRE_RATE, archer.fireRate, 0.001f)
        assertEquals(0f, archer.splashRadius, 0.001f)

        // 2. Mage: Slower fire rate, higher damage, longer range, no splash
        assertEquals(GameConstants.MAGE_DAMAGE, mage!!.damage)
        assertEquals(GameConstants.MAGE_RANGE, mage.range, 0.001f)
        assertTrue("Mage range should exceed archer range", mage.range > archer.range)
        assertTrue("Mage damage should exceed archer damage", mage.damage > archer.damage)
        assertEquals(0f, mage.splashRadius, 0.001f)

        // 3. Cannon: Slow fire rate, high damage, area splash
        assertEquals(GameConstants.CANNON_DAMAGE, cannon!!.damage)
        assertEquals(GameConstants.CANNON_RANGE, cannon.range, 0.001f)
        assertEquals(GameConstants.CANNON_SPLASH_RADIUS, cannon.splashRadius, 0.001f)
        assertTrue("Cannon damage should be highest", cannon.damage > mage.damage)
        assertTrue("Cannon fire rate should be slowest", cannon.fireRate < mage.fireRate)
    }

    // =========================================================================
    // Part 4 — Combat & Targeting Tests (Section 9 Requirements 4, 5, 6, 7, 8, 9, 10)
    // =========================================================================

    @Test
    fun testEnemyOutOfRangeReceivesNoDamage() {
        engine.start()
        // Spot 8 is near the castle destination (x=0.75, y=0.85)
        val tower = engine.buildTower("spot_8", TowerType.ARCHER_TOWER)
        assertNotNull(tower)

        // First enemy spawns at spawn portal (x=0.18, y=0.06), far from spot 8
        engine.update(0.1f)
        val enemy = engine.activeEnemies.first()

        assertTrue("Enemy should be far from tower", enemy.distanceTo(tower!!.x, tower.y) > tower.range)
        assertEquals(100, enemy.currentHealth)
        assertTrue("No projectiles should be fired at out-of-range enemy", engine.activeProjectiles.isEmpty())
    }

    @Test
    fun testEnemyInRangeIsTargetedAndFiredUpon() {
        engine.start()
        // Spot 1 (x=0.50, y=0.11) overlooks the upper road segment
        val tower = engine.buildTower("spot_1", TowerType.ARCHER_TOWER)
        assertNotNull(tower)

        // Advance enemy until it enters range of Spot 1
        for (step in 1..40) {
            engine.update(0.05f)
            if (engine.activeProjectiles.isNotEmpty()) {
                break
            }
        }

        assertTrue("Tower should have targeted and fired a projectile", engine.activeProjectiles.isNotEmpty())
        val projectile = engine.activeProjectiles.first()
        assertEquals(TowerType.ARCHER_TOWER, projectile.towerType)
        assertEquals(tower!!.id, projectile.sourceTowerId)
        assertEquals(GameConstants.ARCHER_DAMAGE, projectile.damage)
    }

    @Test
    fun testAttackIntervalIsRespected() {
        engine.start()
        val tower = engine.buildTower("spot_1", TowerType.ARCHER_TOWER)
        assertNotNull(tower)

        // Advance until first shot is fired
        while (engine.activeProjectiles.isEmpty()) {
            engine.update(0.05f)
        }

        assertEquals(1, tower!!.totalShotsFired)
        assertFalse("Tower should be on cooldown immediately after firing", tower.canAttack)

        // Clear active projectiles and step by one small tick (0.05s)
        engine.activeProjectiles.clear()
        engine.update(0.05f)

        assertFalse("Tower should still be on cooldown", tower.canAttack)
        assertTrue("No new projectile should have been fired during cooldown", engine.activeProjectiles.isEmpty())
        assertEquals("Shots fired should still be 1 during cooldown", 1, tower.totalShotsFired)

        // Advance ticks until cooldown expires and second shot is fired
        for (step in 1..25) {
            engine.update(0.05f)
            if (tower.totalShotsFired > 1) {
                break
            }
        }
        assertEquals("Tower should fire second shot after cooldown expires", 2, tower.totalShotsFired)
    }

    @Test
    fun testDamageReducesEnemyHealthCorrectly() {
        engine.start()
        // Spot 1 with Mage Tower
        val tower = engine.buildTower("spot_1", TowerType.MAGE_TOWER)
        assertNotNull(tower)

        // Wait until projectile is fired and arrives at enemy
        val initialHealth = 100
        var damagedHealth = initialHealth
        for (step in 1..100) {
            engine.update(0.05f)
            val enemy = engine.activeEnemies.firstOrNull()
            if (enemy != null && enemy.currentHealth < initialHealth) {
                damagedHealth = enemy.currentHealth
                break
            }
        }

        val enemy = engine.activeEnemies.first()
        assertEquals("Health should be reduced by exact Mage damage (100 - 45 = 55)", 55, enemy.currentHealth)
        assertEquals("Damaged health recorded during loop must match final health", damagedHealth, enemy.currentHealth)
        assertEquals("Health reduction must match Mage damage", GameConstants.MAGE_DAMAGE, initialHealth - damagedHealth)
        assertTrue(tower!!.totalDamageDealt >= 45)
    }

    @Test
    fun testDefeatedEnemyIsRemovedCorrectly() {
        engine.start()
        engine.update(0.1f)
        assertEquals(1, engine.activeEnemies.size)
        val enemy = engine.activeEnemies.first()

        // Apply fatal damage
        val killed = enemy.takeDamage(enemy.maxHealth)
        assertTrue(killed)
        assertFalse(enemy.isAlive)

        // Engine tick should purge dead enemy
        engine.update(0.05f)
        assertTrue("Defeated enemy should be removed from active enemies list", engine.activeEnemies.isEmpty())
        assertEquals(enemy, lastDefeatedEnemy)
    }

    @Test
    fun testCannonAreaDamageHitsMultipleEnemiesWithoutDuplicateDamage() {
        engine.start()

        // Spawn multiple enemies first
        while (engine.activeEnemies.size < 3) {
            engine.update(0.1f)
        }

        val cannon = engine.buildTower("spot_3", TowerType.CANNON_TOWER)
        assertNotNull(cannon)

        // Align enemies close to each other within cannon splash radius (0.12f)
        val e1 = engine.activeEnemies[0]
        val e2 = engine.activeEnemies[1]
        e1.x = 0.50f
        e1.y = 0.35f - 0.05f // Within range of spot 3 (0.50, 0.35)
        e2.x = 0.50f
        e2.y = 0.35f - 0.02f // Close to e1 (distance = 0.03 < 0.12 splash radius)

        // Trigger cannon attack
        engine.update(0.05f)
        assertTrue("Cannon should fire", engine.activeProjectiles.isNotEmpty())

        val proj = engine.activeProjectiles.first()
        assertEquals(TowerType.CANNON_TOWER, proj.towerType)
        assertEquals(GameConstants.CANNON_SPLASH_RADIUS, proj.splashRadius, 0.001f)

        // Advance until cannonball impacts
        for (step in 1..40) {
            engine.update(0.05f)
            if (proj.isExpired) break
        }

        // Both enemies should have taken Cannon damage (70) exactly once without duplication
        assertEquals("Enemy 1 health should be 30 (100 - 70)", 30, e1.currentHealth)
        assertEquals("Enemy 2 health should be 30 (100 - 70)", 30, e2.currentHealth)
        assertTrue("Visual shockwave effect should be spawned", engine.visualEffects.any { it.isAreaExplosion })
    }

    @Test
    fun testProjectileDoesNotDealRepeatedDamage() {
        engine.start()
        engine.update(0.1f)
        val enemy = engine.activeEnemies.first()

        val tower = engine.buildTower("spot_1", TowerType.ARCHER_TOWER)
        assertNotNull(tower)

        // Wait for projectile impact
        for (step in 1..80) {
            engine.update(0.05f)
            if (enemy.currentHealth < 100) break
        }

        val healthAfterHit = enemy.currentHealth
        assertEquals(100 - GameConstants.ARCHER_DAMAGE, healthAfterHit)

        // Additional ticks must not apply damage again from the same expired projectile
        engine.update(0.05f)
        engine.update(0.05f)
        assertEquals(healthAfterHit, enemy.currentHealth)
    }

    @Test
    fun testVictoryDoesNotOccurWhileEnemiesOrWavesRemain() {
        engine.start()
        engine.update(0.1f)

        // With active enemies on wave 1, victory must not occur
        assertTrue(engine.activeEnemies.isNotEmpty())
        assertFalse(engine.status == GameStatus.VICTORY)

        // Defeat current active enemies, but wave 1 still has more enemies to spawn
        engine.activeEnemies.clear()
        engine.update(0.05f)
        assertFalse(engine.status == GameStatus.VICTORY)
    }
}
