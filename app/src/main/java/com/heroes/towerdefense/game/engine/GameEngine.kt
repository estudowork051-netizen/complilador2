package com.heroes.towerdefense.game.engine

import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.game.boss.ActiveBoss
import com.heroes.towerdefense.game.boss.BossAttack
import com.heroes.towerdefense.game.boss.BossAttackType
import com.heroes.towerdefense.game.boss.BossConfig
import com.heroes.towerdefense.game.boss.BossType
import com.heroes.towerdefense.game.map.StageOneMap
import com.heroes.towerdefense.game.map.Waypoint
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.CombatProjectile
import com.heroes.towerdefense.game.model.DeployedHero
import com.heroes.towerdefense.game.model.EnemyVisualType
import com.heroes.towerdefense.game.model.GameStatus
import com.heroes.towerdefense.game.model.PlacedTower
import com.heroes.towerdefense.game.model.TowerSpot
import com.heroes.towerdefense.game.model.VisualEffect
import com.heroes.towerdefense.game.model.WaveConfig
import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.hero.HeroSkill
import com.heroes.towerdefense.model.hero.SkillTargetType
import com.heroes.towerdefense.model.tower.TowerType

/**
 * Pure-logic Game Engine for Heroes Tower Defense.
 * Completely decoupled from Android UI views for deterministic execution and full unit testability.
 * Coordinates waves, enemy movement, tower placement, automated targeting, projectile physics,
 * combat damage, hero deployment, automated hero combat, active triggerable hero skills,
 * and epic boss battles with telegraphed special abilities and multi-phase mechanics.
 */
