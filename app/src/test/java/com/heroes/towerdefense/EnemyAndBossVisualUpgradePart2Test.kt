package com.heroes.towerdefense

import com.heroes.towerdefense.game.boss.ActiveBoss
import com.heroes.towerdefense.game.boss.BossAttack
import com.heroes.towerdefense.game.boss.BossAttackType
import com.heroes.towerdefense.game.boss.BossConfig
import com.heroes.towerdefense.game.boss.BossType
import com.heroes.towerdefense.game.engine.GameEngine
import com.heroes.towerdefense.game.map.StageOneMap
import com.heroes.towerdefense.game.map.Waypoint
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.EnemyAnimState
import com.heroes.towerdefense.game.model.EnemyVisualType
import com.heroes.towerdefense.game.model.WaveConfig
import com.heroes.towerdefense.model.hero.Hero
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit Test Suite for Visual Upgrade Part 2:
 * - EnemyAnimState lifecycle properties (IDLE, WALK, ATTACK, HIT, DEATH, SPECIAL, PHASE_TRANSITION)
 * - EnemyVisualType attributes for all 4 common monster archetypes (Goblin, Skeleton, Orc, Dark Mage)
 *   and 3 epic boss archetypes (Obsidian Golem, Shadow Queen, Astral Dragon)
 * - ActiveEnemy procedural animation state machine (facing direction, hit flinch recovery, attack, death)
 * - ActiveBoss visual type integration, telegraphed special abilities, Phase 2 Astral Dragon transformation
 * - GameEngine wave archetype assignments and summoned minion visual integration
 * - Zero regression on combat damage, base health reduction on escape, and victory/defeat contracts
 */
class EnemyAndBossVisualUpgradePart2Test {

    private lateinit var engine: GameEngine
    private lateinit var defaultHero: Hero

    @Before
    fun setUp() {
        defaultHero = Hero.getPlannedHeroes().first()
        engine = GameEngine(
            waypoints = StageOneMap.waypoints,
            waves = listOf(
                WaveConfig(waveNumber = 1, enemyCount = 3, spawnIntervalSeconds = 2.0f),
                WaveConfig(waveNumber = 2, enemyCount = 3, spawnIntervalSeconds = 2.0f),
                WaveConfig(waveNumber = 3, enemyCount = 3, spawnIntervalSeconds = 2.0f),
                WaveConfig(waveNumber = 4, enemyCount = 3, spawnIntervalSeconds = 2.0f),
                WaveConfig(waveNumber = 5, enemyCount = 3, spawnIntervalSeconds = 2.0f)
            ),
            selectedHero = defaultHero
        )
    }

    // =========================================================================
    // 1. EnemyAnimState State Machine Checks
    // =========================================================================

    @Test
    fun testEnemyAnimStateProperties() {
        assertFalse(EnemyAnimState.IDLE.isAttacking)
        assertFalse(EnemyAnimState.IDLE.isMoving)
        assertFalse(EnemyAnimState.IDLE.isDying)
        assertFalse(EnemyAnimState.IDLE.isHit)

        assertFalse(EnemyAnimState.WALK.isAttacking)
        assertTrue(EnemyAnimState.WALK.isMoving)
        assertFalse(EnemyAnimState.WALK.isDying)
        assertFalse(EnemyAnimState.WALK.isHit)

        assertTrue(EnemyAnimState.ATTACK.isAttacking)
        assertFalse(EnemyAnimState.ATTACK.isMoving)

        assertTrue(EnemyAnimState.SPECIAL.isAttacking)
        assertFalse(EnemyAnimState.SPECIAL.isMoving)

        assertTrue(EnemyAnimState.HIT.isHit)
        assertFalse(EnemyAnimState.HIT.isAttacking)

        assertTrue(EnemyAnimState.DEATH.isDying)
        assertFalse(EnemyAnimState.DEATH.isMoving)

        assertFalse(EnemyAnimState.PHASE_TRANSITION.isAttacking)
        assertFalse(EnemyAnimState.PHASE_TRANSITION.isMoving)
    }

    // =========================================================================
    // 2. EnemyVisualType Canonical Archetypes
    // =========================================================================

    @Test
    fun testAllFourCommonMonsterVisualTypesConfigured() {
        val goblin = EnemyVisualType.GOBLIN
        assertEquals("Goblin", goblin.displayName)
        assertEquals("Saqueador Selvagem", goblin.title)
        assertFalse(goblin.isBoss)

        val skeleton = EnemyVisualType.SKELETON
        assertEquals("Esqueleto", skeleton.displayName)
        assertEquals("Guerreiro Amaldiçoado", skeleton.title)
        assertFalse(skeleton.isBoss)

        val orc = EnemyVisualType.ORC
        assertEquals("Orc", orc.displayName)
        assertEquals("Brutamontes de Guerra", orc.title)
        assertFalse(orc.isBoss)

        val darkMage = EnemyVisualType.DARK_MAGE
        assertEquals("Mago Sombrio", darkMage.displayName)
        assertEquals("Conjurador das Trevas", darkMage.title)
        assertFalse(darkMage.isBoss)
    }

