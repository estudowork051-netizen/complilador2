package com.heroes.towerdefense.game.map

/**
 * Normalized 2D waypoint along the game path (0.0f to 1.0f).
 * Allows the map and path to scale smoothly across different screen resolutions.
 */
data class Waypoint(
    val x: Float,
    val y: Float
) {
    init {
        require(x in 0f..1f) { "Normalized X coordinate must be between 0.0 and 1.0 (got $x)" }
        require(y in 0f..1f) { "Normalized Y coordinate must be between 0.0 and 1.0 (got $y)" }
    }
}
