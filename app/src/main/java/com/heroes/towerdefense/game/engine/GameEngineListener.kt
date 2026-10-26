package com.heroes.towerdefense.game.engine

import com.heroes.towerdefense.game.boss.ActiveBoss
import com.heroes.towerdefense.game.boss.BossAttack
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.DeployedHero
import com.heroes.towerdefense.game.model.GameStatus
import com.heroes.towerdefense.game.model.PlacedTower
import com.heroes.towerdefense.model.hero.HeroSkill

/**
 * Event callbacks emitted by the GameEngine.
 */
interface GameEngineListener {
    fun onBaseHealthChanged(currentHealth: Int, maxHealth: Int) {}
    fun onWaveStarted(waveNumber: Int, totalWaves: Int, enemiesInWave: Int) {}
    fun onWaveCompleted(waveNumber: Int, isFinalWave: Boolean) {}
    fun onStatusChanged(status: GameStatus) {}
    fun onTowerBuilt(tower: PlacedTower) {}
    fun onEnemyDamaged(enemy: ActiveEnemy, damage: Int, isKilled: Boolean) {}
    fun onEnemyDefeated(enemy: ActiveEnemy) {}
    fun onHeroDeployed(hero: DeployedHero) {}
    fun onHeroSkillCast(hero: DeployedHero, skill: HeroSkill, targetsHit: List<ActiveEnemy>) {}

    // Part 7 — Bosses & Advanced Combat Events
    fun onBossSpawned(boss: ActiveBoss) {}
    fun onBossIntroStarted(boss: ActiveBoss) {}
    fun onBossIntroFinished(boss: ActiveBoss) {}
    fun onBossPhaseChanged(boss: ActiveBoss, newPhase: Int) {}
    fun onBossAttackTelegraphed(boss: ActiveBoss, attack: BossAttack) {}
    fun onBossAttackExecuted(boss: ActiveBoss, attack: BossAttack) {}
    fun onBossDefeated(boss: ActiveBoss) {}
}
