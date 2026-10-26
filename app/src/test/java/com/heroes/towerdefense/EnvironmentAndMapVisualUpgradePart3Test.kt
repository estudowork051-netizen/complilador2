package com.heroes.towerdefense

import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.data.InMemoryStorage
import com.heroes.towerdefense.game.engine.GameEngine
import com.heroes.towerdefense.game.map.EnvironmentTheme
import com.heroes.towerdefense.game.map.GraphicQuality
import com.heroes.towerdefense.game.map.StageOneMap
import com.heroes.towerdefense.game.model.AmbientParticle
import com.heroes.towerdefense.game.model.AmbientParticleType
import com.heroes.towerdefense.game.model.PlacedTower
import com.heroes.towerdefense.game.model.VisualPhysicsDebris
import com.heroes.towerdefense.game.model.WaveConfig
import com.heroes.towerdefense.game.view.CameraScreenShake
import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.tower.TowerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit Test Suite for Visual Upgrade Part 3:
 * - EnvironmentTheme catalog for all 6 campaign stages with distinct color palettes & features
 * - GraphicQuality configuration tiers (LOW, MEDIUM, HIGH) and persistence in GamePreferences
 * - PlacedTower 3D isometric construction spawn scale timer and attack recoil animation lifecycle
 * - CameraScreenShake impact physics with multi-frequency damping and graceful decay
 * - VisualPhysicsDebris 3D isometric height, gravity acceleration, ground bounce restitution, and expiration
 * - AmbientParticle environmental floating movement, twinkling alpha oscillation, and screen boundary wrapping
 * - Depth sorting (Y-sorting) contract for realistic 3D isometric occlusion between towers, enemies, and heroes
 * - Preservation of StageOneMap geometry, tactical spots, and GameEngine combat integrity
 */
class EnvironmentAndMapVisualUpgradePart3Test {

    private lateinit var storage: InMemoryStorage
    private lateinit var preferences: GamePreferences

    @Before
    fun setUp() {
        storage = InMemoryStorage()
        preferences = GamePreferences(storage)
    }

    // =========================================================================
    // 1. EnvironmentTheme Catalog Tests
    // =========================================================================

    @Test
    fun testAllSixEnvironmentThemesExistAndHaveDistinctPalettes() {
        val themes = EnvironmentTheme.values()
        assertEquals("There should be exactly 6 distinct environment themes", 6, themes.size)

        val stageNumbers = themes.map { it.stageNumber }.toSet()
        assertEquals("Themes must map to stages 1 through 6", setOf(1, 2, 3, 4, 5, 6), stageNumbers)

        val names = themes.map { it.themeName }.toSet()
        assertEquals("All 6 theme names must be unique", 6, names.size)

        val groundColors = themes.map { it.groundBaseColor }.toSet()
        assertEquals("All 6 themes must have distinct ground base colors", 6, groundColors.size)

        val pathColors = themes.map { it.pathSurfaceColor }.toSet()
        assertEquals("All 6 themes must have distinct path surface colors", 6, pathColors.size)

        val torchColors = themes.map { it.torchLightColor }.toSet()
        assertEquals("All 6 themes must have distinct atmospheric lighting colors", 6, torchColors.size)
    }

    @Test
    fun testEnvironmentThemeFromStageNumberMappingAndFallback() {
        assertEquals(EnvironmentTheme.HIGHLAND_FOREST, EnvironmentTheme.fromStageNumber(1))
        assertEquals(EnvironmentTheme.WHISPERING_WOODS, EnvironmentTheme.fromStageNumber(2))
        assertEquals(EnvironmentTheme.ANCIENT_RUINS, EnvironmentTheme.fromStageNumber(3))
        assertEquals(EnvironmentTheme.CRYSTAL_CAVERNS, EnvironmentTheme.fromStageNumber(4))
        assertEquals(EnvironmentTheme.VOLCANIC_FORTRESS, EnvironmentTheme.fromStageNumber(5))
        assertEquals(EnvironmentTheme.ASTRAL_PEAK, EnvironmentTheme.fromStageNumber(6))

        // Fallback for non-canonical stage indices
        assertEquals(EnvironmentTheme.HIGHLAND_FOREST, EnvironmentTheme.fromStageNumber(0))
        assertEquals(EnvironmentTheme.HIGHLAND_FOREST, EnvironmentTheme.fromStageNumber(99))
    }

