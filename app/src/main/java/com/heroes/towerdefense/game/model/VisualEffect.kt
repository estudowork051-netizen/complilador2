package com.heroes.towerdefense.game.model

/**
 * Transient visual impact / explosion effect rendered on the canvas.
 * Extended in Part 4 with VisualEffectType classification, multi-tier rendering,
 * and integration with physical debris and camera screen shake.
 */
data class VisualEffect(
    val id: String,
    val x: Float,
    val y: Float,
    val maxRadius: Float,
    val maxDurationSeconds: Float = 0.28f,
    val color: Int,
    val isAreaExplosion: Boolean = false,
    val effectType: VisualEffectType = VisualEffectType.fromEffectId(id, isAreaExplosion)
) {
    var elapsedSeconds: Float = 0f
        private set

    val isExpired: Boolean
        get() = elapsedSeconds >= maxDurationSeconds

    val progress: Float
        get() = (elapsedSeconds / maxDurationSeconds).coerceIn(0f, 1f)

    val currentRadius: Float
        get() = maxRadius * progress

    fun update(deltaTime: Float) {
        elapsedSeconds += deltaTime
    }
}
