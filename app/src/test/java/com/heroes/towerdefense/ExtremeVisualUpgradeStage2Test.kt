package com.heroes.towerdefense

import com.heroes.towerdefense.game.model.Character3DModelRegistry
import com.heroes.towerdefense.game.model.EnemyAnimState
import com.heroes.towerdefense.game.model.EnemyVisualType
import com.heroes.towerdefense.game.model.HeroAnimState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test Suite for Heroes Tower Defense — Extreme Visual Upgrade (Etapa 2/3):
 * 3D Models, Articulated Characters, and Detailed Towers.
 *
 * Verifies:
 * 1. Visual identities and 3D models for all 6 canonical heroes (Kael, Lyra, Brakk, Nyra, Tiko, Vesper).
 * 2. Articulated skeletal hierarchy nodes and weapon attachments for each hero.
 * 3. Animation state machine contracts (IDLE, WINDUP, STRIKE, RECOVERY, SKILL_CAST, VICTORY).
 * 4. Distinct 3D models and animation cycles for all 4 common enemies (Goblin, Skeleton, Orc, Dark Mage).
 * 5. Epic scale and multi-phase animation support for all 3 bosses (Obsidian Golem, Shadow Queen, Astral Dragon).
 * 6. Detailed 3D geometries and recoil animation contracts for all 3 towers (Archer, Mage, Cannon).
 * 7. Scale superiority of epic bosses over common enemies.
 * 8. Strict uniqueness and path validity for all 3D asset contracts.
 */
class ExtremeVisualUpgradeStage2Test {

    // =========================================================================
    // 1. Six Heroes 3D Archetypes & Visual Identity
    // =========================================================================

    @Test
    fun testAllSixHeroesHaveDistinct3DArchetypesAndModels() {
        val expectedHeroes = listOf("hero_kael", "hero_lyra", "hero_brakk", "hero_nyra", "hero_tiko", "hero_vesper")
        val registered = Character3DModelRegistry.HERO_ARCHETYPES

        assertEquals("Registry must contain exactly 6 heroes", 6, registered.size)

        for (heroId in expectedHeroes) {
            val hero = registered[heroId]
            assertNotNull("Hero archetype must be registered for $heroId", hero)
            assertTrue("Model file must end with .glb", hero!!.modelFileName.endsWith(".glb"))
            assertTrue("Scene path must start with res://scenes/heroes/", hero.scenePath.startsWith("res://scenes/heroes/"))
            assertTrue("Hero must have at least 6 articulated parts", hero.articulatedParts.size >= 6)
            assertTrue("Articulated parts must contain Torso and Head", hero.articulatedParts.contains("Torso") && hero.articulatedParts.contains("Head"))
            assertTrue("Hero must have a distinct weapon specified", hero.weaponType.isNotBlank())
        }
    }

    @Test
    fun testHeroWeaponAttachmentsAndDistinctIdentities() {
        val kael = Character3DModelRegistry.getHeroArchetype("hero_kael")!!
        assertEquals("Kael must wield Flaming Greatsword", "Flaming Greatsword", kael.weaponType)
        assertTrue("Kael must have Cape", kael.articulatedParts.contains("Cape"))

        val lyra = Character3DModelRegistry.getHeroArchetype("hero_lyra")!!
        assertEquals("Lyra must wield Composite Longbow", "Composite Longbow", lyra.weaponType)
        assertTrue("Lyra must have Quiver", lyra.articulatedParts.contains("Quiver"))

        val brakk = Character3DModelRegistry.getHeroArchetype("hero_brakk")!!
        assertEquals("Brakk must wield Colossal Warhammer", "Colossal Warhammer", brakk.weaponType)
        assertTrue("Brakk must have imposing scale factor > 1.1", brakk.scaleFactor >= 1.1f)

        val nyra = Character3DModelRegistry.getHeroArchetype("hero_nyra")!!
        assertEquals("Nyra must wield Twin Shadow Daggers", "Twin Shadow Daggers", nyra.weaponType)
        assertTrue("Nyra must have Capelet", nyra.articulatedParts.contains("Cape"))

        val tiko = Character3DModelRegistry.getHeroArchetype("hero_tiko")!!
        assertTrue("Tiko must have Backpack apparatus", tiko.articulatedParts.contains("Backpack"))

        val vesper = Character3DModelRegistry.getHeroArchetype("hero_vesper")!!
        assertEquals("Vesper must wield Astral Crescent Staff", "Astral Crescent Staff", vesper.weaponType)
        assertTrue("Vesper must have RobeSkirt", vesper.articulatedParts.contains("RobeSkirt"))
    }