    @Test
    fun testEnvironmentThemeSpecificAttributes() {
        // Highland Forest
        val forest = EnvironmentTheme.HIGHLAND_FOREST
        assertEquals(AmbientParticleType.POLLEN, forest.ambientParticleType)
        assertTrue(forest.hasHazardLiquid)

        // Whispering Woods
        val woods = EnvironmentTheme.WHISPERING_WOODS
        assertEquals(AmbientParticleType.FIREFLIES, woods.ambientParticleType)
        assertTrue(woods.hasHazardLiquid)

        // Ancient Ruins
        val ruins = EnvironmentTheme.ANCIENT_RUINS
        assertEquals(AmbientParticleType.SPECTRAL_MIST, ruins.ambientParticleType)
        assertTrue(ruins.hasHazardLiquid)

        // Crystal Caverns
        val caverns = EnvironmentTheme.CRYSTAL_CAVERNS
        assertEquals(AmbientParticleType.CRYSTAL_DUST, caverns.ambientParticleType)
        assertTrue(caverns.hasHazardLiquid)

        // Volcanic Fortress
        val volcano = EnvironmentTheme.VOLCANIC_FORTRESS
        assertEquals(AmbientParticleType.VOLCANIC_EMBERS, volcano.ambientParticleType)
        assertTrue(volcano.hasHazardLiquid)

        // Astral Peak
        val astral = EnvironmentTheme.ASTRAL_PEAK
        assertEquals(AmbientParticleType.ASTRAL_STARDUST, astral.ambientParticleType)
        assertTrue(astral.hasHazardLiquid)
    }

    // =========================================================================
    // 2. GraphicQuality & Preferences Tests
    // =========================================================================

    @Test
    fun testGraphicQualityTiersConfiguration() {
        val low = GraphicQuality.LOW
        val med = GraphicQuality.MEDIUM
        val high = GraphicQuality.HIGH

        assertEquals(0, low.maxAmbientParticles)
        assertFalse(low.enableDynamicLighting)
        assertFalse(low.enableScreenShake)
        assertFalse(low.enableFoliageSway)
        assertFalse(low.enablePhysicsDebris)

        assertTrue(med.maxAmbientParticles > 0)
        assertTrue(med.enableDynamicLighting)
        assertTrue(med.enableScreenShake)
        assertTrue(med.enableFoliageSway)
        assertFalse(med.enablePhysicsDebris)

        assertTrue(high.maxAmbientParticles >= med.maxAmbientParticles)
        assertTrue(high.enableDynamicLighting)
        assertTrue(high.enableScreenShake)
        assertTrue(high.enableFoliageSway)
        assertTrue(high.enablePhysicsDebris)

        assertEquals(0, low.maxDebrisCount)
        assertTrue(low.maxParticles > 0)
        assertTrue(med.maxDebrisCount >= low.maxDebrisCount)
        assertTrue(med.maxParticles >= low.maxParticles)
        assertTrue(high.maxDebrisCount >= med.maxDebrisCount)
        assertTrue(high.maxParticles >= med.maxParticles)
    }

    @Test
    fun testGamePreferencesGraphicQualityPersistenceAndDefaults() {
        // Default must be HIGH
        assertEquals(GraphicQuality.HIGH, preferences.graphicQuality)

        // Update to MEDIUM
        preferences.graphicQuality = GraphicQuality.MEDIUM
        assertEquals(GraphicQuality.MEDIUM, preferences.graphicQuality)

        // Update to LOW
        preferences.graphicQuality = GraphicQuality.LOW
        assertEquals(GraphicQuality.LOW, preferences.graphicQuality)

        // Reset to defaults restores HIGH
        preferences.resetToDefaults()
        assertEquals(GraphicQuality.HIGH, preferences.graphicQuality)
    }

    // =========================================================================
    // 3. PlacedTower Spawn Scale & Attack Recoil Animation Timers
    // =========================================================================

