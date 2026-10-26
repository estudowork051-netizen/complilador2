package com.heroes.towerdefense.game.model

/**
 * Visual styling and signature asset categorization for combat VFX, explosions,
 * elemental bursts, and individual hero abilities.
 */
enum class VisualEffectType {
    IMPACT_SPARK,
    AREA_EXPLOSION,
    HERO_BASIC_ATTACK,
    KAEL_FLAME_STRIKE,
    KAEL_FLAME_WAVE,
    LYRA_PRECISION_SHOT,
    LYRA_ARROW_VOLLEY,
    BRAKK_GROUND_SLAM,
    BRAKK_SHIELD_WALL,
    NYRA_SHADOW_STRIKE,
    NYRA_BLADE_FLURRY,
    TIKO_FRAG_MINE,
    TIKO_EXPLOSIVE_DEVICE,
    VESPER_ASTRAL_COMET,
    VESPER_COSMIC_SINGULARITY,
    BOSS_SLAM,
    BOSS_DEFEAT_BURST;

    val triggersScreenShake: Boolean
        get() = this == AREA_EXPLOSION ||
                this == BRAKK_GROUND_SLAM ||
                this == TIKO_EXPLOSIVE_DEVICE ||
                this == KAEL_FLAME_WAVE ||
                this == VESPER_COSMIC_SINGULARITY ||
                this == BOSS_SLAM ||
                this == BOSS_DEFEAT_BURST

    val shakeIntensity: Float
        get() = when (this) {
            BOSS_DEFEAT_BURST -> 12f
            TIKO_EXPLOSIVE_DEVICE -> 10f
            BRAKK_GROUND_SLAM -> 9f
            VESPER_COSMIC_SINGULARITY -> 8f
            AREA_EXPLOSION -> 7f
            KAEL_FLAME_WAVE -> 6f
            BOSS_SLAM -> 8f
            else -> 0f
        }

    val generatesDebris: Boolean
        get() = this == AREA_EXPLOSION ||
                this == BRAKK_GROUND_SLAM ||
                this == TIKO_FRAG_MINE ||
                this == TIKO_EXPLOSIVE_DEVICE ||
                this == KAEL_FLAME_STRIKE ||
                this == BOSS_DEFEAT_BURST

    companion object {
        fun fromEffectId(id: String, isAreaExplosion: Boolean): VisualEffectType {
            return when {
                id.contains("kael_skill_1") -> KAEL_FLAME_STRIKE
                id.contains("kael_skill_2") -> KAEL_FLAME_WAVE
                id.contains("lyra_skill_1") -> LYRA_PRECISION_SHOT
                id.contains("lyra_skill_2") -> LYRA_ARROW_VOLLEY
                id.contains("brakk_skill_1") -> BRAKK_GROUND_SLAM
                id.contains("brakk_skill_2") -> BRAKK_SHIELD_WALL
                id.contains("nyra_skill_1") -> NYRA_SHADOW_STRIKE
                id.contains("nyra_skill_2") -> NYRA_BLADE_FLURRY
                id.contains("tiko_skill_1") -> TIKO_FRAG_MINE
                id.contains("tiko_skill_2") -> TIKO_EXPLOSIVE_DEVICE
                id.contains("vesper_skill_1") -> VESPER_ASTRAL_COMET
                id.contains("vesper_skill_2") -> VESPER_COSMIC_SINGULARITY
                id.contains("hero_atk") -> HERO_BASIC_ATTACK
                id.contains("boss_defeat") -> BOSS_DEFEAT_BURST
                isAreaExplosion -> AREA_EXPLOSION
                else -> IMPACT_SPARK
            }
        }
    }
}
