package com.heroes.towerdefense.game.map

import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 3D Isometric Coordinate Converter for Heroes Tower Defense.
 *
 * Provides mathematically rigorous two-way mapping between 2D normalized screen coordinates
 * (nx in 0f..1f, ny in 0f..1f) and 3D isometric battlefield coordinates (worldX, worldY, worldZ).
 *
 * Dimensions:
 * - Battlefield Width (X axis): 14.0 units (-7.0 to +7.0)
 * - Battlefield Length (Z axis): 20.0 units (-10.0 to +10.0)
 * - Terrain Elevation (Y axis): 0.0 (ground base), 0.04 (road elevation)
 *
 * Canonical Isometric Viewing Angles:
 * - Pitch: -35.264° = -atan(1/sqrt(2)) radians = -0.6154797 rad
 * - Yaw:   45.0°    = pi/4 radians             = 0.785398 rad
 */
object IsometricCoordinateConverter {

    const val BATTLEFIELD_WIDTH_3D = 14.0f
    const val BATTLEFIELD_LENGTH_3D = 20.0f
    const val ROAD_ELEVATION_3D = 0.04f
    const val TOWER_BASE_ELEVATION_3D = 0.0f

    // Canonical isometric constants
    const val ISOMETRIC_PITCH_DEG = -35.264f
    const val ISOMETRIC_YAW_DEG = 45.0f
    const val ISOMETRIC_PITCH_RAD = -0.6154797f
    const val ISOMETRIC_YAW_RAD = 0.7853982f

    data class Vector3D(val x: Float, val y: Float, val z: Float) {
        fun distanceTo(other: Vector3D): Float {
            val dx = other.x - x
            val dy = other.y - y
            val dz = other.z - z
            return sqrt(dx * dx + dy * dy + dz * dz)
        }
    }

    data class Point2D(val x: Float, val y: Float)

    /**
     * Converts normalized 2D screen coordinate (0..1, 0..1) to 3D world position (X, Y, Z).
     */
    fun normalizedToWorld3D(nx: Float, ny: Float, elevation: Float = 0.0f): Vector3D {
        val clampedNx = nx.coerceIn(0.0f, 1.0f)
        val clampedNy = ny.coerceIn(0.0f, 1.0f)
        val worldX = (clampedNx - 0.5f) * BATTLEFIELD_WIDTH_3D
        val worldZ = (clampedNy - 0.5f) * BATTLEFIELD_LENGTH_3D
        return Vector3D(worldX, elevation, worldZ)
    }

    /**
     * Converts 3D world position (X, Y, Z) back to normalized 2D coordinate (nx, ny).
     */
    fun world3DToNormalized(world: Vector3D): Point2D {
        val nx = ((world.x / BATTLEFIELD_WIDTH_3D) + 0.5f).coerceIn(0.0f, 1.0f)
        val ny = ((world.z / BATTLEFIELD_LENGTH_3D) + 0.5f).coerceIn(0.0f, 1.0f)
        return Point2D(nx, ny)
    }

    /**
     * Converts normalized 2D coordinate to projected isometric 2.5D display offset.
     */
    fun projectIsometric2D(world: Vector3D, scale: Float = 1.0f): Point2D {
        // True 2:1 isometric projection ratio:
        // screenX = (worldX - worldZ) * cos(30°)
        // screenY = (worldX + worldZ) * sin(30°) - worldY
        val isoX = (world.x - world.z) * 0.8660254f * scale
        val isoY = ((world.x + world.z) * 0.5f - world.y) * scale
        return Point2D(isoX, isoY)
    }

    /**
     * Maps StageOneMap waypoints to 3D world positions.
     */
    fun getStageOneWaypoints3D(): List<Vector3D> {
        return StageOneMap.waypoints.map { wp ->
            normalizedToWorld3D(wp.x, wp.y, ROAD_ELEVATION_3D)
        }
    }

    /**
     * Maps StageOneMap default tactical tower spots to 3D world positions.
     */
    fun getStageOneTowerSpots3D(): List<Pair<String, Vector3D>> {
        return StageOneMap.defaultTowerSpots.map { spot ->
            spot.id to normalizedToWorld3D(spot.x, spot.y, TOWER_BASE_ELEVATION_3D)
        }
    }

    /**
     * Validates if canonical isometric pitch angle matches mathematical definition: -atan(1/sqrt(2)).
     */
    fun isCanonicalIsometricPitch(degrees: Float, tolerance: Float = 0.05f): Boolean {
        val exactRad = -atan(1.0 / sqrt(2.0))
        val exactDeg = Math.toDegrees(exactRad).toFloat()
        return kotlin.math.abs(degrees - exactDeg) <= tolerance
    }
}