    @Test
    fun testHeroAnimationStatesContract() {
        for ((heroId, archetype) in Character3DModelRegistry.HERO_ARCHETYPES) {
            val anims = archetype.supportedAnimations
            assertTrue("$heroId must support IDLE", anims.contains(HeroAnimState.IDLE))
            assertTrue("$heroId must support WINDUP", anims.contains(HeroAnimState.WINDUP))
            assertTrue("$heroId must support STRIKE", anims.contains(HeroAnimState.STRIKE))
            assertTrue("$heroId must support RECOVERY", anims.contains(HeroAnimState.RECOVERY))
            assertTrue("$heroId must support SKILL_CAST", anims.contains(HeroAnimState.SKILL_CAST))
            assertTrue("$heroId must support VICTORY", anims.contains(HeroAnimState.VICTORY))
        }

        // Validate HeroAnimState isAttacking helper
        assertTrue(HeroAnimState.WINDUP.isAttacking)
        assertTrue(HeroAnimState.STRIKE.isAttacking)
        assertTrue(HeroAnimState.RECOVERY.isAttacking)
        assertFalse(HeroAnimState.IDLE.isAttacking)
        assertFalse(HeroAnimState.VICTORY.isAttacking)
    }

    // =========================================================================
    // 2. Common Enemies 3D Archetypes & Animation Cycles
    // =========================================================================

    @Test
    fun testAllFourCommonEnemiesHaveDistinct3DModels() {
        val commonEnemies = listOf(
            EnemyVisualType.GOBLIN,
            EnemyVisualType.SKELETON,
            EnemyVisualType.ORC,
            EnemyVisualType.DARK_MAGE
        )

        for (enemyType in commonEnemies) {
            val arch = Character3DModelRegistry.getEnemyArchetype(enemyType)
            assertNotNull("Common enemy must exist in registry: $enemyType", arch)
            assertFalse("$enemyType must not be flagged as boss", arch!!.isBoss)
            assertTrue("Model file must end with .glb", arch.modelFileName.endsWith(".glb"))
            assertTrue("Scene path must start with res://scenes/enemies/", arch.scenePath.startsWith("res://scenes/enemies/"))
            assertTrue("Scale factor must be realistic mobile scale (<= 1.2f)", arch.scaleFactor <= 1.2f)
            assertTrue("Must support WALK animation", arch.supportedAnimations.contains(EnemyAnimState.WALK))
            assertTrue("Must support ATTACK animation", arch.supportedAnimations.contains(EnemyAnimState.ATTACK))
            assertTrue("Must support HIT animation", arch.supportedAnimations.contains(EnemyAnimState.HIT))
            assertTrue("Must support DEATH animation", arch.supportedAnimations.contains(EnemyAnimState.DEATH))
        }
    }

    // =========================================================================
    // 3. Epic Bosses 3D Archetypes & Scale Superiority
    // =========================================================================

    @Test
    fun testAllThreeEpicBossesHaveSuperiorVisualScaleAndSpecialStates() {
        val bosses = listOf(
            EnemyVisualType.OBSIDIAN_GOLEM,
            EnemyVisualType.SHADOW_QUEEN,
            EnemyVisualType.ASTRAL_DRAGON
        )

        for (bossType in bosses) {
            val arch = Character3DModelRegistry.getEnemyArchetype(bossType)
            assertNotNull("Boss must exist in registry: $bossType", arch)
            assertTrue("$bossType must be flagged as boss", arch!!.isBoss)
            assertTrue("Boss scene must start with res://scenes/bosses/", arch.scenePath.startsWith("res://scenes/bosses/"))
            assertTrue("Boss scale factor must be imposing (>= 1.7f)", arch.scaleFactor >= 1.7f)
            assertTrue("Boss must support SPECIAL attack", arch.supportedAnimations.contains(EnemyAnimState.SPECIAL))
            assertTrue("Boss must support PHASE_TRANSITION", arch.supportedAnimations.contains(EnemyAnimState.PHASE_TRANSITION))
        }

        val golem = Character3DModelRegistry.getEnemyArchetype(EnemyVisualType.OBSIDIAN_GOLEM)!!
        assertEquals(2.2f, golem.scaleFactor, 0.01f)

        val queen = Character3DModelRegistry.getEnemyArchetype(EnemyVisualType.SHADOW_QUEEN)!!
        assertEquals(1.7f, queen.scaleFactor, 0.01f)
        assertTrue("Queen must have Wings", queen.articulatedParts.contains("Wings"))

        val dragon = Character3DModelRegistry.getEnemyArchetype(EnemyVisualType.ASTRAL_DRAGON)!!
        assertEquals(2.5f, dragon.scaleFactor, 0.01f)
        assertTrue("Dragon must have Wings and Tail", dragon.articulatedParts.contains("Wings") && dragon.articulatedParts.contains("Tail"))
    }

