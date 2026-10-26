package com.heroes.towerdefense.game.model

/**
 * Atmospheric ambient particle for environmental immersion in StageOneCanvasView:
 * - Pollen & drifting leaves in Highland Forest
 * - Spores & glowing fireflies in Whispering Woods
 * - Drifting spectral mist in Ancient Ruins
 * - Glistening mineral crystals in Crystal Caverns
 * - Rising embers in Volcanic Fortress
 * - Twinkling cosmic stardust in Astral Peak
 */
data class AmbientParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Int,
    var size: Float,
    var phase: Float = 0f,
    var oscillationSpeed: Float = 1.5f,
    var baseAlpha: Float = 0.6f
) {
    var currentAlpha: Float = baseAlpha

    fun update(deltaSeconds: Float, boundsWidth: Float, boundsHeight: Float) {
        phase += oscillationSpeed * deltaSeconds
        x += vx * deltaSeconds + (Math.sin(phase.toDouble()).toFloat() * 15f * deltaSeconds)
        y += vy * deltaSeconds

        // Twinkling alpha pulsation
        currentAlpha = (baseAlpha * (0.6f + 0.4f * Math.sin(phase.toDouble() * 2.0).toFloat())).coerceIn(0f, 1f)

        // Wrap around boundaries smoothly
        if (x < -20f) x = boundsWidth + 20f
        else if (x > boundsWidth + 20f) x = -20f

        if (y < -20f) y = boundsHeight + 20f
        else if (y > boundsHeight + 20f) y = -20f
    }
}

enum class AmbientParticleType {
    POLLEN,
    FIREFLIES,
    SPECTRAL_MIST,
    CRYSTAL_DUST,
    VOLCANIC_EMBERS,
    ASTRAL_STARDUST
}