class GameEngine(
    val waypoints: List<Waypoint> = StageOneMap.waypoints,
    val waves: List<WaveConfig> = WaveConfig.getStageOneWaves(),
    val maxBaseHealth: Int = GameConstants.STAGE_ONE_INITIAL_LIVES,
    val towerSpots: List<TowerSpot> = StageOneMap.defaultTowerSpots,
    val maxTowersAllowed: Int = GameConstants.STAGE_ONE_MAX_TOWERS,
    var listener: GameEngineListener? = null,
    var selectedHero: Hero = Hero.getPlannedHeroes().first(),
    val towerDamageMultiplier: Float = 1.0f,
    val startingGoldBonus: Int = 0,
    val stageNumber: Int = 1
) {
    var status: GameStatus = GameStatus.NOT_STARTED
        private set

    var baseHealth: Int = maxBaseHealth
        private set

    var activeBoss: ActiveBoss? = null

    val isBossActive: Boolean
        get() = activeBoss?.let { it.isAlive && !it.isIntroActive } ?: false

    val isBossIntroActive: Boolean
        get() = activeBoss?.isIntroActive == true

    var currentWaveIndex: Int = 0
        private set

    var enemiesSpawnedInCurrentWave: Int = 0
        private set

    private var timeSinceLastSpawn: Float = 0f
    private var interWaveTimer: Float = 0f
    var isWaitingForNextWave: Boolean = false
        private set

    private var enemyIdCounter: Long = 0
    private var towerIdCounter: Long = 0
    private var projectileIdCounter: Long = 0
    private var effectIdCounter: Long = 0

    val activeEnemies: MutableList<ActiveEnemy> = mutableListOf()
    val placedTowers: MutableList<PlacedTower> = mutableListOf()
    val activeProjectiles: MutableList<CombatProjectile> = mutableListOf()
    val visualEffects: MutableList<VisualEffect> = mutableListOf()

    var deployedHero: DeployedHero? = null
        private set

    val towersBuiltCount: Int
        get() = placedTowers.size

    val remainingTowersToBuild: Int
        get() = (maxTowersAllowed - placedTowers.size).coerceAtLeast(0)

    val currentWave: WaveConfig
        get() = waves[currentWaveIndex.coerceIn(0, waves.size - 1)]

    val currentWaveNumber: Int
        get() = currentWaveIndex + 1

    val totalWavesCount: Int
        get() = waves.size

    val remainingEnemiesToSpawn: Int
        get() = (currentWave.enemyCount - enemiesSpawnedInCurrentWave).coerceAtLeast(0)

    val totalEnemiesRemainingInWave: Int
        get() = remainingEnemiesToSpawn + activeEnemies.count { it.isAlive }

    /**
     * Starts the match from Wave 1.
     */
    fun start() {
        if (status == GameStatus.NOT_STARTED || status == GameStatus.DEFEAT || status == GameStatus.VICTORY) {
            resetState()
            status = GameStatus.RUNNING
            listener?.onStatusChanged(status)
            listener?.onBaseHealthChanged(baseHealth, maxBaseHealth)
            listener?.onWaveStarted(currentWaveNumber, totalWavesCount, currentWave.enemyCount)
        }
    }

    /**
     * Pauses the match. Freezes enemies, timers, towers, projectiles, and hero actions.
     */
    fun pause() {
        if (status == GameStatus.RUNNING) {
            status = GameStatus.PAUSED
            listener?.onStatusChanged(status)
        }
    }

    /**
     * Resumes the paused match.
     */
    fun resume() {
        if (status == GameStatus.PAUSED) {
            status = GameStatus.RUNNING
            listener?.onStatusChanged(status)
        }
    }

    /**
     * Completely resets the match state and starts clean from Wave 1.
     */
    fun restart() {
        resetState()
        status = GameStatus.RUNNING
        listener?.onStatusChanged(status)
        listener?.onBaseHealthChanged(baseHealth, maxBaseHealth)
        listener?.onWaveStarted(currentWaveNumber, totalWavesCount, currentWave.enemyCount)
    }

    private fun resetState() {
        baseHealth = maxBaseHealth
        currentWaveIndex = 0
        enemiesSpawnedInCurrentWave = 0
        timeSinceLastSpawn = 0f
        interWaveTimer = 0f
        isWaitingForNextWave = false
        enemyIdCounter = 0
        projectileIdCounter = 0
        effectIdCounter = 0
        activeBoss = null
        activeEnemies.clear()
        activeProjectiles.clear()
        visualEffects.clear()
        for (spot in towerSpots) {
            spot.placedTower = null
        }
        placedTowers.clear()
        deployedHero?.let { current ->
            if (current.hero.id == selectedHero.id) {
                current.reset()
            } else {
                deployedHero = DeployedHero(
                    hero = selectedHero,
                    x = current.x,
                    y = current.y,
                    range = selectedHero.attackRange,
                    attackSpeed = selectedHero.attackSpeed,
                    baseDamage = selectedHero.attackPower
                )
            }
        }
    }

    // =========================================================================
    // Hero Deployment & Skills System (Part 5)
    // =========================================================================

    /**
     * Deploys the selected hero at specified normalized coordinates on the battlefield.
     */
    fun deployHero(hero: Hero = selectedHero, x: Float = 0.42f, y: Float = 0.48f): DeployedHero {
        selectedHero = hero
        val deployed = DeployedHero(
            hero = hero,
            x = x,
            y = y,
            range = hero.attackRange,
            attackSpeed = hero.attackSpeed,
            baseDamage = hero.attackPower
        )
        deployedHero = deployed
        listener?.onHeroDeployed(deployed)
        return deployed
    }

    /**
     * Removes the active deployed hero from the battlefield.
     */
    fun removeHero() {
        deployedHero = null
    }

    /**
     * Casts an active skill by index (0 for Skill 1, 1 for Skill 2).
     * Validates cooldown, resolves single-target or AoE damage without duplicate hits,
     * triggers effects and emits listener notifications.
     */
    fun castHeroSkill(skillIndex: Int): Boolean {
        if (status != GameStatus.RUNNING) return false
        val hero = deployedHero ?: return false
        val skillState = hero.getSkill(skillIndex) ?: return false
        if (!skillState.isReady) return false

        val skill = skillState.skill
        val targetsHit = mutableListOf<ActiveEnemy>()

        if (skill.targetType == SkillTargetType.SELF_BUFF) {
            // Defensive / fortification ability (e.g. Brakk)
            baseHealth = (baseHealth + 2).coerceAtMost(maxBaseHealth)
            listener?.onBaseHealthChanged(baseHealth, maxBaseHealth)

            // Repels nearby enemies
            if (skill.damage > 0) {
                val radius = if (skill.splashRadius > 0f) skill.splashRadius else skill.range
                val inRange = activeEnemies.filter { enemy ->
                    enemy.isAlive && enemy.distanceTo(hero.x, hero.y) <= radius
                }.distinctBy { it.id }
                for (enemy in inRange) {
                    val killed = enemy.takeDamage(skill.damage)
                    hero.totalDamageDealt += skill.damage
                    listener?.onEnemyDamaged(enemy, skill.damage, killed)
                    targetsHit.add(enemy)
                }
            }

            effectIdCounter++
            visualEffects.add(
                VisualEffect(
                    id = "fx_skill_${skill.id}_$effectIdCounter",
                    x = hero.x,
                    y = hero.y,
                    maxRadius = 0.20f,
                    maxDurationSeconds = 0.50f,
                    color = skill.vfxColor,
                    isAreaExplosion = true
                )
            )
        } else if (skill.isAoE) {
            // AoE damage: centered on the closest enemy to the hero, or hero's own position
            val centerTarget = activeEnemies.filter { it.isAlive }.minByOrNull { it.distanceTo(hero.x, hero.y) }
            val cx = centerTarget?.x ?: hero.x
            val cy = centerTarget?.y ?: hero.y
            val radius = if (skill.splashRadius > 0f) skill.splashRadius else skill.range

            // Distinct targets safety check prevents any enemy from taking duplicate damage
            val inRange = activeEnemies.filter { enemy ->
                enemy.isAlive && enemy.distanceTo(cx, cy) <= radius
            }.distinctBy { it.id }

            for (enemy in inRange) {
                val killed = enemy.takeDamage(skill.damage)
                hero.totalDamageDealt += skill.damage
                listener?.onEnemyDamaged(enemy, skill.damage, killed)
                targetsHit.add(enemy)
            }

            effectIdCounter++
            visualEffects.add(
                VisualEffect(
                    id = "fx_skill_${skill.id}_$effectIdCounter",
                    x = cx,
                    y = cy,
                    maxRadius = radius,
                    maxDurationSeconds = 0.45f,
                    color = skill.vfxColor,
                    isAreaExplosion = true
                )
            )
        } else {
            // Single target skill: targets furthest enemy in range, fallback to furthest alive enemy on map
            val inRange = activeEnemies.filter { enemy ->
                enemy.isAlive && enemy.distanceTo(hero.x, hero.y) <= skill.range
            }
            val target = inRange.maxByOrNull { it.distanceTraveled }
                ?: activeEnemies.filter { it.isAlive }.maxByOrNull { it.distanceTraveled }

            if (target != null) {
                val killed = target.takeDamage(skill.damage)
                hero.totalDamageDealt += skill.damage
                listener?.onEnemyDamaged(target, skill.damage, killed)
                targetsHit.add(target)

                effectIdCounter++
                visualEffects.add(
                    VisualEffect(
                        id = "fx_skill_${skill.id}_$effectIdCounter",
                        x = target.x,
                        y = target.y,
                        maxRadius = 0.08f,
                        maxDurationSeconds = 0.35f,
                        color = skill.vfxColor,
                        isAreaExplosion = false
                    )
                )
            } else {
                // If no enemy is present, skill discharges at hero position
                effectIdCounter++
                visualEffects.add(
                    VisualEffect(
                        id = "fx_skill_${skill.id}_$effectIdCounter",
                        x = hero.x,
                        y = hero.y,
                        maxRadius = 0.08f,
                        maxDurationSeconds = 0.35f,
                        color = skill.vfxColor,
                        isAreaExplosion = false
                    )
                )
            }
        }

        skillState.trigger()
        val firstTarget = targetsHit.firstOrNull()
        hero.triggerSkill(skill, firstTarget?.x, firstTarget?.y)
        listener?.onHeroSkillCast(hero, skill, targetsHit)
        return true
    }

    // =========================================================================
    // Tower Building & Placement System
    // =========================================================================

    fun getSpot(spotId: String): TowerSpot? = towerSpots.find { it.id == spotId }

    /**
     * Validates whether a tower can be constructed at the specified spot.
     */
    fun canBuildTowerAt(spotId: String): Boolean {
        if (placedTowers.size >= maxTowersAllowed) {
            return false
        }
        val spot = getSpot(spotId) ?: return false
        if (spot.isOccupied) {
            return false
        }
        if (StageOneMap.isPointTooCloseToPath(spot.x, spot.y, GameConstants.MIN_BUILD_DISTANCE_FROM_PATH)) {
            return false
        }
        return true
    }

    /**
     * Constructs a tower of the given type at a valid spot.
     * Returns the created PlacedTower, or null if construction was invalid.
     */
    fun buildTower(spotId: String, type: TowerType): PlacedTower? {
        if (!canBuildTowerAt(spotId)) {
            return null
        }
        val spot = getSpot(spotId) ?: return null

        towerIdCounter++
        val towerId = "tower_${type.name.lowercase()}_$towerIdCounter"
        val baseTower = when (type) {
            TowerType.ARCHER_TOWER -> PlacedTower.createArcher(towerId, spotId, spot.x, spot.y)
            TowerType.MAGE_TOWER -> PlacedTower.createMage(towerId, spotId, spot.x, spot.y)
            TowerType.CANNON_TOWER -> PlacedTower.createCannon(towerId, spotId, spot.x, spot.y)
            TowerType.FROST_TOWER -> PlacedTower.createMage(towerId, spotId, spot.x, spot.y)
        }
        val tower = if (towerDamageMultiplier != 1.0f) {
            val scaledDamage = (baseTower.damage * towerDamageMultiplier).toInt()
            baseTower.copy(damage = scaledDamage)
        } else {
            baseTower
        }

        spot.placedTower = tower
        placedTowers.add(tower)
        listener?.onTowerBuilt(tower)
        return tower
    }

    /**
     * Removes an existing tower from a spot.
     */
    fun removeTower(spotId: String): Boolean {
        val spot = getSpot(spotId) ?: return false
        val tower = spot.placedTower ?: return false
        spot.placedTower = null
        placedTowers.remove(tower)
        return true
    }

    // =========================================================================
    // Simulation Tick Update (Movement, Targeting, Projectiles & Damage)
    // =========================================================================

    /**
     * Updates match simulation tick.
     * @param rawDeltaTime Elapsed time in seconds since previous tick.
     */
    fun update(rawDeltaTime: Float) {
        if (status != GameStatus.RUNNING) {
            return
        }

        // 1. Handle inter-wave countdown delay (timer-based, not physics-capped)
        if (isWaitingForNextWave) {
            interWaveTimer -= rawDeltaTime.coerceAtLeast(0f)
            if (interWaveTimer <= 0f) {
                interWaveTimer = 0f
                isWaitingForNextWave = false
                currentWaveIndex++
                enemiesSpawnedInCurrentWave = 0
                timeSinceLastSpawn = 0f
                listener?.onWaveStarted(currentWaveNumber, totalWavesCount, currentWave.enemyCount)
            }
            return
        }

        // Cap delta time to prevent large physics jumps after focus return during active combat
        val deltaTime = rawDeltaTime.coerceIn(0f, 0.1f)

        // 2. Handle enemy spawning
        if (enemiesSpawnedInCurrentWave < currentWave.enemyCount) {
            timeSinceLastSpawn += deltaTime
            if (timeSinceLastSpawn >= currentWave.spawnIntervalSeconds || enemiesSpawnedInCurrentWave == 0) {
                spawnEnemy()
                enemiesSpawnedInCurrentWave++
                timeSinceLastSpawn = 0f
            }
        }

        // 3. Update movement of active enemies & base damage on escape
        val enemyIterator = activeEnemies.iterator()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            if (!enemy.isAlive) {
                continue // Dead enemies are safely purged and emitted to listeners in Step 6
            }
            val reachedBase = enemy.update(deltaTime, waypoints)

            if (reachedBase) {
                enemyIterator.remove()
                val damageToApply = if (enemy is ActiveBoss) {
                    enemy.bossConfig.baseDamageOnEscape
                } else {
                    GameConstants.DAMAGE_PER_ESCAPED_ENEMY
                }
                applyBaseDamage(damageToApply)
                if (status == GameStatus.DEFEAT) {
                    return // Stop immediately on defeat
                }
            }
        }

        // 3.1 Update Boss Intro Sequence & Special Combat Abilities (Part 7)
        activeBoss?.let { boss ->
            if (boss.isIntroActive) {
                val finished = boss.updateIntro(deltaTime)
                if (finished) {
                    listener?.onBossIntroFinished(boss)
                }
            } else if (boss.isAlive) {
                val previousPhase = boss.currentPhase
                val attackToExecute = boss.updateBoss(deltaTime, waypoints)

                if (boss.currentPhase != previousPhase) {
                    listener?.onBossPhaseChanged(boss, boss.currentPhase)
                    effectIdCounter++
                    visualEffects.add(
                        VisualEffect(
                            id = "fx_boss_phase_$effectIdCounter",
                            x = boss.x,
                            y = boss.y,
                            maxRadius = 0.25f,
                            maxDurationSeconds = 0.60f,
                            color = boss.bossConfig.primaryColor,
                            isAreaExplosion = true
                        )
                    )
                }

                if (boss.isTelegraphing && boss.activeTelegraphAttack != null) {
                    listener?.onBossAttackTelegraphed(boss, boss.activeTelegraphAttack!!)
                }

                if (attackToExecute != null) {
                    executeBossAttack(boss, attackToExecute)
                }
            }
        }

        // 4. Automated Tower Targeting & Firing (Section 4)
        for (tower in placedTowers) {
            tower.updateCooldown(deltaTime)
            if (tower.canAttack) {
                // Find all live enemies within tower attack range (ignoring boss while intro is active)
                val inRangeEnemies = activeEnemies.filter { enemy ->
                    enemy.isAlive && (!enemy.isBoss || !(enemy as ActiveBoss).isIntroActive) && enemy.distanceTo(tower.x, tower.y) <= tower.range
                }

                // Prioritize enemy furthest along the path
                val target = inRangeEnemies.maxByOrNull { it.distanceTraveled }
                if (target != null) {
                    tower.currentTargetId = target.id
                    projectileIdCounter++
                    val projectile = CombatProjectile(
                        id = "proj_$projectileIdCounter",
                        sourceTowerId = tower.id,
                        towerType = tower.type,
                        startX = tower.x,
                        startY = tower.y,
                        currentX = tower.x,
                        currentY = tower.y,
                        targetEnemyId = target.id,
                        targetLastX = target.x,
                        targetLastY = target.y,
                        damage = tower.damage,
                        speed = tower.projectileSpeed,
                        splashRadius = tower.splashRadius
                    )
                    activeProjectiles.add(projectile)
                    tower.resetCooldown()
                } else {
                    tower.currentTargetId = null
                }
            }
        }

        // 4.1 Automated Hero Targeting & Basic Attack (Part 5)
        deployedHero?.let { hero ->
            hero.update(deltaTime)
            if (hero.canAttack) {
                val inRangeEnemies = activeEnemies.filter { enemy ->
                    enemy.isAlive && (!enemy.isBoss || !(enemy as ActiveBoss).isIntroActive) && enemy.distanceTo(hero.x, hero.y) <= hero.range
                }
                val target = inRangeEnemies.maxByOrNull { it.distanceTraveled }
                if (target != null) {
                    hero.currentTargetId = target.id
                    val killed = target.takeDamage(hero.baseDamage)
                    hero.totalDamageDealt += hero.baseDamage
                    listener?.onEnemyDamaged(target, hero.baseDamage, killed)
                    hero.resetAttackCooldown()
                    hero.triggerAttack(target.x, target.y)

                    effectIdCounter++
                    visualEffects.add(
                        VisualEffect(
                            id = "fx_hero_atk_$effectIdCounter",
                            x = target.x,
                            y = target.y,
                            maxRadius = 0.05f,
                            maxDurationSeconds = 0.20f,
                            color = hero.hero.rarity.colorInt,
                            isAreaExplosion = false
                        )
                    )
                } else {
                    hero.currentTargetId = null
                }
            }
        }

        // 5. Projectiles Movement, Impact Detection & Damage Resolution (Section 5)
        val projIterator = activeProjectiles.iterator()
        while (projIterator.hasNext()) {
            val projectile = projIterator.next()
            val hasHit = projectile.update(deltaTime, activeEnemies)

            if (hasHit) {
                projIterator.remove()
                if (!projectile.hasDealtDamage) {
                    projectile.markDamageDealt()

                    if (projectile.splashRadius > 0f) {
                        // Cannon Tower: Area of Effect (AOE) damage
                        val splashTargets = activeEnemies.filter { enemy ->
                            enemy.isAlive && enemy.distanceTo(projectile.currentX, projectile.currentY) <= projectile.splashRadius
                        }
                        for (enemy in splashTargets) {
                            val killed = enemy.takeDamage(projectile.damage)
                            listener?.onEnemyDamaged(enemy, projectile.damage, killed)
                            val sourceTower = placedTowers.find { it.id == projectile.sourceTowerId }
                            sourceTower?.let { it.totalDamageDealt += projectile.damage }
                        }

                        // Spawn explosion visual shockwave
                        effectIdCounter++
                        visualEffects.add(
                            VisualEffect(
                                id = "fx_$effectIdCounter",
                                x = projectile.currentX,
                                y = projectile.currentY,
                                maxRadius = projectile.splashRadius,
                                maxDurationSeconds = 0.35f,
                                color = 0xFFEF4444.toInt(), // Fiery crimson (#EF4444)
                                isAreaExplosion = true
                            )
                        )
                    } else {
                        // Archer & Mage: Direct Single-Target Damage
                        val target = activeEnemies.find { it.id == projectile.targetEnemyId && it.isAlive }
                        if (target != null) {
                            val killed = target.takeDamage(projectile.damage)
                            listener?.onEnemyDamaged(target, projectile.damage, killed)
                            val sourceTower = placedTowers.find { it.id == projectile.sourceTowerId }
                            sourceTower?.let { it.totalDamageDealt += projectile.damage }
                        }

                        // Spawn impact spark effect
                        effectIdCounter++
                        val effectColor = if (projectile.towerType == TowerType.MAGE_TOWER) {
                            0xFFA855F7.toInt() // Arcane violet (#A855F7)
                        } else {
                            0xFFF59E0B.toInt() // Physical arrow gold (#F59E0B)
                        }
                        visualEffects.add(
                            VisualEffect(
                                id = "fx_$effectIdCounter",
                                x = projectile.currentX,
                                y = projectile.currentY,
                                maxRadius = 0.04f,
                                maxDurationSeconds = 0.20f,
                                color = effectColor,
                                isAreaExplosion = false
                            )
                        )
                    }
                }
            }
        }

        // 6. Remove Defeated Enemies and Emit Events
        val defeatedEnemies = activeEnemies.filter { !it.isAlive }
        if (defeatedEnemies.isNotEmpty()) {
            activeEnemies.removeAll(defeatedEnemies)
            for (defeated in defeatedEnemies) {
                if (defeated is ActiveBoss) {
                    listener?.onBossDefeated(defeated)
                    effectIdCounter++
                    visualEffects.add(
                        VisualEffect(
                            id = "fx_boss_defeat_$effectIdCounter",
                            x = defeated.x,
                            y = defeated.y,
                            maxRadius = 0.30f,
                            maxDurationSeconds = 0.70f,
                            color = defeated.bossConfig.primaryColor,
                            isAreaExplosion = true
                        )
                    )
                    if (activeBoss == defeated) {
                        activeBoss = null
                    }
                }
                listener?.onEnemyDefeated(defeated)
            }
        }

        // 7. Update and Expire Visual Effects
        val fxIterator = visualEffects.iterator()
        while (fxIterator.hasNext()) {
            val fx = fxIterator.next()
            fx.update(deltaTime)
            if (fx.isExpired) {
                fxIterator.remove()
            }
        }

        // 8. Check wave completion and transition (Section 6)
        if (enemiesSpawnedInCurrentWave >= currentWave.enemyCount && activeEnemies.isEmpty() && activeProjectiles.isEmpty()) {
            if (currentWaveIndex < waves.size - 1) {
                // Advance to next wave after delay
                isWaitingForNextWave = true
                interWaveTimer = GameConstants.INTER_WAVE_DELAY_SECONDS
                listener?.onWaveCompleted(currentWaveNumber, isFinalWave = false)
            } else {
                // Final wave completed, all enemies cleared, no pending projectiles -> VICTORY!
                if (status == GameStatus.RUNNING && baseHealth > 0) {
                    status = GameStatus.VICTORY
                    deployedHero?.triggerVictory()
                    listener?.onWaveCompleted(currentWaveNumber, isFinalWave = true)
                    listener?.onStatusChanged(status)
                }
            }
        }
    }

    private fun spawnEnemy() {
        val bossConfig = currentWave.bossConfig
        if (bossConfig != null && enemiesSpawnedInCurrentWave >= currentWave.enemyCount - 1 && activeBoss == null) {
            spawnBoss(bossConfig)
            return
        }

        enemyIdCounter++
        val enemyType = when (currentWaveNumber) {
            1 -> EnemyVisualType.GOBLIN
            2 -> EnemyVisualType.SKELETON
            3 -> EnemyVisualType.ORC
            4 -> EnemyVisualType.DARK_MAGE
            else -> {
                when (enemiesSpawnedInCurrentWave % 4) {
                    0 -> EnemyVisualType.GOBLIN
                    1 -> EnemyVisualType.SKELETON
                    2 -> EnemyVisualType.ORC
                    else -> EnemyVisualType.DARK_MAGE
                }
            }
        }
        val enemy = ActiveEnemy(
            id = "enemy_s${stageNumber}_w${currentWaveNumber}_$enemyIdCounter",
            name = if (stageNumber == 1) "Goblin Invasor" else "Invasor da Fase $stageNumber",
            waveNumber = currentWaveNumber,
            maxHealth = 100 + (stageNumber - 1) * 20,
            currentHealth = 100 + (stageNumber - 1) * 20,
            moveSpeed = currentWave.enemySpeed,
            visualType = enemyType
        )
        enemy.spawnAt(waypoints.first())
        activeEnemies.add(enemy)
    }

    /**
     * Spawns an epic boss onto the battlefield, initiating its cinematic entrance.
     */
    fun spawnBoss(bossConfig: BossConfig): ActiveBoss {
        if (status == GameStatus.NOT_STARTED) {
            status = GameStatus.RUNNING
        }
        enemyIdCounter++
        val boss = ActiveBoss(
            bossConfig = bossConfig,
            waveNumber = currentWaveNumber,
            id = "boss_${bossConfig.id}_$enemyIdCounter"
        )
        boss.spawnAt(waypoints.first())
        activeBoss = boss
        activeEnemies.add(boss)
        listener?.onBossSpawned(boss)
        listener?.onBossIntroStarted(boss)
        return boss
    }

    /**
     * Skips the current boss cinematic intro and immediately starts combat.
     */
    fun skipBossIntro() {
        activeBoss?.let { boss ->
            if (boss.isIntroActive) {
                boss.finishIntro()
                listener?.onBossIntroFinished(boss)
            }
        }
    }

    /**
     * Executes a telegraphed boss special attack after its warning charge concludes.
     */
    private fun executeBossAttack(boss: ActiveBoss, attack: BossAttack) {
        if (!boss.isAlive || status != GameStatus.RUNNING) return

        when (attack.type) {
            BossAttackType.EARTH_SLAM -> {
                applyBaseDamage(attack.damage)
                boss.triggerVulnerability(boss.bossConfig.vulnerabilityDuration)
                effectIdCounter++
                visualEffects.add(
                    VisualEffect(
                        id = "fx_earth_slam_$effectIdCounter",
                        x = boss.x,
                        y = boss.y,
                        maxRadius = attack.range,
                        maxDurationSeconds = 0.50f,
                        color = attack.vfxColor,
                        isAreaExplosion = true
                    )
                )
            }
            BossAttackType.SHADOW_VEIL -> {
                boss.triggerProtection(boss.bossConfig.protectionDuration)
                effectIdCounter++
                visualEffects.add(
                    VisualEffect(
                        id = "fx_shadow_veil_$effectIdCounter",
                        x = boss.x,
                        y = boss.y,
                        maxRadius = 0.15f,
                        maxDurationSeconds = boss.bossConfig.protectionDuration,
                        color = attack.vfxColor,
                        isAreaExplosion = false
                    )
                )
            }
            BossAttackType.SUMMON_MINIONS -> {
                // Summon 2 shadow minions along the path near the boss
                for (i in 1..2) {
                    enemyIdCounter++
                    val minion = ActiveEnemy(
                        id = "shadow_minion_${enemyIdCounter}",
                        name = "Servo das Sombras",
                        waveNumber = currentWaveNumber,
                        maxHealth = 60,
                        currentHealth = 60,
                        moveSpeed = 0.20f,
                        visualType = EnemyVisualType.DARK_MAGE
                    )
                    minion.x = boss.x
                    minion.y = boss.y
                    minion.currentWaypointIndex = boss.currentWaypointIndex
                    minion.isAlive = true
                    minion.hasReachedBase = false
                    minion.distanceTraveled = boss.distanceTraveled
                    activeEnemies.add(minion)
                }
                effectIdCounter++
                visualEffects.add(
                    VisualEffect(
                        id = "fx_summon_$effectIdCounter",
                        x = boss.x,
                        y = boss.y,
                        maxRadius = 0.20f,
                        maxDurationSeconds = 0.40f,
                        color = attack.vfxColor,
                        isAreaExplosion = true
                    )
                )
            }
            BossAttackType.ASTRAL_BREATH -> {
                applyBaseDamage(attack.damage)
                effectIdCounter++
                visualEffects.add(
                    VisualEffect(
                        id = "fx_astral_breath_$effectIdCounter",
                        x = boss.x,
                        y = boss.y,
                        maxRadius = attack.range,
                        maxDurationSeconds = 0.45f,
                        color = attack.vfxColor,
                        isAreaExplosion = true
                    )
                )
            }
            BossAttackType.STARFALL -> {
                applyBaseDamage(attack.damage)
                effectIdCounter++
                visualEffects.add(
                    VisualEffect(
                        id = "fx_starfall_$effectIdCounter",
                        x = boss.x,
                        y = boss.y,
                        maxRadius = attack.range,
                        maxDurationSeconds = 0.55f,
                        color = attack.vfxColor,
                        isAreaExplosion = true
                    )
                )
            }
        }
        listener?.onBossAttackExecuted(boss, attack)
    }

    private fun applyBaseDamage(damage: Int) {
        baseHealth = (baseHealth - damage).coerceIn(0, maxBaseHealth)
        listener?.onBaseHealthChanged(baseHealth, maxBaseHealth)

        if (baseHealth <= 0) {
            status = GameStatus.DEFEAT
            listener?.onStatusChanged(status)
        }
    }

    // =========================================================================
    // Testing and Verification Hooks
    // =========================================================================

    /**
     * Test mechanism to defeat an active enemy without towers (e.g. for victory testing).
     */
    fun defeatEnemy(enemyId: String): Boolean {
        val enemy = activeEnemies.find { it.id == enemyId } ?: return false
        val killed = enemy.takeDamage(enemy.maxHealth)
        if (killed) {
            activeEnemies.remove(enemy)
            listener?.onEnemyDefeated(enemy)
        }
        return killed
    }

    /**
     * Test mechanism to eliminate all currently active enemies (used for automated victory tests).
     */
    fun defeatAllActiveEnemiesForTesting() {
        val listCopy = activeEnemies.toList()
        for (enemy in listCopy) {
            enemy.takeDamage(enemy.maxHealth)
        }
        activeEnemies.clear()
        activeProjectiles.clear()
    }
}