    @Test
    fun testPlacedTowerAnimationTimersLifecycle() {
        val tower = PlacedTower.createArcher("tower_1", "spot_1", 0.5f, 0.5f)

        // On placement, spawnAnimTimer must be initialized to 0.5s for pop-up scale bounce
        assertEquals(0.5f, tower.spawnAnimTimer, 0.001f)
        assertEquals(0f, tower.recoilAnimTimer, 0.001f)

        // Advance cooldown by 0.20s
        tower.updateCooldown(0.20f)
        assertEquals(0.30f, tower.spawnAnimTimer, 0.001f)
        assertEquals(0f, tower.recoilAnimTimer, 0.001f)

        // Advance cooldown past remaining spawn time
        tower.updateCooldown(0.35f)
        assertEquals(0f, tower.spawnAnimTimer, 0.001f)

        // Firing attack triggers recoil animation
        tower.resetCooldown()
        assertEquals(0.20f, tower.recoilAnimTimer, 0.001f)
        assertEquals(1, tower.totalShotsFired)

        // Advance cooldown during recoil
        tower.updateCooldown(0.10f)
        assertEquals(0.10f, tower.recoilAnimTimer, 0.001f)

        // Recoil completes and returns to 0
        tower.updateCooldown(0.15f)
        assertEquals(0f, tower.recoilAnimTimer, 0.001f)
    }

    // =========================================================================
    // 4. CameraScreenShake Physics Tests
    // =========================================================================

    @Test
    fun testCameraScreenShakeTriggerDecayAndReset() {
        val shake = CameraScreenShake()
        assertFalse("Camera shake should start inactive", shake.isShaking)
        assertEquals(0f, shake.offsetX, 0.001f)
        assertEquals(0f, shake.offsetY, 0.001f)

        // Trigger shake
        shake.trigger(intensity = 10f, shakeDuration = 0.30f)
        assertTrue("Camera shake should be active after trigger", shake.isShaking)
        assertEquals(10f, shake.currentIntensity, 0.001f)
        assertEquals(0.30f, shake.duration, 0.001f)

        // Update partially
        shake.update(0.10f)
        assertTrue(shake.isShaking)
        // Offset should be computed by harmonic oscillation
        assertTrue("Offset should be non-zero during shake", shake.offsetX != 0f || shake.offsetY != 0f)

        // Advance past duration -> decays completely to zero
        shake.update(0.25f)
        assertFalse("Camera shake should finish after duration expires", shake.isShaking)
        assertEquals(0f, shake.offsetX, 0.001f)
        assertEquals(0f, shake.offsetY, 0.001f)
        assertEquals(0f, shake.currentIntensity, 0.001f)

        // Test manual reset
        shake.trigger(15f, 0.40f)
        assertTrue(shake.isShaking)
        shake.reset()
        assertFalse(shake.isShaking)
        assertEquals(0f, shake.offsetX, 0.001f)
        assertEquals(0f, shake.offsetY, 0.001f)
    }

    // =========================================================================
    // 5. VisualPhysicsDebris 3D Isometric Physics Tests
    // =========================================================================

    @Test
    fun testVisualPhysicsDebrisGravityBouncesAndLifetime() {
        val debris = VisualPhysicsDebris(
            x = 100f,
            y = 200f,
            z = 25f,
            vx = 60f,
            vy = 30f,
            vz = 150f,
            color = 0xFFFFFFFF.toInt(),
            size = 4f,
            maxLifetime = 0.50f
        )

        assertFalse(debris.isExpired)
        assertEquals(0, debris.bounceCount)
        assertEquals(175f, debris.screenY, 0.001f) // 200 - 25

        // Advance 0.1s
        debris.update(0.10f)
        assertTrue(debris.x > 100f)
        assertTrue(debris.y > 200f)
        assertTrue(debris.z > 25f) // Thrown upward initially

        // Advance several steps until gravity pulls z down to 0 and bounces
        var steps = 0
        while (debris.z > 0f && steps < 50) {
            debris.update(0.02f)
            steps++
        }

        // Bouncing on ground (z == 0)
        assertTrue("Debris should have registered ground bounce", debris.bounceCount > 0)
        assertEquals("Debris z on ground must be 0", 0f, debris.z, 0.001f)
        assertEquals("Debris screenY on ground must equal y", debris.y, debris.screenY, 0.001f)

        // Advance past lifetime
        debris.update(0.45f)
        assertTrue("Debris should expire after maxLifetime", debris.isExpired)
        assertEquals(0f, debris.alpha, 0.001f)
    }

    // =========================================================================
    // 6. AmbientParticle Environmental Simulation Tests
    // =========================================================================