    @Test
    fun testBossScaleStrictlyExceedsCommonEnemyScale() {
        val maxCommonScale = Character3DModelRegistry.ENEMY_ARCHETYPES.values
            .filter { !it.isBoss }
            .maxOf { it.scaleFactor }

        val minBossScale = Character3DModelRegistry.ENEMY_ARCHETYPES.values
            .filter { it.isBoss }
            .minOf { it.scaleFactor }

        assertTrue(
            "Every epic boss must be significantly larger than any common enemy ($minBossScale > $maxCommonScale)",
            minBossScale > maxCommonScale * 1.4f
        )
    }

    // =========================================================================
    // 4. Detailed Towers 3D Archetypes & Recoil Contracts
    // =========================================================================

    @Test
    fun testAllThreeDetailedTowersHaveDistinctGeometriesAndRecoil() {
        val expectedTowers = listOf("ARCHER", "MAGE", "CANNON")

        for (towerType in expectedTowers) {
            val tower = Character3DModelRegistry.getTowerArchetype(towerType)
            assertNotNull("Detailed tower must exist in registry: $towerType", tower)
            assertTrue("Model file must end with .glb", tower!!.modelFileName.endsWith(".glb"))
            assertTrue("Scene path must start with res://scenes/", tower.scenePath.startsWith("res://scenes/"))
            assertTrue("Must contain at least 4 detailed geometric components", tower.detailedComponents.size >= 4)
        }

        val archer = Character3DModelRegistry.getTowerArchetype("ARCHER")!!
        assertTrue("Archer tower must have recoil kickback", archer.hasRecoilAnimation)
        assertTrue("Archer tower must have battlement platform", archer.detailedComponents.contains("BattlementPlatform"))

        val mage = Character3DModelRegistry.getTowerArchetype("MAGE")!!
        assertFalse("Mage tower uses continuous arcane crystal levitation rather than recoil", mage.hasRecoilAnimation)
        assertTrue("Mage tower must have floating focus crystal", mage.detailedComponents.contains("FocusCrystal"))

        val cannon = Character3DModelRegistry.getTowerArchetype("CANNON")!!
        assertTrue("Cannon tower must have heavy recoil kickback", cannon.hasRecoilAnimation)
        assertTrue("Cannon tower must have swivel turret chassis and barrel", cannon.detailedComponents.contains("TurretChassis") && cannon.detailedComponents.contains("CannonBarrel"))
    }

    // =========================================================================
    // 5. Uniqueness of 3D Assets across System
    // =========================================================================

    @Test
    fun testAllModelFileNamesAreUniqueAcrossHeroesEnemiesBossesAndTowers() {
        val heroModels = Character3DModelRegistry.HERO_ARCHETYPES.values.map { it.modelFileName }
        val enemyModels = Character3DModelRegistry.ENEMY_ARCHETYPES.values.map { it.modelFileName }
        val towerModels = Character3DModelRegistry.TOWER_ARCHETYPES.values.map { it.modelFileName }

        val allModels = heroModels + enemyModels + towerModels
        val uniqueModels = allModels.toSet()

        assertEquals("All 16 new 3D model filenames must be unique", allModels.size, uniqueModels.size)
        assertEquals("Total registered 3D models must be 16 (6 heroes + 7 enemies/bosses + 3 towers)", 16, allModels.size)
    }
}
