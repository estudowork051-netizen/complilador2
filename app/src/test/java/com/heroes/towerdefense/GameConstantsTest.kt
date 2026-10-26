package com.heroes.towerdefense

import com.heroes.towerdefense.core.GameConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating game constants and boundary parameters.
 */
class GameConstantsTest {

    @Test
    fun testVolumeBoundaries() {
        assertTrue("Min volume must be 0", GameConstants.MIN_VOLUME == 0)
        assertTrue("Max volume must be 100", GameConstants.MAX_VOLUME == 100)
        assertTrue(
            "Default volume must be within valid bounds",
            GameConstants.DEFAULT_MASTER_VOLUME in GameConstants.MIN_VOLUME..GameConstants.MAX_VOLUME
        )
        assertTrue(
            "Default SFX volume must be within valid bounds",
            GameConstants.DEFAULT_SFX_VOLUME in GameConstants.MIN_VOLUME..GameConstants.MAX_VOLUME
        )
    }

    @Test
    fun testGridDimensions() {
        assertTrue("Columns must be greater than 0", GameConstants.GRID_COLUMNS > 0)
        assertTrue("Rows must be greater than 0", GameConstants.GRID_ROWS > 0)
    }

    @Test
    fun testGameIdentity() {
        assertEquals("Heroes Tower Defense", GameConstants.APP_NAME)
        assertEquals("1.0.0", GameConstants.GAME_VERSION)
    }

    @Test
    fun testTowerCombatConstants() {
        assertTrue("Max towers must be positive", GameConstants.STAGE_ONE_MAX_TOWERS > 0)
        assertTrue("Archer damage must be positive", GameConstants.ARCHER_DAMAGE > 0)
        assertTrue("Mage damage must be positive", GameConstants.MAGE_DAMAGE > 0)
        assertTrue("Cannon damage must be positive", GameConstants.CANNON_DAMAGE > 0)
        assertTrue("Mage range must exceed archer range", GameConstants.MAGE_RANGE > GameConstants.ARCHER_RANGE)
        assertTrue("Cannon damage must exceed archer and mage", GameConstants.CANNON_DAMAGE > GameConstants.MAGE_DAMAGE)
        assertTrue("Cannon splash radius must be positive", GameConstants.CANNON_SPLASH_RADIUS > 0f)
    }
}
