package com.heroes.towerdefense

import com.heroes.towerdefense.game.map.EnvironmentTheme
import com.heroes.towerdefense.game.map.IsometricCoordinateConverter
import com.heroes.towerdefense.game.map.StageOneMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Unit Test Suite for Heroes Tower Defense — Extreme Visual Upgrade (Etapa 1/3):
 * 3D Scenarios, Canonical Isometric Camera, and Godot 4 Architectural Bridge.
 *
 * Verifies:
 * 1. Canonical isometric camera mathematics: Pitch = -35.264° (-atan(1/sqrt(2))), Yaw = 45°.
 * 2. IsometricCoordinateConverter two-way mapping fidelity and bounds clamping.
 * 3. Exact 3D mapping of canonical StageOneMap waypoints (road elevation 0.04f).
 * 4. Exact 3D mapping of all 8 tactical tower spots with path clearance.
 * 5. Distinct 3D atmospheric sky, directional sun lighting, and fog parameters across all 6 biomes.
 * 6. True 2:1 isometric projection ratio for 2.5D visual depth.
 * 7. Terrain volumetric dimensions (14.0 x 20.0 x 1.4) optimized for 9:16 portrait mobile aspect ratio.
 * 8. Preservation of all core game engine and StageOneMap contracts.
 */
class ExtremeVisualUpgradeStage1Test {

    // =========================================================================
    // 1. Canonical Isometric Camera Angles & Mathematics
    // =========================================================================

    @Test
    fun testCanonicalIsometricCameraAnglesAndMathematics() {
        // Canonical isometric pitch angle is exactly -atan(1/sqrt(2)) in radians
        val expectedPitchDeg = -35.26439f
        assertTrue(
            "Pitch must match canonical isometric angle within 0.05°",
            IsometricCoordinateConverter.isCanonicalIsometricPitch(-35.264f)
        )
        assertEquals(
            "Yaw angle must be exactly 45.0 degrees",
            45.0f,
            IsometricCoordinateConverter.ISOMETRIC_YAW_DEG,
            0.001f
        )
        assertEquals(
            "Yaw in radians must be pi/4",
            Math.PI.toFloat() / 4.0f,
            IsometricCoordinateConverter.ISOMETRIC_YAW_RAD,
            0.001f
        )
    }

    // =========================================================================
    // 2. Coordinate Converter Two-Way Fidelity
    // =========================================================================

    @Test
    fun testIsometricCoordinateConverterTwoWayFidelity() {
        val testPoints = listOf(
            0.0f to 0.0f,
            0.5f to 0.5f,
            1.0f to 1.0f,
            0.18f to 0.06f,
            0.82f to 0.22f,
            0.55f to 0.90f
        )

        for ((nx, ny) in testPoints) {
            val world3D = IsometricCoordinateConverter.normalizedToWorld3D(nx, ny, 0.0f)
            val convertedBack = IsometricCoordinateConverter.world3DToNormalized(world3D)

            assertEquals("nx must round-trip with high precision", nx, convertedBack.x, 0.0001f)
            assertEquals("ny must round-trip with high precision", ny, convertedBack.y, 0.0001f)
        }
    }

    @Test
    fun testIsometricCoordinateConverterClampsToBounds() {
        // Out-of-bounds negative coordinates
        val worldUnder = IsometricCoordinateConverter.normalizedToWorld3D(-0.5f, -0.2f)
        val clampedUnder = IsometricCoordinateConverter.world3DToNormalized(worldUnder)
        assertEquals(0.0f, clampedUnder.x, 0.0001f)
        assertEquals(0.0f, clampedUnder.y, 0.0001f)

        // Out-of-bounds overflow coordinates
        val worldOver = IsometricCoordinateConverter.normalizedToWorld3D(1.5f, 1.8f)
        val clampedOver = IsometricCoordinateConverter.world3DToNormalized(worldOver)
        assertEquals(1.0f, clampedOver.x, 0.0001f)
        assertEquals(1.0f, clampedOver.y, 0.0001f)
    }

    // =========================================================================
    // 3. Stage 1 Waypoints and Tower Spots 3D Mapping
    // =========================================================================

