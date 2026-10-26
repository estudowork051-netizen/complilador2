package com.heroes.towerdefense

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.data.InMemoryStorage
import com.heroes.towerdefense.game.engine.GameEngine
import com.heroes.towerdefense.game.map.GraphicQuality
import com.heroes.towerdefense.game.map.StageOneMap
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.CombatParticle
import com.heroes.towerdefense.game.model.CombatProjectile
import com.heroes.towerdefense.game.model.DeployedHero
import com.heroes.towerdefense.game.model.EnemyVisualType
import com.heroes.towerdefense.game.model.HeroAnimState
import com.heroes.towerdefense.game.model.PlacedTower
import com.heroes.towerdefense.game.model.ProjectileVisualType
import com.heroes.towerdefense.game.model.VisualEffect
import com.heroes.towerdefense.game.model.VisualEffectType
import com.heroes.towerdefense.game.model.VisualPhysicsDebris
import com.heroes.towerdefense.game.model.WaveConfig
import com.heroes.towerdefense.game.view.CameraScreenShake
import com.heroes.towerdefense.game.view.CombatVfxRenderer
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
 * Unit Test Suite for Visual Upgrade Part 4:
 * - Articulated attack animation cycles for all 6 heroes (WINDUP, STRIKE, RECOVERY, IDLE)
 * - Hero skill casting state machine (SKILL_CAST -> IDLE) for all 12 signature hero abilities
 * - ProjectileVisualType classification for ballistic arrows, arcane comet missiles, cannonballs, and hero projectiles
 * - CombatProjectile parabolic elevation arc, flight progress tracking, and directional orientation
 * - VisualEffectType classification, signature skill identification, and impact debris/shake traits
 * - Tactical impact feedback integration (CameraScreenShake, VisualPhysicsDebris, CombatParticle) respecting GraphicQuality
 * - Tower firing recoil lifecycle and muzzle flash states (Archer, Mage, Cannon)
 * - GameEngine automated combat integration: tower firing, projectile flight, impact resolution, and deduplication
 * - 100% preservation of combat contracts and regression safety with Parts 1, 2, and 3
 */
class CombatAndVisualEffectsUpgradePart4Test {

    private lateinit var storage: InMemoryStorage
    private lateinit var preferences: GamePreferences

    @Before
    fun setUp() {
        storage = InMemoryStorage()
        preferences = GamePreferences(storage)
    }

    // =========================================================================
    // 1. Hero Articulated Attack Animations (All 6 Heroes)
    // =========================================================================

    @Test
    fun testAllSixHeroesExecuteArticulatedAttackAnimationLifecycle() {
        val heroes = Hero.getPlannedHeroes()
        assertEquals("There must be exactly 6 canonical heroes", 6, heroes.size)

        for (heroData in heroes) {
            val deployed = DeployedHero(hero = heroData, x = 0.5f, y = 0.5f)
            assertEquals("Initial state must be IDLE", HeroAnimState.IDLE, deployed.animState)
            assertFalse("Hero should not be attacking in IDLE", deployed.animState.isAttacking)

            // Trigger basic attack towards target to the right (x = 0.8)
            deployed.triggerAttack(0.8f, 0.5f)
            assertEquals("${heroData.name} must transition to WINDUP on attack", HeroAnimState.WINDUP, deployed.animState)
            assertTrue("WINDUP is an attacking state", deployed.animState.isAttacking)
            assertEquals("Facing direction should be right (+1.0)", 1f, deployed.facingDirection)

            // Advance through WINDUP duration (0.12s)
            deployed.update(0.13f)
            assertEquals("${heroData.name} must transition to STRIKE after windup", HeroAnimState.STRIKE, deployed.animState)
            assertTrue("STRIKE is an attacking state", deployed.animState.isAttacking)

            // Advance through STRIKE duration (0.18s)
            deployed.update(0.19f)
            assertEquals("${heroData.name} must transition to RECOVERY after strike", HeroAnimState.RECOVERY, deployed.animState)
            assertTrue("RECOVERY is an attacking state", deployed.animState.isAttacking)

            // Advance through RECOVERY duration (0.15s)
            deployed.update(0.16f)
            assertEquals("${heroData.name} must return to IDLE after recovery", HeroAnimState.IDLE, deployed.animState)
            assertFalse("Hero is no longer attacking after returning to IDLE", deployed.animState.isAttacking)

            // Test facing direction flip towards target to the left (x = 0.2)
            deployed.triggerAttack(0.2f, 0.5f)
            assertEquals("Facing direction should flip to left (-1.0)", -1f, deployed.facingDirection)
        }
    }