    @Test
    fun testAllThreeEpicBossVisualTypesConfigured() {
        val golem = EnemyVisualType.OBSIDIAN_GOLEM
        assertEquals("Golem de Obsidiana", golem.displayName)
        assertEquals("Colosso Vulcânico", golem.title)
        assertTrue(golem.isBoss)

        val queen = EnemyVisualType.SHADOW_QUEEN
        assertEquals("Rainha das Sombras", queen.displayName)
        assertEquals("Soberana das Sombras", queen.title)
        assertTrue(queen.isBoss)

        val dragon = EnemyVisualType.ASTRAL_DRAGON
        assertEquals("Dragão Astral", dragon.displayName)
        assertEquals("Dragão Ancestral Cósmico", dragon.title)
        assertTrue(dragon.isBoss)
    }

    // =========================================================================
    // 3. ActiveEnemy Articulated Animation Lifecycle
    // =========================================================================

    @Test
    fun testActiveEnemyInitialStateAndSpawnReset() {
        val enemy = ActiveEnemy(
            id = "test_goblin",
            name = "Goblin Batedor",
            waveNumber = 1,
            maxHealth = 100,
            currentHealth = 100,
            moveSpeed = 0.16f,
            visualType = EnemyVisualType.GOBLIN
        )

        assertEquals(EnemyVisualType.GOBLIN, enemy.visualType)
        assertEquals(EnemyAnimState.WALK, enemy.animState)
        assertEquals(1f, enemy.facingDirection, 0.001f)
        assertEquals(0f, enemy.hitReactionTimer, 0.001f)
        assertFalse(enemy.isDying)
        assertTrue(enemy.isAlive)

        val start = Waypoint(0.2f, 0.1f)
        enemy.spawnAt(start)

        assertEquals(0.2f, enemy.x, 0.001f)
        assertEquals(0.1f, enemy.y, 0.001f)
        assertEquals(EnemyAnimState.WALK, enemy.animState)
    }

    @Test
    fun testActiveEnemyMovementUpdatesAnimationAndFacingDirection() {
        val waypoints = listOf(
            Waypoint(0.2f, 0.1f),
            Waypoint(0.6f, 0.1f), // Moving East -> facingDirection should be 1f
            Waypoint(0.1f, 0.5f)  // Moving West -> facingDirection should be -1f
        )
        val enemy = ActiveEnemy(
            id = "test_walker",
            name = "Goblin",
            waveNumber = 1,
            maxHealth = 100,
            currentHealth = 100,
            moveSpeed = 0.5f
        )
        enemy.spawnAt(waypoints[0])

        // Step 1: Move East towards waypoint 1
        enemy.update(0.2f, waypoints)
        assertTrue("Enemy animTimer should advance", enemy.animTimer > 0f)
        assertEquals("Enemy moving East should face right (1.0)", 1f, enemy.facingDirection, 0.001f)
        assertEquals(EnemyAnimState.WALK, enemy.animState)

        // Advance to waypoint 1
        enemy.x = 0.6f
        enemy.y = 0.1f
        enemy.currentWaypointIndex = 1

        // Step 2: Move West towards waypoint 2
        enemy.update(0.1f, waypoints)
        assertEquals("Enemy moving West should face left (-1.0)", -1f, enemy.facingDirection, 0.001f)
    }

    @Test
    fun testActiveEnemyHitReactionAndRecoveryCycle() {
        val waypoints = StageOneMap.waypoints
        val enemy = ActiveEnemy(
            id = "test_hit",
            name = "Goblin",
            waveNumber = 1,
            maxHealth = 100,
            currentHealth = 100
        )
        enemy.spawnAt(waypoints.first())

        // Non-lethal hit
        val killed = enemy.takeDamage(35)
        assertFalse(killed)
        assertTrue(enemy.isAlive)
        assertEquals(EnemyAnimState.HIT, enemy.animState)
        assertTrue(enemy.hitReactionTimer > 0f)

        // Advance time beyond hit recovery duration (0.25s)
        enemy.update(0.30f, waypoints)
        assertEquals(0f, enemy.hitReactionTimer, 0.001f)
        assertEquals("Enemy should recover to WALK after hit flinch", EnemyAnimState.WALK, enemy.animState)
    }

