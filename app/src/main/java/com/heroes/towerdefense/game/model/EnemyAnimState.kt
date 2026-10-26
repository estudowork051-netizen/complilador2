package com.heroes.towerdefense.game.model

/**
 * High-fidelity articulated animation states for enemies and epic bosses
 * in Heroes Tower Defense (Visual Upgrade Part 2).
 *
 * Drives procedural limb movement, weapon swings, cast gestures, hit flinches,
 * death dissolves, and boss phase transitions with zero-allocation rendering.
 */
enum class EnemyAnimState {
    IDLE,
    WALK,
    ATTACK,
    HIT,
    DEATH,
    SPECIAL,
    PHASE_TRANSITION;

    val isAttacking: Boolean
        get() = this == ATTACK || this == SPECIAL

    val isMoving: Boolean
        get() = this == WALK

    val isDying: Boolean
        get() = this == DEATH

    val isHit: Boolean
        get() = this == HIT
}
