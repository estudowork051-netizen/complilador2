package com.heroes.towerdefense.game.view

import kotlin.math.cos
import kotlin.math.sin

/**
 * Camera screen shake controller providing visceral physical impact feedback
 * when cannons fire, bosses land devastating slams, or massive explosions detonate.
 * Implements smooth exponential decay to prevent abrupt stops or jarring jitter.
 */
class CameraScreenShake {

    var offsetX: Float = 0f
        private set
    var offsetY: Float = 0f
        private set

    var currentIntensity: Float = 0f
        private set
    var duration: Float = 0f
        private set
    var elapsed: Float = 0f
        private set

    val isShaking: Boolean
        get() = elapsed < duration && currentIntensity > 0.01f

    /**
     * Triggers screen shake with the given peak intensity (pixels) and duration (seconds).
     * If a shake is already active, blends the stronger intensity.
     */
    fun trigger(intensity: Float, shakeDuration: Float = 0.25f) {
        currentIntensity = maxOf(currentIntensity, intensity)
        duration = maxOf(duration - elapsed, shakeDuration)
        elapsed = 0f
    }

    /**
     * Updates shake simulation using damped multi-frequency harmonic oscillation.
     */
    fun update(deltaSeconds: Float) {
        if (!isShaking) {
            offsetX = 0f
            offsetY = 0f
            currentIntensity = 0f
            elapsed = 0f
            return
        }

        elapsed += deltaSeconds
        val progress = (elapsed / duration).coerceIn(0f, 1f)
        val damping = (1f - progress) * (1f - progress) // Quadratic decay

        val angle = elapsed * 45f // Fast oscillating harmonic
        offsetX = sin(angle) * currentIntensity * damping
        offsetY = cos(angle * 1.37f) * currentIntensity * damping * 0.75f

        if (progress >= 1f) {
            offsetX = 0f
            offsetY = 0f
            currentIntensity = 0f
        }
    }

    fun reset() {
        offsetX = 0f
        offsetY = 0f
        currentIntensity = 0f
        elapsed = 0f
        duration = 0f
    }
}
