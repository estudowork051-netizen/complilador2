package com.heroes.towerdefense.game.model

import com.heroes.towerdefense.model.tower.TowerType
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Runtime projectile moving across the canvas from a tower towards an enemy.
 * Extended in Part 4 with ProjectileVisualType, ballistic parabolic elevation curve,
 * flight time tracking, and directional orientation for stylized rendering.
 */
data class CombatProjectile(
    val id: String,
    val sourceTowerId: String,
    val towerType: TowerType,
    val startX: Float,
    val startY: Float,
    var currentX: Float,
    var currentY: Float,
    val targetEnemyId: String,
    var targetLastX: Float,
    var targetLastY: Float,
    val damage: Int,
    val speed: Float,
    val splashRadius: Float = 0f,
    val visualType: ProjectileVisualType = ProjectileVisualType.fromTowerType(towerType)
) {
    var hasDealtDamage: Boolean = false
        private set

    var isExpired: Boolean = false
        private set

    var flightTime: Float = 0f
        private set

    val totalDistance: Float by lazy {
        val dx = targetLastX - startX
        val dy = targetLastY - startY
        sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
    }

    val progress: Float
        get() {
            val dx = currentX - startX
            val dy = currentY - startY
            val traveled = sqrt(dx * dx + dy * dy)
            return (traveled / totalDistance).coerceIn(0f, 1f)
        }

    /**
     * Parabolic elevation offset for ballistic projectiles (Archer arrows, Cannonballs).
     * Peaks at mid-flight (progress = 0.5) to give authentic 3D vertical arc feeling.
     */
    val arcHeightOffset: Float
        get() {
            if (visualType.isBallisticArc) {
                val p = progress
                val peakHeight = if (visualType == ProjectileVisualType.CANNON_BALL) 0.040f else 0.025f
                return sin(p * PI.toFloat()) * peakHeight
            }
            return 0f
        }

    /**
     * Updates movement towards destination.
     * Returns true when the projectile reaches its target destination.
     */
    fun update(deltaTime: Float, activeEnemies: List<ActiveEnemy>): Boolean {
        if (isExpired) return false
        flightTime += deltaTime

        // If target enemy is still alive, track its updated position
        val target = activeEnemies.find { it.id == targetEnemyId && it.isAlive }
        if (target != null) {
            targetLastX = target.x
            targetLastY = target.y
        }

        // Move towards destination
        val dx = targetLastX - currentX
        val dy = targetLastY - currentY
        val distance = sqrt(dx * dx + dy * dy)
        val moveDist = speed * deltaTime

        if (distance <= moveDist || distance <= 0.025f) {
            currentX = targetLastX
            currentY = targetLastY
            return true
        } else {
            val ratio = moveDist / distance
            currentX += dx * ratio
            currentY += dy * ratio
            return false
        }
    }

    /**
     * Marks damage as delivered to prevent duplicate damage under any circumstances.
     */
    fun markDamageDealt() {
        hasDealtDamage = true
        isExpired = true
    }

    fun expire() {
        isExpired = true
    }
}
