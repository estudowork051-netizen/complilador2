package com.heroes.towerdefense.game.model

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.model.tower.TowerType

/**
 * Runtime placed tower on a map spot, actively targeting and attacking enemies.
 */
data class PlacedTower(
    val id: String,
    val spotId: String,
    val type: TowerType,
    val name: String,
    val x: Float,
    val y: Float,
    val damage: Int,
    val range: Float,
    val fireRate: Float, // Attacks per second
    val projectileSpeed: Float,
    val splashRadius: Float = 0f
) {
    val attackIntervalSeconds: Float = if (fireRate > 0f) 1f / fireRate else 1f
    var timeUntilNextAttack: Float = 0f
    var currentTargetId: String? = null
    var totalShotsFired: Int = 0
    var totalDamageDealt: Int = 0

    var spawnAnimTimer: Float = 0.5f // Construction bounce & pop-up duration
    var recoilAnimTimer: Float = 0f  // Attack kickback recoil duration

    val canAttack: Boolean
        get() = timeUntilNextAttack <= 0f

    fun updateCooldown(deltaTime: Float) {
        if (timeUntilNextAttack > 0f) {
            timeUntilNextAttack = (timeUntilNextAttack - deltaTime).coerceAtLeast(0f)
        }
        if (spawnAnimTimer > 0f) {
            spawnAnimTimer = (spawnAnimTimer - deltaTime).coerceAtLeast(0f)
        }
        if (recoilAnimTimer > 0f) {
            recoilAnimTimer = (recoilAnimTimer - deltaTime).coerceAtLeast(0f)
        }
    }

    fun resetCooldown() {
        timeUntilNextAttack = attackIntervalSeconds
        recoilAnimTimer = 0.20f
        totalShotsFired++
    }

    companion object {
        fun createArcher(id: String, spotId: String, x: Float, y: Float): PlacedTower = PlacedTower(
            id = id,
            spotId = spotId,
            type = TowerType.ARCHER_TOWER,
            name = "Torre de Arqueiros",
            x = x,
            y = y,
            damage = GameConstants.ARCHER_DAMAGE,
            range = GameConstants.ARCHER_RANGE,
            fireRate = GameConstants.ARCHER_FIRE_RATE,
            projectileSpeed = GameConstants.ARCHER_PROJECTILE_SPEED,
            splashRadius = 0f
        )

        fun createMage(id: String, spotId: String, x: Float, y: Float): PlacedTower = PlacedTower(
            id = id,
            spotId = spotId,
            type = TowerType.MAGE_TOWER,
            name = "Torre de Magos",
            x = x,
            y = y,
            damage = GameConstants.MAGE_DAMAGE,
            range = GameConstants.MAGE_RANGE,
            fireRate = GameConstants.MAGE_FIRE_RATE,
            projectileSpeed = GameConstants.MAGE_PROJECTILE_SPEED,
            splashRadius = 0f
        )

        fun createCannon(id: String, spotId: String, x: Float, y: Float): PlacedTower = PlacedTower(
            id = id,
            spotId = spotId,
            type = TowerType.CANNON_TOWER,
            name = "Torre Canhoneira",
            x = x,
            y = y,
            damage = GameConstants.CANNON_DAMAGE,
            range = GameConstants.CANNON_RANGE,
            fireRate = GameConstants.CANNON_FIRE_RATE,
            projectileSpeed = GameConstants.CANNON_PROJECTILE_SPEED,
            splashRadius = GameConstants.CANNON_SPLASH_RADIUS
        )
    }
}