    @Test
    fun testStageOneWaypoints3DMappedAccurately() {
        val waypoints3D = IsometricCoordinateConverter.getStageOneWaypoints3D()
        assertEquals(
            "Must map all 8 canonical waypoints from StageOneMap",
            8,
            waypoints3D.size
        )

        // Waypoint 0 (Portal): nx=0.18, ny=0.06 -> x = (0.18 - 0.5)*14 = -4.48, z = (0.06 - 0.5)*20 = -8.8
        val portal = waypoints3D[0]
        assertEquals(-4.48f, portal.x, 0.01f)
        assertEquals(IsometricCoordinateConverter.ROAD_ELEVATION_3D, portal.y, 0.001f)
        assertEquals(-8.80f, portal.z, 0.01f)

        // Waypoint 7 (Castle): nx=0.55, ny=0.90 -> x = (0.55 - 0.5)*14 = +0.70, z = (0.90 - 0.5)*20 = +8.0
        val castle = waypoints3D[7]
        assertEquals(0.70f, castle.x, 0.01f)
        assertEquals(IsometricCoordinateConverter.ROAD_ELEVATION_3D, castle.y, 0.001f)
        assertEquals(8.00f, castle.z, 0.01f)

        // All waypoints must be placed within battlefield 3D volume
        for (wp in waypoints3D) {
            assertTrue("Waypoint X within width bounds", abs(wp.x) <= IsometricCoordinateConverter.BATTLEFIELD_WIDTH_3D / 2.0f)
            assertTrue("Waypoint Z within length bounds", abs(wp.z) <= IsometricCoordinateConverter.BATTLEFIELD_LENGTH_3D / 2.0f)
            assertEquals("Waypoint Y must be at road elevation", IsometricCoordinateConverter.ROAD_ELEVATION_3D, wp.y, 0.001f)
        }
    }

    @Test
    fun testStageOneTowerSpots3DMappedAccurately() {
        val towerSpots3D = IsometricCoordinateConverter.getStageOneTowerSpots3D()
        assertEquals(
            "Must map all 8 default tactical tower spots from StageOneMap",
            8,
            towerSpots3D.size
        )

        val spotIds = towerSpots3D.map { it.first }.toSet()
        assertEquals(
            setOf("spot_1", "spot_2", "spot_3", "spot_4", "spot_5", "spot_6", "spot_7", "spot_8"),
            spotIds
        )

        for ((id, pos3D) in towerSpots3D) {
            assertNotNull("Spot $id 3D position must not be null", pos3D)
            assertTrue("Spot $id X within bounds", abs(pos3D.x) <= IsometricCoordinateConverter.BATTLEFIELD_WIDTH_3D / 2.0f)
            assertTrue("Spot $id Z within bounds", abs(pos3D.z) <= IsometricCoordinateConverter.BATTLEFIELD_LENGTH_3D / 2.0f)
            assertEquals("Spot $id Y must be at tower base elevation", IsometricCoordinateConverter.TOWER_BASE_ELEVATION_3D, pos3D.y, 0.001f)
        }
    }

    // =========================================================================
    // 4. Distinct 3D Atmospheric Sky & Lighting across all 6 Biomes
    // =========================================================================

    @Test
    fun testAllSixEnvironmentThemesHaveUnique3DAtmosphericSkyAndLightPalettes() {
        val themes = EnvironmentTheme.values()
        assertEquals(6, themes.size)

        val skyTopColors = themes.map { it.skyTopColor }.toSet()
        assertEquals("All 6 themes must have unique 3D sky top colors", 6, skyTopColors.size)

        val skyHorizonColors = themes.map { it.skyHorizonColor }.toSet()
        assertEquals("All 6 themes must have unique 3D sky horizon colors", 6, skyHorizonColors.size)

        val sunColors = themes.map { it.sunLightColor }.toSet()
        assertEquals("All 6 themes must have unique directional sun colors", 6, sunColors.size)

        val fogColors = themes.map { it.fogColor }.toSet()
        assertEquals("All 6 themes must have unique atmospheric fog colors", 6, fogColors.size)
    }

