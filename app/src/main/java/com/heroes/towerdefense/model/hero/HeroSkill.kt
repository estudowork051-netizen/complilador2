package com.heroes.towerdefense.model.hero

/**
 * Target type for hero active skills.
 */
enum class SkillTargetType {
    SINGLE_TARGET,
    AREA_OF_EFFECT,
    SELF_BUFF
}

/**
 * Model representing an active triggerable skill of a Hero.
 * Each of the 6 heroes possesses 2 unique skills (12 unique skills in total).
 */
data class HeroSkill(
    val id: String,
    val name: String,
    val description: String,
    val cooldownSeconds: Float,
    val damage: Int = 0,
    val range: Float = 0.25f,
    val splashRadius: Float = 0.0f,
    val targetType: SkillTargetType = SkillTargetType.SINGLE_TARGET,
    val vfxColor: Int = 0xFFF59E0B.toInt()
) {
    val isAoE: Boolean
        get() = targetType == SkillTargetType.AREA_OF_EFFECT || (targetType != SkillTargetType.SELF_BUFF && splashRadius > 0f)

    init {
        require(id.isNotBlank()) { "Skill ID must not be blank" }
        require(name.isNotBlank()) { "Skill name must not be blank" }
        require(description.isNotBlank()) { "Skill description must not be blank" }
        require(cooldownSeconds > 0f) { "Skill cooldown must be greater than zero" }
        require(damage >= 0) { "Skill damage must be non-negative" }
    }
}