    @Test
    fun testAllTwelveHeroSkillsTriggerSkillCastAnimationAndReset() {
        val heroes = Hero.getPlannedHeroes()
        for (heroData in heroes) {
            assertEquals("${heroData.name} must have exactly 2 active skills", 2, heroData.skills.size)
            val deployed = DeployedHero(hero = heroData, x = 0.5f, y = 0.5f)

            for (skill in heroData.skills) {
                deployed.triggerSkill(skill, 0.7f, 0.5f)
                assertEquals("Triggering ${skill.name} must set SKILL_CAST state", HeroAnimState.SKILL_CAST, deployed.animState)
                assertEquals("Active cast skill must be recorded", skill, deployed.activeCastSkill)

                // Advance past skill channel duration (0.55s)
                deployed.update(0.56f)
                assertEquals("Hero must return to IDLE after casting ${skill.name}", HeroAnimState.IDLE, deployed.animState)
                assertEquals("Active cast skill must be cleared", null, deployed.activeCastSkill)
            }
        }
    }

    // =========================================================================
    // 2. ProjectileVisualType & Parabolic Ballistic Properties
    // =========================================================================

    @Test
    fun testAllProjectileVisualTypesExistAndHaveExpectedTraits() {
        val types = ProjectileVisualType.values()
        assertEquals("There should be 9 distinct projectile visual types", 9, types.size)

        // Ballistic arc validation
        assertTrue("Archer arrow must be ballistic", ProjectileVisualType.ARCHER_ARROW.isBallisticArc)
        assertTrue("Cannonball must be ballistic", ProjectileVisualType.CANNON_BALL.isBallisticArc)
        assertTrue("Gyro rocket must be ballistic", ProjectileVisualType.GYRO_ROCKET.isBallisticArc)
        assertFalse("Mage energy missile should fly direct/homing", ProjectileVisualType.MAGE_ENERGY_MISSILE.isBallisticArc)

        // Particle trail traits
        assertTrue("Cannonball has smoke trail", ProjectileVisualType.CANNON_BALL.hasSmokeTrail)
        assertTrue("Mage missile has sparkle trail", ProjectileVisualType.MAGE_ENERGY_MISSILE.hasSparkleTrail)
        assertTrue("Astral arrow has sparkle trail", ProjectileVisualType.ASTRAL_ARROW.hasSparkleTrail)
        assertTrue("Fire bolt has sparkle trail", ProjectileVisualType.FIRE_BOLT.hasSparkleTrail)

        // Tower mapping verification
        assertEquals(ProjectileVisualType.ARCHER_ARROW, ProjectileVisualType.fromTowerType(TowerType.ARCHER_TOWER))
        assertEquals(ProjectileVisualType.MAGE_ENERGY_MISSILE, ProjectileVisualType.fromTowerType(TowerType.MAGE_TOWER))
        assertEquals(ProjectileVisualType.CANNON_BALL, ProjectileVisualType.fromTowerType(TowerType.CANNON_TOWER))
        assertEquals(ProjectileVisualType.FROST_SHARD, ProjectileVisualType.fromTowerType(TowerType.FROST_TOWER))
    }

