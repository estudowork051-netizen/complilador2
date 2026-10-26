package com.heroes.towerdefense

import com.heroes.towerdefense.game.engine.GameEngine
import com.heroes.towerdefense.game.map.StageOneMap
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.DeployedHero
import com.heroes.towerdefense.game.model.HeroAnimState
import com.heroes.towerdefense.game.model.WaveConfig
import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.hero.HeroClass
import com.heroes.towerdefense.model.hero.HeroRarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit Tests for Visual Upgrade Part 1:
 * - HeroAnimState lifecycle & isAttacking property
 * - DeployedHero articulated animation state machine & timing transitions (Idle -> Windup -> Strike -> Recovery -> Idle)
 * - Directional orientation (facingDirection left/right) relative to enemy targets
 * - Skill casting animation triggers and recovery
 * - Victory celebration state persistence
 * - Combat integration with GameEngine (attack triggers, skill triggers, pause stability, restart reset)
 * - Roster visual identity integrity across all 6 fantasy heroes
 */
class HeroVisualUpgradePart1Test {

    private lateinit var engine: GameEngine
    private lateinit var kael: Hero
    private lateinit var lyra: Hero

    @Before
    fun setUp() {
        val heroes = Hero.getPlannedHeroes()
        kael = heroes.first { it.id == "hero_kael" }
        lyra = heroes.first { it.id == "hero_lyra" }

        engine = GameEngine(
            waypoints = StageOneMap.waypoints,
            waves = listOf(
                WaveConfig(
                    waveNumber = 1,
                    enemyCount = 3,
                    spawnIntervalSeconds = 2.0f
                )
            ),
            selectedHero = kael
        )
    }

    // =========================================================================
    // 1. HeroAnimState Properties & Definitions
    // =========================================================================

    @Test
    fun testHeroAnimStateProperties() {
        assertFalse(HeroAnimState.IDLE.isAttacking)
        assertTrue(HeroAnimState.WINDUP.isAttacking)
        assertTrue(HeroAnimState.STRIKE.isAttacking)
        assertTrue(HeroAnimState.RECOVERY.isAttacking)
        assertFalse(HeroAnimState.SKILL_CAST.isAttacking)
        assertFalse(HeroAnimState.VICTORY.isAttacking)
    }

    // =========================================================================
    // 2. DeployedHero Initial Animation State
    // =========================================================================

    @Test
    fun testDeployedHeroInitialAnimationState() {
        val deployed = DeployedHero(hero = kael, x = 0.42f, y = 0.48f)

        assertEquals(HeroAnimState.IDLE, deployed.animState)
        assertEquals(0f, deployed.animTimer, 0.001f)
        assertEquals(0f, deployed.stateTimer, 0.001f)
        assertEquals(1f, deployed.facingDirection, 0.001f)
        assertNull(deployed.activeCastSkill)
        assertTrue(deployed.canAttack)
    }

    // =========================================================================
    // 3. Attack Animation Cycle (Windup -> Strike -> Recovery -> Idle)
    // =========================================================================

    @Test
    fun testAttackAnimationCycleTransitions() {
        val deployed = DeployedHero(hero = kael, x = 0.42f, y = 0.48f)

        // 1. Trigger Attack to the left of the hero
        deployed.triggerAttack(targetX = 0.20f, targetY = 0.48f)
        assertEquals(HeroAnimState.WINDUP, deployed.animState)
        assertEquals(0f, deployed.stateTimer, 0.001f)
        assertEquals(-1f, deployed.facingDirection, 0.001f) // Faces left!

        // 2. Advance 0.13s (Windup is 0.12s) -> Should transition to STRIKE
        deployed.update(0.13f)
        assertEquals(HeroAnimState.STRIKE, deployed.animState)
        assertTrue(deployed.stateTimer < 0.05f)

        // 3. Advance 0.19s (Strike is 0.18s) -> Should transition to RECOVERY
        deployed.update(0.19f)
        assertEquals(HeroAnimState.RECOVERY, deployed.animState)

        // 4. Advance 0.16s (Recovery is 0.15s) -> Should transition back to IDLE
        deployed.update(0.16f)
        assertEquals(HeroAnimState.IDLE, deployed.animState)
    }