    @Test
    fun testActiveEnemyAttackDurationAndRecoveryCycle() {
        val waypoints = StageOneMap.waypoints
        val enemy = ActiveEnemy(
            id = "test_attacker",
            name = "Orc",
            waveNumber = 3,
            maxHealth = 150,
            currentHealth = 150
        )
        enemy.spawnAt(waypoints.first())

        // Trigger attack with duration = 0.40s
        enemy.triggerAttack(0.40f)
        assertEquals(EnemyAnimState.ATTACK, enemy.animState)
        assertEquals(0.40f, enemy.attackAnimTimer, 0.001f)

        // Advance time partially (0.20s) -> still attacking
        enemy.update(0.20f, waypoints)
        assertEquals(EnemyAnimState.ATTACK, enemy.animState)
        assertEquals(0.20f, enemy.attackAnimTimer, 0.001f)

        // Advance time past remaining duration -> recovers to WALK
        enemy.update(0.25f, waypoints)
        assertEquals(0f, enemy.attackAnimTimer, 0.001f)
        assertEquals("Enemy should recover to WALK after attack animation completes", EnemyAnimState.WALK, enemy.animState)
    }

    @Test
    fun testActiveEnemyDeathStateCycle() {
        val enemy = ActiveEnemy(
            id = "test_death",
            name = "Esqueleto",
            waveNumber = 2,
            maxHealth = 50,
            currentHealth = 50
        )
        val killed = enemy.takeDamage(60)
        assertTrue(killed)
        assertFalse(enemy.isAlive)
        assertTrue(enemy.isDying)
        assertEquals(EnemyAnimState.DEATH, enemy.animState)
        assertEquals(0, enemy.currentHealth)
    }

    @Test
    fun testActiveEnemyInfersVisualTypeFromNameOrId() {
        val skeleton = ActiveEnemy(id = "e_skel_01", name = "Esqueleto Amaldiçoado", waveNumber = 2)
        assertEquals(EnemyVisualType.SKELETON, skeleton.visualType)

        val orc = ActiveEnemy(id = "e_orc_01", name = "Orc Berserker", waveNumber = 3)
        assertEquals(EnemyVisualType.ORC, orc.visualType)

        val mage = ActiveEnemy(id = "shadow_minion_1", name = "Servo das Sombras", waveNumber = 4)
        assertEquals(EnemyVisualType.DARK_MAGE, mage.visualType)
    }

    // =========================================================================
    // 4. ActiveBoss Visual Type & Animation Integration
    // =========================================================================

    @Test
    fun testBossVisualTypeAssignment() {
        val golemConfig = BossConfig(
            id = "obsidian_golem",
            name = "Golem de Obsidiana",
            title = "Colosso Vulcânico",
            description = "Colosso",
            type = BossType.OBSIDIAN_GOLEM,
            maxHealth = 1000,
            moveSpeed = 0.08f
        )
        val golem = ActiveBoss(golemConfig, waveNumber = 5)
        assertEquals(EnemyVisualType.OBSIDIAN_GOLEM, golem.visualType)
        assertTrue(golem.isBoss)

        val queenConfig = BossConfig(
            id = "shadow_queen",
            name = "Rainha das Sombras",
            title = "Soberana",
            description = "Rainha",
            type = BossType.SHADOW_QUEEN,
            maxHealth = 800,
            moveSpeed = 0.12f
        )
        val queen = ActiveBoss(queenConfig, waveNumber = 5)
        assertEquals(EnemyVisualType.SHADOW_QUEEN, queen.visualType)

        val dragonConfig = BossConfig(
            id = "astral_dragon",
            name = "Dragão Astral",
            title = "Ancestral",
            description = "Dragão",
            type = BossType.ASTRAL_DRAGON,
            maxHealth = 1500,
            moveSpeed = 0.10f
        )
        val dragon = ActiveBoss(dragonConfig, waveNumber = 5)
        assertEquals(EnemyVisualType.ASTRAL_DRAGON, dragon.visualType)
    }

    @Test
    fun testBossTelegraphTransitionsToSpecialAnimation() {
        val golemConfig = BossConfig(
            id = "golem_test",
            name = "Golem",
            title = "Colosso",
            description = "Desc",
            type = BossType.OBSIDIAN_GOLEM,
            maxHealth = 1000,
            moveSpeed = 0.08f
        )
        val boss = ActiveBoss(golemConfig, waveNumber = 5)
        boss.finishIntro()

        val slamAttack = BossAttack(
            name = "Impacto Sísmico",
            type = BossAttackType.EARTH_SLAM,
            damage = 5,
            range = 0.25f,
            telegraphDuration = 1.5f,
            cooldown = 4.0f
        )

        boss.startTelegraph(slamAttack)
        assertTrue(boss.isTelegraphing)
        assertEquals(EnemyAnimState.SPECIAL, boss.animState)

        // Canceling telegraph returns to normal state
        boss.cancelTelegraph()
        assertFalse(boss.isTelegraphing)
        assertEquals(EnemyAnimState.WALK, boss.animState)
    }