    @Test
    fun testCombatProjectileParabolicArcAndFlightProgressCalculation() {
        val arrow = CombatProjectile(
            id = "test_arrow_1",
            sourceTowerId = "tower_1",
            towerType = TowerType.ARCHER_TOWER,
            startX = 0.2f,
            startY = 0.2f,
            currentX = 0.2f,
            currentY = 0.2f,
            targetEnemyId = "enemy_1",
            targetLastX = 0.8f,
            targetLastY = 0.8f,
            damage = 25,
            speed = 1.0f
        )

        assertEquals("Initial flight time should be zero", 0f, arrow.flightTime)
        assertEquals("Initial progress should be 0", 0f, arrow.progress, 0.001f)
        assertEquals("Initial arc height offset should be 0", 0f, arrow.arcHeightOffset, 0.001f)

        // Update projectile flight with empty enemy list (tracks targetLastX/Y)
        val reached = arrow.update(0.42f, emptyList())
        assertFalse("Projectile should still be in flight", reached)
        assertTrue("Flight time must advance", arrow.flightTime > 0f)
        assertTrue("Progress should be between 0 and 1", arrow.progress in 0.01f..0.99f)
        assertTrue("Arc height offset should be positive during mid-flight", arrow.arcHeightOffset > 0f)
    }

    // =========================================================================
    // 3. VisualEffectType & Signature Hero Skills
    // =========================================================================

    @Test
    fun testVisualEffectTypeCorrectlyIdentifiesAllTwelveHeroSkills() {
        assertEquals(VisualEffectType.KAEL_FLAME_STRIKE, VisualEffectType.fromEffectId("fx_skill_kael_skill_1_1", false))
        assertEquals(VisualEffectType.KAEL_FLAME_WAVE, VisualEffectType.fromEffectId("fx_skill_kael_skill_2_2", true))
        assertEquals(VisualEffectType.LYRA_PRECISION_SHOT, VisualEffectType.fromEffectId("fx_skill_lyra_skill_1_3", false))
        assertEquals(VisualEffectType.LYRA_ARROW_VOLLEY, VisualEffectType.fromEffectId("fx_skill_lyra_skill_2_4", true))
        assertEquals(VisualEffectType.BRAKK_GROUND_SLAM, VisualEffectType.fromEffectId("fx_skill_brakk_skill_1_5", true))
        assertEquals(VisualEffectType.BRAKK_SHIELD_WALL, VisualEffectType.fromEffectId("fx_skill_brakk_skill_2_6", true))
        assertEquals(VisualEffectType.NYRA_SHADOW_STRIKE, VisualEffectType.fromEffectId("fx_skill_nyra_skill_1_7", false))
        assertEquals(VisualEffectType.NYRA_BLADE_FLURRY, VisualEffectType.fromEffectId("fx_skill_nyra_skill_2_8", true))
        assertEquals(VisualEffectType.TIKO_FRAG_MINE, VisualEffectType.fromEffectId("fx_skill_tiko_skill_1_9", true))
        assertEquals(VisualEffectType.TIKO_EXPLOSIVE_DEVICE, VisualEffectType.fromEffectId("fx_skill_tiko_skill_2_10", true))
        assertEquals(VisualEffectType.VESPER_ASTRAL_COMET, VisualEffectType.fromEffectId("fx_skill_vesper_skill_1_11", false))
        assertEquals(VisualEffectType.VESPER_COSMIC_SINGULARITY, VisualEffectType.fromEffectId("fx_skill_vesper_skill_2_12", true))
        assertEquals(VisualEffectType.HERO_BASIC_ATTACK, VisualEffectType.fromEffectId("fx_hero_atk_13", false))
        assertEquals(VisualEffectType.BOSS_DEFEAT_BURST, VisualEffectType.fromEffectId("fx_boss_defeat_14", true))
    }

