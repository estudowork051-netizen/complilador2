package com.heroes.towerdefense.game.model

import com.heroes.towerdefense.model.hero.HeroSkill

/**
 * Tracks runtime cooldown state and usage of a hero's skill during battle.
 */
class HeroSkillState(
    val skill: HeroSkill,
    var currentCooldown: Float = 0f
) {
    val isReady: Boolean
        get() = currentCooldown <= 0f

    val remainingCooldownRatio: Float
        get() = if (skill.cooldownSeconds > 0f) {
            (currentCooldown / skill.cooldownSeconds).coerceIn(0f, 1f)
        } else {
            0f
        }

    fun trigger(): Boolean {
        if (!isReady) return false
        currentCooldown = skill.cooldownSeconds
        return true
    }

    fun update(deltaTime: Float) {
        if (currentCooldown > 0f) {
            currentCooldown = (currentCooldown - deltaTime).coerceAtLeast(0f)
        }
    }

    fun resetCooldown() {
        currentCooldown = 0f
    }
}