    // =========================================================================
    // 4. Directional Facing Orientation
    // =========================================================================

    @Test
    fun testFacingDirectionCalculation() {
        val deployed = DeployedHero(hero = kael, x = 0.50f, y = 0.50f)

        // Target to right (0.80f > 0.50f) -> Facing direction = 1.0f
        deployed.triggerAttack(targetX = 0.80f, targetY = 0.50f)
        assertEquals(1f, deployed.facingDirection, 0.001f)

        // Target to left (0.20f < 0.50f) -> Facing direction = -1.0f
        deployed.triggerAttack(targetX = 0.20f, targetY = 0.50f)
        assertEquals(-1f, deployed.facingDirection, 0.001f)
    }

    // =========================================================================
    // 5. Skill Casting Animation Trigger & Recovery
    // =========================================================================

    @Test
    fun testSkillCastAnimationCycle() {
        val deployed = DeployedHero(hero = lyra, x = 0.42f, y = 0.48f)
        val skill = lyra.skills.first()

        deployed.triggerSkill(skill, targetX = 0.15f, targetY = 0.48f)
        assertEquals(HeroAnimState.SKILL_CAST, deployed.animState)
        assertEquals(skill, deployed.activeCastSkill)
        assertEquals(-1f, deployed.facingDirection, 0.001f)

        // Advance 0.30s (skill cast still active)
        deployed.update(0.30f)
        assertEquals(HeroAnimState.SKILL_CAST, deployed.animState)

        // Advance remaining 0.30s (total 0.60s > 0.55s) -> Should return to IDLE
        deployed.update(0.30f)
        assertEquals(HeroAnimState.IDLE, deployed.animState)
        assertNull(deployed.activeCastSkill)
    }

    // =========================================================================
    // 6. Victory State Persistence
    // =========================================================================

    @Test
    fun testVictoryAnimationStatePersistence() {
        val deployed = DeployedHero(hero = kael, x = 0.42f, y = 0.48f)

        deployed.triggerVictory()
        assertEquals(HeroAnimState.VICTORY, deployed.animState)

        // Advance time: should remain in VICTORY celebration loop
        deployed.update(1.5f)
        assertEquals(HeroAnimState.VICTORY, deployed.animState)
        assertTrue(deployed.animTimer >= 1.5f)
    }

    // =========================================================================
    // 7. Reset Animation Method
    // =========================================================================

    @Test
    fun testResetAnimationRestoresIdle() {
        val deployed = DeployedHero(hero = kael, x = 0.42f, y = 0.48f)
        deployed.triggerVictory()
        assertEquals(HeroAnimState.VICTORY, deployed.animState)

        deployed.resetAnimation()
        assertEquals(HeroAnimState.IDLE, deployed.animState)
        assertEquals(0f, deployed.stateTimer, 0.001f)
        assertNull(deployed.activeCastSkill)
    }

    // =========================================================================
    // 8. Combat Integration with GameEngine (Attacks & Skills)
    // =========================================================================

    @Test
    fun testGameEngineTriggersAttackAnimationWhenTargetAcquired() {
        val deployed = engine.deployHero(hero = kael, x = 0.20f, y = 0.22f)
        engine.start()

        // Spawn enemy inside hero range
        val enemy = ActiveEnemy(
            id = "test_goblin",
            name = "Goblin Scout",
            waveNumber = 1,
            maxHealth = 100,
            currentHealth = 100
        )
        enemy.x = 0.20f
        enemy.y = 0.24f
        engine.activeEnemies.add(enemy)

        assertEquals(HeroAnimState.IDLE, deployed.animState)

        // Update engine: hero attacks enemy
        engine.update(0.05f)

        // Hero must have entered attack animation
        assertTrue("Hero must be in attack state after firing", deployed.animState.isAttacking)
        assertTrue(deployed.totalDamageDealt > 0)
    }

