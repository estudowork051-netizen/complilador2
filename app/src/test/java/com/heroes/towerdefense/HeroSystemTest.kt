package com.heroes.towerdefense

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.game.engine.GameEngine
import com.heroes.towerdefense.game.engine.GameEngineListener
import com.heroes.towerdefense.game.map.StageOneMap
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.DeployedHero
import com.heroes.towerdefense.game.model.GameStatus
import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.hero.HeroClass
import com.heroes.towerdefense.model.hero.HeroRarity
import com.heroes.towerdefense.model.hero.HeroSkill
import com.heroes.towerdefense.model.hero.SkillTargetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive automated tests for Part 5 (Hero System, Rarities, and Active Skills).
 * Validates all 14 mandatory test points:
 * 1. Hero Creation & Attributes
 * 2. 5 Rarity Tiers & Multipliers
 * 3. 6 Planned Heroes Roster
 * 4. 12 Unique Active Skills
 * 5. Level Up & Stat Scaling
 * 6. Hero Deployment in GameEngine
 * 7. Skill 1 Cast & Damage
 * 8. Skill 2 Cast & Damage
 * 9. Cooldown Lockout
 * 10. Cooldown Countdown Tick
 * 11. AoE Damage Safety (No duplicate hits)
 * 12. Single-Target Priority Damage
 * 13. Hero Automated Basic Combat
 * 14. Defensive Fortification Skill
 */
class HeroSystemTest {

    private lateinit var engine: GameEngine
    private var lastDeployedHero: DeployedHero? = null
    private var lastCastSkill: HeroSkill? = null
    private var lastSkillTargets: List<ActiveEnemy> = emptyList()
    private var lastBaseHealth: Int = -1