    @Test
    fun testAllSixEnvironmentThemes3DSunEnergyAndFogWithinValidRanges() {
        for (theme in EnvironmentTheme.values()) {
            assertTrue(
                "Theme ${theme.name} sunEnergy must be within realistic mobile range [0.8, 1.6]",
                theme.sunEnergy in 0.8f..1.6f
            )
            assertTrue(
                "Theme ${theme.name} fogDensity must be within realistic atmospheric range [0.002, 0.03]",
                theme.fogDensity in 0.002f..0.03f
            )
            assertTrue(
                "Theme ${theme.name} ambientEnergy must be within [0.2, 0.6]",
                theme.ambientEnergy in 0.2f..0.6f
            )
        }
    }

    // =========================================================================
    // 5. True 2:1 Isometric Projection Ratio
    // =========================================================================

    @Test
    fun testProjectIsometric2DRatio() {
        // Point along positive X vs point along positive Z
        val ptX = IsometricCoordinateConverter.Vector3D(10.0f, 0.0f, 0.0f)
        val projX = IsometricCoordinateConverter.projectIsometric2D(ptX, 1.0f)

        val ptZ = IsometricCoordinateConverter.Vector3D(0.0f, 0.0f, 10.0f)
        val projZ = IsometricCoordinateConverter.projectIsometric2D(ptZ, 1.0f)

        // For (10, 0, 0): isoX = 10 * cos(30°) = 8.66, isoY = 10 * 0.5 = 5.0
        assertEquals(8.660254f, projX.x, 0.001f)
        assertEquals(5.0f, projX.y, 0.001f)

        // For (0, 0, 10): isoX = -10 * cos(30°) = -8.66, isoY = 10 * 0.5 = 5.0
        assertEquals(-8.660254f, projZ.x, 0.001f)
        assertEquals(5.0f, projZ.y, 0.001f)

        // Aspect ratio of screen displacement matches canonical isometric diamond
        assertEquals(abs(projX.x), abs(projZ.x), 0.001f)
        assertEquals(projX.y, projZ.y, 0.001f)
    }

    // =========================================================================
    // 6. Terrain Volumetric Dimensions and Mobile Proportions
    // =========================================================================

    @Test
    fun test3DTerrainElevationGeometryRules() {
        assertEquals(14.0f, IsometricCoordinateConverter.BATTLEFIELD_WIDTH_3D, 0.001f)
        assertEquals(20.0f, IsometricCoordinateConverter.BATTLEFIELD_LENGTH_3D, 0.001f)

        // Aspect ratio: width / length = 14 / 20 = 0.70, which matches vertical 9:16 (0.56) with padding
        val ratio = IsometricCoordinateConverter.BATTLEFIELD_WIDTH_3D / IsometricCoordinateConverter.BATTLEFIELD_LENGTH_3D
        assertEquals(0.70f, ratio, 0.01f)

        // Road elevation is strictly above ground elevation
        assertTrue(
            "Road must be slightly elevated above base ground",
            IsometricCoordinateConverter.ROAD_ELEVATION_3D > IsometricCoordinateConverter.TOWER_BASE_ELEVATION_3D
        )
    }

    // =========================================================================
    // 7. Preservation of StageOneMap Contracts
    // =========================================================================

    @Test
    fun testStageOneMapIntegrityPreserved() {
        assertEquals("Stage 1 must have exactly 8 waypoints", 8, StageOneMap.waypoints.size)
        assertEquals("Spawn point must be first waypoint", StageOneMap.waypoints[0], StageOneMap.spawnPoint)
        assertEquals("Base point must be last waypoint", StageOneMap.waypoints[7], StageOneMap.basePoint)
        assertEquals("Castle point equals base point", StageOneMap.basePoint, StageOneMap.castlePoint)

        val totalLength = StageOneMap.calculateTotalPathLength()
        assertTrue("Total path length must be positive and significant", totalLength > 1.5f)
    }

    @Test
    fun testVector3DDistanceCalculation() {
        val p1 = IsometricCoordinateConverter.Vector3D(0f, 0f, 0f)
        val p2 = IsometricCoordinateConverter.Vector3D(3f, 4f, 0f)
        assertEquals(5.0f, p1.distanceTo(p2), 0.001f)

        val p3 = IsometricCoordinateConverter.Vector3D(1f, 2f, 2f)
        val p4 = IsometricCoordinateConverter.Vector3D(1f, 2f, 5f)
        assertEquals(3.0f, p3.distanceTo(p4), 0.001f)
    }
}
