package com.heroes.towerdefense

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.model.enemy.Enemy
import com.heroes.towerdefense.model.enemy.EnemyType
import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.hero.HeroClass
import com.heroes.towerdefense.model.stage.StageInfo
import com.heroes.towerdefense.model.tower.Tower
import com.heroes.towerdefense.model.tower.TowerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating core domain models, formulas, and business rules.
 */
class GameModelsTest {

    @Test
    fun testHeroCreationAndLevelUp() {
        val hero = Hero(
            id = "hero_warrior_01",
            name = "Sir Gareth",
            heroClass = HeroClass.WARRIOR,
            baseHealth = 500,
            attackPower = 40,
            attackRange = 1.5f,
            attackSpeed = 1.2f,
            skillName = "Espada Sagrada",
            level = 1
        )

        assertEquals("Sir Gareth", hero.name)
        assertEquals(1, hero.level)
        val initialDps = hero.calculateDps()
        assertTrue("Initial DPS should be positive", initialDps > 0)

        hero.levelUp()
        assertEquals(2, hero.level)
        val upgradedDps = hero.calculateDps()
        assertTrue("Upgraded DPS should exceed initial DPS", upgradedDps > initialDps)
    }

    @Test
    fun testPlannedHeroRoster() {
        val heroes = Hero.getPlannedHeroes()

        assertEquals("Expected exactly 6 planned heroes for Part 2", 6, heroes.size)

        val expectedNames = listOf("Kael", "Lyra", "Brakk", "Nyra", "Tiko", "Vesper")
        val actualNames = heroes.map { it.name }

        assertEquals(expectedNames, actualNames)

        for (hero in heroes) {
            assertTrue("Hero name must not be blank", hero.name.isNotBlank())
            assertTrue("Hero title must not be blank", hero.title.isNotBlank())
            assertTrue("Hero base health must be positive", hero.baseHealth > 0)
            assertTrue("Hero DPS must be positive", hero.calculateDps() > 0)
        }
    }

    @Test
    fun testTowerCreationAndUpgrade() {
        val tower = Tower(
            id = "tower_archer_01",
            name = "Torre de Arqueiros",
            type = TowerType.ARCHER_TOWER,
            baseDamage = 25,
            range = 3.5f,
            fireRate = 1.5f,
            buildCost = 100,
            upgradeLevel = 1
        )

        assertEquals(25, tower.currentDamage())
        val initialUpgradeCost = tower.nextUpgradeCost()
        assertTrue("Upgrade cost must be greater than zero", initialUpgradeCost > 0)

        tower.upgrade()
        assertEquals(2, tower.upgradeLevel)
        assertTrue("Damage must increase after upgrade", tower.currentDamage() > 25)
    }

    @Test
    fun testEnemyHealthAndDamage() {
        val enemy = Enemy(
            id = "goblin_01",
            name = "Goblin Batedor",
            type = EnemyType.GOBLIN_SCOUT,
            maxHealth = 100,
            currentHealth = 100,
            moveSpeed = 1.5f,
            goldReward = 15,
            isBoss = false
        )

        assertTrue("Enemy should initially be alive", enemy.isAlive)
        assertEquals(1.0f, enemy.healthPercentage, 0.001f)

        val killedByPartialHit = enemy.takeDamage(40)
        assertFalse("Enemy should not die from partial hit", killedByPartialHit)
        assertEquals(60, enemy.currentHealth)
        assertEquals(0.6f, enemy.healthPercentage, 0.001f)
        assertTrue(enemy.isAlive)

        val killedByFatalHit = enemy.takeDamage(70)
        assertTrue("Enemy should die when damage exceeds health", killedByFatalHit)
        assertEquals(0, enemy.currentHealth)
        assertFalse("Enemy should no longer be alive", enemy.isAlive)
    }

    @Test
    fun testStageOneFactoryDefaults() {
        val stageOne = StageInfo.getStageOne()

        assertEquals(GameConstants.STAGE_ONE_ID, stageOne.stageNumber)
        assertEquals("PRIMEIRA FASE", stageOne.title)
        assertEquals(GameConstants.STAGE_ONE_TOTAL_WAVES, stageOne.totalWaves)
        assertEquals(GameConstants.STAGE_ONE_INITIAL_GOLD, stageOne.startingGold)
        assertEquals(GameConstants.STAGE_ONE_INITIAL_LIVES, stageOne.startingLives)
        assertTrue("Stage 1 must be unlocked by default", stageOne.isUnlocked)
    }

    @Test
    fun testCampaignStagesRosterAndLockRules() {
        val stages = StageInfo.getCampaignStages()

        assertEquals("Expected 6 campaign stages in selection screen", 6, stages.size)

        // Stage 1 must be unlocked
        val stage1 = stages[0]
        assertEquals(1, stage1.stageNumber)
        assertTrue("Stage 1 must be unlocked", stage1.isUnlocked)

        // Stages 2 through 6 must be locked
        for (i in 1 until stages.size) {
            val stage = stages[i]
            assertEquals(i + 1, stage.stageNumber)
            assertFalse("Stage ${stage.stageNumber} must be locked in Part 2", stage.isUnlocked)
            assertTrue("Waves must be positive", stage.totalWaves > 0)
            assertTrue("Starting gold must be non-negative", stage.startingGold >= 0)
            assertTrue("Starting lives must be positive", stage.startingLives > 0)
        }
    }
}
