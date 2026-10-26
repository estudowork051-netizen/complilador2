package com.heroes.towerdefense.model.tower

/**
 * Representation of a Defensive Tower.
 */
data class Tower(
    val id: String,
    val name: String,
    val type: TowerType,
    val baseDamage: Int,
    val range: Float,
    val fireRate: Float, // Shots per second
    val buildCost: Int,
    var upgradeLevel: Int = 1
) {
    init {
        require(baseDamage > 0) { "Base damage must be greater than zero" }
        require(range > 0f) { "Range must be greater than zero" }
        require(fireRate > 0f) { "Fire rate must be greater than zero" }
        require(buildCost > 0) { "Build cost must be greater than zero" }
        require(upgradeLevel >= 1) { "Upgrade level must be at least 1" }
    }

    /**
     * Calculates the effective damage taking upgrade level into account.
     */
    fun currentDamage(): Int = (baseDamage * (1f + (upgradeLevel - 1) * 0.25f)).toInt()

    /**
     * Calculates upgrade cost for the next tier.
     */
    fun nextUpgradeCost(): Int = (buildCost * 0.75f * upgradeLevel).toInt()

    /**
     * Upgrades the tower to the next level.
     */
    fun upgrade() {
        upgradeLevel += 1
    }
}

enum class TowerType {
    ARCHER_TOWER,
    MAGE_TOWER,
    CANNON_TOWER,
    FROST_TOWER
}
