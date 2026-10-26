package com.heroes.towerdefense.game.model

import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.hero.HeroSkill
import kotlin.math.sqrt

/**
 * Runtime active Hero unit deployed on the battlefield.
 * Manages automated basic attack cooldowns, target acquisition, active skills,
 * and articulated character animation states (IDLE, WINDUP, STRIKE, RECOVERY, SKILL_CAST, VICTORY).
 */
class DeployedHero(
    val hero: Hero,
    var x: Float = 0.42f,
    var y: Float = 0.48f,
    val range: Float = hero.attackRange,
    val attackSpeed: Float = hero.attackSpeed,
    val baseDamage: Int = hero.attackPower
) {
    val skills: List<HeroSkillState> = hero.skills.map { HeroSkillState(it) }

    var attackCooldown: Float = 0f
        private set

    var currentTargetId: String? = null
    var totalDamageDealt: Int = 0

    // Animation & Articulation State (Visual Upgrade Part 1)
    var animState: HeroAnimState = HeroAnimState.IDLE
        private set

    var animTimer: Float = 0f
        private set

    var stateTimer: Float = 0f
        private set

    var facingDirection: Float = 1f // 1.0f = facing right/down, -1.0f = facing left
        private set

    var lastTargetX: Float = 0f
        private set

    var lastTargetY: Float = 0f
        private set

    var activeCastSkill: HeroSkill? = null
        private set

    val canAttack: Boolean
        get() = attackCooldown <= 0f

    val attackCooldownMax: Float
        get() = if (attackSpeed > 0f) 1f / attackSpeed else 1f

    /**
     * Updates cooldowns and advances articulated animation state timers.
     */
    fun update(deltaTime: Float) {
        updateCooldown(deltaTime)
        animTimer += deltaTime
        stateTimer += deltaTime

        when (animState) {
            HeroAnimState.WINDUP -> {
                val windupDuration = 0.12f
                if (stateTimer >= windupDuration) {
                    animState = HeroAnimState.STRIKE
                    stateTimer = 0f
                }
            }
            HeroAnimState.STRIKE -> {
                val strikeDuration = 0.18f
                if (stateTimer >= strikeDuration) {
                    animState = HeroAnimState.RECOVERY
                    stateTimer = 0f
                }
            }
            HeroAnimState.RECOVERY -> {
                val recoveryDuration = 0.15f
                if (stateTimer >= recoveryDuration) {
                    animState = HeroAnimState.IDLE
                    stateTimer = 0f
                }
            }
            HeroAnimState.SKILL_CAST -> {
                val skillDuration = 0.55f
                if (stateTimer >= skillDuration) {
                    animState = HeroAnimState.IDLE
                    stateTimer = 0f
                    activeCastSkill = null
                }
            }
            HeroAnimState.VICTORY -> {
                // Remains in victory loop!
            }
            HeroAnimState.IDLE -> {
                // Loops continuously via animTimer
            }
        }
    }

    fun updateCooldown(deltaTime: Float) {
        if (attackCooldown > 0f) {
            attackCooldown = (attackCooldown - deltaTime).coerceAtLeast(0f)
        }
        for (skillState in skills) {
            skillState.update(deltaTime)
        }
    }

    fun resetAttackCooldown() {
        attackCooldown = attackCooldownMax
    }

    fun triggerAttack(targetX: Float, targetY: Float) {
        if (animState != HeroAnimState.VICTORY) {
            animState = HeroAnimState.WINDUP
            stateTimer = 0f
            lastTargetX = targetX
            lastTargetY = targetY
            facingDirection = if (targetX < x) -1f else 1f
        }
    }

    fun triggerSkill(skill: HeroSkill, targetX: Float? = null, targetY: Float? = null) {
        if (animState != HeroAnimState.VICTORY) {
            animState = HeroAnimState.SKILL_CAST
            stateTimer = 0f
            activeCastSkill = skill
            if (targetX != null) {
                lastTargetX = targetX
                lastTargetY = targetY ?: y
                facingDirection = if (targetX < x) -1f else 1f
            }
        }
    }

    fun triggerVictory() {
        animState = HeroAnimState.VICTORY
        stateTimer = 0f
    }

    fun resetAnimation() {
        animState = HeroAnimState.IDLE
        stateTimer = 0f
        activeCastSkill = null
    }

    fun reset() {
        attackCooldown = 0f
        currentTargetId = null
        totalDamageDealt = 0
        animState = HeroAnimState.IDLE
        animTimer = 0f
        stateTimer = 0f
        facingDirection = 1f
        lastTargetX = 0f
        lastTargetY = 0f
        activeCastSkill = null
        for (skillState in skills) {
            skillState.resetCooldown()
        }
    }

    fun distanceTo(otherX: Float, otherY: Float): Float {
        val dx = x - otherX
        val dy = y - otherY
        return sqrt(dx * dx + dy * dy)
    }

    fun getSkill(index: Int): HeroSkillState? {
        return skills.getOrNull(index)
    }
}