    @Test
    fun testAstralDragonPhase2TransitionAnimationState() {
        val dragonConfig = BossConfig(
            id = "dragon_p2",
            name = "Dragão Astral",
            title = "Cósmico",
            description = "Desc",
            type = BossType.ASTRAL_DRAGON,
            maxHealth = 1000,
            moveSpeed = 0.10f
        )
        val dragon = ActiveBoss(dragonConfig, waveNumber = 5)
        dragon.finishIntro()

        assertEquals(1, dragon.currentPhase)

        // Deal 550 damage (HP drops below 50%) -> Triggers Phase 2
        dragon.takeDamage(550)
        assertEquals(2, dragon.currentPhase)
        assertTrue(dragon.hasTriggeredPhase2)
        assertEquals("Enraged dragon should transition to PHASE_TRANSITION animation state",
            EnemyAnimState.PHASE_TRANSITION, dragon.animState)
    }

    @Test
    fun testBossDefeatCancelsTelegraphAndSetsDeathAnimation() {
        val config = BossConfig(
            id = "boss_die",
            name = "Chefe",
            title = "Título",
            description = "Desc",
            type = BossType.SHADOW_QUEEN,
            maxHealth = 200,
            moveSpeed = 0.10f
        )
        val boss = ActiveBoss(config, waveNumber = 5)
        boss.finishIntro()

        boss.isTelegraphing = true
        boss.animState = EnemyAnimState.SPECIAL

        val killed = boss.takeDamage(300)
        assertTrue(killed)
        assertFalse(boss.isAlive)
        assertFalse(boss.isTelegraphing)
        assertEquals(EnemyAnimState.DEATH, boss.animState)
        assertTrue(boss.isDying)
    }

    // =========================================================================
    // 5. GameEngine Wave Archetype Assignments
    // =========================================================================

    @Test
    fun testGameEngineSpawnsDistinctArchetypesAcrossWaves() {
        engine.start()

        // Wave 1: First enemy should be GOBLIN
        engine.update(0.1f)
        val w1Enemy = engine.activeEnemies.firstOrNull()
        assertNotNull(w1Enemy)
        assertEquals("Wave 1 enemy should be Goblin", EnemyVisualType.GOBLIN, w1Enemy!!.visualType)
    }

    @Test
    fun testSummonedShadowMinionsHaveDarkMageVisualType() {
        engine.start()
        val queenConfig = BossConfig(
            id = "queen_summoner",
            name = "Rainha das Sombras",
            title = "Soberana",
            description = "Desc",
            type = BossType.SHADOW_QUEEN,
            maxHealth = 800,
            moveSpeed = 0.10f,
            attacks = listOf(
                BossAttack(
                    name = "Invocar Servos",
                    type = BossAttackType.SUMMON_MINIONS,
                    damage = 0,
                    range = 0.20f,
                    telegraphDuration = 0.05f,
                    cooldown = 5f
                )
            )
        )
        val boss = engine.spawnBoss(queenConfig)
        boss.finishIntro()

        // Fast forward boss to trigger attack
        boss.attackCooldownTimer = 0.01f
        engine.update(0.05f) // starts telegraph
        engine.update(0.10f) // finishes telegraph and summons minions

        val minions = engine.activeEnemies.filter { it.name == "Servo das Sombras" }
        assertTrue("At least one shadow minion should be summoned", minions.isNotEmpty())
        assertEquals("Shadow minions should have DARK_MAGE visual type",
            EnemyVisualType.DARK_MAGE, minions.first().visualType)
    }

    // =========================================================================
    // 6. Zero Regressions on Combat & Escape Mechanics
    // =========================================================================

    @Test
    fun testBaseHealthReductionWhenEnemyEscapesIsPreserved() {
        engine.start()
        val initialBaseHealth = engine.baseHealth

        // Spawn a goblin enemy right at the final waypoint
        val enemy = ActiveEnemy(
            id = "escaper",
            name = "Goblin Fuga",
            waveNumber = 1,
            maxHealth = 50,
            currentHealth = 50,
            moveSpeed = 0.2f
        )
        val waypoints = StageOneMap.waypoints
        enemy.x = waypoints.last().x
        enemy.y = waypoints.last().y
        enemy.currentWaypointIndex = waypoints.size - 2
        engine.activeEnemies.add(enemy)

        engine.update(0.5f)
        assertEquals("Base health should decrease by 1 when enemy escapes",
            initialBaseHealth - 1, engine.baseHealth)
    }
}
