package com.heroes.towerdefense.game.map

/**
 * Graphic visual quality tier allowing players to balance visual fidelity
 * (dynamic lights, ambient particles, foliage sway, screen shake, debris)
 * against performance and battery consumption.
 */
enum class GraphicQuality(
    val displayName: String,
    val maxAmbientParticles: Int,
    val enableDynamicLighting: Boolean,
    val enableScreenShake: Boolean,
    val enableFoliageSway: Boolean,
    val enablePhysicsDebris: Boolean,
    val maxDebrisCount: Int = 0,
    val maxParticles: Int = 20
) {
    LOW(
        displayName = "Baixo (Bateria)",
        maxAmbientParticles = 0,
        enableDynamicLighting = false,
        enableScreenShake = false,
        enableFoliageSway = false,
        enablePhysicsDebris = false,
        maxDebrisCount = 0,
        maxParticles = 20
    ),
    MEDIUM(
        displayName = "Médio (Equilibrado)",
        maxAmbientParticles = 12,
        enableDynamicLighting = true,
        enableScreenShake = true,
        enableFoliageSway = true,
        enablePhysicsDebris = false,
        maxDebrisCount = 18,
        maxParticles = 50
    ),
    HIGH(
        displayName = "Alto (Visual Máximo)",
        maxAmbientParticles = 28,
        enableDynamicLighting = true,
        enableScreenShake = true,
        enableFoliageSway = true,
        enablePhysicsDebris = true,
        maxDebrisCount = 36,
        maxParticles = 100
    )
}