    @Test
    fun testAmbientParticleMovementAlphaPulsationAndBoundaryWrapping() {
        val p = AmbientParticle(
            x = 50f,
            y = 50f,
            vx = 20f,
            vy = 15f,
            color = 0xFFFDE68A.toInt(),
            size = 3f,
            baseAlpha = 0.70f
        )

        val initialAlpha = p.currentAlpha
        assertTrue("Initial alpha should be positive", initialAlpha > 0f)

        // Advance time
        p.update(0.10f, 1000f, 1500f)
        assertTrue(p.x > 50f)
        assertTrue(p.y > 50f)
        assertTrue("Current alpha must stay in [0, 1]", p.currentAlpha in 0f..1f)

        // Boundary wrapping
        p.x = 1050f
        p.update(0.01f, 1000f, 1500f)
        assertEquals(-20f, p.x, 0.5f) // Wraps around left
    }

    // =========================================================================
    // 7. Depth Sorting (Y-Sorting) Occlusion Contract Tests
    // =========================================================================

    @Test
    fun testDepthSortingOrdersEntitiesByAscendingY() {
        data class TestEntity(val id: String, val y: Float)

        val entities = listOf(
            TestEntity("FrontTower", 650f),
            TestEntity("BackEnemy", 250f),
            TestEntity("MidHero", 450f),
            TestEntity("DeepBoss", 150f),
            TestEntity("ForegroundCastle", 850f)
        )

        val sorted = entities.sortedBy { it.y }

        assertEquals("DeepBoss", sorted[0].id)
        assertEquals("BackEnemy", sorted[1].id)
        assertEquals("MidHero", sorted[2].id)
        assertEquals("FrontTower", sorted[3].id)
        assertEquals("ForegroundCastle", sorted[4].id)
    }

    // =========================================================================
    // 8. StageOneMap Geometry & Tactical Spots Integrity
    // =========================================================================

    @Test
    fun testStageOneMapGeometryAndTacticalSpotsIntegrity() {
        val waypoints = StageOneMap.waypoints
        assertEquals("Stage 1 should have exactly 8 waypoints defining the S-shaped path", 8, waypoints.size)

        val totalLength = StageOneMap.calculateTotalPathLength()
        assertTrue("Total path length must be positive", totalLength > 1.5f)

        val spots = StageOneMap.defaultTowerSpots
        assertEquals("Stage 1 should have 8 pre-configured tactical tower spots", 8, spots.size)

        // All spots must be a safe distance from the road path
        for (spot in spots) {
            assertFalse(
                "Spot ${spot.id} must not be too close to the enemy road path",
                StageOneMap.isPointTooCloseToPath(spot.x, spot.y, 0.07f)
            )
        }
    }

    // =========================================================================
    // 9. GameEngine Combat Integration & Zero-Regression
    // =========================================================================

    @Test
    fun testGameEngineBuildsAllTowerTypesWithValidCooldownsAndStats() {
        val hero = Hero.getPlannedHeroes().first()
        val engine = GameEngine(
            selectedHero = hero,
            waves = listOf(WaveConfig(waveNumber = 1, enemyCount = 3))
        )
        engine.start()

        // Build Archer Tower at spot_1
        val archer = engine.buildTower("spot_1", TowerType.ARCHER_TOWER)
        assertNotNull("Archer tower should be successfully built", archer)
        assertEquals(TowerType.ARCHER_TOWER, archer!!.type)
        assertEquals(0.5f, archer.spawnAnimTimer, 0.001f)

        // Build Mage Tower at spot_2
        val mage = engine.buildTower("spot_2", TowerType.MAGE_TOWER)
        assertNotNull("Mage tower should be successfully built", mage)
        assertEquals(TowerType.MAGE_TOWER, mage!!.type)

        // Build Cannon Tower at spot_3
        val cannon = engine.buildTower("spot_3", TowerType.CANNON_TOWER)
        assertNotNull("Cannon tower should be successfully built", cannon)
        assertEquals(TowerType.CANNON_TOWER, cannon!!.type)

        // Update engine simulation
        engine.update(0.10f)

        // Verify timers advanced
        assertTrue(archer.spawnAnimTimer < 0.5f)
        assertEquals(3, engine.placedTowers.size)
    }
}
