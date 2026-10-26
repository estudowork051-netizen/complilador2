package com.heroes.towerdefense.game.boss

import com.heroes.towerdefense.game.map.Waypoint
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.EnemyAnimState
import com.heroes.towerdefense.game.model.EnemyVisualType

/**
 * Runtime active instance of an epic Boss moving along the battlefield path.
 * Extends ActiveEnemy for full compatibility with automated tower targeting,
 * projectiles, splash damage, hero basic attacks, and hero active skills.
 *
 * Implements multi-phase boss mechanics, telegraphed special abilities,
 * temporary vulnerability windows, damage protection/evasion modes,
 * and cinematic skippable intro sequences.
 */
class ActiveBoss(
    val bossConfig: BossConfig,
    waveNumber: Int,
    id: String = "boss_${bossConfig.id}_w$waveNumber"
) : ActiveEnemy(
    id = id,
    name = bossConfig.name,
    waveNumber = waveNumber,
    maxHealth = bossConfig.maxHealth,
    currentHealth = bossConfig.maxHealth,
    moveSpeed = bossConfig.moveSpeed,
    visualType = when (bossConfig.type) {
        BossType.OBSIDIAN_GOLEM -> EnemyVisualType.OBSIDIAN_GOLEM
        BossType.SHADOW_QUEEN -> EnemyVisualType.SHADOW_QUEEN
        BossType.ASTRAL_DRAGON -> EnemyVisualType.ASTRAL_DRAGON
    }
) {
    override val isBoss: Boolean = true
    val config: BossConfig
        get() = bossConfig

    var currentPhase: Int = 1
    var isIntroActive: Boolean = true
    var introTimer: Float = bossConfig.introDuration

    var isTelegraphing: Boolean = false
    var telegraphTimer: Float = 0f
    var activeTelegraphAttack: BossAttack? = null

    var isVulnerable: Boolean = false
    var vulnerabilityTimer: Float = 0f

    var isProtected: Boolean = false
    var protectionTimer: Float = 0f

    var attackCooldownTimer: Float = 3.0f // Initial grace period before first attack
    var attackIndexCounter: Int = 0

    var hasTriggeredPhase2: Boolean = false
    var lastAttackExecutedName: String? = null
    var totalAttacksExecuted: Int = 0
    var totalDamageTaken: Int = 0

    val currentEffectiveMoveSpeed: Float
        get() {
            if (isIntroActive || !isAlive) return 0f
            // Astral Dragon enraged in Phase 2 moves 40% faster
            return if (bossConfig.type == BossType.ASTRAL_DRAGON && currentPhase == 2) {
                bossConfig.moveSpeed * 1.40f
            } else {
                bossConfig.moveSpeed
            }
        }

    /**
     * Finishes or skips the boss cinematic intro, officially starting combat.
     */
    fun finishIntro() {
        if (isIntroActive) {
            isIntroActive = false
            introTimer = 0f
        }
    }

    /**
     * Updates cinematic intro timer. Returns true if intro finished this frame.
     */
    fun updateIntro(deltaTime: Float): Boolean {
        if (!isIntroActive) return false
        introTimer -= deltaTime
        if (introTimer <= 0f) {
            finishIntro()
            return true
        }
        return false
    }

    /**
     * Activates a temporary vulnerability window (e.g. after heavy slam).
     */
    fun triggerVulnerability(duration: Float = bossConfig.vulnerabilityDuration) {
        if (!isAlive) return
        isVulnerable = true
        vulnerabilityTimer = duration
    }

    /**
     * Activates a temporary protection/evasion window (e.g. Shadow Veil).
     */
    fun triggerProtection(duration: Float = bossConfig.protectionDuration) {
        if (!isAlive) return
        isProtected = true
        protectionTimer = duration
    }

    /**
     * Initiates a telegraphed attack warning before execution.
     */
    fun startTelegraph(attack: BossAttack) {
        if (!isAlive || isIntroActive) return
        isTelegraphing = true
        activeTelegraphAttack = attack
        telegraphTimer = attack.telegraphDuration
        animState = EnemyAnimState.SPECIAL
        stateTimer = 0f
    }

    /**
     * Cancels active telegraph (e.g. upon defeat).
     */
    fun cancelTelegraph() {
        isTelegraphing = false
        activeTelegraphAttack = null
        telegraphTimer = 0f
        if (animState == EnemyAnimState.SPECIAL) {
            animState = if (currentEffectiveMoveSpeed > 0f) EnemyAnimState.WALK else EnemyAnimState.IDLE
        }
    }

    /**
     * Deals damage to the boss, taking into account resistances and vulnerability multipliers.
     * Enforces strict boundaries: currentHealth never drops below 0 and never exceeds maxHealth.
     * Cancels active attacks and telegraphs immediately upon death.
     */
    override fun takeDamage(damage: Int): Boolean {
        if (!isAlive || isIntroActive) return false
        require(damage >= 0) { "Damage cannot be negative" }

        val finalDamage = if (damage >= maxHealth) {
            damage
        } else {
            var effectiveDamage = damage.toFloat()

            if (isProtected) {
                effectiveDamage *= bossConfig.protectionDamageMultiplier
            } else if (isVulnerable) {
                effectiveDamage *= bossConfig.vulnerabilityDamageMultiplier
            } else if (bossConfig.physicalResistance > 0f) {
                effectiveDamage *= (1.0f - bossConfig.physicalResistance)
            }

            effectiveDamage.toInt().coerceAtLeast(1)
        }

        val wasAlive = isAlive
        currentHealth = (currentHealth - finalDamage).coerceIn(0, maxHealth)
        totalDamageTaken += finalDamage

        // Phase 2 transition check for Astral Dragon at 50% maxHealth
        var triggeredPhaseChange = false
        if (bossConfig.type == BossType.ASTRAL_DRAGON && !hasTriggeredPhase2 && healthFraction <= 0.50f) {
            hasTriggeredPhase2 = true
            currentPhase = 2
            animState = EnemyAnimState.PHASE_TRANSITION
            stateTimer = 0f
            triggeredPhaseChange = true
        }

        if (currentHealth == 0) {
            isAlive = false
            cancelTelegraph()
            isVulnerable = false
            isProtected = false
            triggerDeath(0.80f)
            return wasAlive
        } else if (finalDamage > 0 && !triggeredPhaseChange && animState != EnemyAnimState.PHASE_TRANSITION) {
            triggerHit(0.25f)
        }

        return false
    }

    /**
     * Updates boss state, timers, movement, and attack sequencing.
     * Returns a [BossAttack] if a telegraphed attack has completed charging and is ready to execute.
     */
    fun updateBoss(deltaTime: Float, waypoints: List<Waypoint>): BossAttack? {
        if (!isAlive) return null

        animTimer += deltaTime
        stateTimer += deltaTime

        if (isIntroActive) {
            updateIntro(deltaTime)
            return null
        }

        // 1. Update Movement along path
        if (!hasReachedBase && currentWaypointIndex < waypoints.size - 1) {
            // Apply speed adjustment for current phase
            val speedBackup = currentEffectiveMoveSpeed
            var distanceToMove = speedBackup * deltaTime

            while (distanceToMove > 0f && currentWaypointIndex < waypoints.size - 1) {
                val targetWaypoint = waypoints[currentWaypointIndex + 1]
                val dx = targetWaypoint.x - x
                val dy = targetWaypoint.y - y
                val segmentDistance = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (dx > 0.005f) {
                    facingDirection = 1f
                } else if (dx < -0.005f) {
                    facingDirection = -1f
                }

                if (segmentDistance <= 0.0001f) {
                    currentWaypointIndex++
                    if (currentWaypointIndex >= waypoints.size - 1) {
                        hasReachedBase = true
                        break
                    }
                    continue
                }

                if (distanceToMove < segmentDistance) {
                    val ratio = distanceToMove / segmentDistance
                    x += dx * ratio
                    y += dy * ratio
                    distanceTraveled += distanceToMove
                    distanceToMove = 0f
                } else {
                    x = targetWaypoint.x
                    y = targetWaypoint.y
                    distanceTraveled += segmentDistance
                    distanceToMove -= segmentDistance
                    currentWaypointIndex++

                    if (currentWaypointIndex >= waypoints.size - 1) {
                        hasReachedBase = true
                        break
                    }
                }
            }
        }

        // 2. Update Vulnerability Window
        if (isVulnerable) {
            vulnerabilityTimer -= deltaTime
            if (vulnerabilityTimer <= 0f) {
                isVulnerable = false
                vulnerabilityTimer = 0f
            }
        }

        // 3. Update Protection Mode (Shadow Veil)
        if (isProtected) {
            protectionTimer -= deltaTime
            if (protectionTimer <= 0f) {
                isProtected = false
                protectionTimer = 0f
                // When Shadow Veil drops, Queen is left exposed
                if (bossConfig.type == BossType.SHADOW_QUEEN) {
                    triggerVulnerability(bossConfig.vulnerabilityDuration)
                }
            }
        }

        // 4. Update Telegraphed Attack or Cooldown
        if (isTelegraphing) {
            telegraphTimer -= deltaTime
            if (telegraphTimer <= 0f) {
                // Telegraph complete -> Ready to execute!
                val attack = activeTelegraphAttack
                cancelTelegraph()
                if (attack != null) {
                    lastAttackExecutedName = attack.name
                    totalAttacksExecuted++
                    attackCooldownTimer = attack.cooldown
                    return attack
                }
            }
        } else {
            attackCooldownTimer -= deltaTime
            if (attackCooldownTimer <= 0f && bossConfig.attacks.isNotEmpty()) {
                // Select attack based on boss phase and roster
                val availableAttacks = if (bossConfig.type == BossType.ASTRAL_DRAGON && currentPhase == 1) {
                    bossConfig.attacks.filter { it.type != BossAttackType.STARFALL }
                } else {
                    bossConfig.attacks
                }

                if (availableAttacks.isNotEmpty()) {
                    val selectedAttack = availableAttacks[attackIndexCounter % availableAttacks.size]
                    attackIndexCounter++
                    startTelegraph(selectedAttack)
                }
            }
        }

        return null
    }
}
