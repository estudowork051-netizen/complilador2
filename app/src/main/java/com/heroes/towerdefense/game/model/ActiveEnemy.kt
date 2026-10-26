package com.heroes.towerdefense.game.model

import com.heroes.towerdefense.game.map.Waypoint
import kotlin.math.sqrt

/**
 * Runtime representation of an enemy moving along the path.
 * Tracks spatial coordinates, path progress, life status, and articulated animation states
 * (Visual Upgrade Part 2).
 */
open class ActiveEnemy(
    val id: String,
    val name: String,
    val waveNumber: Int,
    val maxHealth: Int = 100,
    var currentHealth: Int = 100,
    open val moveSpeed: Float = 0.16f, // Normalized distance units per second
    var visualType: EnemyVisualType = EnemyVisualType.GOBLIN
) {
    var x: Float = 0f
    var y: Float = 0f
    var currentWaypointIndex: Int = 0
    var isAlive: Boolean = true
    var hasReachedBase: Boolean = false
    var distanceTraveled: Float = 0f
    open val isBoss: Boolean = false

    // Articulated Animation & Visual Upgrade Part 2 State
    var animState: EnemyAnimState = EnemyAnimState.WALK
    var animTimer: Float = 0f
    var stateTimer: Float = 0f
    var facingDirection: Float = 1f // 1f = right/down, -1f = left
    var hitReactionTimer: Float = 0f
    var attackAnimTimer: Float = 0f
    var deathAnimTimer: Float = 0f
    var deathDuration: Float = 0.50f
    var isDying: Boolean = false

    init {
        require(maxHealth > 0) { "Max health must be greater than zero" }
        require(moveSpeed > 0f) { "Move speed must be greater than zero" }

        // Infer visualType if defaulted to GOBLIN based on name or id
        if (visualType == EnemyVisualType.GOBLIN) {
            val key = (name + "_" + id).lowercase()
            when {
                "skeleton" in key || "esqueleto" in key -> visualType = EnemyVisualType.SKELETON
                "orc" in key -> visualType = EnemyVisualType.ORC
                "mage" in key || "mago" in key || "shadow_minion" in key || "servo" in key || "sombra" in key -> visualType = EnemyVisualType.DARK_MAGE
            }
        }
    }

    val healthFraction: Float
        get() = (currentHealth.toFloat() / maxHealth.toFloat()).coerceIn(0f, 1f)

    /**
     * Initializes enemy position at the starting waypoint.
     */
    fun spawnAt(startWaypoint: Waypoint) {
        x = startWaypoint.x
        y = startWaypoint.y
        currentWaypointIndex = 0
        isAlive = true
        hasReachedBase = false
        distanceTraveled = 0f
        animState = if (moveSpeed > 0f) EnemyAnimState.WALK else EnemyAnimState.IDLE
        animTimer = 0f
        stateTimer = 0f
        hitReactionTimer = 0f
        attackAnimTimer = 0f
        deathAnimTimer = 0f
        isDying = false
    }

    /**
     * Updates movement along the waypoints using delta time.
     * Also updates animation timers, facing direction, and hit recovery.
     * Returns true if the enemy reached the base this frame.
     */
    fun update(deltaTime: Float, waypoints: List<Waypoint>): Boolean {
        // Advance animation timers
        animTimer += deltaTime
        stateTimer += deltaTime

        if (hitReactionTimer > 0f) {
            hitReactionTimer -= deltaTime
            if (hitReactionTimer <= 0f) {
                hitReactionTimer = 0f
                if (animState == EnemyAnimState.HIT) {
                    animState = if (moveSpeed > 0f && isAlive && !hasReachedBase) EnemyAnimState.WALK else EnemyAnimState.IDLE
                }
            }
        }

        if (attackAnimTimer > 0f) {
            attackAnimTimer -= deltaTime
            if (attackAnimTimer <= 0f) {
                attackAnimTimer = 0f
                if (animState == EnemyAnimState.ATTACK) {
                    animState = if (moveSpeed > 0f && isAlive && !hasReachedBase) EnemyAnimState.WALK else EnemyAnimState.IDLE
                }
            }
        }

        if (isDying) {
            deathAnimTimer += deltaTime
            return false
        }

        if (!isAlive || hasReachedBase || currentWaypointIndex >= waypoints.size - 1) {
            return false
        }

        var distanceToMove = moveSpeed * deltaTime

        while (distanceToMove > 0f && currentWaypointIndex < waypoints.size - 1) {
            val targetWaypoint = waypoints[currentWaypointIndex + 1]
            val dx = targetWaypoint.x - x
            val dy = targetWaypoint.y - y
            val segmentDistance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            // Update facing direction based on horizontal movement
            if (dx > 0.005f) {
                facingDirection = 1f
            } else if (dx < -0.005f) {
                facingDirection = -1f
            }

            if (segmentDistance <= 0.0001f) {
                // Already at this waypoint, advance to next
                currentWaypointIndex++
                if (currentWaypointIndex >= waypoints.size - 1) {
                    hasReachedBase = true
                    return true
                }
                continue
            }

            if (distanceToMove < segmentDistance) {
                // Move partially along the segment
                val ratio = distanceToMove / segmentDistance
                x += dx * ratio
                y += dy * ratio
                distanceTraveled += distanceToMove
                distanceToMove = 0f
            } else {
                // Reached the next waypoint
                x = targetWaypoint.x
                y = targetWaypoint.y
                distanceTraveled += segmentDistance
                distanceToMove -= segmentDistance
                currentWaypointIndex++

                // Check if reached destination base
                if (currentWaypointIndex >= waypoints.size - 1) {
                    hasReachedBase = true
                    return true
                }
            }
        }

        return hasReachedBase
    }

    /**
     * Triggers a hit reaction animation.
     */
    fun triggerHit(duration: Float = 0.22f) {
        if (!isAlive || isDying) return
        hitReactionTimer = duration
        animState = EnemyAnimState.HIT
        stateTimer = 0f
    }

    /**
     * Triggers an attack animation.
     */
    fun triggerAttack(duration: Float = 0.35f) {
        if (!isAlive || isDying) return
        attackAnimTimer = duration
        animState = EnemyAnimState.ATTACK
        stateTimer = 0f
    }

    /**
     * Triggers a death animation.
     */
    fun triggerDeath(duration: Float = 0.50f) {
        isDying = true
        isAlive = false
        deathDuration = duration
        animState = EnemyAnimState.DEATH
        stateTimer = 0f
        deathAnimTimer = 0f
    }

    /**
     * Deals damage to this enemy. Returns true if this damage defeated the enemy.
     */
    open fun takeDamage(damage: Int): Boolean {
        if (!isAlive) return false
        val wasAlive = isAlive
        currentHealth = (currentHealth - damage).coerceAtLeast(0)
        if (currentHealth == 0) {
            isAlive = false
            triggerDeath()
            return wasAlive
        } else if (damage > 0) {
            triggerHit()
        }
        return false
    }

    /**
     * Computes Euclidean distance from this enemy's position to a target point.
     */
    fun distanceTo(targetX: Float, targetY: Float): Float {
        val dx = targetX - x
        val dy = targetY - y
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}