    @Before
    fun setUp() {
        lastDeployedHero = null
        lastCastSkill = null
        lastSkillTargets = emptyList()
        lastBaseHealth = -1

        engine = GameEngine(
            waypoints = StageOneMap.waypoints,
            listener = object : GameEngineListener {
                override fun onHeroDeployed(hero: DeployedHero) {
                    lastDeployedHero = hero
                }

                override fun onHeroSkillCast(hero: DeployedHero, skill: HeroSkill, targetsHit: List<ActiveEnemy>) {
                    lastCastSkill = skill
                    lastSkillTargets = targetsHit
                }

                override fun onBaseHealthChanged(currentHealth: Int, maxHealth: Int) {
                    lastBaseHealth = currentHealth
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // Test 1: Hero Creation and Attributes
    // -------------------------------------------------------------------------
    @Test
    fun testHeroAttributesAndModelCreation() {
        val skill = HeroSkill(
            id = "test_skill",
            name = "Lâmina Sagrada",
            description = "Corte luminoso de teste",
            cooldownSeconds = 5.0f,
            damage = 100,
            range = 0.30f
        )
        val hero = Hero(
            id = "hero_custom_01",
            name = "Sir Arthur",
            heroClass = HeroClass.WARRIOR,
            baseHealth = 600,
            attackPower = 50,
            attackRange = 0.25f,
            attackSpeed = 1.2f,
            skillName = "Lâmina Sagrada",
            level = 1,
            title = "Paladino da Luz",
            rarity = HeroRarity.EPIC,
            skills = listOf(skill)
        )

        assertEquals("hero_custom_01", hero.id)
        assertEquals("Sir Arthur", hero.name)
        assertEquals(HeroClass.WARRIOR, hero.heroClass)
        assertEquals(HeroRarity.EPIC, hero.rarity)
        assertEquals(600, hero.baseHealth)
        assertEquals(50, hero.attackPower)
        assertEquals(0.25f, hero.attackRange, 0.001f)
        assertEquals(1.2f, hero.attackSpeed, 0.001f)
        assertEquals(1, hero.skills.size)
        assertTrue(hero.calculateDps() > 0f)
    }

    // -------------------------------------------------------------------------
    // Test 2: Rarity System (5 Tiers & Progression Values)
    // -------------------------------------------------------------------------
    @Test
    fun testHeroRaritySystemFiveTiers() {
        val rarities = HeroRarity.values()
        assertEquals("Expected exactly 5 rarity tiers", 5, rarities.size)

        val expectedRarities = listOf(
            HeroRarity.COMMON,
            HeroRarity.RARE,
            HeroRarity.EPIC,
            HeroRarity.LEGENDARY,
            HeroRarity.MYTHIC
        )
        assertEquals(expectedRarities, rarities.toList())

        for (rarity in rarities) {
            assertTrue("Display name must not be blank", rarity.displayName.isNotBlank())
            assertTrue("Badge label must not be blank", rarity.badgeLabel.isNotBlank())
            assertTrue("Color hex must start with #", rarity.colorHex.startsWith("#"))
            assertTrue("Color int must not be 0", rarity.colorInt != 0)
            assertTrue("Stat multiplier must be >= 1.0", rarity.statMultiplier >= 1.0f)
        }

        // Multipliers must strictly increase with rarity
        assertTrue(HeroRarity.COMMON.statMultiplier < HeroRarity.RARE.statMultiplier)
        assertTrue(HeroRarity.RARE.statMultiplier < HeroRarity.EPIC.statMultiplier)
        assertTrue(HeroRarity.EPIC.statMultiplier < HeroRarity.LEGENDARY.statMultiplier)
        assertTrue(HeroRarity.LEGENDARY.statMultiplier < HeroRarity.MYTHIC.statMultiplier)
    }

    // -------------------------------------------------------------------------
    // Test 3: 6 Planned Heroes Roster
    // -------------------------------------------------------------------------
    @Test
    fun testPlannedHeroRosterSixHeroes() {
        val roster = Hero.getPlannedHeroes()
        assertEquals("Expected 6 canonical heroes", 6, roster.size)

        val expectedNames = listOf("Kael", "Lyra", "Brakk", "Nyra", "Tiko", "Vesper")
        assertEquals(expectedNames, roster.map { it.name })

        for (hero in roster) {
            assertTrue("Hero name must not be blank", hero.name.isNotBlank())
            assertTrue("Hero title must not be blank", hero.title.isNotBlank())
            assertTrue("Hero base health must be positive", hero.baseHealth > 0)
            assertTrue("Hero attack power must be positive", hero.attackPower > 0)
            assertTrue("Hero attack range must be positive", hero.attackRange > 0f)
            assertTrue("Hero attack speed must be positive", hero.attackSpeed > 0f)
            assertNotNull("Hero must have assigned rarity", hero.rarity)
            assertEquals("Each hero must possess exactly 2 active skills", 2, hero.skills.size)
        }
    }

    // -------------------------------------------------------------------------
    // Test 4: All 12 Unique Skills Configured
    // -------------------------------------------------------------------------
    @Test
    fun testAllTwelveSkillsUniqueAndConfigured() {
        val roster = Hero.getPlannedHeroes()
        val allSkills = roster.flatMap { it.skills }

        assertEquals("Expected exactly 12 unique skills (2 per hero)", 12, allSkills.size)

        val uniqueIds = allSkills.map { it.id }.toSet()
        assertEquals("All skill IDs must be distinct", 12, uniqueIds.size)

        for (skill in allSkills) {
            assertTrue("Skill ID must not be blank", skill.id.isNotBlank())
            assertTrue("Skill name must not be blank", skill.name.isNotBlank())
            assertTrue("Skill description must not be blank", skill.description.isNotBlank())
            assertTrue("Skill cooldown must be > 0", skill.cooldownSeconds > 0f)
            assertTrue("Skill damage must be >= 0", skill.damage >= 0)
            assertNotNull("Skill target type must not be null", skill.targetType)
        }
    }

    // -------------------------------------------------------------------------
    // Test 5: Hero Level Up and Stat Scaling
    // -------------------------------------------------------------------------
    @Test
    fun testHeroLevelUpAndProgressionFormulas() {
        val hero = Hero(
            id = "hero_progression_test",
            name = "Tester",
            heroClass = HeroClass.WARRIOR,
            baseHealth = 500,
            attackPower = 40,
            attackRange = 0.25f,
            attackSpeed = 1.0f,
            skillName = "Teste",
            level = 1,
            rarity = HeroRarity.EPIC
        )

        assertEquals(1, hero.level)
        val initialDps = hero.calculateDps()
        val initialHealth = hero.calculateHealth()
        val initialAttack = hero.calculateAttackPower()

        hero.levelUp()
        assertEquals(2, hero.level)

        val upgradedDps = hero.calculateDps()
        val upgradedHealth = hero.calculateHealth()
        val upgradedAttack = hero.calculateAttackPower()

        assertTrue("Upgraded DPS must exceed initial DPS", upgradedDps > initialDps)
        assertTrue("Upgraded health must exceed initial health", upgradedHealth > initialHealth)
        assertTrue("Upgraded attack must exceed initial attack", upgradedAttack > initialAttack)
    }

    // -------------------------------------------------------------------------
    // Test 6: Hero Deployment in GameEngine
    // -------------------------------------------------------------------------
    @Test
    fun testHeroDeploymentInGameEngine() {
        val kael = Hero.getPlannedHeroes().first()
        val deployed = engine.deployHero(hero = kael, x = 0.40f, y = 0.50f)

        assertNotNull("Deployed hero in engine must not be null", engine.deployedHero)
        assertEquals(kael.id, engine.deployedHero?.hero?.id)
        assertEquals(0.40f, deployed.x, 0.001f)
        assertEquals(0.50f, deployed.y, 0.001f)
        assertEquals(deployed, lastDeployedHero)
        assertEquals(2, deployed.skills.size)
    }

    // -------------------------------------------------------------------------
    // Test 7: Skill 1 Casting Triggers Cooldown and Deals Damage
    // -------------------------------------------------------------------------
    @Test
    fun testSkillOneCastingTriggersCooldownAndDamage() {
        engine.start()
        val kael = Hero.getPlannedHeroes().first() // Kael: Skill 1 = Golpe Flamejante (single-target, 120 damage)
        val deployed = engine.deployHero(hero = kael, x = 0.40f, y = 0.50f)

        val enemy = ActiveEnemy(
            id = "target_goblin_01",
            name = "Goblin",
            waveNumber = 1,
            maxHealth = 200,
            currentHealth = 200,
            moveSpeed = 0.15f
        )
        enemy.x = 0.42f
        enemy.y = 0.50f
        engine.activeEnemies.add(enemy)

        val skill1State = deployed.getSkill(0)
        assertNotNull(skill1State)
        assertTrue("Skill 1 should initially be ready", skill1State!!.isReady)

        val castSuccess = engine.castHeroSkill(0)
        assertTrue("Skill 1 cast should succeed", castSuccess)
        assertFalse("Skill 1 should now be on cooldown", skill1State.isReady)
        assertTrue("Skill 1 cooldown should be positive", skill1State.currentCooldown > 0f)

        assertEquals("Enemy should have taken 120 damage", 80, enemy.currentHealth)
        assertEquals(120, deployed.totalDamageDealt)
        assertEquals("kael_skill_1", lastCastSkill?.id)
    }

    // -------------------------------------------------------------------------
    // Test 8: Skill 2 Casting Triggers Cooldown and Deals Damage
    // -------------------------------------------------------------------------
    @Test
    fun testSkillTwoCastingTriggersCooldownAndDamage() {
        engine.start()
        val kael = Hero.getPlannedHeroes().first() // Kael: Skill 2 = Onda de Fogo (AoE, 80 damage)
        val deployed = engine.deployHero(hero = kael, x = 0.40f, y = 0.50f)

        val enemy = ActiveEnemy(
            id = "target_goblin_02",
            name = "Goblin",
            waveNumber = 1,
            maxHealth = 150,
            currentHealth = 150,
            moveSpeed = 0.15f
        )
        enemy.x = 0.44f
        enemy.y = 0.50f
        engine.activeEnemies.add(enemy)

        val skill2State = deployed.getSkill(1)
        assertNotNull(skill2State)
        assertTrue("Skill 2 should initially be ready", skill2State!!.isReady)

        val castSuccess = engine.castHeroSkill(1)
        assertTrue("Skill 2 cast should succeed", castSuccess)
        assertFalse("Skill 2 should now be on cooldown", skill2State.isReady)

        assertEquals("Enemy should have taken 80 AoE damage", 70, enemy.currentHealth)
        assertEquals("kael_skill_2", lastCastSkill?.id)
    }

    // -------------------------------------------------------------------------
    // Test 9: Skill Cannot Be Cast While on Cooldown
    // -------------------------------------------------------------------------
    @Test
    fun testSkillCannotBeCastWhileOnCooldown() {
        engine.start()
        val kael = Hero.getPlannedHeroes().first()
        engine.deployHero(hero = kael, x = 0.40f, y = 0.50f)

        val firstCast = engine.castHeroSkill(0)
        assertTrue("First cast must succeed", firstCast)

        // Immediate second cast without ticking time must fail
        val secondCast = engine.castHeroSkill(0)
        assertFalse("Second cast while on cooldown must fail", secondCast)
    }

    // -------------------------------------------------------------------------
    // Test 10: Skill Cooldown Decrements With Simulation Ticks
    // -------------------------------------------------------------------------
    @Test
    fun testSkillCooldownCountdownOverTime() {
        engine.start()
        val kael = Hero.getPlannedHeroes().first()
        val deployed = engine.deployHero(hero = kael, x = 0.40f, y = 0.50f)

        val skillState = deployed.getSkill(0)!!
        engine.castHeroSkill(0)

        val initialCooldown = skillState.currentCooldown
        assertTrue("Cooldown should be at full duration", initialCooldown > 0f)

        // Advance 2 seconds
        engine.update(0.1f) // Tick
        skillState.update(2.0f) // Simulate 2s
        assertTrue("Remaining cooldown should have decreased", skillState.currentCooldown < initialCooldown)

        // Reset or tick past total cooldown
        skillState.update(skillState.skill.cooldownSeconds + 1.0f)
        assertTrue("Skill should be ready again once cooldown reaches 0", skillState.isReady)

        val thirdCast = engine.castHeroSkill(0)
        assertTrue("Skill should be castable again once ready", thirdCast)
    }

    // -------------------------------------------------------------------------
    // Test 11: AoE Skill Hits Multiple Enemies Without Duplicate Damage
    // -------------------------------------------------------------------------
    @Test
    fun testAoESkillHitsMultipleEnemiesWithoutDuplicateDamage() {
        engine.start()
        val kael = Hero.getPlannedHeroes().first() // Onda de Fogo = 80 AoE damage
        val deployed = engine.deployHero(hero = kael, x = 0.40f, y = 0.50f)

        val enemy1 = ActiveEnemy(id = "e1", name = "G1", waveNumber = 1, maxHealth = 100, currentHealth = 100, moveSpeed = 0.15f)
        enemy1.x = 0.41f
        enemy1.y = 0.50f

        val enemy2 = ActiveEnemy(id = "e2", name = "G2", waveNumber = 1, maxHealth = 100, currentHealth = 100, moveSpeed = 0.15f)
        enemy2.x = 0.42f
        enemy2.y = 0.50f

        val enemy3 = ActiveEnemy(id = "e3", name = "G3", waveNumber = 1, maxHealth = 100, currentHealth = 100, moveSpeed = 0.15f)
        enemy3.x = 0.43f
        enemy3.y = 0.50f

        engine.activeEnemies.addAll(listOf(enemy1, enemy2, enemy3))

        val castSuccess = engine.castHeroSkill(1) // AoE skill
        assertTrue(castSuccess)

        // All 3 enemies in range must take exactly 80 damage (100 - 80 = 20)
        assertEquals(20, enemy1.currentHealth)
        assertEquals(20, enemy2.currentHealth)
        assertEquals(20, enemy3.currentHealth)

        // Total hero damage dealt must be exactly 3 * 80 = 240 (verifying no duplicate hits on any enemy)
        assertEquals(240, deployed.totalDamageDealt)
        assertEquals(3, lastSkillTargets.size)
    }

    // -------------------------------------------------------------------------
    // Test 12: Single-Target Skill Hits Prioritized Enemy
    // -------------------------------------------------------------------------
    @Test
    fun testSingleTargetSkillHitsPrioritizedEnemy() {
        engine.start()
        val lyra = Hero.getPlannedHeroes()[1] // Lyra: Tiro de Precisão (140 damage)
        val deployed = engine.deployHero(hero = lyra, x = 0.40f, y = 0.50f)

        // Enemy 1: Further along the path (distanceTraveled = 2.0)
        val enemy1 = ActiveEnemy(id = "p1", name = "G1", waveNumber = 1, maxHealth = 200, currentHealth = 200, moveSpeed = 0.15f)
        enemy1.x = 0.42f
        enemy1.y = 0.50f
        enemy1.distanceTraveled = 2.0f

        // Enemy 2: Less advanced along path (distanceTraveled = 0.5)
        val enemy2 = ActiveEnemy(id = "p2", name = "G2", waveNumber = 1, maxHealth = 200, currentHealth = 200, moveSpeed = 0.15f)
        enemy2.x = 0.41f
        enemy2.y = 0.50f
        enemy2.distanceTraveled = 0.5f

        engine.activeEnemies.addAll(listOf(enemy1, enemy2))

        val castSuccess = engine.castHeroSkill(0)
        assertTrue(castSuccess)

        // Enemy 1 was prioritized and hit; Enemy 2 was undamaged
        assertEquals("Prioritized enemy must take damage", 60, enemy1.currentHealth)
        assertEquals("Second enemy must remain untouched", 200, enemy2.currentHealth)
        assertEquals(140, deployed.totalDamageDealt)
    }

    // -------------------------------------------------------------------------
    // Test 13: Hero Automated Combat Attack
    // -------------------------------------------------------------------------
    @Test
    fun testHeroAutomatedCombatAttack() {
        engine.start()
        val kael = Hero.getPlannedHeroes().first() // Power: 45, Speed: 1.2/s -> cooldown = ~0.83s
        val deployed = engine.deployHero(hero = kael, x = 0.40f, y = 0.50f)

        val enemy = ActiveEnemy(id = "atk_target", name = "G", waveNumber = 1, maxHealth = 100, currentHealth = 100, moveSpeed = 0.15f)
        enemy.x = 0.42f
        enemy.y = 0.50f
        engine.activeEnemies.add(enemy)

        assertTrue(deployed.canAttack)

        // One simulation tick should trigger hero's basic attack
        engine.update(0.05f)

        assertEquals("Enemy should have taken basic attack damage (45)", 55, enemy.currentHealth)
        assertEquals(45, deployed.totalDamageDealt)
        assertFalse("Hero attack should now be on cooldown", deployed.canAttack)
    }

    // -------------------------------------------------------------------------
    // Test 14: Special Fortification Skill Restores Base Health
    // -------------------------------------------------------------------------
    @Test
    fun testFortificationDefensiveSkillRestoresBaseHealth() {
        engine.start()
        val brakk = Hero.getPlannedHeroes()[2] // Brakk: Skill 2 = Fortificação Protetora (+2 base lives)
        engine.deployHero(hero = brakk, x = 0.40f, y = 0.50f)

        // Damage base down to 15
        val baseDamage = 5
        for (i in 1..baseDamage) {
            val dummyEnemy = ActiveEnemy(id = "esc_$i", name = "G", waveNumber = 1, maxHealth = 10, currentHealth = 10, moveSpeed = 0.1f)
            dummyEnemy.x = StageOneMap.castlePoint.x
            dummyEnemy.y = StageOneMap.castlePoint.y
            dummyEnemy.currentWaypointIndex = StageOneMap.waypoints.size - 2
            engine.activeEnemies.add(dummyEnemy)
        }
        engine.update(0.1f)

        assertEquals(15, engine.baseHealth)

        // Cast Brakk's Fortificação Protetora
        val castSuccess = engine.castHeroSkill(1)
        assertTrue(castSuccess)

        // Base health should be restored by +2 (15 -> 17)
        assertEquals(17, engine.baseHealth)
        assertEquals(17, lastBaseHealth)
    }
}
