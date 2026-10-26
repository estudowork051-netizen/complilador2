package com.heroes.towerdefense.game.model

/**
 * Visual particle representation for dynamic combat effects (Part 8).
 * Represents sparks, magic glitter, explosion fragments, and celebration fireworks.
 */
data class CombatParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Int,
    var radius: Float,
    var alpha: Float = 1.0f,
    val maxLifetime: Float = 0.6f,
    var lifetime: Float = 0.0f
) {
    val isExpired: Boolean
        get() = lifetime >= maxLifetime

    val progress: Float
        get() = (lifetime / maxLifetime).coerceIn(0.0f, 1.0f)

    fun update(deltaSeconds: Float) {
        lifetime += deltaSeconds
        x += vx * deltaSeconds
        y += vy * deltaSeconds
        // Subtle gravity and drag
        vy += 80f * deltaSeconds
        vx *= 0.96f
        vy *= 0.96f
        alpha = (1.0f - progress).coerceIn(0.0f, 1.0f)
        radius = (radius * (1.0f - progress * 0.4f)).coerceAtLeast(1.0f)
    }
}
