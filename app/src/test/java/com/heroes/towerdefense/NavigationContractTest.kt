package com.heroes.towerdefense

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.hero.HeroClass
import com.heroes.towerdefense.model.stage.StageInfo
import com.heroes.towerdefense.ui.heroes.HeroesActivity
import com.heroes.towerdefense.ui.main.MainActivity
import com.heroes.towerdefense.ui.preparation.StagePreparationActivity
import com.heroes.towerdefense.ui.settings.SettingsActivity
import com.heroes.towerdefense.ui.shop.ShopActivity
import com.heroes.towerdefense.ui.stage.StageOneActivity
import com.heroes.towerdefense.ui.stages.StageSelectActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating Navigation Contracts and Screen Declarations for Part 2.
 */
class NavigationContractTest {

    @Test
    fun testAllActivitiesDeclaredAndResolvable() {
        // Verify all 7 activity classes for Part 2 exist and can be loaded
        val activities = listOf(
            MainActivity::class.java,
            StagePreparationActivity::class.java,
            StageOneActivity::class.java,
            StageSelectActivity::class.java,
            HeroesActivity::class.java,
            ShopActivity::class.java,
            SettingsActivity::class.java
        )

        assertEquals("Expected exactly 7 activities for Part 2 navigation", 7, activities.size)
        for (activityClass in activities) {
            assertNotNull("Activity class must not be null", activityClass)
            assertTrue("Class must be an Activity subclass", android.app.Activity::class.java.isAssignableFrom(activityClass))
        }
    }

    @Test
    fun testHeroRosterIntegrity() {
        val heroes = Hero.getPlannedHeroes()
        assertEquals(6, heroes.size)

        val heroMap = heroes.associateBy { it.name }
        assertTrue("Kael must be in hero roster", heroMap.containsKey("Kael"))
        assertTrue("Lyra must be in hero roster", heroMap.containsKey("Lyra"))
        assertTrue("Brakk must be in hero roster", heroMap.containsKey("Brakk"))
        assertTrue("Nyra must be in hero roster", heroMap.containsKey("Nyra"))
        assertTrue("Tiko must be in hero roster", heroMap.containsKey("Tiko"))
        assertTrue("Vesper must be in hero roster", heroMap.containsKey("Vesper"))

        assertEquals(HeroClass.WARRIOR, heroMap["Kael"]?.heroClass)
        assertEquals(HeroClass.ARCHER, heroMap["Lyra"]?.heroClass)
        assertEquals(HeroClass.WARRIOR, heroMap["Brakk"]?.heroClass)
        assertEquals(HeroClass.MAGE, heroMap["Nyra"]?.heroClass)
        assertEquals(HeroClass.ALCHEMIST, heroMap["Tiko"]?.heroClass)
        assertEquals(HeroClass.ASSASSIN, heroMap["Vesper"]?.heroClass)
    }

    @Test
    fun testStageAccessRules() {
        val stages = StageInfo.getCampaignStages()
        assertEquals(6, stages.size)

        // Stage 1 is the only unlocked stage in Part 2
        assertTrue("Stage 1 must be accessible", stages[0].isUnlocked)
        for (i in 1 until stages.size) {
            assertFalse("Stage ${stages[i].stageNumber} must not be accessible in Part 2", stages[i].isUnlocked)
        }
    }

    @Test
    fun testGameIdentityConstants() {
        assertEquals("Heroes Tower Defense", GameConstants.APP_NAME)
        assertEquals("1.0.0", GameConstants.GAME_VERSION)
    }
}
