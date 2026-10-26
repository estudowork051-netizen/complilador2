package com.heroes.towerdefense.game.map

import com.heroes.towerdefense.game.model.TowerSpot

/**
 * Map definition for Stage 1: O Desfiladeiro dos Goblins.
 * Defines the entrance portal, base destination, and winding path through the fantasy forest.
 */
object StageOneMap {

    /**
     * Winding S-shaped path through the forest, optimized for vertical mobile screens.
     */
    val waypoints: List<Waypoint> = listOf(
        Waypoint(0.18f, 0.06f), // 0: Spawn Portal (Northwest)
        Waypoint(0.18f, 0.22f), // 1: Descends into upper forest clearing
        Waypoint(0.82f, 0.22f), // 2: Winds east across the river bridge
        Waypoint(0.82f, 0.48f), // 3: Descends south along the eastern cliffs
        Waypoint(0.20f, 0.48f), // 4: Winds west through the rocky canyon
        Waypoint(0.20f, 0.74f), // 5: Descends south into the castle approach
        Waypoint(0.55f, 0.74f), // 6: Turns toward the fortified gates
        Waypoint(0.55f, 0.90f)  // 7: Castle Base Destination (South)
    )

    val spawnPoint: Waypoint get() = waypoints.first()
    val basePoint: Waypoint get() = waypoints.last()
    val castlePoint: Waypoint get() = basePoint

    /**
     * Calculates the total normalized distance of the entire path.
     */
    fun calculateTotalPathLength(): Float {
        var total = 0f
        for (i in 0 until waypoints.size - 1) {
            val p1 = waypoints[i]
            val p2 = waypoints[i + 1]
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            total += Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
        return total
    }

    /**
     * Pre-configured tactical build spots for Stage 1.
     * Placed along the bends of the S-shaped path with sufficient clearance from the road.
     */
    val defaultTowerSpots: List<com.heroes.towerdefense.game.model.TowerSpot>
        get() = listOf(
            com.heroes.towerdefense.game.model.TowerSpot("spot_1", 0.50f, 0.11f), // North clearing
            com.heroes.towerdefense.game.model.TowerSpot("spot_2", 0.32f, 0.35f), // Center-west clearing
            com.heroes.towerdefense.game.model.TowerSpot("spot_3", 0.50f, 0.35f), // Center meadow
            com.heroes.towerdefense.game.model.TowerSpot("spot_4", 0.68f, 0.35f), // Center-east clearing
            com.heroes.towerdefense.game.model.TowerSpot("spot_5", 0.50f, 0.61f), // Lower-center meadow
            com.heroes.towerdefense.game.model.TowerSpot("spot_6", 0.75f, 0.61f), // Lower-east ridge
            com.heroes.towerdefense.game.model.TowerSpot("spot_7", 0.35f, 0.85f), // Castle southwest approach
            com.heroes.towerdefense.game.model.TowerSpot("spot_8", 0.75f, 0.85f)  // Castle southeast approach
        )

    /**
     * Verifies if a given coordinate is too close to the enemy road path.
     */
    fun isPointTooCloseToPath(px: Float, py: Float, minDistance: Float = 0.08f): Boolean {
        for (i in 0 until waypoints.size - 1) {
            val p1 = waypoints[i]
            val p2 = waypoints[i + 1]
            val dist = distToSegment(px, py, p1.x, p1.y, p2.x, p2.y)
            if (dist < minDistance) {
                return true
            }
        }
        return false
    }

    private fun distToSegment(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        if (dx == 0f && dy == 0f) {
            val dpx = px - x1
            val dpy = py - y1
            return Math.sqrt((dpx * dpx + dpy * dpy).toDouble()).toFloat()
        }
        val t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)
        val clampedT = t.coerceIn(0f, 1f)
        val closestX = x1 + clampedT * dx
        val closestY = y1 + clampedT * dy
        val cdx = px - closestX
        val cdy = py - closestY
        return Math.sqrt((cdx * cdx + cdy * cdy).toDouble()).toFloat()
    }
}
