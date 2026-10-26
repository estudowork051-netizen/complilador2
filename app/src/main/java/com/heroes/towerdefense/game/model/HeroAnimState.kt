package com.heroes.towerdefense.game.model

/**
 * High-fidelity animation states for Heroes in Heroes Tower Defense.
 * Drives articulated skeletal rendering, weapon swing arcs, bow draw cycles,
 * skill charging, and victory celebrations.
 */
enum class HeroAnimState {
    IDLE,
    WINDUP,
    STRIKE,
    RECOVERY,
    SKILL_CAST,
    VICTORY;

    val isAttacking: Boolean
        get() = this == WINDUP || this == STRIKE || this == RECOVERY
}