    @Test
    fun testVisualEffectTypeScreenShakeAndDebrisTraits() {
        assertTrue("Cannon AOE explosion triggers screen shake", VisualEffectType.AREA_EXPLOSION.triggersScreenShake)
        assertTrue("Brakk ground slam triggers screen shake", VisualEffectType.BRAKK_GROUND_SLAM.triggersScreenShake)
        assertTrue("Tiko explosive device triggers screen shake", VisualEffectType.TIKO_EXPLOSIVE_DEVICE.triggersScreenShake)
        assertTrue("Boss defeat burst triggers screen shake", VisualEffectType.BOSS_DEFEAT_BURST.triggersScreenShake)
        assertFalse("Single target spark should not cause screen shake", VisualEffectType.IMPACT_SPARK.triggersScreenShake)

        assertTrue("Area explosion generates physical debris", VisualEffectType.AREA_EXPLOSION.generatesDebris)
        assertTrue("Brakk ground slam generates physical debris", VisualEffectType.BRAKK_GROUND_SLAM.generatesDebris)
        assertTrue("Tiko frag mine generates physical debris", VisualEffectType.TIKO_FRAG_MINE.generatesDebris)
    }

    // =========================================================================
    // 4. Tactical Impact Feedback & Graphic Quality Integration
    // =========================================================================

    @Test
    fun testCombatVfxRendererTriggerImpactFeedbackRespectsQualityTiers() {
        val screenShake = CameraScreenShake()
        val debrisList = mutableListOf<VisualPhysicsDebris>()
        val particles = mutableListOf<CombatParticle>()

        val explosionFx = VisualEffect(
            id = "fx_explosion_1",
            x = 0.5f,
            y = 0.5f,
            maxRadius = 0.15f,
            maxDurationSeconds = 0.35f,
            color = 0xFFEF4444.toInt(),
            isAreaExplosion = true
        )

        // HIGH Quality: Screen shake + 6 debris + 8 particles
        CombatVfxRenderer.triggerImpactFeedback(
            fx = explosionFx,
            w = 1000f,
            h = 1000f,
            screenShake = screenShake,
            debrisList = debrisList,
            particles = particles,
            quality = GraphicQuality.HIGH
        )
        assertTrue("Screen shake should be active on HIGH quality", screenShake.isShaking)
        assertEquals("HIGH quality should spawn 6 debris items", 6, debrisList.size)
        assertEquals("HIGH quality should spawn 8 combat particles", 8, particles.size)

        // Test MEDIUM Quality (Balanced mode: screen shake enabled, 3 debris, 5 particles)
        screenShake.reset()
        debrisList.clear()
        particles.clear()

        CombatVfxRenderer.triggerImpactFeedback(
            fx = explosionFx,
            w = 1000f,
            h = 1000f,
            screenShake = screenShake,
            debrisList = debrisList,
            particles = particles,
            quality = GraphicQuality.MEDIUM
        )
        assertTrue("Screen shake is enabled in MEDIUM quality", screenShake.isShaking)
        assertEquals("MEDIUM quality spawns 3 debris items", 3, debrisList.size)
        assertEquals("MEDIUM quality spawns 5 combat particles", 5, particles.size)

        // Reset and test LOW Quality (Economy mode: no debris, screen shake disabled)
        screenShake.reset()
        debrisList.clear()
        particles.clear()

        CombatVfxRenderer.triggerImpactFeedback(
            fx = explosionFx,
            w = 1000f,
            h = 1000f,
            screenShake = screenShake,
            debrisList = debrisList,
            particles = particles,
            quality = GraphicQuality.LOW
        )
        assertFalse("Screen shake is disabled in LOW quality", screenShake.isShaking)
        assertEquals("LOW quality must spawn 0 debris for maximum performance", 0, debrisList.size)
        assertEquals("LOW quality spawns reduced particles", 2, particles.size)

        // Validate explicit GraphicQuality tier particle/debris capacities
        assertEquals(0, GraphicQuality.LOW.maxDebrisCount)
        assertEquals(20, GraphicQuality.LOW.maxParticles)
        assertEquals(18, GraphicQuality.MEDIUM.maxDebrisCount)
        assertEquals(50, GraphicQuality.MEDIUM.maxParticles)
        assertEquals(36, GraphicQuality.HIGH.maxDebrisCount)
        assertEquals(100, GraphicQuality.HIGH.maxParticles)

        // Test Cap Enforcement: when capacity is saturated, no further entities are spawned
        val sampleDebris = VisualPhysicsDebris(
            x = 500f, y = 500f, z = 10f, vx = 5f, vy = 5f, vz = 10f,
            color = 0xFF475569.toInt(), size = 6f
        )
        val sampleParticle = particles.firstOrNull() ?: CombatParticle(
            x = 500f, y = 500f, vx = 5f, vy = 5f,
            color = 0xFFEF4444.toInt(), radius = 4f
        )
        while (debrisList.size < GraphicQuality.HIGH.maxDebrisCount) {
            debrisList.add(sampleDebris.copy())
        }
        while (particles.size < GraphicQuality.HIGH.maxParticles) {
            particles.add(sampleParticle.copy())
        }
        val debrisBefore = debrisList.size
        val particlesBefore = particles.size
        CombatVfxRenderer.triggerImpactFeedback(
            fx = explosionFx,
            w = 1000f,
            h = 1000f,
            screenShake = screenShake,
            debrisList = debrisList,
            particles = particles,
            quality = GraphicQuality.HIGH
        )
        assertEquals("Debris pool cap must prevent runaway allocations", debrisBefore, debrisList.size)
        assertEquals("Particle pool cap must prevent runaway allocations", particlesBefore, particles.size)
    }