    @Test
    fun testGameEngineCastSkillTriggersSkillCastAnimation() {
        val deployed = engine.deployHero(hero = kael, x = 0.42f, y = 0.48f)
        engine.start()

        val enemy = ActiveEnemy(
            id = "test_target",
            name = "Target Goblin",
            waveNumber = 1,
            maxHealth = 100,
            currentHealth = 100
        )
        enemy.x = 0.42f
        enemy.y = 0.50f
        engine.activeEnemies.add(enemy)

        val castSuccess = engine.castHeroSkill(0)
        assertTrue("Skill 1 cast must succeed", castSuccess)
        assertEquals(HeroAnimState.SKILL_CAST, deployed.animState)
        assertNotNull(deployed.activeCastSkill)
    }

    @Test
    fun testRestartResetsDeployedHeroToIdle() {
        val deployed = engine.deployHero(hero = kael, x = 0.42f, y = 0.48f)
        engine.start()
        deployed.triggerVictory()
        assertEquals(HeroAnimState.VICTORY, deployed.animState)

        engine.restart()
        val freshHero = engine.deployedHero
        assertNotNull(freshHero)
        assertEquals(HeroAnimState.IDLE, freshHero!!.animState)
    }

    // =========================================================================
    // 9. Roster Visual Identity & Rarity Distinctness Across All 6 Heroes
    // =========================================================================

    @Test
    fun testAllSixHeroesHaveDistinctVisualIdentitiesAndAttributes() {
        val roster = Hero.getPlannedHeroes()
        assertEquals(6, roster.size)

        val ids = roster.map { it.id }.toSet()
        assertEquals(6, ids.size)

        val names = roster.map { it.name }.toSet()
        assertEquals(6, names.size)

        // Verify each hero matches canonical identity
        val kael = roster.first { it.id == "hero_kael" }
        assertEquals(HeroClass.WARRIOR, kael.heroClass)
        assertEquals(HeroRarity.COMMON, kael.rarity)
        assertEquals("Cavaleiro do Fogo", kael.title)

        val lyra = roster.first { it.id == "hero_lyra" }
        assertEquals(HeroClass.ARCHER, lyra.heroClass)
        assertEquals(HeroRarity.RARE, lyra.rarity)
        assertEquals("Sentinela dos Bosques", lyra.title)

        val brakk = roster.first { it.id == "hero_brakk" }
        assertEquals(HeroClass.WARRIOR, brakk.heroClass)
        assertEquals(HeroRarity.EPIC, brakk.rarity)
        assertEquals("Guardião das Montanhas", brakk.title)

        val nyra = roster.first { it.id == "hero_nyra" }
        assertEquals(HeroClass.MAGE, nyra.heroClass)
        assertEquals(HeroRarity.EPIC, nyra.rarity)
        assertEquals("Arquimaga da Tempestade", nyra.title)

        val tiko = roster.first { it.id == "hero_tiko" }
        assertEquals(HeroClass.ALCHEMIST, tiko.heroClass)
        assertEquals(HeroRarity.LEGENDARY, tiko.rarity)
        assertEquals("Artífice Alquimista", tiko.title)

        val vesper = roster.first { it.id == "hero_vesper" }
        assertEquals(HeroClass.ASSASSIN, vesper.heroClass)
        assertEquals(HeroRarity.MYTHIC, vesper.rarity)
        assertEquals("Caçadora Espectral", vesper.title)

        for (hero in roster) {
            assertTrue("Hero ${hero.name} attack power must be positive", hero.attackPower > 0)
            assertTrue("Hero ${hero.name} attack range must be positive", hero.attackRange > 0f)
            assertTrue("Hero ${hero.name} attack speed must be positive", hero.attackSpeed > 0f)
            assertEquals("Hero ${hero.name} must possess exactly 2 active skills", 2, hero.skills.size)
        }
    }
}
