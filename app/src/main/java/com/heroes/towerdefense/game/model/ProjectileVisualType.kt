package com.heroes.towerdefense.game.model

import com.heroes.towerdefense.model.tower.TowerType

/**
 * Visual styling and asset specification for stylized 2.5D / 2D combat projectiles.
 * Categorizes ballistic arrows, arcane comet missiles, heavy iron cannonballs,
 * and individual signature hero projectiles.
 */
enum class ProjectileVisualType {
    ARCHER_ARROW,
    MAGE_ENERGY_MISSILE,
    CANNON_BALL,
    FROST_SHARD,
    FIRE_BOLT,
    ASTRAL_ARROW,
    SHADOW_DAGGER,
    GYRO_ROCKET,
    COSMIC_ORB;

    val isBallisticArc: Boolean
        get() = this == ARCHER_ARROW || this == CANNON_BALL || this == GYRO_ROCKET

    val hasSmokeTrail: Boolean
        get() = this == CANNON_BALL || this == GYRO_ROCKET

    val hasSparkleTrail: Boolean
        get() = this == MAGE_ENERGY_MISSILE || this == ASTRAL_ARROW || this == COSMIC_ORB || this == FIRE_BOLT

    companion object {
        fun fromTowerType(towerType: TowerType): ProjectileVisualType {
            return when (towerType) {
                TowerType.ARCHER_TOWER -> ARCHER_ARROW
                TowerType.MAGE_TOWER -> MAGE_ENERGY_MISSILE
                TowerType.CANNON_TOWER -> CANNON_BALL
                TowerType.FROST_TOWER -> FROST_SHARD
            }
        }
    }
}