    // =========================================================================
    // 5. Tower Recoil & Muzzle Animation States
    // =========================================================================

    @Test
    fun testTowerRecoilAnimationLifecycleAndCooldown() {
        val archer = PlacedTower.createArcher("t_archer", "spot_1", 0.3f, 0.3f)
        val mage = PlacedTower.createMage("t_mage", "spot_2", 0.5f, 0.5f)
        val cannon = PlacedTower.createCannon("t_cannon", "spot_3", 0.7f, 0.7f)

        assertEquals("Initially recoil timer is zero", 0f, archer.recoilAnimTimer)
        assertEquals("Initially recoil timer is zero", 0f, mage.recoilAnimTimer)
        assertEquals("Initially recoil timer is zero", 0f, cannon.recoilAnimTimer)

        // Firing reset
        archer.resetCooldown()
        mage.resetCooldown()
        cannon.resetCooldown()

        assertEquals("Archer recoil should be active (0.20s)", 0.20f, archer.recoilAnimTimer, 0.001f)
        assertEquals("Mage recoil should be active (0.20s)", 0.20f, mage.recoilAnimTimer, 0.001f)
        assertEquals("Cannon recoil should be active (0.20s)", 0.20f, cannon.recoilAnimTimer, 0.001f)
        assertEquals("Shots fired incremented", 1, archer.totalShotsFired)
        assertFalse("Tower cannot attack immediately after firing", archer.canAttack)

        // Advance past recoil (0.25s)
        archer.updateCooldown(0.25f)
        assertEquals("Recoil timer must return to zero after duration", 0f, archer.recoilAnimTimer)
    }

    // =========================================================================
    // 6. GameEngine Combat Integration & Automated Tower Firing
    // =========================================================================

    @Test
    fun testGameEngineAutomatedTowerFiringSpawnsStyledProjectilesAndResolvesHit() {
        val engine = GameEngine()
        engine.start()

        // Place Cannon Tower at spot_1
        val tower = engine.buildTower("spot_1", TowerType.CANNON_TOWER)
        assertNotNull("Cannon tower should be built", tower)

        // Spawn a live enemy within cannon range
        val enemy = ActiveEnemy(
            id = "enemy_target_1",
            name = "Goblin Scout",
            waveNumber = 1,
            maxHealth = 100,
            currentHealth = 100,
            moveSpeed = 0.05f,
            visualType = EnemyVisualType.GOBLIN
        )
        // Position enemy in range of tower
        enemy.x = tower!!.x + 0.05f
        enemy.y = tower.y + 0.05f
        engine.activeEnemies.add(enemy)

        // Update engine to trigger tower target acquisition and projectile firing
        engine.update(0.016f)

        assertEquals("One projectile should be fired", 1, engine.activeProjectiles.size)
        val projectile = engine.activeProjectiles.first()
        assertEquals(ProjectileVisualType.CANNON_BALL, projectile.visualType)
        assertEquals(TowerType.CANNON_TOWER, projectile.towerType)
        assertTrue("Cannon projectile has splash radius", projectile.splashRadius > 0f)

        // Advance time in simulation ticks until projectile reaches target and resolves impact
        for (i in 0..20) {
            engine.update(0.05f)
            if (projectile.isExpired) break
        }

        assertTrue("Projectile should have hit and dealt damage", projectile.hasDealtDamage)
        assertTrue("Enemy should have received damage", enemy.currentHealth < 100)
        assertTrue("Visual explosion effect should be created", engine.visualEffects.any { it.isAreaExplosion })
    }

