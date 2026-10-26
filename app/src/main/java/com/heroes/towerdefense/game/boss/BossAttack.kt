package com.heroes.towerdefense.game.boss

/**
 * Types of attacks and behaviors executed by bosses.
 */
enum class BossAttackType {
    EARTH_SLAM,
    SHADOW_VEIL,
    SUMMON_MINIONS,
    ASTRAL_BREATH,
    STARFALL
}

/**
 * Model of a telegraphed special ability or pattern for a boss.
 */
data class BossAttack(
    val type: BossAttackType,
    val name: String,
    val telegraphDuration: Float = 1.5f,
    val cooldown: Float = 6.0f,
    val damage: Int = 2,
    val range: Float = 0.25f,
    val isAoE: Boolean = true,
    val vfxColor: Int = 0xFFEF4444.toInt(),
    val telegraphWarningMessage: String = ""
) {
    init {
        require(name.isNotBlank()) { "Attack name cannot be blank" }
        require(telegraphDuration > 0f) { "Telegraph duration must be positive" }
        require(cooldown > 0f) { "Attack cooldown must be positive" }
        require(damage >= 0) { "Attack damage cannot be negative" }
        require(range > 0f) { "Attack range must be positive" }
    }
}
