package com.heroes.towerdefense.game.model

import kotlin.math.abs

/**
 * Physical debris particle for impacts, rock bursts, construction dust,
 * and cannon explosions.
 * Features 3D isometric height (z) above ground, gravity, ground bounce, and friction.
 */
data class VisualPhysicsDebris(
    var x: Float,
    var y: Float,
    var z: Float,           // Height above ground in pixels
    var vx: Float,          // Horizontal X velocity
    var vy: Float,          // Horizontal Y velocity
    var vz: Float,          // Vertical Z upward velocity
    val color: Int,
    var size: Float,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    val maxLifetime: Float = 0.8f,
    var lifetime: Float = 0f
) {
    var bounceCount: Int = 0

    val isExpired: Boolean
        get() = lifetime >= maxLifetime

    val progress: Float
        get() = (lifetime / maxLifetime).coerceIn(0f, 1f)

    val alpha: Float
        get() = (1f - progress).coerceIn(0f, 1f)

    /**
     * Screen Y coordinate considering isometric elevation:
     * Screen Y = y - z
     */
    val screenY: Float
        get() = y - z

    fun update(deltaSeconds: Float) {
        lifetime += deltaSeconds
        x += vx * deltaSeconds
        y += vy * deltaSeconds
        z += vz * deltaSeconds
        rotation += rotationSpeed * deltaSeconds

        // Gravity pulling down on Z axis
        vz -= 550f * deltaSeconds

        // Ground collision & bounce (z <= 0)
        if (z <= 0f) {
            z = 0f
            if (bounceCount < 2 && abs(vz) > 40f) {
                vz = -vz * 0.42f // Bouncy restitution
                vx *= 0.65f      // Ground friction
                vy *= 0.65f
                bounceCount++
            } else {
                vz = 0f
                vx *= 0.85f      // Sliding friction
                vy *= 0.85f
            }
        }
    }
}
