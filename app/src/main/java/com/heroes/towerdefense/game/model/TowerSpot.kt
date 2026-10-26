package com.heroes.towerdefense.game.model

/**
 * Valid location on the map where a defensive tower can be constructed.
 */
data class TowerSpot(
    val id: String,
    val x: Float, // Normalized X (0.0f..1.0f)
    val y: Float, // Normalized Y (0.0f..1.0f)
    var placedTower: PlacedTower? = null
) {
    val isOccupied: Boolean
        get() = placedTower != null
}
