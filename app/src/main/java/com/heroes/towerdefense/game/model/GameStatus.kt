package com.heroes.towerdefense.game.model

/**
 * Operational states of an active Tower Defense match.
 */
enum class GameStatus {
    NOT_STARTED,
    RUNNING,
    PAUSED,
    VICTORY,
    DEFEAT
}
