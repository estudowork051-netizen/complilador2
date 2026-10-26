package com.heroes.towerdefense.model.enemy

/**
 * Representation of an attacking enemy unit.
 */
data class Enemy(
    val id: String,
    val name: String,
    val type: EnemyType,
    val maxHealth: Int,
    var currentHealth: Int,
    val moveSpeed: Float,
    val goldReward: Int,
    val isBoss: Boolean = false
) {
    init {
        require(maxHealth > 0) { "Max health must be greater than zero" }
        require(currentHealth in 0..maxHealth) { "Current health must be between 0 and maxHealth" }
        require(moveSpeed > 0f) { "Move speed must be greater than zero" }
        require(goldReward >= 0) { "Gold reward cannot be negative" }
    }

    val isAlive: Boolean
        get() = currentHealth > 0

    val healthPercentage: Float
        get() = (currentHealth.toFloat() / maxHealth.toFloat()).coerceIn(0f, 1f)

    /**
     * Applies damage to this enemy, reducing current health.
     * Returns true if this damage caused the enemy to die.
     */
    fun takeDamage(damage: Int): Boolean {
        require(damage >= 0) { "Damage cannot be negative" }
        val wasAlive = isAlive
        currentHealth = (currentHealth - damage).coerceAtLeast(0)
        return wasAlive && !isAlive
    }
}

enum class EnemyType {
    GOBLIN_SCOUT,
    GOBLIN_WARRIOR,
    ORC_BERSERKER,
    DARK_MAGE,
    DRAGON_BOSS
}