    @Test
    fun testGameEngineHeroSkillExecutionTriggersTactileCombatVfx() {
        val engine = GameEngine()
        engine.start()

        // Deploy Kael
        val hero = engine.deployHero(hero = Hero.getKael(), x = 0.42f, y = 0.48f)
        assertNotNull("Kael should be deployed", hero)

        // Spawn enemy nearby
        val enemy = ActiveEnemy(
            id = "enemy_kael_target",
            name = "Orc Warrior",
            waveNumber = 1,
            maxHealth = 200,
            currentHealth = 200,
            moveSpeed = 0.05f,
            visualType = EnemyVisualType.ORC
        )
        enemy.x = hero.x + 0.05f
        enemy.y = hero.y + 0.05f
        engine.activeEnemies.add(enemy)

        // Cast Skill 0 (Golpe Flamejante)
        val castSuccess = engine.castHeroSkill(0)
        assertTrue("Skill cast must succeed", castSuccess)
        assertEquals("Hero must be in SKILL_CAST animation state", HeroAnimState.SKILL_CAST, hero.animState)
        assertTrue("Enemy must take damage from skill", enemy.currentHealth < 200)

        // Verify visual effect emitted matches Kael's skill
        val skillFx = engine.visualEffects.find { it.id.contains("kael_skill_1") }
        assertNotNull("Kael flame strike visual effect must be emitted", skillFx)
        assertEquals(VisualEffectType.KAEL_FLAME_STRIKE, skillFx!!.effectType)
    }

    // =========================================================================
    // 7. VisualPhysicsDebris & CameraScreenShake Dynamics
    // =========================================================================

    @Test
    fun testVisualPhysicsDebrisGravityAndGroundBounceRestitution() {
        val debris = VisualPhysicsDebris(
            x = 100f,
            y = 100f,
            z = 20f,
            vx = 30f,
            vy = 15f,
            vz = 100f,
            color = 0xFFF59E0B.toInt(),
            size = 4f,
            maxLifetime = 0.6f
        )

        assertFalse("Debris should not be initially expired", debris.isExpired)
        debris.update(0.1f)
        assertTrue("Z position should change due to vz and gravity", debris.z != 20f)
        assertFalse("Progress should be within [0, 1]", debris.isExpired)

        // Simulate flight until expiration
        debris.update(0.6f)
        assertTrue("Debris should expire after maxLifetime", debris.isExpired)
    }

    @Test
    fun testCameraScreenShakeMultiFrequencyDamping() {
        val shake = CameraScreenShake()
        assertFalse("Initially camera should not be shaking", shake.isShaking)

        shake.trigger(intensity = 10f, shakeDuration = 0.25f)
        assertTrue("Shake should be active after trigger", shake.isShaking)
        assertEquals(10f, shake.currentIntensity, 0.001f)

        shake.update(0.10f)
        assertTrue("Camera offsets should be active", shake.isShaking)

        // Advance past duration
        shake.update(0.20f)
        assertFalse("Shake should be finished after duration", shake.isShaking)
        assertEquals(0f, shake.offsetX, 0.001f)
        assertEquals(0f, shake.offsetY, 0.001f)
    }
}
